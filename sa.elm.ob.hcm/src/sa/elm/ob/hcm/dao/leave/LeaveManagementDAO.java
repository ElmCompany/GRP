package sa.elm.ob.hcm.dao.leave;

import java.util.List;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMAbsenceReason;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.dto.leave.LeaveRequestDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;

public interface LeaveManagementDAO {
  /**
   * Get the absence type by absence type value
   * 
   * @param absenceType
   *          value
   * @return Absence type
   */

  EHCMAbsenceType findAbsenceType(String absenceType);

  /**
   * Get the absence reason by absence reason
   * 
   * @param absenceReason
   * @return
   */
  EHCMAbsenceReason findAbsenceReason(String absenceReason);

  /**
   * verify the available leave for particular employee by leave type
   * 
   * @param absenceTypeOB
   * @param objEmployeeeOB
   * @param leaveRequestDTO
   * @param strIssueDecisionType
   * @return
   */
  String verifyAvailableLeave(EHCMAbsenceType absenceTypeOB, EhcmEmpPerInfo objEmployeeeOB,
      LeaveRequestDTO leaveRequestDTO, String strIssueDecisionType);

  /**
   * Create the absence attendance record for employee
   * 
   * @param employeeOB
   * @param leaveRequestDTO
   * @return
   */
  EHCMAbsenceAttendance createAbsenceAttendanceForEmployee(EhcmEmpPerInfo employeeOB,
      LeaveRequestDTO leaveRequestDTO, String decisionType, EHCMAbsenceType absenceTypeOB);

  /**
   * 
   * @param absenceAttendance
   * @return Absence Type Accural
   */
  EHCMAbsenceTypeAccruals getAccural(EHCMAbsenceAttendance absenceAttendance)
      throws BusinessException, Exception;

  /**
   * Create leave block for employee based his decision
   * 
   * @param absenceAttendanceOB
   * @param absenceTypeOB
   * @param absenceAccuralOB
   */
  void createLeave(EHCMAbsenceAttendance absenceAttendanceOB, EHCMAbsenceType absenceTypeOB,
      EHCMAbsenceTypeAccruals absenceAccuralOB) throws BusinessException;

  /**
   * find original leave request decision numbers
   * 
   * @param username
   * @return list of decision numbers
   */
  List<String> getAllOriginalDecisionNoByUserName(String username);

  /**
   * find details of leave request based on decision number
   * 
   * @param orginalDecNo
   * @param User
   *          Name
   * @return
   */
  List<EHCMAbsenceAttendance> getLeaveRequestByDecisionNoOrByUsername(String orginalDecNo,
      String userName);

  /**
   * find details of Absence Decision(Absence Attendance) based on record id
   * 
   * @param attendanceId
   * @return
   */
  EHCMAbsenceAttendance getAbsenceAttendanceById(String attendanceId);

  String getLeaveRequestDaysValidation(EHCMAbsenceAttendance absenceAttendance,
      String approvalMessage);

}
