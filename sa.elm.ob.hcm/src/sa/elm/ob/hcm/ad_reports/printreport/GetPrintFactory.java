package sa.elm.ob.hcm.ad_reports.printreport;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.jfree.util.Log;

import sa.elm.ob.hcm.ad_reports.BusinessMissionDecision.BusinessMissionDecisionPrint;
import sa.elm.ob.hcm.ad_reports.absencedecision.AbsenceDecision;
import sa.elm.ob.hcm.ad_reports.certificationletter.CertificationLetter;
import sa.elm.ob.utility.util.printUtils.GenerateJasperPrint;

public class GetPrintFactory {
  Logger log4j = Logger.getLogger(GetPrintFactory.class);

  public GenerateJasperPrint getPrint(HttpServletRequest request, JSONObject paramObject) {
    String paramType = "", inpWindowID = "", inpTabID = "";
    GenerateJasperPrint generateJasperPrint = null;
    try {

      String action = (request.getParameter("action") == null ? ""
          : request.getParameter("action"));
      inpWindowID = (request.getParameter("inpWindowID") == null ? ""
          : request.getParameter("inpWindowID"));
      inpTabID = (request.getParameter("inpTabId") == null ? "" : request.getParameter("inpTabId"));

      if (action.equals("DownloadReport")) {
        paramType = paramObject.getString("paramType");
        Log.info("paramType>" + paramType);
        if (paramType.equals("CLWTOUTSL")) { // Certificate Letter Without Salary
          generateJasperPrint = CertificationLetter.getInstance();
        } else if (paramType.equals("CLWTDTSL")) { // Certificate Letter With Detailed Salary
          generateJasperPrint = CertificationLetter.getInstance();
        } else if (paramType.equals("CLWTTOTSL_AE")) { // Certificate Letter With Total
                                                       // Salary(Arab/Eng)
          generateJasperPrint = CertificationLetter.getInstance();
        } else if (paramType.equals("CLWTTOTSL")) { // Certificate Letter With Detailed Salary
          generateJasperPrint = CertificationLetter.getInstance();
        }
      } else if (action.equals("")) {
        // Business Mission Report
        if (inpWindowID.equals("796272C70BFE4201BBDC848C8F487FAA")) {
          generateJasperPrint = BusinessMissionDecisionPrint.getInstance();
        }
        // Absence Decision
        else if (inpWindowID.equals("C8154257AADE418D8387C2319B85D762")) {
          generateJasperPrint = AbsenceDecision.getInstance();
        }
        // Employment Certificate
        else if (inpWindowID.equals("DCC32BEEF53841FF9A5B1F1585156930")) {
          generateJasperPrint = CertificationLetter.getInstance();
        }
      }

      if (generateJasperPrint != null) {
        generateJasperPrint.getReportVariables(request, paramObject);
      }
    } catch (Exception e) {
      log4j.error("Excpetion while getPrint(): " + e);
      return null;
    }
    return generateJasperPrint;
  }
}
