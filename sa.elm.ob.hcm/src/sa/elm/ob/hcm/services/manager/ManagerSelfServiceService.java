package sa.elm.ob.hcm.services.manager;

import java.text.ParseException;
import java.util.List;

import sa.elm.ob.hcm.dto.employment.ViewEmplInfoDTO;
import sa.elm.ob.hcm.dto.leave.ViewLeaveAccrualDTO;
import sa.elm.ob.hcm.dto.leave.ViewLeaveDTO;
import sa.elm.ob.hcm.dto.manager.EmpBusinessMissionDTO;
import sa.elm.ob.hcm.dto.manager.EmpBusinessMissionManagerDTO;
import sa.elm.ob.hcm.dto.manager.EmpInfoManagerDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

/**
 * Manager Self Service Interface
 *
 */
public interface ManagerSelfServiceService {

  /**
   * @param username
   * @return
   */
  List<EmpInfoManagerDTO> getAllEmployeesByManager(String username)
      throws BusinessException, SystemException;

  /**
   * Get Employee Information by Using Employee Number
   * 
   * @param empNo
   * @return
   */
  ViewEmplInfoDTO getEmployeeInformationByNumber(String empNo);

  /**
   * @param username
   * @return
   */
  List<ViewLeaveAccrualDTO> getAllEmployeesLeaveAccuralByManagerUsername(String username)
      throws BusinessException, SystemException;

  /**
   * @param mngUsername
   * @param asOfDate
   * @param empNo
   * @return
   */
  /*
   * asOfDate (format: yyyy-MM-dd)
   */
  List<ViewLeaveAccrualDTO> getEmployeeLeavesAccuralByDateAndEmpNo(String mngUsername,
      String asOfDate, String empNo) throws BusinessException, SystemException;

  /**
   * @param originalDecNo
   * @return
   */
  ViewLeaveDTO getEmployeeLeaveByOriginalDecNo(String originalDecNo);

  /**
   * @param username
   * @return
   */
  List<EmpBusinessMissionManagerDTO> getAllEmployeesBusinessMissionByManagerUsername(
      String username) throws BusinessException, SystemException;

  /**
   * @param mngUsername
   * @param asOfDate
   * @param empNo
   * @return
   */
  List<EmpBusinessMissionManagerDTO> getEmployeeBusinessMissionsByDateAndEmpNo(String mngUsername,
      String asOfDate, String empNo) throws BusinessException, SystemException, ParseException;

  /**
   * @param originalDecNo
   * @return
   */
  EmpBusinessMissionDTO getEmployeeBusinessMissionByOriginalDecNo(String originalDecNo);

  /**
   * find details of business mission by manager and date
   * 
   * @param mngUsername
   * @param asOfDate
   * @param empNo
   * @return
   */
  /*
   * asOfDate (format: yyyy-MM-dd)
   */
  List<EmpBusinessMissionManagerDTO> getEmployeeBusinessMissionsByManagerAndDate(String mngUsername,
      String asOfDate) throws BusinessException, SystemException, ParseException;

}
