package sa.elm.ob.hcm.ad_forms.preemp.header;

import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ehcmempstatusv;
import sa.elm.ob.hcm.ehcmpreviouservice;
import sa.elm.ob.hcm.ad_forms.employee.dao.EmployeeDAO;
import sa.elm.ob.hcm.ad_forms.qualification.dao.QualificationDAO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class PreviousEmployment extends HttpSecureAppServlet {

  /**
   * Preivious Employment form details
   */
  private static final long serialVersionUID = 1L;

  @SuppressWarnings({ "unchecked", "unused" })
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    QualificationDAO dao = null;
    Connection con = null;
    RequestDispatcher dispatch = null;
    VariablesSecureApp vars = null;

    EmployeeDAO dao1 = null;
    try {
      con = getConnection();
      OBContext.setAdminMode();
      String action = (request.getParameter("inpAction") == null ? ""
          : request.getParameter("inpAction"));
      String preEmplymentId = (request.getParameter("inpPreEmplymentId") == null ? ""
          : request.getParameter("inpPreEmplymentId"));
      String employeeId = request.getParameter("inpEmployeeId") == null ? ""
          : request.getParameter("inpEmployeeId");
      String nextTab = (request.getParameter("inpNextTab") == null ? ""
          : request.getParameter("inpNextTab"));
      String inpempstatus = (request.getParameter("inpEmpStatus") == null ? ""
          : request.getParameter("inpEmpStatus"));
      log4j.debug("inpempstatus" + inpempstatus);
      String inpEmployeeStatus = (request.getParameter("inpEmployeeStatus") == null ? ""
          : request.getParameter("inpEmployeeStatus"));
      log4j.debug("inpEmployeeStatus" + inpEmployeeStatus);
      log4j.debug("preEmplymentId:" + preEmplymentId);
      log4j.debug("employeeId:" + employeeId);
      log4j.debug("action:" + action);
      dao = new QualificationDAO(con);
      dao1 = new EmployeeDAO(con);
      ehcmpreviouservice preservice = null;
      vars = new VariablesSecureApp(request);
      if (request.getParameter("SubmitType") != null
          && (request.getParameter("SubmitType").equals("Save")
              || request.getParameter("SubmitType").equals("SaveGrid")
              || request.getParameter("SubmitType").equals("SaveNew"))) {
        if (preEmplymentId == null || preEmplymentId == "" || preEmplymentId.equals("null")) {
          log4j.debug("if:");
          preservice = OBProvider.getInstance().get(ehcmpreviouservice.class);
        } else {
          log4j.debug("else:");
          preservice = OBDal.getInstance().get(ehcmpreviouservice.class, preEmplymentId);
        }
        if (preEmplymentId == null || preEmplymentId == "" || preEmplymentId.equals("null")) {
          preservice.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
          preservice.setOrganization(OBDal.getInstance().get(Organization.class, vars.getOrg()));
          preservice.setCreationDate(new java.util.Date());
          preservice.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        }
        preservice.setUpdated(new java.util.Date());
        preservice.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        if (request.getParameter("inpempname") != null && request.getParameter("inpempname") != "")
          preservice.setEmployerName(request.getParameter("inpempname").toString());
        if (request.getParameter("inpStartDate") != null
            && request.getParameter("inpStartDate") != "")
          preservice
              .setStartDate(dao.convertGregorian(request.getParameter("inpStartDate").toString()));
        if (request.getParameter("inpEndDate") != null && request.getParameter("inpEndDate") != "")
          preservice
              .setEndDate(dao.convertGregorian(request.getParameter("inpEndDate").toString()));
        preservice.setGrade(request.getParameter("inpgrade").toString());
        preservice.setEMPCategory(request.getParameter("inpempcat").toString());
        preservice.setDeptName(request.getParameter("inpdep").toString());
        preservice.setOtherdetails(request.getParameter("inpotherdet").toString());
        preservice.setEmpposition(request.getParameter("inpposition"));
        preservice.setEhcmEmpPerinfo(OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId));
        OBDal.getInstance().save(preservice);
        OBDal.getInstance().flush();

        OBDal.getInstance().commitAndClose();
        log4j.debug("preEmplymentId:" + preEmplymentId);
        if (nextTab.equals("")) {
          if (request.getParameter("SubmitType").equals("Save")) {
            action = "EditView";
            preEmplymentId = preservice.getId();
            request.setAttribute("inpPreEmplymentId",
                (preEmplymentId == null ? "" : preEmplymentId));
            request.setAttribute("inpEmployeeId", employeeId);
          } else if (request.getParameter("SubmitType").equals("SaveNew")) {
            action = "EditView";
            preEmplymentId = "";
            request.setAttribute("inpPreEmplymentId", "");
            request.setAttribute("inpEmployeeId", employeeId);
            log4j.debug("inpPreEmplymentId:" + request.getAttribute("inpPreEmplymentId"));
          } else if (request.getParameter("SubmitType").equals("SaveGrid")) {
            action = "GridView";
            preEmplymentId = "";
            request.setAttribute("inpPreEmplymentId", "");
            request.setAttribute("inpEmployeeId", employeeId);
          }
        } else {
          ServletContext context = this.getServletContext();
          if (!nextTab.equals("") && !nextTab.equals("PREEMP")) {
            String redirectStr = dao1.redirectStr(nextTab, employeeId, inpempstatus,
                inpEmployeeStatus);
            response.sendRedirect(context.getContextPath() + redirectStr);
          }
        }
        request.setAttribute("savemsg", "Success");

      }

      if (action.equals("EditView")) {
        log4j.debug("EditView");
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = df.format(new Date());
        date = dateYearFormat.format(df.parse(date));
        date = UtilityDAO.convertTohijriDate(date);

        if (preEmplymentId != null && preEmplymentId != "" && !preEmplymentId.equals("null")) {
          ehcmpreviouservice getemp = OBDal.getInstance().get(ehcmpreviouservice.class,
              preEmplymentId);
          EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
          request.setAttribute("inpEmployeeId", employeeId);
          request.setAttribute("inpPreEmplymentId", preEmplymentId);
          request.setAttribute("inpempname", getemp.getEmployerName());
          if (getemp.getStartDate() != null) {
            String startdate = df.format(getemp.getStartDate());
            startdate = dateYearFormat.format(df.parse(startdate));
            startdate = UtilityDAO.convertTohijriDate(startdate);
            request.setAttribute("inpStartDate", startdate);
          }
          if (getemp.getEndDate() != null) {
            String enddate = df.format(getemp.getEndDate());
            enddate = dateYearFormat.format(df.parse(enddate));
            enddate = UtilityDAO.convertTohijriDate(enddate);
            request.setAttribute("inpEndDate", enddate);
          }
          request.setAttribute("inpgrade", getemp.getGrade());
          request.setAttribute("inpempcat", getemp.getEMPCategory());
          request.setAttribute("inpdep", getemp.getDeptName());
          request.setAttribute("inpotherdet", getemp.getOtherdetails());
          request.setAttribute("inpposition", Utility
              .escapeHTML((getemp.getEmpposition().replace("\n", "\\n").replace("\r", "\\r"))));
          if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
            request.setAttribute("inpAddressId", null);
          else

            request.setAttribute("inpAddressId", dao1.getEmployeeAddressId(employeeId));
          if (request.getParameter("inpEmployeeStatus") != null) {
            request.setAttribute("inpEmployeeStatus",
                request.getParameter("inpEmployeeStatus").toString());
          }
          if (request.getParameter("inpEmpStatus") != null) {
            request.setAttribute("inpEmpStatus", request.getParameter("inpEmpStatus").toString());
          }
          dispatch = request
              .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/preemp/PreEmployment.jsp");

        } else if (preEmplymentId == null || preEmplymentId == ""
            || preEmplymentId.equals("null")) {
          request.setAttribute("inpEmployeeId", "");
          request.setAttribute("inpStartDate", "");
          request.setAttribute("inpEndDate", "");
          request.setAttribute("inpgrade", "");
          request.setAttribute("inpempcat", "");
          request.setAttribute("inpdep", "");
          request.setAttribute("inpotherdet", "");
          request.setAttribute("inpposition", "");
          if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
            request.setAttribute("inpAddressId", null);
          else
            request.setAttribute("inpAddressId", dao1.getEmployeeAddressId(employeeId));
          if (request.getParameter("inpEmployeeStatus") != null) {
            request.setAttribute("inpEmployeeStatus",
                request.getParameter("inpEmployeeStatus").toString());
          }
          if (request.getParameter("inpEmpStatus") != null) {
            request.setAttribute("inpEmpStatus", request.getParameter("inpEmpStatus").toString());
          }
          dispatch = request
              .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/preemp/PreEmployment.jsp");
        }
        request.setAttribute("today", date);
        request.setAttribute("inpEmployeeId", employeeId);
        EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
        request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
        request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
        if (objEmployee.getGradeClass() != null) {
          if (objEmployee.getGradeClass().isContract()) {
            request.setAttribute("inpempCategory", "Y");
          } else {
            request.setAttribute("inpempCategory", "");
          }
        } else {
          request.setAttribute("inpempCategory", "");
        }
        request.setAttribute("inpName1", objEmployee.getArabicfullname());
        request.setAttribute("inpName2",
            objEmployee.getName().concat(" ").concat(objEmployee.getFathername()).concat(" ")
                .concat(objEmployee.getGrandfathername()));
        request.setAttribute("inpHireDate", UtilityDAO.convertTohijriDate(
            dateYearFormat.format(df.parse(df.format(objEmployee.getHiredate())))));
        request.setAttribute("CancelHiring",
            dao1.checkEmploymentStatusCancel(vars.getClient(), employeeId));
        dispatch = request
            .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/preemp/PreEmployment.jsp");
      }

      else if (action.equals("") || action.equals("GridView")) {
        ehcmempstatusv view = OBDal.getInstance().get(ehcmempstatusv.class, employeeId);
        EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class,
            view.getEhcmEmpPerinfo().getId());
        request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
        request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
        request.setAttribute("inpEmployeeId", employeeId);
        if (request.getParameter("inpEmpStatus") != null) {
          request.setAttribute("inpEmpStatus", request.getParameter("inpEmpStatus").toString());
        }
        if (request.getParameter("inpEmployeeStatus") != null) {
          request.setAttribute("inpEmployeeStatus",
              request.getParameter("inpEmployeeStatus").toString());
        }
        if (objEmployee.getGradeClass() != null) {
          if (objEmployee.getGradeClass().isContract()) {
            request.setAttribute("inpempCategory", "Y");
          } else {
            request.setAttribute("inpempCategory", "");
          }
        } else {
          request.setAttribute("inpempCategory", "");
        }
        request.setAttribute("inpName1", objEmployee.getArabicfullname());
        request.setAttribute("inpName2", objEmployee.getName().concat(" ").concat(
            StringUtils.isNotEmpty(objEmployee.getFathername()) ? objEmployee.getFathername() : "")
            .concat(" ")
            .concat(StringUtils.isNotEmpty(objEmployee.getGrandfathername())
                ? objEmployee.getGrandfathername()
                : ""));

        if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
          request.setAttribute("inpAddressId", null);
        else
          request.setAttribute("inpAddressId", dao1.getEmployeeAddressId(employeeId));
        request.setAttribute("CancelHiring",
            dao1.checkEmploymentStatusCancel(vars.getClient(), employeeId));
        dispatch = request
            .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/preemp/PreEmploymentList.jsp");
      }
      OBDal.getInstance().commitAndClose();
    } catch (final Exception e) {
      dispatch = request.getRequestDispatcher("../web/jsp/ErrorPage.jsp");
      log4j.error("Error in Employee : ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
      try {
        con.close();
        OBContext.restorePreviousMode();
        if (dispatch != null) {
          response.setContentType("text/html; charset=UTF-8");
          response.setCharacterEncoding("UTF-8");
          dispatch.include(request, response);
        } else
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (final Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log4j.error("Error in Employee : ", e);
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }
}
