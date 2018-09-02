package sa.elm.ob.hcm.dao.workflow;

import java.util.List;

import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

public interface WorkFlowDAO {
  /**
   * 
   * @param empUsername
   * @param empUserId
   * @return line manager Id
   */
  String getLineManagerIdByUserNameOrUserId(String empUsername, String empUserId)
      throws SystemException;

  /**
   * 
   * @param empUsername
   * @return department manager id
   */
  String getDepartmentManagerByUserName(String empUsername) throws SystemException;

  /**
   * Get role details for user
   * 
   * @param userName
   * @return role id
   */
  List<String> getRoleListByUsingUsername(String userName);

  /**
   * Top Manager Id for user
   * 
   * @param userName
   * @return
   */
  String getTopManagerByUsername(String userName) throws BusinessException, SystemException;

}
