package sa.elm.ob.hcm.ad_callouts;

import java.text.DateFormat;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EHCMLoanTransaction;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_callouts.common.UpdateEmpDetailsInCallouts;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class LoanTransactionCallout extends SimpleCallout {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected void execute(CalloutInfo info) {
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inploanType = vars.getStringParameter("inploanType");
    String inporiginalDecisionNo = vars.getStringParameter("inporiginalDecisionNo");
    DateFormat dateFormat = sa.elm.ob.utility.util.Utility.YearFormat;
    log4j.debug("lastfiled:" + lastfieldChanged);

    try {
      UpdateEmpDetailsInCallouts callouts = new UpdateEmpDetailsInCallouts();
      EmploymentInfo empinfo = null;
      empinfo = Utility.getActiveEmployInfo(employeeId);

      if (lastfieldChanged.equals("inpehcmEmpPerinfoId")) {
        if (StringUtils.isNotEmpty(employeeId)) {
          EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          info.addResult("inpempName", employee.getArabicfullname());

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
            } else {
              info.addResult("inpsectionId", null);
            }
            info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
            info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
            info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
            info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
            info.addResult("inpassignedDept", empinfo.getSECDeptName());
            info.addResult("inpehcmGradestepsId",
                empinfo.getEhcmPayscale().getEhcmGradesteps().getId());
            info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());
            info.addResult("inporiginalDecisionsNo", "");
          }
        } else {
          callouts.SetEmpDetailsNull(info);
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Assigned_Dept').setValue('')");
        }
      }

      if (lastfieldChanged.equals("inploanRequest") || lastfieldChanged.equals("inploanType")
          || (lastfieldChanged.equals("inporiginalDecisionNo"))) {
        if (inploanType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
            || inploanType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
            || inploanType.equals(DecisionTypeConstants.DECISION_TYPE_HOLD)) {
          EHCMLoanTransaction loan = OBDal.getInstance().get(EHCMLoanTransaction.class,
              inporiginalDecisionNo);
          if (loan != null) {
            if (loan.getLetterNo() != null)
              info.addResult("inpletterNo", loan.getLetterNo());
            else
              info.addResult("inpletterNo", null);
            if (loan.getLetterDate() != null)
              info.addResult("inpletterDate",
                  UtilityDAO.convertTohijriDate(dateFormat.format(loan.getLetterDate())));
            else
              info.addResult("inpletterDate", null);
            // if (loan.getDecisionNo() != null)
            // info.addResult("inpdecisionNo", loan.getDecisionNo());
            // else
            // info.addResult("inpdecisionNo", null);
            if (loan.getDecisionDate() != null)
              info.addResult("inpdecisionDate",
                  UtilityDAO.convertTohijriDate(dateFormat.format(loan.getDecisionDate())));
            else
              info.addResult("inpdecisionDate", null);

            if (loan.getLoanRequest() != null)
              info.addResult("inploanRequest", loan.getLoanRequest().getId());
            if (loan.getContractNumber() != null)
              info.addResult("inpcontractNumber", loan.getContractNumber());
            else
              info.addResult("inpcontractNumber", null);

            if (loan.getLoanDate() != null)
              info.addResult("inploanDate",
                  UtilityDAO.convertTohijriDate(dateFormat.format(loan.getLoanDate())));
            else
              info.addResult("inploanDate", null);
            if (loan.getLoanInitialBal() != null)
              info.addResult("inploanInitialBal", loan.getLoanInitialBal());
            else
              info.addResult("inploanInitialBal", null);
            if (loan.getLoanOriginalAmount() != null)
              info.addResult("inploanOriginalAmount", loan.getLoanOriginalAmount());
            else
              info.addResult("inploanOriginalAmount", null);
            if (loan.getInstallmentStartDate() != null)
              info.addResult("inpinstallmentStartDate",
                  UtilityDAO.convertTohijriDate(dateFormat.format(loan.getInstallmentStartDate())));
            if (loan.getInstallmentAmount() != null)
              info.addResult("inpinstallmentAmount", loan.getInstallmentAmount());
            else
              info.addResult("inpinstallmentAmount", null);
            if (loan.getFirstInstallmentPeriod() != null)
              info.addResult("inpfirstInstallmentPeriod", loan.getFirstInstallmentPeriod().getId());
            // if (loan.getHoldDate() != null)
            // info.addResult("inpholdDate", loan.getHoldDate());
            // else
            // info.addResult("inpholdDate", null);
            // if (loan.getHoldDuration() != null)
            // info.addResult("inpholdDuration", loan.getHoldDuration());
            // else
            // info.addResult("inpholdDuration", null);
            if (loan.getHoldEndPeriod() != null)
              info.addResult("inpholdEndPeriod", loan.getHoldEndPeriod().getId());
            else
              info.addResult("inpholdEndPeriod", null);
          }
        }

      }

      if (inploanType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        info.addResult("inpletterNo", null);
        info.addResult("inpletterDate", null);
        info.addResult("inpdecisionNo", null);
        info.addResult("inpdecisionDate", null);
        info.addResult("inpcontractNumber", null);
        info.addResult("inploanOriginalAmount", 0);
        info.addResult("inpinstallmentAmount", 0);
        info.addResult("inploanInitialBal", 0);
      }

    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in LoanTransactionCallout Callout", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

}
