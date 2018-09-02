package sa.elm.ob.hcm.ad_reports.BusinessMissionDecision;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.ad_reports.employeesbusinessmission.EmployeesBusinessMissionDAO;
import sa.elm.ob.utility.util.printUtils.GenerateJasperPrint;

public class BusinessMissionDecisionPrint extends GenerateJasperPrint {
  private static Logger log = Logger.getLogger(BusinessMissionDecisionPrint.class);

  private static BusinessMissionDecisionPrint IRPrintReport;

  public static BusinessMissionDecisionPrint getInstance() {
    if (IRPrintReport == null) {
      IRPrintReport = new BusinessMissionDecisionPrint();
    }
    return IRPrintReport;
  }

  @Override
  public void getReportVariables(HttpServletRequest request, JSONObject paramObject) {
    try {
      VariablesSecureApp vars = new VariablesSecureApp(request);
      OBContext.setAdminMode();
      String reportDir = "", basedesign = "";
      EHCMEmpBusinessMission busMis = OBDal.getInstance().get(EHCMEmpBusinessMission.class,
          request.getParameter("inpRecordId"));

      reportDir = paramObject.getString("reportDir");
      basedesign = paramObject.getString("basedesign");
      isJasper = Boolean.TRUE;
      connection = (Connection) paramObject.get("connection");

      designParameters.put("businessMissionId", request.getParameter("inpRecordId"));
      designParameters.put("decisionNum", busMis.getDecisionNo());
      designParameters.put("userId", paramObject.getString("inpUserID"));
      designParameters.put("BASE_DESIGN", basedesign);
      designParameters.put("Hdept", EmployeesBusinessMissionDAO.getUserDepartment(vars.getUser()));
      if (busMis.getDecisionType().equals("UP") || busMis.getDecisionType().equals("EX")
          || busMis.getDecisionType().equals("CO") || busMis.getDecisionType().equals("CA")
          || busMis.getDecisionType().equals("CR")) {
        strReportName = reportDir + "BusinessMissionDecision/BusinessMissionDecisionReport.jrxml";
        strFileName = "BusinessMissionDecision" + " " + busMis.getDecisionNo() + " " + hijriDate;
      } else if (busMis.getDecisionType().equals("BP")) {
        strReportName = reportDir
            + "BusinessMissionPaymentReport/BusinessMissionPaymentReport.jrxml";
        strFileName = "BusinessMissionPayment" + " " + busMis.getDecisionNo() + " " + hijriDate;
        // strReportName = basedesign
        // +
        // "/sa/elm/ob/hcm/ad_reports/BusinessMissionPaymentReport/BusinessMissionPaymentReport.jrxml";
        // strFileName = "BusinessMissionDecision" + " " + busMis.getDecisionNo() + " " + hijriDate;
      }
    } catch (Exception e) {
      log.error("Exception in getReportVariables(): " + e);
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
