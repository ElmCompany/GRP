package sa.elm.ob.hcm.ad_process.empScholarshipTraining;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.DecisionBalance;
import sa.elm.ob.hcm.EHCMEmpScholarship;
import sa.elm.ob.hcm.EHCMScholarshipSummary;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.util.Utility;

/**
 * This Process will handle the Employee Scholarship & Training
 * 
 * @author Divya-12-02-2018
 *
 */
public class EmpScholarshipTrainingAction implements Process {
  private static final Logger log4j = LoggerFactory.getLogger(EmpScholarshipTrainingAction.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String lang = vars.getLanguage();

    final String empScholarShipId = bundle.getParams().get("Ehcm_Emp_Scholarship_ID").toString();
    final String clientId = (String) bundle.getContext().getClient();
    final String userId = (String) bundle.getContext().getUser();

    EHCMEmpScholarship empScholarshipObj = OBDal.getInstance().get(EHCMEmpScholarship.class,
        empScholarShipId);
    final String orgId = empScholarshipObj.getOrganization().getId();
    final String roleId = (String) bundle.getContext().getRole();

    Boolean errorFlag = false;
    String decisionType = empScholarshipObj.getDecisionType();
    EHCMScholarshipSummary scholarshipSummary = null;
    EmpScholarshipTrainingDAO empScholarshipDAOImpl = new EmpScholarshipTrainingDAOImpl();
    DateFormat dateYearFormat = sa.elm.ob.utility.util.Utility.YearFormat;
    int periodOfService = 0, scholarShipEligible = 0;
    final String BUSINESS_MISSION_TYPE_EXTERNAL = "EXT";
    Boolean reactive = false;
    JSONObject result = new JSONObject();
    Date enddate = null;

    try {
      OBContext.setAdminMode();

      EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
          empScholarshipObj.getEmployee().getId());

      if (!empScholarshipObj.isSueDecision()) {

        scholarshipSummary = empScholarshipDAOImpl.getActEmpScholarSummary(empScholarshipObj);

        // payment amount should not be null
        if (empScholarshipObj.getDecisionType().equals("SP")
            && (empScholarshipObj.getPaymentAmount()) == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_payment_amount_error"));
          bundle.setResult(obError);
          return;
        }
        // net amount and payment amount should not be zero
        if (empScholarshipObj.getDecisionType().equals("SP")
            && (empScholarshipObj.getNETAmt().compareTo(BigDecimal.ZERO) == 0)
            && (empScholarshipObj.getPaymentAmount().compareTo(BigDecimal.ZERO) == 0)) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_Netamountnotzero"));
          bundle.setResult(obError);
          return;
        }
        if (empScholarshipObj.getOriginalDecisionNo() != null
            && !empScholarshipObj.getOriginalDecisionNo().isSueDecision()) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_SecOrgDecNo_NotProcessed"));
          bundle.setResult(obError);
          return;
        }

        if (empScholarshipObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
            || (empScholarshipObj.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE))
            || empScholarshipObj.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
          if (empScholarshipObj.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
            result = Utility.chkDecisionOverlap(Constants.SCHOLARSHIP_OVERLAP,
                sa.elm.ob.utility.util.Utility.formatDate(empScholarshipObj.getExtendStartdate()),
                sa.elm.ob.utility.util.Utility.formatDate(empScholarshipObj.getExtendEnddate()),
                empScholarshipObj.getEmployee().getId(),
                empScholarshipObj.getScholarshipCategory().getId(), empScholarshipObj.getId());
          } else {
            result = Utility.chkDecisionOverlap(Constants.SCHOLARSHIP_OVERLAP,
                sa.elm.ob.utility.util.Utility.formatDate(empScholarshipObj.getStartDate()),
                sa.elm.ob.utility.util.Utility.formatDate(empScholarshipObj.getEndDate()),
                empScholarshipObj.getEmployee().getId(),
                empScholarshipObj.getScholarshipCategory().getId(), empScholarshipObj.getId());
          }

          if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
            if (empScholarshipObj.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
                || empScholarshipObj.getDecisionType()
                    .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
                || (empScholarshipObj.getDecisionType()
                    .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                    && (result.has("scholarShipId") && !result.getString("scholarShipId")
                        .equals(empScholarshipObj.getOriginalDecisionNo().getId()))
                    || !result.has("scholarShipId"))) {
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

        // The Employee eligible of external scholarship or training if his period of service
        // >=scholarship eligible
        if (empScholarshipObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
            || empScholarshipObj.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
            || empScholarshipObj.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
          if (empScholarshipObj.getScholarshipType() != null && empScholarshipObj
              .getScholarshipType().getSearchKey().equals(BUSINESS_MISSION_TYPE_EXTERNAL)) {

            enddate = new Date(empScholarshipObj.getStartDate().getTime() + 1 * 24 * 3600 * 1000);

            periodOfService = sa.elm.ob.utility.util.UtilityDAO.calculateMonths(
                sa.elm.ob.utility.util.UtilityDAO.convertTohijriDate(
                    dateYearFormat.format(empScholarshipObj.getEmployee().getHiredate())),
                sa.elm.ob.utility.util.UtilityDAO
                    .convertTohijriDate(dateYearFormat.format(enddate)),
                clientId, true);

            DecisionBalance periodOfServiceInitialBalance = sa.elm.ob.hcm.util.UtilityDAO
                .getInitialBaanceObjforEmployee(empScholarshipObj.getEmployee().getId(),
                    Constants.TOTALPERIODOFSERVICE);
            if (periodOfServiceInitialBalance != null) {
              periodOfService = periodOfService
                  + (periodOfServiceInitialBalance.getPeriodOfService().intValue());
            }
            Client client = OBDal.getInstance().get(Client.class, clientId);
            scholarShipEligible = client.getEhcmExtscholreligible().intValue();
            if (periodOfService < scholarShipEligible) {
              errorFlag = true;
              obError.setType("Error");
              obError.setTitle("Error");
              obError.setMessage(OBMessageUtils.messageBD("EHCM_EmpScholarship_External"));
              bundle.setResult(obError);
              return;
            }
          }

          // external scholarship or training cant be extend more than the period of training
          if (empScholarshipObj.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
            JSONObject extendAllow = Utility.calculateEmpExtScholarship(
                empScholarshipObj.getEmployee().getId(), empScholarshipObj);
            log4j.debug("extendAllow:" + extendAllow);
            if (extendAllow != null && extendAllow.has("extendnotAllowFlag")
                && extendAllow.getBoolean("extendnotAllowFlag")) {
              errorFlag = true;
              obError.setType("Error");
              obError.setTitle("Error");
              obError.setMessage(OBMessageUtils.messageBD("EHCM_ExtScholar_ExtMoreThanTraining"));
              bundle.setResult(obError);
              return;
            }
          }
        }

        // create case
        if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          // insert employee info record
          empScholarshipDAOImpl.insertScholarshipSummary(empScholarshipObj, scholarshipSummary,
              vars, decisionType);

        }
        // update case
        else if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
            || decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
            || decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          // update created employee info scholarship record
          empScholarshipDAOImpl.updateScholarshipSummary(empScholarshipObj, scholarshipSummary,
              vars, decisionType);

        }
        // cancel case
        else if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {

          // remove the created or updated scholarship record in employee info
          empScholarshipDAOImpl.removeScholarshipActRecord(empScholarshipObj);

        }
        // Scholaship & Payment case
        else if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_SCHOLARSHIP_PAYMENT)) {

          empScholarshipDAOImpl.updatePaymentFlag(empScholarshipObj.getEmployee().getId(),
              empScholarshipObj.getOriginalDecisionNo().getId(), reactive);
        }

        empScholarshipDAOImpl.updateEmpScholarshipStatus(empScholarshipObj);
      }

      if (!errorFlag) {
        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_EmpScholarship_Com"));
      } else if (errorFlag) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_EmpScholarship_NotCom"));
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