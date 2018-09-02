package sa.elm.ob.hcm.ad_forms.qualification.header;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
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

import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ehcmempstatusv;
import sa.elm.ob.hcm.ehcmqualification;
import sa.elm.ob.hcm.ad_forms.employee.dao.EmployeeDAO;
import sa.elm.ob.hcm.ad_forms.qualification.dao.QualificationDAO;
import sa.elm.ob.hcm.ad_forms.qualification.vo.QualificationVO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class Qualification extends HttpSecureAppServlet {

  /**
   * Employee form details
   */
  private static final long serialVersionUID = 1L;
  private static final String TMP_DIR_PATH = "/tmp";
  private static final String DESTINATION_DIR_PATH = "/files";
  private File tmpDir = null, destinationDir = null;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;

    tmpDir = new File(TMP_DIR_PATH);
    if (!tmpDir.isDirectory()) {
      new File(TMP_DIR_PATH).mkdir();
    }

    String realPath = getServletContext().getRealPath(DESTINATION_DIR_PATH);
    destinationDir = new File(realPath);
    if (!destinationDir.isDirectory()) {
      new File(realPath).mkdir();
    }
  }

  @SuppressWarnings("unchecked")
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    QualificationDAO dao = null;
    Connection con = null;
    RequestDispatcher dispatch = null;
    VariablesSecureApp vars = null;

    QualificationVO vo = null;
    EmployeeDAO dao1 = null;
    try {
      con = getConnection();
      OBContext.setAdminMode();
      String action = (request.getParameter("inpAction") == null ? ""
          : request.getParameter("inpAction"));
      String qualid = (request.getParameter("inpQualificationId") == null ? ""
          : request.getParameter("inpQualificationId"));
      String employeeId = request.getParameter("inpEmployeeId") == null ? ""
          : request.getParameter("inpEmployeeId");
      String nextTab = (request.getParameter("inpNextTab") == null ? ""
          : request.getParameter("inpNextTab"));
      String inpempstatus = (request.getParameter("inpEmpStatus") == null ? ""
          : request.getParameter("inpEmpStatus"));
      log4j.debug("inpempstatus" + inpempstatus);
      String inpEmployeeStatus = (request.getParameter("inpEmployeeStatus") == null ? ""
          : request.getParameter("inpEmployeeStatus"));
      log4j.debug("action" + action);
      dao = new QualificationDAO(con);
      dao1 = new EmployeeDAO(con);
      ehcmqualification qualinfo = null;
      vars = new VariablesSecureApp(request);
      if (request.getParameter("SubmitType") != null
          && (request.getParameter("SubmitType").equals("Save")
              || request.getParameter("SubmitType").equals("SaveGrid")
              || request.getParameter("SubmitType").equals("SaveNew"))) {

        // need to insert a record in employee personal info table

        if (qualid.equals("") || qualid == null || qualid.equals("null")) {

          qualinfo = OBProvider.getInstance().get(ehcmqualification.class);
        } else {

          qualinfo = OBDal.getInstance().get(ehcmqualification.class, qualid);
        }
        if (qualid.equals("") || qualid == null || qualid.equals("null")) {

          qualinfo.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
          qualinfo.setOrganization(OBDal.getInstance().get(Organization.class, vars.getOrg()));
          qualinfo.setCreationDate(new java.util.Date());
          qualinfo.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        }

        qualinfo.setUpdated(new java.util.Date());
        qualinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));

        if (request.getParameter("inpStartDate") != null
            && request.getParameter("inpStartDate") != "")
          qualinfo
              .setStartDate(dao.convertGregorian(request.getParameter("inpStartDate").toString()));
        if (request.getParameter("inpEndDate") != null && request.getParameter("inpEndDate") != "")
          qualinfo.setEndDate(dao.convertGregorian(request.getParameter("inpEndDate").toString()));
        log4j.debug("inpexpirydate" + request.getParameter("inpexpirydate"));

        qualinfo
            .setExpirydate(dao.convertGregorian(request.getParameter("inpexpirydate").toString()));
        qualinfo.setLocation(request.getParameter("inpLocation").toString());
        qualinfo.setEdulevel(request.getParameter("inpeddulevel").toString());
        qualinfo.setEhcmEmpPerinfo(
            OBDal.getInstance().get(EhcmEmpPerInfo.class, request.getParameter("inpEmployeeId")));
        qualinfo.setDegree(request.getParameter("inpDegree").toString());
        log4j.debug("inpcompletionyear" + request.getParameter("inpcompletionyear"));
        qualinfo.setCompletionyear(request.getParameter("inpcompletionyear").toString());
        qualinfo.setLicensesub(request.getParameter("inpLicensesub").toString());
        qualinfo.setEstablishment(request.getParameter("inpestablishname").toString());
        OBDal.getInstance().save(qualinfo);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
        qualid = qualinfo.getId();
        log4j.debug("qualidsave" + qualid);
        if (nextTab.equals("")) {
          if (request.getParameter("SubmitType").equals("Save")) {
            action = "EditView";
            request.setAttribute("inpEmployeeId",
                (qualinfo.getId() == null ? "" : qualinfo.getId()));
          } else if (request.getParameter("SubmitType").equals("SaveNew")) {
            action = "EditView";
            qualid = "";
            request.setAttribute("inpQualificationId", "");
          } else if (request.getParameter("SubmitType").equals("SaveGrid")) {

            action = "GridView";
            qualid = "";
            request.setAttribute("inpQualificationId", "");
          }
        } else {

          ServletContext context = this.getServletContext();
          if (!nextTab.equals("") && !nextTab.equals("EMPQUAL")) {
            String redirectStr = dao1.redirectStr(nextTab, employeeId, inpempstatus,
                inpEmployeeStatus);
            response.sendRedirect(context.getContextPath() + redirectStr);
          }
        }

        request.setAttribute("savemsg", "Success");
      }

      if (action.equals("EditView")) {
        log4j.debug("EditView");
        log4j.debug("qualid:" + qualid);
        if (qualid != null && qualid != "" && !qualid.equals("null")) {
          log4j.debug("if:");

          DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
          SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
          String date = df.format(new Date());
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          vo = dao.getEmployeeEditList(qualid);
          log4j.debug("vo.getDegree()" + vo.getDegree());
          log4j.debug("vo.vo.getPersonid()" + vo.getPersonid());
          log4j.debug("vo.getEstablishment()" + vo.getEstablishment());
          request.setAttribute("inpQualificationId", qualid);
          request.setAttribute("inpEmployeeId", request.getParameter("inpEmployeeId"));
          request.setAttribute("inpStartDate", vo.getStartdate());
          request.setAttribute("inpexpirydate", vo.getExpirydate());
          request.setAttribute("inpEndDate", vo.getEnddate());
          request.setAttribute("inpeddulevel", vo.getEducationlevel());
          request.setAttribute("inpestablishname", Utility
              .escapeHTML((vo.getEstablishment().replace("\n", "\\n").replace("\r", "\\r"))));
          request.setAttribute("inpDegree", vo.getDegree());
          request.setAttribute("inpcompletionyear", vo.getCompletionyear());
          request.setAttribute("inpLocation", vo.getLocation());
          request.setAttribute("inpLicensesub", vo.getLicensesub());
          // EMPLOYEE
          EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
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
          request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
          request.setAttribute("inpName1", objEmployee.getArabicfullname());
          request.setAttribute("inpName2",
              objEmployee.getName().concat(" ").concat(objEmployee.getFathername()).concat(" ")
                  .concat(objEmployee.getGrandfathername()));
          if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
            request.setAttribute("inpAddressId", null);
          else
            request.setAttribute("inpAddressId", dao1.getEmployeeAddressId(employeeId));
          request.setAttribute("CancelHiring",
              dao1.checkEmploymentStatusCancel(vars.getClient(), employeeId));
          dispatch = request
              .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/qualification/qualification.jsp");
        }

        else if (qualid == null || qualid == "" || qualid.equals("null")) {

          DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
          SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
          String date = df.format(new Date());
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);

          request.setAttribute("inpStartDate", date);
          request.setAttribute("inpEndDate", null);
          request.setAttribute("inpEmployeeId", request.getParameter("inpEmployeeId"));
          request.setAttribute("inpexpirydate", null);
          request.setAttribute("inpeddulevel", null);
          request.setAttribute("inpestablishname", null);
          request.setAttribute("inpDegree", null);
          request.setAttribute("inpcompletionyear", null);
          request.setAttribute("inpLocation", null);
          request.setAttribute("inpLicensesub", null);
          EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
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
          request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
          request.setAttribute("inpName1", objEmployee.getArabicfullname());
          request.setAttribute("inpName2",
              objEmployee.getName().concat(" ")
                  .concat(StringUtils.isNotEmpty(objEmployee.getFathername())
                      ? objEmployee.getFathername()
                      : "")
                  .concat(" ")
                  .concat(StringUtils.isNotEmpty(objEmployee.getGrandfathername())
                      ? objEmployee.getGrandfathername()
                      : ""));
          if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
            request.setAttribute("inpAddressId", null);
          else
            request.setAttribute("inpAddressId", dao1.getEmployeeAddressId(employeeId));
          dispatch = request
              .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/qualification/qualification.jsp");

        }
      }

      else if (action.equals("") || action.equals("GridView")) {
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("Employee_ChildOrg", Utility.getAccessibleOrg(vars));
        ehcmempstatusv view = OBDal.getInstance().get(ehcmempstatusv.class, employeeId);
        EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class,
            view.getEhcmEmpPerinfo().getId());
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
        if (request.getParameter("inpEmpStatus") != null) {
          request.setAttribute("inpEmpStatus", request.getParameter("inpEmpStatus").toString());
        }
        if (request.getParameter("inpEmployeeStatus") != null) {
          request.setAttribute("inpEmployeeStatus",
              request.getParameter("inpEmployeeStatus").toString());
        }
        request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
        request.setAttribute("inpEmployeeId", employeeId);
        request.setAttribute("inpName1", objEmployee.getArabicfullname());
        request.setAttribute("inpName2", objEmployee.getName().concat(" ")
            .concat(objEmployee.getFathername() == null ? "" : objEmployee.getFathername())
            .concat(" ").concat(
                objEmployee.getGrandfathername() == null ? "" : objEmployee.getGrandfathername()));
        if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
          request.setAttribute("inpAddressId", null);
        else
          request.setAttribute("inpAddressId", dao1.getEmployeeAddressId(employeeId));

        request.setAttribute("CancelHiring",
            dao1.checkEmploymentStatusCancel(vars.getClient(), employeeId));
        dispatch = request
            .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/qualification/qualificationList.jsp");
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
