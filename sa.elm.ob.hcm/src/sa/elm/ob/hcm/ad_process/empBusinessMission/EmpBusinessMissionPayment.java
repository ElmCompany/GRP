package sa.elm.ob.hcm.ad_process.empBusinessMission;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMElmttypeDef;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAOImpl;
import sa.elm.ob.hcm.util.PayrollConstants;
import sa.elm.ob.hcm.util.payroll.PayrollUtility;

/**
 * This Process will handle the Employee Business Mission
 */

public class EmpBusinessMissionPayment implements Process {
  private static final Logger log4j = LoggerFactory.getLogger(EmpBusinessMissionPayment.class);
  private final OBError obError = new OBError();
  public static Boolean errorFlag;
  public static String errorMessage;

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    vars.getLanguage();
    errorFlag = false;
    errorMessage = null;

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    final String empBusinessMissionId = bundle.getParams().get("Ehcm_Emp_Businessmission_ID")
        .toString();
    bundle.getContext().getClient();
    bundle.getContext().getUser();

    EHCMEmpBusinessMission empBusinessMissionObj = OBDal.getInstance()
        .get(EHCMEmpBusinessMission.class, empBusinessMissionId);
    empBusinessMissionObj.getOrganization().getId();
    bundle.getContext().getRole();

    empBusinessMissionObj.getDecisionType();
    new EmpBusinessMissionDAOImpl();
    new MissionCategoryDAOImpl();
    new JSONObject();
    try {
      OBContext.setAdminMode();

      // checking decision overlap
      if (empBusinessMissionObj.getDecisionType()
          .equals(DecisionTypeConstants.DECISION_TYPE_BUSINESSMISSION_PAYMENT)) {
        System.out.println("Inside Business Mission Payment");

        JSONObject payRollComponents = new JSONObject();

        // Business Mission Type
        payRollComponents.put(PayrollConstants.ELEMENT_BUSINESS_MISSION_TYPE_CODE,
            "'" + empBusinessMissionObj.getMissionType().getSearchKey() + "'");

        // Business Mission House Provided
        payRollComponents.put(PayrollConstants.ELEMENT_BUSINESS_MISSION_HOUSING_CODE,
            "'" + (empBusinessMissionObj.isHousingProvided() ? "Y" : "N") + "'");

        // Business Mission Food Provided
        payRollComponents.put(PayrollConstants.ELEMENT_BUSINESS_MISSION_FOOD_CODE,
            "'" + (empBusinessMissionObj.isFoodProvided() ? "Y" : "N") + "'");

        // Business Mission Country Category (CA, CB, CC)
        if (empBusinessMissionObj.getToCity().getEhcmCategory() != null) {
          payRollComponents.put("BM_COUNTRYCAT",
              "'" + empBusinessMissionObj.getToCity().getEhcmCategory() + "'");
        } else {
          errorFlag = true;
          errorMessage = "Category is not configured for city "
              + empBusinessMissionObj.getToCity().getName();
        }

        // Business Mission Days
        payRollComponents.put(PayrollConstants.ELEMENT_BUSINESS_MISSION_DAYS,
            empBusinessMissionObj.getMissionDays());

        // Business Mission Days Before
        payRollComponents.put(PayrollConstants.ELEMENT_BUSINESS_MISSION_DAYS_BEFORE,
            empBusinessMissionObj.getNoofdaysBefore());

        // Business Mission Days After
        payRollComponents.put(PayrollConstants.ELEMENT_BUSINESS_MISSION_DAYS_AFTER,
            empBusinessMissionObj.getNoofdaysAfter());

        ehcmgrade grade = empBusinessMissionObj.getEmploymentGrade();
        Date bmStartDate = dateFormat.parse(empBusinessMissionObj.getStartDate().toString());
        Date bmEndDate = dateFormat.parse(empBusinessMissionObj.getEndDate().toString());

        EHCMElmttypeDef payrollElement = PayrollUtility.getElementsFromBaseProcess("BM");

        if (payrollElement != null) {
          long payRollDefDays = 30;
          BigDecimal processingDays = new BigDecimal(payRollDefDays);
          BigDecimal differenceDays = BigDecimal.ZERO;
          if (payrollElement.getMonthDays().equalsIgnoreCase("GV")) {
            BigDecimal globalValue = payrollElement.getGlobalValue().getNumericValue();
            processingDays = globalValue;
            if (payrollElement.getType().equalsIgnoreCase("REC")) {
              differenceDays = globalValue.subtract(new BigDecimal(payRollDefDays));
            }
          }

          String formula = PayrollUtility.getElementFormula(payrollElement, grade, bmStartDate,
              bmEndDate, processingDays, BigDecimal.ZERO, bmEndDate, false, empBusinessMissionObj);

          if (formula != null) {
            BigDecimal calculatedBMValue = BigDecimal.ZERO;
            if (!StringUtils.isEmpty(formula)) {
              String appliedFormula = applyPredefinedValues(formula.toString(), payRollComponents);

              calculatedBMValue = calculateFormulaValue(payrollElement, appliedFormula);

              log4j.info("Calculated Value ===>" + calculatedBMValue);

              empBusinessMissionObj.setUpdated(new java.util.Date());
              empBusinessMissionObj
                  .setPaymentAmt(calculatedBMValue.setScale(2, BigDecimal.ROUND_HALF_UP));
              // Calculate Advance and Net Amount
              BigDecimal advPercentage = empBusinessMissionObj.getAdvancePercentage();
              BigDecimal advAmount = empBusinessMissionObj.getAdvanceAmount();
              if (advPercentage != null && advPercentage.compareTo(BigDecimal.ZERO) > 0) {
                advAmount = calculatedBMValue.multiply(advPercentage.divide(new BigDecimal(100)));
                empBusinessMissionObj
                    .setAdvanceAmount(advAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
                empBusinessMissionObj.setBMNetAmount(
                    calculatedBMValue.subtract(advAmount).setScale(2, BigDecimal.ROUND_HALF_UP));
              } else if (advAmount != null && advAmount.compareTo(BigDecimal.ZERO) > 0) {
                empBusinessMissionObj.setBMNetAmount(
                    calculatedBMValue.subtract(advAmount).setScale(2, BigDecimal.ROUND_HALF_UP));
              } else {
                empBusinessMissionObj
                    .setBMNetAmount(calculatedBMValue.setScale(2, BigDecimal.ROUND_HALF_UP));
              }
              OBDal.getInstance().save(empBusinessMissionObj);
            }
          } else {
            errorFlag = true;
            errorMessage = "Error while fetching Formula for Business Mission ";
          }

        } else {
          errorFlag = true;
          errorMessage = "Error while fetching Business Mission element type definition ";
        }

      }

      if (!errorFlag) {
        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_BMPayment_Calc"));
        OBDal.getInstance().flush();
      } else if (errorFlag) {
        obError.setType("Error");
        obError.setTitle("Error");
        String message = (!StringUtils.isEmpty(errorMessage)) ? errorMessage
            : OBMessageUtils.messageBD("EHCM_BMPayment_NCalc");
        obError.setMessage(message);
        OBDal.getInstance().rollbackAndClose();
      }
      bundle.setResult(obError);
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

  private String applyPredefinedValues(String formula, JSONObject payRollComponents) {
    String appliedFormula = formula;
    try {
      java.util.Iterator<?> componentCodes = payRollComponents.keys();
      while (componentCodes.hasNext()) {
        String code = (String) componentCodes.next();
        appliedFormula = appliedFormula.replace(code, payRollComponents.getString(code));
      }
      return appliedFormula;
    } catch (JSONException e) {
      log4j.error("Error while in PayrollProcess.java : applyPredefinedValues() ");
      e.printStackTrace();
      return "";
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