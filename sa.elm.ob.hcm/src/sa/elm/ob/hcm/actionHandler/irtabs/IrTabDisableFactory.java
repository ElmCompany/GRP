package sa.elm.ob.hcm.actionHandler.irtabs;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import sa.elm.ob.hcm.actionHandler.irtabs.irtabprocess.AbsenceDecision;
import sa.elm.ob.hcm.actionHandler.irtabs.irtabprocess.EmployeeEvaluation;
import sa.elm.ob.utility.util.irtabsutils.IRTabIconVariables;

public class IrTabDisableFactory {
  Logger log4j = Logger.getLogger(IrTabDisableFactory.class);
  private static final String EMP_EVAL_EMPLOYEES_TAB_ID = "F206150C87E14866A68F08389DE85549";
  private static final String ABSENCE_DECISION_TAB_ID = "076B159D222E4EEB85C70B3FEE6B22F6";

  public IRTabIconVariables getTab(HttpServletRequest request, JSONObject jsonData) {
    IRTabIconVariables irtabIcon = null;
    try {
      final String tabId = jsonData.getString("tabId") == null ? "" : jsonData.getString("tabId");

      /* Absence Decision */
      if (tabId.equals(ABSENCE_DECISION_TAB_ID)) {
        irtabIcon = new AbsenceDecision();
      }
      // Employee Evaluation - Employees Tab
      else if (tabId.equals(EMP_EVAL_EMPLOYEES_TAB_ID)) {
        irtabIcon = new EmployeeEvaluation();
      }
      if (irtabIcon != null) {
        irtabIcon.getIconVariables(request, jsonData);
      }
    } catch (Exception e) {
      log4j.error("Excpetion in getTab(): " + e);
      return null;
    }
    return irtabIcon;
  }
}