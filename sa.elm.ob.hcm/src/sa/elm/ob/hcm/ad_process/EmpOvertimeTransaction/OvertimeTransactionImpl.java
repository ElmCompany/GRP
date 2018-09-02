package sa.elm.ob.hcm.ad_process.EmpOvertimeTransaction;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMElementFormulaHdr;
import sa.elm.ob.hcm.EHCMElementFormulaLne;
import sa.elm.ob.hcm.EHCMElmttypeDef;
import sa.elm.ob.hcm.EhcmEmployeeOvertime;
import sa.elm.ob.hcm.EhcmOvertimeType;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmpayscaleline;
import sa.elm.ob.hcm.util.PayrollConstants;

/**
 * This process class used for Overtime Transaction Implementation
 * 
 * @author poongodi 15-03-2018
 *
 */

public class OvertimeTransactionImpl implements OvertimeTransactionDAO {

  private static final Logger log = LoggerFactory.getLogger(OvertimeTransactionImpl.class);
  public static final String DECISION_STATUS_ISSUED = "I";

  @Override
  public int getOvertimeTypeRecord(String overtimeTypeId, EhcmEmployeeOvertime overtimeObj) {
    int count = 0;
    EhcmOvertimeType overtimeType = null;
    boolean workingdaysFlag = false;
    boolean weekendoneFlag = false;
    boolean weekendtwoFlag = false;
    boolean hajjFlag = false;
    boolean feterFlag = false;
    boolean nationalFlag = false;
    try {
      overtimeType = OBDal.getInstance().get(EhcmOvertimeType.class, overtimeTypeId);
      if (overtimeType.isWorkingdays()) {
        workingdaysFlag = true;
      }
      if (overtimeType.isWeekendonedays()) {
        weekendoneFlag = true;
      }
      if (overtimeType.isWeekendtwodays()) {
        weekendtwoFlag = true;
      }
      if (overtimeType.isHajjdays()) {
        hajjFlag = true;
      }
      if (overtimeType.isFeterdays()) {
        feterFlag = true;
      }
      if (overtimeType.isNationalday()) {
        nationalFlag = true;
      }
      if (workingdaysFlag || weekendoneFlag || weekendtwoFlag || feterFlag || hajjFlag
          || nationalFlag) {
        if ((new BigDecimal(overtimeObj.getWorkingDays()).compareTo(new BigDecimal(0)) == 0)
            && (new BigDecimal(overtimeObj.getWeekend1Days()).compareTo(new BigDecimal(0)) == 0)
            && (new BigDecimal(overtimeObj.getWeekend2Days()).compareTo(new BigDecimal(0)) == 0)
            && (new BigDecimal(overtimeObj.getHajjDays()).compareTo(new BigDecimal(0)) == 0)
            && (new BigDecimal(overtimeObj.getNationalDay()).compareTo(new BigDecimal(0)) == 0)
            && (new BigDecimal(overtimeObj.getFeterDays()).compareTo(new BigDecimal(0)) == 0)) {
          count = 1;

        }
      }

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in getOvertimeTypeRecord " + e.getMessage());
      e.printStackTrace();
    }
    return count;
  }

  @Override
  public List<ehcmpayscaleline> getPayscaleValues(EmploymentInfo empInfo, Date startDate,
      Date endDate) throws Exception {
    List<ehcmpayscaleline> payScaleLneList = null;
    try {
      String empmtPayStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String empmtPayEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);
      if (empmtPayEndDate.isEmpty()) {
        empmtPayEndDate = "21-06-2058";
      }
      String whereClause = "e where e.ehcmPayscale.ehcmGrade.id=:gradeId "
          + "and e.ehcmProgressionpt.id = :pointsId "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) "
          + "order by e.startDate ";

      OBQuery<ehcmpayscaleline> payScaleQry = OBDal.getInstance()
          .createQuery(ehcmpayscaleline.class, whereClause);
      payScaleQry.setNamedParameter("gradeId", empInfo.getEmploymentgrade().getId());
      payScaleQry.setNamedParameter("pointsId",
          empInfo.getEhcmPayscaleline().getEhcmProgressionpt().getId());
      payScaleQry.setNamedParameter("startDate", empmtPayStartDate);
      payScaleQry.setNamedParameter("endDate", empmtPayEndDate);
      payScaleQry.setFilterOnActive(false);
      payScaleLneList = payScaleQry.list();
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getPayScaleValue() " + e);
      e.printStackTrace();
      return payScaleLneList;
    }
    return payScaleLneList;
  }

  public String getOverTimeFormula(EHCMElmttypeDef elementType, boolean isBaseCalculation,
      EhcmEmployeeOvertime overTime) {

    try {
      StringBuffer completeFormula = new StringBuffer();

      // No need of date validation so bring latest formula
      String whereClause = "e where elementType.id= :empTypeId "
          + "and e.enabled = 'Y' order by e.creationDate desc ";
      OBQuery<EHCMElementFormulaHdr> formulaHdrQry = OBDal.getInstance()
          .createQuery(EHCMElementFormulaHdr.class, whereClause);
      formulaHdrQry.setNamedParameter("empTypeId", elementType.getId());
      formulaHdrQry.setMaxResult(1);
      List<EHCMElementFormulaHdr> formulaHdrList = formulaHdrQry.list();
      EHCMElementFormulaHdr formulaHdr = formulaHdrList.get(0);

      // formula = generateFormulaFromLines(formulaHdr, elementType, grade, startDate, endDate,
      // processingDays, differenceDays, payrollEndDate, isBaseCalculation, overTime);

      whereClause = "  e where e.element.id=:formulaHdrId and e.enabled = 'Y' order by e.priority";
      OBQuery<EHCMElementFormulaLne> formulaLneQry = OBDal.getInstance()
          .createQuery(EHCMElementFormulaLne.class, whereClause);
      formulaLneQry.setNamedParameter("formulaHdrId", formulaHdr.getId());
      List<EHCMElementFormulaLne> formulaLneList = formulaLneQry.list();
      for (EHCMElementFormulaLne formulaLne : formulaLneList) {
        String formula = formulaLne.getFormula();
        String condition = !StringUtils.isEmpty(formulaLne.getCondition())
            ? formulaLne.getCondition().replace("=", "==")
            : "true";

        // check and apply overtime values
        if (elementType.getElementSource() != null
            && elementType.getElementSource().equalsIgnoreCase("OT")) {
          if (!StringUtils.isEmpty(condition)) {
            EhcmOvertimeType overtimeTypeObj = OBDal.getInstance().get(EhcmOvertimeType.class,
                overTime.getEhcmOvertimeType().getId());
            // replace working days & hours
            if (overtimeTypeObj.isWorkingdays().equals(true)) {
              condition = condition.replace(PayrollConstants.OVERTIME_WORKINGDAYS,
                  overTime.getWorkingDays().toString());
              condition = condition.replace(PayrollConstants.OVERTIME_WORKINGHOURS,
                  overTime.getWorkingHours().getName());
            }

            // replace weekend1day & hours
            if (overtimeTypeObj.isWeekendonedays().equals(true)) {
              condition = condition.replace(PayrollConstants.OVERTIME_WEEKEND1DAYS,
                  overTime.getWeekend1Days().toString());
              condition = condition.replace(PayrollConstants.OVERTIME_WEEKEND1HOURS,
                  overTime.getWeekendonehours().getName());
            }
            // replace weekend2day & hours
            if (overtimeTypeObj.isWeekendtwodays().equals(true)) {
              condition = condition.replace(PayrollConstants.OVERTIME_WEEKEND2DAYS,
                  overTime.getWeekend2Days().toString());
              condition = condition.replace(PayrollConstants.OVERTIME_WEEKEND2HOURS,
                  overTime.getWeekendtwohours().getName());
            }
            // replace feterday & hours
            if (overtimeTypeObj.isFeterdays().equals(true)) {
              condition = condition.replace(PayrollConstants.OVERTIME_FETERDAYS,
                  overTime.getFeterDays().toString());
              condition = condition.replace(PayrollConstants.OVERTIME_FETERHOURS,
                  overTime.getFeterhours().getName());
            }

            // replace hajjiday & hours
            if (overtimeTypeObj.isHajjdays().equals(true)) {
              condition = condition.replace(PayrollConstants.OVERTIME_HAJJDAYS,
                  overTime.getHajjDays().toString());
              condition = condition.replace(PayrollConstants.OVERTIME_HAJJHOURS,
                  overTime.getHajjhours().getName());
            }
            // replace nationalday & hours
            if (overtimeTypeObj.isNationalday().equals(true)) {
              condition = condition.replace(PayrollConstants.OVERTIME_NATIONALDAYS,
                  overTime.getNationalDay().toString());
              condition = condition.replace(PayrollConstants.OVERTIME_NATIONALHOURS,
                  overTime.getNationalhours().getName());
            }
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

      log.info("Formula ===> ");
      log.info(completeFormula.toString());

      return completeFormula.toString();

    } catch (Exception e) {
      log.error("Error in getOverTimeFormula() " + e);
      e.printStackTrace();
    }
    return null;

  }

}
