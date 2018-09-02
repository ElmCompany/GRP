package sa.elm.ob.hcm.ad_process.AbsenceAccrual;

import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;

import sa.elm.ob.hcm.ad_reports.absenceinperiod.AbsenceInPeriodDAO;
import sa.elm.ob.hcm.ad_reports.employeesbusinessmission.EmployeesBusinessMissionDAO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author divya on 17/07/2018
 *
 */
public class AbsenceAccruals extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private String jspPage = "../web/sa.elm.ob.hcm/jsp/absenceAccruals/AbsenceAccruals.jsp";

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    try {
      OBContext.setAdminMode();
      String action = (request.getParameter("inpAction") == null ? ""
          : request.getParameter("inpAction"));
      VariablesSecureApp vars = new VariablesSecureApp(request);
      DateFormat dateYearFormat = Utility.YearFormat;
      AbsenceAccrualDAO absenceAccrualDAO = new AbsenceAccrualDAOImpl();
      Connection conn = getConnection();
      DateFormat yearFormat = Utility.YearFormat;
      if (action.equals("")) {
        request.setAttribute("EmployeeList",
            EmployeesBusinessMissionDAO.getEmployeesList(vars.getClient()));
        request.setAttribute("AbsenceTypeList",
            AbsenceInPeriodDAO.getAbsenceType(vars.getClient()));
        String date = dateYearFormat.format(new Date());
        date = UtilityDAO.convertTohijriDate(date);
        request.setAttribute("inpCalculationDate", date);

        // Localization support
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        request.getRequestDispatcher(jspPage).include(request, response);
      } else if (action.equals("getAccrualList")) {
        JSONObject result = new JSONObject();
        result.put("page", "0");
        result.put("total", "0");
        result.put("records", "0");
        result.put("rows", new JSONArray());
        String searchFlag = request.getParameter("_search");
        JSONObject searchAttr = new JSONObject();
        searchAttr.put("rows", request.getParameter("rows").toString());
        searchAttr.put("page", request.getParameter("page").toString());
        searchAttr.put("search", searchFlag);
        searchAttr.put("sortName", request.getParameter("sidx").toString());
        searchAttr.put("sortType", request.getParameter("sord").toString());
        searchAttr.put("offset", "0");

        result = absenceAccrualDAO.getAbsenceAccrualList(vars.getClient(),
            request.getParameter("inpEmployee"), request.getParameter("inpAbsenceType"),
            request.getParameter("inpCalculationDate"), searchAttr, conn);
        // Localization support
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(result.toString());
      } else if (action.equals("getEmpDetails")) {
        JSONObject result = new JSONObject();
        String employeeId = request.getParameter("employeeId");

        result = absenceAccrualDAO.getEmployeeDetails(employeeId, vars.getLanguage());
        // Localization support
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(result.toString());
      }
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in absence Accruals :", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}