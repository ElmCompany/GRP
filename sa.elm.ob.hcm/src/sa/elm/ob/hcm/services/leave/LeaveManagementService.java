package sa.elm.ob.hcm.services.leave;

import java.util.List;

import sa.elm.ob.hcm.dto.leave.LeaveRequestDTO;
import sa.elm.ob.hcm.dto.leave.RejoinLeaveRequestDTO;
import sa.elm.ob.hcm.dto.leave.ViewLeaveAccrualDTO;
import sa.elm.ob.hcm.dto.leave.ViewLeaveDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

/**
 * Leave Management Service Interface
 *
 */

public interface LeaveManagementService {

  /**
   * 
   * @param username
   * @return Leave Details
   */
  List<ViewLeaveDTO> viewLeaves(String username, String asOfDate);

  /**
   * @param username
   * @return Leave accruals Details
   */
  List<ViewLeaveAccrualDTO> viewLeavesAccrual(String username, String asOfDate)
      throws BusinessException, SystemException;

  /**
   * @param username
   * @param leaveRequestDTO
   * @return
   */
  Boolean submitLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO)
      throws SystemException, BusinessException;

  /**
   * 
   * @param username
   * @param leaveRequestDTO
   * @return
   * @throws SystemException
   * @throws BusinessException
   */
  Boolean submitLeaveRequestWithApprovalFlow(String username, LeaveRequestDTO leaveRequestDTO)
      throws SystemException, BusinessException;

  /**
   * @param username
   * @param leaveRequestDTO
   * @return
   */
  LeaveRequestDTO saveForLaterLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO);

  /**
   * Get All Original Decision Numbers belong to given username
   * 
   * @param username
   * @return
   */
  List<String> getAllOriginalDecisionNo(String username);

  /**
   * Retrieve Leave Request Info by original decision number
   * 
   * @param orginalDecNo
   * @return
   */
  LeaveRequestDTO getLeaveRequestByOriginalDecisionNo(String orginalDecNo);

  /**
   * Retrieve the Leave Request Info by User name
   * 
   * @param userName
   * @return
   */
  List<LeaveRequestDTO> getLeaveRequestByUserName(String userName);

  /**
   * Submit Cutoff leave request
   * 
   * @param username
   * @param leaveRequestDTO
   * @return
   */
  Boolean submitCutoffLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO)
      throws BusinessException, SystemException;

  /**
   * update Cutoff leave request
   *
   * @param username
   * @param leaveRequestDTO
   * @return
   */
  Boolean updateCutoffLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO)
      throws BusinessException, SystemException;

  /**
   * Submit Cutoff leave request with approval
   * 
   * @param username
   * @param leaveRequestDTO
   * @return
   */
  Boolean submitCutoffLeaveRequestWithApproval(String username, LeaveRequestDTO leaveRequestDTO)
      throws BusinessException, SystemException;

  // /**
  // * Update Cutoff leave request with approval
  // *
  // * @param username
  // * @param leaveRequestDTO
  // * @return
  // */
  // Boolean updateCutoffLeaveRequestWithApproval(String username, String orginalDecNo,
  // LeaveRequestDTO leaveRequestDTO) throws BusinessException, SystemException;

  /**
   * @param username
   * @param leaveRequestDTO
   * @return
   */
  LeaveRequestDTO saveForLaterCutoffLeaveRequest(String username, String orginalDecNo,
      LeaveRequestDTO leaveRequestDTO);

  /**
   * submit the cancel leave request
   * 
   * @param username
   * @param leaveRequestDTO
   * @return
   */
  Boolean submitCancelLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO)
      throws BusinessException, SystemException;

  /**
   * @param username
   * @param leaveRequestDTO
   * @return
   */
  RejoinLeaveRequestDTO submitRejoinLeaveRequest(String username, String orginalDecNo,
      RejoinLeaveRequestDTO leaveRequestDTO);

  /**
   * Update the leave request by using original decision number and leave details
   * 
   * @param username
   * @param leaveRequestDTO
   * @return
   * @throws SystemException
   * @throws BusinessException
   */

  Boolean updateLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO)
      throws SystemException, BusinessException;

  // /**
  // * Update the leave request by using original decision number and leave details with approval
  // workflow
  // * @param username
  // * @param leaveRequestDTO
  // * @return
  // * @throws SystemException
  // * @throws BusinessException
  // */
  // Boolean updateLeaveRequestWithApprovalFlow(String username, LeaveRequestDTO leaveRequestDTO)
  // throws SystemException, BusinessException;
  /**
   * create the extended leave request
   * 
   * @param username
   * @param leaveRequestDTO
   * @return
   * @throws SystemException
   * @throws BusinessException
   */
  Boolean createExtendLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO)
      throws SystemException, BusinessException;

  /**
   * create the extended leave request with approval flow
   * 
   * @param username
   * @param leaveRequestDTO
   * @return
   * @throws SystemException
   * @throws BusinessException
   */
  Boolean createExtendLeaveRequestWithApproval(String username, LeaveRequestDTO leaveRequestDTO)
      throws SystemException, BusinessException;

  /**
   * update the extended leave request
   * 
   * @param username
   * @param leaveRequestDTO
   * @return
   * @throws SystemException
   * @throws BusinessException
   */
  Boolean updateExtendLeaveRequest(String username, LeaveRequestDTO leaveRequestDTO)
      throws SystemException, BusinessException;
  // /**
  // * update the extended leave request with approval flow
  // * @param username
  // * @param leaveRequestDTO
  // * @return
  // * @throws SystemException
  // * @throws BusinessException
  // */
  // Boolean updateExtendLeaveRequestWithApproval(String username, LeaveRequestDTO leaveRequestDTO)
  // throws SystemException, BusinessException;

  /**
   * submit the cancel leave request with approval
   * 
   * @param username
   * @param leaveRequestDTO
   * @return
   */
  Boolean submitCancelLeaveRequestWithApproval(String username, LeaveRequestDTO leaveRequestDTO)
      throws BusinessException, SystemException;

  /**
   * @param username
   * @param origDecNo
   * @param leaveRequestDTO
   * @return
   */
  Boolean submitRejoinLeaveRequestWithApproval(String username,
      RejoinLeaveRequestDTO leaveRequestDTO);
}
