package sa.elm.ob.hcm.ad_reports.SecondmentDuringPeriod;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;

import sa.elm.ob.hcm.ad_reports.employeeretirement.EmployeeRetirementDAO;
import sa.elm.ob.hcm.ad_reports.employeesbusinessmission.EmployeesBusinessMissionDAO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * @author Rashika.V.S on 24/07/2018
 */

public class SecondmentDuringPeriod extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;
  private String jspPage = "../web/sa.elm.ob.hcm/jsp/SecondmentDuringPeriod/SecondmentDuringPeriod.jsp";

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
      DateFormat dateYearFormat = Utility.YearFormat;
      log4j.debug("action");
      if (action.equals("")) {
        // Load Employee
        request.setAttribute("inpEmployeeList",
            EmployeesBusinessMissionDAO.getEmployeesList(inpClientId));
        // Load Department
        request.setAttribute("inpDeptList",
            SecondmentDuringPeriodDAO.getDepartmentCode(inpClientId));
        // Load Employee Type
        request.setAttribute("inpEmployeeTypeList",
            SecondmentDuringPeriodDAO.getEmployeeType(inpClientId));
        // Load Grade From and Grade To
        request.setAttribute("inpGradeFromList",
            SecondmentDuringPeriodDAO.getEmployeeGrade(inpClientId));
        request.setAttribute("inpGradeToList",
            SecondmentDuringPeriodDAO.getEmployeeGrade(inpClientId));
        // Load Line Manager
        request.setAttribute("inpLineManagerList",
            SecondmentDuringPeriodDAO.getLineManager(inpClientId));
        String date = dateYearFormat.format(new Date());
        date = UtilityDAO.convertTohijriDate(date);
        request.setAttribute("inpDate", date);
        // Localization support
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        request.getRequestDispatcher(jspPage).include(request, response);
      } else if (action.equals("getEmployeesList")) {
        JSONObject jsob = new JSONObject();
        jsob = SecondmentDuringPeriodDAO.getEmployeesList(vars.getClient(),
            request.getParameter("searchTerm"), Integer.parseInt(request.getParameter("pageLimit")),
            Integer.parseInt(request.getParameter("page")));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());

      }

      else if (action.equals("getDepartmentList")) {
        JSONObject jsob = new JSONObject();
        jsob = EmployeesBusinessMissionDAO
            .getDepartmentListUsingLeave(request.getParameter("inpEmpId"));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());
      } else if (action.equals("getEmpType")) {
        JSONObject list = new JSONObject();
        try {
          list = EmployeeRetirementDAO.getEmpTypeList(request.getParameter("inpEmpId"),
              getConnection());

        } catch (final Exception e) {
          log4j.error("Exception in Secondment during Period java file - getEmpType : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(list.toString());
        }
      } else if (action.equals("getEmpGrade")) {
        JSONObject list = new JSONObject();
        try {
          list = EmployeeRetirementDAO.getEmpGradeList(request.getParameter("inpEmpId"),
              getConnection());

        } catch (final Exception e) {
          log4j.error("Exception in Secondment during Period java file - getEmpGrade : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(list.toString());
        }
      } else if (action.equals("getLineManager")) {
        JSONObject list = new JSONObject();
        try {
          list = EmployeeRetirementDAO.getLineManagerList(request.getParameter("inpEmpId"),
              getConnection());

        } catch (final Exception e) {
          log4j.error("Exception in Secondment during Period java file - getLineManager : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(list.toString());
        }
      } else if (action.equals("getGender")) {
        JSONObject list = new JSONObject();
        try {
          list = EmployeeRetirementDAO.getGenderList(request.getParameter("inpEmpId"),
              getConnection());

        } catch (final Exception e) {
          log4j.error("Exception in Secondment during Period java file - getGender : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(list.toString());
        }
      } else if (action.equals("Submit")) {
        String inpEmpId = request.getParameter("inpempId").equals("0") ? null
            : request.getParameter("inpempId");
        String inpDeptId = request.getParameter("inpdeptId").equals("0") ? null
            : request.getParameter("inpdeptId");
        String inpEmpTypeId = request.getParameter("inpempTypeId").equals("0") ? null
            : request.getParameter("inpempTypeId");
        String inpEmpGradeFromId = request.getParameter("inpgradeFromId").equals("0") ? "0"
            : request.getParameter("inpgradeFromId");
        String inpEmpGradeToId = request.getParameter("inpgradeToId").equals("0") ? "0"
            : request.getParameter("inpgradeToId");
        String inpLineManagerId = request.getParameter("inpLineManagerId").equals("0") ? null
            : request.getParameter("inpLineManagerId");
        String inpGenderId = request.getParameter("inpgenderId").equals("0") ? null
            : request.getParameter("inpgenderId");
        log4j.debug("inpEmpGradeToId" + inpEmpGradeToId);
        String fromDate = request.getParameter("fromdate");
        String toDate = request.getParameter("todate");
        String inpFromDate = sa.elm.ob.hcm.util.UtilityDAO
            .convertToGregorian_tochar(request.getParameter("fromdate"));
        String inpToDate = sa.elm.ob.hcm.util.UtilityDAO
            .convertToGregorian_tochar(request.getParameter("todate"));

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("inpEmpId",
            (inpEmpId == null || inpEmpId.equals("0") || inpEmpId.equals("")) ? null : inpEmpId);
        parameters.put("inpDepartmentId",
            (inpDeptId == null || inpDeptId.equals("0") || inpDeptId.equals("")) ? null
                : inpDeptId);
        parameters.put("inpEmpTypeId",
            (inpEmpTypeId == null || inpEmpTypeId.equals("0") || inpEmpTypeId.equals("")) ? null
                : inpEmpTypeId);
        parameters.put("inpEmpGradeFromId",
            (inpEmpGradeFromId == null || inpEmpGradeFromId.equals("0")
                || inpEmpGradeFromId.equals("")) ? "0" : inpEmpGradeFromId);
        parameters.put("inpEmpGradeToId",
            (inpEmpGradeToId == null || inpEmpGradeToId.equals("0") || inpEmpGradeToId.equals(""))
                ? "0"
                : inpEmpGradeToId);
        parameters.put("inpLineManagerId", (inpLineManagerId == null || inpLineManagerId.equals("0")
            || inpLineManagerId.equals("")) ? null : inpLineManagerId);
        parameters.put("inpGenderId",
            (inpGenderId == null || inpGenderId.equals("0") || inpGenderId.equals("")) ? null
                : inpGenderId);
        parameters.put("inpFromDate", inpFromDate);
        parameters.put("inpToDate", inpToDate);

        parameters.put("inpClientId", vars.getClient());
        parameters.put("inpDepartmentName",
            EmployeesBusinessMissionDAO.getUserDepartment(vars.getUser()));
        parameters.put("fromDate", fromDate);
        parameters.put("toDate", toDate);

        strReportName = "@basedesign@/sa/elm/ob/hcm/ad_reports/secondmentPeriod/SecondmentDuringPeriod.jrxml";
        String strOutput = "pdf";

        renderJR(vars, response, strReportName, strOutput, parameters, null, null);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in Secondment During Period: ", e);
    }

  }
}
