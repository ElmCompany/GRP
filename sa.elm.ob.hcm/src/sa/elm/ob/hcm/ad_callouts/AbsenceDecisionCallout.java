package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMAbsenceReason;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAction;
import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_callouts.common.UpdateEmpDetailsInCallouts;
import sa.elm.ob.hcm.ad_callouts.dao.EndofEmploymentCalloutDAO;
import sa.elm.ob.hcm.ad_callouts.dao.EndofEmploymentCalloutDAOImpl;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.AbsenceDecision.AbsenceIssueDecisionDAOImpl;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author divya 01-01-2018
 *
 */
public class AbsenceDecisionCallout extends SimpleCallout {

  private static final long serialVersionUID = -9141215828493029089L;
  private static final String errorMessage = "EHCM_DecimalValueNotAllow";

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;

    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String inpehcmAbsenceTypeId = vars.getStringParameter("inpehcmAbsenceTypeId");
    String inpstartdate = vars.getStringParameter("inpstartdate");
    String inpenddate = vars.getStringParameter("inpenddate");
    String inpextendStartdate = vars.getStringParameter("inpextendStartdate");
    String inpextendEnddate = vars.getStringParameter("inpextendEnddate");
    String inpclientId = vars.getStringParameter("inpadClientId");
    String inpabsenceDays = vars.getStringParameter("inpabsenceDays");
    String inpextendLeaveDay = vars.getStringParameter("inpextendLeaveDay");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpehcmAuthorizePersonId = vars.getStringParameter("inpehcmAuthorizePersonId");
    String inporiginalDecisionNo = vars.getStringParameter("inporiginalDecisionNo");
    String startDate = "";
    String enddate = "";
    String extendEnddate = "";
    String employmentInfoId = "";
    String parsedMessage = null;
    String departmentId = null;
    Date strtDate = null;
    String departmentCode = vars.getStringParameter("inpdepartmentId");
    DateFormat yearFormat = sa.elm.ob.utility.util.Utility.YearFormat;
    DateFormat dateFormat = sa.elm.ob.utility.util.Utility.dateFormat;
    Connection conn = OBDal.getInstance().getConnection();

    PreparedStatement st = null;
    ResultSet rs = null;

    Boolean isIncludeHoliday = false;

    int oneDayinMilliSeconds = 1 * 24 * 3600 * 1000;
    int countOfHoldiay = 0;

    EHCMAbsenceType absenceType = null;
    EHCMAbsenceType dependentAbsenceType = null;
    EHCMAbsenceAttendance absence = null;

    List<EHCMAbsenceTypeAction> absenceActionList = new ArrayList<EHCMAbsenceTypeAction>();

    AbsenceIssueDecisionDAOImpl absenceIssueDecisionDAOImpl = new AbsenceIssueDecisionDAOImpl();
    EndofEmploymentCalloutDAO endofemploymentobj = new EndofEmploymentCalloutDAOImpl();

    try {
      /*
       * get Latest active EmploymentInfo by using EmployeeId and set the value based on Employment
       * Info
       */
      UpdateEmpDetailsInCallouts callouts = new UpdateEmpDetailsInCallouts();
      EmploymentInfo empinfo = null;
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id='" + employeeId + "' and enabled='Y' order by creationDate desc ");
      log4j.debug("employeeId:" + employeeId);
      log4j.debug("positiontype:" + empInfo.list().size());
      if (empInfo.list().size() > 0) {
        empinfo = empInfo.list().get(0);
      }

      if (StringUtils.isNotEmpty(inpehcmAbsenceTypeId)) {
        absenceType = OBDal.getInstance().get(EHCMAbsenceType.class, inpehcmAbsenceTypeId);
      }

      absenceActionList = absenceType.getEHCMAbsenceTypeActionList();
      if (absenceActionList.size() > 0) {
        for (EHCMAbsenceTypeAction action : absenceActionList) {
          dependentAbsenceType = action.getDependent();
        }
      }
      // get isinclude holiday value
      if (dependentAbsenceType != null) {
        isIncludeHoliday = dependentAbsenceType.isInculdeholiday();
      } else {
        isIncludeHoliday = absenceType.isInculdeholiday();
      }

      log4j.debug("lastfieldChanged:" + lastfieldChanged);
      if (lastfieldChanged.equals("inpehcmEmpPerinfoId")) {
        if (StringUtils.isNotEmpty(employeeId)) {
          log4j.debug("entered  person");
          /* get Employee Details by using employeeId */
          EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          info.addResult("inpempName", employee.getArabicfullname());

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
                (UtilityDAO.convertTohijriDate(yearFormat.format(employee.getHiredate()))));
          }
          if (empinfo != null) {
            employmentInfoId = empinfo.getId();
            info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
            if (empinfo.getPosition() != null && empinfo.getPosition().getSection() != null) {
              info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
            } else {
              info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
            }
            info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
            info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
            log4j.debug("inpehcmPositionId:" + empinfo.getPosition().getJOBNo());
            info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
            info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
            info.addResult("inpassignedDept", empinfo.getSECDeptName());
            info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());
            info.addResult("inpehcmGradestepsId",
                empinfo.getEhcmPayscale().getEhcmGradesteps().getId());
            String query = " select eut_convert_to_hijri_timestamp('"
                + yearFormat.format(new Date()) + "')";

            st = conn.prepareStatement(query);
            rs = st.executeQuery();
            if (rs.next())
              info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri_timestamp"));

            info.addResult("inpdecisionType", "CR");
            info.addResult("inporiginalDecisionNo", "");

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
          info.addResult("inpabsenceDays", "0");
          info.addResult("inpenddate", "");
        } else {
          callouts.SetEmpDetailsNull(info);
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Ehcm_Payscaleline_ID').setValue('')");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Assigned_Dept').setValue('')");
        }

      }
      if ((lastfieldChanged.equals("inpdecisionType")
          || lastfieldChanged.equals("inporiginalDecisionNo")
          || lastfieldChanged.equals("inpehcmAbsenceTypeId"))
          && StringUtils.isNotEmpty(inporiginalDecisionNo)) {
        log4j.debug("entered  decisiontype");

        if (lastfieldChanged.equals("inpehcmAbsenceTypeId")) {
          OBQuery<EHCMAbsenceReason> objResQuery = OBDal.getInstance()
              .createQuery(EHCMAbsenceReason.class, "as e where e.ehcmAbsenceType.id='"
                  + inpehcmAbsenceTypeId + "'  order by e.creationDate asc");
          objResQuery.setMaxResult(1);
          if (objResQuery.list().size() > 0) {
            EHCMAbsenceReason reason = objResQuery.list().get(0);
            info.addResult("inpehcmAbsenceReasonId", reason.getId());
          }
        }
        if (!inpdecisionType.equals("CR")) {
          absence = OBDal.getInstance().get(EHCMAbsenceAttendance.class, inporiginalDecisionNo);
          log4j.debug("entered  decisiontype:" + inporiginalDecisionNo);

          startDate = UtilityDAO.convertTohijriDate(yearFormat.format(absence.getStartDate()));
          info.addResult("inpstartdate", startDate);

          enddate = UtilityDAO.convertTohijriDate(yearFormat.format(absence.getEndDate()));
          info.addResult("inpenddate", enddate);

          if (absenceType.isSubtype()
              && !inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
              && absence.getSubtype() != null) {
            info.addResult("inpsubtype", absence.getSubtype().getId());
          } else {
            info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Subtype').setValue('')");
          }
          if (inpdecisionType.equals("EX")) {
            Date dateafter = new Date(absence.getEndDate().getTime() + oneDayinMilliSeconds);
            startDate = UtilityDAO.convertTohijriDate(yearFormat.format(dateafter));
            info.addResult("inpextendStartdate", startDate);
          }
          info.addResult("inpabsenceDays", absence.getAbsenceDays());
          info.addResult("inpauthorizePersonTitle", absence.getAuthorizedPersonJobTitle());
          info.addResult("inpehcmAuthorizePersonId", absence.getAuthorizedPerson());
        } else {
          // info.addResult("inporiginalDecisionNo", null);
          info.addResult("inpabsenceDays", "0");
          info.addResult("inpenddate", "");
          info.addResult("inpauthorizePersonTitle", "");
          info.addResult("inpehcmAuthorizePersonId", "");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Subtype').setValue('')");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Original_Decision_No').setValue('')");
        }
      }
      if (lastfieldChanged.equals("inpstartdate") || lastfieldChanged.equals("inpabsenceDays")
          || lastfieldChanged.equals("inpehcmAbsenceTypeId")) {
        inpabsenceDays = inpabsenceDays.replaceAll(",", "");
        if ((new Double(inpabsenceDays) - new Double(inpabsenceDays).intValue()) != 0) {
          parsedMessage = org.openbravo.erpCommon.utility.Utility.messageBD(this, errorMessage,
              OBContext.getOBContext().getLanguage().getId());
          info.addResult("ERROR", parsedMessage);
          return;
        }

        if (new Double(inpabsenceDays).intValue() > 0) {
          Date endDate = absenceIssueDecisionDAOImpl.getEndDate(Integer.parseInt(inpabsenceDays),
              yearFormat.parse(UtilityDAO.convertToGregorian(inpstartdate)), inpclientId,
              isIncludeHoliday);
          enddate = UtilityDAO.convertTohijriDate(yearFormat.format(endDate));
          // inpenddate = enddate;
          info.addResult("inpenddate", enddate);
          log4j.debug("startdate" + yearFormat.parse(UtilityDAO.convertToGregorian(inpstartdate)));
          log4j.debug("inpabsenceDays;" + inpabsenceDays);
        } else {
          info.addResult("inpenddate", null);
        }

        if (lastfieldChanged.equals("inpstartdate")) {
          String strGregterminateDate = UtilityDAO.convertToGregorian(inpstartdate);
          Date starDate = yearFormat.parse(strGregterminateDate);
          log4j.debug("Date after conversion:" + starDate);
          JSONObject authorizationInfoObj1 = endofemploymentobj
              .getAuthorizationInfoDetails(departmentCode, starDate);
          if (authorizationInfoObj1 != null && authorizationInfoObj1.length() > 0) {
            info.addResult("inpehcmAuthorizePersonId",
                authorizationInfoObj1.getString("authorizedPerson"));
            info.addResult("inpauthorizePersonTitle",
                authorizationInfoObj1.getString("authorizedJobTitle"));
          } else {
            info.addResult("inpehcmAuthorizePersonId", "");
            info.addResult("inpauthorizePersonTitle", "");
          }

        }

      }
      if (lastfieldChanged.equals("inpextendStartdate")
          || lastfieldChanged.equals("inpextendLeaveDay")
          || lastfieldChanged.equals("inpehcmAbsenceTypeId")) {
        inpextendLeaveDay = inpextendLeaveDay.replaceAll(",", "");
        if ((new Double(inpextendLeaveDay) - new Double(inpextendLeaveDay).intValue()) != 0) {
          parsedMessage = org.openbravo.erpCommon.utility.Utility.messageBD(this, errorMessage,
              OBContext.getOBContext().getLanguage().getId());
          info.addResult("ERROR", parsedMessage);
          return;
        }

        if (new Double(inpextendLeaveDay).intValue() > 0) {
          Date extendEndDate = absenceIssueDecisionDAOImpl.getEndDate(
              Integer.parseInt(inpextendLeaveDay),
              yearFormat.parse(UtilityDAO.convertToGregorian(inpextendStartdate)), inpclientId,
              isIncludeHoliday);
          extendEnddate = UtilityDAO.convertTohijriDate(yearFormat.format(extendEndDate));
          inpextendEnddate = extendEnddate;
          info.addResult("inpextendEnddate", inpextendEnddate);
          info.addResult("inpenddate", inpextendEnddate);
          lastfieldChanged = "inpenddate";
          inpenddate = inpextendEnddate;
          log4j.debug(
              "startdate" + yearFormat.parse(UtilityDAO.convertToGregorian(inpextendStartdate)));
          log4j.debug("inpabsenceDays;" + inpextendLeaveDay);
        } else {
          info.addResult("inpextendEnddate", null);
          if (StringUtils.isNotEmpty(inporiginalDecisionNo)) {
            absence = OBDal.getInstance().get(EHCMAbsenceAttendance.class, inporiginalDecisionNo);
            enddate = UtilityDAO.convertTohijriDate(yearFormat.format(absence.getEndDate()));
            info.addResult("inpenddate", enddate);
            lastfieldChanged = "inpenddate";
            inpenddate = enddate;
          }
        }

      }
      if (lastfieldChanged.equals("inpenddate")) {
        if (StringUtils.isNotEmpty(inpenddate) && StringUtils.isNotEmpty(inpstartdate)) {

          int noofdays = Utility.calculatetheDays(inpstartdate, inpenddate);

          if (!isIncludeHoliday) {
            countOfHoldiay = absenceIssueDecisionDAOImpl.countofHolidays(
                yearFormat.parse(UtilityDAO.convertToGregorian(inpstartdate)),
                yearFormat.parse(UtilityDAO.convertToGregorian(inpenddate)), inpclientId,
                isIncludeHoliday);
            info.addResult("inpnoOfHolidays", countOfHoldiay);

            if (countOfHoldiay > 0) {
              noofdays = noofdays - countOfHoldiay;
            }
          }
          info.addResult("inpabsenceDays", noofdays);
          log4j.debug("noofdays;" + noofdays);
        }
      }

      if (lastfieldChanged.equals("inpextendEnddate")) {
        if (StringUtils.isNotEmpty(inpextendEnddate)
            && StringUtils.isNotEmpty(inpextendStartdate)) {

          int noofdays = Utility.calculatetheDays(inpextendStartdate, inpextendEnddate);

          if (!isIncludeHoliday) {
            countOfHoldiay = absenceIssueDecisionDAOImpl.countofHolidays(
                yearFormat.parse(UtilityDAO.convertToGregorian(inpextendStartdate)),
                yearFormat.parse(UtilityDAO.convertToGregorian(inpextendEnddate)), inpclientId,
                isIncludeHoliday);
            info.addResult("inpnoOfHolidays", countOfHoldiay);

            if (countOfHoldiay > 0) {
              noofdays = noofdays - countOfHoldiay;
            }
          }
          info.addResult("inpextendLeaveDay", noofdays);
          log4j.debug("noofdays:" + noofdays);

          noofdays = Utility.calculatetheDays(inpstartdate, inpextendEnddate);
          if (!isIncludeHoliday) {
            countOfHoldiay = absenceIssueDecisionDAOImpl.countofHolidays(
                yearFormat.parse(UtilityDAO.convertToGregorian(inpstartdate)),
                yearFormat.parse(UtilityDAO.convertToGregorian(inpextendEnddate)), inpclientId,
                isIncludeHoliday);
            info.addResult("inpnoOfHolidays", countOfHoldiay);
            log4j.debug("noofdays;" + countOfHoldiay);
            if (countOfHoldiay > 0) {
              noofdays = noofdays - countOfHoldiay;
            }
          }
          log4j.debug("inpabsenceDays;" + noofdays);
          info.addResult("inpabsenceDays", noofdays);
        }
      }
      /*
       * if (lastfieldChanged.equals("inpehcmAuthorizePersonId")) { // get active employment info
       * EmploymentInfo emplyinfo = Utility.getActiveEmployInfo(inpehcmAuthorizePersonId); if
       * (emplyinfo != null && emplyinfo.getPosition().getEhcmJobs() != null) {
       * info.addResult("inpauthorizePersonTitle", emplyinfo.getPosition().getId()); } }
       */

    } catch (Exception e) {
      log4j.error("Exception in AbsenceDecisionCallout ", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}