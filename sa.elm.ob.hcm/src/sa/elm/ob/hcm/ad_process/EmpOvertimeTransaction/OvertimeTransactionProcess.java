package sa.elm.ob.hcm.ad_process.EmpOvertimeTransaction;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMElmttypeDef;
import sa.elm.ob.hcm.EhcmEmployeeOvertime;
import sa.elm.ob.hcm.EhcmPayrollGlobalValue;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ehcmgradeclass;
import sa.elm.ob.hcm.ehcmpayscaleline;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.Payroll.PayrollBaseProcess;
import sa.elm.ob.hcm.ad_process.Payroll.PayrollBaseProcessDAO;
import sa.elm.ob.hcm.util.PayrollConstants;
import sa.elm.ob.hcm.util.Utility;

/**
 * 
 * @author poongodi on 13-03-2018
 *
 */

public class OvertimeTransactionProcess implements Process {
  private static final Logger log4j = LoggerFactory.getLogger(OvertimeTransactionProcess.class);
  private final OBError obError = new OBError();
  static JSONObject payRollComponents = new JSONObject();
  private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  private static long WORKING_DAY = 0;
  private static BigDecimal WORKING_HOUR = BigDecimal.ZERO;
  private static long WEEKEND1_DAY = 0;
  private static BigDecimal WEEKEND1_HOUR = BigDecimal.ZERO;
  private static long WEEKEND2_DAY = 0;
  private static BigDecimal WEEKEND2_HOUR = BigDecimal.ZERO;
  private static long FETER_DAY = 0;
  private static BigDecimal FETER_HOUR = BigDecimal.ZERO;
  private static long HAJJ_DAY = 0;
  private static BigDecimal HAJJ_HOUR = BigDecimal.ZERO;
  private static long NATIONAL_DAY = 0;
  private static BigDecimal NATIONAL_HOUR = BigDecimal.ZERO;

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    final String EmpOvertimeId = bundle.getParams().get("Ehcm_Emp_Overtime_ID").toString();
    EhcmEmployeeOvertime overtimeObj = OBDal.getInstance().get(EhcmEmployeeOvertime.class,
        EmpOvertimeId);
    String overtimeTypeId = overtimeObj.getEhcmOvertimeType().getId();
    OvertimeTransactionDAO empovertimeDAOImpl = new OvertimeTransactionImpl();
    String clientId = vars.getClient();
    String holidayType = null;
    int count = 0;

    try {
      OBContext.setAdminMode();
      // check whether the employee is suspended or not
      if (overtimeObj.getEmployee().getEmploymentStatus()
          .equals(DecisionTypeConstants.EMPLOYMENTSTATUS_SUSPENDED)) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_emplo_suspend"));
        bundle.setResult(obError);
        return;
      }

      // condition for checking the hours when days greater than 0
      if (new BigDecimal(overtimeObj.getWorkingDays()).compareTo(new BigDecimal(0)) > 0) {
        if (overtimeObj.getWorkingHours() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_Workinghr_Empty"));
          bundle.setResult(obError);
          return;
        }
      }
      if (new BigDecimal(overtimeObj.getWeekend1Days()).compareTo(new BigDecimal(0)) > 0) {
        if (overtimeObj.getWeekendonehours() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_Weekendhr_Empty"));
          bundle.setResult(obError);
          return;
        }
      }
      if (new BigDecimal(overtimeObj.getWeekend2Days()).compareTo(new BigDecimal(0)) > 0) {
        if (overtimeObj.getWeekendtwohours() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_Weekendtwohr_Empty"));
          bundle.setResult(obError);
          return;
        }
      }
      if (new BigDecimal(overtimeObj.getHajjDays()).compareTo(new BigDecimal(0)) > 0) {
        if (overtimeObj.getHajjhours() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_Hajjhr_Empty"));
          bundle.setResult(obError);
          return;
        }
      }
      if (new BigDecimal(overtimeObj.getFeterDays()).compareTo(new BigDecimal(0)) > 0) {
        if (overtimeObj.getFeterhours() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_Feterhr_Empty"));
          bundle.setResult(obError);
          return;
        }
      }
      if (new BigDecimal(overtimeObj.getNationalDay()).compareTo(new BigDecimal(0)) > 0) {
        if (overtimeObj.getNationalhours() == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_Nationalhr_Empty"));
          bundle.setResult(obError);
          return;
        }
      }

      // checking decision overlap
      if (overtimeObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
          || (overtimeObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE))) {
        JSONObject result = Utility.chkDecisionOverlap(Constants.OVERTIME_OVERLAP,
            sa.elm.ob.utility.util.Utility.formatDate(overtimeObj.getStartDate()),
            sa.elm.ob.utility.util.Utility.formatDate(overtimeObj.getEndDate()),
            overtimeObj.getEmployee().getId(), overtimeObj.getEhcmOvertimeType().getId(),
            overtimeObj.getId());
        if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
          if (overtimeObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
              || (overtimeObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                  && (result.has("overTimeId") && !result.getString("overTimeId")
                      .equals(overtimeObj.getOriginalDecisionNo().getId()))
                  || !result.has("overTimeId"))) {
            if (result.has("errormsg")) {
              obError.setType("Error");
              obError.setTitle("Error");
              obError.setMessage(OBMessageUtils.messageBD(result.getString("errormsg")));
              bundle.setResult(obError);
              return;
            }
          }
        }
      }

      if (overtimeObj.getDecisionType()
          .equals(DecisionTypeConstants.DECISION_TYPE_OVERTIMEPAYMENT)) {
        EHCMElmttypeDef ElementTypeDef = null;
        int countofDays = 0;
        int days = 0;
        int workingDay = 0;
        BigDecimal elementCalculatedValue = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;

        // get payroll Element for Overtime.
        OBQuery<EHCMElmttypeDef> ElementDefList = OBDal.getInstance()
            .createQuery(EHCMElmttypeDef.class, " baseProcess='OT' order by creationDate desc ");
        ElementDefList.setMaxResult(1);
        ElementTypeDef = ElementDefList.list().get(0);

        // Global Values
        List<EhcmPayrollGlobalValue> globalValueList = PayrollBaseProcessDAO
            .getGlobalValues(clientId);
        for (EhcmPayrollGlobalValue globalValue : globalValueList) {
          if (globalValue.getType().equalsIgnoreCase("C")) {
            payRollComponents.put(globalValue.getCode(), "'" + globalValue.getCharValue() + "'");
          } else if (globalValue.getType().equalsIgnoreCase("D")) {
            payRollComponents.put(globalValue.getCode(),
                "new Date('" + globalValue.getDateValue() + "')");
          } else {
            payRollComponents.put(globalValue.getCode(), globalValue.getNumericValue());
          }
        }

        String otStartDateSql = sa.elm.ob.utility.util.Utility
            .formatDate(overtimeObj.getPaymentStartDate());
        String otEndDateSql = sa.elm.ob.utility.util.Utility
            .formatDate(overtimeObj.getPaymentEndDate());

        // Employment Details & Eligibility
        List<EmploymentInfo> employmentList = PayrollBaseProcessDAO
            .getEmploymentsOfEmployee(overtimeObj.getEmployee(), otStartDateSql, otEndDateSql);

        for (EmploymentInfo employment : employmentList) {

          // Checking element eligible for the employment
          EhcmPosition position = employment.getPosition();
          Organization department = employment.getPosition().getDepartment();
          Jobs job = employment.getPosition().getEhcmJobs();
          ehcmgrade positionGrade = employment.getPosition().getGrade();
          ehcmgradeclass gradeClass = employment.getPosition().getGrade().getEhcmGradeclass();

          // check Element Eligibility criteria
          boolean isEligible = PayrollBaseProcessDAO.checkElementEligibleForEmployment(
              ElementTypeDef, position, department, job, positionGrade, gradeClass);
          if (isEligible) {

            // calculate formula
            String formula = empovertimeDAOImpl.getOverTimeFormula(ElementTypeDef, false,
                overtimeObj);

            // get applicable employment date in overtime period.
            JSONObject empmentPeriodJSON = PayrollBaseProcess.getOverlapingDateRange(
                employment.getStartDate(), employment.getEndDate(),
                overtimeObj.getPaymentStartDate(), overtimeObj.getPaymentEndDate());
            Date empmentStartDate = dateFormat.parse(empmentPeriodJSON.getString("startDate"));
            Date empmentEndDate = dateFormat.parse(empmentPeriodJSON.getString("endDate"));

            List<ehcmpayscaleline> payScaleLneList = empovertimeDAOImpl
                .getPayscaleValues(employment, empmentStartDate, empmentEndDate);

            // pay scale loop for an employment
            for (ehcmpayscaleline payScaleLn : payScaleLneList) {
              // get applicable pay scale date from employment period.
              JSONObject payScalePeriodJSON = PayrollBaseProcess.getOverlapingDateRange(
                  payScaleLn.getStartDate(), payScaleLn.getEndDate(), empmentStartDate,
                  empmentEndDate);

              if (payScalePeriodJSON != null) {
                Date payscaleStartDate = dateFormat
                    .parse(payScalePeriodJSON.getString("startDate"));
                Date payscaleEndDate = dateFormat.parse(payScalePeriodJSON.getString("endDate"));
                String payscaleStrStartDate = payScalePeriodJSON.getString("startDate");
                String payscaleStrEndDate = payScalePeriodJSON.getString("endDate");

                String formula1 = formula;

                // get Overtime Workingday and Hours.
                if (overtimeObj.getWorkingDays() > 0) {
                  countofDays = 0;
                  days = 0;
                  countofDays = sa.elm.ob.hcm.util.UtilityDAO
                      .caltheDaysUsingGreDate(payscaleStartDate, payscaleEndDate);
                  days = sa.elm.ob.hcm.util.UtilityDAO.getWorkingDaysFromHolidayCalendar(
                      payscaleStrStartDate, payscaleStrEndDate, "", clientId);
                  workingDay = countofDays - days;

                  WORKING_DAY = workingDay;
                  WORKING_HOUR = new BigDecimal(overtimeObj.getWorkingHours().getName());
                }

                // get Overtime weekend1 and Hours.
                if (overtimeObj.getWeekend1Days() > 0) {
                  days = 0;
                  holidayType = "WE1";
                  days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(
                      payscaleStrStartDate, payscaleStrEndDate, holidayType, "", clientId);

                  WEEKEND1_DAY = days;
                  WEEKEND1_HOUR = new BigDecimal(overtimeObj.getWeekendonehours().getName());
                }

                // get Overtime weekend2 and Hours.
                if (overtimeObj.getWeekend2Days() > 0) {
                  days = 0;
                  holidayType = "WE2";
                  days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(
                      payscaleStrStartDate, payscaleStrEndDate, holidayType, "", clientId);

                  WEEKEND2_DAY = days;
                  WEEKEND2_HOUR = new BigDecimal(overtimeObj.getWeekendtwohours().getName());
                }

                // get Overtime feter day and hours
                if (overtimeObj.getFeterDays() > 0) {
                  days = 0;
                  holidayType = "FE";
                  days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(
                      payscaleStrStartDate, payscaleStrEndDate, holidayType, "", clientId);

                  FETER_DAY = days;
                  FETER_HOUR = new BigDecimal(overtimeObj.getFeterhours().getName());
                }

                // get Overtime hajj day and hours
                if (overtimeObj.getHajjDays() > 0) {
                  days = 0;
                  holidayType = "AD";
                  days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(
                      payscaleStrStartDate, payscaleStrEndDate, holidayType, "", clientId);

                  HAJJ_DAY = days;
                  HAJJ_HOUR = new BigDecimal(overtimeObj.getHajjhours().getName());
                }

                // get Overtime National day and hour
                if (overtimeObj.getNationalDay() > 0) {
                  days = 0;
                  holidayType = "NH";
                  days = sa.elm.ob.hcm.util.UtilityDAO.getDaysFromHolidayCalendar(
                      payscaleStrStartDate, payscaleStrEndDate, holidayType, "", clientId);

                  NATIONAL_DAY = days;
                  NATIONAL_HOUR = new BigDecimal(overtimeObj.getNationalhours().getName());
                }

                // replace overtime values in formula
                formula1 = replaceValueFormula(formula1);

                // replace pay scale value in formula
                formula1 = formula1.replace(PayrollConstants.ELEMENT_PAYSCALE_CODE,
                    payScaleLn.getAmount().toString());

                // replace global working hour value in formula
                formula1 = formula1.replace(PayrollConstants.GLOBAL_WORKINGHOURS_MONTH,
                    payRollComponents.getString(PayrollConstants.GLOBAL_WORKINGHOURS_MONTH));

                if (!StringUtils.isEmpty(formula)) {
                  log4j.info("OT Formula ===> " + formula1);
                  elementCalculatedValue = PayrollBaseProcessDAO
                      .calculateFormulaValue(ElementTypeDef, formula1);
                  totalValue = totalValue.add(elementCalculatedValue);
                }
              }

              // clear Values of overtime
              clearOvertimeValues();
            }
          }

        }
        overtimeObj.setOvertimeAmount(totalValue);
        OBDal.getInstance().save(overtimeObj);
      }

      // For selected overtime Type the days will be zero then throw the error
      count = empovertimeDAOImpl.getOvertimeTypeRecord(overtimeTypeId, overtimeObj);
      if (count == 1) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_OvertimeDays_Empty"));
        bundle.setResult(obError);
        return;
      }

      // check Issued or not
      if (!overtimeObj.isSueDecision()) {
        // update status as Issued and set decision date for all cases
        overtimeObj.setSueDecision(true);
        overtimeObj.setDecisionDate(new Date());
        overtimeObj.setDecisionStatus("I");
        OBDal.getInstance().save(overtimeObj);
        OBDal.getInstance().flush();

        if (overtimeObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
          // update old overtime as inactive
          EhcmEmployeeOvertime oldOvertimeObj = overtimeObj.getOriginalDecisionNo();
          oldOvertimeObj.setEnabled(false);
          OBDal.getInstance().save(oldOvertimeObj);
          OBDal.getInstance().flush();
        }

        if (overtimeObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
          // update old overtime as inactive
          EhcmEmployeeOvertime oldOvertimeObj = overtimeObj.getOriginalDecisionNo();
          oldOvertimeObj.setEnabled(false);
          OBDal.getInstance().save(oldOvertimeObj);
          OBDal.getInstance().flush();

          overtimeObj.setEnabled(false);
          OBDal.getInstance().save(overtimeObj);
          OBDal.getInstance().flush();
        }
        if (overtimeObj.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_OVERTIMEPAYMENT)) {
          // update old overtime as inactive
          EhcmEmployeeOvertime oldOvertimeObj = overtimeObj.getOriginalDecisionNo();
          String endDate = overtimeObj.getEndDate().toString();
          String paymentEndDate = overtimeObj.getPaymentEndDate().toString();
          oldOvertimeObj.setEnabled(false);
          OBDal.getInstance().save(oldOvertimeObj);
          OBDal.getInstance().flush();

          if (endDate.equals(paymentEndDate)) {
            overtimeObj.setEnabled(false);
            OBDal.getInstance().save(overtimeObj);
            OBDal.getInstance().flush();
          }

        }

        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_ExtraStep_Process"));
        bundle.setResult(obError);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();

      }

    }

    catch (Exception e) {
      log4j.debug("Error while doing overtime transacton process");
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      Throwable t = DbUtility.getUnderlyingSQLException(e);
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  /**
   * Clear Overtime Element values
   */
  public void clearOvertimeValues() {
    try {
      WORKING_DAY = 0;
      WORKING_HOUR = BigDecimal.ZERO;
      WEEKEND1_DAY = 0;
      WEEKEND1_HOUR = BigDecimal.ZERO;
      WEEKEND2_DAY = 0;
      WEEKEND2_HOUR = BigDecimal.ZERO;
      FETER_DAY = 0;
      FETER_HOUR = BigDecimal.ZERO;
      HAJJ_DAY = 0;
      HAJJ_HOUR = BigDecimal.ZERO;
      NATIONAL_DAY = 0;
      NATIONAL_HOUR = BigDecimal.ZERO;
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Error while clearOvertimeValues" + e);
    }
  }

  /**
   * Replace formula with over time value.
   * 
   * @param formula
   * @return
   */
  public String replaceValueFormula(String formula) {
    String ReplacedFormula = formula;
    try {
      ReplacedFormula = ReplacedFormula.replace(PayrollConstants.OVERTIME_WORKINGDAYS,
          String.valueOf(WORKING_DAY));
      ReplacedFormula = ReplacedFormula.replace(PayrollConstants.OVERTIME_WORKINGHOURS,
          WORKING_HOUR.toString());

      ReplacedFormula = ReplacedFormula.replace(PayrollConstants.OVERTIME_WEEKEND1DAYS,
          String.valueOf(WEEKEND1_DAY));
      ReplacedFormula = ReplacedFormula.replace(PayrollConstants.OVERTIME_WEEKEND1HOURS,
          WEEKEND1_HOUR.toString());

      ReplacedFormula = ReplacedFormula.replace(PayrollConstants.OVERTIME_WEEKEND2DAYS,
          String.valueOf(WEEKEND2_DAY));
      ReplacedFormula = ReplacedFormula.replace(PayrollConstants.OVERTIME_WEEKEND2HOURS,
          WEEKEND2_HOUR.toString());

      ReplacedFormula = ReplacedFormula.replace(PayrollConstants.OVERTIME_FETERDAYS,
          String.valueOf(FETER_DAY));
      ReplacedFormula = ReplacedFormula.replace(PayrollConstants.OVERTIME_FETERHOURS,
          FETER_HOUR.toString());

      ReplacedFormula = ReplacedFormula.replace(PayrollConstants.OVERTIME_HAJJDAYS,
          String.valueOf(HAJJ_DAY));
      ReplacedFormula = ReplacedFormula.replace(PayrollConstants.OVERTIME_HAJJHOURS,
          HAJJ_HOUR.toString());

      ReplacedFormula = ReplacedFormula.replace(PayrollConstants.OVERTIME_NATIONALDAYS,
          String.valueOf(NATIONAL_DAY));
      ReplacedFormula = ReplacedFormula.replace(PayrollConstants.OVERTIME_NATIONALHOURS,
          NATIONAL_HOUR.toString());
      return ReplacedFormula;

    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Error while clearOvertimeValues" + e);
      return null;
    }
  }

}