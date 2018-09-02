package sa.elm.ob.hcm.ad_reports.printreport;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import sa.elm.ob.utility.util.printUtils.GenerateJasperPrint;

public class PrintReportPopup extends GenerateJasperPrint {

  private static PrintReportPopup printpoPopup;

  public static PrintReportPopup getInstance() {
    if (printpoPopup == null) {
      printpoPopup = new PrintReportPopup();
    }
    return printpoPopup;
  }

  public void getReportVariables(HttpServletRequest request, JSONObject paramObject) {
    try {
      request.setAttribute("pageType", paramObject.getString("pageType"));
      request.setAttribute("inpTabId", paramObject.getString("tabId"));
      request.setAttribute("inpWindowID", paramObject.getString("inpWindowID"));
      request.setAttribute("inpRecordId", paramObject.getString("inpRecordId"));
      request.setAttribute("inpDocNo", paramObject.getString("documentNo"));
      isJasper = Boolean.FALSE;
      strJspPage = "../web/sa.elm.ob.scm/jsp/printreport/PrintReport.jsp";
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}
