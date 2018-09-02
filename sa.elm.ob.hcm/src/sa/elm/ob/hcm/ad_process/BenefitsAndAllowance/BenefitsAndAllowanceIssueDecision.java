package sa.elm.ob.hcm.ad_process.BenefitsAndAllowance;

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
    try {
      OBContext.setAdminMode(true);
      log.debug("issueDecision B&A :" + allowance.isSueDecision());
      log.debug("getDecisionType B&A :" + allowance.getDecisionType());

      // check Issued or not
      if (!allowance.isSueDecision()) {
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
