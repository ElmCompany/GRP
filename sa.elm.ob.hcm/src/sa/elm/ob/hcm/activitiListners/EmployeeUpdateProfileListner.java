package sa.elm.ob.hcm.activitiListners;

import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.activiti.ActivitiConstants;
import org.openbravo.base.exception.OBException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import sa.elm.ob.hcm.GenericActivitiData;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.dao.activiti.SelfServiceTransactionDAO;
import sa.elm.ob.hcm.dto.profile.AddressInformationDTO;
import sa.elm.ob.hcm.dto.profile.DependentInformationDTO;
import sa.elm.ob.hcm.dto.profile.EmployeeAdditionalInformationDTO;
import sa.elm.ob.hcm.dto.profile.PersonalInformationDTO;
import sa.elm.ob.hcm.services.profile.EmployeeProfileUpdateService;

@Component
public class EmployeeUpdateProfileListner implements TaskListener, ApplicationContextAware {

  private static ApplicationContext applicationContext;

  @SuppressWarnings("static-access")
  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    this.applicationContext = context;

  }

  @Override
  public void notify(DelegateTask delegateTask) {

    SelfServiceTransactionDAO commonActivitiDAO = applicationContext.getBean(SelfServiceTransactionDAO.class);

    TaskEntity taskEntity = (TaskEntity) delegateTask;
    @SuppressWarnings("rawtypes")
    Map variablesMap = taskEntity.getVariables();
    boolean approved = (boolean) variablesMap.get("requestApproved");
    if (approved) {
      GenericActivitiData genericActivtiData = commonActivitiDAO
          .findTransactionRecord(variablesMap.get(ActivitiConstants.TARGET_IDENTIFIER).toString());
      moveTransactionRecordToActualTable(
          variablesMap.get(sa.elm.ob.utility.util.Constants.TASK_REQUESTER_USERNAME).toString(),
          genericActivtiData);
    }

  }

  /**
   * 
   * @param userName
   * @param genericActivtiData
   */
  public void moveTransactionRecordToActualTable(String userName,
      GenericActivitiData genericActivtiData) {

    EmployeeProfileUpdateService employeeProfileUpdateService = applicationContext
        .getBean(EmployeeProfileUpdateService.class);

    JSONObject jsonObject = null;
    ObjectMapper mapper = new ObjectMapper();
    // TODO Auto-generated method stub
    try {
      if (genericActivtiData != null)
        jsonObject = new JSONObject(new String(genericActivtiData.getTemporaryData()));
      if (genericActivtiData != null
          && genericActivtiData.getTypeOfActiviti().equals(Constants.UPDATE_ADDRESS)) {

        AddressInformationDTO addressInformationDTO = mapper.readValue(jsonObject.toString(),
            AddressInformationDTO.class);
        // move data from transaction table to main table
        employeeProfileUpdateService.updateEmployeeAddress(userName, addressInformationDTO);

      } else if (genericActivtiData != null
          && genericActivtiData.getTypeOfActiviti().equals(Constants.UPDATE_PERSONAL_INFORMATION)) {

        PersonalInformationDTO personalInformationDTO = mapper.readValue(jsonObject.toString(),
            PersonalInformationDTO.class);
        // move data from transaction table to main table
        employeeProfileUpdateService.updatePersonalInformation(userName, personalInformationDTO);

      } else if (genericActivtiData != null
          && genericActivtiData.getTypeOfActiviti().equals(Constants.UPDATE_DEPENDENT)) {

        DependentInformationDTO dependentInformationDTO = mapper.readValue(jsonObject.toString(),
            DependentInformationDTO.class);
        // move data from transaction table to main table
        employeeProfileUpdateService.updateEmployeeDependent(userName, dependentInformationDTO);

      } else if (genericActivtiData != null
          && genericActivtiData.getTypeOfActiviti().equals(Constants.UPDATE_CONTACT_INFORMATION)) {

        EmployeeAdditionalInformationDTO contactInfoDTO = mapper.readValue(jsonObject.toString(),
            EmployeeAdditionalInformationDTO.class);
        employeeProfileUpdateService.updateContactInformation(userName, contactInfoDTO);

      } else if (genericActivtiData != null
          && genericActivtiData.getTypeOfActiviti().equals(Constants.ADD_DEPENDENT)) {

        DependentInformationDTO dependentInformationDTO = mapper.readValue(jsonObject.toString(),
            DependentInformationDTO.class);
        // move data from transaction table to main table
        employeeProfileUpdateService.addDependent(userName, dependentInformationDTO);

      } else if (genericActivtiData != null
          && genericActivtiData.getTypeOfActiviti().equals(Constants.REMOVE_DEPENDENT)) {

        DependentInformationDTO dependent = mapper.readValue(jsonObject.toString(),
            DependentInformationDTO.class);
        // move data from transaction table to main table
        employeeProfileUpdateService.removeDependent(userName, dependent.getNationalId());
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(e.getMessage());
    }

  }

}
