package sa.elm.ob.hcm.ad_process.EndofEmployment.TerminationReactivate;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmterminationEmpV;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmempstatus;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.EmpExtendService.DAO.ExtendServiceHandlerDAO;
import sa.elm.ob.hcm.util.UtilityDAO;

/**
 * This process class used for empTerminationDAO Implementation
 * 
 * @author poongodi 28-08-2018
 *
 */

public class EmpTerminationDAOImpl implements EndofEmploymentDAO {

  private Connection connection = null;
  private static final Logger log = LoggerFactory.getLogger(EmpTerminationDAOImpl.class);
  public static final String Status_Terminate = "T";
  public static final String Status_TerminateEnd = "TE";

  public EmpTerminationDAOImpl() {
    connection = getDbConnection();
  }

  /**
   * Get the database connection
   * 
   * @return
   */
  private Connection getDbConnection() {
    return OBDal.getInstance().getConnection();
  }

  public void removeEmploymentRecord(String terminationId, VariablesSecureApp vars,
      EHCMEMPTermination termination) {
    OBQuery<EmploymentInfo> empInfo = null;
    EmploymentInfo empinfo = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    try {
      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpTermination.id=:terminationId ");
      empInfo.setNamedParameter("terminationId", terminationId);
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        empinfo = employmentInfo.get(0);
        updateEmploymentRecord(terminationId, vars, termination, empinfo.getId());

        OBDal.getInstance().remove(empinfo);
        OBDal.getInstance().flush();
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in removeEmploymentRecord " + e.getMessage());
      e.printStackTrace();
    }

  }

  public void updateEmploymentRecord(String terminationId, VariablesSecureApp vars,
      EHCMEMPTermination termination, String recentEmpInfoId) {
    OBQuery<EmploymentInfo> empInfo = null;
    EmploymentInfo empinfo = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    Date endDate = null;
    try {
      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "   ehcmEmpPerinfo.id=:employeeId and id <> '" + recentEmpInfoId
              + "' order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", termination.getEhcmEmpPerinfo().getId());
      empInfo.setMaxResult(1);
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        empinfo = employmentInfo.get(0);
        EmploymentInfo empinfoObj = empinfo;
        empinfoObj.setUpdated(new java.util.Date());
        empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        endDate = ExtendServiceHandlerDAO.updateEndDateInEmploymentInfo(
            termination.getEhcmEmpPerinfo().getId(), termination.getClient().getId(),
            recentEmpInfoId);
        empinfo.setEndDate(endDate);
        empinfo.setEnabled(true);
        empinfo.setAlertStatus(DecisionTypeConstants.Status_active);
        OBDal.getInstance().save(empinfo);
        OBDal.getInstance().flush();
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in updateEmploymentRecord " + e.getMessage());
      e.printStackTrace();
    }

  }

  public void updateEmpStatusRecord(String employeeId, EHCMEMPTermination terminationId) {
    try {
      OBQuery<ehcmempstatus> employeestatus = OBDal.getInstance().createQuery(ehcmempstatus.class,
          " ehcmEmpPerinfo.id=:employeeId ");
      employeestatus.setNamedParameter("employeeId", employeeId);
      if (employeestatus.list().size() > 0) {
        ehcmempstatus employee = employeestatus.list().get(0);
        employee.setEhcmEmpTermination(terminationId);
        OBDal.getInstance().save(employee);
        OBDal.getInstance().flush();
      }

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in updateEmpStatusRecord " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void removeEmpStatusRecord(String employeeId, String terminationId) {
    try {
      OBQuery<ehcmempstatus> employeestatus = OBDal.getInstance().createQuery(ehcmempstatus.class,
          " ehcmEmpPerinfo.id=:employeeId and ehcmEmpTermination.id = :terminationId ");
      employeestatus.setNamedParameter("employeeId", employeeId);
      employeestatus.setNamedParameter("terminationId", terminationId);
      if (employeestatus.list().size() > 0) {
        ehcmempstatus employee = employeestatus.list().get(0);
        OBDal.getInstance().remove(employee);
        OBDal.getInstance().flush();
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in removeEmpStatusRecord " + e.getMessage());
      e.printStackTrace();
    }

  }

  public void updateEmpRecord(String terminationId, VariablesSecureApp vars,
      EHCMEMPTermination termination) {
    OBQuery<EmploymentInfo> empInfo = null;
    EmploymentInfo empinfo = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    String employmentStatus = null;
    try {
      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "   ehcmEmpPerinfo.id=:employeeId order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", termination.getEhcmEmpPerinfo().getId());
      empInfo.setMaxResult(1);
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        empinfo = employmentInfo.get(0);
        EmploymentInfo empinfoObj = empinfo;
        if (empinfoObj.getEhcmEmpSecondment() != null) {
          employmentStatus = DecisionTypeConstants.EMPLOYMENTSTATUS_SECONDMENT;
        } else if (empinfoObj.getEhcmExtendService() != null) {
          employmentStatus = DecisionTypeConstants.EMPLOYMENTSTATUS_EXTENDSERVICE;
        } else if (empinfoObj.getEhcmEmpExtrastep() != null) {
          employmentStatus = DecisionTypeConstants.EMPLOYMENTSTATUS_EXTRASTEP;
        } else {
          employmentStatus = DecisionTypeConstants.EMPLOYMENTSTATUS_ACTIVE;
        }
        EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class,
            termination.getEhcmEmpPerinfo().getId());
        employee.setEmploymentStatus(employmentStatus);
        employee.setEnabled(true);
        OBDal.getInstance().save(employee);
        OBDal.getInstance().flush();

      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in updateEmpRecord " + e.getMessage());
      e.printStackTrace();
    }

  }

  public void insertRecordinEmploymentInfo(EHCMEMPTermination terminationoldObj,
      VariablesSecureApp vars) {
    EmploymentInfo info = null;
    EhcmterminationEmpV terminationView = terminationoldObj.getEhcmEmpPerinfo();
    int oneDay = 1 * 24 * 3600 * 1000;
    try {
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e where ehcmEmpPerinfo.id=:employeeId and e.enabled='Y' order by e.creationDate desc");
      empInfo.setNamedParameter("employeeId", terminationoldObj.getEhcmEmpPerinfo().getId());
      empInfo.setMaxResult(1);
      if (empInfo.list().size() > 0) {
        info = empInfo.list().get(0);
      }
      EmploymentInfo employInfo = OBProvider.getInstance().get(EmploymentInfo.class);
      employInfo.setChangereason(Status_Terminate);
      employInfo.setChangereasoninfo(terminationoldObj.getEhcmTerminationReason().getSearchKey());
      UtilityDAO.insertActEmplymntInfoDetailsInIssueDecision(terminationView.getEhcmEmpPerinfo(),
          employInfo, false, true);
      employInfo.setStartDate(terminationoldObj.getTerminationDate());
      employInfo.setEndDate(null);
      employInfo.setAlertStatus(Status_TerminateEnd);
      employInfo.setEhcmEmpTermination(terminationoldObj);
      employInfo.setDecisionNo(terminationoldObj.getDecisionNo());
      employInfo.setDecisionDate(terminationoldObj.getDecisionDate());

      OBDal.getInstance().save(employInfo);
      OBDal.getInstance().flush();

      EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
          terminationoldObj.getEhcmEmpPerinfo().getId());
      person.setEmploymentStatus(Status_TerminateEnd);
      person.setEnabled(false);
      Date dateBefore = new Date(terminationoldObj.getTerminationDate().getTime() - oneDay);
      if (person.getStartDate().compareTo(terminationoldObj.getTerminationDate()) == 0)
        person.setEndDate(person.getStartDate());
      else
        person.setEndDate(dateBefore);
      OBDal.getInstance().save(person);
      OBDal.getInstance().flush();
      // update the endate and active flag for old hiring record.
      OBQuery<EmploymentInfo> empInfoold = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId  and id not in ('" + employInfo.getId()
              + "') and enabled='Y' and alertStatus=:alertStatus order by creationDate desc ");
      empInfoold.setNamedParameter("employeeId", terminationoldObj.getEhcmEmpPerinfo().getId());
      empInfoold.setNamedParameter("alertStatus", DecisionTypeConstants.Status_active);
      empInfoold.setMaxResult(1);
      if (empInfoold.list().size() > 0) {
        EmploymentInfo empinfo = empInfoold.list().get(0);
        empinfo.setUpdated(new java.util.Date());
        empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        Date startdate = empinfo.getStartDate();
        dateBefore = new Date(terminationoldObj.getTerminationDate().getTime() - oneDay);

        if (startdate.compareTo(terminationoldObj.getTerminationDate()) == 0)
          empinfo.setEndDate(empinfo.getStartDate());
        else
          empinfo.setEndDate(dateBefore);
        empinfo.setEnabled(false);
        empinfo.setAlertStatus(DecisionTypeConstants.Status_Inactive);
        OBDal.getInstance().save(empinfo);
        OBDal.getInstance().flush();
        insertEmpStatus(terminationoldObj, terminationView);
      }

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in insertRecordinEmploymentInfo " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      log.error("Exception in insertRecordinEmploymentInfo " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void insertEmpStatus(EHCMEMPTermination terminationoldobj,
      EhcmterminationEmpV terminationView) {
    ehcmempstatus ehcmempstatus = null;
    try {
      ehcmempstatus = OBProvider.getInstance().get(ehcmempstatus.class);
      ehcmempstatus.setClient(terminationoldobj.getClient());
      ehcmempstatus.setOrganization(terminationoldobj.getOrganization());
      ehcmempstatus.setCreationDate(new java.util.Date());
      ehcmempstatus.setCreatedBy(terminationoldobj.getCreatedBy());
      ehcmempstatus.setUpdated(new java.util.Date());
      ehcmempstatus.setUpdatedBy(terminationoldobj.getUpdatedBy());
      ehcmempstatus.setEnabled(false);
      ehcmempstatus.setEhcmEmpPerinfo(terminationView.getEhcmEmpPerinfo());
      ehcmempstatus.setDecisionno(terminationoldobj.getDecisionNo());
      ehcmempstatus.setEhcmEmpTermination(terminationoldobj);
      ehcmempstatus.setStartDate(terminationoldobj.getTerminationDate());
      ehcmempstatus.setStatus(Status_TerminateEnd);
      OBDal.getInstance().save(ehcmempstatus);
      OBDal.getInstance().flush();
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in insertEmpStatus " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void updateTerminationRecord(EHCMEMPTermination terminationObj,
      EHCMEMPTermination terminationOldObj, VariablesSecureApp vars) {
    List<EmploymentInfo> employment = new ArrayList<EmploymentInfo>();
    int oneDay = 1 * 24 * 3600 * 1000;
    try {

      OBQuery<EmploymentInfo> employmentInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and ehcmEmpTermination.id = :terminationId ");
      employmentInfo.setNamedParameter("employeeId", terminationObj.getEhcmEmpPerinfo().getId());
      employmentInfo.setNamedParameter("terminationId", terminationObj.getId());
      employment = employmentInfo.list();
      if (employment.size() > 0) {
        EmploymentInfo termination = employment.get(0);
        termination.setDecisionDate(terminationOldObj.getDecisionDate());
        termination.setDecisionNo(terminationOldObj.getDecisionNo());
        termination.setEhcmEmpTermination(terminationOldObj);
        termination.setStartDate(terminationOldObj.getTerminationDate());
        OBDal.getInstance().save(termination);
        OBDal.getInstance().flush();
        OBQuery<EmploymentInfo> employInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ehcmEmpPerinfo.id=:employeeId and ehcmEmpTermination.id is null order by creationDate desc");
        employInfo.setNamedParameter("employeeId", terminationObj.getEhcmEmpPerinfo().getId());
        employInfo.setMaxResult(1);
        employment = employInfo.list();
        if (employment.size() > 0) {
          EmploymentInfo empinfo = employment.get(0);
          empinfo.setUpdated(new java.util.Date());
          empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          Date startdate = empinfo.getStartDate();
          Date dateBefore = new Date(terminationOldObj.getTerminationDate().getTime() - oneDay);

          if (startdate.compareTo(terminationOldObj.getTerminationDate()) == 0)
            empinfo.setEndDate(empinfo.getStartDate());
          else
            empinfo.setEndDate(dateBefore);
          empinfo.setEnabled(false);
          empinfo.setAlertStatus(DecisionTypeConstants.Status_Inactive);
          OBDal.getInstance().save(empinfo);
          OBDal.getInstance().flush();
        }
      }

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in updateTerminationRecord " + e.getMessage());
      e.printStackTrace();
    }
  }

}
