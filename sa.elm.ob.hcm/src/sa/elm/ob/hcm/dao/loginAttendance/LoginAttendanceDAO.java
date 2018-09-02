package sa.elm.ob.hcm.dao.loginAttendance;

import java.util.List;

import sa.elm.ob.hcm.EHCMLoginAttendance;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.dto.loginAttendance.LoginAttendanceDTO;

public interface LoginAttendanceDAO {
  /**
   * Get Attendance Details By Using Employee Details
   * 
   * @param employeeOB
   * @return
   */
  List<EHCMLoginAttendance> getAttendanceDetails(EhcmEmpPerInfo employeeOB);

  /**
   * Submit Attendance for Employee
   * 
   * @param loginAttendanceDTO
   * @return
   */
  EHCMLoginAttendance submitAttendanceForEmployee(LoginAttendanceDTO loginAttendanceDTO);

}
