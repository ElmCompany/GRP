package sa.elm.ob.hcm.ad_callouts;

import java.text.DateFormat;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMBenefitAllowance;
import sa.elm.ob.hcm.EHCMElmttypeDef;
import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_callouts.common.UpdateEmpDetailsInCallouts;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

@SuppressWarnings("serial")
public class BenefitsAndAllowanceCallout extends SimpleCallout {

  @SuppressWarnings("unchecked")
  protected void execute(CalloutInfo info) {
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String elementId = vars.getStringParameter("inpehcmElmttypeDefId");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inporiginalDecisionNo = vars.getStringParameter("inporiginalDecisionNo");
    String inpvaluetype = vars.getStringParameter("inpvaluetype");
    DateFormat dateFormat = sa.elm.ob.utility.util.Utility.YearFormat;

    log4j.debug("lastfiled:" + lastfieldChanged);

    try {
      EmploymentInfo empinfo = null;
      empinfo = Utility.getActiveEmployInfo(employeeId);
      UpdateEmpDetailsInCallouts callouts = new UpdateEmpDetailsInCallouts();

      if (lastfieldChanged.equals("inpehcmEmpPerinfoId")) {
        if (StringUtils.isNotEmpty(employeeId)) {
          EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          info.addResult("inpempName", employee.getArabicfullname());
          log4j.debug("employee B&A :" + employee);

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
            info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
            if (empinfo.getPosition() != null && empinfo.getPosition().getSection() != null) {
              info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
            }
            info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
            info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
            info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
            info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
            info.addResult("inpassignedDept", empinfo.getSECDeptName());
            info.addResult("inpehcmGradestepsId",
                empinfo.getEhcmPayscale().getEhcmGradesteps().getId());
            info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());
            log4j.debug("inpehcmPayscalelineId:" + empinfo.getEhcmPayscaleline().getId());
            if (empinfo.getStartDate() != null) {
              info.addResult("inpstartdate",
                  (UtilityDAO.convertTohijriDate(dateFormat.format(empinfo.getStartDate()))));
            }
            info.addResult("inpdecisionType", DecisionTypeConstants.DECISION_TYPE_CREATE);
            info.addResult("inporiginalDecisionsNo", "");
          }
        } else {
          callouts.SetEmpDetailsNull(info);
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Ehcm_Payscaleline_ID').setValue('')");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Assigned_Dept').setValue('')");
        }
      }

      if (lastfieldChanged.equals("inpehcmElmttypeDefId")
          || lastfieldChanged.equals("inpdecisionType")
          || (lastfieldChanged.equals("inporiginalDecisionNo"))) {
        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
            || inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
            || inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_HOLD)) {

          EHCMBenefitAllowance allow = OBDal.getInstance().get(EHCMBenefitAllowance.class,
              inporiginalDecisionNo);
          if (allow != null) {
            if (allow.getLetterNo() != null)
              info.addResult("inpletterNo", allow.getLetterNo());
            else
              info.addResult("inpletterNo", null);
            if (allow.getLetterDate() != null)
              info.addResult("inpletterDate",
                  UtilityDAO.convertTohijriDate(dateFormat.format(allow.getLetterDate())));
            else
              info.addResult("inpletterDate", null);
            if (allow.getDecisionNo() != null)
              info.addResult("inpdecisionNo", allow.getDecisionNo());
            else
              info.addResult("inpdecisionNo", null);
            if (allow.getDecisionDate() != null)
              info.addResult("inpdecisionDate",
                  UtilityDAO.convertTohijriDate(dateFormat.format(allow.getDecisionDate())));
            else
              info.addResult("inpdecisionDate", null);
            if (allow.getStartDate() != null)
              info.addResult("inpstartdate",
                  UtilityDAO.convertTohijriDate(dateFormat.format(allow.getStartDate())));
            if (allow.getEndDate() != null)
              info.addResult("inpenddate",
                  UtilityDAO.convertTohijriDate(dateFormat.format(allow.getEndDate())));
            else
              info.addResult("inpenddate", null);

          } else {
            info.addResult("inpletterNo", null);
            info.addResult("inpletterDate", null);
            info.addResult("inpdecisionNo", null);
            info.addResult("inpdecisionDate", null);
            info.addResult("inpenddate", null);
          }

          // if (benefitAllow.size() > 0) {
          // EHCMBenefitAllowance allowance = benefitAllow.get(0);
          // info.addResult("inporiginalDecisionNo", allowance.getId());
          // } else {
          // info.addResult("inporiginalDecisionNo", null);
          // }
        } else if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          info.addResult("inpletterNo", null);
          info.addResult("inpletterDate", null);
          info.addResult("inpdecisionNo", null);
          info.addResult("inpdecisionDate", null);
          info.addResult("inpenddate", null);
        }

        // Get Base Process of Element Type
        EHCMElmttypeDef elmType = OBDal.getInstance().get(EHCMElmttypeDef.class, elementId);

        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          if (elmType != null && !elmType.getBaseProcess().equalsIgnoreCase("AD")) {
            info.addResult("JSEXECUTE", "form.getFieldFromColumnName('valuetype').setValue(null)");
            info.addResult("JSEXECUTE", "form.getFieldFromColumnName('percentage').setValue(null)");
            info.addResult("JSEXECUTE",
                "form.getFieldFromColumnName('percentcategory').setValue(null)");
            info.addResult("JSEXECUTE",
                "form.getFieldFromColumnName('fixedamount').setValue(null)");
          }
        }

        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('canceldate').setValue(null)");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('hold_date').setValue(null)");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('hold_duration').setValue(null)");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('hold_duration_type').setValue(null)");
          if (elmType != null && !elmType.getBaseProcess().equalsIgnoreCase("AD")) {
            info.addResult("JSEXECUTE", "form.getFieldFromColumnName('valuetype').setValue(null)");
            info.addResult("JSEXECUTE", "form.getFieldFromColumnName('percentage').setValue(null)");
            info.addResult("JSEXECUTE",
                "form.getFieldFromColumnName('percentcategory').setValue(null)");
            info.addResult("JSEXECUTE",
                "form.getFieldFromColumnName('fixedamount').setValue(null)");
          }
        }

        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('percentage').setValue(null)");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('percentcategory').setValue(null)");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('fixedamount').setValue(null)");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('hold_date').setValue(null)");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('hold_duration').setValue(null)");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('hold_duration_type').setValue(null)");
        }

        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_HOLD)) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('canceldate').setValue(null)");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('percentage').setValue(null)");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('percentcategory').setValue(null)");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('fixedamount').setValue(null)");
        }

      } else {
        if (StringUtils.isNotEmpty(employeeId)) {
          info.addResult("inpstartdate",
              UtilityDAO.convertTohijriDate(dateFormat.format(empinfo.getStartDate())));
          info.addResult("inpenddate", null);
        } else {
          info.addResult("inpstartdate", null);
          info.addResult("inpenddate", null);
        }
      }

      if (lastfieldChanged.equals("inpvaluetype")) {
        if (inpvaluetype.equalsIgnoreCase("FA")) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('percentage').setValue(null)");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('percentcategory').setValue(null)");
        } else if (inpvaluetype.equalsIgnoreCase("P")) {
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('fixedamount').setValue(null)");
        }
      }

    } catch (

    Exception e) {
      log4j.error("Exception in BenefitsAndAllowanceCallout Callout", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
