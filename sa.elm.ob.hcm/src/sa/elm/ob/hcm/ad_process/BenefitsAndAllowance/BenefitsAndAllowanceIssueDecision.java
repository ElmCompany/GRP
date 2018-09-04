package sa.elm.ob.hcm.ad_process.BenefitsAndAllowance;

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

import sa.elm.ob.hcm.EHCMBenefitAllowance;

public class BenefitsAndAllowanceIssueDecision implements Process {
  private static final Logger log = Logger.getLogger(BenefitsAndAllowanceIssueDecision.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {

    final String allowanceId = (String) bundle.getParams().get("Ehcm_Benefit_Allowance_ID")
        .toString();
    EHCMBenefitAllowance allowance = OBDal.getInstance().get(EHCMBenefitAllowance.class,
        allowanceId);
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    BenefitsAndAllowanceDAOImpl empAllowanceDAOImpl = new BenefitsAndAllowanceDAOImpl();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    try {
      OBContext.setAdminMode(true);
      log.debug("issueDecision B&A :" + allowance.isSueDecision());
      log.debug("getDecisionType B&A :" + allowance.getDecisionType());

      // check Issued or not
      if (!allowance.isSueDecision()) {
        if ("UP".equals(allowance.getDecisionType())) {
          boolean isProc = false;
          EHCMBenefitAllowance orgAllowance = OBDal.getInstance().get(EHCMBenefitAllowance.class,
              allowance.getOriginalDecisionNo().getId());
          // Check if original decision processed
          if (empAllowanceDAOImpl.checkPayrollProcessed(orgAllowance)) {
            Date originalStartDate = orgAllowance.getStartDate();
            Date updateStartDate = allowance.getStartDate();
            Date originalEndDate = orgAllowance.getEndDate();
            Date updateEndDate = allowance.getEndDate();

            originalStartDate = formatter.parse(orgAllowance.getStartDate().toString());
            updateStartDate = formatter.parse(allowance.getStartDate().toString());
            if (orgAllowance.getEndDate() != null)
              originalEndDate = formatter.parse(orgAllowance.getEndDate().toString());
            if (allowance.getEndDate() != null)
              updateEndDate = formatter.parse(allowance.getEndDate().toString());

            // Case 1:Check start date should not be changed
            if (originalStartDate.compareTo(updateStartDate) != 0) {
              isProc = true;
            } // Case 2:Check original end date is greater if yes, don't allow to update
            else if ((originalEndDate != null && updateEndDate != null)
                && originalEndDate.compareTo(updateEndDate) > 0) {
              isProc = true;
            }
          }
          if (isProc) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("EHCM_OrginalDecisionPayrollProc"));
            bundle.setResult(obError);
            return;
          }
        }
        if (empAllowanceDAOImpl.checkPayrollProcessed(allowance)) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_PayrollProcessed"));
          bundle.setResult(obError);
          return;
        } else {
          // update status as Issued and set decision date for all cases
          allowance.setSueDecision(true);
          allowance.setDecisionDate(new Date());
          allowance.setDecisionStatus("I");
          OBDal.getInstance().save(allowance);
          OBDal.getInstance().flush();

          obError.setType("Success");
          obError.setTitle("Success");
          obError.setMessage(OBMessageUtils.messageBD("Efin_BudgetPre_Submit"));
          bundle.setResult(obError);
        }

        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
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
