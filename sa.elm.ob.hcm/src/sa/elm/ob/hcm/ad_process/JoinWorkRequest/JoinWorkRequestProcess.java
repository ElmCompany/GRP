package sa.elm.ob.hcm.ad_process.JoinWorkRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DbUtility;

import sa.elm.ob.hcm.EHCMBusMissionSummary;
import sa.elm.ob.hcm.EHCMEmpPromotion;
import sa.elm.ob.hcm.EHCMEmpTransfer;
import sa.elm.ob.hcm.EHCMScholarshipSummary;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmJoiningWorkRequest;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.EmployeePromotion.EmployeePromotionHandlerDAO;
import sa.elm.ob.hcm.ad_process.EmployeeSecondment.EmpSecondmentDAOImpl;
import sa.elm.ob.hcm.ad_process.EmployeeSuspension.EmpSuspensionHandlerDAO;
import sa.elm.ob.hcm.ad_process.EmployeeTransfer.EmpTransferIssueDecisionDAO;
import sa.elm.ob.hcm.ad_process.JoinWorkRequest.DAO.JoinReqProcessDAO;

/**
 * @author poongodi on 19/02/2018
 */
public class JoinWorkRequestProcess implements Process {
  private static final Logger log = Logger.getLogger(JoinWorkRequestProcess.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub

    final String joinReqId = bundle.getParams().get("Ehcm_Join_Workrequest_ID").toString();
    EhcmJoiningWorkRequest joinReqProcess = OBDal.getInstance().get(EhcmJoiningWorkRequest.class,
        joinReqId);
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String employmentId = null;
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    Boolean transferisjoinWorkReq = false;
    boolean promotionJoinReqFlag = false;
    boolean susjoinFlag = false;
    EHCMEmpTransfer empTransfer = null;
    EHCMEmpPromotion empPromotion = null;
    EmployeeSuspension empSuspension = null;
    EmploymentInfo oldempInfo = null;
    String lang = vars.getLanguage();
    Boolean errorFlag = false;
    Boolean chkPeriodCondition = false;
    int millSec = 1 * 24 * 3600 * 1000;
    EmployeeSuspension oldSuspension = null;
    EmpSecondmentDAOImpl empSecondmentDAOImpl = new EmpSecondmentDAOImpl();
    Boolean chkAnyEmployInfoExistsAfter = false;
    try {
      OBContext.setAdminMode(true);

      if (joinReqProcess.getEmployee() != null
          && joinReqProcess.getEmployee().getStatus().equals("TE")) {
        {
          OBError result = OBErrorBuilder.buildMessage(null, "error", "@Ehcm_joinWorkCancelHire@");
          bundle.setResult(result);
          return;
        }
      }
      if (joinReqProcess.getOriginalDecisionNo() != null) {
        employmentId = joinReqProcess.getOriginalDecisionNo().getId();
      }

      EmploymentInfo empInfo = OBDal.getInstance().get(EmploymentInfo.class, employmentId);

      chkAnyEmployInfoExistsAfter = JoinReqProcessDAO.chkAnyEmployInfoExistsAfter(joinReqProcess,
          empInfo);
      if (chkAnyEmployInfoExistsAfter) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_JoinWorkReqBefProcess"));
        bundle.setResult(obError);
        return;
      }

      if (joinReqProcess.getJoinWorkreason().equals("H")) {
        EmploymentInfo info = OBDal.getInstance().get(EmploymentInfo.class, employmentId);
        chkPeriodCondition = JoinReqProcessDAO.chkPeriodExist(joinReqProcess, employmentId, info);
        if (chkPeriodCondition) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_joinHiring_Period"));
          bundle.setResult(obError);
          return;

        }

      }
      if (!chkPeriodCondition) {
        if (!joinReqProcess.isSueDecision()) {
          joinReqProcess.setSueDecision(true);
          joinReqProcess.setDecisionStatus("I");
          joinReqProcess.setEhcmReactivate(false);
          OBDal.getInstance().save(joinReqProcess);
          OBDal.getInstance().flush();
        }
      }
      // Employee Transfer
      if (joinReqProcess.getJoinWorkreason().equals("PRT")) {
        empTransfer = OBDal.getInstance().get(EHCMEmpTransfer.class, employmentId);
        if (empTransfer != null && empTransfer.isJoinworkreq()) {
          transferisjoinWorkReq = true;
        }
      }
      // Employee Promotion
      if (joinReqProcess.getJoinWorkreason().equals("PR")) {
        empPromotion = OBDal.getInstance().get(EHCMEmpPromotion.class, employmentId);
        if (empPromotion != null && empPromotion.isJoinWorkRequest()) {
          promotionJoinReqFlag = true;
        }
      }
      // Employee Suspension
      if (joinReqProcess.getJoinWorkreason().equals("SUS")) {
        empSuspension = OBDal.getInstance().get(EmployeeSuspension.class, employmentId);
        if (empSuspension != null && empSuspension.isJoinWorkRequestRequired()) {
          oldSuspension = empSuspension.getOriginalDecisionNo();
          susjoinFlag = true;
        }
      }

      // Scholarship
      if (joinReqProcess.getJoinWorkreason().equals("SCTR")) {
        EHCMScholarshipSummary scholarsummary = OBDal.getInstance()
            .get(EHCMScholarshipSummary.class, employmentId);
        scholarsummary.setEndDate(joinReqProcess.getJoindate());
        OBDal.getInstance().save(scholarsummary);
        OBDal.getInstance().flush();
      }
      // business mission
      if (joinReqProcess.getJoinWorkreason().equals("BM")) {
        EHCMBusMissionSummary missionsummary = OBDal.getInstance().get(EHCMBusMissionSummary.class,
            employmentId);
        missionsummary.setEndDate(joinReqProcess.getJoindate());
        OBDal.getInstance().save(missionsummary);
        OBDal.getInstance().flush();
      }
      // Hiring
      if (!joinReqProcess.getJoinWorkreason().equals("SCTR")
          && !joinReqProcess.getJoinWorkreason().equals("BM") && !transferisjoinWorkReq
          && !promotionJoinReqFlag && !susjoinFlag
          && !joinReqProcess.getJoinWorkreason().equals("SEC")) {
        // updating joining date in employment tab
        EmploymentInfo info = OBDal.getInstance().get(EmploymentInfo.class, employmentId);

        info.setStartDate(joinReqProcess.getJoindate());
        info.setJoinworkreq(true);
        info.setEhcmJoinWorkrequest(joinReqProcess);
        OBDal.getInstance().save(info);
        OBDal.getInstance().flush();

        // updating enddate for previous record
        OBQuery<EmploymentInfo> empoldrecord = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " id not in ('" + employmentId
                + "') and ehcmEmpPerinfo.id = :employeeId and creationDate < '"
                + info.getCreationDate() + "'  order by creationDate desc ");
        empoldrecord.setNamedParameter("employeeId", joinReqProcess.getEmployee().getId());
        empInfoList = empoldrecord.list();
        if (empoldrecord.list().size() > 0) {
          for (EmploymentInfo empinfo : empoldrecord.list()) {
            if (!info.getChangereason().equals("SEC") && !info.getChangereason().equals("PR")
                || (info.getChangereason().equals("SEC") && !empinfo.getChangereason().equals("H")
                    && !empinfo.getChangereason().equals("PR")
                    && !empinfo.getChangereason().equals("TR"))
                || (info.getChangereason().equals("PR")
                    && !empinfo.getChangereason().equals("SEC"))) {
              Date dateBefore = new Date(info.getStartDate().getTime() - millSec);
              empinfo.setEndDate(dateBefore);
              OBDal.getInstance().save(empinfo);
              OBDal.getInstance().flush();
            }
          }
        }
      }
      // secondment -mail
      if (joinReqProcess.getJoinWorkreason().equals("SEC")) {
        // updating joining date in employment tab
        EmploymentInfo secInfo = OBDal.getInstance().get(EmploymentInfo.class, employmentId);

        // insert Secondment delay record in employment Info
        if (joinReqProcess.getJoindate().compareTo(joinReqProcess.getDecisionDate()) != 0) {
          empSecondmentDAOImpl.insertEmploymentRecord(secInfo.getEhcmEmpSecondment(), secInfo, true,
              true, joinReqProcess);
        }

        // insert JWR-Secondment record in employment Info
        empSecondmentDAOImpl.insertEmploymentRecord(secInfo.getEhcmEmpSecondment(), secInfo, true,
            false, joinReqProcess);

        secInfo.setAlertStatus(Constants.EMPSTATUS_INACTIVE);
        OBDal.getInstance().save(secInfo);
      }

      // IsJoinRequest as 'Yes' in employee transfer
      if (transferisjoinWorkReq) {
        if (empTransfer.getEndDate() != null
            && empTransfer.getEndDate().compareTo(joinReqProcess.getJoindate()) <= -1) {
          errorFlag = true;
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_EmpJoinDateGrtThanTransEndDate"));
          bundle.setResult(obError);
          return;
        }
        if (!errorFlag) {
          // get Active Employemnt Info
          oldempInfo = sa.elm.ob.hcm.util.Utility
              .getActiveEmployInfo(empTransfer.getEhcmEmpPerinfo().getId());
          // insert Employement For Employee Transfer
          EmploymentInfo infoobj = EmpTransferIssueDecisionDAO.insertEmploymentInfo(empTransfer,
              oldempInfo, vars, "CR", lang, joinReqProcess.getJoindate(), joinReqProcess);

        }
      }
      // IsJoinRequest as 'Yes' in employee promotion
      if (promotionJoinReqFlag) {
        // get Active Employement Info
        oldempInfo = sa.elm.ob.hcm.util.Utility
            .getPromotionEmployee(empPromotion.getEhcmEmpPerinfo().getId());
        // insert Employement For Employee Promotion
        EmploymentInfo infoobj = EmployeePromotionHandlerDAO.insertEmploymentInfo(empPromotion,
            oldempInfo, vars, "CR", lang, joinReqProcess.getJoindate(), joinReqProcess, false);
      }
      // IsJoinRequest as 'Yes' in employee suspension
      if (susjoinFlag) {
        // insert Employement For Employee suspension
        EmpSuspensionHandlerDAO.preSuspensionRecordInactive(empSuspension, empSuspension.getId());
        EhcmEmpPerInfo perObj = EmpSuspensionHandlerDAO
            .InsertRecordUsingSuspensionEnd(empSuspension, oldSuspension, joinReqProcess);
      }

      // success message
      obError.setType("Success");
      obError.setTitle("Success");
      obError.setMessage(OBMessageUtils.messageBD("Ehcm_ExtraStep_Process"));
      bundle.setResult(obError);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();

    } catch (Exception e) {
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      Throwable t = DbUtility.getUnderlyingSQLException(e);
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
