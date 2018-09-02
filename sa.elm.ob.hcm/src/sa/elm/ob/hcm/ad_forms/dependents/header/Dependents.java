package sa.elm.ob.hcm.ad_forms.dependents.header;

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

import sa.elm.ob.hcm.EhcmDependents;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ehcmempstatusv;
import sa.elm.ob.hcm.ad_forms.dependents.dao.DependentDAO;
import sa.elm.ob.hcm.ad_forms.dependents.vo.DependentVO;
import sa.elm.ob.hcm.ad_forms.employee.dao.EmployeeDAO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class Dependents extends HttpSecureAppServlet {

  /**
   * dependents related to employee details
   */
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    log4j.debug("action:" + request.getParameter("inpAction"));
    String action = (request.getParameter("inpAction") == null ? ""
        : request.getParameter("inpAction"));
    String submittype = (request.getParameter("SubmitType") == null ? ""
        : request.getParameter("SubmitType"));
    String employeeId = request.getParameter("inpEmployeeId") == null ? ""
        : request.getParameter("inpEmployeeId");
    RequestDispatcher dispatch = null;
    String nextTab = request.getParameter("inpNextTab") == null ? ""
        : request.getParameter("inpNextTab");
    log4j.debug("inpNextTab" + nextTab);
    String inpempstatus = (request.getParameter("inpEmpStatus") == null ? ""
        : request.getParameter("inpEmpStatus"));
    log4j.debug("inpempstatus" + inpempstatus);
    String inpEmployeeStatus = (request.getParameter("inpEmployeeStatus") == null ? ""
        : request.getParameter("inpEmployeeStatus"));
    log4j.debug("inpEmployeeStatus" + inpEmployeeStatus);

    try {
      OBContext.setAdminMode();
      DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
      SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
      String date = df.format(new Date());
      date = dateYearFormat.format(df.parse(date));
      date = UtilityDAO.convertTohijriDate(date);

      EmployeeDAO empdao = null;
      DependentDAO depdao = null;
      DependentVO depvo = null;
      EmployeeDAO dao1 = null;

      String dependentId = (request.getParameter("inpDependentId") == null ? ""
          : request.getParameter("inpDependentId"));
      employeeId = (request.getParameter("inpEmployeeId") == null ? ""
          : request.getParameter("inpEmployeeId"));

      Connection con = getConnection();
      empdao = new EmployeeDAO(con);
      depdao = new DependentDAO(con);
      dao1 = new EmployeeDAO(con);

      EhcmDependents depent = null;
      VariablesSecureApp vars = new VariablesSecureApp(request);
      if (submittype.equals("Save") || submittype.equals("SaveNew")
          || submittype.equals("SaveGrid")) {
        log4j.debug("dependentId: " + dependentId);

        request.setAttribute("today", date);

        if (dependentId.equals("") || dependentId.equals("null")) {
          depent = OBProvider.getInstance().get(EhcmDependents.class);
        } else {
          depent = OBDal.getInstance().get(EhcmDependents.class, dependentId);
        }
        // log4j.debug("depen:" + depent.getId());
        if (dependentId.equals("") || dependentId.equals("null")) {
          depent.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
          depent.setOrganization(OBDal.getInstance().get(Organization.class, vars.getOrg()));
          depent.setCreationDate(new java.util.Date());
          depent.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        }

        depent.setUpdated(new java.util.Date());
        depent.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));

        EhcmEmpPerInfo perinfo = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
        depent.setEhcmEmpPerinfo(perinfo);
        if (request.getParameter("inpRelationship") != null
            && request.getParameter("inpRelationship") != "")
          depent.setRelationship(request.getParameter("inpRelationship").toString());
        if (request.getParameter("inpFirstName") != null
            && request.getParameter("inpFirstName") != "")
          depent.setFirstName(request.getParameter("inpFirstName").toString());
        if (request.getParameter("inpFatherName") != null
            && request.getParameter("inpFatherName") != "")
          depent.setFathername(request.getParameter("inpFatherName").toString());
        if (request.getParameter("inpGrandFather") != null
            && request.getParameter("inpGrandFather") != "")
          depent.setGrandfather(request.getParameter("inpGrandFather").toString());
        if (request.getParameter("inpFourthName") != null
            && request.getParameter("inpFourthName") != "")
          depent.setFourthname(request.getParameter("inpFourthName").toString());
        else
          depent.setFourthname("");
        if (request.getParameter("inpFamily") != null && request.getParameter("inpFamily") != "")
          depent.setFamily(request.getParameter("inpFamily").toString());
        if (request.getParameter("inpDoj") != null && request.getParameter("inpDoj") != "")
          depent.setDob(empdao.convertGregorian(request.getParameter("inpDoj").toString()));
        if (request.getParameter("inpAgeMY") != null && request.getParameter("inpAgeMY") != "") {
          String age = request.getParameter("inpAgeMY").toString();
          depent.setAge((long) Integer.parseInt(age));
        }
        if (request.getParameter("inpGender") != null && request.getParameter("inpGender") != "")
          depent.setGender(request.getParameter("inpGender").toString());
        if (request.getParameter("inpNatIdf") != null && request.getParameter("inpNatIdf") != "")
          depent.setNationalidentifier(request.getParameter("inpNatIdf").toString());
        if (request.getParameter("inpStartDate") != null
            && request.getParameter("inpStartDate") != "")
          depent.setStartDate(
              empdao.convertGregorian(request.getParameter("inpStartDate").toString()));
        if (request.getParameter("inpEndDate") != null && request.getParameter("inpEndDate") != "")
          depent.setEndDate(empdao.convertGregorian(request.getParameter("inpEndDate").toString()));
        if (request.getParameter("inpPhoneNo") != null
            && request.getParameter("inpPhoneNo") != "") {
          depent.setPhoneno(request.getParameter("inpPhoneNo").toString());
        } else {
          depent.setPhoneno("");
        }
        if (request.getParameter("inpLocation") != null
            && request.getParameter("inpLocation") != "")
          depent.setLocation(request.getParameter("inpLocation").toString());
        else
          depent.setLocation("");

        OBDal.getInstance().save(depent);
        OBDal.getInstance().flush();

        OBDal.getInstance().commitAndClose();

        dependentId = depent.getId();
        request.setAttribute("inpDependentId", dependentId);
        request.setAttribute("savemsg", "Success");
        log4j.debug("action: " + action);

        if (nextTab.equals("") || nextTab.equals("null")) {
          if (submittype.equals("SaveNew")) {
            dependentId = "";
            request.setAttribute("inpDependentId", "");
            action = "EditView";
          } else if (submittype.equals("Save")) {
            request.setAttribute("inpDependentId", (dependentId == null ? "" : dependentId));
            action = "EditView";
          } else if (submittype.equals("SaveGrid")) {
            dependentId = "";
            action = "GridView";
          }
        } else {
          ServletContext context = this.getServletContext();
          if (!nextTab.equals("") && !nextTab.equals("Dependent") && !nextTab.equals("null")) {
            log4j.debug("nextTab:" + nextTab);
            String redirectStr = empdao.redirectStr(nextTab, employeeId, inpempstatus,
                inpEmployeeStatus);
            response.sendRedirect(context.getContextPath() + redirectStr);
          }
        }
      }

      if (action.equals("EditView")) {
        log4j.debug("entered DependenteditView");

        request.setAttribute("today", date);

        if (dependentId != null && dependentId != "" && dependentId != "null") {
          depvo = depdao.getDependentEditList(employeeId, dependentId);
          request.setAttribute("inpEmployeeId", (employeeId == null ? "" : employeeId));
          request.setAttribute("inpDependentId", (dependentId == null ? "" : dependentId));
          request.setAttribute("inpFirstName", Utility.nullToEmpty(depvo.getFirstname()));
          request.setAttribute("inpRelationship", Utility.nullToEmpty(depvo.getRelationship()));
          request.setAttribute("inpFatherName", Utility.nullToEmpty(depvo.getFathername()));
          request.setAttribute("inpGrandFather", Utility.nullToEmpty(depvo.getGrandfather()));
          request.setAttribute("inpFourthName", Utility.nullToEmpty(depvo.getFourthname()));
          request.setAttribute("inpFamily", Utility.nullToEmpty(depvo.getFamily()));
          request.setAttribute("inpDoj", Utility.nullToEmpty(depvo.getDob()));
          request.setAttribute("inpGender", Utility.nullToEmpty(depvo.getGender()));
          request.setAttribute("inpNatIdf", Utility.nullToEmpty(depvo.getNatidf()));
          request.setAttribute("inpStartDate", Utility.nullToEmpty(depvo.getStartdate()));
          request.setAttribute("inpEndDate", Utility.nullToEmpty(depvo.getEnddate()));
          request.setAttribute("inpPhoneNo", Utility.nullToEmpty(depvo.getPhoneno()));
          log4j.debug("father:" + depvo.getPhoneno());
          if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
            request.setAttribute("inpAddressId", null);
          else
            request.setAttribute("inpAddressId", dao1.getEmployeeAddressId(employeeId));
          request.setAttribute("inpLocation", Utility.nullToEmpty(depvo.getLocation()));

          // employee details
          EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
          if (objEmployee.getGradeClass() != null) {
            if (objEmployee.getGradeClass().isContract()) {
              request.setAttribute("inpempCategory", "Y");
            } else {
              request.setAttribute("inpempCategory", "");
            }
          } else {
            request.setAttribute("inpempCategory", "");
          }
          request.setAttribute("inpDocumentId", "");
          request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
          request.setAttribute("inpName1", objEmployee.getArabicfullname());
          request.setAttribute("inpName2",
              objEmployee.getName().concat(" ").concat(objEmployee.getFathername()).concat(" ")
                  .concat(objEmployee.getGrandfathername()));
          if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
            request.setAttribute("inpAddressId", null);
          else
            request.setAttribute("inpAddressId", empdao.getEmployeeAddressId(employeeId));
        } else {
          request.setAttribute("today", date);
          request.setAttribute("inpDependentId", "");
          request.setAttribute("inpEmployeeId", (employeeId == null ? "" : employeeId));
          request.setAttribute("inpRelationship", "");
          request.setAttribute("inpFirstName", "");
          request.setAttribute("inpFatherName", "");
          request.setAttribute("inpGrandFather", "");
          request.setAttribute("inpFourthName", "");
          request.setAttribute("inpFamily", "");
          request.setAttribute("inpDoj", date);
          request.setAttribute("inpGender", "");
          request.setAttribute("inpNatIdf", "");
          request.setAttribute("inpStartDate", date);
          request.setAttribute("inpEndDate", "");
          request.setAttribute("inpPhoneno", "");
          request.setAttribute("inpLocation", "");
          if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
            request.setAttribute("inpAddressId", null);
          else
            request.setAttribute("inpAddressId", dao1.getEmployeeAddressId(employeeId));

          // employee details
          EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
          request.setAttribute("inpDocumentId", "");
          if (objEmployee.getGradeClass() != null) {
            if (objEmployee.getGradeClass().isContract()) {
              request.setAttribute("inpempCategory", "Y");
            } else {
              request.setAttribute("inpempCategory", "");
            }
          } else {
            request.setAttribute("inpempCategory", "");
          }
          request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
          request.setAttribute("inpName1", objEmployee.getArabicfullname());
          request.setAttribute("inpName2",
              objEmployee.getName().concat(" ")
                  .concat(objEmployee.getFathername() == null ? "" : objEmployee.getFathername())
                  .concat(" ").concat(objEmployee.getGrandfathername() == null ? ""
                      : objEmployee.getGrandfathername()));
          if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
            request.setAttribute("inpAddressId", null);
          else {

            request.setAttribute("inpAddressId", empdao.getEmployeeAddressId(employeeId));
          }
        }
        dispatch = request
            .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/dependents/Dependents.jsp");

      } else if (action.equals("") || action.equals("GridView")) {
        ehcmempstatusv view = OBDal.getInstance().get(ehcmempstatusv.class, employeeId);
        EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class,
            view.getEhcmEmpPerinfo().getId());
        dependentId = "";
        action = "GridView";
        log4j.debug("employeeId: " + employeeId);
        request.setAttribute("inpEmployeeId", (employeeId == null ? "" : employeeId));
        request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
        request.setAttribute("inpDependentId", "");
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
        request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
        // request.setAttribute("inpEmployeeId", employeeId);
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
            .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/dependents/DependentsList.jsp");
      }

    } catch (final Exception e) {
      dispatch = request.getRequestDispatcher("../web/jsp/ErrorPage.jsp");
      log4j.error("Error in Dependent : ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
      OBContext.restorePreviousMode();
      try {
        if (dispatch != null) {
          response.setContentType("text/html; charset=UTF-8");
          response.setCharacterEncoding("UTF-8");
          dispatch.include(request, response);
        } else
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (final Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log4j.error("Error in Dependents : ", e);
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }
}
