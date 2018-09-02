package sa.elm.ob.hcm.ad_reports.positionTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

/**
 * 
 * @author poongodi on 30/03/2018
 *
 */

public class PositionTransactionsDetailsDAO {
  private Connection connection = null;
  private static final Logger log4j = Logger.getLogger(PositionTransactionsDetailsDAO.class);

  public PositionTransactionsDetailsDAO() {
    connection = getDbConnection();
  }

  /**
   * Get the database connection
   * 
   * @return
   */
  private Connection getDbConnection() {
    return OBDal.getInstance().getConnection();
  }

  /**
   * 
   * @param inpClientId
   * @return
   */
  public static List<PositionTransactionsDetailsVO> getGradeCode(String inpClientId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    PositionTransactionsDetailsVO vo = null;
    List<PositionTransactionsDetailsVO> gradels = new ArrayList<PositionTransactionsDetailsVO>();
    try {
      OBContext.setAdminMode();
      st = OBDal.getInstance().getConnection().prepareStatement(
          "select ehcm_grade_id,(value || '-' || name) as value from ehcm_grade where isactive = 'Y' and ad_client_id = ? order by value");
      st.setString(1, inpClientId);
      rs = st.executeQuery();
      while (rs.next()) {
        vo = new PositionTransactionsDetailsVO();
        vo.setGradeId(rs.getString("ehcm_grade_id"));
        vo.setGradeName(rs.getString("value"));
        gradels.add(vo);
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getGradeCode " + e.getMessage());
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
    return gradels;
  }

  /**
   * 
   * @param inpClientId
   * @return
   */
  public static List<PositionTransactionsDetailsVO> getJoBNo(String inpClientId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    PositionTransactionsDetailsVO vo = null;
    List<PositionTransactionsDetailsVO> jobls = new ArrayList<PositionTransactionsDetailsVO>();
    try {
      OBContext.setAdminMode();
      st = OBDal.getInstance().getConnection()
          .prepareStatement("select ehcm_position_id,job_no from ehcm_position where isactive = 'Y'"
              + "and ad_client_id = ? and ehcm_cancelposition_id is  null order by job_no");
      st.setString(1, inpClientId);
      rs = st.executeQuery();
      while (rs.next()) {
        vo = new PositionTransactionsDetailsVO();
        vo.setPositionId(rs.getString("ehcm_position_id"));
        vo.setJobNo(rs.getString("job_no"));
        jobls.add(vo);
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getJoBNo " + e.getMessage());
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
    return jobls;
  }

  /**
   * 
   * @param inpClientId
   * @return
   */

  public static List<PositionTransactionsDetailsVO> getDepartmentCode(String inpClientId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    PositionTransactionsDetailsVO vo = null;
    List<PositionTransactionsDetailsVO> depls = new ArrayList<PositionTransactionsDetailsVO>();
    try {
      OBContext.setAdminMode();
      st = OBDal.getInstance().getConnection().prepareStatement(
          "select ad_org_id,concat(value,'-' ,name) as name from ad_org where ad_org_id in (select ad_org_id from Ehcm_Hrorg_Classfication where Ehcm_Org_Classfication_ID in (select ehcm_org_classfication_id from ehcm_org_classfication where classification = 'HR' and isactive ='Y') "
              + "and isactive ='Y' and ad_client_id =?)");
      st.setString(1, inpClientId);
      rs = st.executeQuery();
      while (rs.next()) {
        vo = new PositionTransactionsDetailsVO();
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

  public static List<PositionTransactionsDetailsVO> getJobNoBasedOnGrade(String inpGradeId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    PositionTransactionsDetailsVO vo = null;
    List<PositionTransactionsDetailsVO> groupList = new ArrayList<PositionTransactionsDetailsVO>();
    try {
      OBContext.setAdminMode();
      st = OBDal.getInstance().getConnection().prepareStatement(
          "select ehcm_position_id,job_no from ehcm_position where ehcm_grade_id = ?  order by job_no");
      st.setString(1, inpGradeId);
      rs = st.executeQuery();
      log4j.debug(st.toString());
      while (rs.next()) {
        vo = new PositionTransactionsDetailsVO();
        vo.setPositionId(rs.getString("ehcm_position_id"));
        vo.setJobNo(rs.getString("job_no"));
        groupList.add(vo);
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getJobNoBasedOnGrade " + e.getMessage());
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
    return groupList;
  }

  public static List<PositionTransactionsDetailsVO> getChildDepartment(String inpClientId,
      String depId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    PositionTransactionsDetailsVO vo = null;
    List<PositionTransactionsDetailsVO> depls = new ArrayList<PositionTransactionsDetailsVO>();
    boolean chkFlag = false;
    try {
      OBContext.setAdminMode();
      if (!depId.equals("0")) {
        st = OBDal.getInstance().getConnection().prepareStatement(
            "select ad_org_id from ad_org where ad_org_id in (select ad_org_id from Ehcm_Hrorg_Classfication where Ehcm_Org_Classfication_ID in (select ehcm_org_classfication_id from ehcm_org_classfication where classification = 'HR' and isactive ='Y') "
                + "and isactive ='Y' and ad_client_id =?) and ad_org_id in (select unnest(ehcm_get_all_hrchildren_array('"
                + depId + "'):: character varying [])) ");
        st.setString(1, inpClientId);
        rs = st.executeQuery();
        while (rs.next()) {
          chkFlag = true;
          vo = new PositionTransactionsDetailsVO();
          vo.setOrgId(rs.getString("ad_org_id"));
          depls.add(vo);
        }
        if (!chkFlag || chkFlag) {
          vo = new PositionTransactionsDetailsVO();
          vo.setOrgId(depId);
          depls.add(vo);
        }
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getChildDepartment " + e.getMessage());
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
}
