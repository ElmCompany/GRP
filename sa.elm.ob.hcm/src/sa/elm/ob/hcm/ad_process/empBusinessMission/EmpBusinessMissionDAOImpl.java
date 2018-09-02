package sa.elm.ob.hcm.ad_process.empBusinessMission;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EHCMBusMissionSummary;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMMisCatPeriod;
import sa.elm.ob.hcm.EHCMMiscatEmployee;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;

/**
 * This process class used for Business MissionDAO Implementation
 * 
 * @author divya 28-02-2018
 *
 */
@Repository
public class EmpBusinessMissionDAOImpl implements EmpBusinessMissionDAO {

  private Connection connection = null;
  private static final Logger log = LoggerFactory.getLogger(EmpBusinessMissionDAOImpl.class);
  public static final String DECISION_STATUS_ISSUED = "I";

  public EmpBusinessMissionDAOImpl() {
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

  public static EmploymentInfo getActiveEmployInfo(String employeeId) {
    OBQuery<EmploymentInfo> empInfo = null;
    EmploymentInfo empinfo = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    try {
      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and changereason='H'");
      empInfo.setNamedParameter("employeeId", employeeId);
      empInfo.setFilterOnReadableClients(false);
      empInfo.setFilterOnReadableOrganization(false);
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        empinfo = employmentInfo.get(0);
        return empinfo;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in getActiveEmployInfo " + e.getMessage());
      e.printStackTrace();
    }
    return empinfo;
  }

  public EHCMBusMissionSummary getActEmpBusinessMissSummary(
      EHCMEmpBusinessMission empBusinessMission) throws Exception {
    String sql = null;
    OBQuery<EHCMBusMissionSummary> empBusmissSummaryQry = null;
    List<EHCMBusMissionSummary> empBusmissSummaryList = new ArrayList<EHCMBusMissionSummary>();
    EHCMBusMissionSummary empBusmissSummary = null;
    try {
      if (empBusinessMission.getOriginalDecisionNo() != null) {
        sql = " as e where e.employee.id=:employeeId   ";

        sql += "  and e.ehcmEmpBusinessmission.id=:originaldeicionnoId  ";

        log.debug("sql:" + sql);
        // get Scholarship summary Information by passing the corresponding employee id.
        empBusmissSummaryQry = OBDal.getInstance().createQuery(EHCMBusMissionSummary.class, sql);
        empBusmissSummaryQry.setNamedParameter("employeeId",
            empBusinessMission.getEmployee().getId());

        empBusmissSummaryQry.setNamedParameter("originaldeicionnoId",
            empBusinessMission.getOriginalDecisionNo().getId());

        empBusmissSummaryQry.setMaxResult(1);
        empBusmissSummaryList = empBusmissSummaryQry.list();
        if (empBusmissSummaryList.size() > 0) {
          empBusmissSummary = empBusmissSummaryList.get(0);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in getActEmpBusinessMissSummary in EmpBusinessMissionDAO: ", e);
    }
    return empBusmissSummary;

  }

  public void updateEmpBusMissionStatus(EHCMEmpBusinessMission empBusinessMission)
      throws Exception {
    // TODO Auto-generated method stub
    try {
      // update status as Issued and set decision date for all cases
      OBContext.setAdminMode();
      empBusinessMission.setUpdated(new java.util.Date());
      empBusinessMission.setSueDecision(true);
      empBusinessMission.setDecisionDate(new Date());
      empBusinessMission.setDecisionStatus(DECISION_STATUS_ISSUED);
      OBDal.getInstance().save(empBusinessMission);
    } catch (OBException e) {
      e.printStackTrace();
      log.error("Exception in updateEmpBusMissionStatus in EmpBusinessMissionDAOImpl: ", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void insertBusMissionSummary(EHCMEmpBusinessMission empBusinessMission,
      EHCMBusMissionSummary busMissionSummary, String userId, String decisionType)
      throws Exception {
    // TODO Auto-generated method stub
    Date dateafter = null;
    try {
      OBContext.setAdminMode();
      dateafter = new Date(empBusinessMission.getEndDate().getTime() + 1 * 24 * 3600 * 1000);
      EHCMBusMissionSummary busMissSummary = OBProvider.getInstance()
          .get(EHCMBusMissionSummary.class);
      busMissSummary.setClient(empBusinessMission.getClient());
      busMissSummary.setOrganization(empBusinessMission.getOrganization());
      busMissSummary.setCreatedBy(OBDal.getInstance().get(User.class, userId));
      busMissSummary.setCreationDate(new java.util.Date());
      busMissSummary.setUpdated(new java.util.Date());
      busMissSummary.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
      busMissSummary.setEmployee(empBusinessMission.getEmployee());
      busMissSummary.setEhcmEmpBusinessmission(empBusinessMission);
      busMissSummary.setDecisionType(decisionType);
      busMissSummary.setDecisionDate(new Date());
      busMissSummary.setEnabled(true);
      busMissSummary.setDecisionNo(empBusinessMission.getDecisionNo());

      busMissSummary.setStartDate(empBusinessMission.getStartDate());
      busMissSummary.setEndDate(empBusinessMission.getEndDate());

      OBDal.getInstance().save(busMissSummary);
      // update for old info record as inactive
      // updateActiveFlagforOldRecord(busMissionSummary, vars, empBusinessMission, decisionType);
      // OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in inserBusinessMisionInfo in EmpBusinessMissionDAOImpl: ", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void updateActiveFlagforOldRecord(EHCMBusMissionSummary busMissSummary,
      VariablesSecureApp vars, EHCMEmpBusinessMission empBusinessMission, String decisionType) {
    // TODO Auto-generated method stub
    try {
      if (busMissSummary != null) {
        busMissSummary.setUpdated(new java.util.Date());
        busMissSummary.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        busMissSummary.setEnabled(false);
        /*
         * if(empScholarShip.getDecisionType().equals("CO")) {
         * busMissSummary.setEndDate(empScholarShip.getEndDate()); }
         */
        OBDal.getInstance().save(busMissSummary);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateActiveFlagforOldRecord in EmpBusinessMissionDAOImpl: ", e);
    }
  }

  public void updateBusMissionSummary(EHCMEmpBusinessMission empBusinessMission,
      EHCMBusMissionSummary busMissSummary, String decisiontype) {
    // TODO Auto-generated method stub
    try {

      if (decisiontype.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
          || decisiontype.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
          || decisiontype.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        busMissSummary.setStartDate(empBusinessMission.getStartDate());
        busMissSummary.setEndDate(empBusinessMission.getEndDate());
        busMissSummary.setDecisionType(empBusinessMission.getDecisionType());
        busMissSummary.setDecisionNo(empBusinessMission.getDecisionNo());
        busMissSummary.setDecisionDate(new Date());
        busMissSummary.setEhcmEmpBusinessmission(empBusinessMission);
        OBDal.getInstance().save(busMissSummary);

      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateBusMissionSummary in EmpBusinessMissionDAOImpl: ", e);
    }
  }

  public void updateOldEmpBusinessMissionInAct(EHCMEmpBusinessMission empBusinessMission) {
    // TODO Auto-generated method stub
    try {
      // update old scholarship as inactive
      EHCMEmpBusinessMission oldEmpBusinessMission = empBusinessMission.getOriginalDecisionNo();
      oldEmpBusinessMission.setEnabled(false);
      OBDal.getInstance().save(oldEmpBusinessMission);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateOldEmpBusinessMissionInAct in EmpBusinessMissionDAOImpl: ", e);
    }
  }

  public void removebusinessMissionActRecord(EHCMEmpBusinessMission empBusinessMission) {
    // TODO Auto-generated method stub
    List<EHCMBusMissionSummary> busMissionSummaryList = new ArrayList<EHCMBusMissionSummary>();
    try {
      // remove the recent record
      OBQuery<EHCMBusMissionSummary> busMissionSummaryQry = OBDal.getInstance().createQuery(
          EHCMBusMissionSummary.class,
          " as e where  e.employee.id=:employeeId   and e.enabled='Y'  and e.ehcmEmpBusinessmission.id =:empBusinessMissionId  "
              + "  order by creationDate desc");
      busMissionSummaryQry.setNamedParameter("employeeId",
          empBusinessMission.getEmployee().getId());
      busMissionSummaryQry.setNamedParameter("empBusinessMissionId",
          empBusinessMission.getOriginalDecisionNo().getId());
      busMissionSummaryQry.setMaxResult(1);
      busMissionSummaryList = busMissionSummaryQry.list();
      if (busMissionSummaryList.size() > 0) {
        EHCMBusMissionSummary busMissionSummary = busMissionSummaryList.get(0);
        OBDal.getInstance().remove(busMissionSummary);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateOldEmpScholarshipInAct in EmpBusinessMissionDAOImpl: ", e);
    }
  }

  public static EHCMBusMissionSummary getActiveBusMissionSummary(String employeeId,
      String originalDecId) {
    EHCMBusMissionSummary busMissSummary = null;
    List<EHCMBusMissionSummary> busMissionSummaryList = new ArrayList<EHCMBusMissionSummary>();
    OBQuery<EHCMBusMissionSummary> busMissSummaryQry = null;

    try {

      busMissSummaryQry = OBDal.getInstance().createQuery(EHCMBusMissionSummary.class,
          " as e where  e.employee.id=:employeeId  and e.ehcmEmpBusinessmission.id  = :originaldecId order by creationDate desc   ");// and
      // e.enabled='Y'
      busMissSummaryQry.setNamedParameter("employeeId", employeeId);
      busMissSummaryQry.setNamedParameter("originaldecId", originalDecId);
      busMissionSummaryList = busMissSummaryQry.list();
      if (busMissionSummaryList.size() > 0) {
        busMissSummary = busMissionSummaryList.get(0);
        return busMissSummary;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in getActiveBusMissionSummary " + e.getMessage());
      e.printStackTrace();
    }
    return busMissSummary;
  }

  public EHCMBusMissionSummary updatePaymentFlag(String employeeId, String originaldecId,
      boolean reactive) {

    EHCMBusMissionSummary busMissSummary = null;
    List<EHCMBusMissionSummary> busMissionSummaryList = new ArrayList<EHCMBusMissionSummary>();
    OBQuery<EHCMBusMissionSummary> busMissSummaryQry = null;

    try {
      busMissSummaryQry = OBDal.getInstance().createQuery(EHCMBusMissionSummary.class,
          " as e where  e.employee.id=:employeeId and e.ehcmEmpBusinessmission.id = :originaldecId    ");
      busMissSummaryQry.setNamedParameter("employeeId", employeeId);
      busMissSummaryQry.setNamedParameter("originaldecId", originaldecId);
      busMissionSummaryList = busMissSummaryQry.list();
      if (busMissionSummaryList.size() > 0) {
        busMissSummary = busMissionSummaryList.get(0);
        if (!reactive)
          busMissSummary.setEhcmIspayment(true);
        else
          busMissSummary.setEhcmIspayment(false);
        OBDal.getInstance().save(busMissSummary);
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in updatePaymentFlag " + e.getMessage());
      e.printStackTrace();
    }
    return busMissSummary;
  }

  public EHCMBusMissionSummary updateBusinessMissionSummary(String employeeId,
      EHCMEmpBusinessMission BusinessMissionObj) {

    EHCMBusMissionSummary busMissSummary = null;
    List<EHCMBusMissionSummary> busMissionSummaryList = new ArrayList<EHCMBusMissionSummary>();
    OBQuery<EHCMBusMissionSummary> busMissSummaryQry = null;
    String originaldecId = BusinessMissionObj.getOriginalDecisionNo().getId();
    String decisionNo = BusinessMissionObj.getOriginalDecisionNo().getDecisionNo();
    try {
      busMissSummaryQry = OBDal.getInstance().createQuery(EHCMBusMissionSummary.class,
          " as e where  e.employee.id=:employeeId and e.ehcmEmpBusinessmission.id = :businessMissionId    ");
      busMissSummaryQry.setNamedParameter("employeeId", employeeId);
      busMissSummaryQry.setNamedParameter("businessMissionId", BusinessMissionObj.getId());
      busMissionSummaryList = busMissSummaryQry.list();
      if (busMissionSummaryList.size() > 0) {
        busMissSummary = busMissionSummaryList.get(0);
        busMissSummary.setEhcmEmpBusinessmission(
            OBDal.getInstance().get(EHCMEmpBusinessMission.class, originaldecId));
        busMissSummary.setDecisionNo(decisionNo);
        busMissSummary.setStartDate(BusinessMissionObj.getOriginalDecisionNo().getStartDate());
        busMissSummary.setEndDate(BusinessMissionObj.getOriginalDecisionNo().getEndDate());
        busMissSummary
            .setDecisionType(BusinessMissionObj.getOriginalDecisionNo().getDecisionType());
        OBDal.getInstance().save(busMissSummary);
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in updateBusinessMissionSummary " + e.getMessage());
      e.printStackTrace();
    }
    return busMissSummary;
  }

  public void removeBusinessmissionSummary(String employeeId, String originaldecId) {

    EHCMBusMissionSummary busMissSummary = null;
    List<EHCMBusMissionSummary> busMissionSummaryList = new ArrayList<EHCMBusMissionSummary>();
    OBQuery<EHCMBusMissionSummary> busMissSummaryQry = null;

    try {
      busMissSummaryQry = OBDal.getInstance().createQuery(EHCMBusMissionSummary.class,
          " as e where  e.employee.id=:employeeId and e.ehcmEmpBusinessmission.id = :originaldecId    ");
      busMissSummaryQry.setNamedParameter("employeeId", employeeId);
      busMissSummaryQry.setNamedParameter("originaldecId", originaldecId);
      busMissionSummaryList = busMissSummaryQry.list();
      if (busMissionSummaryList.size() > 0) {
        EHCMBusMissionSummary busMissionSummary = busMissionSummaryList.get(0);
        OBDal.getInstance().remove(busMissionSummary);
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in updatePaymentFlag " + e.getMessage());
      e.printStackTrace();
    }

  }

  public static EHCMBusMissionSummary getPreviousBusMissionRecord(
      EHCMEmpBusinessMission empBusinessMission) {
    // TODO Auto-generated method stub
    EHCMBusMissionSummary busMisSummary = null;
    List<EHCMBusMissionSummary> busMissionsummaryList = new ArrayList<EHCMBusMissionSummary>();
    try {
      OBQuery<EHCMBusMissionSummary> busMissionSummary = OBDal.getInstance().createQuery(
          EHCMBusMissionSummary.class,
          "as e where e.employee.id=:employeeId and e.ehcmEmpBusinessmission.id <>:empBusinessMissionId   order by creationDate desc");
      busMissionSummary.setNamedParameter("employeeId", empBusinessMission.getEmployee().getId());
      busMissionSummary.setNamedParameter("empBusinessMissionId", empBusinessMission.getId());
      busMissionSummary.setMaxResult(1);
      busMissionsummaryList = busMissionSummary.list();
      if (busMissionsummaryList.size() > 0) {
        busMisSummary = busMissionsummaryList.get(0);

      }
      return busMisSummary;
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in getPreviousBusMissionRecord in EmpBusinessMissionDAOImpl: ", e);
    }
    return busMisSummary;
  }

  public void updateMissionBalance(EHCMMiscatEmployee misCatEmp, String decisionType,
      EHCMEmpBusinessMission empBusinessMission, String userId) {
    // TODO Auto-generated method stub
    Long diff = (long) 0;
    List<EHCMMiscatEmployee> misCatEmployeeList = new ArrayList<EHCMMiscatEmployee>();
    EHCMMisCatPeriod originalMisCatPrd = null;
    try {
      if (misCatEmp != null) {
        misCatEmp.setUpdated(new java.util.Date());
        misCatEmp.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
        if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          misCatEmp.setUseddays(misCatEmp.getUseddays() + empBusinessMission.getMissionDays());
        }
        if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
          misCatEmp.setUseddays(misCatEmp.getUseddays() + empBusinessMission.getExtendMissionDay());
        }
        if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
          misCatEmp.setUseddays(misCatEmp.getUseddays()
              - empBusinessMission.getOriginalDecisionNo().getMissionDays());
        }
        log.debug(" misCatEmp" + misCatEmp.getUseddays());
        /*
         * if (empBusinessMission.getOriginalDecisionNo() == null) { originalMisCatPrd =
         * sa.elm.ob.hcm.util.UtilityDAO.getMissionBalance( empBusinessMission.getClient().getId(),
         * empBusinessMission.getMissionCategory(),
         * sa.elm.ob.utility.util.Utility.formatDate(empBusinessMission.getStartDate()),
         * sa.elm.ob.utility.util.Utility.formatDate(empBusinessMission.getEndDate()));
         * 
         * } else {
         */
        if (empBusinessMission.getOriginalDecisionNo() != null) {
          originalMisCatPrd = sa.elm.ob.hcm.util.UtilityDAO.getMissionPeriod(
              empBusinessMission.getClient().getId(),
              empBusinessMission.getOriginalDecisionNo().getMissionCategory(),
              sa.elm.ob.utility.util.Utility
                  .formatDate(empBusinessMission.getOriginalDecisionNo().getStartDate()),
              sa.elm.ob.utility.util.Utility
                  .formatDate(empBusinessMission.getOriginalDecisionNo().getEndDate()));

          if ((decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
              && originalMisCatPrd != null
              && originalMisCatPrd.getId().equals(misCatEmp.getEhcmMiscatPeriod().getId()))
              || decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
            diff = empBusinessMission.getMissionDays()
                - empBusinessMission.getOriginalDecisionNo().getMissionDays();
            misCatEmp.setUseddays(misCatEmp.getUseddays() + diff);
          }

          if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
              && originalMisCatPrd != null
              && !originalMisCatPrd.getId().equals(misCatEmp.getEhcmMiscatPeriod().getId())) {

            diff = empBusinessMission.getMissionDays();
            misCatEmp.setUseddays(misCatEmp.getUseddays() + diff);

            OBQuery<EHCMMiscatEmployee> misCatQry = OBDal.getInstance().createQuery(
                EHCMMiscatEmployee.class,
                " as e where e.ehcmMiscatPeriod.id=:origmisCatPerd and e.employee.id=:employeeId ");
            misCatQry.setNamedParameter("origmisCatPerd", originalMisCatPrd);
            misCatQry.setNamedParameter("employeeId", empBusinessMission.getEmployee().getId());
            misCatQry.setMaxResult(1);
            misCatEmployeeList = misCatQry.list();
            if (misCatEmployeeList.size() > 0) {
              EHCMMiscatEmployee origDecMisCatEmp = misCatEmployeeList.get(0);
              origDecMisCatEmp.setUseddays(misCatEmp.getUseddays()
                  - empBusinessMission.getOriginalDecisionNo().getMissionDays());
              OBDal.getInstance().save(origDecMisCatEmp);
            }

          }
        }
        OBDal.getInstance().save(misCatEmp);

      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateActiveFlagforOldRecord in EmpBusinessMissionDAOImpl: ", e);
    }
  }

  public boolean checkBusinessmissionAlreadyUsed(String businessmissionId) {
    boolean chkFlag = false;
    try {
      OBQuery<EHCMEmpBusinessMission> businessmissionObj = OBDal.getInstance().createQuery(
          EHCMEmpBusinessMission.class,
          " as e where e.id in (select a.originalDecisionNo.id from EHCM_Emp_BusinessMission a where a.decisionStatus = 'I') and e.id = :businessmissionId");
      businessmissionObj.setNamedParameter("businessmissionId", businessmissionId);
      if (businessmissionObj.list().size() > 0) {
        chkFlag = true;
      }

    } catch (Exception e) {
      log.error("Exception in checkBusinessmissionAlreadyUsed: ", e);
      OBDal.getInstance().rollbackAndClose();
    }
    return chkFlag;
  }

}
