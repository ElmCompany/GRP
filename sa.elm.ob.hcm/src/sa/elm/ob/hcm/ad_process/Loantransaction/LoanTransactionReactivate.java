package sa.elm.ob.hcm.ad_process.Loantransaction;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;

import sa.elm.ob.hcm.EHCMLoanTransaction;

/**
 * 
 * @author Gokul 29/06/2018
 *
 */
public class LoanTransactionReactivate implements Process {

  private static final Logger LOG = LoggerFactory.getLogger(LoanTransactionRecativateDAO.class);

  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub

    final String loanId = (String) bundle.getParams().get("Ehcm_Loan_Transaction_ID").toString();
    EHCMLoanTransaction loan = OBDal.getInstance().get(EHCMLoanTransaction.class, loanId);
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    try {
      boolean result = false;
      OBContext.setAdminMode(true);

      if (loan.isSueDecision()) {
        // update status as Issued and set decision date for all cases
        result = LoanTransactionRecativateDAO.LoanTransactionReactivate(loan);
        if (result) {
          obError.setType("Success");
          obError.setTitle("Success");
          obError.setMessage(OBMessageUtils.messageBD("ehcm_loan_reactivate"));
          bundle.setResult(obError);
        } else {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_Loan_line_check"));
          bundle.setResult(obError);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error(" Exception in Loan Transaction Reactivate : " + e);
      OBDal.getInstance().rollbackAndClose();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}
