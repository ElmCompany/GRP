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
import org.openbravo.service.db.DbUtility;

import sa.elm.ob.hcm.EHCMLoanTransaction;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;

/**
 * 
 * @author Gokul 22/06/18
 *
 */
public class LoanTransactionIssueDecision implements Process {
  private final OBError obError = new OBError();
  private static final int payrollNotDefined = 1;
  private static final int payrollProcessedCantCancel = 2;
  private static final int payrollProcessed = 3;
  private static final int payrollProcessedHoldPeriod = 4;

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    final String loanId = (String) bundle.getParams().get("Ehcm_Loan_Transaction_ID").toString();
    EHCMLoanTransaction loan = OBDal.getInstance().get(EHCMLoanTransaction.class, loanId);
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    try {
      int result = 0;
      OBContext.setAdminMode(true);
      // check whether the employee is suspended or not
      if (loan.getEmployee().getEmploymentStatus()
          .equals(DecisionTypeConstants.EMPLOYMENTSTATUS_SUSPENDED)) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_emplo_suspend"));
        bundle.setResult(obError);
        return;
      }

      // check Issued or not
      if (!loan.isSueDecision()) {

        // update status as Issued and set decision date for all cases
        result = LoanTransactionDAO.loanTransactionissue(loan);
        // if result 0 success else need to handle propoer exception
        obError.setType("Error");
        obError.setTitle("Error");
        if (result == payrollNotDefined) {
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_Payroll_Not_Defined"));
          bundle.setResult(obError);
        } else if (result == payrollProcessedCantCancel) {
          obError.setMessage(OBMessageUtils.messageBD("EHCM_loan_installment_check"));
          bundle.setResult(obError);
        } else if (result == payrollProcessed) {
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_Payrollinstall_processed"));
          bundle.setResult(obError);
        } else if (result == payrollProcessedHoldPeriod) {
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_PayrollHold_processed"));
          bundle.setResult(obError);
        } else {
          obError.setType("Success");
          obError.setTitle("Success");
          obError.setMessage(OBMessageUtils.messageBD("ehcm_loan_transaction"));
          bundle.setResult(obError);
        }
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
