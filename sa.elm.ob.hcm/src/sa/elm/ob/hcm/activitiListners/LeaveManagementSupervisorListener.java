package sa.elm.ob.hcm.activitiListners;

import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.springframework.stereotype.Component;

import sa.elm.ob.hcm.util.ActivitiProcess;
import sa.elm.ob.hcm.util.ActivitiProcessImpl;

@Component
public class LeaveManagementSupervisorListener implements TaskListener {

  @Override
  public void notify(DelegateTask delegateTask) {

    ActivitiProcess activitiProcess = new ActivitiProcessImpl();

    TaskEntity taskEntity = (TaskEntity) delegateTask;
    String taskId = taskEntity.getId();
    String assignee = taskEntity.getAssignee();

    @SuppressWarnings("rawtypes")
    Map variablesMap = taskEntity.getVariables();
    String departmentMangager = (String) variablesMap.get("departmentManager");

    if (assignee.equals(departmentMangager)) {
      activitiProcess.setVariableToTask(taskId, "isDepManager", true);
    } else {
      activitiProcess.setVariableToTask(taskId, "isDepManager", false);
    }

  }
}
