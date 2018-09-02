package sa.elm.ob.hcm.actionHandler.irtabs.irtabprocess;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.utility.util.irtabsutils.IRTabIconVariables;

public class AbsenceDecision extends IRTabIconVariables {
  Logger log = Logger.getLogger(AbsenceDecision.class);

  public void getIconVariables(HttpServletRequest request, JSONObject jsonData) {
    try {
      OBContext.setAdminMode(true);
      final String recordId = jsonData.getString("recordId");
      final String tabId = jsonData.getString("tabId") == null ? "" : jsonData.getString("tabId");

      if (tabId.equals("076B159D222E4EEB85C70B3FEE6B22F6")) {
        if (!recordId.equals("")) {
          EHCMAbsenceAttendance abAtt = OBDal.getInstance().get(EHCMAbsenceAttendance.class,
              recordId);
          if (abAtt.getDecisionStatus().equals("I")) {
            enable = 1;
          }
        }
      }
    } catch (Exception e) {
      log.error("Exception in getIconVariables(): " + e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
