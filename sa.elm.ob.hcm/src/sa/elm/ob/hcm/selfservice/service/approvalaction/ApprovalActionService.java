package sa.elm.ob.hcm.selfservice.service.approvalaction;

import java.util.List;

import sa.elm.ob.hcm.selfservice.dto.approvalaction.ApprovalActionDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

public interface ApprovalActionService {
  /**
   * Save Approval Action History Based on Decision Type
   * 
   * @param approvalActionDTO
   * @throws SystemException
   * @throws BusinessException
   */
  public void save(ApprovalActionDTO approvalActionDTO) throws SystemException, BusinessException;

  /**
   * Find list of actions performed on each decision
   * 
   * @param decisionRecordId
   * @return History Approval Action performed
   * @throws SystemException
   * @throws BusinessException
   */
  public List<ApprovalActionDTO> getActionHistoryByDecisionRecordId(String decisionRecordId)
      throws SystemException, BusinessException;

}
