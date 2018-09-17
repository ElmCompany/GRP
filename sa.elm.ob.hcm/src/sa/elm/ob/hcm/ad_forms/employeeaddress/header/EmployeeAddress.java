package sa.elm.ob.hcm.ad_forms.employeeaddress.header;

import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.geography.City;
import org.openbravo.model.common.geography.Country;

import sa.elm.ob.hcm.EHCMEmpAddress;
import sa.elm.ob.hcm.EhcmAddressStyle;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ehcmempstatusv;
import sa.elm.ob.hcm.ad_forms.employee.dao.EmployeeDAO;
import sa.elm.ob.hcm.ad_forms.employee.vo.EmployeeVO;
import sa.elm.ob.hcm.ad_forms.employeeaddress.vo.EmployeeAddressVO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class EmployeeAddress extends HttpSecureAppServlet {

  /**
   * Employment form details
   */
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    EmployeeDAO dao = null;
    Connection con = null;
    RequestDispatcher dispatch = null;
    VariablesSecureApp vars = null;
    EmployeeAddressVO vo = null;
    UtilityDAO utilitydao = null;

    try {
      OBContext.setAdminMode();
      String action = (request.getParameter("inpAction") == null ? ""
          : request.getParameter("inpAction"));
      String submitType = request.getParameter("SubmitType") == null ? ""
          : request.getParameter("SubmitType");
      String empAddId = (request.getParameter("inpAddressId") == null ? ""
          : request.getParameter("inpAddressId"));
      String employeeId = (request.getParameter("inpEmployeeId") == null ? ""
          : request.getParameter("inpEmployeeId"));
      String nextTab = request.getParameter("inpNextTab") == null ? ""
          : request.getParameter("inpNextTab");
      String inpempstatus = (request.getParameter("inpEmpStatus") == null ? ""
          : request.getParameter("inpEmpStatus"));
      log4j.debug("inpempstatus" + inpempstatus);
      String inpEmployeeStatus = (request.getParameter("inpEmployeeStatus") == null ? ""
          : request.getParameter("inpEmployeeStatus"));
      log4j.debug("action" + action);
      con = getConnection();
      vars = new VariablesSecureApp(request);
      dao = new EmployeeDAO(con);
      utilitydao = new UtilityDAO();
      EHCMEmpAddress empaddrs = null;
      empAddId = dao.getEmployeeAddressId(employeeId);
      // save operation
      if (submitType != null && (submitType.equals("Save"))) {
        if (empAddId.equals("") || empAddId == null || empAddId.equals("null")) {
          empaddrs = OBProvider.getInstance().get(EHCMEmpAddress.class);
        } else {
          empaddrs = OBDal.getInstance().get(EHCMEmpAddress.class, empAddId);
        }

        if (empAddId.equals("")) {
          empaddrs.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
          empaddrs.setOrganization(OBDal.getInstance().get(Organization.class, vars.getOrg()));
          empaddrs.setCreationDate(new java.util.Date());
          empaddrs.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        }

        empaddrs.setUpdated(new java.util.Date());
        empaddrs.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));

        empaddrs.setAddressStyle(OBDal.getInstance().get(EhcmAddressStyle.class,
            Utility.nullToEmpty(request.getParameter("inpAddressStyle")).toString()));
        empaddrs
            .setStartDate(dao.convertGregorian(request.getParameter("inpStartDate1").toString()));
        empaddrs.setCountry(
            OBDal.getInstance().get(Country.class, request.getParameter("inpCountry").toString()));
        empaddrs.setCity(
            OBDal.getInstance().get(City.class, request.getParameter("inpCity").toString()));

        empaddrs.setAddressLine1(request.getParameter("inpAdd1"));
        empaddrs.setPostBox(request.getParameter("inpPostBox"));
        empaddrs.setSECStartdate(
            dao.convertGregorian(request.getParameter("inpStartDate2").toString()));

        empaddrs.setEhcmEmpPerinfo(
            OBDal.getInstance().get(EhcmEmpPerInfo.class, request.getParameter("inpEmployeeId")));
        empaddrs.setAddressLine2(request.getParameter("inpAdd2"));
        empaddrs.setDistrict(request.getParameter("inpDistrict").toString());
        empaddrs.setStreet(request.getParameter("inpStreet"));
        empaddrs.setPostalCode(request.getParameter("inpPostalcode").toString());
        if (request.getParameter("inpEndDate1") != null
            && request.getParameter("inpEndDate1") != "") {
          empaddrs.setEndDate(dao.convertGregorian(request.getParameter("inpEndDate1").toString()));
        } else {
          empaddrs.setEndDate(null);
        }

        if (request.getParameter("inpEndDate2") != null
            && request.getParameter("inpEndDate2") != "") {
          empaddrs
              .setSECEnddate(dao.convertGregorian(request.getParameter("inpEndDate2").toString()));
        } else {
          empaddrs.setSECEnddate(null);
        }
        empaddrs.setSECDistrict(request.getParameter("inpSecDistrict"));
        empaddrs.setSECStreet(request.getParameter("inpSecStreet"));
        empaddrs.setSECCCountry(OBDal.getInstance().get(Country.class,
            Utility.nullToEmpty(request.getParameter("inpSecCountry")).toString()));
        if (request.getParameter("inpSecCity") != null) {
          empaddrs.setSECCCity(
              OBDal.getInstance().get(City.class, request.getParameter("inpSecCity").toString()));
        } else {
          empaddrs.setSECCCity(null);
        }

        empaddrs.setSECAddress1(request.getParameter("inpSecAdd1"));
        empaddrs.setSECAddress2(request.getParameter("inpSecAdd2"));
        empaddrs.setSECPostalcode(request.getParameter("inpSecPostalcode"));
        empaddrs.setSECPostbox(request.getParameter("inpSecPostbox"));

        if (request.getParameter("inpActive").equals("true")) {
          empaddrs.setActive(true);
        } else if (request.getParameter("inpActive").equals("false")) {
          empaddrs.setActive(false);
        }
        if (request.getParameter("inpprimarychk").equals("true")) {
          empaddrs.setPrimaryCk(true);
        } else if (request.getParameter("inpprimarychk").equals("false")) {
          empaddrs.setPrimaryCk(false);
        }

        OBDal.getInstance().save(empaddrs);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();

        empAddId = empaddrs.getId();
        log4j.debug("empAddId:" + empaddrs.getId());
        request.setAttribute("savemsg", "Success");

        if (nextTab.equals("") || nextTab.equals("null")) {
          if (submitType.equals("SaveNew")) {
            empAddId = "";
            request.setAttribute("inpAddressId", "");
            action = "EditView";
          } else if (submitType.equals("Save")) {
            request.setAttribute("inpAddressId", (empAddId == null ? "" : empAddId));
            action = "EditView";
          } else if (submitType.equals("SaveGrid")) {
            empAddId = "";
            action = "GridView";
          }
        } else {
          ServletContext context = this.getServletContext();
          if (!nextTab.equals("") && !nextTab.equals("DOC") && !nextTab.equals("null")) {
            log4j.debug("nextTab:" + nextTab);
            String redirectStr = dao.redirectStr(nextTab, employeeId, inpempstatus,
                inpEmployeeStatus);
            response.sendRedirect(context.getContextPath() + redirectStr);
          }
        }

      }
      if (action.equals("") || action.equals("EditView")) {

        log4j.debug("EditView");
        log4j.debug("inpEmployeeId:" + request.getParameter("inpEmployeeId"));
        log4j.debug("empAddId" + empAddId);
        ehcmempstatusv view = OBDal.getInstance().get(ehcmempstatusv.class, employeeId);
        // EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
        // view.getEhcmEmpPerinfo().getId());
        String hiredate = OBDal.getInstance()
            .get(EhcmEmpPerInfo.class, view.getEhcmEmpPerinfo().getId()).getHiredate().toString();
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("Employee_ChildOrg", Utility.getAccessibleOrg(vars));
        EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class,
            view.getEhcmEmpPerinfo().getId());
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
        if (request.getParameter("inpEmpStatus") != null) {
          request.setAttribute("inpEmpStatus", request.getParameter("inpEmpStatus").toString());
        }
        if (request.getParameter("inpEmployeeStatus") != null) {
          request.setAttribute("inpEmployeeStatus",
              request.getParameter("inpEmployeeStatus").toString());
        }
        request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
        request.setAttribute("inpEmployeeId", employeeId);
        request.setAttribute("inpName1", objEmployee.getArabicfullname());
        request.setAttribute("inpName2", objEmployee.getName().concat(" ").concat(
            StringUtils.isNotEmpty(objEmployee.getFathername()) ? objEmployee.getFathername() : "")
            .concat(" ")
            .concat(StringUtils.isNotEmpty(objEmployee.getGrandfathername())
                ? objEmployee.getGrandfathername()
                : ""));
        request.setAttribute("CancelHiring",
            dao.checkEmploymentStatusCancel(vars.getClient(), employeeId));

        if (empAddId != null && empAddId != "" && !empAddId.equals("null")) {

          DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
          SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
          String date = df.format(new Date());
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          vo = dao.getEmployeeAddress(employeeId);
          // EHCMEmpAddress objEmpAddress = OBDal.getInstance().get(EHCMEmpAddress.class,
          // employeeId);
          request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
          request.setAttribute("inpAddressId", vo.getEmpAddId());
          request.setAttribute("inpAddressStyleId", vo.getAddressStyleId());
          request.setAttribute("inpAddressStyleName", vo.getAddressStyleId() == null ? ""
              : dao.getAddressStyleName(vo.getAddressStyleId()));
          request.setAttribute("inpStartDate1", vo.getStartDate());
          request.setAttribute("inpEndDate1", vo.getEndDate());
          request.setAttribute("inpCountryId", vo.getCountryId() == null ? "" : vo.getCountryId());
          request.setAttribute("inpCountryName",
              vo.getCountryId() == null ? "" : dao.countryName(vo.getCountryId()));
          request.setAttribute("inpSecCountryId",
              vo.getSecCountryId() == null ? "" : vo.getSecCountryId());

          if (vo.getSecCountryId() == null || vo.getSecCountryId() == "") {
            request.setAttribute("inpSecCountryName", "");
          } else {
            request.setAttribute("inpSecCountryName",
                vo.getSecCountryId() == null ? "" : dao.countryName(vo.getSecCountryId()));
          }

          request.setAttribute("inpCityId", vo.getCityId() == null ? "" : vo.getCityId());

          request.setAttribute("inpCityName",
              vo.getCityId() == null ? "" : dao.cityName(vo.getCityId()));

          request.setAttribute("inpSecCityId", vo.getSecCityId() == null ? "" : vo.getSecCityId());

          if (vo.getSecCityId() == null || vo.getSecCityId() == "") {
            request.setAttribute("inpSecCityName", "");
          } else {
            request.setAttribute("inpSecCityName",
                vo.getSecCityId() == null ? "" : dao.cityName(vo.getSecCityId()));
          }
          log4j.debug("sec city value:" + vo.getSecCityId());

          request.setAttribute("inpDistrict", vo.getDistrict());
          request.setAttribute("inpStreet", vo.getStreet());
          request.setAttribute("inpAdd1", vo.getAddress1());
          request.setAttribute("inpAdd2", vo.getAddress2());
          request.setAttribute("inpPostBox", vo.getPostBox());
          request.setAttribute("inpPostalcode", vo.getPostalCode());
          request.setAttribute("inpStartDate2", vo.getStartDate1());
          request.setAttribute("inpEndDate2", vo.getEndDate1());
          request.setAttribute("inpprimarychk", vo.getCheckbox());
          log4j.debug("primarychk:" + vo.getCheckbox());

          log4j.debug("activeflag:" + vo.getActive());
          log4j.debug("endate2:" + vo.getEndDate1());

          // request.setAttribute("inpSecCity", vo.getSecCityId());
          request.setAttribute("inpSecDistrict", vo.getSecDistrict());
          request.setAttribute("inpSecStreet", vo.getSecStreet());
          request.setAttribute("inpSecPostbox", vo.getSecPostBox());
          request.setAttribute("inpSecAdd1", vo.getSecAddress1());
          request.setAttribute("inpSecAdd2", vo.getSecAddress2());
          request.setAttribute("inpSecPostalcode", vo.getSecPostalCode());
          request.setAttribute("inpActiveflag", vo.getActive());
          request.setAttribute("today", date);
          request.setAttribute("inpEmployeeId", request.getParameter("inpEmployeeId"));
          request.setAttribute("inpHireDate", UtilityDAO.convertTohijriDate(hiredate).toString());

          dispatch = request
              .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/employeeaddress/EmployeeAddress.jsp");

        } else if (empAddId == null || empAddId == "" || empAddId.equals("null")) {
          DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
          SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
          String date = df.format(new Date());
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          request.setAttribute("today", date);

          request.setAttribute("inpEmployeeId", request.getParameter("inpEmployeeId"));
          request.setAttribute("inpprimarychk", "Y");

          request.setAttribute("inpActiveflag", "Y");
          request.setAttribute("inpCityId", "");
          request.setAttribute("inpSecCityId", "");
          request.setAttribute("inpAddressStyleId", "");
          request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
          List<EmployeeVO> ls = dao.getDefaultCountry(vars.getClient());
          for (EmployeeVO vo1 : ls) {
            if (vo1.getIsdefault().equals("Y")) {
              request.setAttribute("inpCountryId", vo1.getCountryId());
              request.setAttribute("inpCountryName", dao.countryName(vo1.getCountryId()));
              request.setAttribute("inpSecCountryId", vo1.getCountryId());
              request.setAttribute("inpSecCountryName", dao.countryName(vo1.getCountryId()));

              break;
            }
          }

          dispatch = request
              .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/employeeaddress/EmployeeAddress.jsp");
        }
      }
    } catch (final Exception e) {
      dispatch = request.getRequestDispatcher("../web/jsp/ErrorPage.jsp");
      log4j.error("Error in Employment : ", e);
    } finally {
      try {
        OBContext.restorePreviousMode();
        con.close();
        if (dispatch != null) {
          response.setContentType("text/html; charset=UTF-8");
          response.setCharacterEncoding("UTF-8");
          dispatch.include(request, response);
        } else
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (final Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log4j.error("Error in Employment : ", e);
      }
    }

  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }
}
