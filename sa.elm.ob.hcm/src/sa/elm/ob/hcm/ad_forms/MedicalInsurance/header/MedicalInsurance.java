package sa.elm.ob.hcm.ad_forms.MedicalInsurance.header;

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
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmMedicalInsurance;
import sa.elm.ob.hcm.ehcmempstatusv;
import sa.elm.ob.hcm.ad_forms.MedicalInsurance.dao.MedicalInsuranceDAOImpl;
import sa.elm.ob.hcm.ad_forms.MedicalInsurance.vo.MedicalInsuranceVO;
import sa.elm.ob.hcm.ad_forms.dependents.vo.DependentVO;
import sa.elm.ob.hcm.ad_forms.employee.dao.EmployeeDAO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author Priyanka Ranjan 17-03-2018
 *
 */

public class MedicalInsurance extends HttpSecureAppServlet {

  /**
   * Medical Insurance related to employee details
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
    MedicalInsuranceDAOImpl medicalInsuranceDAOImpl = new MedicalInsuranceDAOImpl();
    MedicalInsuranceDAOImpl medicalInsuranceDAOImplconn = null;
    JSONObject searchAttr = new JSONObject();
    JSONObject result = new JSONObject(), json = null;
    JSONArray jsonArray = new JSONArray();
    // String insuranceCategory = "E";
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String lang = vars.getLanguage();

    try {
      OBContext.setAdminMode();
      DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
      SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
      String date = df.format(new Date());
      date = dateYearFormat.format(df.parse(date));
      date = UtilityDAO.convertTohijriDate(date);

      EmployeeDAO empdao = null;
      // MedicalInsuranceDAOImpl depdao = null;
      DependentVO depvo = null;
      EmployeeDAO dao1 = null;
      MedicalInsuranceVO vo = null;

      String medInsId = (request.getParameter("inpMedInsId") == null ? ""
          : request.getParameter("inpMedInsId"));
      employeeId = (request.getParameter("inpEmployeeId") == null ? ""
          : request.getParameter("inpEmployeeId"));

      Connection con = getConnection();
      medicalInsuranceDAOImplconn = new MedicalInsuranceDAOImpl(con);
      empdao = new EmployeeDAO(con);
      // depdao = new MedicalInsuranceDAOImpl(con);
      dao1 = new EmployeeDAO(con);

      EhcmMedicalInsurance depent = null;
      if (submittype.equals("Save") || submittype.equals("SaveNew")
          || submittype.equals("SaveGrid")) {
        log4j.debug("medInsId: " + medInsId);

        // need to insert a record in medical Insurance table
        vo = new MedicalInsuranceVO();
        vo.setInsuranceCompanyName(request.getParameter("inpInsComName"));
        vo.setInsuranceCategory(request.getParameter("inpInsuCategory"));
        vo.setDependents(request.getParameter("inpDependent"));
        vo.setInsuranceSchema(request.getParameter("inpInsuSchema"));
        vo.setMemberShipNo(request.getParameter("inpMemshipNo"));
        vo.setStartDate(request.getParameter("inpStartDate"));
        vo.setEndDate(request.getParameter("inpEndDate"));
        vo.setEmployee(employeeId);

        if (medInsId.equals("") || medInsId.equals("null"))
          // medInsId = dao.addEmployment(vars.getClient(), vars.getUser(), vo, vars);
          medInsId = medicalInsuranceDAOImpl.addMedicalInsurance(vars.getClient(), vars.getUser(),
              vo, vars);
        else
          medInsId = medicalInsuranceDAOImpl.updateMedicalInsurance(vars.getClient(),
              vars.getUser(), vo, medInsId, vars);

        if (medInsId != null)
          request.setAttribute("savemsg", Resource.getProperty("hcm.success", lang));
        else
          request.setAttribute("savemsg", Resource.getProperty("hcm.error", lang));
        if (nextTab.equals("") || nextTab.equals("null")) {
          if (submittype.equals("SaveNew")) {
            medInsId = "";
            request.setAttribute("inpMedInsId", "");
            action = "EditView";
          } else if (submittype.equals("Save")) {
            request.setAttribute("inpMedInsId", (medInsId == null ? "" : medInsId));
            action = "EditView";
          } else if (submittype.equals("SaveGrid")) {
            medInsId = "";
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

        request.setAttribute("today", date);

        if (medInsId != null && medInsId != "" && !medInsId.equals("null")) {
          result = medicalInsuranceDAOImplconn.getMedicalInsEditList(vars.getClient(), employeeId,
              searchAttr, medInsId);
          if (result != null) {
            jsonArray = result.getJSONArray("rows");
            if (jsonArray.length() > 0) {
              json = jsonArray.getJSONObject(0);
              request.setAttribute("inpInsComName", json.getString("insucompname"));
              if (json.has("dependents")) {
                request.setAttribute("inpDependentId", json.getString("dependents"));
                request.setAttribute("inpDependentName",
                    dao1.dependentsName(json.getString("dependents")));

              }

              else {
                request.setAttribute("inpDependentId", "");
              }
              request.setAttribute("inpMemshipNo", json.getString("memshipno"));
              request.setAttribute("inpStartDate", json.getString("startdate"));
              if (json.has("enddate")) {
                request.setAttribute("inpEndDate", json.getString("enddate"));
              } else {
                request.setAttribute("inpEndDate", "");
              }
              request.setAttribute("inpInsuCategoryKey", json.getString("inscategorykey"));
              request.setAttribute("inpInsuSchemaId", json.getString("insuschemaId"));
              request.setAttribute("inpInsuSchemaName",
                  dao1.insuranceSchemaName(json.getString("insuschemaId")));
            }

          }
          request.setAttribute("inpEmployeeId", (employeeId == null ? "" : employeeId));
          // request.setAttribute("inpDependentId", (medInsId == null ? "" : medInsId));
          if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
            request.setAttribute("inpAddressId", null);
          else
            request.setAttribute("inpAddressId", dao1.getEmployeeAddressId(employeeId));

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

          // request.setAttribute("inpInsuSchema",
          // medicalInsuranceDAOImpl.getInsuranceSchema(vars.getClient()));

          // request.setAttribute("inpDependent",
          // medicalInsuranceDAOImpl.getDependents(vars.getClient(), employeeId));
          request.setAttribute("inpInsuCategory",
              medicalInsuranceDAOImpl.getInsuCategoryReference());
          request.setAttribute("inpMedInsId", (medInsId == null ? "" : medInsId));
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
          request.setAttribute("inpInsuSchemaId", "");

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
              objEmployee.getName().concat(" ").concat(objEmployee.getFathername()).concat(" ")
                  .concat(objEmployee.getGrandfathername()));
          if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
            request.setAttribute("inpAddressId", null);
          else {

            request.setAttribute("inpAddressId", empdao.getEmployeeAddressId(employeeId));
          }
          // request.setAttribute("inpInsuSchema",
          // medicalInsuranceDAOImpl.getInsuranceSchema(vars.getClient()));

          // request.setAttribute("inpDependent",
          // medicalInsuranceDAOImpl.getDependents(vars.getClient(), employeeId));
          request.setAttribute("inpInsuCategory",
              medicalInsuranceDAOImpl.getInsuCategoryReference());
          // request.setAttribute("inpInsuCategoryKey", insuranceCategory);
          request.setAttribute("CancelHiring",
              dao1.checkEmploymentStatusCancel(vars.getClient(), employeeId));
        }
        if (request.getParameter("inpEmployeeStatus") != null) {
          request.setAttribute("inpEmployeeStatus",
              request.getParameter("inpEmployeeStatus").toString());
        }
        if (request.getParameter("inpEmpStatus") != null) {
          request.setAttribute("inpEmpStatus", request.getParameter("inpEmpStatus").toString());
        }
        dispatch = request.getRequestDispatcher(
            "../web/sa.elm.ob.hcm/jsp/employeemedicalinsurance/employeemedicalinsurance.jsp");

      } else if (action.equals("") || action.equals("GridView")) {
        ehcmempstatusv view = OBDal.getInstance().get(ehcmempstatusv.class, employeeId);
        log4j.debug("employeeId: " + employeeId);
        log4j.debug("view: " + view);
        log4j.debug("view: " + view.getEhcmEmpPerinfo());
        EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class,
            view.getEhcmEmpPerinfo().getId());
        log4j.debug("objEmployee: " + objEmployee);
        medInsId = request.getParameter("inpMedInsId");
        action = "GridView";
        log4j.debug("employeeId: " + employeeId);
        request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
        request.setAttribute("inpEmployeeId", (employeeId == null ? "" : employeeId));
        request.setAttribute("inpMedInsId", (medInsId == null ? "" : medInsId));

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
        request.setAttribute("inpDependentId", medicalInsuranceDAOImpl
            .getDependents(vars.getClient(), objEmployee.getId(), null, 0, 0));
        request.setAttribute("inpInsuSchemaId",
            medicalInsuranceDAOImpl.getInsuranceSchema(vars.getClient(), null, 0, 0));
        if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
          request.setAttribute("inpAddressId", null);
        else
          request.setAttribute("inpAddressId", dao1.getEmployeeAddressId(employeeId));

        request.setAttribute("inpInsuCategory", medicalInsuranceDAOImpl.getInsuCategoryReference());

        // request.setAttribute("inpInsuSchema",
        // medicalInsuranceDAOImpl.getInsuranceSchema(vars.getClient()));

        // request.setAttribute("inpDependent",
        // medicalInsuranceDAOImpl.getDependents(vars.getClient(), employeeId));
        request.setAttribute("CancelHiring",
            dao1.checkEmploymentStatusCancel(vars.getClient(), employeeId));
        dispatch = request.getRequestDispatcher(
            "../web/sa.elm.ob.hcm/jsp/employeemedicalinsurance/employeemedicalinsuranceList.jsp");
      }

    } catch (final Exception e) {
      dispatch = request.getRequestDispatcher("../web/jsp/ErrorPage.jsp");
      log4j.error("Error in MedicalInsuranceDAO : ", e);
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
        log4j.error("Error in MedicalInsuranceDAO : ", e);
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }
}
