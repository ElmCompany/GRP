package sa.elm.ob.hcm.util.payroll;

import java.math.BigDecimal;
import java.util.Date;

import org.codehaus.jettison.json.JSONObject;

import sa.elm.ob.hcm.EHCMElementFormulaHdr;
import sa.elm.ob.hcm.EHCMElmttypeDef;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ehcmgraderates;

public class PayrollUtility {

  /**
   * @param baseProcess
   * @return
   */
  public static EHCMElmttypeDef getElementsFromBaseProcess(String baseProcess) {
    return PayrollUtilityDAO.getElementsFromBaseProcess(baseProcess);
  }

  /**
   * Gets Element Formula
   * 
   * @param elementType
   * @param grade
   * @param startDate
   * @param endDate
   * @param processingDays
   * @param differenceDays
   * @param payrollEndDate
   * @param isBaseCalculation
   * @param bMission
   * @return
   */
  public static String getElementFormula(EHCMElmttypeDef elementType, ehcmgrade grade,
      Date startDate, Date endDate, BigDecimal processingDays, BigDecimal differenceDays,
      Date payrollEndDate, boolean isBaseCalculation, EHCMEmpBusinessMission bMission) {
    return PayrollUtilityDAO.getElementFormula(elementType, grade, startDate, endDate,
        processingDays, differenceDays, payrollEndDate, isBaseCalculation, bMission);
  }

  /**
   * @param formula
   * @param payRollComponents
   * @return
   */
  public static String applyPredefinedValues(String formula, JSONObject payRollComponents) {
    return PayrollUtilityDAO.applyPredefinedValues(formula, payRollComponents);
  }

  /**
   * Generate element formula from line
   * 
   * @param formulaHdr
   * @param elementType
   * @param grade
   * @param startDate
   * @param endDate
   * @param processingDays
   * @param differenceDays
   * @param payrollEndDate
   * @param isBaseCalculation
   * @param bMission
   * @return
   */
  public static String generateFormulaFromLines(EHCMElementFormulaHdr formulaHdr,
      EHCMElmttypeDef elementType, ehcmgrade grade, Date startDate, Date endDate,
      BigDecimal processingDays, BigDecimal differenceDays, Date payrollEndDate,
      boolean isBaseCalculation, EHCMEmpBusinessMission bMission) {
    return PayrollUtilityDAO.generateFormulaFromLines(formulaHdr, elementType, grade, startDate,
        endDate, processingDays, differenceDays, payrollEndDate, isBaseCalculation, bMission);
  }

  /**
   * @param elmTypDef
   * @param gradeRate
   * @param grade
   * @param startDate
   * @param endDate
   * @param processingDays
   * @param differenceDays
   * @param payrollEndDate
   * @return
   */
  public static BigDecimal getGradeRateValue(EHCMElmttypeDef elmTypDef, ehcmgraderates gradeRate,
      ehcmgrade grade, Date startDate, Date endDate, BigDecimal processingDays,
      BigDecimal differenceDays, Date payrollEndDate) {
    return PayrollUtilityDAO.getGradeRateValue(elmTypDef, gradeRate, grade, startDate, endDate,
        processingDays, differenceDays, payrollEndDate);
  }

  /**
   * @param elmTypDef
   * @param gradeRate
   * @param grade
   * @param startDate
   * @param endDate
   * @param processingDays
   * @return
   */
  public static BigDecimal getLatestGradeRateValue(EHCMElmttypeDef elmTypDef,
      ehcmgraderates gradeRate, ehcmgrade grade, Date startDate, Date endDate,
      BigDecimal processingDays) {
    return PayrollUtilityDAO.getLatestGradeRateValue(elmTypDef, gradeRate, grade, startDate,
        endDate, processingDays);
  }

  /**
   * @param empPerInfo
   * @param holdUnholdReqId
   * @return
   */
  public static boolean hasDraftHoldUnHoldRequest(EhcmEmpPerInfo empPerInfo,
      String holdUnholdReqId) {
    return PayrollUtilityDAO.hasDraftHoldUnHoldRequest(empPerInfo, holdUnholdReqId);
  }

  /**
   * @param empPerInfo
   * @param holdUnholdReqId
   * @return
   */
  public static boolean hasActiveHoldRequest(EhcmEmpPerInfo empPerInfo, String holdUnholdReqId) {
    return PayrollUtilityDAO.hasActiveHoldRequest(empPerInfo, holdUnholdReqId);
  }

  /**
   * @param empPerInfo
   * @return
   */
  public static String getLatestHoldEndDate(EhcmEmpPerInfo empPerInfo) {
    return PayrollUtilityDAO.getLatestHoldEndDate(empPerInfo);
  }
}
