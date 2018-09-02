package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

@SuppressWarnings("serial")
public class PosTransactionTypeEndDate extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    String inpActive = info.getStringParameter("inpisactive", null);

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
        if (inpActive.equals("N")) {
          info.addResult("inpenddate", rs.getString("eut_convert_to_hijri_timestamp"));
        } else {
          info.addResult("inpenddate", null);
        }
      }
    } catch (Exception e) {
      log4j.error("Exception in PosTransactionTypeEndDate Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }
}
