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

@Component
public class SupervisorListener implements TaskListener, ApplicationContextAware {

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

    ActivitiProcess activitiProcess = applicationContext.getBean(ActivitiProcess.class);

    TaskEntity taskEntity = (TaskEntity) delegateTask;

    String taskId = taskEntity.getId();

    @SuppressWarnings("rawtypes")
    Map variablesMap = taskEntity.getVariables();
    String departmentMangager = (String) variablesMap.get("departmentManager");
    String nextAssignee = (String) variablesMap.get("lineManager");
    String assignee = taskEntity.getAssignee();

    String lineManager = null;
    try {
      if (nextAssignee.equals(departmentMangager)) {
        activitiProcess.setVariableToTask(taskId, "isDepManager", true);
      } else {
        activitiProcess.setVariableToTask(taskId, "isDepManager", false);
        lineManager = workflowUtilityService.getLineManagerByUserId(nextAssignee);
        activitiProcess.setVariableToTask(taskId, "lineManager", lineManager);
      }
    } catch (SystemException e1) {
      e1.printStackTrace();
    }

  }
}
