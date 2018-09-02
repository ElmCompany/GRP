package sa.elm.ob.hcm.ad_callouts;

import java.text.DateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EHCMticketordertransaction;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_callouts.dao.EndofEmploymentCalloutDAO;
import sa.elm.ob.hcm.ad_callouts.dao.EndofEmploymentCalloutDAOImpl;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.hcm.util.UtilityDAO;

/**
 * 
 * @author Gokul 25/07/18
 *
 */

public class TicketOrderCallout extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String inporiginalDecisionNo = vars.getStringParameter("inporiginalDecisionNo");
    DateFormat dateFormat = sa.elm.ob.utility.util.Utility.YearFormat;
    log4j.debug("lastfiled:" + lastfieldChanged);
    String departmentId = null;
    Date strtDate = null;
    DateFormat yearFormat = sa.elm.ob.utility.util.Utility.YearFormat;

    try {
      EmploymentInfo empinfo = null;
      empinfo = Utility.getActiveEmployInfo(employeeId);
      if (lastfieldChanged.equals("inpehcmEmpPerinfoId") && (StringUtils.isNotEmpty(employeeId))) {

        EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
        if (employee.getArabicfullname() != null)
          info.addResult("inpempName", employee.getArabicfullname());
        else
          info.addResult("inpempName", null);
        EHCMEmployeeStatusV employeeStatus = OBDal.getInstance().get(EHCMEmployeeStatusV.class,
            employeeId);
        if (employeeStatus != null)
          info.addResult("inpempStatus", employeeStatus.getStatusvalue());
        else
          info.addResult("inpempStatus", "");

        info.addResult("inpehcmGradeclassId", employee.getGradeClass().getId());
        info.addResult("inpempType", employee.getEhcmActiontype().getPersonType());
        if (employee.getHiredate() != null) {
          info.addResult("inphireDate",
              (UtilityDAO.convertTohijriDate(dateFormat.format(employee.getHiredate()))));
        }
        if (empinfo != null) {
          info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
          if (empinfo.getPosition() != null && empinfo.getPosition().getSection() != null) {
            info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
          }
          info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
          info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
          info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
          info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
          info.addResult("inpassignedDept", empinfo.getSECDeptName());
          info.addResult("inpehcmGradestepsId",
              empinfo.getEhcmPayscale().getEhcmGradesteps().getId());
          info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());
          info.addResult("inporiginalDecisionsNo", "");
          EndofEmploymentCalloutDAO endofemploymentobj = new EndofEmploymentCalloutDAOImpl();

          departmentId = empinfo.getPosition().getDepartment().getId();
          strtDate = yearFormat.parse(yearFormat.format(new Date()));

          JSONObject authorizationInfoObj = endofemploymentobj
              .getAuthorizationInfoDetails(departmentId, strtDate);
          if ((authorizationInfoObj != null) && (authorizationInfoObj.length() > 0)) {
            info.addResult("inpehcmAuthorizePersonId",
                authorizationInfoObj.getString("authorizedPerson"));
            info.addResult("inpauthorizePersonTitle",
                authorizationInfoObj.getString("authorizedJobTitle"));
          } else {
            info.addResult("inpehcmAuthorizePersonId", "");
            info.addResult("inpauthorizePersonTitle", "");
          }
        }
      }
      if (lastfieldChanged.equals("inpdecisionType")
          || (lastfieldChanged.equals("inporiginalDecisionNo"))) {
        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
            || inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
            || inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_TICKET_PAYMENT)) {
          EHCMticketordertransaction ticketOrder = OBDal.getInstance()
              .get(EHCMticketordertransaction.class, inporiginalDecisionNo);

          if (ticketOrder != null) {

            if (ticketOrder.getBusinessMission() != null)
              info.addResult("inpehcmEmpBusinessmissionId",
                  ticketOrder.getBusinessMission().getId());
            else
              info.addResult("inpehcmEmpBusinessmissionId", null);
            if (ticketOrder.getEhcmEmpScholarship() != null)
              info.addResult("inpehcmEmpScholarshipId",
                  ticketOrder.getEhcmEmpScholarship().getId());
            else
              info.addResult("inpehcmEmpScholarshipId", null);

            if (ticketOrder.getLetterNo() != null)
              info.addResult("inpletterNo", ticketOrder.getLetterNo());
            else
              info.addResult("inpletterNo", null);
            if (ticketOrder.getLetterDate() != null)
              info.addResult("inpletterDate",
                  UtilityDAO.convertTohijriDate(dateFormat.format(ticketOrder.getLetterDate())));
            else
              info.addResult("inpletterDate", null);
            if (ticketOrder.getDecisionDate() != null)
              info.addResult("inpdecisionDate",
                  UtilityDAO.convertTohijriDate(dateFormat.format(ticketOrder.getDecisionDate())));
            else
              info.addResult("inpdecisionDate", null);
            if (ticketOrder.getRequestdate() != null)
              info.addResult("inprequestdate",
                  UtilityDAO.convertTohijriDate(dateFormat.format(ticketOrder.getRequestdate())));
            else
              info.addResult("inprequestdate", null);
            if (ticketOrder.getTravelstartdate() != null)
              info.addResult("inptravelstartdate", UtilityDAO
                  .convertTohijriDate(dateFormat.format(ticketOrder.getTravelstartdate())));
            else
              info.addResult("inptravelstartdate", null);
            if (ticketOrder.getTravelenddate() != null)
              info.addResult("inptravelenddate",
                  UtilityDAO.convertTohijriDate(dateFormat.format(ticketOrder.getTravelenddate())));
            else
              info.addResult("inptravelenddate", null);

            if (ticketOrder.getTravelrouote() != null)
              info.addResult("inptravelrouote", ticketOrder.getTravelrouote());
            else
              info.addResult("inptravelrouote", null);
            if (ticketOrder.getTicketClass() != null)
              info.addResult("inpticketClass", ticketOrder.getTicketClass().getId());
            else
              info.addResult("inpticketClass", null);
            if (ticketOrder.getTicketNo() != null)
              info.addResult("inpticketNo", ticketOrder.getTicketNo());
            else
              info.addResult("inpticketNo", null);
            if (ticketOrder.getDependent1() != null)
              info.addResult("inpdependent1", ticketOrder.getDependent1().getId());
            if (ticketOrder.getDependent2() != null)
              info.addResult("inpdependent2", ticketOrder.getDependent2().getId());
            if (ticketOrder.getDependent3() != null)
              info.addResult("inpdependent3", ticketOrder.getDependent3().getId());
            if (ticketOrder.getDependent4() != null)
              info.addResult("inpdependent4", ticketOrder.getDependent4().getId());
            if (ticketOrder.getDependent5() != null)
              info.addResult("inpdependent5", ticketOrder.getDependent5().getId());
            if (ticketOrder.getDependent6() != null)
              info.addResult("inpdependent6", ticketOrder.getDependent6().getId());
            if (ticketOrder.getAdultTicketPrice() != null)
              info.addResult("inpadultTicketPrice", ticketOrder.getAdultTicketPrice());
            else
              info.addResult("inpadultTicketPrice", null);
            if (ticketOrder.isRoundtripTicket() != null)
              info.addResult("inproundtripTicket", ticketOrder.isRoundtripTicket());
            else
              info.addResult("inproundtripTicket", null);
          }
        }

      }

      if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        info.addResult("inpletterNo", null);
        info.addResult("inpletterDate", null);
        info.addResult("inpdecisionNo", null);
        info.addResult("inpdecisionDate", null);
        info.addResult("inptravelrouote", null);
        info.addResult("inproundtripTicket", null);
        info.addResult("inpticketNo", null);
        info.addResult("inpadultTicketPrice", null);

      }
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in Ticket Order Callout ", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }

}
