package sa.elm.ob.hcm.selfservice.service.delegate;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sa.elm.ob.hcm.EhcmApprovalDelegation;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.dao.delegate.DelegateTasksDAO;
import sa.elm.ob.hcm.selfservice.dto.delegate.DelegateTasksDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.util.MessageKeys;
import sa.elm.ob.utility.util.DateUtils;
import sa.elm.ob.utility.util.Utility;

@Service
public class DelegateTasksServiceImpl implements DelegateTasksService {
  private static final String OPEN_BRAVO_GREG_DATE_FORMAT = "yyyy-MM-dd";

  @Autowired
  private DelegateTasksDAO delegateTasksDAO;

  @Override
  public void save(DelegateTasksDTO delegateTasksDTO) {

    String username = SecurityUtils.loggedInUserName();

    delegateTasksDAO.save(username, delegateTasksDTO);
  }

  @Override
  public void update(DelegateTasksDTO delegateTasksDTO) throws BusinessException {
    final String userLang = SecurityUtils.getUserLanguage();
    if (delegateTasksDTO.getId() == null)

      throw new BusinessException(
          Resource.getProperty(MessageKeys.DELEGATE_ID_NOT_EXIST, userLang));

    String username = SecurityUtils.loggedInUserName();

    delegateTasksDAO.update(username, delegateTasksDTO);

  }

  @Override
  public List<DelegateTasksDTO> getAllByUsername(String username) {

    List<EhcmApprovalDelegation> ehcmDelegationList = delegateTasksDAO.getAllByUsername(username);

    List<DelegateTasksDTO> delegatedList = new ArrayList<DelegateTasksDTO>();

    for (EhcmApprovalDelegation delegation : ehcmDelegationList) {
      DelegateTasksDTO delegateTask = new DelegateTasksDTO();

      delegateTask.setId(delegation.getId());
      delegateTask.setFromUsername(delegation.getFromEmployee().getSearchKey());
      delegateTask.setToUsername(delegation.getToEmployee().getSearchKey());
      delegateTask.setFromDate(Utility.convertTohijriDate(
          DateUtils.convertDateToString(OPEN_BRAVO_GREG_DATE_FORMAT, delegation.getFromDate())));
      delegateTask.setToDate(Utility.convertTohijriDate(
          DateUtils.convertDateToString(OPEN_BRAVO_GREG_DATE_FORMAT, delegation.getToDate())));

      delegatedList.add(delegateTask);
    }

    return delegatedList;
  }

  @Override
  public void delete(String id) throws BusinessException {
    final String userLang = SecurityUtils.getUserLanguage();
    EhcmApprovalDelegation delegate = delegateTasksDAO.getById(id);

    if (delegate == null)
      throw new BusinessException(
          Resource.getProperty(MessageKeys.DELEGATE_ID_NOT_EXIST, userLang));

    delegateTasksDAO.delete(delegate);

  }

}
