package sa.elm.ob.hcm.ad_process.EmpExtendService;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EhcmExtendService;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.EmpExtendService.DAO.ExtendServiceHandlerDAO;

/**
 * @author Priyanka Ranjan 30/08/2018
 */

public class ExtendOfServiceReactivate extends DalBaseProcess {

  private static final Logger log = LoggerFactory.getLogger(ExtendOfServiceReactivate.class);

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Entering into Extend of Service Reactivate");

    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    try {
      OBContext.setAdminMode();
      String empExtendServiceId = (String) bundle.getParams().get("Ehcm_Extend_Service_ID");
      EhcmExtendService extendservice = OBDal.getInstance().get(EhcmExtendService.class,
          empExtendServiceId);

      if (!extendservice.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
        // revert the changes after reactivate- remove extended record and update enddate null and
        // change status for recent record
        ExtendServiceHandlerDAO.revertTheChangesInEmploymentInfo(extendservice, vars,
            extendservice.getClient().getId());

        // Update Decision Status of Extend Of Service back to UnderProcess
        ExtendServiceHandlerDAO.updateExtendofServiceStatus(extendservice);
      } else {
        // insert the new record in employment info and update enddate,active flag for recent record
        ExtendServiceHandlerDAO.revertChangesAfterCancelReactivate(extendservice,
            extendservice.getClient().getId());
      }

      OBError result = OBErrorBuilder.buildMessage(null, "success", "@ProcessOK@");
      bundle.setResult(result);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();

    } catch (Exception e) {
      e.printStackTrace();
      log.debug("Exeception in ExtendOfServiceReactivate:" + e);
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
