package sa.elm.ob.hcm.services.businessTrips;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.activiti.ActivitiConstants;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMMisCatPeriod;
import sa.elm.ob.hcm.EHCMMiscatEmployee;
import sa.elm.ob.hcm.EHCMMissionCategory;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.dao.businessTrip.BusinessTripDAO;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.dto.businessTrips.BusinessPaymentDTO;
import sa.elm.ob.hcm.dto.businessTrips.BusinessTripRequestDTO;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.services.workflow.WorkflowUtilityService;
import sa.elm.ob.hcm.util.MessageKeys;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.utility.util.DateUtils;
import sa.elm.ob.utility.util.UtilityDAO;

@Service
public class BusinessTripsServiceImpl implements BusinessTripsService {
  private static final Logger log = LoggerFactory.getLogger(BusinessTripsServiceImpl.class);

  DateFormat dateYearFormat = sa.elm.ob.utility.util.Utility.dateFormat;

  private static final String OPEN_BRAVO_DATE_FORMAT = "dd-MM-yyyy";

  private static final String CONVERT_TO_HIJRI_DATE_FORMAT = "yyyy-MM-dd";

  @Autowired
  BusinessTripDAO businessTripDAO;

  @Autowired
  EmployeeProfileDAO employeeProfileDAO;

  @Autowired
  MissionCategoryDAO missionCategoryDAO;

  @Autowired
  WorkflowUtilityService workflowUtilityService;

  @Override
  public Boolean submitBusinessTripRequest(String username,
      BusinessTripRequestDTO businessTripRequestDTO) throws BusinessException, SystemException {
    createOrUpdateBusinessTrip(username, businessTripRequestDTO, Constants.CREATE_DECISION);
    return true;
  }

  private void createOrUpdateBusinessTrip(String username,
      BusinessTripRequestDTO businessTripRequestDTO, String decisionType)
      throws BusinessException, SystemException {
    // find employee by user name
    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);
    EHCMEmpBusinessMission businessMissionOB = createBusinessMission(employeeOB,
        businessTripRequestDTO, decisionType);
    // pre-validation before moving businessMission from transaction table to actual
    businessMissionValidations(businessMissionOB);
    // insert record into Summary table from Business Mission table
    businessTripDAO.createBusinessMissionSummary(businessMissionOB, decisionType);

  }

  private void createOrUpdateBusinessTripWithWorkflow(String username,
      BusinessTripRequestDTO businessTripRequestDTO, String decisionType, String subject)
      throws BusinessException, SystemException {

    // find employee by user name
    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);
    EHCMEmpBusinessMission businessMissionOB = createBusinessMission(employeeOB,
        businessTripRequestDTO, decisionType);

    // pre-validation before moving businessMission from transaction table to actual
    businessMissionValidations(businessMissionOB);

    String firstLineManagerId = workflowUtilityService.getLineManagerByUserName(username);
    String departmentManagerId = workflowUtilityService.getDepartmentManager(username);
    String governor = workflowUtilityService.getTopManagerByUsername(username);
    boolean isDepManager = firstLineManagerId.equals(departmentManagerId) ? true : false;

    Map<String, Object> variablesMap = new HashMap<String, Object>();
    variablesMap.put(ActivitiConstants.TARGET_IDENTIFIER, businessMissionOB.getDecisionNo());
    variablesMap.put(sa.elm.ob.utility.util.Constants.TASK_REQUESTER_USERNAME, username);
    variablesMap.put(sa.elm.ob.utility.util.Constants.TASK_REQUESTER_NAME, employeeOB.getName());
    variablesMap.put(sa.elm.ob.utility.util.Constants.TASK_SUBJECT, subject);
    variablesMap.put(sa.elm.ob.utility.util.Constants.TASK_LETTER_NUMBER, "");
    variablesMap.put(sa.elm.ob.utility.util.Constants.TAKS_REQUEST_DATE,
        sa.elm.ob.utility.util.Utility
            .convertTohijriDate(DateUtils.convertDateToString("yyyy-MM-dd", new Date())));
    variablesMap.put(sa.elm.ob.utility.util.Constants.TASK_STATUS, Constants.REQUEST_IN_PROGRESS);
    variablesMap.put(sa.elm.ob.utility.util.Constants.TASK_TYPE, decisionType);
    variablesMap.put(sa.elm.ob.utility.util.Constants.TASK_REQUESTER_EMAIL, employeeOB.getEmail());

    variablesMap.put("lineManager", firstLineManagerId);
    variablesMap.put("departmentManager", departmentManagerId);
    variablesMap.put("missionType", businessTripRequestDTO.getMissionType());
    variablesMap.put("isDepManager", isDepManager);
    variablesMap.put("governor", governor);

    startWorkflow(variablesMap);
  }

  /**
   * it will handle all pre validations before creating business Mission Trip
   * 
   * @param businessMissionOB
   * @throws BusinessException
   */
  private void businessMissionValidations(EHCMEmpBusinessMission businessMissionOB)
      throws BusinessException, SystemException {
    // checking decision overlap
    JSONObject result = new JSONObject();
    EHCMMissionCategory businessMissionCategory = null;
    EHCMMisCatPeriod businessMissionCatPeriod = null;
    EHCMMiscatEmployee businessMissionCatEmployee = null;
    String decisionType = businessMissionOB.getDecisionType();
    String clientId = businessMissionOB.getClient().getId();
    final String userLang = SecurityUtils.getUserLanguage();
    try {
      OBContext.setAdminMode();
      // find employee
      EhcmEmpPerInfo employeeOB = OBDal.getInstance().get(EhcmEmpPerInfo.class,
          businessMissionOB.getEmployee().getId());
      if (businessMissionOB != null) {
        if (businessMissionOB.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
            || businessMissionOB.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
            || businessMissionOB.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
          if (businessMissionOB.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
            result = Utility.chkDecisionOverlap(Constants.BUSINESSMISSION_OVERLAP,
                sa.elm.ob.utility.util.Utility.formatDate(businessMissionOB.getExtendStartdate()),
                sa.elm.ob.utility.util.Utility.formatDate(businessMissionOB.getExtendEnddate()),
                businessMissionOB.getEmployee().getId(),
                businessMissionOB.getMissionCategory().getId(), businessMissionOB.getId());
          } else {
            result = Utility.chkDecisionOverlap(Constants.BUSINESSMISSION_OVERLAP,
                sa.elm.ob.utility.util.Utility.formatDate(businessMissionOB.getStartDate()),
                sa.elm.ob.utility.util.Utility.formatDate(businessMissionOB.getEndDate()),
                businessMissionOB.getEmployee().getId(),
                businessMissionOB.getMissionCategory().getId(), businessMissionOB.getId());
          }
          log.debug("result:" + result);
          if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
            if (businessMissionOB.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
                || businessMissionOB.getDecisionType()
                    .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
                || (businessMissionOB.getDecisionType()
                    .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                    && (result.has("businessMissionId") && !result.getString("businessMissionId")
                        .equals(businessMissionOB.getOriginalDecisionNo().getId()))
                    || !result.has("businessMissionId"))) {
              if (result.has("errormsg")) {
                throw new BusinessException(OBMessageUtils.messageBD(result.getString("errormsg")));
              }
            }
          }
        }

        businessMissionCategory = businessMissionOB.getMissionCategory();
        if (!decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND))
          businessMissionCatPeriod = sa.elm.ob.hcm.util.UtilityDAO.getMissionPeriod(clientId,
              businessMissionCategory, dateYearFormat.format(businessMissionOB.getStartDate()),
              dateYearFormat.format(businessMissionOB.getEndDate()));
        if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
          businessMissionCatPeriod = sa.elm.ob.hcm.util.UtilityDAO.getMissionPeriod(clientId,
              businessMissionCategory,
              dateYearFormat.format(businessMissionOB.getExtendStartdate()),
              dateYearFormat.format(businessMissionOB.getExtendEnddate()));
        }
        if (businessMissionCatPeriod != null) {
          businessMissionCatEmployee = missionCategoryDAO
              .getEmployeeinPeriod(businessMissionCatPeriod, employeeOB.getId());
          if (businessMissionCatEmployee != null && businessMissionCatEmployee.isEnabled()) {
            Long remainingDays = businessMissionCatPeriod.getDays()
                - businessMissionCatEmployee.getUseddays();
            if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
              if (remainingDays < businessMissionOB.getMissionDays()) {
                throw new BusinessException(
                    OBMessageUtils.messageBD("EHCM_MisBal_GrtThan_MissDays"));
              }
            }
            if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
              if (remainingDays < businessMissionOB.getExtendMissionDay()) {
                throw new BusinessException(
                    OBMessageUtils.messageBD("EHCM_MisBal_GrtThan_MissDays"));
              }
            }
            if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
              Long diff = businessMissionOB.getMissionDays()
                  - businessMissionOB.getOriginalDecisionNo().getMissionDays();
              if (remainingDays < diff) {
                throw new BusinessException(
                    OBMessageUtils.messageBD("EHCM_MisBal_GrtThan_MissDays"));
              }
            }
          } else {
            throw new BusinessException(OBMessageUtils.messageBD("EHCM_MisBalOfEmp_Inactive"));
          }
        } else {
          throw new BusinessException(OBMessageUtils.messageBD("EHCM_MisCat_Period_DoesntExist"));

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

  private EHCMEmpBusinessMission createBusinessMission(EhcmEmpPerInfo employeeOB,
      BusinessTripRequestDTO businessTripRequestDTO, String decisionType)
      throws SystemException, BusinessException {

    EHCMEmpBusinessMission busineesMissionOB = businessTripDAO.createBusinessMission(employeeOB,
        businessTripRequestDTO, decisionType);
    return busineesMissionOB;
  }

  @Override
  public List<String> getAllOriginalDecisionNoByUsername(String username) {

    List<String> originalDecisionList = businessTripDAO
        .getAllOriginalDecisionNoByUserName(username);
    return originalDecisionList;

  }

  @Override
  public BusinessTripRequestDTO getBusinessTripRequestByOrginalDecNo(String originalDecNo) {

    List<BusinessTripRequestDTO> businessTripList = getBusinessTripRequestByDecisionNo(
        originalDecNo);
    return businessTripList.get(0);
  }

  /**
   * Find business trip request by userName
   * 
   * @param username
   * @param originalDecNo
   * @return
   */
  private List<BusinessTripRequestDTO> getBusinessTripRequestByDecisionNo(String originalDecNo) {

    List<EHCMEmpBusinessMission> businessMissionList = businessTripDAO
        .getBusinessTripByDecisionNo(originalDecNo);
    List<BusinessTripRequestDTO> mappedBusinessMissionList = mapBusinessTrip(businessMissionList);
    return mappedBusinessMissionList;
  }

  /**
   * fill details to business trip
   * 
   * @param businessMissionList
   * @return
   */
  private List<BusinessTripRequestDTO> mapBusinessTrip(
      List<EHCMEmpBusinessMission> businessMissionList) {

    List<BusinessTripRequestDTO> businessTripList = new ArrayList<BusinessTripRequestDTO>();
    BusinessTripRequestDTO businessTripRequestDTO = null;
    for (EHCMEmpBusinessMission businessMissionOB : businessMissionList) {
      businessTripRequestDTO = new BusinessTripRequestDTO();
      if (businessMissionOB.getLetterDate() != null) {
        businessTripRequestDTO.setLetterDate(UtilityDAO.convertTohijriDate(DateUtils
            .convertDateToString(CONVERT_TO_HIJRI_DATE_FORMAT, businessMissionOB.getLetterDate())));
      }
      businessTripRequestDTO.setLetterNo(businessMissionOB.getLetterNo());
      businessTripRequestDTO.setMissionType(businessMissionOB.getMissionType().getSearchKey());
      businessTripRequestDTO.setMissionCategory(businessMissionOB.getMissionCategory().getName());
      businessTripRequestDTO.setFromCountry(businessMissionOB.getFromCountry().getName());
      businessTripRequestDTO.setToCountry(businessMissionOB.getToCountry().getName());
      businessTripRequestDTO.setToCity(businessMissionOB.getToCity().getName());
      businessTripRequestDTO.setFromCity(businessMissionOB.getFromCity().getName());
      businessTripRequestDTO.setMissionDays(businessMissionOB.getMissionDays().intValue());

      businessTripRequestDTO.setStartDate(UtilityDAO.convertTohijriDate(DateUtils
          .convertDateToString(CONVERT_TO_HIJRI_DATE_FORMAT, businessMissionOB.getStartDate())));
      businessTripRequestDTO.setEndDate(UtilityDAO.convertTohijriDate(DateUtils
          .convertDateToString(CONVERT_TO_HIJRI_DATE_FORMAT, businessMissionOB.getEndDate())));
      businessTripRequestDTO.setNoOfDaysAfter(businessMissionOB.getNoofdaysAfter().intValue());
      businessTripRequestDTO.setNoOfDaysBefore(businessMissionOB.getNoofdaysBefore().intValue());
      businessTripRequestDTO.setHousingProvided(businessMissionOB.isHousingProvided());
      businessTripRequestDTO.setFoodProvided(businessMissionOB.isFoodProvided());
      businessTripRequestDTO.setTicketsProvided(businessMissionOB.isTicketsProvided());
      businessTripRequestDTO.setRoundTrip(businessMissionOB.isRoundTrip());
      businessTripRequestDTO.setTaskDescription(businessMissionOB.getTaskDescription());
      businessTripList.add(businessTripRequestDTO);

      if (businessMissionOB.getOriginalDecisionNo() != null)
        businessTripRequestDTO
            .setOrginalDecNo(businessMissionOB.getOriginalDecisionNo().getDecisionNo());
    }
    return businessTripList;
  }

  @Override
  public Boolean submitCancelBusinessTripRequest(String username, String originalDecNo)
      throws BusinessException, SystemException {

    // fill businessTrip Request Details by Original Decision Number
    BusinessTripRequestDTO businessTripRequestDTO = getBusinessTripRequestByOrginalDecNo(
        originalDecNo);
    createOrUpdateBusinessTrip(username, businessTripRequestDTO, Constants.CANCEL_DECISION);
    return true;
  }

  @Override
  public Boolean submitPaymentBusinessTripRequest(String username, String originalDecNo,
      BusinessPaymentDTO businessPaymentDTO) throws BusinessException, SystemException {

    // fill businessTrip Request Details by Original Decision Number
    BusinessTripRequestDTO businessTripRequestDTO = getBusinessTripRequestByOrginalDecNo(
        originalDecNo);
    businessTripRequestDTO.setPaymentDetails(businessPaymentDTO);
    createOrUpdateBusinessTrip(username, businessTripRequestDTO,
        Constants.DECISION_TYPE_BUSINESSMISSION_PAYMENT);
    // need to confirm about payment details
    return true;
  }

  @Override
  public Boolean submitBusinessTripRequestWithApprovalFlow(String username,
      BusinessTripRequestDTO businessTripRequestDTO) throws BusinessException, SystemException {

    createOrUpdateBusinessTripWithWorkflow(username, businessTripRequestDTO,
        Constants.CREATE_DECISION, "Business Request");
    return true;
  }

  @Override
  public Boolean submitCancelBusinessTripRequestWithWorkflow(String username, String originalDecNo)
      throws BusinessException, SystemException {

    // fill businessTrip Request Details by Original Decision Number
    BusinessTripRequestDTO businessTripRequestDTO = getBusinessTripRequestByOrginalDecNo(
        originalDecNo);

    businessTripRequestDTO.setOrginalDecNo(originalDecNo);

    createOrUpdateBusinessTripWithWorkflow(username, businessTripRequestDTO,
        Constants.CANCEL_DECISION, "Cancel Busintess Request");
    return true;
  }

  @Override
  public Boolean submitPaymentBusinessTripRequestWithWorkflow(String username, String originalDecNo)
      throws BusinessException, SystemException {

    // fill businessTrip Request Details by Original Decision Number
    BusinessTripRequestDTO businessTripRequestDTO = getBusinessTripRequestByOrginalDecNo(
        originalDecNo);

    businessTripRequestDTO.setOrginalDecNo(originalDecNo);

    BusinessPaymentDTO businessPaymentDTO = new BusinessPaymentDTO();
    businessPaymentDTO.setPaymentAmount(100);
    businessPaymentDTO.setAdvancePercentage(3);
    businessPaymentDTO.setAdvanceAmount(50);
    businessTripRequestDTO.setPaymentDetails(businessPaymentDTO);

    createOrUpdateBusinessTripWithWorkflow(username, businessTripRequestDTO,
        Constants.DECISION_TYPE_BUSINESSMISSION_PAYMENT, "Payment Request for Business Trip");
    return true;
  }

  private void startWorkflow(Map<String, Object> variablesMap) {
    workflowUtilityService.startWorkflow(
        sa.elm.ob.utility.util.Constants.BUSINESS_MISSION_WORKFLOW_KEY, variablesMap);
  }

}
