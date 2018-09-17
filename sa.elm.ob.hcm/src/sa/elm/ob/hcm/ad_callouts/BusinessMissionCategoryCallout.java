package sa.elm.ob.hcm.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.utility.EUTDeflookupsTypeLn;

/**
 * @author Gopalakrishnan 
 */
/**
 * Callout to fill Name in Business Mission Category
 */
public class BusinessMissionCategoryCallout extends SimpleCallout {

  /**
   * 
   */
  private static final long serialVersionUID = -4385293929137423366L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    String inpLastFieldChanged = info.vars.getStringParameter("inpLastFieldChanged");
    String lookUplineId = info.vars.getStringParameter("inplookupName");

    try {
      if (inpLastFieldChanged.equals("inplookupName")) {
        EUTDeflookupsTypeLn objLookUpLine = OBDal.getInstance().get(EUTDeflookupsTypeLn.class,
            lookUplineId);
        if (objLookUpLine != null)
          info.addResult("inpname", objLookUpLine.getEnglishName());
      }
    } catch (Exception e) {
      log4j.error("Exception in BusinessMissionCategoryCallout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }

}
