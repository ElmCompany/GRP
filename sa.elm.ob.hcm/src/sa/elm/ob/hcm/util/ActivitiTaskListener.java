package sa.elm.ob.hcm.util;

import java.util.Map;
import java.util.Set;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

import sa.elm.ob.utility.util.Constants;

public class ActivitiTaskListener implements TaskListener {

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public void notify(DelegateTask delegateTask) {

    TaskEntity taskEntity = (TaskEntity) delegateTask;
    Set<Expression> candidateGroupSet = taskEntity.getTaskDefinition()
        .getCandidateGroupIdExpressions();
    Map variablesMap = taskEntity.getVariables();
    if (null != candidateGroupSet && candidateGroupSet.toString().trim().length() > 0) {
      variablesMap.put(Constants.ACTVITI_CANDIDATE_GROUP_EXPRESSIONS,
          candidateGroupSet.toString().replaceAll("\\[", "").replaceAll("\\]", ""));
      taskEntity.setVariables(variablesMap);
    }
  }

}
