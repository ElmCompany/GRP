package sa.elm.ob.hcm.ad_process.EndofEmployment;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;

import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.ad_reports.employeesbusinessmission.EmployeesBusinessMissionDAO;

public class EndOfEmploymentPrint extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = null;

    try {
      vars = new VariablesSecureApp(request);
      String action = (request.getParameter("action") == null ? ""
          : request.getParameter("action"));
      String inpRecordId = (request.getParameter("inpRecordId") == null ? ""
          : request.getParameter("inpRecordId"));
      log4j.debug("action" + action);

      log4j.debug("inpRecordId :" + inpRecordId);
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      EHCMEMPTermination termination = OBDal.getInstance().get(EHCMEMPTermination.class,
          inpRecordId);
      parameters.put("endofemploymentid", inpRecordId);
      parameters.put("terminationDate", termination.getTerminationDate());
      parameters.put("inpDepartmentName",
          EmployeesBusinessMissionDAO.getUserDepartment(vars.getUser()));
      String strReportName = "@basedesign@/sa/elm/ob/hcm/ad_reports/EndOfEmploymentReport/EndOfEmploymentReport.jrxml";
      String strOutput = "pdf";
      renderJR(vars, response, strReportName, strOutput, parameters, null, null);

    } catch (final Exception e) {
      log4j.error("Error file", e);
      e.printStackTrace();
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }
}
