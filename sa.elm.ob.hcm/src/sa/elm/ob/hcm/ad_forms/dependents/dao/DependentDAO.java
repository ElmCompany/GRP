package sa.elm.ob.hcm.ad_forms.dependents.dao;

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
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;

import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ad_forms.dependents.vo.DependentVO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.utility.Utils;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @authors gopalakrishnan, urmila
 * 
 */
public class DependentDAO {
  private Connection conn = null;
  VariablesSecureApp vars = null;
  private static Logger log4j = Logger.getLogger(DependentDAO.class);

  public DependentDAO(Connection con) {
    this.conn = con;
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
   * @return DependentList
   */
  public List<DependentVO> getDependentList(String clientId, String employeeId, DependentVO vo,
      int limit, int offset, String sortColName, String sortColType, String searchFlag,
      String lang) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<DependentVO> ls = new ArrayList<DependentVO>();
    String sqlQuery = "";
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    String date = "";
    try {
      sqlQuery = " SELECT ehcm_dependents_id as id,concat(dep.firstname,' ',dep.fathername,' ',dep.grandfather,' ',dep.fourthname,' ',dep.family) as name, "
          + " dep.age,dep.gender,dep.startdate,dep.enddate,dep.nationalidentifier as natidf, "
          + " dep.phoneno,dep.location,relationship,dep.dob,dep.ad_client_id FROM ehcm_dependents dep where ehcm_emp_perinfo_id=? ";

      if (searchFlag.equals("true")) {
        if (vo.getName() != null)
          sqlQuery += " and concat(dep.firstname,' ',dep.fathername,' ',dep.grandfather,' ',dep.fourthname,' ',dep.family) ilike '%"
              + vo.getName() + "%'";
        if (vo.getRelationship() != null && !vo.getRelationship().equals("0"))
          sqlQuery += " and relationship ='" + vo.getRelationship() + "' ";
        if (vo.getAge() != null)
          sqlQuery += " and age ='" + vo.getAge() + "'";
        if (vo.getGender() != null && !vo.getGender().equals("0"))
          sqlQuery += " and gender ='" + vo.getGender() + "' ";
        if (vo.getNatidf() != null)
          sqlQuery += " and nationalidentifier ilike '%" + vo.getNatidf() + "%'";
        if (vo.getPhoneno() != null)
          sqlQuery += " and phoneno ilike '%" + vo.getPhoneno() + "%'";
        if (vo.getLocation() != null)
          sqlQuery += " and location ilike '%" + vo.getLocation() + "%'";
        if (!StringUtils.isEmpty(vo.getStartdate()))
          sqlQuery += " and startdate " + vo.getStartdate().split("##")[0] + " to_timestamp('"
              + vo.getStartdate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";

        if (!StringUtils.isEmpty(vo.getEnddate()))
          sqlQuery += " and enddate " + vo.getEnddate().split("##")[0] + " to_timestamp('"
              + vo.getEnddate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";
      }

      if (sortColName != null && sortColName.equals("name"))
        sqlQuery += " order by name  " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("rel"))
        sqlQuery += " order by relationship " + sortColType + " limit " + limit + " offset "
            + offset;
      else if (sortColName != null && sortColName.equals("age"))
        sqlQuery += " order by age " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("gender"))
        sqlQuery += " order by gender " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("nationdalId"))
        sqlQuery += " order by natidf " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("phoneno"))
        sqlQuery += " order by phoneno " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("location"))
        sqlQuery += " order by location  " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("startdate"))
        sqlQuery += " order by startdate  " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("enddate"))
        sqlQuery += " order by enddate  " + sortColType + " limit " + limit + " offset " + offset;
      else
        sqlQuery += " order by name " + sortColType + " limit " + limit + " offset " + offset;
      log4j.debug("DAO select Query:" + sqlQuery + ">> employeeId:" + employeeId);
      st = conn.prepareStatement(sqlQuery);
      st.setString(1, employeeId);
      rs = st.executeQuery();
      while (rs.next()) {
        DependentVO depVO = new DependentVO();
        depVO.setId(Utility.nullToEmpty(rs.getString("id")));
        depVO.setName(Utility.nullToEmpty(rs.getString("name")));

        if (Utility.nullToEmpty(rs.getString("relationship")).equals("H")) {
          depVO.setRelationship(Resource.getProperty("hcm.husband", lang));
        } else if (Utility.nullToEmpty(rs.getString("relationship")).equals("S")) {
          depVO.setRelationship(Resource.getProperty("hcm.son", lang));
        } else if (Utility.nullToEmpty(rs.getString("relationship")).equals("D")) {
          depVO.setRelationship(Resource.getProperty("hcm.daughter", lang));
        } else if (Utility.nullToEmpty(rs.getString("relationship")).equals("M")) {
          depVO.setRelationship(Resource.getProperty("hcm.mother", lang));
        } else if (Utility.nullToEmpty(rs.getString("relationship")).equals("F")) {
          depVO.setRelationship(Resource.getProperty("hcm.father", lang));
        } else if (Utility.nullToEmpty(rs.getString("relationship")).equals("B")) {
          depVO.setRelationship(Resource.getProperty("hcm.brother", lang));
        } else if (Utility.nullToEmpty(rs.getString("relationship")).equals("SP")) {
          depVO.setRelationship(Resource.getProperty("hcm.spouse", lang));
        } else if (Utility.nullToEmpty(rs.getString("relationship")).equals("SI")) {
          depVO.setRelationship(Resource.getProperty("hcm.sister", lang));
        } else if (Utility.nullToEmpty(rs.getString("relationship")).equals("EC")) {
          depVO.setRelationship(Resource.getProperty("hcm.emergencyContact", lang));
        }
        if (rs.getTimestamp("dob") != null) {
          String age = UtilityDAO.CalculateAge(rs.getTimestamp("dob"),
              rs.getString("ad_client_id"));
          log4j.debug("age Calculated:" + age);
          depVO.setAge(Utility.nullToEmpty(age));
        }
        depVO.setGender(Utility.nullToEmpty(rs.getString("gender").equals("M") ? "Male"
            : (rs.getString("gender").equals("F") ? "Female" : "")));

        if (rs.getDate("startdate") != null) {
          date = df.format(rs.getDate("startdate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          depVO.setStartdate(date);
        } else
          depVO.setStartdate(null);
        if (rs.getDate("enddate") != null) {
          date = df.format(rs.getDate("enddate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          depVO.setEnddate(date);
        } else
          depVO.setEnddate(null);

        depVO.setNatidf(Utility.nullToEmpty(rs.getString("natidf")));
        depVO.setPhoneno(Utility.nullToEmpty(rs.getString("phoneno")));
        depVO.setLocation(Utility.nullToEmpty(rs.getString("location")));
        ls.add(depVO);
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
   * @return Dependent Count
   */
  public int getDependentCount(String clientId, String employeeId, String searchFlag,
      DependentVO vo) {
    PreparedStatement st = null;
    ResultSet rs = null;
    int totalRecord = 0;
    String sqlQuery = "";
    try {
      sqlQuery = "select count(ehcm_dependents_id) as totalRecord from ehcm_dependents where ehcm_emp_perinfo_id =? ";
      if (searchFlag.equals("true")) {
        if (vo.getName() != null)
          sqlQuery += " and concat(firstname,' ',fathername,' ',grandfather,' ',fourthname,' ',family) ilike '%"
              + vo.getName() + "%'";
        if (vo.getRelationship() != null && !vo.getRelationship().equals("0"))
          sqlQuery += " and relationship ='" + vo.getRelationship() + "' ";
        if (vo.getAge() != null)
          sqlQuery += " and age ='" + vo.getAge() + "'";
        if (vo.getGender() != null && !vo.getGender().equals("0"))
          sqlQuery += " and gender ='" + vo.getGender() + "' ";
        if (vo.getNatidf() != null)
          sqlQuery += " and nationalidentifier ilike '%" + vo.getNatidf() + "%'";
        if (vo.getPhoneno() != null)
          sqlQuery += " and phoneno ilike '%" + vo.getPhoneno() + "%'";
        if (vo.getLocation() != null)
          sqlQuery += " and location ilike '%" + vo.getLocation() + "%'";
        if (!StringUtils.isEmpty(vo.getStartdate()))
          sqlQuery += " and startdate " + vo.getStartdate().split("##")[0] + " to_timestamp('"
              + vo.getStartdate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";

        if (!StringUtils.isEmpty(vo.getEnddate()))
          sqlQuery += " and enddate " + vo.getEnddate().split("##")[0] + " to_timestamp('"
              + vo.getEnddate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";

      }
      log4j.debug("ajaxQuery:" + sqlQuery);
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

  /**
   * 
   * @param employeeId
   * @param dependentId
   * @return dependents value
   */

  public DependentVO getDependentEditList(String employeeId, String dependentId) {

    // TODO Auto-generated method stub
    PreparedStatement st = null;
    ResultSet rs = null;
    DependentVO dependentVO = null;
    try {
      String depqry = "SELECT dep.relationship as rel,dep.firstname,dep.fathername,dep.grandfather,dep.fourthname,dep.family,"
          + "(select eut_convert_to_hijri(to_char(dep.dob,'YYYY-MM-DD HH24:MI:SS'))) as dob,dep.gender,dep.nationalidentifier as natidf,"
          + "(select eut_convert_to_hijri(to_char(dep.startdate,'YYYY-MM-DD HH24:MI:SS'))) as startdate,"
          + "(select eut_convert_to_hijri(to_char(dep.enddate,'YYYY-MM-DD HH24:MI:SS'))) as enddate,dep.phoneno,dep.location,"
          + "dep.firstname_en, dep.fathername_en, dep.grandfather_en, dep.fourthname_en, dep.family_en FROM ehcm_dependents dep "
          + "where ehcm_emp_perinfo_id = '" + employeeId + "'";
      if (dependentId != "" || dependentId != null || dependentId != "null") {
        depqry = depqry + "and ehcm_dependents_id = '" + dependentId + "'";
      }
      st = conn.prepareStatement(depqry);

      // st.setString(0, employeeId);
      log4j.debug("Employeeid : " + employeeId);

      log4j.debug("Employee Info : " + st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        dependentVO = new DependentVO();
        dependentVO.setRelationship(Utility.nullToEmpty(rs.getString("rel")));
        dependentVO.setFirstname(Utility.nullToEmpty(rs.getString("firstname")));
        dependentVO.setFathername(Utility.nullToEmpty(rs.getString("fathername")));
        dependentVO.setGrandfather(Utility.nullToEmpty(rs.getString("grandfather")));
        dependentVO.setFourthname(Utility.nullToEmpty(rs.getString("fourthname")));
        dependentVO.setFamily(Utility.nullToEmpty(rs.getString("family")));
        dependentVO.setDob(Utility.nullToEmpty(rs.getString("dob")));
        dependentVO.setGender(Utility.nullToEmpty(rs.getString("gender")));
        dependentVO.setNatidf(Utility.nullToEmpty(rs.getString("natidf")));
        dependentVO
            .setStartdate(rs.getString("startdate") == null ? "" : rs.getString("startdate"));
        dependentVO.setEnddate(rs.getString("enddate") == null ? "" : rs.getString("enddate"));
        dependentVO.setPhoneno(Utility.nullToEmpty(rs.getString("phoneno")));
        dependentVO.setLocation(Utility.nullToEmpty(rs.getString("location")));

        dependentVO.setFirstnameEn(Utility.nullToEmpty(rs.getString("firstname_en")));
        dependentVO.setFathernameEn(Utility.nullToEmpty(rs.getString("fathername_en")));
        dependentVO.setGrandfatherEn(Utility.nullToEmpty(rs.getString("grandfather_en")));
        dependentVO.setFourthnameEn(Utility.nullToEmpty(rs.getString("fourthname_en")));
        dependentVO.setFamilyEn(Utility.nullToEmpty(rs.getString("family_en")));
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
    return dependentVO;
  }

  /**
   * 
   * @param employmentId
   * @return success
   */
  public boolean deleteDependent(String dependentId) {
    PreparedStatement st = null;
    try {

      st = conn.prepareStatement("DELETE FROM ehcm_dependents WHERE ehcm_dependents_id = ?");
      st.setString(1, dependentId);
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

  /**
   * 
   * @param employeeId
   * @return Employee Names List
   */

  public EhcmEmpPerInfo getEmployeeNames(String employeeId) {
    EhcmEmpPerInfo empnames = null;
    try {
      empnames = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
    } catch (Exception e) {
      e.printStackTrace();
      log4j.debug("Exception in getEmployeeNames :" + e);
    }
    return empnames;
  }

  public List<DependentVO> getRelationShipList(String employeeId) {
    List<DependentVO> ls = new ArrayList<DependentVO>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    DependentVO vo = null;

    try {
      ps = conn.prepareStatement(
          "select distinct relationship as rel from ehcm_dependents where ehcm_emp_perinfo_id  ='"
              + employeeId + "'");
      rs = ps.executeQuery();
      while (rs.next()) {
        vo = new DependentVO();
        if (rs.getString("rel").equals("H")) {
          vo.setRelationship("Husband");
          vo.setRelationValue("H");
        } else if (rs.getString("rel").equals("SP")) {
          vo.setRelationship("Spouse");
          vo.setRelationValue("SP");
        } else if (rs.getString("rel").equals("S")) {
          vo.setRelationship("Son");
          vo.setRelationValue("S");
        } else if (rs.getString("rel").equals("D")) {
          vo.setRelationship("Daughter");
          vo.setRelationValue("D");
        } else if (rs.getString("rel").equals("M")) {
          vo.setRelationship("Mother");
          vo.setRelationValue("M");
        } else if (rs.getString("rel").equals("F")) {
          vo.setRelationship("Father");
          vo.setRelationValue("F");
        } else if (rs.getString("rel").equals("B")) {
          vo.setRelationship("Brother");
          vo.setRelationValue("B");
        } else if (rs.getString("rel").equals("SI")) {
          vo.setRelationship("Sister");
          vo.setRelationValue("SI");
        } else if (rs.getString("rel").equals("EC")) {
          vo.setRelationship("Emergency Contact");
          vo.setRelationValue("EC");
        }
        ls.add(vo);
      }
    } catch (Exception e) {
      log4j.debug("Exception getRelationShipList :" + e);
    }
    return ls;
  }

  public DependentVO checkNationalID(String clientId, String nationalId, String dependentId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    DependentVO vo = null;
    try {
      vo = new DependentVO();
      boolean checkNID = Utils.isNINNumber(nationalId);
      st = conn.prepareStatement(
          "select count(*) as total from ehcm_dependents where ad_client_id=? and nationalidentifier= ?  and  ehcm_dependents_id <> ?");
      st.setString(1, clientId);
      st.setString(2, nationalId);
      st.setString(3, dependentId);
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

}
