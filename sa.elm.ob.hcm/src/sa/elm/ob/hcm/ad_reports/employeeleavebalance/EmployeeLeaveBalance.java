package sa.elm.ob.hcm.ad_reports.employeeleavebalance;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;

import sa.elm.ob.hcm.ad_reports.employeesbusinessmission.EmployeesBusinessMissionDAO;
import sa.elm.ob.hcm.ad_reports.positionTransaction.PositionTransactionsDetailsDAO;
import sa.elm.ob.hcm.ad_reports.positionTransaction.PositionTransactionsDetailsVO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author Mouli.K on 21/06/2018
 *
 */
public class EmployeeLeaveBalance extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private String jspPage = "../web/sa.elm.ob.hcm/jsp/employeeleavebalance/EmployeeLeaveBalance.jsp";

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
      DateFormat dateYearFormat = Utility.YearFormat;

      log4j.debug("action" + action);
      if (action.equals("")) {
        List<PositionTransactionsDetailsVO> depls = new ArrayList<PositionTransactionsDetailsVO>();
        request.setAttribute("EmployeeList",
            EmployeesBusinessMissionDAO.getEmployeesList(vars.getClient()));
        request.setAttribute("AbsenceTypeList",
            EmployeeLeaveBalanceDAO.getAbsenceType(vars.getClient()));

        depls = PositionTransactionsDetailsDAO.getDepartmentCode(vars.getClient());
        request.setAttribute("DepartmentList", depls);

        // Load FromDate
        String date = dateYearFormat.format(new Date());
        date = UtilityDAO.convertTohijriDate(date);
        request.setAttribute("inpDate", date);

        // Localization support
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        request.getRequestDispatcher(jspPage).include(request, response);
      } else if (action.equals("getDepartmentList")) {
        JSONObject jsob = new JSONObject();
        jsob = EmployeesBusinessMissionDAO
            .getDepartmentListUsingLeave(request.getParameter("empId"));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());
      } else if (action.equals("Submit")) {
        String inpEmpId = request.getParameter("inpEmpId").equals("0") ? ""
            : request.getParameter("inpEmpId");
        String inpDeptId = request.getParameter("inpDeptId");
        String inpAbsenceTypeId = request.getParameter("inpAbsenceTypeId").equals("0") ? ""
            : request.getParameter("inpAbsenceTypeId");
        String inpEndDateH = request.getParameter("inpEndDateH");
        String inpEndDate = inpEndDateH;
        String departmentName = "";
        String deptListId = "";

        inpEndDate = sa.elm.ob.hcm.util.UtilityDAO.convertToGregorian_tochar(inpEndDate);
        // Department Name && childDept

        log4j.debug("inpEmpId>" + inpEmpId);
        log4j.debug("inpDeptId>" + deptListId);
        log4j.debug("inpAbsenceTypeId>" + inpAbsenceTypeId);
        log4j.debug("inpEndDate>" + inpEndDate);
        log4j.debug("inpEndDateH>" + inpEndDateH);
        log4j.debug("inpClientId>" + vars.getClient());

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("inpEmpId", inpEmpId);
        parameters.put("inpDepartmentName", departmentName);
        parameters.put("inpDepartmentId",
            (inpDeptId == null || inpDeptId.equals("00")) ? "" : inpDeptId);
        parameters.put("inpAbsenceTypeId", inpAbsenceTypeId);
        parameters.put("inpEndDate", inpEndDate);
        parameters.put("inpEndDateH", inpEndDateH);
        parameters.put("inpClientId", vars.getClient());
        parameters.put("inpDepartmentName",
            EmployeesBusinessMissionDAO.getUserDepartment(vars.getUser()));

        strReportName = "@basedesign@/sa/elm/ob/hcm/ad_reports/employeeleavebalance/BalanceLeaveReport.jrxml";
        String strOutput = "pdf";

        renderJR(vars, response, strReportName, strOutput, parameters, null, null);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in EmployeeLeaveBalance :", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}