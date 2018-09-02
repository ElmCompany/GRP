package sa.elm.ob.hcm.ad_process.OvertimeDecree;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;

import sa.elm.ob.hcm.ad_reports.employeesbusinessmission.EmployeesBusinessMissionDAO;

/**
 * 
 * @author Sowmiya on 25-06-2018
 */

public class OvertimeDecreePrint extends HttpSecureAppServlet {

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
      if (action.equals("overtimeDecreeVariable")) {
        try {
          log4j.debug("inpRecordId :" + inpRecordId);
          HashMap<String, Object> parameters = new HashMap<String, Object>();
          parameters.put("overtimeDecree", inpRecordId);
          parameters.put("DepartmentId",
              EmployeesBusinessMissionDAO.getUserDepartment(vars.getUser()));
          String strReportName = "@basedesign@/sa/elm/ob/hcm/ad_reports/OvertimeDecreeReport/OvertimeDecreeReport.jrxml";
          String strOutput = "pdf";
          renderJR(vars, response, strReportName, strOutput, parameters, null, null);
        } catch (Exception e) {
          e.printStackTrace();
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