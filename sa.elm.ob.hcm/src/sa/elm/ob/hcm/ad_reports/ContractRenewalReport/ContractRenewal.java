package sa.elm.ob.hcm.ad_reports.ContractRenewalReport;

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
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;

import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.ad_reports.employeesbusinessmission.EmployeesBusinessMissionDAO;
import sa.elm.ob.hcm.ad_reports.positionTransaction.PositionTransactionsDetailsDAO;
import sa.elm.ob.hcm.ad_reports.positionTransaction.PositionTransactionsDetailsVO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author DivyaPrakash J.S on 20/07/2018
 *
 */
public class ContractRenewal extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private String jspPage = "../web/sa.elm.ob.hcm/jsp/ContractRenewalReport/ContractRenewal.jsp";

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
      FieldProvider[] data = null;
      if (action.equals("")) {
        List<PositionTransactionsDetailsVO> depls = new ArrayList<PositionTransactionsDetailsVO>();
        request.setAttribute("inpEmployeeList",
            ContractRenewalDAO.getContractEmployeesList(vars.getClient()));

        depls = PositionTransactionsDetailsDAO.getDepartmentCode(vars.getClient());
        request.setAttribute("inpDepartmentList", depls);

        java.util.List<ContractRenewalVO> lineManagerList = new ArrayList<ContractRenewalVO>();
        OBQuery<EHCMEmpSupervisor> port2 = OBDal.getInstance().createQuery(EHCMEmpSupervisor.class,
            " as e  where e.client.id ='" + OBContext.getOBContext().getCurrentClient().getId()
                + "'  order by e.name asc");
        List<EHCMEmpSupervisor> destPortList2 = port2.list();
        if (port2 != null && destPortList2.size() > 0) {
          for (EHCMEmpSupervisor destPort2 : destPortList2) {
            ContractRenewalVO portVO2 = new ContractRenewalVO();
            portVO2.setPortId(destPort2.getId());
            portVO2.setPortName(destPort2.getEmployee().getSearchKey() + "-"
                + destPort2.getEmployee().getArabicfullname());
            lineManagerList.add(portVO2);
          }
        }
        request.setAttribute("inpLineManagerList", lineManagerList);

        String date = dateYearFormat.format(new Date());
        date = UtilityDAO.convertTohijriDate(date);
        request.setAttribute("inpDate", date);

        // Localization support
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        request.getRequestDispatcher(jspPage).include(request, response);
      } else if (action.equals("getDepartmentList")) {
        JSONObject jsob = new JSONObject();
        if (request.getParameter("empId").equals("0")) {
          jsob = ContractRenewalDAO.getDepartmentList(vars.getClient());
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(jsob.toString());
        } else {
          jsob = EmployeesBusinessMissionDAO.getDepartmentList(vars.getClient(),
              request.getParameter("empId"));
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(jsob.toString());
        }

      } else if (action.equals("getLineManagerList")) {
        if (request.getParameter("empId").equals("0")) {
          JSONObject jsob = new JSONObject();
          jsob = ContractRenewalDAO.getLineManagerlistByNullEmployee(request.getParameter("empId"));
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(jsob.toString());
        } else {
          JSONObject jsob = new JSONObject();
          jsob = ContractRenewalDAO.getLineManagerList(request.getParameter("empId"));
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(jsob.toString());
        }
      } else if (action.equals("Submit")) {

        String inpEmpId = request.getParameter("inpEmpId").equals("0") ? ""
            : request.getParameter("inpEmpId");
        String inpDeptId = request.getParameter("inpDeptId");
        String inpStartdateH = request.getParameter("inpStartdateH");
        String inpEndDateH = request.getParameter("inpEndDateH");
        String inpLinemanagerId = request.getParameter("inpLinemanagerId");
        String inpEndDate = inpEndDateH;
        String departmentName = "";
        String deptListId = "";
        String inpStartdate = sa.elm.ob.hcm.util.UtilityDAO
            .convertToGregorian_tochar(inpStartdateH);
        inpEndDate = sa.elm.ob.hcm.util.UtilityDAO.convertToGregorian_tochar(inpEndDateH);
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("inpEmpId", inpEmpId);
        parameters.put("inpDepartmentId", inpDeptId);
        parameters.put("inpLinemanagerId", inpLinemanagerId);
        parameters.put("inpEndDateH", inpEndDate);
        parameters.put("inpStartdateH", inpStartdate);
        parameters.put("inpDisplayDate", inpStartdateH);
        parameters.put("inpClientId", vars.getClient());
        parameters.put("HDeptname", EmployeesBusinessMissionDAO.getUserDepartment(vars.getUser()));
        strReportName = "@basedesign@/sa/elm/ob/hcm/ad_reports/ContractRenewalReport/ContractRenewalReport.jrxml";
        String strOutput = "xls";
        String strFileName = "ContractRenewalReport";

        renderJR(vars, response, strReportName, strFileName, strOutput, parameters, data, null);

      }
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in ContractRenewal :", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}