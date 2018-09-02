package sa.elm.ob.hcm.ad_forms.personalpaymentmethod.header;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

import sa.elm.ob.hcm.EHCMPersonalPaymethd;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ehcmempstatusv;
import sa.elm.ob.hcm.ad_forms.employee.dao.EmployeeDAO;
import sa.elm.ob.hcm.ad_forms.personalpaymentmethod.dao.PersonalPaymentMethodDAO;
import sa.elm.ob.hcm.ad_forms.personalpaymentmethod.vo.PersonalPaymentMethodVO;
import sa.elm.ob.hcm.properties.Resource;

/**
 * 
 * @author Gokul 12/07/18
 *
 */
public class PersonalPaymentMethod extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unchecked")
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    Connection con = null;
    RequestDispatcher dispatch = null;
    VariablesSecureApp vars = null;
    PersonalPaymentMethodVO vo = null;
    PersonalPaymentMethodDAO dao = null;
    EmployeeDAO dao1 = new EmployeeDAO(con);
    try {
      con = getConnection();
      OBContext.setAdminMode();
      String action = (request.getParameter("inpAction") == null ? ""
          : request.getParameter("inpAction"));
      String submitType = request.getParameter("SubmitType") == null ? ""
          : request.getParameter("SubmitType");
      String perpaymethodId = (request.getParameter("inpehcmPersonalPaymethdId") == null ? ""
          : request.getParameter("inpehcmPersonalPaymethdId"));
      String nextTab = (request.getParameter("inpNextTab") == null ? ""
          : request.getParameter("inpNextTab"));
      String inpempstatus = (request.getParameter("inpEmpStatus") == null ? ""
          : request.getParameter("inpEmpStatus"));
      String employeeId = (request.getParameter("inpEmployeeId") == null ? ""
          : request.getParameter("inpEmployeeId"));
      log4j.debug("inpempstatus" + inpempstatus);
      String inpEmployeeStatus = (request.getParameter("inpEmployeeStatus") == null ? ""
          : request.getParameter("inpEmployeeStatus"));
      vars = new VariablesSecureApp(request);
      dao = new PersonalPaymentMethodDAO(con);

      // process save operation
      if (submitType != null && (submitType.equals("Save") || submitType.equals("SaveNew")
          || submitType.equals("SaveGrid"))) {
        vo = new PersonalPaymentMethodVO();
        vo.setpaymenttypecode(request.getParameter("inppaycode"));
        vo.setpaymenttypename(request.getParameter("inppayname"));
        vo.setcurrency(request.getParameter("inppaycurrency"));
        vo.setehcmemployeeId(request.getParameter("inpEmployeeId"));
        if (request.getParameter("inpdefaultflag") != null
            && request.getParameter("inpdefaultflag").equals("on")) {
          vo.setisdefault(true);
        } else {
          vo.setisdefault(false);
        }

        if (perpaymethodId.equals("") || perpaymethodId.equals("null"))
          perpaymethodId = dao.addPerPayMethod(vars.getClient(), vars.getUser(), vo);
        else
          perpaymethodId = dao.updateaddPerPayMethod(vars.getClient(), vars.getUser(), vo,
              perpaymethodId);
        if (perpaymethodId != null)
          request.setAttribute("savemsg", Resource.getProperty("hcm.success", vars.getLanguage()));
        else
          request.setAttribute("savemsg", Resource.getProperty("hcm.error", vars.getLanguage()));
        if (nextTab.equals("") || nextTab.equals("PERPAYMETHD")) {

          log4j.debug("submit type :" + submitType);
          if (submitType.equals("SaveNew")) {
            perpaymethodId = "";
            request.setAttribute("inpehcmPersonalPaymethdId", "");
            action = "EditView";
          } else if (submitType.equals("Save")) {
            request.setAttribute("inpehcmPersonalPaymethdId",
                (perpaymethodId == null ? "" : perpaymethodId));
            action = "EditView";
          } else if (submitType.equals("SaveGrid")) {
            perpaymethodId = "";
            action = "GridView";

          }
        } else {
          ServletContext context = this.getServletContext();
          if (!nextTab.equals("") && !nextTab.equals("PERPAYMETHD") && !nextTab.equals("null")) {
            String redirectStr = dao1.redirectStr(nextTab, employeeId, inpempstatus,
                inpEmployeeStatus);
            response.sendRedirect(context.getContextPath() + redirectStr);
          }
        }

        if (action.equals("GridView"))
          action = "GridView";
        else
          action = "EditView";

      }

      if (action.equals("EditView")) {

        if (perpaymethodId == null || perpaymethodId.equals("") || perpaymethodId.equals("null"))

        {
          log4j.debug("if:");
          ehcmempstatusv view = OBDal.getInstance().get(ehcmempstatusv.class, employeeId);
          EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class,
              view.getEhcmEmpPerinfo().getId());
          request.setAttribute("inpEmployeeIsActive", objEmployee.isEnabled());
          request.setAttribute("inpEmployeeId", request.getParameter("inpEmployeeId"));
          request.setAttribute("inpvalue", dao.getpaymenttypecode(vars.getClient()));
          request.setAttribute("inpname", dao.getpaymenttypecode(vars.getClient()));
          request.setAttribute("inpcurrency",
              dao.getcurrency(vars.getClient(), request.getParameter("inppaycode")));
          request.setAttribute("inpbank", dao.getbankname(vars.getClient()));
          request.setAttribute("inpbranch", dao.getBankBranchOnLoad(vars.getClient()));

        }
        if (perpaymethodId != null && !perpaymethodId.equals("")
            && !perpaymethodId.equals("null")) {
          EHCMPersonalPaymethd objPerpaymethod = OBDal.getInstance().get(EHCMPersonalPaymethd.class,
              perpaymethodId);
          request.setAttribute("inpehcmPersonalPaymethdId", perpaymethodId);
          request.setAttribute("inpEmployeeId", request.getParameter("inpEmployeeId"));
          request.setAttribute("inpvalue", dao.getpaymenttypecode(vars.getClient()));
          request.setAttribute("inpname", dao.getpaymenttypecode(vars.getClient()));
          request.setAttribute("inpcurrency",
              dao.getcurrency(vars.getClient(), objPerpaymethod.getCode().getId()));
          request.setAttribute("inpbank", dao.getbankname(vars.getClient()));
          request.setAttribute("inpbranch", dao.getBankBranchOnLoad(vars.getClient()));
          request.setAttribute("inppaycode", objPerpaymethod.getCode().getPaymenttypecode());
          request.setAttribute("inpisbanktransfer", objPerpaymethod.getCode().isBanktransfer());
          request.setAttribute("inpsavedvalue", objPerpaymethod.getCode().getId());
          request.setAttribute("inpsavedname", objPerpaymethod.getName().getId());
          request.setAttribute("inpsavedcurrency", objPerpaymethod.getCurrency().getId());
          request.setAttribute("inpdefaultflag", objPerpaymethod.isDefault());
        }
        if (request.getParameter("inpEmpStatus").toString() != null) {
          request.setAttribute("inpEmpStatus", request.getParameter("inpEmpStatus").toString());
        }
        if (request.getParameter("inpEmployeeStatus").toString() != null) {
          request.setAttribute("inpEmployeeStatus",
              request.getParameter("inpEmployeeStatus").toString());
        }
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
        request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
        request.setAttribute("inpName1", objEmployee.getArabicfullname());
        request.setAttribute("inpName2", objEmployee.getName().concat(" ")
            .concat(objEmployee.getFathername() == null ? "" : objEmployee.getFathername())
            .concat(" ").concat(
                objEmployee.getGrandfathername() == null ? "" : objEmployee.getGrandfathername()));
        request.setAttribute("CancelHiring",
            dao1.checkEmploymentStatusCancel(vars.getClient(), employeeId));
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        dispatch = request.getRequestDispatcher(
            "../web/sa.elm.ob.hcm/jsp/personalpaymentmethod/Personalpaymentmethod.jsp");

      }

      else if (action.equals("") || action.equals("GridView")) {
        HttpSession httpSession = request.getSession();
        request.setAttribute("inpEmployeeId", request.getParameter("inpEmployeeId"));
        if (request.getParameter("inpEmpStatus") != null) {
          request.setAttribute("inpEmpStatus", request.getParameter("inpEmpStatus").toString());
        }
        if (request.getParameter("inpEmployeeStatus") != null) {
          request.setAttribute("inpEmployeeStatus",
              request.getParameter("inpEmployeeStatus").toString());
        }

        request.setAttribute("inpehcmPersonalPaymethdId",
            (perpaymethodId == null ? "" : perpaymethodId));

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
        request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
        request.setAttribute("inpName1", objEmployee.getArabicfullname());
        request.setAttribute("inpName2", objEmployee.getName().concat(" ")
            .concat(objEmployee.getFathername() == null ? "" : objEmployee.getFathername())
            .concat(" ").concat(
                objEmployee.getGrandfathername() == null ? "" : objEmployee.getGrandfathername()));
        request.setAttribute("CancelHiring",
            dao1.checkEmploymentStatusCancel(vars.getClient(), employeeId));
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        dispatch = request.getRequestDispatcher(
            "../web/sa.elm.ob.hcm/jsp/personalpaymentmethod/Personalpaymentmethodlist.jsp");
      }

      OBDal.getInstance().commitAndClose();
    } catch (final Exception e) {
      dispatch = request.getRequestDispatcher("../web/jsp/ErrorPage.jsp");
      log4j.error("Error in personal Payment Method : ", e);
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
        log4j.error("Error in personal Payment Method : ", e);
      }
    }

  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }
}
