package sa.elm.ob.hcm.actionHandler.irtabs.irtabprocess;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

import sa.elm.ob.hcm.EHCMEmpEvaluation;
import sa.elm.ob.utility.util.irtabsutils.IRTabIconVariables;

public class EmployeeEvaluation extends IRTabIconVariables {
  Logger log = Logger.getLogger(EmployeeEvaluation.class);
  private static final String EmpEvalEmployeesTabID = "F206150C87E14866A68F08389DE85549";
  private static final String EMP_EVAL_STATUS_COMPLETED = "CO";

  public void getIconVariables(HttpServletRequest request, JSONObject jsonData) {
    try {
      OBContext.setAdminMode(true);
      final String recordId = jsonData.getString("recordId");
      final String tabId = jsonData.getString("tabId") == null ? "" : jsonData.getString("tabId");

      // Employee Evaluation - employees tab
      if (tabId.equals(EmpEvalEmployeesTabID)) {
        if (!recordId.equals("")) {
          EHCMEmpEvaluation empEvlAtt = OBDal.getInstance().get(EHCMEmpEvaluation.class, recordId);
          if (empEvlAtt.getStatus().equals(EMP_EVAL_STATUS_COMPLETED)) {
            enable = 1;
          }
        }
      }
    } catch (Exception e) {
      log.error("Exception in getIconVariables(): " + e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
