package sa.elm.ob.hcm.ad_reports.absenceinperiod;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.EHCMAbsenceType;

public class AbsenceInPeriodDAO {

  private static Logger log4j = Logger.getLogger(AbsenceInPeriodDAO.class);

  public static JSONObject getAbsenceType(String clientId) throws JSONException {
    String query = " as e where e.client.id = ? ";
    List<EHCMAbsenceType> absTypLs = null;
    List<Object> parametersList = new ArrayList<Object>();
    parametersList.add(clientId);
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      OBContext.setAdminMode();
      jsob = new JSONObject();
      OBQuery<EHCMAbsenceType> absTyp = OBDal.getInstance().createQuery(EHCMAbsenceType.class,
          query, parametersList);
      absTypLs = absTyp.list();

      if (absTypLs.size() > 0) {
        for (EHCMAbsenceType absTypVO : absTypLs) {
          JSONObject jsonData = new JSONObject();
          jsonData.put("id", absTypVO.getId());
          jsonData.put("absTypeCode", absTypVO.getSearchKey());
          jsonData.put("absTypeName", absTypVO.getSearchKey() + "-" + absTypVO.getJobGroupName());

          jsonArray.put(jsonData);
        }
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");
    } catch (OBException e) {
      log4j.error("Exception while getAbsenceType:" + e);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsob;
  }
}