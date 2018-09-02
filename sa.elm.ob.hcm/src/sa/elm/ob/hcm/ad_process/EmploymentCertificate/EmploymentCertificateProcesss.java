package sa.elm.ob.hcm.ad_process.EmploymentCertificate;

import javax.servlet.http.HttpServletRequest;

import org.jfree.util.Log;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import sa.elm.ob.hcm.ehcmemploymentcertificate;

/**
 * 
 * @author Gokul 02/07/2018
 *
 */
public class EmploymentCertificateProcesss implements Process {
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    final String processid = (String) bundle.getParams().get("Ehcm_Employment_Certificate_ID")
        .toString();
    ehcmemploymentcertificate process = OBDal.getInstance().get(ehcmemploymentcertificate.class,
        processid);
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    try {
      boolean result = false;
      if (process.getCertificateStatus().equals("DR")) {
        result = EmploymentCertificateProcessDao.employmentprocess(process);
        if (result) {
          obError.setType("Success");
          obError.setTitle("Success");
          obError.setMessage(OBMessageUtils.messageBD("ehcm_emp_cer_process"));
          bundle.setResult(obError);
        }
      } else {
        result = EmploymentCertificateProcessDao.employmentreactivate(process);
        if (result) {
          obError.setType("Success");
          obError.setTitle("Success");
          obError.setMessage(OBMessageUtils.messageBD("ehcm_emp_cer_reactivate"));
          bundle.setResult(obError);
        }
      }

    } catch (Exception e) {
      Log.error("Error in Employment Certificate Process:" + e);
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    }
  }

}
