package sa.elm.ob.hcm.dao.loginAttendance;

import java.sql.Timestamp;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EHCMLoginAttendance;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.dto.loginAttendance.LoginAttendanceDTO;
import sa.elm.ob.utility.util.DateUtils;
import sa.elm.ob.utility.util.Utility;

@Repository
public class LoginAttendanceDAOImpl implements LoginAttendanceDAO {
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(LoginAttendanceDAOImpl.class);
  private static final String OPEN_BRAVO_GREG_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
  @Autowired
  EmployeeProfileDAO employeeProfileDAO;

  @Override
  public List<EHCMLoginAttendance> getAttendanceDetails(EhcmEmpPerInfo employeeOB) {
    // TODO Auto-generated method stub
    try {
      OBContext.setAdminMode();
      if (employeeOB.getEHCMLoginAttendanceList().size() > 0) {
        return employeeOB.getEHCMLoginAttendanceList();
      } else
        return null;
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Error while fetching absence details for employee :" + employeeOB.getName());
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public EHCMLoginAttendance submitAttendanceForEmployee(LoginAttendanceDTO loginAttendanceDTO) {
    // TODO Auto-generated method stub
    try {
      OBContext.setAdminMode();
      // find employee details
      EhcmEmpPerInfo employeeOB = employeeProfileDAO
          .getEmployeeProfileByUser(loginAttendanceDTO.getUserName());
      // find user
      User userOB = employeeProfileDAO.getUserDetailsByUserName(employeeOB.getSearchKey());
      Timestamp inTimeTs = new Timestamp((DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
          Utility.convertToGregorian(loginAttendanceDTO.getInTime()))).getTime());
      Timestamp outTimeTs = new Timestamp(
          (DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
              Utility.convertToGregorian(loginAttendanceDTO.getOutTime()))).getTime());
      EHCMLoginAttendance loginAttendanceOB = OBProvider.getInstance()
          .get(EHCMLoginAttendance.class);
      loginAttendanceOB.setOrganization(employeeOB.getOrganization());
      loginAttendanceOB.setClient(employeeOB.getClient());
      loginAttendanceOB.setCreationDate(new java.util.Date());
      loginAttendanceOB.setCreatedBy(userOB);
      loginAttendanceOB.setUpdated(new java.util.Date());
      loginAttendanceOB.setUpdatedBy(userOB);
      loginAttendanceOB.setDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
          Utility.convertToGregorian(loginAttendanceDTO.getDate())));
      loginAttendanceOB.setIntime(inTimeTs);
      loginAttendanceOB.setEmployee(employeeOB);
      loginAttendanceOB.setOuttime(outTimeTs);
      OBDal.getInstance().save(loginAttendanceOB);
      OBDal.getInstance().flush();
      return loginAttendanceOB;
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(
          "Exception While Submitting Attendance For Employee:" + loginAttendanceDTO.getUserName());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
