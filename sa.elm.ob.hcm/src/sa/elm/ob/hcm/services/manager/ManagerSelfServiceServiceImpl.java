package sa.elm.ob.hcm.services.manager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.axis.utils.StringUtils;
import org.openbravo.dal.core.OBContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMEmpSupervisorNode;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.dao.managerSelfService.ManagerSelfServiceDAO;
import sa.elm.ob.hcm.dto.employment.ViewEmplInfoDTO;
import sa.elm.ob.hcm.dto.leave.ViewLeaveAccrualDTO;
import sa.elm.ob.hcm.dto.leave.ViewLeaveDTO;
import sa.elm.ob.hcm.dto.manager.EmpBusinessMissionDTO;
import sa.elm.ob.hcm.dto.manager.EmpBusinessMissionManagerDTO;
import sa.elm.ob.hcm.dto.manager.EmpInfoManagerDTO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.services.leave.LeaveManagementService;
import sa.elm.ob.hcm.util.MessageKeys;
import sa.elm.ob.utility.util.DateUtils;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

@Service
public class ManagerSelfServiceServiceImpl implements ManagerSelfServiceService {
  private static final String OPEN_BRAVO_DATE_FORMAT = "dd-MM-yyyy";
  private static final String OPEN_BRAVO_YEARDATE_FORMAT = "yyyy-MM-dd";
  @Autowired
  ManagerSelfServiceDAO managerSelfServiceDAO;
  @Autowired
  LeaveManagementService leaveManagementService;

  @Override
  public List<EmpInfoManagerDTO> getAllEmployeesByManager(String username)
      throws BusinessException, SystemException {

    List<EHCMEmpSupervisorNode> employeeList = managerSelfServiceDAO
        .getAllEmployeeByManager(username);
    List<EmpInfoManagerDTO> empInforManagerDTO = mapEmployeesDetails(employeeList);
    return empInforManagerDTO;
  }

  /**
   * fill employee details to DTO
   * 
   * @param employeeList
   * @return
   */
  private List<EmpInfoManagerDTO> mapEmployeesDetails(List<EHCMEmpSupervisorNode> employeeList) {

    List<EmpInfoManagerDTO> employeeInfoList = new ArrayList<EmpInfoManagerDTO>();
    EmpInfoManagerDTO employeeInfoDTO = null;
    for (EHCMEmpSupervisorNode employeeOB : employeeList) {
      employeeInfoDTO = new EmpInfoManagerDTO();
      EhcmEmpPerInfo employee = employeeOB.getEhcmEmpPerinfo();
      // find active employment info
      EmploymentInfo employementInfo = findActiveEmployement(employee);
      employeeInfoDTO.setEmpNo(employee.getSearchKey());
      employeeInfoDTO.setEmpName(employee.getArabicfullname());
      employeeInfoDTO.setGrade(employementInfo.getEmploymentgrade().getSearchKey());
      employeeInfoDTO.setHireDate(UtilityDAO.convertTohijriDate(
          DateUtils.convertDateToString(OPEN_BRAVO_YEARDATE_FORMAT, employee.getHiredate())));
      // TODO Monthly or Annually Salary?
      // employeeInfoDTO.setNetSalary(employee.getnet);
      employeeInfoDTO.setStatus(employee.getStatus());
      employeeInfoList.add(employeeInfoDTO);
    }
    return employeeInfoList;
  }

  private EmploymentInfo findActiveEmployement(EhcmEmpPerInfo employee) {
    // TODO Auto-generated method stub
    EmploymentInfo employementInfo = managerSelfServiceDAO.findActiveEmploymentInfo(employee);
    return employementInfo;
  }

  @Override
  public ViewEmplInfoDTO getEmployeeInformationByNumber(String empNo) {

    EhcmEmpPerInfo employeeOB = managerSelfServiceDAO.getEmployeeInformationByNumber(empNo);

    return mapEmployeeInfoDetail(employeeOB);
  }

  /**
   * Fill Employee Details in View Employee DTO
   * 
   * @param employeeOB
   * @return
   */
  private ViewEmplInfoDTO mapEmployeeInfoDetail(EhcmEmpPerInfo employeeOB) {

    ViewEmplInfoDTO viewInfoDTO = new ViewEmplInfoDTO();

    // TODO from where to get Reason? ask Nassar
    viewInfoDTO.setReason(null);
    if (employeeOB.getStartDate() != null)
      viewInfoDTO.setStartDate(UtilityDAO.convertTohijriDate(
          DateUtils.convertDateToString(OPEN_BRAVO_YEARDATE_FORMAT, employeeOB.getStartDate())));
    if (employeeOB.getEndDate() != null)
      viewInfoDTO.setEndDate(UtilityDAO.convertTohijriDate(
          DateUtils.convertDateToString(OPEN_BRAVO_YEARDATE_FORMAT, employeeOB.getEndDate())));
    viewInfoDTO.setDecisionNo(employeeOB.getDecisionno());
    viewInfoDTO.setDepartment(employeeOB.getDeptCode());
    viewInfoDTO.setGrade(findActiveEmployement(employeeOB).getEmploymentgrade().getSearchKey());
    viewInfoDTO.setJobNo(findActiveEmployement(employeeOB).getPosition().getJOBNo());
    viewInfoDTO.setStep(
        findActiveEmployement(employeeOB).getEhcmPayscaleline().getEhcmProgressionpt().getPoint());
    viewInfoDTO.setJobTitle(findActiveEmployement(employeeOB).getJobtitle());
    return viewInfoDTO;
  }

  @Override
  public List<ViewLeaveAccrualDTO> getAllEmployeesLeaveAccuralByManagerUsername(String username)
      throws BusinessException, SystemException {
    final String userLang = SecurityUtils.getUserLanguage();
    List<ViewLeaveAccrualDTO> viewAllLeaveAccuralList = new ArrayList<ViewLeaveAccrualDTO>();
    List<ViewLeaveAccrualDTO> viewLeaveAccuralList = new ArrayList<ViewLeaveAccrualDTO>();
    try {
      OBContext.setAdminMode();
      List<EmpInfoManagerDTO> employeeInfoList = getAllEmployeesByManager(username);
      for (EmpInfoManagerDTO employeeInfo : employeeInfoList) {
        viewLeaveAccuralList = leaveManagementService.viewLeavesAccrual(employeeInfo.getEmpNo(),
            DateUtils.convertDateToString(OPEN_BRAVO_YEARDATE_FORMAT, new Date()));
        viewAllLeaveAccuralList.addAll(viewLeaveAccuralList);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }
    return viewAllLeaveAccuralList;

  }

  @Override
  public List<ViewLeaveAccrualDTO> getEmployeeLeavesAccuralByDateAndEmpNo(String mngUsername,
      String asOfDate, String empNo) throws BusinessException, SystemException {
    final String userLang = SecurityUtils.getUserLanguage();
    List<ViewLeaveAccrualDTO> viewAllLeaveAccuralList = new ArrayList<ViewLeaveAccrualDTO>();
    List<ViewLeaveAccrualDTO> viewLeaveAccuralList = new ArrayList<ViewLeaveAccrualDTO>();
    if (StringUtils.isEmpty(empNo)) {
      List<EmpInfoManagerDTO> employeeInfoList = getAllEmployeesByManager(mngUsername);
      for (EmpInfoManagerDTO employeeInfo : employeeInfoList) {
        viewLeaveAccuralList = leaveManagementService.viewLeavesAccrual(employeeInfo.getEmpNo(),
            asOfDate);
        viewAllLeaveAccuralList.addAll(viewLeaveAccuralList);
      }
      return viewAllLeaveAccuralList;
    } else {
      // check Manager is existing for Empno
      Boolean isManagerForEmployee = managerSelfServiceDAO.checkManagerWithEmployee(mngUsername,
          empNo);
      if (!isManagerForEmployee) {
        throw new BusinessException(
            Resource.getProperty(MessageKeys.EMPLOYEE_AND_MANAGER_NOT_MATCHING, userLang));
      }
      viewLeaveAccuralList = leaveManagementService.viewLeavesAccrual(empNo, asOfDate);
      return viewLeaveAccuralList;
    }
  }

  @Override
  public ViewLeaveDTO getEmployeeLeaveByOriginalDecNo(String originalDecNo) {

    // find leave details by decision number
    EHCMAbsenceAttendance absenceAttendance = managerSelfServiceDAO
        .findLeaveDetailsByDecNo(originalDecNo);
    return mapViewLeaveDTO(absenceAttendance);
  }

  /**
   * fill leave details in view Leave DTO
   * 
   * @param absenceAttendance
   * @return
   */
  private ViewLeaveDTO mapViewLeaveDTO(EHCMAbsenceAttendance absenceAttendance) {

    ViewLeaveDTO viewLeaveDTO = new ViewLeaveDTO();
    viewLeaveDTO.setAbsenceType(absenceAttendance.getEhcmAbsenceType().getJobGroupName());
    if (null != absenceAttendance.getStartDate()) {
      viewLeaveDTO.setStartDate(
          DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, absenceAttendance.getStartDate()));
    }
    if (null != absenceAttendance.getEndDate()) {
      viewLeaveDTO.setEndDate(
          DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, absenceAttendance.getEndDate()));
    }
    viewLeaveDTO.setPendingUser(absenceAttendance.getAuthorizedPerson());
    viewLeaveDTO.setStatus(absenceAttendance.getDecisionStatus());
    viewLeaveDTO.setPeriod(absenceAttendance.getAbsenceDays());
    if (null != absenceAttendance.getDecisionDate()) {
      viewLeaveDTO.setRequestDate(DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT,
          absenceAttendance.getDecisionDate()));
    }
    return viewLeaveDTO;
  }

  @Override
  public List<EmpBusinessMissionManagerDTO> getAllEmployeesBusinessMissionByManagerUsername(
      String username) throws BusinessException, SystemException {

    List<EmpInfoManagerDTO> employeeInfoList = getAllEmployeesByManager(username);
    return mapEmployeeBusinessMissionByManagerInfo(employeeInfoList);
  }

  /**
   * fill employee mission details
   * 
   * @param employeeInfoList
   * @return
   */
  private List<EmpBusinessMissionManagerDTO> mapEmployeeBusinessMissionByManagerInfo(
      List<EmpInfoManagerDTO> employeeInfoList) {

    List<EmpBusinessMissionManagerDTO> mapEmployeeBusinessMissionList = new ArrayList<EmpBusinessMissionManagerDTO>();
    EmpBusinessMissionManagerDTO empBusinessMissionManagerDTO = null;
    for (EmpInfoManagerDTO empInfoManagerDTO : employeeInfoList) {
      List<EHCMEmpBusinessMission> businessMissionList = managerSelfServiceDAO
          .findBusinessMissionByEmployee(empInfoManagerDTO.getEmpNo());
      for (EHCMEmpBusinessMission businessMission : businessMissionList) {
        empBusinessMissionManagerDTO = new EmpBusinessMissionManagerDTO();
        empBusinessMissionManagerDTO.setBalance(businessMission.getMissionBalance().intValue());
        empBusinessMissionManagerDTO.setEmpName(businessMission.getEmployee().getName());
        empBusinessMissionManagerDTO.setEmpNo(businessMission.getEmployee().getSearchKey());
        empBusinessMissionManagerDTO.setEndDate(
            DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, businessMission.getEndDate()));
        empBusinessMissionManagerDTO
            .setMissionCategory(businessMission.getMissionCategory().getName());
        empBusinessMissionManagerDTO.setOriginalDecisionNo(businessMission.getDecisionNo());
        empBusinessMissionManagerDTO.setStartDate(
            DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, businessMission.getStartDate()));
        mapEmployeeBusinessMissionList.add(empBusinessMissionManagerDTO);
      }
    }
    return mapEmployeeBusinessMissionList;
  }

  @Override
  public List<EmpBusinessMissionManagerDTO> getEmployeeBusinessMissionsByDateAndEmpNo(
      String mngUsername, String asOfDate, String empNo)
      throws BusinessException, SystemException, ParseException {
    final String userLang = SecurityUtils.getUserLanguage();
    Date asOfDate_yyyyMMdd = DateUtils.convertStringToDate(OPEN_BRAVO_YEARDATE_FORMAT,
        Utility.convertToGregorian(asOfDate));

    List<EHCMEmpBusinessMission> businessMissionAsOfDate = new ArrayList<EHCMEmpBusinessMission>();

    if (!StringUtils.isEmpty(empNo)) {
      // check Given Manager is existing for Employee
      Boolean isManagerForEmployee = managerSelfServiceDAO.checkManagerWithEmployee(mngUsername,
          empNo);
      if (!isManagerForEmployee) {
        throw new BusinessException(
            Resource.getProperty(MessageKeys.EMPLOYEE_AND_MANAGER_NOT_MATCHING, userLang));
      }
      List<EHCMEmpBusinessMission> businessMissionList = managerSelfServiceDAO
          .findBusinessMissionByEmployee(empNo);
      for (EHCMEmpBusinessMission businessMission : businessMissionList) {
        if (asOfDate_yyyyMMdd.compareTo(businessMission.getStartDate()) >= 0) {
          businessMissionAsOfDate.add(businessMission);
        }
      }
      return mapBusinessMission(businessMissionAsOfDate);
    } else {
      List<EmpBusinessMissionManagerDTO> businessMissionByManagerDTO = getAllEmployeesBusinessMissionByManagerUsername(
          mngUsername);
      List<EmpBusinessMissionManagerDTO> businessMissionManagerDTO_asOfDate = new ArrayList<EmpBusinessMissionManagerDTO>();
      for (EmpBusinessMissionManagerDTO businessMissionDTO : businessMissionByManagerDTO) {

        String strMissionStarDate_yyyyMMdd = DateUtils
            .convertDateToString(OPEN_BRAVO_YEARDATE_FORMAT, asOfDate_yyyyMMdd);
        Date missionStartDate_yyyyMMdd = DateUtils.convertStringToDate(OPEN_BRAVO_YEARDATE_FORMAT,
            strMissionStarDate_yyyyMMdd);
        if (missionStartDate_yyyyMMdd.compareTo(DateUtils
            .convertStringToDate(OPEN_BRAVO_DATE_FORMAT, businessMissionDTO.getStartDate())) >= 0) {
          businessMissionManagerDTO_asOfDate.add(businessMissionDTO);
        }
      }
      return businessMissionManagerDTO_asOfDate;
    }

  }

  /**
   * fill business mission details
   * 
   * @param businessMissionAsOfDate
   * @return
   */
  private List<EmpBusinessMissionManagerDTO> mapBusinessMission(
      List<EHCMEmpBusinessMission> businessMissionAsOfDate) {

    List<EmpBusinessMissionManagerDTO> mapEmployeeBusinessMissionList = new ArrayList<EmpBusinessMissionManagerDTO>();
    EmpBusinessMissionManagerDTO empBusinessMissionManagerDTO = null;
    for (EHCMEmpBusinessMission businessMission : businessMissionAsOfDate) {
      empBusinessMissionManagerDTO = new EmpBusinessMissionManagerDTO();
      empBusinessMissionManagerDTO.setBalance(businessMission.getMissionBalance().intValue());
      empBusinessMissionManagerDTO.setEmpName(businessMission.getEmployee().getName());
      empBusinessMissionManagerDTO.setEndDate(
          DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, businessMission.getEndDate()));
      empBusinessMissionManagerDTO
          .setMissionCategory(businessMission.getMissionCategory().getName());
      empBusinessMissionManagerDTO.setOriginalDecisionNo(businessMission.getDecisionNo());
      empBusinessMissionManagerDTO.setStartDate(
          DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, businessMission.getStartDate()));
      mapEmployeeBusinessMissionList.add(empBusinessMissionManagerDTO);
    }

    return mapEmployeeBusinessMissionList;
  }

  @Override
  public EmpBusinessMissionDTO getEmployeeBusinessMissionByOriginalDecNo(String originalDecNo) {

    return null;
  }

  @Override
  public List<EmpBusinessMissionManagerDTO> getEmployeeBusinessMissionsByManagerAndDate(
      String mngUsername, String asOfDate)
      throws BusinessException, SystemException, ParseException {

    return getEmployeeBusinessMissionsByDateAndEmpNo(mngUsername, asOfDate, "");
  }

}
