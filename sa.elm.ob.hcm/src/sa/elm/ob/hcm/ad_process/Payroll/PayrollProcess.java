package sa.elm.ob.hcm.ad_process.Payroll;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMBenefitAllowance;
import sa.elm.ob.hcm.EHCMEarnDeductHdr;
import sa.elm.ob.hcm.EHCMEarnDeductLne;
import sa.elm.ob.hcm.EHCMElementECriteria;
import sa.elm.ob.hcm.EHCMElementFormulaHdr;
import sa.elm.ob.hcm.EHCMElementFormulaLne;
import sa.elm.ob.hcm.EHCMEligbltyCriteria;
import sa.elm.ob.hcm.EHCMElmttypeDef;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMPayrollDefinition;
import sa.elm.ob.hcm.EHCMPayrolldefPeriod;
import sa.elm.ob.hcm.EHCMPayscalePointV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmPayrollGlobalValue;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ehcmgradeclass;
import sa.elm.ob.hcm.ehcmgraderatelines;
import sa.elm.ob.hcm.ehcmgraderates;
import sa.elm.ob.hcm.ehcmgradesteps;
import sa.elm.ob.hcm.ehcmpayscale;
import sa.elm.ob.hcm.ehcmpayscaleline;
import sa.elm.ob.hcm.ehcmprogressionpoint;
import sa.elm.ob.hcm.ad_process.Constants;

public class PayrollProcess extends DalBaseProcess {
  private static final Logger log = LoggerFactory.getLogger(EHCMPayrolldefPeriod.class);
  private final OBError obError = new OBError();
  private static final BigDecimal percentMax = new BigDecimal("100");
  private static final String GradeRateCode = "GRADERATE";
  private static int hours = 24, minutes = 60, seconds = 60, milliseconds = 1000;
  private static JSONObject payRollComponents = null;
  private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  private static boolean errorFlag = false;
  private static String errorMessage = "";
  private static final String allowanceFixed = "FA";
  private static final String allowancePercent = "P";
  private static final String percentBasic = "BS";
  private static final String percentFistStepGrade = "GS1";

  @SuppressWarnings("unused")
  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    int periodType = 0, year = 0, MM = 0, YYYY = 0, maxYear = 0;
    Date nextStartDate = null;

    try {
      OBContext.setAdminMode(true);
      String payrollPeriodId = (String) bundle.getParams().get("Ehcm_Payrolldef_Period_ID");
      EHCMPayrolldefPeriod PayrollDefPeriod = OBDal.getInstance().get(EHCMPayrolldefPeriod.class,
          payrollPeriodId);
      EHCMPayrollDefinition payrollDef = PayrollDefPeriod.getEhcmPayrollDefinition();
      Date startDate = dateFormat.parse(PayrollDefPeriod.getStartDate().toString());
      Date endDate = PayrollDefPeriod.getEndDate() != null
          ? dateFormat.parse(PayrollDefPeriod.getEndDate().toString())
          : null;
      long payRollDefDays = 30;
      if (startDate != null && endDate != null) {
        long diffMillSeconds = Math.abs(startDate.getTime() - endDate.getTime());
        payRollDefDays = (diffMillSeconds / (hours * minutes * seconds * milliseconds)) + 1;
      }

      final String clientId = (String) bundle.getContext().getClient();
      final String orgId = PayrollDefPeriod.getOrganization().getId();
      final String userId = (String) bundle.getContext().getUser();
      final String roleId = (String) bundle.getContext().getRole();
      PreparedStatement st = null;
      ResultSet rs = null;
      Connection conn = OBDal.getInstance().getConnection();
      NumberFormat Numformatter = new DecimalFormat("00");
      final Session session = OBDal.getInstance().getSession();
      payRollComponents = new JSONObject();

      // Remove existing employee and elements of this payroll
      errorFlag = PayrollPreDefineCompDAO.removeExistingPayrollEmp(PayrollDefPeriod);

      if (!errorFlag) {
        String periodStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
        String periodEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

        log.info(
            "Getting employess of the payroll period: " + periodStartDate + " : " + periodEndDate);

        if (!errorFlag) {
          // Fetching employees in the selected payroll
          List<EhcmEmpPerInfo> employeeList = getEmployeesFromPayroll(payrollDef, periodStartDate,
              periodEndDate, session);

          if (employeeList != null) {
            // Get Global Elements
            getGlobalPreDefinedElements(clientId);

            for (EhcmEmpPerInfo empPerInfo : employeeList) {
              HashMap<String, List<EHCMEarnDeductLne>> elementValueMap = new HashMap<String, List<EHCMEarnDeductLne>>();

              // Generate Employee Based Pre Defined Elements
              generateEmployeeBasedPreDefElements(empPerInfo);

              if (!errorFlag) {
                log.info("Getting Available Elements");
                // Getting Available Elements
                List<EHCMElmttypeDef> elementList = getAvailablePayrollElementsInClient(clientId);

                for (EHCMElmttypeDef payrollElement : elementList) {

                  // Calculate Processing Days based on Month Days Setup in Element
                  BigDecimal processingDays = null;
                  BigDecimal differenceDays = BigDecimal.ZERO;
                  if (payrollElement.getMonthDays().equalsIgnoreCase("GV")) {
                    BigDecimal globalValue = payrollElement.getGlobalValue().getNumericValue();
                    processingDays = globalValue;
                    if (payrollElement.getType().equalsIgnoreCase("REC")) {
                      differenceDays = globalValue.subtract(new BigDecimal(payRollDefDays));
                    }
                  } else {
                    processingDays = new BigDecimal(payRollDefDays);
                  }

                  log.info(
                      "processingDays : " + processingDays + " differenceDays : " + differenceDays);

                  if (!errorFlag) {
                    if (payrollElement.getBaseProcess().equalsIgnoreCase("E")) {

                      log.info("Getting employments for Employee: " + empPerInfo.getName()
                          + " Employment Element : " + payrollElement.getName());

                      // Fetching employments for an employee
                      List<EmploymentInfo> employmentList = getEmploymentsOfEmployee(empPerInfo,
                          periodStartDate, periodEndDate);

                      for (EmploymentInfo employment : employmentList) {
                        if (!errorFlag) {
                          // Checking element eligible for the employment
                          EhcmPosition position = employment.getPosition();
                          Organization department = employment.getPosition().getDepartment();
                          Jobs job = employment.getPosition().getEhcmJobs();
                          ehcmgrade positionGrade = employment.getPosition().getGrade();
                          ehcmgradeclass gradeClass = employment.getPosition().getGrade()
                              .getEhcmGradeclass();
                          ehcmgrade grade = employment.getEmploymentgrade();

                          boolean isEligible = checkElementEligibleForEmployment(payrollElement,
                              position, department, job, positionGrade, gradeClass);

                          BigDecimal totalEmploymentValue = BigDecimal.ZERO;

                          if (isEligible) {
                            log.info("Element " + payrollElement.getName()
                                + " is Eligible for Employment Grade "
                                + employment.getEmploymentgrade().getSearchKey());

                            // Converting employment dates to proper date format
                            Date emplStartDate = dateFormat
                                .parse(employment.getStartDate().toString());
                            Date emplEndDate = employment.getEndDate() != null
                                ? dateFormat.parse(employment.getEndDate().toString())
                                : null;

                            // Calculating applicable days in employment according to payrollDate
                            JSONObject employmentPeriodJSON = getOverlapingDateRange(emplStartDate,
                                emplEndDate, startDate, endDate);

                            if (employmentPeriodJSON != null) {
                              Date employmentStartDate = dateFormat
                                  .parse(employmentPeriodJSON.getString("startDate"));
                              Date employmentEndDate = dateFormat
                                  .parse(employmentPeriodJSON.getString("endDate"));

                              log.info("Applicable Dates for Employment "
                                  + employment.getEmploymentgrade().getSearchKey());
                              log.info(employmentPeriodJSON.getString("startDate") + " to "
                                  + employmentPeriodJSON.getString("endDate"));
                              log.info("No of days : " + employmentPeriodJSON.getString("days"));

                              String employStartDate = sa.elm.ob.utility.util.Utility
                                  .formatDate(employmentStartDate);
                              String employEndDate = sa.elm.ob.utility.util.Utility
                                  .formatDate(employmentEndDate);

                              BigDecimal value = BigDecimal.ZERO;
                              // Check Extend Process for Validation
                              if (payrollElement.getExtendProcess() != null) {
                                log.info("Element '" + payrollElement.getName()
                                    + "' has Extend Process '" + payrollElement.getExtendProcess()
                                    + "'");
                                if (payrollElement.getExtendProcess().equalsIgnoreCase("AD")) {
                                  log.info("Getting Allowance for validation for Employee: "
                                      + empPerInfo.getName() + " Employment Element : "
                                      + payrollElement.getName());

                                  // Fetching allowance decision for an employee and element
                                  List<EHCMBenefitAllowance> allowanceDecisionList = getAllowanceDecisionForEmployee(
                                      empPerInfo, payrollElement, employStartDate, employEndDate);

                                  log.info("Allowance Size for Emp : " + empPerInfo.getName()
                                      + " Element : " + payrollElement.getName() + " is "
                                      + allowanceDecisionList.size());

                                  for (EHCMBenefitAllowance allowancDecision : allowanceDecisionList) {
                                    if (!errorFlag) {
                                      // Converting Allowance dates to proper date format
                                      Date allowStartDate = dateFormat
                                          .parse(allowancDecision.getStartDate().toString());
                                      Date allowEndDate = allowancDecision.getEndDate() != null
                                          ? dateFormat.parse(
                                              allowancDecision.getEndDate().toString())
                                          : null;

                                      // Calculating applicable days in Allowance according to
                                      // payrollDate
                                      JSONObject allowancePeriodJSON = getOverlapingDateRange(
                                          allowStartDate, allowEndDate, employmentStartDate,
                                          employmentEndDate);

                                      if (allowancePeriodJSON != null) {
                                        Date allowanceStartDate = dateFormat
                                            .parse(allowancePeriodJSON.getString("startDate"));
                                        Date allowanceEndDate = dateFormat
                                            .parse(allowancePeriodJSON.getString("endDate"));

                                        log.info("Applicable Dates for Allowance "
                                            + allowancDecision.getDecisionNo());
                                        log.info(allowancePeriodJSON.getString("startDate") + " to "
                                            + allowancePeriodJSON.getString("endDate"));
                                        log.info("No of days : "
                                            + allowancePeriodJSON.getString("days"));

                                        value = calculateEmploymentBasedValue(payrollElement,
                                            employment, allowanceStartDate, allowanceEndDate,
                                            processingDays, differenceDays, endDate);

                                        // Sum up value to total value
                                        totalEmploymentValue = totalEmploymentValue.add(value);

                                      } else {
                                        break;
                                      }

                                    } else {
                                      break;
                                    }
                                  }
                                }
                              } else {
                                value = calculateEmploymentBasedValue(payrollElement, employment,
                                    employmentStartDate, employmentEndDate, processingDays,
                                    differenceDays, endDate);

                                // Sum up value to total value
                                totalEmploymentValue = totalEmploymentValue.add(value);
                              }

                              log.info("Sum up value for Element '" + payrollElement.getName()
                                  + "' is " + totalEmploymentValue);

                            } else {
                              break;
                            }
                          }

                          if (!errorFlag) {
                            if (elementValueMap.containsKey(payrollElement.getId())) {
                              // Sum up similar element values
                              List<EHCMEarnDeductLne> calcLineList = elementValueMap
                                  .get(payrollElement.getId());

                              // If Multiple Entries Allows do not sum
                              if (payrollElement.getType().equalsIgnoreCase("NREC")
                                  && payrollElement.isMultipleEntries()) {
                                log.info("Element is Non Recurring with Multiple Entries");
                                EHCMEarnDeductLne calLine = new EHCMEarnDeductLne();
                                calLine.setCalculatedValue(totalEmploymentValue);
                                calcLineList.add(calLine);
                                log.info("Adding new entry to List " + totalEmploymentValue);
                                log.info("List Size " + calcLineList.size());
                                elementValueMap.put(payrollElement.getId(), calcLineList);
                              } else {
                                log.info("Element is Non Multiple Entries");
                                EHCMEarnDeductLne calLine = calcLineList.get(0);
                                BigDecimal elementValue = calLine.getCalculatedValue();
                                calLine.setCalculatedValue(elementValue.add(totalEmploymentValue));
                                calcLineList.set(0, calLine);
                                log.info(
                                    "Sum Up Value is " + elementValue.add(totalEmploymentValue));
                                log.info("List Size " + calcLineList.size());
                                elementValueMap.put(payrollElement.getId(), calcLineList);
                              }
                            } else {
                              List<EHCMEarnDeductLne> calcLineList = new ArrayList<EHCMEarnDeductLne>();
                              EHCMEarnDeductLne calLine = new EHCMEarnDeductLne();
                              calLine.setCalculatedValue(totalEmploymentValue);
                              calcLineList.add(calLine);
                              elementValueMap.put(payrollElement.getId(), calcLineList);
                            }
                          } else {
                            break;
                          }
                        } else {
                          break;
                        }
                      }

                      // Remove employment based pre-defined elements
                      removeEmploymentBasedPreDefElements();

                    } else if (payrollElement.getBaseProcess().equalsIgnoreCase("BM")) {
                      log.info("Getting Business Mission for Employee: " + empPerInfo.getName()
                          + " Business Mission Element : " + payrollElement.getName());

                      // Fetching employments for an employee
                      List<EHCMEmpBusinessMission> businessMissionList = getBusinessMissionForEmployee(
                          empPerInfo, periodStartDate, periodEndDate);

                      for (EHCMEmpBusinessMission businessMission : businessMissionList) {
                        if (!errorFlag) {
                          // Checking element eligible for the business mission details
                          EhcmPosition position = businessMission.getPosition();
                          Organization department = businessMission.getPosition().getDepartment();
                          Jobs job = businessMission.getPosition().getEhcmJobs();
                          ehcmgrade positionGrade = businessMission.getPosition().getGrade();
                          ehcmgradeclass gradeClass = businessMission.getPosition().getGrade()
                              .getEhcmGradeclass();
                          ehcmgrade grade = businessMission.getEmploymentGrade();

                          boolean isEligible = checkElementEligibleForEmployment(payrollElement,
                              position, department, job, positionGrade, gradeClass);

                          if (isEligible) {
                            log.info(
                                "Element " + payrollElement.getName() + " is Eligible for Grade "
                                    + businessMission.getPosition().getGrade().getSearchKey());

                            // Converting business mission dates to proper date format
                            Date bmStartDate = dateFormat
                                .parse(businessMission.getStartDate().toString());
                            Date bmEndDate = dateFormat
                                .parse(businessMission.getEndDate().toString());

                            // Generate Business Mission Based Pre-Defined Elements
                            generateBMBasedPreDefElements(businessMission);

                            if (!errorFlag) {
                              // Generate Element Based Pre Defined Element
                              generateElementBasedPreDefElements(payrollElement, grade, bmStartDate,
                                  bmEndDate, processingDays, BigDecimal.ZERO, endDate);

                              if (!errorFlag) {
                                // Calculating element value by applying formula
                                BigDecimal elementCalculatedValue = calculateElementValue(
                                    payrollElement, grade, bmStartDate, bmEndDate, processingDays,
                                    BigDecimal.ZERO, endDate);

                                if (!errorFlag) {
                                  if (elementValueMap.containsKey(payrollElement.getId())) {
                                    // Sum up similar element values
                                    List<EHCMEarnDeductLne> calcLineList = elementValueMap
                                        .get(payrollElement.getId());

                                    // If Multiple Entries Allows do not sum
                                    if (payrollElement.getType().equalsIgnoreCase("NREC")
                                        && payrollElement.isMultipleEntries()) {
                                      log.info("Element is Non Recurring with Multiple Entries");
                                      EHCMEarnDeductLne calLine = new EHCMEarnDeductLne();
                                      calLine.setCalculatedValue(elementCalculatedValue);
                                      calLine.setBusinessMission(businessMission);
                                      calcLineList.add(calLine);
                                      log.info(
                                          "Adding new entry to List " + elementCalculatedValue);
                                      log.info("List Size " + calcLineList.size());
                                      elementValueMap.put(payrollElement.getId(), calcLineList);
                                    } else {
                                      log.info("Element is Non Multiple Entries");
                                      EHCMEarnDeductLne calLine = calcLineList.get(0);
                                      BigDecimal elementValue = calLine.getCalculatedValue();
                                      calLine.setCalculatedValue(
                                          elementValue.add(elementCalculatedValue));
                                      calLine.setBusinessMission(null);
                                      calcLineList.set(0, calLine);
                                      log.info("Sum Up Value is "
                                          + elementValue.add(elementCalculatedValue));
                                      log.info("List Size " + calcLineList.size());
                                      elementValueMap.put(payrollElement.getId(), calcLineList);
                                    }
                                  } else {
                                    List<EHCMEarnDeductLne> calcLineList = new ArrayList<EHCMEarnDeductLne>();
                                    EHCMEarnDeductLne calLine = new EHCMEarnDeductLne();
                                    calLine.setCalculatedValue(elementCalculatedValue);
                                    calLine.setBusinessMission(businessMission);
                                    calcLineList.add(calLine);
                                    elementValueMap.put(payrollElement.getId(), calcLineList);
                                  }
                                }
                              }
                            }

                          } else {
                            log.info("Element " + payrollElement.getName()
                                + " is NOT Eligible for Grade "
                                + businessMission.getPosition().getGrade().getSearchKey());

                            if (!elementValueMap.containsKey(payrollElement.getId())) {
                              List<EHCMEarnDeductLne> calcLineList = new ArrayList<EHCMEarnDeductLne>();
                              EHCMEarnDeductLne calLine = new EHCMEarnDeductLne();
                              calLine.setCalculatedValue(BigDecimal.ZERO);
                              calcLineList.add(calLine);
                              elementValueMap.put(payrollElement.getId(), calcLineList);
                            }
                          }
                        } else {
                          break;
                        }
                      }

                      // Remove Business Mission based pre-defined elements
                      removeBMBasedPreDefElements();
                    } else if (payrollElement.getBaseProcess().equalsIgnoreCase("AD")) {
                      log.info("Getting Allowance Decision for Employee: " + empPerInfo.getName()
                          + " Allowance Element : " + payrollElement.getName());

                      // Fetching allowance decision for an employee and element
                      List<EHCMBenefitAllowance> allowanceDecisionList = getAllowanceDecisionForEmployee(
                          empPerInfo, payrollElement, periodStartDate, periodEndDate);

                      for (EHCMBenefitAllowance allowancDecision : allowanceDecisionList) {
                        if (!errorFlag) {

                          // Converting Allowance dates to proper date format
                          Date allowStartDate = dateFormat
                              .parse(allowancDecision.getStartDate().toString());
                          Date allowEndDate = allowancDecision.getEndDate() != null
                              ? dateFormat.parse(allowancDecision.getEndDate().toString())
                              : null;

                          // Calculating applicable days in Allowance according to payrollDate
                          JSONObject allowancePeriodJSON = getOverlapingDateRange(allowStartDate,
                              allowEndDate, startDate, endDate);

                          if (allowancePeriodJSON != null) {
                            Date allowanceStartDate = dateFormat
                                .parse(allowancePeriodJSON.getString("startDate"));
                            Date allowanceEndDate = dateFormat
                                .parse(allowancePeriodJSON.getString("endDate"));

                            log.info("Applicable Dates for Allowance "
                                + allowancDecision.getDecisionNo());
                            log.info(allowancePeriodJSON.getString("startDate") + " to "
                                + allowancePeriodJSON.getString("endDate"));
                            log.info("No of days : " + allowancePeriodJSON.getString("days"));

                            // Fetching employments for an employee in allowance period
                            BigDecimal totalAllowanceValue = BigDecimal.ZERO;
                            String allowStartDateSql = sa.elm.ob.utility.util.Utility
                                .formatDate(allowanceStartDate);
                            String allowEndDateSql = sa.elm.ob.utility.util.Utility
                                .formatDate(allowanceEndDate);

                            List<EmploymentInfo> employmentList = getEmploymentsOfEmployee(
                                empPerInfo, allowStartDateSql, allowEndDateSql);

                            for (EmploymentInfo employment : employmentList) {
                              if (!errorFlag) {
                                // Checking element eligible for the employment
                                EhcmPosition position = employment.getPosition();
                                Organization department = employment.getPosition().getDepartment();
                                Jobs job = employment.getPosition().getEhcmJobs();
                                ehcmgrade positionGrade = employment.getPosition().getGrade();
                                ehcmgradeclass gradeClass = employment.getPosition().getGrade()
                                    .getEhcmGradeclass();
                                ehcmgrade grade = employment.getEmploymentgrade();

                                boolean isEligible = checkElementEligibleForEmployment(
                                    payrollElement, position, department, job, positionGrade,
                                    gradeClass);

                                if (isEligible) {
                                  log.info("Element " + payrollElement.getName()
                                      + " is Eligible for Employment Grade "
                                      + employment.getEmploymentgrade().getSearchKey());

                                  // Converting Employment dates to proper date format
                                  Date employStartDate = dateFormat
                                      .parse(employment.getStartDate().toString());
                                  Date employEndDate = employment.getEndDate() != null
                                      ? dateFormat.parse(employment.getEndDate().toString())
                                      : null;

                                  // Calculating applicable days in Employment according to
                                  // Allowance
                                  JSONObject employmentPeriodJSON = getOverlapingDateRange(
                                      employStartDate, employEndDate, allowanceStartDate,
                                      allowanceEndDate);

                                  if (employmentPeriodJSON != null) {
                                    Date employmentStartDate = dateFormat
                                        .parse(employmentPeriodJSON.getString("startDate"));
                                    Date employmentEndDate = dateFormat
                                        .parse(employmentPeriodJSON.getString("endDate"));

                                    log.info("Applicable Dates for Employment in Allowance "
                                        + allowancDecision.getDecisionNo());
                                    log.info(employmentPeriodJSON.getString("startDate") + " to "
                                        + employmentPeriodJSON.getString("endDate"));
                                    log.info(
                                        "No of days : " + employmentPeriodJSON.getString("days"));

                                    // Calculate allowance value
                                    BigDecimal days = new BigDecimal(
                                        employmentPeriodJSON.getLong("days"));
                                    BigDecimal value = BigDecimal.ZERO;
                                    if (allowancDecision.getValueType()
                                        .equalsIgnoreCase(allowanceFixed)) {
                                      BigDecimal perDayFixedAmt = allowancDecision.getFixedAmount()
                                          .divide(processingDays, 6, BigDecimal.ROUND_HALF_UP);
                                      value = perDayFixedAmt.multiply(days);
                                      log.info("Fixed Value after for days " + value);
                                    } else if (allowancDecision.getValueType()
                                        .equalsIgnoreCase(allowancePercent)) {
                                      BigDecimal percent = allowancDecision.getPercentage()
                                          .divide(percentMax, 6, BigDecimal.ROUND_HALF_UP);

                                      if (allowancDecision.getCategory()
                                          .equalsIgnoreCase(percentBasic)) {
                                        BigDecimal payscale = getPayScaleValue(employment,
                                            employmentStartDate, employmentEndDate, processingDays,
                                            differenceDays, endDate);
                                        value = payscale.multiply(percent);
                                        log.info("Basic Value after applying percentage " + value);
                                      } else if (allowancDecision.getCategory()
                                          .equalsIgnoreCase(percentFistStepGrade)) {
                                        BigDecimal firstStepGradeVal = getFirstStepGradeValue(
                                            employment, employmentStartDate, employmentEndDate,
                                            processingDays, differenceDays, endDate);
                                        value = firstStepGradeVal.multiply(percent);
                                        log.info(
                                            "First Step Value after applying percentage " + value);
                                      }
                                    }
                                    totalAllowanceValue = totalAllowanceValue.add(value);
                                    log.info("Total Allowance Value " + totalAllowanceValue);
                                  } else {
                                    break;
                                  }
                                }

                              } else {
                                break;
                              }
                            }

                            if (!errorFlag) {
                              if (elementValueMap.containsKey(payrollElement.getId())) {
                                // Sum up similar element values
                                List<EHCMEarnDeductLne> calcLineList = elementValueMap
                                    .get(payrollElement.getId());

                                // If Multiple Entries Allows do not sum
                                if (payrollElement.getType().equalsIgnoreCase("NREC")
                                    && payrollElement.isMultipleEntries()) {
                                  log.info("Element is Non Recurring with Multiple Entries");
                                  EHCMEarnDeductLne calLine = new EHCMEarnDeductLne();
                                  calLine.setCalculatedValue(totalAllowanceValue);
                                  calcLineList.add(calLine);
                                  log.info("Adding new entry to List " + totalAllowanceValue);
                                  log.info("List Size " + calcLineList.size());
                                  elementValueMap.put(payrollElement.getId(), calcLineList);
                                } else {
                                  log.info("Element is Non Multiple Entries");
                                  EHCMEarnDeductLne calLine = calcLineList.get(0);
                                  BigDecimal elementValue = calLine.getCalculatedValue();
                                  calLine.setCalculatedValue(elementValue.add(totalAllowanceValue));
                                  calcLineList.set(0, calLine);
                                  log.info(
                                      "Sum Up Value is " + elementValue.add(totalAllowanceValue));
                                  log.info("List Size " + calcLineList.size());
                                  elementValueMap.put(payrollElement.getId(), calcLineList);
                                }
                              } else {
                                List<EHCMEarnDeductLne> calcLineList = new ArrayList<EHCMEarnDeductLne>();
                                EHCMEarnDeductLne calLine = new EHCMEarnDeductLne();
                                calLine.setCalculatedValue(totalAllowanceValue);
                                calcLineList.add(calLine);
                                elementValueMap.put(payrollElement.getId(), calcLineList);
                              }
                            }
                          } else {
                            break;
                          }

                        } else {
                          break;
                        }
                      }
                    }
                  } else {
                    break;
                  }
                }

                if (!errorFlag && !elementValueMap.isEmpty()) {
                  log.info(
                      "Inserting into Earning and Deduction for Employee: " + empPerInfo.getName());

                  EmploymentInfo employmnt = getLatestEmploymentInPayPeriod(empPerInfo,
                      periodStartDate, periodEndDate);

                  if (!errorFlag) {
                    // Insert into earning and deduction
                    createEarningAndDeduction(empPerInfo, employmnt, payrollDef, PayrollDefPeriod,
                        elementValueMap, clientId, orgId, userId);
                  }

                  // Remove calculated element like BASIC
                }

              } else {
                break;
              }
            }
          }
        }
      }

      if (errorFlag) {
        OBDal.getInstance().rollbackAndClose();
        obError.setType("Error");
        obError.setTitle("Error");
        if (!StringUtils.isEmpty(errorMessage)) {
          obError.setMessage(errorMessage);
        } else {
          obError.setMessage(OBMessageUtils.messageBD("EHCM_PayrollProcess_Failed"));
        }
      } else {
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_PayrollProcess_Success"));
      }
      bundle.setResult(obError);
    } catch (

    Exception e) {
      OBDal.getInstance().rollbackAndClose();
      e.printStackTrace();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private BigDecimal calculateEmploymentBasedValue(EHCMElmttypeDef payrollElement,
      EmploymentInfo employment, Date startDate, Date endDate, BigDecimal processingDays,
      BigDecimal differenceDays, Date payrollEndDate) {
    BigDecimal elementCalculatedValue = BigDecimal.ZERO;
    try {
      // Generate Employment Based Pre Defined Elements
      generateEmploymentBasedPreDefElements(employment, startDate, endDate, processingDays,
          differenceDays, payrollEndDate);

      if (!errorFlag) {
        // Generate Element Based Pre Defined Element
        generateElementBasedPreDefElements(payrollElement, employment.getEmploymentgrade(),
            startDate, endDate, processingDays, differenceDays, payrollEndDate);

        if (!errorFlag) {
          // Calculating element value by applying formula
          elementCalculatedValue = calculateElementValue(payrollElement,
              employment.getEmploymentgrade(), startDate, endDate, processingDays, differenceDays,
              payrollEndDate);
        }
      }

      return elementCalculatedValue;
    } catch (Exception cebv) {
      log.error("Error in PayrollProcess.java : calculateEmploymentBasedValue() ");
      cebv.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while calculating Employment Based Value";
      return elementCalculatedValue;
    }
  }

  private void getGlobalPreDefinedElements(String clientId) {
    try {
      // Global Values
      List<EhcmPayrollGlobalValue> globalValueList = getGlobalValues(clientId);
      for (EhcmPayrollGlobalValue globalValue : globalValueList) {
        if (globalValue.getType().equalsIgnoreCase("C")) {
          payRollComponents.put(globalValue.getCode(), "'" + globalValue.getCharValue() + "'");
          log.info("Assigned Global Code - Value : " + globalValue.getCode() + " - " + "'"
              + globalValue.getCharValue() + "'");
        } else if (globalValue.getType().equalsIgnoreCase("D")) {
          payRollComponents.put(globalValue.getCode(),
              "new Date('" + globalValue.getDateValue() + "')");
          log.info("Assigned Global Code - Value : " + globalValue.getCode() + " - "
              + globalValue.getDateValue());
        } else {
          payRollComponents.put(globalValue.getCode(), globalValue.getNumericValue());
          log.info("Assigned Global Code - Value : " + globalValue.getCode() + " - "
              + globalValue.getNumericValue());
        }
      }
    } catch (JSONException gge) {
      log.error("Error in PayrollProcess.java : getGlobalPreDefinedElements() ");
      gge.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while getting Global Pre Defined Elements For Payroll";
    }
  }

  public List<EhcmPayrollGlobalValue> getGlobalValues(String clientId) {
    List<EhcmPayrollGlobalValue> globalValueList = new ArrayList<EhcmPayrollGlobalValue>();
    try {
      String whereClause = "e where e.client.id=:clientId)";

      OBQuery<EhcmPayrollGlobalValue> globalValueQry = OBDal.getInstance()
          .createQuery(EhcmPayrollGlobalValue.class, whereClause);
      globalValueQry.setNamedParameter("clientId", clientId);
      globalValueList = globalValueQry.list();
      return globalValueList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getGlobalValues() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while getting Global Values";
      return globalValueList;
    }
  }

  public List<EHCMElmttypeDef> getAvailablePayrollElementsInClient(String clientId) {
    List<EHCMElmttypeDef> elementList = new ArrayList<EHCMElmttypeDef>();
    try {
      String whereClause = "e where e.client.id=:clientId and e.active='Y' order by e.priority)";

      OBQuery<EHCMElmttypeDef> elementQry = OBDal.getInstance().createQuery(EHCMElmttypeDef.class,
          whereClause);
      elementQry.setNamedParameter("clientId", clientId);
      elementList = elementQry.list();
      return elementList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getAvailablePayrollElementsInClient() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while getting available payroll elements in client";
      return elementList;
    }
  }

  public static JSONObject getOverlapingDateRange(Date range1StartDate, Date range1EndDate,
      Date range2StartDate, Date range2endDate) {
    log.info("Calculating no of days applicable in both date ranges");
    log.info("Range 1 :" + range1StartDate + "to" + range1EndDate);
    log.info("Range 2 :" + range2StartDate + "to" + range2endDate);

    try {
      JSONObject result = new JSONObject();

      Date startdate1 = range1StartDate;
      Date enddate1 = range1EndDate != null ? range1EndDate : range2endDate;

      Date overlappingStartDate = getApplicableStartDate(startdate1, range2StartDate);
      Date overlappingEndDate = getApplicableEndDate(enddate1, range2endDate);

      long diffMillSeconds = Math
          .abs(overlappingStartDate.getTime() - overlappingEndDate.getTime());
      long overlappingDays = (diffMillSeconds / (hours * minutes * seconds * milliseconds)) + 1;

      result.put("startDate", dateFormat.format(overlappingStartDate));
      result.put("endDate", dateFormat.format(overlappingEndDate));
      result.put("days", overlappingDays);
      return result;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getOverlapingDateRange() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while calculating number of days in 2 ranges ";
      return null;
    }
  }

  public static Date getApplicableStartDate(Date empStartDate, Date payRollStartDate) {
    Date payEmpStartDate = null;
    if (payRollStartDate.compareTo(empStartDate) >= 0) {
      payEmpStartDate = payRollStartDate;
    } else {
      payEmpStartDate = empStartDate;
    }
    return payEmpStartDate;
  }

  public static Date getApplicableEndDate(Date empEndDate, Date payRollEndDate) {
    Date payEmpEndDate = null;
    if (payRollEndDate.compareTo(empEndDate) <= 0) {
      payEmpEndDate = payRollEndDate;
    } else {
      payEmpEndDate = empEndDate;
    }
    return payEmpEndDate;
  }

  private void generateEmployeeBasedPreDefElements(EhcmEmpPerInfo empPerInfo) {
    try {
      // Employee Country
      payRollComponents.put("EMP_COUNTRY", "'" + empPerInfo.getCountry().getISOCountryCode() + "'");

      // Employee Category
      payRollComponents.put("EMP_CATEGORY", "'" + empPerInfo.getGradeClass().getSearchKey() + "'");
    } catch (JSONException gce) {
      log.error("Error in PayrollProcess.java : generateEmployeeBasedPreDefElements() ");
      gce.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while generating Employee Based Pre Defined Element for Employee"
          + empPerInfo.getName();
    }
  }

  private void generateEmploymentBasedPreDefElements(EmploymentInfo employment, Date startDate,
      Date endDate, BigDecimal processingDays, BigDecimal differenceDays, Date payrollEndDate) {
    try {
      // Employee Grade
      payRollComponents.put("EMP_GRADE",
          "'" + employment.getEmploymentgrade().getSearchKey() + "'");

      // Pay Scale
      BigDecimal payscale = getPayScaleValue(employment, startDate, endDate, processingDays,
          differenceDays, payrollEndDate);

      payRollComponents.put("PAYSALGS", payscale);
    } catch (JSONException gce) {
      log.error("Error in PayrollProcess.java : generateEmploymentBasedPreDefElements() ");
      gce.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while generating Employment Based Pre Defined Element for employment Grade"
          + employment.getEmploymentgrade().getSearchKey();
    }
  }

  private void removeEmploymentBasedPreDefElements() {
    try {
      // Employee Grade
      payRollComponents.remove("EMP_GRADE");
      // Pay Scale
      payRollComponents.remove("PAYSALGS");
    } catch (Exception rebpe) {
      log.error("Error in PayrollProcess.java : removeEmploymentBasedPreDefElements() ");
      rebpe.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while removing Employment Based Pre Defined Element";
    }
  }

  public static BigDecimal getPayScaleValue(EmploymentInfo empInfo, Date startDate, Date endDate,
      BigDecimal processingDays, BigDecimal differenceDays, Date payrollEndDate) {
    BigDecimal totalPayScaleValue = BigDecimal.ZERO;
    try {
      log.info("Generating PayScale Value for Employment : "
          + empInfo.getEmploymentgrade().getSearchKey());
      String empmtPayStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String empmtPayEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      String whereClause = "e where e.ehcmPayscale.ehcmGrade.id=:gradeId "
          + "and e.ehcmProgressionpt.id = :pointsId and e.enabled = 'Y' "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) ";

      OBQuery<ehcmpayscaleline> payScaleQry = OBDal.getInstance()
          .createQuery(ehcmpayscaleline.class, whereClause);
      payScaleQry.setNamedParameter("gradeId", empInfo.getEmploymentgrade().getId());
      payScaleQry.setNamedParameter("pointsId",
          empInfo.getEhcmPayscaleline().getEhcmProgressionpt().getId());
      payScaleQry.setNamedParameter("startDate", empmtPayStartDate);
      payScaleQry.setNamedParameter("endDate", empmtPayEndDate);

      List<ehcmpayscaleline> payScaleLneList = payScaleQry.list();
      for (ehcmpayscaleline payScaleLne : payScaleLneList) {
        if (!errorFlag) {
          JSONObject payScalePeriodJSON = getOverlapingDateRange(payScaleLne.getStartDate(),
              payScaleLne.getEndDate(), startDate, endDate);

          if (payScalePeriodJSON != null) {
            log.info("Applicable Dates for Payscale Point "
                + payScaleLne.getEhcmProgressionpt().getPoint());
            log.info(payScalePeriodJSON.getString("startDate") + " to "
                + payScalePeriodJSON.getString("endDate"));
            log.info("No of days : " + payScalePeriodJSON.getString("days"));

            BigDecimal days = new BigDecimal(payScalePeriodJSON.getLong("days"));

            // If pay scale is for payroll end date, Check and add extra days in month
            Date payScaleEndDate = dateFormat.parse(payScalePeriodJSON.getString("endDate"));
            if (differenceDays.compareTo(BigDecimal.ZERO) > 0
                && payScaleEndDate.compareTo(payrollEndDate) == 0) {
              log.info("Adding Additional Days for Payscale for Employment "
                  + empInfo.getEmploymentgrade().getCommercialName() + " and End Day "
                  + payrollEndDate);
              log.info("Days Before " + days);
              days = days.add(differenceDays);
              log.info("Days After " + days);
            }

            BigDecimal payScaleAmount = payScaleLne.getAmount();
            log.info("Pay Scale : " + payScaleAmount);
            BigDecimal perDayPayScale = payScaleAmount.divide(processingDays, 6,
                BigDecimal.ROUND_HALF_UP);
            log.info("perDayPayScale : " + perDayPayScale);
            BigDecimal payScaleCalDays = perDayPayScale.multiply(days).setScale(6,
                BigDecimal.ROUND_HALF_UP);
            totalPayScaleValue = totalPayScaleValue.add(payScaleCalDays);

            log.info("Pay Scale value is : " + payScaleCalDays);
          }
        } else {
          break;
        }
      }

      // Validate Month Days

      if (!errorFlag) {
        log.info("Total Pay Scale value is : " + totalPayScaleValue);
        return totalPayScaleValue;
      } else {
        log.info("Total Pay Scale value is : " + 0);
        return BigDecimal.ZERO;
      }
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getPayScaleValue() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while calculating Pay Scale value for Employment Grade "
          + empInfo.getEmploymentgrade().getSearchKey();
      return BigDecimal.ZERO;
    }
  }

  public static BigDecimal getFirstStepGradeValue(EmploymentInfo empInfo, Date startDate,
      Date endDate, BigDecimal processingDays, BigDecimal differenceDays, Date payrollEndDate) {
    BigDecimal totalPayScaleValue = BigDecimal.ZERO;
    try {
      log.info("Generating First Step Value for Employment : "
          + empInfo.getEmploymentgrade().getSearchKey());

      // Get First Step of Grade
      ehcmprogressionpoint firstStepOfGrade = getFirstStepGrade(empInfo);

      if (firstStepOfGrade != null) {
        String empmtPayStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
        String empmtPayEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

        String whereClause = "e where e.ehcmPayscale.ehcmGrade.id=:gradeId "
            + "and e.ehcmProgressionpt.id = :pointsId and e.enabled = 'Y' "
            + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
            + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
            + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
            + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) ";

        OBQuery<ehcmpayscaleline> payScaleQry = OBDal.getInstance()
            .createQuery(ehcmpayscaleline.class, whereClause);
        payScaleQry.setNamedParameter("gradeId", empInfo.getEmploymentgrade().getId());
        payScaleQry.setNamedParameter("pointsId", firstStepOfGrade.getId());
        payScaleQry.setNamedParameter("startDate", empmtPayStartDate);
        payScaleQry.setNamedParameter("endDate", empmtPayEndDate);

        List<ehcmpayscaleline> payScaleLneList = payScaleQry.list();
        for (ehcmpayscaleline payScaleLne : payScaleLneList) {
          if (!errorFlag) {
            JSONObject payScalePeriodJSON = getOverlapingDateRange(payScaleLne.getStartDate(),
                payScaleLne.getEndDate(), startDate, endDate);

            if (payScalePeriodJSON != null) {
              log.info("Applicable Dates for Payscale Point "
                  + payScaleLne.getEhcmProgressionpt().getPoint());
              log.info(payScalePeriodJSON.getString("startDate") + " to "
                  + payScalePeriodJSON.getString("endDate"));
              log.info("No of days : " + payScalePeriodJSON.getString("days"));

              BigDecimal days = new BigDecimal(payScalePeriodJSON.getLong("days"));

              // If pay scale is for payroll end date, Check and add extra days in month
              Date payScaleEndDate = dateFormat.parse(payScalePeriodJSON.getString("endDate"));
              if (differenceDays.compareTo(BigDecimal.ZERO) > 0
                  && payScaleEndDate.compareTo(payrollEndDate) == 0) {
                log.info("Adding Additional Days for FirstStep Grade for Employment "
                    + empInfo.getEmploymentgrade().getCommercialName() + " and End Day "
                    + payrollEndDate);
                log.info("Days Before " + days);
                days = days.add(differenceDays);
                log.info("Days After " + days);
              }

              BigDecimal payScaleAmount = payScaleLne.getAmount();
              log.info("Pay Scale : " + payScaleAmount);
              BigDecimal perDayPayScale = payScaleAmount.divide(processingDays, 6,
                  BigDecimal.ROUND_HALF_UP);
              log.info("perDayPayScale : " + perDayPayScale);
              BigDecimal payScaleCalDays = perDayPayScale.multiply(days).setScale(6,
                  BigDecimal.ROUND_HALF_UP);
              totalPayScaleValue = totalPayScaleValue.add(payScaleCalDays);

              log.info("Pay Scale value is : " + payScaleCalDays);
            }
          } else {
            break;
          }
        }

        if (!errorFlag) {
          log.info("Total First Step Grade value is : " + totalPayScaleValue);
          return totalPayScaleValue;
        } else {
          log.info("Total First Step Grade value is : " + 0);
          return BigDecimal.ZERO;
        }
      } else {
        log.info("Total First Step Grade value is : " + 0);
        return BigDecimal.ZERO;
      }
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getFirstStepGradeValue() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while calculating First Step Grade value for Employment Grade "
          + empInfo.getEmploymentgrade().getSearchKey();
      return BigDecimal.ZERO;
    }
  }

  public static ehcmprogressionpoint getFirstStepGrade(EmploymentInfo empInfo) {
    ehcmprogressionpoint progPt = null;
    try {
      log.info("Getting First Step Grade for Employment : "
          + empInfo.getEmploymentgrade().getSearchKey());

      String whereClause = "e where e.ehcmGrade.id=:gradeId ";

      // Get Grade Steps
      OBQuery<ehcmpayscale> payScaleQry = OBDal.getInstance().createQuery(ehcmpayscale.class,
          whereClause);
      payScaleQry.setNamedParameter("gradeId", empInfo.getEmploymentgrade().getId());
      payScaleQry.setMaxResult(1);

      List<ehcmpayscale> payScaleList = payScaleQry.list();

      if (payScaleList.size() > 0) {
        ehcmpayscale payScale = payScaleList.get(0);
        ehcmgradesteps gradeSteps = payScale.getEhcmGradesteps();

        log.info("Grade Step of grade is : " + gradeSteps.getName());

        whereClause = "e where ehcmGradesteps.id = :gradeStepId order by seq ";

        // Get Grade Steps
        OBQuery<ehcmprogressionpoint> progPtQry = OBDal.getInstance()
            .createQuery(ehcmprogressionpoint.class, whereClause);
        progPtQry.setNamedParameter("gradeStepId", gradeSteps.getId());
        progPtQry.setMaxResult(1);

        List<ehcmprogressionpoint> progPtList = progPtQry.list();

        if (payScaleList.size() > 0) {
          progPt = progPtList.get(0);
        }
      }

      return progPt;

    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getFirstStepGrade() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while getting First Step Grade for Employment Grade "
          + empInfo.getEmploymentgrade().getSearchKey();
      return progPt;
    }
  }

  private void generateBMBasedPreDefElements(EHCMEmpBusinessMission bMission) {
    try {

      // Business Mission Type
      payRollComponents.put("BM_TYPE", "'" + bMission.getMissionType().getSearchKey() + "'");

      // Business Mission House Provided
      payRollComponents.put("BM_HOUSING", "'" + (bMission.isHousingProvided() ? "Y" : "N") + "'");

      // Business Mission Food Provided
      payRollComponents.put("BM_FOOD", "'" + (bMission.isFoodProvided() ? "Y" : "N") + "'");

      // Business Mission Country Category (CA, CB, CC)
      if (bMission.getToCity().getEhcmCategory() != null) {
        payRollComponents.put("BM_COUNTRYCAT", "'" + bMission.getToCity().getEhcmCategory() + "'");
      } else {
        errorFlag = true;
        errorMessage = "Category is not configured for city " + bMission.getToCity().getName();
      }

      // Business Mission Days
      payRollComponents.put("BM_Days", bMission.getMissionDays());

      // Business Mission Advance
      payRollComponents.put("BM_Advance", bMission.getNoofdaysBefore());

      // Business Mission Days After
      payRollComponents.put("BM_AfterDays", bMission.getNoofdaysAfter());

    } catch (JSONException gce) {
      log.error("Error in PayrollProcess.java : generateBMBasedPreDefElements() ");
      gce.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while generating BM Based Pre Defined Element";
    }
  }

  private void removeBMBasedPreDefElements() {
    try {

      // Business Mission Type
      payRollComponents.remove("BM_TYPE");

      // Business Mission House Provided
      payRollComponents.remove("BM_HOUSING");

      // Business Mission Food Provided
      payRollComponents.remove("BM_FOOD");

      // Business Mission Country Category (CA, CB, CC)
      payRollComponents.remove("BM_COUNTRYCAT");

      // Business Mission Days
      payRollComponents.remove("BM_Days");

      // Business Mission Advance
      payRollComponents.remove("BM_Advance");

      // Business Mission Days After
      payRollComponents.remove("BM_AfterDays");

    } catch (Exception rbme) {
      log.error("Error in PayrollProcess.java : removeBMBasedPreDefElements() ");
      rbme.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while removing BM Based Pre-Defined Element";
    }
  }

  public String applyPredefinedValues(String formula) {
    String appliedFormula = formula;
    try {
      java.util.Iterator<?> componentCodes = payRollComponents.keys();
      while (componentCodes.hasNext()) {
        String code = (String) componentCodes.next();
        appliedFormula = appliedFormula.replace(code, payRollComponents.getString(code));
      }
      return appliedFormula;
    } catch (JSONException e) {
      log.error("Error while in PayrollProcess.java : applyPredefinedValues() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while applying predefined values";
      return "";
    }
  }

  public List<EhcmEmpPerInfo> getEmployeesFromPayroll(EHCMPayrollDefinition payroll,
      String periodStartDate, String periodEndDate, Session session) {
    try {
      // Note should not check active flag because active flag set to false after new employment
      String hqlString = " select distinct e.ehcmEmpPerinfo from Ehcm_Employment_Info e where e.ehcmPayrollDefinition.id = :payDefId and "
          + "((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')))";

      final Query empQuery = session.createQuery(hqlString);
      empQuery.setParameter("payDefId", payroll.getId());
      empQuery.setParameter("fromdate", periodStartDate);
      empQuery.setParameter("todate", periodEndDate);

      @SuppressWarnings("unchecked")
      List<EhcmEmpPerInfo> empList = empQuery.list();
      return empList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getEmployeesFromPayroll() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while getting employments in Payroll : " + payroll.getPayrollName();
      return null;
    }
  }

  public List<EmploymentInfo> getEmploymentsOfEmployee(EhcmEmpPerInfo empPerInfo,
      String periodStartDate, String periodEndDate) {
    List<EmploymentInfo> employmentList = new ArrayList<EmploymentInfo>();
    try {
      // Note should not check active flag because active flag set to false after new employment
      String whereClause = " e where e.ehcmEmpPerinfo.id = :empPerInfo and "
          + "e.changereason in ('H', 'PR', 'PRT', 'OD',  'ID', 'ES', 'EOS') and "
          + "((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) "
          + "order by e.creationDate ";

      OBQuery<EmploymentInfo> employmentQry = OBDal.getInstance().createQuery(EmploymentInfo.class,
          whereClause);
      employmentQry.setNamedParameter("empPerInfo", empPerInfo.getId());
      employmentQry.setNamedParameter("fromdate", periodStartDate);
      employmentQry.setNamedParameter("todate", periodEndDate);
      employmentList = employmentQry.list();
      return employmentList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getEmploymentsOfEmployee() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while getting employments of Employee " + empPerInfo.getName();
      return employmentList;
    }
  }

  public EmploymentInfo getLatestEmploymentInPayPeriod(EhcmEmpPerInfo empPerInfo,
      String periodStartDate, String periodEndDate) {
    try {
      // Note should not check active flag because active flag set to false after new employment
      String whereClause = " e where e.ehcmEmpPerinfo.id = :empPerInfo and "
          + "e.changereason in ('H', 'PR', 'PRT', 'OD',  'ID', 'ES', 'EOS') and "
          + "((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) "
          + "order by e.creationDate desc ";

      OBQuery<EmploymentInfo> employmentQry = OBDal.getInstance().createQuery(EmploymentInfo.class,
          whereClause);
      employmentQry.setNamedParameter("empPerInfo", empPerInfo.getId());
      employmentQry.setNamedParameter("fromdate", periodStartDate);
      employmentQry.setNamedParameter("todate", periodEndDate);
      employmentQry.setMaxResult(1);
      List<EmploymentInfo> employmentList = employmentQry.list();
      return employmentList.get(0);
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getLatestEmploymentInPayPeriod() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while getting Latest employment of Employee " + empPerInfo.getName();
      return null;
    }
  }

  public EmploymentInfo getActiveEmploymentsOfEmployee(EhcmEmpPerInfo empPerInfo) {
    EmploymentInfo latestEmployment = null;
    try {
      String whereClause = " e where e.ehcmEmpPerinfo.id = :empPerInfo and e.enabled = 'Y' order by e.creationDate desc";

      OBQuery<EmploymentInfo> employmentQry = OBDal.getInstance().createQuery(EmploymentInfo.class,
          whereClause);
      employmentQry.setNamedParameter("empPerInfo", empPerInfo.getId());
      employmentQry.setMaxResult(1);
      latestEmployment = employmentQry.list().get(0);
      return latestEmployment;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getActiveEmploymentsOfEmployee() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while getting Active employments of Employee " + empPerInfo.getName();
      return latestEmployment;
    }
  }

  @SuppressWarnings("unchecked")
  public List<EHCMElementECriteria> getEligibleElementsInEmployment(EmploymentInfo employment,
      String clientId, Session session) {
    List<EHCMElementECriteria> eligibleElementList = new ArrayList<EHCMElementECriteria>();

    try {
      String payrollElementHqlQuery = " select distinct e.ehcmElementECriteria from EHCM_Eligblty_Criteria e "
          + "where e.client.id=:clientId and e.enabled = 'Y' and (e.position is null or e.position.id=:positionId) "
          + "and (e.department is null or e.department.id=:departmentId) "
          + "and (e.ehcmJobs is null or e.ehcmJobs.id=:jobId) and (e.grade is null or e.grade.id=:gradeId) "
          + "and (e.gradeClassifications is null or e.gradeClassifications.id=:gradeClassId) ";
      // + "and (e.ehcmPayrollDefinition is null or e.ehcmPayrollDefinition.id=:payRollId)
      // Location and Payroll Validation missing

      final Query payrollElementQuery = session.createQuery(payrollElementHqlQuery);
      payrollElementQuery.setParameter("clientId", clientId);
      payrollElementQuery.setParameter("positionId", employment.getPosition().getId());
      payrollElementQuery.setParameter("departmentId",
          employment.getPosition().getDepartment().getId());
      payrollElementQuery.setParameter("jobId", employment.getPosition().getEhcmJobs().getId());
      payrollElementQuery.setParameter("gradeId", employment.getPosition().getGrade().getId());
      payrollElementQuery.setParameter("gradeClassId",
          employment.getPosition().getGrade().getEhcmGradeclass().getId());
      /*
       * payrollElementQuery.setParameter("payRollId",
       * employment.getEhcmPayrollDefinition().getId());
       */

      eligibleElementList = payrollElementQuery.list();
      return eligibleElementList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getEligibleElementsInEmployment() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while getting eligible elements of Employment "
          + employment.getEmploymentgrade().getSearchKey();
      return eligibleElementList;
    }
  }

  public boolean checkElementEligibleForEmployment(EHCMElmttypeDef element, EhcmPosition position,
      Organization department, Jobs job, ehcmgrade grade, ehcmgradeclass gradeClass) {
    try {
      // Payroll and Location Validation Missing
      String whereClause = " e where e.ehcmElementECriteria.code.id=:elementId and e.enabled = 'Y' "
          + "and (e.position is null or e.position.id=:positionId) "
          + "and (e.department is null or e.department.id=:departmentId) "
          + "and (e.ehcmJobs is null or e.ehcmJobs.id=:jobId) and (e.grade is null or e.grade.id=:gradeId) "
          + "and (e.gradeClassifications is null or e.gradeClassifications.id=:gradeClassId) ";

      OBQuery<EHCMEligbltyCriteria> elementEligiblityQry = OBDal.getInstance()
          .createQuery(EHCMEligbltyCriteria.class, whereClause);
      elementEligiblityQry.setNamedParameter("elementId", element.getId());
      elementEligiblityQry.setNamedParameter("positionId", position.getId());
      elementEligiblityQry.setNamedParameter("departmentId", department.getId());
      elementEligiblityQry.setNamedParameter("jobId", job.getId());
      elementEligiblityQry.setNamedParameter("gradeId", grade.getId());
      elementEligiblityQry.setNamedParameter("gradeClassId", gradeClass.getId());

      List<EHCMEligbltyCriteria> elementEligiblityList = elementEligiblityQry.list();

      if (elementEligiblityList.size() > 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception ccefe) {
      log.error("Error in PayrollProcess.java : checkElementEligibleForEmployment() ");
      ccefe.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while checking Element '" + element.getName()
          + "' eligibility for Employment '";
      return false;
    }
  }

  public List<EHCMEmpBusinessMission> getBusinessMissionForEmployee(EhcmEmpPerInfo empPerInfo,
      String periodStartDate, String periodEndDate) {
    List<EHCMEmpBusinessMission> businessMissionList = new ArrayList<EHCMEmpBusinessMission>();
    try {
      // Note should not check active flag because active flag set to false after new employment
      String whereClause = " e where e.employee.id = :employeeId and e.decisionType='CR' and e.issueDecision='Y' and "
          + "(to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') and "
          + "to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + "order by e.creationDate ";

      OBQuery<EHCMEmpBusinessMission> businessMissionQry = OBDal.getInstance()
          .createQuery(EHCMEmpBusinessMission.class, whereClause);
      businessMissionQry.setNamedParameter("employeeId", empPerInfo.getId());
      businessMissionQry.setNamedParameter("fromdate", periodStartDate);
      businessMissionQry.setNamedParameter("todate", periodEndDate);
      businessMissionList = businessMissionQry.list();
      return businessMissionList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getBusinessMissionForEmployee() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while getting Business Mission of Employee " + empPerInfo.getName();
      return businessMissionList;
    }
  }

  public List<EHCMBenefitAllowance> getAllowanceDecisionForEmployee(EhcmEmpPerInfo empPerInfo,
      EHCMElmttypeDef element, String periodStartDate, String periodEndDate) {
    List<EHCMBenefitAllowance> AllowanceList = new ArrayList<EHCMBenefitAllowance>();
    try {
      String whereClause = " e where e.employee.id = :employeeId and e.elementType.id = :elementId "
          + "and e.decisionType='CR' and e.issueDecision='Y' "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) "
          + "order by e.creationDate ";

      OBQuery<EHCMBenefitAllowance> allowanceQry = OBDal.getInstance()
          .createQuery(EHCMBenefitAllowance.class, whereClause);
      allowanceQry.setNamedParameter("employeeId", empPerInfo.getId());
      allowanceQry.setNamedParameter("elementId", element.getId());
      allowanceQry.setNamedParameter("startDate", periodStartDate);
      allowanceQry.setNamedParameter("endDate", periodEndDate);
      AllowanceList = allowanceQry.list();
      return AllowanceList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getAllowanceDecisionForEmployee() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while getting Allowance Decision of Employee " + empPerInfo.getName()
          + " and Element " + element.getName();
      return AllowanceList;
    }
  }

  private void generateElementBasedPreDefElements(EHCMElmttypeDef typDef, ehcmgrade grade,
      Date startDate, Date endDate, BigDecimal processingDays, BigDecimal differenceDays,
      Date payrollEndDate) {
    try {
      // Grade Rate
      if (typDef.getElementSource() != null && typDef.getElementSource().equalsIgnoreCase("GR")) {
        // Fetching grade rate value based on grade rate in element definition
        BigDecimal gradeRate = getGradeRateValue(typDef, typDef.getGradeRate(), grade, startDate,
            endDate, processingDays, differenceDays, payrollEndDate);
        payRollComponents.put("GRADERATE", gradeRate);
      }
    } catch (Exception gese) {
      log.error("Error in PayrollProcess.java : generateElementBasedPreDefElements() ");
      gese.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while generating Element Based Value for Pre Defined Element"
          + typDef.getName();
    }
  }

  public static BigDecimal getGradeRateValue(EHCMElmttypeDef elmTypDef, ehcmgraderates gradeRate,
      ehcmgrade grade, Date startDate, Date endDate, BigDecimal processingDays,
      BigDecimal differenceDays, Date payrollEndDate) {
    BigDecimal totalGradeRateValue = BigDecimal.ZERO;
    try {
      log.info("Generating Grade Rate Value for element : " + elmTypDef.getName());

      String dbFormattedStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String dbFormattedEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      String whereClause = "e where e.ehcmGraderates.id = :gradeRateId and e.grade.id=:gradeId and e.enabled = 'Y' "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')))";

      OBQuery<ehcmgraderatelines> gradeLineQry = OBDal.getInstance()
          .createQuery(ehcmgraderatelines.class, whereClause);
      gradeLineQry.setNamedParameter("gradeRateId", gradeRate.getId());
      gradeLineQry.setNamedParameter("gradeId", grade.getId());
      gradeLineQry.setNamedParameter("startDate", dbFormattedStartDate);
      gradeLineQry.setNamedParameter("endDate", dbFormattedEndDate);

      List<ehcmgraderatelines> gradeLineList = gradeLineQry.list();
      for (ehcmgraderatelines gradeRateLne : gradeLineList) {
        JSONObject gradeRatePeriodJSON = getOverlapingDateRange(gradeRateLne.getStartDate(),
            gradeRateLne.getEndDate(), startDate, endDate);

        if (gradeRatePeriodJSON != null) {
          log.info("Applicable Dates for Grade Rate for Element " + elmTypDef.getName());
          log.info(gradeRatePeriodJSON.getString("startDate") + " to "
              + gradeRatePeriodJSON.getString("endDate"));
          log.info("No of days : " + gradeRatePeriodJSON.getString("days"));

          BigDecimal gradeRateDays = new BigDecimal(gradeRatePeriodJSON.getLong("days"));

          // If Grade Rate is for payroll end date, Check and add extra days in month
          Date gradeRateEndDate = dateFormat.parse(gradeRatePeriodJSON.getString("endDate"));
          if (differenceDays.compareTo(BigDecimal.ZERO) > 0
              && gradeRateEndDate.compareTo(payrollEndDate) == 0) {
            log.info("Adding Additional Days for Grade Rate " + gradeRate.getCommercialName()
                + " with Grade Rate end date " + payrollEndDate);
            log.info("Days Before " + gradeRateDays);
            gradeRateDays = gradeRateDays.add(differenceDays);
            log.info("Days After " + gradeRateDays);
          }

          BigDecimal gradeRateValue = gradeRateLne.getSearchKey();

          BigDecimal perDayGradeRate = BigDecimal.ZERO;
          BigDecimal GradeRateCalDays = BigDecimal.ZERO;
          // Validate Monthly or Day Rate
          if (gradeRateLne.getEhcmGraderates().getDuration().equalsIgnoreCase("PM")) {
            log.info("Per Month");
            perDayGradeRate = gradeRateValue.divide(processingDays, 6, BigDecimal.ROUND_HALF_UP);
          } else {
            log.info("Per Day");
            perDayGradeRate = gradeRateValue;
          }

          GradeRateCalDays = perDayGradeRate.multiply(gradeRateDays).setScale(6,
              BigDecimal.ROUND_HALF_UP);
          totalGradeRateValue = totalGradeRateValue.add(GradeRateCalDays);

          log.info("Grade Rate value is : " + GradeRateCalDays);
        }
      }
      return totalGradeRateValue;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getGradeRateValue() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while calculating grade rate value for " + elmTypDef.getName();
      return BigDecimal.ZERO;
    }
  }

  public BigDecimal calculateElementValue(EHCMElmttypeDef elementType, ehcmgrade grade,
      Date startDate, Date endDate, BigDecimal processingDays, BigDecimal differenceDays,
      Date payrollEndDate) {
    BigDecimal elementCalculatedValue;

    if (!errorFlag) {
      try {
        // No need of date validation so bring latest formula
        String whereClause = "e where elementType.id= :empTypeId "
            + "and e.enabled = 'Y' order by e.creationDate desc ";
        OBQuery<EHCMElementFormulaHdr> formulaHdrQry = OBDal.getInstance()
            .createQuery(EHCMElementFormulaHdr.class, whereClause);
        formulaHdrQry.setNamedParameter("empTypeId", elementType.getId());
        formulaHdrQry.setMaxResult(1);
        List<EHCMElementFormulaHdr> formulaHdrList = formulaHdrQry.list();
        EHCMElementFormulaHdr formulaHdr = formulaHdrList.get(0);

        if (!errorFlag) {
          String formula = generateFormulaFromLines(formulaHdr, elementType, grade, startDate,
              endDate, processingDays, differenceDays, payrollEndDate);

          if (!StringUtils.isEmpty(formula)) {
            log.info("Formula :" + formula.toString());
            String appliedFormula = applyPredefinedValues(formula.toString());
            log.info("Applied Formula :" + appliedFormula);

            elementCalculatedValue = calculateFormulaValue(elementType, appliedFormula);

            log.info("Calculated Element Value for element " + elementType.getName() + " is :"
                + elementCalculatedValue);
          } else {
            elementCalculatedValue = BigDecimal.ZERO;
          }
        } else {
          elementCalculatedValue = BigDecimal.ZERO;
        }

        return elementCalculatedValue;
      } catch (Exception cev) {
        log.error("Error in PayrollProcess.java : calculateElementValue() ");
        cev.printStackTrace();
        errorFlag = true;
        errorMessage = "Error while calculating value for element " + elementType.getName();
        return BigDecimal.ZERO;
      }
    } else {
      return BigDecimal.ZERO;
    }
  }

  public String generateFormulaFromLines(EHCMElementFormulaHdr formulaHdr,
      EHCMElmttypeDef elementType, ehcmgrade grade, Date startDate, Date endDate,
      BigDecimal processingDays, BigDecimal differenceDays, Date payrollEndDate) {
    StringBuffer completeFormula = new StringBuffer();
    try {
      String whereClause = "  e where e.element.id=:formulaHdrId and e.enabled = 'Y' order by e.priority";
      OBQuery<EHCMElementFormulaLne> formulaLneQry = OBDal.getInstance()
          .createQuery(EHCMElementFormulaLne.class, whereClause);
      formulaLneQry.setNamedParameter("formulaHdrId", formulaHdr.getId());
      List<EHCMElementFormulaLne> formulaLneList = formulaLneQry.list();
      for (EHCMElementFormulaLne formulaLne : formulaLneList) {
        String formula = formulaLne.getFormula();
        String condition = !StringUtils.isEmpty(formulaLne.getCondition())
            ? formulaLne.getCondition().replace("=", "==")
            : "true";

        // Checking and Apply Multi Rate Value
        if (elementType.getElementSource() != null
            && elementType.getElementSource().equalsIgnoreCase("MGR")) {

          // Fetching grade rate value based on grade rate in formula
          BigDecimal gradeRate = getGradeRateValue(elementType, formulaLne.getGradeRate(), grade,
              startDate, endDate, processingDays, differenceDays, payrollEndDate);

          // Replace grade rate in formula and condition
          formula = formula.replace(GradeRateCode, gradeRate.toString());
          if (!StringUtils.isEmpty(condition)) {
            condition = condition.replace(GradeRateCode, gradeRate.toString());
          }
          log.info("Formula after applying multi rate value : " + formula);
        }

        if (StringUtils.isEmpty(completeFormula.toString())) {
          completeFormula.append("(" + condition + ") ? (" + formula + ")");
        } else {
          completeFormula.append(": (" + condition + ") ? (" + formula + ")");
        }
      }

      if (completeFormula.length() > 0) {
        completeFormula.append(": 0");
      }

      log.info("Formula: " + completeFormula.toString());

      return completeFormula.toString();
    } catch (Exception gffl) {
      log.error("Error in PayrollProcess.java : generateFormulaFromLines() ");
      gffl.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while generaing formula for " + formulaHdr.getElementType().getName();
      return null;
    }
  }

  public BigDecimal calculateFormulaValue(EHCMElmttypeDef elementType, String appliedFormula) {
    try {
      ScriptEngineManager mgr = new ScriptEngineManager();
      ScriptEngine engine = mgr.getEngineByName("JavaScript");
      BigDecimal elementCalculatedValue = new BigDecimal(engine.eval(appliedFormula).toString())
          .setScale(2, BigDecimal.ROUND_HALF_UP);
      return elementCalculatedValue;
    } catch (Exception e) {
      errorFlag = true;
      if (e.getMessage() != null) {
        int index = e.getMessage().indexOf("is not defined");
        if (e.getMessage().startsWith("ReferenceError:") && index > 0) {
          String missingElement = e.getMessage().substring(0, index).replace("ReferenceError: ",
              "");
          errorMessage = String.format(OBMessageUtils.messageBD("EHCM_Payroll_CompNotDefined"),
              missingElement, elementType.getName());
        } else {
          errorMessage = String.format(OBMessageUtils.messageBD("EHCM_Payroll_InvalidFormula"),
              elementType.getName());
        }
      } else {
        errorMessage = "Error while calculating Formula Value";
      }
      return BigDecimal.ZERO;
    }
  }

  public void createEarningAndDeduction(EhcmEmpPerInfo empPerInfo, EmploymentInfo employment,
      EHCMPayrollDefinition payrollDef, EHCMPayrolldefPeriod PayrollDefPeriod,
      HashMap<String, List<EHCMEarnDeductLne>> elementValueMap, String clientId, String orgId,
      String userId) {
    try {
      EHCMEarnDeductHdr earnDeduHdr = OBProvider.getInstance().get(EHCMEarnDeductHdr.class);
      earnDeduHdr.setClient(OBDal.getInstance().get(Client.class, clientId));
      earnDeduHdr.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
      earnDeduHdr.setEnabled(true);
      earnDeduHdr.setCreatedBy(OBDal.getInstance().get(User.class, userId));
      earnDeduHdr.setCreationDate(new java.util.Date());
      earnDeduHdr.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
      earnDeduHdr.setUpdated(new java.util.Date());
      earnDeduHdr.setEmployee(empPerInfo);
      earnDeduHdr.setPayroll(payrollDef);
      earnDeduHdr.setPayrollPeriod(PayrollDefPeriod);
      earnDeduHdr.setStatus("UP");
      earnDeduHdr.setEmployeeType(empPerInfo.getEhcmActiontype().getPersonType());
      earnDeduHdr.setEmployeeName(empPerInfo.getArabicfullname());
      earnDeduHdr.setHireDate(empPerInfo.getHiredate());
      earnDeduHdr.setDepartmentCode(employment.getPosition().getDepartment());
      if (employment.getPosition() != null && employment.getPosition().getSection() != null) {
        earnDeduHdr.setSectionCode(employment.getPosition().getSection());
      }
      earnDeduHdr.setGrade(employment.getGrade());
      earnDeduHdr.setPosition(employment.getPosition());
      earnDeduHdr.setJobTitle(employment.getPosition().getJOBName().getJOBTitle());
      earnDeduHdr.setAssignedDepartment(employment.getSECDeptName());
      earnDeduHdr.setEmploymentGrade(employment.getEmploymentgrade());
      earnDeduHdr.setGradePoint(OBDal.getInstance().get(EHCMPayscalePointV.class,
          employment.getEhcmPayscaleline().getId()));
      earnDeduHdr.setEmployeeCategory(empPerInfo.getGradeClass());
      if (empPerInfo.isEnabled()) {
        earnDeduHdr.setEmployeeStatus(Constants.EMPSTATUS_ACTIVE);
      } else {
        earnDeduHdr.setEmployeeStatus(Constants.EMPSTATUS_INACTIVE);
      }

      OBDal.getInstance().save(earnDeduHdr);
      OBDal.getInstance().flush();

      for (Map.Entry<String, List<EHCMEarnDeductLne>> elementsToSave : elementValueMap.entrySet()) {
        EHCMElmttypeDef element = OBDal.getInstance().get(EHCMElmttypeDef.class,
            elementsToSave.getKey());

        boolean isDeduction = false;
        if (element.getElementClassification().equalsIgnoreCase("DE")) {
          isDeduction = true;
        }

        List<EHCMEarnDeductLne> CalLineList = elementValueMap.get(elementsToSave.getKey());
        log.info("Element " + element.getName() + " has " + CalLineList.size() + " Entries");
        for (EHCMEarnDeductLne calLine : CalLineList) {
          EHCMEarnDeductLne earnDeduLne = OBProvider.getInstance().get(EHCMEarnDeductLne.class);
          earnDeduLne.setClient(OBDal.getInstance().get(Client.class, clientId));
          earnDeduLne.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
          earnDeduLne.setEnabled(true);
          earnDeduLne.setCreatedBy(OBDal.getInstance().get(User.class, userId));
          earnDeduLne.setCreationDate(new java.util.Date());
          earnDeduLne.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
          earnDeduLne.setUpdated(new java.util.Date());
          earnDeduLne.setEhcmEarnDeductHdr(earnDeduHdr);
          earnDeduLne.setElementType(element);
          earnDeduLne.setBaseValue(calLine.getCalculatedValue());
          earnDeduLne.setCalculatedValue(calLine.getCalculatedValue());
          if (calLine.getBusinessMission() != null) {
            earnDeduLne.setBusinessMission(calLine.getBusinessMission());
          }
          earnDeduLne.setProcessed(true);
          earnDeduLne.setDeduction(isDeduction);
          log.info("Added Element of value " + calLine.getCalculatedValue());
          OBDal.getInstance().save(earnDeduLne);
        }
      }
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : createEarningAndDeduction() ");
      e.printStackTrace();
      errorFlag = true;
      errorMessage = "Error while creating Earning And Deduction for Employee "
          + empPerInfo.getName();
    }
  }
}
