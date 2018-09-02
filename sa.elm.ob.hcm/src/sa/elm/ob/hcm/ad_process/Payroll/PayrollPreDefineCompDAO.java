package sa.elm.ob.hcm.ad_process.Payroll;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.EHCMEarnDeductHdr;
import sa.elm.ob.hcm.EHCMEarnDeductLne;
import sa.elm.ob.hcm.EHCMPayrolldefPeriod;

public class PayrollPreDefineCompDAO {
  private Connection conn = null;
  VariablesSecureApp vars = null;
  private static Logger log4j = Logger.getLogger(PayrollPreDefineCompDAO.class);

  public PayrollPreDefineCompDAO(Connection con) {
    this.conn = con;
  }

  public static boolean removeExistingPayrollEmp(EHCMPayrolldefPeriod period) {
    try {
      final OBQuery<EHCMEarnDeductLne> earDedLneQry = OBDal.getInstance().createQuery(
          EHCMEarnDeductLne.class,
          "e where ehcmEarnDeductHdr.id in (select e.id from EHCM_Earn_Deduct_Hdr e where payrollPeriod='"
              + period.getId() + "')");
      earDedLneQry.deleteQuery().executeUpdate();

      final OBQuery<EHCMEarnDeductHdr> earDedHdrQry = OBDal.getInstance()
          .createQuery(EHCMEarnDeductHdr.class, " e where payrollPeriod='" + period.getId() + "')");
      earDedHdrQry.deleteQuery().executeUpdate();
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      return true;
    }
  }

}
