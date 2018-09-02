package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

/**
 * @author Priyanka Ranjan on 19/01/2017
 */

public class EhcmElementCategoryCallout extends SimpleCallout {

  /**
   * Callout to update the "End Date" by session date if we uncheck the enabled flag in "Element
   * Category" Window
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    VariablesSecureApp vars = info.vars;

    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpisactive = vars.getStringParameter("inpisactive");
    String inpendDate = vars.getStringParameter("inpendDate");
    try {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      Date now = new Date();
      Connection conn = OBDal.getInstance().getConnection();
      PreparedStatement st = null;
      ResultSet rs = null;
      String query = " select eut_convert_to_hijri_timestamp('" + dateFormat.format(now) + "')";
      st = conn.prepareStatement(query);
      rs = st.executeQuery();
      if (rs.next()) {
        if (inpLastFieldChanged.equals("inpisactive")) {
          if (inpisactive.equals("N")) {
            info.addResult("inpendDate", rs.getString("eut_convert_to_hijri_timestamp"));
          } else {
            info.addResult("inpendDate", null);
          }
        }
      }
    } catch (Exception e) {
      log4j.error("Exception in Element Category Action Type Active Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
