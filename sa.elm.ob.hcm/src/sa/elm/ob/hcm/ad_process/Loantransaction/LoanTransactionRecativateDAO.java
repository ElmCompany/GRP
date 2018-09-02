package sa.elm.ob.hcm.ad_process.Loantransaction;

import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;

import sa.elm.ob.hcm.EHCMLoanTransaction;
import sa.elm.ob.hcm.EHCMPayrollProcessHdr;
import sa.elm.ob.hcm.EHCMPayrollProcessLne;
import sa.elm.ob.hcm.EHCMPayrolldefPeriod;
import sa.elm.ob.hcm.EhcmLoanHistory;

/**
 * 
 * @author Gokul 29/06/2018
 *
 */
public class LoanTransactionRecativateDAO {

  private static final Logger LOG = LoggerFactory.getLogger(LoanTransactionRecativateDAO.class);

  // This class is to reactivate the loan transaction.
  public static boolean LoanTransactionReactivate(EHCMLoanTransaction loan) {
    EHCMPayrolldefPeriod period = null;
    List<EHCMPayrollProcessHdr> payrollHeaderList = null;
    List<EhcmLoanHistory> history = null;
    String periodDate = "";
    boolean isEmployee = false;
    try {
      // validating payroll is processed for this decision.
      OBQuery<EhcmLoanHistory> line = OBDal.getInstance().createQuery(EhcmLoanHistory.class,
          "as eh where  eh.ehcmLoanTransaction.id =:loanid AND ischild = 'Y' ");
      line.setNamedParameter("loanid", loan.getId());
      history = line.list();
      if (history.size() > 0) {
        return false;
      }

      if (loan.getLoanType().equals("HO")) {
        // chk hold date period is processed in payroll.
        periodDate = sa.elm.ob.utility.util.Utility.formatDate(loan.getHoldDate());
        period = LoanTransactionDAO.getPayrollPeriod(periodDate, loan.getEmployee().getId());
        OBQuery<EHCMPayrollProcessHdr> header = OBDal.getInstance().createQuery(
            EHCMPayrollProcessHdr.class,
            "as e where e.payrollPeriod.endDate >= to_date(:holdDate) order by e.payrollPeriod.startDate desc");
        header.setNamedParameter("holdDate", periodDate);
        payrollHeaderList = header.list();
        for (EHCMPayrollProcessHdr payrollHdrObj : payrollHeaderList) {
          for (EHCMPayrollProcessLne payrollLne : payrollHdrObj.getEHCMPayrollProcessLneList()) {
            if (payrollLne.getEmployee().equals(loan.getEmployee())) {
              isEmployee = true;
              break;
            }
          }
          if (isEmployee && period.getStartDate()
              .compareTo(payrollHdrObj.getPayrollPeriod().getEndDate()) <= 0) {
            return false;
          }
        }
      }

      // remove history reference.
      List<EhcmLoanHistory> historyList = loan.getEhcmLoanHistoryList();
      loan.getEhcmLoanHistoryList().removeAll(historyList);
      for (EhcmLoanHistory historyObj : historyList) {
        OBDal.getInstance().remove(historyObj);
      }

      // Reactivate Loan.
      loan.setSueDecision(false);
      loan.setDecisionStatus("UP");
      loan.setEffectivePeriod(null);
      OBDal.getInstance().save(loan);
      return true;

    } catch (

    OBException e) {
      LOG.error(" Exception in Loan Transaction Reactivate : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error(" Exception in Loan Transaction Reactivate : " + e);
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

}
