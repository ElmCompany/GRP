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

@SuppressWarnings("serial")
public class LocationEnddatecallout extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    String inpActive = info.getStringParameter("inpisactive", null);
    VariablesSecureApp vars = info.vars;
    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpehcmAddressStyleId = vars.getStringParameter("inpehcmAddressStyleId");

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

      if (inpLastFieldChanged.equals("inpehcmAddressStyleId")) {
        info.addResult("JSEXECUTE", "form.getFieldFromColumnName('C_Country_ID').setValue('')");
        info.addResult("JSEXECUTE", "form.getFieldFromColumnName('C_City_ID').setValue('')");
        info.addResult("inpaddress1", null);
        info.addResult("inpaddress2", null);
        info.addResult("inpstreet", null);
        info.addResult("inpdistrict", null);
        info.addResult("inppostbox", null);
        info.addResult("inppostalcode", null);

      }
    } catch (Exception e) {
      log4j.error("Exception in LocationEnddatecallout Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }

}
