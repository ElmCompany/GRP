package sa.elm.ob.hcm.ad_reports.VacantOccupiedPosition;

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
 * @author poongodi on 27/03/2018
 *
 */

public class VacantOccupiedPositionDAO {
  private Connection connection = null;
  private static final Logger log4j = Logger.getLogger(VacantOccupiedPositionDAO.class);

  public VacantOccupiedPositionDAO() {
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
  public static List<VacantOccupiedPositionVO> getGradeCode(String inpClientId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    VacantOccupiedPositionVO vo = null;
    List<VacantOccupiedPositionVO> gradels = new ArrayList<VacantOccupiedPositionVO>();
    try {
      OBContext.setAdminMode();
      st = OBDal.getInstance().getConnection().prepareStatement(
          "select ehcm_grade_id,(value || '-' || name) as value from ehcm_grade where isactive = 'Y' and ad_client_id = ? order by value");
      st.setString(1, inpClientId);
      rs = st.executeQuery();
      while (rs.next()) {
        vo = new VacantOccupiedPositionVO();
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

  public static List<VacantOccupiedPositionVO> getDepartmentCode(String inpClientId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    VacantOccupiedPositionVO vo = null;
    List<VacantOccupiedPositionVO> depls = new ArrayList<VacantOccupiedPositionVO>();
    try {
      OBContext.setAdminMode();
      st = OBDal.getInstance().getConnection().prepareStatement(
          "select ad_org_id,name from ad_org where ad_org_id in (select ad_org_id from Ehcm_Hrorg_Classfication where Ehcm_Org_Classfication_ID in (select ehcm_org_classfication_id from ehcm_org_classfication where classification = 'HR' and isactive ='Y') "
              + "and isactive ='Y' and ad_client_id =?)");
      st.setString(1, inpClientId);
      rs = st.executeQuery();
      while (rs.next()) {
        vo = new VacantOccupiedPositionVO();
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
}
