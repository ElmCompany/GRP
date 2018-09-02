package sa.elm.ob.hcm.ad_forms.EmploymentGroup.dao;

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
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

import sa.elm.ob.hcm.EhcmEmploymentGroup;
import sa.elm.ob.hcm.ad_forms.EmploymentGroup.vo.EmploymentGroupVO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author gopalakrishnan on 09/02/2017
 * 
 */
public class EmploymentGroupDAO {
  private Connection conn = null;
  VariablesSecureApp vars = null;
  private static Logger log4j = Logger.getLogger(EmploymentGroupDAO.class);

  public EmploymentGroupDAO(Connection con) {
    this.conn = con;
  }

  public int getEmploymentGrpCount(String clientId, String searchFlag, EmploymentGroupVO vo) {
    PreparedStatement st = null;
    ResultSet rs = null;
    int totalRecord = 0;
    String sqlQuery = "";
    try {
      sqlQuery = " select count(*) as totalRecord from ehcm_employment_group grp";
      if (searchFlag.equals("true")) {
        if (vo.getCode() != null)
          sqlQuery += " and grp.value ilike '%" + vo.getCode() + "%'";
        if (vo.getName() != null)
          sqlQuery += " and grp.name ilike '%" + vo.getName() + "%'";

        if (!StringUtils.isEmpty(vo.getStartdate()))
          sqlQuery += " and grp.startdate " + vo.getStartdate().split("##")[0] + " to_timestamp('"
              + vo.getStartdate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";

        if (!StringUtils.isEmpty(vo.getEnddate()))
          sqlQuery += " and grp.enddate " + vo.getEnddate().split("##")[0] + " to_timestamp('"
              + vo.getEnddate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";

      }
      st = conn.prepareStatement(sqlQuery);
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

  public List<EmploymentGroupVO> getEmploymentGrpList(String clientId, EmploymentGroupVO vo,
      int limit, int offset, String sortColName, String sortColType, String searchFlag, String lang) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<EmploymentGroupVO> ls = new ArrayList<EmploymentGroupVO>();
    String sqlQuery = "";
    String date = "";
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      sqlQuery = " select grp.ehcm_employment_group_id as id,grp.value,grp.name ,grp.startdate,grp.enddate from ehcm_employment_group grp ";
      if (searchFlag.equals("true")) {
        if (vo.getCode() != null)
          sqlQuery += " and grp.value ilike '%" + vo.getCode() + "%'";
        if (vo.getName() != null)
          sqlQuery += " and grp.name ilike '%" + vo.getName() + "%'";

        if (!StringUtils.isEmpty(vo.getStartdate()))
          sqlQuery += " and grp.startdate " + vo.getStartdate().split("##")[0] + " to_timestamp('"
              + vo.getStartdate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";

        if (!StringUtils.isEmpty(vo.getEnddate()))
          sqlQuery += " and grp.enddate " + vo.getEnddate().split("##")[0] + " to_timestamp('"
              + vo.getEnddate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";

      }

      if (sortColName != null && sortColName.equals("Code"))
        sqlQuery += " order by grp.value  " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("Name"))
        sqlQuery += " order by grp.name " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("StartDate"))
        sqlQuery += " order by grp.startdate  " + sortColType + " limit " + limit + " offset "
            + offset;
      else if (sortColName != null && sortColName.equals("enddate"))
        sqlQuery += " order by enddate  " + sortColType + " limit " + limit + " offset " + offset;
      else
        sqlQuery += " order by grp.value " + sortColType + " limit " + limit + " offset " + offset;
      st = conn.prepareStatement(sqlQuery);
      rs = st.executeQuery();
      while (rs.next()) {
        EmploymentGroupVO VO = new EmploymentGroupVO();
        VO.setGrpId(Utility.nullToEmpty(rs.getString("id")));
        VO.setCode(Utility.nullToEmpty(rs.getString("code")));
        if (rs.getDate("startdate") != null) {
          date = df.format(rs.getDate("startdate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          VO.setStartdate(date);
        } else
          VO.setStartdate(null);
        if (rs.getDate("enddate") != null && !StringUtils.isEmpty(rs.getDate("enddate").toString())) {
          date = df.format(rs.getDate("enddate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          VO.setEnddate(date);
        } else
          VO.setEnddate("");

        ls.add(VO);
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

  public String addEmploymentGroup(String clientId, String userId, EmploymentGroupVO vo) {
    String employmentGrpId = "";
    try {
      OBContext.setAdminMode();
      EhcmEmploymentGroup employmentGrp = OBProvider.getInstance().get(EhcmEmploymentGroup.class);
      employmentGrp.setCode(vo.getCode());
      employmentGrp.setName(vo.getName());
      if (vo.getStartdate() != null && !vo.getStartdate().equals("")) {
        employmentGrp.setStartDate(convertGregorian(vo.getStartdate()));
      }
      if (vo.getEnddate() != null && !vo.getEnddate().equals("")) {
        employmentGrp.setEndDate(convertGregorian(vo.getEnddate()));
      }
      OBDal.getInstance().save(employmentGrp);
      OBDal.getInstance().flush();
      employmentGrpId = employmentGrp.getId();
    } catch (Exception e) {
      log4j.error("error while addEmploymentGroup", e);
      return null;
      // TODO: handle exception
    } finally {
      OBContext.restorePreviousMode();
    }
    return employmentGrpId;
  }

  public String updateEmploymentGrp(String clientId, String userId, EmploymentGroupVO vo,
      String EmploymentGrpid) {
    String employmentGrpid = "";
    try {
      OBContext.setAdminMode();
      EhcmEmploymentGroup employmentGrp = OBDal.getInstance().get(EhcmEmploymentGroup.class,
          EmploymentGrpid);
      employmentGrp.setCode(vo.getCode());
      employmentGrp.setName(vo.getName());
      if (vo.getStartdate() != null && !vo.getStartdate().equals("")) {
        employmentGrp.setStartDate(convertGregorian(vo.getStartdate()));
      }
      if (vo.getEnddate() != null && !vo.getEnddate().equals("")) {
        employmentGrp.setEndDate(convertGregorian(vo.getEnddate()));
      }
      OBDal.getInstance().save(employmentGrp);
      OBDal.getInstance().flush();
      employmentGrpid = employmentGrp.getId();
    } catch (Exception e) {
      log4j.error("error while updateEmploymentGrp", e);
      return null;
      // TODO: handle exception
    } finally {
      OBContext.restorePreviousMode();
    }
    return employmentGrpid;
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
      log4j.error("Exception in convertGregorian", e);

    }
    return greDate;

  }
}

// /////

