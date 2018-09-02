package sa.elm.ob.hcm.dao.leave;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EHCMAbsenceAccrual;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.util.MessageKeys;

@Repository
public class ViewLeaveDAOimpl implements ViewLeaveDAO {
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ViewLeaveDAOimpl.class);
  @Autowired
  private EmployeeProfileDAO employeeProfileDAO;

  @Override
  public EhcmEmpPerInfo getEmployeeProfileByUser(String username) {
    EhcmEmpPerInfo ehcmEmpPerInfo = null;
    try {
      OBContext.setAdminMode();
      ehcmEmpPerInfo = employeeProfileDAO.getEmployeeProfileByUser(username);
    } catch (OBException e) {
      log.error("Error while fetching employee personal data-->emp user name -->" + username);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return ehcmEmpPerInfo;
  }

  @Override
  public EHCMAbsenceAccrual getAbsenceAccural(String username)
      throws BusinessException, SystemException {
    EHCMAbsenceAccrual absenceAccural = null;
    final String userLang = SecurityUtils.getUserLanguage();
    EhcmEmpPerInfo ehcmEmpPerInfo = employeeProfileDAO.getEmployeeProfileByUser(username);
    if (ehcmEmpPerInfo.getEHCMAbsenceAccrualList().size() == 0) {
      throw new BusinessException(
          Resource.getProperty(MessageKeys.ACCURAL_NOT_AVAILABLE, userLang));
    } else {
      absenceAccural = ehcmEmpPerInfo.getEHCMAbsenceAccrualList().get(0);
    }
    // TODO Auto-generated method stub
    return absenceAccural;
  }

  @Override
  public List<EHCMAbsenceType> getAbsenceType(String clientId) {
    // TODO Auto-generated method stub
    OBQuery<EHCMAbsenceType> objAbsenceQry = null;
    List<EHCMAbsenceType> objAbsenceTypeList = new ArrayList<EHCMAbsenceType>();
    try {
      OBContext.setAdminMode(true);
      final String query = " as e where e.client.id=:client  ";
      objAbsenceQry = OBDal.getInstance().createQuery(EHCMAbsenceType.class, query);
      objAbsenceQry.setNamedParameter("client", clientId);
      objAbsenceQry.setFilterOnReadableClients(false);
      objAbsenceQry.setFilterOnReadableOrganization(false);
      objAbsenceTypeList = objAbsenceQry.list();
      if (objAbsenceTypeList.size() > 0) {
        return objAbsenceTypeList;
      } else {
        return null;
      }
    } catch (OBException e) {
      log.error("Error while fetching absence type->Client Id-->" + clientId);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public JSONObject getAvailedAndAvailableDays(EHCMAbsenceType absenceType,
      EhcmEmpPerInfo objEmpInfo, String asOfDate) {
    BigDecimal days = BigDecimal.ZERO;
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject result = new JSONObject();
    try {

      st = OBDal.getInstance().getConnection().prepareStatement(
          " select * from ehcm_getavailed_availablelev(?, ?, ?, ?, ?,?,?,?,?,?) ");
      st.setString(1, objEmpInfo.getId());
      st.setString(2, asOfDate);
      st.setString(3, null);
      st.setString(4, absenceType.getId());
      st.setString(5, objEmpInfo.getId());
      st.setInt(6, 1);
      st.setString(7, "CR");
      st.setString(8, null);
      st.setBoolean(9, true);
      st.setString(10, "");// subtype need to add
      log.debug("st" + st.toString());

      rs = st.executeQuery();
      if (rs.next()) {
        result.put("availeddays", rs.getBigDecimal("p_availedleavedays"));
        result.put("availabledays", rs.getBigDecimal("p_availableleavedays"));
        // days = rs.getBigDecimal("ehcm_checkavailableleave");
      }
      log.debug("days" + days);
    } catch (final Exception e) {
      e.printStackTrace();
      log.error("Exception in getavailableandavaileddays() Method : ", e);
    }
    return result;
  }

}
