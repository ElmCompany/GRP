package sa.elm.ob.hcm.util.payroll;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.EHCMElementFormulaHdr;
import sa.elm.ob.hcm.EHCMElementFormulaLne;
import sa.elm.ob.hcm.EHCMElmttypeDef;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMHoldUnHoldSalary;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ehcmgraderatelines;
import sa.elm.ob.hcm.ehcmgraderates;
import sa.elm.ob.hcm.ad_process.Payroll.PayrollBaseProcess;
import sa.elm.ob.hcm.ad_process.Payroll.PayrollBaseProcessDAO;
import sa.elm.ob.hcm.util.PayrollConstants;
import sa.elm.ob.utility.util.Utility;

public class PayrollUtilityDAO {
  private Connection connection = null;

  private static final Logger log4j = Logger.getLogger(PayrollUtilityDAO.class);
  static DateFormat dateFormat = Utility.dateFormat;
  static DateFormat yeareFormat = Utility.YearFormat;

  public PayrollUtilityDAO() {
    connection = getDbConnection();
  }

  /**
   * Get the database connection
   * 
   * @return
   */
  private Connection getDbConnection() {
    return OBDal.getInstance().getConnection();
  }

  public static EHCMElmttypeDef getElementsFromBaseProcess(String baseProcess) {
    EHCMElmttypeDef element = null;
    try {
      String whereClause = " e where e.baseProcess = :basePross order by startDate desc ";

      OBQuery<EHCMElmttypeDef> elementDefQry = OBDal.getInstance()
          .createQuery(EHCMElmttypeDef.class, whereClause);
      elementDefQry.setNamedParameter("basePross", baseProcess);
      elementDefQry.setMaxResult(1);
      @SuppressWarnings("unchecked")
      List<EHCMElmttypeDef> elementDefLst = elementDefQry.list();
      if (elementDefLst.size() > 0) {
        element = elementDefLst.get(0);
      }
      return element;
    } catch (Exception egl) {
      log4j.error("Error in PayrollBaseProcess.java : getElementsFromBaseProcess() ");
      egl.printStackTrace();
      return null;
    }
  }

  public static String getElementFormula(EHCMElmttypeDef elementType, ehcmgrade grade,
      Date startDate, Date endDate, BigDecimal processingDays, BigDecimal differenceDays,
      Date payrollEndDate, boolean isBaseCalculation, EHCMEmpBusinessMission bMission) {
    String formula = null;
    try {
      // No need of date validation so bring latest formula
      String whereClause = "e where elementType.id= :empTypeId "
          + "and e.enabled = 'Y' order by e.creationDate desc ";
      OBQuery<EHCMElementFormulaHdr> formulaHdrQry = OBDal.getInstance()
          .createQuery(EHCMElementFormulaHdr.class, whereClause);
      formulaHdrQry.setNamedParameter("empTypeId", elementType.getId());
      formulaHdrQry.setMaxResult(1);
      List<EHCMElementFormulaHdr> formulaHdrList = formulaHdrQry.list();
      if (formulaHdrList.size() > 0) {
        EHCMElementFormulaHdr formulaHdr = formulaHdrList.get(0);

        formula = generateFormulaFromLines(formulaHdr, elementType, grade, startDate, endDate,
            processingDays, differenceDays, payrollEndDate, isBaseCalculation, bMission);

      }
      return formula;
    } catch (Exception e) {
      log4j.error("Error in PayrollUtilityDAO.java : getElementFormula() ");
      e.printStackTrace();
      return null;
    }
  }

  public static String generateFormulaFromLines(EHCMElementFormulaHdr formulaHdr,
      EHCMElmttypeDef elementType, ehcmgrade grade, Date startDate, Date endDate,
      BigDecimal processingDays, BigDecimal differenceDays, Date payrollEndDate,
      boolean isBaseCalculation, EHCMEmpBusinessMission bMission) {
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
          BigDecimal gradeRate;
          if (isBaseCalculation) {
            // Fetching latest grade rate value based on grade rate in formula
            gradeRate = getLatestGradeRateValue(elementType, formulaLne.getGradeRate(), grade,
                startDate, endDate, processingDays);
          } else {
            // Fetching grade rate value based on grade rate in formula
            gradeRate = getGradeRateValue(elementType, formulaLne.getGradeRate(), grade, startDate,
                endDate, processingDays, differenceDays, payrollEndDate);
          }

          // Calculate Business Mission Based Muliti Rate Elements
          if (bMission != null) {
            // Business Mission Days Before Rate
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            c.add(Calendar.DAY_OF_MONTH,
                new BigDecimal(bMission.getNoofdaysBefore()).negate().intValue());
            Date daysBfStartDate = c.getTime();
            c.setTime(startDate);
            c.add(Calendar.DAY_OF_MONTH, -1);
            Date daysBfEndDate = c.getTime();
            BigDecimal daysBeforeRate = PayrollBaseProcessDAO.getGradeRateValue(elementType,
                formulaLne.getGradeRate(), grade, daysBfStartDate, daysBfEndDate, processingDays,
                BigDecimal.ZERO, payrollEndDate);

            // Business Mission Days After Rate
            c.setTime(endDate);
            c.add(Calendar.DAY_OF_MONTH, 1);
            Date daysAfStartDate = c.getTime();
            c.setTime(endDate);
            c.add(Calendar.DAY_OF_MONTH, bMission.getNoofdaysAfter().intValue());
            Date daysAfEndDate = c.getTime();

            BigDecimal daysAfterRate = PayrollBaseProcessDAO.getGradeRateValue(elementType,
                formulaLne.getGradeRate(), grade, daysAfStartDate, daysAfEndDate, processingDays,
                BigDecimal.ZERO, payrollEndDate);

            formula = formula.replace(PayrollConstants.ELEMENT_BUSINESS_MISSION_DAYS_BEFORE_RATE,
                daysBeforeRate.toString());
            formula = formula.replace(PayrollConstants.ELEMENT_BUSINESS_MISSION_DAYS_AFTER_RATE,
                daysAfterRate.toString());
          }

          // Replace grade rate in formula and condition
          formula = formula.replace(PayrollConstants.ELEMENT_GRADERATE_CODE, gradeRate.toString());
          if (!StringUtils.isEmpty(condition)) {
            condition = condition.replace(PayrollConstants.ELEMENT_GRADERATE_CODE,
                gradeRate.toString());
          }
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

      log4j.info("Formula ===> ");
      log4j.info(completeFormula.toString());
      return completeFormula.toString();
    } catch (Exception gffl) {
      log4j.error("Error in PayrollProcess.java : generateFormulaFromLines() ");
      gffl.printStackTrace();
      return null;
    }
  }

  public static String applyPredefinedValues(String formula, JSONObject payRollComponents) {
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

  public static BigDecimal getGradeRateValue(EHCMElmttypeDef elmTypDef, ehcmgraderates gradeRate,
      ehcmgrade grade, Date startDate, Date endDate, BigDecimal processingDays,
      BigDecimal differenceDays, Date payrollEndDate) {
    BigDecimal totalGradeRateValue = BigDecimal.ZERO;
    try {
      String dbFormattedStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String dbFormattedEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      String whereClause = "e where e.ehcmGraderates.id = :gradeRateId and e.grade.id=:gradeId "
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
      gradeLineQry.setFilterOnActive(false);

      List<ehcmgraderatelines> gradeLineList = gradeLineQry.list();
      for (ehcmgraderatelines gradeRateLne : gradeLineList) {
        JSONObject gradeRatePeriodJSON = PayrollBaseProcess.getOverlapingDateRange(
            gradeRateLne.getStartDate(), gradeRateLne.getEndDate(), startDate, endDate);

        if (gradeRatePeriodJSON != null) {
          BigDecimal gradeRateDays = new BigDecimal(gradeRatePeriodJSON.getLong("days"));

          // If Grade Rate is for payroll end date, Check and add extra days in month
          Date gradeRateEndDate = PayrollConstants.dateFormat
              .parse(gradeRatePeriodJSON.getString("endDate"));
          if (differenceDays.compareTo(BigDecimal.ZERO) > 0
              && gradeRateEndDate.compareTo(payrollEndDate) == 0) {
            gradeRateDays = gradeRateDays.add(differenceDays);
          }

          BigDecimal gradeRateValue = gradeRateLne.getSearchKey();

          BigDecimal perDayGradeRate = BigDecimal.ZERO;
          BigDecimal GradeRateCalDays = BigDecimal.ZERO;
          // Validate Monthly or Day Rate
          if (gradeRateLne.getEhcmGraderates().getDuration().equalsIgnoreCase("PM")) {
            perDayGradeRate = gradeRateValue.divide(processingDays, 6, BigDecimal.ROUND_HALF_UP);
          } else {
            perDayGradeRate = gradeRateValue;
          }

          GradeRateCalDays = perDayGradeRate.multiply(gradeRateDays).setScale(6,
              BigDecimal.ROUND_HALF_UP);
          totalGradeRateValue = totalGradeRateValue.add(GradeRateCalDays);
        }
      }

      return totalGradeRateValue;
    } catch (Exception e) {
      log4j.error("Error in PayrollProcess.java : getGradeRateValue() ");
      e.printStackTrace();
      return BigDecimal.ZERO;
    }
  }

  public static BigDecimal getLatestGradeRateValue(EHCMElmttypeDef elmTypDef,
      ehcmgraderates gradeRate, ehcmgrade grade, Date startDate, Date endDate,
      BigDecimal processingDays) {
    BigDecimal gradeRateValue = BigDecimal.ZERO;
    try {
      String dbFormattedStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String dbFormattedEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      String whereClause = "e where e.ehcmGraderates.id = :gradeRateId and e.grade.id=:gradeId "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) order by e.startDate desc ";

      OBQuery<ehcmgraderatelines> gradeLineQry = OBDal.getInstance()
          .createQuery(ehcmgraderatelines.class, whereClause);
      gradeLineQry.setNamedParameter("gradeRateId", gradeRate.getId());
      gradeLineQry.setNamedParameter("gradeId", grade.getId());
      gradeLineQry.setNamedParameter("startDate", dbFormattedStartDate);
      gradeLineQry.setNamedParameter("endDate", dbFormattedEndDate);
      gradeLineQry.setFilterOnActive(false);
      gradeLineQry.setMaxResult(1);

      if (gradeLineQry.list().size() > 0) {
        ehcmgraderatelines gradeRateLne = gradeLineQry.list().get(0);
        gradeRateValue = gradeRateLne.getSearchKey();
        // Validate Monthly or Day Rate
        if (!gradeRateLne.getEhcmGraderates().getDuration().equalsIgnoreCase("PM")) {
          gradeRateValue = gradeRateValue.multiply(processingDays);
        }
      }
      return gradeRateValue;
    } catch (Exception e) {
      log4j.error("Error in PayrollUtilityDAO.java : getLatestGradeRateValue() ");
      e.printStackTrace();
      return BigDecimal.ZERO;
    }
  }

  public static boolean hasDraftHoldUnHoldRequest(EhcmEmpPerInfo empPerInfo,
      String holdUnholdReqId) {
    try {
      String whereClause = " e where e.ehcmEmpPerinfo.id=:empId and e.processed='N' and e.id!=:holdUnHoldId";

      OBQuery<EHCMHoldUnHoldSalary> holdSlryReq = OBDal.getInstance()
          .createQuery(EHCMHoldUnHoldSalary.class, whereClause);
      holdSlryReq.setNamedParameter("empId", empPerInfo.getId());
      holdSlryReq.setNamedParameter("holdUnHoldId", holdUnholdReqId);
      if (holdSlryReq.list().size() > 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      log4j.error("Error in PayrollUtilityDAO.java : hasDraftHoldUnHoldRequest() ");
      e.printStackTrace();
      return false;
    }
  }

  public static boolean hasActiveHoldRequest(EhcmEmpPerInfo empPerInfo, String holdUnholdReqId) {
    try {
      String whereClause = " e where e.ehcmEmpPerinfo.id= :empId and e.requestType='HS' and e.processed='Y' and e.holdEndPeriod is null and e.id!=:holdUnHoldId";

      OBQuery<EHCMHoldUnHoldSalary> holdSlryReq = OBDal.getInstance()
          .createQuery(EHCMHoldUnHoldSalary.class, whereClause);
      holdSlryReq.setNamedParameter("empId", empPerInfo.getId());
      holdSlryReq.setNamedParameter("holdUnHoldId", holdUnholdReqId);
      if (holdSlryReq.list().size() > 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      log4j.error("Error in PayrollUtilityDAO.java : hasDraftHoldUnHoldRequest() ");
      e.printStackTrace();
      return false;
    }
  }

  public static String getLatestHoldEndDate(EhcmEmpPerInfo empPerInfo) {
    try {
      String whereClause = "  e where e.ehcmEmpPerinfo.id=:empId and e.requestType='HS' and e.processed='Y' order by e.holdEndPeriod.startDate desc";

      OBQuery<EHCMHoldUnHoldSalary> holdSlryReq = OBDal.getInstance()
          .createQuery(EHCMHoldUnHoldSalary.class, whereClause);
      holdSlryReq.setNamedParameter("empId", empPerInfo.getId());
      holdSlryReq.setMaxResult(1);
      if (holdSlryReq.list().size() > 0) {
        return holdSlryReq.list().get(0).getHoldEndPeriod().getStartDate().toString();
      } else {
        return null;
      }
    } catch (Exception e) {
      log4j.error("Error in PayrollUtilityDAO.java : getLatestHoldEndDate() ");
      e.printStackTrace();
      return null;
    }
  }

}
