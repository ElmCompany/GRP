package sa.elm.ob.hcm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.openbravo.activiti.ActivitiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.selfservice.dto.tasks.TaskDTO;
import sa.elm.ob.hcm.services.profile.EmployeeProfileService;
import sa.elm.ob.utility.util.Constants;

/**
 * Implementation of Activiti Workflow Operations
 * 
 */
@Service
public class ActivitiProcessImpl implements ActivitiProcess {

  @Autowired
  EmployeeProfileService employeeProfileService;

  @Autowired
  EmployeeProfileDAO employeeProfileDAO;

  /**
   * Starts the workflow process for the given process key
   */
  @Override
  public void startWorkFlowProcess(String processKey, Map<String, Object> variables) {

    final RuntimeService runtimeService = getRuntimeService();

    runtimeService.startProcessInstanceByKey(processKey, variables);
  }

  @Override
  public void completeProcessTask(String taskId, Map<String, Object> taskVariables) {

    final TaskService taskService = getProcessEngine().getTaskService();
    List<Task> tasks = taskService.createTaskQuery().taskId(taskId).orderByDueDate().desc().list();

    if (tasks.size() > 0) {
      taskService.complete(tasks.get(0).getId(), taskVariables);
    }

  }

  /**
   * Complete the task
   */
  @Override
  public void completeProcessTask(String userId, String taskId, Map<String, Object> taskVariables) {

    final TaskService taskService = getProcessEngine().getTaskService();

    List<Task> tasks = taskService.createTaskQuery().taskId(taskId)
        // .processVariableValueEquals(ActivitiConstants.TARGET_RECORD_ID,
        // receiptId).orderByDueDate()
        // .desc()
        .list();

    if (tasks.size() > 0) {
      taskService.complete(tasks.get(0).getId(), taskVariables);
    }

  }

  /**
   * Get the process engine instance [Activiti]
   * 
   * @return
   */
  private ProcessEngine getProcessEngine() {
    return ProcessEngines.getDefaultProcessEngine();
  }

  /**
   * Get the Run Time Service [Activiti]
   * 
   * @return
   */
  private RuntimeService getRuntimeService() {
    return getProcessEngine().getRuntimeService();
  }

  @Override
  public boolean completeReSubmitTransaction(String userId, String receiptId) {
    boolean complete = false;
    final TaskService taskService = getProcessEngine().getTaskService();
    List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId)
        .processVariableValueEquals(ActivitiConstants.TARGET_RECORD_ID, receiptId).orderByDueDate()
        .desc().list();

    if (tasks.size() > 0) {
      taskService.complete(tasks.get(0).getId());
      complete = true;
    }

    return complete;
  }

  @Override
  public List<TaskEntity> getTaskAssignedToUserByDocTypeAndRole(String userId, String roleId,
      String documentType) {

    final TaskService taskService = getProcessEngine().getTaskService();

    List<Task> tasksList = taskService.createTaskQuery().taskAssignee(userId)
        .processVariableValueEquals(Constants.ACTVITI_DOCUMENT_TYPE, documentType).orderByDueDate()
        .desc().list();
    // Filter the tasks which belong to this role Id
    List<TaskEntity> taskEntityList = filterTasksByRoleId(tasksList, roleId);
    return taskEntityList;
  }

  @Override
  public List<Task> getTaskAssignedToUser(String userId) {
    final TaskService taskService = getProcessEngine().getTaskService();

    List<Task> tasksList = new ArrayList<>();
    tasksList = taskService.createTaskQuery().taskAssignee(userId).orderByDueDate().desc().list();

    return tasksList;
  }

  @Override
  public Map<String, Object> getTaskDetails(String taskId) {
    final TaskService taskService = getProcessEngine().getTaskService();

    return taskService.getVariables(taskId);
  }

  @Override
  public List<TaskDTO> getTasksByUser(String username) {

    EhcmEmpPerInfo manger = employeeProfileDAO.getEmployeeProfileByUser(username);

    List<Task> tasks = getTaskAssignedToUser(manger.getId());
    List<TaskDTO> tasksDTO = new ArrayList<>();

    for (Task task : tasks) {
      Map<String, Object> variables = getTaskDetails(task.getId());
      TaskDTO taskDTO = new TaskDTO();
      taskDTO.setTaskId(task.getId());
      taskDTO.setSubject(variables.get(Constants.TASK_SUBJECT).toString());
      taskDTO.setRequester(variables.get(Constants.TASK_REQUESTER_NAME).toString());
      taskDTO.setLetterNo(variables.get(Constants.TASK_LETTER_NUMBER).toString());
      taskDTO.setRequestDate(variables.get(Constants.TAKS_REQUEST_DATE).toString());
      taskDTO.setStatus(variables.get(Constants.TASK_STATUS).toString());
      taskDTO.setIdentifierId(variables.get(ActivitiConstants.TARGET_IDENTIFIER).toString());
      tasksDTO.add(taskDTO);
    }
    return tasksDTO;
  }

  @Override
  public List<TaskDTO> getTasksByRole(String roleId) {

    List<Task> tasks = getTasksAssignedToRole(roleId);
    List<TaskDTO> tasksDTO = new ArrayList<>();

    for (Task task : tasks) {
      Map<String, Object> variables = getTaskDetails(task.getId());
      TaskDTO taskDTO = new TaskDTO();
      taskDTO.setTaskId(task.getId());
      taskDTO.setSubject(variables.get(Constants.TASK_SUBJECT).toString());
      taskDTO.setRequester(variables.get(Constants.TASK_REQUESTER_NAME).toString());
      taskDTO.setLetterNo(variables.get(Constants.TASK_LETTER_NUMBER).toString());
      taskDTO.setRequestDate(variables.get(Constants.TAKS_REQUEST_DATE).toString());
      taskDTO.setStatus(variables.get(Constants.TASK_STATUS).toString());
      taskDTO.setIdentifierId(variables.get(ActivitiConstants.TARGET_IDENTIFIER).toString());
      tasksDTO.add(taskDTO);
    }
    return tasksDTO;
  }

  @Override
  public void assignTaskToUserByRole(String fromUserId, String fromRoleId, String documentType,
      String toUserId, String toRoleId) {
    final TaskService taskService = getProcessEngine().getTaskService();

    List<TaskEntity> tasksList = getTaskAssignedToUserByDocTypeAndRole(fromUserId, fromRoleId,
        documentType);
    // Now loop through to assign to new user
    for (TaskEntity task : tasksList) {
      taskService.setVariable(task.getId(), Constants.ACTVITI_CANDIDATE_GROUP_EXPRESSIONS,
          toRoleId);
      task.setAssignee(toUserId);// Assign new user
      // task.setVariable(Constants.ACTVITI_CANDIDATE_GROUP_EXPRESSIONS, toRoleId); // Assign Role
      // to task variables
      taskService.saveTask(task);

    }
  }

  private List<TaskEntity> filterTasksByRoleId(List<Task> tasksList, String roleId) {
    final TaskService taskService = getProcessEngine().getTaskService();
    List<TaskEntity> filteredList = new ArrayList<TaskEntity>();
    for (Task task : tasksList) {
      TaskEntity taskEntity = (TaskEntity) task;
      String candidateGroups = (String) taskService.getVariable(task.getId(),
          Constants.ACTVITI_CANDIDATE_GROUP_EXPRESSIONS);
      if (null != candidateGroups && candidateGroups.trim().length() > 0) {
        String[] roles = candidateGroups.split(",");
        for (String role : roles) {
          if (role.trim().equals(roleId)) {
            filteredList.add(taskEntity);
          }
        }
      } else {// This else for the case when a user was assigned task irrespective of his role
        filteredList.add(taskEntity);
      }

    }

    return filteredList;
  }

  @Override
  public Task getLastTaskByReceiptId(String receiptId) {
    final TaskService taskService = getProcessEngine().getTaskService();
    List<Task> tasks = taskService.createTaskQuery()
        .processVariableValueEquals(ActivitiConstants.TARGET_RECORD_ID, receiptId).orderByTaskId()
        .desc().list();
    if (tasks.size() > 0) {
      return tasks.get(0);
    }
    return null;
  }

  @Override
  public String getCommaSeparatedRoles(String receiptId) {

    final TaskService taskService = getProcessEngine().getTaskService();
    TaskEntity task = (TaskEntity) getLastTaskByReceiptId(receiptId);

    String roles = "";

    if (null != task) {

      if (null != taskService.getVariable(task.getId(),
          Constants.ACTVITI_CANDIDATE_GROUP_EXPRESSIONS)) {
        roles = (String) taskService.getVariable(task.getId(),
            Constants.ACTVITI_CANDIDATE_GROUP_EXPRESSIONS);
      } else {
        String assignee = task.getAssignee();
        // NOw get the role of assignee
        // OBDal.getInstance().get
      }
    }
    return roles;
  }

  @Override
  public List<Task> getTasksAssignedToRole(String roleId) {
    final TaskService taskService = getProcessEngine().getTaskService();
    List<Task> tasks = new ArrayList<>();
    tasks = taskService.createTaskQuery().taskCandidateGroup(roleId).orderByTaskId().desc().list();

    return tasks;
  }

  @Override
  public void assignTaskToUser(String username, String taskId) {
    final TaskService taskService = getProcessEngine().getTaskService();
    taskService.setAssignee(taskId, username);

  }

  @Override
  public void setVariableToTask(String taskId, String variableName, Object value) {

    final TaskService taskService = getProcessEngine().getTaskService();
    taskService.setVariable(taskId, variableName, value);

  }

  @Override
  public void claimTask(String username, String taskId) {

    final TaskService taskService = getProcessEngine().getTaskService();
    taskService.claim(taskId, username);
  }

  @Override
  public void deleteTasksByRole(String roleId) {
    List<Task> tasks = getTasksAssignedToRole(roleId);
    List<String> tasksId = new ArrayList<>();

    for (Task task : tasks) {
      tasksId.add(task.getId());
    }

    final TaskService taskService = getProcessEngine().getTaskService();
    taskService.deleteTasks(tasksId, true);

  }

  @Override
  public void deleteTasksByUser(String username) {
    EhcmEmpPerInfo manger = employeeProfileDAO.getEmployeeProfileByUser(username);
    List<Task> tasks = getTaskAssignedToUser(manger.getId());
    List<String> tasksId = new ArrayList<>();

    for (Task task : tasks) {
      tasksId.add(task.getId());
    }

    final TaskService taskService = getProcessEngine().getTaskService();
    taskService.deleteTasks(tasksId, true);

  }
}
