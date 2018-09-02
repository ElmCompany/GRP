package sa.elm.ob.hcm.ad_reports.SecondmentDuringPeriod;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ehcmgradeclass;
import sa.elm.ob.utility.util.Utility;

/**
 * @author Rashika.V.S on 24/07/2018
 */

public class SecondmentDuringPeriodDAO {

  private static final Logger log4j = Logger.getLogger(SecondmentDuringPeriodDAO.class);

  public static JSONObject getEmployeesList(String clientId, String searchTerm, int pagelimit,
      int page) {
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(ehcm_emp_perinfo_id) as count ");
      selectQuery.append(" select ehcm_emp_perinfo_id,concat(value,'-',arabicfullname) as name ");
      fromQuery.append(
          " from ehcm_emp_perinfo where ad_client_id = ?  and status = 'I' and isactive='Y'     ");

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(
            " and concat(value,arabicfullname)  ilike '%" + searchTerm.toLowerCase() + "%' ");

      st = OBDal.getInstance().getConnection()
          .prepareStatement(countQuery.append(fromQuery).toString());
      st.setString(1, clientId);
      rs = st.executeQuery();
      log4j.debug("st" + st.toString());
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);

      if (totalRecords > 0) {
        st = OBDal.getInstance().getConnection().prepareStatement(
            (selectQuery.append(fromQuery)).toString() + " order by value limit ? offset ? ");
        st.setString(1, clientId);
        st.setInt(2, pagelimit);
        st.setInt(3, (page - 1) * pagelimit);
      }

      rs = st.executeQuery();

      JSONObject jsonData = new JSONObject();
      if (totalRecords > 0) {

        while (rs.next()) {
          jsonData = new JSONObject();
          jsonData.put("id", Utility.nullToEmpty(rs.getString("ehcm_emp_perinfo_id")));
          jsonData.put("recordIdentifier", Utility.nullToEmpty(rs.getString("name")));
          jsonArray.put(jsonData);
        }
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      log4j.error("Exception in getCountry :", e);
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

  public static List<SecondmentDuringPeriodVO> getDepartmentCode(String inpClientId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    SecondmentDuringPeriodVO vo = null;
    List<SecondmentDuringPeriodVO> depls = new ArrayList<SecondmentDuringPeriodVO>();
    try {
      OBContext.setAdminMode();
      st = OBDal.getInstance().getConnection().prepareStatement(
          "select ad_org_id,name from ad_org where ad_org_id in (select ad_org_id from Ehcm_Hrorg_Classfication where Ehcm_Org_Classfication_ID in (select ehcm_org_classfication_id from ehcm_org_classfication where classification = 'HR' and isactive ='Y') "
              + "and isactive ='Y' and ad_client_id =?)");
      st.setString(1, inpClientId);
      rs = st.executeQuery();
      while (rs.next()) {
        vo = new SecondmentDuringPeriodVO();
        vo.setOrgId(rs.getString("ad_org_id"));
        vo.setOrgName(rs.getString("name"));
        depls.add(vo);
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getDepartmentCode " + e.getMessage());
      e.printStackTrace();
    } finally {
      try {
        if (rs != null)
          rs.close();
        if (st != null)
          st.close();
      } catch (Exception e) {
      }
      OBContext.restorePreviousMode();
    }
    return depls;
  }

  public static List<SecondmentDuringPeriodVO> getEmployeeType(String inpClientId) {

    List<SecondmentDuringPeriodVO> employeeTypeList = new ArrayList<SecondmentDuringPeriodVO>();
    OBQuery<ehcmgradeclass> gradeclass = OBDal.getInstance().createQuery(ehcmgradeclass.class,
        " as e  where e.client.id ='" + inpClientId + "'  order by e.searchKey asc");
    List<ehcmgradeclass> gclassList = gradeclass.list();
    if (gradeclass != null && gclassList.size() > 0) {
      for (ehcmgradeclass destList : gclassList) {
        SecondmentDuringPeriodVO secVO = new SecondmentDuringPeriodVO();
        secVO.setGradeClassId(destList.getId());
        secVO.setGradeClassName(destList.getSearchKey() + "-" + destList.getName());
        employeeTypeList.add(secVO);
      }
    }
    return employeeTypeList;

  }

  public static List<SecondmentDuringPeriodVO> getEmployeeGrade(String inpClientId) {
    List<SecondmentDuringPeriodVO> employeeGradeList = new ArrayList<SecondmentDuringPeriodVO>();
    OBQuery<ehcmgrade> grade = OBDal.getInstance().createQuery(ehcmgrade.class,
        " as e  where e.client.id ='" + inpClientId + "'  order by e.searchKey asc");
    List<ehcmgrade> gradeList = grade.list();
    if (grade != null && gradeList.size() > 0) {
      for (ehcmgrade destList1 : gradeList) {
        SecondmentDuringPeriodVO secVO1 = new SecondmentDuringPeriodVO();
        secVO1.setGradeId(destList1.getSearchKey());
        secVO1.setGradeName(destList1.getSearchKey() + "-" + destList1.getCommercialName());
        employeeGradeList.add(secVO1);
      }
    }
    return employeeGradeList;
  }

  public static List<SecondmentDuringPeriodVO> getLineManager(String inpClientId) {
    List<SecondmentDuringPeriodVO> empLineManagerList = new ArrayList<SecondmentDuringPeriodVO>();
    OBQuery<EHCMEmpSupervisor> lineManager = OBDal.getInstance().createQuery(
        EHCMEmpSupervisor.class,
        " as e  where e.client.id ='" + inpClientId + "'  order by e.name asc");
    List<EHCMEmpSupervisor> lineManagerList = lineManager.list();
    if (lineManager != null && lineManagerList.size() > 0) {
      for (EHCMEmpSupervisor destList2 : lineManagerList) {
        SecondmentDuringPeriodVO secVO2 = new SecondmentDuringPeriodVO();
        secVO2.setsupervisorId(destList2.getId());
        secVO2.setsupervisorName(destList2.getEmployee().getSearchKey() + "-"
            + destList2.getEmployee().getArabicfullname());
        empLineManagerList.add(secVO2);
      }
    }
    return empLineManagerList;
  }

}