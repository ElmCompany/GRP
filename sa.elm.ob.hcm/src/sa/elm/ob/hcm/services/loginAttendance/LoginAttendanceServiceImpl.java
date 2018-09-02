package sa.elm.ob.hcm.services.loginAttendance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sa.elm.ob.hcm.EHCMLoginAttendance;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.dao.loginAttendance.LoginAttendanceDAO;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.dto.loginAttendance.LoginAttendanceDTO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.util.MessageKeys;
import sa.elm.ob.utility.util.DateUtils;

/**
 * Leave Management Service Implementation
 *
 */
@Service("loginAttendanceService")
public class LoginAttendanceServiceImpl implements LoginAttendanceService {
  private static final String OPEN_BRAVO_DATE_FORMAT = "dd-MM-yyyy";
  private static final String OPEN_BRAVO_GREG_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
  @Autowired
  private EmployeeProfileDAO employeeProfileDAO;
  @Autowired
  private LoginAttendanceDAO loginAttendanceDAO;

  @Override
  public List<LoginAttendanceDTO> viewLoginAttendance(String userName)
      throws SystemException, BusinessException {
    // TODO Auto-generated method stub
    final String userLang = SecurityUtils.getUserLanguage();
    // find employee details
    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(userName);
    if (employeeOB == null)
      throw new BusinessException(
          Resource.getProperty(MessageKeys.ABSENCETYPE_NOT_AVAILABLE, userLang));
    List<EHCMLoginAttendance> attendanceList = loginAttendanceDAO.getAttendanceDetails(employeeOB);
    if (attendanceList.size() > 0)
      return mapAttendanceDetails(attendanceList);
    else
      return null;
  }

  /**
   * Convert Attendance Details Domain to DTO
   * 
   * @param attendanceList
   * @return
   */
  private List<LoginAttendanceDTO> mapAttendanceDetails(List<EHCMLoginAttendance> attendanceList) {
    // TODO Auto-generated method stub
    List<LoginAttendanceDTO> loginAttendanceList = new ArrayList<LoginAttendanceDTO>();
    LoginAttendanceDTO loginAttendanceDTO = null;
    for (EHCMLoginAttendance loginAttandance : attendanceList) {
      loginAttendanceDTO = new LoginAttendanceDTO();
      if (null != loginAttandance.getDate()) {
        loginAttendanceDTO.setDate(
            DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, loginAttandance.getDate()));
      }
      if (null != loginAttandance.getIntime()) {
        loginAttendanceDTO.setInTime(
            DateUtils.convertDateToString(OPEN_BRAVO_GREG_DATE_FORMAT, loginAttandance.getDate()));
      }
      if (null != loginAttandance.getOuttime()) {
        loginAttendanceDTO.setOutTime(
            DateUtils.convertDateToString(OPEN_BRAVO_GREG_DATE_FORMAT, loginAttandance.getDate()));
      }
      loginAttendanceDTO.setStatus(loginAttandance.getStatus());
      loginAttendanceDTO.setUserName(loginAttandance.getEmployee().getName());
      loginAttendanceList.add(loginAttendanceDTO);
    }
    return loginAttendanceList;
  }

  @Override
  public Boolean submitLoginAttendance(LoginAttendanceDTO loginAttendanceDTO)
      throws BusinessException, SystemException {
    // TODO Auto-generated method stub
    final String userLang = SecurityUtils.getUserLanguage();
    EHCMLoginAttendance loginAttendance = loginAttendanceDAO
        .submitAttendanceForEmployee(loginAttendanceDTO);
    if (loginAttendance == null)
      throw new BusinessException(
          Resource.getProperty(MessageKeys.ATTENDANCE_NOT_CREATED, userLang));

    return true;
  }

}
