package sa.elm.ob.hcm.services.profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbravo.activiti.ActivitiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sa.elm.ob.hcm.EhcmDependents;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.GenericActivitiData;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.dao.activiti.CommonActivitiDAO;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileUpdateDAO;
import sa.elm.ob.hcm.dto.profile.AddressInformationDTO;
import sa.elm.ob.hcm.dto.profile.DependentInformationDTO;
import sa.elm.ob.hcm.dto.profile.EmployeeAdditionalInformationDTO;
import sa.elm.ob.hcm.dto.profile.PersonalInformationDTO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.services.workflow.WorkflowUtilityService;
import sa.elm.ob.hcm.util.MessageKeys;
import sa.elm.ob.hcm.util.UtilityDAO;

@Service("employeeProfileUpdateService")
public class EmployeeProfileUpdateServiceImpl implements EmployeeProfileUpdateService {
  private final String UPDATE_PROFILE_WORKFLOW_KEY = "updateProfileWorkflow";
  @Autowired
  private EmployeeProfileUpdateDAO employeeProfileUpdateDAO;

  @Autowired
  private EmployeeProfileDAO employeeProfileDAO;

  @Autowired
  private CommonActivitiDAO commonActivitiDAO;

  @Autowired
  private WorkflowUtilityService workflowUtilityService;

  private String jsonInString = "";

  UtilityDAO utilityDAO = new UtilityDAO();

  @Override
  public Boolean updateEmployeeProfile(String userName,
      PersonalInformationDTO personalInformationDTO, AddressInformationDTO addressInformationDTO,
      List<DependentInformationDTO> ehcmDependentList) throws BusinessException, SystemException {
    EhcmEmpPerInfo employeeOB = checkEmployee(userName);
    employeeProfileUpdateDAO.updateEmployeeProfileByUser(employeeOB, personalInformationDTO);
    employeeProfileUpdateDAO.updateEmployeeAddress(employeeOB, addressInformationDTO);
    for (DependentInformationDTO empDep : ehcmDependentList) {
      employeeProfileUpdateDAO.updateEmployeeDependent(employeeOB, empDep);
    }

    return true;
  }

  @Override
  public Boolean updateEmployeeDependent(String userName, DependentInformationDTO ehcmDependent)
      throws BusinessException, SystemException {

    EhcmEmpPerInfo employeeOB = checkEmployee(userName);
    employeeProfileUpdateDAO.updateEmployeeDependent(employeeOB, ehcmDependent);

    return true;

  }

  @Override
  public Boolean updateEmployeeAddress(String userName, AddressInformationDTO addressInformationDTO)
      throws BusinessException, SystemException {

    EhcmEmpPerInfo employeeOB = checkEmployee(userName);
    employeeProfileUpdateDAO.updateEmployeeAddress(employeeOB, addressInformationDTO);

    return true;
  }

  @Override
  public Boolean updatePersonalInformation(String userName,
      PersonalInformationDTO personalInformationDTO) throws BusinessException, SystemException {

    EhcmEmpPerInfo employeeOB = checkEmployee(userName);
    employeeProfileUpdateDAO.updateEmployeeProfileByUser(employeeOB, personalInformationDTO);

    return true;
  }

  @Override
  public EhcmEmpPerInfo checkEmployee(String userName) throws BusinessException {
    final String userLang = SecurityUtils.getUserLanguage();
    EhcmEmpPerInfo employeePersonalInfo = employeeProfileDAO.getEmployeeProfileByUser(userName);
    if (employeePersonalInfo == null) {
      throw new BusinessException(
          Resource.getProperty(MessageKeys.EMPLOYEE_NOT_AVAILABLE, userLang));
    }
    return employeePersonalInfo;
  }

  @Override
  public Boolean addDependent(String userName, DependentInformationDTO dependentInformationDTO)
      throws BusinessException, SystemException {

    checkEmployee(userName);
    employeeProfileUpdateDAO.addDependent(userName, dependentInformationDTO);

    return true;
  }

  @Override
  public Boolean removeDependent(String userName, String dependentId)
      throws BusinessException, SystemException {

    employeeProfileUpdateDAO.removeDependent(userName, dependentId);

    return true;
  }

  @Override
  public Boolean updateContactInformation(String userName,
      EmployeeAdditionalInformationDTO additionalInformationDTO)
      throws BusinessException, SystemException {

    checkEmployee(userName);
    employeeProfileUpdateDAO.updateContactInformation(userName, additionalInformationDTO);

    return true;
  }

  @Override
  public EhcmDependents getDependentByNationalId(String userName, String NationalID)
      throws SystemException, BusinessException {

    EhcmDependents ehcmDependents = employeeProfileUpdateDAO.findDependentByNationalId(userName,
        NationalID);
    return ehcmDependents;
  }

  @Override
  public Boolean updateProfilePhoto(String userName, String photoBytes)
      throws SystemException, BusinessException {

    employeeProfileUpdateDAO.updateProfilePhoto(userName, photoBytes);

    return true;
  }

  @Override
  public Boolean updateEmployeeProfileWithWorkflow(String userName,
      PersonalInformationDTO personalInformationDTO, AddressInformationDTO addressInformationDTO,
      DependentInformationDTO dependentInformationDTO) throws BusinessException, SystemException {

    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(userName);

    jsonInString = utilityDAO.convertObjectTojsonString(personalInformationDTO);
    GenericActivitiData storedData = storeDataInTransaction(userName, jsonInString,
        Constants.UPDATE_PROFILE);

    Map<String, Object> variablesMap = new HashMap<String, Object>();
    variablesMap.put(ActivitiConstants.TARGET_IDENTIFIER, storedData.getId());
    variablesMap.put("userName", SecurityUtils.loggedInUserName());
    variablesMap.put("taskTitle", "Update Profile");
    variablesMap.put("taskType", Constants.UPDATE_PROFILE);
    variablesMap.put("emailAddress", employeeOB.getEmail());

    workflowUtilityService.startWorkflow(UPDATE_PROFILE_WORKFLOW_KEY, variablesMap);

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
  public Boolean updateEmployeeDependentWithWorkFlow(String userName,
      DependentInformationDTO dependentInformationDTO) throws BusinessException, SystemException {

    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(userName);

    jsonInString = utilityDAO.convertObjectTojsonString(dependentInformationDTO);
    GenericActivitiData storedData = storeDataInTransaction(userName, jsonInString,
        Constants.UPDATE_DEPENDENT);

    Map<String, Object> variablesMap = new HashMap<String, Object>();
    variablesMap.put(ActivitiConstants.TARGET_IDENTIFIER, storedData.getId());
    variablesMap.put("userName", SecurityUtils.loggedInUserName());
    variablesMap.put("taskTitle", "Update Employee Dependent");
    variablesMap.put("taskType", Constants.UPDATE_DEPENDENT);
    variablesMap.put("emailAddress", employeeOB.getEmail());

    workflowUtilityService.startWorkflow(UPDATE_PROFILE_WORKFLOW_KEY, variablesMap);

    return true;
  }

  @Override
  public Boolean updatePersonalInformationWithWorkFlow(String userName,
      PersonalInformationDTO personalInformationDTO) throws BusinessException, SystemException {

    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(userName);

    jsonInString = utilityDAO.convertObjectTojsonString(personalInformationDTO);
    GenericActivitiData storedData = storeDataInTransaction(userName, jsonInString,
        Constants.UPDATE_PERSONAL_INFORMATION);

    Map<String, Object> variablesMap = new HashMap<String, Object>();
    variablesMap.put(ActivitiConstants.TARGET_IDENTIFIER, storedData.getId());
    variablesMap.put("userName", SecurityUtils.loggedInUserName());
    variablesMap.put("taskTitle", "Update Personal Information");
    variablesMap.put("taskType", Constants.UPDATE_PERSONAL_INFORMATION);
    variablesMap.put("emailAddress", employeeOB.getEmail());

    workflowUtilityService.startWorkflow(UPDATE_PROFILE_WORKFLOW_KEY, variablesMap);

    return true;
  }

  @Override
  public Boolean updateEmployeeAddressWithWorkFlow(String userName,
      AddressInformationDTO addressInformationDTO) throws BusinessException, SystemException {

    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(userName);

    jsonInString = utilityDAO.convertObjectTojsonString(addressInformationDTO);

    GenericActivitiData storedData = storeDataInTransaction(userName, jsonInString,
        Constants.UPDATE_ADDRESS);

    Map<String, Object> variablesMap = new HashMap<String, Object>();
    variablesMap.put(ActivitiConstants.TARGET_IDENTIFIER, storedData.getId());
    variablesMap.put("userName", userName);
    variablesMap.put("taskTitle", "Update Address");
    variablesMap.put("taskType", Constants.UPDATE_ADDRESS);
    variablesMap.put("emailAddress", employeeOB.getEmail());

    workflowUtilityService.startWorkflow(UPDATE_PROFILE_WORKFLOW_KEY, variablesMap);

    return true;
  }

}
