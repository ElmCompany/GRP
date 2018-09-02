package sa.elm.ob.hcm.ad_reports.certificationletter;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.ehcmemploymentcertificate;
import sa.elm.ob.utility.util.printUtils.GenerateJasperPrint;

public class CertificationLetter extends GenerateJasperPrint {
  private static Logger log = Logger.getLogger(CertificationLetter.class);

  private static CertificationLetter CLPrintReport;

  public static CertificationLetter getInstance() {
    if (CLPrintReport == null) {
      CLPrintReport = new CertificationLetter();
    }
    return CLPrintReport;
  }

  @Override
  public void getReportVariables(HttpServletRequest request, JSONObject paramObject) {
    try {
      OBContext.setAdminMode();
      String reportDir = "", inpCertificateLetterRpt = "";
      ehcmemploymentcertificate cert = OBDal.getInstance().get(ehcmemploymentcertificate.class,
          request.getParameter("inpRecordId"));

      reportDir = paramObject.getString("reportDir");
      String action = (request.getParameter("action") == null ? ""
          : request.getParameter("action"));
      if (action.equals(""))
        isJasper = Boolean.FALSE;
      else if (action.equals("DownloadReport"))
        isJasper = Boolean.TRUE;

      connection = (Connection) paramObject.get("connection");
      designParameters.put("inpCertificateId", request.getParameter("inpRecordId"));
      designParameters.put("inpClientId", paramObject.getString("inpClientId"));
      designParameters.put("inprequestDate", (cert.getRequestDate()));
      inpCertificateLetterRpt = (request.getParameter("inpCertReport") == null ? ""
          : request.getParameter("inpCertReport"));

      if (inpCertificateLetterRpt.equals("CLWTOUTSL")) {
        strReportName = reportDir + "certificationletter/CertificationLetterWithoutSalary.jrxml";
        strFileName = "CertificationLetter" + " " + cert.getId() + " " + hijriDate;
      } else if (inpCertificateLetterRpt.equals("CLWTDTSL")) {
        strReportName = reportDir + "certificationletter/CertificationLetterWithSalary.jrxml";
        strFileName = "CertificationLetter" + " " + cert.getId() + " " + hijriDate;
      } else if (inpCertificateLetterRpt.equals("CLWTTOTSL")) {
        strReportName = reportDir
            + "certificationletter/CertificationLetterArabicWithTotalSalary.jrxml";
        strFileName = "CertificationLetterArabicWithTotalSalary" + " " + hijriDate;
      } else if (inpCertificateLetterRpt.equals("CLWTTOTSL_AE")) {
        strReportName = reportDir + "certificationletter/CertificationLetterWithTotalSalary.jrxml";
        strFileName = "CertificationLetterWithTotalSalary" + " " + hijriDate;
      }

      request.setAttribute("pageType", paramObject.getString("pageType"));
      request.setAttribute("inpTabId", paramObject.getString("tabId"));
      request.setAttribute("inpWindowID", paramObject.getString("inpWindowID"));
      request.setAttribute("inpRecordId", paramObject.getString("inpRecordId"));
      strJspPage = "../web/sa.elm.ob.hcm/jsp/printreport/CertificationLetter.jsp";
    } catch (Exception e) {
      log.error("Exception in getReportVariables(): " + e);
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
