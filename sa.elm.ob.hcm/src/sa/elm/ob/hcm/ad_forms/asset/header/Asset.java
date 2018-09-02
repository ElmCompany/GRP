package sa.elm.ob.hcm.ad_forms.asset.header;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

import sa.elm.ob.hcm.EhcmEmpAsset;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ehcmempstatusv;
import sa.elm.ob.hcm.ad_forms.asset.dao.AssetDAO;
import sa.elm.ob.hcm.ad_forms.asset.vo.AssetVO;
import sa.elm.ob.hcm.ad_forms.employee.dao.EmployeeDAO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class Asset extends HttpSecureAppServlet {

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

    AssetDAO dao = null;
    Connection con = null;
    RequestDispatcher dispatch = null;
    VariablesSecureApp vars = null;

    AssetVO vo = null;
    EmployeeDAO dao1 = null;
    try {
      con = getConnection();
      OBContext.setAdminMode();
      String action = (request.getParameter("inpAction") == null ? ""
          : request.getParameter("inpAction"));
      String assetid = (request.getParameter("inpAssetId") == null ? ""
          : request.getParameter("inpAssetId"));
      String employeeId = request.getParameter("inpEmployeeId") == null ? ""
          : request.getParameter("inpEmployeeId");
      String nextTab = (request.getParameter("inpNextTab") == null ? ""
          : request.getParameter("inpNextTab"));
      String empCategory = (request.getParameter("inpempCategory") == null ? ""
          : request.getParameter("inpempCategory"));
      String inpempstatus = (request.getParameter("inpEmpStatus") == null ? ""
          : request.getParameter("inpEmpStatus"));
      log4j.debug("inpempstatus" + inpempstatus);
      String inpEmployeeStatus = (request.getParameter("inpEmployeeStatus") == null ? ""
          : request.getParameter("inpEmployeeStatus"));
      log4j.debug("inpEmployeeStatus" + inpEmployeeStatus);

      dao = new AssetDAO(con);
      dao1 = new EmployeeDAO(con);
      EhcmEmpAsset asset = null;
      // ehcmqualification qualinfo = null;
      vars = new VariablesSecureApp(request);
      if (request.getParameter("SubmitType") != null
          && (request.getParameter("SubmitType").equals("Save")
              || request.getParameter("SubmitType").equals("SaveGrid")
              || request.getParameter("SubmitType").equals("SaveNew"))) {

        // need to insert a record in Asset info table

        if (assetid.equals("") || assetid == null || assetid.equals("null")) {
          asset = OBProvider.getInstance().get(EhcmEmpAsset.class);
        } else {
          asset = OBDal.getInstance().get(EhcmEmpAsset.class, assetid);
        }

        if (assetid.equals("") || assetid == null || assetid.equals("null")) {
          asset.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
          asset.setOrganization(OBDal.getInstance().get(Organization.class, vars.getOrg()));
          asset.setCreationDate(new java.util.Date());
          asset.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        }

        asset.setUpdated(new java.util.Date());
        asset.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));

        if (request.getParameter("inpStartDate") != null
            && request.getParameter("inpStartDate") != "")
          asset.setStartDate(dao.convertGregorian(request.getParameter("inpStartDate").toString()));
        if (request.getParameter("inpEndDate") != "" && request.getParameter("inpEndDate") != null)
          asset.setEndDate(dao.convertGregorian(request.getParameter("inpEndDate").toString()));
        else
          asset.setEndDate(null);
        if (request.getParameter("inpAsset") != null && request.getParameter("inpAsset") != "")
          asset.setName(request.getParameter("inpAsset").toString());
        if (request.getParameter("inpLetterDate") != ""
            && request.getParameter("inpLetterDate") != null)
          asset.setLetterDate(
              dao.convertGregorian(request.getParameter("inpLetterDate").toString()));
        else
          asset.setLetterDate(null);
        asset.setLetterNo(request.getParameter("inpLetterNo").toString());
        asset.setDecisionNo(request.getParameter("inpDecisionNo").toString());
        asset.setEhcmEmpPerinfo(
            OBDal.getInstance().get(EhcmEmpPerInfo.class, request.getParameter("inpEmployeeId")));
        asset.setDescription(request.getParameter("inpdescription").toString());
        if (request.getParameter("inpBalance") != null && request.getParameter("inpBalance") != "")
          asset.setBalance(new BigDecimal(request.getParameter("inpBalance")));
        else
          asset.setBalance(new BigDecimal(0));

        OBDal.getInstance().save(asset);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
        assetid = asset.getId();
        log4j.debug("assetsave" + assetid);
        if (nextTab.equals("")) {
          if (request.getParameter("SubmitType").equals("Save")) {
            action = "EditView";
            request.setAttribute("inpAssetId", (asset.getId() == null ? "" : asset.getId()));
          } else if (request.getParameter("SubmitType").equals("SaveNew")) {
            action = "EditView";
            assetid = "";
            request.setAttribute("inpAssetId", "");
          } else if (request.getParameter("SubmitType").equals("SaveGrid")) {
            action = "GridView";
            assetid = "";
            request.setAttribute("inpAssetId", "");
          }
        } else {
          ServletContext context = this.getServletContext();
          if (!nextTab.equals("") && !nextTab.equals("Asset")) {
            String redirectStr = dao1.redirectStr(nextTab, employeeId, inpempstatus,
                inpEmployeeStatus);
            response.sendRedirect(context.getContextPath() + redirectStr);
          }
        }

        request.setAttribute("savemsg", "Success");

      }

      if (action.equals("EditView")) {
        log4j.debug("EditView");
        log4j.debug("assetid:" + assetid);
        if (assetid != null && assetid != "" && !assetid.equals("null")) {
          log4j.debug("if:");

          DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
          SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
          String date = df.format(new Date());
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          vo = dao.getAssetEditList(assetid);
          request.setAttribute("inpAssetId", assetid);
          request.setAttribute("inpEmployeeId", request.getParameter("inpEmployeeId"));
          request.setAttribute("inpAsset", vo.getAssetname());
          request.setAttribute("inpStartDate", vo.getStartdate());
          request.setAttribute("inpEndDate", vo.getEnddate());
          request.setAttribute("inpLetterNo", vo.getLetterno());
          request.setAttribute("inpLetterDate", vo.getLetterdate());
          request.setAttribute("inpDecisionNo", vo.getDecisionno());
          request.setAttribute("inpBalance", vo.getBalance());
          request.setAttribute("inpdescription", vo.getDescription());
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

          request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
          request.setAttribute("inpName1", objEmployee.getArabicfullname());
          request.setAttribute("inpName2",
              objEmployee.getName().concat(" ").concat(objEmployee.getFathername()).concat(" ")
                  .concat(objEmployee.getGrandfathername()));
          if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
            request.setAttribute("inpAddressId", null);
          else
            request.setAttribute("inpAddressId", dao1.getEmployeeAddressId(employeeId));

          dispatch = request.getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/asset/asset.jsp");

        }

        else if (assetid == null || assetid == "" || assetid.equals("null")) {
          DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
          SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
          String date = df.format(new Date());
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          request.setAttribute("inpEmployeeId", request.getParameter("inpEmployeeId"));
          request.setAttribute("inpAsset", null);
          request.setAttribute("inpStartDate", date);
          request.setAttribute("inpEndDate", null);
          request.setAttribute("inpLetterNo", null);
          request.setAttribute("inpLetterDate", null);
          request.setAttribute("inpDecisionNo", null);
          request.setAttribute("inpdescription", null);
          request.setAttribute("inpBalance", null);
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
          if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
            request.setAttribute("inpAddressId", null);
          else
            request.setAttribute("inpAddressId", dao1.getEmployeeAddressId(employeeId));

          dispatch = request.getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/asset/asset.jsp");

        }
        request.setAttribute("CancelHiring",
            dao1.checkEmploymentStatusCancel(vars.getClient(), employeeId));
      }

      else if (action.equals("") || action.equals("GridView")) {
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("Employee_ChildOrg", Utility.getAccessibleOrg(vars));
        ehcmempstatusv view = OBDal.getInstance().get(ehcmempstatusv.class, employeeId);
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
        if (employeeId.equals("") || employeeId.equals("null") || employeeId.equals(""))
          request.setAttribute("inpAddressId", null);
        else
          request.setAttribute("inpAddressId", dao1.getEmployeeAddressId(employeeId));
        request.setAttribute("CancelHiring",
            dao1.checkEmploymentStatusCancel(vars.getClient(), employeeId));

        dispatch = request.getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/asset/assetList.jsp");
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
