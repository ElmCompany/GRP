package sa.elm.ob.hcm.ad_process.empScholarshipTraining;

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

import sa.elm.ob.hcm.EHCMEmpScholarship;
import sa.elm.ob.hcm.EHCMScholarShipHeader;
import sa.elm.ob.hcm.EHCMScholarshipSummary;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;

/**
 * This process class used for Employee ScholarshipDAO Implementation
 * 
 * @author divya 12-02-2018
 *
 */

public class EmpScholarshipTrainingDAOImpl implements EmpScholarshipTrainingDAO {

  private Connection connection = null;
  private static final Logger log = LoggerFactory.getLogger(EmpScholarshipTrainingDAOImpl.class);
  public static final String DECISION_STATUS_ISSUED = "I";

  public EHCMScholarshipSummary getActEmpScholarSummary(EHCMEmpScholarship empScholarShip)
      throws Exception {
    String sql = null;
    OBQuery<EHCMScholarshipSummary> scholarshipSummaryQry = null;
    List<EHCMScholarshipSummary> scholarshipSummaryList = new ArrayList<EHCMScholarshipSummary>();
    EHCMScholarshipSummary scholarshipSummary = null;
    try {
      if (empScholarShip.getOriginalDecisionNo() != null) {
        sql = " as e where e.employee.id=:employeeId   ";

        sql += "  and e.ehcmEmpScholarship.id=:originaldeicionnoId  ";

        log.debug("sql" + sql);
        // get Scholarship summary Information by passing the corresponding employee id.
        scholarshipSummaryQry = OBDal.getInstance().createQuery(EHCMScholarshipSummary.class, sql);
        scholarshipSummaryQry.setNamedParameter("employeeId", empScholarShip.getEmployee().getId());
        scholarshipSummaryQry.setNamedParameter("originaldeicionnoId",
            empScholarShip.getOriginalDecisionNo().getId());

        log.debug("sql" + scholarshipSummaryQry.getWhereAndOrderBy());
        scholarshipSummaryQry.setMaxResult(1);
        scholarshipSummaryList = scholarshipSummaryQry.list();
        if (scholarshipSummaryList.size() > 0) {
          scholarshipSummary = scholarshipSummaryList.get(0);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in getActEmpScholarSummary in EmpScholarshipTrainingDAOImpl: ", e);
    }
    return scholarshipSummary;

  }

  public void updateEmpScholarshipStatus(EHCMEmpScholarship empScholarShip) throws Exception {
    // TODO Auto-generated method stub
    try {
      // update status as Issued and set decision date for all cases
      empScholarShip.setUpdated(new java.util.Date());
      empScholarShip.setSueDecision(true);
      empScholarShip.setDecisionDate(new Date());
      empScholarShip.setDecisionStatus(DECISION_STATUS_ISSUED);
      OBDal.getInstance().save(empScholarShip);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateEmpScholarshipStatus in EmpScholarshipTrainingDAOImpl: ", e);
    }
  }

  public void insertScholarshipSummary(EHCMEmpScholarship empScholarShip,
      EHCMScholarshipSummary scholarshipSummary, VariablesSecureApp vars, String decisionType)
      throws Exception {
    // TODO Auto-generated method stub
    Date dateafter = null;
    try {

      dateafter = new Date(empScholarShip.getEndDate().getTime() + 1 * 24 * 3600 * 1000);
      EHCMScholarshipSummary scholarShipSummary = OBProvider.getInstance()
          .get(EHCMScholarshipSummary.class);
      scholarShipSummary.setClient(empScholarShip.getClient());
      scholarShipSummary.setOrganization(empScholarShip.getOrganization());
      scholarShipSummary.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      scholarShipSummary.setCreationDate(new java.util.Date());
      scholarShipSummary.setUpdated(new java.util.Date());
      scholarShipSummary.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      scholarShipSummary.setEmployee(empScholarShip.getEmployee());
      scholarShipSummary.setEhcmEmpScholarship(empScholarShip);
      scholarShipSummary.setDecisionType(decisionType);
      scholarShipSummary.setDecisionDate(new Date());
      scholarShipSummary.setDecisionNo(empScholarShip.getDecisionNo());

      scholarShipSummary.setStartDate(empScholarShip.getStartDate());
      scholarShipSummary.setEndDate(empScholarShip.getEndDate());

      OBDal.getInstance().save(scholarShipSummary);
      // update for old info record as inactive
      // updateActiveFlagforOldRecord(scholarshipSummary, vars, empScholarShip, decisionType);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in insertEmploymentInfo in EmpScholarshipTrainingDAOImpl: ", e);
    }
  }

  public static EHCMScholarShipHeader inserScholarshipHeader(EHCMEmpScholarship empScholarshipObj,
      VariablesSecureApp vars) {
    // TODO Auto-generated method stub
    EHCMScholarShipHeader scholarshipHeader = null;
    try {
      scholarshipHeader = OBProvider.getInstance().get(EHCMScholarShipHeader.class);
      scholarshipHeader.setClient(empScholarshipObj.getClient());
      scholarshipHeader.setOrganization(empScholarshipObj.getOrganization());
      scholarshipHeader.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      scholarshipHeader.setCreationDate(new java.util.Date());
      scholarshipHeader.setUpdated(new java.util.Date());
      scholarshipHeader.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      scholarshipHeader.setEmployee(empScholarshipObj.getEmployee());
      OBDal.getInstance().save(scholarshipHeader);
      return scholarshipHeader;

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in inserScholarshipHeader in EmpScholarshipTrainingDAOImpl: ", e);
    }
    return scholarshipHeader;
  }

  public void updateActiveFlagforOldRecord(EHCMScholarshipSummary scholarshipSummary,
      VariablesSecureApp vars, EHCMEmpScholarship empScholarShip, String decisionType) {
    // TODO Auto-generated method stub
    try {
      if (scholarshipSummary != null) {
        scholarshipSummary.setUpdated(new java.util.Date());
        scholarshipSummary.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        scholarshipSummary.setEnabled(false);
        /*
         * if(empScholarShip.getDecisionType().equals("CO")) {
         * scholarshipSummary.setEndDate(empScholarShip.getEndDate()); }
         */
        OBDal.getInstance().save(scholarshipSummary);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateActiveFlagforOldRecord in EmpScholarshipTrainingDAOImpl: ", e);
    }
  }

  public void updateScholarshipSummary(EHCMEmpScholarship empScholarShip,
      EHCMScholarshipSummary scholarshipSummary, VariablesSecureApp vars, String decisiontype) {
    // TODO Auto-generated method stub
    try {

      if (decisiontype.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
          || decisiontype.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
          || decisiontype.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        scholarshipSummary.setStartDate(empScholarShip.getStartDate());
        scholarshipSummary.setEndDate(empScholarShip.getEndDate());
        scholarshipSummary.setDecisionType(empScholarShip.getDecisionType());
        scholarshipSummary.setDecisionNo(empScholarShip.getDecisionNo());
        scholarshipSummary.setDecisionDate(new Date());
        scholarshipSummary.setEhcmEmpScholarship(empScholarShip);
        OBDal.getInstance().save(scholarshipSummary);

      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateEmployInfoforScholarship in EmpScholarshipTrainingDAOImpl: ",
          e);
    }
  }

  public EmploymentInfo getRecentEmployInfoOtherThanCurrRecd(EHCMEmpScholarship empScholarShip,
      EmploymentInfo employInfo) {
    OBQuery<EmploymentInfo> empInfo = null;
    EmploymentInfo empinfo = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    try {
      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId  and id <> :employInfoId order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", empScholarShip.getEmployee().getId());
      empInfo.setNamedParameter("employInfoId", employInfo.getId());
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        empinfo = employmentInfo.get(0);
        return empinfo;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in getRecentEmployInfoOtherThanCurrRecd " + e.getMessage());
      e.printStackTrace();
    }
    return empinfo;
  }

  public void updateOldEmpScholarshipInAct(EHCMEmpScholarship empScholarshipObj) {
    // TODO Auto-generated method stub
    try {
      // update old scholarship as inactive
      EHCMEmpScholarship oldEmpScholarship = empScholarshipObj.getOriginalDecisionNo();
      oldEmpScholarship.setEnabled(false);
      OBDal.getInstance().save(oldEmpScholarship);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateOldEmpScholarshipInAct in EmpScholarshipTrainingDAOImpl: ", e);
    }
  }

  public void removeScholarshipActRecord(EHCMEmpScholarship empScholarshipObj) {
    // TODO Auto-generated method stub
    List<EHCMScholarshipSummary> scholarShipSummaryList = new ArrayList<EHCMScholarshipSummary>();
    try {
      // remove the recent record
      OBQuery<EHCMScholarshipSummary> scholarShipSummaryQry = OBDal.getInstance().createQuery(
          EHCMScholarshipSummary.class,
          " as e where  e.employee.id=:employeeId   and e.enabled='Y'  and e.ehcmEmpScholarship.id =:empScholarshipId  "
              + "  order by creationDate desc");
      scholarShipSummaryQry.setNamedParameter("employeeId",
          empScholarshipObj.getEmployee().getId());
      scholarShipSummaryQry.setNamedParameter("empScholarshipId",
          empScholarshipObj.getOriginalDecisionNo().getId());
      scholarShipSummaryQry.setMaxResult(1);
      scholarShipSummaryList = scholarShipSummaryQry.list();
      if (scholarShipSummaryList.size() > 0) {
        EHCMScholarshipSummary scholarShipSummary = scholarShipSummaryList.get(0);
        OBDal.getInstance().remove(scholarShipSummary);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateOldEmpScholarshipInAct in EmpScholarshipTrainingDAOImpl: ", e);
    }
  }

  public static EmploymentInfo getPreviousEmploymentRecord(EHCMEmpScholarship empScholarshipObj) {
    // TODO Auto-generated method stub
    EmploymentInfo employinfo = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    try {
      OBQuery<EmploymentInfo> emply = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id=:employeeId and (e.ehcmEmpScholarship is null  "
              + " or ( e.ehcmEmpScholarship is not  null and e.ehcmEmpScholarship.id <> :ehcmEmpScholarshipId  ) ) and e.enabled='Y' order by e.creationDate desc");
      emply.setNamedParameter("employeeId", empScholarshipObj.getEmployee().getId());
      emply.setNamedParameter("ehcmEmpScholarshipId",
          empScholarshipObj.getOriginalDecisionNo().getId());
      emply.setMaxResult(1);
      employmentInfo = emply.list();
      if (employmentInfo.size() > 0) {
        employinfo = emply.list().get(0);
      }
      return employinfo;
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in getPreviousEmploymentRecord in EmpScholarshipTrainingDAOImpl: ", e);
    }
    return employinfo;
  }

  public EHCMScholarshipSummary getActiveScholarshipSummary(String employeeId,
      String originaldecId) {
    EHCMScholarshipSummary scholarshipSummary = null;
    List<EHCMScholarshipSummary> scholarshipSummaryList = new ArrayList<EHCMScholarshipSummary>();
    OBQuery<EHCMScholarshipSummary> scholarshipSummaryQry = null;

    try {
      scholarshipSummaryQry = OBDal.getInstance().createQuery(EHCMScholarshipSummary.class,
          " as e where  e.employee.id=:employeeId and e.ehcmEmpScholarship.id = :originaldecId order by creationDate desc ");// and
      // e.enabled='Y'

      scholarshipSummaryQry.setNamedParameter("employeeId", employeeId);
      scholarshipSummaryQry.setNamedParameter("originaldecId", originaldecId);
      scholarshipSummaryList = scholarshipSummaryQry.list();
      if (scholarshipSummaryList.size() > 0) {
        scholarshipSummary = scholarshipSummaryList.get(0);
        return scholarshipSummary;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in getActiveScholarshipSummary " + e.getMessage());
      e.printStackTrace();
    }
    return scholarshipSummary;
  }

  public EHCMScholarshipSummary updatePaymentFlag(String employeeId, String originaldecId,
      boolean reactive) {

    EHCMScholarshipSummary scholarshipSummary = null;
    List<EHCMScholarshipSummary> scholarshipSummaryList = new ArrayList<EHCMScholarshipSummary>();
    OBQuery<EHCMScholarshipSummary> scholarshipSummaryQry = null;

    try {
      scholarshipSummaryQry = OBDal.getInstance().createQuery(EHCMScholarshipSummary.class,
          " as e where  e.employee.id=:employeeId and e.ehcmEmpScholarship.id = :originaldecId    ");
      scholarshipSummaryQry.setNamedParameter("employeeId", employeeId);
      scholarshipSummaryQry.setNamedParameter("originaldecId", originaldecId);
      scholarshipSummaryList = scholarshipSummaryQry.list();
      if (scholarshipSummaryList.size() > 0) {
        scholarshipSummary = scholarshipSummaryList.get(0);
        if (!reactive)
          scholarshipSummary.setEhcmIspayment(true);
        else
          scholarshipSummary.setEhcmIspayment(false);
        OBDal.getInstance().save(scholarshipSummary);
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in updatePaymentFlag " + e.getMessage());
      e.printStackTrace();
    }
    return scholarshipSummary;
  }

  public void removeScholarshipInfo(String employeeId, String originaldecId) {

    List<EHCMScholarshipSummary> scholarshipSummaryList = new ArrayList<EHCMScholarshipSummary>();
    OBQuery<EHCMScholarshipSummary> scholarshipSummaryQuery = null;

    try {
      scholarshipSummaryQuery = OBDal.getInstance().createQuery(EHCMScholarshipSummary.class,
          " as e where  e.employee.id=:employeeId and e.ehcmEmpScholarship.id = :originaldecId    ");
      scholarshipSummaryQuery.setNamedParameter("employeeId", employeeId);
      scholarshipSummaryQuery.setNamedParameter("originaldecId", originaldecId);
      scholarshipSummaryList = scholarshipSummaryQuery.list();
      if (scholarshipSummaryList.size() > 0) {
        EHCMScholarshipSummary scholarSummary = scholarshipSummaryList.get(0);
        OBDal.getInstance().remove(scholarSummary);
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in removeScholarshipInfo " + e.getMessage());
      e.printStackTrace();
    }

  }

  public EHCMScholarshipSummary updateScholarshipSummary(String employeeId,
      EHCMEmpScholarship scholarshipObj) {

    EHCMScholarshipSummary scholarSummary = null;
    List<EHCMScholarshipSummary> scholarshipSummaryList = new ArrayList<EHCMScholarshipSummary>();
    OBQuery<EHCMScholarshipSummary> scholarShipQry = null;
    String originaldecId = scholarshipObj.getOriginalDecisionNo().getId();
    String decisionNo = scholarshipObj.getOriginalDecisionNo().getDecisionNo();
    try {
      scholarShipQry = OBDal.getInstance().createQuery(EHCMScholarshipSummary.class,
          " as e where  e.employee.id=:employeeId and e.ehcmEmpScholarship.id = :scholarshipId    ");
      scholarShipQry.setNamedParameter("employeeId", employeeId);
      scholarShipQry.setNamedParameter("scholarshipId", scholarshipObj.getId());
      scholarshipSummaryList = scholarShipQry.list();
      if (scholarshipSummaryList.size() > 0) {
        scholarSummary = scholarshipSummaryList.get(0);
        scholarSummary.setEhcmEmpScholarship(
            OBDal.getInstance().get(EHCMEmpScholarship.class, originaldecId));
        scholarSummary.setDecisionNo(decisionNo);
        scholarSummary.setStartDate(scholarshipObj.getOriginalDecisionNo().getStartDate());
        scholarSummary.setEndDate(scholarshipObj.getOriginalDecisionNo().getEndDate());
        scholarSummary.setDecisionType(scholarshipObj.getOriginalDecisionNo().getDecisionType());
        OBDal.getInstance().save(scholarSummary);
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in updateScholarshipSummary " + e.getMessage());
      e.printStackTrace();
    }
    return scholarSummary;
  }
}
