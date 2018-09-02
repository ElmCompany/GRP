package sa.elm.ob.hcm.activitiListners;

import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.services.workflow.WorkflowUtilityService;
import sa.elm.ob.hcm.util.ActivitiProcess;
import sa.elm.ob.hcm.util.ActivitiProcessImpl;

@Component
public class NextSupervisorListener implements TaskListener, ApplicationContextAware {

  private static ApplicationContext applicationContext;

  @SuppressWarnings("static-access")
  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    this.applicationContext = context;

  }

  @Override
  public void notify(DelegateTask delegateTask) {

    WorkflowUtilityService workflowUtilityService = applicationContext
        .getBean(WorkflowUtilityService.class);

    ActivitiProcess activitiProcess = new ActivitiProcessImpl();

    TaskEntity taskEntity = (TaskEntity) delegateTask;

    @SuppressWarnings("rawtypes")
    Map variablesMap = taskEntity.getVariables();

    String taskId = taskEntity.getId();
    String assignee = (String) variablesMap.get("lineManager");
    String lineManager = null;
    try {
      lineManager = workflowUtilityService.getLineManagerByUserId(assignee);
      activitiProcess.setVariableToTask(taskId, "lineManager", lineManager);
    } catch (SystemException e1) {
      e1.printStackTrace();
    }

  }
}
