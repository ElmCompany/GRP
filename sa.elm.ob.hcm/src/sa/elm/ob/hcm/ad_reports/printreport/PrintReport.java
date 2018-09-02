package sa.elm.ob.hcm.ad_reports.printreport;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;

import net.sf.jasperreports.engine.JasperExportManager;
import sa.elm.ob.utility.util.printUtils.GenerateJasperPrint;

public class PrintReport extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;
  String pageType = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = null;
    RequestDispatcher dispatch = null;
    Connection con = null;
    try {
      con = getConnection();
      vars = new VariablesSecureApp(request);
      ServletOutputStream os = null;

      String action = (request.getParameter("action") == null ? ""
          : request.getParameter("action"));
      log4j.debug("action" + action);
      HashMap<String, Object> designParameters = new HashMap<String, Object>();
      final String basedesign = getBaseDesignPath(vars.getLanguage());
      final String reportDir = basedesign + "/sa/elm/ob/hcm/ad_reports/";
      designParameters.put("BASE_DESIGN", basedesign);

      String strFileName = "";

      JSONObject paramObject = new JSONObject();
      if (action.equals("Close")) {
        if (request.getParameter("pageType") != null
            && request.getParameter("pageType").equals("Grid"))
          printPageClosePopUp(response, vars);
        else
          printPageClosePopUpAndRefreshParent(response, vars);
      } else {
        try {

          String tabId = request.getParameter("inpTabId") == null ? ""
              : request.getParameter("inpTabId");
          String inpWindowID = (request.getParameter("inpWindowID") == null ? ""
              : request.getParameter("inpWindowID"));
          String inpRecordId = (request.getParameter("inpRecordId") == null ? ""
              : request.getParameter("inpRecordId"));
          String paramType = request.getParameter("report") == null ? ""
              : request.getParameter("report");
          String documentNo = (request.getParameter("documentNo") == null ? ""
              : request.getParameter("documentNo"));

          paramObject.put("paramType", paramType);
          paramObject.put("reportDir", reportDir);
          paramObject.put("connection", con);
          paramObject.put("tabId", tabId);
          paramObject.put("inpWindowID", inpWindowID);
          paramObject.put("inpUserID", vars.getUser());
          paramObject.put("inpClientId", vars.getClient());
          paramObject.put("inpRecordId", inpRecordId);
          paramObject.put("pageType", pageType);
          paramObject.put("documentNo", documentNo);
          paramObject.put("basedesign", basedesign);

          GetPrintFactory printFactory = new GetPrintFactory();
          GenerateJasperPrint print = printFactory.getPrint(request, paramObject);

          if (print.isJasper()) {
            strFileName = print.getFileName();

            os = response.getOutputStream();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition",
                "inline" + "; filename=" + strFileName + ".pdf" + "");

            JasperExportManager.exportReportToPdfStream(print.getJasperPrint(), os);
            print.getDesignParameters().clear();

          } else {
            dispatch = request.getRequestDispatcher(print.getJspPage());
          }

        } catch (Exception e) {
          log4j.error("Exception while downloading : ", e);
        }

        finally {
          try {
            if (os != null) {
              os.flush();
              os.close();
            }
          } catch (Exception e) {
            log4j.error("Exception while downloading print report : ", e);
          }
        }
        return;
      }
    } catch (final Exception e) {
      dispatch = request.getRequestDispatcher("../web/jsp/ErrorPage.jsp");
      log4j.error("Error file", e);
      e.printStackTrace();
      vars = new VariablesSecureApp(request);
      if (request.getParameter("pageType") != null
          && request.getParameter("pageType").equals("Grid"))
        printPageClosePopUp(response, vars);
      else
        printPageClosePopUpAndRefreshParent(response, vars);
    } finally {
      try {
        if (dispatch != null) {
          response.setContentType("text/html; charset=UTF-8");
          response.setCharacterEncoding("UTF-8");
          dispatch.include(request, response);
        }
      } catch (final Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log4j.error("Error file", e);
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }
}
