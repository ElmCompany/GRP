package sa.elm.ob.hcm.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

/**
 * @author Priyanka Ranjan on 27/01/2017
 */

public class EhcmCountryAndRegionHeaderCallout extends SimpleCallout {

  /**
   * This Callout is for process of "Country and Region" Window
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    VariablesSecureApp vars = info.vars;

    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inphasregion = vars.getStringParameter("inphasregion");
    String inpregionname = vars.getStringParameter("inpregionname");

    // while unselect the Has Region checkbox then Region Name field should be blank
    if (inpLastFieldChanged.equals("inphasregion")) {
      if (inphasregion.equals("N")) {
        info.addResult("inpregionname", "");
      }
    }

  }
}
