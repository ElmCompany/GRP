package sa.elm.ob.hcm.ad_process.EmployeeSecondment;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DbUtility;

import sa.elm.ob.hcm.EHCMEmpSecondment;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.event.dao.EmpSecondmentEventDAO;
import sa.elm.ob.hcm.event.dao.EmpSecondmentEventDAOImpl;
import sa.elm.ob.hcm.util.Utility;

public class EmpSecondment implements Process {
  private static final Logger log = Logger.getLogger(EmpSecondment.class);
  private final OBError obError = new OBError();
  public static String DECISION_TYPE_CANCEL = DecisionTypeConstants.DECISION_TYPE_CANCEL;
  public static String DECISION_TYPE_CUTOFF = DecisionTypeConstants.DECISION_TYPE_CUTOFF;
  EmpSecondmentEventDAO empSecondmentEventDAO = new EmpSecondmentEventDAOImpl();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Issue the EmpSecondment");

    final String secondmentId = (String) bundle.getParams().get("Ehcm_Emp_Secondment_ID")
        .toString();
    EHCMEmpSecondment secondment = OBDal.getInstance().get(EHCMEmpSecondment.class, secondmentId);
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    EmploymentInfo info = null;
    log.debug("secondmentId:" + secondmentId);
    EmpSecondmentDAO empSecondmentDAOImpl = new EmpSecondmentDAOImpl();
    EmploymentInfo newEmployInfo = null;
    EmploymentInfo recentEmpInfo = null;
    EmploymentInfo activeEmployInfo = null;
    String decType = secondment.getDecisionType();
    Boolean yearValidation = false;
    try {
      log.debug("isSueDecision:" + secondment.isSueDecision());
      log.debug("getDecisionType:" + secondment.getDecisionType());
      OBContext.setAdminMode(true);

      // check Issued or not
      if (!secondment.isSueDecision()) {
        /* six year validation */
        if (!decType.equals(DECISION_TYPE_CANCEL) && !decType.equals(DECISION_TYPE_CUTOFF)) {
          yearValidation = empSecondmentEventDAO.YearValidationForSecondment(secondment, decType,
              Constants.SecondmentMaxYear);
          if (yearValidation == Boolean.FALSE) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("EHCM_EmpSecMoreThanSixYr"));
            bundle.setResult(obError);
            return;
          }
        }

        // check original decision no is issued or not

        if (secondment.getOriginalDecisionsNo() != null
            && !secondment.getOriginalDecisionsNo().isSueDecision()) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_SecOrgDecNo_NotProcessed"));
          bundle.setResult(obError);
          return;
        }
        activeEmployInfo = empSecondmentDAOImpl
            .getPreviousEmployInfo(secondment.getEhcmEmpPerinfo().getId());
        // checking empsecondment startdate should not be lesser than empinfo startdate
        if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
            || secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
          activeEmployInfo = sa.elm.ob.hcm.util.UtilityDAO
              .getActiveEmployInfo(secondment.getEhcmEmpPerinfo().getId());
        } else if (secondment.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
          activeEmployInfo = empSecondmentDAOImpl
              .getPreviousEmployInfo(secondment.getEhcmEmpPerinfo().getId());
        }

        if (activeEmployInfo != null) {
          if (secondment.getStartDate().compareTo(activeEmployInfo.getStartDate()) < 0) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("EHCM_EmpTransfer_StartDate"));
            bundle.setResult(obError);
            return;
          }
        }

        // checking decision overlap
        if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
            || (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                || secondment.getDecisionType()
                    .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND))) {
          JSONObject result = Utility.chkDecisionOverlap(Constants.SECONDMENT_OVERLAP,
              sa.elm.ob.utility.util.Utility.formatDate(secondment.getStartDate()),
              sa.elm.ob.utility.util.Utility.formatDate(secondment.getEndDate()),
              secondment.getEhcmEmpPerinfo().getId(), null, secondment.getId());
          if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
            if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
                || (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                    && (result.has("secondmentId") && !result.getString("secondmentId")
                        .equals(secondment.getOriginalDecisionsNo().getId()))
                    || !result.has("secondmentId"))) {
              if (result.has("errormsg")) {
                obError.setType("Error");
                obError.setTitle("Error");
                obError.setMessage(OBMessageUtils.messageBD(result.getString("errormsg")));
                bundle.setResult(obError);
                return;
              }
            }
          }
        }

        // get employment Information for getting the values Location,payroll,payscale
        info = empSecondmentDAOImpl.getEmploymentInfo(secondment);

        // Create , update,Extend,CutOff Cases
        if (!secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {

          // insert a employe Info Record
          newEmployInfo = empSecondmentDAOImpl.insertEmploymentRecord(secondment, info, false,
              false, null);

          // Create ,Extend,CutOff Cases
          if (!secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
            // update the endate and active flag for old record.
            empSecondmentDAOImpl.updateEndDateForOldRecord(newEmployInfo, secondment, vars);

            // Extend,CutOff Cases
            if (!secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
              // update old secondment as inactive
              empSecondmentDAOImpl
                  .updateOldSecondmentActiveFlag(secondment.getOriginalDecisionsNo(), false);
            }
          }
          // update case
          else {
            // update the endate and active flag for old record.
            empSecondmentDAOImpl.updateEndDateForOldRecord(newEmployInfo, secondment, vars);
            // update old secondment as inactive
            empSecondmentDAOImpl.updateOldSecondmentActiveFlag(secondment.getOriginalDecisionsNo(),
                false);
          }

          empSecondmentDAOImpl.updateEmploymentStatus(secondment, false);

        }
        // cancel case
        else {

          // update the acive flag='Y' and enddate is null for recently update record
          recentEmpInfo = empSecondmentDAOImpl.updateEndDateForOldRecordInCancel(secondment, vars);
          if (recentEmpInfo != null) {
            empSecondmentDAOImpl.updateOldSecondmentActiveFlag(recentEmpInfo.getEhcmEmpSecondment(),
                true);
            empSecondmentDAOImpl.updateDelegation(recentEmpInfo, secondment);
            empSecondmentDAOImpl.remRecntEmpInfoInCancel(recentEmpInfo, secondment);
            empSecondmentDAOImpl.updateEmploymentStatus(recentEmpInfo.getEhcmEmpSecondment(), true);

          }
          // update old secondment as inactive
          empSecondmentDAOImpl.updateOldSecondmentActiveFlag(secondment.getOriginalDecisionsNo(),
              false);
          secondment.setEnabled(false);
          OBDal.getInstance().save(secondment);
        }
        // update status as Issued and set decision date for all cases
        empSecondmentDAOImpl.updateSecondmentStatus(secondment);
        OBDal.getInstance().flush();
        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_Submit_Process"));
        bundle.setResult(obError);
      }
    } catch (Exception e) {
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      Throwable t = DbUtility.getUnderlyingSQLException(e);
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), t.getMessage());
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
