package sa.elm.ob.hcm.dao.managerSelfService;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.EHCMEmpSupervisorNode;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.util.MessageKeys;

@Repository
public class ManagerSelfServiceDAOImpl implements ManagerSelfServiceDAO {
  private static final Logger log = LoggerFactory.getLogger(ManagerSelfServiceDAOImpl.class);
  @Autowired
  EmployeeProfileDAO employeeProfileDAO;

  @Override
  public List<EHCMEmpSupervisorNode> getAllEmployeeByManager(String username)
      throws BusinessException {
    final String userLang = SecurityUtils.getUserLanguage();
    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);
    // find super visor Hierarchy for manager
    EHCMEmpSupervisor superVisorOB = getManagerRoleDataForEmployee(employeeOB);
    if (superVisorOB == null)
      throw new BusinessException(
          Resource.getProperty(MessageKeys.EMPLOYEE_NOT_HAVE_MANAGER_ROLE, userLang));
    return superVisorOB.getEHCMEmpSupervisorNodeList();
  }

  /**
   * find the super visor data for given input employee
   * 
   * @param employeeOB
   * @return
   */
  private EHCMEmpSupervisor getManagerRoleDataForEmployee(EhcmEmpPerInfo employeeOB) {
    // TODO Auto-generated method stub
    EHCMEmpSupervisor objSupervisior = null;
    OBQuery<EHCMEmpSupervisor> objSuperVisorQry = null;
    List<EHCMEmpSupervisor> superVisorList = new ArrayList<EHCMEmpSupervisor>();
    try {
      OBContext.setAdminMode(true);
      final String query = " as e where e.employee.id=:employeeId and e.ehcmEmpHierarchy.primaryFlag='Y' and e.client.id=:clientId";
      objSuperVisorQry = OBDal.getInstance().createQuery(EHCMEmpSupervisor.class, query);
      objSuperVisorQry.setNamedParameter("employeeId", employeeOB.getId());
      objSuperVisorQry.setNamedParameter("clientId", employeeOB.getClient().getId());
      objSuperVisorQry.setFilterOnReadableClients(false);
      objSuperVisorQry.setFilterOnReadableOrganization(false);
      objSuperVisorQry.setMaxResult(1);
      superVisorList = objSuperVisorQry.list();
      if (superVisorList.size() > 0) {
        objSupervisior = superVisorList.get(0);
      }
    } catch (OBException e) {
      log.error("Error while fetching Manager Role Details of Employee-->employee user name -->"
          + employeeOB.getName());
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return objSupervisior;
  }

  @Override
  public EhcmEmpPerInfo getEmployeeInformationByNumber(String empNo) {
    // TODO Auto-generated method stub
    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(empNo);
    return employeeOB;
  }

  @Override
  public EmploymentInfo findActiveEmploymentInfo(EhcmEmpPerInfo employee) {
    // TODO Auto-generated method stub
    EmploymentInfo employmentInfo = null;
    try {
      OBContext.setAdminMode();
      for (EmploymentInfo empInfo : employee.getEhcmEmploymentInfoList()) {
        if (empInfo.isEnabled() && empInfo.getAlertStatus().equals("ACT")) {
          employmentInfo = empInfo;
          break;
        }
      }
    } catch (Exception e) {

    } finally {
      OBContext.restorePreviousMode();
    }
    return employmentInfo;
  }

  @Override
  public EHCMAbsenceAttendance findLeaveDetailsByDecNo(String originalDecNo) {
    // TODO Auto-generated method stub
    EHCMAbsenceAttendance absenceAttendance = null;
    OBQuery<EHCMAbsenceAttendance> objAttendanceQry = null;
    List<EHCMAbsenceAttendance> objAttendanceList = new ArrayList<EHCMAbsenceAttendance>();
    try {
      OBContext.setAdminMode(true);
      final String query = " as e where e.originalDecisionNo=:value";
      objAttendanceQry = OBDal.getInstance().createQuery(EHCMAbsenceAttendance.class, query);
      objAttendanceQry.setNamedParameter("value", originalDecNo);
      objAttendanceQry.setFilterOnReadableClients(false);
      objAttendanceQry.setFilterOnReadableOrganization(false);
      objAttendanceQry.setMaxResult(1);
      objAttendanceList = objAttendanceQry.list();
      if (objAttendanceList.size() > 0) {
        absenceAttendance = objAttendanceList.get(0);
      }
    } catch (OBException e) {
      log.error(
          "Error while fetching leave details for original decision number->decision Number -->"
              + originalDecNo);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return absenceAttendance;
  }

  @Override
  public List<EHCMEmpBusinessMission> findBusinessMissionByEmployee(String employeeNumber) {
    // TODO Auto-generated method stub
    List<EHCMEmpBusinessMission> businessMissionList = null;
    try {
      OBContext.setAdminMode();
      EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(employeeNumber);
      businessMissionList = employeeOB.getEHCMEmpBusinessMissionList();
    } catch (Exception e) {
      log.debug(
          "Error while fetching business mission details for employee ===>>" + employeeNumber);
    } finally {
      OBContext.restorePreviousMode();
    }
    return businessMissionList;
  }

  @Override
  public Boolean checkManagerWithEmployee(String mngUsername, String empNo) {
    // TODO Auto-generated method stub
    EHCMEmpSupervisorNode superVisorNode = null;
    OBQuery<EHCMEmpSupervisorNode> superVisorNodeQry = null;
    List<EHCMEmpSupervisorNode> superVisorNodeList = new ArrayList<EHCMEmpSupervisorNode>();
    Boolean isManagerForEmployee = false;
    try {
      OBContext.setAdminMode(true);
      final String query = " as e where e.ehcmEmpPerinfo.searchKey=:empNo and e.ehcmEmpSupervisor.employee.searchKey=:empManager ";
      // + "and e.ehcmEmpSupervisor.employee.primaryflag='Y'";
      superVisorNodeQry = OBDal.getInstance().createQuery(EHCMEmpSupervisorNode.class, query);
      superVisorNodeQry.setNamedParameter("empNo", empNo);
      superVisorNodeQry.setNamedParameter("empManager", mngUsername);
      superVisorNodeQry.setFilterOnReadableClients(false);
      superVisorNodeQry.setFilterOnReadableOrganization(false);
      superVisorNodeQry.setMaxResult(1);
      superVisorNodeList = superVisorNodeQry.list();
      if (superVisorNodeList.size() > 0) {
        superVisorNode = superVisorNodeList.get(0);
      }
      if (superVisorNode != null) {
        isManagerForEmployee = true;
      }
    } catch (OBException e) {
      log.error("Error while verifiying employee with his manager==> for employee==>" + empNo);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return isManagerForEmployee;
  }

}
