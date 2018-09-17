package sa.elm.ob.hcm.util;

import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;

import sa.elm.ob.hcm.selfservice.dto.tasks.TaskDTO;

/**
 * This is the interface for defining activi workflow engine processes
 *
 */
public interface ActivitiProcess {
  /**
   * Start a new workflow process
   * 
   * @param processKey
   * @param variables
   */
  void startWorkFlowProcess(String processKey, Map<String, Object> variables);

  /**
   * Complete a task using given task id
   * 
   * @param taskId
   */
  void completeProcessTask(String userId, String receiptId, Map<String, Object> taskVariables);

  /**
   * Complete a task using given task id
   * 
   * @param taskId
   */
  public void completeProcessTask(String receiptId, Map<String, Object> taskVariables);

  /**
   * Check if the transaction is resubmitted
   * 
   * @param userId
   * @return
   */
  boolean completeReSubmitTransaction(String userId, String receiptId);

  /**
   * This method returns list of tasks assigned to user again given role and document type
   * 
   * @param userId
   * @param roleId
   * @param documentType
   * @return
   */
  List<TaskEntity> getTaskAssignedToUserByDocTypeAndRole(String userId, String roleId,
      String documentType);

  /**
   * Assigns the tasks from to user to from user [using document type and role id as parameters]
   * 
   * @param fromUserId
   * @param fromRoleId
   * @param documentType
   * @param toUserId
   * @param toRoleId
   */
  void assignTaskToUserByRole(String fromUserId, String fromRoleId, String documentType,
      String toUserId, String toRoleId);

  /**
   * Get the last task for the receipt id
   * 
   * @param receiptId
   * @return
   */
  Task getLastTaskByReceiptId(String receiptId);

  /**
   * Get the roles associated with task approval
   * 
   * @param receiptId
   * @return
   */
  String getCommaSeparatedRoles(String receiptId);

  /**
   * @param username
   * @return
   */
  List<Task> getTaskAssignedToUser(String username);

  /**
   * @param taskId
   * @return
   */
  Map<String, Object> getTaskDetails(String taskId);

  /**
   * @param roleId
   * @return
   */
  List<Task> getTasksAssignedToRole(String roleId);

  /**
   * @param username
   * @param taskId
   */
  void assignTaskToUser(String username, String taskId);

  /**
   * @param username
   * @param taskId
   */
  void claimTask(String username, String taskId);

  /**
   * @param taskId
   * @param variableName
   * @param value
   */
  void setVariableToTask(String taskId, String variableName, Object value);

  /**
   * @param username
   * @return
   */
  List<TaskDTO> getTasksByUser(String username);

  /**
   * @param roleId
   * @return
   */
  List<TaskDTO> getTasksByRole(String roleId);

  void deleteTasksByRole(String roleId);

  void deleteTasksByUser(String username);

}
