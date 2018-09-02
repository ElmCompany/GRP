package sa.elm.ob.hcm.selfservice.dao.delegate;

import java.util.List;

import sa.elm.ob.hcm.EhcmApprovalDelegation;
import sa.elm.ob.hcm.selfservice.dto.delegate.DelegateTasksDTO;

public interface DelegateTasksDAO {

  public void save(String username, DelegateTasksDTO delegateTasksDTO);

  public void update(String username, DelegateTasksDTO delegateTasksDTO);

  public List<EhcmApprovalDelegation> getAllByUsername(String username);

  public void delete(EhcmApprovalDelegation delegate);

  public EhcmApprovalDelegation getById(String id);

  public List<EhcmApprovalDelegation> getAll();

}
