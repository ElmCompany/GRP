package sa.elm.ob.hcm.ad_reports.employeeretirement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class EmployeeRetirementDAO {

  private Connection conn = null;
  private static Logger log4j = Logger.getLogger(EmployeeRetirementDAO.class);

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
              + " join ehcm_employment_info ed2 on ed1.ehcm_emp_perinfo_id=ed2.ehcm_emp_perinfo_id  "
              + " left join ehcm_gradeclass ed3 on ed2.empcategory=ed3.ehcm_gradeclass_id "
              + " where ed1.ehcm_emp_perinfo_id= ? and ed2.status='ACT' ");
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

  public static JSONObject getEmpGradeList(String empId, Connection conn) {
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      jsob = new JSONObject();
      StringBuilder selectQuery = new StringBuilder();
      selectQuery.append(
          " select ed3.value as id,(ed3.value||'-'||ed3.name)as empGrade from ehcm_emp_perinfo ed1 "
              + " join ehcm_employment_info ed2 on ed1.ehcm_emp_perinfo_id=ed2.ehcm_emp_perinfo_id  "
              + " left join ehcm_grade ed3 on ed2.employmentgrade=ed3.ehcm_grade_id "
              + " where ed1.ehcm_emp_perinfo_id= ? and ed2.status='ACT' ");
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
              + " where ed1.ehcm_emp_perinfo_id= ?  and ed2.status='ACT' ");
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

  public static String get30Days(String inpStartDate, int days, Connection conn) {

    PreparedStatement st = null;
    ResultSet rs = null;
    String startdate = "", enddate = "", inpEndDate = "";
    startdate = inpStartDate.split("-")[2] + inpStartDate.split("-")[1]
        + inpStartDate.split("-")[0];
    try {
      st = conn.prepareStatement(
          "select hijri_date from (select max(hijri_date)  as hijri_date from eut_hijri_dates where   hijri_date >= ? group by   hijri_date "
              + "    order by hijri_date asc   limit ? ) dual order by hijri_date desc limit 1   ");
      st.setString(1, startdate);
      st.setInt(2, Integer.valueOf(days));
      log4j.debug("st:" + st.toString());
      // System.out.println(("st:" + st.toString()));
      rs = st.executeQuery();
      if (rs.next()) {
        log4j.debug("hijri_date:" + rs.getString("hijri_date"));
        enddate = rs.getString("hijri_date").substring(6, 8) + "-"// (Integer.valueOf(rs.getString("hijri_date").substring(6,
        // 8)) - 1) + "-"
            + rs.getString("hijri_date").substring(4, 6) + "-"
            + rs.getString("hijri_date").substring(0, 4);
        inpEndDate = enddate;
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return inpEndDate;
  }

}
