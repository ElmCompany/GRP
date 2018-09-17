package sa.elm.ob.hcm.ad_process.EmployeeSuspension;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.EHCMPayscalePointV;
import sa.elm.ob.hcm.EHCMTerminationReason;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EhcmSuspensionEmpV;
import sa.elm.ob.hcm.EhcmterminationEmpV;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmempstatus;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;

/**
 * 
 * 
 * @author Mouli.K
 *
 */

public class EmpSuspensionReactiveDAO {

  private Connection connection = null;
  private static final Logger log = LoggerFactory.getLogger(EmpSuspensionReactiveDAO.class);

  public EmpSuspensionReactiveDAO() {
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

  public static void cancelEmploymentRecordSUS(EmployeeSuspension objSuspension,
      EmployeeSuspension oldSuspension, EHCMEMPTermination oldTermination,
      VariablesSecureApp vars) {
    // TODO Auto-generated method stub
    EhcmSuspensionEmpV suspensionview = objSuspension.getEhcmEmpPerinfo();
    EhcmEmpPerInfo employee = suspensionview.getEmployee();

    EmploymentInfo oldEmployment = null;
    ehcmempstatus EmpStatusList = null;
    List<EHCMEMPTermination> empTerminationList = null;

    try {

      if (objSuspension.getSuspensionEndReason() != null) {
        if (objSuspension.getSuspensionEndReason().getSearchKey().equals("T")
            || objSuspension.getSuspensionEndReason().getSearchKey().equals("TRD")) {

          OBQuery<ehcmempstatus> EmpStatus = OBDal.getInstance().createQuery(ehcmempstatus.class,
              " as a where  a.ehcmEmpPerinfo.id=:employeeId ");
          EmpStatus.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
          EmpStatus.setFilterOnActive(false);
          EmpStatus.setMaxResult(1);
          log.debug("EmpStatus:" + EmpStatus.getWhereAndOrderBy());
          if (EmpStatus.list().size() > 0) {
            EmpStatusList = EmpStatus.list().get(0);
            OBDal.getInstance().remove(EmpStatusList);
            OBDal.getInstance().flush();
          }

          OBQuery<EHCMEMPTermination> EmpTermination = OBDal.getInstance().createQuery(
              EHCMEMPTermination.class, " as a where  a.ehcmEmpPerinfo.id=:employeeId ");
          EmpTermination.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
          EmpTermination.setFilterOnActive(false);
          EmpTermination.setMaxResult(1);
          log.debug("EmpTermination:" + EmpTermination.getWhereAndOrderBy());
          empTerminationList = EmpTermination.list();
          if (empTerminationList.size() > 0) {

            EHCMEMPTermination empTermination = empTerminationList.get(0);
            empTermination.setDecisionStatus("UP");
            OBDal.getInstance().remove(empTermination);
            OBDal.getInstance().flush();
          }

          employee.setEmploymentStatus("SD");
          employee.setEnabled(false);
        } else {
          employee.setEmploymentStatus("AC");
        }
        OBDal.getInstance().flush();
      }

      OBQuery<EmploymentInfo> empInfoq1 = OBDal.getInstance()
          .createQuery(EmploymentInfo.class, " as a  where a.ehcmEmpSuspension.id='"
              + objSuspension.getId()
              + "' and a.ehcmEmpPerinfo.id=:employeeId and a.enabled='Y'  order by a.creationDate desc");
      empInfoq1.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
      empInfoq1.setMaxResult(1);
      log.debug("empInfoq1:" + empInfoq1.getWhereAndOrderBy());
      if (empInfoq1.list().size() > 0) {
        oldEmployment = empInfoq1.list().get(0);
        OBDal.getInstance().remove(oldEmployment);
        OBDal.getInstance().flush();
      }

      OBQuery<EmploymentInfo> empInfoq2 = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ( ehcmEmpSuspension.id<>'" + objSuspension.getId()
              + "'or ehcmEmpSuspension.id is null)  and ehcmEmpPerinfo.id=:employeeId order by creationDate desc");
      empInfoq2.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
      empInfoq2.setFilterOnActive(false);
      empInfoq2.setMaxResult(1);
      if (empInfoq2.list().size() > 0) {
        oldEmployment = empInfoq2.list().get(0);
        oldEmployment.setEndDate(null);
        oldEmployment.setAlertStatus("ACT");
        oldEmployment.setEnabled(true);
        OBDal.getInstance().save(oldEmployment);
        OBDal.getInstance().flush();
      }

    } catch (

    Exception e) {
      e.printStackTrace();
      log.error("Exception in cancelEmploymentRecord in EmpSuspensionReactiveDAO: ", e);
    }

  }

  public static void updateEmploymentRecordSUS(EmployeeSuspension objSuspension,
      EmployeeSuspension oldSuspension, EHCMEMPTermination oldTermination, VariablesSecureApp vars)
      throws Exception {
    // TODO Auto-generated method stub

    EmploymentInfo oldEmployment = null;
    int millSec = 1 * 24 * 3600 * 1000;

    try {

      OBQuery<EmploymentInfo> empInfoq2 = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ( ehcmEmpSuspension.id<>'" + objSuspension.getId()
              + "' or ehcmEmpSuspension.id is null)   and enabled='N' and ehcmEmpPerinfo.id=:employeeId order by creationDate desc");
      empInfoq2.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
      empInfoq2.setFilterOnActive(false);
      empInfoq2.setMaxResult(1);
      if (empInfoq2.list().size() > 0) {
        oldEmployment = empInfoq2.list().get(0);
        oldEmployment.setEndDate(new Date(oldSuspension.getStartDate().getTime() - millSec));
        OBDal.getInstance().save(oldEmployment);
      }

      OBQuery<EmploymentInfo> empInfoq1 = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ( ehcmEmpSuspension.id='" + objSuspension.getId()
              + "'or ehcmEmpSuspension.id is null)  and ehcmEmpPerinfo.id=:employeeId order by creationDate desc");
      empInfoq1.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
      empInfoq1.setFilterOnActive(false);
      empInfoq1.setMaxResult(1);
      if (empInfoq1.list().size() > 0) {
        oldEmployment = empInfoq1.list().get(0);
        oldEmployment.setStartDate(oldSuspension.getStartDate());
        oldEmployment.setEhcmEmpSuspension(oldSuspension);
        oldEmployment.setDecisionNo(oldSuspension.getDecisionNo());
        oldEmployment.setDecisionDate(oldSuspension.getDecisionDate());
        OBDal.getInstance().save(oldEmployment);
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateEmploymentRecord in EmpSuspensionReactiveDAO: ", e);
    }

  }

  public static void insertEmploymentRecordSUS(EmployeeSuspension objSuspension,
      EmployeeSuspension oldSuspension, EHCMEMPTermination oldTermination, VariablesSecureApp vars)
      throws Exception {
    // TODO Auto-generated method stub

    EmploymentInfo oldEmployment = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();

    int millSec = 1 * 24 * 3600 * 1000;

    try {

      // clone new Record in Employement Info
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and enabled='Y'  and alertStatus='ACT' order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
      empInfo.setMaxResult(1);
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        EmploymentInfo empinfo = employmentInfo.get(0);
        EmploymentInfo objCloneEmplyment = (EmploymentInfo) DalUtil.copy(empinfo, false);

        objCloneEmplyment.setStartDate(oldSuspension.getStartDate());
        objCloneEmplyment.setDecisionDate(oldSuspension.getDecisionDate());
        objCloneEmplyment.setDecisionNo(oldSuspension.getDecisionNo());
        objCloneEmplyment.setEhcmEmpSuspension(oldSuspension);
        objCloneEmplyment.setCreationDate(new Date());
        objCloneEmplyment.setUpdated(new Date());
        objCloneEmplyment.setAlertStatus("ACT");
        objCloneEmplyment.setEnabled(true);
        objCloneEmplyment.setChangereason(oldSuspension.getSuspensionType());
        objCloneEmplyment.setChangereasoninfo(oldSuspension.getSuspensionReason().getSearchKey());
        OBDal.getInstance().save(objCloneEmplyment);
        OBDal.getInstance().flush();

        OBQuery<EmploymentInfo> empInfoq1 = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ( ehcmEmpSuspension.id='" + objSuspension.getId()
                + "'or ehcmEmpSuspension.id is null)  and ehcmEmpPerinfo.id=:employeeId order by creationDate desc");
        empInfoq1.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
        empInfoq1.setFilterOnActive(false);
        empInfoq1.setMaxResult(1);
        if (empInfoq1.list().size() > 0) {
          oldEmployment = empInfoq1.list().get(0);
          oldEmployment.setEndDate(new Date(oldSuspension.getStartDate().getTime() - millSec));
          oldEmployment.setAlertStatus("INACT");
          oldEmployment.setEnabled(false);
          OBDal.getInstance().save(oldEmployment);
          OBDal.getInstance().flush();
        }
      }

    } catch (

    Exception e) {
      e.printStackTrace();
      log.error("Exception in insertEmploymentRecord in EmpSuspensionReactiveDAO: ", e);
    }

  }

  public static void updEmpRcdSUE(EmployeeSuspension objSuspension,
      EmployeeSuspension oldSuspension, EHCMEMPTermination oldTermination,
      VariablesSecureApp vars) {
    // TODO Auto-generated method stub
    EhcmSuspensionEmpV suspensionView = objSuspension.getEhcmEmpPerinfo();
    EhcmEmpPerInfo employee = suspensionView.getEmployee();
    AssingedOrReleaseEmpInPositionDAO positionEmpHist = new AssingedOrReleaseEmpInPositionDAOImpl();

    EmploymentInfo oldEmployment = null;
    ehcmempstatus EmpStatusList = null;
    int millSec = 1 * 24 * 3600 * 1000;
    List<EHCMEMPTermination> empTerminationList = null;

    try {

      OBQuery<EmploymentInfo> empInfoq1 = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "  ehcmEmpSuspension.id='" + objSuspension.getId()
              + "'and ehcmEmpPerinfo.id=:employeeId");
      empInfoq1.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
      // empInfoq1.setFilterOnActive(false);
      empInfoq1.setMaxResult(1);
      if (empInfoq1.list().size() > 0) {
        oldEmployment = empInfoq1.list().get(0);
        oldEmployment.setStartDate(new Date(oldSuspension.getEndDate().getTime() + millSec));
        oldEmployment.setEhcmEmpSuspension(oldSuspension);
        oldEmployment.setDecisionNo(oldSuspension.getDecisionNo());
        oldEmployment.setDecisionDate(oldSuspension.getDecisionDate());
        oldEmployment.setChangereason(oldSuspension.getSuspensionType());
        oldEmployment.setChangereasoninfo(oldSuspension.getSuspensionEndReason().getSearchKey());
        OBDal.getInstance().save(oldEmployment);
        OBDal.getInstance().flush();
      }

      if (oldSuspension.getSuspensionEndReason() != null) {
        if (oldSuspension.getSuspensionEndReason().getSearchKey().equals("T")
            || oldSuspension.getSuspensionEndReason().getSearchKey().equals("TRD")) {

          EHCMEMPTermination objEmpTermination = insertRecordinTermination(oldSuspension,
              oldEmployment);
          EmpSuspensionHandlerDAO.insertRecordInEmpStatus(oldSuspension);

          // position release
          EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
              oldSuspension.getPosition().getId());

          // update position employee history records with end date for assigned emp
          positionEmpHist.updateEndDateInPositionEmployeeHisotry(suspensionView.getEmployee(),
              oldSuspension.getPosition(), oldSuspension.getEndDate(), null, null, null, null,
              oldSuspension, null, null);

          // update position employee history records with end date for delegated emp
          positionEmpHist.updateEndDateForDelegatedEmployee(suspensionView.getEmployee(),
              oldSuspension.getEndDate(), oldSuspension, null, oldSuspension.getDecisionType());

          OBDal.getInstance().save(pos);
          OBDal.getInstance().flush();

          // make termination to inactive to avoid update or cancel
          objEmpTermination.setEnabled(false);
          OBDal.getInstance().save(objEmpTermination);

          employee.setEmploymentStatus("SD");
          employee.setEnabled(false);

        } else {

          // ---------------
          if (objSuspension.getSuspensionEndReason().getSearchKey().equals("T")
              || objSuspension.getSuspensionEndReason().getSearchKey().equals("TRD")) {

            OBQuery<ehcmempstatus> EmpStatus = OBDal.getInstance().createQuery(ehcmempstatus.class,
                " as a where  a.ehcmEmpPerinfo.id=:employeeId ");
            EmpStatus.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
            EmpStatus.setFilterOnActive(false);
            EmpStatus.setMaxResult(1);
            log.debug("EmpStatus:" + EmpStatus.getWhereAndOrderBy());
            if (EmpStatus.list().size() > 0) {
              EmpStatusList = EmpStatus.list().get(0);
              OBDal.getInstance().remove(EmpStatusList);
              OBDal.getInstance().flush();
            }

            OBQuery<EHCMEMPTermination> EmpTermination = OBDal.getInstance().createQuery(
                EHCMEMPTermination.class, " as a where  a.ehcmEmpPerinfo.id=:employeeId ");
            EmpTermination.setNamedParameter("employeeId",
                objSuspension.getEhcmEmpPerinfo().getId());
            EmpTermination.setFilterOnActive(false);
            EmpTermination.setMaxResult(1);
            log.debug("EmpTermination:" + EmpTermination.getWhereAndOrderBy());
            empTerminationList = EmpTermination.list();
            if (empTerminationList.size() > 0) {

              EHCMEMPTermination empTermination = empTerminationList.get(0);
              empTermination.setDecisionStatus("UP");
              OBDal.getInstance().remove(empTermination);
              OBDal.getInstance().flush();
            }

            employee.setEmploymentStatus("SD");
            employee.setEnabled(false);
          }
          // ---------------
          else {
            employee.setEmploymentStatus("AC");
          }
        }
        OBDal.getInstance().flush();
      }

      OBQuery<EmploymentInfo> empInfoq2 = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ( ehcmEmpSuspension.id<>'" + objSuspension.getId()
              + "'or ehcmEmpSuspension.id is null)  and enabled='N' and ehcmEmpPerinfo.id=:employeeId order by creationDate desc");
      empInfoq2.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
      // empInfoq2.setFilterOnActive(false);
      empInfoq2.setMaxResult(1);
      if (empInfoq2.list().size() > 0) {
        oldEmployment = empInfoq2.list().get(0);
        oldEmployment.setEndDate(oldSuspension.getEndDate());
        OBDal.getInstance().save(oldEmployment);
        OBDal.getInstance().flush();
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updEmpRcdSUE in EmpSuspensionReactiveDAO: ", e);
    }

  }

  public static void insEmpRcdSUE(EmployeeSuspension objSuspension,
      EmployeeSuspension oldSuspension, EHCMEMPTermination oldTermination, VariablesSecureApp vars)
      throws Exception {
    // TODO Auto-generated method stub
    EhcmSuspensionEmpV suspensionView = objSuspension.getEhcmEmpPerinfo();
    EhcmEmpPerInfo employee = suspensionView.getEmployee();

    EmploymentInfo oldEmployment = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    ehcmempstatus EmpStatusList = null;

    int millSec = 1 * 24 * 3600 * 1000;
    List<EHCMEMPTermination> empTerminationList = null;

    try {

      if (objSuspension.getSuspensionEndReason() != null) {
        if (objSuspension.getSuspensionEndReason().getSearchKey().equals("T")
            || objSuspension.getSuspensionEndReason().getSearchKey().equals("TRD")) {

          OBQuery<ehcmempstatus> EmpStatus = OBDal.getInstance().createQuery(ehcmempstatus.class,
              " ehcmEmpPerinfo.id='" + objSuspension.getEhcmEmpPerinfo().getId() + "'");
          EmpStatus.setMaxResult(1);
          if (EmpStatus.list().size() > 0) {
            EmpStatusList = EmpStatus.list().get(0);
            OBDal.getInstance().remove(EmpStatusList);
            OBDal.getInstance().flush();
          }

          // ---------
          OBQuery<EHCMEMPTermination> EmpTermination = OBDal.getInstance().createQuery(
              EHCMEMPTermination.class, " as a where  a.ehcmEmpPerinfo.id=:employeeId ");
          EmpTermination.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
          EmpTermination.setFilterOnActive(false);
          EmpTermination.setMaxResult(1);
          log.debug("EmpTermination:" + EmpTermination.getWhereAndOrderBy());
          empTerminationList = EmpTermination.list();
          if (empTerminationList.size() > 0) {

            EHCMEMPTermination empTermination = empTerminationList.get(0);
            empTermination.setDecisionStatus("UP");
            OBDal.getInstance().remove(empTermination);
            OBDal.getInstance().flush();
          }
          // --------

          employee.setEmploymentStatus("SD");
          employee.setEnabled(false);

        } else {
          employee.setEmploymentStatus("AC");
        }
        OBDal.getInstance().flush();
      }

      // clone new Record in Employement Info
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and enabled='Y'  and alertStatus='ACT' order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
      empInfo.setMaxResult(1);
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        EmploymentInfo empinfo = employmentInfo.get(0);
        EmploymentInfo objCloneEmplyment = (EmploymentInfo) DalUtil.copy(empinfo, false);

        objCloneEmplyment
            .setChangereasoninfo(oldSuspension.getSuspensionEndReason().getSearchKey());
        objCloneEmplyment.setChangereason(oldSuspension.getSuspensionType());
        objCloneEmplyment.setStartDate(new Date(oldSuspension.getEndDate().getTime() + millSec));
        objCloneEmplyment.setDecisionDate(oldSuspension.getDecisionDate());
        objCloneEmplyment.setDecisionNo(oldSuspension.getDecisionNo());
        objCloneEmplyment.setEhcmEmpSuspension(oldSuspension);
        objCloneEmplyment.setEnabled(true);
        objCloneEmplyment.setCreationDate(new Date());
        objCloneEmplyment.setUpdated(new Date());
        objCloneEmplyment.setAlertStatus("ACT");
        objCloneEmplyment.setEnabled(true);

        OBDal.getInstance().save(objCloneEmplyment);
        OBDal.getInstance().flush();

        OBQuery<EmploymentInfo> empInfoq1 = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ( ehcmEmpSuspension.id='" + objSuspension.getId()
                + "'or ehcmEmpSuspension.id is null)  and ehcmEmpPerinfo.id=:employeeId order by creationDate desc");
        empInfoq1.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
        empInfoq1.setFilterOnActive(false);
        empInfoq1.setMaxResult(1);
        if (empInfoq1.list().size() > 0) {
          oldEmployment = empInfoq1.list().get(0);
          oldEmployment.setEndDate(oldSuspension.getEndDate());
          oldEmployment.setAlertStatus("INACT");
          oldEmployment.setEnabled(false);
          OBDal.getInstance().save(oldEmployment);
          OBDal.getInstance().flush();
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in insertEmploymentRecord in EmpSuspensionReactiveDAO: ", e);
    }

  }

  public static void cncEmpRcdSUE(EmployeeSuspension objSuspension,
      EmployeeSuspension oldSuspension, EHCMEMPTermination oldTermination,
      VariablesSecureApp vars) {
    // TODO Auto-generated method stub
    EhcmSuspensionEmpV suspensionview = objSuspension.getEhcmEmpPerinfo();
    EhcmEmpPerInfo employee = suspensionview.getEmployee();

    EmploymentInfo oldEmployment = null;
    ehcmempstatus EmpStatusList = null;
    List<EHCMEMPTermination> empTerminationList = null;

    try {

      if (objSuspension.getSuspensionEndReason() != null) {
        if (objSuspension.getSuspensionEndReason().getSearchKey().equals("T")
            || objSuspension.getSuspensionEndReason().getSearchKey().equals("TRD")) {

          OBQuery<ehcmempstatus> EmpStatus = OBDal.getInstance().createQuery(ehcmempstatus.class,
              " ehcmEmpPerinfo.id='" + objSuspension.getEhcmEmpPerinfo().getId() + "'");
          EmpStatus.setMaxResult(1);
          if (EmpStatus.list().size() > 0) {
            EmpStatusList = EmpStatus.list().get(0);
            OBDal.getInstance().remove(EmpStatusList);
            OBDal.getInstance().flush();
          }

          // ----------
          OBQuery<EHCMEMPTermination> EmpTermination = OBDal.getInstance().createQuery(
              EHCMEMPTermination.class, " as a where  a.ehcmEmpPerinfo.id=:employeeId ");
          EmpTermination.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
          EmpTermination.setFilterOnActive(false);
          EmpTermination.setMaxResult(1);
          log.debug("EmpTermination:" + EmpTermination.getWhereAndOrderBy());
          empTerminationList = EmpTermination.list();
          if (empTerminationList.size() > 0) {

            EHCMEMPTermination empTermination = empTerminationList.get(0);
            empTermination.setDecisionStatus("UP");
            OBDal.getInstance().remove(empTermination);
            OBDal.getInstance().flush();
          }
          // ----------

          employee.setEmploymentStatus("SD");
          employee.setEnabled(false);
        } else {
          employee.setEmploymentStatus("AC");
        }
        OBDal.getInstance().flush();
      }

      OBQuery<EmploymentInfo> empInfoq1 = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ( ehcmEmpSuspension.id='" + objSuspension.getId()
              + "'or ehcmEmpSuspension.id is null)  and ehcmEmpPerinfo.id=:employeeId order by creationDate desc");
      empInfoq1.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
      empInfoq1.setFilterOnActive(false);
      empInfoq1.setMaxResult(1);
      if (empInfoq1.list().size() > 0) {
        oldEmployment = empInfoq1.list().get(0);
        OBDal.getInstance().remove(oldEmployment);
        OBDal.getInstance().flush();
      }

      OBQuery<EmploymentInfo> empInfoq2 = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ( ehcmEmpSuspension.id<>'" + objSuspension.getId()
              + "'or ehcmEmpSuspension.id is null)  and ehcmEmpPerinfo.id=:employeeId order by creationDate desc");
      empInfoq2.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
      empInfoq2.setFilterOnActive(false);
      empInfoq2.setMaxResult(1);
      if (empInfoq2.list().size() > 0) {
        oldEmployment = empInfoq2.list().get(0);
        oldEmployment.setEndDate(null);
        oldEmployment.setAlertStatus("ACT");
        oldEmployment.setEnabled(true);
        OBDal.getInstance().save(oldEmployment);
        OBDal.getInstance().flush();

      }

    } catch (

    Exception e) {
      e.printStackTrace();
      log.error("Exception in cancelEmploymentRecord in EmpSuspensionReactiveDAO: ", e);
    }

  }

  public static void insertEmploymentRecordSUE(EmployeeSuspension objSuspension,
      EmployeeSuspension oldSuspension, EHCMEMPTermination oldTermination, VariablesSecureApp vars)
      throws Exception {
    // TODO Auto-generated method stub
    EhcmSuspensionEmpV suspensionView = objSuspension.getEhcmEmpPerinfo();
    EhcmEmpPerInfo employee = suspensionView.getEmployee();
    AssingedOrReleaseEmpInPositionDAO positionEmpHist = new AssingedOrReleaseEmpInPositionDAOImpl();

    EmploymentInfo oldEmployment = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    List<EmployeeSuspension> empSusList = new ArrayList<EmployeeSuspension>();
    EmployeeSuspension objSuspensionT = objSuspension;

    int millSec = 1 * 24 * 3600 * 1000;

    try {

      OBQuery<EmployeeSuspension> empSus = OBDal.getInstance().createQuery(EmployeeSuspension.class,
          " enabled='Y' and ehcmEmpPerinfo.id=:employeeId order by creationDate desc");
      empSus.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
      empSusList = empSus.list();
      if (empSusList.size() > 0) {
        EmployeeSuspension EmpSUS = empSusList.get(0);
        EmpSUS.setEnabled(false);
        objSuspensionT = EmpSUS;

        OBDal.getInstance().save(EmpSUS);
        OBDal.getInstance().flush();
      }

      // clone new Record in Employement Info
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and enabled='Y'  and alertStatus='ACT' order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", objSuspensionT.getEhcmEmpPerinfo().getId());
      empInfo.setMaxResult(1);
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        EmploymentInfo empinfo = employmentInfo.get(0);
        EmploymentInfo objCloneEmplyment = (EmploymentInfo) DalUtil.copy(empinfo, false);

        objCloneEmplyment.setStartDate(new Date(oldSuspension.getEndDate().getTime() + millSec));
        objCloneEmplyment.setDecisionDate(oldSuspension.getDecisionDate());
        objCloneEmplyment.setDecisionNo(oldSuspension.getDecisionNo());
        objCloneEmplyment.setEhcmEmpSuspension(oldSuspension);
        objCloneEmplyment.setEnabled(true);
        objCloneEmplyment.setCreationDate(new Date());
        objCloneEmplyment.setUpdated(new Date());
        objCloneEmplyment.setAlertStatus("ACT");
        objCloneEmplyment.setEnabled(true);
        objCloneEmplyment.setChangereason(oldSuspension.getSuspensionType());
        objCloneEmplyment
            .setChangereasoninfo(oldSuspension.getSuspensionEndReason().getSearchKey());
        OBDal.getInstance().save(objCloneEmplyment);
        OBDal.getInstance().flush();

        if (objSuspension.getSuspensionEndReason() != null) {
          if (objSuspension.getSuspensionEndReason().getSearchKey().equals("T")
              || objSuspension.getSuspensionEndReason().getSearchKey().equals("TRD")) {

            EHCMEMPTermination objEmpTermination = insertRecordinTermination(oldSuspension,
                objCloneEmplyment);
            EmpSuspensionHandlerDAO.insertRecordInEmpStatus(oldSuspension);

            // position release
            EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
                oldSuspension.getPosition().getId());

            // update position employee history records with end date for assigned emp
            positionEmpHist.updateEndDateInPositionEmployeeHisotry(suspensionView.getEmployee(),
                oldSuspension.getPosition(), oldSuspension.getEndDate(), null, null, null, null,
                oldSuspension, null, null);

            // update position employee history records with end date for delegated emp
            positionEmpHist.updateEndDateForDelegatedEmployee(suspensionView.getEmployee(),
                oldSuspension.getEndDate(), oldSuspension, null, oldSuspension.getDecisionType());

            OBDal.getInstance().save(pos);
            OBDal.getInstance().flush();

            // make termination to inactive to avoid update or cancel
            objEmpTermination.setEnabled(false);
            OBDal.getInstance().save(objEmpTermination);

            employee.setEmploymentStatus("SD");
            employee.setEnabled(false);
          } else {
            employee.setEmploymentStatus("AC");
          }
          OBDal.getInstance().flush();
        }

        OBQuery<EmploymentInfo> empInfoq1 = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ( ehcmEmpSuspension.id='" + objSuspensionT.getId()
                + "'or ehcmEmpSuspension.id is null)  and ehcmEmpPerinfo.id=:employeeId order by creationDate desc");
        empInfoq1.setNamedParameter("employeeId", objSuspensionT.getEhcmEmpPerinfo().getId());
        empInfoq1.setFilterOnActive(false);
        empInfoq1.setMaxResult(1);
        if (empInfoq1.list().size() > 0) {
          oldEmployment = empInfoq1.list().get(0);
          oldEmployment.setEndDate(oldSuspension.getEndDate());
          oldEmployment.setAlertStatus("INACT");
          oldEmployment.setEnabled(false);
          OBDal.getInstance().save(oldEmployment);
          OBDal.getInstance().flush();
        }
      }

    } catch (

    Exception e) {
      e.printStackTrace();
      log.error("Exception in insertEmploymentRecord in EmpSuspensionReactiveDAO: ", e);
    }

  }

  // insert record in EmpStatusView
  public static void insertRecordInEmpStatus(EmployeeSuspension oldSuspension) {
    ehcmempstatus ehcmempstatus = null;
    try {
      // create new empstatus
      ehcmempstatus = OBProvider.getInstance().get(ehcmempstatus.class);
      ehcmempstatus.setClient(oldSuspension.getClient());
      ehcmempstatus.setOrganization(oldSuspension.getOrganization());
      ehcmempstatus.setCreationDate(new java.util.Date());
      ehcmempstatus.setCreatedBy(oldSuspension.getCreatedBy());
      ehcmempstatus.setUpdated(new java.util.Date());
      ehcmempstatus.setUpdatedBy(oldSuspension.getUpdatedBy());
      ehcmempstatus.setEnabled(false);
      ehcmempstatus.setEhcmEmpPerinfo(
          OBDal.getInstance().get(EhcmEmpPerInfo.class, oldSuspension.getEhcmEmpPerinfo().getId()));
      ehcmempstatus.setDecisionno(oldSuspension.getDecisionNo());
      ehcmempstatus.setStartDate(oldSuspension.getStartDate());
      ehcmempstatus.setTodate(oldSuspension.getEndDate());
      ehcmempstatus.setEhcmEmpSuspension(oldSuspension);
      ehcmempstatus.setStatus("TE");
      OBDal.getInstance().save(ehcmempstatus);
      OBDal.getInstance().flush();

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in insert record in emp status:", e);
    }
  }

  // insert Record in termintion if the reason as terminated.
  public static EHCMEMPTermination insertRecordinTermination(EmployeeSuspension objSuspension,
      EmploymentInfo objCloneEmplyment) {
    EHCMEMPTermination objEmpTermination = null;
    int millSec = 1 * 24 * 3600 * 1000;
    try {

      EhcmterminationEmpV terminationView = OBDal.getInstance().get(EhcmterminationEmpV.class,
          objSuspension.getEhcmEmpPerinfo().getId());
      objEmpTermination = OBProvider.getInstance().get(EHCMEMPTermination.class);
      if (objSuspension.getAssignedDepartment() != null) {
        objEmpTermination.setAssignedDepartment(objSuspension.getAssignedDepartment().getName());
      }
      objEmpTermination.setAuthorizePersonTitle(objSuspension.getAuthorisedPersonJobTitle());
      objEmpTermination.setDecisionDate(objSuspension.getDecisionDate());
      objEmpTermination.setDecisionNo(objSuspension.getDecisionNo());
      objEmpTermination.setDecisionStatus("I");
      objEmpTermination.setDecisionType("CR");
      objEmpTermination.setTitle(objSuspension.getTitle());
      objEmpTermination.setDepartmentCode(objSuspension.getDepartmentCode());
      objEmpTermination.setEhcmAuthorizePerson(objSuspension.getAuthorisedPerson());
      objEmpTermination.setEhcmEmpPerinfo(terminationView);
      if (objSuspension.getEhcmPayscaleline() != null) {
        objEmpTermination.setEhcmPayscaleline(OBDal.getInstance().get(EHCMPayscalePointV.class,
            objSuspension.getEhcmPayscaleline().getId()));
      }
      // get termination reason id
      OBQuery<EHCMTerminationReason> objTerminationReason = OBDal.getInstance()
          .createQuery(EHCMTerminationReason.class, "as e where e.searchKey='LDS' and e.client.id='"
              + objSuspension.getClient().getId() + "'");
      if (objTerminationReason.list().size() > 0) {
        objEmpTermination.setEhcmTerminationReason(objTerminationReason.list().get(0));
      }
      objEmpTermination.setEmployeeCategory(objSuspension.getEmployeeCategory());
      objEmpTermination.setEmployeeName(objSuspension.getEmployeeName());
      objEmpTermination.setEmployeeStatus(objSuspension.getEmployeeStatus());
      objEmpTermination.setEmployeeType(objSuspension.getEmployeeType());
      if (objSuspension.getEmploymentGrade() != null) {
        objEmpTermination.setEmploymentGrade(objSuspension.getEmploymentGrade());
      }
      if (objSuspension.getGrade() != null) {
        objEmpTermination.setGrade(objSuspension.getGrade());
      }
      objEmpTermination.setHireDate(objSuspension.getHireDate());
      objEmpTermination.setLetterDate(objSuspension.getLetterDate());
      objEmpTermination.setLetterNo(objSuspension.getLetterNo());
      objEmpTermination.setEhcmEmploymentInfo(objCloneEmplyment);
      objEmpTermination.setOrganization(objSuspension.getOrganization());
      objEmpTermination.setOriginalDecisionNo(objSuspension.getOriginalDecisionNo().getId());
      objEmpTermination.setPosition(objSuspension.getPosition());
      objEmpTermination.setRemarks(objSuspension.getRemarks());
      objEmpTermination.setEhcmEmpSuspension(objSuspension);
      if (objSuspension.getSectionCode() != null) {
        objEmpTermination.setSectionCode(objSuspension.getSectionCode());
      }
      objEmpTermination.setSueDecision(true);
      objEmpTermination
          .setTerminationDate(new Date(objSuspension.getEndDate().getTime() + millSec));
      OBDal.getInstance().save(objEmpTermination);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in insertRecordinTermination: ", e);
    }
    return objEmpTermination;
  }

}