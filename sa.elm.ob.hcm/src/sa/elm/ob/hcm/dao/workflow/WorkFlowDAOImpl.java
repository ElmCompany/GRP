package sa.elm.ob.hcm.dao.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.common.enterprise.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmOrgManager;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.dao.managerSelfService.ManagerSelfServiceDAO;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.util.MessageKeys;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.utility.util.DateUtils;

@Repository
public class WorkFlowDAOImpl implements WorkFlowDAO {
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(WorkFlowDAOImpl.class);
  private static final String OPEN_BRAVO_DATE_FORMAT = "dd-MM-yyyy";
  @Autowired
  EmployeeProfileDAO employeeProficeDAO;
  @Autowired
  ManagerSelfServiceDAO managerSelfServiceDAO;

  @Override
  public String getLineManagerIdByUserNameOrUserId(String empUsername, String empUserId)
      throws SystemException {
    // TODO Auto-generated method stub
    String lineManagerId = "";
    EhcmEmpPerInfo employeeOB = null;
    final String userLang = SecurityUtils.getUserLanguage();
    try {
      OBContext.setAdminMode();
      if (empUsername != null) {
        employeeOB = employeeProficeDAO.getEmployeeProfileByUser(empUsername);
      } else if (empUserId != null) {
        employeeOB = getEmployeeProfileByUserId(empUserId);
      }
      if (employeeOB != null) {
        for (EmploymentInfo emloymentInfo : employeeOB.getEhcmEmploymentInfoList()) {
          if (emloymentInfo.isEnabled()) {
            lineManagerId = emloymentInfo.getEhcmEmpSupervisor() != null
                ? emloymentInfo.getEhcmEmpSupervisor().getEmployee().getId()
                : "";
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Error while fetching -->emp user name -->" + empUsername);
      throw new OBException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }
    return lineManagerId;
  }

  /**
   * 
   * @param userId
   * @return Employee Personal info
   */
  private EhcmEmpPerInfo getEmployeeProfileByUserId(String userId) throws SystemException {
    EhcmEmpPerInfo objEmpPerInfo = null;
    OBQuery<EhcmEmpPerInfo> empInfoQry = null;
    List<EhcmEmpPerInfo> empInfoList = new ArrayList<EhcmEmpPerInfo>();
    final String userLang = SecurityUtils.getUserLanguage();
    try {
      OBContext.setAdminMode(true);
      final String query = " as e where e.id=:userId  ";
      empInfoQry = OBDal.getInstance().createQuery(EhcmEmpPerInfo.class, query);
      empInfoQry.setNamedParameter("userId", userId);
      empInfoQry.setFilterOnReadableClients(false);
      empInfoQry.setFilterOnReadableOrganization(false);
      empInfoQry.setMaxResult(1);
      empInfoList = empInfoQry.list();
      if (empInfoList.size() > 0) {
        objEmpPerInfo = empInfoList.get(0);
      }
    } catch (Exception e) {
      log.error("Error while fetching employee personal data-->emp user Id -->" + userId);
      throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }
    return objEmpPerInfo;
  }

  @Override
  public String getDepartmentManagerByUserName(String empUsername) throws SystemException {
    // TODO Auto-generated method stub
    String departmentManagerValue = "";
    EhcmEmpPerInfo employeeOB = null;
    EhcmEmpPerInfo managerOB = null;
    String deptName = "";
    Date today = new Date();
    final String userLang = SecurityUtils.getUserLanguage();
    try {
      OBContext.setAdminMode();
      employeeOB = employeeProficeDAO.getEmployeeProfileByUser(empUsername);
      for (EmploymentInfo emloymentInfo : employeeOB.getEhcmEmploymentInfoList()) {
        if (emloymentInfo.isEnabled()) {
          deptName = emloymentInfo.getDepartmentName();
        }
      }
      // find department details by dept name
      Organization orgOB = findDepartmentByName(deptName);
      // find current active mananger for dept
      for (EhcmOrgManager orgManager : orgOB.getEhcmOrgManagerList()) {
        if (today.compareTo(orgManager.getEhcmFromdate()) >= 0
            && ((orgManager.getEhcmTodate() != null
                && orgManager.getEhcmTodate().compareTo(today) >= 0)
                || orgManager.getEhcmTodate() == null)
            && orgManager.getBusinessPartner() != null) {
          departmentManagerValue = orgManager.getBusinessPartner().getSearchKey() != null
              ? orgManager.getBusinessPartner().getSearchKey()
              : "";
        }
      }
      // find department manager id from employee table
      managerOB = employeeProficeDAO.getEmployeeProfileByUser(departmentManagerValue);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Error while fetching -->emp user name -->" + empUsername);
      throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }
    return managerOB == null ? "" : managerOB.getId();
  }

  private Organization findDepartmentByName(String deptName) {
    // TODO Auto-generated method stub
    Organization orgOB = null;
    OBQuery<Organization> orgQuery = null;
    List<Organization> orgQryList = new ArrayList<Organization>();
    try {
      OBContext.setAdminMode(true);
      final String query = " as e where e.name=:deptName  ";
      orgQuery = OBDal.getInstance().createQuery(Organization.class, query);
      orgQuery.setNamedParameter("deptName", deptName);
      orgQuery.setFilterOnReadableClients(false);
      orgQuery.setFilterOnReadableOrganization(false);
      orgQuery.setMaxResult(1);
      orgQryList = orgQuery.list();
      if (orgQryList.size() > 0) {
        orgOB = orgQryList.get(0);
      }
      return orgOB;
    } catch (Exception e) {
      log.error("Error while fetching dept data for department-->" + deptName);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public List<String> getRoleListByUsingUsername(String userName) {
    // TODO Auto-generated method stub
    User userOB = null;
    OBQuery<User> userQuery = null;
    List<String> roleList = new ArrayList<String>();
    try {
      OBContext.setAdminMode(true);
      final String query = " as e where e.username=:username or e.name=:name  ";
      userQuery = OBDal.getInstance().createQuery(User.class, query);
      userQuery.setNamedParameter("username", userName);
      userQuery.setNamedParameter("name", userName);
      userQuery.setFilterOnReadableClients(false);
      userQuery.setFilterOnReadableOrganization(false);
      userQuery.setMaxResult(1);
      if (userQuery.list().size() > 0) {
        userOB = userQuery.list().get(0);
      }
      if (userOB != null && userOB.getADUserRolesList().size() > 0) {
        for (UserRoles userRolesOB : userOB.getADUserRolesList()) {
          roleList.add(userRolesOB.getRole().getId());
        }
      }
      return roleList;

    } catch (Exception e) {
      log.error("Error while fetching Role Details for -->User -->" + userName);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public String getTopManagerByUsername(String userName) throws BusinessException, SystemException {
    // TODO Auto-generated method stub
    String employeeDepartmentId = "", employeeTopManagerCode = "", employeeTopManagerId = "",
        strEndDate = "21-06-2058";// max
    // of
    // existing
    // date
    Date orgManagerTodate;
    Date employmentTodate;
    final String userLang = SecurityUtils.getUserLanguage();
    try {
      OBContext.setAdminMode();
      // find employee
      EhcmEmpPerInfo employeeOB = employeeProficeDAO.getEmployeeProfileByUser(userName);
      // find active employment
      EmploymentInfo activeEmploymentOB = managerSelfServiceDAO
          .findActiveEmploymentInfo(employeeOB);
      if (activeEmploymentOB.getEndDate() == null) {
        employmentTodate = DateUtils.convertStringToDate(OPEN_BRAVO_DATE_FORMAT, strEndDate);
      } else {
        employmentTodate = activeEmploymentOB.getEndDate();
      }
      // find employment position and it department
      if (activeEmploymentOB.getPosition() != null
          && activeEmploymentOB.getPosition().getDepartment() != null) {
        employeeDepartmentId = activeEmploymentOB.getPosition().getDepartment().getId();
      }
      // find parent department id
      String hrParentDepartmentId = Utility.getTopLevelParentDepartmentId(employeeDepartmentId);
      // find top level parentDepartment Manager Details
      Organization orgOB = OBDal.getInstance().get(Organization.class, hrParentDepartmentId);
      if (orgOB.getEhcmOrgManagerList().size() > 0) {
        for (EhcmOrgManager orgManagerOB : orgOB.getEhcmOrgManagerList()) {
          if (orgManagerOB.getEhcmTodate() == null) {
            orgManagerTodate = DateUtils.convertStringToDate(OPEN_BRAVO_DATE_FORMAT, strEndDate);
          } else {
            orgManagerTodate = orgManagerOB.getEhcmTodate();
          }
          if ((orgManagerOB.getEhcmFromdate().compareTo(activeEmploymentOB.getStartDate()) >= 0
              && orgManagerTodate.compareTo(employmentTodate) <= 0)
              || (orgManagerTodate.compareTo(activeEmploymentOB.getStartDate()) >= 0
                  && orgManagerOB.getEhcmFromdate().compareTo(employmentTodate) <= 0)) {
            employeeTopManagerCode = orgManagerOB.getBusinessPartner().getSearchKey();// employee
                                                                                      // top level
                                                                                      // manager
                                                                                      // search key
            employeeTopManagerId = orgManagerOB.getBusinessPartner().getEhcmEmpPerinfo() == null
                ? ""
                : orgManagerOB.getBusinessPartner().getEhcmEmpPerinfo().getId();
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();

    }
    return employeeTopManagerId;
  }

}
