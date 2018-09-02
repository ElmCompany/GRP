package sa.elm.ob.hcm.ad_forms.employee.ajax;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;

import sa.elm.ob.hcm.ad_forms.employee.dao.EmployeeDAO;
import sa.elm.ob.hcm.ad_forms.employee.vo.EmployeeVO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.utility.util.Utility;

public class EmployeeAjax extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String lang = vars.getLanguage();
    Connection con = null;
    EmployeeDAO dao = null;
    try {
      con = getConnection();
      // commonDAO = new CommonDAO(con);
      dao = new EmployeeDAO(con);
      String action = (request.getParameter("action") == null ? ""
          : request.getParameter("action"));
      if (action.equals("DeleteEmployee")) {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write("<DeleteEmployee>");
        response.getWriter()
            .write("<Response>" + dao.deleteEmployee(request.getParameter("inpEmployeeId"),
                request.getParameter("inpEmpstatus")) + "</Response>");
        response.getWriter().write("</DeleteEmployee>");
      } else if (action.equals("GetEmployeeList")) {
        String employeeId = request.getParameter("inpEmployeeId");

        if (request.getParameter("DeleteEmployee") != null
            && request.getParameter("DeleteEmployee").length() == 32)
          // if(dao.deleteEmployee(request.getParameter("DeleteEmployee")))
          employeeId = "";
        String searchFlag = request.getParameter("_search");
        String hijiriHireDate = "";
        EmployeeVO employeeVO = new EmployeeVO();
        if (searchFlag != null && searchFlag.equals("true")) {
          if (!StringUtils.isEmpty(request.getParameter("org")))
            employeeVO.setOrgId(request.getParameter("org"));
          if (!StringUtils.isEmpty(request.getParameter("name")))
            employeeVO.setEmpName(request.getParameter("name").replace("'", "''"));
          if (!StringUtils.isEmpty(request.getParameter("arabicname")))
            employeeVO.setArbfourthName(request.getParameter("arabicname").replace("'", "''"));
          if (!StringUtils.isEmpty(request.getParameter("Salutation"))
              && !"0".equals(request.getParameter("Salutation")))
            employeeVO.setSaluatation(request.getParameter("Salutation").replace("'", "''"));
          if (!StringUtils.isEmpty(request.getParameter("value")))
            employeeVO.setEmpNo(request.getParameter("value").replace("'", "''"));
          if (!StringUtils.isEmpty(request.getParameter("status"))
              && !"0".equals(request.getParameter("status")))
            employeeVO.setStatus(request.getParameter("status").replace("'", "''"));
          if (!StringUtils.isEmpty(request.getParameter("emp_type"))
              && !"0".equals(request.getParameter("emp_type")))
            employeeVO.setCategoryId(request.getParameter("emp_type").replace("'", "''"));
          if (!StringUtils.isEmpty(request.getParameter("isactive"))
              && !"0".equals(request.getParameter("isactive")))
            employeeVO.setActive(request.getParameter("isactive").replace("'", "''"));
          if (!StringUtils.isEmpty(request.getParameter("hiredate"))) {
            hijiriHireDate = Utility.convertToGregorian(request.getParameter("hiredate"));
            employeeVO.setHiredate(request.getParameter("hiredate_s") + "##" + hijiriHireDate);
          }
          if (!StringUtils.isEmpty(request.getParameter("gender"))
              && !"0".equals(request.getParameter("gender")))
            employeeVO.setGender(request.getParameter("gender"));
          if (!StringUtils.isEmpty(request.getParameter("nationality_identifier")))
            employeeVO.setNationalCode(request.getParameter("nationality_identifier"));
          if (!StringUtils.isEmpty(request.getParameter("persontype")))
            employeeVO.setPersonType(request.getParameter("persontype"));
          if (!StringUtils.isEmpty(request.getParameter("employeeStatus"))
              && !"0".equals(request.getParameter("employeeStatus"))) {
            employeeVO.setEmployeeStatus(request.getParameter("employeeStatus"));
          }
        }
        JSONObject searchAttr = new JSONObject();
        searchAttr.put("rows", request.getParameter("rows").toString());
        searchAttr.put("page", request.getParameter("page").toString());
        searchAttr.put("search", searchFlag);
        searchAttr.put("sortName", request.getParameter("sidx").toString());
        searchAttr.put("sortType", request.getParameter("sord").toString());
        List<EmployeeVO> list = dao.getEmployeeList(vars.getClient(),
            request.getSession().getAttribute("Employee_ChildOrg").toString(), employeeVO,
            searchAttr, employeeId, lang);
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        StringBuffer xmlData = new StringBuffer("<?xml version='1.0' encoding='utf-8'?><rows>");
        if (list.size() > 0) {
          String[] pageDetails = list.get(0).getStatus().split("_");
          xmlData.append("<page>" + pageDetails[0] + "</page><total>" + pageDetails[1]
              + "</total><records>" + pageDetails[2] + "</records>");
          for (int i = 1; i < list.size(); i++) {
            employeeVO = (EmployeeVO) list.get(i);
            boolean cancelHiringResult = dao.checkEmploymentStatusCancel(vars.getClient(),
                employeeVO.getId());
            boolean hiringDecisionResult = dao.checkHiringDecisionStatus(vars.getClient(),
                employeeVO.getId());
            xmlData.append("<row id='" + employeeVO.getId() + "'>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getOrgName() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getEmpNo() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getSaluatation() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getEmpName() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getEmpArabicName() + "]]></cell>");
            if (employeeVO.getGender().equals("F"))
              xmlData.append(
                  "<cell><![CDATA[" + Resource.getProperty("hcm.female", lang) + "]]></cell>");
            else
              xmlData.append(
                  "<cell><![CDATA[" + Resource.getProperty("hcm.male", lang) + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getNationalCode() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getHireDate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getCategoryId() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getPersonType() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getPerStatus() + "]]></cell>");

            if (employeeVO.getActive().equals("Y"))
              xmlData
                  .append("<cell><![CDATA[" + Resource.getProperty("hcm.yes", lang) + "]]></cell>");
            else
              xmlData
                  .append("<cell><![CDATA[" + Resource.getProperty("hcm.no", lang) + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getAddressId() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getEmployeeCategory() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getChangereason() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getEmploymentstatus() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getDelegationcount() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getShortstatus() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getEmployeeId() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + cancelHiringResult + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + hiringDecisionResult + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + employeeVO.getEmployeeStatus() + "]]></cell>");
            xmlData.append("</row>");

          }
        } else
          xmlData
              .append("<page>" + 0 + "</page><total>" + 0 + "</total><records>" + 0 + "</records>");
        xmlData.append("</rows>");
        response.getWriter().write(xmlData.toString());
      } else if (action.equals("GetPersonActiveType")) {
        StringBuffer sb = new StringBuffer();
        try {
          List<EmployeeVO> vo = dao.getactionType(vars.getClient(),
              request.getParameter("inpActiveTypeId"), null);
          sb.append("<GetActiveDetails>");

          for (EmployeeVO VO : vo) {
            sb.append("<ID>" + VO.getActTypeId() + "</ID>");
            sb.append("<NAME>" + VO.getActTypeName() + "</NAME>");
            sb.append("<PERTYPE>" + VO.getPersonType() + "</PERTYPE>");

          }
          sb.append("</GetActiveDetails>");
        } catch (final Exception e) {
          log4j.error("Exception in PayrollProcess - GetPayrollYear : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      }

      else if (action.equals("GetCity")) {
        StringBuffer sb = new StringBuffer();
        JSONObject jsob = null;
        try {
          log4j.debug("getCity");
          jsob = dao.getCity(vars.getClient(), request.getParameter("inpCountry"),
              request.getParameter("searchTerm"),
              Integer.parseInt(request.getParameter("pageLimit")),
              Integer.parseInt(request.getParameter("page")));

        } catch (final Exception e) {
          log4j.error("Exception in GetCity - in employee : ", e);
        } finally {
          response.setContentType("text/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(jsob.toString());

        }
      } else if (action.equals("CheckEmployeeNo")) {
        StringBuffer sb = new StringBuffer();
        try {
          Boolean chk = dao.checkEmpAlreadyExists(vars.getClient(),
              request.getParameter("inpEmpNo"), request.getParameter("inpstatus"),
              request.getParameter("inpEmpId"));
          sb.append("<CheckEmployeeNo>");
          sb.append("<value>" + chk + "</value>");
          sb.append("</CheckEmployeeNo>");
        } catch (final Exception e) {
          log4j.error("Exception in PayrollProcess - GetPayrollYear : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      } else if (action.equals("getDays")) {
        StringBuffer sb = new StringBuffer();
        try {
          int chk = dao.getDays(vars.getClient(), request.getParameter("monthyear"));
          sb.append("<GetDay>");
          sb.append("<noofdays>" + chk + "</noofdays>");
          sb.append("</GetDay>");
        } catch (final Exception e) {
          log4j.error("Exception in PayrollProcess - GetPayrollYear : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      } else if (action.equals("checkRecEmpment")) {
        StringBuffer sb = new StringBuffer();
        try {
          boolean chk = dao.getEmployementcount(vars.getClient(),
              request.getParameter("inpEmployeeId"));
          sb.append("<ChkEmp>");
          sb.append("<result>" + chk + "</result>");
          sb.append("</ChkEmp>");
        } catch (final Exception e) {
          log4j.error("Exception in PayrollProcess - GetPayrollYear : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      } else if (action.equals("checkAlreadyCancel")) {
        StringBuffer sb = new StringBuffer();
        try {
          boolean chk = dao.checkAlreadyCancel(vars.getClient(), request.getParameter("inpEmpNo"));
          sb.append("<ChkEmp>");
          sb.append("<result>" + chk + "</result>");
          sb.append("</ChkEmp>");
        } catch (final Exception e) {
          log4j.error("Exception in PayrollProcess - GetPayrollYear : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      } else if (action.equals("checkCancel")) {
        StringBuffer sb = new StringBuffer();
        try {
          boolean chk = dao.checkCancelcondition(vars.getClient(),
              request.getParameter("inpEmpid"));
          sb.append("<ChkEmp>");
          sb.append("<result>" + chk + "</result>");
          sb.append("</ChkEmp>");
        } catch (final Exception e) {
          log4j.error("Exception in checking cancel condtion : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      } else if (action.equals("checkAlreadyCancelIssue")) {
        StringBuffer sb = new StringBuffer();
        try {
          boolean chk = dao.checkAlreadyCancelIssue(vars.getClient(),
              request.getParameter("inpEmpNo"));
          sb.append("<ChkEmp>");
          sb.append("<result>" + chk + "</result>");
          sb.append("</ChkEmp>");
        } catch (final Exception e) {
          log4j.error("Exception in PayrollProcess - GetPayrollYear : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      } else if (action.equals("getStartdate")) {
        StringBuffer sb = new StringBuffer();
        log4j.debug("sb:" + sb.toString());
        try {
          String startdate = dao.getStarteDate(vars.getClient(), request.getParameter("inpEmpNo"),
              true);
          sb.append("<ChkEmp>");
          sb.append("<result>" + startdate + "</result>");
          sb.append("</ChkEmp>");
        } catch (final Exception e) {
          log4j.error("Exception in employeeajax - getStartdate : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      } else if (action.equals("CheckNationalNum")) {
        StringBuffer sb = new StringBuffer();
        EmployeeVO vo = null;
        try {
          vo = dao.checkNationalID(vars.getClient(), request.getParameter("inpNationalId"),
              request.getParameter("inpEmployeeId"));
          sb.append("<ChkNatID>");
          sb.append("<Valid>" + vo.isValue() + "</Valid>");
          sb.append("<Exist>" + vo.isResult() + "</Exist>");
          log4j.debug("vo.isResult():" + vo.isResult());
          sb.append("</ChkNatID>");
        } catch (final Exception e) {
          log4j.error("Exception in employee saving - checking national identifier : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      } else if (action.equals("Checkdatevalidation")) {
        StringBuffer sb = new StringBuffer();
        // EmployeeVO vo = null;
        try {
          String date1 = request.getParameter("hiredate");
          String date2 = request.getParameter("dobdate");
          boolean chk = dao.checkDateval(vars.getClient(), date1, date2);

          sb.append("<Checkdatevalidation>");
          sb.append("<value>" + chk + "</value>");
          sb.append("</Checkdatevalidation>");
        } catch (final Exception e) {
          log4j.error("Exception in employee saving - Checkdatevalidation: ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      } else if (action.equals("CheckBusinessPartner")) {
        StringBuffer sb = new StringBuffer();
        EmployeeVO vo = new EmployeeVO();
        try {
          vo = dao.checkBpartnerValidation(vars.getClient(), vars.getOrg(),
              request.getParameter("inpEmployeeId"), request.getParameter("inpSalText"));

          sb.append("<CheckBusinessPartner>");
          sb.append("<result>" + vo.getCategorycode() + "</result>");
          sb.append("<Exist>" + vo.isResult() + "</Exist>");
          log4j.debug("vo.isResult():" + vo.isResult());
          sb.append("</CheckBusinessPartner>");
        } catch (final Exception e) {
          log4j.error("Exception in CheckBusinessPartner  : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      } else if (action.equals("checkEmploymentStatus")) {
        log4j.debug("checkEmploymentStatus");
        StringBuffer sb = new StringBuffer();
        try {
          boolean chk = dao.checkEmploymentStatusCancel(vars.getClient(),
              request.getParameter("inpEmployeeId"));
          sb.append("<ChkEmployeeStatus>");
          sb.append("<result>" + chk + "</result>");
          sb.append("</ChkEmployeeStatus>");
        } catch (final Exception e) {
          log4j.error("Exception in checkEmploymentStatus : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      } else if (action.equals("getCountry")) {

        JSONObject jsob = null;
        try {
          OBContext.setAdminMode();
          jsob = dao.getCountry(vars.getClient(), request.getParameter("searchTerm"),
              Integer.parseInt(request.getParameter("pageLimit")),
              Integer.parseInt(request.getParameter("page")));

        } finally {
          response.setContentType("text/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(jsob.toString());
        }
      } else if (action.equals("getAddressStyle")) {

        JSONObject jsob = null;
        try {
          OBContext.setAdminMode();
          jsob = dao.getAdrsStyle(vars.getClient(), request.getParameter("searchTerm"),
              Integer.parseInt(request.getParameter("pageLimit")),
              Integer.parseInt(request.getParameter("page")));

        } finally {
          response.setContentType("text/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(jsob.toString());
        }
      }

    } catch (final Exception e) {
      log4j.error("Error in EmployeeAjax : ", e);
    } finally {
      try {
        con.close();
      } catch (final SQLException e) {
        log4j.error("Error in EmployeeAjax : ", e);
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }

  public String getServletInfo() {
    return "EmployeeAjax Servlet";
  }
}