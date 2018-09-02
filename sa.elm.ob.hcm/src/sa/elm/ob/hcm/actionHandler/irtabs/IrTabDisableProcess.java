package sa.elm.ob.hcm.actionHandler.irtabs;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;

import sa.elm.ob.utility.util.irtabsutils.IRTabIconVariables;

public class IrTabDisableProcess extends BaseActionHandler {
  Logger log4j = Logger.getLogger(IrTabDisableProcess.class);
  JSONObject json;

  protected JSONObject execute(Map<String, Object> parameters, String data) {
    try {
      /* Get the data as json */
      final JSONObject jsonData = new JSONObject(data);
      HttpServletRequest request = RequestContext.get().getRequest();
      json = new JSONObject();

      IrTabDisableFactory irtab = new IrTabDisableFactory();
      IRTabIconVariables irtabIcon = irtab.getTab(request, jsonData);
      if (irtabIcon.getEnable() == 1) {
        json.put("IsDraft", 1);
      } else {
        json.put("IsDraft", 0);
      }
      log4j.debug("json:" + json);
    } catch (Exception e) {
      throw new OBException(e);
    }
    return json;
  }
}