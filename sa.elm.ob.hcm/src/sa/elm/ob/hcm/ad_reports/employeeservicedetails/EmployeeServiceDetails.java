package sa.elm.ob.hcm.ad_reports.employeeservicedetails;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.ad.access.User;

import sa.elm.ob.hcm.ad_reports.employeesbusinessmission.EmployeesBusinessMissionDAO;
import sa.elm.ob.hcm.ad_reports.positionTransaction.PositionTransactionsDetailsDAO;
import sa.elm.ob.hcm.ad_reports.positionTransaction.PositionTransactionsDetailsVO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author kousalya on 19/04/2018
 *
 */
public class EmployeeServiceDetails extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private String jspPage = "../web/sa.elm.ob.hcm/jsp/employeeservicedetails/EmployeeServiceDetails.jsp";

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

      log4j.debug("action" + action);
      if (action.equals("")) {
        List<PositionTransactionsDetailsVO> depls = new ArrayList<PositionTransactionsDetailsVO>();
        request.setAttribute("EmployeeList",
            EmployeesBusinessMissionDAO.getEmployeesList(vars.getClient()));

        depls = PositionTransactionsDetailsDAO.getDepartmentCode(inpClientId);
        request.setAttribute("DepartmentList", depls);
        // Load FromDate
        String date = dateYearFormat.format(new Date());
        date = UtilityDAO.convertTohijriDate(date);
        request.setAttribute("inpEffectiveDate", date);

        // Localization support
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        request.getRequestDispatcher(jspPage).include(request, response);

      } else if (action.equals("Submit")) {
        String inpEmpId = request.getParameter("inpEmpId");
        String inpDeptId = request.getParameter("inpDeptId").equals("00") ? ""
            : request.getParameter("inpDeptId");
        String inpEffectDate = request.getParameter("inpEffectDate");
        // String departmentName = "";
        String deptListId = "";

        inpEffectDate = sa.elm.ob.hcm.util.UtilityDAO.convertToGregorian_tochar(inpEffectDate);
        // Department Name && childDept
        if (inpDeptId != null && !inpDeptId.equals("")) {
          /*
           * Organization org = OBDal.getInstance().get(Organization.class, inpDeptId);
           * departmentName = org.getName();
           */
          List<PositionTransactionsDetailsVO> depChildLs = new ArrayList<PositionTransactionsDetailsVO>();
          depChildLs = PositionTransactionsDetailsDAO.getChildDepartment(inpClientId, inpDeptId);
          for (PositionTransactionsDetailsVO vo : depChildLs) {
            if (deptListId.equals("")) {
              deptListId = "'" + vo.getOrgId() + "'";
            } else {
              deptListId += ",'" + vo.getOrgId() + "'";
            }
          }
        }

        User user = Utility.getObject(User.class, vars.getUser());
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("inpEmpId", inpEmpId);
        parameters.put("inpDepartmentName",
            EmployeesBusinessMissionDAO.getUserDepartment(vars.getUser()));
        parameters.put("inpDepartmentId", inpDeptId != null ? deptListId : "");
        parameters.put("inpEffectiveDate", inpEffectDate);
        parameters.put("inpClientId", inpClientId);
        parameters.put("inpUserName", user.getName());

        strReportName = "@basedesign@/sa/elm/ob/hcm/ad_reports/employeeservicedetails/EmployeeServiceDetails.jrxml";
        String strOutput = "pdf";

        renderJR(vars, response, strReportName, strOutput, parameters, null, null);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in EmployeeServiceDetails :", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}