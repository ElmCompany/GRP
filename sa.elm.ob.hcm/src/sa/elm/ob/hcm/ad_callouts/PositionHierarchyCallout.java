package sa.elm.ob.hcm.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.ad_callouts.dao.SupervisorCalloutDAO;

/**
 * this process handle the call out for  poshierarchy posnode when change the position
 * 
 * @author Mouli.K
 *
 */
@SuppressWarnings("serial")
public class PositionHierarchyCallout extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String inpehcmPositionId = vars.getStringParameter("inpehcmPositionId"); // inpehcmPositionId
    String inpehcmPositionTreeId = vars.getStringParameter("inpehcmPositionTreeId"); // inpehcmPositionTreeId
    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    try {
      if (inpLastFieldChanged.equals("inpehcmEmpPerinfoId")) {
        int NoOfSubordinates = SupervisorCalloutDAO.getNoOfSubordinates(inpehcmPositionId,
            inpehcmPositionTreeId);
        info.addResult("inpnoofsubordinates", NoOfSubordinates);  //inpnoofsubordinates
      }
    } catch (Exception e) {
      log4j.error("Exception in PositionHierarchyCallout  :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }
}
