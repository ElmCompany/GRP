package sa.elm.ob.hcm.ad_process.empBusinessMission;

import java.math.BigDecimal;
import java.text.DateFormat;
import javax.servlet.http.HttpServletRequest;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sa.elm.ob.hcm.EHCMBusMissionSummary;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMMisCatPeriod;
import sa.elm.ob.hcm.EHCMMiscatEmployee;
import sa.elm.ob.hcm.EHCMMissionCategory;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAO;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAOImpl;
import sa.elm.ob.hcm.util.Utility;

/**
 * This Process will handle the Employee Business Mission
 * 
 * @author Divya-28-02-2018
 *
 */
public class EmpBusinessMissionAction implements Process {
  private static final Logger log4j = LoggerFactory.getLogger(EmpBusinessMissionAction.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String lang = vars.getLanguage();

    final String empBusinessMissionId = bundle.getParams().get("Ehcm_Emp_Businessmission_ID")
        .toString();
    final String clientId = (String) bundle.getContext().getClient();
    final String userId = (String) bundle.getContext().getUser();

    EHCMEmpBusinessMission empBusinessMissionObj = OBDal.getInstance()
        .get(EHCMEmpBusinessMission.class, empBusinessMissionId);
    final String orgId = empBusinessMissionObj.getOrganization().getId();
    final String roleId = (String) bundle.getContext().getRole();

    Boolean errorFlag = false;
    String decisionType = empBusinessMissionObj.getDecisionType();
    EHCMBusMissionSummary businessMissSummary = null;
    EmpBusinessMissionDAO empBusMissDAOImpl = new EmpBusinessMissionDAOImpl();
    DateFormat dateYearFormat = sa.elm.ob.utility.util.Utility.dateFormat;
    EHCMMissionCategory missCategory = null;
    EHCMMisCatPeriod misCatPrd = null;
    EHCMMiscatEmployee misCatEmp = null;
    MissionCategoryDAO missionCategoryDAOImpl = new MissionCategoryDAOImpl();
    boolean reactive = false;

    JSONObject result = new JSONObject();
    try {
      OBContext.setAdminMode();

      EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
          empBusinessMissionObj.getEmployee().getId());

      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_BUSINESSMISSION_PAYMENT)) {
        if (empBusinessMissionObj.getPaymentAmt() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_BMAmount_Mandatory"));
          bundle.setResult(obError);
          return;
        } else if (empBusinessMissionObj.getPayrollPeriod() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_payrollPeriod_Mandatory"));
          bundle.setResult(obError);
          return;
        }

        if (empBusinessMissionObj.getPaymentAmt().compareTo(new BigDecimal(0)) == 0) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_Paymentamt_Zero"));
          bundle.setResult(obError);
          return;
        }
      }

      if (!empBusinessMissionObj.isSueDecision()) {
        if (empBusinessMissionObj.getOriginalDecisionNo() != null
            && !empBusinessMissionObj.getOriginalDecisionNo().isSueDecision()) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_SecOrgDecNo_NotProcessed"));
          bundle.setResult(obError);
          return;
        }

        // checking decision overlap
        if (empBusinessMissionObj.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
            || empBusinessMissionObj.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
            || empBusinessMissionObj.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
          if (empBusinessMissionObj.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
            result = Utility.chkDecisionOverlap(Constants.BUSINESSMISSION_OVERLAP,
                sa.elm.ob.utility.util.Utility
                    .formatDate(empBusinessMissionObj.getExtendStartdate()),
                sa.elm.ob.utility.util.Utility.formatDate(empBusinessMissionObj.getExtendEnddate()),
                empBusinessMissionObj.getEmployee().getId(),
                empBusinessMissionObj.getMissionCategory().getId(), empBusinessMissionObj.getId());
          } else {
            result = Utility.chkDecisionOverlap(Constants.BUSINESSMISSION_OVERLAP,
                sa.elm.ob.utility.util.Utility.formatDate(empBusinessMissionObj.getStartDate()),
                sa.elm.ob.utility.util.Utility.formatDate(empBusinessMissionObj.getEndDate()),
                empBusinessMissionObj.getEmployee().getId(),
                empBusinessMissionObj.getMissionCategory().getId(), empBusinessMissionObj.getId());
          }
          log4j.debug("result:" + result);
          if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
            if (empBusinessMissionObj.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
                || empBusinessMissionObj.getDecisionType()
                    .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
                || (empBusinessMissionObj.getDecisionType()
                    .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                    && (result.has("businessMissionId") && !result.getString("businessMissionId")
                        .equals(empBusinessMissionObj.getOriginalDecisionNo().getId()))
                    || !result.has("businessMissionId"))) {
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
            Long remainingDays = misCatPrd.getDays() - misCatEmp.getUseddays();
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
            if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
              Long diff = empBusinessMissionObj.getMissionDays()
                  - empBusinessMissionObj.getOriginalDecisionNo().getMissionDays();
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

        businessMissSummary = empBusMissDAOImpl.getActEmpBusinessMissSummary(empBusinessMissionObj);
        // create case
        if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          // insert employee info record
          empBusMissDAOImpl.insertBusMissionSummary(empBusinessMissionObj, businessMissSummary,
              vars.getUser(), decisionType);

          empBusMissDAOImpl.updateMissionBalance(misCatEmp, decisionType, empBusinessMissionObj,
              vars.getUser());
        }
        // update case
        else if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
            || decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
            || decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          // update record in business mission summary
          empBusMissDAOImpl.updateBusMissionSummary(empBusinessMissionObj, businessMissSummary,
            decisionType);

          empBusMissDAOImpl.updateMissionBalance(misCatEmp, decisionType, empBusinessMissionObj,
              vars.getUser());
        }
        // cancel case
        else if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {

          // remove the created business mission summary record
          empBusMissDAOImpl.removebusinessMissionActRecord(empBusinessMissionObj);
          empBusMissDAOImpl.updateMissionBalance(misCatEmp, decisionType, empBusinessMissionObj,
              vars.getUser());

        }
        // business & Payment case
        else if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_BUSINESSMISSION_PAYMENT)) {
          empBusMissDAOImpl.updatePaymentFlag(empBusinessMissionObj.getEmployee().getId(),
              empBusinessMissionObj.getOriginalDecisionNo().getId(), reactive);

        }
        empBusMissDAOImpl.updateEmpBusMissionStatus(empBusinessMissionObj);
      }

      if (!errorFlag) {
        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_EmpBusMission_Com"));
      } else if (errorFlag) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_EmpBusMiss_NotCom"));
      }
      bundle.setResult(obError);
      OBDal.getInstance().flush();
    }

    catch (Exception e) {
      bundle.setResult(obError);
      OBDal.getInstance().rollbackAndClose();
      if (log4j.isErrorEnabled()) {
        log4j.error("exception :", e);
      }
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}