package sa.elm.ob.hcm.ad_callouts;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMDeflookupsType;

/**
 * @author poongodi on 09/03/2018
 */
public class EhcmReflookupCallout extends SimpleCallout {

  private static final long serialVersionUID = 1L;
  private static final String ReferenceType = "EOT";

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;

    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String defLookTypeId = vars.getStringParameter("inpehcmDeflookupsTypeId");
    try {
      EHCMDeflookupsType defLookUp = OBDal.getInstance().get(EHCMDeflookupsType.class,
          defLookTypeId);
      if (defLookUp.getReference().equals(ReferenceType)) {
        if (lastfieldChanged.equals("inpname")) {
          String value = vars.getNumericParameter("inpname");
          DecimalFormat f = new DecimalFormat("##.00"); // this will helps you to always keeps in
                                                        // two
                                                        // decimal places
          info.addResult("inpname", f.format(new BigDecimal(value)));
        }
      }

    } catch (Exception e) {
      log4j.error("Exception in EhcmReflookupCallout", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

}
