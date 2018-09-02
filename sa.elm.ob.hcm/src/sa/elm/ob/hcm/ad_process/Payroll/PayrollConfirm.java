package sa.elm.ob.hcm.ad_process.Payroll;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMPayrollProcessHdr;
import sa.elm.ob.hcm.ad_process.Payroll.DAO.PayrollConfirmDao;

/**
 * This class used to check element reference status.
 * 
 * @author Gowtham
 *
 */
public class PayrollConfirm extends DalBaseProcess {
  private static final Logger log = LoggerFactory.getLogger(PayrollBaseProcess.class);
  private final OBError obError = new OBError();
  static boolean errorFlagMajor = false;
  static boolean errorFlagMinor = false;
  static String errorMessage = "";

  public void doExecute(ProcessBundle bundle) throws Exception {
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    errorFlagMajor = false;
    errorFlagMinor = false;
    errorMessage = "";

    try {
      OBContext.setAdminMode(true);
      String payrollProcessHdrId = (String) bundle.getParams().get("Ehcm_Payroll_Process_Hdr_ID");
      boolean isPayrollLineError = false;

      EHCMPayrollProcessHdr header = OBDal.getInstance().get(EHCMPayrollProcessHdr.class,
          payrollProcessHdrId);

      isPayrollLineError = PayrollConfirmDao.getPayrollLineStatus(header);

      if (isPayrollLineError) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_PayProcess_EmpFailed"));
      } else {
        // set header status as confirm
        header.setStatus("C");
        OBDal.getInstance().save(header);

        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_PayrollProcess_Success"));
      }
      bundle.setResult(obError);

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      e.printStackTrace();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
