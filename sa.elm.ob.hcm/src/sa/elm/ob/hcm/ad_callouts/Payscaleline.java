package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.SQLQuery;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;

@SuppressWarnings("serial")
public class Payscaleline extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String inpActive = info.getStringParameter("inpisactive", null);
    String inpcCurrencyId = vars.getStringParameter("inpcCurrencyId");
    String inpehcmPayscaleId = vars.getStringParameter("inpehcmPayscaleId");
    String inpOrg = vars.getStringParameter("inpadOrgId");
    String inpClientId = vars.getStringParameter("inpadClientId");
    Organization organization = null;
    Currency currency = null;

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
      String sql = "";
      String org = "";
      String units = "";
      sql = "select ad_org_id,units from ehcm_payscale where ehcm_payscale_id = ?";
      st = conn.prepareStatement(sql);
      st.setString(1, inpehcmPayscaleId);

      rs = st.executeQuery();

      if (rs.next()) {

        org = rs.getString("ad_org_id");

        units = rs.getString("units");

        if (units.equals("1")) {
          SQLQuery query1 = OBDal.getInstance().getSession().createSQLQuery(
              "select  efin_getcurrency ('" + org + "','" + inpClientId + "','false')");

          List<String> list = query1.list();

          if (query1 != null && query1.list().size() > 0) {

            String inpcurrency = list.get(0);

            info.addResult("inpcCurrencyId", inpcurrency);
          } else {
            info.addResult("inpcCurrencyId", null);
          }

        } else {
          info.addResult("inpcCurrencyId", null);
        }
      }

    } catch (Exception e) {
      log4j.error("Exception in payscaleline Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }
}