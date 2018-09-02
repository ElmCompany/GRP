package sa.elm.ob.hcm.ad_actionbutton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMElmttypeDef;
import sa.elm.ob.hcm.EhcmElementGroup;
import sa.elm.ob.hcm.EhcmElementGroupLine;

public class EhcmAddClassification extends BaseProcessActionHandler {

  private static final Logger log = LoggerFactory.getLogger(EhcmAddClassification.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    // TODO Auto-generated method stub
    JSONObject jsonResponse = new JSONObject();
    try {
      OBContext.setAdminMode(true);
      JSONObject jsonRequest = new JSONObject(content);
      final String groupId = jsonRequest.getString("inpehcmElementGroupId");
      JSONObject jsonparams = jsonRequest.getJSONObject("_params");
      final String Earning = jsonparams.getString("Earning");
      final String Deduction = jsonparams.getString("Deduction");
      final String Information = jsonparams.getString("Information");
      final String Balance = jsonparams.getString("Balance");
      String classificationList = "";
      // List li = new ArrayList();

      if (Earning.equalsIgnoreCase("true")) {
        if (classificationList.equals(""))
          classificationList = classificationList + "'ER'";
        else
          classificationList = classificationList + ",'ER'";
      }
      if (Deduction.equalsIgnoreCase("true")) {
        if (classificationList.equals(""))
          classificationList = classificationList + "'DE'";
        else
          classificationList = classificationList + ",'DE'";
      }
      if (Information.equalsIgnoreCase("true")) {
        if (classificationList.equals(""))
          classificationList = classificationList + "'INFO'";
        else
          classificationList = classificationList + ",'INFO'";
      }
      if (Balance.equalsIgnoreCase("true")) {
        if (classificationList.equals(""))
          classificationList = classificationList + "'BAL'";
        else
          classificationList = classificationList + ",'BAL'";
      }
      if (!classificationList.equalsIgnoreCase("")) {
        OBQuery<EHCMElmttypeDef> elementDef = OBDal
            .getInstance()
            .createQuery(
                EHCMElmttypeDef.class,
                "elementClassification in ("
                    + classificationList
                    + ") "
                    + "and id not in(select ehcmElmttypeDef.id from Ehcm_Element_Group_Line where ehcmElementGroup.id = '"
                    + groupId + "')");
        List<EHCMElmttypeDef> li = new ArrayList<EHCMElmttypeDef>();
        li = elementDef.list();
        if (li != null && li.size() > 0) {
          for (int i = 0; i < li.size(); i++) {
            EhcmElementGroupLine line = OBProvider.getInstance().get(EhcmElementGroupLine.class);
            line.setEhcmElementGroup(OBDal.getInstance().get(EhcmElementGroup.class, groupId));
            line.setEhcmElmttypeDef(OBDal.getInstance().get(EHCMElmttypeDef.class,
                li.get(i).getId()));
            line.setClassification(li.get(i).getElementClassification());
            line.setType(li.get(i).getType());
            OBDal.getInstance().save(line);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      log.error("exception while adding classification:", e);
    }
    return jsonResponse;
  }
}
