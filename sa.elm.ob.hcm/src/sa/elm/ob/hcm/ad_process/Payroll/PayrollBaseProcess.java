package sa.elm.ob.hcm.ad_process.Payroll;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Session;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMBenefitAllowance;
import sa.elm.ob.hcm.EHCMEarnDeductElm;
import sa.elm.ob.hcm.EHCMEarnDeductElmRef;
import sa.elm.ob.hcm.EHCMEarnDeductEmp;
import sa.elm.ob.hcm.EHCMEarnDeductPayroll;
import sa.elm.ob.hcm.EHCMEligbltyCriteria;
import sa.elm.ob.hcm.EHCMElmttypeDef;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMEmpScholarship;
import sa.elm.ob.hcm.EHCMLoanTransaction;
import sa.elm.ob.hcm.EHCMPayrollDefinition;
import sa.elm.ob.hcm.EHCMPayrollProcessHdr;
import sa.elm.ob.hcm.EHCMPayrollProcessLne;
import sa.elm.ob.hcm.EHCMPayrolldefPeriod;
import sa.elm.ob.hcm.EHCMScholarshipDedConf;
import sa.elm.ob.hcm.EHCMticketordertransaction;
import sa.elm.ob.hcm.EhcmDisciplineAction;
import sa.elm.ob.hcm.EhcmElementGroup;
import sa.elm.ob.hcm.EhcmElementGroupLine;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmEmployeeOvertime;
import sa.elm.ob.hcm.EhcmEmploymentGroup;
import sa.elm.ob.hcm.EhcmLoanHistory;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ehcmgradeclass;
import sa.elm.ob.hcm.ehcmgraderatelines;
import sa.elm.ob.hcm.ehcmpayscaleline;
import sa.elm.ob.hcm.dto.payroll.BankDetailsDTO;
import sa.elm.ob.hcm.dto.payroll.EmploymentGroupDTO;
import sa.elm.ob.hcm.dto.payroll.GenericPayrollDTO;
import sa.elm.ob.hcm.util.PayrollConstants;

public class PayrollBaseProcess extends DalBaseProcess {
  private static final Logger log = LoggerFactory.getLogger(PayrollBaseProcess.class);
  private final OBError obError = new OBError();
  static boolean errorFlagMajor = false;
  static boolean errorFlagMinor = false;
  static String errorMessage = "";
  static String infoMessage = "";
  static JSONObject payRollComponents = null;
  static JSONObject calculatedElementValue = null;
  static JSONObject absPaymentComponents = null;
  static JSONObject allowanceComponents = null;

  private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  static BigDecimal totalPayLineBasic;
  static BigDecimal totalPayLineAllowance;
  static BigDecimal totalPayLineDeduction;
  static BigDecimal totalPayLinePension;
  static List<EHCMEarnDeductElm> savedElements = new ArrayList<EHCMEarnDeductElm>();
  static HashMap<String, List<EHCMEarnDeductElm>> elementValueMap = new HashMap<String, List<EHCMEarnDeductElm>>();
  static HashMap<String, List<EHCMEarnDeductElmRef>> elementRefMap = new HashMap<String, List<EHCMEarnDeductElmRef>>();

  @SuppressWarnings("unused")
  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    errorFlagMajor = false;
    errorFlagMinor = false;
    errorMessage = "";
    infoMessage = "";

    try {
      boolean hasMinorError = false;
      final Session session = OBDal.getInstance().getSession();
      // Parameters
      // int a = 0;
      OBContext.setAdminMode(true);
      String payrollProcessHdrId = (String) bundle.getParams().get("Ehcm_Payroll_Process_Hdr_ID");
      final String clientId = (String) bundle.getContext().getClient();
      final String orgId = (String) bundle.getContext().getOrganization();
      final String userId = (String) bundle.getContext().getUser();
      final String roleId = (String) bundle.getContext().getRole();
      EHCMPayrollProcessHdr payrollProcessHdr = OBDal.getInstance().get(EHCMPayrollProcessHdr.class,
          payrollProcessHdrId);

      EHCMPayrolldefPeriod payrollPeriod = payrollProcessHdr.getPayrollPeriod();
      EhcmElementGroup elementGroup = payrollProcessHdr.getElementGroup();
      EhcmEmploymentGroup employmentGroup = payrollProcessHdr.getEhcmEmploymentGroup();

      Date absPaymentStartDate = null;
      Date absPaymentEndDate = null;

      // Payroll Period Dates and No of Days in Period
      Date periodStartDate = dateFormat.parse(payrollPeriod.getStartDate().toString());
      Date periodEndDate = payrollPeriod.getEndDate() != null
          ? dateFormat.parse(payrollPeriod.getEndDate().toString())
          : null;
      long payRollDefDays = 30;
      if (periodStartDate != null && periodEndDate != null) {
        long diffMillSeconds = Math.abs(periodStartDate.getTime() - periodEndDate.getTime());
        payRollDefDays = (diffMillSeconds / (PayrollConstants.HOURS * PayrollConstants.MINUTES
            * PayrollConstants.SECONDS * PayrollConstants.MILLISECONDS)) + 1;
      }
      String periodDBStartDate = sa.elm.ob.utility.util.Utility.formatDate(periodStartDate);
      String periodDBEndDate = sa.elm.ob.utility.util.Utility.formatDate(periodEndDate);
      JSONObject absEmploymentPeriodJSON = null;

      // To store Sub-Element and its value
      payRollComponents = new JSONObject();
      calculatedElementValue = new JSONObject();

      // Set Element Constants
      payRollComponents.put("SYSDATE", "new Date().toDateString()");

      // Get Global Elements
      PayrollBaseProcessDAO.getGlobalPreDefinedElements(clientId);

      if (!errorFlagMajor) {
        // Create (Earning and Deduction - Payroll Details) if not available
        EHCMEarnDeductPayroll earnDeduPayroll = PayrollBaseProcessDAO
            .getEarningDeductionPayrollDetails(payrollProcessHdr, clientId, orgId, userId);

        if (!errorFlagMajor && earnDeduPayroll != null) {

          // Get Employees
          List<GenericPayrollDTO> employeeList = new ArrayList<GenericPayrollDTO>();
          GenericPayrollDTO empGenInput = new GenericPayrollDTO();
          EmploymentGroupDTO empGrp = new EmploymentGroupDTO();
          empGrp.setPayDefId(payrollProcessHdr.getPayroll().getId());
          empGrp.setEmploymentGrpId(employmentGroup != null ? employmentGroup.getId() : null);
          empGrp.setEmployeeId(
              payrollProcessHdr.getEmployee() != null ? payrollProcessHdr.getEmployee().getId()
                  : null);
          empGrp.setStartDate(periodDBStartDate);
          empGrp.setEndDate(periodDBEndDate);
          empGenInput.setEmploymentGroup(empGrp);
          if (employmentGroup != null) {
            employeeList = PayrollBaseProcessDAO.getEmployeesFromEmploymntGrp(empGenInput);
          } else {
            employeeList = PayrollBaseProcessDAO.getEmployeesOfPayrollProcess(empGenInput);
          }

          // Delete employees which is not present in employeelist.
          PayrollBaseProcessDAO.deletePayrollEmployee(payrollProcessHdr, employeeList);

          if (!errorFlagMajor && employeeList != null) {
            for (GenericPayrollDTO empDTO : employeeList) {
              EhcmEmpPerInfo empPerInfo = empDTO.getEmploymentGroup().getEmployeeInfo();

              log.info("******************* Employee " + empPerInfo.getSearchKey()
                  + " **********************");

              savedElements.clear();
              errorFlagMinor = false;
              calculatedElementValue = new JSONObject();

              EmploymentInfo latestEmployment = PayrollBaseProcessDAO
                  .getLatestEmploymentInPayPeriod(empPerInfo, periodDBStartDate, periodDBEndDate);

              if (latestEmployment != null) {
                // Create Payroll Process - Line if not available
                EHCMPayrollProcessLne payrollProcessLne = PayrollBaseProcessDAO
                    .getPayrollProcessLineForEmployee(payrollProcessHdr, earnDeduPayroll,
                        empPerInfo, latestEmployment, periodDBStartDate, periodDBEndDate, clientId,
                        orgId, userId);

                if (payrollProcessLne != null) {
                  totalPayLineBasic = BigDecimal.ZERO;
                  totalPayLineAllowance = BigDecimal.ZERO;
                  totalPayLineDeduction = BigDecimal.ZERO;
                  totalPayLinePension = BigDecimal.ZERO;

                  // Create Earning and Deduction - Employee Details if not available
                  EHCMEarnDeductEmp earnDeduEmployee = PayrollBaseProcessDAO
                      .getEarningDeductionEmployeeDetails(earnDeduPayroll, empPerInfo,
                          latestEmployment, periodDBStartDate, periodDBEndDate, clientId, orgId,
                          userId);

                  if (earnDeduEmployee != null) {
                    // Delete Existing Elements
                    PayrollBaseProcessDAO.deleteEarnDeductElements(payrollProcessLne,
                        earnDeduEmployee);

                    if (!errorFlagMinor) {
                      // Check salary is holded for the employee
                      if (!PayrollBaseProcessDAO.isEmpSalaryHolded(empPerInfo, periodDBStartDate)) {

                        if (!errorFlagMinor) {
                          log.info("////////////////////Salary NOT Holded//////////////");

                          // Generate Employee Based Pre Defined Elements
                          generateEmployeeBasedPreDefElements(empPerInfo);

                          if (!errorFlagMinor) {
                            // Get elements in element group
                            List<EhcmElementGroupLine> elementGrpLineList = PayrollBaseProcessDAO
                                .getElementsInElementGroup(elementGroup);
                            for (EhcmElementGroupLine element : elementGrpLineList) {
                              calculatedElementValue.put(
                                  "E_" + element.getEhcmElmttypeDef().getCode(), BigDecimal.ZERO);
                            }

                            if (elementGrpLineList != null) {
                              for (EhcmElementGroupLine elementGrpLine : elementGrpLineList) {
                                if (!errorFlagMinor) {
                                  log.info("<<<<<<<<<<<<<<<<<Element "
                                      + elementGrpLine.getEhcmElmttypeDef().getName()
                                      + ">>>>>>>>>>>>>>>>>");

                                  // Element Details
                                  EHCMElmttypeDef payrollElement = elementGrpLine
                                      .getEhcmElmttypeDef();
                                  Date elementStartDate = dateFormat
                                      .parse(payrollElement.getStartDate().toString());
                                  Date elementEndDate = payrollElement.getEndDate() != null
                                      ? dateFormat.parse(payrollElement.getEndDate().toString())
                                      : null;

                                  if (isElementValidForPeriod(elementStartDate, elementEndDate,
                                      periodStartDate, periodEndDate)) {

                                    log.info("Applicable Element Dates");
                                    JSONObject elementPeriodJSON = getOverlapingDateRange(
                                        elementStartDate, elementEndDate, periodStartDate,
                                        periodEndDate);

                                    if (elementPeriodJSON != null) {
                                      Date elementOverLappingStartDate = dateFormat
                                          .parse(elementPeriodJSON.getString("startDate"));
                                      Date elementOverLappingEndDate = dateFormat
                                          .parse(elementPeriodJSON.getString("endDate"));

                                      String elementOverLappingDBStartDate = sa.elm.ob.utility.util.Utility
                                          .formatDate(elementOverLappingStartDate);
                                      String elementOverLappingDBEndDate = sa.elm.ob.utility.util.Utility
                                          .formatDate(elementOverLappingEndDate);

                                      // add absence payment value for calculation
                                      absPaymentComponents = new JSONObject();

                                      // Map to store Element and its Value List(Multiple Entries)
                                      elementValueMap.clear();
                                      elementRefMap.clear();

                                      // Check element is from different process
                                      boolean isDifferentProcessElement = false;
                                      if (!payrollElement.getBaseProcess().equalsIgnoreCase("BM")
                                          && !payrollElement.getBaseProcess().equalsIgnoreCase("OT")
                                          && !payrollElement.getBaseProcess().equalsIgnoreCase("SA")
                                          && !payrollElement.getBaseProcess()
                                              .equalsIgnoreCase("TO")) {
                                        isDifferentProcessElement = PayrollBaseProcessDAO
                                            .isEarnDeductElementFromDiffProcess(payrollProcessLne,
                                                earnDeduEmployee, payrollElement);
                                      }

                                      if (!errorFlagMinor && !isDifferentProcessElement) {
                                        // Calculate Processing Days based on Month Days Setup in
                                        // Element
                                        BigDecimal processingDays = null;
                                        BigDecimal differenceDays = BigDecimal.ZERO;
                                        if (payrollElement.getMonthDays().equalsIgnoreCase("GV")) {
                                          BigDecimal globalValue = payrollElement.getGlobalValue()
                                              .getNumericValue();
                                          processingDays = globalValue;
                                          if (payrollElement.getType().equalsIgnoreCase("REC")) {
                                            differenceDays = globalValue
                                                .subtract(new BigDecimal(payRollDefDays));
                                          }
                                        } else {
                                          processingDays = new BigDecimal(payRollDefDays);
                                        }

                                        log.info("Element's processingDays ===> " + processingDays);
                                        log.info("Element's differenceDays ===> " + differenceDays);

                                        if (!errorFlagMinor) {
                                          boolean isDeduction = false;
                                          if (payrollElement.getElementClassification()
                                              .equalsIgnoreCase("DE")) {
                                            isDeduction = true;
                                          }
                                          // base process
                                          if (payrollElement.getBaseProcess()
                                              .equalsIgnoreCase("E")) {
                                            BigDecimal employmentBaseValue = BigDecimal.ZERO;
                                            BigDecimal absPaymentValue = BigDecimal.ZERO;
                                            int employmentCount = 0;
                                            int allowanceCount = 0;

                                            // Fetching employments for an employee
                                            List<EmploymentInfo> employmentList = PayrollBaseProcessDAO
                                                .getEmploymentsOfEmployee(empPerInfo,
                                                    elementOverLappingDBStartDate,
                                                    elementOverLappingDBEndDate);

                                            for (EmploymentInfo employment : employmentList) {
                                              if (!errorFlagMinor) {
                                                employmentCount++;

                                                BigDecimal totalEmploymentValue = BigDecimal.ZERO;

                                                // Employment Details
                                                EhcmPosition position = employment.getPosition();
                                                Organization department = employment.getPosition()
                                                    .getDepartment();
                                                Jobs job = employment.getPosition().getEhcmJobs();
                                                ehcmgrade positionGrade = employment.getPosition()
                                                    .getGrade();
                                                ehcmgradeclass gradeClass = employment.getPosition()
                                                    .getGrade().getEhcmGradeclass();
                                                ehcmgrade grade = employment.getEmploymentgrade();
                                                EHCMPayrollDefinition payroll = employment
                                                    .getEhcmPayrollDefinition();
                                                Date employmentStartDate = dateFormat
                                                    .parse(employment.getStartDate().toString());
                                                Date employmentEndDate = employment
                                                    .getEndDate() != null
                                                        ? dateFormat.parse(
                                                            employment.getEndDate().toString())
                                                        : null;

                                                log.info("Employment ===> " + employmentCount);
                                                log.info("Grade ===> " + grade.getSearchKey());
                                                log.info(
                                                    "Payroll ===> " + payroll.getPayrollName());

                                                // Applicable Employment Days
                                                log.info("Applicable Employment Days");
                                                JSONObject employmentPeriodJSON = getOverlapingDateRange(
                                                    employmentStartDate, employmentEndDate,
                                                    elementOverLappingStartDate,
                                                    elementOverLappingEndDate);

                                                if (employmentPeriodJSON != null) {
                                                  Date employmentOverlappingStartDate = dateFormat
                                                      .parse(employmentPeriodJSON
                                                          .getString("startDate"));
                                                  Date employmentOverlappingEndDate = dateFormat
                                                      .parse(employmentPeriodJSON
                                                          .getString("endDate"));

                                                  String employmentOverlappingDBStartDate = sa.elm.ob.utility.util.Utility
                                                      .formatDate(employmentOverlappingStartDate);
                                                  String employmentOverlappingDBEndDate = sa.elm.ob.utility.util.Utility
                                                      .formatDate(employmentOverlappingEndDate);

                                                  EHCMEligbltyCriteria eligiblity = PayrollBaseProcessDAO
                                                      .getElementEligiblityForEmployment(
                                                          payrollElement, position, department, job,
                                                          positionGrade, gradeClass, payroll,
                                                          employmentOverlappingDBStartDate,
                                                          employmentOverlappingDBEndDate);

                                                  if (eligiblity != null) {

                                                    // Eligiblity Details
                                                    Date eligiblityStartDate = dateFormat.parse(
                                                        eligiblity.getStartDate().toString());
                                                    Date eligiblityEndDate = eligiblity
                                                        .getEndDate() != null
                                                            ? dateFormat.parse(
                                                                eligiblity.getEndDate().toString())
                                                            : null;

                                                    // Applicable Employment Days
                                                    log.info("Applicable Eligiblity Days");
                                                    JSONObject eligiblityPeriodJSON = getOverlapingDateRange(
                                                        eligiblityStartDate, eligiblityEndDate,
                                                        employmentOverlappingStartDate,
                                                        employmentOverlappingEndDate);

                                                    if (eligiblityPeriodJSON != null) {
                                                      Date eligiblityOverlappingStartDate = dateFormat
                                                          .parse(eligiblityPeriodJSON
                                                              .getString("startDate"));
                                                      Date eligiblityOverlappingEndDate = dateFormat
                                                          .parse(eligiblityPeriodJSON
                                                              .getString("endDate"));
                                                      String eligiblityOverlappingDBStartDate = sa.elm.ob.utility.util.Utility
                                                          .formatDate(
                                                              eligiblityOverlappingStartDate);
                                                      String eligiblityOverlappingDBEndDate = sa.elm.ob.utility.util.Utility
                                                          .formatDate(eligiblityOverlappingEndDate);

                                                      absPaymentStartDate = eligiblityOverlappingStartDate;
                                                      absPaymentEndDate = eligiblityOverlappingEndDate;
                                                      absEmploymentPeriodJSON = eligiblityPeriodJSON;
                                                      BigDecimal value = BigDecimal.ZERO;

                                                      // Check Extend Process for Validation
                                                      if (payrollElement
                                                          .getExtendProcess() != null) {
                                                        if (payrollElement.getExtendProcess()
                                                            .equalsIgnoreCase("AD")) {

                                                          List<EHCMBenefitAllowance> allowanceDecisionList = PayrollBaseProcessDAO
                                                              .getAllowanceDecisionForEmployee(
                                                                  empPerInfo, payrollElement,
                                                                  eligiblityOverlappingDBStartDate,
                                                                  eligiblityOverlappingDBEndDate);

                                                          for (EHCMBenefitAllowance allowancDecision : allowanceDecisionList) {
                                                            if (!errorFlagMinor) {
                                                              allowanceCount++;

                                                              log.info("allowanceDecision "
                                                                  + allowanceCount);

                                                              // Allowance Details
                                                              Date allowanceStartDate = dateFormat
                                                                  .parse(allowancDecision
                                                                      .getStartDate().toString());
                                                              Date allowanceEndDate = allowancDecision
                                                                  .getEndDate() != null ? dateFormat
                                                                      .parse(allowancDecision
                                                                          .getEndDate().toString())
                                                                      : null;

                                                              // Applicable Allowance Days
                                                              log.info("Applicable Allowance Days");
                                                              JSONObject allowancePeriodJSON = getOverlapingDateRange(
                                                                  allowanceStartDate,
                                                                  allowanceEndDate,
                                                                  eligiblityOverlappingStartDate,
                                                                  eligiblityOverlappingEndDate);

                                                              if (allowancePeriodJSON != null) {
                                                                Date allowanceOverlappingStartDate = dateFormat
                                                                    .parse(allowancePeriodJSON
                                                                        .getString("startDate"));
                                                                Date allowanceOverlappingEndDate = dateFormat
                                                                    .parse(allowancePeriodJSON
                                                                        .getString("endDate"));

                                                                value = calculateEmploymentBasedValue(
                                                                    payrollElement, employment,
                                                                    allowanceOverlappingStartDate,
                                                                    allowanceOverlappingEndDate,
                                                                    processingDays, differenceDays,
                                                                    periodEndDate, false, false);

                                                                // Sum up value to total value
                                                                totalEmploymentValue = totalEmploymentValue
                                                                    .add(value);

                                                                // Base Value
                                                                if (!errorFlagMinor
                                                                    && payrollElement.getType()
                                                                        .equalsIgnoreCase("REC")
                                                                    && employmentCount == employmentList
                                                                        .size()
                                                                    && allowanceCount == allowanceDecisionList
                                                                        .size()) {
                                                                  employmentBaseValue = calculateEmploymentBasedValue(
                                                                      payrollElement, employment,
                                                                      allowanceOverlappingStartDate,
                                                                      allowanceOverlappingEndDate,
                                                                      processingDays,
                                                                      differenceDays, periodEndDate,
                                                                      true, false);
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
                                                        value = calculateEmploymentBasedValue(
                                                            payrollElement, employment,
                                                            eligiblityOverlappingStartDate,
                                                            eligiblityOverlappingEndDate,
                                                            processingDays, differenceDays,
                                                            periodEndDate, false, false);

                                                        // added elementid for calculate the absence
                                                        // payment
                                                        // value
                                                        if (employmentCount == 1) {
                                                          PayrollBaseProcess.absPaymentComponents
                                                              .put("elementId",
                                                                  payrollElement.getId());
                                                        }

                                                        // Sum up value to total value
                                                        totalEmploymentValue = totalEmploymentValue
                                                            .add(value);

                                                        // Base Value
                                                        if (!errorFlagMinor
                                                            && payrollElement.getType()
                                                                .equalsIgnoreCase("REC")
                                                            && employmentCount == employmentList
                                                                .size()) {
                                                          employmentBaseValue = calculateEmploymentBasedValue(
                                                              payrollElement, employment,
                                                              eligiblityOverlappingStartDate,
                                                              eligiblityOverlappingEndDate,
                                                              processingDays, differenceDays,
                                                              periodEndDate, true, false);
                                                        }
                                                      }
                                                    } else {
                                                      break;
                                                    }
                                                  }
                                                } else {
                                                  break;
                                                }

                                                //// ****

                                                if (!errorFlagMinor) {
                                                  addValueToMap(payrollElement, employmentBaseValue,
                                                      totalEmploymentValue, isDeduction, "E", null);
                                                } else {
                                                  break;
                                                }
                                              } else {
                                                break;
                                              }
                                            }

                                            // Remove employment based pre-defined elements
                                            removeEmploymentBasedPreDefElements();

                                          } else if (payrollElement.getBaseProcess()
                                              .equalsIgnoreCase("BM")) {
                                            BigDecimal businessMissionBaseValue = BigDecimal.ZERO;
                                            int businessMissionCount = 0;

                                            // Fetching employments for an employee
                                            List<EHCMEmpBusinessMission> businessMissionList = PayrollBaseProcessDAO
                                                .getBusinessMissionForEmployee(empPerInfo,
                                                    payrollPeriod, payrollProcessLne);

                                            for (EHCMEmpBusinessMission businessMission : businessMissionList) {
                                              if (!errorFlagMinor) {
                                                businessMissionCount++;
                                                log.info("Business Mission ===> "
                                                    + businessMissionCount);
                                                BigDecimal totalBusinessMissionValue = BigDecimal.ZERO;

                                                // Business Mission Details
                                                EhcmPosition position = businessMission
                                                    .getPosition();
                                                Organization department = businessMission
                                                    .getPosition().getDepartment();
                                                Jobs job = businessMission.getPosition()
                                                    .getEhcmJobs();
                                                ehcmgrade positionGrade = businessMission
                                                    .getPosition().getGrade();
                                                ehcmgradeclass gradeClass = businessMission
                                                    .getPosition().getGrade().getEhcmGradeclass();
                                                ehcmgrade grade = businessMission
                                                    .getEmploymentGrade();
                                                Date BMPayPeriodStartDate = dateFormat
                                                    .parse(businessMission.getPayrollPeriod()
                                                        .getStartDate().toString());
                                                String BMPayPeriodDBStartDate = sa.elm.ob.utility.util.Utility
                                                    .formatDate(BMPayPeriodStartDate);

                                                boolean isEligible = PayrollBaseProcessDAO
                                                    .checkElementEligibleForEmployment(
                                                        payrollElement, position, department, job,
                                                        positionGrade, gradeClass, null,
                                                        BMPayPeriodDBStartDate);

                                                if (isEligible) {
                                                  if (businessMission.getBMNetAmount() != null) {
                                                    totalBusinessMissionValue = businessMission
                                                        .getBMNetAmount();
                                                  }
                                                }

                                                if (!errorFlagMinor) {
                                                  addValueToMap(payrollElement,
                                                      businessMissionBaseValue,
                                                      totalBusinessMissionValue, isDeduction, "BM",
                                                      businessMission);
                                                  // set payroll processed and line reference
                                                  if (!errorFlagMinor) {
                                                    businessMission.setProcessed(true);
                                                    businessMission
                                                        .setPayrollProcessLine(payrollProcessLne);
                                                    OBDal.getInstance().save(businessMission);
                                                  } else {
                                                    break;
                                                  }
                                                } else {
                                                  break;
                                                }
                                              } else {
                                                break;
                                              }
                                            }
                                            // Remove Business Mission based pre-defined elements
                                            removeBMBasedPreDefElements();
                                          } else if (payrollElement.getBaseProcess()
                                              .equalsIgnoreCase("AD")) {
                                            log.info("Getting Allowance Decision for Employee: "
                                                + empPerInfo.getName() + " Allowance Element : "
                                                + payrollElement.getName());

                                            BigDecimal allowanceBaseValue = BigDecimal.ZERO;
                                            int allowanceCount = 0;
                                            int employmentCount = 0;

                                            // Fetching allowance decision for an employee and
                                            // element
                                            List<EHCMBenefitAllowance> allowanceDecisionList = PayrollBaseProcessDAO
                                                .getAllowanceDecisionForEmployee(empPerInfo,
                                                    payrollElement, elementOverLappingDBStartDate,
                                                    elementOverLappingDBEndDate);

                                            for (EHCMBenefitAllowance allowancDecision : allowanceDecisionList) {
                                              if (!errorFlagMinor) {
                                                allowanceCount++;
                                                BigDecimal totalAllowanceValue = BigDecimal.ZERO;
                                                log.info("Allowance ===> " + allowanceCount);

                                                // Allowance Details
                                                Date allowanceStartDate = dateFormat.parse(
                                                    allowancDecision.getStartDate().toString());
                                                Date allowanceEndDate = allowancDecision
                                                    .getEndDate() != null ? dateFormat.parse(
                                                        allowancDecision.getEndDate().toString())
                                                        : null;

                                                log.info("Applicable Allowance Days");
                                                JSONObject allowancePeriodJSON = getOverlapingDateRange(
                                                    allowanceStartDate, allowanceEndDate,
                                                    elementOverLappingStartDate,
                                                    elementOverLappingEndDate);

                                                if (allowancePeriodJSON != null) {
                                                  Date allowanceOverlappingStartDate = dateFormat
                                                      .parse(allowancePeriodJSON
                                                          .getString("startDate"));
                                                  Date allowanceOverlappingEndDate = dateFormat
                                                      .parse(
                                                          allowancePeriodJSON.getString("endDate"));
                                                  String allowanceOverlappingDBStartDate = sa.elm.ob.utility.util.Utility
                                                      .formatDate(allowanceOverlappingStartDate);
                                                  String allowanceOverlappingDBEndDate = sa.elm.ob.utility.util.Utility
                                                      .formatDate(allowanceOverlappingEndDate);

                                                  // Employment Details
                                                  List<EmploymentInfo> employmentList = PayrollBaseProcessDAO
                                                      .getEmploymentsOfEmployee(empPerInfo,
                                                          allowanceOverlappingDBStartDate,
                                                          allowanceOverlappingDBEndDate);

                                                  for (EmploymentInfo employment : employmentList) {
                                                    if (!errorFlagMinor) {
                                                      allowanceComponents = new JSONObject();
                                                      employmentCount++;
                                                      log.info(
                                                          "Employment ===> " + employmentCount);

                                                      // Employment Details
                                                      EhcmPosition position = employment
                                                          .getPosition();
                                                      Organization department = employment
                                                          .getPosition().getDepartment();
                                                      Jobs job = employment.getPosition()
                                                          .getEhcmJobs();
                                                      ehcmgrade positionGrade = employment
                                                          .getPosition().getGrade();
                                                      ehcmgradeclass gradeClass = employment
                                                          .getPosition().getGrade()
                                                          .getEhcmGradeclass();
                                                      ehcmgrade grade = employment
                                                          .getEmploymentgrade();
                                                      EHCMPayrollDefinition payroll = employment
                                                          .getEhcmPayrollDefinition();
                                                      Date employmentStartDate = dateFormat.parse(
                                                          employment.getStartDate().toString());
                                                      Date employmentEndDate = employment
                                                          .getEndDate() != null ? dateFormat.parse(
                                                              employment.getEndDate().toString())
                                                              : null;

                                                      // Applicable Employment Days
                                                      log.info("Applicable Employment Days");
                                                      JSONObject employmentPeriodJSON = getOverlapingDateRange(
                                                          employmentStartDate, employmentEndDate,
                                                          allowanceOverlappingStartDate,
                                                          allowanceOverlappingEndDate);

                                                      if (employmentPeriodJSON != null) {
                                                        Date employmentOverlappingStartDate = dateFormat
                                                            .parse(employmentPeriodJSON
                                                                .getString("startDate"));
                                                        Date employmentOverlappingEndDate = dateFormat
                                                            .parse(employmentPeriodJSON
                                                                .getString("endDate"));
                                                        String employmentOverlappingDBStartDate = sa.elm.ob.utility.util.Utility
                                                            .formatDate(
                                                                employmentOverlappingStartDate);
                                                        String employmentOverlappingDBEndDate = sa.elm.ob.utility.util.Utility
                                                            .formatDate(
                                                                employmentOverlappingEndDate);

                                                        EHCMEligbltyCriteria eligiblity = PayrollBaseProcessDAO
                                                            .getElementEligiblityForEmployment(
                                                                payrollElement, position,
                                                                department, job, positionGrade,
                                                                gradeClass, payroll,
                                                                employmentOverlappingDBStartDate,
                                                                employmentOverlappingDBEndDate);

                                                        if (eligiblity != null) {
                                                          // Eligiblity Details
                                                          Date eligiblityStartDate = dateFormat
                                                              .parse(eligiblity.getStartDate()
                                                                  .toString());
                                                          Date eligiblityEndDate = eligiblity
                                                              .getEndDate() != null
                                                                  ? dateFormat.parse(eligiblity
                                                                      .getEndDate().toString())
                                                                  : null;

                                                          log.info("Applicable Eligiblity Days");
                                                          JSONObject eligiblityPeriodJSON = getOverlapingDateRange(
                                                              eligiblityStartDate,
                                                              eligiblityEndDate,
                                                              employmentOverlappingStartDate,
                                                              employmentOverlappingEndDate);

                                                          if (eligiblityPeriodJSON != null) {
                                                            Date eligiblityOverlappingStartDate = dateFormat
                                                                .parse(eligiblityPeriodJSON
                                                                    .getString("startDate"));
                                                            Date eligiblityOverlappingEndDate = dateFormat
                                                                .parse(eligiblityPeriodJSON
                                                                    .getString("endDate"));

                                                            // Calculate allowance value
                                                            BigDecimal days = new BigDecimal(
                                                                eligiblityPeriodJSON
                                                                    .getLong("days"));
                                                            BigDecimal value = BigDecimal.ZERO;
                                                            if (allowancDecision.getValueType()
                                                                .equalsIgnoreCase(
                                                                    PayrollConstants.ALLOWANCE_FIXEDAMOUNT)) {
                                                              BigDecimal perDayFixedAmt = allowancDecision
                                                                  .getFixedAmount()
                                                                  .divide(processingDays, 6,
                                                                      BigDecimal.ROUND_HALF_UP);
                                                              value = perDayFixedAmt.multiply(days);

                                                              // absence calculation
                                                              allowanceComponents
                                                                  .put("isFixedAmount", true);
                                                              allowanceComponents.put("fixedAmount",
                                                                  allowancDecision
                                                                      .getFixedAmount());
                                                              allowanceComponents.put(
                                                                  "perDayFixedAmt", perDayFixedAmt);

                                                              // Base Value
                                                              if (!errorFlagMinor
                                                                  && payrollElement.getType()
                                                                      .equalsIgnoreCase("REC")
                                                                  && allowanceCount == allowanceDecisionList
                                                                      .size()
                                                                  && employmentCount == employmentList
                                                                      .size()) {
                                                                allowanceBaseValue = perDayFixedAmt
                                                                    .multiply(processingDays);
                                                              }
                                                            } else if (allowancDecision
                                                                .getValueType().equalsIgnoreCase(
                                                                    PayrollConstants.ALLOWANCE_PERCENTAGE)) {
                                                              BigDecimal percent = allowancDecision
                                                                  .getPercentage().divide(
                                                                      PayrollConstants.PERCENTAGE_MAXIMUM,
                                                                      6, BigDecimal.ROUND_HALF_UP);

                                                              // absence calculation
                                                              allowanceComponents
                                                                  .put("isFixedAmount", false);
                                                              allowanceComponents.put("percent",
                                                                  percent);

                                                              if (allowancDecision.getCategory()
                                                                  .equalsIgnoreCase(
                                                                      PayrollConstants.ALLOWANCE_PERCENT_BASIC)) {
                                                                BigDecimal payscale = PayrollBaseProcessDAO
                                                                    .getPayScaleValue(employment,
                                                                        eligiblityOverlappingStartDate,
                                                                        eligiblityOverlappingEndDate,
                                                                        processingDays,
                                                                        differenceDays,
                                                                        periodEndDate);
                                                                value = payscale.multiply(percent);

                                                                // absence calculation
                                                                allowanceComponents
                                                                    .put("isPercentBasic", true);
                                                                allowanceComponents.put("payscale",
                                                                    payscale);

                                                                // Base Value
                                                                if (!errorFlagMinor
                                                                    && payrollElement.getType()
                                                                        .equalsIgnoreCase("REC")
                                                                    && allowanceCount == allowanceDecisionList
                                                                        .size()
                                                                    && employmentCount == employmentList
                                                                        .size()) {
                                                                  BigDecimal latestPayscale = PayrollBaseProcessDAO
                                                                      .getLatestPayScaleValueInEmployment(
                                                                          employment,
                                                                          eligiblityOverlappingStartDate,
                                                                          eligiblityOverlappingEndDate);
                                                                  allowanceBaseValue = latestPayscale
                                                                      .multiply(percent);
                                                                }
                                                              } else if (allowancDecision
                                                                  .getCategory().equalsIgnoreCase(
                                                                      PayrollConstants.ALLOWANCE_PERCENT_FIRSTSTEPGRADE)) {
                                                                BigDecimal firstStepGradeVal = PayrollBaseProcessDAO
                                                                    .getFirstStepGradeValue(
                                                                        employment,
                                                                        eligiblityOverlappingStartDate,
                                                                        eligiblityOverlappingEndDate,
                                                                        processingDays,
                                                                        differenceDays,
                                                                        periodEndDate);
                                                                value = firstStepGradeVal
                                                                    .multiply(percent);

                                                                // absence calculation
                                                                allowanceComponents
                                                                    .put("isPercentBasic", false);
                                                                allowanceComponents.put(
                                                                    "firstStepGradeVal",
                                                                    firstStepGradeVal);

                                                                // Base Value
                                                                if (!errorFlagMinor
                                                                    && payrollElement.getType()
                                                                        .equalsIgnoreCase("REC")
                                                                    && allowanceCount == allowanceDecisionList
                                                                        .size()
                                                                    && employmentCount == employmentList
                                                                        .size()) {
                                                                  BigDecimal latestFirstStepGradeVal = PayrollBaseProcessDAO
                                                                      .getLatestFirstStepGradeValue(
                                                                          employment,
                                                                          eligiblityOverlappingStartDate,
                                                                          eligiblityOverlappingEndDate);
                                                                  allowanceBaseValue = latestFirstStepGradeVal
                                                                      .multiply(percent);
                                                                }
                                                              }
                                                            }
                                                            totalAllowanceValue = totalAllowanceValue
                                                                .add(value);

                                                            if (employmentCount == 1) {
                                                              PayrollBaseProcess.absPaymentComponents
                                                                  .put("elementId",
                                                                      payrollElement.getId());
                                                            }

                                                            PayrollBaseProcessDAO
                                                                .calculateAbsenceDeductedValueForAllowance(
                                                                    employment,
                                                                    eligiblityOverlappingStartDate,
                                                                    eligiblityOverlappingEndDate,
                                                                    processingDays, differenceDays,
                                                                    periodEndDate, false,
                                                                    allowancDecision,
                                                                    allowanceComponents,
                                                                    payrollElement,
                                                                    new BigDecimal(
                                                                        eligiblityPeriodJSON
                                                                            .getLong("days")));

                                                          } else {
                                                            break;
                                                          }
                                                        }
                                                      } else {
                                                        break;
                                                      }

                                                      //// ****

                                                    } else {
                                                      break;
                                                    }
                                                  }

                                                  if (!errorFlagMinor) {

                                                    addValueToMap(payrollElement,
                                                        allowanceBaseValue, totalAllowanceValue,
                                                        isDeduction, "AD", allowancDecision);
                                                  } else {
                                                    break;
                                                  }

                                                } else {
                                                  break;
                                                }

                                              } else {
                                                break;
                                              }
                                            }
                                          } else if (payrollElement.getBaseProcess()
                                              .equalsIgnoreCase("DA")) {

                                            log.info("Getting Disciplinary action for Employee: "
                                                + empPerInfo.getName() + " Allowance Element : "
                                                + payrollElement.getName());

                                            GenericPayrollDTO payrollDto = new GenericPayrollDTO();
                                            EmploymentGroupDTO dto = new EmploymentGroupDTO();
                                            dto.setEmployeeId(empPerInfo.getId());
                                            dto.setStartDate(periodDBStartDate);
                                            dto.setEndDate(periodDBEndDate);
                                            dto.setPayrollLineId(payrollProcessLne.getId());
                                            payrollDto.setEmploymentGroup(dto);

                                            // Fetching Disciplinary action for an employee
                                            List<GenericPayrollDTO> disciplinaryActionList = PayrollBaseProcessDAO
                                                .getDisciplinaryAction(payrollDto);
                                            if (disciplinaryActionList != null) {
                                              for (GenericPayrollDTO disciplinaryList : disciplinaryActionList) {
                                                if (!errorFlagMinor) {
                                                  BigDecimal disciplinaryActionAmount = BigDecimal.ZERO;

                                                  // Disciplinary Action Details
                                                  EhcmDisciplineAction disciplinaryAction = disciplinaryList
                                                      .getEmploymentGroup().getDisciplineAction();
                                                  EhcmPosition position = disciplinaryAction
                                                      .getPosition();
                                                  Organization department = disciplinaryAction
                                                      .getPosition().getDepartment();
                                                  Jobs job = disciplinaryAction.getPosition()
                                                      .getEhcmJobs();
                                                  ehcmgrade positionGrade = disciplinaryAction
                                                      .getPosition().getGrade();
                                                  ehcmgradeclass gradeClass = disciplinaryAction
                                                      .getPosition().getGrade().getEhcmGradeclass();
                                                  ehcmgrade grade = disciplinaryAction
                                                      .getEmploymentGrade();
                                                  Date disciplinaryActionEffectiveDate = dateFormat
                                                      .parse(disciplinaryAction.getEffectiveDate()
                                                          .toString());
                                                  String disciplinaryActionDBEffectiveDate = sa.elm.ob.utility.util.Utility
                                                      .formatDate(disciplinaryActionEffectiveDate);

                                                  disciplinaryActionAmount = disciplinaryAction
                                                      .getAmount();

                                                  if (!errorFlagMinor) {
                                                    addValueToMap(payrollElement, BigDecimal.ZERO,
                                                        disciplinaryActionAmount, isDeduction, "DA",
                                                        disciplinaryAction);

                                                    // set payroll processed and line reference
                                                    if (!errorFlagMinor) {
                                                      disciplinaryAction.setPayrollProcessed(true);
                                                      disciplinaryAction
                                                          .setPayrollProcessLine(payrollProcessLne);
                                                      OBDal.getInstance().save(disciplinaryAction);
                                                    } else {
                                                      break;
                                                    }
                                                  } else {
                                                    break;
                                                  }

                                                  //// ****
                                                } else {
                                                  break;
                                                }
                                              }
                                            }
                                          } else if (payrollElement.getBaseProcess()
                                              .equalsIgnoreCase("LD")) {
                                            log.info("Getting Loan deduction for Employee: "
                                                + empPerInfo.getName() + " Allowance Element : "
                                                + payrollElement.getName());
                                            BigDecimal installAmount = BigDecimal.ZERO;
                                            BigDecimal diffAmt = BigDecimal.ZERO;

                                            // Fetching Loan deduction for an employee
                                            List<EHCMLoanTransaction> loanTransaction = PayrollBaseProcessDAO
                                                .getLoanTransaction(periodStartDate,
                                                    empPerInfo.getId());
                                            if (loanTransaction != null) {
                                              for (EHCMLoanTransaction loanList : loanTransaction) {
                                                if (!errorFlagMinor) {
                                                  // chek hold periods
                                                  if (loanList.getHoldEndPeriod() == null
                                                      || (loanList.getHoldEndPeriod() != null
                                                          && payrollPeriod.getStartDate()
                                                              .compareTo(loanList.getHoldEndPeriod()
                                                                  .getStartDate()) >= 0
                                                          || payrollPeriod.getEndDate().compareTo(
                                                              loanList.getHoldDate()) <= 0)) {

                                                    if (loanList.getInstallmentAmount()
                                                        .compareTo(loanList.getRemamount()) <= 0) {
                                                      installAmount = loanList
                                                          .getInstallmentAmount();
                                                    } else {
                                                      installAmount = loanList.getRemamount();
                                                    }

                                                    // insert loan payment history.
                                                    // if same payroll process then check amount
                                                    // diff
                                                    // else
                                                    // insert new history
                                                    EhcmLoanHistory loanHistory = PayrollBaseProcessDAO
                                                        .getLoanhistoryObj(loanList,
                                                            payrollProcessLne);
                                                    if (loanHistory != null) {
                                                      if (loanHistory.getAmount()
                                                          .compareTo(installAmount) != 0) {
                                                        loanHistory.setAmount(installAmount);
                                                        OBDal.getInstance().save(loanHistory);
                                                      }
                                                    } else {
                                                      PayrollBaseProcessDAO.insertLoanHistory(
                                                          loanList, payrollProcessLne,
                                                          installAmount);
                                                    }

                                                    if (!errorFlagMinor) {
                                                      addValueToMap(payrollElement, BigDecimal.ZERO,
                                                          installAmount, isDeduction, "LA",
                                                          loanList);
                                                    } else {
                                                      break;
                                                    }
                                                  }
                                                } else {
                                                  break;
                                                }
                                              }
                                            }
                                          } else if (payrollElement.getBaseProcess()
                                              .equalsIgnoreCase("OT")) {
                                            log.info("Getting Overtime txn for Employee: "
                                                + empPerInfo.getName() + " Overtime Element : "
                                                + payrollElement.getName());

                                            // Fetching Overtime transaction for an employee.
                                            List<EhcmEmployeeOvertime> overTimeList = PayrollBaseProcessDAO
                                                .getOvertimeForEmployee(empPerInfo,
                                                    payrollProcessLne,
                                                    elementOverLappingDBStartDate,
                                                    elementOverLappingDBEndDate);

                                            for (EhcmEmployeeOvertime overTime : overTimeList) {
                                              if (!errorFlagMinor) {
                                                BigDecimal totalBusinessMissionValue = BigDecimal.ZERO;

                                                // Employment Details & Eligibility
                                                EhcmPosition position = overTime.getPosition();
                                                Organization department = overTime.getPosition()
                                                    .getDepartment();
                                                Jobs job = overTime.getPosition().getEhcmJobs();
                                                ehcmgrade positionGrade = overTime.getPosition()
                                                    .getGrade();
                                                ehcmgradeclass gradeClass = overTime.getPosition()
                                                    .getGrade().getEhcmGradeclass();
                                                ehcmgrade grade = overTime.getEmploymentGrade();

                                                Date overTimePaymentStartDate = dateFormat.parse(
                                                    overTime.getPaymentStartDate().toString());
                                                String overTimePaymentDBStartDate = sa.elm.ob.utility.util.Utility
                                                    .formatDate(overTimePaymentStartDate);

                                                boolean isEligible = PayrollBaseProcessDAO
                                                    .checkElementEligibleForEmployment(
                                                        payrollElement, position, department, job,
                                                        positionGrade, gradeClass, null,
                                                        overTimePaymentDBStartDate);

                                                if (isEligible) {
                                                  if (!errorFlagMinor) {
                                                    addValueToMap(payrollElement, BigDecimal.ZERO,
                                                        overTime.getOvertimeAmount(), isDeduction,
                                                        "OT", overTime);
                                                    // set payroll processed and line reference
                                                    if (!errorFlagMinor) {
                                                      overTime.setPayrollprocessed(true);
                                                      overTime
                                                          .setPayrollProcessLine(payrollProcessLne);
                                                      OBDal.getInstance().save(overTime);
                                                    } else {
                                                      break;
                                                    }
                                                  } else {
                                                    break;
                                                  }
                                                }
                                              } else {
                                                break;
                                              }
                                            }
                                          } else if (payrollElement.getBaseProcess()
                                              .equalsIgnoreCase("PE")) {
                                            BigDecimal employmentBaseValue = BigDecimal.ZERO;
                                            BigDecimal totalFullBasicValue = BigDecimal.ZERO;
                                            BigDecimal elementCalculatedValue = BigDecimal.ZERO;
                                            int employmentCount = 0;
                                            int allowanceCount = 0;
                                            List<EHCMElmttypeDef> basicElementList = null;

                                            // FETCH BASIC EMPLOYMENT
                                            basicElementList = PayrollBaseProcessDAO
                                                .getBasicElement();
                                            if (basicElementList != null) {
                                              // EHCMElmttypeDef payrollElement
                                              EHCMElmttypeDef payrollBaicElement = basicElementList
                                                  .get(0);

                                              // Fetching employments for an employee
                                              List<EmploymentInfo> employmentList = PayrollBaseProcessDAO
                                                  .getEmploymentsOfEmployee(empPerInfo,
                                                      periodDBStartDate, periodDBEndDate);

                                              for (EmploymentInfo employment : employmentList) {
                                                if (!errorFlagMinor) {
                                                  employmentCount++;

                                                  log.info("Employment ===> " + employmentCount);

                                                  // Converting employment dates to proper date
                                                  // format
                                                  Date emplStartDate = dateFormat
                                                      .parse(employment.getStartDate().toString());
                                                  Date emplEndDate = employment.getEndDate() != null
                                                      ? dateFormat
                                                          .parse(employment.getEndDate().toString())
                                                      : null;

                                                  // Calculating applicable days in employment
                                                  // according
                                                  // to
                                                  // payrollDate
                                                  log.info(
                                                      "Employment Days applicable in Payroll Period");
                                                  JSONObject employmentPeriodJSON = getOverlapingDateRange(
                                                      emplStartDate, emplEndDate, periodStartDate,
                                                      periodEndDate);

                                                  if (employmentPeriodJSON != null) {

                                                    Date employmentStartDate = dateFormat
                                                        .parse(employmentPeriodJSON
                                                            .getString("startDate"));
                                                    Date employmentEndDate = dateFormat.parse(
                                                        employmentPeriodJSON.getString("endDate"));

                                                    String employStartDate = sa.elm.ob.utility.util.Utility
                                                        .formatDate(employmentStartDate);
                                                    String employEndDate = sa.elm.ob.utility.util.Utility
                                                        .formatDate(employmentEndDate);
                                                    absPaymentStartDate = employmentStartDate;
                                                    absPaymentEndDate = employmentEndDate;
                                                    absEmploymentPeriodJSON = employmentPeriodJSON;
                                                    BigDecimal value = BigDecimal.ZERO;

                                                    value = calculateEmploymentBasedValue(
                                                        payrollBaicElement, employment,
                                                        employmentStartDate, employmentEndDate,
                                                        processingDays, differenceDays,
                                                        periodEndDate, false, true);

                                                    // Sum up value to total value
                                                    totalFullBasicValue = totalFullBasicValue
                                                        .add(value);

                                                  } else {
                                                    break;
                                                  }

                                                } else {
                                                  break;
                                                }
                                              }

                                              // set payrollelement for fullbasicvalue (without
                                              // absence
                                              // calculation)
                                              payRollComponents.put("FULL_BASIC_VALUE",
                                                  totalFullBasicValue);

                                              elementCalculatedValue = PayrollBaseProcessDAO
                                                  .calculateElementValue(payrollElement, null,
                                                      periodStartDate, periodEndDate,
                                                      processingDays, differenceDays, periodEndDate,
                                                      false, null);

                                              if (!errorFlagMinor) {
                                                addValueToMap(payrollElement, employmentBaseValue,
                                                    elementCalculatedValue, isDeduction, "PE",
                                                    null);
                                              }

                                              // Remove employment based pre-defined elements
                                              removeEmploymentBasedPreDefElements();

                                            }
                                          } else if (payrollElement.getBaseProcess()
                                              .equalsIgnoreCase("SA")) {
                                            BigDecimal stBaseValue = BigDecimal.ZERO;
                                            int stCount = 0;

                                            // Fetching scholarship&Training for an employee

                                            List<EHCMEmpScholarship> scholarshipList = PayrollBaseProcessDAO
                                                .getScholarshipForEmployee(empPerInfo,
                                                    payrollPeriod, payrollProcessLne);

                                            for (EHCMEmpScholarship scholarship : scholarshipList) {
                                              if (!errorFlagMinor) {
                                                stCount++;
                                                BigDecimal totalScholarshipValue = BigDecimal.ZERO;
                                                log.info("Scholarship & Training ===> " + stCount);

                                                // Employment Details & Eligibility
                                                EhcmPosition position = scholarship.getPosition();
                                                Organization department = scholarship.getPosition()
                                                    .getDepartment();
                                                Jobs job = scholarship.getPosition().getEhcmJobs();
                                                ehcmgrade positionGrade = scholarship.getPosition()
                                                    .getGrade();
                                                ehcmgradeclass gradeClass = scholarship
                                                    .getPosition().getGrade().getEhcmGradeclass();
                                                ehcmgrade grade = scholarship.getEmploymentGrade();
                                                Date scholarshipPaymentPeriodStartDate = dateFormat
                                                    .parse(scholarship.getPayrollPeriod()
                                                        .getStartDate().toString());
                                                String scholarshipPaymentPeriodDBStartDate = sa.elm.ob.utility.util.Utility
                                                    .formatDate(scholarshipPaymentPeriodStartDate);

                                                boolean isEligible = PayrollBaseProcessDAO
                                                    .checkElementEligibleForEmployment(
                                                        payrollElement, position, department, job,
                                                        positionGrade, gradeClass, null,
                                                        scholarshipPaymentPeriodDBStartDate);

                                                if (isEligible) {
                                                  if (scholarship.getNETAmt() != null) {
                                                    totalScholarshipValue = scholarship.getNETAmt();
                                                  }
                                                }

                                                if (!errorFlagMinor) {
                                                  addValueToMap(payrollElement, stBaseValue,
                                                      totalScholarshipValue, isDeduction, "SA",
                                                      scholarship);
                                                  // set payroll processed and line reference
                                                  if (!errorFlagMinor) {
                                                    scholarship.setProcessed(true);
                                                    scholarship
                                                        .setPayrollProcessLine(payrollProcessLne);
                                                    OBDal.getInstance().save(scholarship);
                                                  } else {
                                                    break;
                                                  }
                                                } else {
                                                  break;
                                                }
                                              } else {
                                                break;
                                              }
                                            }
                                          } else if (payrollElement.getBaseProcess()
                                              .equalsIgnoreCase("TO")) {
                                            BigDecimal toBaseValue = BigDecimal.ZERO;
                                            int toCount = 0;

                                            // Fetching Ticket Orders for an employee

                                            List<EHCMticketordertransaction> ticketOrderList = PayrollBaseProcessDAO
                                                .getTicketOrdersForEmployee(empPerInfo,
                                                    payrollPeriod, payrollProcessLne);

                                            for (EHCMticketordertransaction ticketOrder : ticketOrderList) {
                                              if (!errorFlagMinor) {
                                                toCount++;
                                                BigDecimal totalTicketOrderValue = BigDecimal.ZERO;
                                                log.info("Ticket Orders ===> " + toCount);

                                                // Employment Details & Eligibility
                                                EhcmPosition position = ticketOrder.getPosition();
                                                Organization department = ticketOrder.getPosition()
                                                    .getDepartment();
                                                Jobs job = ticketOrder.getPosition().getEhcmJobs();
                                                ehcmgrade positionGrade = ticketOrder.getPosition()
                                                    .getGrade();
                                                ehcmgradeclass gradeClass = ticketOrder
                                                    .getPosition().getGrade().getEhcmGradeclass();
                                                ehcmgrade grade = ticketOrder.getEmploymentGrade();
                                                Date ticketOrderPeriodStartDate = dateFormat
                                                    .parse(ticketOrder.getPaymentPeriod()
                                                        .getStartDate().toString());
                                                String ticketOrderPeriodDBStartDate = sa.elm.ob.utility.util.Utility
                                                    .formatDate(ticketOrderPeriodStartDate);

                                                boolean isEligible = PayrollBaseProcessDAO
                                                    .checkElementEligibleForEmployment(
                                                        payrollElement, position, department, job,
                                                        positionGrade, gradeClass, null,
                                                        ticketOrderPeriodDBStartDate);

                                                if (isEligible) {
                                                  if (ticketOrder.getAdultTicketPrice() != null) {
                                                    totalTicketOrderValue = ticketOrder
                                                        .getAdultTicketPrice();
                                                  }
                                                }

                                                if (!errorFlagMinor) {
                                                  addValueToMap(payrollElement, toBaseValue,
                                                      totalTicketOrderValue, isDeduction, "TO",
                                                      ticketOrder);
                                                  // set payroll processed and line reference
                                                  if (!errorFlagMinor) {
                                                    ticketOrder.setProcessed(true);
                                                    ticketOrder
                                                        .setPayrollProcessLine(payrollProcessLne);
                                                    OBDal.getInstance().save(ticketOrder);
                                                  } else {
                                                    break;
                                                  }
                                                } else {
                                                  break;
                                                }
                                              } else {
                                                break;
                                              }
                                            }
                                          } else if (payrollElement.getBaseProcess()
                                              .equalsIgnoreCase("SBA")) {
                                            int scholarshipBusinessCount = 0;

                                            // Get Scholarship Allowance Days
                                            BigDecimal scholarshipAllowanceDays = new BigDecimal(
                                                "30");
                                            if (!StringUtils.isEmpty(payRollComponents.getString(
                                                PayrollConstants.GLOBAL_SCHOLARSHIP_REWARD_DAYS))) {
                                              String scholarshipAllowanceDays_Str = payRollComponents
                                                  .getString(
                                                      PayrollConstants.GLOBAL_SCHOLARSHIP_REWARD_DAYS);
                                              scholarshipAllowanceDays = new BigDecimal(
                                                  scholarshipAllowanceDays_Str);
                                            }
                                            log.info("scholarshipAllowanceDays ==> "
                                                + scholarshipAllowanceDays);

                                            // Get Scholarship Business List
                                            List<EHCMEmpScholarship> scholarshipList = PayrollBaseProcessDAO
                                                .getScholarshipBusinessForEmployee(empPerInfo,
                                                    elementOverLappingDBStartDate,
                                                    elementOverLappingDBEndDate,
                                                    scholarshipAllowanceDays);

                                            for (EHCMEmpScholarship scholarship : scholarshipList) {
                                              if (!errorFlagMinor) {
                                                scholarshipBusinessCount++;
                                                log.info("scholarshipBusiness ===> "
                                                    + scholarshipBusinessCount);

                                                // Scholarship Details
                                                EhcmPosition position = scholarship.getPosition();
                                                Organization department = scholarship.getPosition()
                                                    .getDepartment();
                                                Jobs job = scholarship.getPosition().getEhcmJobs();
                                                ehcmgrade positionGrade = scholarship.getPosition()
                                                    .getGrade();
                                                ehcmgradeclass gradeClass = scholarship
                                                    .getPosition().getGrade().getEhcmGradeclass();
                                                ehcmgrade grade = scholarship.getEmploymentGrade();
                                                Date scholarshipStartDate = dateFormat
                                                    .parse(scholarship.getStartDate().toString());
                                                Date scholarshipEndDate = scholarship
                                                    .getEndDate() != null
                                                        ? dateFormat.parse(
                                                            scholarship.getEndDate().toString())
                                                        : null;
                                                BigDecimal scholarshipDays = BigDecimal.ZERO;
                                                if (scholarship.getNoofdays() != null) {
                                                  scholarshipDays = new BigDecimal(
                                                      scholarship.getNoofdays());
                                                }
                                                log.info("Scholarship Days ==> " + scholarshipDays);

                                                // Calculate Scholarship Business Start Date
                                                Calendar c = Calendar.getInstance();
                                                c.setTime(scholarshipStartDate);
                                                c.add(Calendar.DAY_OF_MONTH,
                                                    scholarshipAllowanceDays.intValue());
                                                Date scholarshipBusinessStartDate = c.getTime();
                                                log.info("Scholarship Start Date ==> "
                                                    + scholarshipStartDate);
                                                log.info("Scholarship Business Start Date ==> "
                                                    + scholarshipBusinessStartDate);
                                                Date scholarshipBusinessEndDate = scholarshipEndDate;

                                                log.info("Applicable Scholarship Dates ");
                                                JSONObject scholarshipPeriodJSON = getOverlapingDateRange(
                                                    scholarshipStartDate, scholarshipEndDate,
                                                    elementOverLappingStartDate,
                                                    elementOverLappingEndDate);

                                                if (scholarshipPeriodJSON != null) {
                                                  Date scholarshipOverlappingStartDate = dateFormat
                                                      .parse(scholarshipPeriodJSON
                                                          .getString("startDate"));
                                                  Date scholarshipOverlappingEndDate = dateFormat
                                                      .parse(scholarshipPeriodJSON
                                                          .getString("endDate"));
                                                  String scholarshipeOverlappingDBStartDate = sa.elm.ob.utility.util.Utility
                                                      .formatDate(scholarshipOverlappingStartDate);
                                                  String scholarshipOverlappingDBEndDate = sa.elm.ob.utility.util.Utility
                                                      .formatDate(scholarshipOverlappingEndDate);

                                                  // Check Scholarship Dates in current month has
                                                  // Scholarship Business
                                                  if (isElementValidForPeriod(
                                                      scholarshipBusinessStartDate,
                                                      scholarshipBusinessEndDate,
                                                      scholarshipOverlappingStartDate,
                                                      scholarshipOverlappingEndDate)) {

                                                    BigDecimal scholarshipBusinessValue = BigDecimal.ZERO;

                                                    log.info(
                                                        "Applicable Scholarship Business Dates ");
                                                    JSONObject scholarshipBusinessPeriodJSON = getOverlapingDateRange(
                                                        scholarshipBusinessStartDate,
                                                        scholarshipBusinessEndDate,
                                                        scholarshipOverlappingStartDate,
                                                        scholarshipOverlappingEndDate);

                                                    if (scholarshipBusinessPeriodJSON != null) {
                                                      Date scholarshipBusinessOverlappingStartDate = dateFormat
                                                          .parse(scholarshipBusinessPeriodJSON
                                                              .getString("startDate"));
                                                      Date scholarshipBusinessOverlappingEndDate = dateFormat
                                                          .parse(scholarshipBusinessPeriodJSON
                                                              .getString("endDate"));
                                                      String scholarshipBusinessOverlappingDBStartDate = sa.elm.ob.utility.util.Utility
                                                          .formatDate(
                                                              scholarshipBusinessOverlappingStartDate);
                                                      String scholarshipBusinessOverlappingDBEndDate = sa.elm.ob.utility.util.Utility
                                                          .formatDate(
                                                              scholarshipBusinessOverlappingEndDate);

                                                      // Check element eligibility
                                                      EHCMEligbltyCriteria eligiblity = PayrollBaseProcessDAO
                                                          .getElementEligiblityForEmployment(
                                                              payrollElement, position, department,
                                                              job, positionGrade, gradeClass, null,
                                                              scholarshipBusinessOverlappingDBStartDate,
                                                              scholarshipBusinessOverlappingDBEndDate);

                                                      if (eligiblity != null) {
                                                        // Eligiblity Details
                                                        Date eligiblityStartDate = dateFormat.parse(
                                                            eligiblity.getStartDate().toString());
                                                        Date eligiblityEndDate = eligiblity
                                                            .getEndDate() != null
                                                                ? dateFormat.parse(eligiblity
                                                                    .getEndDate().toString())
                                                                : null;

                                                        log.info("Applicable Eligibility Days");
                                                        JSONObject eligiblityPeriodJSON = getOverlapingDateRange(
                                                            eligiblityStartDate, eligiblityEndDate,
                                                            scholarshipBusinessOverlappingStartDate,
                                                            scholarshipBusinessOverlappingEndDate);

                                                        if (eligiblityPeriodJSON != null) {
                                                          Date eligiblityOverlappingStartDate = dateFormat
                                                              .parse(eligiblityPeriodJSON
                                                                  .getString("startDate"));
                                                          Date eligiblityOverlappingEndDate = dateFormat
                                                              .parse(eligiblityPeriodJSON
                                                                  .getString("endDate"));

                                                          // Set Scholarship Based Element Value
                                                          if (scholarship.getCity()
                                                              .getEhcmScholarshipCtgy() != null) {
                                                            payRollComponents.put(
                                                                PayrollConstants.SCHOLARSHIP_COUNTRY_CTGRY,
                                                                "'" + scholarship.getCity()
                                                                    .getEhcmScholarshipCtgy()
                                                                    + "'");
                                                            payRollComponents.put(
                                                                PayrollConstants.SCHOLARSHIP_DAYS,
                                                                scholarshipDays);
                                                          } else {
                                                            errorFlagMinor = true;
                                                            errorMessage = "Scholarship Country Category is not configured for city "
                                                                + scholarship.getCity().getName();
                                                            break;
                                                          }

                                                          // Check the employee is married
                                                          if (empPerInfo.getMarialstatus()
                                                              .equalsIgnoreCase("M")) {

                                                            Date marriedDate = dateFormat
                                                                .parse(empPerInfo.getMarrieddate()
                                                                    .toString());

                                                            if (isMarriedInDuration(marriedDate,
                                                                eligiblityOverlappingStartDate,
                                                                eligiblityOverlappingEndDate)) {
                                                              // Married in current period
                                                              Date startDateBeforeMarriage = eligiblityOverlappingStartDate;
                                                              c.setTime(marriedDate);
                                                              c.add(Calendar.DAY_OF_MONTH, -1);
                                                              Date endDateBeforeMarriage = c
                                                                  .getTime();
                                                              Date startDateAfterMarriage = marriedDate;
                                                              Date endDateAfterMarriage = eligiblityOverlappingEndDate;
                                                              log.info("Married Date ==> "
                                                                  + marriedDate);
                                                              log.info(
                                                                  "startDate Before Marriage ==> "
                                                                      + startDateBeforeMarriage);
                                                              log.info(
                                                                  "EndDate Before Marriage ==> "
                                                                      + endDateBeforeMarriage);
                                                              log.info(
                                                                  "startDate After Marriage ==> "
                                                                      + startDateAfterMarriage);
                                                              log.info("endDate After Marriage ==> "
                                                                  + endDateAfterMarriage);

                                                              // Before Marriage Calculation
                                                              payRollComponents.put(
                                                                  PayrollConstants.EMP_MARRIED,
                                                                  "'N'");
                                                              payRollComponents.put(
                                                                  PayrollConstants.EMP_NOOFCHILDS,
                                                                  0);
                                                              BigDecimal scholarshipBusinessValue_BeforeMrrg = PayrollBaseProcessDAO
                                                                  .calculateElementValue(
                                                                      payrollElement, grade,
                                                                      startDateBeforeMarriage,
                                                                      endDateBeforeMarriage,
                                                                      processingDays,
                                                                      differenceDays, periodEndDate,
                                                                      false, null);

                                                              // After Marriage Calculation
                                                              int noOfChild = PayrollBaseProcessDAO
                                                                  .getNoOfChildrens(empPerInfo);

                                                              if (!errorFlagMinor) {
                                                                payRollComponents.put(
                                                                    PayrollConstants.EMP_MARRIED,
                                                                    "'Y'");
                                                                payRollComponents.put(
                                                                    PayrollConstants.EMP_NOOFCHILDS,
                                                                    noOfChild);
                                                                BigDecimal scholarshipBusinessValue_AfterMrrg = PayrollBaseProcessDAO
                                                                    .calculateElementValue(
                                                                        payrollElement, grade,
                                                                        startDateAfterMarriage,
                                                                        endDateAfterMarriage,
                                                                        processingDays,
                                                                        differenceDays,
                                                                        periodEndDate, false, null);

                                                                scholarshipBusinessValue = scholarshipBusinessValue_BeforeMrrg
                                                                    .add(
                                                                        scholarshipBusinessValue_AfterMrrg);

                                                                log.info(
                                                                    "Scholarship married in Current ==> "
                                                                        + scholarshipBusinessValue);
                                                              }

                                                            } else if (isMarriedInFuture(
                                                                marriedDate,
                                                                eligiblityOverlappingEndDate)) {
                                                              // Married in future date
                                                              payRollComponents.put(
                                                                  PayrollConstants.EMP_MARRIED,
                                                                  "'N'");
                                                              payRollComponents.put(
                                                                  PayrollConstants.EMP_NOOFCHILDS,
                                                                  0);

                                                              scholarshipBusinessValue = PayrollBaseProcessDAO
                                                                  .calculateElementValue(
                                                                      payrollElement, grade,
                                                                      eligiblityOverlappingStartDate,
                                                                      eligiblityOverlappingEndDate,
                                                                      processingDays,
                                                                      differenceDays, periodEndDate,
                                                                      false, null);

                                                              log.info(
                                                                  "Scholarship married in future ==> "
                                                                      + scholarshipBusinessValue);
                                                            } else {
                                                              // Married Already
                                                              int noOfChild = PayrollBaseProcessDAO
                                                                  .getNoOfChildrens(empPerInfo);

                                                              if (!errorFlagMinor) {
                                                                payRollComponents.put(
                                                                    PayrollConstants.EMP_MARRIED,
                                                                    "'Y'");
                                                                payRollComponents.put(
                                                                    PayrollConstants.EMP_NOOFCHILDS,
                                                                    noOfChild);

                                                                scholarshipBusinessValue = PayrollBaseProcessDAO
                                                                    .calculateElementValue(
                                                                        payrollElement, grade,
                                                                        eligiblityOverlappingStartDate,
                                                                        eligiblityOverlappingEndDate,
                                                                        processingDays,
                                                                        differenceDays,
                                                                        periodEndDate, false, null);

                                                                log.info(
                                                                    "Scholarship married already ==> "
                                                                        + scholarshipBusinessValue);
                                                              }
                                                            }

                                                          } else {
                                                            // Not Married
                                                            payRollComponents.put(
                                                                PayrollConstants.EMP_MARRIED,
                                                                "'N'");
                                                            payRollComponents.put(
                                                                PayrollConstants.EMP_NOOFCHILDS, 0);

                                                            // Calculating element value by applying
                                                            // formula
                                                            scholarshipBusinessValue = PayrollBaseProcessDAO
                                                                .calculateElementValue(
                                                                    payrollElement, grade,
                                                                    eligiblityOverlappingStartDate,
                                                                    eligiblityOverlappingEndDate,
                                                                    processingDays, differenceDays,
                                                                    periodEndDate, false, null);

                                                            log.info(
                                                                "Scholarship for unmarried ==> "
                                                                    + scholarshipBusinessValue);

                                                          }

                                                        }
                                                      }

                                                      // Add calculated value to map
                                                      if (!errorFlagMinor) {
                                                        addValueToMap(payrollElement,
                                                            BigDecimal.ZERO,
                                                            scholarshipBusinessValue, isDeduction,
                                                            "SBA", scholarship);
                                                      } else {
                                                        break;
                                                      }

                                                    }

                                                  }
                                                }

                                              } else {
                                                break;
                                              }
                                            }

                                          }

                                          if (!errorFlagMinor) {

                                            // add calculate element value
                                            addCalculatedElementValue(elementValueMap);
                                            createEarnDeductElementDetails(payrollProcessLne,
                                                earnDeduEmployee, elementValueMap, clientId, orgId,
                                                userId);

                                            calculatePayrollLineValues(elementValueMap);

                                          } else {
                                            break;
                                          }

                                        } else {
                                          break;
                                        }

                                      } else {
                                        log.info("Skipped Element ===> "
                                            + elementGrpLine.getEhcmElmttypeDef().getName());
                                        break;
                                      }

                                    } else {
                                      break;
                                    }
                                    //// ****
                                  }

                                } else {
                                  break;
                                }
                              }

                              // Update payrollProcessLne values
                              if (!errorFlagMinor) {
                                payrollProcessLne.setBasic(totalPayLineBasic);
                                payrollProcessLne.setTotalAllowance(totalPayLineAllowance);
                                payrollProcessLne.setTotalDeduction(totalPayLineDeduction);
                                payrollProcessLne.setPension(totalPayLinePension);
                                payrollProcessLne
                                    .setGrossSalary(totalPayLineBasic.add(totalPayLineAllowance));
                                payrollProcessLne
                                    .setNetSalary((totalPayLineBasic.add(totalPayLineAllowance))
                                        .subtract(totalPayLineDeduction.add(totalPayLinePension)));

                                // Set Bank Details
                                GenericPayrollDTO genericBankDTO = new GenericPayrollDTO();
                                BankDetailsDTO bankDTO = new BankDetailsDTO();
                                bankDTO.setEmployeeId(empPerInfo.getId());
                                bankDTO.setDateFrom(periodDBStartDate);
                                bankDTO.setDateTo(periodDBEndDate);
                                genericBankDTO.setBankDetails(bankDTO);
                                GenericPayrollDTO bankDetails = PayrollBaseProcessDAO
                                    .getBankDetails(genericBankDTO);
                                if (bankDetails != null) {
                                  payrollProcessLne.setBankDetails(bankDetails.getBankdetailOB());
                                }

                              } else {
                                payrollProcessLne.setBasic(BigDecimal.ZERO);
                                payrollProcessLne.setTotalAllowance(BigDecimal.ZERO);
                                payrollProcessLne.setTotalDeduction(BigDecimal.ZERO);
                                payrollProcessLne.setPension(BigDecimal.ZERO);
                                payrollProcessLne.setGrossSalary(BigDecimal.ZERO);
                                payrollProcessLne.setNetSalary(BigDecimal.ZERO);
                              }
                            }
                          }
                        }
                      } else {
                        log.info("////////////////////Salary Holded//////////////");
                        payrollProcessLne.setBasic(BigDecimal.ZERO);
                        payrollProcessLne.setTotalAllowance(BigDecimal.ZERO);
                        payrollProcessLne.setTotalDeduction(BigDecimal.ZERO);
                        payrollProcessLne.setPension(BigDecimal.ZERO);
                        payrollProcessLne.setGrossSalary(BigDecimal.ZERO);
                        payrollProcessLne.setNetSalary(BigDecimal.ZERO);
                        infoMessage = OBMessageUtils.messageBD("EHCM_SalaryHolded");
                      }
                      payrollProcessLne.setEarningAndDeductionDetails(earnDeduEmployee);
                    }
                  }

                  // If the element group is not primary and all element value is zero, skip
                  // employee
                  boolean skipZeroValueElement = false;
                  if (!errorFlagMinor && !elementGroup.isPrimary()) {
                    if (totalPayLineBasic.compareTo(BigDecimal.ZERO) == 0
                        && totalPayLineAllowance.compareTo(BigDecimal.ZERO) == 0
                        && totalPayLineDeduction.compareTo(BigDecimal.ZERO) == 0
                        && totalPayLinePension.compareTo(BigDecimal.ZERO) == 0) {
                      skipZeroValueElement = true;

                      if (earnDeduEmployee != null) {
                        boolean hasOtherProcElem = PayrollBaseProcessDAO
                            .isEarnDeductEmpHasOtherProcessElement(payrollProcessLne,
                                earnDeduEmployee);

                        if (!errorFlagMinor) {
                          // Delete inserted elements
                          int i = 1;

                          for (EHCMEarnDeductElm earnDedElm : savedElements) {

                            // List<EHCMEarnDeductElmRef> ref = earnDedElm
                            // .getEHCMEarnDeductElmRefList();
                            // for (EHCMEarnDeductElmRef elmRef : ref) {
                            // OBDal.getInstance().remove(elmRef);
                            // }
                            // earnDedElm.getEHCMEarnDeductElmRefList().removeAll(ref);
                            //
                            OBDal.getInstance().remove(earnDedElm);
                            log.info("Not Primary and Zero ==> Removed Element " + i++);
                          }

                          // Delete earning and deduction employee if ther is no element from other
                          // process
                          if (!hasOtherProcElem) {
                            OBDal.getInstance().remove(earnDeduEmployee);
                            log.info("Not Primary and Zero ==> Removed Earning and Deduction Emp");
                          }
                        }
                      }

                      // Delete inserted Payroll Lines
                      if (!errorFlagMinor) {
                        OBDal.getInstance().remove(payrollProcessLne);
                        log.info("Not Primary and Zero ==> Removed Payroll Process Line");
                      }
                    }
                  }

                  // Update Payroll Lines Status and Message
                  if (!errorFlagMinor) {
                    if (!skipZeroValueElement) {
                      payrollProcessLne.setStatus("S");
                      if (!StringUtils.isEmpty(infoMessage)) {
                        if (infoMessage.length() > 255) {
                          payrollProcessLne.setMessage(infoMessage.substring(0, 254));
                        } else {
                          payrollProcessLne.setMessage(infoMessage);
                        }
                      } else {
                        payrollProcessLne.setMessage(null);
                      }
                      log.info("Result ===> Success");

                      OBDal.getInstance().save(payrollProcessLne);
                    }
                  } else {
                    hasMinorError = true;
                    payrollProcessLne.setStatus("F");
                    if (errorMessage.length() > 255) {
                      payrollProcessLne.setMessage(errorMessage.substring(0, 254));
                    } else {
                      payrollProcessLne.setMessage(errorMessage);
                    }
                    log.info("Result ===> Failure");
                    log.info("errorMessage ===> " + errorMessage);

                    OBDal.getInstance().save(payrollProcessLne);
                  }

                } else {
                  if (errorFlagMajor) {
                    break;
                  }
                }
              } else {
                if (errorFlagMajor) {
                  break;
                }
              }

            }
          }
        }
      }

      if (errorFlagMajor) {
        OBDal.getInstance().rollbackAndClose();
        obError.setType("Error");
        obError.setTitle("Error");
        if (!StringUtils.isEmpty(errorMessage)) {
          obError.setMessage(errorMessage);
        } else {
          obError.setMessage(OBMessageUtils.messageBD("EHCM_PayrollProcess_Failed"));
        }
      } else if (hasMinorError) {
        payrollProcessHdr.setStatus("IC");
        OBDal.getInstance().save(payrollProcessHdr);
        OBDal.getInstance().flush();
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_PayProcess_EmpFailed"));
      } else {
        payrollProcessHdr.setStatus("DR");
        OBDal.getInstance().save(payrollProcessHdr);
        OBDal.getInstance().flush();
        // OBDal.getInstance().commitAndClose();
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

  public static void addValueToMap(EHCMElmttypeDef payrollElement, BigDecimal baseValue,
      BigDecimal calculatedValue, boolean isDeduction, String type, Object referenceObj) {
    try {
      if (elementValueMap.containsKey(payrollElement.getId())) {
        // Sum up similar element values
        List<EHCMEarnDeductElm> earnDedElmList = elementValueMap.get(payrollElement.getId());
        List<EHCMEarnDeductElmRef> earnDedElmRefList = new ArrayList<EHCMEarnDeductElmRef>();

        // If Multiple Entries Allows do not sum
        if (payrollElement.getType().equalsIgnoreCase("NREC")
            && payrollElement.isMultipleEntries()) {
          EHCMEarnDeductElm earnDedElm = new EHCMEarnDeductElm();
          earnDedElm.setId(SequenceIdData.getUUID());
          earnDedElm.setBaseValue(baseValue);
          earnDedElm.setCalculatedValue(calculatedValue);
          earnDedElm.setDeduction(isDeduction);
          if (absPaymentComponents.has("totalAbsPaymentValue")) {
            earnDedElm.setAbsencevalue(
                new BigDecimal(absPaymentComponents.getString("totalAbsPaymentValue")));
          }
          if (type.equals("LA")) {
            earnDedElm.setEhcmLoanTransaction((EHCMLoanTransaction) referenceObj);
          } else if (type.equals("DA")) {
            earnDedElm.setEhcmDisciplineAction((EhcmDisciplineAction) referenceObj);
          } else if (type.equals("BM")) {
            earnDedElm.setBusinessMission((EHCMEmpBusinessMission) referenceObj);
          } else if (type.equals("OT")) {
            earnDedElm.setEhcmEmpOvertime((EhcmEmployeeOvertime) referenceObj);
          } else if (type.equals("SA") || type.equals("SBA")) {
            earnDedElm.setEhcmEmpScholarship((EHCMEmpScholarship) referenceObj);
          } else if (type.equals("AD")) {
            earnDedElm.setBenefitsAndAllowance((EHCMBenefitAllowance) referenceObj);
          } else if (type.equals("TO")) {
            earnDedElm.setTicketOrders((EHCMticketordertransaction) referenceObj);
          }
          earnDedElmList.add(earnDedElm);
          elementValueMap.put(payrollElement.getId(), earnDedElmList);

          // add element refernce
          EHCMEarnDeductElmRef earnDedElmRef = new EHCMEarnDeductElmRef();
          earnDedElmRef.setElementDetails(earnDedElm);
          earnDedElmRef.setBaseValue(baseValue);
          earnDedElmRef.setCalculatedValue(calculatedValue);
          earnDedElmRef.setDeduction(isDeduction);
          if (type.equals("LA")) {
            earnDedElmRef.setEhcmLoanTransaction((EHCMLoanTransaction) referenceObj);
          } else if (type.equals("DA")) {
            earnDedElmRef.setEhcmDisciplineAction((EhcmDisciplineAction) referenceObj);
          } else if (type.equals("BM")) {
            earnDedElmRef.setBusinessMission((EHCMEmpBusinessMission) referenceObj);
          } else if (type.equals("OT")) {
            earnDedElmRef.setOvertimeTransaction((EhcmEmployeeOvertime) referenceObj);
          } else if (type.equals("SA") || type.equals("SBA")) {
            earnDedElmRef.setEhcmEmpScholarship((EHCMEmpScholarship) referenceObj);
          } else if (type.equals("AD")) {
            earnDedElmRef.setBenefitsAndAllowance((EHCMBenefitAllowance) referenceObj);
          } else if (type.equals("TO")) {
            earnDedElmRef.setTicketOrders((EHCMticketordertransaction) referenceObj);
          }

          earnDedElmRefList.add(earnDedElmRef);
          elementRefMap.put(earnDedElm.getId(), earnDedElmRefList);

        } else {
          EHCMEarnDeductElm earnDedElm = earnDedElmList.get(0);
          earnDedElm.setBaseValue(baseValue);
          BigDecimal elementValue = earnDedElm.getCalculatedValue();
          earnDedElm.setCalculatedValue(elementValue.add(calculatedValue));
          if (absPaymentComponents.has("totalAbsPaymentValue")) {
            earnDedElm.setAbsencevalue(earnDedElm.getAbsencevalue()
                .add(new BigDecimal(absPaymentComponents.getString("totalAbsPaymentValue"))));
          }
          earnDedElm.setDeduction(isDeduction);
          if (type.equals("LA")) {
            earnDedElm.setEhcmLoanTransaction(null);
          } else if (type.equals("DA")) {
            earnDedElm.setEhcmDisciplineAction(null);
          } else if (type.equals("BM")) {
            earnDedElm.setBusinessMission(null);
          } else if (type.equals("OT")) {
            earnDedElm.setEhcmEmpOvertime(null);
          } else if (type.equals("SA") || type.equals("SBA")) {
            earnDedElm.setEhcmEmpScholarship(null);
          } else if (type.equals("AD")) {
            earnDedElm.setBenefitsAndAllowance(null);
          } else if (type.equals("TO")) {
            earnDedElm.setTicketOrders(null);
          }
          earnDedElmList.set(0, earnDedElm);
          elementValueMap.put(payrollElement.getId(), earnDedElmList);

          // add element refernce
          EHCMEarnDeductElmRef earnDedElmRef = new EHCMEarnDeductElmRef();
          earnDedElmRef.setElementDetails(earnDedElm);
          earnDedElmRef.setBaseValue(baseValue);
          earnDedElmRef.setCalculatedValue(calculatedValue);
          earnDedElmRef.setDeduction(isDeduction);
          if (type.equals("LA")) {
            earnDedElmRef.setEhcmLoanTransaction((EHCMLoanTransaction) referenceObj);
          } else if (type.equals("DA")) {
            earnDedElmRef.setEhcmDisciplineAction((EhcmDisciplineAction) referenceObj);
          } else if (type.equals("BM")) {
            earnDedElmRef.setBusinessMission((EHCMEmpBusinessMission) referenceObj);
          } else if (type.equals("OT")) {
            earnDedElmRef.setOvertimeTransaction((EhcmEmployeeOvertime) referenceObj);
          } else if (type.equals("SA") || type.equals("SBA")) {
            earnDedElmRef.setEhcmEmpScholarship((EHCMEmpScholarship) referenceObj);
          } else if (type.equals("AD")) {
            earnDedElmRef.setBenefitsAndAllowance((EHCMBenefitAllowance) referenceObj);
          } else if (type.equals("TO")) {
            earnDedElmRef.setTicketOrders((EHCMticketordertransaction) referenceObj);
          }
          earnDedElmRefList = elementRefMap.get(earnDedElm.getId());
          earnDedElmRefList.add(earnDedElmRef);

        }
      } else {
        List<EHCMEarnDeductElm> earnDedElmList = new ArrayList<EHCMEarnDeductElm>();
        EHCMEarnDeductElm earnDedElm = new EHCMEarnDeductElm();
        earnDedElm.setId(SequenceIdData.getUUID());
        earnDedElm.setBaseValue(baseValue);
        earnDedElm.setCalculatedValue(calculatedValue);
        earnDedElm.setDeduction(isDeduction);
        if (absPaymentComponents.has("totalAbsPaymentValue")) {
          earnDedElm.setAbsencevalue(
              new BigDecimal(absPaymentComponents.getString("totalAbsPaymentValue")));
        }
        if (type.equals("LA")) {
          earnDedElm.setEhcmLoanTransaction((EHCMLoanTransaction) referenceObj);
        } else if (type.equals("DA")) {
          earnDedElm.setEhcmDisciplineAction((EhcmDisciplineAction) referenceObj);
        } else if (type.equals("BM")) {
          earnDedElm.setBusinessMission((EHCMEmpBusinessMission) referenceObj);
        } else if (type.equals("OT")) {
          earnDedElm.setEhcmEmpOvertime((EhcmEmployeeOvertime) referenceObj);
        } else if (type.equals("SA") || type.equals("SBA")) {
          earnDedElm.setEhcmEmpScholarship((EHCMEmpScholarship) referenceObj);
        } else if (type.equals("AD")) {
          earnDedElm.setBenefitsAndAllowance((EHCMBenefitAllowance) referenceObj);
        } else if (type.equals("TO")) {
          earnDedElm.setTicketOrders((EHCMticketordertransaction) referenceObj);
        }
        earnDedElmList.add(earnDedElm);
        elementValueMap.put(payrollElement.getId(), earnDedElmList);

        // add element refernce
        List<EHCMEarnDeductElmRef> earnDedElmRefList = new ArrayList<EHCMEarnDeductElmRef>();
        EHCMEarnDeductElmRef earnDedElmRef = new EHCMEarnDeductElmRef();
        earnDedElmRef.setElementDetails(earnDedElm);
        earnDedElmRef.setBaseValue(baseValue);
        earnDedElmRef.setCalculatedValue(calculatedValue);
        earnDedElmRef.setDeduction(isDeduction);
        if (type.equals("LA")) {
          earnDedElmRef.setEhcmLoanTransaction((EHCMLoanTransaction) referenceObj);
        } else if (type.equals("DA")) {
          earnDedElmRef.setEhcmDisciplineAction((EhcmDisciplineAction) referenceObj);
        } else if (type.equals("BM")) {
          earnDedElmRef.setBusinessMission((EHCMEmpBusinessMission) referenceObj);
        } else if (type.equals("OT")) {
          earnDedElmRef.setOvertimeTransaction((EhcmEmployeeOvertime) referenceObj);
        } else if (type.equals("SA") || type.equals("SBA")) {
          earnDedElmRef.setEhcmEmpScholarship((EHCMEmpScholarship) referenceObj);
        } else if (type.equals("AD")) {
          earnDedElmRef.setBenefitsAndAllowance((EHCMBenefitAllowance) referenceObj);
        } else if (type.equals("TO")) {
          earnDedElmRef.setTicketOrders((EHCMticketordertransaction) referenceObj);
        }
        earnDedElmRefList.add(earnDedElmRef);
        elementRefMap.put(earnDedElm.getId(), earnDedElmRefList);

      }
    } catch (Exception cebv) {
      log.error("Error in PayrollProcess.java : addValueToMap() ");
      cebv.printStackTrace();
      errorFlagMinor = true;
      errorMessage = "Error while adding Value To Map";
    }
  }

  public static void generateEmployeeBasedPreDefElements(EhcmEmpPerInfo empPerInfo) {
    try {
      // Employee Country
      payRollComponents.put("EMP_COUNTRY", "'" + empPerInfo.getCountry().getISOCountryCode() + "'");

      // Employee Category
      payRollComponents.put("EMP_CATEGORY", "'" + empPerInfo.getGradeClass().getSearchKey() + "'");

      // Employee Hire Date
      payRollComponents.put("EMP_HIREDATE",
          "new Date('" + empPerInfo.getHiredate() + "').toDateString()");

    } catch (JSONException gce) {
      log.error("Error in PayrollProcess.java : generateEmployeeBasedPreDefElements() ");
      gce.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while generating Employee Based Pre Defined Element for Employee"
          + empPerInfo.getName();
    }
  }

  private BigDecimal calculateEmploymentBasedValue(EHCMElmttypeDef payrollElement,
      EmploymentInfo employment, Date startDate, Date endDate, BigDecimal processingDays,
      BigDecimal differenceDays, Date payrollEndDate, boolean isBaseCalculation,
      boolean isfullValueCalculation) {
    BigDecimal elementCalculatedValue = BigDecimal.ZERO;
    try {
      // Generate Employment Based Pre Defined Elements

      generateEmploymentBasedPreDefElements(employment, startDate, endDate, processingDays,
          differenceDays, payrollEndDate, isBaseCalculation, isfullValueCalculation,
          payrollElement);

      if (!errorFlagMinor) {
        // Generate Element Based Pre Defined Element
        generateElementBasedPreDefElements(payrollElement, employment.getEmploymentgrade(),
            startDate, endDate, processingDays, differenceDays, payrollEndDate, isBaseCalculation,
            isfullValueCalculation, employment);

        if (!errorFlagMinor) {
          // Calculating element value by applying formula
          elementCalculatedValue = PayrollBaseProcessDAO.calculateElementValue(payrollElement,
              employment.getEmploymentgrade(), startDate, endDate, processingDays, differenceDays,
              payrollEndDate, isBaseCalculation, null);
        }
      }

      return elementCalculatedValue;
    } catch (Exception cebv) {
      log.error("Error in PayrollProcess.java : calculateEmploymentBasedValue() ");
      cebv.printStackTrace();
      errorFlagMinor = true;
      errorMessage = "Error while calculating Employment Based Value";
      return elementCalculatedValue;
    }
  }

  private void generateEmploymentBasedPreDefElements(EmploymentInfo employment, Date startDate,
      Date endDate, BigDecimal processingDays, BigDecimal differenceDays, Date payrollEndDate,
      boolean isBaseCalculation, boolean isfullValueCalculation, EHCMElmttypeDef payrollElement) {
    BigDecimal totalAbsPaymentValue = BigDecimal.ZERO;
    BigDecimal totalScholarshipDeductionValue = BigDecimal.ZERO;
    BigDecimal totalSuspensionDeductionValue = BigDecimal.ZERO;
    BigDecimal suspensionDeduction = BigDecimal.ZERO;
    BigDecimal scholarshipDeduction = BigDecimal.ZERO;
    try {
      // Employee Grade
      payRollComponents.put("EMP_GRADE",
          "'" + employment.getEmploymentgrade().getSearchKey() + "'");

      // Pay Scale
      BigDecimal payscale;
      if (isBaseCalculation) {
        payscale = PayrollBaseProcessDAO.getLatestPayScaleValueInEmployment(employment, startDate,
            endDate);
      } else {
        payscale = PayrollBaseProcessDAO.getPayScaleValue(employment, startDate, endDate,
            processingDays, differenceDays, payrollEndDate);

        log.info("payscale ==> " + payscale);

        // Get Payscale list applicable in the duration
        List<ehcmpayscaleline> payscaleList = PayrollBaseProcessDAO
            .getApplicablePayScaleList(employment, startDate, endDate, processingDays);

        // absence calculation for payscale
        if (employment != null && payrollElement.getBaseProcess().equals("E")
            && payrollElement.getElementSource().equals("PS")) {
          totalAbsPaymentValue = PayrollBaseProcessDAO.calculateAbsencePaymentValue(employment,
              startDate, endDate, processingDays, differenceDays, payrollEndDate, false, null,
              payrollElement);
          log.info("totalAbsPaymentValue ==> " + totalAbsPaymentValue);
        }

        // Scholarship Deduction Amount Calculation
        EHCMScholarshipDedConf deduConfig = PayrollBaseProcessDAO.getScholarshipDedConfig();
        if (deduConfig != null) {
          EHCMElmttypeDef dedElement = deduConfig.getPayrollElement();
          BigDecimal minScholdays = new BigDecimal(deduConfig.getMinimumScholarshipDays());
          BigDecimal scholDedPercentage = deduConfig.getDeductionPercentage();
          if (dedElement.getId().equalsIgnoreCase(payrollElement.getId())) {
            for (ehcmpayscaleline payscaleObj : payscaleList) {
              BigDecimal suspensionValue = PayrollBaseProcessDAO.calculateScholarshipDeductionValue(
                  employment, payscaleObj.getStartDate(), payscaleObj.getEndDate(),
                  payscaleObj.getAmount(), minScholdays);
              totalScholarshipDeductionValue = totalScholarshipDeductionValue.add(suspensionValue);
              log.info("totalScholarshipDeductionValue ==> " + totalScholarshipDeductionValue);
            }

            // Apply Percentage accoding to config
            if (totalScholarshipDeductionValue.compareTo(BigDecimal.ZERO) > 0) {
              BigDecimal scholarshipDedPercent = scholDedPercentage.divide(new BigDecimal("100"));
              scholarshipDeduction = totalScholarshipDeductionValue.multiply(scholarshipDedPercent);
            }
          }
        }

        // Suspension Deduction
        if (payrollElement.getBaseProcess().equals("E")
            && payrollElement.getElementClassification().equalsIgnoreCase("ER")
            && payrollElement.getElementSource().equals("PS")) {
          for (ehcmpayscaleline payscaleObj : payscaleList) {
            BigDecimal suspensionValue = PayrollBaseProcessDAO.calculateSuspensionValue(employment,
                payscaleObj.getStartDate(), payscaleObj.getEndDate(), payscaleObj.getAmount());
            totalSuspensionDeductionValue = totalSuspensionDeductionValue.add(suspensionValue);
            log.info("totalSuspensionDeductionValue ==> " + totalSuspensionDeductionValue);
          }

          if (totalSuspensionDeductionValue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal suspensionDedPercent = new BigDecimal("50").divide(new BigDecimal("100"));
            suspensionDeduction = totalSuspensionDeductionValue.multiply(suspensionDedPercent);
          }
        }

      }

      if (!isfullValueCalculation) {
        log.info("totalAbsPaymentValue " + totalAbsPaymentValue);
        log.info("scholarshipDeduction " + scholarshipDeduction);
        log.info("suspensionDeduction " + suspensionDeduction);
        BigDecimal totaldeductions = totalAbsPaymentValue.add(scholarshipDeduction)
            .add(suspensionDeduction);
        log.info("totaldeductions " + totaldeductions);
        BigDecimal payScaleAfterDeduction = payscale.subtract(totaldeductions);
        log.info("payScaleAfterDeduction ==> " + payScaleAfterDeduction);
        payRollComponents.put("PAYSALGS", payScaleAfterDeduction);
      } else {
        payRollComponents.put("PAYSALGS", payscale);
      }
    } catch (JSONException gce) {
      log.error("Error in PayrollProcess.java : generateEmploymentBasedPreDefElements() ");
      gce.printStackTrace();
      errorFlagMinor = true;
      errorMessage = "Error while generating Employment Based Pre Defined Element for employment Grade"
          + employment.getEmploymentgrade().getSearchKey();
    }
  }

  private void generateElementBasedPreDefElements(EHCMElmttypeDef typDef, ehcmgrade grade,
      Date startDate, Date endDate, BigDecimal processingDays, BigDecimal differenceDays,
      Date payrollEndDate, boolean isBaseCalculation, boolean isfullValueCalculation,
      EmploymentInfo employment) {
    BigDecimal totalAbsPaymentValue = BigDecimal.ZERO;
    BigDecimal totalSuspensionDeductionValue = BigDecimal.ZERO;
    BigDecimal suspensionDeduction = BigDecimal.ZERO;
    try {
      // Grade Rate
      if (typDef.getElementSource() != null && typDef.getElementSource().equalsIgnoreCase("GR")) {
        // Fetching grade rate value based on grade rate in element definition
        BigDecimal gradeRate;
        if (isBaseCalculation) {
          gradeRate = PayrollBaseProcessDAO.getLatestGradeRateValue(typDef, typDef.getGradeRate(),
              grade, startDate, endDate, processingDays);
        } else {
          gradeRate = PayrollBaseProcessDAO.getGradeRateValue(typDef, typDef.getGradeRate(), grade,
              startDate, endDate, processingDays, differenceDays, payrollEndDate);

          // Get Graderate list applicable in the duration
          List<ehcmgraderatelines> gradeRateList = PayrollBaseProcessDAO.getApplicableGradeRateList(
              typDef, typDef.getGradeRate(), grade, startDate, endDate, processingDays);

          // absence calculation for grade rates
          if (employment != null && typDef.getBaseProcess().equals("E")
              && typDef.getElementSource().equals("GR")) {
            totalAbsPaymentValue = PayrollBaseProcessDAO.calculateAbsencePaymentValueForGradeRates(
                typDef, typDef.getGradeRate(), employment, grade, startDate, endDate,
                processingDays, differenceDays, payrollEndDate, isBaseCalculation);
          }

          if (typDef.getBaseProcess().equals("E")
              && typDef.getElementClassification().equalsIgnoreCase("ER")
              && typDef.getElementSource().equals("GR")) {
            for (ehcmgraderatelines gradeRateObj : gradeRateList) {
              BigDecimal suspensionValue = PayrollBaseProcessDAO.calculateSuspensionValue(
                  employment, gradeRateObj.getStartDate(), gradeRateObj.getEndDate(),
                  gradeRateObj.getSearchKey());
              totalSuspensionDeductionValue = totalSuspensionDeductionValue.add(suspensionValue);
              log.info("totalSuspensionDeductionValue ==> " + totalSuspensionDeductionValue);
            }

            if (totalSuspensionDeductionValue.compareTo(BigDecimal.ZERO) > 0) {
              BigDecimal suspensionDedPercent = new BigDecimal("50").divide(new BigDecimal("100"));
              suspensionDeduction = totalSuspensionDeductionValue.multiply(suspensionDedPercent);
            }
          }

        }
        if (!isfullValueCalculation) {
          BigDecimal totalGradeRateDeductions = totalAbsPaymentValue.add(suspensionDeduction);
          payRollComponents.put("GRADERATE", gradeRate.subtract(totalGradeRateDeductions));
        } else {
          payRollComponents.put("GRADERATE", gradeRate);
        }
      }
    } catch (Exception gese) {
      log.error("Error in PayrollProcess.java : generateElementBasedPreDefElements() ");
      gese.printStackTrace();
      errorFlagMinor = true;
      errorMessage = "Error while generating Element Based Value for Pre Defined Element"
          + typDef.getName();
    }
  }

  private void generateBMBasedPreDefElements(EHCMEmpBusinessMission bMission) {
    try {
      // Business Mission Type
      payRollComponents.put(PayrollConstants.ELEMENT_BUSINESS_MISSION_TYPE_CODE,
          "'" + bMission.getMissionType().getSearchKey() + "'");

      // Business Mission House Provided
      payRollComponents.put(PayrollConstants.ELEMENT_BUSINESS_MISSION_HOUSING_CODE,
          "'" + (bMission.isHousingProvided() ? "Y" : "N") + "'");

      // Business Mission Food Provided
      payRollComponents.put(PayrollConstants.ELEMENT_BUSINESS_MISSION_FOOD_CODE,
          "'" + (bMission.isFoodProvided() ? "Y" : "N") + "'");

      // Business Mission Country Category (CA, CB, CC)
      if (bMission.getToCity().getEhcmCategory() != null) {
        payRollComponents.put("BM_COUNTRYCAT", "'" + bMission.getToCity().getEhcmCategory() + "'");
      } else {
        errorFlagMinor = true;
        errorMessage = "Category is not configured for city " + bMission.getToCity().getName();
      }

      // Business Mission Days
      payRollComponents.put(PayrollConstants.ELEMENT_BUSINESS_MISSION_DAYS,
          bMission.getMissionDays());

      // Business Mission Days Before
      payRollComponents.put(PayrollConstants.ELEMENT_BUSINESS_MISSION_DAYS_BEFORE,
          bMission.getNoofdaysBefore());

      // Business Mission Days After
      payRollComponents.put(PayrollConstants.ELEMENT_BUSINESS_MISSION_DAYS_AFTER,
          bMission.getNoofdaysAfter());

      // Business Mission Payment Amount
      payRollComponents.put(PayrollConstants.ELEMENT_BUSINESS_MISSION_PAYMENT_AMT,
          bMission.getPaymentAmt());

      // Business Mission Advance Amount
      payRollComponents.put(PayrollConstants.ELEMENT_BUSINESS_MISSION_ADVANCE_AMT,
          bMission.getAdvanceAmount());

    } catch (JSONException gce) {
      log.error("Error in PayrollBaseProcess.java : generateBMBasedPreDefElements() ");
      gce.printStackTrace();
      errorFlagMinor = true;
      errorMessage = "Error while generating BM Based Pre Defined Element";
    }
  }

  private void removeEmploymentBasedPreDefElements() {
    try {
      // Employee Grade
      payRollComponents.remove("EMP_GRADE");
      // Pay Scale
      payRollComponents.remove("PAYSALGS");
      // Full Basic Value
      payRollComponents.remove("FULL_BASIC_VALUE");

    } catch (Exception rebpe) {
      log.error("Error in PayrollBaseProcess.java : removeEmploymentBasedPreDefElements() ");
      rebpe.printStackTrace();
      errorFlagMinor = true;
      errorMessage = "Error while removing Employment Based Pre Defined Element";
    }
  }

  private void removeBMBasedPreDefElements() {
    try {

      // Business Mission Type
      payRollComponents.remove(PayrollConstants.ELEMENT_BUSINESS_MISSION_TYPE_CODE);

      // Business Mission House Provided
      payRollComponents.remove(PayrollConstants.ELEMENT_BUSINESS_MISSION_HOUSING_CODE);

      // Business Mission Food Provided
      payRollComponents.remove(PayrollConstants.ELEMENT_BUSINESS_MISSION_FOOD_CODE);

      // Business Mission Country Category (CA, CB, CC)
      payRollComponents.remove("BM_COUNTRYCAT");

      // Business Mission Days
      payRollComponents.remove(PayrollConstants.ELEMENT_BUSINESS_MISSION_DAYS);

      // Business Mission Advance
      payRollComponents.remove(PayrollConstants.ELEMENT_BUSINESS_MISSION_DAYS_BEFORE);

      // Business Mission Days After
      payRollComponents.remove(PayrollConstants.ELEMENT_BUSINESS_MISSION_DAYS_AFTER);

      // Business Mission Payment Amount
      payRollComponents.remove(PayrollConstants.ELEMENT_BUSINESS_MISSION_PAYMENT_AMT);

      // Business Mission Advance Amount
      payRollComponents.remove(PayrollConstants.ELEMENT_BUSINESS_MISSION_ADVANCE_AMT);

    } catch (Exception rbme) {
      log.error("Error in PayrollBaseProcess.java : removeBMBasedPreDefElements() ");
      rbme.printStackTrace();
      errorFlagMinor = true;
      errorMessage = "Error while removing BM Based Pre-Defined Element";
    }
  }

  public static JSONObject getOverlapingDateRange(Date range1StartDate, Date range1EndDate,
      Date range2StartDate, Date range2endDate) {
    try {
      JSONObject result = new JSONObject();

      Date startdate1 = range1StartDate;
      Date enddate1 = range1EndDate != null ? range1EndDate : range2endDate;

      Date overlappingStartDate = getApplicableStartDate(startdate1, range2StartDate);
      Date overlappingEndDate = getApplicableEndDate(enddate1, range2endDate);

      long diffMillSeconds = Math
          .abs(overlappingStartDate.getTime() - overlappingEndDate.getTime());
      long overlappingDays = (diffMillSeconds / (PayrollConstants.HOURS * PayrollConstants.MINUTES
          * PayrollConstants.SECONDS * PayrollConstants.MILLISECONDS)) + 1;

      result.put("startDate", PayrollConstants.dateFormat.format(overlappingStartDate));
      result.put("endDate", PayrollConstants.dateFormat.format(overlappingEndDate));
      result.put("days", overlappingDays);

      log.info("startDate ===> " + result.getString("startDate"));
      log.info("endDate ===> " + result.getString("endDate"));
      log.info("days ===> " + result.getString("days"));

      return result;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getOverlapingDateRange() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while calculating number of days in 2 ranges ";
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

  private void createEarnDeductElementDetails(EHCMPayrollProcessLne payrollProcessLne,
      EHCMEarnDeductEmp earnDeduEmployee, HashMap<String, List<EHCMEarnDeductElm>> elementValueMap,
      String clientId, String orgId, String userId) {
    try {
      for (Map.Entry<String, List<EHCMEarnDeductElm>> elementsToSave : elementValueMap.entrySet()) {
        EHCMElmttypeDef payrollElement = OBDal.getInstance().get(EHCMElmttypeDef.class,
            elementsToSave.getKey());

        if (payrollElement != null) {
          List<EHCMEarnDeductElm> earnDedElmList = elementsToSave.getValue();
          for (EHCMEarnDeductElm earnDedElm : earnDedElmList) {
            EHCMEarnDeductElm insertedElm = PayrollBaseProcessDAO.insertEarnDeductElementDetails(
                earnDeduEmployee, payrollElement, earnDedElm, payrollProcessLne, clientId, orgId,
                userId);
            savedElements.add(insertedElm);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : createEarnDeductElementDetails() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while creating Earning and Deduction Element Details";
    }
  }

  private void calculatePayrollLineValues(
      HashMap<String, List<EHCMEarnDeductElm>> elementValueMap) {
    try {
      for (Map.Entry<String, List<EHCMEarnDeductElm>> elementsToSave : elementValueMap.entrySet()) {
        List<EHCMEarnDeductElm> earnDedElmList = elementsToSave.getValue();
        for (EHCMEarnDeductElm earnDedElm : earnDedElmList) {
          BigDecimal value = earnDedElm.getCalculatedValue();
          EHCMElmttypeDef payrollElement = OBDal.getInstance().get(EHCMElmttypeDef.class,
              elementsToSave.getKey());
          if (payrollElement != null) {
            String elementType = payrollElement.getEhcmElementCatgry().getType();
            if (elementType.equalsIgnoreCase(PayrollConstants.BASIC_ELEMENTCTGRY_TYPE)) {

              totalPayLineBasic = totalPayLineBasic.add(value);
            } else if (elementType.equalsIgnoreCase(PayrollConstants.ALLOWANCE_ELEMENTCTGRY_TYPE)
                || elementType.equalsIgnoreCase(PayrollConstants.OTHER_ELEMENTCTGRY_TYPE)) {
              totalPayLineAllowance = totalPayLineAllowance.add(value);
            } else if (elementType.equalsIgnoreCase(PayrollConstants.DEDUCTION_ELEMENTCTGRY_TYPE)) {
              totalPayLineDeduction = totalPayLineDeduction.add(value);
            } else if (elementType.equalsIgnoreCase(PayrollConstants.PENSION_ELEMENTCTGRY_TYPE)) {
              totalPayLinePension = totalPayLinePension.add(value);
              totalPayLineDeduction = totalPayLineDeduction.add(value);
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : calculatePayrollLineValues() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while creating Earning and Deduction Element Details";
    }
  }

  /**
   * Add calculated value in json.
   * 
   * @param elementValueMap
   */
  private void addCalculatedElementValue(HashMap<String, List<EHCMEarnDeductElm>> elementValueMap) {
    try {
      for (Map.Entry<String, List<EHCMEarnDeductElm>> elementsToSave : elementValueMap.entrySet()) {
        EHCMElmttypeDef payrollElement = OBDal.getInstance().get(EHCMElmttypeDef.class,
            elementsToSave.getKey());
        if (payrollElement.getType().equals("REC")) {
          List<EHCMEarnDeductElm> earnDedElmList = elementsToSave.getValue();
          calculatedElementValue.put("E_" + payrollElement.getCode(),
              earnDedElmList.get(0).getCalculatedValue());
        }
      }
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : addCalculatedElementValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while adding element value";
    }
  }

  private boolean isElementValidForPeriod(Date elementStartDate, Date elementEndDate,
      Date periodStartDate, Date periodEndDate) throws ParseException {
    Date calcElementEndDate = null;
    if (elementEndDate == null) {
      calcElementEndDate = dateFormat.parse("2058-06-21");
    } else {
      calcElementEndDate = elementEndDate;
    }

    if ((elementStartDate.compareTo(periodStartDate) >= 0
        && calcElementEndDate.compareTo(periodEndDate) <= 0)
        || (calcElementEndDate.compareTo(periodStartDate) >= 0
            && elementStartDate.compareTo(periodEndDate) <= 0)) {
      return true;
    } else {
      return false;
    }
  }

  private boolean isMarriedInDuration(Date marriedDate, Date startDate, Date endDate) {
    boolean marriedInDuration = false;
    if (marriedDate.compareTo(startDate) > 0 && marriedDate.compareTo(endDate) <= 0) {
      marriedInDuration = true;
    }
    return marriedInDuration;
  }

  private boolean isMarriedInFuture(Date marriedDate, Date endDate) {
    boolean marriedInFuture = false;
    if (marriedDate.compareTo(endDate) > 0) {
      marriedInFuture = true;
    }
    return marriedInFuture;
  }
}
