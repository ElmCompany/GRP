package sa.elm.ob.hcm.services.employment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbravo.activiti.ActivitiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sa.elm.ob.hcm.EhcmEmpAsset;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.GenericActivitiData;
import sa.elm.ob.hcm.ehcmpreviouservice;
import sa.elm.ob.hcm.ehcmqualification;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.dao.activiti.CommonActivitiDAO;
import sa.elm.ob.hcm.dao.employmentinformation.EmploymentInformationDAO;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.dto.employment.CertificationsDTO;
import sa.elm.ob.hcm.dto.employment.CustodiesDTO;
import sa.elm.ob.hcm.dto.employment.QualificationsDTO;
import sa.elm.ob.hcm.dto.employment.ViewEmplInfoDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.services.workflow.WorkflowUtilityService;
import sa.elm.ob.hcm.util.UtilityDAO;
import sa.elm.ob.utility.util.DateUtils;

@Service("employmentInformationService")
public class EmploymentInformationServiceImpl implements EmploymentInformationService {

  private static final String OPEN_BRAVO_DATE_FORMAT = "dd-MM-yyyy";

  private final String EMPLOYEE_INFORMATIONS_WORKFLOW_KEY = "empInfoWorkflow";

  @Autowired
  private CommonActivitiDAO commonActivitiDAO;

  private String jsonInString = "";

  UtilityDAO utilityDAO = new UtilityDAO();

  @Autowired
  EmploymentInformationDAO employmentInformationDAO;

  @Autowired
  private WorkflowUtilityService workflowUtilityService;

  @Autowired
  private EmployeeProfileDAO employeeProfileDAO;

  @Override
  public List<ViewEmplInfoDTO> getAllPreviousEmploymentInfo(String username)
      throws SystemException {
    // no need for this method in Self-service
    List<ehcmpreviouservice> previousEmploymentList = employmentInformationDAO
        .getAllPreviousEmploymentInformationByUsername(username);
    List<ViewEmplInfoDTO> listofPreviusEmployment = mapEmployment(previousEmploymentList);
    return listofPreviusEmployment;
  }

  /**
   * Fill the Employment details
   * 
   * @param previousEmploymentList
   * @return
   */
  private List<ViewEmplInfoDTO> mapEmployment(List<ehcmpreviouservice> previousEmploymentList) {

    List<ViewEmplInfoDTO> viewEmpInfoList = new ArrayList<ViewEmplInfoDTO>();
    for (ehcmpreviouservice previousEmployment : previousEmploymentList) {
      ViewEmplInfoDTO viewEmplInfoDTO = new ViewEmplInfoDTO();
      viewEmplInfoDTO.setDepartment(previousEmployment.getDeptName());
      viewEmplInfoDTO.setStartDate(
          DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, previousEmployment.getStartDate()));
      viewEmplInfoDTO.setEndDate(
          DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, previousEmployment.getEndDate()));
      viewEmplInfoDTO.setGrade(previousEmployment.getGrade());
      // viewEmplInfoDTO.setEmployerName(previousEmployment.getEmployerName());
      // viewEmplInfoDTO.setJobNo(jobNo);
      // viewEmplInfoDTO.setJobTitle(jobTitle);
      // viewEmplInfoDTO.setStep(step); //need to ask details // where to fetch above data

      // TODO no need for this method in Self-service

      viewEmpInfoList.add(viewEmplInfoDTO);
    }
    return viewEmpInfoList;
  }

  @Override
  public List<QualificationsDTO> getQualifications(String username) {

    List<ehcmqualification> qualificationList = employmentInformationDAO
        .getAllQualificationByUserName(username);

    List<QualificationsDTO> listOfQualification = new ArrayList<>();

    if (qualificationList.size() > 0)
      listOfQualification = mapQualifications(qualificationList);

    return listOfQualification;

  }

  /**
   * Fill the qualification Details
   * 
   * @param qualificationList
   * @return
   */
  private List<QualificationsDTO> mapQualifications(List<ehcmqualification> qualificationList) {

    List<QualificationsDTO> qualificationViewList = new ArrayList<QualificationsDTO>();
    for (ehcmqualification qualificationOB : qualificationList) {
      if (!qualificationOB.getEdulevel().equals("C")) { // based on education level identify
                                                        // as either qualification or certification
        QualificationsDTO qualificationDTO = new QualificationsDTO();

        if (qualificationOB.getStartDate() != null)
          qualificationDTO.setStartDate(DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT,
              qualificationOB.getStartDate()));

        if (qualificationOB.getEndDate() != null)
          qualificationDTO.setEndDate(
              DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, qualificationOB.getEndDate()));

        if (qualificationOB.getExpirydate() != null)
          qualificationDTO.setExpiryDate(DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT,
              qualificationOB.getExpirydate()));

        qualificationDTO.setId(qualificationOB.getId());
        qualificationDTO.setLocation(qualificationOB.getLocation());
        qualificationDTO.setDegree(qualificationOB.getDegree());
        qualificationDTO
            .setEducationLevel(qualificationOB.getLookupEducationLevel().getEnglishName());
        qualificationDTO.setMajor(qualificationOB.getLicensesub());
        qualificationViewList.add(qualificationDTO);
      }
    }
    return qualificationViewList;
  }

  @Override
  public List<CertificationsDTO> getCertifications(String username) {

    List<ehcmqualification> qualificationList = employmentInformationDAO
        .getAllQualificationByUserName(username);

    List<CertificationsDTO> listOfCertification = new ArrayList<>();

    if (qualificationList.size() > 0)
      listOfCertification = mapCertifications(qualificationList);
    return listOfCertification;

  }

  /**
   * fill the certification details
   * 
   * @param qualificationList
   * @return
   */
  private List<CertificationsDTO> mapCertifications(List<ehcmqualification> qualificationList) {
    List<CertificationsDTO> certificationViewList = new ArrayList<CertificationsDTO>();
    for (ehcmqualification qualificationOB : qualificationList) {
      if (qualificationOB.getEdulevel().equals("C")) {

        CertificationsDTO certificationsDTO = new CertificationsDTO();

        if (qualificationOB.getStartDate() != null)
          certificationsDTO.setStartDate(DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT,
              qualificationOB.getStartDate()));

        if (qualificationOB.getEndDate() != null)
          certificationsDTO.setEndDate(
              DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, qualificationOB.getEndDate()));

        if (qualificationOB.getExpirydate() != null)
          certificationsDTO.setExpiryDate(DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT,
              qualificationOB.getExpirydate()));

        certificationsDTO.setId(qualificationOB.getId());
        certificationsDTO.setSubject(qualificationOB.getDegree());
        certificationsDTO.setLicense(qualificationOB.getLicensesub());
        certificationsDTO.setLocation(qualificationOB.getLocation());
        certificationViewList.add(certificationsDTO);
      }
    }
    return certificationViewList;
  }

  @Override
  public Boolean submitCertificate(String username, CertificationsDTO certificationsDTO)
      throws BusinessException, SystemException {

    employmentInformationDAO.submitCertification(username, certificationsDTO);
    return true;
  }

  @Override
  public List<CustodiesDTO> getAllCustodies(String username) {

    List<EhcmEmpAsset> custodyList = employmentInformationDAO.getAllCustodies(username);

    List<CustodiesDTO> custodyDTOList = new ArrayList<>();

    if (custodyList.size() > 0)
      custodyDTOList = mapCustodies(custodyList);

    return custodyDTOList;
  }

  /**
   * fill the custody details
   * 
   * @param custodyList
   * @return
   */
  private List<CustodiesDTO> mapCustodies(List<EhcmEmpAsset> custodyList) {

    List<CustodiesDTO> custodyDTOList = new ArrayList<CustodiesDTO>();
    for (EhcmEmpAsset custodyOB : custodyList) {
      CustodiesDTO custodyDTO = new CustodiesDTO();
      custodyDTO.setAssetName(custodyOB.getName());
      custodyDTO.setDateFrom(
          DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, custodyOB.getStartDate()));
      custodyDTO
          .setDateTo(DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, custodyOB.getEndDate()));
      custodyDTO.setDescription(custodyOB.getDescription());
      custodyDTOList.add(custodyDTO);
    }
    return custodyDTOList;
  }

  @Override
  public Boolean submitQualification(String username, QualificationsDTO qualificationsDTO)
      throws SystemException, BusinessException {

    employmentInformationDAO.submitQualification(username, qualificationsDTO);
    return true;
  }

  @Override
  public Boolean submitQualificationWithApproval(String username,
      QualificationsDTO qualificationsDTO) throws SystemException, BusinessException {

    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);

    jsonInString = utilityDAO.convertObjectTojsonString(qualificationsDTO);
    storeDataInTransaction(username, jsonInString, Constants.UPDATE_QUALIFICATION);

    GenericActivitiData storedData = storeDataInTransaction(username, jsonInString,
        Constants.UPDATE_QUALIFICATION);

    String lineManager = workflowUtilityService.getLineManagerByUserName(username);

    Map<String, Object> variablesMap = new HashMap<String, Object>();
    variablesMap.put(ActivitiConstants.TARGET_IDENTIFIER, storedData.getId());
    variablesMap.put("userName", username);
    variablesMap.put("taskTitle", "New Qualification");
    variablesMap.put("taskType", Constants.UPDATE_QUALIFICATION);
    variablesMap.put("lineManager", lineManager);
    variablesMap.put("emailAddress", employeeOB.getEmail());

    workflowUtilityService.startWorkflow(EMPLOYEE_INFORMATIONS_WORKFLOW_KEY, variablesMap);
    return true;
  }

  /**
   * Store the data into generic transaction table before it moves to final approval
   * 
   * @param jsonData
   *          to store in temporary table
   */
  private GenericActivitiData storeDataInTransaction(String userName, String jsoninString,
      String decisionType) {

    GenericActivitiData storedData = null;
    try {
      storedData = commonActivitiDAO.storeDataInTransaction(userName, jsoninString, decisionType);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return storedData;
  }

  @Override
  public Boolean submitCertificateWithApproval(String username, CertificationsDTO certificationsDTO)
      throws SystemException {

    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);

    jsonInString = utilityDAO.convertObjectTojsonString(certificationsDTO);
    GenericActivitiData storedData = storeDataInTransaction(username, jsonInString,
        Constants.UPDATE_CERTIFICATION);

    String lineManager = workflowUtilityService.getLineManagerByUserName(username);

    Map<String, Object> variablesMap = new HashMap<String, Object>();
    variablesMap.put(ActivitiConstants.TARGET_IDENTIFIER, storedData.getId());
    variablesMap.put("userName", username);
    variablesMap.put("taskTitle", "New Certification");
    variablesMap.put("taskType", Constants.UPDATE_CERTIFICATION);
    variablesMap.put("lineManager", lineManager);
    variablesMap.put("emailAddress", employeeOB.getEmail());

    workflowUtilityService.startWorkflow(EMPLOYEE_INFORMATIONS_WORKFLOW_KEY, variablesMap);
    return true;
  }

  @Override
  public List<ViewEmplInfoDTO> getCurrentEmploymentInfoByUserName(String userName)
      throws BusinessException, SystemException {

    List<EmploymentInfo> employmentInfoList = employmentInformationDAO
        .getCurrentEmploymentInfoByUserName(userName);
    return mapCurrentEmploymentInfo(employmentInfoList);
  }

  /**
   * Fill the current employment details
   * 
   * @param previousEmploymentList
   * @return
   */
  private List<ViewEmplInfoDTO> mapCurrentEmploymentInfo(List<EmploymentInfo> employmentInfoList) {

    List<ViewEmplInfoDTO> viewEmpInfoList = new ArrayList<ViewEmplInfoDTO>();
    for (EmploymentInfo currentEmployment : employmentInfoList) {
      ViewEmplInfoDTO viewEmplInfoDTO = new ViewEmplInfoDTO();
      viewEmplInfoDTO.setReason(currentEmployment.getChangereason());
      viewEmplInfoDTO.setDepartment(currentEmployment.getDepartmentName());
      viewEmplInfoDTO.setDecisionNo(currentEmployment.getDecisionNo());
      viewEmplInfoDTO.setGrade(currentEmployment.getGrade().getCommercialName());
      viewEmplInfoDTO.setJobNo(currentEmployment.getPosition().getJOBNo());
      viewEmplInfoDTO.setJobTitle(currentEmployment.getJobtitle());
      viewEmplInfoDTO.setStatus(currentEmployment.getAlertStatus());
      viewEmplInfoDTO
          .setStep(currentEmployment.getEhcmPayscaleline().getEhcmProgressionpt().getPoint());
      if (currentEmployment.getStartDate() != null)
        viewEmplInfoDTO.setStartDate(DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT,
            currentEmployment.getStartDate()));

      if (currentEmployment.getEndDate() != null)
        viewEmplInfoDTO.setEndDate(
            DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, currentEmployment.getEndDate()));
      viewEmpInfoList.add(viewEmplInfoDTO);
    }
    return viewEmpInfoList;
  }

  @Override
  public CertificationsDTO getCertificationsById(String recordId) {

    ehcmqualification certification = employmentInformationDAO.getQualificationById(recordId);

    CertificationsDTO certificationsDTO = new CertificationsDTO();

    certificationsDTO.setId(certification.getId());
    certificationsDTO.setSubject(certification.getDegree());
    certificationsDTO.setLicense(certification.getLicensesub());
    certificationsDTO.setLocation(certification.getLocation());

    if (certification.getStartDate() != null)
      certificationsDTO.setStartDate(
          DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, certification.getStartDate()));

    if (certification.getEndDate() != null)
      certificationsDTO.setEndDate(
          DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, certification.getEndDate()));

    if (certification.getExpirydate() != null)
      certificationsDTO.setExpiryDate(
          DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, certification.getExpirydate()));

    return certificationsDTO;
  }

  @Override
  public QualificationsDTO getQualificationById(String recordId) {

    ehcmqualification qualification = employmentInformationDAO.getQualificationById(recordId);

    QualificationsDTO qualificationDTO = new QualificationsDTO();

    qualificationDTO.setId(qualification.getId());
    qualificationDTO.setLocation(qualification.getLocation());
    qualificationDTO.setStartDate(
        DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, qualification.getStartDate()));
    qualificationDTO.setEndDate(
        DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, qualification.getEndDate()));
    // qualificationDTO.setCountry(country); //need to ask // where to fetch
    qualificationDTO.setExpiryDate(
        DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, qualification.getExpirydate()));

    qualificationDTO.setDegree(qualification.getDegree());
    qualificationDTO.setEducationLevel(qualification.getLookupEducationLevel().getEnglishName());
    qualificationDTO.setMajor(qualification.getLicensesub());

    return qualificationDTO;
  }

}
