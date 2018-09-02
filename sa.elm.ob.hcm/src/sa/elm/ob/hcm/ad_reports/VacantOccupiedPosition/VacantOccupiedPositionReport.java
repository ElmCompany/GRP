package sa.elm.ob.hcm.ad_reports.VacantOccupiedPosition;

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

import sa.elm.ob.hcm.ad_reports.positionTransaction.PositionTransactionsDetailsDAO;
import sa.elm.ob.hcm.ad_reports.positionTransaction.PositionTransactionsDetailsVO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author poongodi on 26/03/2018
 *
 */
public class VacantOccupiedPositionReport extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private String jspPage = "../web/sa.elm.ob.hcm/jsp/VacantOccupiedPosition/VacantOccupiedReport.jsp";

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
        // Load Grade
        List<VacantOccupiedPositionVO> gradels = new ArrayList<VacantOccupiedPositionVO>();
        List<VacantOccupiedPositionVO> depls = new ArrayList<VacantOccupiedPositionVO>();

        gradels = VacantOccupiedPositionDAO.getGradeCode(inpClientId);

        request.setAttribute("inpGradeIdName", gradels);

        // Load Department
        depls = VacantOccupiedPositionDAO.getDepartmentCode(inpClientId);

        request.setAttribute("inpOrgName", depls);

        // Load Todate
        String date = dateYearFormat.format(new Date());
        date = UtilityDAO.convertTohijriDate(date);
        request.setAttribute("inpToDate", date);

        // Localization support
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        request.getRequestDispatcher(jspPage).include(request, response);

      } else if (action.equals("Submit")) {
        String inpgradeId = request.getParameter("inpgradeId");
        String inpdeptId = request.getParameter("inpdeptId");
        Date inpFromDate = null;
        String departmentName = "";
        String deptListId = null;
        log4j.debug("inpdeptId" + inpdeptId);

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
        if (request.getParameter("fromdate") != "" && request.getParameter("fromdate") != null
            && !request.getParameter("fromdate").equals("null")) {
          inpFromDate = dateYearFormat.parse(sa.elm.ob.hcm.util.UtilityDAO
              .convertToGregorian_tochar(request.getParameter("fromdate")));
        } else {
          inpFromDate = dateYearFormat.parse(
              sa.elm.ob.hcm.util.UtilityDAO.convertToGregorian_tochar(UtilityDAO.HijriMinDate()));
        }

        Date inpToDate = dateYearFormat.parse(sa.elm.ob.hcm.util.UtilityDAO
            .convertToGregorian_tochar(request.getParameter("todate")));

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("inpgradeId", inpgradeId);
        parameters.put("inpdeptId", inpdeptId != null ? deptListId : "");
        parameters.put("inpFromDate", inpFromDate);
        parameters.put("inpToDate", inpToDate);
        parameters.put("inpClientId", inpClientId);
        parameters.put("inpdepartmentName", departmentName);

        strReportName = "@basedesign@/sa/elm/ob/hcm/ad_reports/VacantOccupiedPosition/VacantOccupiedPositionReport.jrxml";
        String strOutput = "xls";

        renderJR(vars, response, strReportName, strOutput, parameters, null, null);
      }

    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in VacantOccupiedposition :", e);
    }

  }
}