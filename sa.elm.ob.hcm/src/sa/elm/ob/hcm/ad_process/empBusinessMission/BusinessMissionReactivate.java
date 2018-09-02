package sa.elm.ob.hcm.ad_process.empBusinessMission;

import java.text.DateFormat;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMBusMissionSummary;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMMisCatPeriod;
import sa.elm.ob.hcm.EHCMMiscatEmployee;
import sa.elm.ob.hcm.EHCMMissionCategory;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAO;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAOImpl;

/**
 * @author poongodi on 15/05/2018
 */
public class BusinessMissionReactivate extends DalBaseProcess {

  private static final Logger log = LoggerFactory.getLogger(BusinessMissionReactivate.class);
  private final OBError obError = new OBError();

  @SuppressWarnings("rawtypes")
  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub

    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    EmpBusinessMissionDAO empBusMissDAOImpl = new EmpBusinessMissionDAOImpl();
    EHCMBusMissionSummary businessMissSummary = null;
    EHCMMisCatPeriod misCatPrd = null;
    EHCMMiscatEmployee misCatEmp = null;
    EHCMMissionCategory missCategory = null;
    DateFormat dateYearFormat = sa.elm.ob.utility.util.Utility.dateFormat;
    MissionCategoryDAO missionCategoryDAOImpl = new MissionCategoryDAOImpl();
    boolean reactive = true;
    String olddecisionType = null;

    log.debug("entering into BusinessMissionReactivate");
    try {
      OBContext.setAdminMode();
      final String empBusinessMissionId = bundle.getParams().get("Ehcm_Emp_Businessmission_ID")
          .toString();
      EHCMEmpBusinessMission empBusinessMissionObj = OBDal.getInstance()
          .get(EHCMEmpBusinessMission.class, empBusinessMissionId);
      EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
          empBusinessMissionObj.getEmployee().getId());
      String clientId = empBusinessMissionObj.getClient().getId();
      String decisionType = empBusinessMissionObj.getDecisionType();
      EHCMEmpBusinessMission businessmissionOldObj = null;

      if (empBusinessMissionObj.getDecisionType().equals("BP")
          && (empBusinessMissionObj.getPayrollProcessLine() != null)) {
        if (!(empBusinessMissionObj.getPayrollProcessLine().getPayrollProcessHeader().getStatus()
            .equals("UP")
            || empBusinessMissionObj.getPayrollProcessLine().getPayrollProcessHeader().getStatus()
                .equals("IC")
            || empBusinessMissionObj.getPayrollProcessLine().getPayrollProcessHeader().getStatus()
                .equals("DR"))) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_payroll_processed"));
          bundle.setResult(obError);
          return;
        }
      }

      if (empBusinessMissionObj.getEHCMTicketordertransactionList().size() > 0) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_BM_TOT_val"));
        bundle.setResult(obError);
      } else {
        if (empBusinessMissionObj.getOriginalDecisionNo() != null) {
          businessmissionOldObj = OBDal.getInstance().get(EHCMEmpBusinessMission.class,
              empBusinessMissionObj.getOriginalDecisionNo().getId());
          olddecisionType = businessmissionOldObj.getDecisionType();

        }

        boolean chkCondition = false;
        chkCondition = empBusMissDAOImpl.checkBusinessmissionAlreadyUsed(empBusinessMissionId);
        if (chkCondition) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_Businessmission_Used"));
          bundle.setResult(obError);
          return;
        }
        missCategory = empBusinessMissionObj.getMissionCategory();
        if (!decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND))
          misCatPrd = sa.elm.ob.hcm.util.UtilityDAO.getMissionPeriod(clientId, missCategory,
              dateYearFormat.format(empBusinessMissionObj.getStartDate()),
              dateYearFormat.format(empBusinessMissionObj.getEndDate()));
        if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
          misCatPrd = sa.elm.ob.hcm.util.UtilityDAO.getMissionPeriod(clientId, missCategory,
              dateYearFormat.format(empBusinessMissionObj.getExtendStartdate()),
              dateYearFormat.format(empBusinessMissionObj.getExtendEnddate()));
        }
        if (misCatPrd != null) {
          misCatEmp = missionCategoryDAOImpl.getEmployeeinPeriod(misCatPrd, person.getId());
          if (misCatEmp != null && misCatEmp.isEnabled()) {
            Long remainingDays = misCatPrd.getDays()
                - (misCatEmp.getUseddays() - empBusinessMissionObj.getMissionDays());
            if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
              if (remainingDays < empBusinessMissionObj.getMissionDays()) {
                obError.setType("Error");
                obError.setTitle("Error");
                obError.setMessage(OBMessageUtils.messageBD("EHCM_MisBal_GrtThan_MissDays"));
                bundle.setResult(obError);
                return;
              }
            }
            if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
              if (remainingDays < empBusinessMissionObj.getExtendMissionDay()) {
                obError.setType("Error");
                obError.setTitle("Error");
                obError.setMessage(OBMessageUtils.messageBD("EHCM_MisBal_GrtThan_MissDays"));
                bundle.setResult(obError);
                return;
              }
            }
            if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                || decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
              Long diff = empBusinessMissionObj.getOriginalDecisionNo().getMissionDays();
              if (remainingDays < diff) {
                obError.setType("Error");
                obError.setTitle("Error");
                obError.setMessage(OBMessageUtils.messageBD("EHCM_MisBal_GrtThan_MissDays"));
                bundle.setResult(obError);
                return;
              }
            }
          } else {

            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("EHCM_MisBalOfEmp_Inactive"));
            bundle.setResult(obError);
            return;
          }
        } else {

          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_MisCat_Period_DoesntExist"));
          bundle.setResult(obError);
          return;
        }

        // create case
        if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          empBusMissDAOImpl.removeBusinessmissionSummary(
              empBusinessMissionObj.getEmployee().getId(), empBusinessMissionObj.getId());

          misCatEmp.setUseddays(misCatEmp.getUseddays() - empBusinessMissionObj.getMissionDays());
          OBDal.getInstance().save(misCatEmp);

        } else if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
            || decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
            || decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          empBusMissDAOImpl.updateBusinessMissionSummary(
              empBusinessMissionObj.getEmployee().getId(), empBusinessMissionObj);
          Long diff = (long) 0;
          diff = empBusinessMissionObj.getMissionDays()
              - empBusinessMissionObj.getOriginalDecisionNo().getMissionDays();
          misCatEmp.setUseddays(misCatEmp.getUseddays() - diff);
        }
        if (empBusinessMissionObj.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {

        empBusMissDAOImpl.insertBusMissionSummary(businessmissionOldObj, businessMissSummary,
            vars.getUser(), olddecisionType);
          misCatEmp.setUseddays(misCatEmp.getUseddays() + businessmissionOldObj.getMissionDays());
          OBDal.getInstance().save(misCatEmp);

        }

        if (empBusinessMissionObj.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_BUSINESSMISSION_PAYMENT)) {
          empBusMissDAOImpl.updatePaymentFlag(businessmissionOldObj.getEmployee().getId(),
              businessmissionOldObj.getId(), reactive);

        }
        empBusinessMissionObj.setReactivate(true);
        empBusinessMissionObj.setDecisionStatus("UP");
        empBusinessMissionObj.setSueDecision(false);
        OBError result = OBErrorBuilder.buildMessage(null, "success",
            "@Ehcm_Businessmission_Reactivate@");
        bundle.setResult(result);
        return;
      }

    } catch (

    Exception e) {
      e.printStackTrace();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
      OBDal.getInstance().commitAndClose();
    }
  }

}
