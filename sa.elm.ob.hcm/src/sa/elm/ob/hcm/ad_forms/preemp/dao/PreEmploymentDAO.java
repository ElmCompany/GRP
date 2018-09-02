package sa.elm.ob.hcm.ad_forms.preemp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;

import sa.elm.ob.hcm.ad_forms.employee.vo.EmployeeVO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class PreEmploymentDAO {
  private Connection conn = null;
  VariablesSecureApp vars = null;
  private static Logger log4j = Logger.getLogger(PreEmploymentDAO.class);

  public PreEmploymentDAO(Connection con) {
    this.conn = con;
  }

  /**
   * 
   * @param employmentId
   * @return success message
   */
  public boolean deletePrevEmployment(String preemploymentId) {
    PreparedStatement st = null;
    try {

      st = conn
          .prepareStatement("DELETE FROM ehcm_previou_service WHERE ehcm_previou_service_id = ?");
      st.setString(1, preemploymentId);
      st.executeUpdate();

    } catch (final SQLException e) {
      log4j.error("", e);
      return false;
    } catch (final Exception e) {
      log4j.error("", e);
      return false;
    } finally {
      try {
        st.close();
      } catch (final SQLException e) {
        log4j.error("", e);
        return false;
      }
    }
    return true;
  }

  public int overlapRecord(String employeeId, String startdate, String enddate,
      String peremploymentId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    int result = 0;
    try {
      if (enddate == null || enddate == "")
        enddate = "21-06-2058";
      else
        enddate = UtilityDAO.convertToGregorian(enddate);

      startdate = UtilityDAO.convertToGregorian(startdate);
      st = conn
          .prepareStatement("select count(*) as total FROM ehcm_previou_service  WHERE ehcm_emp_perinfo_id = ?  and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
              + startdate
              + "','yyyy-MM-dd')  "
              + " and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy')  <= to_date('"
              + enddate
              + "','yyyy-MM-dd'))  "
              + " or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy')  >= to_date('"
              + startdate
              + "','yyyy-MM-dd') "
              + "  and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
              + enddate
              + "','yyyy-MM-dd'))) and ehcm_previou_service_id <> '"
              + peremploymentId
              + "'");
      st.setString(1, employeeId);
      rs = st.executeQuery();
      log4j.debug("st:" + st.toString());
      if (rs.next()) {
        result = rs.getInt("total");

      }

    } catch (final SQLException e) {
      log4j.error("", e);
      return result;
    } finally {
      try {
        st.close();
      } catch (final SQLException e) {
        log4j.error("", e);
        return result;
      }
    }
    return result;
  }

  public List<EmployeeVO> getPrevEmployeeList(String clientId, String childOrgId,
      EmployeeVO employeeVO, JSONObject searchAttr, String selEmployeeId, String employeeId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<EmployeeVO> ls = new ArrayList<EmployeeVO>();
    EmployeeVO eVO = null;
    String sqlQuery = "", whereClause = "", orderClause = "";
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    String date = "";
    try {
      eVO = new EmployeeVO();
      eVO.setStatus("0_0_0");
      ls.add(eVO);

      int offset = 0, totalPage = 0, totalRecord = 0;
      int rows = Integer.parseInt(searchAttr.getString("rows")), page = Integer.parseInt(searchAttr
          .getString("page"));

      whereClause = " AND ser.ad_client_id = '" + clientId + "' and ser.ehcm_emp_perinfo_id = '"
          + employeeId + "'   AND ser.ad_org_id in (" + childOrgId + ") ";

      if (searchAttr.has("search") && searchAttr.getString("search").equals("true")) {
        if (!StringUtils.isEmpty(employeeVO.getEmpName()))
          whereClause += " and ser.employer_name  ilike '%" + employeeVO.getEmpName() + "%'";
        if (!StringUtils.isEmpty(employeeVO.getStartdate()))
          whereClause += " and ser.startdate " + employeeVO.getStartdate().split("##")[0]
              + " to_timestamp('" + employeeVO.getStartdate().split("##")[1]
              + "', 'yyyy-MM-dd HH24:MI:SS') ";

        if (!StringUtils.isEmpty(employeeVO.getEnddate()))
          whereClause += " and ser.enddate " + employeeVO.getEnddate().split("##")[0]
              + " to_timestamp('" + employeeVO.getEnddate().split("##")[1]
              + "', 'yyyy-MM-dd HH24:MI:SS') ";

        if (!StringUtils.isEmpty(employeeVO.getPosition()))
          whereClause += " and ser.empposition  ilike '%" + employeeVO.getPosition() + "%'";
        if (!StringUtils.isEmpty(employeeVO.getCategorycode()))
          whereClause += " and ser.emp_category   ilike '%" + employeeVO.getCategorycode() + "%'";
        if (!StringUtils.isEmpty(employeeVO.getDeptname()))
          whereClause += " and ser.dept_name  ilike '%" + employeeVO.getDeptname() + "%'";

      }

      if (StringUtils.equals(searchAttr.getString("sortName"), "value"))
        searchAttr.put("sortName", "info.value");

      orderClause = " order by " + searchAttr.getString("sortName") + " "
          + searchAttr.getString("sortType");

      // Get Row Count
      sqlQuery = " SELECT count(ser.ehcm_previou_service_id) as count FROM ehcm_previou_service ser  left join ad_org ao on ao.ad_org_id = ser.ad_org_id  WHERE 1 = 1  ";
      sqlQuery += whereClause;
      st = conn.prepareStatement(sqlQuery);
      rs = st.executeQuery();
      if (rs.next())
        totalRecord = rs.getInt("count");

      // Selected Employee Row
      if (selEmployeeId != null && selEmployeeId.length() == 32) {
        sqlQuery = "  select tb.rowno from (SELECT row_number() OVER ("
            + orderClause
            + ") as rowno, ser.ehcm_previou_service_id  from  ehcm_previou_service ser   left join ad_org ao on ao.ad_org_id = ser.ad_org_id  WHERE 1 = 1  ";
        sqlQuery += whereClause;
        sqlQuery += orderClause;
        sqlQuery += ")tb where tb.ehcm_previou_service_id = '" + selEmployeeId + "';";
        st = conn.prepareStatement(sqlQuery);
        rs = st.executeQuery();
        if (rs.next()) {
          int rowNo = rs.getInt("rowno"), currentPage = rowNo / rows;
          if (currentPage == 0) {
            page = 1;
            offset = 0;
          } else {
            page = currentPage;
            if ((rowNo % rows) == 0)
              offset = ((page - 1) * rows);
            else {
              offset = (page * rows);
              page = currentPage + 1;
            }
          }
        }
      } else {
        if (totalRecord > 0) {
          totalPage = totalRecord / rows;
          if (totalRecord % rows > 0)
            totalPage += 1;
          offset = ((page - 1) * rows);
          if (page > totalPage) {
            page = totalPage;
            offset = ((page - 1) * rows);
          }
        } else {
          page = 0;
          totalPage = 0;
          offset = 0;
        }
      }
      if (totalRecord > 0) {
        totalPage = totalRecord / rows;
        if (totalRecord % rows > 0)
          totalPage += 1;
      } else {
        page = 0;
        totalPage = 0;
      }

      // Adding Page Details
      eVO.setStatus(page + "_" + totalPage + "_" + totalRecord);
      ls.remove(0);
      ls.add(eVO);
      log4j.debug("whereClause : " + whereClause);

      // pre Employment Details
      sqlQuery = "   select ser.ehcm_previou_service_id ,ser.ad_client_id,ser.ehcm_emp_perinfo_id ,ao.name as org,ser.startdate,ser.enddate,ser.empposition,ser.grade,ser.employer_name as employer_name,ser.empposition,ser.emp_category as emp_cat ,ser.dept_name as department,ser.otherdetails as others FROM ehcm_previou_service ser    left join ad_org ao on ao.ad_org_id = ser.ad_org_id  WHERE 1 = 1 ";
      sqlQuery += whereClause;
      sqlQuery += orderClause;
      sqlQuery += " limit " + rows + " offset " + offset;
      st = conn.prepareStatement(sqlQuery);
      log4j.debug("Pre Employment Info : " + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        eVO = new EmployeeVO();
        eVO.setPreEmpId(Utility.nullToEmpty(rs.getString(("ehcm_previou_service_id"))));
        eVO.setEmployeeId(Utility.nullToEmpty(rs.getString(("ehcm_emp_perinfo_id"))));
        eVO.setOrgName(Utility.nullToEmpty(rs.getString("org")));
        eVO.setEmpName(Utility.nullToEmpty(rs.getString(("employer_name"))));
        if (rs.getDate("startdate") != null) {
          date = df.format(rs.getDate("startdate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          eVO.setStartdate(date);
        } else
          eVO.setStartdate(null);
        if (rs.getDate("enddate") != null) {
          date = df.format(rs.getDate("enddate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          eVO.setEnddate(date);
        } else
          eVO.setEnddate(null);

        eVO.setPosition(Utility.nullToEmpty(rs.getString("empposition")));
        eVO.setOtherdetails(Utility.nullToEmpty(rs.getString("others")));
        eVO.setDeptname(Utility.nullToEmpty(rs.getString("department")));
        eVO.setCategorycode(Utility.nullToEmpty(rs.getString("emp_cat")));
        eVO.setGradeclassId(Utility.nullToEmpty(rs.getString("grade")));
        ls.add(eVO);
      }
    } catch (final Exception e) {
      log4j.error("Exception in getPrevEmployeeList", e);
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getPrevEmployeeList", e);
      }
    }
    return ls;
  }
}
