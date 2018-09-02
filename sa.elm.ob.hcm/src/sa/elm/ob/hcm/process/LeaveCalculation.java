package sa.elm.ob.hcm.process;

import java.sql.Connection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;

import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;
import sa.elm.ob.hcm.EHCMEmpLeave;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ad_forms.employee.vo.EmployeeVO;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.process.dao.LeaveCalculationDAOImpl;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class LeaveCalculation extends DalBaseProcess {
  private ProcessLogger logger;
  private static final Logger log = Logger.getLogger(LeaveCalculation.class);

  protected void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    logger = bundle.getLogger();
    OBError result = new OBError();
    Connection conn = OBDal.getInstance().getConnection();
    try {
      OBContext.setAdminMode();
      final String clientId = (String) bundle.getContext().getClient();
      // today hijiri last date
      String hiredate = "";
      // String curntYearhijirihiredate = "";
      String nextYearhijirihiredate = "";
      String prevYearGrehireEnddateHY = "";
      String prevYearGrehireStartdateHY = "";

      String startDate = null;
      String endDate = null;
      String nextYearStartDate = null;

      // calculated date
      String currentDate = null;
      String hijiridate = null;
      String fstHijirGreDate = null;
      String nxtYearHijiriGerDate = null;
      String fstGergDate = null;
      String nextYearGregDate = null;
      String lastGregDate = null;
      String accrualResetValue = null;
      String hijirihiredate = null;
      String nxtYearGreHireDateHY = null;
      String nxtYearGreHireDateGY = null;
      String prevYearGreHireStartdateGY = null;
      String prevYearGreHireEnddateGY = null;

      boolean hijiriEndDate = false;
      boolean gregEndDate = false;
      boolean empHireAnniversaryDateHY = false;
      boolean empHireAnniversaryDateGY = false;

      DateFormat dateFormat = Utility.dateFormat;
      DateFormat YearFormat = Utility.YearFormat;
      String gradeIdList = "";
      EHCMEmpLeave prevYearleave = null;
      String nextDayHijiStr = "";
      Date nextDayHiji = null;
      String nextDayGregStr = "";
      Date nextDayGreg = null;
      JSONObject json = null;// test

      List<EHCMAbsenceTypeAccruals> absenceTypeAccrualList = new ArrayList<EHCMAbsenceTypeAccruals>();
      List<EhcmEmpPerInfo> empPerInfoList = new ArrayList<EhcmEmpPerInfo>();
      LeaveCalculationDAOImpl leaveCalculationDAOImpl = new LeaveCalculationDAOImpl();
      List<EmployeeVO> empHireDateLsHY = new ArrayList<EmployeeVO>();
      List<EmployeeVO> empHireDateLsGY = new ArrayList<EmployeeVO>();

      EHCMEmpLeave leave = null;
      // taking current date and format
      currentDate = dateFormat.format(new Date());
      currentDate = YearFormat.format(dateFormat.parse(currentDate));
      log.debug("currentDate:" + currentDate);
      // currentDate = "2018-04-27"; // "2018-04-27"; // "2018-12-31";// "2018-09-10";

      // convert the current date as hijiridate
      hijiridate = UtilityDAO.convertTohijriDate(currentDate);

      // calculate the year first hijiri date using current date(in hijirdate)
      fstHijirGreDate = UtilityDAO.convertToGregorian("01-01-" + (hijiridate.split("-")[2]));

      // calculate the next year first hijiri date using current date(in hijirdate)
      nxtYearHijiriGerDate = UtilityDAO
          .convertToGregorian("01-01-" + ((Integer.valueOf(hijiridate.split("-")[2])) + 1));

      // calculate the year first Gregorian date using current date(in gregorian date)
      fstGergDate = (currentDate.split("-")[0]) + "-01-01";

      // calculate the next year first Gregorian date using current date(in gregorian date)
      nextYearGregDate = ((Integer.valueOf(currentDate.split("-")[0])) + 1) + "-01-01";

      // for hire anniversay date
      JSONObject resutlGrHiDate = leaveCalculationDAOImpl
          .getNextDayGregAndHijiriDate(YearFormat.parse(currentDate));
      if (resutlGrHiDate != null) {
        nextDayHijiStr = resutlGrHiDate.getString("hijiriDate").substring(6, 8) + "-"
            + resutlGrHiDate.getString("hijiriDate").substring(4, 6);
        nextDayHiji = dateFormat.parse(resutlGrHiDate.getString("gregorianDate"));
        nextDayGregStr = resutlGrHiDate.getString("gregorianDate").split("-")[0] + "-"
            + resutlGrHiDate.getString("gregorianDate").split("-")[1];
        nextDayGreg = dateFormat.parse(resutlGrHiDate.getString("gregorianDate"));

        empHireDateLsHY = leaveCalculationDAOImpl.getEmployeeHireAnniversaryHYList(nextDayHiji,
            nextDayHijiStr, clientId);
        if (empHireDateLsHY.size() > 0) {
          empHireAnniversaryDateHY = true;
        }
        empHireDateLsGY = leaveCalculationDAOImpl.getEmployeeHireAnniversaryGYList(nextDayGreg,
            nextDayGregStr, clientId);
        if (empHireDateLsGY.size() > 0) {
          empHireAnniversaryDateGY = true;
        }

        if (empHireAnniversaryDateHY) {
          hiredate = YearFormat.format(empHireDateLsHY.get(0).getEmpHireDate());

          hijirihiredate = UtilityDAO.convertTohijriDate(hiredate);
          log.debug("hijirihiredate:" + hijirihiredate);

          nextYearhijirihiredate = hijirihiredate.split("-")[0] + "-" + hijirihiredate.split("-")[1]
              + "-" + hijiridate.split("-")[2]; // 12-08-1441
          log.debug("nextYearhijirihiredate:" + nextYearhijirihiredate);
          prevYearGrehireStartdateHY = UtilityDAO
              .convertToGregorian(hijirihiredate.split("-")[0] + "-" + hijirihiredate.split("-")[1]
                  + "-" + (Integer.parseInt(hijiridate.split("-")[2]) - 1));
          log.debug("prevYearGrehireStartdateHY:" + prevYearGrehireStartdateHY);
          prevYearGrehireEnddateHY = YearFormat.format(dateFormat
              .parse(sa.elm.ob.hcm.util.UtilityDAO.getBeforeDateInGreg(nextYearhijirihiredate)));

          log.debug("prevYearGrehireEnddateHY:" + prevYearGrehireEnddateHY);
          nxtYearGreHireDateHY = UtilityDAO.convertToGregorian(nextYearhijirihiredate);
        }
        if (empHireAnniversaryDateGY) {

          hiredate = YearFormat.format(empHireDateLsGY.get(0).getEmpHireDate());

          hijirihiredate = UtilityDAO.convertTohijriDate(hiredate);

          nxtYearGreHireDateGY = (Integer.parseInt(currentDate.split("-")[0])) + "-"
              + hiredate.split("-")[1] + "-" + hiredate.split("-")[2];
          prevYearGreHireStartdateGY = (Integer.parseInt(currentDate.split("-")[0]) - 1) + "-"
              + hiredate.split("-")[1] + "-" + hiredate.split("-")[2];
          prevYearGreHireEnddateGY = YearFormat
              .format(dateFormat.parse(sa.elm.ob.hcm.util.UtilityDAO
                  .getBeforeDateInGregUsingGregDate(nxtYearGreHireDateGY)));
        }
      }
      // check gregorian last date

      lastGregDate = (currentDate.split("-")[0]) + "-12-31";
      log.debug("lastGregDate:" + YearFormat.parse(lastGregDate));
      if (YearFormat.parse(lastGregDate).compareTo(YearFormat.parse(currentDate)) == 0) {
        gregEndDate = true;
      }

      hijiriEndDate = leaveCalculationDAOImpl.chkCurrentDateAsHijiriEnddate(currentDate,
          nxtYearHijiriGerDate);

      if (hijiriEndDate || gregEndDate || empHireAnniversaryDateGY || empHireAnniversaryDateHY
          || empHireAnniversaryDateGY) {
        if (hijiriEndDate) {
          startDate = dateFormat.format(YearFormat.parse(fstHijirGreDate));
          endDate = sa.elm.ob.hcm.util.UtilityDAO
              .getBeforeDateInGreg("01-01-" + ((Integer.valueOf(hijiridate.split("-")[2])) + 1));// minus
          // one
          accrualResetValue = Constants.ACCRUALRESETDATE_HIJIRI;
          nextYearStartDate = nxtYearHijiriGerDate;
        } else if (gregEndDate) {
          startDate = dateFormat.format(YearFormat.parse(fstGergDate));
          endDate = dateFormat.format(YearFormat.parse(lastGregDate));
          nextYearStartDate = nextYearGregDate;
          accrualResetValue = Constants.ACCRUALRESETDATE_GREGORIAN;
        } else if (empHireAnniversaryDateGY) {
          startDate = dateFormat.format(YearFormat.parse(prevYearGreHireStartdateGY));
          endDate = dateFormat.format(YearFormat.parse(prevYearGreHireEnddateGY));
          nextYearStartDate = nxtYearGreHireDateGY;
          accrualResetValue = Constants.ACCRUALRESETDATE_HIREANNIVERSARY;
        } else if (empHireAnniversaryDateHY) {
          startDate = dateFormat.format(YearFormat.parse(prevYearGrehireStartdateHY));
          endDate = dateFormat.format(YearFormat.parse(prevYearGrehireEnddateHY));
          nextYearStartDate = nxtYearGreHireDateHY;
          accrualResetValue = Constants.ACCRUALRESETDATE_HIREANNIVERSARY;
        }

        absenceTypeAccrualList = leaveCalculationDAOImpl.getAccrualList(clientId, startDate,
            endDate, accrualResetValue);

        if (absenceTypeAccrualList.size() > 0) {
          for (EHCMAbsenceTypeAccruals accrual : absenceTypeAccrualList) {
            EHCMAbsenceType absencetype = accrual.getAbsenceType();

            if (accrual.getGradeClassifications() != null) {
              if (StringUtils.isNotEmpty(gradeIdList))
                gradeIdList = ",'" + accrual.getGradeClassifications().getId() + "'";
              else
                gradeIdList = "'" + accrual.getGradeClassifications().getId() + "'";
            }
            log.debug("hijiriEndDate:" + hijiriEndDate);
            log.debug("gregEndDate:" + gregEndDate);
            log.debug("empHireAnniversaryDateHY:" + empHireAnniversaryDateHY);
            log.debug("empHireAnniversaryDateGY:" + empHireAnniversaryDateGY);
            // take absence type for each accrual to know the accural reset date
            if ((absencetype.getAccrualResetDate().equals(Constants.ACCRUALRESETDATE_HIJIRI)
                && hijiriEndDate)
                || (absencetype.getAccrualResetDate().equals(Constants.ACCRUALRESETDATE_GREGORIAN)
                    && gregEndDate)) {

              empPerInfoList = leaveCalculationDAOImpl.getEmployeeList(clientId,
                  accrual.getGradeClassifications() == null ? null
                      : accrual.getGradeClassifications().getId(),
                  currentDate, (accrual.getGradeClassifications() == null ? gradeIdList : null));

              if (empPerInfoList.size() > 0) {
                for (EhcmEmpPerInfo employee : empPerInfoList) {
                  // get emp leave based on empid and absence type
                  prevYearleave = leaveCalculationDAOImpl.getEmpLeave(absencetype, employee,
                      startDate);
                  leave = leaveCalculationDAOImpl.insertEmpLeave(absencetype, employee, startDate,
                      endDate, nextYearStartDate, prevYearleave, accrual);
                  if (leave != null) {
                    if (hijiriEndDate) {
                      leaveCalculationDAOImpl.insertEmpLeaveLine(conn, leave, fstHijirGreDate,
                          nxtYearHijiriGerDate, accrual.getDays(), prevYearleave,
                          nextYearStartDate);
                    } else if (gregEndDate) {
                      leaveCalculationDAOImpl.insertEmpLeaveLine(conn, leave, fstGergDate,
                          lastGregDate, accrual.getDays(), prevYearleave, nextYearStartDate);
                    }
                  }
                }
              }
            }
            // && (empHireAnniversaryDateHY || empHireAnniversaryDateGY)
            else if (absencetype.getAccrualResetDate()
                .equals(Constants.ACCRUALRESETDATE_HIREANNIVERSARY)
                && absencetype.getFrequency().equals(Constants.ACCRUALFREQUENCY_HIJIRI)
                && empHireAnniversaryDateHY) {

              if (empHireDateLsHY.size() > 0) {
                for (EmployeeVO employee : empHireDateLsHY) {
                  log.debug("gradeId" + employee.getGradeclassId());
                  log.debug("gradeIdList:" + gradeIdList);
                  log.debug("contains:" + employee.getGradeclassId().contains(gradeIdList));
                  if (accrual.getGradeClassifications() != null && (accrual
                      .getGradeClassifications().getId().equals(employee.getGradeclassId()))) {
                    prevYearleave = leaveCalculationDAOImpl.getEmpLeave(absencetype,
                        employee.getEmployee(), startDate);
                    leave = leaveCalculationDAOImpl.insertEmpLeave(absencetype,
                        employee.getEmployee(), startDate, endDate, nextYearStartDate,
                        prevYearleave, accrual);
                    if (leave != null) {
                      leaveCalculationDAOImpl.insertEmpLeaveLine(conn, leave,
                          (empHireAnniversaryDateHY ? prevYearGrehireStartdateHY
                              : prevYearGreHireStartdateGY),
                          (empHireAnniversaryDateHY ? prevYearGrehireEnddateHY
                              : prevYearGreHireEnddateGY),
                          accrual.getDays(), prevYearleave, nextYearStartDate);
                    }

                  }

                  else if (accrual.getGradeClassifications() == null
                      && (!employee.getGradeclassId().contains(gradeIdList)
                          || StringUtils.isEmpty(gradeIdList))) {
                    prevYearleave = leaveCalculationDAOImpl.getEmpLeave(absencetype,
                        employee.getEmployee(), startDate);
                    log.debug("prevleave:" + prevYearleave);
                    leave = leaveCalculationDAOImpl.insertEmpLeave(absencetype,
                        employee.getEmployee(), startDate, endDate, nextYearStartDate,
                        prevYearleave, accrual);
                    if (leave != null) {
                      leaveCalculationDAOImpl.insertEmpLeaveLine(conn, leave,
                          (empHireAnniversaryDateHY ? prevYearGrehireStartdateHY
                              : prevYearGreHireStartdateGY),
                          (empHireAnniversaryDateHY ? prevYearGrehireEnddateHY
                              : prevYearGreHireEnddateGY),
                          accrual.getDays(), prevYearleave, nextYearStartDate);
                    }

                  }

                }
              }
            }

            else if (absencetype.getAccrualResetDate()
                .equals(Constants.ACCRUALRESETDATE_HIREANNIVERSARY)
                && absencetype.getFrequency().equals(Constants.ACCRUALRESETDATE_GREGORIAN)
                && empHireAnniversaryDateGY) {

              if (empHireDateLsGY.size() > 0) {
                for (EmployeeVO employee : empHireDateLsGY) {
                  log.debug("gradeId:" + employee.getGradeclassId());
                  log.debug("gradeIdList:" + gradeIdList);
                  log.debug("contains:" + employee.getGradeclassId().contains(gradeIdList));
                  if (accrual.getGradeClassifications() != null && (accrual
                      .getGradeClassifications().getId().equals(employee.getGradeclassId()))) {
                    prevYearleave = leaveCalculationDAOImpl.getEmpLeave(absencetype,
                        employee.getEmployee(), startDate);
                    leave = leaveCalculationDAOImpl.insertEmpLeave(absencetype,
                        employee.getEmployee(), startDate, endDate, nextYearStartDate,
                        prevYearleave, accrual);
                    if (leave != null) {
                      leaveCalculationDAOImpl.insertEmpLeaveLine(conn, leave,
                          (empHireAnniversaryDateHY ? prevYearGrehireStartdateHY
                              : prevYearGreHireStartdateGY),
                          (empHireAnniversaryDateHY ? prevYearGrehireEnddateHY
                              : prevYearGreHireEnddateGY),
                          accrual.getDays(), prevYearleave, nextYearStartDate);
                    }

                  }

                  else if (accrual.getGradeClassifications() == null
                      && (!employee.getGradeclassId().contains(gradeIdList)
                          || StringUtils.isEmpty(gradeIdList))) {
                    prevYearleave = leaveCalculationDAOImpl.getEmpLeave(absencetype,
                        employee.getEmployee(), startDate);
                    log.debug("prevleave:" + prevYearleave);
                    leave = leaveCalculationDAOImpl.insertEmpLeave(absencetype,
                        employee.getEmployee(), startDate, endDate, nextYearStartDate,
                        prevYearleave, accrual);
                    if (leave != null) {
                      leaveCalculationDAOImpl.insertEmpLeaveLine(conn, leave,
                          (empHireAnniversaryDateHY ? prevYearGrehireStartdateHY
                              : prevYearGreHireStartdateGY),
                          (empHireAnniversaryDateHY ? prevYearGrehireEnddateHY
                              : prevYearGreHireEnddateGY),
                          accrual.getDays(), prevYearleave, nextYearStartDate);
                    }

                  }

                }
              }
            }
            //
          }
        }
      }

      addLog(OBMessageUtils.messageBD("Efin_UpdateAcc_Tree"));

      bundle.setResult(result);
    } catch (

    Exception e) {
      OBDal.getInstance().rollbackAndClose();
      result = OBMessageUtils.translateError(bundle.getConnection(), bundle.getContext().toVars(),
          OBContext.getOBContext().getLanguage().getLanguage(), e.getMessage());
      log.error(result.getMessage(), e);
      addLog(result.getMessage());
      bundle.setResult(result);
      return;
    } finally {
      OBContext.restorePreviousMode();
      addLog("Ending background process.");
    }
  }

  /**
   * Adds a message to the log.
   * 
   * @param msg
   *          to add to the log
   */
  private void addLog(String msg) {
    addLog(msg, false);
  }

  /**
   * Add a message to the log.
   * 
   * @param msg
   * @param generalLog
   */
  private void addLog(String msg, boolean generalLog) {
    logger.log(msg + "\n");
  }

  public void kill(ProcessBundle processBundle) throws Exception {
    addLog("Process Killed");
  }

}
