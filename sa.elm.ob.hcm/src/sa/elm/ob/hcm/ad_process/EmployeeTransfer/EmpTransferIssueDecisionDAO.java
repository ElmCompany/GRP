package sa.elm.ob.hcm.ad_process.EmployeeTransfer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.EHCMEmpSupervisorNode;
import sa.elm.ob.hcm.EHCMEmpTransfer;
import sa.elm.ob.hcm.EhcmJoiningWorkRequest;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.EmpExtendService.DAO.ExtendServiceHandlerDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.utility.util.Utility;

/**
 * This process class used for Employee EvaluationDAO Implementation
 * 
 * @author divya 12-02-2018
 *
 */

public class EmpTransferIssueDecisionDAO {

  private Connection connection = null;
  private static final Logger log = LoggerFactory.getLogger(EmpTransferIssueDecisionDAO.class);

  public static EmploymentInfo insertEmploymentInfo(EHCMEmpTransfer empTransfer,
      EmploymentInfo oldempInfo, VariablesSecureApp vars, String decisionType, String lang,
      Date JoinStartDate, EhcmJoiningWorkRequest joinReqId) throws Exception {
    // TODO Auto-generated method stub
    Date dateafter = null;
    String transferType = null;
    EmploymentInfo employInfo = null;
    EhcmJoiningWorkRequest joinRequestId = null;
    EHCMEmpSupervisor supervisorId = null;
    try {

      // dateafter = new Date(empTransfer.getEndDate().getTime() + 1 * 24 * 3600 * 1000);

      // Create a record in Employement Information Window
      if (decisionType.equals("CR"))
        employInfo = OBProvider.getInstance().get(EmploymentInfo.class);
      else if (decisionType.equals("UP"))
        employInfo = oldempInfo;
      transferType = Resource.getProperty("hcm.employinfo.outside.department", lang);

      if (decisionType.equals("UP")) {
        employInfo.setUpdated(new java.util.Date());
        employInfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      }

      // employInfo.setChangereason(transferType);
      employInfo.setChangereason(empTransfer.getTransferType());
      employInfo.setEhcmPayscale(oldempInfo.getEhcmPayscale());
      employInfo.setEmpcategory(empTransfer.getGradeClass().getId());
      employInfo.setEmployeeno(empTransfer.getEhcmEmpPerinfo().getSearchKey());
      employInfo.setEhcmPayscaleline(oldempInfo.getEhcmPayscaleline());
      employInfo.setLocation(oldempInfo.getLocation());
      if (oldempInfo.getEhcmPayrollDefinition() != null)
        employInfo.setEhcmPayrollDefinition(oldempInfo.getEhcmPayrollDefinition());
      employInfo.setEhcmEmpPerinfo(empTransfer.getEhcmEmpPerinfo());
      if (JoinStartDate == null)
        employInfo.setStartDate(empTransfer.getStartDate());
      else
        employInfo.setStartDate(JoinStartDate);
      employInfo.setEndDate(empTransfer.getEndDate());
      employInfo.setAlertStatus("ACT");
      employInfo.setEhcmEmpTransfer(empTransfer);
      employInfo.setDecisionNo(empTransfer.getDecisionNo());
      employInfo.setDecisionDate(empTransfer.getDecisionDate());
      employInfo.setEmploymentgrade(oldempInfo.getEmploymentgrade());
      if (oldempInfo.getToGovernmentAgency() != null)
        employInfo.setToGovernmentAgency(oldempInfo.getToGovernmentAgency());

      if (empTransfer.getNewDepartmentCode() != null) {
        employInfo.setDepartmentName(empTransfer.getNewDepartmentCode().getName());
        employInfo.setDeptcode(empTransfer.getNewDepartmentCode());
      } else {
        employInfo.setDepartmentName(empTransfer.getDepartmentCode().getName());
        employInfo.setDeptcode(empTransfer.getDepartmentCode());
      }
      if (empTransfer.getNEWEhcmPosition() != null) {
        employInfo.setGrade(empTransfer.getNEWEhcmPosition().getGrade());
        employInfo.setJobcode(empTransfer.getNEWEhcmPosition().getEhcmJobs());
        employInfo.setPosition(
            OBDal.getInstance().get(EhcmPosition.class, empTransfer.getNEWEhcmPosition().getId()));
        employInfo.setJobtitle(empTransfer.getNEWJobTitle());
      } else {
        employInfo.setGrade(empTransfer.getGrade());
        employInfo.setJobcode(empTransfer.getPosition().getEhcmJobs());
        employInfo.setPosition(empTransfer.getPosition());
        employInfo.setJobtitle(empTransfer.getTitle());
      }

      if (empTransfer.getNewSectionCode() != null) {
        employInfo.setSectionName(empTransfer.getNewSectionCode().getName());
        employInfo.setSectioncode(empTransfer.getNewSectionCode());
      } else {
        if (empTransfer.getSectionCode() != null) {
          employInfo.setSectionName(empTransfer.getSectionCode().getName());
          employInfo.setSectioncode(empTransfer.getSectionCode());
        }
      }

      /* secondary */

      employInfo.setSecpositionGrade(oldempInfo.getSecpositionGrade());
      employInfo.setSecpositionGrade(oldempInfo.getSecpositionGrade());
      employInfo.setSecjobno(oldempInfo.getSecjobno());
      employInfo.setSecjobcode(oldempInfo.getSecjobcode());
      employInfo.setSecjobtitle(oldempInfo.getSecjobtitle());
      employInfo.setSECDeptCode(oldempInfo.getSECDeptCode());
      employInfo.setAssignedDepartment(oldempInfo.getAssignedDepartment());
      employInfo.setSECDeptName(oldempInfo.getSECDeptName());
      if (oldempInfo.getSECSectionCode() != null) {
        employInfo.setSECSectionCode(oldempInfo.getSECSectionCode());
        employInfo.setSECSectionName(oldempInfo.getSECSectionName());
      }
      employInfo.setSECLocation(oldempInfo.getSECLocation());

      employInfo.setSECStartdate(oldempInfo.getSECStartdate());
      employInfo.setSECEnddate(oldempInfo.getSECEnddate());
      employInfo.setSECDecisionNo(oldempInfo.getSECDecisionNo());
      employInfo.setSECDecisionDate(oldempInfo.getSECDecisionDate());

      employInfo.setSECChangeReason(oldempInfo.getSECChangeReason());
      employInfo.setSECEmploymentNumber(oldempInfo.getSECEmploymentNumber());
      OBQuery<EHCMEmpSupervisorNode> supervisior = OBDal.getInstance().createQuery(
          EHCMEmpSupervisorNode.class,
          "  as e where e.ehcmEmpPerinfo.id=:employeeId and e.client.id =:client");
      supervisior.setNamedParameter("employeeId", empTransfer.getEhcmEmpPerinfo().getId());
      supervisior.setNamedParameter("client", empTransfer.getClient().getId());
      List<EHCMEmpSupervisorNode> node = supervisior.list();
      if (node.size() > 0) {
        supervisorId = node.get(0).getEhcmEmpSupervisor();
        employInfo.setEhcmEmpSupervisor(supervisorId);
      }
      if (empTransfer.isJoinworkreq()) {
        employInfo.setJoinworkreq(true);
        employInfo.setEhcmJoinWorkrequest(joinReqId);
      }

      OBDal.getInstance().save(employInfo);
      OBDal.getInstance().flush();
      // update assigned employee in employee position
      updatePositionAssEmp(empTransfer, vars);

      // update for old info record as inactive
      updateEndDateforOldRecord(employInfo, vars, empTransfer, decisionType, joinReqId);

      if (decisionType.equals("UP"))
        updateOldEmpTransferInAct(empTransfer);

      OBDal.getInstance().flush();
      return employInfo;
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in insertEmploymentInfo in EmpTransferIssueDecisionDAO: ", e);
    }
    return employInfo;
  }

  public static void updateEndDateforOldRecord(EmploymentInfo empInfo, VariablesSecureApp vars,
      EHCMEmpTransfer empTransfer, String decisionType, EhcmJoiningWorkRequest joinRequest) {
    // TODO Auto-generated method stub
    Date dateBefore = null;
    Date startdate = null;
    try {
      EmploymentInfo oldempInfo = getRecentEmployInfoOtherThanCurrRecd(empTransfer, empInfo);
      log.debug("oldempInfo:" + oldempInfo);
      //
      if (oldempInfo != null) {
        oldempInfo.setUpdated(new java.util.Date());
        oldempInfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        startdate = oldempInfo.getStartDate();
        if (!empTransfer.isJoinworkreq()) {
          dateBefore = new Date(
              empTransfer.getStartDate().getTime() - DecisionTypeConstants.ONE_DAY_IN_MILISEC);

          log.debug("stat:" + startdate);
          log.debug("updateposition.getStartDate():" + oldempInfo.getStartDate());
          log.debug(
              "updateposition.compareTo():" + startdate.compareTo(empTransfer.getStartDate()));
          log.debug("updateposition.dateBefore():" + dateBefore);
          if (startdate.compareTo(empTransfer.getStartDate()) == 0)
            oldempInfo.setEndDate(oldempInfo.getStartDate());
          else
            oldempInfo.setEndDate(dateBefore);
        } else {
          dateBefore = new Date(
              joinRequest.getJoindate().getTime() - DecisionTypeConstants.ONE_DAY_IN_MILISEC);

          log.debug("stat:" + startdate);
          log.debug("updateposition.getStartDate():" + oldempInfo.getStartDate());
          log.debug(
              "updateposition.compareTo():" + startdate.compareTo(empTransfer.getStartDate()));
          log.debug("updateposition.dateBefore():" + dateBefore);
          if (startdate.compareTo(joinRequest.getJoindate()) == 0)
            oldempInfo.setEndDate(oldempInfo.getStartDate());
          else
            oldempInfo.setEndDate(dateBefore);
        }
        oldempInfo.setEnabled(false);
        oldempInfo.setSecpositionGrade(null);
        oldempInfo.setSecjobno(null);
        oldempInfo.setSecjobcode(null);
        oldempInfo.setSecjobtitle(null);
        oldempInfo.setSECDeptName(null);
        oldempInfo.setSECDeptCode(null);
        oldempInfo.setAssignedDepartment(null);
        oldempInfo.setSECSectionCode(null);
        oldempInfo.setSECSectionName(null);
        oldempInfo.setSECLocation(null);
        oldempInfo.setSECStartdate(null);
        oldempInfo.setSECEnddate(null);
        oldempInfo.setSECDecisionDate(null);
        oldempInfo.setSECDecisionNo(null);
        oldempInfo.setSECChangeReason(null);
        oldempInfo.setSECEmploymentNumber(null);
        oldempInfo.setAlertStatus("INACT");

        OBDal.getInstance().save(oldempInfo);
        OBDal.getInstance().flush();

        OBQuery<EmployeeDelegation> del = OBDal.getInstance().createQuery(EmployeeDelegation.class,
            " ehcmEmploymentInfo.id='" + oldempInfo.getId()
                + "' and enabled='Y'  order by  creationDate desc ");
        del.setMaxResult(1);
        if (del.list().size() > 0) {
          EmployeeDelegation delegation = del.list().get(0);
          delegation.setEhcmEmploymentInfo(empInfo);
          OBDal.getInstance().save(delegation);
          OBDal.getInstance().flush();
        }
        log.debug("getEndDate:" + empInfo.getEndDate());
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in insertEmploymentInfo in EmpTransferIssueDecisionDAO: ", e);
    }
  }

  public static void updatePositionAssEmp(EHCMEmpTransfer empTransfer, VariablesSecureApp vars)
      throws Exception {
    // TODO Auto-generated method stub
    Date dateBeforeForassign = null;
    int millSec = 1 * 24 * 3600 * 1000;
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
    try {
      if (empTransfer.getNEWEhcmPosition() != null) {

        EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
            empTransfer.getPosition().getId());
        log.debug("employInfo.getEhcmEmpTransfer:" + empTransfer.getPosition().getJOBNo());
        /*
         * Task No.6797 pos.setAssignedEmployee(null); OBDal.getInstance().save(pos);
         * OBDal.getInstance().flush();
         */

        EhcmPosition newpos = OBDal.getInstance().get(EhcmPosition.class,
            empTransfer.getNEWEhcmPosition().getId());
        /*
         * Task No.6797 newpos.setAssignedEmployee( OBDal.getInstance().get(EmployeeView.class,
         * empTransfer.getEhcmEmpPerinfo().getId())); OBDal.getInstance().save(newpos);
         * OBDal.getInstance().flush();
         */
        // update case if original decision no is there
        if (empTransfer.getOriginalDecisionsNo() != null) {
          /*
           * revert the old changes whatever we did while doing the issue decision for selected
           * original decision no and return current position ( before doing transfer(previous
           * employment info record of transfer) what is the position of the employee)
           */
          EhcmPosition currentPos = assingedOrReleaseEmpInPositionDAO
              .revertOldValuesAndGetOldestPosition(empTransfer.getEhcmEmpPerinfo(), empTransfer,
                  null, null, false);

          // if current position is not equal to current transfer new position
          if (!currentPos.getId().equals(empTransfer.getNEWEhcmPosition().getId())) {
            /*
             * if (!empTransfer.getPosition().getId()
             * .equals(empTransfer.getNEWEhcmPosition().getId()) &&
             * !currentPos.getId().equals(empTransfer.getPosition().getId())) {
             * assingedOrReleaseEmpInPositionDAO
             * .deletePositionEmployeeHisotry(empTransfer.getEhcmEmpPerinfo(), pos); }
             */

            dateBeforeForassign = new Date(empTransfer.getStartDate().getTime() - millSec);

            // update the enddate & transferid for currentposition
            assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
                empTransfer.getEhcmEmpPerinfo(), currentPos, dateBeforeForassign, empTransfer, null,
                null, null, null, null, null);
            // insert the new position in employee history
            assingedOrReleaseEmpInPositionDAO.insertPositionEmployeeHisotry(empTransfer.getClient(),
                empTransfer.getOrganization(), empTransfer.getEhcmEmpPerinfo(), null,
                empTransfer.getStartDate(), empTransfer.getEndDate(), empTransfer.getDecisionNo(),
                empTransfer.getDecisionDate(), newpos, vars, empTransfer, null, null);
          }
          // if current position is equal to current transfer new position then update transfer id
          // and update the enddate as transfer enddate if enddate given
          else {
            // if (empTransfer.getEndDate() != null) {
            assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
                empTransfer.getEhcmEmpPerinfo(), currentPos, empTransfer.getEndDate(), empTransfer,
                null, null, null, null, null, null);
            // }
          }

        } else {
          // create case

          // if current position and selected new position is not equal then update enddate for old
          // position and insert new pos emp history for new position
          if (!pos.getId().equals(newpos.getId())) {
            dateBeforeForassign = new Date(empTransfer.getStartDate().getTime() - millSec);

            // update old pos enddate as current transfer startdate -1
            assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
                empTransfer.getEhcmEmpPerinfo(), pos, dateBeforeForassign, empTransfer, null, null,
                null, null, null, null);

            // insert new pos emp history for current transfer of new position
            assingedOrReleaseEmpInPositionDAO.insertPositionEmployeeHisotry(empTransfer.getClient(),
                empTransfer.getOrganization(), empTransfer.getEhcmEmpPerinfo(), null,
                empTransfer.getStartDate(), empTransfer.getEndDate(), empTransfer.getDecisionNo(),
                empTransfer.getDecisionDate(), newpos, vars, empTransfer, null, null);

          } else {
            // if current position and selected new position is equal then update transferid of
            // currentposition , and in tranfer having enddate also so need to update the enddate
            assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
                empTransfer.getEhcmEmpPerinfo(), newpos, empTransfer.getEndDate(), empTransfer,
                null, null, null, null, null, null);
          }
        }
      }
    } catch (

    Exception e) {
      e.printStackTrace();
      log.error("Exception in updatePositionAssEmp in EmpTransferIssueDecisionDAO: ", e);
    }
  }

  public static EmploymentInfo getRecentEmployInfoOtherThanCurrRecd(EHCMEmpTransfer empTransfer,
      EmploymentInfo employInfo) {
    OBQuery<EmploymentInfo> empInfo = null;
    EmploymentInfo empinfo = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    try {
      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId  and id <> :employInfoId  order by creationDate desc "); // and
      // enabled='Y' and enabled='Y' and alertStatus='ACT'
      empInfo.setNamedParameter("employeeId", empTransfer.getEhcmEmpPerinfo().getId());
      empInfo.setNamedParameter("employInfoId", employInfo.getId());
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        empinfo = employmentInfo.get(0);
        return empinfo;
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      log.error("Exception in getRecentEmployInfoOtherThanCurrRecd " + e.getMessage());
      e.printStackTrace();
    }
    return empinfo;
  }

  public static void updateOldEmpTransferInAct(EHCMEmpTransfer empTransfer) {
    // TODO Auto-generated method stub
    try {
      // update old scholarship as inactive
      EHCMEmpTransfer oldTransfer = empTransfer.getOriginalDecisionsNo();
      if (oldTransfer != null) {
        oldTransfer.setEnabled(false);
        OBDal.getInstance().save(oldTransfer);
        OBDal.getInstance().flush();
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateOldEmpTransferInAct in EmpTransferIssueDecisionDAO: ", e);
    }
  }

  public static void updateOldEmpTransferActCancel(EHCMEmpTransfer empTransfer,
      EmploymentInfo empinfo, VariablesSecureApp vars) {
    // TODO Auto-generated method stub
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
    try {
      Date endDate = null;
      EmploymentInfo empInfor = null;
      /*
       * // update old employment info as active empinfo.setUpdated(new java.util.Date());
       * empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
       * empinfo.setEndDate(null); empinfo.setEnabled(true); empinfo.setAlertStatus("ACT");
       * 
       * OBDal.getInstance().save(empinfo);
       */

      // update position
      if (empinfo.getPosition() != null) {
        updateEmpPositionInCancel(empTransfer, empinfo);

      }

      // update delegation values
      OBQuery<EmployeeDelegation> del = OBDal.getInstance().createQuery(EmployeeDelegation.class,
          " ehcmEmpPerinfo.id='" + empTransfer.getEhcmEmpPerinfo().getId()
              + "' and enabled='Y' order by creationDate desc");
      del.setMaxResult(1);
      if (del.list().size() > 0) {
        EmployeeDelegation delegation = del.list().get(0);
        log.debug("delegation:" + delegation.getEhcmEmploymentInfo().getId());
        delegation.setEhcmEmploymentInfo(empinfo);
        log.debug("delegation:" + delegation.getId());
        OBDal.getInstance().save(delegation);
        OBDal.getInstance().flush();
        OBDal.getInstance().refresh(delegation);
        log.debug("delegation:" + delegation.getEhcmEmploymentInfo().getId());

        if (delegation.getNewPosition() != null) {
          EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
              delegation.getNewPosition().getId());
          empinfo.setSecpositionGrade(objPosition.getGrade());
          empinfo.setSecjobno(objPosition);
          empinfo.setSecjobcode(objPosition.getEhcmJobs());
          empinfo.setSecjobtitle(objPosition.getEhcmJobs().getJOBTitle());
        }
        if (delegation.getNewDepartment() != null) {
          empinfo.setSECDeptName(delegation.getNewDepartment().getName());
          empinfo.setSECDeptCode(delegation.getNewDepartment());
          empinfo.setAssignedDepartment(delegation.getNewDepartment());
        }
        if (delegation.getNewSection() != null) {
          empinfo.setSECSectionCode(delegation.getNewSection());
          empinfo.setSECSectionName(delegation.getNewSection().getName());
          if (delegation.getNewSection().getEhcmLocation() != null) {
            empinfo.setSECLocation(delegation.getNewSection().getEhcmLocation().getLocationName());
          }
        }
        if (delegation.getStartDate() != null) {
          empinfo.setSECStartdate(delegation.getStartDate());
        }
        if (delegation.getEndDate() != null) {
          empinfo.setSECEnddate(delegation.getEndDate());
        }
        if (delegation.getDecisionDate() != null) {
          empinfo.setSECDecisionDate(delegation.getDecisionDate());
        }
        empinfo.setSECDecisionNo(delegation.getDecisionNo());
        empinfo.setSECChangeReason(delegation.getDelegationType());
        empinfo.setSECEmploymentNumber(delegation.getEhcmEmpPerinfo().getSearchKey());
        OBDal.getInstance().save(empinfo);
        OBDal.getInstance().flush();
      }
      // remove the recent record
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id='" + empTransfer.getEhcmEmpPerinfo().getId()
              + "'  and enabled='Y' and id not in ('" + empinfo.getId()
              + "') order by creationDate desc ");
      empInfo.setMaxResult(1);
      if (empInfo.list().size() > 0) {
        empInfor = empInfo.list().get(0);

        log.debug("info:" + empInfor.getEhcmEmpTransfer());
        OBQuery<EmployeeDelegation> delegate = OBDal.getInstance().createQuery(
            EmployeeDelegation.class,
            " ehcmEmploymentInfo.id='" + empInfor.getId() + "'  order by creationDate desc");
        if (delegate.list().size() > 0) {
          for (EmployeeDelegation delgate : delegate.list()) {
            delgate.setEhcmEmploymentInfo(empinfo);
            OBDal.getInstance().save(delgate);
            OBDal.getInstance().flush();
          }
        }
        OBDal.getInstance().remove(empInfor);
        // OBDal.getInstance().flush();
      }
      // update old employment info as active
      empinfo.setUpdated(new java.util.Date());
      empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      endDate = ExtendServiceHandlerDAO.updateEndDateInEmploymentInfo(
          empTransfer.getEhcmEmpPerinfo().getId(), empTransfer.getClient().getId(),
          empInfor.getId());
      empinfo.setEndDate(endDate);
      empinfo.setEnabled(true);
      empinfo.setAlertStatus("ACT");

      OBDal.getInstance().save(empinfo);

      updateOldEmpTransferInAct(empTransfer);
      if (!empTransfer.isJoinworkreq()) {
        empTransfer.setEnabled(false);
      } else {
        empTransfer.setEnabled(true);
      }

      OBDal.getInstance().save(empTransfer);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateOldEmpTransferActCancel in EmpTransferIssueDecisionDAO: ", e);
    }
  }

  public static void updateEmpPositionInCancel(EHCMEmpTransfer empTransfer,
      EmploymentInfo empinfo) {
    // TODO Auto-generated method stub
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
    try {

      EhcmPosition pos = null;
      EmploymentInfo recentEmployeInfo = null;
      if (empTransfer.isJoinworkreq().equals(true)) {
        pos = OBDal.getInstance().get(EhcmPosition.class, empTransfer.getNEWEhcmPosition().getId());
      } else {
        pos = OBDal.getInstance().get(EhcmPosition.class, empTransfer.getPosition().getId());
      }
      /*
       * Task No.6797 pos.setAssignedEmployee(null); OBDal.getInstance().save(pos);
       * OBDal.getInstance().flush();
       */
      /*
       * EhcmPosition currentPos = assingedOrReleaseEmpInPositionDAO.getRecentPosition(
       * empTransfer.getEhcmEmpPerinfo(), null, empTransfer.getOriginalDecisionsNo(), null);
       */
      if (empTransfer.isJoinworkreq().equals(true)) {
        recentEmployeInfo = assingedOrReleaseEmpInPositionDAO
            .getRecentEmploymentInfo(empTransfer.getEhcmEmpPerinfo(), null, empTransfer, null);
      } else {
        recentEmployeInfo = assingedOrReleaseEmpInPositionDAO.getRecentEmploymentInfo(
            empTransfer.getEhcmEmpPerinfo(), null, empTransfer.getOriginalDecisionsNo(), null);
      }

      if (pos != null && recentEmployeInfo != null && recentEmployeInfo.getPosition() != null
          && !recentEmployeInfo.getPosition().getId().equals(pos.getId())) {
        if (pos != null) {
          assingedOrReleaseEmpInPositionDAO
              .deletePositionEmployeeHisotry(empTransfer.getEhcmEmpPerinfo(), pos);
        }
      }

      /*
       * EhcmPosition newpos = OBDal.getInstance().get(EhcmPosition.class,
       * empinfo.getPosition().getId());
       * 
       * Task No.6797 newpos.setAssignedEmployee( OBDal.getInstance().get(EmployeeView.class,
       * empTransfer.getEhcmEmpPerinfo().getId())); OBDal.getInstance().save(newpos);
       * OBDal.getInstance().flush();
       */
      if (recentEmployeInfo != null) {
        assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
            empTransfer.getEhcmEmpPerinfo(), recentEmployeInfo.getPosition(), null, empTransfer,
            null, null, null, null, null, recentEmployeInfo);
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateOldEmpTransferActCancel in EmpTransferIssueDecisionDAO: ", e);
    }
  }

  public boolean chkCrtTransferSamePeriod(EHCMEmpTransfer empTransfer) {
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    OBQuery<EmploymentInfo> empInfo = null;
    String hql = "";
    try {

      if (empTransfer.getOriginalDecisionsNo() != null) {
        hql = "  and (ehcmEmpTransfer.id is null or ehcmEmpTransfer.id<>:transferId) ";
      }
      hql += " and ((to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate,'dd-MM-yyyy') "
          + " and to_date(to_char(coalesce (endDate,startDate),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + " or (to_date(to_char( coalesce (endDate,startDate) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate,'dd-MM-yyyy') "
          + " and to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')))";

      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and client.id=:clientId " + hql);// and enabled ='Y'
      empInfo.setNamedParameter("employeeId", empTransfer.getEhcmEmpPerinfo().getId());
      empInfo.setNamedParameter("clientId", empTransfer.getClient().getId());
      if (empTransfer.getOriginalDecisionsNo() != null) {
        empInfo.setNamedParameter("transferId", empTransfer.getOriginalDecisionsNo().getId());
      }
      empInfo.setNamedParameter("startDate", Utility.formatDate(empTransfer.getStartDate()));
      if (empTransfer.getEndDate() != null)
        empInfo.setNamedParameter("endDate", Utility.formatDate(empTransfer.getEndDate()));
      else
        empInfo.setNamedParameter("endDate", Utility.formatDate(empTransfer.getStartDate()));
      log.debug("empInfo:" + empInfo.getWhereAndOrderBy());
      empInfoList = empInfo.list();
      if (empInfoList.size() > 0) {
        return true;
      }
      // return true;
    } catch (Exception e) {
      log.error("error while chkCrtTransferSamePeriod", e);
      return false;
    }
    return false;
  }
}
