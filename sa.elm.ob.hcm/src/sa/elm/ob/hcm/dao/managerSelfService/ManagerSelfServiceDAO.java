package sa.elm.ob.hcm.dao.managerSelfService;

import java.util.List;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMEmpSupervisorNode;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;

public interface ManagerSelfServiceDAO {
  /**
   * Get all employee details by using their manager name
   * 
   * @param username
   * @return
   */
  List<EHCMEmpSupervisorNode> getAllEmployeeByManager(String username) throws BusinessException;

  /**
   * Get Employee Information By Using Number
   * 
   * @param empNo
   * @return
   */
  EhcmEmpPerInfo getEmployeeInformationByNumber(String empNo);

  /**
   * find Active Employment Information for Employee
   * 
   * @param employee
   * @return
   */
  EmploymentInfo findActiveEmploymentInfo(EhcmEmpPerInfo employee);

  /**
   * find leave details by original decision number
   * 
   * @param originalDecNo
   * @return
   */
  EHCMAbsenceAttendance findLeaveDetailsByDecNo(String originalDecNo);

  /**
   * find Business Mission Details By usng employee
   * 
   * @param empName
   * @return
   */
  List<EHCMEmpBusinessMission> findBusinessMissionByEmployee(String employeeNumber);

  /**
   * validate manager with user
   * 
   * @param mngUsername
   * @param empNo
   * @return
   */
  Boolean checkManagerWithEmployee(String mngUsername, String empNo);

}
