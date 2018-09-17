package sa.elm.ob.hcm.ad_callouts;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmEmployeeOvertime;
import sa.elm.ob.hcm.EhcmOvertimeType;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_callouts.common.UpdateEmpDetailsInCallouts;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * @author poongodi on 09/03/2018
 */
public class EmployeeOvertimeCallout extends SimpleCallout {

  private static final long serialVersionUID = 1L;
  private static final String DecisionType = "CR";
  private static final String OvertimePayment = "OP";

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String employmentInfoId = "";
    DateFormat dateFormat = Utility.YearFormat;
    String inpStartDate = vars.getStringParameter("inpstartdate");
    String inpEndDate = vars.getStringParameter("inpenddate");
    String overtypeId = vars.getStringParameter("inpehcmOvertimeTypeId");
    String holidayType = null;
    String inpEmpOvertimeId = vars.getStringParameter("inpehcmEmpOvertimeId");
    BigDecimal holiday = new BigDecimal(0);
    String startDate = UtilityDAO.convertToGregorian(inpStartDate);
    String endDate = UtilityDAO.convertToGregorian(inpEndDate);
    String clientId = info.vars.getStringParameter("inpadClientId");
    String inpPaymentStartDate = vars.getStringParameter("inppaymentStartdate");
    String inpPaymentEndDate = vars.getStringParameter("inppaymentEnddate");
    String paymentStartDate = UtilityDAO.convertToGregorian(inpPaymentStartDate);
    String paymentEndDate = UtilityDAO.convertToGregorian(inpPaymentEndDate);
    int countofDays;
    int days;
    int workingDay;
    String reflookupId = null;
    String reflookUpname = null;
    JSONObject json = new JSONObject();
    log4j.debug("lastfieldChanged:" + lastfieldChanged);
    try {
      UpdateEmpDetailsInCallouts callouts = new UpdateEmpDetailsInCallouts();
      EmploymentInfo empinfo = sa.elm.ob.hcm.util.UtilityDAO.getActiveEmployInfo(employeeId);
      // get Employee Details by using employeeId
      if (lastfieldChanged.equals("inpehcmEmpPerinfoId")) {
        if (StringUtils.isNotEmpty(employeeId)) {
          EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          info.addResult("inpempName", employee.getArabicfullname());
          info.addResult("inpehcmEmpPerinfoId", employeeId);

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
            employmentInfoId = empinfo.getId();
            info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
            if (empinfo.getPosition().getSection() != null) {
              info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
            } else {
              info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
            }
            info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
            info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
            info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
            info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
            info.addResult("inpassignedDept", empinfo.getSECDeptName());

          }
          if (empinfo.getStartDate() != null) {
            info.addResult("inpstartdate",
                (UtilityDAO.convertTohijriDate(dateFormat.format(empinfo.getStartDate()))));
          }

          inpdecisionType = DecisionType;
          info.addResult("inpdecisionType", inpdecisionType);
          info.addResult("inporiginalDecisionNo", "");
          info.addResult("inpovertimeAmount", null);
          /*
           * info.addResult("JSEXECUTE",
           * "form.getFieldFromColumnName('Ehcm_Payrolldef_Period_ID').setValue('')");
           */
          lastfieldChanged = "inpstartdate";
          inpStartDate = UtilityDAO.convertTohijriDate(dateFormat.format(empinfo.getStartDate()));
          startDate = UtilityDAO.convertToGregorian(inpStartDate);
        } else {
          callouts.SetEmpDetailsNull(info);
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Assigned_Dept').setValue('')");

        }
      }
      // Decision type is payment,update and cancel then set the previous original decision no
      if (lastfieldChanged.equals("inpdecisionType")) {
        if (!inpdecisionType.equals(DecisionType)) {

          // Load the decision details based on the chosen employee's original decision number
          if (inpdecisionType.equals(OvertimePayment)) {
            EhcmEmployeeOvertime objEmpQuery = sa.elm.ob.hcm.util.UtilityDAO
                .getOvertimeEmployee(employeeId);
            if (objEmpQuery != null) {
              info.addResult("inporiginalDecisionNo", objEmpQuery.getId());

              EhcmEmployeeOvertime overtimeObj = OBDal.getInstance().get(EhcmEmployeeOvertime.class,
                  objEmpQuery.getId());
              if (overtimeObj.getEhcmOvertimeType() != null) {
                info.addResult("inpehcmOvertimeTypeId", overtimeObj.getEhcmOvertimeType().getId());
                overtypeId = overtimeObj.getEhcmOvertimeType().getId();
              }
              if (overtimeObj.getJustification() != null)
                info.addResult("inpjustification", overtimeObj.getJustification());
              if (overtimeObj.getStartDate() != null)
                info.addResult("inpstartdate",
                    (UtilityDAO.convertTohijriDate(dateFormat.format(overtimeObj.getStartDate()))));
              if (overtimeObj.getEndDate() != null)
                info.addResult("inpenddate",
                    (UtilityDAO.convertTohijriDate(dateFormat.format(overtimeObj.getEndDate()))));
              if (overtimeObj.getWorkingHours() != null)
                info.addResult("inpworkingHours", overtimeObj.getWorkingHours().getId());
              if (overtimeObj.getWeekendonehours() != null)
                info.addResult("inpweekendonehours", overtimeObj.getWeekendonehours().getId());
              if (overtimeObj.getWeekendtwohours() != null)
                info.addResult("inpweekendtwohours", overtimeObj.getWeekendtwohours().getId());
              if (overtimeObj.getFeterhours() != null)
                info.addResult("inpfeterhours", overtimeObj.getFeterhours().getId());
              if (overtimeObj.getHajjhours() != null)
                info.addResult("inphajjhours", overtimeObj.getHajjhours().getId());
              if (overtimeObj.getNationalhours() != null)
                info.addResult("inpnationalhours", overtimeObj.getNationalhours().getId());

              if (overtimeObj.getDecisionType().equals(OvertimePayment)) {
                if (overtimeObj.getPaymentStartDate() != null) {
                  Date paySDate = overtimeObj.getPaymentEndDate();
                  GregorianCalendar cal = new GregorianCalendar();
                  cal.setTime(paySDate);
                  cal.add(Calendar.DATE, 1);
                  info.addResult("inppaymentStartdate",
                      (UtilityDAO.convertTohijriDate(dateFormat.format(cal.getTime()))));
                  inpPaymentStartDate = UtilityDAO
                      .convertTohijriDate(dateFormat.format(cal.getTime()));
                }
              } else {
                if (overtimeObj.getStartDate() != null)
                  info.addResult("inppaymentStartdate", (UtilityDAO
                      .convertTohijriDate(dateFormat.format(overtimeObj.getStartDate()))));
                inpPaymentStartDate = UtilityDAO
                    .convertTohijriDate(dateFormat.format(overtimeObj.getStartDate()));
              }
              if (overtimeObj.getEndDate() != null) {
                info.addResult("inppaymentEnddate",
                    (UtilityDAO.convertTohijriDate(dateFormat.format(overtimeObj.getEndDate()))));
                inpPaymentEndDate = UtilityDAO
                    .convertTohijriDate(dateFormat.format(overtimeObj.getEndDate()));
              }
              EhcmOvertimeType overtimeTypeObj = OBDal.getInstance().get(EhcmOvertimeType.class,
                  overtypeId);
              paymentStartDate = UtilityDAO.convertToGregorian(inpPaymentStartDate);
              paymentEndDate = UtilityDAO.convertToGregorian(inpPaymentEndDate);
              if (overtimeTypeObj != null) {
                // Working Day
                if (overtimeTypeObj.isWorkingdays().equals(true)) {
                  countofDays = sa.elm.ob.hcm.util.UtilityDAO.calculatetheDays(inpPaymentStartDate,
                      inpPaymentEndDate);
                  days = sa.elm.ob.hcm.util.UtilityDAO.getWorkingDaysFromHolidayCalendar(
                      paymentStartDate, paymentEndDate, inpEmpOvertimeId, clientId);
                  workingDay = countofDays - days;
                  info.addResult("inpworkingdays", workingDay);

                } else {
                  info.addResult("inpworkingdays", holiday);
                }
                // Feter days
                if (overtimeTypeObj.isFeterdays().equals(true)) {
                  holidayType = "FE";
                  days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(paymentStartDate,
                      paymentEndDate, holidayType, inpEmpOvertimeId, clientId);
                  info.addResult("inpfeterdays", days);

                } else {
                  info.addResult("inpfeterdays", holiday);
                }
                // Hajj Days
                if (overtimeTypeObj.isHajjdays().equals(true)) {
                  holidayType = "AD";
                  days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(paymentStartDate,
                      paymentEndDate, holidayType, inpEmpOvertimeId, clientId);
                  info.addResult("inphajjdays", days);

                } else {
                  info.addResult("inphajjdays", holiday);
                }
                // National Day
                if (overtimeTypeObj.isNationalday().equals(true)) {
                  holidayType = "NH";
                  days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(paymentStartDate,
                      paymentEndDate, holidayType, inpEmpOvertimeId, clientId);
                  info.addResult("inpnationalday", days);

                } else {
                  info.addResult("inpnationalday", holiday);
                }
                // WeekendoneDay
                if (overtimeTypeObj.isWeekendonedays().equals(true)) {
                  holidayType = "WE1";
                  days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(paymentStartDate,
                      paymentEndDate, holidayType, inpEmpOvertimeId, clientId);
                  info.addResult("inpweekendonedays", days);

                } else {
                  info.addResult("inpweekendonedays", holiday);
                }
                // WeekEndTwoDay
                if (overtimeTypeObj.isWeekendtwodays().equals(true)) {
                  holidayType = "WE2";
                  days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(paymentStartDate,
                      paymentEndDate, holidayType, inpEmpOvertimeId, clientId);
                  info.addResult("inpweekendtwodays", days);

                } else {
                  info.addResult("inpweekendtwodays", holiday);
                }
              }
            }
          } else {

            EhcmEmployeeOvertime objEmpQuery = sa.elm.ob.hcm.util.UtilityDAO
                .getOvertimeEmployee(employeeId);
            if (objEmpQuery != null) {
              EhcmEmployeeOvertime overtimeObj = OBDal.getInstance().get(EhcmEmployeeOvertime.class,
                  objEmpQuery.getId());

              if (!overtimeObj.getDecisionType().equals(OvertimePayment)) {
                info.addResult("inporiginalDecisionNo", objEmpQuery.getId());
                if (overtimeObj.getEhcmOvertimeType() != null)
                  info.addResult("inpehcmOvertimeTypeId",
                      overtimeObj.getEhcmOvertimeType().getId());
                if (overtimeObj.getJustification() != null)
                  info.addResult("inpjustification", overtimeObj.getJustification());
                if (overtimeObj.getStartDate() != null)
                  info.addResult("inpstartdate", (UtilityDAO
                      .convertTohijriDate(dateFormat.format(overtimeObj.getStartDate()))));
                if (overtimeObj.getEndDate() != null)
                  info.addResult("inpenddate",
                      (UtilityDAO.convertTohijriDate(dateFormat.format(overtimeObj.getEndDate()))));
                if (overtimeObj.getWorkingDays() != null)
                  info.addResult("inpworkingdays", overtimeObj.getWorkingDays());
                if (overtimeObj.getWorkingHours() != null)
                  info.addResult("inpworkingHours", overtimeObj.getWorkingHours().getId());
                if (overtimeObj.getWeekend1Days() != null)
                  info.addResult("inpweekendonedays", overtimeObj.getWeekend1Days());
                if (overtimeObj.getWeekendonehours() != null)
                  info.addResult("inpweekendonehours", overtimeObj.getWeekendonehours().getId());
                if (overtimeObj.getWeekend2Days() != null)
                  info.addResult("inpweekendtwodays", overtimeObj.getWeekend2Days());
                if (overtimeObj.getWeekendtwohours() != null)
                  info.addResult("inpweekendtwohours", overtimeObj.getWeekendtwohours().getId());
                if (overtimeObj.getFeterDays() != null)
                  info.addResult("inpfeterdays", overtimeObj.getFeterDays());
                if (overtimeObj.getFeterhours() != null)
                  info.addResult("inpfeterhours", overtimeObj.getFeterhours().getId());
                if (overtimeObj.getHajjDays() != null)
                  info.addResult("inphajjdays", overtimeObj.getHajjDays());
                if (overtimeObj.getHajjhours() != null)
                  info.addResult("inphajjhours", overtimeObj.getHajjhours().getId());
                if (overtimeObj.getNationalDay() != null)
                  info.addResult("inpnationalday", overtimeObj.getNationalDay());
                if (overtimeObj.getNationalhours() != null)
                  info.addResult("inpnationalhours", overtimeObj.getNationalhours().getId());
              } else {
                info.addResult("inporiginalDecisionNo", null);

              }
            }

          }
        } else {
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Original_Decision_No').setValue('')");
        }
        info.addResult("inpdecisionType", inpdecisionType);

      }
      if (!inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_OVERTIMEPAYMENT)) {
        info.addResult("inpovertimeAmount", null);
        /*
         * info.addResult("JSEXECUTE",
         * "form.getFieldFromColumnName('Ehcm_Payrolldef_Period_ID').setValue('')");
         */
      }

      // To calculate no of days based on overtime Type
      if (lastfieldChanged.equals("inpstartdate") || lastfieldChanged.equals("inpenddate")
          || lastfieldChanged.equals("inpehcmOvertimeTypeId")) {

        EhcmOvertimeType overtimeTypeObj = OBDal.getInstance().get(EhcmOvertimeType.class,
            overtypeId);
        JSONArray jsonArray = null;
        json = sa.elm.ob.hcm.util.UtilityDAO.gethoursFromReflookup(clientId);
        if (json != null && json.length() > 0) {
          jsonArray = json.getJSONArray("data");
        }
        if (overtimeTypeObj != null) {
          // Working Day
          if (overtimeTypeObj.isWorkingdays().equals(true)) {
            countofDays = sa.elm.ob.hcm.util.UtilityDAO.calculatetheDays(inpStartDate, inpEndDate);
            days = sa.elm.ob.hcm.util.UtilityDAO.getWorkingDaysFromHolidayCalendar(startDate,
                endDate, inpEmpOvertimeId, clientId);
            workingDay = countofDays - days;
            info.addResult("inpworkingdays", workingDay);
            info.addSelect("inpworkingHours");
            if (jsonArray != null) {
              if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                  json = jsonArray.getJSONObject(i);
                  reflookupId = json.getString("id").toString();
                  reflookUpname = json.getString("name").toString();
                  info.addSelectResult(reflookupId, reflookUpname, false);

                }

              }
            }
            info.endSelect();

          } else {
            info.addResult("inpworkingdays", holiday);
            info.addSelect("inpworkingHours");
            info.addSelectResult("", "", true);
            info.endSelect();

          }
          // Feter days
          if (overtimeTypeObj.isFeterdays().equals(true)) {
            holidayType = "FE";
            days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(startDate, endDate,
                holidayType, inpEmpOvertimeId, clientId);
            info.addResult("inpfeterdays", days);
            info.addSelect("inpfeterhours");
            if (jsonArray != null) {
              if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                  json = jsonArray.getJSONObject(i);
                  reflookupId = json.getString("id").toString();
                  reflookUpname = json.getString("name").toString();
                  info.addSelectResult(reflookupId, reflookUpname, false);

                }

              }
            }
            info.endSelect();

          } else {
            info.addResult("inpfeterdays", holiday);
            info.addSelect("inpfeterhours");
            info.addSelectResult("", "", true);
            info.endSelect();
          }
          // Hajj Days
          if (overtimeTypeObj.isHajjdays().equals(true)) {
            holidayType = "AD";
            days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(startDate, endDate,
                holidayType, inpEmpOvertimeId, clientId);
            info.addResult("inphajjdays", days);
            info.addSelect("inphajjhours");
            if (jsonArray != null) {
              if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                  json = jsonArray.getJSONObject(i);
                  reflookupId = json.getString("id").toString();
                  reflookUpname = json.getString("name").toString();
                  info.addSelectResult(reflookupId, reflookUpname, false);

                }

              }
            }
            info.endSelect();

          } else {
            info.addResult("inphajjdays", holiday);
            info.addSelect("inphajjhours");
            info.addSelectResult("", "", true);
            info.endSelect();
          }
          // National Day
          if (overtimeTypeObj.isNationalday().equals(true)) {
            holidayType = "NH";
            days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(startDate, endDate,
                holidayType, inpEmpOvertimeId, clientId);
            info.addResult("inpnationalday", days);
            info.addSelect("inpnationalhours");
            if (jsonArray != null) {
              if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                  json = jsonArray.getJSONObject(i);
                  reflookupId = json.getString("id").toString();
                  reflookUpname = json.getString("name").toString();
                  info.addSelectResult(reflookupId, reflookUpname, false);

                }

              }
            }
            info.endSelect();

          } else {
            info.addResult("inpnationalday", holiday);
            info.addSelect("inpnationalhours");
            info.addSelectResult("", "", true);
            info.endSelect();
          }
          // WeekendoneDay
          if (overtimeTypeObj.isWeekendonedays().equals(true)) {
            holidayType = "WE1";
            days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(startDate, endDate,
                holidayType, inpEmpOvertimeId, clientId);
            info.addResult("inpweekendonedays", days);
            info.addSelect("inpweekendonehours");
            if (jsonArray != null) {
              if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                  json = jsonArray.getJSONObject(i);
                  reflookupId = json.getString("id").toString();
                  reflookUpname = json.getString("name").toString();
                  info.addSelectResult(reflookupId, reflookUpname, false);

                }

              }
            }
            info.endSelect();

          } else {
            info.addResult("inpweekendonedays", holiday);
            info.addSelect("inpweekendonehours");
            info.addSelectResult("", "", true);
            info.endSelect();

          }
          // WeekEndTwoDay
          if (overtimeTypeObj.isWeekendtwodays().equals(true)) {
            holidayType = "WE2";
            days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(startDate, endDate,
                holidayType, inpEmpOvertimeId, clientId);
            info.addResult("inpweekendtwodays", days);
            info.addSelect("inpweekendtwohours");
            if (jsonArray != null) {
              if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                  json = jsonArray.getJSONObject(i);
                  reflookupId = json.getString("id").toString();
                  reflookUpname = json.getString("name").toString();
                  info.addSelectResult(reflookupId, reflookUpname, false);

                }

              }
            }
            info.endSelect();

          } else {
            info.addResult("inpweekendtwodays", holiday);
            info.addSelect("inpweekendtwohours");
            info.addSelectResult("", "", true);
            info.endSelect();
          }
        }

      }
      // To calculate no of days based on payment end date
      if (lastfieldChanged.equals("inppaymentEnddate")) {

        EhcmOvertimeType overtimeTypeObj = OBDal.getInstance().get(EhcmOvertimeType.class,
            overtypeId);

        if (overtimeTypeObj != null) {
          // Working Day
          if (overtimeTypeObj.isWorkingdays().equals(true)) {
            countofDays = sa.elm.ob.hcm.util.UtilityDAO.calculatetheDays(inpPaymentStartDate,
                inpPaymentEndDate);
            days = sa.elm.ob.hcm.util.UtilityDAO.getWorkingDaysFromHolidayCalendar(paymentStartDate,
                paymentEndDate, inpEmpOvertimeId, clientId);
            workingDay = countofDays - days;
            info.addResult("inpworkingdays", workingDay);

          } else {
            info.addResult("inpworkingdays", holiday);

          }
          // Feter days
          if (overtimeTypeObj.isFeterdays().equals(true)) {
            holidayType = "FE";
            days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(paymentStartDate,
                paymentEndDate, holidayType, inpEmpOvertimeId, clientId);
            info.addResult("inpfeterdays", days);

          } else {
            info.addResult("inpfeterdays", holiday);

          }
          // Hajj Days
          if (overtimeTypeObj.isHajjdays().equals(true)) {
            holidayType = "AD";
            days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(paymentStartDate,
                paymentEndDate, holidayType, inpEmpOvertimeId, clientId);
            info.addResult("inphajjdays", days);

          } else {
            info.addResult("inphajjdays", holiday);

          }
          // National Day
          if (overtimeTypeObj.isNationalday().equals(true)) {
            holidayType = "NH";
            days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(paymentStartDate,
                paymentEndDate, holidayType, inpEmpOvertimeId, clientId);
            info.addResult("inpnationalday", days);

          } else {
            info.addResult("inpnationalday", holiday);

          }
          // WeekendoneDay
          if (overtimeTypeObj.isWeekendonedays().equals(true)) {
            holidayType = "WE1";
            days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(paymentStartDate,
                paymentEndDate, holidayType, inpEmpOvertimeId, clientId);
            info.addResult("inpweekendonedays", days);

          } else {
            info.addResult("inpweekendonedays", holiday);

          }
          // WeekEndTwoDay
          if (overtimeTypeObj.isWeekendtwodays().equals(true)) {
            holidayType = "WE2";
            days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(paymentStartDate,
                paymentEndDate, holidayType, inpEmpOvertimeId, clientId);
            info.addResult("inpweekendtwodays", days);

          } else {
            info.addResult("inpweekendtwodays", holiday);

          }
        }

      }

    } catch (

    Exception e) {
      log4j.error("Exception in EmpOvertime Callout", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

}
