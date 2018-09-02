package sa.elm.ob.hcm.selfservice.service.approvalaction;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sa.elm.ob.hcm.EHCMApiActionHistory;
import sa.elm.ob.hcm.selfservice.dao.approvalaction.ApprovalActionDAO;
import sa.elm.ob.hcm.selfservice.dto.approvalaction.ApprovalActionDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

@Service
public class ApprovalActionServiceImpl implements ApprovalActionService {
  @Autowired
  private ApprovalActionDAO approvalActionDAO;

  @Override
  public void save(ApprovalActionDTO approvalActionDTO) throws SystemException, BusinessException {
    approvalActionDAO.save(approvalActionDTO);
  }

  @Override
  public List<ApprovalActionDTO> getActionHistoryByDecisionRecordId(String decisionRecordId)
      throws SystemException, BusinessException {
    // TODO Auto-generated method stub
    List<EHCMApiActionHistory> actionHistoryList = approvalActionDAO
        .getActionHistoryByDecisionRecordId(decisionRecordId);
    return mapActionHistoryDetails(actionHistoryList);
  }

  /**
   * Fill Approval Action History Details in DTO
   * 
   * @param actionHistoryList
   * @return
   */
  private List<ApprovalActionDTO> mapActionHistoryDetails(
      List<EHCMApiActionHistory> actionHistoryList) {
    // TODO Auto-generated method stub
    List<ApprovalActionDTO> approvalActionDetailsList = new ArrayList<ApprovalActionDTO>();
    ApprovalActionDTO approvalActionDetails = null;
    for (EHCMApiActionHistory actionHistory : actionHistoryList) {
      approvalActionDetails = new ApprovalActionDTO();
      approvalActionDetails
          .setAction(actionHistory.getAction() == null ? "" : actionHistory.getAction());
      approvalActionDetails.setApprover(actionHistory.getApprover().getName());
      approvalActionDetails.setRequester(actionHistory.getRequester().getName());
      approvalActionDetails.setDecisionType(actionHistory.getDecisionType());
      approvalActionDetailsList.add(approvalActionDetails);
    }
    return approvalActionDetailsList;
  }

}
