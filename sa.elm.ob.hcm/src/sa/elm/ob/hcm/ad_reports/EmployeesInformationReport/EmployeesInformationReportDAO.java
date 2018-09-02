package sa.elm.ob.hcm.ad_reports.EmployeesInformationReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

public class EmployeesInformationReportDAO {

  private Connection conn = null;
  private static Logger log4j = Logger.getLogger(EmployeesInformationReportDAO.class);

  public static JSONObject getEmpTypeList(String empId, Connection conn) {
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      jsob = new JSONObject();
      StringBuilder selectQuery = new StringBuilder();
      selectQuery.append(
          " select ed3.ehcm_gradeclass_id as id, (ed3.value||'-'||ed3.name) as empType from ehcm_emp_perinfo ed1  "
              // + " join ehcm_employment_info ed2 on
              // ed1.ehcm_emp_perinfo_id=ed2.ehcm_emp_perinfo_id "
              + " left join ehcm_gradeclass ed3 on ed1.ehcm_gradeclass_id=ed3.ehcm_gradeclass_id "
              + " where ed1.ehcm_emp_perinfo_id= ? ");
      st = conn.prepareStatement(selectQuery.toString());
      st.setString(1, empId);
      log4j.debug("empTypelist:" + st.toString());
      log4j.debug("qry>>" + st.toString());
      rs = st.executeQuery();

      while (rs.next()) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("id", rs.getString("id"));
        jsonData.put("emptype", rs.getString("empType"));
        jsonArray.put(jsonData);
      }

      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      log4j.error("Exception in getEmpTypeList :", e);
      return jsob;
    } finally {
      try {
        if (rs != null)
          rs.close();
        if (st != null)
          st.close();
      } catch (Exception e) {

      }
    }
    return jsob;
  }

  public static JSONObject getEmpStatusList(String empId, Connection conn, String lang) {
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      jsob = new JSONObject();
      StringBuilder selectQuery = new StringBuilder();
      selectQuery.append(
          " select status.ad_ref_list_id as id,status.statusvalue as value ,coalesce(statuslist.name,status.name) as employeestatus from ehcm_emp_perinfo info "
              + " left join ehcm_employeestatus_v status on status.ehcm_employeestatus_v_id= info.ehcm_emp_perinfo_id "
              + "  left join AD_Ref_List_Trl statuslist on statuslist.ad_ref_list_id= status.ad_ref_list_id  and  AD_Language= ?"
              + " ");

      if (!empId.equals("0"))
        selectQuery.append("  where info.ehcm_emp_perinfo_id = ?");
      st = conn.prepareStatement(selectQuery.toString());
      st.setString(1, lang);
      if (!empId.equals("0"))
        st.setString(2, empId);
      log4j.debug("qry>>" + st.toString());
      rs = st.executeQuery();

      while (rs.next()) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("id", rs.getString("value"));
        jsonData.put("empstatus", rs.getString("employeestatus"));
        jsonArray.put(jsonData);
      }

      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      log4j.error("Exception in getEmpTypeList :", e);
      return jsob;
    } finally {
      try {
        if (rs != null)
          rs.close();
        if (st != null)
          st.close();
      } catch (Exception e) {

      }
    }
    return jsob;
  }

  public static JSONObject getEmpGradeList(String empId, Connection conn) {
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      jsob = new JSONObject();
      StringBuilder selectQuery = new StringBuilder();
      selectQuery.append(
          " select ed3.ehcm_grade_id as id,(ed3.value||'-'||ed3.name)as empGrade from ehcm_emp_perinfo ed1 "
              + " join ehcm_employment_info ed2 on ed1.ehcm_emp_perinfo_id=ed2.ehcm_emp_perinfo_id  "
              + " left join ehcm_grade ed3 on ed2.employmentgrade=ed3.ehcm_grade_id "
              + " where ed1.ehcm_emp_perinfo_id= ? and ed2.created = (select max(info.created) from ehcm_employment_info info where info.ehcm_emp_perinfo_id = ed1.ehcm_emp_perinfo_id  )");
      st = conn.prepareStatement(selectQuery.toString());
      st.setString(1, empId);
      log4j.debug("empGradelist:" + st.toString());
      log4j.debug("qry>>" + st.toString());
      rs = st.executeQuery();

      while (rs.next()) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("id", rs.getString("id"));
        jsonData.put("empgrade", rs.getString("empGrade"));
        jsonArray.put(jsonData);
      }

      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      log4j.error("Exception in getEmpGradeList :", e);
      return jsob;
    } finally {
      try {
        if (rs != null)
          rs.close();
        if (st != null)
          st.close();
      } catch (Exception e) {

      }
    }
    return jsob;
  }

  public static JSONObject getLineManagerList(String empId, Connection conn) {
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      jsob = new JSONObject();
      StringBuilder selectQuery = new StringBuilder();
      selectQuery.append(
          " select ed3.ehcm_emp_supervisor_id as id, (ed4.value||'-'||ed4.arabicfullname) as lineManager from ehcm_emp_perinfo ed1 "
              + " join ehcm_employment_info ed2 on ed1.ehcm_emp_perinfo_id=ed2.ehcm_emp_perinfo_id "
              + " left join ehcm_emp_supervisor ed3 on ed2.ehcm_emp_supervisor_id=ed3.ehcm_emp_supervisor_id "
              + " left join ehcm_emp_perinfo ed4 on ed3.ehcm_emp_perinfo_id=ed4.ehcm_emp_perinfo_id "
              + " where ed1.ehcm_emp_perinfo_id= ? ");
      st = conn.prepareStatement(selectQuery.toString());
      st.setString(1, empId);
      log4j.debug("lineManagerlist:" + st.toString());
      log4j.debug("qry>>" + st.toString());
      rs = st.executeQuery();

      while (rs.next()) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("id", rs.getString("id"));
        jsonData.put("linemanager", rs.getString("lineManager"));
        jsonArray.put(jsonData);
      }

      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      log4j.error("Exception in getLineManagerList :", e);
      return jsob;
    } finally {
      try {
        if (rs != null)
          rs.close();
        if (st != null)
          st.close();
      } catch (Exception e) {

      }
    }
    return jsob;
  }

  public static JSONObject getGenderList(String empId, Connection conn) {
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      jsob = new JSONObject();
      StringBuilder selectQuery = new StringBuilder();
      selectQuery.append(
          " select gender as id, (case when gender='M' then 'Male' else 'Female' end) as gender from ehcm_emp_perinfo "
              + " where ehcm_emp_perinfo_id= ? ");
      st = conn.prepareStatement(selectQuery.toString());
      st.setString(1, empId);
      log4j.debug("genderlist:" + st.toString());
      log4j.debug("qry>>" + st.toString());
      rs = st.executeQuery();

      while (rs.next()) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("id", rs.getString("id"));
        jsonData.put("gender", rs.getString("gender"));
        jsonArray.put(jsonData);
      }

      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      log4j.error("Exception in getGenderList :", e);
      return jsob;
    } finally {
      try {
        if (rs != null)
          rs.close();
        if (st != null)
          st.close();
      } catch (Exception e) {

      }
    }
    return jsob;
  }

  @SuppressWarnings("rawtypes")
  public static JSONObject getEmployeesList(String clientId, String searchTerm, int pagelimit,
      int page) throws JSONException {
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    String whereclause = "";
    Query query = null;
    try {
      OBContext.setAdminMode();
      jsob = new JSONObject();

      if (searchTerm != null && !searchTerm.equals(""))
        whereclause = " and emp.value || '-'|| emp.arabicfullname ilike :name ";

      query = OBDal.getInstance().getSession().createSQLQuery(
          " select   distinct emp.ehcm_emp_perinfo_id, emp.value || '-'|| emp.arabicfullname as value from ehcm_emp_perinfo as emp where emp.ad_client_id = :clientId"
              + " and emp.status not in ('UP') " + whereclause);
      query.setParameter("clientId", clientId);

      if (searchTerm != null && !searchTerm.equals(""))
        query.setParameter("name", "%" + searchTerm + "%");

      log4j.debug("where :" + query.getQueryString());
      List totalList = query.list();
      jsob.put("totalRecords", totalList.size());

      query.setFirstResult((page - 1) * pagelimit); // equivalent to OFFSET
      query.setMaxResults(pagelimit);
      log4j.debug("where :" + query.getQueryString());
      List employeeList = query.list();

      if (employeeList != null && employeeList.size() > 0) {
        for (Object emp : employeeList) {
          Object[] row = (Object[]) emp;
          JSONObject jsonData = new JSONObject();
          jsonData.put("id", row[0].toString());
          jsonData.put("recordIdentifier", row[1].toString());
          jsonArray.put(jsonData);
        }
      }
      // jsob.put("totalRecords", jsonArray.length());
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsob;
  }

}