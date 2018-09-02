package sa.elm.ob.hcm.ad_callouts;


import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;
/**
 * this callout is to enable endDate when inactive is ticked
 * @author anup  14-06-2018
 *
 */
public class Ehcm_authInfo_callout extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    try {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      VariablesSecureApp vars = info.vars;
      Date now = new Date();
      String active = vars.getStringParameter("inpisactive");
      Connection conn = OBDal.getInstance().getConnection();
      PreparedStatement st = null;
      ResultSet rs = null;
      String query = " select eut_convert_to_hijri_timestamp('" + dateFormat.format(now) + "')";

      st = conn.prepareStatement(query);
      rs = st.executeQuery();
      if (rs.next()) {
        if (active.equals("N")) {
          info.addResult("inpenddate", rs.getString("eut_convert_to_hijri_timestamp"));
        } else {
          info.addResult("inpenddate", null);
        }
      }
    }
     catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in EHCM_authoinfocallout Callout :", e);
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}