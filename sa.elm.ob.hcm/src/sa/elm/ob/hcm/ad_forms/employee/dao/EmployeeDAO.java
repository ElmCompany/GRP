package sa.elm.ob.hcm.ad_forms.employee.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.SQLQuery;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.domain.ListTrl;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.geography.City;
import org.openbravo.model.common.geography.Country;

import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;
import sa.elm.ob.hcm.EHCMDeflookupsTypeLn;
import sa.elm.ob.hcm.EHCMEmpLeave;
import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EHCMMisCatPeriod;
import sa.elm.ob.hcm.EHCMMisEmpCategory;
import sa.elm.ob.hcm.EHCMMiscatEmployee;
import sa.elm.ob.hcm.EHCMMissionCategory;
import sa.elm.ob.hcm.EhcmActiontype;
import sa.elm.ob.hcm.EhcmAddNationality;
import sa.elm.ob.hcm.EhcmAddressStyle;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EhcmReligion;
import sa.elm.ob.hcm.EhcmTitletype;
import sa.elm.ob.hcm.Ehcmdependentsv;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmempstatus;
import sa.elm.ob.hcm.ehcmempstatusv;
import sa.elm.ob.hcm.ehcmgradeclass;
import sa.elm.ob.hcm.ad_forms.employee.vo.EmployeeVO;
import sa.elm.ob.hcm.ad_forms.employeeaddress.vo.EmployeeAddressVO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAO;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAOImpl;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.utility.Utils;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class EmployeeDAO {
  private Connection conn = null;
  private static Logger log4j = Logger.getLogger(EmployeeDAO.class);
  DateFormat yearFormat = Utility.YearFormat;
  DateFormat dateFormat = Utility.dateFormat;

  public EmployeeDAO(Connection con) {
    this.conn = con;
  }

  DateFormat YearFormat = sa.elm.ob.utility.util.Utility.YearFormat;
  String EMPLOYEE_STATUS_REFERENCE_ID = "57889F5818294AE6B371B3FD3369E8B3";

  public List<EmployeeVO> getEmpCategory(String clientId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<EmployeeVO> ls = new ArrayList<EmployeeVO>();
    try {
      st = conn.prepareStatement(
          "select class.ehcm_gradeclass_id as gradeid ,(class.value ||'-'|| class.name) as cat  from  ehcm_gradeclass  class  left join ad_client cli on cli.ad_client_id= class.ad_client_id "
              + "   where class.ad_client_id = ? and class.isactive='Y' order by class.value");
      st.setString(1, clientId);
      log4j.debug("cat:" + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        EmployeeVO eVO = new EmployeeVO();
        eVO.setCategoryId(Utility.nullToEmpty(rs.getString("gradeid")));
        eVO.setCategorycode(Utility.nullToEmpty(rs.getString("cat")));
        ls.add(eVO);
      }
    } catch (final SQLException e) {
      log4j.error("Exception in getEmpCategory", e);
      return ls;
    } catch (final Exception e) {
      log4j.error("Exception in getEmpCategory", e);
      return ls;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getPayrollOfficer", e);
        return ls;
      }
    }
    return ls;
  }

  public JSONObject getCountry(String clientId, String searchTerm, int pagelimit, int page) {
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(coun.c_country_id) as count ");
      selectQuery
          .append(" select coun.c_country_id as countryid ,coun.name as name ,coun.isdefault ");
      fromQuery.append(
          " from  c_country  coun  left join ad_client cli on cli.ad_client_id= coun.ad_client_id  where coun.isactive='Y'     ");

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and coun.name ilike '%" + searchTerm.toLowerCase() + "%' ");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());

      rs = st.executeQuery();
      log4j.debug("st" + st.toString());
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);

      if (totalRecords > 0) {
        st = conn.prepareStatement(
            (selectQuery.append(fromQuery)).toString() + " order by coun.name limit ? offset ? ");
        st.setInt(1, pagelimit);
        st.setInt(2, (page - 1) * pagelimit);
      }

      rs = st.executeQuery();

      JSONObject jsonData = new JSONObject();
      if (totalRecords > 0) {

        while (rs.next()) {
          jsonData = new JSONObject();
          jsonData.put("id", Utility.nullToEmpty(rs.getString("countryid")));
          jsonData.put("recordIdentifier", Utility.nullToEmpty(rs.getString("name")));
          jsonData.put("isdefault", Utility.nullToEmpty(rs.getString("isdefault")));
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

  public List<EmployeeVO> getDefaultCountry(String clientId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<EmployeeVO> ls = new ArrayList<EmployeeVO>();
    try {
      st = conn.prepareStatement(
          "select coun.c_country_id as countryid ,coun.name as name ,coun.isdefault from  c_country  coun  left join ad_client cli on cli.ad_client_id= coun.ad_client_id  where  coun.isactive='Y' and coun.isdefault = 'Y'");
      log4j.debug("count:" + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        EmployeeVO eVO = new EmployeeVO();
        eVO.setCountryId(Utility.nullToEmpty(rs.getString("countryid")));
        eVO.setCountryName(Utility.nullToEmpty(rs.getString("name")));
        eVO.setIsdefault(Utility.nullToEmpty(rs.getString("isdefault")));
        ls.add(eVO);
      }
    } catch (final SQLException e) {
      log4j.error("Exception in getDefaultCountry", e);
      return ls;
    } catch (final Exception e) {
      log4j.error("Exception in getDefaultCountry", e);
      return ls;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getDefaultCountry", e);
        return ls;
      }
    }
    return ls;
  }

  public JSONObject getCity(String clientId, String CountryId, String searchTerm, int pagelimit,
      int page) {
    PreparedStatement st = null;
    PreparedStatement st1 = null;
    ResultSet rs = null;
    ResultSet rs1 = null;

    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(cty.c_city_id) as count ");
      selectQuery.append(
          " select cty.c_city_id as cityid ,cty.name as name ,cty.em_ehcm_isdefault as default ");
      fromQuery.append(
          "from  c_city  cty  left join ad_client cli on cli.ad_client_id= cty.ad_client_id where  cty.c_country_id = ?  and cty.isactive='Y'  and cty.ad_client_id in ('0', ?)    ");

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and cty.name ilike '%" + searchTerm.toLowerCase() + "%' ");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());
      st.setString(1, CountryId);
      st.setString(2, clientId);
      log4j.debug("city:" + st.toString());
      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);

      if (totalRecords > 0) {
        st1 = conn.prepareStatement(
            (selectQuery.append(fromQuery)).toString() + " order by cty.name   limit ? offset ? ");
        st1.setString(1, CountryId);
        st1.setString(2, clientId);
        st1.setInt(3, pagelimit);
        st1.setInt(4, (page - 1) * pagelimit);
        log4j.debug("city:" + st1.toString());
        rs1 = st1.executeQuery();

        JSONObject jsonData = new JSONObject();

        while (rs1.next()) {
          jsonData = new JSONObject();
          jsonData.put("id", Utility.nullToEmpty(rs1.getString("cityid")));
          jsonData.put("recordIdentifier", Utility.nullToEmpty(rs1.getString("name")));
          jsonData.put("isdefault", Utility.nullToEmpty(rs1.getString("default")));
          jsonArray.put(jsonData);
        }
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      log4j.error("Exception in getCity :", e);
      return jsob;
    } finally {
      try {
        if (rs != null)
          rs.close();
        if (st != null)
          st.close();
        if (rs1 != null)
          rs1.close();
        if (st1 != null)
          st1.close();

      } catch (Exception e) {

      }
    }
    return jsob;
  }

  public JSONObject getAdrsStyle(String clientId, String searchTerm, int pagelimit, int page) {
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(ehcm_address_style_id) as count ");
      selectQuery.append(" select ehcm_address_style_id,name ");
      fromQuery.append("from  ehcm_address_style  where ad_client_id =?   ");

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and name ilike '%" + searchTerm.toLowerCase() + "%' ");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());
      st.setString(1, clientId);
      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);

      if (totalRecords > 0) {
        st = conn
            .prepareStatement((selectQuery.append(fromQuery)).toString() + "    limit ? offset ? ");
        st.setString(1, clientId);
        st.setInt(2, pagelimit);
        st.setInt(3, (page - 1) * pagelimit);
      }

      rs = st.executeQuery();

      JSONObject jsonData = new JSONObject();
      if (totalRecords > 0) {

        while (rs.next()) {
          jsonData = new JSONObject();
          jsonData.put("id", Utility.nullToEmpty(rs.getString("ehcm_address_style_id")));
          jsonData.put("recordIdentifier", Utility.nullToEmpty(rs.getString("name")));
          jsonArray.put(jsonData);
        }
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      log4j.error("Exception in getAddressStyle :", e);
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

  public boolean checkEmpAlreadyExists(String clientId, String empno, String status, String empId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = conn.prepareStatement(
          " select count(*) as total from ehcm_emp_perinfo where ad_client_id=? and value=?  and ehcm_emp_perinfo_id<> ?");

      st.setString(1, clientId);
      st.setString(2, empno);
      st.setString(3, empId);
      log4j.debug("checkEmpAlreadyExists:" + st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        if (rs.getInt("total") > 0) {
          return true;
        } else
          return false;
      } else
        return false;
    } catch (final SQLException e) {
      log4j.error("Exception in checkEmpAlreadyExists", e);
      return false;
    } catch (final Exception e) {
      log4j.error("Exception in checkEmpAlreadyExists", e);
      return false;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in checkEmpAlreadyExists", e);
      }
    }
  }

  public boolean getEmployementcount(String clientId, String empId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = conn.prepareStatement(
          " select count(*) as total from ehcm_employment_info where ad_client_id=? and ehcm_emp_perinfo_id=?");
      st.setString(1, clientId);
      st.setString(2, empId);
      log4j.debug("emp:" + st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        if (rs.getInt("total") > 0) {
          return true;
        } else
          return false;
      } else
        return false;
    } catch (final SQLException e) {
      log4j.error("Exception in checkEmpAlreadyExists", e);
      return false;
    } catch (final Exception e) {
      log4j.error("Exception in checkEmpAlreadyExists", e);
      return false;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in checkEmpAlreadyExists", e);
      }
    }
  }

  public EmployeeVO checkBpartnerValidation(String clientId, String orgId, String employeeId,
      String salText) {
    EmployeeVO vo = new EmployeeVO();
    EhcmEmpPerInfo empObj = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
    List<Category> categoryList = null;
    Category categoryObj = null;
    try {
      if (salText.equals("HE") || salText.equals("HC") || salText.equals("HP")) {
        OBQuery<Category> cat = OBDal.getInstance().createQuery(Category.class,
            " as e where e.ehcmCategorytype='EMP' and e.client.id=:clientId order by e.creationDate desc");
        cat.setNamedParameter("clientId", clientId);
        cat.setMaxResult(1);
        categoryList = cat.list();
        if (categoryList.size() > 0) {
          categoryObj = categoryList.get(0);
        }
      }
      if (categoryObj == null) {
        vo.setCategorycode("false");
      } else {
        vo.setCategorycode("true");
      }
      OBQuery<BusinessPartner> bpartnerObj = OBDal.getInstance().createQuery(BusinessPartner.class,
          "client.id = :clientId and organization.id = :OrgId and searchKey = :value");
      bpartnerObj.setNamedParameter("clientId", clientId);
      bpartnerObj.setNamedParameter("OrgId", orgId);
      bpartnerObj.setNamedParameter("value", empObj.getSearchKey());

      if (bpartnerObj != null && bpartnerObj.list().size() > 0) {
        vo.setResult(false);
      } else {
        vo.setResult(true);
      }

    } catch (final Exception e) {
      log4j.error("Exception in checkBpartnerValidation", e);
    }
    return vo;

  }

  public int getDays(String clientId, String monthyear) {
    PreparedStatement st = null;
    ResultSet rs = null;
    int total = 0;
    try {

      st = conn.prepareStatement(
          "  select count(a.hijri_date)  as total from ( select  max(hijri_date) as hijri_date,gregorian_date from eut_hijri_dates  where "
              + " hijri_date ilike '%" + monthyear + "%'  group by  gregorian_date ) a");
      // st.setString(1, clientId);
      // st.setString(1, monthyear);
      log4j.debug("getDays:" + st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        if (rs.getInt("total") > 0) {
          total = rs.getInt("total");
        }
      }
    } catch (final SQLException e) {
      log4j.error("Exception in getDays", e);
      return 0;
    } catch (final Exception e) {
      log4j.error("Exception in getDays", e);
      return 0;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in checkEmpAlreadyExists", e);
      }
    }
    return total;
  }

  public List<EmployeeVO> getactionType(String clientId, String activeId, String value) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = "";
    List<EmployeeVO> ls = new ArrayList<EmployeeVO>();
    try {
      sql = "select actype.ehcm_actiontype_id as actid ,actype.value as value, actype.cancel_action as cancel_action ,actype.name as name, actype.persontype,actype.cancel_persontype from  ehcm_actiontype  actype "
          + " left join ad_client cli on cli.ad_client_id= actype.ad_client_id "
          + "   where  actype.ad_client_id = ? and actype.isactive='Y' ";
      if (activeId != null) {
        sql += " and actype.ehcm_actiontype_id = ?";
      }
      if (value != null) {
        sql += " and actype.value = ?";
      }
      sql += " order by actype.value ";
      st = conn.prepareStatement(sql);
      st.setString(1, clientId);
      if (activeId != null) {
        st.setString(2, activeId);
      }
      if (value != null) {
        st.setString(2, value);
      }
      log4j.debug("getactionType:" + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        EmployeeVO eVO = new EmployeeVO();
        eVO.setActTypeId(Utility.nullToEmpty(rs.getString("actid")));
        eVO.setActTypeName(Utility.nullToEmpty(rs.getString("name")));
        eVO.setActTypeValue(Utility.nullToEmpty(rs.getString("value")));
        eVO.setPersonType(Utility.nullToEmpty(rs.getString("persontype")));
        eVO.setCancelaction(Utility.nullToEmpty(rs.getString("cancel_action")));
        eVO.setCancelpersontype(Utility.nullToEmpty(rs.getString("cancel_persontype")));
        ls.add(eVO);
      }
    } catch (final SQLException e) {
      log4j.error("Exception in getactionType", e);
      return ls;
    } catch (final Exception e) {
      log4j.error("Exception in getactionType", e);
      return ls;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getactionType", e);
        return ls;
      }
    }
    return ls;
  }

  public boolean deleteEmployee(String employeeId, String inpEmpstatus) {
    PreparedStatement st = null;
    PreparedStatement st1 = null;
    try {

      if (inpEmpstatus != null && (inpEmpstatus.equals("C") || inpEmpstatus.equals("TE"))) {
        st = conn.prepareStatement("DELETE FROM ehcm_empstatus WHERE ehcm_emp_perinfo_id = ?");
      } else {

        st1 = conn
            .prepareStatement("DELETE FROM ehcm_miscat_employee WHERE ehcm_emp_perinfo_id = ?");

        st1.setString(1, employeeId);
        st1.executeUpdate();
        st = conn.prepareStatement("DELETE FROM ehcm_emp_perinfo WHERE ehcm_emp_perinfo_id = ?");

      }
      st.setString(1, employeeId);
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

  public boolean checkAlreadyCancel(String clientId, String empNo) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {

      st = conn.prepareStatement(
          "select count(*) as total from ehcm_emp_perinfo  where ad_client_id=? and value = ? and ehcm_emp_perinfo_id in (select ehcm_emp_perinfo_id from ehcm_empstatus)");
      st.setString(1, clientId);
      st.setString(2, empNo);
      log4j.debug("checkAlreadyCancel:" + st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        if (rs.getInt("total") > 0) {
          return true;
        } else
          return false;
      }
      return false;
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
  }

  public boolean checkEmploymentStatusCancel(String clientId, String inpEmployeeId) {
    boolean chkCondition = false;
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id = :employeeId  and e.ehcmEmpPerinfo.status = 'TE'");
      empInfo.setNamedParameter("employeeId", inpEmployeeId);
      int count = empInfo.list().size();
      if (count == 1) {
        EmploymentInfo employment = empInfo.list().get(0);
        if (employment.getChangereason().equals("CHD")) {
          chkCondition = true;
        } else {
          chkCondition = false;
        }
      } else {
        chkCondition = false;
      }

    } catch (Exception e) {
      log4j.debug("checkEmploymentStatusCancel", e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return chkCondition;

  }

  public boolean checkHiringDecisionStatus(String clientId, String inpEmployeeId) {
    boolean chkCondition = false;
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id = :employeeId and e.changereason = 'H' and e.ehcmEmpPerinfo.status = 'I' and e.ehcmEmpPerinfo.enabled = 'Y'");
      empInfo.setNamedParameter("employeeId", inpEmployeeId);

      if (empInfo.list().size() > 0) {
        chkCondition = true;
      }

    } catch (Exception e) {
      log4j.debug("checkHiringDecisionStatus", e);
    } finally {
      OBContext.restorePreviousMode();

    }
    return chkCondition;

  }

  public boolean checkCancelcondition(String clientId, String empid) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {

      st = conn.prepareStatement(
          "select coalesce(delegation.count,0) as count from ehcm_employment_info info "
              + "left join (select count(ehcm_emp_delegation_id) as count,ehcm_emp_perinfo_id from ehcm_emp_delegation where isactive='Y' group by ehcm_emp_perinfo_id) as delegation on delegation.ehcm_emp_perinfo_id = info.ehcm_emp_perinfo_id "
              + "where info.ehcm_emp_perinfo_id=? and info.changereason='H' and info.status='ACT' and info.isactive='Y'");
      st.setString(1, empid);
      log4j.debug("checkCancelcondition:" + st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        if (rs.getInt("count") > 0) {
          return false;
        } else
          return true;
      }
      return false;
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
  }

  public boolean checkAlreadyCancelIssue(String clientId, String empNo) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {

      st = conn.prepareStatement(
          "select count(*) as total from ehcm_emp_perinfo  where ad_client_id=? and value = ? and status='I' and isactive='N'");
      st.setString(1, clientId);
      st.setString(2, empNo);
      log4j.debug("checkAlreadyCancelIssue:" + st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        if (rs.getInt("total") > 0) {
          return true;
        } else
          return false;
      }
      return false;
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
  }

  public String getStarteDate(String clientId, String empNo, boolean date1) {
    PreparedStatement st = null;
    ResultSet rs = null;
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    String date = null;
    log4j.debug("getStarteDate");
    try {

      st = conn.prepareStatement(
          "select startdate , ehcm_emp_perinfo_id from ehcm_emp_perinfo  where ad_client_id=? and value = ? and status='I' ");
      st.setString(1, clientId);
      st.setString(2, empNo);
      log4j.debug("checkAlreadyCancel:" + st.toString());
      rs = st.executeQuery();

      if (rs.next()) {
        if (date1) {
          date = df.format(rs.getDate("startdate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          return date;
        } else {
          date = rs.getString("ehcm_emp_perinfo_id");
        }

      }

    } catch (final SQLException e) {
      log4j.error("", e);
      return date;
    } catch (final Exception e) {
      log4j.error("", e);
      return date;
    } finally {
      try {
        st.close();
      } catch (final SQLException e) {
        log4j.error("", e);
        return date;
      }
    }
    return date;
  }

  public EmployeeVO checkNationalID(String clientId, String nationalId, String employeeId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    EmployeeVO vo = null;
    try {
      vo = new EmployeeVO();
      boolean checkNID = Utils.isNINNumber(nationalId);
      st = conn.prepareStatement(
          "select count(*) as total from ehcm_emp_perinfo where ad_client_id=? and nationality_identifier= ?  and  ehcm_emp_perinfo_id <> ?");
      st.setString(1, clientId);
      st.setString(2, nationalId);
      st.setString(3, employeeId);
      log4j.debug("checkAlreadyCancel:" + st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        if (rs.getInt("total") > 0) {
          vo.setResult(true);
        }
      } else
        vo.setResult(false);
      vo.setValue(checkNID);

    } catch (final SQLException e) {
      log4j.error("", e);
      return vo;
    } catch (final Exception e) {
      log4j.error("", e);
      return vo;
    } finally {
      try {
        st.close();
      } catch (final SQLException e) {
        log4j.error("", e);
        return vo;
      }
    }
    return vo;
  }

  public boolean checkDateval(String clientId, String hiredate, String dobdate) {

    PreparedStatement st = null;
    ResultSet rs = null;
    boolean value = true;
    try {
      st = conn.prepareStatement(
          "select em_ehcm_minempage,em_ehcm_maxempage from ad_client  where ad_client_id=? ");
      st.setString(1, clientId);

      rs = st.executeQuery();
      if (rs.next()) {

        Integer minage = rs.getInt("em_ehcm_minempage");
        Integer maxage = rs.getInt("em_ehcm_maxempage");
        if (minage != 0 && maxage != 0) {
          boolean chkminage = UtilityDAO.periodyearValidation(dobdate, hiredate, minage);
          boolean chkmaxage = UtilityDAO.periodyearValidation(dobdate, hiredate, maxage);
          if (!chkminage || chkmaxage) {
            return false;
          } else {
            return true;
          }
        } else {
          return true;
        }
      }

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
    return value;
  }

  public List<EmployeeVO> getNationality(String clientId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = "";
    List<EmployeeVO> ls = new ArrayList<EmployeeVO>();
    try {
      sql = "select nat.ehcm_addnationality_id as natid ,(nat.value ||'-'|| nat.name) as  name ,nat.isdefault as default from  ehcm_addnationality  nat  "
          + " left join ad_client cli on cli.ad_client_id= nat.ad_client_id "
          + "   where  nat.ad_client_id = ? and nat.isactive='Y' ";
      sql += " order by nat.value ";
      st = conn.prepareStatement(sql);
      st.setString(1, clientId);
      log4j.debug("getNationality:" + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        EmployeeVO eVO = new EmployeeVO();
        eVO.setNationalId(Utility.nullToEmpty(rs.getString("natid")));
        eVO.setNationalCode(Utility.nullToEmpty(rs.getString("name")));
        eVO.setIsdefault(Utility.nullToEmpty(rs.getString("default")));
        ls.add(eVO);
      }
    } catch (final SQLException e) {
      log4j.error("Exception in getNationality", e);
      return ls;
    } catch (final Exception e) {
      log4j.error("Exception in getNationality", e);
      return ls;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getPayrollOfficer", e);
        return ls;
      }
    }
    return ls;
  }

  public List<EmployeeVO> getReligion(String clientId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = "";
    List<EmployeeVO> ls = new ArrayList<EmployeeVO>();
    try {
      sql = "select reli.ehcm_religion_id as relid ,(reli.value ||'-'|| reli.name) as  name,reli.isdefault as default from  ehcm_religion  reli  "
          + " left join ad_client cli on cli.ad_client_id= reli.ad_client_id "
          + "   where  reli.ad_client_id = ? and reli.isactive='Y'  ";
      sql += " order by reli.value ";
      st = conn.prepareStatement(sql);
      st.setString(1, clientId);
      log4j.debug("getReligion:" + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        EmployeeVO eVO = new EmployeeVO();
        eVO.setReligionId(Utility.nullToEmpty(rs.getString("relid")));
        eVO.setReligionCode(Utility.nullToEmpty(rs.getString("name")));
        eVO.setIsdefault(Utility.nullToEmpty(rs.getString("default")));
        ls.add(eVO);
      }
    } catch (final SQLException e) {
      log4j.error("Exception in getReligion", e);
      return ls;
    } catch (final Exception e) {
      log4j.error("Exception in getReligion", e);
      return ls;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getPayrollOfficer", e);
        return ls;
      }
    }
    return ls;
  }

  public List<EmployeeVO> getTitleType(String clientId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = "";
    List<EmployeeVO> ls = new ArrayList<EmployeeVO>();
    try {
      sql = "select type.ehcm_titletype_id as typeid ,(type.name) as  name from  ehcm_titletype  type  "
          + " left join ad_client cli on cli.ad_client_id= type.ad_client_id "
          + "   where  type.ad_client_id = ?  and type.isactive='Y'  ";
      sql += " order by type.value ";
      st = conn.prepareStatement(sql);
      st.setString(1, clientId);
      log4j.debug("getTitleType:" + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        EmployeeVO eVO = new EmployeeVO();
        eVO.setTitleId(Utility.nullToEmpty(rs.getString("typeid")));
        eVO.setTitleName(Utility.nullToEmpty(rs.getString("name")));
        ls.add(eVO);
      }
    } catch (final SQLException e) {
      log4j.error("Exception in getTitleType", e);
      return ls;
    } catch (final Exception e) {
      log4j.error("Exception in getTitleType", e);
      return ls;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getTitleType", e);
        return ls;
      }
    }
    return ls;
  }

  public List<EmployeeVO> getEmployeeStatus() {
    List<EmployeeVO> ls = new ArrayList<EmployeeVO>();
    try {

      SQLQuery Query = OBDal.getInstance().getSession().createSQLQuery(
          " select list.ad_ref_list_id as listId ,list.name as  name,list.value from  ad_ref_list  list"
              + " where  list.ad_reference_id = :ad_reference_id  and list.isactive='Y'"
              + " and list.value not in('COSCTR','EXSCTR') " + "   order by list.name ");
      Query.setParameter("ad_reference_id", EMPLOYEE_STATUS_REFERENCE_ID);
      if (Query.list().size() > 0) {
        for (Object o : Query.list()) {
          Object[] row = (Object[]) o;
          EmployeeVO eVO = new EmployeeVO();
          eVO.setAdRefListId(Utility.nullToEmpty(row[0].toString()));
          eVO.setEmployeeStatus(Utility.nullToEmpty(row[1].toString()));
          eVO.setStatus(Utility.nullToEmpty(row[2].toString()));
          ls.add(eVO);
        }
      }
    } catch (final Exception e) {
      log4j.error("Exception in getEmployeeStatus", e);
      return ls;
    } finally {

    }
    return ls;
  }

  public List<EmployeeVO> getEmployeeList(String clientId, String childOrgId, EmployeeVO employeeVO,
      JSONObject searchAttr, String selEmployeeId, String lang) {
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
      int rows = Integer.parseInt(searchAttr.getString("rows")),
          page = Integer.parseInt(searchAttr.getString("page"));

      whereClause = " AND info.ad_client_id = '" + clientId + "' AND info.ad_org_id in ("
          + childOrgId + ") ";

      if (searchAttr.has("search") && searchAttr.getString("search").equals("true")) {
        /*
         * if(!StringUtils.isEmpty(employeeVO.getOrgId())) whereClause += " and info.ad_org_id = '"
         * + employeeVO.getOrgId() + "'";
         */
        if (!StringUtils.isEmpty(employeeVO.getEmpName()))
          whereClause += " and (info.name||' '||info.fathername||' '||info.grandfathername) ilike '%"
              + employeeVO.getEmpName() + "%'";
        if (!StringUtils.isEmpty(employeeVO.getArbfourthName()))
          whereClause += " and (info.arabicname||' '||info.arabicfatname||' '||info.arbgrafaname||' '||info.arabicfamilyname) ilike '%"
              + employeeVO.getArbfourthName() + "%'";
        if (!StringUtils.isEmpty(employeeVO.getEmpNo()))
          whereClause += " and info.value ilike '%" + employeeVO.getEmpNo() + "%'";
        if (!StringUtils.isEmpty(employeeVO.getStatus()))
          whereClause += " and st.status ='" + employeeVO.getStatus() + "'";
        if (!StringUtils.isEmpty(employeeVO.getSaluatation()))
          whereClause += " and info.ehcm_titletype_id ='" + employeeVO.getSaluatation() + "'";
        if (!StringUtils.isEmpty(employeeVO.getCategoryId()))
          whereClause += " and info.ehcm_gradeclass_id ='" + employeeVO.getCategoryId() + "'";
        if (!StringUtils.isEmpty(employeeVO.getGender()))
          whereClause += " and info.gender ='" + employeeVO.getGender() + "'";
        if (!StringUtils.isEmpty(employeeVO.getNationalCode()))
          whereClause += " and info.nationality_identifier ilike '%" + employeeVO.getNationalCode()
              + "%'";
        if (!StringUtils.isEmpty(employeeVO.getPersonType()))
          whereClause += " and act.persontype ilike '%" + employeeVO.getPersonType() + "%'";
        if (!StringUtils.isEmpty(employeeVO.getHiredate()))
          whereClause += " and info.hiredate " + employeeVO.getHiredate().split("##")[0]
              + " to_timestamp('" + employeeVO.getHiredate().split("##")[1]
              + "', 'yyyy-MM-dd HH24:MI:SS') ";
        if (!StringUtils.isEmpty(employeeVO.getActive()))
          whereClause += " and info.isactive ='" + employeeVO.getActive() + "'";
        if (!StringUtils.isEmpty(employeeVO.getEmployeeStatus()))
          whereClause += " and status.statusvalue ='" + employeeVO.getEmployeeStatus() + "'";
      }

      if (StringUtils.equals(searchAttr.getString("sortName"), "value"))
        searchAttr.put("sortName", "info.value");

      orderClause = " order by " + searchAttr.getString("sortName") + " "
          + searchAttr.getString("sortType");

      // Get Row Count
      sqlQuery = " SELECT count(info.ehcm_emp_perinfo_id) as count FROM ehcm_empstatus_v st        left join ehcm_emp_perinfo info on st.ehcm_emp_perinfo_id = info.ehcm_emp_perinfo_id   LEFT JOIN ehcm_titletype tit on tit.ehcm_titletype_id  = info.ehcm_titletype_id  LEFT JOIN ehcm_actiontype act on info.ehcm_actiontype_id  = act.ehcm_actiontype_id   left join ehcm_emp_address add on add.ehcm_emp_perinfo_id=info.ehcm_emp_perinfo_id  LEFT JOIN ehcm_gradeclass class on class.ehcm_gradeclass_id  = info.ehcm_gradeclass_id left join ad_org ao on ao.ad_org_id = info.ad_org_id  left join (select count(ehcm_emp_delegation_id) as count,ehcm_emp_perinfo_id from ehcm_emp_delegation where isactive='Y' group by ehcm_emp_perinfo_id) as delegation on delegation.ehcm_emp_perinfo_id = info.ehcm_emp_perinfo_id "
          + " left join ehcm_employeestatus_v status on status.ehcm_employeestatus_v_id= info.ehcm_emp_perinfo_id"
          + " left join AD_Ref_List_Trl statuslist on statuslist.ad_ref_list_id= status.ad_ref_list_id and AD_Language= ? "
          + "    WHERE 1 = 1  ";
      sqlQuery += whereClause;
      st = conn.prepareStatement(sqlQuery);
      log4j.debug("Employee count : " + st.toString());
      st.setString(1, lang);
      rs = st.executeQuery();
      if (rs.next())
        totalRecord = rs.getInt("count");

      // Selected Employee Row
      if (selEmployeeId != null && selEmployeeId.length() == 32) {
        sqlQuery = "  select tb.rowno from (SELECT row_number() OVER (" + orderClause
            + ") as rowno, st.ehcm_empstatus_v_id  from  ehcm_empstatus_v st        left join ehcm_emp_perinfo info on st.ehcm_emp_perinfo_id = info.ehcm_emp_perinfo_id     LEFT JOIN ehcm_titletype tit on tit.ehcm_titletype_id  = info.ehcm_titletype_id  LEFT JOIN ehcm_actiontype act on info.ehcm_actiontype_id  = act.ehcm_actiontype_id   LEFT JOIN ehcm_gradeclass class on class.ehcm_gradeclass_id  = info.ehcm_gradeclass_id left join ad_org ao on ao.ad_org_id = info.ad_org_id  left join (select count(ehcm_emp_delegation_id) as count,ehcm_emp_perinfo_id from ehcm_emp_delegation where isactive='Y' group by ehcm_emp_perinfo_id) as delegation on delegation.ehcm_emp_perinfo_id = info.ehcm_emp_perinfo_id "
            + "  left join ehcm_employeestatus_v status on status.ehcm_employeestatus_v_id= info.ehcm_emp_perinfo_id  WHERE 1 = 1  ";
        sqlQuery += whereClause;
        sqlQuery += orderClause;
        sqlQuery += ")tb where tb.ehcm_empstatus_v_id = '" + selEmployeeId + "';";
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
      // Employee Details
      /*
       * sqlQuery =
       * "   select info.ad_client_id,info.ehcm_emp_perinfo_id ,act.persontype,info.nationality_identifier,info.gender, info.ehcm_titletype_id, tit.value as title,info.value ,ao.name as org , ( info.name||' '||info.fathername||' '||info.grandfathername)as name,(info.arabicname||' '||info.arabicfatname||' '||info.arbgrafaname) as arabicname , info.hiredate as hiredate,(class.value||'-'||class.name) as cat,"
       * +
       * "  info.status as status, emt.changereason as reason, emt.status as emtstatus,coalesce(delegation.count,0) as count, info.isactive,add.ehcm_emp_address_id as addressid,class.iscontract as contract,info.person_type FROM ehcm_emp_perinfo info    left join ad_org ao on ao.ad_org_id = info.ad_org_id  LEFT JOIN ehcm_actiontype act on info.ehcm_actiontype_id  = act.ehcm_actiontype_id "
       * +
       * "  LEFT JOIN ehcm_titletype tit on tit.ehcm_titletype_id  = info.ehcm_titletype_id   LEFT JOIN ehcm_gradeclass class on class.ehcm_gradeclass_id  = info.ehcm_gradeclass_id  left join ehcm_emp_address add on add.ehcm_emp_perinfo_id=info.ehcm_emp_perinfo_id left join ehcm_employment_info emt on emt.ehcm_emp_perinfo_id = info.ehcm_emp_perinfo_id and emt.isactive='Y' left join (select count(ehcm_emp_delegation_id) as count,ehcm_emp_perinfo_id from ehcm_emp_delegation where isactive='Y' group by ehcm_emp_perinfo_id) as delegation on delegation.ehcm_emp_perinfo_id = info.ehcm_emp_perinfo_id WHERE 1 = 1  "
       * ;
       */
      sqlQuery = "   select info.ad_client_id,info.ehcm_emp_perinfo_id ,st.ehcm_empstatus_v_id,act.persontype,info.nationality_identifier,info.gender, info.ehcm_titletype_id, tit.value as title,info.value ,ao.name as org , ( coalesce( info.name,'')||' '||coalesce(info.fathername,'')||' '|| coalesce(info.grandfathername,''))as name,(info.arabicname||' '||info.arabicfatname||' '||info.arbgrafaname || ' ' || info.arabicfamilyname) as arabicname , info.hiredate as hiredate,(class.value||'-'||class.name) as cat,"
          + "  st.status as status,"
          + "(select changereason from ehcm_employment_info where ehcm_emp_perinfo_id=info.ehcm_emp_perinfo_id and isactive='Y' order by created desc limit 1) as reason,"
          + "(select status from ehcm_employment_info where ehcm_emp_perinfo_id=info.ehcm_emp_perinfo_id and isactive='Y' order by created desc limit 1) as emtstatus,"
          + "coalesce(delegation.count,0) as count, st.isactive,add.ehcm_emp_address_id as addressid,class.iscontract as contract,info.person_type, coalesce(statuslist.name,status.name) as employeestatus FROM  ehcm_empstatus_v st left join ehcm_emp_perinfo info on st.ehcm_emp_perinfo_id = info.ehcm_emp_perinfo_id    left join ad_org ao on ao.ad_org_id = info.ad_org_id  LEFT JOIN ehcm_actiontype act on info.ehcm_actiontype_id  = act.ehcm_actiontype_id "
          + "  LEFT JOIN ehcm_titletype tit on tit.ehcm_titletype_id  = info.ehcm_titletype_id   LEFT JOIN ehcm_gradeclass class on class.ehcm_gradeclass_id  = info.ehcm_gradeclass_id  left join ehcm_emp_address add on add.ehcm_emp_perinfo_id=info.ehcm_emp_perinfo_id left join (select count(ehcm_emp_delegation_id) as count,ehcm_emp_perinfo_id from ehcm_emp_delegation where isactive='Y' group by ehcm_emp_perinfo_id) as delegation on delegation.ehcm_emp_perinfo_id = info.ehcm_emp_perinfo_id";

      sqlQuery += " left join ehcm_employeestatus_v status on status.ehcm_employeestatus_v_id= info.ehcm_emp_perinfo_id "
          + " left join AD_Ref_List_Trl statuslist on statuslist.ad_ref_list_id= status.ad_ref_list_id and AD_Language= ?    ";

      sqlQuery += " WHERE 1 = 1  ";

      sqlQuery += whereClause;
      sqlQuery += orderClause;
      sqlQuery += " limit " + rows + " offset " + offset;
      st = conn.prepareStatement(sqlQuery);
      log4j.debug("Employee Info : " + st.toString());
      st.setString(1, lang);
      rs = st.executeQuery();
      while (rs.next()) {
        eVO = new EmployeeVO();
        eVO.setId(Utility.nullToEmpty(rs.getString(("ehcm_empstatus_v_id"))));
        eVO.setEmployeeId(Utility.nullToEmpty(rs.getString(("ehcm_emp_perinfo_id"))));
        eVO.setOrgName(Utility.nullToEmpty(rs.getString("org")));
        eVO.setEmpNo(Utility.nullToEmpty(rs.getString("value")));
        eVO.setEmpName(Utility.nullToEmpty(rs.getString(("name"))));
        eVO.setEmpArabicName(Utility.nullToEmpty(rs.getString(("arabicname"))));
        eVO.setShortstatus(Utility.nullToEmpty(rs.getString("status")));
        if (Utility.nullToEmpty(rs.getString("status")).equals("UP"))
          eVO.setPerStatus(Resource.getProperty("hcm.underproc", lang));
        else if (Utility.nullToEmpty(rs.getString("status")).equals("I"))
          eVO.setPerStatus(Resource.getProperty("hcm.issued", lang));
        else if (Utility.nullToEmpty(rs.getString("status")).equals("TE"))
          eVO.setPerStatus(Resource.getProperty("hcm.terminate.employment", lang));
        else
          eVO.setPerStatus(Resource.getProperty("hcm.cancelled", lang));
        eVO.setCategoryId(Utility.nullToEmpty(rs.getString(("cat"))));
        if (rs.getDate("hiredate") != null) {
          date = df.format(rs.getDate("hiredate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          eVO.setHireDate(date);
        } else
          eVO.setHireDate(null);

        eVO.setSaluatation(Utility.nullToEmpty(rs.getString("title")));
        eVO.setActive(Utility.nullToEmpty(rs.getString("isactive")));
        eVO.setGender(Utility.nullToEmpty(rs.getString("gender")));
        if (StringUtils.isNotEmpty(rs.getString("status"))
            && (rs.getString("status").equals("TE") || rs.getString("status").equals("C"))) {
          if (rs.getString("person_type") != null && !rs.getString("person_type").equals("")) {

            EhcmActiontype actype = OBDal.getInstance().get(EhcmActiontype.class,
                rs.getString("person_type"));
            eVO.setPersonType(Utility.nullToEmpty(actype.getCancelPersontype()));
            log4j.debug("getPersonType" + eVO.getPersonType());
          }
        } else {
          eVO.setPersonType(Utility.nullToEmpty(rs.getString("persontype")));
        }
        eVO.setNationalCode(Utility.nullToEmpty(rs.getString("nationality_identifier")));
        eVO.setAddressId(Utility.nullToEmpty(rs.getString("addressid")));
        eVO.setEmployeeCategory(Utility.nullToEmpty(rs.getString("contract")));
        eVO.setChangereason(Utility.nullToEmpty(rs.getString("reason")));
        eVO.setEmploymentstatus(Utility.nullToEmpty(rs.getString("emtstatus")));
        eVO.setDelegationcount(rs.getInt("count"));
        eVO.setDelegationcount(rs.getInt("count"));
        eVO.setEmployeeStatus(Utility.nullToEmpty(rs.getString("employeestatus")));
        ls.add(eVO);
      }
    } catch (final Exception e) {
      log4j.error("Exception in getEmployeeList", e);
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getEmployeeList", e);
      }
    }
    return ls;
  }

  public EmployeeVO getEmpEditList(String employeeId, String Status) {
    EmployeeVO employeeVO = null;
    OBQuery<EhcmEmpPerInfo> empQry = null;
    List<EhcmEmpPerInfo> empList = new ArrayList<EhcmEmpPerInfo>();
    try {

      if (Status.equals("C") || Status.equals("TE")) {
        empQry = OBDal.getInstance().createQuery(EhcmEmpPerInfo.class,
            " as e  left join e.ehcmEmpstatusVList as st where (st.alertStatus='C' or st.alertStatus='TE' ) "
                + " and (e.id=:employeeId  or st.id=:employeeId) and e.id= st.ehcmEmpPerinfo.id ");
        empQry.setNamedParameter("employeeId", employeeId);
        log4j.debug("empQry:" + empQry.getWhereAndOrderBy());
        empQry.setMaxResult(1);
        empList = empQry.list();
      } else {
        empQry = OBDal.getInstance().createQuery(EhcmEmpPerInfo.class,
            " as e where e.id=:employeeId  and (e.status='I' or e.status = 'UP' or e.status = 'TE') ");
        empQry.setNamedParameter("employeeId", employeeId);
        empQry.setMaxResult(1);
        empList = empQry.list();
      }

      if (empList.size() > 0) {
        EhcmEmpPerInfo employee = empList.get(0);
        employeeVO = new EmployeeVO();
        employeeVO.setEmployeeId(employee.getId());
        employeeVO.setEmpNo(Utility.nullToEmpty(employee.getSearchKey()));
        employeeVO.setEmpName(Utility.nullToEmpty(employee.getName()));
        employeeVO.setFourthName(Utility.nullToEmpty(employee.getFourthname()));
        employeeVO.setGradfatName(Utility.nullToEmpty(employee.getGrandfathername()));
        employeeVO.setFamilyName(Utility.nullToEmpty(employee.getFamilyname()));
        employeeVO.setFatName(Utility.nullToEmpty(employee.getFathername()));
        employeeVO.setEmpArabicName(Utility.nullToEmpty(employee.getArabicname()));
        employeeVO.setArbfatName(Utility.nullToEmpty(employee.getArabicfatname()));
        employeeVO.setArbgradfatName(Utility.nullToEmpty(employee.getArbgrafaname()));
        employeeVO.setArbfamilyName(Utility.nullToEmpty(employee.getArabicfamilyname()));
        employeeVO.setArbfourthName(Utility.nullToEmpty(employee.getArbfouname()));
        employeeVO.setNationalCode(Utility.nullToEmpty(employee.getNationalityIdentifier()));
        if (Status.equals("C")) {
          String actTypeId = "";
          List<EmployeeVO> actls = getactionType(employee.getClient().getId(), null, "CAH");
          for (EmployeeVO vo1 : actls) {
            actTypeId = vo1.getActTypeId();
            break;
          }
          employeeVO.setActTypeId(Utility.nullToEmpty(actTypeId));
        } else {
          employeeVO.setActTypeId(Utility.nullToEmpty(employee.getEhcmActiontype().getId()));
        }
        employeeVO.setGradeclassId(Utility.nullToEmpty(employee.getGradeClass().getId()));
        employeeVO.setHiredate(Utility
            .nullToEmpty(UtilityDAO.convertTohijriDate(YearFormat.format(employee.getHiredate()))));

        log4j.debug("gove:" + employee.getGovhiredate());

        if (employee.getGovhiredate() != null && !employee.getGovhiredate().equals("")) {
          employeeVO.setGovhiredate(
              UtilityDAO.convertTohijriDate(YearFormat.format(employee.getGovhiredate())));
        } else {
          employeeVO.setGovhiredate("");
        }
        if (employee.getMarialstatus().equals("M")) {
          if (employee.getMarrieddate() != null) {
            employeeVO.setMarrieDate(Utility.nullToEmpty(
                UtilityDAO.convertTohijriDate(YearFormat.format(employee.getMarrieddate()))));
          }
        } else {
          employeeVO.setMarrieDate(null);
        }
        employeeVO.setLetterno(Utility.nullToEmpty(employee.getMcsletterno()));
        employeeVO.setDecisionno(Utility.nullToEmpty(employee.getDecisionno()));
        if (employee.getMcsletterdate() != null) {
          employeeVO.setLetterdate(
              UtilityDAO.convertTohijriDate(YearFormat.format(employee.getMcsletterdate())));
        } else {
          employeeVO.setLetterdate("");
        }
        if (employee.getDecisiondate() != null) {
          employeeVO.setDecisiondate(
              UtilityDAO.convertTohijriDate(YearFormat.format(employee.getDecisiondate())));
        } else {
          employeeVO.setDecisiondate("");
        }
        employeeVO.setTitleId(Utility.nullToEmpty(employee.getEhcmTitletype().getId()));
        employeeVO.setGender(Utility.nullToEmpty(employee.getGender()));
        employeeVO.setMobno(Utility.nullToEmpty(employee.getMobno()));
        employeeVO.setHomeno(Utility.nullToEmpty(employee.getHomeno()));
        employeeVO.setWorkno(Utility.nullToEmpty(employee.getWorkno()));
        employeeVO.setOffice(Utility.nullToEmpty(employee.getOfficename()));
        employeeVO.setLocation(Utility.nullToEmpty(employee.getLocation()));
        employeeVO.setBloodtype(Utility.nullToEmpty(employee.getBloodtype()));
        employeeVO.setTownofbirth(Utility.nullToEmpty(employee.getTownbirth()));
        employeeVO.setCountryId(Utility.nullToEmpty(employee.getCountry().getId()));
        employeeVO.setCityId(Utility.nullToEmpty(employee.getCity().getId()));
        employeeVO.setMaritalstauts(Utility.nullToEmpty(employee.getMarialstatus()));
        employeeVO.setReligionId(Utility.nullToEmpty(employee.getEhcmReligion().getId()));
        employeeVO.setNationalId(Utility.nullToEmpty(employee.getEhcmAddnationality().getId()));
        employeeVO.setHeight(Utility.nullToEmpty(employee.getHeight()));
        employeeVO.setWeight(Utility.nullToEmpty(employee.getWeight()));
        employeeVO.setEmail(Utility.nullToEmpty(employee.getEmail()));

        employeeVO.setDob(Utility
            .nullToEmpty(UtilityDAO.convertTohijriDate(YearFormat.format(employee.getDob()))));
        employeeVO.setCivimg(employee.getCIVAdImage());
        employeeVO.setWrkimg(employee.getWorkAdImage());

        if (Status.equals("C") || Status.equals("TE")) {
          ehcmempstatusv statusv = OBDal.getInstance().get(ehcmempstatusv.class, employeeId);
          employeeVO.setDecisionno(Utility.nullToEmpty(statusv.getDecisionno()));
          employeeVO.setStatus(Utility.nullToEmpty(statusv.getAlertStatus()));
          if (statusv.getStartDate() != null) {
            employeeVO.setStartdate(
                UtilityDAO.convertTohijriDate(YearFormat.format(statusv.getStartDate())));
          } else {
            employeeVO.setStartdate("");
          }
          log4j.debug("getStatus:" + employeeVO.getStatus());
          if (statusv.getTodate() != null) {
            employeeVO
                .setEnddate(UtilityDAO.convertTohijriDate(YearFormat.format(statusv.getTodate())));
          } else {
            employeeVO.setEnddate("");
          }
          if (statusv.getMcsletterno() != null) {
            employeeVO.setLetterno(statusv.getMcsletterno());
          }
          if (statusv.getMcsletterdate() != null) {
            employeeVO.setLetterdate(
                UtilityDAO.convertTohijriDate(YearFormat.format(statusv.getMcsletterdate())));
          }

        } else {
          employeeVO.setDecisionno(Utility.nullToEmpty(employee.getDecisionno()));
          employeeVO.setStatus(Utility.nullToEmpty(employee.getStatus()));
          employeeVO.setStartdate(Utility.nullToEmpty(
              UtilityDAO.convertTohijriDate(YearFormat.format(employee.getStartDate()))));
          if (employee.getEndDate() != null) {
            employeeVO.setEnddate(
                UtilityDAO.convertTohijriDate(YearFormat.format(employee.getEndDate())));
          } else {
            employeeVO.setEnddate("");
          }

        }
        if (employeeVO.getStatus().equals("C")) {
          employeeVO.setPersonType(
              "EX-" + getPersonType(employee.getClient().getId(), employee.getSearchKey()));
        }
      }

    } catch (final Exception e) {
      log4j.error("", e);
    } finally {
    }
    return employeeVO;
  }

  public EmployeeVO getEmployeeEditListS(String employeeId, String Status) {

    log4j.debug("emp" + employeeId);
    log4j.debug("Status" + Status);

    PreparedStatement st = null;
    ResultSet rs = null;
    EmployeeVO employeeVO = null;
    String sql = "";
    try {

      sql = "SELECT  info.ad_client_id,info.ehcm_emp_perinfo_id, info.value, info.name, info.fourthname, info.fathername, info.grandfathername,info.familyname, info.arabicname,info.arabicfatname,info.arabicfamilyname,info.arbgrafaname, info.arbfouname,"
          + " info.ehcm_addnationality_id, info.nationality_identifier, info.ehcm_actiontype_id, info.ehcm_gradeclass_id, "
          + "  (select eut_convert_to_hijri (to_char(info.hiredate,'YYYY-MM-DD HH24:MI:SS' )))	as hiredate,(select eut_convert_to_hijri (to_char(info.govhiredate,'YYYY-MM-DD HH24:MI:SS' )))	 as govthirdate, 	info.mcsletterno as letterno ,(select eut_convert_to_hijri (to_char(info.mcsletterdate,'YYYY-MM-DD HH24:MI:SS' ))) as letterdate, "
          + "  (select eut_convert_to_hijri (to_char(info.decisiondate,'YYYY-MM-DD HH24:MI:SS' )))	as deciondate,info.ehcm_titletype_id as title , info.gender,info.mobno , info.homeno,info.workno,info.email,info.officename,info.location ,info.c_country_id ,  "
          + " info.c_city_id , info.townbirth,info.marialstatus, info.bloodtype,info.ehcm_addnationality_id,info.height,info.weight,info.ehcm_religion_id, (select eut_convert_to_hijri (to_char(info.dob,'YYYY-MM-DD HH24:MI:SS' ))) as dob,info.civ_ad_image_id as civimg,info.work_ad_image_id as wrkimg,  ";
      if (Status.equals("C") || Status.equals("TE"))
        sql += " st.decisionno as decisiono,st.status as status,(select eut_convert_to_hijri (to_char(st.startdate,'YYYY-MM-DD HH24:MI:SS' )))  as startdate,(select eut_convert_to_hijri (to_char(st.todate,'YYYY-MM-DD HH24:MI:SS' )))      as enddate ";
      else
        sql += " info.decisionno as decisiono,info.status as status,(select eut_convert_to_hijri (to_char(info.startdate,'YYYY-MM-DD HH24:MI:SS' )))  as startdate,(select eut_convert_to_hijri (to_char(info.enddate,'YYYY-MM-DD HH24:MI:SS' )))      as enddate ";
      if (Status.equals("C") || Status.equals("TE"))

        sql += " FROM ehcm_emp_perinfo info   left join ehcm_empstatus_v st on st.ehcm_emp_perinfo_id = info.ehcm_emp_perinfo_id and (st.status='C' or st.status = 'TE') WHERE (st.ehcm_empstatus_v_id = ? or info.ehcm_emp_perinfo_id = ? ) ";
      else
        sql += " FROM ehcm_emp_perinfo info   WHERE  info.ehcm_emp_perinfo_id = ?   and (info.status='I' or info.status = 'UP') ";

      st = conn.prepareStatement(sql);
      if (Status.equals("C") || Status.equals("TE")) {
        st.setString(1, employeeId);
        st.setString(2, employeeId);
      } else
        st.setString(1, employeeId);
      log4j.debug("Employee Info : " + st.toString());

      rs = st.executeQuery();
      if (rs.next()) {
        employeeVO = new EmployeeVO();
        employeeVO.setEmployeeId(Utility.nullToEmpty(rs.getString("ehcm_emp_perinfo_id")));
        employeeVO.setEmpNo(Utility.nullToEmpty(rs.getString("value")));
        employeeVO.setEmpName(Utility.nullToEmpty(rs.getString("name")));
        employeeVO.setFourthName(Utility.nullToEmpty(rs.getString("fourthname")));
        employeeVO.setGradfatName(Utility.nullToEmpty(rs.getString("grandfathername")));
        employeeVO.setFamilyName(Utility.nullToEmpty(rs.getString("familyname")));
        employeeVO.setFatName(Utility.nullToEmpty(rs.getString("fathername")));
        employeeVO.setEmpArabicName(Utility.nullToEmpty(rs.getString("arabicname")));
        employeeVO.setArbfatName(Utility.nullToEmpty(rs.getString("arabicfatname")));
        employeeVO.setArbgradfatName(Utility.nullToEmpty(rs.getString("arbgrafaname")));
        employeeVO.setArbfamilyName(Utility.nullToEmpty(rs.getString("arabicfamilyname")));
        employeeVO.setArbfourthName(Utility.nullToEmpty(rs.getString("arbfouname")));
        // employeeVO.setNationalId(Utility.nullToEmpty(rs.getString("em_efin_nationality")));
        employeeVO.setNationalCode(Utility.nullToEmpty(rs.getString("nationality_identifier")));
        employeeVO.setActTypeId(Utility.nullToEmpty(rs.getString("ehcm_actiontype_id")));
        employeeVO.setGradeclassId(Utility.nullToEmpty(rs.getString("ehcm_gradeclass_id")));
        employeeVO.setStartdate(rs.getString("startdate") == null ? "" : rs.getString("startdate"));
        employeeVO.setEnddate(rs.getString("enddate") == null ? "" : rs.getString("enddate"));
        employeeVO.setHiredate(rs.getString("hiredate") == null ? "" : rs.getString("hiredate"));
        employeeVO
            .setGovhiredate(rs.getString("govthirdate") == null ? "" : rs.getString("govthirdate"));
        employeeVO.setLetterno(Utility.nullToEmpty(rs.getString("letterno")));
        employeeVO.setDecisionno(Utility.nullToEmpty(rs.getString("decisiono")));
        log4j.debug("dec:" + rs.getString("decisiono"));
        log4j.debug("getDecisionno:" + employeeVO.getDecisionno());
        employeeVO
            .setLetterdate(rs.getString("letterdate") == null ? "" : rs.getString("letterdate"));
        employeeVO
            .setDecisiondate(rs.getString("deciondate") == null ? "" : rs.getString("deciondate"));
        employeeVO.setTitleId(Utility.nullToEmpty(rs.getString("title")));
        employeeVO.setGender(Utility.nullToEmpty(rs.getString("gender")));
        employeeVO.setMobno(Utility.nullToEmpty(rs.getString("mobno")));
        employeeVO.setHomeno(Utility.nullToEmpty(rs.getString("homeno")));
        employeeVO.setWorkno(Utility.nullToEmpty(rs.getString("workno")));
        employeeVO.setOffice(Utility.nullToEmpty(rs.getString("officename")));
        employeeVO.setLocation(Utility.nullToEmpty(rs.getString("location")));
        employeeVO.setBloodtype(Utility.nullToEmpty(rs.getString("bloodtype")));
        employeeVO.setTownofbirth(Utility.nullToEmpty(rs.getString("townbirth")));
        employeeVO.setCountryId(Utility.nullToEmpty(rs.getString("c_country_id")));
        employeeVO.setCityId(Utility.nullToEmpty(rs.getString("c_city_id")));
        employeeVO.setMaritalstauts(Utility.nullToEmpty(rs.getString("marialstatus")));
        log4j.debug("getMaritalstauts:" + employeeVO.getMaritalstauts());
        employeeVO.setReligionId(Utility.nullToEmpty(rs.getString("ehcm_religion_id")));
        employeeVO.setNationalId(Utility.nullToEmpty(rs.getString("ehcm_addnationality_id")));
        employeeVO.setHeight(Utility.nullToEmpty(rs.getString("height")));
        employeeVO.setWeight(Utility.nullToEmpty(rs.getString("weight")));
        employeeVO.setEmail(Utility.nullToEmpty(rs.getString("email")));

        employeeVO.setStatus(Utility.nullToEmpty(rs.getString("status")));
        log4j.debug(employeeVO.getStatus());
        employeeVO.setDob(rs.getString("dob") == null ? "" : rs.getString("dob"));
        if (rs.getString("civimg") != null) {
          Image civimg = OBDal.getInstance().get(Image.class, rs.getString("civimg"));
          employeeVO.setCivimg(civimg);
          log4j.debug("imgidddaoc:" + rs.getString("civimg"));
        }
        if (rs.getString("wrkimg") != null) {
          Image wrkImg = OBDal.getInstance().get(Image.class, rs.getString("wrkimg"));
          employeeVO.setWrkimg(wrkImg);
          log4j.debug("imgidddaow:" + rs.getString("wrkimg"));
        }
        if (employeeVO.getStatus().equals("C")) {
          employeeVO.setPersonType(
              "EX-" + getPersonType(rs.getString("ad_client_id"), rs.getString("value")));
        }

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
    return employeeVO;
  }

  public EmployeeAddressVO getEmployeeAddress(String employeeId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    EmployeeAddressVO employeeAddVO = null;
    try {
      st = conn.prepareStatement(
          "select ehcm_emp_address_id,ehcm_address_style_id ,ehcm_emp_perinfo_id ,primary_ck,(select eut_convert_to_hijri (to_char(startdate,'YYYY-MM-DD HH24:MI:SS' )))	 as startdate1, "
              + "(select eut_convert_to_hijri (to_char(enddate,'YYYY-MM-DD HH24:MI:SS' ))) as enddate1,c_country_id,c_city_id,district,street,address1,address2,postbox,postalcode,"
              + " sec_c_country_id,sec_c_city_id,sec_district,sec_street,sec_address1,sec_address2,sec_postbox,sec_postalcode,(select eut_convert_to_hijri (to_char(sec_startdate,'YYYY-MM-DD HH24:MI:SS' ))) as startdate2,"
              + " (select eut_convert_to_hijri (to_char(sec_enddate,'YYYY-MM-DD HH24:MI:SS' ))) as enddate2,active from ehcm_emp_address where ehcm_emp_perinfo_id=? ");
      st.setString(1, employeeId);
      rs = st.executeQuery();
      if (rs.next()) {

        employeeAddVO = new EmployeeAddressVO();
        employeeAddVO.setEmpAddId(rs.getString("ehcm_emp_address_id"));
        employeeAddVO.setAddressStyleId(rs.getString("ehcm_address_style_id"));
        employeeAddVO.setEmployeeId(rs.getString("ehcm_emp_perinfo_id"));
        employeeAddVO.setCheckbox(rs.getString("primary_ck"));
        employeeAddVO.setStartDate(rs.getString("startdate1"));
        employeeAddVO.setEndDate(rs.getString("enddate1"));
        employeeAddVO.setCountryId(rs.getString("c_country_id"));
        employeeAddVO.setCityId(rs.getString("c_city_id"));
        employeeAddVO.setDistrict(rs.getString("district"));
        employeeAddVO.setStreet(rs.getString("street"));
        employeeAddVO.setAddress1(rs.getString("address1"));
        employeeAddVO.setAddress2(rs.getString("address2"));
        employeeAddVO.setPostBox(rs.getString("postbox"));
        employeeAddVO.setPostalCode(rs.getString("postalcode"));
        employeeAddVO.setSecCountryId(
            rs.getString("sec_c_country_id") == null ? "" : rs.getString("sec_c_country_id"));
        employeeAddVO.setSecCityId(
            rs.getString("sec_c_city_id") == null ? "" : rs.getString("sec_c_city_id"));
        employeeAddVO.setSecDistrict(
            rs.getString("sec_district") == null ? "" : rs.getString("sec_district"));
        employeeAddVO.setSecStreet(rs.getString("sec_street"));
        employeeAddVO.setSecAddress1(rs.getString("sec_address1"));
        employeeAddVO.setSecAddress2(
            (rs.getString("sec_address2") == null) ? "" : rs.getString("sec_address2"));
        employeeAddVO.setSecPostBox(rs.getString("sec_postbox"));
        employeeAddVO.setSecPostalCode(rs.getString("sec_postalcode"));
        employeeAddVO.setStartDate1(rs.getString("startdate2"));
        employeeAddVO.setEndDate1(rs.getString("enddate2"));
        employeeAddVO.setActive(rs.getString("active"));

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
    return employeeAddVO;
  }

  public String getPersonType(String clientId, String empNo) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String personType = "";
    try {
      st = conn.prepareStatement(
          " select persontype from ehcm_emp_perinfo info left join  ehcm_actiontype act on act.ehcm_actiontype_id= info.ehcm_actiontype_id "
              + " where info.value=? and info.status='I' and info.ad_client_id=? ");
      st.setString(1, empNo);
      st.setString(2, clientId);
      rs = st.executeQuery();
      if (rs.next()) {
        personType = rs.getString("persontype");
        return personType;
      } else
        return "";
    } catch (final SQLException e) {
      log4j.error("Exception in checkEmpAlreadyExists", e);
      return "";
    } catch (final Exception e) {
      log4j.error("Exception in checkEmpAlreadyExists", e);
      return "";
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in checkEmpAlreadyExists", e);
      }
    }
  }

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
      log4j.error("Exception creating multiple transactions from payments", e);

    }
    return greDate;

  }

  public String getEmployeeAddressId(String employeeId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = conn.prepareStatement(
          "select ehcm_emp_address_id from ehcm_emp_address where ehcm_emp_perinfo_id=?");
      st.setString(1, employeeId);
      log4j.debug("empaddid:" + st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        String employeeAddressId = rs.getString("ehcm_emp_address_id");
        return employeeAddressId;
      } else
        return "";
    } catch (final SQLException e) {
      log4j.error("Exception in getEmployeeAddressId", e);
      return "";
    } catch (final Exception e) {
      log4j.error("Exception in getEmployeeAddressId", e);
      return "";
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getEmployeeAddressId", e);
      }
    }
  }

  public String redirectStr(String tab, String employeeId, String empStatus,
      String employeeStatus) {
    String redirStr = "";
    try {
      log4j.debug("tab:" + tab);
      String url = "inpEmployeeId=" + employeeId + "&inpEmpStatus=" + empStatus
          + "&inpEmployeeStatus=" + employeeStatus;
      if (tab.equals("EMP")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView&" + url;
      } else if (tab.equals("EMPINF")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.employment.header/Employment?inpAction=GridView&" + url;
      } else if (tab.equals("EMPADD")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView&"
            + url;
      } else if (tab.equals("Dependent")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents?" + url;
      } else if (tab.equals("EMPCTRCT")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&" + url;
      } else if (tab.equals("EMPQUAL")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification?inpAction=GridView&"
            + url;
      } else if (tab.equals("Asset")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.asset.header/Asset?" + url;
      } else if (tab.equals("PREEMP")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment?" + url;
      } else if (tab.equals("DOC")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.documents.header/Documents?" + url;
      } else if (tab.equals("MEDIN")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.MedicalInsurance.header/MedicalInsurance?" + url;
      } else if (tab.equals("PERPAYMETHOD")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.header/PersonalPaymentMethod?"
            + url;
      }
    } catch (final Exception e) {
      log4j.error("Exception in redirectStr", e);
    }
    return redirStr;
  }

  public EmployeeVO getagevalue(String clientId) {
    EmployeeVO vo = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = conn.prepareStatement(
          "select em_ehcm_minempage,em_ehcm_maxempage from ad_client where ad_client_id= ?");
      st.setString(1, clientId);
      rs = st.executeQuery();
      log4j.debug("getagevalue" + st.toString());
      if (rs.next()) {
        vo = new EmployeeVO();
        vo.setActive((rs.getString("em_ehcm_minempage")));
        vo.setCategoryId((rs.getString("em_ehcm_maxempage")));

      }
    } catch (final Exception e) {
      log4j.error("Exception in getagevalue", e);
    }
    return vo;
  }

  public int insertEmpLeave(EhcmEmpPerInfo employee) {
    int count = 0;
    EHCMAbsenceTypeAccruals accrualQry = null;
    List<EHCMAbsenceTypeAccruals> accrualList = new ArrayList<EHCMAbsenceTypeAccruals>();
    List<EHCMEmpLeave> empLeaveList = new ArrayList<EHCMEmpLeave>();
    String tempAbsenceTypeId = null;
    Boolean isGradeClassPresent = false, isAllGradeClassPresent = false;
    JSONObject result = null;
    Date StartDate = null;
    Date EndDate = null;
    String startDate = null;
    String endDate = null;
    int startYear = 0, endYear = 0;
    int diff = 0;
    String oneDayAfter = null;
    try {

      // get absencetype accrual
      OBQuery<EHCMAbsenceTypeAccruals> accruals = OBDal.getInstance().createQuery(
          EHCMAbsenceTypeAccruals.class,
          " as e where e.absenceType.id in ( select a.id from EHCM_Absence_Type  a where a.isAccrual='Y' and a.enabled='Y' "
              + "  ) and (e.gradeClassifications.id=:gradeclass  or e.gradeClassifications is null )  and e.enabled='Y' "
              + " order by e.absenceType.id , e.gradeClassifications asc ");
      accruals.setNamedParameter("gradeclass", employee.getGradeClass().getId());
      accrualList = accruals.list();
      if (accrualList.size() > 0) {
        for (EHCMAbsenceTypeAccruals accrual : accrualList) {
          log4j.debug("accrual:" + accrual.getAbsenceType());

          // check absence type having accrual for both blank grade and corresponding employee grade
          // if balnk grade(all grade) accrual define then isAllGradeClassPresent=true
          // or if employee grade accrual define then isGradeClassPresent=true
          if (tempAbsenceTypeId == null
              || tempAbsenceTypeId.equals(accrual.getAbsenceType().getId())) {
            if (accrual.getGradeClassifications() != null) {
              isGradeClassPresent = true;
            } else {
              isAllGradeClassPresent = true;
            }
            tempAbsenceTypeId = accrual.getAbsenceType().getId();

          } else {
            isGradeClassPresent = false;
            isAllGradeClassPresent = false;
            if (accrual.getGradeClassifications() != null) {
              isGradeClassPresent = true;
            } else {
              isAllGradeClassPresent = true;
            }
            tempAbsenceTypeId = accrual.getAbsenceType().getId();

          }
          // if already corresponding employee grade accrual insert then continue the loop
          if (isGradeClassPresent && isAllGradeClassPresent) {
            continue;
          }

          StartDate = accrual.getStartDate();
          if (accrual.getStartDate() != null) {
            startDate = UtilityDAO.convertTohijriDate(yearFormat.format(accrual.getStartDate()));
            startYear = Integer.parseInt(startDate.split("-")[2]);
          }
          if (accrual.getEndDate() != null) {
            endDate = UtilityDAO.convertTohijriDate(yearFormat.format(accrual.getEndDate()));
            endYear = Integer.parseInt(endDate.split("-")[2]);
          } else {
            endYear = startYear;
          }
          diff = (endYear - startYear) + 1;
          for (int i = 0; i < diff; i++) {

            if (StartDate != null) {
              result = sa.elm.ob.hcm.util.UtilityDAO.getMinMaxStartDateUsingDate(StartDate);
            }

            // check already empleave exists or not
            OBQuery<EHCMEmpLeave> empLeaveQry = OBDal.getInstance().createQuery(EHCMEmpLeave.class,
                " as e where e.absenceType.id=:absenceType and e.ehcmEmpPerinfo.id=:employeeId "
                    + "   and to_date(:startDate,'yyyy-MM-dd') between  to_date(to_char(e.startDate ,'dd-MM-yyyy'),'dd-MM-yyyy') "
                    + "          and to_date(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy')  ");
            empLeaveQry.setNamedParameter("absenceType", accrual.getAbsenceType().getId());
            empLeaveQry.setNamedParameter("employeeId", employee.getId());
            empLeaveQry.setNamedParameter("startDate", StartDate);
            empLeaveQry.setMaxResult(1);
            empLeaveList = empLeaveQry.list();
            if (empLeaveList.size() == 0) {
              EHCMEmpLeave leave = OBProvider.getInstance().get(EHCMEmpLeave.class);
              leave.setClient(employee.getClient());
              leave.setOrganization(employee.getOrganization());
              leave.setCreationDate(new java.util.Date());
              leave.setCreatedBy(employee.getCreatedBy());
              leave.setUpdated(new java.util.Date());
              leave.setUpdatedBy(employee.getUpdatedBy());
              leave.setEhcmEmpPerinfo(employee);
              leave.setAbsenceType(accrual.getAbsenceType());
              leave.setGradeClassifications(employee.getGradeClass());
              leave.setEnabled(true);
              leave.setOfLeaves(new BigDecimal(0));// accrual.getDays();
              leave.setPooleddays(new BigDecimal(0));
              leave.setAvailabledays(new BigDecimal(0));
              leave.setAvaileddays(new BigDecimal(0));
              leave.setCreditOn(accrual.getCreditOn());
              if (result != null) {
                leave.setStartDate(YearFormat.parse(result.getString("mingregdate")));
                leave.setEndDate(YearFormat.parse(result.getString("maxgregdate")));

                if (diff > 1 && (diff - 1) > i) {
                  oneDayAfter = UtilityDAO
                      .convertTohijriDate(yearFormat.format(leave.getEndDate()));
                  oneDayAfter = sa.elm.ob.hcm.util.UtilityDAO.getAfterDateInGreg(oneDayAfter);
                  StartDate = dateFormat.parse(oneDayAfter);
                }
              }
              OBDal.getInstance().save(leave);
            }
          }
        }
      }

      //
      // get absencetype accural

      /*
       * OBQuery<EHCMAbsenceTypeAccruals> accruals1 = OBDal.getInstance().createQuery(
       * EHCMAbsenceTypeAccruals.class,
       * " as e where e.absenceType.id in ( select a.id from EHCM_Absence_Type  a where a.isAccrual='Y' and a.enabled='Y' "
       * +
       * "  ) and (e.gradeClassifications.id=:gradeclass  or e.gradeClassifications is null )  and e.enabled='Y' "
       * + " order by e.absenceType.id , e.gradeClassifications asc ");
       * accruals.setNamedParameter("gradeclass", employee.getGradeClass().getId()); accrualList =
       * accruals.list(); if (accrualList.size() > 0) { for (EHCMAbsenceTypeAccruals accrual :
       * accrualList) { log4j.debug("accrual:" + accrual.getAbsenceType());
       * 
       * // check absence type having accrual both blank gradeclass and corresponding employee // if
       * all grade class accrual define then isAllGradeClassPresent=true // or if employee grade
       * class accrual define then isGradeClassPresent=true if (tempAbsenceTypeId == null ||
       * tempAbsenceTypeId.equals(accrual.getAbsenceType().getId())) { if
       * (accrual.getGradeClassifications() != null) { isGradeClassPresent = true; } else {
       * isAllGradeClassPresent = true; } tempAbsenceTypeId = accrual.getAbsenceType().getId();
       * 
       * } else { isGradeClassPresent = false; isAllGradeClassPresent = false; if
       * (accrual.getGradeClassifications() != null) { isGradeClassPresent = true; } else {
       * isAllGradeClassPresent = true; } tempAbsenceTypeId = accrual.getAbsenceType().getId();
       * 
       * } // if already corresponding employee grade accrual insert then continue the loop if
       * (isGradeClassPresent && isAllGradeClassPresent) { continue; }
       * 
       * // check employee leave already exists or not for the particular employee if exsits then //
       * update no of leave only OBQuery<EHCMEmpLeave> empLeaveQry =
       * OBDal.getInstance().createQuery(EHCMEmpLeave.class,
       * " as e where e.absenceType.id=:absenceType and e.ehcmEmpPerinfo.id=:employeeId ");
       * empLeaveQry.setNamedParameter("absenceType", accrual.getAbsenceType().getId());
       * empLeaveQry.setNamedParameter("employeeId", employee.getId()); empLeaveQry.setMaxResult(1);
       * empLeaveList = empLeaveQry.list(); if (empLeaveList.size() == 0) {
       * 
       * EHCMEmpLeave leave = OBProvider.getInstance().get(EHCMEmpLeave.class);
       * leave.setClient(employee.getClient()); leave.setOrganization(employee.getOrganization());
       * leave.setCreationDate(new java.util.Date()); leave.setCreatedBy(employee.getCreatedBy());
       * leave.setUpdated(new java.util.Date()); leave.setUpdatedBy(employee.getUpdatedBy());
       * leave.setEhcmEmpPerinfo(employee); leave.setAbsenceType(accrual.getAbsenceType());
       * leave.setGradeClassifications(employee.getGradeClass()); leave.setEnabled(true);
       * leave.setOfLeaves(accrual.getDays()); leave.setPooleddays(new BigDecimal(0));
       * leave.setAvailabledays(new BigDecimal(0)); leave.setAvaileddays(new BigDecimal(0));
       * leave.setCreditOn(accrual.getCreditOn()); OBDal.getInstance().save(leave);
       * OBDal.getInstance().flush(); } else { EHCMEmpLeave leave = empLeaveList.get(0);
       * leave.setOfLeaves(leave.getOfLeaves().add(accrual.getDays()));
       * OBDal.getInstance().save(leave); } }
       * 
       * }
       */
      OBDal.getInstance().flush();

    } catch (

    final Exception e) {
      log4j.error("Exception in insertEmpLeave");
    }
    return count;

  }

  public ehcmempstatus insertempstatus(EhcmEmpPerInfo employee, String startdate, String enddate,
      String decisionno, String mcsletterno, String mcsletterdate) {

    int count = 0;
    ehcmempstatus ehcmempstatus = null;
    try {

      EmployeeDAO dao = null;
      Connection con = null;
      dao = new EmployeeDAO(con);
      ehcmempstatus = OBProvider.getInstance().get(ehcmempstatus.class);
      ehcmempstatus.setClient(employee.getClient());
      ehcmempstatus.setOrganization(employee.getOrganization());
      ehcmempstatus.setCreationDate(new java.util.Date());
      ehcmempstatus.setCreatedBy(employee.getCreatedBy());
      ehcmempstatus.setUpdated(new java.util.Date());
      ehcmempstatus.setUpdatedBy(employee.getUpdatedBy());
      ehcmempstatus.setEnabled(false);
      ehcmempstatus.setEhcmEmpPerinfo(employee);
      ehcmempstatus.setDecisionno(decisionno);
      ehcmempstatus.setStartDate(dao.convertGregorian(startdate));
      ehcmempstatus.setMcsletterno(mcsletterno);
      ehcmempstatus.setMcsletterdate(dao.convertGregorian(mcsletterdate));
      if (StringUtils.isNotEmpty(enddate))
        ehcmempstatus.setTodate(dao.convertGregorian(enddate));
      else {
        ehcmempstatus.setTodate(null);
      }
      ehcmempstatus.setStatus("C");
      OBDal.getInstance().save(ehcmempstatus);
      OBDal.getInstance().flush();
    } catch (final Exception e) {
      log4j.error("Exception in insertempstatus");
    }
    return ehcmempstatus;

  }

  public String getEmployeeStatus(String employeeId, String lang) {
    String employeeStatus = null;
    List<ListTrl> listTrlList = null;
    try {

      EHCMEmployeeStatusV empStatusV = OBDal.getInstance().get(EHCMEmployeeStatusV.class,
          employeeId);
      if (empStatusV != null) {
        OBQuery<ListTrl> translation = OBDal.getInstance().createQuery(ListTrl.class,
            " as e where e.listReference.id=:listId and e.language.language=:lang ");
        translation.setNamedParameter("listId", empStatusV.getListReference().getId());
        translation.setNamedParameter("lang", lang);
        translation.setMaxResult(1);
        log4j.debug("sds" + translation.list().size());
        listTrlList = translation.list();

        if (listTrlList.size() > 0) {
          employeeStatus = listTrlList.get(0).getName();
        } else {
          employeeStatus = empStatusV.getJobGroupName();
        }
      } else {
        employeeStatus = "";
      }

    } catch (final Exception e) {
      log4j.error("Exception in getEmployeeStatus", e);
    }
    return employeeStatus;

  }

  public boolean checkExtendServiceExist(String employeeId) {
    boolean extendService = false;
    OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
        " as e where e.ehcmEmpPerinfo.id =:employeeid and e.ehcmExtendService.id is not null");
    empInfoObj.setNamedParameter("employeeid", employeeId);
    List<EmploymentInfo> empInfoList = empInfoObj.list();
    if (empInfoList.size() > 0) {
      extendService = true;
    }
    return extendService;

  }

  public void updateArabicFullName(String employeeId) {

    try {
      OBContext.setAdminMode();
      if (StringUtils.isNotEmpty(employeeId)) {
        EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
        employee.setArabicfullname(employee.getArabicname() + " " + employee.getArabicfatname()
            + " " + employee.getArbgrafaname() + " " + employee.getArabicfamilyname());
        OBDal.getInstance().save(employee);
      }

    } catch (final Exception e) {
      log4j.error("Exception in getagevalue", e);
    }
  }

  public String countryName(String countryId) {
    String countryName = null;
    try {
      Country countryObj = OBDal.getInstance().get(Country.class, countryId);
      countryName = countryObj.getName();

    } catch (final Exception e) {
      log4j.error("Exception in countryName", e);
    }
    return countryName;
  }

  public String cityName(String cityId) {
    String cityName = null;
    try {
      City cityObj = OBDal.getInstance().get(City.class, cityId);
      cityName = cityObj.getName();

    } catch (final Exception e) {
      log4j.error("Exception in cityName", e);
    }
    return cityName;
  }

  public String getAddressStyleName(String addressStyleId) {
    String addressName = null;
    try {
      EhcmAddressStyle addressObj = OBDal.getInstance().get(EhcmAddressStyle.class, addressStyleId);
      addressName = addressObj.getCommercialName();

    } catch (final Exception e) {
      log4j.error("Exception in getAddressStyleName", e);
    }
    return addressName;
  }

  public String dependentsName(String dependentId) {
    String dependentName = null;
    try {
      Ehcmdependentsv dependentObj = OBDal.getInstance().get(Ehcmdependentsv.class, dependentId);
      dependentName = dependentObj.getDependentname();

    } catch (final Exception e) {
      log4j.error("Exception in dependentsName", e);
    }
    return dependentName;
  }

  public String insuranceSchemaName(String insuranceId) {
    String insuranceSchemaName = null;
    try {
      EHCMDeflookupsTypeLn defLkupObj = OBDal.getInstance().get(EHCMDeflookupsTypeLn.class,
          insuranceId);
      insuranceSchemaName = defLkupObj.getName();

    } catch (final Exception e) {
      log4j.error("Exception in insuranceSchemaName", e);
    }
    return insuranceSchemaName;
  }

  public EhcmEmpPerInfo insertOrUpdateEmployee(HttpServletRequest request, String employeeId,
      VariablesSecureApp vars) {
    EhcmEmpPerInfo perinfo = null;
    try {

      // need to insert a record in employee personal info table
      if (employeeId.equals("")) {
        perinfo = OBProvider.getInstance().get(EhcmEmpPerInfo.class);
        perinfo.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
        perinfo.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
        perinfo.setCreationDate(new java.util.Date());
        perinfo.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      } else {
        perinfo = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
      }

      perinfo.setUpdated(new java.util.Date());
      perinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      perinfo.setNationalityIdentifier(request.getParameter("inpNatIdf").toString());
      perinfo.setSearchKey(request.getParameter("inpEmpNo"));
      if (request.getParameter("inpCountry") != null) {
        perinfo.setCountry(
            OBDal.getInstance().get(Country.class, request.getParameter("inpCountry").toString()));
      }
      if (StringUtils.isNotEmpty(request.getParameter("inpCity"))) {
        perinfo.setCity(
            OBDal.getInstance().get(City.class, request.getParameter("inpCity").toString()));
      }
      if (StringUtils.isNotEmpty(request.getParameter("inpNat"))) {
        perinfo.setEhcmAddnationality(OBDal.getInstance().get(EhcmAddNationality.class,
            request.getParameter("inpNat").toString()));
      }
      if (StringUtils.isNotEmpty(request.getParameter("inpRel"))) {
        perinfo.setEhcmReligion(
            OBDal.getInstance().get(EhcmReligion.class, request.getParameter("inpRel").toString()));
      }
      /* cat detail */
      perinfo.setEhcmActiontype(
          OBDal.getInstance().get(EhcmActiontype.class, request.getParameter("inpSalutation")));
      if (request.getParameter("inpEmpCat") != null)
        perinfo.setGradeClass(
            OBDal.getInstance().get(ehcmgradeclass.class, request.getParameter("inpEmpCat")));
      if (request.getParameter("inpStartDate") != null
          && request.getParameter("inpStartDate") != "")
        perinfo.setStartDate(convertGregorian(request.getParameter("inpStartDate").toString()));
      if (request.getParameter("inpEndDate") != null && request.getParameter("inpEndDate") != "")
        perinfo.setEndDate(convertGregorian(request.getParameter("inpEndDate").toString()));
      if (request.getParameter("inpHireDate") != null && request.getParameter("inpHireDate") != "")
        perinfo.setHiredate(convertGregorian(request.getParameter("inpHireDate").toString()));
      if (request.getParameter("inpGovHireDate") != null
          && request.getParameter("inpGovHireDate") != "")
        perinfo.setGovhiredate(convertGregorian(request.getParameter("inpGovHireDate").toString()));
      else
        perinfo.setGovhiredate(null);
      if (request.getParameter("inpMcsLetterDate") != null
          && request.getParameter("inpMcsLetterDate") != "")
        perinfo.setMcsletterdate(
            convertGregorian(request.getParameter("inpMcsLetterDate").toString()));
      else
        perinfo.setMcsletterdate(null);
      perinfo.setMcsletterno(request.getParameter("inpMcsLetterNo").toString());
      if (request.getParameter("inpDecisionDate") != null
          && request.getParameter("inpDecisionDate") != "")
        perinfo
            .setDecisiondate(convertGregorian(request.getParameter("inpDecisionDate").toString()));
      perinfo.setDecisionno(request.getParameter("inpDecisionNo").toString());

      /* name details */
      perinfo.setEhcmTitletype(
          OBDal.getInstance().get(EhcmTitletype.class, request.getParameter("inpTitle")));
      perinfo.setGender(request.getParameter("inpGen").toString());
      perinfo.setGrandfathername(request.getParameter("inpEngGraFatName").toString());
      perinfo.setArbgrafaname(request.getParameter("inpAraGraFatName").toString());

      perinfo.setArabicfamilyname(
          Utility.unescapeHTML(request.getParameter("inpAraFamName").toString()));
      perinfo.setFamilyname(request.getParameter("inpEngFamName").toString());

      perinfo.setArabicname(Utility.unescapeHTML(request.getParameter("inpAraFName").toString()));
      perinfo.setName(request.getParameter("inpEngFName"));

      perinfo
          .setArabicfatname(Utility.unescapeHTML(request.getParameter("inpAraFatName").toString()));
      perinfo.setFathername(request.getParameter("inpEngFatName").toString());

      perinfo
          .setArbfouname(Utility.unescapeHTML(request.getParameter("inpAraFourthName").toString()));
      perinfo.setFourthname(request.getParameter("inpEngFourthName"));

      /* contact details */
      perinfo.setMobno(request.getParameter("inpMobno"));
      perinfo.setHomeno(request.getParameter("inpHomeNo"));
      perinfo.setWorkno(request.getParameter("inpWorkNo"));
      perinfo.setOfficename(request.getParameter("inpOff"));
      perinfo.setEmail(request.getParameter("inpEmail"));
      perinfo.setLocation(request.getParameter("inpLoc"));
      if (!StringUtils.isEmpty(request.getParameter("inpmary"))) {
        perinfo.setMarrieddate(convertGregorian(request.getParameter("inpmary").toString()));
      }
      /* image details */
      Image civImg = null;
      String mimetype = request.getParameter("inpcivfiletype");

      String civ = request.getParameter("inpcivfilebyte");
      String wrk = request.getParameter("inpwrkfilebyte");

      String[] civparts = civ.split("base64,");
      String[] wrkparts = wrk.split("base64,");

      if (civparts.length > 1) {
        byte[] civbytes = Base64.decodeBase64(civparts[1]);
        civImg = OBProvider.getInstance().get(Image.class);
        civImg.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
        civImg.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
        civImg.setName(request.getParameter("inpcivfilename"));
        civImg.setBindaryData(civbytes);
        civImg.setWidth(new Long(200));
        civImg.setHeight(new Long(200));
        civImg.setMimetype(mimetype);
        OBDal.getInstance().save(civImg);
        OBDal.getInstance().flush();
        perinfo.setCIVAdImage(civImg);

      }
      if (wrkparts.length > 1) {
        Image wrkImg = null;
        byte[] wrkbytes = Base64.decodeBase64(wrkparts[1]);

        wrkImg = OBProvider.getInstance().get(Image.class);
        wrkImg.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
        wrkImg.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
        wrkImg.setName(request.getParameter("inpwrkfilename"));
        wrkImg.setBindaryData(wrkbytes);
        wrkImg.setWidth(new Long(200));
        wrkImg.setHeight(new Long(200));
        wrkImg.setMimetype(mimetype);
        OBDal.getInstance().save(wrkImg);
        OBDal.getInstance().flush();
        perinfo.setWorkAdImage(wrkImg);

      }

      /* personal details */
      perinfo.setDob(convertGregorian(request.getParameter("inpDoj").toString()));
      perinfo.setHeight(request.getParameter("inpHeight").toString());
      perinfo.setWeight(request.getParameter("inpWeight").toString());
      perinfo.setMarialstatus(request.getParameter("inpMarStat").toString());
      perinfo.setBloodtype(request.getParameter("inpBlodTy").toString());
      perinfo.setTownbirth(request.getParameter("inpTownBirth").toString());
      /*
       * if (request.getParameter("inpStatus") != null &&
       * request.getParameter("inpStatus").equals("C")) perinfo.setStatus("C"); else
       */
      if (request.getParameter("inpStatus") != null
          && request.getParameter("inpStatus").equals("I"))
        perinfo.setStatus("I");
      else
        perinfo.setStatus("UP");
      if (request.getParameter("inpStatus") != null
          && !request.getParameter("inpStatus").equals("C"))
        perinfo.setPersonType(
            OBDal.getInstance().get(EhcmActiontype.class, request.getParameter("inpSalutation")));
      /*
       * else if (request.getParameter("inpStatus") != null &&
       * request.getParameter("inpStatus").equals("C")) { EhcmEmpPerInfo experinfo =
       * OBDal.getInstance().get(EhcmEmpPerInfo.class, request.getParameter("inpExEmployeeId"));
       * perinfo.setPersonType(experinfo.getEhcmActiontype()); }
       */

      OBDal.getInstance().save(perinfo);
      OBDal.getInstance().flush();

    } catch (final Exception e) {
      log4j.error("Exception in insertOrUpdateEmployee", e);
    }
    return perinfo;
  }

  public void updateBusinessPartnerDetails(EhcmEmpPerInfo perinfo, String saluation) {
    List<BusinessPartner> bpartnerList = null;
    try {

      if (saluation.equals("HE") || saluation.equals("HC") || saluation.equals("HA")) {
        OBQuery<BusinessPartner> bp = OBDal.getInstance().createQuery(BusinessPartner.class,
            " ehcmEmpPerinfo.id=:employeeId ");
        bp.setNamedParameter("employeeId", perinfo.getId());
        bp.setMaxResult(1);
        bpartnerList = bp.list();
        if (bpartnerList.size() > 0) {
          BusinessPartner bpart = bpartnerList.get(0);
          bpart.setSearchKey(perinfo.getSearchKey());
          bpart.setName(perinfo.getArabicfullname());
          bpart.setName2(perinfo.getArabicname());
          bpart.setEfinIdentityname("NID");
          bpart.setEfinNationalidnumber(perinfo.getNationalityIdentifier());
          bpart.setEhcmEmpPerinfo(perinfo);
          bpart.setEfinNationality(
              OBDal.getInstance().get(Country.class, perinfo.getCountry().getId()));
          bpart.setEhcmProcessing(true);
          OBDal.getInstance().save(bpart);
          OBDal.getInstance().flush();
        }
      }

    } catch (

    final Exception e) {
      log4j.error("Exception in updateBusinessPartnerDetails", e);
    }
  }

  public ehcmempstatus insertOrUpdateEmpStatus(HttpServletRequest request,
      VariablesSecureApp vars) {
    List<ehcmempstatus> empStatusList = null;
    EhcmEmpPerInfo perinfo = null;
    ehcmempstatus employeestatus = null;
    try {

      perinfo = OBDal.getInstance().get(EhcmEmpPerInfo.class,
          request.getParameter("inpExEmployeeId"));

      OBQuery<ehcmempstatus> duplicate = OBDal.getInstance().createQuery(ehcmempstatus.class,
          " ehcmEmpPerinfo.id=:employeeId ");
      duplicate.setNamedParameter("employeeId", request.getParameter("inpExEmployeeId"));
      duplicate.setMaxResult(1);
      empStatusList = duplicate.list();
      if (empStatusList.size() > 0) {
        employeestatus = empStatusList.get(0);

        employeestatus.setUpdated(new java.util.Date());
        employeestatus.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        employeestatus.setDecisionno(request.getParameter("inpDecisionNo").toString());
        if (request.getParameter("inpStartDate") != null
            && request.getParameter("inpStartDate") != "")
          employeestatus
              .setStartDate(convertGregorian(request.getParameter("inpStartDate").toString()));
        if (request.getParameter("inpEndDate") != null && request.getParameter("inpEndDate") != "")
          employeestatus.setTodate(convertGregorian(request.getParameter("inpEndDate").toString()));

        employeestatus.setStatus(request.getParameter("inpStatus").toString());
        OBDal.getInstance().save(employeestatus);
        // OBDal.getInstance().flush();
        // OBDal.getInstance().commitAndClose();

      } else {
        employeestatus = insertempstatus(perinfo, request.getParameter("inpStartDate").toString(),
            request.getParameter("inpEndDate").toString(), request.getParameter("inpDecisionNo"),
            request.getParameter("inpMcsLetterNo"),
            request.getParameter("inpMcsLetterDate").toString());
      }

    } catch (

    final Exception e) {
      log4j.error("Exception in insertOrUpdateEmpStatus", e);
    }
    return employeestatus;
  }

  public BusinessPartner insertBusinessPartner(VariablesSecureApp vars, EhcmEmpPerInfo person,
      Category category, Currency currencyObj, Connection con, String bpartnerSeqName) {
    BusinessPartner partner = null;
    try {
      partner = OBProvider.getInstance().get(BusinessPartner.class);
      partner.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
      partner.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
      partner.setCreationDate(new java.util.Date());
      partner.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      partner.setUpdated(new java.util.Date());
      partner.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      partner.setSearchKey(person.getSearchKey());
      partner.setName(person.getArabicname().concat(" ").concat(person.getArabicfatname())
          .concat(" ").concat(person.getArbgrafaname()));
      // partner.setName2(person.getFourthname());
      partner.setName2(person.getArabicname());
      partner.setBusinessPartnerCategory(category);
      partner.setEmployee(true);
      partner.setCustomer(true);
      partner.setVendor(true);
      partner.setEfinIdentityname("NID");
      partner.setCurrency(currencyObj);
      partner.setEfinNationalidnumber(person.getNationalityIdentifier());
      partner
          .setEfinNationality(OBDal.getInstance().get(Country.class, person.getCountry().getId()));
      partner.setEhcmEmpPerinfo(person);
      partner
          .setEfinDocumentno(Utility.getSequenceNo(con, vars.getClient(), bpartnerSeqName, false));
      OBDal.getInstance().save(partner);
      OBDal.getInstance().flush();

    } catch (

    final Exception e) {
      log4j.error("Exception in insertBusinessPartner", e);
    }
    return partner;
  }

  public void updatePositionOnIssueDecision(EmploymentInfo objEmplyment, EhcmEmpPerInfo person,
      VariablesSecureApp vars) {
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
    try {
      /*
       * EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
       * objEmplyment.getPosition().getId());
       * 
       * Task No.6797 objPosition .setAssignedEmployee(OBDal.getInstance().get(EmployeeView.class,
       * person.getId()));
       */

      // insert position employee history
      assingedOrReleaseEmpInPositionDAO.insertPositionEmployeeHisotry(objEmplyment.getClient(),
          objEmplyment.getOrganization(), objEmplyment.getEhcmEmpPerinfo(), null,
          objEmplyment.getStartDate(), objEmplyment.getEndDate(), objEmplyment.getDecisionNo(),
          objEmplyment.getDecisionDate(), objEmplyment.getPosition(), vars, null, null, null);

      objEmplyment.setDecisionDate(person.getDecisiondate());
      OBDal.getInstance().save(objEmplyment);

    } catch (

    final Exception e) {
      log4j.error("Exception in updatePosition", e);
    }
  }

  public void inActiveTheBusinessPartner(String EmployeeId, VariablesSecureApp vars) {
    List<BusinessPartner> bpartnerList = null;
    try {
      OBQuery<BusinessPartner> bpar = OBDal.getInstance().createQuery(BusinessPartner.class,
          " ehcmEmpPerinfo.id=:employeeId ");
      bpar.setMaxResult(1);
      bpartnerList = bpar.list();
      if (bpartnerList.size() > 0) {
        BusinessPartner bp = bpartnerList.get(0);
        bp.setUpdated(new java.util.Date());
        bp.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        bp.setActive(false);
        OBDal.getInstance().save(bp);
        OBDal.getInstance().flush();
      }

    } catch (

    final Exception e) {
      log4j.error("Exception in inActiveTheBusinessPartner", e);
    }
  }

  public void updateEmployeeEnddateAndPositionReleaseInCancel(String employeeId,
      VariablesSecureApp vars, EhcmEmpPerInfo issuedEmp, ehcmempstatus cancelEmp) {
    List<EmploymentInfo> employInfoList = null;
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
    Date dateBeforeEmployInfo = null;
    Date startDateEmployInfo = null;
    try {

      // update employee enddate

      issuedEmp.setUpdated(new java.util.Date());
      issuedEmp.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      issuedEmp.setEnabled(false);
      Date startdate = issuedEmp.getStartDate();
      Date dateBefore = new Date(cancelEmp.getStartDate().getTime() - 1 * 24 * 3600 * 1000);

      if (startdate.compareTo(cancelEmp.getStartDate()) == 0)
        issuedEmp.setEndDate(cancelEmp.getStartDate());
      else
        issuedEmp.setEndDate(dateBefore);
      OBDal.getInstance().save(issuedEmp);

      // update employement enddate while cancel
      OBQuery<EmploymentInfo> empinfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and enabled='Y' order by creationDate desc");
      empinfo.setNamedParameter("employeeId", employeeId);
      empinfo.setMaxResult(1);
      employInfoList = empinfo.list();
      if (employInfoList.size() > 0) {
        EmploymentInfo recentEmployInfo = employInfoList.get(0);
        recentEmployInfo.setUpdated(new java.util.Date());
        recentEmployInfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        recentEmployInfo.setEnabled(false);
        startDateEmployInfo = recentEmployInfo.getStartDate();
        dateBeforeEmployInfo = new Date(cancelEmp.getStartDate().getTime() - 1 * 24 * 3600 * 1000);

        if (startDateEmployInfo.compareTo(cancelEmp.getStartDate()) == 0)
          recentEmployInfo.setEndDate(cancelEmp.getStartDate());
        else
          recentEmployInfo.setEndDate(dateBeforeEmployInfo);
        OBDal.getInstance().save(recentEmployInfo);
        OBDal.getInstance().flush();
        /* release the employee */
        EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
            recentEmployInfo.getPosition().getId());
        /*
         * Task No.6797 pos.setAssignedEmployee(null); OBDal.getInstance().save(info);
         * OBDal.getInstance().flush();
         */

        // delete the position employee history
        assingedOrReleaseEmpInPositionDAO.deletePositionEmployeeHisotry(issuedEmp, pos);

      }

    } catch (

    final Exception e) {
      log4j.error("Exception in inActiveTheBusinessPartner", e);
    }
  }

  public void insertBusinessMissionCategory(EhcmEmpPerInfo perinfo) {
    List<EHCMMissionCategory> missionCatList = null;
    MissionCategoryDAO missionCategoryDAO = new MissionCategoryDAOImpl();
    EHCMMisCatPeriod empMiscatPeriod = null;
    EHCMMiscatEmployee misCatEmployee = null;
    try {

      OBQuery<EHCMMissionCategory> missionCategoryQry = OBDal.getInstance()
          .createQuery(EHCMMissionCategory.class, " as e where e.client.id=:clientId");
      missionCategoryQry.setNamedParameter("clientId", perinfo.getClient().getId());

      missionCatList = missionCategoryQry.list();
      if (missionCatList.size() > 0) {
        for (EHCMMissionCategory missionCatObj : missionCatList) {

          missionCategoryDAO.addNewEmployeesToAllPeriodGreaterThanOfEmpStartDate(missionCatObj,
              perinfo.getCreatedBy(), perinfo.getGradeClass(), perinfo);

          /*
           * empMiscatPeriod = missionCategoryDAO.getRecentEmpCategoryPeriod(missionCatObj); if
           * (empMiscatPeriod != null) {
           * 
           * misCatEmployee = OBProvider.getInstance().get(EHCMMiscatEmployee.class);
           * misCatEmployee.setClient(missionCatObj.getClient());
           * misCatEmployee.setOrganization(missionCatObj.getOrganization());
           * misCatEmployee.setCreatedBy(perinfo.getCreatedBy()); misCatEmployee.setCreationDate(new
           * java.util.Date()); misCatEmployee.setUpdated(new java.util.Date());
           * misCatEmployee.setUpdatedBy(perinfo.getCreatedBy());
           * misCatEmployee.setEmployee(perinfo);
           * misCatEmployee.setEhcmMiscatPeriod(empMiscatPeriod);
           * OBDal.getInstance().save(misCatEmployee); }
           */
        }
      }

    } catch (

    final Exception e) {
      log4j.error("Exception in insertBusinessMissionCategory", e);
    }
  }

  public EHCMMisEmpCategory chkGradeIsPresentOrNotInMissionCategoryGrade(
      EHCMMissionCategory missionCategory, EhcmEmpPerInfo employee) {
    List<EHCMMisEmpCategory> missionCatList = null;
    EHCMMisEmpCategory missionCat = null;
    try {

      OBQuery<EHCMMisEmpCategory> missionCategoryGradeQry = OBDal.getInstance().createQuery(
          EHCMMisEmpCategory.class,
          " as e where e.client.id=:clientId and e.gradeClassifications.id=:gradeId ");
      missionCategoryGradeQry.setNamedParameter("clientId", missionCategory.getClient().getId());
      missionCategoryGradeQry.setNamedParameter("gradeId", employee.getGradeClass().getId());
      missionCategoryGradeQry.setMaxResult(1);
      missionCatList = missionCategoryGradeQry.list();
      if (missionCatList.size() > 0) {
        missionCat = missionCatList.get(0);
      } else {
        missionCat = OBProvider.getInstance().get(EHCMMisEmpCategory.class);
        missionCat.setClient(missionCategory.getClient());
        missionCat.setOrganization(missionCategory.getOrganization());
        missionCat.setCreatedBy(missionCategory.getCreatedBy());
        missionCat.setUpdatedBy(missionCategory.getUpdatedBy());
        missionCat.setEhcmMissionCategory(missionCategory);
        missionCat.setGradeClassifications(employee.getGradeClass());
        OBDal.getInstance().save(missionCat);
      }

    } catch (final Exception e) {
      log4j.error("Exception in chkGradeIsPresentOrNotInMissionCategoryGrade", e);
    }
    return missionCat;
  }
}
