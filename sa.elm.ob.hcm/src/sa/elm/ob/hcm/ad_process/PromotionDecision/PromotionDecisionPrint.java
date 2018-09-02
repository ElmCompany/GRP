package sa.elm.ob.hcm.ad_process.PromotionDecision;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;

import sa.elm.ob.hcm.ad_reports.employeesbusinessmission.EmployeesBusinessMissionDAO;

/**
 * 
 * @author DivyaPrakash JS on 04-07-2018
 */

public class PromotionDecisionPrint extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = null;
    try {
      vars = new VariablesSecureApp(request);
      String action = (request.getParameter("action") == null ? ""
          : request.getParameter("action"));
      String inpRecordId = (request.getParameter("inpRecordId") == null ? ""
          : request.getParameter("inpRecordId"));
      log4j.debug("action" + action);
      if (action.equals("PromotionDecisionVariable")) {
        try {
          FieldProvider[] data = null;
          log4j.debug("inpRecordId :" + inpRecordId);
          HashMap<String, Object> parameters = new HashMap<String, Object>();
          parameters.put("PromotionDecisionID", inpRecordId);
          parameters.put("HDeptname",
              EmployeesBusinessMissionDAO.getUserDepartment(vars.getUser()));
          String strReportName = "@basedesign@/sa/elm/ob/hcm/ad_reports/PromotionDecisionReport/PromotionDecisionReport.jrxml";
          String strOutput = "pdf";
          String strFileName = "PromotionDecisionReport";
          renderJR(vars, response, strReportName, strFileName, strOutput, parameters, data, null);
        } catch (Exception e) {
          log4j.error("Exception while downloading : ", e);
        }
      }
    } catch (final Exception e) {
      log4j.error("Error file", e);
      e.printStackTrace();
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }
}
