package sa.elm.ob.hcm.ad_process.AbsenceDecision;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
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

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;
import sa.elm.ob.hcm.EHCMAbsenceTypeAction;
import sa.elm.ob.hcm.EHCMAbsenceTypeRules;
import sa.elm.ob.hcm.EHCMEmpLeave;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.util.Utility;

public class AbsenceIssueDecision implements Process {
  private static final Logger log = Logger.getLogger(AbsenceIssueDecision.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Issue the Absence Decision");
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    final String absenceId = (String) bundle.getParams().get("Ehcm_Absence_Attendance_ID")
        .toString();

    EHCMAbsenceAttendance absence = OBDal.getInstance().get(EHCMAbsenceAttendance.class, absenceId);
    log.debug("absenceId12:" + absenceId);
    EHCMEmpLeave leave = null, deductedleave = null;
    EHCMAbsenceType absencetype = null;
    EHCMAbsenceTypeAccruals accrual = null;
    EHCMAbsenceTypeAccruals definedAccrual = null;

    String chkapp = "";
    String chkappmessage = "";
    String message = "";
    String absencetypeAccral = null;

    Boolean empGradeClassDef = false;
    Boolean isAccrual = absence.getEhcmAbsenceType().isAccrual();

    Connection con = OBDal.getInstance().getConnection();

    Date absStartDate = null;
    Date absEndDate = null;

    List<EHCMAbsenceTypeAccruals> accrualList = new ArrayList<EHCMAbsenceTypeAccruals>();
    List<EHCMAbsenceTypeAccruals> accrualAllList = new ArrayList<EHCMAbsenceTypeAccruals>();
    List<EHCMAbsenceTypeAction> absenceActionList = new ArrayList<EHCMAbsenceTypeAction>();
    List<EHCMAbsenceTypeRules> absenceRuleList = new ArrayList<EHCMAbsenceTypeRules>();

    AbsenceIssueDecisionDAO absenceIssueDecisionDAO = new AbsenceIssueDecisionDAOImpl();
    Boolean checkExtendAbsTypeLevIsTakenBefIssue = false;
    try {
      OBContext.setAdminMode(true);
      // check whether the employee is suspended or not
      if (absence.getEhcmEmpPerinfo().getEmploymentStatus()
          .equals(DecisionTypeConstants.EMPLOYMENTSTATUS_SUSPENDED)) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_emplo_suspend"));
        bundle.setResult(obError);
        return;
      }

      // check current original decision no is issued or not
      if (absence.getOriginalDecisionNo() != null) {
        if (!absence.getOriginalDecisionNo().isSueDecision()) {

          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_AbsenceOrigianlDecNoUP"));
          bundle.setResult(obError);
          return;
        }
      }
      // check extend absence type validation

      checkExtendAbsTypeLevIsTakenBefIssue = absenceIssueDecisionDAO
          .checkExtendAbsenceTypeLeaveIsTakenBeforeIssueDecision(absence);
      if (checkExtendAbsTypeLevIsTakenBefIssue) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_AbsDelLev_ExtMatEndDate"));
        bundle.setResult(obError);
        return;
      }

      // checking decision overlap
      if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
          || absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
          || absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
        if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
          absStartDate = absence.getExtendStartdate();
          absEndDate = absence.getExtendEnddate();
        } else {
          absStartDate = absence.getStartDate();
          absEndDate = absence.getEndDate();
        }
        JSONObject decresult = Utility.chkDecisionOverlap(Constants.ABSENCE_OVERLAP,
            sa.elm.ob.utility.util.Utility.formatDate(absStartDate),
            sa.elm.ob.utility.util.Utility.formatDate(absEndDate),
            absence.getEhcmEmpPerinfo().getId(), absence.getEhcmAbsenceType().getId(),
            absence.getId());
        log.debug("decresult1:" + decresult);
        if (decresult != null && decresult.has("errorFlag") && decresult.getBoolean("errorFlag")) {
          if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
              || absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
              || (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                  && (decresult.has("absenceDecisionId")
                      && !decresult.getString("absenceDecisionId")
                          .equals(absence.getOriginalDecisionNo().getId()))
                  || !decresult.has("absenceDecisionId"))) {
            if (decresult.has("errormsg")) {
              obError.setType("Error");
              obError.setTitle("Error");
              obError.setMessage(OBMessageUtils.messageBD(decresult.getString("errormsg")));
              bundle.setResult(obError);
              return;
            }
          }
        }
      }

      log.debug("accrua:" + isAccrual);
      // chk if absence type is accrual then chk accrual define for that particular emp grade
      // category or null( accept for all employee) grade assigned
      if (isAccrual != null && isAccrual) {
        accrualAllList = absence.getEhcmAbsenceType().getEHCMAbsenceTypeAccrualsList();
        log.debug("getEHCMAbsenceTypeAccrualsList:" + accrualAllList);
        if (accrualAllList.size() == 0) {
          OBError result = OBErrorBuilder.buildMessage(null, "error",
              "@EHCM_AbsTypeAccrual_NotDefine@");
          bundle.setResult(result);
          return;
        } else {
          accrualList = absenceIssueDecisionDAO.getAbsenceAccrual(absence, empGradeClassDef);
          if (accrualList.size() == 0) {
            OBError result = OBErrorBuilder.buildMessage(null, "error",
                "@EHCM_AbsDec_AccrualNotDefPartPeriod@");
            bundle.setResult(result);
            return;
          } else {
            for (EHCMAbsenceTypeAccruals defAccrual : accrualList) {
              if (defAccrual.getGradeClassifications() != null && absence.getGradeClassifications()
                  .getId().equals(defAccrual.getGradeClassifications().getId())) {
                definedAccrual = defAccrual;
                break;
              } else {
                definedAccrual = defAccrual;
              }
            }
            absencetypeAccral = definedAccrual.getId();
          }
        }
      }
      /* get Absence type */
      absenceActionList = absence.getEhcmAbsenceType().getEHCMAbsenceTypeActionList();
      if (absenceActionList.size() > 0) {
        for (EHCMAbsenceTypeAction action : absenceActionList) {
          absencetype = action.getDependent();
        }
      } else {
        absencetype = absence.getEhcmAbsenceType();
      }

      // date changes

      // validation to chk leave balance available or not
      if (!absence.getDecisionType().equals("CA") && !absence.getDecisionType().equals("CO")) {
        chkapp = absenceIssueDecisionDAO.chkleaveapprove(absence);
        chkappmessage = OBMessageUtils.messageBD(chkapp);
        log.debug("chkappmessages:" + chkappmessage);
        if (!chkappmessage.equals("Success")) {
          // leave less than 5 or 7 days then throw the error msg with corresponding input in
          // absence Type Rules
          if (chkapp.equals("EHCM_LLTF")) {
            OBQuery<EHCMAbsenceTypeRules> ruleQry = OBDal.getInstance().createQuery(
                EHCMAbsenceTypeRules.class,
                " as e where e.code=:code and e.absenceType.id=:absenceTypeId ");
            ruleQry.setNamedParameter("code", "LLTF");
            ruleQry.setNamedParameter("absenceTypeId", absencetype.getId());
            ruleQry.setMaxResult(1);
            absenceRuleList = ruleQry.list();
            if (absenceRuleList.size() > 0) {
              EHCMAbsenceTypeRules absrule = absenceRuleList.get(0);
              log.debug("getCondition:" + absrule.getCondition().split("<=")[1].toString());
              String input = absrule.getCondition().split("<=")[1].toString();
              log.debug("input:" + input);
              chkapp = OBMessageUtils.messageBD(chkapp);
              chkapp = chkapp.replace("%", input);
            }
          } else if (chkapp.contains("EHCM_LevNotAvailable")) {
            String output = chkapp.split("-")[1];
            log.debug("output:" + output);
            chkapp = OBMessageUtils.messageBD(chkapp.split("-")[0]);
            chkapp = chkapp.replace("%", output);
            log.debug("chkapp:" + chkapp);
          } else {
            String output = chkapp.split("_")[1];
            log.debug("output:" + output);
            chkapp = OBMessageUtils.messageBD("EHCM_AbsDecisionLevApp_Error");
            chkapp = chkapp.replace("%", output);
            log.debug("chkapp:" + chkapp);

            OBDal.getInstance().rollbackAndClose();
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD(chkapp));
            bundle.setResult(obError);
            return;
          }
          OBDal.getInstance().rollbackAndClose();
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD(chkapp));
          bundle.setResult(obError);
          return;
        }
      }

      if (StringUtils.isNotEmpty(absencetypeAccral))
        accrual = OBDal.getInstance().get(EHCMAbsenceTypeAccruals.class, absencetypeAccral);
      if (absence.getDecisionType().equals("CR")) {
        if (absence.getEhcmAbsenceType().getAccrualResetDate().equals("LO")) {
          leave = absenceIssueDecisionDAO.insertLeaveOccuranceEmpLeave(absence, absencetype,
              accrual, absence.getStartDate(), absence.getEndDate());
        } else {
          if (!absence.getEhcmAbsenceType().isDeducted()) {
            leave = absenceIssueDecisionDAO.insertLeaveOccuranceEmpLeave(absence, absencetype,
                accrual, absence.getStartDate(), absence.getEndDate());
          }
        }
      }
      if (!absence.isSueDecision()) {

        // Create or Extend Case
        if (absence.getDecisionType().equals("CR")) {
          log.debug("isDeducted::" + absence.getEhcmAbsenceType().isDeducted());

          // Need to create a block in empleaveblock table while taking exception leaave (Leave
          // occurance)
          if (absence.getEhcmAbsenceType().getAccrualResetDate().equals("LO")) {
            message = absenceIssueDecisionDAO.chkexceptionleaveval(con, absence, absencetypeAccral);
            if (!message.equals("Success")) {
              OBDal.getInstance().rollbackAndClose();
              OBError result = OBErrorBuilder.buildMessage(null, "error", message);
              bundle.setResult(result);
              return;
            }
          }
          // check absence leave type is deducted leave or not
          if (absence.getEhcmAbsenceType().isDeducted()) {
            absenceIssueDecisionDAO.deductedLeave(absencetype, con, absence, leave, accrual, null);
          }
          if (!absence.getEhcmAbsenceType().isDeducted()) {
            /* insert a new record in emp leave type table */
            absenceIssueDecisionDAO.insertEmpLeaveLine(leave, absencetype, absence,
                absence.getAbsenceDays(), absence.getStartDate(), absence.getEndDate());
          }

          absenceIssueDecisionDAO.updateAbsenceDecision(absence);

        } else if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
            || absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)
            || absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
          if (absence.getOriginalDecisionNo() != null) {

            if (absence.getEhcmAbsenceType().getAccrualResetDate().equals("LO")) {
              // update the emp leave block
              message = absenceIssueDecisionDAO.chkexceptionleaveval(con, absence,
                  absencetypeAccral);
              if (!message.equals("Success")) {
                OBDal.getInstance().rollbackAndClose();
                OBError result = OBErrorBuilder.buildMessage(null, "error", message);
                bundle.setResult(result);
                return;
              }
            }

            log.debug("deducted:" + absence.getEhcmAbsenceType().isDeducted());
            if (absence.getEhcmAbsenceType().isDeducted()) {
              absenceIssueDecisionDAO.deductedLeave(absencetype, con, absence, leave, accrual,
                  null);
            } else {
              absenceIssueDecisionDAO.updateEmpLeave(absence);
              absenceIssueDecisionDAO.insertLeaveOccuranceEmpLeave(absence, absencetype, accrual,
                  absence.getStartDate(), absence.getEndDate());
              absenceIssueDecisionDAO.insertEmpLeaveLine(null, absence.getEhcmAbsenceType(),
                  absence, absence.getAbsenceDays(), absence.getStartDate(), absence.getEndDate());
            }

            // update old absence as inactive
            absenceIssueDecisionDAO.updateAbsenceEnableFlag(absence.getOriginalDecisionNo(), false);
            absenceIssueDecisionDAO.updateAbsenceDecision(absence);

          }

        } else if (absence.getDecisionType().equals("CA")) {

          absenceIssueDecisionDAO.cancelEmpLeave(absence, absencetype, con, accrual, leave);
          // update old absence as inactive
          absenceIssueDecisionDAO.updateAbsenceEnableFlag(absence.getOriginalDecisionNo(), false);

          if (absence.getOriginalDecisionNo().getOriginalDecisionNo() != null) {
            absenceIssueDecisionDAO.updateAbsenceEnableFlag(
                absence.getOriginalDecisionNo().getOriginalDecisionNo(), true);
          }

          absence.setEnabled(false);
          OBDal.getInstance().save(absence);
          OBDal.getInstance().flush();
          absenceIssueDecisionDAO.updateAbsenceDecision(absence);

          if (absence.getEhcmAbsenceType().getAccrualResetDate().equals("LO")) {
            // delete the block
            // update the emp leave block
            message = absenceIssueDecisionDAO.chkexceptionleaveval(con, absence, absencetypeAccral);
          }
        }
        OBDal.getInstance().flush();
        // update the related & depenedent absence days
        if (!absence.getDecisionType().equals("CA")) {
          absenceIssueDecisionDAO.updateDependentRelatedAbsDays(absence);
        }
        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("Efin_BudgetPre_Submit"));
        bundle.setResult(obError);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
      }
    }

    catch (Exception e) {
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
}
