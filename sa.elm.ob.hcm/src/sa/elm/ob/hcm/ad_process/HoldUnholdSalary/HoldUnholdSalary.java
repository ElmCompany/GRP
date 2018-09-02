package sa.elm.ob.hcm.ad_process.HoldUnholdSalary;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DbUtility;

import sa.elm.ob.hcm.EHCMHoldUnHoldSalary;
import sa.elm.ob.hcm.EHCMPayrollDefinition;
import sa.elm.ob.hcm.EHCMPayrolldefPeriod;
import sa.elm.ob.hcm.EhcmEmpPerInfo;

public class HoldUnholdSalary implements Process {
  private static final Logger log = Logger.getLogger(HoldUnholdSalary.class);
  private final OBError obError = new OBError();
  private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  @Override
  public void execute(ProcessBundle bundle) throws Exception {

    final String holdUnholdSlryId = (String) bundle.getParams().get("Ehcm_Holdunhold_Salary_ID")
        .toString();
    EHCMHoldUnHoldSalary holdUnholdSlry = OBDal.getInstance().get(EHCMHoldUnHoldSalary.class,
        holdUnholdSlryId);
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    try {
      OBContext.setAdminMode(true);
      log.debug("Processed HoldUnhold :" + holdUnholdSlry.getProcessed());

      EhcmEmpPerInfo empPerInfo = holdUnholdSlry.getEhcmEmpPerinfo();
      EHCMPayrolldefPeriod payrollDefPeriod = holdUnholdSlry.getPayrollPeriod();
      EHCMPayrollDefinition payrollDef = payrollDefPeriod.getEhcmPayrollDefinition();
      Date prdStartDate = dateFormat.parse(payrollDefPeriod.getStartDate().toString());
      String periodStartDate = sa.elm.ob.utility.util.Utility
          .formatDate(payrollDefPeriod.getStartDate());

      // Check payroll processed for the Hold/UnHold period
      String payrollStartDateStr = HoldUnholdSalaryDAO.getLastPayrollProcessedStartDate(empPerInfo);
      if (payrollStartDateStr != null) {
        Date payrollStartDate = dateFormat.parse(payrollStartDateStr);
        if (payrollStartDate != null && payrollStartDate.compareTo(prdStartDate) >= 0) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_HoldUnhold_PayrollRef"));
          bundle.setResult(obError);
          return;
        }
      }

      if (holdUnholdSlry.getProcessed().equalsIgnoreCase("N")) {
        // Process
        holdUnholdSlry.setProcessed("Y");
        holdUnholdSlry.setAlertStatus("P");
        // If unhold process, update hold end period
        if (holdUnholdSlry.getRequestType().equalsIgnoreCase("UHS")) {
          EHCMHoldUnHoldSalary holdReq = OBDal.getInstance().get(EHCMHoldUnHoldSalary.class,
              holdUnholdSlry.getHoldSalaryReference().getId());
          String holdPeriodId = holdUnholdSlry.getHoldSalaryReference().getPayrollPeriod().getId();
          String unHoldPeriodId = holdUnholdSlry.getPayrollPeriod().getId();
          if (holdPeriodId.equalsIgnoreCase(unHoldPeriodId)) {
            holdReq.setHoldEndPeriod(holdUnholdSlry.getPayrollPeriod());
          } else {
            EHCMPayrolldefPeriod prevPeriod = HoldUnholdSalaryDAO
                .getPreviousPayrollPeriod(periodStartDate, holdUnholdSlry.getPayrollPeriod());
            holdReq.setHoldEndPeriod(prevPeriod);
          }
          OBDal.getInstance().save(holdReq);
        }
        OBDal.getInstance().save(holdUnholdSlry);
        OBDal.getInstance().flush();

        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("Efin_BudgetPre_Submit"));
        bundle.setResult(obError);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
      } else if (holdUnholdSlry.getProcessed().equalsIgnoreCase("Y")) {

        if (holdUnholdSlry.getRequestType().equalsIgnoreCase("HS")) {
          // Check Reference exists in UnHold request
          if (holdUnholdSlry.getEHCMHoldUnHoldSalaryHoldSalaryReferenceList().size() > 0) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("EHCM_HasUnhold_Ref"));
            bundle.setResult(obError);
            return;
          }
        } else if (holdUnholdSlry.getRequestType().equalsIgnoreCase("UHS")) {
          // Check Hold Request exists in future periods
          if (HoldUnholdSalaryDAO.hasFutureHoldRequest(empPerInfo, periodStartDate)) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("EHCM_HasFutureHold_Req"));
            bundle.setResult(obError);
            return;
          }
        }

        holdUnholdSlry.setProcessed("N");
        holdUnholdSlry.setAlertStatus("DR");
        // If Unhold Reactivate, remove hold end period
        if (holdUnholdSlry.getRequestType().equalsIgnoreCase("UHS")) {
          EHCMHoldUnHoldSalary holdReq = OBDal.getInstance().get(EHCMHoldUnHoldSalary.class,
              holdUnholdSlry.getHoldSalaryReference().getId());
          holdReq.setHoldEndPeriod(null);
          OBDal.getInstance().save(holdReq);
        }
        OBDal.getInstance().save(holdUnholdSlry);
        OBDal.getInstance().flush();

        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("Efin_BudgetPre_Submit"));
        bundle.setResult(obError);
        OBDal.getInstance().flush();
      }
    } catch (Exception e) {
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      Throwable t = DbUtility.getUnderlyingSQLException(e);
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), t.getMessage());
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
