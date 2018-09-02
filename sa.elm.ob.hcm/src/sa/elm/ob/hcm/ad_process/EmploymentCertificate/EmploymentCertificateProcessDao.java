package sa.elm.ob.hcm.ad_process.EmploymentCertificate;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;

import sa.elm.ob.hcm.ehcmemploymentcertificate;

/**
 * 
 * @author Gokul 02/07/18
 *
 */
public class EmploymentCertificateProcessDao {

  private static final Logger LOG = LoggerFactory.getLogger(EmploymentCertificateProcessDao.class);

  public static boolean employmentprocess(ehcmemploymentcertificate process) {
    try {
      HttpServletRequest request = RequestContext.get().getRequest();
      VariablesSecureApp vars = new VariablesSecureApp(request);
      process.setCertificateStatus("PR");
      process.setAction("RE");
      process.setUpdated(new java.util.Date());
      process.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      OBDal.getInstance().save(process);
      return true;
    } catch (Exception e) {
      LOG.error("Error in Employment Certificate Process DAO: ");
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

  public static boolean employmentreactivate(ehcmemploymentcertificate process) {
    try {
      HttpServletRequest request = RequestContext.get().getRequest();
      VariablesSecureApp vars = new VariablesSecureApp(request);
      process.setCertificateStatus("DR");
      process.setAction("CO");
      process.setUpdated(new java.util.Date());
      process.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      OBDal.getInstance().save(process);
      return true;

    } catch (Exception e) {
      LOG.error("Error in Employment Certificate Process DAO: ");
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
