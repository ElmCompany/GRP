package sa.elm.ob.hcm.ad_actionbutton;

import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EhcmUpdateMcsHandler extends BaseProcessActionHandler {

  private static final Logger log = LoggerFactory.getLogger(EhcmUpdateMcsHandler.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    // TODO Auto-generated method stub
    JSONObject jsonResponse = new JSONObject();
    try {
      OBContext.setAdminMode();
      JSONObject jsonRequest = new JSONObject(content);
      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
      JSONObject jsonparams = jsonRequest.getJSONObject("_params");
      String message = "";
      log.debug("params:" + jsonparams);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
      JSONObject errormsg = new JSONObject();
      message = OBMessageUtils.parseTranslation("@Success@");
      errormsg.put("severity", "success");
      errormsg.put("text", message);
      jsonResponse.put("message", errormsg);
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error("Exception handling the UpdateMcs", e);

      try {
        jsonResponse = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        jsonResponse.put("message", errorMessage);
      } catch (Exception ignore) {
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    log.debug(" Returned Response " + jsonResponse);
    return jsonResponse;
  }

}
