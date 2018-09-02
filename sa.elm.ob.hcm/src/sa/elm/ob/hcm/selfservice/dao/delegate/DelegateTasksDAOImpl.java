package sa.elm.ob.hcm.selfservice.dao.delegate;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EhcmApprovalDelegation;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.selfservice.dto.delegate.DelegateTasksDTO;
import sa.elm.ob.utility.util.DateUtils;
import sa.elm.ob.utility.util.Utility;

@Repository
public class DelegateTasksDAOImpl implements DelegateTasksDAO {

  private static final String OPEN_BRAVO_GREG_DATE_FORMAT = "yyyy-MM-dd";

  @Autowired
  private EmployeeProfileDAO employeeProfileDAO;

  @Override
  public void save(String username, DelegateTasksDTO delegateTasksDTO) {

    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);

    EhcmEmpPerInfo fromEmp = employeeProfileDAO
        .getEmployeeProfileByUser(delegateTasksDTO.getFromUsername());

    EhcmEmpPerInfo toEmp = employeeProfileDAO
        .getEmployeeProfileByUser(delegateTasksDTO.getToUsername());

    User userOB = employeeProfileDAO.getUserDetailsByUserName(employeeOB.getSearchKey());

    try {
      OBContext.setAdminMode();

      EhcmApprovalDelegation newDelegation = OBProvider.getInstance()
          .get(EhcmApprovalDelegation.class);

      newDelegation.setOrganization(employeeOB.getOrganization());
      newDelegation.setClient(employeeOB.getClient());
      newDelegation.setCreationDate(new java.util.Date());
      newDelegation.setCreatedBy(userOB);
      newDelegation.setUpdated(new java.util.Date());
      newDelegation.setUpdatedBy(userOB);

      newDelegation.setFromEmployee(fromEmp);
      newDelegation.setToEmployee(toEmp);

      newDelegation.setFromDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
          Utility.convertToGregorian(delegateTasksDTO.getFromDate())));

      newDelegation.setToDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
          Utility.convertToGregorian(delegateTasksDTO.getToDate())));

      OBDal.getInstance().save(newDelegation);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  @Override
  public void update(String username, DelegateTasksDTO delegateTasksDTO) {

    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);

    EhcmEmpPerInfo fromEmp = employeeProfileDAO
        .getEmployeeProfileByUser(delegateTasksDTO.getFromUsername());

    EhcmEmpPerInfo toEmp = employeeProfileDAO
        .getEmployeeProfileByUser(delegateTasksDTO.getToUsername());

    User userOB = employeeProfileDAO.getUserDetailsByUserName(employeeOB.getSearchKey());

    try {
      OBContext.setAdminMode();

      EhcmApprovalDelegation newDelegation = getById(delegateTasksDTO.getId());

      newDelegation.setOrganization(employeeOB.getOrganization());
      newDelegation.setClient(employeeOB.getClient());
      newDelegation.setUpdated(new java.util.Date());
      newDelegation.setUpdatedBy(userOB);

      newDelegation.setFromEmployee(fromEmp);
      newDelegation.setToEmployee(toEmp);

      newDelegation.setFromDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
          Utility.convertToGregorian(delegateTasksDTO.getFromDate())));

      newDelegation.setToDate(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
          Utility.convertToGregorian(delegateTasksDTO.getToDate())));

      OBDal.getInstance().save(newDelegation);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  @Override
  public List<EhcmApprovalDelegation> getAllByUsername(String username) {

    List<EhcmApprovalDelegation> tasksDelegated = new ArrayList<EhcmApprovalDelegation>();

    try {
      OBContext.setAdminMode(true);

      OBQuery<EhcmApprovalDelegation> delegationTasks = OBDal.getInstance().createQuery(
          EhcmApprovalDelegation.class,
          " fromEmployee.searchKey=:username and enabled='Y' order by creationDate desc ");
      delegationTasks.setNamedParameter("username", username);

      delegationTasks.setFilterOnReadableClients(false);
      delegationTasks.setFilterOnReadableOrganization(false);

      if (delegationTasks.list().size() > 0) {
        tasksDelegated = delegationTasks.list();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }

    return tasksDelegated;
  }

  @Override
  public void delete(EhcmApprovalDelegation delegate) {

    try {
      OBContext.setAdminMode(true);
      OBDal.getInstance().remove(delegate);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  @Override
  public EhcmApprovalDelegation getById(String id) {

    EhcmApprovalDelegation delegation = null;

    try {
      OBContext.setAdminMode(true);
      delegation = OBDal.getInstance().get(EhcmApprovalDelegation.class, id);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }

    return delegation;
  }

  @Override
  public List<EhcmApprovalDelegation> getAll() {

    List<EhcmApprovalDelegation> allDelagation = new ArrayList<>();

    try {
      OBContext.setAdminMode(true);

      String query = "as e where e.enabled='Y' order by creationDate desc";
      allDelagation = OBDal.getInstance().createQuery(EhcmApprovalDelegation.class, query).list();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }

    return allDelagation;
  }

}
