package sa.elm.ob.hcm.ad_process.EmpOvertimeTransaction;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EhcmEmployeeOvertime;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;

/**
 * @author poongodi on 09/05/2018
 */
public class OvertimeReactivateProcess extends DalBaseProcess {

  private static final Logger log = LoggerFactory.getLogger(OvertimeReactivateProcess.class);
  private final OBError obError = new OBError();

  @SuppressWarnings("rawtypes")
  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub

    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String clientid = vars.getClient();
    log.debug("entering into OvertimeReactivateProcess");
    try {
      OBContext.setAdminMode();
      final String EmpOvertimeId = bundle.getParams().get("Ehcm_Emp_Overtime_ID").toString();
      EhcmEmployeeOvertime overtimeObj = OBDal.getInstance().get(EhcmEmployeeOvertime.class,
          EmpOvertimeId);

      if ((overtimeObj.getPayrollProcessLine() != null)
          && overtimeObj.getDecisionType().equals("OP")) {
        if (!(overtimeObj.getPayrollProcessLine().getPayrollProcessHeader().getStatus().equals("UP")
            || overtimeObj.getPayrollProcessLine().getPayrollProcessHeader().getStatus()
                .equals("IC")
            || overtimeObj.getPayrollProcessLine().getPayrollProcessHeader().getStatus()
                .equals("DR"))) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_payroll_processed"));
          bundle.setResult(obError);
          return;
        }
      }
      if (!overtimeObj.isPayrollprocessed()) {
        if (overtimeObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
            || overtimeObj.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_OVERTIMEPAYMENT)) {
          OBQuery<EhcmEmployeeOvertime> childrecord = OBDal.getInstance()
              .createQuery(EhcmEmployeeOvertime.class,
                  " as e  where e.client.id ='" + clientid
                      + "'  and e.originalDecisionNo.decisionNo ='" + overtimeObj.getDecisionNo()
                      + "'");
          if (childrecord.count() > 0) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("EHCM_record_reactivate"));
            bundle.setResult(obError);
          } else {
            EhcmEmployeeOvertime oldOvertimeObj = overtimeObj.getOriginalDecisionNo();
            oldOvertimeObj.setEnabled(true);
            OBDal.getInstance().save(oldOvertimeObj);
            OBDal.getInstance().flush();

            overtimeObj.setEnabled(true);
            overtimeObj.setEhcmReactivate(true);
            overtimeObj.setDecisionStatus("UP");
            overtimeObj.setSueDecision(false);
            OBError result = OBErrorBuilder.buildMessage(null, "success",
                "@Ehcm_Overtime_Reactivate@");
            bundle.setResult(result);
            return;
          }
        }
      } else {
        OBError result = OBErrorBuilder.buildMessage(null, "error", "@Ehcm_Payroll_Cant_Reactive@");
        bundle.setResult(result);
        return;
      }
    }

    catch (Exception e) {
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
