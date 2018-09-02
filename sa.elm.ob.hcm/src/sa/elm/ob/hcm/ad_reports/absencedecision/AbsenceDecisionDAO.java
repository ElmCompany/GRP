package sa.elm.ob.hcm.ad_reports.absencedecision;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

public class AbsenceDecisionDAO {

  private static Logger log4j = Logger.getLogger(AbsenceDecisionDAO.class);

  /**
   * Get Decision No
   * 
   * @param clientId
   * @param empId
   * @return JSONObject
   */
  @SuppressWarnings("rawtypes")
  public static JSONObject getDecisionNo(String clientId, String empId, String absenceTypeId) {
    String strQuery = null;
    Query query = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      OBContext.setAdminMode();
      jsob = new JSONObject();
      strQuery = " select ehcm_absence_attendance_id, decision_no "
          + " from ehcm_absence_attendance "
          + " where ehcm_absence_attendance_id in (with recursive rel_tree(ehcm_absence_attendance_id, level, path_info) as ("
          + "   select ehcm_absence_attendance_id, "
          + "   1 as level, ehcm_absence_attendance_id as path_info "
          + "   from ehcm_absence_attendance "
          + "   where original_decision_no is null and ad_client_id=? and decision_status='I' "
          + "   union all " + "   select ch.ehcm_absence_attendance_id, "
          + "   par.level + 1, par.path_info " + "   from ehcm_absence_attendance ch "
          + "   join rel_tree par on ch.original_decision_no = par.ehcm_absence_attendance_id  and ch.decision_status='I' "
          + " )" + "select ehcm_absence_attendance_id " + "from rel_tree "
          + " where level=(select max(level) from rel_tree e where e.path_info=rel_tree.path_info group by path_info) "
          + " order by path_info, level) and decision_type<>'CA' and decision_status='I' ";

      if (empId != null && !("null").equals(empId))
        strQuery += " and ehcm_emp_perinfo_id  =? ";
      if (absenceTypeId != null && !("null").equals(absenceTypeId))
        strQuery += " and ehcm_absence_type_id  =? ";
      strQuery += " order by decision_no";
      query = OBDal.getInstance().getSession().createSQLQuery(strQuery);
      query.setParameter(0, clientId);
      if (empId != null && !("null").equals(empId))
        query.setParameter(1, empId);
      if (absenceTypeId != null && !("null").equals(absenceTypeId)) {
        if (empId != null && !("null").equals(empId))
          query.setParameter(2, absenceTypeId);
        else
          query.setParameter(1, absenceTypeId);
      }
      log4j.debug("strQuery:" + query.toString());
      if (query.list() != null && query.list().size() > 0) {
        for (Iterator iterator = query.list().iterator(); iterator.hasNext();) {
          Object[] row = (Object[]) iterator.next();
          JSONObject jsonData = new JSONObject();
          jsonData.put("id", row[0].toString());
          jsonData.put("recordIdentifier", row[1].toString());
          jsonArray.put(jsonData);
        }
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");
    } catch (Exception e) {
      log4j.error("Exception in getDecisionNo " + e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsob;
  }
}