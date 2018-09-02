package sa.elm.ob.hcm.selfservice.dao.approvalaction;

import java.util.List;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EHCMApiActionHistory;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.dto.approvalaction.ApprovalActionDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.util.MessageKeys;

@Repository
public class ApprovalActionDAOImpl implements ApprovalActionDAO {

  @Autowired
  private EmployeeProfileDAO employeeProfileDAO;

  @Override
  public void save(ApprovalActionDTO approvalActionDTO) throws SystemException, BusinessException {
    String username = SecurityUtils.loggedInUserName();
    EhcmEmpPerInfo requester = employeeProfileDAO
        .getEmployeeProfileByUser(approvalActionDTO.getRequester());
    EhcmEmpPerInfo approver = employeeProfileDAO
        .getEmployeeProfileByUser(approvalActionDTO.getApprover());
    User userOB = employeeProfileDAO.getUserDetailsByUserName(username);
    final String userLang = SecurityUtils.getUserLanguage();

    try {
      OBContext.setAdminMode();
      EHCMApiActionHistory actionHistoryOB = OBProvider.getInstance()
          .get(EHCMApiActionHistory.class);
      actionHistoryOB.setOrganization(requester.getOrganization());
      actionHistoryOB.setClient(requester.getClient());
      actionHistoryOB.setCreationDate(new java.util.Date());
      actionHistoryOB.setCreatedBy(userOB);
      actionHistoryOB.setUpdated(new java.util.Date());
      actionHistoryOB.setUpdatedBy(userOB);

      actionHistoryOB.setRequester(requester);
      actionHistoryOB.setApprover(approver);
      actionHistoryOB.setDecisionrecord(approvalActionDTO.getDecisionrecord());
      actionHistoryOB.setDecisionType(approvalActionDTO.getDecisionType());
      actionHistoryOB.setDescription(approvalActionDTO.getDescription());
      actionHistoryOB.setAction(approvalActionDTO.getAction());
      OBDal.getInstance().save(actionHistoryOB);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  @Override
  public List<EHCMApiActionHistory> getActionHistoryByDecisionRecordId(String decisionRecordId)
      throws SystemException, BusinessException {
    // TODO Auto-generated method stub
    final String userLang = SecurityUtils.getUserLanguage();
    List<EHCMApiActionHistory> actionHistoryList = null;
    try {
      OBContext.setAdminMode();
      OBQuery<EHCMApiActionHistory> actionHistoryQry = OBDal.getInstance().createQuery(
          EHCMApiActionHistory.class, "as e where e.decisionrecord =:decisionRecordId");
      actionHistoryQry.setNamedParameter("decisionRecordId", decisionRecordId);
      actionHistoryQry.setFilterOnReadableClients(false);
      actionHistoryQry.setFilterOnReadableOrganization(false);
      if (actionHistoryQry.list().size() > 0) {
        throw new BusinessException(
            Resource.getProperty(MessageKeys.ACTION_HISTORY_NOT_EXISTS, userLang));
      } else {
        actionHistoryList = actionHistoryQry.list();
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new BusinessException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }
    return actionHistoryList;
  }

}
