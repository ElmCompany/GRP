package sa.elm.ob.hcm.ad_process.EmployeeTransferSelf;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import sa.elm.ob.hcm.EHCMEmpTransferSelf;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;

public class EmpEmpTransferSelfIssueDecision implements Process {
  private static final Logger log = Logger.getLogger(EmpEmpTransferSelfIssueDecision.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Issue the position");

    final String transferselfId = (String) bundle.getParams().get("Ehcm_Emp_Transfer_Self_ID")
        .toString();
    EHCMEmpTransferSelf transfer = OBDal.getInstance().get(EHCMEmpTransferSelf.class,
        transferselfId);
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    EmploymentInfo info = null;
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
    log.debug("transferId:" + transferselfId);
    Boolean chkPositionAvailableOrNot = false;
    try {
      OBContext.setAdminMode(true);
      log.debug("isSueDecision:" + transfer.isSueDecision());
      log.debug("getDecisionType:" + transfer.getDecisionType());

      // checking position is available or not
      if (!transfer.getDecisionType().equals("CA")) {
        EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class,
            transfer.getNewPosition().getId());
        if (!position.getId().equals(transfer.getPosition().getId())) {
          chkPositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO.chkPositionAvailableOrNot(
              transfer.getEhcmEmpPerinfo(), position, transfer.getStartDate(),
              transfer.getEndDate(), transfer.getDecisionType(), false);
          if (chkPositionAvailableOrNot) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("EHCM_PosNotAvailable"));
            bundle.setResult(obError);
            return;
          }
        }
      } else {
        /*
         * EhcmPosition currentPos = assingedOrReleaseEmpInPositionDAO.getRecentPosition(
         * transfer.getEhcmEmpPerinfo(), null, transfer.getOriginalDecisionsNo(), null);
         */

        EmploymentInfo recentEmployeInfo = assingedOrReleaseEmpInPositionDAO
            .getRecentEmploymentInfo(transfer.getEhcmEmpPerinfo(), null, null,
                transfer.getOriginalDecisionsNo());

        if (recentEmployeInfo != null && recentEmployeInfo.getPosition() != null
            && !recentEmployeInfo.getPosition().getId().equals(transfer.getPosition().getId())) {
          chkPositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO.chkPositionAvailableOrNot(
              transfer.getEhcmEmpPerinfo(), recentEmployeInfo.getPosition(),
              transfer.getStartDate(), null, transfer.getDecisionType(), false);
          if (chkPositionAvailableOrNot) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("EHCM_PosNotAvailable"));
            bundle.setResult(obError);
            return;
          }
        }
      }

      if (transfer.getDecisionType().equals("CA")) {
        // dont allow to cancel the transfer if the employee has promoted
        OBQuery<EmploymentInfo> chkemphavepromt = OBDal.getInstance()
            .createQuery(EmploymentInfo.class, " as e where e.ehcmEmpPerinfo.id='"
                + transfer.getEhcmEmpPerinfo().getId()
                + "' and e.enabled='Y' and e.ehcmEmpPromotion is not null order by e.creationDate desc ");
        log.debug("chkemphavepromt:" + chkemphavepromt.list().size());
        if (chkemphavepromt.list().size() > 0) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_EmpTranCan_Error"));
          bundle.setResult(obError);
          return;
        }
      }

      // check Issued or not
      if (!transfer.isSueDecision()) {
        // update status as Issued and set decision date for all cases
        transfer.setSueDecision(true);
        transfer.setDecisionDate(new Date());
        transfer.setDecisionStatus("I");
        OBDal.getInstance().save(transfer);
        OBDal.getInstance().flush();

        // Create Cases
        if (transfer.getDecisionType().equals("CR")) {

          // get employment Information for getting the values Location,payroll,payscale
          OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " as e where ehcmEmpPerinfo.id='" + transfer.getEhcmEmpPerinfo().getId()
                  + "' and e.enabled='Y' order by e.creationDate desc");
          empInfo.setMaxResult(1);
          if (empInfo.list().size() > 0) {
            info = empInfo.list().get(0);
          }
          // Create a record in Employement Information Window
          EmploymentInfo employInfo = OBProvider.getInstance().get(EmploymentInfo.class);
          /*
           * if (transfer.getTransferType().equals("ID")) transferType = "Inside Department"; else
           * transferType = "Outside Department"; employInfo.setChangereason(transferType);
           */
          employInfo.setChangereason(transfer.getTransferType());
          if (transfer.getNewDepartment() != null) {
            employInfo.setDepartmentName(transfer.getNewDepartment().getName());
            employInfo.setDeptcode(transfer.getNewDepartment());

          }

          else {
            employInfo.setDepartmentName(transfer.getDepartmentCode().getName());
            employInfo.setDeptcode(transfer.getDepartmentCode());
          }

          employInfo.setEhcmPayscale(info.getEhcmPayscale());
          employInfo.setEmpcategory(transfer.getEmployeeCategory().getId());
          employInfo.setEmployeeno(transfer.getEhcmEmpPerinfo().getSearchKey());
          if (transfer.getNewPosition() != null) {
            employInfo.setGrade(transfer.getNewPosition().getGrade());
            employInfo.setJobcode(OBDal.getInstance().get(Jobs.class, transfer.getNewJobCode()));
            employInfo.setPosition(
                OBDal.getInstance().get(EhcmPosition.class, transfer.getNewPosition().getId()));
            employInfo.setJobtitle(transfer.getNewJobTitle());

          } else {
            employInfo.setGrade(transfer.getGrade());
            employInfo.setJobcode(transfer.getNewPosition().getEhcmJobs());
            employInfo.setPosition(transfer.getPosition());
            employInfo.setJobtitle(transfer.getTitle());

          }
          employInfo.setEhcmPayscaleline(info.getEhcmPayscaleline());
          if (info.getToGovernmentAgency() != null)
            employInfo.setToGovernmentAgency(info.getToGovernmentAgency());
          employInfo.setLocation(info.getLocation());
          if (info.getEhcmPayrollDefinition() != null)
            employInfo.setEhcmPayrollDefinition(info.getEhcmPayrollDefinition());
          if (transfer.getNewSectionCode() != null) {
            employInfo.setSectionName(transfer.getNewSectionCode().getName());
            employInfo.setSectioncode(transfer.getNewSectionCode());

          }

          else {
            if (transfer.getSectionCode() != null) {
              employInfo.setSectionName(transfer.getSectionCode().getName());
              employInfo.setSectioncode(transfer.getSectionCode());
            }
          }
          employInfo.setEhcmEmpPerinfo(transfer.getEhcmEmpPerinfo());
          employInfo.setStartDate(transfer.getStartDate());
          employInfo.setEndDate(null);
          employInfo.setAlertStatus("ACT");
          employInfo.setEhcmEmpTransferSelf(transfer);
          employInfo.setDecisionNo(transfer.getDecisionNo());
          employInfo.setDecisionDate(transfer.getDecisionDate());
          employInfo.setEmploymentgrade(info.getEmploymentgrade());

          /* secondary */

          employInfo.setSecpositionGrade(info.getSecpositionGrade());
          employInfo.setSecpositionGrade(info.getSecpositionGrade());
          employInfo.setSecjobno(info.getSecjobno());
          employInfo.setSecjobcode(info.getSecjobcode());
          employInfo.setSecjobtitle(info.getSecjobtitle());
          employInfo.setSECDeptCode(info.getSECDeptCode());
          employInfo.setAssignedDepartment(info.getAssignedDepartment());
          employInfo.setSECDeptName(info.getSECDeptName());
          if (info.getSECSectionCode() != null) {
            employInfo.setSECSectionCode(info.getSECSectionCode());
            employInfo.setSECSectionName(info.getSECSectionName());
          }
          employInfo.setSECLocation(info.getSECLocation());

          employInfo.setSECStartdate(info.getSECStartdate());
          employInfo.setSECEnddate(info.getSECEnddate());
          employInfo.setSECDecisionNo(info.getSECDecisionNo());
          employInfo.setSECDecisionDate(info.getSECDecisionDate());

          employInfo.setSECChangeReason(info.getSECChangeReason());
          employInfo.setSECEmploymentNumber(info.getSECEmploymentNumber());

          OBDal.getInstance().save(employInfo);
          OBDal.getInstance().flush();

          if (transfer.getNewPosition() != null) {
            updatePositionAssEmp(transfer, vars);
          }
          log.debug(
              "employInfo.getEhcmEmpTransferSelf:" + employInfo.getEhcmEmpTransferSelf().getId());
          log.debug("getChangereason:" + employInfo.getChangereason());

          // update the endate and active flag for old hiring record.
          OBQuery<EmploymentInfo> empInfoold = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id='" + transfer.getEhcmEmpPerinfo().getId() + "'  and id not in ('"
                  + employInfo.getId() + "') and enabled='Y'  order by  creationDate desc ");
          empInfoold.setMaxResult(1);
          if (empInfoold.list().size() > 0) {
            EmploymentInfo empinfo = empInfoold.list().get(0);
            // for (EmploymentInfo empinfo : empInfoold.list()) {
            empinfo.setUpdated(new java.util.Date());
            empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
            Date startdate = empinfo.getStartDate();
            Date dateBefore = new Date(transfer.getStartDate().getTime() - 1 * 24 * 3600 * 1000);

            log.debug("stat:" + startdate);
            log.debug("updateposition.getStartDate():" + empinfo.getStartDate());
            log.debug("updateposition.compareTo():" + startdate.compareTo(transfer.getStartDate()));
            log.debug("updateposition.dateBefore():" + dateBefore);
            if (startdate.compareTo(transfer.getStartDate()) == 0)
              empinfo.setEndDate(empinfo.getStartDate());
            else
              empinfo.setEndDate(dateBefore);
            empinfo.setEnabled(false);
            empinfo.setSecpositionGrade(null);
            empinfo.setSecjobno(null);
            empinfo.setSecjobcode(null);
            empinfo.setSecjobtitle(null);
            empinfo.setSECDeptName(null);
            empinfo.setSECDeptCode(null);
            empinfo.setAssignedDepartment(null);
            empinfo.setSECSectionCode(null);
            empinfo.setSECSectionName(null);
            empinfo.setSECLocation(null);
            empinfo.setSECStartdate(null);
            empinfo.setSECEnddate(null);
            empinfo.setSECDecisionDate(null);
            empinfo.setSECDecisionNo(null);
            empinfo.setSECChangeReason(null);
            empinfo.setSECEmploymentNumber(null);
            empinfo.setAlertStatus("INACT");
            empinfo.setDecisionNo(transfer.getDecisionNo());
            empinfo.setDecisionDate(transfer.getDecisionDate());

            OBDal.getInstance().save(empinfo);
            OBDal.getInstance().flush();

            OBQuery<EmployeeDelegation> del = OBDal.getInstance()
                .createQuery(EmployeeDelegation.class, " ehcmEmploymentInfo.id='" + empinfo.getId()
                    + "' and enabled='Y'  order by  creationDate desc ");
            del.setMaxResult(1);
            if (del.list().size() > 0) {
              EmployeeDelegation delegation = del.list().get(0);
              delegation.setEhcmEmploymentInfo(employInfo);
              OBDal.getInstance().save(delegation);
              OBDal.getInstance().flush();
            }
            log.debug("getEndDate:" + empinfo.getEndDate());
            // }
          }
        }
        // update case
        else if (transfer.getDecisionType().equals("UP")) {

          // getting active record in employment info
          OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id='" + transfer.getEhcmEmpPerinfo().getId()
                  + "' and enabled='Y' order by creationDate desc ");
          if (empInfo.list().size() > 0) {
            // update the "create" record
            for (EmploymentInfo empinfo : empInfo.list()) {
              empinfo.setUpdated(new java.util.Date());
              empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
              /*
               * if (transfer.getTransferType().equals("ID")) transferType = "Inside Department";
               * else transferType = "Outside Department"; empinfo.setChangereason(transferType);
               */
              empinfo.setChangereason(transfer.getTransferType());
              if (transfer.getNewDepartment() != null) {
                empinfo.setDepartmentName(transfer.getNewDepartment().getName());
                empinfo.setDeptcode(transfer.getNewDepartment());

              }

              else {
                empinfo.setDepartmentName(transfer.getDepartmentCode().getName());
                empinfo.setDeptcode(transfer.getDepartmentCode());
              }

              // empinfo.setEhcmPayscale(info.getEhcmPayscale());
              empinfo.setEmpcategory(transfer.getGrade().getId());
              empinfo.setEmployeeno(transfer.getEhcmEmpPerinfo().getSearchKey());
              if (transfer.getNewPosition() != null) {
                empinfo.setGrade(transfer.getNewPosition().getGrade());
                empinfo.setJobcode(OBDal.getInstance().get(Jobs.class, transfer.getNewJobCode()));
                empinfo.setPosition(
                    OBDal.getInstance().get(EhcmPosition.class, transfer.getNewPosition().getId()));
                empinfo.setJobtitle(transfer.getNewJobTitle());

              } else {
                empinfo.setGrade(transfer.getGrade());
                empinfo.setJobcode(transfer.getPosition().getEhcmJobs());
                empinfo.setPosition(transfer.getPosition());
                empinfo.setJobtitle(transfer.getTitle());

              }
              if (transfer.getNewSectionCode() != null) {
                empinfo.setSectionName(transfer.getNewSectionCode().getName());
                empinfo.setSectioncode(transfer.getNewSectionCode());

              }

              else {
                if (transfer.getSectionCode() != null) {
                  empinfo.setSectionName(transfer.getSectionCode().getName());
                  empinfo.setSectioncode(transfer.getSectionCode());
                }
              }
              empinfo.setEhcmEmpPerinfo(transfer.getEhcmEmpPerinfo());
              empinfo.setStartDate(transfer.getStartDate());
              empinfo.setEndDate(null);
              empinfo.setAlertStatus("ACT");
              empinfo.setEhcmEmpTransferSelf(transfer);
              empinfo.setDecisionNo(transfer.getDecisionNo());
              empinfo.setDecisionDate(transfer.getDecisionDate());
              empinfo.setEmploymentgrade(transfer.getEmploymentGrade());
              OBDal.getInstance().save(empinfo);
              OBDal.getInstance().flush();

              if (transfer.getNewPosition() != null) {
                updatePositionAssEmp(transfer, vars);

              }

              log.debug("getChangereason:" + empinfo.getChangereason());
            }
          }

        }
        // cancel case
        else if (transfer.getDecisionType().equals("CA")) {
          // update the acive flag='Y' and enddate is null for recently update record
          OBQuery<EmploymentInfo> originalemp = OBDal.getInstance().createQuery(
              EmploymentInfo.class, " ehcmEmpPerinfo.id='" + transfer.getEhcmEmpPerinfo().getId()
                  + "' and enabled='N' order by creationDate desc ");
          originalemp.setMaxResult(1);
          if (originalemp.list().size() > 0) {
            EmploymentInfo empinfo = originalemp.list().get(0);
            log.debug("getChangereasoncancel12:" + empinfo.getId());
            empinfo.setUpdated(new java.util.Date());
            empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
            empinfo.setEndDate(null);
            empinfo.setEnabled(true);
            empinfo.setAlertStatus("ACT");
            empinfo.setEhcmEmpTransferSelf(null);
            if (empinfo.getPosition() != null) {
              updateEmpPositionInCancel(transfer, empinfo);
            }

            OBDal.getInstance().save(empinfo);
            OBDal.getInstance().flush();

            OBQuery<EmployeeDelegation> del = OBDal.getInstance().createQuery(
                EmployeeDelegation.class,
                " ehcmEmpPerinfo.id='" + transfer.getEhcmEmpPerinfo().getId()
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
                  empinfo.setSECLocation(
                      delegation.getNewSection().getEhcmLocation().getLocationName());
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
                " ehcmEmpPerinfo.id='" + transfer.getEhcmEmpPerinfo().getId()
                    + "'  and enabled='Y' and id not in ('" + empinfo.getId()
                    + "') order by creationDate desc ");
            empInfo.setMaxResult(1);
            if (empInfo.list().size() > 0) {
              EmploymentInfo empInfor = empInfo.list().get(0);
              // for (EmploymentInfo empinfo : empInfo.list()) {
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
              /*
               * OBQuery<EHCMEmpTransferSelf> emptransfer = OBDal.getInstance().createQuery(
               * EHCMEmpTransferSelf.class, " ehcmEmploymentInfo.id='" + empInfor.getId() + "'"); if
               * (emptransfer.list().size() > 0) { for (EHCMEmpTransferSelf tran :
               * emptransfer.list()) { tran.setEhcmEmploymentInfo(null);
               * OBDal.getInstance().save(tran); OBDal.getInstance().flush(); } }
               */

              OBDal.getInstance().remove(empInfor);
              OBDal.getInstance().flush();

            }

          }
        }

      }
      obError.setType("Success");
      obError.setTitle("Success");
      obError.setMessage(OBMessageUtils.messageBD("Ehcm_Submit_Process"));
      bundle.setResult(obError);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    } catch (Exception e) {
      bundle.setResult(obError);
      log.error("exception :", e);
      OBDal.getInstance().rollbackAndClose();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static void updatePositionAssEmp(EHCMEmpTransferSelf empTransfer, VariablesSecureApp vars)
      throws Exception {
    // TODO Auto-generated method stub
    Date dateBeforeForassign = null;
    int millSec = 1 * 24 * 3600 * 1000;
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
    try {

      EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
          empTransfer.getPosition().getId());
      log.debug("employInfo.getEhcmEmpTransfer:" + empTransfer.getPosition().getJOBNo());
      /*
       * Task No.6797 pos.setAssignedEmployee(null); OBDal.getInstance().save(pos);
       * OBDal.getInstance().flush();
       */

      EhcmPosition newpos = OBDal.getInstance().get(EhcmPosition.class,
          empTransfer.getNewPosition().getId());
      /*
       * Task No.6797 newpos.setAssignedEmployee( OBDal.getInstance().get(EmployeeView.class,
       * empTransfer.getEhcmEmpPerinfo().getId())); OBDal.getInstance().save(newpos);
       * OBDal.getInstance().flush();
       */
      // update case if original decision no is there
      if (empTransfer.getOriginalDecisionsNo() != null) {
        /*
         * revert the old changes whatever we did while doing the issue decision for selected
         * original decision no and return current position ( before doing transferself(previous
         * employment info record of transferself) what is the position of the employee)
         */
        EhcmPosition currentPos = assingedOrReleaseEmpInPositionDAO
            .revertOldValuesAndGetOldestPosition(empTransfer.getEhcmEmpPerinfo(), null, null,
                empTransfer, false);
        // if current position is not equal to current transferself new position
        if (!currentPos.getId().equals(empTransfer.getNewPosition().getId())) {

          /*
           * if (!empTransfer.getPosition().getId().equals(empTransfer.getNewPosition().getId()) &&
           * !currentPos.getId().equals(empTransfer.getPosition().getId())) {
           * assingedOrReleaseEmpInPositionDAO
           * .deletePositionEmployeeHisotry(empTransfer.getEhcmEmpPerinfo(), pos); }
           */

          dateBeforeForassign = new Date(empTransfer.getStartDate().getTime() - millSec);

          // update the enddate & transferselfid for currentposition
          assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
              empTransfer.getEhcmEmpPerinfo(), currentPos, dateBeforeForassign, null, null,
              empTransfer, null, null, null, null);

          // insert the new position in employee history
          assingedOrReleaseEmpInPositionDAO.insertPositionEmployeeHisotry(empTransfer.getClient(),
              empTransfer.getOrganization(), empTransfer.getEhcmEmpPerinfo(), null,
              empTransfer.getStartDate(), empTransfer.getEndDate(), empTransfer.getDecisionNo(),
              empTransfer.getDecisionDate(), newpos, vars, null, null, empTransfer);
        }
        // if current position is equal to current transferself of new position then update
        // transferself id
        // and update the enddate as transfer self enddate if enddate given
        else {
          // if (empTransfer.getEndDate() != null) {
          assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
              empTransfer.getEhcmEmpPerinfo(), currentPos, empTransfer.getEndDate(), null, null,
              empTransfer, null, null, null, null);
          // }
        }

      }
      // create case
      else {

        // if current position and selected new position is not equal then update enddate for old
        // position and insert new pos emp history for new position
        if (!pos.getId().equals(newpos.getId())) {
          dateBeforeForassign = new Date(empTransfer.getStartDate().getTime() - millSec);

          // update old pos enddate as current transfer self startdate -1
          assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
              empTransfer.getEhcmEmpPerinfo(), pos, dateBeforeForassign, null, null, empTransfer,
              null, null, null, null);

          // insert new pos emp history for current transfer self of new position
          assingedOrReleaseEmpInPositionDAO.insertPositionEmployeeHisotry(empTransfer.getClient(),
              empTransfer.getOrganization(), empTransfer.getEhcmEmpPerinfo(), null,
              empTransfer.getStartDate(), empTransfer.getEndDate(), empTransfer.getDecisionNo(),
              empTransfer.getDecisionDate(), newpos, vars, null, null, empTransfer);

        }
        // if current position and selected new position is equal then update transferid of
        // currentposition , and in tranfer having enddate also so need to update the enddate
        else {
          assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
              empTransfer.getEhcmEmpPerinfo(), newpos, empTransfer.getEndDate(), null, null,
              empTransfer, null, null, null, null);
        }
      }
    } catch (

    Exception e) {
      e.printStackTrace();
      log.error("Exception in updatePositionAssEmp in EmpEmpTransferSelfIssueDecision: ", e);
    }
  }

  public static void updateEmpPositionInCancel(EHCMEmpTransferSelf empTransfer,
      EmploymentInfo empinfo) {
    // TODO Auto-generated method stub
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
    try {

      EhcmPosition pos = null;
      EmploymentInfo recentEmployeInfo = null;

      pos = OBDal.getInstance().get(EhcmPosition.class, empTransfer.getPosition().getId());

      /*
       * Task No.6797 pos.setAssignedEmployee(null); OBDal.getInstance().save(pos);
       * OBDal.getInstance().flush();
       */
      /*
       * EhcmPosition currentPos = assingedOrReleaseEmpInPositionDAO.getRecentPosition(
       * empTransfer.getEhcmEmpPerinfo(), null, empTransfer.getOriginalDecisionsNo(), null);
       */

      recentEmployeInfo = assingedOrReleaseEmpInPositionDAO.getRecentEmploymentInfo(
          empTransfer.getEhcmEmpPerinfo(), null, null, empTransfer.getOriginalDecisionsNo());

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
            empTransfer.getEhcmEmpPerinfo(), recentEmployeInfo.getPosition(), null, null, null,
            empTransfer, null, null, null, recentEmployeInfo);
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateEmpPositionInCancel in EmpEmpTransferSelfIssueDecision: ", e);
    }
  }
}
