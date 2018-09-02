package sa.elm.ob.hcm.selfservice.service.delegate;

import java.util.List;

import sa.elm.ob.hcm.selfservice.dto.delegate.DelegateTasksDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;

public interface DelegateTasksService {

  public void save(DelegateTasksDTO delegateTasksDTO);

  public void update(DelegateTasksDTO delegateTasksDTO) throws BusinessException;

  public List<DelegateTasksDTO> getAllByUsername(String username);

  public void delete(String id) throws BusinessException;

}
