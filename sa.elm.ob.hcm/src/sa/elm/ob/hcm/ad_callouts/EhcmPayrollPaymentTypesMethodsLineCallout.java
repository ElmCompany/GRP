package sa.elm.ob.hcm.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

/**
 * @author Priyanka Ranjan on 12/01/2017
 */

public class EhcmPayrollPaymentTypesMethodsLineCallout extends SimpleCallout {

  /**
   * Callout to update the "IsActive='N'" if we enter End Date in "Payroll Payment Types and Methods
   * line- Payment Methods" Window
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    VariablesSecureApp vars = info.vars;

    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpenddate = vars.getStringParameter("inpenddate");
    try {
      if (inpLastFieldChanged.equals("inpenddate")) {
        if (inpenddate != null) {
          info.addResult("inpisactive", "N");
        } else {
          info.addResult("inpisactive", "Y");
        }
      }
    } catch (Exception e) {
      log4j.error("Exception in payroll Payment types and method Action Type end Date Callout :",
          e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
