package sa.elm.ob.hcm.ad_forms.MedicalInsurance.ajax;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;

import sa.elm.ob.hcm.ad_forms.MedicalInsurance.dao.MedicalInsuranceDAOImpl;
import sa.elm.ob.utility.util.Utility;

/**
 * @author Priyanka Ranjan 17-03-2018
 */
public class MedicalInsuranceAjax extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Connection con = null;
    MedicalInsuranceDAOImpl dao = null;
    MedicalInsuranceDAOImpl medicalInsuranceDAOImpl = new MedicalInsuranceDAOImpl();
    try {
      OBContext.setAdminMode();
      con = getConnection();
      dao = new MedicalInsuranceDAOImpl(con);
      String action = (request.getParameter("action") == null ? ""
          : request.getParameter("action"));
      String medicalInsurancId = request.getParameter("inpMedInsId") == null ? ""
          : request.getParameter("inpMedInsId");
      String gregorianStartDate = "", gregorianEndDate = "";

      if (action.equals("getMedicalInsuranceList")) {
        JSONObject result = new JSONObject();

        try {
          String employeeId = request.getParameter("inpEmployeeId") == null ? ""
              : request.getParameter("inpEmployeeId");
          // String medicalInsurancId = request.getParameter("inpMedInsId") == null ? ""
          // : request.getParameter("inpMedInsId");
          result.put("page", "0");
          result.put("total", "0");
          result.put("records", "0");
          result.put("rows", new JSONArray());
          String searchFlag = request.getParameter("_search");
          JSONObject searchAttr = new JSONObject();
          searchAttr.put("rows", request.getParameter("rows").toString());
          searchAttr.put("page", request.getParameter("page").toString());
          searchAttr.put("search", searchFlag);
          searchAttr.put("sortName", request.getParameter("sidx").toString());
          searchAttr.put("sortType", request.getParameter("sord").toString());
          searchAttr.put("offset", "0");

          if (Boolean.valueOf(searchFlag)) {
            if (!StringUtils.isEmpty(request.getParameter("insucompname"))) {
              searchAttr.put("insucompname",
                  request.getParameter("insucompname").replace("'", "''"));
            }
            if (!StringUtils.isEmpty(request.getParameter("dependents"))
                && !request.getParameter("dependents").equals("0")) {
              searchAttr.put("dependents", request.getParameter("dependents").replace("'", "''"));
            }
            if (!StringUtils.isEmpty(request.getParameter("insuschema"))
                && !request.getParameter("insuschema").equals("0")) {
              searchAttr.put("insuschema", request.getParameter("insuschema").replace("'", "''"));
            }
            if (!StringUtils.isEmpty(request.getParameter("memshipno"))) {
              searchAttr.put("memshipno", request.getParameter("memshipno").replace("'", "''"));
            }
            if (!StringUtils.isEmpty(request.getParameter("inscategory"))
                && !request.getParameter("inscategory").equals("0")) {
              searchAttr.put("inscategory", request.getParameter("inscategory").replace("'", "''"));
            }
            if (!StringUtils.isEmpty(request.getParameter("startdate"))) {
              gregorianStartDate = Utility.convertToGregorian(request.getParameter("startdate"));
              searchAttr.put("startdate",
                  request.getParameter("startdate_s") + "##" + gregorianStartDate);
            }
            if (!StringUtils.isEmpty(request.getParameter("enddate"))) {
              gregorianEndDate = Utility.convertToGregorian(request.getParameter("enddate"));
              searchAttr.put("enddate",
                  request.getParameter("enddate_s") + "##" + gregorianEndDate);
            }
          }

          result = dao.getMedicalInsuranceList(vars.getClient(), employeeId, searchAttr,
              medicalInsurancId);
        } catch (Exception e) {
          log4j.error("Exception in MedicalInsuranceAjax - getMedicalInsuranceList : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(result.toString());
        }
      } else if (action.equals("deleteMedicalInsurance")) {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write("<deleteMedicalInsurance>");
        response.getWriter().write("<Response>"
            + medicalInsuranceDAOImpl.deleteMedicalInsurance(medicalInsurancId) + "</Response>");
        response.getWriter().write("</deleteMedicalInsurance>");
      } else if (action.equals("CheckInsuranceAlreadyExist")) {
        StringBuffer sb = new StringBuffer();
        try {
          Boolean chk = dao.checkInsuranceAlreadyExistsForDependent(
              request.getParameter("inpdependents"), request.getParameter("inpMembershipNo"),
              request.getParameter("inpempId"), request.getParameter("inpmedicalinsuId"),
              vars.getClient());
          sb.append("<CheckInsuranceAlreadyExist>");
          sb.append("<value>" + chk + "</value>");
          sb.append("</CheckInsuranceAlreadyExist>");
        } catch (final Exception e) {
          log4j.error("Exception in MedicalInsuranceAjax - CheckInsuranceAlreadyExist : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      } else if (action.equals("getDependent")) {

        JSONObject jsob = null;
        try {
          OBContext.setAdminMode();
          jsob = dao.getDependents(vars.getClient(), request.getParameter("employeeId"),
              request.getParameter("searchTerm"),
              Integer.parseInt(request.getParameter("pageLimit")),
              Integer.parseInt(request.getParameter("page")));

        } finally {
          response.setContentType("text/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(jsob.toString());
        }
      } else if (action.equals("getInsuranceSchema")) {

        JSONObject jsob = null;
        try {
          OBContext.setAdminMode();
          jsob = dao.getInsuranceSchema(vars.getClient(), request.getParameter("searchTerm"),
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
      log4j.error("Error in MedicalInsuranceAjax : ", e);
    } finally {
      OBContext.restorePreviousMode();
      try {
        con.close();
      } catch (final SQLException e) {
        log4j.error("Error in MedicalInsuranceAjax : ", e);
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }

  public String getServletInfo() {
    return "MedicalInsuranceAjax Servlet";
  }

}
