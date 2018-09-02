package sa.elm.ob.hcm.ad_process.empScholarshipTraining;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONObject;
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

import sa.elm.ob.hcm.EHCMEmpScholarship;
import sa.elm.ob.hcm.EHCMScholarshipSummary;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.util.Utility;

/**
 * @author poongodi on 15/05/2018
 */
public class ScholarshipReactivateProcess extends DalBaseProcess {

  private static final Logger log = LoggerFactory.getLogger(ScholarshipReactivateProcess.class);
  private final OBError obError = new OBError();
  final String SCHOLARSHIP_TRAINNING = "SCTR";

  @SuppressWarnings("rawtypes")
  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub

    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    EHCMScholarshipSummary scholarshipSummary = null;
    EmpScholarshipTrainingDAOImpl empScholarshipDAOImpl = new EmpScholarshipTrainingDAOImpl();
    boolean reactive = true;
    String olddecisionType = null;

    EHCMEmpScholarship scholarshipOldObj = null;
    log.debug("entering into ScholarshipReactivateProcess");
    try {
      OBContext.setAdminMode();
      final String empScholarShipId = bundle.getParams().get("Ehcm_Emp_Scholarship_ID").toString();
      EHCMEmpScholarship empScholarshipObj = OBDal.getInstance().get(EHCMEmpScholarship.class,
          empScholarShipId);
      String decisionType = empScholarshipObj.getDecisionType();
      // should not allow to create scholarship for same period
      if (empScholarshipObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
        JSONObject result = Utility.overlapWithDecisionsDate(SCHOLARSHIP_TRAINNING,
            sa.elm.ob.utility.util.Utility.formatDate(empScholarshipObj.getStartDate()),
            sa.elm.ob.utility.util.Utility.formatDate(empScholarshipObj.getEndDate()),
            empScholarshipObj.getEmployee().getId());
        if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {

          if (result.has("errormsg")) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("EHCM_EmpScholarship_CreCant"));
            bundle.setResult(obError);
            return;
          }

        }
      }
      if (empScholarshipObj.getDecisionType().equals("SP")
          && (empScholarshipObj.getPayrollProcessLine() != null)) {
        if (!(empScholarshipObj.getPayrollProcessLine().getPayrollProcessHeader().getStatus()
            .equals("UP")
            || empScholarshipObj.getPayrollProcessLine().getPayrollProcessHeader().getStatus()
                .equals("IC")
            || empScholarshipObj.getPayrollProcessLine().getPayrollProcessHeader().getStatus()
                .equals("DR"))) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_payroll_processed"));
          bundle.setResult(obError);
          return;
        }
      }
      if (empScholarshipObj.getOriginalDecisionNo() != null) {
        scholarshipOldObj = OBDal.getInstance().get(EHCMEmpScholarship.class,
            empScholarshipObj.getOriginalDecisionNo().getId());
        olddecisionType = scholarshipOldObj.getDecisionType();
      }
      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        empScholarshipDAOImpl.removeScholarshipInfo(empScholarshipObj.getEmployee().getId(),
            empScholarshipObj.getId());

      }
      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
          || decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
          || decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        empScholarshipDAOImpl.updateScholarshipSummary(empScholarshipObj.getEmployee().getId(),
            empScholarshipObj);

      }
      if (empScholarshipObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
        empScholarshipDAOImpl.insertScholarshipSummary(scholarshipOldObj, scholarshipSummary, vars,
            olddecisionType);
      }
      if (empScholarshipObj.getDecisionType()
          .equals(DecisionTypeConstants.DECISION_TYPE_SCHOLARSHIP_PAYMENT)) {
        empScholarshipDAOImpl.updatePaymentFlag(scholarshipOldObj.getEmployee().getId(),
            scholarshipOldObj.getId(), reactive);

      }
      empScholarshipObj.setReactivate(true);
      empScholarshipObj.setDecisionStatus("UP");
      empScholarshipObj.setSueDecision(false);
      OBError result = OBErrorBuilder.buildMessage(null, "success",
          "@Ehcm_Scholarship_Reactivate@");
      bundle.setResult(result);
      return;
    } catch (Exception e) {
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
