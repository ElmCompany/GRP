package sa.elm.ob.hcm.ad_reports.positionTransaction;

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
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;

import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author poongodi on 30/03/2018
 *
 */
public class PositionTransactionsDetails extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private String jspPage = "../web/sa.elm.ob.hcm/jsp/positionTransaction/PositionTransactionsDetails.jsp";

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
        // Load Grade
        List<PositionTransactionsDetailsVO> gradels = new ArrayList<PositionTransactionsDetailsVO>();
        List<PositionTransactionsDetailsVO> depls = new ArrayList<PositionTransactionsDetailsVO>();
        List<PositionTransactionsDetailsVO> jobls = new ArrayList<PositionTransactionsDetailsVO>();
        gradels = PositionTransactionsDetailsDAO.getGradeCode(inpClientId);

        request.setAttribute("inpGradeIdName", gradels);

        // Load Department
        depls = PositionTransactionsDetailsDAO.getDepartmentCode(inpClientId);

        request.setAttribute("inpOrgName", depls);

        // Load Job
        jobls = PositionTransactionsDetailsDAO.getJoBNo(inpClientId);
        request.setAttribute("inpJobNo", jobls);

        // Load FromDate
        String date = dateYearFormat.format(new Date());
        date = UtilityDAO.convertTohijriDate(date);
        request.setAttribute("inpFromDate", date);

        // Localization support
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        request.getRequestDispatcher(jspPage).include(request, response);

      } else if (action.equals("GetJobNo")) {

        StringBuilder sb = new StringBuilder();

        try {
          List<PositionTransactionsDetailsVO> groupList = PositionTransactionsDetailsDAO
              .getJobNoBasedOnGrade(request.getParameter("inpGradeName"));
          log4j.debug(groupList);
          sb.append("<GetGroup>");

          for (PositionTransactionsDetailsVO vo1 : groupList) {
            sb.append("<JobGroup>");
            sb.append("<ID>" + vo1.getPositionId() + "</ID>");
            sb.append("<Name><![CDATA[" + vo1.getJobNo() + "]]></Name>");
            sb.append("</JobGroup>");
          }
          sb.append("</GetGroup>");
        } catch (Exception e) {
          log4j.error("Exception in  GetJobNo : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }

      }

      else if (action.equals("Submit")) {
        String inpgradeId = request.getParameter("inpgradeId");
        String inpdeptId = request.getParameter("inpdeptId");
        String inpjobId = request.getParameter("inpjobId");
        Date inpFromDate = null;
        Date inpToDate = null;
        String departmentName = "";
        String deptListId = null;
        String gradeCode = null;
        String jobNo = null;
        String startDate = request.getParameter("fromdate");
        // Grade Code
        ehcmgrade grade = OBDal.getInstance().get(ehcmgrade.class, inpgradeId);
        gradeCode = grade.getSearchKey();

        // JobNo
        EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class, inpjobId);
        jobNo = position.getJOBNo();

        // Department Name && childDept
        if (inpdeptId != "" && inpdeptId != null && !inpdeptId.equals("null")) {
          if (!inpdeptId.equals("0")) {
            Organization org = OBDal.getInstance().get(Organization.class, inpdeptId);
            departmentName = org.getName();
          }
          List<PositionTransactionsDetailsVO> depChildLs = new ArrayList<PositionTransactionsDetailsVO>();
          depChildLs = PositionTransactionsDetailsDAO.getChildDepartment(inpClientId, inpdeptId);
          for (PositionTransactionsDetailsVO vo : depChildLs) {
            if (deptListId == null) {
              deptListId = "'" + vo.getOrgId() + "'";
            } else {
              deptListId += ",'" + vo.getOrgId() + "'";
            }
          }
        }

        if (deptListId == null) {
          deptListId = "0";
        }
        inpFromDate = dateYearFormat.parse(sa.elm.ob.hcm.util.UtilityDAO
            .convertToGregorian_tochar(request.getParameter("fromdate")));
        if (request.getParameter("todate") != "" && request.getParameter("todate") != null
            && !request.getParameter("todate").equals("null")) {
          inpToDate = dateYearFormat.parse(sa.elm.ob.hcm.util.UtilityDAO
              .convertToGregorian_tochar(request.getParameter("todate")));
        } else {
          inpToDate = dateYearFormat.parse(
              sa.elm.ob.hcm.util.UtilityDAO.convertToGregorian_tochar(UtilityDAO.HijriMaxDate()));
        }

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("inpgradeId", inpgradeId);
        parameters.put("inpdeptId", inpdeptId != null ? deptListId : "");
        parameters.put("inpjobId", inpjobId);
        parameters.put("inpFromDate", inpFromDate);
        parameters.put("inpToDate", inpToDate);
        parameters.put("inpClientId", inpClientId);
        parameters.put("departmentName", departmentName);
        parameters.put("gradeCode", gradeCode);
        parameters.put("jobNo", jobNo);
        parameters.put("startDate", startDate);
        strReportName = "@basedesign@/sa/elm/ob/hcm/ad_reports/positionTransaction/PositionTransactionsDetailsReport.jrxml";
        String strOutput = "xls";

        renderJR(vars, response, strReportName, strOutput, parameters, null, null);
      }

    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in PositionTransactionsDetails :", e);
    } finally {
      OBContext.restorePreviousMode();
    }

  }
}