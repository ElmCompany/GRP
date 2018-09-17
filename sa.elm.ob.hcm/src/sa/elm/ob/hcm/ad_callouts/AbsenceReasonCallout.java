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
public class AbsenceReasonCallout extends SimpleCallout {

  /**
   * 
   */
  private static final long serialVersionUID = -6150641792651343448L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    String inpLastFieldChanged = info.vars.getStringParameter("inpLastFieldChanged");
    String lookUplineId = info.vars.getStringParameter("inpeutDeflookupsTypelnId");
    try {
      if (inpLastFieldChanged.equals("inpeutDeflookupsTypelnId")) {
        EUTDeflookupsTypeLn objLookUpLine = OBDal.getInstance().get(EUTDeflookupsTypeLn.class,
            lookUplineId);
        if (objLookUpLine != null)
          info.addResult("inpabsenceReason", objLookUpLine.getEnglishName());
      }
    } catch (Exception e) {
      log4j.error("Exception in AbsenceReasonCallout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }

}
