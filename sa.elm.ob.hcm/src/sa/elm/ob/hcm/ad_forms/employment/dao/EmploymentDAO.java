package sa.elm.ob.hcm.ad_forms.employment.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.EHCMEmpSupervisorNode;
import sa.elm.ob.hcm.EHCMPayrollDefinition;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ehcmgradeclass;
import sa.elm.ob.hcm.ehcmpayscale;
import sa.elm.ob.hcm.ehcmpayscaleline;
import sa.elm.ob.hcm.ad_callouts.dao.SupervisorCalloutDAO;
import sa.elm.ob.hcm.ad_forms.employment.vo.EmploymentVO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class EmploymentDAO {
  private static Connection conn = null;
  VariablesSecureApp vars = null;
  private static Logger log4j = Logger.getLogger(EmploymentDAO.class);
  public static final String EmpInfo_ChangeReason_List_ID = "57889F5818294AE6B371B3FD3369E8B3";
  public static final String EmpInfo_Status_RefId = "C1F70DD4E2E140D8939D047C2B504728";

  public EmploymentDAO(Connection con) {
    this.conn = con;
  }

  /**
   * 
   * @param employmentId
   * @return success message
   */
  public boolean deleteEmployment(String employmentId) {
    PreparedStatement st = null;
    PreparedStatement pt = null;
    EmploymentInfo info = null;
    String empInfoId = null;
    try {
      info = OBDal.getInstance().get(EmploymentInfo.class, employmentId);
      empInfoId = info.getEhcmEmpPerinfo().getId();
      pt = conn
          .prepareStatement("DELETE FROM EHCM_Emp_SupervisorNode WHERE Ehcm_Emp_Perinfo_ID = ?");
      pt.setString(1, empInfoId);
      pt.executeUpdate();

      st = conn
          .prepareStatement("DELETE FROM ehcm_employment_info WHERE ehcm_employment_info_id = ?");
      st.setString(1, employmentId);
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
        pt.close();
      } catch (final SQLException e) {
        log4j.error("", e);
        return false;
      }
    }
    return true;
  }

  /**
   * 
   * @param ClientId
   * @return gradeList
   */

  @SuppressWarnings("resource")
  public synchronized static JSONObject getGradeList(String clientId, String searchTerm,
      int pagelimit, int page, String roleId, String orgId) {
    log4j.debug("Inside get grade list");
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(distinct gd.ehcm_grade_id) as count ");
      selectQuery.append(" select distinct gd.value as gdName , gd.ehcm_grade_id as gdId ");
      fromQuery.append(" from ehcm_grade gd where ad_org_id in ("
          + Utility.getChildOrg(clientId, orgId)
          + ") and ad_org_id in( select ad_org_id from ad_role_orgaccess where ad_role_id = ? and ad_client_id = ? ) and ad_client_id=? ");

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and ( gd.value ilike '%" + searchTerm.toLowerCase() + "%' )");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());

      st.setString(1, roleId);
      st.setString(2, clientId);
      st.setString(3, clientId);

      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);

      if (totalRecords > 0) {
        fromQuery.append(" order by gd.value limit ? offset ? ");
        st = conn.prepareStatement((selectQuery.append(fromQuery)).toString());
        st.setString(1, roleId);
        st.setString(2, clientId);
        st.setString(3, clientId);
        st.setInt(4, pagelimit);
        st.setInt(5, (page - 1) * pagelimit);
      }
      log4j.debug(st);
      rs = st.executeQuery();

      while (rs.next()) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("id", rs.getString("gdId"));
        jsonData.put("recordIdentifier", rs.getString("gdName"));
        jsonArray.put(jsonData);
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      e.printStackTrace();
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

  public List<ehcmgrade> getEmpGrade(String ClientId, String GradeId, Long seqNo) {
    OBQuery<ehcmgrade> empgradeList = null;
    try {
      empgradeList = OBDal.getInstance().createQuery(ehcmgrade.class, "as e where e.client.id='"
          + ClientId + "' and e.sequenceNumber <='" + seqNo + "' order by  e.sequenceNumber desc ");
      empgradeList.setMaxResult(3);
      empgradeList.setFilterOnReadableOrganization(false);
    } catch (Exception e) {
      e.printStackTrace();
      log4j.debug("Exception getEmpGrade :" + e);
    }
    return empgradeList.list();
  }

  @SuppressWarnings("resource")
  public synchronized static JSONObject getEmpGrade(String GradeId, Long seqNo, String clientId,
      String searchTerm, String roleId, String orgId) {
    log4j.debug("Inside get Employee Grade list");
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(distinct gd.ehcm_grade_id) as count ");
      selectQuery
          .append(" select distinct gd.value as gdNo , gd.ehcm_grade_id as gdId, gd.seqno as seq ");
      fromQuery.append(" from ehcm_grade gd where ad_org_id in ("
          + Utility.getChildOrg(clientId, orgId)
          + ") and ad_org_id in( select ad_org_id from ad_role_orgaccess where ad_role_id = ? and ad_client_id = ? ) and ad_client_id=? "
          + "and gd.seqno <= ? ");

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and ( gd.value ilike '%" + searchTerm.toLowerCase() + "%' )");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());

      st.setString(1, roleId);
      st.setString(2, clientId);
      st.setString(3, clientId);
      st.setLong(4, seqNo);
      log4j.debug(st);

      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);

      if (totalRecords > 0) {
        fromQuery.append(" order by gd.seqno desc limit 3 ");
        st = conn.prepareStatement((selectQuery.append(fromQuery)).toString());
        st.setString(1, roleId);
        st.setString(2, clientId);
        st.setString(3, clientId);
        st.setLong(4, seqNo);
      }
      log4j.debug(st);
      rs = st.executeQuery();

      while (rs.next()) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("id", rs.getString("gdId"));
        jsonData.put("recordIdentifier", rs.getString("gdNo"));
        jsonArray.put(jsonData);
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      e.printStackTrace();
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

  @SuppressWarnings("resource")
  public synchronized static JSONObject getPayrollDefinition(String clientId, String searchTerm,
      int pagelimit, int page, String roleId, String orgId) {
    log4j.debug("Inside get Payroll Definition list");
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(distinct payrl.ehcm_payroll_definition_id) as count ");
      selectQuery.append(
          " select distinct payrl.payroll_name as payrlNo , payrl.ehcm_payroll_definition_id as payrlId ");
      fromQuery.append(" from ehcm_payroll_definition payrl where ad_org_id in ("
          + Utility.getChildOrg(clientId, orgId)
          + ") and ad_org_id in( select ad_org_id from ad_role_orgaccess where ad_role_id = ? and ad_client_id = ? ) and ad_client_id=? ");

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and ( payrl.payroll_name ilike '%" + searchTerm.toLowerCase() + "%' )");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());

      st.setString(1, roleId);
      st.setString(2, clientId);
      st.setString(3, clientId);
      log4j.debug(st);

      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);

      if (totalRecords > 0) {
        fromQuery.append(" order by payrl.payroll_name limit ? offset ? ");
        st = conn.prepareStatement((selectQuery.append(fromQuery)).toString());
        st.setString(1, roleId);
        st.setString(2, clientId);
        st.setString(3, clientId);
        st.setInt(4, pagelimit);
        st.setInt(5, (page - 1) * pagelimit);
      }
      log4j.debug(st);
      rs = st.executeQuery();

      while (rs.next()) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("id", rs.getString("payrlId"));
        jsonData.put("recordIdentifier", rs.getString("payrlNo"));
        jsonArray.put(jsonData);
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      e.printStackTrace();
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

  /**
   * 
   * @param GradeId
   * @return position List corresponding to Grade
   */
  @SuppressWarnings("resource")
  public synchronized static JSONObject getPositionList(String GradeId, String inpEmployeeId,
      String startDate, String clientId, String searchTerm, int pagelimit, int page, String roleId,
      String orgId, String inpEmploymentId) {
    log4j.debug("Inside get position list");
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    String startdate = null;
    try {
      EmploymentInfo employment = OBDal.getInstance().get(EmploymentInfo.class, inpEmploymentId);

      startdate = UtilityDAO.convertToGregorian(startDate);

      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(distinct pos.ehcm_position_id) as count ");
      selectQuery.append(" select distinct pos.job_no as posNo , pos.ehcm_position_id as posId ");
      fromQuery.append(" from ehcm_position pos where ad_org_id in ("
          + Utility.getChildOrg(clientId, orgId)
          + ") and ad_org_id in( select ad_org_id from ad_role_orgaccess where ad_role_id = ? and ad_client_id = ? ) and ad_client_id=? "
          + "and pos.transaction_status='I' and pos.ehcm_grade_id= ?"
          + " and pos.isactive='Y' and pos.ehcm_postransactiontype_id in "
          + " ( select t.ehcm_postransactiontype_id from ehcm_postransactiontype t where t.value not in ('CAPO','TROPO') ) "
          + " and (ehcm_position_id not  in    (select  ehcm_position_id   from ehcm_posemp_history where case  when ehcm_posemp_history.enddate is not null then "
          + " ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(?,'yyyy-MM-dd') "
          + " and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') "
          + " <= to_date('21-06-2058','dd-MM-yyyy'))  or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') "
          + "   >= to_date(?,'yyyy-MM-dd')       and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('21-06-2058','dd-MM-yyyy')))    "
          + " else ehcm_posemp_history.enddate is  null end and ehcm_position_id is not null  )) ");

      if (employment != null && GradeId.equals(employment.getGrade().getId())) {
        fromQuery.append(
            " or ehcm_position_id in (select ehcm_position_id from ehcm_employment_info as a where a.ehcm_employment_info_id = '"
                + inpEmploymentId + "') ");
      }

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and ( pos.job_no ilike '%" + searchTerm.toLowerCase() + "%' )");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());

      st.setString(1, roleId);
      st.setString(2, clientId);
      st.setString(3, clientId);
      st.setString(4, GradeId);
      st.setString(5, startdate);
      st.setString(6, startdate);
      // st.setString(7, inpEmployeeId);
      log4j.debug(st);

      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);

      if (totalRecords > 0) {
        fromQuery.append(" order by pos.job_no limit ? offset ? ");
        st = conn.prepareStatement((selectQuery.append(fromQuery)).toString());
        st.setString(1, roleId);
        st.setString(2, clientId);
        st.setString(3, clientId);
        st.setString(4, GradeId);
        st.setString(5, startdate);
        st.setString(6, startdate);
        // st.setString(7, inpEmployeeId);
        st.setInt(7, pagelimit);
        st.setInt(8, (page - 1) * pagelimit);

        log4j.debug(st);
        rs = st.executeQuery();

        while (rs.next()) {
          JSONObject jsonData = new JSONObject();
          jsonData.put("id", rs.getString("posId"));
          jsonData.put("recordIdentifier", rs.getString("posNo"));
          jsonArray.put(jsonData);
        }
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      e.printStackTrace();
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

  /**
   * 
   * @param strPostionId
   * @param strGradeId
   * @return Postion Details
   */
  public List<EhcmPosition> getJobDetailsList(String strPostionId, String strGradeId) {
    OBQuery<EhcmPosition> jobList = null;
    try {
      jobList = OBDal.getInstance().createQuery(EhcmPosition.class,
          " as e where e.id='" + strPostionId + "' and e.grade.id='" + strGradeId + "'");
      jobList.setMaxResult(1);
    } catch (Exception e) {
      log4j.debug("Exception getJobDetailsList :" + e);
    }
    return jobList.list();
  }

  public static String getGradeClass(String strGradeClassId) {
    OBQuery<ehcmgradeclass> gclasslist = null;
    String returndata = null;
    try {
      gclasslist = OBDal.getInstance().createQuery(ehcmgradeclass.class,
          " as e where e.id='" + strGradeClassId + "'");
      gclasslist.setMaxResult(1);
      returndata = gclasslist.list().get(0).getSearchKey() + "-"
          + gclasslist.list().get(0).getName();
    } catch (Exception e) {
      log4j.debug("Exception getJobDetailsList :" + e);
    }
    return returndata;
  }

  /**
   * 
   * @param clientId
   * @return employmentCategory List
   */

  @SuppressWarnings("resource")
  public synchronized static JSONObject getEmploymentCatList(String clientId, String searchTerm,
      int pagelimit, int page, String roleId, String orgId) {
    log4j.debug("Inside get Employment Cat list");
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(distinct gd.ehcm_gradeclass_id) as count ");
      selectQuery.append(
          " select distinct gd.value as gdValue ,gd.name as gdName, gd.ehcm_gradeclass_id as gdId ");
      fromQuery.append(" from ehcm_gradeclass gd where ad_org_id in ("
          + Utility.getChildOrg(clientId, orgId)
          + ") and ad_org_id in( select ad_org_id from ad_role_orgaccess where ad_role_id = ? and ad_client_id = ? ) and ad_client_id=? ");
      log4j.debug(searchTerm);
      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and ( gd.value ilike '%" + searchTerm.toLowerCase() + "%' )");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());

      st.setString(1, roleId);
      st.setString(2, clientId);
      st.setString(3, clientId);

      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);

      if (totalRecords > 0) {
        fromQuery.append(" order by gd.value limit ? offset ? ");
        st = conn.prepareStatement((selectQuery.append(fromQuery)).toString());
        st.setString(1, roleId);
        st.setString(2, clientId);
        st.setString(3, clientId);
        st.setInt(4, pagelimit);
        st.setInt(5, (page - 1) * pagelimit);
      }
      log4j.debug(st);
      rs = st.executeQuery();

      while (rs.next()) {
        JSONObject jsonData = new JSONObject();
        String combineData = rs.getString("gdValue") + "-" + rs.getString("gdName");
        jsonData.put("id", rs.getString("gdId"));
        jsonData.put("recordIdentifier", combineData);
        jsonArray.put(jsonData);
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      e.printStackTrace();
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

  /**
   * 
   * @param clientId
   * @return pay scale List
   */

  @SuppressWarnings("resource")
  public synchronized static JSONObject getPayscaleList(String strEmpGradeId, String clientId,
      String searchTerm, int pagelimit, int page, String roleId, String orgId) {
    log4j.debug("Inside get Payscale list");
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(distinct payscl.ehcm_payscale_id) as count ");
      selectQuery.append(
          " select distinct payscl.name as paysclNo , payscl.ehcm_payscale_id as paysclId ");
      fromQuery.append(" from ehcm_payscale payscl where ad_org_id in ("
          + Utility.getChildOrg(clientId, orgId)
          + ") and ad_org_id in( select ad_org_id from ad_role_orgaccess where ad_role_id = ? and ad_client_id = ? ) and ad_client_id=? "
          + "and payscl.ehcm_grade_id = ?");

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and ( payscl.name ilike '%" + searchTerm.toLowerCase() + "%' )");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());

      log4j.debug(strEmpGradeId);

      st.setString(1, roleId);
      st.setString(2, clientId);
      st.setString(3, clientId);
      st.setString(4, strEmpGradeId);
      log4j.debug(st);

      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);
      log4j.debug(totalRecords);

      if (totalRecords > 0) {
        fromQuery.append(" order by payscl.name limit ? offset ? ");
        st = conn.prepareStatement((selectQuery.append(fromQuery)).toString());
        st.setString(1, roleId);
        st.setString(2, clientId);
        st.setString(3, clientId);
        st.setString(4, strEmpGradeId);
        st.setInt(5, pagelimit);
        st.setInt(6, (page - 1) * pagelimit);

        log4j.debug(st);
        rs = st.executeQuery();

        while (rs.next()) {
          JSONObject jsonData = new JSONObject();
          jsonData.put("id", rs.getString("paysclId"));
          jsonData.put("recordIdentifier", rs.getString("paysclNo"));
          jsonArray.put(jsonData);
        }
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      e.printStackTrace();
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

  /**
   * 
   * @param clientId
   * @return payscale
   */

  @SuppressWarnings("resource")
  public synchronized static JSONObject getGradeStepList(String strPayScaleId, String clientId,
      String searchTerm, int pagelimit, int page, String roleId, String orgId) {
    log4j.debug("Inside get Payscale list");
    log4j.debug(strPayScaleId);
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();
      countQuery.append(" select count(distinct payscl.ehcm_payscaleline_id) as count ");
      selectQuery
          .append(" select distinct ppt.point as value , payscl.ehcm_payscaleline_id as paysclId ");
      fromQuery.append(
          " from ehcm_payscaleline payscl JOIN ehcm_progressionpt ppt ON ppt.ehcm_progressionpt_id=payscl.ehcm_progressionpt_id"
              + " where payscl.ad_org_id in (" + Utility.getChildOrg(clientId, orgId)
              + ") and payscl.ad_org_id in( select ad_org_id from ad_role_orgaccess where ad_role_id = ? and ad_client_id = ? ) "
              + "and payscl.ad_client_id=? and payscl.ehcm_payscale_id = ?");

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and ( ppt.point ilike '%" + searchTerm.toLowerCase() + "%' )");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());

      st.setString(1, roleId);
      st.setString(2, clientId);
      st.setString(3, clientId);
      st.setString(4, strPayScaleId);
      log4j.debug(st);

      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);

      if (totalRecords > 0) {
        fromQuery.append(" order by ppt.point limit ? offset ? ");
        st = conn.prepareStatement((selectQuery.append(fromQuery)).toString());
        st.setString(1, roleId);
        st.setString(2, clientId);
        st.setString(3, clientId);
        st.setString(4, strPayScaleId);
        st.setInt(5, pagelimit);
        st.setInt(6, (page - 1) * pagelimit);
      }
      log4j.debug(st);
      rs = st.executeQuery();

      while (rs.next()) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("id", rs.getString("paysclId"));
        jsonData.put("recordIdentifier", rs.getString("value"));
        jsonArray.put(jsonData);
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      e.printStackTrace();
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

  public List<EmploymentVO> getSearchEmployee(String clientId, JSONObject searchAttr) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<EmploymentVO> ls = new ArrayList<EmploymentVO>();
    EmploymentVO eVO = null;
    StringBuilder whereClause = new StringBuilder();
    String orderClause = "";
    try {

      whereClause.append("where ad_client_id = '" + clientId + "' ");

      if (searchAttr.has("fname"))
        whereClause.append(" and concat(name,' ',fathername,' ',grandfathername) ilike '%")
            .append(searchAttr.getString("fname")).append("%'");
      if (searchAttr.has("aname"))
        whereClause.append(
            " and concat(arabicname,' ',arabicfatname,' ',arbgrafaname,' ',arabicfamilyname) ilike '%")
            .append(searchAttr.getString("aname")).append("%'");
      if (searchAttr.has("empno"))
        whereClause.append(" and value ilike '%").append(searchAttr.getString("empno"))
            .append("%'");
      if (searchAttr.getString("sortName").equals("empno")) {
        orderClause = " order by value asc";
      } else {
        orderClause = " order by " + searchAttr.getString("sortName") + " asc";
      }

      // Employee Details
      StringBuilder sqlQuery = new StringBuilder(
          " select ehcm_emp_perinfo_id as id,concat(name,' ',fathername,' ',grandfathername) as fname ,concat(arabicname,' ',arabicfatname,' ',arbgrafaname,' ',arabicfamilyname) as aname,value from ehcm_emp_perinfo ");
      sqlQuery.append(whereClause);
      sqlQuery.append(orderClause);
      sqlQuery.append(" limit 10 offset 0");
      log4j.debug("employee no search:" + sqlQuery);
      st = conn.prepareStatement(sqlQuery.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        eVO = new EmploymentVO();
        eVO.setEmployeeId(rs.getString("id"));
        eVO.setEmploymentNo(rs.getString("value"));
        eVO.setFullName(Utility.nullToEmpty(rs.getString("fname")));
        eVO.setArabicName(Utility.nullToEmpty(rs.getString("aname")));
        ls.add(eVO);
      }
    } catch (final Exception e) {
      log4j.error("Exception in getSearchEmployee", e);
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getSearchEmployee", e);
      }
    }
    return ls;
  }

  /**
   * 
   * @param clientId
   * @param userId
   * @param EmploymentVO
   * @return employmentId
   */
  public String addEmployment(String clientId, String userId, EmploymentVO vo,
      VariablesSecureApp vars) {
    String employmentId = "";
    try {
      OBContext.setAdminMode();
      EmploymentInfo employInfo = OBProvider.getInstance().get(EmploymentInfo.class);
      employInfo.setChangereason(vo.getChangeReason());
      employInfo.setDepartmentName(vo.getDeptName());
      employInfo.setDeptcode(OBDal.getInstance().get(Organization.class, vo.getDeptCode()));
      employInfo.setEhcmPayscale(OBDal.getInstance().get(ehcmpayscale.class, vo.getPayScaleId()));
      employInfo.setEmpcategory(vo.getEmploymentCategoryId());
      employInfo.setEmployeeno(vo.getEmploymentNo());
      employInfo.setGrade(OBDal.getInstance().get(ehcmgrade.class, vo.getGradeId()));
      employInfo.setEmploymentgrade(OBDal.getInstance().get(ehcmgrade.class, vo.getEmpGrade()));
      employInfo.setEhcmPayscaleline(
          OBDal.getInstance().get(ehcmpayscaleline.class, vo.getGradeStepId()));
      employInfo.setJobcode(OBDal.getInstance().get(Jobs.class, vo.getJobCode()));
      // employInfo.setJobGroup(OBDal.getInstance().get(EhcmJobGroup.class, vo.getj))
      employInfo.setPosition(OBDal.getInstance().get(EhcmPosition.class, vo.getJobNo()));
      employInfo.setJobtitle(vo.getJobTitle());
      employInfo.setLocation(vo.getLocation());
      // employInfo.setPayroll(vo.getPayroll());
      employInfo.setEhcmPayrollDefinition(
          OBDal.getInstance().get(EHCMPayrollDefinition.class, vo.getEhcmPayrollDefinition()));
      employInfo.setSectionName(vo.getSectionName());
      if (vo.getSectionCode() != null)
        employInfo.setSectioncode(OBDal.getInstance().get(Organization.class, vo.getSectionCode()));
      employInfo
          .setEhcmEmpPerinfo(OBDal.getInstance().get(EhcmEmpPerInfo.class, vo.getEmployeeId()));
      if (vo.getStartDate() != null && !vo.getStartDate().equals("")) {
        employInfo.setStartDate(convertGregorian(vo.getStartDate()));
      }
      if (vo.getEndDate() != null && !vo.getEndDate().equals("")) {
        employInfo.setEndDate(convertGregorian(vo.getEndDate()));
      }
      employInfo.setDecisionNo(vo.getDecisionNo());
      employInfo.setAlertStatus("ACT");

      // insert supervisorId
      if (StringUtils.isNotEmpty(vo.getEmpSupervisorId()))
        employInfo.setEhcmEmpSupervisor(
            OBDal.getInstance().get(EHCMEmpSupervisor.class, vo.getEmpSupervisorId()));
      OBDal.getInstance().save(employInfo);

      // insert supervisor node
      if (employInfo.getEhcmEmpSupervisor() != null) {
        insertEmpSuperVisorNode(employInfo.getEhcmEmpSupervisor().getId(),
            employInfo.getEhcmEmpPerinfo().getId(), vars);
      }
      employInfo.setJoinworkreq(vo.getJoinworkRequest());
      OBDal.getInstance().flush();
      employmentId = employInfo.getId();
    } catch (Exception e) {
      log4j.error("error while updateEmployment", e);
      return null;
      // TODO: handle exception
    } finally {
      OBContext.restorePreviousMode();
    }
    return employmentId;
  }

  /**
   * 
   * @param clientId
   * @param userId
   * @param vo
   * @param employmentId
   * @return employmentId
   */

  public String updateEmployment(String clientId, String userId, EmploymentVO vo,
      String employmentId, VariablesSecureApp vars) {
    String strEmplymentId = "";
    String oldSupervisorId = null;
    String preferenceName = "EHCM_EmpInfo_Update";
    String listFlag = "Y";
    try {
      OBContext.setAdminMode();
      EmploymentInfo employInfo = OBDal.getInstance().get(EmploymentInfo.class, employmentId);
      log4j.debug("vo:" + vo.getJobTitle() + "-" + vo.getLocation() + "-" + vo.getPayroll() + "-"
          + vo.getSectionName() + "-" + vo.getSectionCode());
      if (vo.getChangeReason() != null)
        employInfo.setChangereason(vo.getChangeReason());
      employInfo.setDepartmentName(vo.getDeptName());
      employInfo.setDeptcode(OBDal.getInstance().get(Organization.class, vo.getDeptCode()));
      if (StringUtils.isNotEmpty(vo.getPayScaleId()))
        employInfo.setEhcmPayscale(OBDal.getInstance().get(ehcmpayscale.class, vo.getPayScaleId()));
      employInfo.setEmpcategory(vo.getEmploymentCategoryId());
      employInfo.setEmployeeno(vo.getEmploymentNo());
      if (StringUtils.isNotEmpty(vo.getGradeId())) {
        employInfo.setGrade(OBDal.getInstance().get(ehcmgrade.class, vo.getGradeId()));
      }
      if (StringUtils.isNotEmpty(vo.getEmpGrade())) {
        employInfo.setEmploymentgrade(OBDal.getInstance().get(ehcmgrade.class, vo.getEmpGrade()));
      }
      if (StringUtils.isNotEmpty(vo.getGradeStepId())) {
        employInfo.setEhcmPayscaleline(
            OBDal.getInstance().get(ehcmpayscaleline.class, vo.getGradeStepId()));
        employInfo.setJobcode(OBDal.getInstance().get(Jobs.class, vo.getJobCode()));
      }
      if (StringUtils.isNotEmpty(vo.getGradeStepId())) {
        employInfo.setPosition(OBDal.getInstance().get(EhcmPosition.class, vo.getJobNo()));
      }
      employInfo.setJobtitle(vo.getJobTitle());
      employInfo.setLocation(vo.getLocation());
      // employInfo.setPayroll(vo.getPayroll());
      if (StringUtils.isNotEmpty(vo.getEhcmPayrollDefinition())) {
        employInfo.setEhcmPayrollDefinition(
            OBDal.getInstance().get(EHCMPayrollDefinition.class, vo.getEhcmPayrollDefinition()));
      }
      if (vo.getSectionName() != null)
        employInfo.setSectionName(vo.getSectionName());
      if (vo.getSectionCode() != null)
        employInfo.setSectioncode(OBDal.getInstance().get(Organization.class, vo.getSectionCode()));
      if (vo.getStartDate() != null && !vo.getStartDate().equals("")) {
        employInfo.setStartDate(convertGregorian(vo.getStartDate()));
      }
      if (vo.getEndDate() != null && !vo.getEndDate().equals("")) {
        employInfo.setEndDate(convertGregorian(vo.getEndDate()));
      }
      employInfo.setAlertStatus("ACT");
      OBDal.getInstance().save(employInfo);

      if ((employInfo.getEhcmEmpSupervisor() != null
          && !employInfo.getEhcmEmpSupervisor().getId().equals(vo.getEmpSupervisorId()))
          || employInfo.getEhcmEmpSupervisor() == null) {
        // take old supervisor Id
        if (employInfo.getEhcmEmpSupervisor() != null
            && (!employInfo.getEhcmEmpSupervisor().getId().equals(vo.getEmpSupervisorId())))
          oldSupervisorId = employInfo.getEhcmEmpSupervisor().getId();

        // update supervisorId
        log4j.debug("getEmpSupervisorId" + vo.getEmpSupervisorId());
        if (StringUtils.isNotEmpty(vo.getEmpSupervisorId())) {
          employInfo.setEhcmEmpSupervisor(
              OBDal.getInstance().get(EHCMEmpSupervisor.class, vo.getEmpSupervisorId()));
          OBDal.getInstance().save(employInfo);

        }

        // create preference
        Preferences.setPreferenceValue(preferenceName, listFlag, false, employInfo.getClient(),
            employInfo.getOrganization(), null, null, null, null);
        OBDal.getInstance().flush();
        // insert supervisor node
        if (employInfo.getEhcmEmpSupervisor() != null
            && StringUtils.isNotEmpty(vo.getEmpSupervisorId())) {
          insertEmpSuperVisorNode(employInfo.getEhcmEmpSupervisor().getId(),
              employInfo.getEhcmEmpPerinfo().getId(), vars);
        }

        // delete preference
        Preference preference = getPreference(preferenceName);
        OBDal.getInstance().remove(preference);

        // delete employee from old supervisor node
        if (StringUtils.isNotEmpty(oldSupervisorId)) {
          deleteEmpSupervisorNode(oldSupervisorId, employInfo.getEhcmEmpPerinfo().getId(), vars);
        }
      }

      OBDal.getInstance().flush();
      strEmplymentId = employInfo.getId();
    } catch (Exception e) {
      log4j.error("error while updateEmployment", e);
      return null;
      // TODO: handle exception
    } finally {
      OBContext.restorePreviousMode();
    }
    return strEmplymentId;
  }

  /**
   * 
   * @param hijridate
   * @return gregorian Date
   */
  public Date convertGregorian(String hijridate) {
    log4j.debug("hi:" + hijridate);
    String gregDate = Utility.convertToGregorian(hijridate);
    log4j.debug("gregDate:" + gregDate);
    Date greDate = null;
    try {
      DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
      greDate = df1.parse(gregDate);
      log4j.debug("greDate:" + greDate);
    } catch (Exception e) {
      log4j.error("Exception in convertGregorian", e);

    }
    return greDate;

  }

  /**
   * 
   * @param clientId
   * @param employeeId
   * @param vo
   * @param limit
   * @param offset
   * @param sortColName
   * @param sortColType
   * @param searchFlag
   * @return Employmentlist
   */
  public List<EmploymentVO> getEmployMentList(String clientId, String employeeId, EmploymentVO vo,
      int limit, int offset, String sortColName, String sortColType, String searchFlag,
      String lang) {
    log4j.debug("sort" + sortColType);
    PreparedStatement st = null;
    ResultSet rs = null;
    List<EmploymentVO> ls = new ArrayList<EmploymentVO>();
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    String sqlQuery = "";
    String date = "";

    try {
      sqlQuery = " select emp.ehcm_employment_info_id as id,grade.value as grade,pos.job_no as jobno, "
          + " job.value as jobcode,org.value as deptcode,orgsec.value as seccode, "
          + " psc.name as pscale,po.point as gstep,emp.employeeno as empno ,emp.startdate,emp.enddate,emp.status,ref.name as changereason,emp.changereason as changereasonemp, coalesce(emp.changereasoninfo,'') as changereasoninfo,empgrade.value as empgrade "
          + " from ehcm_employment_info emp left join ehcm_grade grade on grade.ehcm_grade_id=emp.ehcm_grade_id left join ehcm_grade empgrade on empgrade.ehcm_grade_id=emp.employmentgrade "
          + " left join ehcm_position pos on pos.ehcm_position_id =emp.ehcm_position_id "
          + " left join ehcm_payscale psc on psc.ehcm_payscale_id =emp.ehcm_payscale_id "
          + " left join ehcm_payscaleline gstep on gstep.ehcm_payscaleline_id=emp.ehcm_payscaleline_id"
          + " left join ehcm_progressionpt po on po.ehcm_progressionpt_id =gstep.ehcm_progressionpt_id "
          + " left join ad_org org on org.ad_org_id=emp.deptcode "
          + " left join ad_org orgsec on orgsec.ad_org_id=emp.sectioncode "
          + " left join ehcm_jobs job on job.ehcm_jobs_id=emp.jobcode ";
      if (lang.equals("ar_SA")) {
        sqlQuery = sqlQuery
            + " left join (select coalesce(tr.name,list.name) as name,list.value as value ";
      } else {
        sqlQuery = sqlQuery + " left join (select list.name as name,list.value as value ";
      }
      sqlQuery = sqlQuery
          + " from ad_ref_list list left join ad_ref_list_trl tr on list.ad_ref_list_id = tr.ad_ref_list_id  where ad_reference_id=?) ref on ref.value=emp.changereason "
          + " where emp.ehcm_emp_perinfo_id =? ";

      log4j.debug("query" + sqlQuery.toString());
      if (searchFlag.equals("true")) {
        if (vo.getGrade() != null)
          sqlQuery += " and grade.value ilike '%" + vo.getGrade() + "%'";
        if (vo.getJobNo() != null)
          sqlQuery += " and pos.job_no ilike '%" + vo.getJobNo() + "%'";
        if (vo.getJobCode() != null)
          sqlQuery += " and emp.jobcode ilike '%" + vo.getJobCode() + "%'";
        if (vo.getDeptCode() != null)
          sqlQuery += " and org.value ilike '%" + vo.getDeptCode() + "%'";
        if (vo.getSectionCode() != null)
          sqlQuery += " and orgsec.value ilike '%" + vo.getSectionCode() + "%'";
        if (vo.getPayscale() != null)
          sqlQuery += " and psc.name ilike '%" + vo.getPayscale() + "%'";
        if (vo.getGradeStep() != null)
          sqlQuery += " and po.point ilike '%" + vo.getGradeStep() + "%'";
        if (vo.getEmpGrade() != null)
          sqlQuery += " and empgrade.value ilike '%" + vo.getEmpGrade() + "%'";
        if (vo.getStatus() != null && !vo.getStatus().equals("0"))
          sqlQuery += " and emp.status ='" + vo.getStatus() + "'";
        if (vo.getChangeReason() != null && !vo.getChangeReason().equals("0"))
          sqlQuery += " and emp.changereason ='" + vo.getChangeReason() + "'";
        if (!StringUtils.isEmpty(vo.getStartDate()))
          sqlQuery += " and emp.startdate " + vo.getStartDate().split("##")[0] + " to_timestamp('"
              + vo.getStartDate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";
        if (!StringUtils.isEmpty(vo.getEndDate()))
          sqlQuery += " and emp.enddate " + vo.getEndDate().split("##")[0] + " to_timestamp('"
              + vo.getEndDate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";

      }

      if (sortColName != null && sortColName.equals("grade"))
        sqlQuery += " order by grade.value  " + sortColType + " limit " + limit + " offset "
            + offset;
      else if (sortColName != null && sortColName.equals("jobno"))
        sqlQuery += " order by pos.job_no " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("jobno"))
        sqlQuery += " order by pos.job_no " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("jobcode"))
        sqlQuery += " order by emp.jobcode " + sortColType + " limit " + limit + " offset "
            + offset;
      else if (sortColName != null && sortColName.equals("DepartmentCode"))
        sqlQuery += " order by emp.deptcode " + sortColType + " limit " + limit + " offset "
            + offset;
      else if (sortColName != null && sortColName.equals("sectioncode"))
        sqlQuery += " order by emp.sectioncode " + sortColType + " limit " + limit + " offset "
            + offset;
      else if (sortColName != null && sortColName.equals("Payscale"))
        sqlQuery += " order by psc.name  " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("EmploymentGrade"))
        sqlQuery += " order by empgrade.value  " + sortColType + " limit " + limit + " offset "
            + offset;
      else if (sortColName != null && sortColName.equals("gradeStep"))
        sqlQuery += " order by po.point   " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("enddate"))
        sqlQuery += " order by emp.enddate desc " + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("startdate"))
        sqlQuery += " order by emp.startdate desc " + " limit " + limit + " offset " + offset;
      else
        sqlQuery += " order by emp.startdate desc " + " limit " + limit + " offset " + offset;

      log4j.debug("DAO select Query:" + sqlQuery + ">> employeeId:" + employeeId + ">> ref:"
          + EmpInfo_ChangeReason_List_ID);
      st = conn.prepareStatement(sqlQuery);
      st.setString(1, EmpInfo_ChangeReason_List_ID);
      st.setString(2, employeeId);
      rs = st.executeQuery();
      while (rs.next()) {
        EmploymentVO empVO = new EmploymentVO();
        empVO.setEmploymnetId(Utility.nullToEmpty(rs.getString("id")));
        empVO.setGrade(Utility.nullToEmpty(rs.getString("grade")));
        empVO.setJobNo(Utility.nullToEmpty(rs.getString("jobno")));
        empVO.setJobCode(Utility.nullToEmpty(rs.getString("jobcode")));
        empVO.setDeptCode(Utility.nullToEmpty(rs.getString("deptcode")));
        empVO.setSectionCode(Utility.nullToEmpty(rs.getString("seccode")));
        empVO.setPayscale(Utility.nullToEmpty(rs.getString("pscale")));
        empVO.setGradeStep(Utility.nullToEmpty(rs.getString("gstep")));
        empVO.setEmploymentNo(Utility.nullToEmpty(rs.getString("empno")));
        if (rs.getString("status") != null && rs.getString("status").equalsIgnoreCase("ACT")) {
          empVO.setStatus(Resource.getProperty("hcm.active", lang));
        } else if (rs.getString("status") != null
            && rs.getString("status").equalsIgnoreCase("TE")) {
          empVO.setStatus(Resource.getProperty("hcm.terminate", lang));
        } else
          empVO.setStatus(Resource.getProperty("hcm.inactive", lang));
        empVO.setEmpGrade(Utility.nullToZero(rs.getString("empgrade")));
        empVO.setChangeReason(Utility.nullToEmpty(rs.getString("changereason")));
        empVO.setChangereasonemp(Utility.nullToEmpty(rs.getString("changereasonemp")));
        empVO.setChangereasoninfo(Utility.nullToEmpty(rs.getString("changereasoninfo")));
        if (rs.getDate("startdate") != null) {
          date = df.format(rs.getDate("startdate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          empVO.setStartDate(date);
        } else
          empVO.setStartDate("");
        if (rs.getDate("enddate") != null) {
          date = df.format(rs.getDate("enddate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          empVO.setEndDate(date);
        } else
          empVO.setEndDate("");
        ls.add(empVO);
      }
    } catch (final SQLException e) {
      log4j.error("", e);
    } catch (final Exception e) {
      log4j.error("", e);
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("", e);
      }
    }
    return ls;
  }

  /**
   * 
   * @param clientId
   * @param employeeId
   * @param searchFlag
   * @param vo
   * @return EmploymentCount
   */
  public int getEmploymentCount(String clientId, String employeeId, String searchFlag,
      EmploymentVO vo) {
    PreparedStatement st = null;
    ResultSet rs = null;
    int totalRecord = 0;
    String sqlQuery = "";
    try {
      sqlQuery = "select count(*) as totalRecord from ehcm_employment_info emp "
          + " join ehcm_grade grade on grade.ehcm_grade_id=emp.ehcm_grade_id join ehcm_grade empgrade on empgrade.ehcm_grade_id=emp.employmentgrade "
          + " join ehcm_position pos on pos.ehcm_position_id =emp.ehcm_position_id "
          + " left join ehcm_payscale psc on psc.ehcm_payscale_id =emp.ehcm_payscale_id "
          + " left join ehcm_payscaleline gstep on gstep.ehcm_payscaleline_id=emp.ehcm_payscaleline_id "
          + "  left join ehcm_progressionpt po on po.ehcm_progressionpt_id =gstep.ehcm_progressionpt_id where emp.ehcm_emp_perinfo_id=? ";
      if (searchFlag.equals("true")) {
        if (vo.getGrade() != null)
          sqlQuery += " and grade.value ilike '%" + vo.getGrade() + "%'";
        if (vo.getJobNo() != null)
          sqlQuery += " and pos.job_no ilike '%" + vo.getJobNo() + "%'";
        if (vo.getJobCode() != null)
          sqlQuery += " and emp.jobcode ilike '%" + vo.getJobCode() + "%'";
        if (vo.getDeptCode() != null)
          sqlQuery += " and emp.deptcode ilike '%" + vo.getDeptCode() + "%'";
        if (vo.getSectionCode() != null)
          sqlQuery += " and emp.sectioncode ilike '%" + vo.getSectionCode() + "%'";
        if (vo.getPayscale() != null)
          sqlQuery += " and psc.name ilike '%" + vo.getPayscale() + "%'";
        if (vo.getGradeStep() != null)
          sqlQuery += " and po.point ilike '%" + vo.getGradeStep() + "%'";
        if (vo.getEmpGrade() != null)
          sqlQuery += " and empgrade.value ilike '%" + vo.getEmpGrade() + "%'";
        if (vo.getStatus() != null)
          sqlQuery += " and emp.status ilike '%" + vo.getStatus() + "%'";
        if (!StringUtils.isEmpty(vo.getStartDate()))
          sqlQuery += " and emp.startdate " + vo.getStartDate().split("##")[0] + " to_timestamp('"
              + vo.getStartDate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";
        if (!StringUtils.isEmpty(vo.getEndDate()))
          sqlQuery += " and emp.enddate " + vo.getEndDate().split("##")[0] + " to_timestamp('"
              + vo.getEndDate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";

      }
      st = conn.prepareStatement(sqlQuery);
      st.setString(1, employeeId);
      rs = st.executeQuery();
      if (rs.next())
        totalRecord = rs.getInt("totalRecord");
    } catch (final SQLException e) {
      log4j.error("", e);
    } catch (final Exception e) {
      log4j.error("", e);
    } finally {
      try {
        st.close();
      } catch (final SQLException e) {
        log4j.error("", e);
      }
    }
    return totalRecord;
  }

  public String getDocTypeValue(String clientId, String value, boolean isvalue) {
    String name = "";
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = conn.prepareStatement(
          " select name from ad_ref_list  where ad_reference_id  = ?   and  value= ?    order by created desc  ");
      st.setString(1, "C98A10E67A334776AABDA733F971137A");
      st.setString(2, value);
      rs = st.executeQuery();
      if (rs.next()) {
        name = rs.getString("name");
        if (isvalue)
          name = value;
      } else {
        if (isvalue)
          name = "";
      }

    } catch (final SQLException e) {
      log4j.error("", e);
    }
    return name;

  }

  public String getTermiationReason(String clientId, String value) {
    String name = "";
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = conn.prepareStatement(
          " select name from ehcm_termination_reason  where ad_client_id  = ?   and  value= ?    order by created desc  ");
      st.setString(1, clientId);
      st.setString(2, value);
      rs = st.executeQuery();
      if (rs.next()) {
        name = rs.getString("name");
      }
    } catch (final SQLException e) {
      log4j.error("", e);
    }
    return name;

  }

  public String getSuspensionReason(String value) {
    String name = "";
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = conn.prepareStatement(" select name from ehcm_suspension_reason   where value  =?    ");
      st.setString(1, value);
      log4j.debug("st:" + st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        name = rs.getString("name");
      }
    } catch (final SQLException e) {
      log4j.error("", e);
    }
    return name;

  }

  public String getEmploymentInfo(String value, String changeinfo, String clientId, String lang) {
    String name = "";
    String sql = "";
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      if (lang.equals("ar_SA")) {
        sql = "select coalesce(tr.name,list.name) as name ";
      } else {
        sql = "select list.name as name ";
      }
      sql = sql
          + " from ad_ref_list list left join ad_ref_list_trl tr on list.ad_ref_list_id = tr.ad_ref_list_id  where ad_reference_id  = ?   and  value= ?    order by list.created desc ";

      st = conn.prepareStatement(sql);
      st.setString(1, EmpInfo_ChangeReason_List_ID);
      st.setString(2, value);
      /*
       * st = conn.prepareStatement(
       * " select name from ad_ref_list  where ad_reference_id  = ?   and  value= ?    order by created desc  "
       * ); st.setString(1, "57889F5818294AE6B371B3FD3369E8B3"); // st.setString(2, clientId);
       * st.setString(2, value);
       */
      log4j.debug("st:" + st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        name = rs.getString("name");
      }
      if (changeinfo != null) {
        name += "-" + getTermiationReason(clientId, changeinfo);
      }
      log4j.debug("name" + name);
    } catch (final SQLException e) {
      log4j.error("", e);
    }
    return name;

  }

  /**
   * get the supervisor list from the superviosr hierarchy
   * 
   * @param clientId
   * @param searchTerm
   * @param pagelimit
   * @param page
   * @param roleId
   * @param orgId
   * @return
   */
  @SuppressWarnings("resource")
  public synchronized JSONObject getSupervisorList(String clientId, String searchTerm,
      int pagelimit, int page, String roleId, String orgId, String employeeId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(distinct sup.ehcm_emp_supervisor_id) as count ");
      selectQuery.append(
          " select distinct sup.ehcm_emp_supervisor_id as supId, concat(emp.value,'-',emp.arabicname) as value ");
      fromQuery.append(
          " from ehcm_emp_supervisor sup   join ehcm_emp_perinfo emp on emp.ehcm_emp_perinfo_id = sup.ehcm_emp_perinfo_id join ehcm_emp_hierarchy emphir on emphir.ehcm_emp_hierarchy_id =sup.ehcm_emp_hierarchy_id "
              + "  where emp.ad_client_id= ?    and emphir.primaryflag='Y' and sup.ehcm_emp_perinfo_id <> ? and sup.ehcm_emp_perinfo_id not in ( select ehcm_emp_perinfo_id from ehcm_emp_supervisornode where ehcm_emp_supervisor_id "
              + "   = ( select ehcm_emp_supervisor_id from ehcm_emp_supervisor sup   join ehcm_emp_hierarchy emphir on emphir.ehcm_emp_hierarchy_id =sup.ehcm_emp_hierarchy_id  "
              + "  where ehcm_emp_perinfo_id= ?  and  emphir.primaryflag='Y'  ))");

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and  concat(emp.value,'-',emp.arabicname) ilike '%"
            + searchTerm.toLowerCase() + "%'");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());
      st.setString(1, clientId);
      st.setString(2, employeeId);
      st.setString(3, employeeId);
      // st.setString(4, employeeId);

      log4j.debug("supervisorList count qry>>" + st.toString());
      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);

      if (totalRecords > 0) {
        st = conn.prepareStatement(
            (selectQuery.append(fromQuery)).toString() + " order by value limit ? offset ? ");
        st.setString(1, clientId);
        st.setString(2, employeeId);
        st.setString(3, employeeId);
        // st.setString(4, employeeId);
        st.setInt(4, pagelimit);
        st.setInt(5, (page - 1) * pagelimit);

        log4j.debug("supervisorList:" + st.toString());
        rs = st.executeQuery();

        while (rs.next()) {
          JSONObject jsonData = new JSONObject();
          jsonData.put("id", rs.getString("supId"));
          jsonData.put("recordIdentifier", rs.getString("value"));
          jsonArray.put(jsonData);
        }
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      log4j.error("Exception in getSupervisorList :", e);
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

  /**
   * Insert the employee supervisor node for the particular employee
   * 
   * @param superVisorId
   * @param employeeId
   * @param vars
   */
  public static void insertEmpSuperVisorNode(String superVisorId, String employeeId,
      VariablesSecureApp vars) {
    try {
      OBContext.setAdminMode();
      EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);

      EHCMEmpSupervisorNode superVisorNode = OBProvider.getInstance()
          .get(EHCMEmpSupervisorNode.class);
      superVisorNode.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
      superVisorNode.setOrganization(employee.getOrganization());
      superVisorNode.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      superVisorNode.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      superVisorNode.setCreationDate(new java.util.Date());
      superVisorNode.setUpdated(new java.util.Date());
      superVisorNode
          .setEhcmEmpSupervisor(OBDal.getInstance().get(EHCMEmpSupervisor.class, superVisorId));
      superVisorNode.setEhcmEmpPerinfo(employee);
      superVisorNode.setNoofsubordinates(Long.valueOf(SupervisorCalloutDAO.getNoOfSubordinates(
          employee.getId(), superVisorNode.getEhcmEmpSupervisor().getEhcmEmpHierarchy().getId())));
      OBDal.getInstance().save(superVisorNode);
    } catch (Exception e) {
      log4j.error("Exception in insertEmpSuperVisorNode: ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
    }
  }

  /**
   * if supervisor change then remove the old superviser node from the supervisor hierarchy
   * 
   * @param superVisorId
   * @param employeeId
   * @param vars
   */
  public static void deleteEmpSupervisorNode(String superVisorId, String employeeId,
      VariablesSecureApp vars) {
    List<EHCMEmpSupervisorNode> supervisorNodeList = new ArrayList<EHCMEmpSupervisorNode>();

    try {
      OBContext.setAdminMode();
      OBQuery<EHCMEmpSupervisorNode> supervisorNodeQry = OBDal.getInstance().createQuery(
          EHCMEmpSupervisorNode.class,
          " as e where e.ehcmEmpPerinfo.id=:employeeId and e.ehcmEmpSupervisor.id=:superVisorId ");
      supervisorNodeQry.setNamedParameter("employeeId", employeeId);
      supervisorNodeQry.setNamedParameter("superVisorId", superVisorId);
      supervisorNodeList = supervisorNodeQry.list();
      log4j.debug("supervisorNodeList.size()" + supervisorNodeQry.getWhereAndOrderBy());
      if (supervisorNodeList.size() > 0) {
        EHCMEmpSupervisorNode removeoldSupNode = supervisorNodeList.get(0);
        OBDal.getInstance().remove(removeoldSupNode);
      }
    } catch (Exception e) {
      log4j.error("Exception in deleteEmpSupervisorNode: ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
    }
  }

  /**
   * get preference of EHCM_EmpInfo_Update
   * 
   * @param propoertyName
   * @return
   */
  public static Preference getPreference(String propoertyName) {
    List<Preference> preferenceList = new ArrayList<Preference>();
    Preference preference = null;
    try {
      // create preference
      OBQuery<Preference> removePrefQry = OBDal.getInstance().createQuery(Preference.class,
          " as e where e.attribute=:propertyName ");
      removePrefQry.setNamedParameter("propertyName", propoertyName);
      preferenceList = removePrefQry.list();
      if (preferenceList.size() > 0) {
        preference = preferenceList.get(0);
      }
      return preference;

    } catch (Exception e) {
      log4j.error("Exception in getPreference: ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
    }
    return preference;
  }

  // public static void getGradeName(String gradeId) {
  // OBQuery<ehcmgrade> gradeName = OBDal.getInstance().createQuery(ehcmgrade.class, gradeId);
  //
  // return;
  // }

  public Boolean terminateEmployeeByDecisionNumber(String employmentId, String decisionNo,
      Date decisionDate) {
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
    try {
      OBContext.setAdminMode();
      EmploymentInfo empInfoOB = OBDal.getInstance().get(EmploymentInfo.class, employmentId);
      Date Startdate = empInfoOB.getStartDate();
      empInfoOB.setEndDate(Startdate);
      empInfoOB.setEnabled(false);
      empInfoOB.setChangereason("CHD");
      empInfoOB.setAlertStatus("INACT");
      empInfoOB.setDecisionNo(decisionNo);
      empInfoOB.setDecisionDate(decisionDate);

      OBDal.getInstance().save(empInfoOB);
      EhcmEmpPerInfo employeeObj = OBDal.getInstance().get(EhcmEmpPerInfo.class,
          empInfoOB.getEhcmEmpPerinfo().getId());
      employeeObj.setStatus("TE");
      employeeObj.setEnabled(false);
      // remove assigned position from position
      assingedOrReleaseEmpInPositionDAO.updateEndDatePositionEmployeeHisotryForCancelledEmp(
          employeeObj, empInfoOB.getPosition());

      /*
       * Task No.6797 OBQuery<EhcmPosition> positionQry =
       * OBDal.getInstance().createQuery(EhcmPosition.class,
       * "as e where e.assignedEmployee.id=:employeeId");
       * positionQry.setNamedParameter("employeeId", employeeObj.getId()); if
       * (positionQry.list().size() > 0) { for (EhcmPosition positionOB : positionQry.list()) {
       * positionOB.setAssignedEmployee(null); OBDal.getInstance().save(positionOB); } }
       */
      OBDal.getInstance().save(employeeObj);
      OBDal.getInstance().flush();
      return true;
    } catch (Exception e) {
      log4j.debug("error while terminating employee", e);
      return false;
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  public Boolean employmentCancelHiring(String inpEmployeeId, String clientId) {
    // TODO Auto-generated method stub
    String sql = "";
    PreparedStatement st = null;
    ResultSet rs = null;
    boolean chkCondition = false;
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id = :employeeId and e.ehcmEmpPerinfo.id not in (select a.ehcmEmpPerinfo.id from ehcm_empstatus a where a.client.id = :clientId) "
              + " and client.id = :clientId and e.ehcmEmpPerinfo.status = 'I'  ");
      empInfo.setNamedParameter("employeeId", inpEmployeeId);
      empInfo.setNamedParameter("clientId", clientId);
      int count = empInfo.list().size();
      if (count == 1) {
        EmploymentInfo employment = empInfo.list().get(0);

        if (employment.getChangereason().equals("H") && employment.getEhcmJoinWorkrequest() == null
            && (employment.isJoinworkreq() == null || !employment.isJoinworkreq())) {
          chkCondition = true;

        } else {
          chkCondition = false;

        }

      } else {
        chkCondition = false;
      }

    } catch (Exception e) {
      log4j.debug("error while employmentCancelHiring", e);
      chkCondition = false;
    } finally {
      OBContext.restorePreviousMode();
    }
    return chkCondition;

  }

  public List<EmploymentVO> getEmploymentStatusList(String lang) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<EmploymentVO> ls = new ArrayList<EmploymentVO>();
    try {

      st = OBDal.getInstance().getConnection().prepareStatement(
          " select ad_ref_list.value as code,coalesce(ad_ref_list_trl.name,ad_ref_list.name) as name from ad_ref_list left join ad_ref_list_trl on ad_ref_list.ad_ref_list_id = ad_ref_list_trl.ad_ref_list_id and ad_ref_list_trl.ad_language=? where ad_ref_list.ad_reference_id = ? ");
      st.setString(1, lang);
      st.setString(2, EmpInfo_Status_RefId);
      rs = st.executeQuery();

      while (rs.next()) {
        EmploymentVO empVO = new EmploymentVO();
        empVO = new EmploymentVO();
        empVO.setStatus(rs.getString("code"));
        empVO.setArabicName(rs.getString("name"));

        ls.add(empVO);

      }

    } catch (final Exception e) {
      e.printStackTrace();
      log4j.error("Exception in getEmploymentStatusList : ", e);
    }
    return ls;

  }

  public List<EmploymentVO> getChangeReasonList(String lang) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<EmploymentVO> ls = new ArrayList<EmploymentVO>();
    try {

      st = OBDal.getInstance().getConnection().prepareStatement(
          " select ad_ref_list.value as code,coalesce(ad_ref_list_trl.name,ad_ref_list.name) as name from ad_ref_list left join ad_ref_list_trl on ad_ref_list.ad_ref_list_id = ad_ref_list_trl.ad_ref_list_id and ad_ref_list_trl.ad_language=? where ad_ref_list.ad_reference_id = ? ");
      st.setString(1, lang);
      st.setString(2, EmpInfo_ChangeReason_List_ID);
      rs = st.executeQuery();

      while (rs.next()) {
        EmploymentVO empVO = new EmploymentVO();
        empVO = new EmploymentVO();
        empVO.setStatus(rs.getString("code"));
        empVO.setArabicName(rs.getString("name"));

        ls.add(empVO);

      }

    } catch (final Exception e) {
      e.printStackTrace();
      log4j.error("Exception in getChangeReasonList : ", e);
    }
    return ls;

  }
}
