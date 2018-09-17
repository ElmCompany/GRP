package sa.elm.ob.hcm.selfservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import sa.elm.ob.hcm.selfservice.dto.tasks.TaskDTO;
import sa.elm.ob.hcm.services.selfservicetransactions.SelfServiceTransactionService;
import sa.elm.ob.hcm.services.workflow.WorkflowUtilityService;
import sa.elm.ob.hcm.util.ActivitiProcess;

@RestController
@RequestMapping("openerp/hr/tasks")
public class TasksController {

  @Autowired
  private SelfServiceTransactionService selfServiceTransaction;

  @Autowired
  private WorkflowUtilityService workflowUtilityService;

  @Autowired
  private ActivitiProcess activitiProcess;

  @RequestMapping(value = "/user/{username}", method = RequestMethod.GET)
  public ResponseEntity<List<TaskDTO>> getTaskAssignedToUser(@PathVariable String username) {

    List<TaskDTO> tasks = activitiProcess.getTasksByUser(username);

    return new ResponseEntity<List<TaskDTO>>(tasks, HttpStatus.OK);
  }

  @RequestMapping(value = "/taskId/{taskId}", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getTaskDetails(@PathVariable("taskId") String taskId) {

    Map<String, Object> taskDetails = activitiProcess.getTaskDetails(taskId);

    return new ResponseEntity<Map<String, Object>>(taskDetails, HttpStatus.OK);
  }

  @RequestMapping(value = "/approve/taskId/{taskId}", method = RequestMethod.PUT)
  public ResponseEntity<Boolean> approveTask(@PathVariable("taskId") String taskId) {
    workflowUtilityService.approveTask(taskId);
    return new ResponseEntity<Boolean>(true, HttpStatus.OK);
  }

  @RequestMapping(value = "/reject/taskId/{taskId}", method = RequestMethod.PUT)
  public ResponseEntity<Boolean> rejectTask(@PathVariable("taskId") String taskId) {
    workflowUtilityService.rejectTask(taskId);
    return new ResponseEntity<Boolean>(true, HttpStatus.OK);
  }

  @RequestMapping(value = "/role/{roleId}", method = RequestMethod.GET)
  public ResponseEntity<List<TaskDTO>> getTasksAssignedToRole(@PathVariable String roleId) {

    List<TaskDTO> tasks = activitiProcess.getTasksByRole(roleId);

    return new ResponseEntity<List<TaskDTO>>(tasks, HttpStatus.OK);
  }

  @RequestMapping(value = "/username/{username}/taskId/{taskId}", method = RequestMethod.PUT)
  public ResponseEntity<Boolean> claimTask(@PathVariable("username") String username,
      @PathVariable("taskId") String taskId) {

    String empId = workflowUtilityService.getEmpId(username);
    activitiProcess.claimTask(empId, taskId);
    return new ResponseEntity<Boolean>(true, HttpStatus.OK);
  }

  @RequestMapping(value = "/details/{recordId}", method = RequestMethod.GET)
  public ResponseEntity<Object> getRequestDetails(@PathVariable("recordId") String recordId) {

    Object requestData = selfServiceTransaction.getRequestDetails(recordId);

    return new ResponseEntity<Object>(requestData, HttpStatus.OK);
  }

  @RequestMapping(value = "/roles/{username}", method = RequestMethod.GET)
  public ResponseEntity<List<String>> getUserRoles(@PathVariable String username) {

    List<String> roles = workflowUtilityService.getRoleListByUsingUsername(username);

    return new ResponseEntity<List<String>>(roles, HttpStatus.OK);
  }

  @RequestMapping(value = "/role/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Boolean> deleteTasksByRole(@PathVariable String id) {
    activitiProcess.deleteTasksByRole(id);
    return new ResponseEntity<Boolean>(true, HttpStatus.OK);
  }

  @RequestMapping(value = "/user/{username}", method = RequestMethod.DELETE)
  public ResponseEntity<Boolean> deleteTasksByUsername(@PathVariable String username) {
    activitiProcess.deleteTasksByUser(username);
    return new ResponseEntity<Boolean>(true, HttpStatus.OK);
  }

}
