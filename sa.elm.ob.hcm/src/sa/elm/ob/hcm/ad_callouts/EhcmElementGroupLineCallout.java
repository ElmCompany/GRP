package sa.elm.ob.hcm.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMElmttypeDef;

public class EhcmElementGroupLineCallout extends SimpleCallout {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String elementTypeDef = vars.getStringParameter("inpehcmElmttypeDefId");
    try {
      if (inpLastFieldChanged.equals("inpehcmElmttypeDefId")) {
        EHCMElmttypeDef elementType = OBDal.getInstance().get(EHCMElmttypeDef.class,
            elementTypeDef);
        info.addResult("inpelementType", elementType.getType());
        info.addResult("inpelementClassification", elementType.getElementClassification());
      }
    } catch (Exception e) {
      log4j.error("Exception in element group line Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }
}
