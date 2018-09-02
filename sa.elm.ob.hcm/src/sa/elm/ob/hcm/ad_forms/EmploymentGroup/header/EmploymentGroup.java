package sa.elm.ob.hcm.ad_forms.EmploymentGroup.header;

import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

import sa.elm.ob.hcm.EhcmEmploymentGroup;
import sa.elm.ob.hcm.ad_forms.EmploymentGroup.dao.EmploymentGroupDAO;
import sa.elm.ob.hcm.ad_forms.EmploymentGroup.vo.EmploymentGroupVO;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author gopalakrishnan on 09/02/2017
 */

public class EmploymentGroup extends HttpSecureAppServlet {

  /**
   * Contract form details
   */
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    EmploymentGroupDAO dao = null;
    Connection con = null;
    // EmploymentVO employmentVO = null;
    RequestDispatcher dispatch = null;
    VariablesSecureApp vars = null;
    EmploymentGroupVO vo = null;
    try {

    } catch (final Exception e) {
      dispatch = request.getRequestDispatcher("../web/jsp/ErrorPage.jsp");
      log4j.error("Error in Contract : ", e);
    } finally {
      try {
        OBContext.setAdminMode();
        String action = (request.getParameter("inpAction") == null ? "" : request
            .getParameter("inpAction"));
        String submitType = request.getParameter("SubmitType") == null ? "" : request
            .getParameter("SubmitType");
        String employmentGrpId = request.getParameter("inpEmploymentGrpId") == null ? "" : request
            .getParameter("inpEmployeeId");

        con = getConnection();
        vars = new VariablesSecureApp(request);
        dao = new EmploymentGroupDAO(con);
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (submitType != null && (submitType.equals("Save") || submitType.equals("SaveNew"))) {
          vo = new EmploymentGroupVO();
          vo.setCode(request.getParameter("inpCode"));
          vo.setName(request.getParameter("inpName"));
          vo.setStartdate(request.getParameter("inpStartDate"));
          vo.setEnddate(request.getParameter("inpEndDate"));

          if (employmentGrpId.equals(""))
            employmentGrpId = dao.addEmploymentGroup(vars.getClient(), vars.getUser(), vo);
          else
            employmentGrpId = dao.updateEmploymentGrp(vars.getClient(), vars.getUser(), vo,
                employmentGrpId);

          if (employmentGrpId == null) {
            request.setAttribute("SaveStatus", "False");
            request.setAttribute("ErrorMsg", "Process Failed");
          } else {
            if (request.getParameter("inpEmploymentGrpId").equals(""))
              request.setAttribute("SaveStatus", "Add-True");
            else
              request.setAttribute("SaveStatus", "Update-True");
            if (request.getParameter("SubmitType").equals("SaveNew"))
              employmentGrpId = "";
          }
          log4j.debug("employmentGrpId:" + employmentGrpId);
          if (action.equals("GridView"))
            action = "GridView";
          else
            action = "EditView";

        }

        if (action.equals("") || action.equals("GridView")) {
          dispatch = request
              .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/EmploymentGroup/EmploymentGroupList.jsp");
        }
        if (action.equals("EditView")) {
          if (employmentGrpId == null || employmentGrpId.equals("")
              || employmentGrpId.equals("null")) {
            String date = df.format(new Date());
            date = dateYearFormat.format(df.parse(date));
            date = UtilityDAO.convertTohijriDate(date);

            request.setAttribute("inpStartDate", date);
            request.setAttribute("inpName", "");
            request.setAttribute("inpCode", "");
          }
          if (employmentGrpId != null && !employmentGrpId.equals("")
              && !employmentGrpId.equals("null")) {
            EhcmEmploymentGroup objEmploymentGroup = OBDal.getInstance().get(
                EhcmEmploymentGroup.class, employmentGrpId);
            if (objEmploymentGroup.getStartDate() != null) {
              String date = df.format(objEmploymentGroup.getStartDate());
              date = dateYearFormat.format(df.parse(date));
              date = UtilityDAO.convertTohijriDate(date);
              request.setAttribute("inpStartDate", date);
            }
            if (objEmploymentGroup.getEndDate() != null) {
              String date = df.format(objEmploymentGroup.getStartDate());
              date = dateYearFormat.format(df.parse(date));
              date = UtilityDAO.convertTohijriDate(date);
              request.setAttribute("inpStartDate", date);
            }
            request.setAttribute("inpName", objEmploymentGroup.getName());
            request.setAttribute("inpCode", objEmploymentGroup.getCode());

          }
          dispatch = request
              .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/EmploymentGroup/EmploymentGroup.jsp");
        }

      } catch (final Exception e) {
        dispatch = request.getRequestDispatcher("../web/jsp/ErrorPage.jsp");
        log4j.error("Error in Employment : ", e);
      } finally {
        OBContext.restorePreviousMode();
        try {
          con.close();
          if (dispatch != null) {
            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            dispatch.include(request, response);
          } else
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (final Exception e) {
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          log4j.error("Error in Employment Group : ", e);
        }
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    doPost(request, response);
  }
}
