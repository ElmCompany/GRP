package sa.elm.ob.hcm.services.leave;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.AbsenceAccrual.AbsenceAccrualDAO;
import sa.elm.ob.hcm.ad_process.AbsenceDecision.AbsenceIssueDecisionDAO;
import sa.elm.ob.hcm.dao.leave.LeaveManagementDAO;
import sa.elm.ob.hcm.dao.leave.ViewLeaveDAO;
import sa.elm.ob.hcm.dto.leave.LeaveRequestDTO;
import sa.elm.ob.hcm.dto.leave.RejoinLeaveRequestDTO;
import sa.elm.ob.hcm.dto.leave.ViewLeaveAccrualDTO;
import sa.elm.ob.hcm.dto.leave.ViewLeaveDTO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.services.workflow.WorkflowUtilityService;
import sa.elm.ob.hcm.util.MessageKeys;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.utility.util.DateUtils;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * Leave Management Service Implementation
 *
 */
@Service("leaveManagementService")
public class LeaveManagementServiceImpl implements LeaveManagementService {
  private static final String OPEN_BRAVO_DATE_FORMAT = "dd-MM-yyyy";
  private static final String OPEN_BRAVO_GREG_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

  private final String LEAVE_MANAGEMENT_WORKFLOW_KEY = "leaveManagementWorkflow";

  private final String CANCEL_LEAVE_MANAGEMENT_WORKFLOW_KEY = "cancelLeaveManagementWorkflow";

  @Autowired
  private ViewLeaveDAO viewLeaveDAO;

  @Autowired
  private AbsenceAccrualDAO absenceAccrualDAO;

  @Autowired
  private LeaveManagementDAO leaveManagementDAO;

  @Autowired
  private AbsenceIssueDecisionDAO absenceIssueDecisionDAO;

  @Autowired
  private WorkflowUtilityService workflowUtilityService;

  @Override
  public List<ViewLeaveDTO> viewLeaves(String username, String asOfDate) {

    EhcmEmpPerInfo objEmployeeeInfo = viewLeaveDAO.getEmployeeProfileByUser(username);
    List<ViewLeaveDTO> viewLeaveDetails = getLeaveDetails(objEmployeeeInfo);
    List<ViewLeaveDTO> filteredLeaves = new ArrayList<>();

    for (ViewLeaveDTO leave : viewLeaveDetails) {
      try {
        if (DateUtils.convertStringToDate(OPEN_BRAVO_DATE_FORMAT, leave.getStartDate())
            .compareTo(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
                UtilityDAO.convertToGregorian(asOfDate))) <= 0) // FIXME convert asOfDate
          filteredLeaves.add(leave);
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return filteredLeaves;
  }

  /**
   * Get the list of leave Details
   * 
   * @param Employee
   *          Info
   * @return Employee leave Details List
   */
  private List<ViewLeaveDTO> getLeaveDetails(EhcmEmpPerInfo objEmployeeeInfo) {

    List<ViewLeaveDTO> leaveDetailList = mapLeaveDetails(
        objEmployeeeInfo.getEHCMAbsenceAttendanceList());

    return leaveDetailList;
  }

  /**
   * Convert leave details domain to DTO's
   * 
   * @param Leave
   *          List
   * @return
   */
  private List<ViewLeaveDTO> mapLeaveDetails(
      List<EHCMAbsenceAttendance> ehcmAbsenceAttendanceList) {
    List<ViewLeaveDTO> viewLeaveList = new ArrayList<ViewLeaveDTO>();
    ViewLeaveDTO viewLeaveDTO = null;

    for (EHCMAbsenceAttendance absenceAttendance : ehcmAbsenceAttendanceList) {
      viewLeaveDTO = new ViewLeaveDTO();
      viewLeaveDTO.setAbsenceType(absenceAttendance.getEhcmAbsenceType().getJobGroupName());
      if (null != absenceAttendance.getStartDate()) {
        viewLeaveDTO.setStartDate(DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT,
            absenceAttendance.getStartDate()));
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
      viewLeaveList.add(viewLeaveDTO);

    }

    return viewLeaveList;
  }

  /*
   * asOfDate (format: yyyy-MM-dd)
   */
  @Override
  public List<ViewLeaveAccrualDTO> viewLeavesAccrual(String username, String asOfDate)
      throws BusinessException, SystemException {
    final String userLang = SecurityUtils.getUserLanguage();
    EhcmEmpPerInfo objEmployeeeInfo = viewLeaveDAO.getEmployeeProfileByUser(username);

    List<EHCMAbsenceType> absenceTypeList = viewLeaveDAO
        .getAbsenceType(objEmployeeeInfo.getClient().getId());
    if (absenceTypeList == null)
      throw new BusinessException(
          Resource.getProperty(MessageKeys.ABSENCETYPE_NOT_AVAILABLE, userLang));
    List<ViewLeaveAccrualDTO> accuralLeaveDTO = new ArrayList<>();
    try {
      // convert as of date to "yyyy-MM-dd" format gregorian Date
      accuralLeaveDTO = mapAccuralLeave(objEmployeeeInfo, absenceTypeList,
          UtilityDAO.convertToGregorian(asOfDate));
    } catch (Exception e) {
      e.printStackTrace();
    }

    return accuralLeaveDTO;
  }

  /**
   * Convert Accural Details to DTO
   * 
   * @param objEmployeeeInfo
   * @param absenceTypeList
   * @return
   * @throws Exception
   */
  private List<ViewLeaveAccrualDTO> mapAccuralLeave(EhcmEmpPerInfo objEmployeeeInfo,
      List<EHCMAbsenceType> absenceTypeList, String asOfDate) throws Exception {
    List<ViewLeaveAccrualDTO> viewAccuralList = new ArrayList<ViewLeaveAccrualDTO>();
    ViewLeaveAccrualDTO viewLeaveAccuralDTO = null;
    // AbsenceAccrualDAOImpl absenceAccrualDAOImpl = new AbsenceAccrualDAOImpl();
    for (EHCMAbsenceType absenceType : absenceTypeList) {
      JSONObject jsonObject = viewLeaveDAO.getAvailedAndAvailableDays(absenceType, objEmployeeeInfo,
          asOfDate);
      JSONObject jsonObjectDates = absenceAccrualDAO.getStartDateAndEndDate(asOfDate, absenceType,
          objEmployeeeInfo.getId());
      viewLeaveAccuralDTO = new ViewLeaveAccrualDTO();
      viewLeaveAccuralDTO.setAbsenceType(absenceType.getJobGroupName());
      try {
        if (jsonObject.length() > 0) {
          viewLeaveAccuralDTO
              .setBalance(Math.round(Float.valueOf(jsonObject.getString("availabledays"))));
          viewLeaveAccuralDTO
              .setLeaves(Math.round(Float.valueOf(jsonObject.getString("availeddays"))));
        }

        viewLeaveAccuralDTO.setEmpName(objEmployeeeInfo.getName());
        viewLeaveAccuralDTO.setEmpNo(objEmployeeeInfo.getSearchKey());
        if (jsonObjectDates.length() > 0) {
          viewLeaveAccuralDTO.setStartDate(jsonObjectDates.getString("startdate"));
          viewLeaveAccuralDTO.setEndDate(jsonObjectDates.getString("enddate"));
        }

      } catch (NumberFormatException | JSONException e) {
        e.printStackTrace();
      }
      viewAccuralList.add(viewLeaveAccuralDTO);
    }

    return viewAccuralList;
  }

  @Override
  public Boolean submitLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO)
      throws SystemException, BusinessException {

    // create leave request based on decision
    createOrUpdateLeaveRequest(username, leaveRequestDTO, Constants.CREATE_DECISION);
    return true;
  }

  /**
   * 
   * @param employeeOB
   * @param leaveRequestDTO
   * @param decisionType
   * @return created absence decision
   */
  private EHCMAbsenceAttendance createAbsenceAttendanceForEmployee(EhcmEmpPerInfo employeeOB,
      LeaveRequestDTO leaveRequestDTO, String decisionType, EHCMAbsenceType absenceTypeOB) {

    EHCMAbsenceAttendance absenceAttendance = leaveManagementDAO.createAbsenceAttendanceForEmployee(
        employeeOB, leaveRequestDTO, decisionType, absenceTypeOB);
    return absenceAttendance;

  }

  /**
   * it will handle all pre validations before creating leave request
   * 
   * @param absenceAttendance
   * @throws BusinessException
   */
  private void leaveRequestValidations(EHCMAbsenceAttendance absenceAttendance)
      throws BusinessException, SystemException {
    final String userLang = SecurityUtils.getUserLanguage();
    try {
      OBContext.setAdminMode();
      // checking decision overlap
      if (absenceAttendance.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
          || absenceAttendance.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
          || absenceAttendance.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
        JSONObject decresult = Utility.chkDecisionOverlap(Constants.ABSENCE_OVERLAP,
            sa.elm.ob.utility.util.Utility.formatDate(absenceAttendance.getStartDate()),
            sa.elm.ob.utility.util.Utility.formatDate(absenceAttendance.getEndDate()),
            absenceAttendance.getEhcmEmpPerinfo().getId(),
            absenceAttendance.getEhcmAbsenceType().getId(), absenceAttendance.getId());

        if (decresult != null && decresult.has("errorFlag") && decresult.getBoolean("errorFlag")) {
          if (absenceAttendance.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
              || absenceAttendance.getDecisionType()
                  .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
              || (absenceAttendance.getDecisionType()
                  .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                  && (decresult.has("businessMissionId")
                      && !decresult.getString("businessMissionId")
                          .equals(absenceAttendance.getOriginalDecisionNo().getId()))
                  || !decresult.has("leaveId"))) {
            if (decresult.has("errormsg")) {
              OBDal.getInstance().rollbackAndClose();
              throw new BusinessException(
                  OBMessageUtils.messageBD(decresult.getString("errormsg")));
            }
          }
        }
      }
      if (!absenceAttendance.getDecisionType().equals("CA")
          && !absenceAttendance.getDecisionType().equals("CO")) {
        String chkapp = absenceIssueDecisionDAO.chkleaveapprove(absenceAttendance);
        String chkappmessage = OBMessageUtils.messageBD(chkapp);

        if (!chkappmessage.equals("Success")) {
          // leave less than 5 or 7 days then throw the error msg with corresponding input
          // in
          // absenceAttendance Type Rules
          if (chkapp.equals("EHCM_LLTF")) {
            chkapp = leaveManagementDAO.getLeaveRequestDaysValidation(absenceAttendance, chkapp);
          } else if (chkapp.contains("EHCM_LevNotAvailable")) {
            String output = chkapp.split("-")[1];
            chkapp = OBMessageUtils.messageBD(chkapp.split("-")[0]);
            chkapp = chkapp.replace("%", output);
          } else {
            String output = chkapp.split("_")[1];
            chkapp = OBMessageUtils.messageBD("EHCM_AbsDecisionLevApp_Error");
            chkapp = chkapp.replace("%", output);
            OBDal.getInstance().rollbackAndClose();
            throw new BusinessException(OBMessageUtils.messageBD(chkapp));
          }
          OBDal.getInstance().rollbackAndClose();
          throw new BusinessException(OBMessageUtils.messageBD(chkapp));
        }
      }
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      e.printStackTrace();
      throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * 
   * @param absenceType
   *          value
   * @return absence type entity
   */
  private EHCMAbsenceType findAbsenceType(String absenceType) {

    EHCMAbsenceType absenceTypeOB = leaveManagementDAO.findAbsenceType(absenceType);

    return absenceTypeOB;
  }

  @Override
  public LeaveRequestDTO saveForLaterLeaveRequest(String username,
      LeaveRequestDTO leaveRequestDTO) {

    return null;
  }

  @Override
  public List<String> getAllOriginalDecisionNo(String username) {

    List<String> originalDecisionList = leaveManagementDAO
        .getAllOriginalDecisionNoByUserName(username);
    return originalDecisionList;
  }

  @Override
  public LeaveRequestDTO getLeaveRequestByOriginalDecisionNo(String orginalDecNo) {

    List<LeaveRequestDTO> leaveRequestList = getLeaveRequestByDecisionNo(orginalDecNo);
    return leaveRequestList.get(0);
  }

  private List<LeaveRequestDTO> getLeaveRequestByDecisionNo(String orginalDecNo) {

    List<EHCMAbsenceAttendance> absenceDecisionList = leaveManagementDAO
        .getLeaveRequestByDecisionNoOrByUsername(orginalDecNo, null);
    List<LeaveRequestDTO> leaveRequestList = mapLeaveRequest(absenceDecisionList);
    return leaveRequestList;
  }

  /**
   * Fill details to leave request
   * 
   * @param absenceDecisionList
   * @return
   */
  private List<LeaveRequestDTO> mapLeaveRequest(List<EHCMAbsenceAttendance> absenceDecisionList) {

    List<LeaveRequestDTO> leaveRequestList = new ArrayList<LeaveRequestDTO>();
    LeaveRequestDTO leaveRequestDTO = null;
    for (EHCMAbsenceAttendance absenceAttance : absenceDecisionList) {
      leaveRequestDTO = new LeaveRequestDTO();
      leaveRequestDTO.setDecisionNumber(absenceAttance.getDecisionNo());
      leaveRequestDTO.setAbsenceType(absenceAttance.getEhcmAbsenceType().getJobGroupName());
      leaveRequestDTO.setStartDate(UtilityDAO.convertTohijriDate(
          DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, absenceAttance.getStartDate())));
      leaveRequestDTO.setEndDate(UtilityDAO.convertTohijriDate(
          DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, absenceAttance.getEndDate())));
      leaveRequestDTO.setAbsenceDays(absenceAttance.getAbsenceDays().intValue());
      leaveRequestDTO.setAbsenceReason(absenceAttance.getEhcmAbsenceReason().getAbsenceReason());
      leaveRequestDTO.setRemarks(absenceAttance.getRemarks());
      leaveRequestList.add(leaveRequestDTO);
    }
    return leaveRequestList;
  }

  @Override
  public Boolean submitCutoffLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO)
      throws BusinessException, SystemException {

    createOrUpdateLeaveRequest(username, leaveRequestDTO, Constants.CUTOFF_DECISION);
    return true;
  }

  @Override
  public Boolean submitCutoffLeaveRequestWithApproval(String username,
      LeaveRequestDTO leaveRequestDTO) throws BusinessException, SystemException {

    createLeaveRequestWithApproval(username, leaveRequestDTO, Constants.CUTOFF_DECISION,
        "Cutoff Leave Request");
    return true;
  }

  @Override
  public LeaveRequestDTO saveForLaterCutoffLeaveRequest(String username, String orginalDecNo,
      LeaveRequestDTO leaveRequestDTO) {

    return null;
  }

  @Override
  public Boolean submitCancelLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO)
      throws BusinessException, SystemException {

    // create leave request based on decision
    createOrUpdateLeaveRequest(username, leaveRequestDTO, Constants.CANCEL_DECISION);
    return true;
  }

  @Override
  public RejoinLeaveRequestDTO submitRejoinLeaveRequest(String username, String orginalDecNo,
      RejoinLeaveRequestDTO leaveRequestDTO) {

    return null;
  }

  @Override
  public Boolean submitLeaveRequestWithApprovalFlow(String username,
      LeaveRequestDTO leaveRequestDTO) throws SystemException, BusinessException {

    createLeaveRequestWithApproval(username, leaveRequestDTO, Constants.CREATE_DECISION,
        "Leave Request");
    return true;
  }

  @Override
  public List<LeaveRequestDTO> getLeaveRequestByUserName(String userName) {
    List<LeaveRequestDTO> leaveRequestList = getLeaveRequestByuserName(userName);
    return leaveRequestList;
  }

  private List<LeaveRequestDTO> getLeaveRequestByuserName(String userName) {

    List<EHCMAbsenceAttendance> absenceDecisionList = leaveManagementDAO
        .getLeaveRequestByDecisionNoOrByUsername(null, userName);
    List<LeaveRequestDTO> leaveRequestList = mapLeaveRequest(absenceDecisionList);
    return leaveRequestList;
  }

  @Override
  public Boolean updateLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO)
      throws SystemException, BusinessException {

    // create leave request based on decision
    createOrUpdateLeaveRequest(username, leaveRequestDTO, Constants.UPDATE_DECISION);
    return true;
  }

  // @Override
  // public Boolean updateLeaveRequestWithApprovalFlow(String username,
  // LeaveRequestDTO leaveRequestDTO) throws SystemException, BusinessException {
  //
  // createOrUpdateLeaveRequest(username, leaveRequestDTO, Constants.UPDATE_DECISION);
  // startApprovalWorkFlow(username);
  // return true;
  // }

  @Override
  public Boolean createExtendLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO)
      throws SystemException, BusinessException {

    createOrUpdateLeaveRequest(username, leaveRequestDTO, Constants.EXTEND_DECISION);
    return true;
  }

  /**
   * //create leave request based on decision
   * 
   * @param username
   * @param leaveRequestDTO
   * @param decisionType
   */
  private void createOrUpdateLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO,
      String decisionType) throws BusinessException, SystemException {
    final String userLang = SecurityUtils.getUserLanguage();
    try {
      OBContext.setAdminMode();

      // find absenceAttendance type by absenceAttendance type value
      EHCMAbsenceType absenceTypeOB = findAbsenceType(leaveRequestDTO.getAbsenceType());
      if (absenceTypeOB == null)
        throw new BusinessException(
            Resource.getProperty(MessageKeys.ABSENCETYPE_NOT_AVAILABLE, userLang));
      EhcmEmpPerInfo employeeOB = viewLeaveDAO.getEmployeeProfileByUser(username);

      // save the request in transaction table
      EHCMAbsenceAttendance absenceAttendanceOB = createAbsenceAttendanceForEmployee(employeeOB,
          leaveRequestDTO, decisionType, absenceTypeOB);
      // call pre validations to create leave
      leaveRequestValidations(absenceAttendanceOB);
      // find accural
      EHCMAbsenceTypeAccruals absenceAccuralOB = leaveManagementDAO.getAccural(absenceAttendanceOB);
      if (absenceAccuralOB == null)
        throw new BusinessException(OBMessageUtils.messageBD("EHCM_AbsTypeAccrual_NotDefine"));

      // insert record into actual leave table from absence desicion table
      leaveManagementDAO.createLeave(absenceAttendanceOB, absenceTypeOB, absenceAccuralOB);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  // Saving the request in the transaction table and starting the workflow
  private void createLeaveRequestWithApproval(String username, LeaveRequestDTO leaveRequestDTO,
      String decisionType, String taskTitle) throws BusinessException, SystemException {
    EHCMAbsenceType absenceTypeOB = findAbsenceType(leaveRequestDTO.getAbsenceType());
    final String userLang = SecurityUtils.getUserLanguage();
    if (absenceTypeOB == null)
      throw new BusinessException(
          Resource.getProperty(MessageKeys.ABSENCETYPE_NOT_AVAILABLE, userLang));
    EhcmEmpPerInfo employeeOB = viewLeaveDAO.getEmployeeProfileByUser(username);

    // save the request in transaction table
    EHCMAbsenceAttendance absenceAttendanceOB = createAbsenceAttendanceForEmployee(employeeOB,
        leaveRequestDTO, decisionType, absenceTypeOB);
    // call pre validations to create leave
    leaveRequestValidations(absenceAttendanceOB);
    // find accural
    EHCMAbsenceTypeAccruals absenceAccuralOB = null;
    try {
      absenceAccuralOB = leaveManagementDAO.getAccural(absenceAttendanceOB);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (absenceAccuralOB == null)
      throw new BusinessException(OBMessageUtils.messageBD("EHCM_AbsTypeAccrual_NotDefine"));

    String firstLineManagerId = workflowUtilityService.getLineManagerByUserName(username);
    String departmentManagerId = workflowUtilityService.getDepartmentManager(username);
    String secondLineManagerId = "";

    // Is the first line manager a department manager?
    boolean isDepMng = false;
    // Is the second line manager a department manager?
    boolean isSecDepMng = false;

    if (!firstLineManagerId.equals(departmentManagerId)) {
      secondLineManagerId = workflowUtilityService.getLineManagerByUserId(firstLineManagerId);
      if (secondLineManagerId.equals(departmentManagerId))
        isSecDepMng = true;
    } else {
      isDepMng = true;
    }

    Map<String, Object> variablesMap = new HashMap<String, Object>();
    variablesMap.put("username", username);
    variablesMap.put("taskTitle", taskTitle);
    variablesMap.put("firstLineManager", firstLineManagerId);
    variablesMap.put("secondLineManager", secondLineManagerId);
    variablesMap.put("departmentManager", departmentManagerId);
    variablesMap.put("isDepManager", isDepMng);
    variablesMap.put("isSecDepManager", isSecDepMng);
    variablesMap.put("leaveType", leaveRequestDTO.getAbsenceType());
    variablesMap.put("absenceAttendanceId", absenceAttendanceOB.getId());
    variablesMap.put("emailAddress", employeeOB.getEmail());

    if (decisionType.equals(Constants.CANCEL_DECISION)) {
      workflowUtilityService.startWorkflow(CANCEL_LEAVE_MANAGEMENT_WORKFLOW_KEY, variablesMap);
    } else {
      workflowUtilityService.startWorkflow(LEAVE_MANAGEMENT_WORKFLOW_KEY, variablesMap);
    }
  }

  @Override
  public Boolean createExtendLeaveRequestWithApproval(String username,
      LeaveRequestDTO leaveRequestDTO) throws SystemException, BusinessException {

    createLeaveRequestWithApproval(username, leaveRequestDTO, Constants.EXTEND_DECISION,
        "Extend Leave Request");
    return true;
  }

  @Override
  public Boolean updateExtendLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO)
      throws SystemException, BusinessException {

    updateLeaveRequest(username, leaveRequestDTO);
    return true;
  }

  // @Override
  // public Boolean updateExtendLeaveRequestWithApproval(String username,
  // LeaveRequestDTO leaveRequestDTO) throws SystemException, BusinessException {
  //
  // updateLeaveRequest(username, leaveRequestDTO);
  // startApprovalWorkFlow(username);
  // return true;
  // }

  @Override
  public Boolean updateCutoffLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO)
      throws BusinessException, SystemException {

    updateLeaveRequest(username, leaveRequestDTO);
    return true;
  }

  // @Override
  // public Boolean updateCutoffLeaveRequestWithApproval(String username, String orginalDecNo,
  // LeaveRequestDTO leaveRequestDTO) throws BusinessException, SystemException {
  //
  // updateLeaveRequest(username, leaveRequestDTO);
  // startApprovalWorkFlow(username);
  // return true;
  // }

  @Override
  public Boolean submitCancelLeaveRequestWithApproval(String username,
      LeaveRequestDTO leaveRequestDTO) throws BusinessException, SystemException {

    createLeaveRequestWithApproval(username, leaveRequestDTO, Constants.CANCEL_DECISION,
        "Cancel Leave Request");
    return true;
  }

  @Override
  public Boolean submitRejoinLeaveRequestWithApproval(String username,
      RejoinLeaveRequestDTO leaveRequestDTO) {

    return true;
  }
}
