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
import sa.elm.ob.hcm.dto.employment.CertificationsDTO;
import sa.elm.ob.hcm.dto.employment.QualificationsDTO;
import sa.elm.ob.hcm.services.employment.EmploymentInformationService;

@Component
public class EmploymentInformationsListner implements TaskListener, ApplicationContextAware {

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

    EmploymentInformationService employmentInformationService = (EmploymentInformationService) applicationContext
        .getBean("employmentInformationService");

    JSONObject jsonObject = null;
    ObjectMapper mapper = new ObjectMapper();

    try {
      if (genericActivtiData != null)
        jsonObject = new JSONObject(new String(genericActivtiData.getTemporaryData()));
      if (genericActivtiData != null
          && genericActivtiData.getTypeOfActiviti().equals(Constants.UPDATE_CERTIFICATION)) {
        if (genericActivtiData.getTemporaryData() != null) {
          CertificationsDTO certification = mapper.readValue(jsonObject.toString(),
              CertificationsDTO.class);

          // move data from transaction table to main table
          employmentInformationService.submitCertificate(userName, certification);
        }
      } else if (genericActivtiData != null
          && genericActivtiData.getTypeOfActiviti().equals(Constants.UPDATE_QUALIFICATION)) {
        QualificationsDTO qualification = mapper.readValue(jsonObject.toString(),
            QualificationsDTO.class);

        // move data from transaction table to main table
        employmentInformationService.submitQualification(userName, qualification);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(e.getMessage());
    }

  }

}
