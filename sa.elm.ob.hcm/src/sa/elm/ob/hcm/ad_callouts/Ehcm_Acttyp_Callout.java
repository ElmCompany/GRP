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

public class Ehcm_Acttyp_Callout extends SimpleCallout {

  /**
   * Callout to fill end date as today when action type is inactive
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String inpActive = vars.getStringParameter("inpisactive");
    String inpEndDate = vars.getStringParameter("inpenddate");
    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");

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
          if (inpActive.equals("N")) {
            info.addResult("inpenddate", rs.getString("eut_convert_to_hijri_timestamp"));
          } else {
            info.addResult("inpenddate", null);
          }
        } else if (inpLastFieldChanged.equals("inpenddate")) {
          if (inpEndDate == null || inpEndDate.equals("")) {
            info.addResult("inpisactive", true);
          } else {
            info.addResult("inpisactive", false);
          }
        }
      }
    } catch (Exception e) {
      log4j.error("Exception in Action Type Active Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }

}
