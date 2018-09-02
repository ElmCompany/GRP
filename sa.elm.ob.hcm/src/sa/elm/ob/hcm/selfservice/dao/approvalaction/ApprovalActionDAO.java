package sa.elm.ob.hcm.selfservice.dao.approvalaction;

import java.util.List;

import sa.elm.ob.hcm.EHCMApiActionHistory;
import sa.elm.ob.hcm.selfservice.dto.approvalaction.ApprovalActionDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

public interface ApprovalActionDAO {

  public void save(ApprovalActionDTO approvalActionDTO) throws SystemException, BusinessException;

  public List<EHCMApiActionHistory> getActionHistoryByDecisionRecordId(String decisionRecordId)
      throws SystemException, BusinessException;

}
