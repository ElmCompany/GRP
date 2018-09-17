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
import org.openbravo.model.ad.access.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.EHCMEmpSupervisorNode;
import sa.elm.ob.hcm.EHCMPayscalePointV;
import sa.elm.ob.hcm.EHCMTerminationReason;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmJoiningWorkRequest;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EhcmSuspensionEmpV;
import sa.elm.ob.hcm.EhcmterminationEmpV;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmempstatus;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.hcm.util.UtilityDAO;

/**
 * 
 * 
 * @author poongodi 02-03-2018
 *
 */

public class EmpSuspensionHandlerDAO {

  private Connection connection = null;
  private static final Logger log = LoggerFactory.getLogger(EmpSuspensionHandlerDAO.class);

  public EmpSuspensionHandlerDAO() {
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

  /**
   * 
   * @param objSuspension
   * @return
   * @throws Exception
   */
  // Create New Record in employment information for suspension type start
  public static EhcmEmpPerInfo insertEmploymentInfo(EmployeeSuspension objSuspension)
      throws Exception {
    // TODO Auto-generated method stub
    EhcmSuspensionEmpV suspensionView = objSuspension.getEhcmEmpPerinfo();
    EhcmEmpPerInfo employee = suspensionView.getEmployee();
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    int millSec = 1 * 24 * 3600 * 1000;
    EHCMEmpSupervisor supervisorId = null;
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
        objCloneEmplyment.setChangereason(objSuspension.getSuspensionType());
        objCloneEmplyment.setChangereasoninfo(objSuspension.getSuspensionReason().getSearchKey());
        objCloneEmplyment.setStartDate(objSuspension.getStartDate());
        objCloneEmplyment.setDecisionDate(objSuspension.getDecisionDate());
        objCloneEmplyment.setDecisionNo(objSuspension.getDecisionNo());
        objCloneEmplyment.setEhcmEmpSuspension(objSuspension);
        objCloneEmplyment.setCreationDate(new Date());
        objCloneEmplyment.setUpdated(new Date());
        objCloneEmplyment.setEnabled(true);
        objCloneEmplyment.setAlertStatus("ACT");

        OBDal.getInstance().save(objCloneEmplyment);
        OBDal.getInstance().flush();

        // Make old Employment Info as Inactive
        empinfo.setEnabled(false);
        empinfo.setAlertStatus("INACT");
        Date startdate = empinfo.getStartDate();
        Date dateBefore = new Date(objSuspension.getStartDate().getTime() - millSec);

        if (startdate.compareTo(objSuspension.getStartDate()) == 0) {
          empinfo.setEndDate(empinfo.getStartDate());
        } else {
          empinfo.setEndDate(dateBefore);
        }
        supervisorId = UtilityDAO.getSupervisorforEmployee(
            objSuspension.getEhcmEmpPerinfo().getId(), objSuspension.getClient().getId());
        empinfo.setEhcmEmpSupervisor(supervisorId);
        OBDal.getInstance().save(empinfo);
      }
      // make employee as suspended
      employee.setEmploymentStatus("SD");
      OBDal.getInstance().save(employee);
      OBDal.getInstance().flush();

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in insertEmploymentInfo using suspension start: ", e);
    }
    return employee;
  }

  /**
   * 
   * @param objSuspension
   * @param oldSuspension
   * @return
   * @throws Exception
   */
  // Create new Record using Suspension End
  public static EhcmEmpPerInfo InsertRecordUsingSuspensionEnd(EmployeeSuspension objSuspension,
      EmployeeSuspension oldSuspension, EhcmJoiningWorkRequest joinReqId) throws Exception {
    // TODO Auto-generated method stub
    EhcmSuspensionEmpV suspensionView = objSuspension.getEhcmEmpPerinfo();
    EhcmEmpPerInfo employee = suspensionView.getEmployee();
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    int millSec = 1 * 24 * 3600 * 1000;
    EHCMEmpSupervisor supervisorId = null;
    AssingedOrReleaseEmpInPositionDAO positionEmpHist = new AssingedOrReleaseEmpInPositionDAOImpl();
    try {
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and enabled='Y'  and alertStatus='ACT' and changereason='SUS' and ehcmEmpSuspension.id='"
              + oldSuspension.getId() + "'order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
      empInfo.setMaxResult(1);
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        EmploymentInfo empinfo = employmentInfo.get(0);
        // clone new Record in Employement Info
        EmploymentInfo objCloneEmplyment = (EmploymentInfo) DalUtil.copy(empinfo, false);
        objCloneEmplyment.setChangereason(objSuspension.getSuspensionType());
        objCloneEmplyment
            .setChangereasoninfo(objSuspension.getSuspensionEndReason().getSearchKey());
        if (!objSuspension.isJoinWorkRequestRequired()) {
          objCloneEmplyment.setStartDate(new Date(objSuspension.getEndDate().getTime() + millSec));
        } else {
          objCloneEmplyment.setStartDate(joinReqId.getJoindate());
        }
        objCloneEmplyment.setDecisionDate(objSuspension.getDecisionDate());
        objCloneEmplyment.setDecisionNo(objSuspension.getDecisionNo());
        objCloneEmplyment.setEhcmEmpSuspension(objSuspension);
        objCloneEmplyment.setEnabled(true);
        objCloneEmplyment.setCreationDate(new Date());
        objCloneEmplyment.setAlertStatus("ACT");
        objCloneEmplyment.setUpdated(new Date());
        OBQuery<EHCMEmpSupervisorNode> supervisior = OBDal.getInstance().createQuery(
            EHCMEmpSupervisorNode.class,
            "  as e where e.ehcmEmpPerinfo.id=:employeeId and e.client.id =:client");
        supervisior.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
        supervisior.setNamedParameter("client", objSuspension.getClient().getId());
        List<EHCMEmpSupervisorNode> node = supervisior.list();
        if (node.size() > 0) {
          supervisorId = node.get(0).getEhcmEmpSupervisor();
          objCloneEmplyment.setEhcmEmpSupervisor(supervisorId);
        }
        if (objSuspension.isJoinWorkRequestRequired()) {
          objCloneEmplyment.setJoinworkreq(true);
          objCloneEmplyment.setEhcmJoinWorkrequest(joinReqId);
        }
        OBDal.getInstance().save(objCloneEmplyment);
        OBDal.getInstance().flush();

        // Make old Employment Info as Inactive
        empinfo.setEnabled(false);
        empinfo.setAlertStatus("INACT");
        if (!objSuspension.isJoinWorkRequestRequired()) {
          empinfo.setEndDate(objSuspension.getEndDate());
        } else {
          Date dateBefore = new Date(joinReqId.getJoindate().getTime() - millSec);

          if (empinfo.getStartDate().compareTo(joinReqId.getJoindate()) == 0)
            empinfo.setEndDate(empinfo.getStartDate());
          else
            empinfo.setEndDate(dateBefore);
        }

        OBDal.getInstance().save(empinfo);

        // make current suspension as active
        objSuspension.setEnabled(true);
        OBDal.getInstance().save(objSuspension);

        // make old suspension as inactive
        oldSuspension.setEnabled(false);
        OBDal.getInstance().save(oldSuspension);

        // If Suspension reason is Termination then make entry termination
        if (objSuspension.getSuspensionEndReason() != null) {
          if (objSuspension.getSuspensionEndReason().getSearchKey().equals("T")
              || objSuspension.getSuspensionEndReason().getSearchKey().equals("TRD")) {
            EHCMEMPTermination objEmpTermination = insertRecordinTermination(objSuspension,
                objCloneEmplyment, oldSuspension);
            insertRecordInEmpStatus(objSuspension);
            // position release
            EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
                objSuspension.getPosition().getId());
            // Task No.6797 pos.setAssignedEmployee(null);
            // EhcmPosition oldDelegatedPosition = null;

            // update position employee history records with end date for assigned emp
            positionEmpHist.updateEndDateInPositionEmployeeHisotry(suspensionView.getEmployee(),
                objSuspension.getPosition(), objSuspension.getEndDate(), null, null, null, null,
                objSuspension, null, null);

            // update position employee history records with end date for delegated emp
            positionEmpHist.updateEndDateForDelegatedEmployee(suspensionView.getEmployee(),
                objSuspension.getEndDate(), objSuspension, null, objSuspension.getDecisionType());

            // old delegation remove
            /*
             * Task No.6797 OBQuery<EhcmPosition> oldPositionQry = OBDal.getInstance().createQuery(
             * EhcmPosition.class, "as e where e.jOBNo='" + empinfo.getSecjobno() +
             * "' and e.client.id='" + empinfo.getClient().getId() +
             * "' order by creationDate desc"); oldPositionQry.setMaxResult(1); if
             * (oldPositionQry.list().size() > 0) { oldDelegatedPosition =
             * oldPositionQry.list().get(0); } if (oldDelegatedPosition != null) {
             * oldDelegatedPosition.setDelegatedEmployee(null);
             * OBDal.getInstance().save(oldDelegatedPosition); }
             */

            OBDal.getInstance().save(pos);
            OBDal.getInstance().flush();
            // make termination to inactive to avoid update or cancel
            objEmpTermination.setEnabled(false);
            OBDal.getInstance().save(objEmpTermination);

          }
        }

      }
      // make employee release from suspension(Active)

      if (objSuspension.getSuspensionEndReason().getSearchKey().equals("T")
          || objSuspension.getSuspensionEndReason().getSearchKey().equals("TRD")) {
        employee.setEmploymentStatus("SD");
        employee.setEnabled(false);
      } else {
        employee.setEmploymentStatus("AC");
      }

      OBDal.getInstance().flush();

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in InsertRecordUsingSuspensionEnd : ", e);
    }
    return employee;
  }

  /**
   * 
   * @param empInfo
   * @param objSuspension
   * @param suspensionId
   */
  public static void preSuspensionRecordInactive(EmployeeSuspension objSuspension,
      String suspensionId) {

    try {
      // before start suspension make all previous active suspension as disabled
      OBQuery<EmployeeSuspension> prevSuspendQry = OBDal.getInstance().createQuery(
          EmployeeSuspension.class,
          " ehcmEmpPerinfo.id='" + objSuspension.getEhcmEmpPerinfo().getId()
              + "' and enabled='Y' and decisionStatus='I' and  id <> '" + suspensionId + "'");
      if (prevSuspendQry.list().size() > 0) {
        for (EmployeeSuspension prevSuspension : prevSuspendQry.list()) {
          prevSuspension.setEnabled(false);
          OBDal.getInstance().save(prevSuspension);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in preSuspensionRecordInactive: ", e);
    }
  }

  /**
   * 
   * @param objSuspension
   * @param oldSuspension
   * @param oldTermination
   * @param vars
   * @return
   * @throws Exception
   */

  public static int updateEmploymentRecord(EmployeeSuspension objSuspension,
      EmployeeSuspension oldSuspension, EHCMEMPTermination oldTermination, VariablesSecureApp vars)
      throws Exception {

    // TODO Auto-generated method stub
    EhcmSuspensionEmpV suspensionView = objSuspension.getEhcmEmpPerinfo();
    EhcmEmpPerInfo employee = suspensionView.getEmployee();
    AssingedOrReleaseEmpInPositionDAO positionEmpHist = new AssingedOrReleaseEmpInPositionDAOImpl();
    Boolean chkPositionAvailableOrNot = false;
    Boolean chkDelegatePositionAvailableOrNot = false;
    int count = 0;
    Date suspensionEndDateAfter = null;
    int millSec = 1 * 24 * 3600 * 1000;
    try {

      // release employee from suspension
      if (objSuspension.getSuspensionEndReason() != null) {
        if (objSuspension.getSuspensionEndReason().getSearchKey().equals("T")
            || objSuspension.getSuspensionEndReason().getSearchKey().equals("TRD")) {
          employee.setEmploymentStatus("SD");
        }

        else if (objSuspension.getSuspensionType().equals("SUE")) {
          employee.setEmploymentStatus("AC");
        } else {
          employee.setEmploymentStatus("SD");
        }
      }
      OBDal.getInstance().save(employee);
      EmploymentInfo preEmpinfo = null;
      // update end date in previous record in employment(not belongs to this suspension)
      OBQuery<EmploymentInfo> preEmpInfoQry = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and (ehcmEmpSuspension.id <> '" + oldSuspension.getId()
              + "' or ehcmEmpSuspension.id is null ) order by creationDate desc ");
      preEmpInfoQry.setNamedParameter("employeeId", oldSuspension.getEhcmEmpPerinfo().getId());
      preEmpInfoQry.setMaxResult(1);
      if (preEmpInfoQry.list().size() > 0) {
        preEmpinfo = preEmpInfoQry.list().get(0);
        if (objSuspension.getSuspensionType().equals("SUE")) {
          preEmpinfo.setEndDate(objSuspension.getEndDate());
        } else {
          Date startdate = preEmpinfo.getStartDate();
          Date dateBefore = new Date(objSuspension.getStartDate().getTime() - millSec);
          if (startdate.compareTo(objSuspension.getStartDate()) == 0) {
            preEmpinfo.setEndDate(preEmpinfo.getStartDate());
          } else {
            preEmpinfo.setEndDate(dateBefore);
          }
        }
        OBDal.getInstance().save(preEmpinfo);
      }
      // get updating employment Record
      EmploymentInfo oldEmployment = null;
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and enabled='Y'  and ( alertStatus='ACT' or alertStatus='T' )  and ehcmEmpSuspension.id='"
              + oldSuspension.getId() + "'order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
      empInfo.setMaxResult(1);
      if (empInfo.list().size() > 0) {
        oldEmployment = empInfo.list().get(0);
        oldEmployment.setEhcmEmpSuspension(objSuspension);
        oldEmployment.setChangereason(objSuspension.getSuspensionType());
        if (objSuspension.getSuspensionType().equals("SUE")) {
          oldEmployment.setStartDate(objSuspension.getJoinDate());
          oldEmployment.setChangereasoninfo(objSuspension.getSuspensionEndReason().getSearchKey());
        } else {
          oldEmployment.setStartDate(objSuspension.getStartDate());
          oldEmployment.setChangereasoninfo(objSuspension.getSuspensionReason().getSearchKey());
        }
        oldEmployment.setDecisionDate(objSuspension.getDecisionDate());
        oldEmployment.setDecisionNo(objSuspension.getDecisionNo());
        OBDal.getInstance().save(oldEmployment);
      }
      // make old suspension as inactive
      oldSuspension.setEnabled(false);
      OBDal.getInstance().save(oldSuspension);
      OBDal.getInstance().flush();
      // update empstatus
      OBQuery<ehcmempstatus> empstatus = OBDal.getInstance().createQuery(ehcmempstatus.class,
          " ehcmEmpPerinfo.id='" + objSuspension.getEhcmEmpPerinfo().getId() + "'");
      empstatus.setMaxResult(1);
      if (empstatus.list().size() > 0) {
        ehcmempstatus employeestatus = empstatus.list().get(0);
        employeestatus.setUpdated(new java.util.Date());
        employeestatus.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        employeestatus.setDecisionno(objSuspension.getDecisionNo());
        employeestatus.setStartDate(objSuspension.getStartDate());
        employeestatus.setTodate(objSuspension.getEndDate());
        employeestatus.setEhcmEmpSuspension(objSuspension);
        employeestatus.setStatus("TE");
        OBDal.getInstance().save(employeestatus);
        OBDal.getInstance().flush();

      }
      log.debug("update suspe" + objSuspension.getSuspensionType());
      // If Suspension End Reason
      if (objSuspension.getSuspensionEndReason() != null
          && objSuspension.getSuspensionType().equals("SUE")) {
        // If Suspension reason is from others to Termination then make entry in termination
        if ((objSuspension.getSuspensionEndReason().getSearchKey().equals("T")
            || objSuspension.getSuspensionEndReason().getSearchKey().equals("TRD"))) {
          if (oldTermination == null) {
            EHCMEMPTermination objEmpTermination = insertRecordinTermination(objSuspension,
                oldEmployment, oldSuspension);
            // change old employment status
            oldEmployment.setChangereason(objSuspension.getSuspensionType());
            oldEmployment
                .setChangereasoninfo(objSuspension.getSuspensionEndReason().getSearchKey());
            oldEmployment.setAlertStatus("ACT");
            OBDal.getInstance().save(oldEmployment);
            OBDal.getInstance().flush();

            // make Corresponding employee as terminated
            suspensionView.getEmployee().setEndDate(objSuspension.getEndDate());
            OBDal.getInstance().save(objSuspension.getEhcmEmpPerinfo());
            OBDal.getInstance().flush();

            insertRecordInEmpStatus(objSuspension);

            // position release
            /*
             * Task No.6797 EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
             * objSuspension.getPosition().getId()); pos.setAssignedEmployee(null); EhcmPosition
             * oldDelegatedPosition = null;
             */
            // old delegation remove
            /*
             * Task No.6797 OBQuery<EhcmPosition> oldPositionQry = OBDal.getInstance().createQuery(
             * EhcmPosition.class, "as e where e.jOBNo='" + preEmpinfo.getSecjobno() +
             * "' and e.client.id='" + preEmpinfo.getClient().getId() +
             * "' order by creationDate desc"); oldPositionQry.setMaxResult(1); if
             * (oldPositionQry.list().size() > 0) { oldDelegatedPosition =
             * oldPositionQry.list().get(0); } if (oldDelegatedPosition != null) {
             * oldDelegatedPosition.setDelegatedEmployee(null);
             * OBDal.getInstance().save(oldDelegatedPosition); } OBDal.getInstance().save(pos);
             * OBDal.getInstance().flush();
             */
            // make termination to inactive to avoid update or cancel
            objEmpTermination.setEhcmEmpSuspension(objSuspension);
            objEmpTermination.setEnabled(false);
            OBDal.getInstance().save(objEmpTermination);

          }

          // update position employee history records with end date
          positionEmpHist.updateEndDateInPositionEmployeeHisotry(suspensionView.getEmployee(),
              objSuspension.getPosition(), objSuspension.getEndDate(), null, null, null, null,
              objSuspension, null, null);

          // update position employee history records with end date for employee delegation
          positionEmpHist.updateEndDateForDelegatedEmployee(suspensionView.getEmployee(),
              objSuspension.getEndDate(), objSuspension, null, objSuspension.getDecisionType());

        }

        if ((!objSuspension.getSuspensionEndReason().getSearchKey().equals("T")
            && !objSuspension.getSuspensionEndReason().getSearchKey().equals("TRD"))
            && oldTermination != null) {

          oldEmployment.setChangereason(objSuspension.getSuspensionType());
          oldEmployment.setChangereasoninfo(objSuspension.getSuspensionEndReason().getSearchKey());
          oldEmployment.setAlertStatus("ACT");

          OBDal.getInstance().save(oldEmployment);
          OBDal.getInstance().flush();
          OBDal.getInstance().refresh(oldEmployment);

          // delete empstatus
          OBQuery<ehcmempstatus> employeestatus = OBDal.getInstance().createQuery(
              ehcmempstatus.class,
              " ehcmEmpPerinfo.id='" + objSuspension.getEhcmEmpPerinfo().getId() + "'");
          if (employeestatus.list().size() > 0) {
            ehcmempstatus employe = employeestatus.list().get(0);
            OBDal.getInstance().remove(employe);
            OBDal.getInstance().flush();
          }

          // reassign the position to this employee

          EhcmPosition pos = preEmpinfo.getPosition();
          EhcmPosition oldDelegatedPosition = null;
          suspensionEndDateAfter = new Date(objSuspension.getEndDate().getTime() + millSec);
          chkPositionAvailableOrNot = positionEmpHist.chkPositionAvailableOrNot(
              suspensionView.getEmployee(), pos, suspensionEndDateAfter, null, "CA", false);

          chkDelegatePositionAvailableOrNot = positionEmpHist.chkDelegatePositionAvailableOrNot(
              suspensionView.getEmployee(), objSuspension.getOriginalDecisionNo(), null,
              suspensionEndDateAfter, null, "CA", true);

          OBQuery<EhcmPosition> oldPositionQry = OBDal.getInstance().createQuery(EhcmPosition.class,
              "as e where e.jOBNo='" + preEmpinfo.getSecjobno() + "' and e.client.id='"
                  + preEmpinfo.getClient().getId() + "' order by creationDate desc");
          oldPositionQry.setMaxResult(1);
          if (oldPositionQry.list().size() > 0) {
            oldDelegatedPosition = oldPositionQry.list().get(0);
          }
          // if (pos.getAssignedEmployee() != null) {
          if (chkPositionAvailableOrNot) {
            count = 1;

          }
          /*
           * else if (oldDelegatedPosition != null && oldDelegatedPosition.getDelegatedEmployee() !=
           * null) {
           */
          else if (chkDelegatePositionAvailableOrNot) {
            count = 2;

          } else {
            if (pos != null) {
              /*
               * Task No.6797 pos.setAssignedEmployee(OBDal.getInstance().get(EmployeeView.class,
               * objSuspension.getEhcmEmpPerinfo().getId()));
               */
              // update position employee history records with end date
              positionEmpHist.updateEndDateInPositionEmployeeHisotry(suspensionView.getEmployee(),
                  objSuspension.getPosition(), null, null, null, null, null, objSuspension, null,
                  null);
            }
            // update position employee history records with end date for delegated emp
            positionEmpHist.updateEndDateForDelegatedEmployee(suspensionView.getEmployee(),
                objSuspension.getEndDate(), objSuspension, null, "CA");

            /*
             * Task No.6797 if (oldDelegatedPosition != null) {
             * oldDelegatedPosition.setDelegatedEmployee(OBDal.getInstance().get(EmployeeView.class,
             * objSuspension.getEhcmEmpPerinfo().getId())); }
             */
            OBDal.getInstance().save(pos);
            OBDal.getInstance().flush();
          }

          // delete termination
          OBDal.getInstance().getConnection()
              .prepareStatement("delete from ehcm_emp_termination where ehcm_emp_termination_id='"
                  + oldTermination.getId() + "'")
              .executeUpdate();
        }
        // If Suspension reason is from Termination to Termination then Revert All Changes
        if ((objSuspension.getSuspensionEndReason().getSearchKey().equals("T")
            || objSuspension.getSuspensionEndReason().getSearchKey().equals("TRD"))
            && oldTermination != null) {
          // update end date in corresponding employee
          suspensionView.getEmployee().setEndDate(objSuspension.getEndDate());
          // change termination ref
          oldTermination.setEhcmEmpSuspension(objSuspension);
          oldTermination.setDecisionNo(objSuspension.getDecisionNo());
          oldTermination.setDecisionDate(objSuspension.getDecisionDate());
          oldTermination.setLetterDate(objSuspension.getLetterDate());
          oldTermination.setLetterNo(objSuspension.getLetterNo());
          oldTermination.setRemarks(objSuspension.getRemarks());
          oldTermination.setAuthorizePersonTitle(objSuspension.getAuthorisedPersonJobTitle());
          OBDal.getInstance().save(objSuspension.getEhcmEmpPerinfo());
        }
      }
      OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateEmploymentRecord in SuspensionDAO: ", e);
    }
    return count;
  }

  /**
   * 
   * @param objSuspension
   * @param oldSuspension
   * @param oldTermination
   * @param vars
   * @return
   * @throws Exception
   */
  public static int cancelEmploymentRecord(EmployeeSuspension objSuspension,
      EmployeeSuspension oldSuspension, EHCMEMPTermination oldTermination, VariablesSecureApp vars)
      throws Exception {
    // TODO Auto-generated method stub
    EhcmSuspensionEmpV suspensionview = objSuspension.getEhcmEmpPerinfo();
    EhcmEmpPerInfo employee = suspensionview.getEmployee();
    int count = 0;
    AssingedOrReleaseEmpInPositionDAO positionEmpHist = new AssingedOrReleaseEmpInPositionDAOImpl();
    boolean chkPositionAvailableOrNot = false;
    boolean chkDelegatePositionAvailableOrNot = false;
    EmploymentInfo oldEmployment = null;
    EmploymentInfo preEmpinfo = null;
    Date suspensionEndDateAfter = null;
    int millSec = 1 * 24 * 3600 * 1000;
    try {
      // release employee from suspension
      if (objSuspension.getSuspensionType().equals("SUS")) {
        employee.setEmploymentStatus("AC");
      } else {
        employee.setEmploymentStatus("SD");
        if (objSuspension.getSuspensionEndReason().getSearchKey().equals("T")
            || objSuspension.getSuspensionEndReason().getSearchKey().equals("TRD")) {
          employee.setEnabled(true);
        }
      }
      OBDal.getInstance().save(employee);

      if (oldSuspension.getSuspensionType().equals("SUE")
          && objSuspension.getSuspensionType().equals("SUE") && oldTermination != null) {

        // delete newly created employee
        EHCMEMPTermination objExistTermination = oldTermination;
        objExistTermination.setEhcmEmploymentInfo(null);
        OBDal.getInstance().save(objExistTermination);
        OBDal.getInstance().flush();

        // remove linked employment
        OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ehcmEmpPerinfo.id=:employeeId and enabled='Y'  and (alertStatus='ACT' or alertStatus='T' ) and ehcmEmpSuspension.id='"
                + oldSuspension.getId() + "'order by creationDate desc ");
        empInfo.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
        empInfo.setMaxResult(1);
        log.debug(empInfo.getWhereAndOrderBy());
        if (empInfo.list().size() > 0) {
          oldEmployment = empInfo.list().get(0);
          preEmpinfo = updateEmployInfoIdInDelgateAndActPreRecordInEmployInfo(objSuspension,
              oldEmployment);
          OBDal.getInstance().remove(oldEmployment);
          OBDal.getInstance().flush();
        }
        // delete empstatus
        log.debug("Cancel");
        OBQuery<ehcmempstatus> employeestatus = OBDal.getInstance().createQuery(ehcmempstatus.class,
            " ehcmEmpPerinfo.id=:employeeId");
        employeestatus.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
        if (employeestatus.list().size() > 0) {
          ehcmempstatus employe = employeestatus.list().get(0);
          OBDal.getInstance().remove(employe);
          OBDal.getInstance().flush();
        }

        // delete termination

        OBDal.getInstance().getConnection()
            .prepareStatement("delete from ehcm_emp_termination where ehcm_emp_termination_id='"
                + oldTermination.getId() + "'")
            .executeUpdate();

      } else {
        // remove old employment
        OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ehcmEmpPerinfo.id=:empId and enabled='Y'  and (alertStatus='ACT' or alertStatus='T' ) and ehcmEmpSuspension.id='"
                + oldSuspension.getId() + "'order by creationDate desc ");
        empInfo.setNamedParameter("empId", objSuspension.getEhcmEmpPerinfo().getId());
        empInfo.setMaxResult(1);
        if (empInfo.list().size() > 0) {
          oldEmployment = empInfo.list().get(0);
          preEmpinfo = updateEmployInfoIdInDelgateAndActPreRecordInEmployInfo(objSuspension,
              oldEmployment);
          OBDal.getInstance().remove(oldEmployment);
          OBDal.getInstance().flush();
        }
      }
      // reassign the position to this employee(termination)
      if (oldSuspension.getSuspensionType().equals("SUE")
          && objSuspension.getSuspensionType().equals("SUE") && oldTermination != null) {
        EhcmPosition pos = preEmpinfo.getPosition();
        // EhcmPosition oldDelegatedPosition = null;

        suspensionEndDateAfter = new Date(objSuspension.getEndDate().getTime() + millSec);
        chkPositionAvailableOrNot = positionEmpHist.chkPositionAvailableOrNot(
            suspensionview.getEmployee(), pos, suspensionEndDateAfter, null,
            objSuspension.getDecisionType(), false);

        chkDelegatePositionAvailableOrNot = positionEmpHist.chkDelegatePositionAvailableOrNot(
            suspensionview.getEmployee(), objSuspension.getOriginalDecisionNo(), null,
            suspensionEndDateAfter, null, "CA", true);

        /*
         * Task No 6796 OBQuery<EhcmPosition> oldPositionQry =
         * OBDal.getInstance().createQuery(EhcmPosition.class, "as e where e.jOBNo='" +
         * preEmpinfo.getSecjobno() + "' and e.client.id='" + preEmpinfo.getClient().getId() +
         * "' order by creationDate desc"); oldPositionQry.setMaxResult(1); if
         * (oldPositionQry.list().size() > 0) { oldDelegatedPosition = oldPositionQry.list().get(0);
         * }
         */

        // Task No 6796 if (pos.getAssignedEmployee() != null) {
        if (chkPositionAvailableOrNot) {
          count = 1;

        }
        /*
         * else if (oldDelegatedPosition != null && oldDelegatedPosition.getDelegatedEmployee() !=
         * null) {
         */
        else if (chkDelegatePositionAvailableOrNot) {
          count = 2;

        } else {
          if (pos != null) {
            /*
             * Task No. 6797 pos.setAssignedEmployee(OBDal.getInstance().get(EmployeeView.class,
             * objSuspension.getEhcmEmpPerinfo().getId())); OBDal.getInstance().save(pos);
             */
            // update position employee history records with end date
            positionEmpHist.updateEndDateInPositionEmployeeHisotry(suspensionview.getEmployee(),
                objSuspension.getPosition(), null, null, null, null, null, objSuspension, null,
                null);
          }
          // update position employee history records with end date for delegated emp
          positionEmpHist.updateEndDateForDelegatedEmployee(suspensionview.getEmployee(),
              objSuspension.getEndDate(), objSuspension, null, objSuspension.getDecisionType());

          /*
           * Task No. 6797 if (oldDelegatedPosition != null) {
           * oldDelegatedPosition.setDelegatedEmployee(OBDal.getInstance().get(EmployeeView.class,
           * objSuspension.getEhcmEmpPerinfo().getId()));
           * OBDal.getInstance().save(oldDelegatedPosition); }
           */

          OBDal.getInstance().flush();
        }
      }

      // Cancel Suspension Disabled
      objSuspension.setEnabled(false);
      OBDal.getInstance().save(objSuspension);
      // make old suspension as inactive
      oldSuspension.setEnabled(false);
      OBDal.getInstance().save(oldSuspension);
      OBDal.getInstance().flush();

    } catch (

    Exception e) {
      e.printStackTrace();
      log.error("Exception in cancelEmploymentRecord in SuspensionDAO: ", e);
    }
    return count;
  }

  public static EmploymentInfo updateEmployInfoIdInDelgateAndActPreRecordInEmployInfo(
      EmployeeSuspension objSuspension, EmploymentInfo oldEmployment) {
    EmploymentInfo preEmpinfo = null;
    List<EmployeeDelegation> empDelList = new ArrayList<EmployeeDelegation>();

    try {

      // Activate previous record in employment(not belongs to this suspension)
      OBQuery<EmploymentInfo> preEmpInfoQry = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:empId and  ehcmEmpSecondment.id is null and id <>:currentemployInfoId order by creationDate desc ");
      preEmpInfoQry.setNamedParameter("empId", objSuspension.getEhcmEmpPerinfo().getId());
      preEmpInfoQry.setNamedParameter("currentemployInfoId", oldEmployment.getId());
      preEmpInfoQry.setMaxResult(1);
      if (preEmpInfoQry.list().size() > 0) {
        preEmpinfo = preEmpInfoQry.list().get(0);
        preEmpinfo.setEnabled(true);
        preEmpinfo.setAlertStatus("ACT");
        preEmpinfo.setEndDate(null);
        if (preEmpinfo.getEhcmEmpSuspension() != null) {
          EmployeeSuspension preSuspension = preEmpinfo.getEhcmEmpSuspension();
          preSuspension.setEnabled(true);
          OBDal.getInstance().save(preSuspension);
        }
        OBDal.getInstance().save(preEmpinfo);

        /// update employment info in delegation
        OBQuery<EmployeeDelegation> empDelegateQry = OBDal.getInstance().createQuery(
            EmployeeDelegation.class, " as e where e.ehcmEmploymentInfo.id=:employInfoId "
                + " and e.ehcmEmpPerinfo.id=:empId and e.enabled='Y' order by e.creationDate desc");
        empDelegateQry.setNamedParameter("employInfoId", oldEmployment.getId());
        empDelegateQry.setNamedParameter("empId", objSuspension.getEhcmEmpPerinfo().getId());
        empDelegateQry.setMaxResult(1);
        empDelList = empDelegateQry.list();
        if (empDelList.size() > 0) {
          EmployeeDelegation empDelegate = empDelList.get(0);
          empDelegate.setEhcmEmploymentInfo(preEmpinfo);
          OBDal.getInstance().save(empDelegate);
        }
        return preEmpinfo;

      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error(
          "Exception in updateEmployInfoIdInDelgateAndActPreRecordInEmployInfo in SuspensionDAO: ",
          e);
    }
    return preEmpinfo;
  }

  /**
   * 
   * @param objSuspension
   */
  // insert Record in termintion if the reason as terminated.
  public static EHCMEMPTermination insertRecordinTermination(EmployeeSuspension objSuspension,
      EmploymentInfo objCloneEmplyment, EmployeeSuspension oldSuspension) {
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
      objEmpTermination.setOriginalDecisionNo(oldSuspension.getDecisionNo());
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

  /**
   * 
   * @param objSuspension
   */
  // insert record in EmpStatusView
  public static void insertRecordInEmpStatus(EmployeeSuspension objSuspension) {
    ehcmempstatus ehcmempstatus = null;
    try {
      // create new empstatus
      ehcmempstatus = OBProvider.getInstance().get(ehcmempstatus.class);
      ehcmempstatus.setClient(objSuspension.getClient());
      ehcmempstatus.setOrganization(objSuspension.getOrganization());
      ehcmempstatus.setCreationDate(new java.util.Date());
      ehcmempstatus.setCreatedBy(objSuspension.getCreatedBy());
      ehcmempstatus.setUpdated(new java.util.Date());
      ehcmempstatus.setUpdatedBy(objSuspension.getUpdatedBy());
      ehcmempstatus.setEnabled(false);
      ehcmempstatus.setEhcmEmpPerinfo(
          OBDal.getInstance().get(EhcmEmpPerInfo.class, objSuspension.getEhcmEmpPerinfo().getId()));
      ehcmempstatus.setDecisionno(objSuspension.getDecisionNo());
      ehcmempstatus.setStartDate(objSuspension.getStartDate());
      ehcmempstatus.setTodate(objSuspension.getEndDate());
      ehcmempstatus.setEhcmEmpSuspension(objSuspension);
      ehcmempstatus.setStatus("TE");
      OBDal.getInstance().save(ehcmempstatus);
      OBDal.getInstance().flush();

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in insert record in emp status:", e);
    }
  }

}
