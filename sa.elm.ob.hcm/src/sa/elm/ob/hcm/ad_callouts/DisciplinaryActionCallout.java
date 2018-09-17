package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.text.DateFormat;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EhcmDisciplineAction;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_callouts.common.UpdateEmpDetailsInCallouts;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * @author poongodi on 04/05/2018
 */
public class DisciplinaryActionCallout extends SimpleCallout {

  private static final long serialVersionUID = 1L;
  private static final String DecisionType = "CR";

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String inpactionTaken = vars.getStringParameter("inpactionTaken");
    String inporiginalDecisionNo = vars.getStringParameter("inporiginalDecisionNo");
    String employmentInfoId = "";
    DateFormat dateFormat = Utility.YearFormat;
    String clientId = info.vars.getStringParameter("inpadClientId");
    EhcmDisciplineAction objEmpQuery = null;
    EhcmDisciplineAction objOrgDec = null;
    Connection con = OBDal.getInstance().getConnection();

    log4j.debug("lastfieldChanged:" + lastfieldChanged);
    try {
      UpdateEmpDetailsInCallouts callouts = new UpdateEmpDetailsInCallouts();
      EmploymentInfo empinfo = sa.elm.ob.hcm.util.UtilityDAO.getActiveEmployInfo(employeeId);
      // get Employee Details by using employeeId
      if (lastfieldChanged.equals("inpehcmEmpPerinfoId")) {
        if (StringUtils.isNotEmpty(employeeId)) {
          EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          info.addResult("inpempName", employee.getArabicfullname());
          info.addResult("inpehcmEmpPerinfoId", employeeId);

          EHCMEmployeeStatusV employeeStatus = OBDal.getInstance().get(EHCMEmployeeStatusV.class,
              employeeId);
          if (employeeStatus != null)
            info.addResult("inpempStatus", employeeStatus.getStatusvalue());
          else
            info.addResult("inpempStatus", "");

          info.addResult("inpehcmGradeclassId", employee.getGradeClass().getId());
          info.addResult("inpempType", employee.getEhcmActiontype().getPersonType());

          if (employee.getHiredate() != null) {
            info.addResult("inphireDate",
                (UtilityDAO.convertTohijriDate(dateFormat.format(employee.getHiredate()))));

          }
          if (empinfo != null) {
            employmentInfoId = empinfo.getId();
            info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
            if (empinfo.getPosition().getSection() != null) {
              info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
            } else {
              info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
            }
            info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
            info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
            info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
            info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
            info.addResult("inpassignedDept", empinfo.getSECDeptName());

          }
          if (empinfo.getStartDate() != null) {
            info.addResult("inpstartdate",
                (UtilityDAO.convertTohijriDate(dateFormat.format(empinfo.getStartDate()))));
          }

          inpdecisionType = DecisionType;
          info.addResult("inpdecisionType", inpdecisionType);
          info.addResult("inporiginalDecisionNo", "");
        } else {
          callouts.SetEmpDetailsNull(info);
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Assigned_Dept').setValue('')");
        }
      }
      // Decision type is update and cancel then set the previous originaldecisionno\

      if (lastfieldChanged.equals("inpdecisionType")
          || (lastfieldChanged.equals("inporiginalDecisionNo"))
              && (!inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE))) {
        EhcmDisciplineAction displineAction = OBDal.getInstance().get(EhcmDisciplineAction.class,
            inporiginalDecisionNo);
        if (displineAction != null) {
          info.addResult("inpnoofdays", displineAction.getNoOfDays());
          // // Load the decision details based on choosen employee original dec no
          if (displineAction.getLetterNo() != null)
            info.addResult("inpletterNo", displineAction.getLetterNo());
          else
            info.addResult("inpletterNo", null);
          if (displineAction.getLetterDate() != null)
            info.addResult("inpletterDate",
                UtilityDAO.convertTohijriDate(dateFormat.format(displineAction.getLetterDate())));
          else
            info.addResult("inpletterDate", null);
          if (displineAction.getDecisionNo() != null)
            info.addResult("inpdecisionNo", displineAction.getDecisionNo());
          else
            info.addResult("inpdecisionNo", null);
          if (displineAction.getDecisionDate() != null)
            info.addResult("inpdecisionDate",
                UtilityDAO.convertTohijriDate(dateFormat.format(displineAction.getDecisionDate())));
          else
            info.addResult("inpdecisionDate", null);
          if (displineAction.getDisciplineReason() != null)
            info.addResult("inpdisciplineReason", displineAction.getDisciplineReason().getId());
          else
            info.addResult("inpdisciplineReason", null);
          if (displineAction.getCommitee() != null)
            info.addResult("inpcommitee", displineAction.getCommitee());
          else
            info.addResult("inpcommitee", null);
          if (displineAction.getActionTaken() != null)
            info.addResult("inpactionTaken", displineAction.getActionTaken());
          else
            info.addResult("inpactionTaken", null);
          if (displineAction.getNoOfDays() != null)
            info.addResult("inpnoofdays", displineAction.getNoOfDays());
          else
            info.addResult("inpnoofdays", null);
          if (displineAction.getAmount() != null)
            info.addResult("inpamount", displineAction.getAmount().toString());
          else
            info.addResult("inpamount", null);
          if (displineAction.getStartDate() != null)
            info.addResult("inpstartdate",
                UtilityDAO.convertTohijriDate(dateFormat.format(displineAction.getStartDate())));
          else
            info.addResult("inpstartdate", null);
          if (displineAction.getEndDate() != null)
            info.addResult("inpenddate",
                UtilityDAO.convertTohijriDate(dateFormat.format(displineAction.getEndDate())));
          else
            info.addResult("inpenddate", null);
          if (displineAction.getEffectiveDate() != null)
            info.addResult("inpeffectiveDate", UtilityDAO
                .convertTohijriDate(dateFormat.format(displineAction.getEffectiveDate())));
          else
            info.addResult("inpeffectiveDate", null);

        } else {
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('original_decision_no').setValue('')");

        }
        // info.addResult("inpdecisionType", inpdecisionType);

      } else {
        if (StringUtils.isNotEmpty(employeeId)) {
          info.addResult("inpamount", null);
          info.addResult("inpstartdate",
              UtilityDAO.convertTohijriDate(dateFormat.format(empinfo.getStartDate())));
          info.addResult("inpenddate", null);
        } else {
          info.addResult("inpamount", null);
          info.addResult("inpstartdate", null);
          info.addResult("inpenddate", null);
        }

      }
      if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        info.addResult("inpletterNo", null);
        info.addResult("inpletterDate", null);
        info.addResult("inpdecisionNo", null);
        info.addResult("inpdecisionDate", null);
        // info.addResult("inpdisciplineReason", null);
        info.addResult("inpcommitee", null);
        // info.addResult("inpactionTaken", null);
        info.addResult("inpnoofdays", null);
        info.addResult("inpamount", null);
        // info.addResult("inpstartdate", null);
        info.addResult("inpenddate", null);
        // info.addResult("inpeffectiveDate", null);
        info.addResult("inpamount", null);
      }

      // Changing the action Taken
      if (lastfieldChanged.equals("inpactionTaken")) {
        info.addResult("inpnoofdays", null);
        info.addResult("inpamount", null);

      }

    } catch (Exception e) {
      log4j.error("Exception in DisciplinaryActionCallout ", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

}
