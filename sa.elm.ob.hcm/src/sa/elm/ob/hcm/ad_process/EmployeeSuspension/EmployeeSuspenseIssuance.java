package sa.elm.ob.hcm.ad_process.EmployeeSuspension;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DbUtility;

import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.EhcmSuspensionEmpV;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.SuspensionReason;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.hcm.util.Utility;

public class EmployeeSuspenseIssuance implements Process {
  /**
   * Employee suspension tracking ,Table(Ehcm_emp_Suspension)
   */
  private static final Logger log = Logger.getLogger(EmployeeSuspenseIssuance.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Issue the Suspension");
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    AssingedOrReleaseEmpInPositionDAO positionEmpHist = new AssingedOrReleaseEmpInPositionDAOImpl();
    final String suspensionId = (String) bundle.getParams().get("Ehcm_Emp_Suspension_ID")
        .toString();
    EmployeeSuspension objSuspension = OBDal.getInstance().get(EmployeeSuspension.class,
        suspensionId);
    EhcmSuspensionEmpV suspensionView = objSuspension.getEhcmEmpPerinfo();
    EmployeeSuspension oldSuspension = objSuspension.getOriginalDecisionNo();
    EHCMEMPTermination oldTermination = null;
    SuspensionReason susReason = null;
    String endDate = null;
    boolean isExist = false;

    // get suspension record termination record
    if (oldSuspension != null) {
      OBQuery<EHCMEMPTermination> oldTerminationQry = OBDal.getInstance().createQuery(
          EHCMEMPTermination.class,
          "as e where e.ehcmEmpSuspension.id='" + oldSuspension.getId() + "'");
      if (oldTerminationQry.list().size() > 0) {
        oldTermination = oldTerminationQry.list().get(0);
      }
    }

    if (objSuspension.getSuspensionType().equals("SUS")) {
      susReason = objSuspension.getSuspensionReason();
    } else {
      susReason = objSuspension.getSuspensionEndReason();
    }
    if (objSuspension.getExpectedEndDate() == null) {
      endDate = "21-06-2058";
    } else {
      endDate = sa.elm.ob.utility.util.Utility.formatDate(objSuspension.getEndDate());
    }
    try {
      OBContext.setAdminMode(true);
      // check whether employee is terminated.
      if (suspensionView.getEmployee().getEmploymentStatus().equals("TE")) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_emp_termination"));
        bundle.setResult(obError);
        return;
      }
      if (!objSuspension.isSueDecision()) {
        // if delegated position is exist for the employee with greater than start date the throw
        // error , for cancel the delegation
        if (objSuspension.getSuspensionType().equals("SUE")
            && objSuspension.getSuspensionEndReason() != null
            && (objSuspension.getSuspensionEndReason().getSearchKey().equals("T")
                || objSuspension.getSuspensionEndReason().getSearchKey().equals("TRD"))) {
          isExist = positionEmpHist.checkDelegatedRecordwithGreaterthanStartDate(
              suspensionView.getEmployee(), objSuspension.getEndDate());
          if (isExist) {
            OBError result = OBErrorBuilder.buildMessage(null, "error", "@EHCM_CancelDelegation@");
            bundle.setResult(result);
            // return;
          }
        }
        // checking decision overlap
        if (objSuspension.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
            || (objSuspension.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE))) {
          JSONObject decresult = Utility.chkDecisionOverlap(Constants.SUSPENSION_OVERLAP,
              sa.elm.ob.utility.util.Utility.formatDate(objSuspension.getStartDate()), endDate,
              objSuspension.getEhcmEmpPerinfo().getId(), susReason.getId(), objSuspension.getId());
          log.debug("result:" + decresult);
          if (decresult != null && decresult.has("errorFlag")
              && decresult.getBoolean("errorFlag")) {
            if (objSuspension.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
                || (objSuspension.getDecisionType()
                    .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                    && (decresult.has("suspensionId") && !decresult.getString("suspensionId")
                        .equals(objSuspension.getOriginalDecisionNo().getId()))
                    || !decresult.has("suspensionId"))) {
              if (decresult.has("errormsg")) {
                obError.setType("Error");
                obError.setTitle("Error");
                obError.setMessage(OBMessageUtils.messageBD(decresult.getString("errormsg")));
                bundle.setResult(obError);
                return;
              }
            }
          }
        }
        // update status as Issued and set decision date
        objSuspension.setEnabled(true);
        objSuspension.setSueDecision(true);
        objSuspension.setDecisionDate(new Date());
        objSuspension.setDecisionStatus("I");
        OBDal.getInstance().save(objSuspension);
        OBDal.getInstance().flush();
        // Create Suspension
        if (objSuspension.getDecisionType().equals("CR")) {
          // Create New Record in employment information for suspension type start
          if (objSuspension.getSuspensionType().equals("SUS")) {
            EmpSuspensionHandlerDAO.preSuspensionRecordInactive(objSuspension, suspensionId);
            EmpSuspensionHandlerDAO.insertEmploymentInfo(objSuspension);
          }

          // Create New Record info in employment information for suspension type "Suspension End"
          else if (objSuspension.getSuspensionType().equals("SUE")
              && !objSuspension.isJoinWorkRequestRequired() && (oldSuspension != null)) {
            EmpSuspensionHandlerDAO.InsertRecordUsingSuspensionEnd(objSuspension, oldSuspension,
                null);
          } else if (objSuspension.getSuspensionType().equals("SUE")
              && objSuspension.isJoinWorkRequestRequired() && (oldSuspension != null)) {
            oldSuspension.setEnabled(false);
            OBDal.getInstance().save(objSuspension);
            OBDal.getInstance().flush();
          }
        }
        // Decision type Update
        else if (objSuspension.getDecisionType().equals("UP")
            && !objSuspension.isJoinWorkRequestRequired() && (oldSuspension != null)) {
          int count = EmpSuspensionHandlerDAO.updateEmploymentRecord(objSuspension, oldSuspension,
              oldTermination, vars);
          if (count == 1) {
            OBDal.getInstance().rollbackAndClose();
            OBError result = OBErrorBuilder.buildMessage(null, "error",
                "@Ehcm_Terminated_position(not available)@");
            bundle.setResult(result);
            return;

          } else if (count == 2) {
            OBDal.getInstance().rollbackAndClose();
            OBError result = OBErrorBuilder.buildMessage(null, "error",
                "@Ehcm_Terminated_position(not available)@");
            bundle.setResult(result);
            return;
          }

        }
        // Decision Type Cancel
        else if (objSuspension.getDecisionType().equals("CA")
            && !objSuspension.isJoinWorkRequestRequired() && (oldSuspension != null)) {
          int count = EmpSuspensionHandlerDAO.cancelEmploymentRecord(objSuspension, oldSuspension,
              oldTermination, vars);
          if (count == 1) {
            OBDal.getInstance().rollbackAndClose();
            OBError result = OBErrorBuilder.buildMessage(null, "error",
                "@Ehcm_Terminated_position(not available)@");
            bundle.setResult(result);
            return;
          } else if (count == 2) {
            OBDal.getInstance().rollbackAndClose();
            OBError result = OBErrorBuilder.buildMessage(null, "error",
                "@Ehcm_Terminated_position(not available)@");
            bundle.setResult(result);
            return;
          }

        }
      }
      obError.setType("Success");
      obError.setTitle("Success");
      obError.setMessage(OBMessageUtils.messageBD("Ehcm_Submit_Process"));
      bundle.setResult(obError);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    }

    catch (Exception e) {
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