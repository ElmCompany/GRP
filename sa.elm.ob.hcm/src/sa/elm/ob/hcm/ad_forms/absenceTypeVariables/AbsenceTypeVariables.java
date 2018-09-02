package sa.elm.ob.hcm.ad_forms.absenceTypeVariables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;

/**
 * process file for absence type variables
 * 
 * @author divya-17-05-2018
 *
 */
public class AbsenceTypeVariables extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    RequestDispatcher dispatch = null;
    VariablesSecureApp vars = new VariablesSecureApp(request);
    AbsenceTypeVariablesDAOImpl absenceTypeVariablesDAOImpl = new AbsenceTypeVariablesDAOImpl();
    List<AbsenceTypeVariablesVO> list = new ArrayList<AbsenceTypeVariablesVO>();
    try {
      String action = (request.getParameter("action") == null ? ""
          : request.getParameter("action"));
      if (action.equals("showAbsenceTypeVariables")) {
        list = absenceTypeVariablesDAOImpl.getAbsenceTypeVariables(vars);
        if (list != null && list.size() > 0) {
          request.setAttribute("AbsenceTypeList", list);
          request.setAttribute("Type", "ABV");
          dispatch = request.getRequestDispatcher(
              "../web/sa.elm.ob.hcm/jsp/absenceTypeVariables/AbsenceTypeVariables.jsp");
        }
      }

    } catch (final Exception e) {
      log4j.error("Exception in AbsenceTypeVariables ", e);
      printPageClosePopUpAndRefreshParent(response, vars);
    } finally {
      if (dispatch != null) {
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        dispatch.include(request, response);
      } else
        printPageClosePopUpAndRefreshParent(response, vars);
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }

  public String getServletInfo() {
    return "ComponentTree";
  }
}
