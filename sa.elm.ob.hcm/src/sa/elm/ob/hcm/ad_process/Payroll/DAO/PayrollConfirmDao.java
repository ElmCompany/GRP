package sa.elm.ob.hcm.ad_process.Payroll.DAO;

import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEarnDeductElm;
import sa.elm.ob.hcm.EHCMEarnDeductElmRef;
import sa.elm.ob.hcm.EHCMPayrollProcessHdr;
import sa.elm.ob.hcm.EHCMPayrollProcessLne;

/**
 * 
 * @author Gowtham
 *
 */

public class PayrollConfirmDao {
  private static final Logger LOG = LoggerFactory.getLogger(PayrollConfirmDao.class);

  // check payroll line referred elements are in issued status.
  public static boolean getPayrollLineStatus(EHCMPayrollProcessHdr header) {
    boolean isError = false;
    boolean isElementError = false;
    boolean isPayrollLineError = false;
    String errorMessage = "";
    String seperator = ",";
    try {
      // get payroll process line
      for (EHCMPayrollProcessLne processline : header.getEHCMPayrollProcessLneList()) {
        isElementError = false;
        errorMessage = "";

        // get earning and deduction element.
        for (EHCMEarnDeductElm element : processline.getEHCMEarnDeductElmList()) {
          isError = false;

          for (EHCMEarnDeductElmRef ref : element.getEHCMEarnDeductElmRefList()) {

            // chk disciplinary action
            if (ref.getEhcmDisciplineAction() != null) {
              if (!ref.getEhcmDisciplineAction().getDecisionStatus().equals("I")) {
                errorMessage = seperator + ref.getPayrollElement().getName() + "-"
                    + ref.getEhcmDisciplineAction().getDecisionNo();
                isError = true;
              }
            }

            // chk business mission
            if (ref.getBusinessMission() != null) {
              if (!ref.getBusinessMission().getDecisionStatus().equals("I")) {
                errorMessage = errorMessage + seperator + ref.getPayrollElement().getName() + "-"
                    + ref.getBusinessMission().getDecisionNo();
                isError = true;
              }
            }

            // overtime
            if (ref.getOvertimeTransaction() != null) {
              if (!ref.getOvertimeTransaction().getDecisionStatus().equals("I")) {
                errorMessage = errorMessage + seperator + ref.getPayrollElement().getName() + "-"
                    + ref.getOvertimeTransaction().getDecisionNo();
                isError = true;
              }
            }

            // scholarship
            if (ref.getEhcmEmpScholarship() != null) {
              if (!ref.getEhcmEmpScholarship().getDecisionStatus().equals("I")) {
                errorMessage = errorMessage + seperator + ref.getPayrollElement().getName() + "-"
                    + ref.getEhcmEmpScholarship().getDecisionNo();
                isError = true;
              }
            }

            // loan
            if (ref.getEhcmLoanTransaction() != null) {
              if (!ref.getEhcmLoanTransaction().getDecisionStatus().equals("I")) {
                errorMessage = errorMessage + seperator + ref.getPayrollElement().getName() + "-"
                    + ref.getEhcmLoanTransaction().getDecisionNo();
                isError = true;
              }
            }

            // allowance
            if (ref.getBenefitsAndAllowance() != null) {
              if (!ref.getBenefitsAndAllowance().getDecisionStatus().equals("I")) {
                errorMessage = errorMessage + seperator + ref.getPayrollElement().getName() + "-"
                    + ref.getBenefitsAndAllowance().getDecisionNo();
                isError = true;
              }
            }

            // Ticket Order
            if (ref.getTicketOrders() != null) {
              if (!ref.getTicketOrders().getDecisionStatus().equals("PR")) {
                errorMessage = errorMessage + seperator + ref.getPayrollElement().getName() + "-"
                    + ref.getTicketOrders().getDecisionNo();
                isError = true;
              }
            }

          }

          // update element to processed if no error.
          if (!isError) {
            element.setPayrollProcessLine(processline);
            element.setProcessed(true);
          } else {
            isElementError = true;
          }
        }

        if (isElementError) {
          errorMessage = errorMessage.replaceFirst(seperator, "");
          processline.setMessage(errorMessage);
          OBDal.getInstance().save(processline);
          isPayrollLineError = true;
        }
      }
      return isPayrollLineError;

    } catch (Exception e) {
      LOG.error("Error in payrollConfrom process Dao " + e);
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

}