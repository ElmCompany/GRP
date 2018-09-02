package sa.elm.ob.hcm.services.loginAttendance;

import java.util.List;

import sa.elm.ob.hcm.dto.loginAttendance.LoginAttendanceDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

/**
 * Login Attendance Service Interface
 * 
 *
 */

public interface LoginAttendanceService {
  /**
   * View Login Attendance details by using username
   * 
   * @param payrollDefinition
   * @param payrollId
   * @return
   */
  List<LoginAttendanceDTO> viewLoginAttendance(String userName)
      throws SystemException, BusinessException;

  /**
   * Submit LoginAttendance for user
   * 
   * @param loginAttendanceDTO
   * @return
   */
  Boolean submitLoginAttendance(LoginAttendanceDTO loginAttendanceDTO)
      throws BusinessException, SystemException;
}
