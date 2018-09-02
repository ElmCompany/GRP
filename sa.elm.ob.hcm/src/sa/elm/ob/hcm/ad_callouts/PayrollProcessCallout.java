package sa.elm.ob.hcm.ad_callouts;

import java.util.List;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMPayrolldefPeriod;

public class PayrollProcessCallout extends SimpleCallout {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub

    VariablesSecureApp vars = info.vars;
    String payrolldefId = vars.getStringParameter("inpehcmPayrollDefinitionId");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    List<EHCMPayrolldefPeriod> payrolllist = null;
    // dateFormat = sa.elm.ob.utility.util.Utility.YearFormat;
    try {
      if (lastfieldChanged.equals("inpehcmPayrollDefinitionId")) {
        OBQuery<EHCMPayrolldefPeriod> paydef = OBDal.getInstance().createQuery(
            EHCMPayrolldefPeriod.class,
            " ehcmPayrollDefinition.id=:payrolldefId and now() between startDate and endDate");
        paydef.setNamedParameter("payrolldefId", payrolldefId);
        paydef.setMaxResult(1);
        payrolllist = paydef.list();
        if (payrolllist.size() > 0) {
          info.addResult("inpehcmPayrolldefPeriodId", payrolllist.get(0).getId());
        }

      }

    } catch (Exception e) {
      e.printStackTrace();
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }

}
