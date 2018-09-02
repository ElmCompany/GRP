package sa.elm.ob.hcm.ad_process.AbsenceDecision;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;
import sa.elm.ob.hcm.EHCMAbsenceTypeAction;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.util.Utility;

/**
 * @author divya on 08/05/2018
 */
public class AbsenceDecisionReactivate extends DalBaseProcess {

  private static final Logger log = LoggerFactory.getLogger(AbsenceDecisionReactivate.class);
  private final OBError obError = new OBError();

  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub

    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);

    log.debug("entering into AbsenceDecisionReactivates");
    try {
      OBContext.setAdminMode();
      final String absenceId = (String) bundle.getParams().get("Ehcm_Absence_Attendance_ID")
          .toString();
      EHCMAbsenceAttendance absence = OBDal.getInstance().get(EHCMAbsenceAttendance.class,
          absenceId);

      String checkAppMsg = "";
      String message = "";
      Date absStartDate = null;
      Date absEndDate = null;
      EHCMAbsenceType absenceType = null;
      Connection con = OBDal.getInstance().getConnection();
      AbsenceDecisionReactivateDAO absenceDecisionReactivateDAO = new AbsenceDecisionReactivateDAOImpl();
      AbsenceIssueDecisionDAO absenceIssueDecisionDAO = new AbsenceIssueDecisionDAOImpl();

      List<EHCMAbsenceTypeAction> absenceActionList = new ArrayList<EHCMAbsenceTypeAction>();
      List<EHCMAbsenceTypeAccruals> accrualList = new ArrayList<EHCMAbsenceTypeAccruals>();

      EHCMAbsenceTypeAccruals definedAccrual = null;
      EHCMAbsenceAttendance originalAbsence = null;

      Boolean chkExtendAbsTypeLevIsTakenBefReact = false;

      /* get Absence type */
      absenceActionList = absence.getEhcmAbsenceType().getEHCMAbsenceTypeActionList();
      if (absenceActionList.size() > 0) {
        for (EHCMAbsenceTypeAction action : absenceActionList) {
          absenceType = action.getDependent();
        }
      } else {
        absenceType = absence.getEhcmAbsenceType();
      }

      /* get Absence Accruals */
      accrualList = absenceIssueDecisionDAO.getAbsenceAccrual(absence, false);
      for (EHCMAbsenceTypeAccruals defAccrual : accrualList) {
        if (defAccrual.getGradeClassifications() != null && absence.getGradeClassifications()
            .getId().equals(defAccrual.getGradeClassifications().getId())) {
          definedAccrual = defAccrual;
          break;
        } else {
          definedAccrual = defAccrual;
        }
      }

      // check absence type is used anywhere as extend absence type, then need to check extended
      // absence type leave applied

      if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {

        chkExtendAbsTypeLevIsTakenBefReact = absenceDecisionReactivateDAO
            .checkExtendAbsenceTypeLeaveIsTakenBeforeReactivate(absence);
        if (chkExtendAbsTypeLevIsTakenBefReact) {
          OBDal.getInstance().rollbackAndClose();
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_AbsMatLeav_React"));
          bundle.setResult(obError);
          return;
        }

      }

      // validation before reactivate while cancel/cutoff
      if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
          || ((absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)
              || absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE))
              && absence.getOriginalDecisionNo().getAbsenceDays()
                  .compareTo(absence.getAbsenceDays()) > 0)) {

        // check leave balance is sufficent or not while reactivate the cancel or cutoff record
        checkAppMsg = absenceDecisionReactivateDAO.chkleaveapprove(absence.getOriginalDecisionNo(),
            absence);
        message = absenceIssueDecisionDAO.getChkLeaveApproveMsg(checkAppMsg, absenceType);
        message = OBMessageUtils.messageBD(message);
        if (!message.equals("Success")) {
          OBDal.getInstance().rollbackAndClose();
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD(message));
          bundle.setResult(obError);
          return;
        }

        // decision overlap checking
        if (absence.getOriginalDecisionNo() != null) {
          originalAbsence = absence.getOriginalDecisionNo();
          if (originalAbsence.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
            absStartDate = originalAbsence.getExtendStartdate();
            absEndDate = originalAbsence.getExtendEnddate();
          } else {
            absStartDate = originalAbsence.getStartDate();
            absEndDate = originalAbsence.getEndDate();
          }
          JSONObject decisionOverlapresult = Utility.chkDecisionOverlap(Constants.ABSENCE_OVERLAP,
              sa.elm.ob.utility.util.Utility.formatDate(absStartDate),
              sa.elm.ob.utility.util.Utility.formatDate(absEndDate),
              originalAbsence.getEhcmEmpPerinfo().getId(),
              originalAbsence.getEhcmAbsenceType().getId(), originalAbsence.getId());
          log.debug("decresult1:" + decisionOverlapresult);

          if (decisionOverlapresult != null && decisionOverlapresult.has("errorFlag")
              && decisionOverlapresult.getBoolean("errorFlag")) {
            if (!originalAbsence.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
                && (decisionOverlapresult.has("absenceDecisionId") && !decisionOverlapresult
                    .getString("absenceDecisionId").equals(absence.getId()))) {
              if (decisionOverlapresult.has("errormsg")) {
                obError.setType("Error");
                obError.setTitle("Error");
                obError.setMessage(
                    OBMessageUtils.messageBD(decisionOverlapresult.getString("errormsg")));
                bundle.setResult(obError);
                return;
              }
            }
          }
        }

        if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
          originalAbsence = absence.getOriginalDecisionNo();
          // if decision overlap does not define then checking overlap manually
          if (originalAbsence != null && (originalAbsence.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
              || originalAbsence.getDecisionType()
                  .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE))) {
            JSONObject result = Utility.overlapWithDecisionsDate(Constants.ABSENCE_OVERLAP,
                sa.elm.ob.utility.util.Utility.formatDate(absStartDate),
                sa.elm.ob.utility.util.Utility.formatDate(absEndDate),
                originalAbsence.getEhcmEmpPerinfo().getId());
            if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
              if (!originalAbsence.getDecisionType()
                  .equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
                  && (result.has("absenceDecisionId")
                      && !result.getString("absenceDecisionId").equals(absence.getId()))) {
                if (result.has("errormsg"))
                  obError.setType("Error");
                obError.setTitle("Error");
                obError.setMessage(OBMessageUtils.messageBD(result.getString("errormsg")));
                bundle.setResult(obError);
                return;
              }
            }
          }
        }
      }

      // create case
      if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {

        // delete Emp Leave record , in case absence type is exceptional leave then delete emp leave
        // block also
        absenceDecisionReactivateDAO.deleteEmpLeaveRecordInCreateCase(absence);
      }
      // update,Extend,Cutoff,Cancel Case
      else {
        absenceDecisionReactivateDAO.updateEmpLeaveRecrdInOtherThanCreateCase(con, absence,
            absenceType, definedAccrual);
      }
      // update the status as under processing while reactivate
      absenceDecisionReactivateDAO.updateAbsenceDecisionStatus(absence);

      OBError result = OBErrorBuilder.buildMessage(null, "success", "@EHCM_AbsDecisionReactivate@");
      bundle.setResult(result);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
      return;

    }

    catch (Exception e) {
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();

    }
  }

}
