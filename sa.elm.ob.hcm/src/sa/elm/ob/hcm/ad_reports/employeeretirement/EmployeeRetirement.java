package sa.elm.ob.hcm.ad_reports.employeeretirement;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ehcmgradeclass;
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
public class EmployeeRetirement extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private String jspPage = "../web/sa.elm.ob.hcm/jsp/employeeretirement/EmployeeRetirement.jsp";

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
      long AddingDays = 20 * 24 * 3600 * 1000;

      log4j.debug("action" + action);
      if (action.equals("")) {
        List<PositionTransactionsDetailsVO> depls = new ArrayList<PositionTransactionsDetailsVO>();
        request.setAttribute("inpEmployeeList",
            EmployeesBusinessMissionDAO.getEmployeesList(vars.getClient()));

        depls = PositionTransactionsDetailsDAO.getDepartmentCode(vars.getClient());
        request.setAttribute("inpDepartmentList", depls);

        java.util.List<EmployeeRetirementVO> employeeTypeList = new ArrayList<EmployeeRetirementVO>();
        OBQuery<ehcmgradeclass> port = OBDal.getInstance().createQuery(ehcmgradeclass.class,
            " as e  where e.client.id ='" + OBContext.getOBContext().getCurrentClient().getId()
                + "'  order by e.searchKey asc");
        List<ehcmgradeclass> destPortList = port.list();
        if (port != null && destPortList.size() > 0) {
          for (ehcmgradeclass destPort : destPortList) {
            EmployeeRetirementVO portVO = new EmployeeRetirementVO();
            portVO.setPortId(destPort.getId());
            portVO.setPortName(destPort.getSearchKey() + "-" + destPort.getName());
            employeeTypeList.add(portVO);
          }
        }
        request.setAttribute("inpEmployeeTypeList", employeeTypeList);

        java.util.List<EmployeeRetirementVO> employeeGradeList = new ArrayList<EmployeeRetirementVO>();
        OBQuery<ehcmgrade> port1 = OBDal.getInstance().createQuery(ehcmgrade.class,
            " as e  where e.client.id ='" + OBContext.getOBContext().getCurrentClient().getId()
                + "'  order by e.searchKey asc");
        List<ehcmgrade> destPortList1 = port1.list();
        if (port1 != null && destPortList1.size() > 0) {
          for (ehcmgrade destPort1 : destPortList1) {
            EmployeeRetirementVO portVO1 = new EmployeeRetirementVO();
            portVO1.setPortId(destPort1.getSearchKey());
            portVO1.setPortName(destPort1.getSearchKey() + "-" + destPort1.getCommercialName());
            employeeGradeList.add(portVO1);
          }
        }
        request.setAttribute("inpEmployeeGradeFromList", employeeGradeList);
        request.setAttribute("inpEmployeeGradeToList", employeeGradeList);

        java.util.List<EmployeeRetirementVO> lineManagerList = new ArrayList<EmployeeRetirementVO>();
        OBQuery<EHCMEmpSupervisor> port2 = OBDal.getInstance().createQuery(EHCMEmpSupervisor.class,
            " as e  where e.client.id ='" + OBContext.getOBContext().getCurrentClient().getId()
                + "'  order by e.name asc");
        List<EHCMEmpSupervisor> destPortList2 = port2.list();
        if (port2 != null && destPortList2.size() > 0) {
          for (EHCMEmpSupervisor destPort2 : destPortList2) {
            EmployeeRetirementVO portVO2 = new EmployeeRetirementVO();
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
        jsob = EmployeesBusinessMissionDAO
            .getDepartmentListUsingLeave(request.getParameter("inpEmpId"));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());
      } else if (action.equals("getEmpType")) {
        JSONObject list = new JSONObject();
        try {
          JSONArray jsonArray = new JSONArray();
          list = EmployeeRetirementDAO.getEmpTypeList(request.getParameter("inpEmpId"),
              getConnection());

        } catch (final Exception e) {
          log4j.error("Exception in EmployeeRetirement java file - getEmpType : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(list.toString());
        }
      } else if (action.equals("getEmpGrade")) {
        JSONObject list = new JSONObject();
        try {
          JSONArray jsonArray = new JSONArray();
          list = EmployeeRetirementDAO.getEmpGradeList(request.getParameter("inpEmpId"),
              getConnection());

        } catch (final Exception e) {
          log4j.error("Exception in EmployeeRetirement java file - getEmpGrade : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(list.toString());
        }
      } else if (action.equals("getLineManager")) {
        JSONObject list = new JSONObject();
        try {
          JSONArray jsonArray = new JSONArray();
          list = EmployeeRetirementDAO.getLineManagerList(request.getParameter("inpEmpId"),
              getConnection());

        } catch (final Exception e) {
          log4j.error("Exception in EmployeeRetirement java file - getLineManager : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(list.toString());
        }
      } else if (action.equals("getGender")) {
        JSONObject list = new JSONObject();
        try {
          JSONArray jsonArray = new JSONArray();
          list = EmployeeRetirementDAO.getGenderList(request.getParameter("inpEmpId"),
              getConnection());

        } catch (final Exception e) {
          log4j.error("Exception in EmployeeRetirement java file - getGender : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(list.toString());
        }
      } else if (action.equals("Submit")) {
        String inpEmpId = request.getParameter("inpEmpId").equals("0") ? "0"
            : request.getParameter("inpEmpId");
        String inpDeptId = request.getParameter("inpDeptId").equals("0") ? "0"
            : request.getParameter("inpDeptId");
        String inpEmpTypeId = request.getParameter("inpEmpTypeId").equals("0") ? "0"
            : request.getParameter("inpEmpTypeId");
        String inpEmpGradeFromId = request.getParameter("inpEmpGradeFromId").equals("0") ? "0"
            : request.getParameter("inpEmpGradeFromId");
        String inpEmpGradeToId = request.getParameter("inpEmpGradeToId").equals("0") ? "0"
            : request.getParameter("inpEmpGradeToId");
        String inpLineManagerId = request.getParameter("inpLineManagerId").equals("0") ? ""
            : request.getParameter("inpLineManagerId");
        String inpGenderId = request.getParameter("inpGenderId").equals("0") ? "0"
            : request.getParameter("inpGenderId");
        SimpleDateFormat dateFormatTem = new SimpleDateFormat("dd-MM-yyyy");

        EmployeeRetirementDAO.getLineManagerList(request.getParameter("inpEmpId"), getConnection());
        String DateTemp = EmployeeRetirementDAO.get30Days(request.getParameter("inpEndDateH"), 30,
            getConnection());
        // System.out.println("--------------------dao converted date------------>>>>>>>"+DateTemp);

        Date AddedDate = dateYearFormat
            .parse(sa.elm.ob.hcm.util.UtilityDAO.convertToGregorian_tochar(DateTemp));

        /*
         * Date inpToDate = dateYearFormat.parse(sa.elm.ob.hcm.util.UtilityDAO
         * .convertToGregorian_tochar(request.getParameter("todate")));
         */

        // AddedDate.setTime(AddedDate.getTime()+20 * 24 * 3600 * 1000);
        // AddedDate.setTime(AddedDate.getTime()+10 * 24 * 3600 * 1000);
        // AddedDate.setTime(AddedDate.getTime());

        String inpEndDateH = dateYearFormat.format(AddedDate);

        String inpEndDate = inpEndDateH;
        /*
         * String departmentName = ""; String deptListId = "";
         */

        inpEndDate = sa.elm.ob.hcm.util.UtilityDAO.convertToGregorian_tochar(inpEndDate);
        String inpCurrentDate = sa.elm.ob.hcm.util.UtilityDAO
            .convertToGregorian_tochar(request.getParameter("inpEndDateH"));

        // Department Name && childDept

        /*
         * log4j.debug("inpEmpId>" + inpEmpId); log4j.debug("inpDeptId>" + inpDeptId);
         * log4j.debug("inpEmpTypeId>" + inpEmpTypeId); log4j.debug("inpEmpGradeFromId>" +
         * inpEmpGradeFromId); log4j.debug("inpEmpGradeToId>" + inpEmpGradeToId);
         * log4j.debug("inpLineManagerId>" + inpLineManagerId); log4j.debug("inpGenderId>" +
         * inpGenderId); // log4j.debug("inpEndDate>" + inpEndDate); log4j.debug("inpEndDateH>" +
         * inpEndDateH); log4j.debug("inpCurrentDate>" + inpCurrentDate); log4j.debug("inpClientId>"
         * + vars.getClient());
         */

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("inpDateTemp", DateTemp);

        parameters.put("inpEmpId",
            (inpEmpId == null || inpEmpId.equals("0") || inpEmpId.equals("")) ? "0" : inpEmpId);
        parameters.put("inpDepartmentId",
            (inpDeptId == null || inpDeptId.equals("0") || inpDeptId.equals("")) ? "0" : inpDeptId);
        parameters.put("inpEmpTypeId",
            (inpEmpTypeId == null || inpEmpTypeId.equals("0") || inpEmpTypeId.equals("")) ? "0"
                : inpEmpTypeId);
        parameters.put("inpEmpGradeFromId",
            (inpEmpGradeFromId == null || inpEmpGradeFromId.equals("0")
                || inpEmpGradeFromId.equals("")) ? "0" : inpEmpGradeFromId);
        parameters.put("inpEmpGradeToId",
            (inpEmpGradeToId == null || inpEmpGradeToId.equals("0") || inpEmpGradeToId.equals(""))
                ? "0"
                : inpEmpGradeToId);
        parameters.put("inpLineManagerId", (inpLineManagerId == null || inpLineManagerId.equals("0")
            || inpLineManagerId.equals("")) ? "0" : inpLineManagerId);
        parameters.put("inpGenderId",
            (inpGenderId == null || inpGenderId.equals("0") || inpGenderId.equals("")) ? "0"
                : inpGenderId);
        parameters.put("inpEndDateH", inpEndDateH);
        parameters.put("inpCurrentDate", inpCurrentDate);

        // parameters.put("inpEndDateH", AddedDate);
        parameters.put("inpClientId", vars.getClient());
        parameters.put("inpDepartmentName",
            EmployeesBusinessMissionDAO.getUserDepartment(vars.getUser()));
        parameters.put("inpClient", vars.getClient());

        strReportName = "@basedesign@/sa/elm/ob/hcm/ad_reports/employeeretirement/EmployeeRetirement.jrxml";
        String format = "xls";
        String strOutput = format;

        renderJR(vars, response, strReportName, strOutput, parameters, null, null);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in EmployeeRetirement :", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}