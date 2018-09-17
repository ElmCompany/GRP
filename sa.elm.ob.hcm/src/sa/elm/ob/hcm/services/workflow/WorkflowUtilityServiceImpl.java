package sa.elm.ob.hcm.services.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.dao.workflow.WorkFlowDAO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.util.ActivitiProcess;

@Service
public class WorkflowUtilityServiceImpl implements WorkflowUtilityService {

  @Autowired
  WorkFlowDAO workFlowDAO;

  @Autowired
  EmployeeProfileDAO employeeProfileDAO;

  @Autowired
  ActivitiProcess activitiProcess;

  @Override
  public String getLineManagerByUserName(String empUsername) throws SystemException {

    String lineManagerId = workFlowDAO.getLineManagerIdByUserNameOrUserId(empUsername, null);
    return lineManagerId;
  }

  @Override
  public String getDepartmentManager(String empUsername) throws SystemException {

    String departmentManagerId = workFlowDAO.getDepartmentManagerByUserName(empUsername);
    return departmentManagerId;
  }

  @Override
  public void startWorkflow(String processKey, Map<String, Object> variablesMap) {
    activitiProcess.startWorkFlowProcess(processKey, variablesMap);
  }

  @Override
  public void approveTask(String taskId) {
    Map<String, Object> variablesMap = new HashMap<String, Object>();
    variablesMap.put("requestApproved", true);
    activitiProcess.completeProcessTask(taskId, variablesMap);
  }

  @Override
  public void rejectTask(String taskId) {
    Map<String, Object> variablesMap = new HashMap<String, Object>();
    variablesMap.put("requestApproved", false);
    activitiProcess.completeProcessTask(taskId, variablesMap);
  }

  @Override
  public String getLineManagerByUserId(String empUserId) throws SystemException {

    String lineManagerId = workFlowDAO.getLineManagerIdByUserNameOrUserId(null, empUserId);
    return lineManagerId;
  }

  @Override
  public List<String> getRoleListByUsingUsername(String userName) {

    return workFlowDAO.getRoleListByUsingUsername(userName);
  }

  @Override
  public String getTopManagerByUsername(String userName) throws SystemException, BusinessException {

    String topManagerId = workFlowDAO.getTopManagerByUsername(userName);
    return topManagerId;
  }

  @Override
  public String getEmpId(String username) {
    return employeeProfileDAO.getEmployeeProfileByUser(username).getId();
  }

}
