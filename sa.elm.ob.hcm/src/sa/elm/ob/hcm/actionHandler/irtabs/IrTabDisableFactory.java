package sa.elm.ob.hcm.actionHandler.irtabs;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import sa.elm.ob.hcm.actionHandler.irtabs.irtabprocess.AbsenceDecision;
import sa.elm.ob.utility.util.irtabsutils.IRTabIconVariables;

public class IrTabDisableFactory {
  Logger log4j = Logger.getLogger(IrTabDisableFactory.class);

  public IRTabIconVariables getTab(HttpServletRequest request, JSONObject jsonData) {
    IRTabIconVariables irtabIcon = null;
    try {
      final String tabId = jsonData.getString("tabId") == null ? "" : jsonData.getString("tabId");

      /* Absence Decision */
      if (tabId.equals("076B159D222E4EEB85C70B3FEE6B22F6")) {
        irtabIcon = new AbsenceDecision();
      }
      if (irtabIcon != null) {
        irtabIcon.getIconVariables(request, jsonData);
      }
    } catch (Exception e) {
      log4j.error("Excpetion in getTab(): " + e);
      return null;
    }
    return irtabIcon;
  }
}