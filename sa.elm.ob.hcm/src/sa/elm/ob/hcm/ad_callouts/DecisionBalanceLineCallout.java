package sa.elm.ob.hcm.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.ad_process.Constants;

/**
 * @author divya on 22/08/2018
 */
public class DecisionBalanceLineCallout extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String absenceTypeId = vars.getStringParameter("inpehcmAbsenceTypeId");
    String decisionType = vars.getStringParameter("inpdecisionType");
    try {

      if (lastfieldChanged.equals("inpehcmAbsenceTypeId")) {
        if (absenceTypeId != null) {
          EHCMAbsenceType absenceType = OBDal.getInstance().get(EHCMAbsenceType.class,
              absenceTypeId);
          if (absenceType != null && decisionType.equals(Constants.ALLPAIDLEAVEBALANCE)
              && !absenceType.isSubtype()) {
            info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Subtype').setValue('')");
          }
          if (absenceType != null
              && !absenceType.getAccrualResetDate().equals(Constants.ACCRUALRESETDATE_LEAVEOCCUR)) {
            info.addResult("inpblockStartdate", null);
          }
        }
      }

    } catch (Exception e) {
      log4j.error("Exception in DecisionBalanceLineCallout ", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

}
