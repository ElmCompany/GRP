package sa.elm.ob.hcm.ad_process.Loantransaction;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.DalUtil;
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
import sa.elm.ob.hcm.EmploymentInfo;

/**
 * 
 * @author Gokul 22/06/18
 *
 */

public class LoanTransactionDAO {

  private static final Logger LOG = LoggerFactory.getLogger(LoanTransactionDAO.class);

  /**
   * sets decision status to issued (I)
   * 
   * @param loan
   * 
   * @return 0 ->success else failed
   */
  public static int loanTransactionissue(EHCMLoanTransaction loan) {
    EHCMPayrolldefPeriod period = null;
    List<EHCMPayrollProcessHdr> payrollHeaderList = null;
    Calendar c = Calendar.getInstance();
    Long duration = loan.getHoldDuration();
    String periodDate = "";
    boolean isEmployee = false;
    try {

      if (loan.getLoanType().equals("CA")) {
        // chk loan installment before cancel.
        if (loan.getOriginalDecisionNo().getPaidamount().compareTo(BigDecimal.ZERO) > 0) {
          return 2;
        }
      }

      if (loan.getLoanType().equals("CR")) {
        // chk record period is already processed in payroll.
        periodDate = sa.elm.ob.utility.util.Utility.formatDate(loan.getInstallmentStartDate());
        period = getPayrollPeriod(periodDate, loan.getEmployee().getId());
        OBQuery<EHCMPayrollProcessHdr> header = OBDal.getInstance().createQuery(
            EHCMPayrollProcessHdr.class,
            "as e where e.payrollPeriod.endDate >= to_date(:instalDate) order by e.payrollPeriod.startDate desc");
        header.setNamedParameter("instalDate", periodDate);
        header.setMaxResult(1);
        payrollHeaderList = header.list();
        for (EHCMPayrollProcessHdr payrollHdrObj : payrollHeaderList) {
          for (EHCMPayrollProcessLne payrollLne : payrollHdrObj.getEHCMPayrollProcessLneList()) {
            if (payrollLne.getEmployee().equals(loan.getEmployee())) {
              isEmployee = true;
              break;
            }
          }
          if (isEmployee && period.getStartDate()
              .compareTo(payrollHdrObj.getPayrollPeriod().getStartDate()) >= 0) {
            return 3;
          }
        }
      }

      if (loan.getLoanType().equals("HO")) {
        // chk hold date period is processed in payroll.
        periodDate = sa.elm.ob.utility.util.Utility.formatDate(loan.getHoldDate());
        period = getPayrollPeriod(periodDate, loan.getEmployee().getId());
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
            return 4;
          }
        }

        // calculate hold end period
        c.setTime(loan.getHoldDate());
        c.add(Calendar.MONTH, duration.intValue());
        Date aftr2Month = c.getTime();
        String aftr2Mon = sa.elm.ob.utility.util.Utility.formatDate(aftr2Month);
        period = getPayrollPeriod(aftr2Mon, loan.getEmployee().getId());
        if (period == null) {
          return 1;
        }
      }

      if (loan.getLoanType() != "CR") {
        // copy loan history from previous decision.
        copyLoanHistory(loan);
      }

      loan.setSueDecision(true);
      loan.setDecisionDate(new Date());
      loan.setDecisionStatus("I");
      loan.setHoldEndPeriod(period);
      OBDal.getInstance().save(loan);
      return 0;

    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      LOG.error(" Exception in Loan Transaction Issue Decision : " + e);
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

  /**
   * Get payroll period based on employment payroll definition
   * 
   * @param periodDate
   * @param employeeId
   * @return
   */
  public static EHCMPayrolldefPeriod getPayrollPeriod(String periodDate, String employeeId) {
    EHCMPayrolldefPeriod payrollPeriod = null;
    try {
      List<EmploymentInfo> employment = null;
      List<EHCMPayrolldefPeriod> EHCMPayrolldefPeriod = null;
      String payrollDef = "";
      // Get Employment for employee for payroll definition
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e where e.ehcmEmpPerinfo.id=:empId order by e.creationDate desc");
      empInfo.setNamedParameter("empId", employeeId);
      employment = empInfo.list();
      payrollDef = employment.get(0).getEhcmPayrollDefinition().getId();

      // getpayroll Period
      OBQuery<EHCMPayrolldefPeriod> period = OBDal.getInstance().createQuery(
          EHCMPayrolldefPeriod.class,
          "as e where e.ehcmPayrollDefinition.id=:payrollDef and e.startDate <= to_date(:periodDate,'dd-MM-yyyy')  and e.endDate >= to_date(:periodDate,'dd-MM-yyyy')");
      period.setNamedParameter("payrollDef", payrollDef);
      period.setNamedParameter("periodDate", periodDate);
      EHCMPayrolldefPeriod = period.list();
      payrollPeriod = EHCMPayrolldefPeriod.get(0);

      return payrollPeriod;
    } catch (Exception e) {
      LOG.error("Error in getPayrollPeriod:" + e);
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }

  /**
   * Copy loan histroy to maintain in new loan transaction.
   * 
   * @param loanobj
   * @return
   */
  public static boolean copyLoanHistory(EHCMLoanTransaction loanobj) {
    try {
      EHCMPayrolldefPeriod latest_period = null;
      Calendar c = Calendar.getInstance();

      if (loanobj.getOriginalDecisionNo() != null) {
        for (EhcmLoanHistory history : loanobj.getOriginalDecisionNo().getEhcmLoanHistoryList()) {
          EhcmLoanHistory objCloneHistory = (EhcmLoanHistory) DalUtil.copy(history, false);
          objCloneHistory.setEhcmLoanTransaction(loanobj);
          objCloneHistory.setChild(false);
          OBDal.getInstance().save(objCloneHistory);
          OBDal.getInstance().flush();
          latest_period = history.getPayrollPeriod();
        }

        // calculate hold end period
        c.setTime(latest_period.getEndDate());
        c.add(Calendar.DATE, 1);
        Date aftr1Month = c.getTime();
        String aftr1Mon = sa.elm.ob.utility.util.Utility.formatDate(aftr1Month);
        latest_period = getPayrollPeriod(aftr1Mon, loanobj.getEmployee().getId());

        loanobj.setEffectivePeriod(latest_period);
        OBDal.getInstance().save(loanobj);
      }
    } catch (Exception e) {
      LOG.error("Error in PayrollBaseProcess.java : insertLoanHistory() ");
      e.printStackTrace();
      return false;
    }
    return true;
  }

}
