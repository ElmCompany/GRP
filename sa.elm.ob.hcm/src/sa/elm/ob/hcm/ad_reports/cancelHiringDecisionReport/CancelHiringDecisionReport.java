package sa.elm.ob.hcm.ad_reports.cancelHiringDecisionReport;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;

import sa.elm.ob.hcm.ad_reports.employeesbusinessmission.EmployeesBusinessMissionDAO;

/**
 * 
 * @author poongodi on 14/06/2018
 *
 */
public class CancelHiringDecisionReport extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    try {
      OBContext.setAdminMode();
      String strReportName = "";
      String action = (request.getParameter("inpAction") == null ? ""
          : request.getParameter("inpAction"));
      VariablesSecureApp vars = new VariablesSecureApp(request);
      String inpClientId = "";
      inpClientId = vars.getClient();

      if (action.equals("")) {

        // Localization support
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        // request.getRequestDispatcher(jspPage).include(request, response);

      } else if (action.equals("Submit")) {
        String inpEmployeeId = request.getParameter("inpEmployeeId");
        String inpdecisionFlow = null;

        if (StringUtils.isNotEmpty(request.getParameter("inpdecisionFlow"))
            && request.getParameter("inpdecisionFlow").equals("on")) {
          inpdecisionFlow = "Y";
        } else {
          inpdecisionFlow = "N";
        }

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("inpEmployeeId", inpEmployeeId);
        parameters.put("inpdecisionFlow", inpdecisionFlow);
        parameters.put("inpclientId", inpClientId);
        parameters.put("HDeptname", EmployeesBusinessMissionDAO.getUserDepartment(vars.getUser()));
        strReportName = "@basedesign@/sa/elm/ob/hcm/ad_reports/cancelHiringDecisionReport/CancelHiringDecisionReport.jrxml";
        String strOutput = "pdf";

        renderJR(vars, response, strReportName, strOutput, parameters, null, null);

      }

    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in CancelHiringDecisionReport :", e);
    }
  }
}