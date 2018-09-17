package sa.elm.ob.hcm.ad_reports.EmployeesInformationReport;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.SQLQuery;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;

import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ehcmgradeclass;
import sa.elm.ob.hcm.ad_reports.employeesbusinessmission.EmployeesBusinessMissionDAO;
import sa.elm.ob.hcm.ad_reports.positionTransaction.PositionTransactionsDetailsDAO;
import sa.elm.ob.hcm.ad_reports.positionTransaction.PositionTransactionsDetailsVO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class EmployeesInformationReport extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private String jspPage = "../web/sa.elm.ob.hcm/jsp/EmployeesInformationReport/EmployeesInformationReport.jsp";
  String EMPLOYEE_STATUS_REFERENCE_ID = "57889F5818294AE6B371B3FD3369E8B3";

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
        /*
         * request.setAttribute("inpEmployeeList",
         * EmployeesBusinessMissionDAO.getEmployeesList(vars.getClient()));
         */

        depls = PositionTransactionsDetailsDAO.getDepartmentCode(vars.getClient());
        request.setAttribute("inpDepartmentList", depls);

        java.util.List<EmployeesInformationReportVO> employeeTypeList = new ArrayList<EmployeesInformationReportVO>();
        OBQuery<ehcmgradeclass> port = OBDal.getInstance().createQuery(ehcmgradeclass.class,
            " as e  where e.client.id ='" + OBContext.getOBContext().getCurrentClient().getId()
                + "'  order by e.searchKey asc");
        List<ehcmgradeclass> destPortList = port.list();
        if (port != null && destPortList.size() > 0) {
          for (ehcmgradeclass destPort : destPortList) {
            EmployeesInformationReportVO portVO = new EmployeesInformationReportVO();
            portVO.setPortId(destPort.getId());
            portVO.setPortName(destPort.getSearchKey() + "-" + destPort.getName());
            employeeTypeList.add(portVO);
          }
        }
        request.setAttribute("inpEmployeeTypeList", employeeTypeList);

        java.util.List<EmployeesInformationReportVO> employeeGradeList = new ArrayList<EmployeesInformationReportVO>();
        OBQuery<ehcmgrade> port1 = OBDal.getInstance().createQuery(ehcmgrade.class,
            " as e  where e.client.id ='" + OBContext.getOBContext().getCurrentClient().getId()
                + "'  order by e.searchKey asc");
        List<ehcmgrade> destPortList1 = port1.list();
        if (port1 != null && destPortList1.size() > 0) {
          for (ehcmgrade destPort1 : destPortList1) {
            EmployeesInformationReportVO portVO1 = new EmployeesInformationReportVO();
            portVO1.setPortId(destPort1.getId());
            portVO1.setPortName(destPort1.getSearchKey() + "-" + destPort1.getCommercialName());
            employeeGradeList.add(portVO1);
          }
        }
        request.setAttribute("inpEmployeeGradeFromList", employeeGradeList);
        request.setAttribute("inpEmployeeGradeToList", employeeGradeList);

        java.util.List<EmployeesInformationReportVO> lineManagerList = new ArrayList<EmployeesInformationReportVO>();
        OBQuery<EHCMEmpSupervisor> port2 = OBDal.getInstance().createQuery(EHCMEmpSupervisor.class,
            " as e  where e.client.id ='" + OBContext.getOBContext().getCurrentClient().getId()
                + "'  order by e.name asc");
        List<EHCMEmpSupervisor> destPortList2 = port2.list();
        if (port2 != null && destPortList2.size() > 0) {
          for (EHCMEmpSupervisor destPort2 : destPortList2) {
            EmployeesInformationReportVO portVO2 = new EmployeesInformationReportVO();
            portVO2.setPortId(destPort2.getId());
            portVO2.setPortName(destPort2.getEmployee().getSearchKey() + "-"
                + destPort2.getEmployee().getArabicfullname());
            lineManagerList.add(portVO2);
          }
        }
        request.setAttribute("inpLineManagerList", lineManagerList);

        java.util.List<EmployeesInformationReportVO> empstatus = new ArrayList<EmployeesInformationReportVO>();
        SQLQuery Query = OBDal.getInstance().getSession().createSQLQuery(
            " select list.ad_ref_list_id as listId ,list.name as  name,list.value from  ad_ref_list  list"
                + " where  list.ad_reference_id = :ad_reference_id  and list.isactive='Y'"
                + " and list.value in ('T','PR','H','SCTR','ABS','BM','PRT') "
                + "   order by list.name ");
        Query.setParameter("ad_reference_id", EMPLOYEE_STATUS_REFERENCE_ID);
        if (Query.list().size() > 0) {
          for (Object o : Query.list()) {
            Object[] row = (Object[]) o;
            EmployeesInformationReportVO portVO2 = new EmployeesInformationReportVO();
            portVO2.setPortId(Utility.nullToEmpty(row[2].toString()));
            portVO2.setPortName(Utility.nullToEmpty(row[2].toString()) + "-"
                + Utility.nullToEmpty(row[1].toString()));

            empstatus.add(portVO2);
          }
        }
        request.setAttribute("inpEmpStatusList", empstatus);

        String date = dateYearFormat.format(new Date());
        date = UtilityDAO.convertTohijriDate(date);
        request.setAttribute("inpDate", date);

        // Localization support
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        request.getRequestDispatcher(jspPage).include(request, response);
      } else if (action.equals("getEmployeesList")) {
        JSONObject jsob = new JSONObject();
        jsob = EmployeesInformationReportDAO.getEmployeesList(vars.getClient(),
            request.getParameter("searchTerm"), Integer.parseInt(request.getParameter("pageLimit")),
            Integer.parseInt(request.getParameter("page")));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());

      } else if (action.equals("getDepartmentList")) {
        log4j.debug("Dept");
        JSONObject jsob = new JSONObject();
        jsob = EmployeesBusinessMissionDAO
            .getDepartmentListUsingLeave(request.getParameter("inpEmpId"));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());
      } else if (action.equals("getEmpType")) {
        log4j.debug("emptype");
        JSONObject list = new JSONObject();
        try {
          JSONArray jsonArray = new JSONArray();
          list = EmployeesInformationReportDAO.getEmpTypeList(request.getParameter("inpEmpId"),
              getConnection());

        } catch (final Exception e) {
          log4j.error("Exception in EmployeesInformationReport java file - getEmpType : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(list.toString());
        }
      } else if (action.equals("getEmpStatus")) {
        log4j.debug("EmpStatus");
        JSONObject list = new JSONObject();
        try {
          JSONArray jsonArray = new JSONArray();
          list = EmployeesInformationReportDAO.getEmpStatusList(request.getParameter("inpEmpId"),
              getConnection(), vars.getLanguage());

        } catch (final Exception e) {
          log4j.error("Exception in EmployeesInformationReport java file - getEmpStatus : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(list.toString());
        }
      } else if (action.equals("getEmpGrade")) {
        log4j.debug("EmpGrade");
        JSONObject list = new JSONObject();
        try {
          JSONArray jsonArray = new JSONArray();
          list = EmployeesInformationReportDAO.getEmpGradeList(request.getParameter("inpEmpId"),
              getConnection());

        } catch (final Exception e) {
          log4j.error("Exception in EmployeesInformationReport java file - getEmpGrade : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(list.toString());
        }
      } else if (action.equals("getLineManager")) {
        log4j.debug("line");
        JSONObject list = new JSONObject();
        try {
          JSONArray jsonArray = new JSONArray();
          list = EmployeesInformationReportDAO.getLineManagerList(request.getParameter("inpEmpId"),
              getConnection());

        } catch (final Exception e) {
          log4j.error("Exception in EmployeesInformationReport java file - getLineManager : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(list.toString());
        }
      } else if (action.equals("getGender")) {
        log4j.debug("gender");
        JSONObject list = new JSONObject();
        try {
          JSONArray jsonArray = new JSONArray();
          list = EmployeesInformationReportDAO.getGenderList(request.getParameter("inpEmpId"),
              getConnection());

        } catch (final Exception e) {
          log4j.error("Exception in EmployeesInformationReport java file - getGender : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(list.toString());
        }
      } else if (action.equals("Submit")) {
        String inpEmpId = request.getParameter("inpEmpId").equals("0") ? null
            : request.getParameter("inpEmpId");
        String inpDeptId = request.getParameter("inpDeptId").equals("0") ? null
            : request.getParameter("inpDeptId");
        String inpEmpTypeId = request.getParameter("inpEmpTypeId").equals("0") ? null
            : request.getParameter("inpEmpTypeId");
        String inpEmpGradeFromId = request.getParameter("inpEmpGradeFromId").equals("0") ? null
            : request.getParameter("inpEmpGradeFromId");

        ehcmgrade gradefrom = null;
        ehcmgrade gradeto = null;

        if (inpEmpGradeFromId != null) {
          gradefrom = OBDal.getInstance().get(ehcmgrade.class, inpEmpGradeFromId);
        }

        String inpEmpGradeToId = request.getParameter("inpEmpGradeToId").equals("0") ? null
            : request.getParameter("inpEmpGradeToId");
        if (inpEmpGradeToId != null) {
          gradeto = OBDal.getInstance().get(ehcmgrade.class, inpEmpGradeToId);
        }
        String inpEmpStatusId = request.getParameter("inpEmpStatusId").equals("0") ? null
            : request.getParameter("inpEmpStatusId");
        String inpLineManagerId = request.getParameter("inpLineManagerId").equals("0") ? null
            : request.getParameter("inpLineManagerId");
        String inpGenderId = request.getParameter("inpGenderId").equals("0") ? null
            : request.getParameter("inpGenderId");
        String inpEndDateH = request.getParameter("inpEndDateH");
        String inpEndDate = inpEndDateH;
        String inpStartDateH = request.getParameter("inpStartDateH");
        String inpStartDate = inpStartDateH;
        /*
         * String departmentName = ""; String deptListId = "";
         */

        inpEndDate = sa.elm.ob.hcm.util.UtilityDAO.convertToGregorian_tochar(inpEndDate);
        inpStartDate = sa.elm.ob.hcm.util.UtilityDAO.convertToGregorian_tochar(inpStartDate);
        // Department Name && childDept

        log4j.debug("inpEmpId>" + inpEmpId);
        log4j.debug("inpDeptId>" + inpDeptId);
        log4j.debug("inpEmpTypeId>" + inpEmpTypeId);
        log4j.debug("inpEmpGradeFromId>" + inpEmpGradeFromId);
        log4j.debug("inpEmpGradeToId>" + inpEmpGradeToId);
        log4j.debug("inpEmpStatusId>" + inpEmpStatusId);
        log4j.debug("inpLineManagerId>" + inpLineManagerId);
        log4j.debug("inpGenderId>" + inpGenderId);
        log4j.debug("inpEndDate>" + inpEndDate);
        log4j.debug("inpEndDateH>" + inpEndDateH);
        log4j.debug("inpStartDateH>" + inpStartDateH);
        log4j.debug("inpClientId>" + vars.getClient());

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("inpEmpId", inpEmpId);
        parameters.put("inpDepartmentId", inpDeptId);
        parameters.put("inpEmpTypeId", inpEmpTypeId);
        parameters.put("inpEmpGradeFromId",
            (inpEmpGradeFromId == null || inpEmpGradeFromId.equals("0")
                || inpEmpGradeFromId.equals("")) ? null : gradefrom.getSearchKey());
        parameters.put("inpEmpGradeToId",
            (inpEmpGradeToId == null || inpEmpGradeToId.equals("0") || inpEmpGradeToId.equals(""))
                ? null
                : gradeto.getSearchKey());
        parameters.put("inpLineManagerId",
            (inpLineManagerId == null || inpLineManagerId.equals("")) ? null : inpLineManagerId);
        parameters.put("inpEmpStatus",
            (inpEmpStatusId == null || inpEmpStatusId.equals("")) ? null : inpEmpStatusId);
        parameters.put("inpGenderId", inpGenderId);
        parameters.put("inpEndDate", inpEndDate);
        parameters.put("inpStartDate", inpStartDate);

        parameters.put("inpEndDateH", inpEndDateH);
        parameters.put("inpStartDateH", inpStartDateH);

        parameters.put("inpClientId", vars.getClient());
        parameters.put("inpDepartmentName",
            EmployeesBusinessMissionDAO.getUserDepartment(vars.getUser()));

        strReportName = "@basedesign@/sa/elm/ob/hcm/ad_reports/EmployeesInformationReport/EmployeesInformationReport.jrxml";
        String strOutput = "xls";
        String strFileName = "EmployeesInformationReport";

        renderJR(vars, response, strReportName, strFileName, strOutput, parameters, data, null);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in EmployeesInformationReport :", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}