package sa.elm.ob.hcm.services.workflow;

import java.util.List;
import java.util.Map;

import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

public interface WorkflowUtilityService {
  /**
   * 
   * @param empUsername
   * @return employee manager id (32 bit string)
   */
  public String getLineManagerByUserName(String empUsername) throws SystemException;

  /**
   * 
   * @param empUserId(32
   *          bit string)
   * @return employee manager id (32 bit string)
   */
  public String getLineManagerByUserId(String empUserId) throws SystemException;

  /**
   * 
   * @param empUsername
   * @return employee department manager id (32 bit string)
   */

  public String getDepartmentManager(String empUsername) throws SystemException;

  /**
   * Get Role Details for User
   * 
   * @param userName
   * @return Rold id
   */
  public List<String> getRoleListByUsingUsername(String userName);

  /**
   * Get top level manager id by using username
   * 
   * @param userName
   * @return employee top manager(32 bit id)
   * @throws SystemException
   */
  public String getTopManagerByUsername(String userName) throws SystemException, BusinessException;

  /**
   * @param processKey
   * @param variablesMap
   */
  public void startWorkflow(String processKey, Map<String, Object> variablesMap);

  /**
   * @param taskId
   */
  public void approveTask(String taskId);

  /**
   * @param taskId
   */
  public void rejectTask(String taskId);

  /**
   * @param username
   * @return
   */
  public String getEmpId(String username);

}
