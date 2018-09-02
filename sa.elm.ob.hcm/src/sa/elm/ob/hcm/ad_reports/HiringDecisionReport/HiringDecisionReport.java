package sa.elm.ob.hcm.ad_reports.HiringDecisionReport;

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

public class HiringDecisionReport extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  // private String jspPage =
  // "../web/sa.elm.ob.hcm/jsp/HiringDecisionReport/HiringDecisionReport.jsp";

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
        String isJoiningworkReq = null;

        if (StringUtils.isNotEmpty(request.getParameter("inpIsjoiningWorkReq"))
            && request.getParameter("inpIsjoiningWorkReq").equals("on")) {
          isJoiningworkReq = "Y";
        } else {
          isJoiningworkReq = "N";
        }

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("inpEmployeeId", inpEmployeeId);
        parameters.put("isJoiningworkReq", isJoiningworkReq);
        parameters.put("inpDepartmentName",
            EmployeesBusinessMissionDAO.getUserDepartment(vars.getUser()));

        strReportName = "@basedesign@/sa/elm/ob/hcm/ad_reports/HiringDecisionReport/HiringDecisionReport.jrxml";
        String strOutput = "pdf";

        renderJR(vars, response, strReportName, strOutput, parameters, null, null);
      }

    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in HiringDecisionReport :", e);
    }

  }
}