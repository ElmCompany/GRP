package sa.elm.ob.hcm.ad_process.BenefitsAndAllowance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMBenefitAllowance;

/**
 * This process class used for Benefits and AllowanceDAO Implementation
 * 
 * @author Kousalya 28-07-2018
 *
 */

public class BenefitsAndAllowanceDAOImpl implements BenefitsAndAllowanceDAO {

  private static final Logger log = LoggerFactory.getLogger(BenefitsAndAllowanceDAOImpl.class);

  /**
   * Reactivate allowance by update status as under processing
   * 
   * @param allowance
   * @return boolean
   * @throws Exception
   */
  public boolean reactivateEmpBenefitandAllowance(EHCMBenefitAllowance allowance) throws Exception {
    try {
      OBContext.setAdminMode();
      allowance.setUpdated(new java.util.Date());
      allowance.setUpdatedBy(null);
      allowance.setReactivate(true);
      allowance.setDecisionStatus("UP");
      allowance.setSueDecision(false);
      OBDal.getInstance().save(allowance);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in reactivateEmpBenefitandAllowance: ", e);
      return false;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Check Payroll Processed for an employee within a period
   * 
   * @param allowance
   * @return boolean
   * @throws Exception
   */
  @SuppressWarnings("rawtypes")
  public boolean checkPayrollProcessed(EHCMBenefitAllowance allowance, boolean isOrigProc) {
    StringBuffer query = null;
    Query payPrdQuery = null;
    boolean isPayrollProcessed = false;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    try {
      OBContext.setAdminMode();
      Date allowanceStartDate = allowance.getStartDate();
      Date allowanceEndDate = allowance.getEndDate();
      query = new StringBuffer();
      query.append("select distinct hdr.payrollPeriod.id, prd.startDate, prd.endDate "
          + " from EHCM_Payroll_Process_Lne pln " + "left join pln.payrollProcessHeader hdr "
          + " left join hdr.payrollPeriod prd " + "where pln.employee.id=:employeeId ");
      payPrdQuery = OBDal.getInstance().getSession().createQuery(query.toString());
      payPrdQuery.setParameter("employeeId", allowance.getEmployee().getId());

      log.debug(" Query : " + query.toString());
      if (payPrdQuery != null) {
        if (payPrdQuery.list().size() > 0) {
          for (Iterator iterator = payPrdQuery.iterate(); iterator.hasNext();) {
            Object[] row = (Object[]) iterator.next();
            String payrollStartDateStr = row[1].toString();
            String payrollEndDateStr = row[2].toString();

            Date payrollStartDate = formatter.parse(payrollStartDateStr);
            Date payrollEndDate = formatter.parse(payrollEndDateStr);

            allowanceStartDate = formatter.parse(allowance.getStartDate().toString());

            if (allowanceEndDate == null)
              allowanceEndDate = formatter.parse("9999-12-31");
            else
              allowanceEndDate = formatter.parse(allowance.getEndDate().toString());
            log.debug("Payroll>" + payrollStartDate + "//" + payrollEndDate);
            log.debug("Allowance>" + allowanceStartDate + "//" + allowanceEndDate);

            if ((payrollStartDate.compareTo(allowanceStartDate) <= 0
                && payrollEndDate.compareTo(allowanceStartDate) >= 0 && !isOrigProc)
                || (payrollStartDate.compareTo(allowanceEndDate) <= 0
                    && payrollEndDate.compareTo(allowanceEndDate) > 0
                    && (payrollEndDate.compareTo(allowanceEndDate) == 0
                        && !"UP".equals(allowance.getDecisionType())))) {
              return true;
            }
          }
        }
      }
    } catch (OBException e) {
      log.error("Exception while checkPayrollProcessed:" + e);
      throw new OBException(e.getMessage());
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }
    return isPayrollProcessed;
  }
}
