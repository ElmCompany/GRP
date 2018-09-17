package sa.elm.ob.hcm.event.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.EHCMEarnDeductPayroll;
import sa.elm.ob.hcm.EHCMPayrollProcessHdr;

/**
 * 
 * @author Gokul 05/09/18
 *
 */
public class PayrollProcessEventDAO {
  private static Logger LOG = Logger.getLogger(PayrollProcessEventDAO.class);

  /**
   * Deletes the header in the payroll and also in Earning and deduction.
   * 
   * @param payroll
   * @param payrollPeriod
   * @param elementGroup
   * @return
   */

  public boolean deleteHeader(String payroll, String payrollPeriod, String elementGroup) {
    try {
      List<EHCMPayrollProcessHdr> ls = new ArrayList<EHCMPayrollProcessHdr>();
      OBQuery<EHCMPayrollProcessHdr> payrollHdr = OBDal.getInstance().createQuery(
          EHCMPayrollProcessHdr.class,
          "as e  where e.payroll.id=:payroll and e.payrollPeriod.id=:payrollPeriod and e.elementGroup.id=:elementGroup ");
      payrollHdr.setNamedParameter("payroll", payroll);
      payrollHdr.setNamedParameter("payrollPeriod", payrollPeriod);
      payrollHdr.setNamedParameter("elementGroup", elementGroup);
      ls = payrollHdr.list();
      // EHCMPayrollProcessHdr duplicateRecord = null;

      if (ls.size() > 0) {
        if (ls.size() == 1) {
          List<EHCMEarnDeductPayroll> earningDedList = new ArrayList<EHCMEarnDeductPayroll>();
          OBQuery<EHCMEarnDeductPayroll> earDedQry = OBDal.getInstance().createQuery(
              EHCMEarnDeductPayroll.class,
              "as e  where e.payroll.id=:payroll and e.payrollPeriod.id=:payrollPeriod and e.elementGroup.id=:elementGroup ");
          earDedQry.setNamedParameter("payroll", payroll);
          earDedQry.setNamedParameter("payrollPeriod", payrollPeriod);
          earDedQry.setNamedParameter("elementGroup", elementGroup);
          earningDedList = earDedQry.list();
          EHCMEarnDeductPayroll earnDedtnId = null;
          if (earningDedList.size() == 1) {
            earnDedtnId = earDedQry.list().get(0);
            OBDal.getInstance().remove(earnDedtnId);
          }
        }
        // duplicateRecord = payrollHdr.list().get(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("Exception in PayrollProcessEvent Dao Delete Header Method : ", e);
    }

    return true;
  }
}
