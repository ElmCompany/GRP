package sa.elm.ob.hcm.ad_process.EmpExtendService;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DbUtility;

import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmExtendService;
import sa.elm.ob.hcm.ad_process.EmpExtendService.DAO.ExtendServiceHandlerDAO;

/**
 * @author poongodi on 08/02/2018
 */
public class ExtendServiceProcess implements Process {
  private static final Logger log = Logger.getLogger(ExtendServiceProcess.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Issue the EmpExtendService");

    final String extendServiceId = bundle.getParams().get("Ehcm_Extend_Service_ID").toString();
    EhcmExtendService extendServiceProcess = OBDal.getInstance().get(EhcmExtendService.class,
        extendServiceId);
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    int count = 0;
    String ClientId = extendServiceProcess.getClient().getId();

    try {

      OBContext.setAdminMode(true);
      EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
          extendServiceProcess.getEmployee().getId());

      // If extension has reached for that employee then throw the error.
      if (extendServiceProcess.getDecisionType().equals("CR")) {
        JSONObject result = new JSONObject();

        result = sa.elm.ob.hcm.util.UtilityDAO.getExtendPeriodRecord(ClientId,
            extendServiceProcess.getOrganization().getId());
        if (result != null) {
          if (result.has("extendAllowed") && result.has("maxExtPeriod")) {
            if (result.getInt("extendAllowed") == 0 && result.getInt("maxExtPeriod") > 0) {
              obError.setType("Error");
              obError.setTitle("Error");
              obError.setMessage(OBMessageUtils.messageBD("Ehcm_client_Extperiod"));
              bundle.setResult(obError);
              return;
            }

            else if (result.getInt("maxExtPeriod") == 0 && result.getInt("extendAllowed") > 0) {
              obError.setType("Error");
              obError.setTitle("Error");
              obError.setMessage(OBMessageUtils.messageBD("Ehcm_client_maxextpeiod"));
              bundle.setResult(obError);
              return;
            } else if (result.getInt("maxExtPeriod") == 0 && result.getInt("extendAllowed") == 0) {
              obError.setType("Error");
              obError.setTitle("Error");
              obError.setMessage(OBMessageUtils.messageBD("Ehcm_Extend_Error"));
              bundle.setResult(obError);
              return;
            } else {
              int empCount = ExtendServiceHandlerDAO
                  .getExtendCountFromEmploymentInfo(extendServiceProcess);

              if (empCount > 0 && result.getInt("extendAllowed") > 0) {
                if (empCount >= result.getInt("extendAllowed")) {
                  obError.setType("Error");
                  obError.setTitle("Error");
                  obError.setMessage(OBMessageUtils.messageBD("Ehcm_Emp_ExtendPeriod"));
                  bundle.setResult(obError);
                  return;
                }
              }
            }
          }
        }
        // Check the employee period exist.
        boolean periodExists = ExtendServiceHandlerDAO.checkPeriodExists(extendServiceProcess);
        if (periodExists) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_period_exist"));
          bundle.setResult(obError);
          return;
        }
      }

      // check Issued or not
      if (!extendServiceProcess.isSueDecision()) {
        // update status as Issued and set decision date for all cases
        extendServiceProcess.setSueDecision(true);
        extendServiceProcess.setDecisionDate(new Date());
        extendServiceProcess.setDecisionStatus("I");
        OBDal.getInstance().save(extendServiceProcess);
        OBDal.getInstance().flush();

        count = ExtendServiceHandlerDAO.insertLineinEmploymentInfo(extendServiceProcess, vars);

        if (count == 1) {
          obError.setType("Success");
          obError.setTitle("Success");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_ExtraStep_Process"));
          bundle.setResult(obError);
          OBDal.getInstance().flush();
          OBDal.getInstance().commitAndClose();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      Throwable t = DbUtility.getUnderlyingSQLException(e);
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
