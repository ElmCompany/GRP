package sa.elm.ob.hcm.ad_process;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMBusMissionSummary;
import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMEmpPromotion;
import sa.elm.ob.hcm.EHCMEmpTransfer;
import sa.elm.ob.hcm.EHCMScholarshipSummary;
import sa.elm.ob.hcm.EhcmJoiningWorkRequest;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.EmployeePromotion.EmployeePromotionHandlerDAO;
import sa.elm.ob.hcm.ad_process.EmployeeTransfer.EmpTransferIssueDecisionDAO;
import sa.elm.ob.hcm.ad_process.JoinWorkRequest.DAO.JoinReqProcessDAO;

/**
 * @author poongodi on 26/02/2018
 */
public class JoinReqReactivateProcess extends DalBaseProcess {

  private static final Logger log = LoggerFactory.getLogger(JoinReqReactivateProcess.class);
  private final OBError obError = new OBError();

  @SuppressWarnings("rawtypes")
  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub

    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);

    log.debug("entering into JoinReqReactivateProcess");
    try {
      OBContext.setAdminMode();
      final String joinReqId = bundle.getParams().get("Ehcm_Join_Workrequest_ID").toString();
      EhcmJoiningWorkRequest joinReqProcess = OBDal.getInstance().get(EhcmJoiningWorkRequest.class,
          joinReqId);
      String employmentId = joinReqProcess.getOriginalDecisionNo().getId();
      List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
      Date Startdate = null;
      boolean tranisjoinWorkReq = false;
      boolean promotionisjoinReq = false;
      boolean susjoinFlag = false;
      EHCMEmpTransfer empTransfer = null;
      EHCMEmpPromotion empPromotion = null;
      EmployeeSuspension empSuspension = null;
      EmployeeSuspension oldSuspension = null;
      EHCMEMPTermination oldTermination = null;
      JoinReqProcessDAO joinReqProcessDAO = new JoinReqProcessDAO();
      boolean chkIsActive = false;
      boolean chkEmplyInfoExistAfterJWR = false;
      int millSec = 1 * 24 * 3600 * 1000, a = 0;

      EmploymentInfo employmentInfo = OBDal.getInstance().get(EmploymentInfo.class, employmentId);
      chkEmplyInfoExistAfterJWR = JoinReqProcessDAO.chkEmplyInfoExistAfterJWR(joinReqProcess,
          employmentInfo);
      if (chkEmplyInfoExistAfterJWR) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_JoinIsactive_Error"));
        bundle.setResult(obError);
        return;
      }
      chkIsActive = JoinReqProcessDAO.chkIsActiveRecord(joinReqProcess);
      if (chkIsActive) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_JoinIsactive_Error"));
        bundle.setResult(obError);
        return;
      }
      // Employee Transfer
      if (joinReqProcess.getJoinWorkreason().equals("PRT")) {
        empTransfer = OBDal.getInstance().get(EHCMEmpTransfer.class, employmentId);
        if (empTransfer != null && empTransfer.isJoinworkreq()) {
          tranisjoinWorkReq = true;
        }
      }
      // Employee Promotion
      if (joinReqProcess.getJoinWorkreason().equals("PR")) {
        empPromotion = OBDal.getInstance().get(EHCMEmpPromotion.class, employmentId);
        if (empPromotion != null && empPromotion.isJoinWorkRequest()) {
          promotionisjoinReq = true;
        }
      }
      // Employee Suspension
      if (joinReqProcess.getJoinWorkreason().equals("SUS")) {
        empSuspension = OBDal.getInstance().get(EmployeeSuspension.class, employmentId);

        if (empSuspension != null) {
          oldSuspension = empSuspension.getOriginalDecisionNo();
          if (oldSuspension != null) {
            OBQuery<EHCMEMPTermination> oldTerminationQry = OBDal.getInstance().createQuery(
                EHCMEMPTermination.class,
                "as e where  e.ehcmEmpSuspension.id = '" + empSuspension.getId() + "' ");
            if (oldTerminationQry.list().size() > 0) {
              oldTermination = oldTerminationQry.list().get(0);
            }
          }
          if (empSuspension.isJoinWorkRequestRequired())
            susjoinFlag = true;
        }
      }
      if (!joinReqProcess.getJoinWorkreason().equals("SCTR")
          && !joinReqProcess.getJoinWorkreason().equals("BM") && !tranisjoinWorkReq
          && !promotionisjoinReq && !susjoinFlag
          && !joinReqProcess.getJoinWorkreason().equals("SEC")) {
        EmploymentInfo employmentInfoObj = OBDal.getInstance().get(EmploymentInfo.class,
            employmentId);
        if (joinReqProcess.getJoinWorkreason().equals("SEC")) {
          Startdate = employmentInfoObj.getEhcmEmpSecondment().getStartDate();
        }
        if (joinReqProcess.getJoinWorkreason().equals("PR")) {
          Startdate = employmentInfoObj.getEhcmEmpPromotion().getStartDate();
        }
        if (joinReqProcess.getJoinWorkreason().equals("PRT")) {
          Startdate = employmentInfoObj.getEhcmEmpTransfer().getStartDate();
        }
        if (joinReqProcess.getJoinWorkreason().equals("SUS")) {
          Startdate = employmentInfoObj.getEhcmEmpSuspension().getStartDate();
        }
        if (joinReqProcess.getJoinWorkreason().equals("H")) {
          Startdate = joinReqProcess.getDecisionDate();
        }

        employmentInfoObj.setStartDate(Startdate);
        if (employmentInfoObj.getEhcmJoinWorkrequest() != null) {
          employmentInfoObj.setEhcmJoinWorkrequest(null);
          employmentInfoObj.setJoinworkreq(false);
        }
        OBDal.getInstance().save(employmentInfoObj);
        OBDal.getInstance().flush();
        log.debug("startdate" + Startdate);
        OBQuery<EmploymentInfo> empoldrecord = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " id not in ('" + employmentId
                + "') and ehcmEmpPerinfo.id = :employeeId and creationDate < '"
                + employmentInfoObj.getCreationDate() + "'  order by creationDate desc ");
        empoldrecord.setNamedParameter("employeeId", joinReqProcess.getEmployee().getId());
        empInfoList = empoldrecord.list();

        if (empoldrecord.list().size() > 0) {
          for (EmploymentInfo empinfo : empoldrecord.list()) {
            if (!joinReqProcess.getJoinWorkreason().equals("SEC")
                && !joinReqProcess.getJoinWorkreason().equals("PR")
                || (joinReqProcess.getJoinWorkreason().equals("SEC")
                    && !empinfo.getChangereason().equals("H")
                    && !empinfo.getChangereason().equals("PR")
                    && !empinfo.getChangereason().equals("TR"))
                || (joinReqProcess.getJoinWorkreason().equals("PR")
                    && !empinfo.getChangereason().equals("SEC"))) {
              millSec = 1 * 24 * 3600 * 1000;
              Date dateBefore = new Date(employmentInfoObj.getStartDate().getTime() - millSec);
              if (empinfo.getEndDate() != null) {
                empinfo.setEndDate(dateBefore);
              }

              OBDal.getInstance().save(empinfo);
              OBDal.getInstance().flush();
            }
          }
        }
      }
      // secondment
      else if (joinReqProcess.getJoinWorkreason().equals("SEC")) {
        JoinReqProcessDAO.deleteEmployeInfo(joinReqProcess);
        EmploymentInfo secInfo = OBDal.getInstance().get(EmploymentInfo.class, employmentId);
        secInfo.setAlertStatus(Constants.EMPSTATUS_ACTIVE);
        OBDal.getInstance().save(secInfo);

      } else if (joinReqProcess.getJoinWorkreason().equals("SCTR")) {
        EHCMScholarshipSummary scholarsummary = OBDal.getInstance()
            .get(EHCMScholarshipSummary.class, employmentId);
        millSec = 1 * 24 * 3600 * 1000;
        Date dateBefore = new Date(joinReqProcess.getDecisionDate().getTime() - millSec);
        scholarsummary.setEndDate(dateBefore);
        OBDal.getInstance().save(scholarsummary);
        OBDal.getInstance().flush();
      } else if (joinReqProcess.getJoinWorkreason().equals("BM")) {
        EHCMBusMissionSummary missionCategory = OBDal.getInstance().get(EHCMBusMissionSummary.class,
            employmentId);
        if (missionCategory != null) {
          EHCMEmpBusinessMission mission = missionCategory.getEhcmEmpBusinessmission();
          missionCategory.setEndDate(mission.getEndDate());
        }
        OBDal.getInstance().save(missionCategory);
        OBDal.getInstance().flush();
      }
      if (tranisjoinWorkReq) {
        EmploymentInfo employmentinfo = null;
        // update the acive flag='Y' and enddate is null for recently update record
        OBQuery<EmploymentInfo> originalemp = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ehcmEmpPerinfo.id='" + joinReqProcess.getEmployee().getId()
                + "' and enabled='N' order by creationDate desc ");
        originalemp.setMaxResult(1);
        if (originalemp.list().size() > 0) {
          employmentinfo = originalemp.list().get(0);
        }
        EmpTransferIssueDecisionDAO.updateOldEmpTransferActCancel(empTransfer, employmentinfo,
            vars);

      }
      if (promotionisjoinReq) {
        EmployeePromotionHandlerDAO.CancelinPromotion(empPromotion, vars);
      }
      if (susjoinFlag) {
        int count = JoinReqProcessDAO.cancelEmploymentRecord(empSuspension, oldSuspension,
            oldTermination, vars);
      }
      joinReqProcess.setEhcmReactivate(true);
      joinReqProcess.setDecisionStatus("UP");
      joinReqProcess.setSueDecision(false);
      OBError result = OBErrorBuilder.buildMessage(null, "success", "@Ehcm_Join_Reactivate@");
      bundle.setResult(result);
      return;

    }

    catch (Exception e) {
      e.printStackTrace();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
      OBDal.getInstance().commitAndClose();
    }
  }

}
