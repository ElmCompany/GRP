package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdatePosNewValue extends BaseActionHandler {
  private static final Logger log = LoggerFactory.getLogger(UpdatePosNewValue.class);

  protected JSONObject execute(Map<String, Object> parameters, String data) {
    Connection con = null;
    try {
      Connection conn = OBDal.getInstance().getConnection();
      // get the data as json
      final JSONObject jsonData = new JSONObject(data);
      final String value = jsonData.getString("value");
      // log.debug(value);
      PreparedStatement st = null;
      ResultSet rs = null;
      String trxType = "";
      try {
        st = conn.prepareStatement(
            "select value from ehcm_postransactiontype where ehcm_postransactiontype_id=? ");
        st.setString(1, value);
        rs = st.executeQuery();
        if (rs.next()) {
          trxType = rs.getString("value");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      // create the result
      JSONObject json = new JSONObject();
      json.put("Value", trxType);

      // and return it
      return json;
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}