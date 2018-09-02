package sa.elm.ob.hcm.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.ad_callouts.dao.SupervisorCalloutDAO;

/**
 * this process handle the call out for supervisor subordinates when change the employee
 * 
 * @author divya
 *
 */
@SuppressWarnings("serial")
public class SupervisorCallout extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String inpehcmEmpPerinfoId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String inpehcmEmpHierarchyId = vars.getStringParameter("inpehcmEmpHierarchyId");
    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    try {
      if (inpLastFieldChanged.equals("inpehcmEmpPerinfoId")) {
        int NoOfSubordinates = SupervisorCalloutDAO.getNoOfSubordinates(inpehcmEmpPerinfoId,
            inpehcmEmpHierarchyId);
        info.addResult("inpnoofsubordinates", NoOfSubordinates);
      }
    } catch (Exception e) {
      log4j.error("Exception in SupervisorCallout  :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }
}
