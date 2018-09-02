package sa.elm.ob.hcm.ad_process.empScholarshipTraining;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.geography.City;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import sa.elm.ob.hcm.EHCMElmttypeDef;
import sa.elm.ob.hcm.EHCMEmpScholarship;
import sa.elm.ob.hcm.EhcmPayrollGlobalValue;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ehcmgradeclass;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.Payroll.PayrollBaseProcessDAO;
import sa.elm.ob.hcm.util.PayrollConstants;
import sa.elm.ob.hcm.util.payroll.PayrollUtility;

public class EmpScholarshipPayment implements Process {
  private static final Logger log4j = LoggerFactory.getLogger(EmpScholarshipPayment.class);
  private final OBError obError = new OBError();
  Boolean errorFlag;
  public static String errorMessage;
  private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  static JSONObject payRollComponents = new JSONObject();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    final String empScholarShipId = bundle.getParams().get("Ehcm_Emp_Scholarship_ID").toString();
    final String clientId = (String) bundle.getContext().getClient();
    EHCMEmpScholarship empScholarshipObj = OBDal.getInstance().get(EHCMEmpScholarship.class,
        empScholarShipId);
    String decisionType = empScholarshipObj.getDecisionType();

    try {
      OBContext.setAdminMode();

      errorFlag = false;

      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_SCHOLARSHIP_PAYMENT)) {

        BigDecimal scholarshipReward = calculatePayment(clientId, empScholarshipObj);

        empScholarshipObj.setUpdated(new java.util.Date());
        empScholarshipObj.setPaymentAmount(scholarshipReward.setScale(2, BigDecimal.ROUND_HALF_UP));

        // Calculate Advance and Net Amount
        BigDecimal advPercentage = empScholarshipObj.getAdvancePercentage();
        BigDecimal advAmount = empScholarshipObj.getAdvanceAmount();
        if (advPercentage != null && advPercentage.compareTo(BigDecimal.ZERO) > 0) {
          advAmount = scholarshipReward.multiply(advPercentage.divide(new BigDecimal(100)));
          empScholarshipObj.setAdvanceAmount(advAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
          empScholarshipObj.setNETAmt(
              scholarshipReward.subtract(advAmount).setScale(2, BigDecimal.ROUND_HALF_UP));
        } else if (advAmount != null && advAmount.compareTo(BigDecimal.ZERO) > 0) {
          empScholarshipObj.setNETAmt(
              scholarshipReward.subtract(advAmount).setScale(2, BigDecimal.ROUND_HALF_UP));
        } else {
          empScholarshipObj.setNETAmt(scholarshipReward.setScale(2, BigDecimal.ROUND_HALF_UP));
        }

        OBDal.getInstance().save(empScholarshipObj);

      }

      if (!errorFlag) {
        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_Scholarship_Payment_Suc"));
      } else if (errorFlag) {
        obError.setType("Error");
        obError.setTitle("Error");
        String message = (!StringUtils.isEmpty(errorMessage)) ? errorMessage
            : OBMessageUtils.messageBD("EHCM_Scholarship_Payment_Err");
        obError.setMessage(message);
      }
      bundle.setResult(obError);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      bundle.setResult(obError);
      OBDal.getInstance().rollbackAndClose();
      if (log4j.isErrorEnabled()) {
        log4j.error("exception :", e);
      }
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  public BigDecimal calculatePayment(String clientId, EHCMEmpScholarship empScholarshipObj) {
    BigDecimal scholarshipValue = BigDecimal.ZERO;
    try {
      // Get Scholarship Allowance Element
      OBQuery<EHCMElmttypeDef> elementDefQry = OBDal.getInstance()
          .createQuery(EHCMElmttypeDef.class, " baseProcess='SA' order by creationDate desc ");
      elementDefQry.setMaxResult(1);
      List<EHCMElmttypeDef> elementDefList = elementDefQry.list();

      if (elementDefList.size() > 0) {
        EHCMElmttypeDef scholarshipElement = elementDefList.get(0);

        // Scholarship Details
        City city = empScholarshipObj.getCity();
        EhcmPosition position = empScholarshipObj.getPosition();
        Organization department = empScholarshipObj.getPosition().getDepartment();
        Jobs job = empScholarshipObj.getPosition().getEhcmJobs();
        ehcmgrade positionGrade = empScholarshipObj.getPosition().getGrade();
        ehcmgradeclass gradeClass = empScholarshipObj.getPosition().getGrade().getEhcmGradeclass();
        ehcmgrade grade = empScholarshipObj.getEmploymentGrade();
        Date payrollStartDate = empScholarshipObj.getPayrollPeriod().getStartDate();
        Date payrollEndDate = empScholarshipObj.getPayrollPeriod().getEndDate();
        Date scholarshipPayPeriodStartDate = dateFormat.parse(payrollStartDate.toString());
        String scholarshipPayPeriodDBStartDate = sa.elm.ob.utility.util.Utility
            .formatDate(scholarshipPayPeriodStartDate);

        // Element Eligibility based on Payment Period Start Date
        boolean isEligible = PayrollBaseProcessDAO.checkElementEligibleForEmployment(
            scholarshipElement, position, department, job, positionGrade, gradeClass, null,
            scholarshipPayPeriodDBStartDate);

        if (isEligible) {
          if (!StringUtils.isEmpty(city.getEhcmScholarshipCtgy())) {

            // Get Global Values
            List<EhcmPayrollGlobalValue> globalValueList = PayrollBaseProcessDAO
                .getGlobalValues(clientId);
            for (EhcmPayrollGlobalValue globalValue : globalValueList) {
              if (globalValue.getType().equalsIgnoreCase("C")) {
                payRollComponents.put(globalValue.getCode(),
                    "'" + globalValue.getCharValue() + "'");
              } else if (globalValue.getType().equalsIgnoreCase("D")) {
                payRollComponents.put(globalValue.getCode(),
                    "new Date('" + globalValue.getDateValue() + "')");
              } else {
                payRollComponents.put(globalValue.getCode(), globalValue.getNumericValue());
              }
            }

            // Get Global Scholarship Days Value or Set Default Value 30
            BigDecimal scholarshipAllowanceDays = new BigDecimal("30");
            if (payRollComponents.has(PayrollConstants.GLOBAL_SCHOLARSHIP_REWARD_DAYS)
                && !StringUtils.isEmpty(
                    payRollComponents.getString(PayrollConstants.GLOBAL_SCHOLARSHIP_REWARD_DAYS))) {
              String scholarshipAllowanceDays_Str = payRollComponents
                  .getString(PayrollConstants.GLOBAL_SCHOLARSHIP_REWARD_DAYS);
              scholarshipAllowanceDays = new BigDecimal(scholarshipAllowanceDays_Str);
            }
            log4j.info("scholarshipAllowanceDays ==> " + scholarshipAllowanceDays);

            // Calculate Start Date and End Dates
            Date scholarshipAllowanceStartDate = dateFormat
                .parse(empScholarshipObj.getStartDate().toString());
            Date scholarshipEndDate = dateFormat.parse(empScholarshipObj.getEndDate().toString());
            Calendar c = Calendar.getInstance();
            c.setTime(scholarshipAllowanceStartDate);
            c.add(Calendar.DAY_OF_MONTH, (scholarshipAllowanceDays.intValue() - 1));
            Date scholarshipAllowanceEndDate = c.getTime();
            if (scholarshipEndDate.compareTo(scholarshipAllowanceEndDate) < 0) {
              scholarshipAllowanceEndDate = scholarshipEndDate;
            }
            log4j.info("scholarshipAllowanceStartDate ==> " + scholarshipAllowanceStartDate);
            log4j.info("scholarshipAllowanceEndDate ==> " + scholarshipAllowanceEndDate);
            log4j.info("ScholarshipCtgy ==> " + city.getEhcmScholarshipCtgy());

            // Calculate Scholarship Based Elements
            payRollComponents.put("ST_Country_Category", "'" + city.getEhcmScholarshipCtgy() + "'");

            // Processing Days Calculation
            Date periodStartDate = dateFormat.parse(payrollStartDate.toString());
            Date periodEndDate = payrollEndDate != null
                ? dateFormat.parse(payrollEndDate.toString())
                : null;
            long payRollDefDays = 30;
            if (periodStartDate != null && periodEndDate != null) {
              long diffMillSeconds = Math.abs(periodStartDate.getTime() - periodEndDate.getTime());
              payRollDefDays = (diffMillSeconds / (PayrollConstants.HOURS * PayrollConstants.MINUTES
                  * PayrollConstants.SECONDS * PayrollConstants.MILLISECONDS)) + 1;
            }

            BigDecimal processingDays = new BigDecimal(payRollDefDays);
            if (scholarshipElement.getMonthDays().equalsIgnoreCase("GV")) {
              BigDecimal globalValue = scholarshipElement.getGlobalValue().getNumericValue();
              processingDays = globalValue;
            }

            // Calculate Value from formula
            String formula = PayrollUtility.getElementFormula(scholarshipElement, grade,
                scholarshipAllowanceStartDate, scholarshipAllowanceEndDate, processingDays,
                BigDecimal.ZERO, scholarshipAllowanceEndDate, false, null);

            if (formula != null) {
              if (!StringUtils.isEmpty(formula)) {
                String appliedFormula = PayrollUtility.applyPredefinedValues(formula.toString(),
                    payRollComponents);

                scholarshipValue = calculateFormulaValue(scholarshipElement, appliedFormula);

                log4j.info("Calculated Value ===>" + scholarshipValue);
              }
            } else {
              errorFlag = true;
              errorMessage = "Error while fetching Formula for Business Mission ";
            }

          } else {
            errorFlag = true;
            errorMessage = OBMessageUtils.messageBD("EHCM_NoScholarshipCategory");
          }
        }
      } else {
        errorFlag = true;
        errorMessage = OBMessageUtils.messageBD("EHCM_NoScholarshipAllowanceElement");
      }
      return scholarshipValue;
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Error in EmpScholarshipTrainingAction.java : calculatePayment() ");
      return scholarshipValue;
    }
  }

  private BigDecimal calculateFormulaValue(EHCMElmttypeDef elementType, String appliedFormula) {
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

}