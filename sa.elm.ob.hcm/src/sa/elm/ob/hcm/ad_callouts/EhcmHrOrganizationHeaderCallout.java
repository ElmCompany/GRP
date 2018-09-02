/*
 *************************************************************************
 * All Rights Reserved.
 * Contributor(s):  Qualian
 ************************************************************************
 */
package sa.elm.ob.hcm.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

/**
 * @author Priyanka Ranjan on 13/10/2017
 */

public class EhcmHrOrganizationHeaderCallout extends SimpleCallout {

  /**
   * Callout to update in HR Organization Window
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    VariablesSecureApp vars = info.vars;

    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpemEhcmOrgtyp = vars.getStringParameter("inpemEhcmOrgtyp");

    // while change HR Organization Type Set Department Fund Checkbox as 'N'
    if (inpLastFieldChanged.equals("inpemEhcmOrgtyp")) {
      info.addResult("inpemEfinIsdeptfund", false);
    }

  }
}
