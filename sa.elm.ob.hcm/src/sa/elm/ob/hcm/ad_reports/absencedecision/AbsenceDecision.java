package sa.elm.ob.hcm.ad_reports.absencedecision;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.ad_reports.employeesbusinessmission.EmployeesBusinessMissionDAO;
import sa.elm.ob.utility.util.printUtils.GenerateJasperPrint;

public class AbsenceDecision extends GenerateJasperPrint {
  private static Logger log = Logger.getLogger(AbsenceDecision.class);

  private static AbsenceDecision IRPrintReport;

  public static AbsenceDecision getInstance() {
    if (IRPrintReport == null) {
      IRPrintReport = new AbsenceDecision();
    }
    return IRPrintReport;
  }

  @Override
  public void getReportVariables(HttpServletRequest request, JSONObject paramObject) {
    try {
      OBContext.setAdminMode();
      String reportDir = "", basedesign = "";
      EHCMAbsenceAttendance busMis = OBDal.getInstance().get(EHCMAbsenceAttendance.class,
          request.getParameter("inpRecordId"));

      reportDir = paramObject.getString("reportDir");
      basedesign = paramObject.getString("basedesign");
      isJasper = Boolean.TRUE;
      connection = (Connection) paramObject.get("connection");

      designParameters.put("inpAbsenceDecisionId", request.getParameter("inpRecordId"));
      designParameters.put("inpClientId", paramObject.getString("inpClientId"));
      designParameters.put("inpDepartmentName",
          EmployeesBusinessMissionDAO.getUserDepartment(paramObject.getString("inpUserID")));
      designParameters.put("BASE_DESIGN", basedesign);

      strReportName = reportDir + "absencedecision/AbsenceDecision.jrxml";
      strFileName = "AbsenceDecision" + " " + busMis.getDecisionNo() + " " + hijriDate;
    } catch (Exception e) {
      log.error("Exception in getReportVariables(): " + e);
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
