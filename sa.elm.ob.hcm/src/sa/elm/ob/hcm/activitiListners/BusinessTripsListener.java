package sa.elm.ob.hcm.activitiListners;

import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.openbravo.activiti.ActivitiConstants;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.dao.businessTrip.BusinessTripDAO;
import sa.elm.ob.utility.util.Constants;

@Component
public class BusinessTripsListener implements TaskListener, ApplicationContextAware {

  private static ApplicationContext applicationContext;

  @SuppressWarnings("static-access")
  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    this.applicationContext = context;

  }

  @Override
  public void notify(DelegateTask delegateTask) {

    BusinessTripDAO businessTripDAO = applicationContext.getBean(BusinessTripDAO.class);

    TaskEntity taskEntity = (TaskEntity) delegateTask;

    @SuppressWarnings("rawtypes")
    Map variablesMap = taskEntity.getVariables();
    boolean approved = (boolean) variablesMap.get("requestApproved");
    if (approved) {
      String businessMissionDecNo = (String) variablesMap.get(ActivitiConstants.TARGET_IDENTIFIER);
      EHCMEmpBusinessMission businessMissionOB = businessTripDAO
          .getBusinessTripByDecisionNo(businessMissionDecNo).get(0);
      String decisionType = variablesMap.get(Constants.TASK_TYPE).toString();

      businessTripDAO.createBusinessMissionSummary(businessMissionOB, decisionType);

    }

  }
}
