package sa.elm.ob.hcm.ad_process.AbsenceDecision;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;
import sa.elm.ob.hcm.EHCMEmpLeave;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;

/**
 * Interface for all Absence Decision related DB Operations
 * 
 * @author divya -09-04-2018
 *
 */
public interface AbsenceIssueDecisionDAO {

  /**
   * chk already leave block exists for particular absence startdate and enddate for exceptional
   * leave
   * 
   * @param absence
   * @return
   * @throws Exception
   */
  Boolean chkEmpLeavePresentInTwoBlk(EHCMAbsenceAttendance absence) throws Exception;

  /**
   * 
   * @param absence
   * @param empgrdclssdef
   * @return
   * @throws Exception
   */
  List<EHCMAbsenceTypeAccruals> getAbsenceAccrual(EHCMAbsenceAttendance absence,
      boolean empgrdclssdef) throws Exception;

  /**
   * insert emp leave header for each absence type based on start date and enddate
   * 
   * @param absence
   * @param absencetype
   * @param accrual
   * @param absStartDate
   * @param absEnddate
   * @return
   * @throws Exception
   */
  EHCMEmpLeave insertEmpLeave(EHCMAbsenceAttendance absence, EHCMAbsenceType absencetype,
      EHCMAbsenceTypeAccruals accrual, Date absStartDate, Date absEnddate) throws Exception;

  /**
   * check sufficient leave balance is exists or not and absence rules satisfied or not
   * 
   * @param absence
   * @return
   * @throws Exception
   */
  String chkleaveapprove(EHCMAbsenceAttendance absence) throws Exception;

  /**
   * handling deducted leave i.e,Authorized unpaid leave isdeducted='Y'
   * 
   * @param absencetype
   * @param con
   * @param absence
   * @param leave
   * @param accrual
   * @param cancelAbsence
   * @return
   * @throws Exception
   */
  int deductedLeave(EHCMAbsenceType absencetype, Connection con, EHCMAbsenceAttendance absence,
      EHCMEmpLeave leave, EHCMAbsenceTypeAccruals accrual, EHCMAbsenceAttendance cancelAbsence)
      throws Exception;

  /**
   * insert emp leaveline -line table
   * 
   * @param header
   * @param absenceType
   * @param absence
   * @param absencedays
   * @param startdate
   * @param enddate
   * @return
   * @throws Exception
   */
  int insertEmpLeaveLine(EHCMEmpLeave header, EHCMAbsenceType absenceType,
      EHCMAbsenceAttendance absence, BigDecimal absencedays, Date startdate, Date enddate)
      throws Exception;

  /**
   * 
   * @param absence
   * @param absencetype
   * @param accrual
   * @param absStartDate
   * @param absEnddate
   * @return
   */
  EHCMEmpLeave insertLeaveOccuranceEmpLeave(EHCMAbsenceAttendance absence,
      EHCMAbsenceType absencetype, EHCMAbsenceTypeAccruals accrual, Date absStartDate,
      Date absEnddate);

  /**
   * handling exception leave i.e, if accrual reset date is leave occurance 'LO'
   * 
   * @param con
   * @param absence
   * @param absencetypeAccral
   * @return
   * @throws Exception
   */
  String chkexceptionleaveval(Connection con, EHCMAbsenceAttendance absence,
      String absencetypeAccral) throws Exception;

  /**
   * update absence decision status 'Under Processing'/'Issued'
   * 
   * @param absence
   * @throws Exception
   */
  void updateAbsenceDecision(EHCMAbsenceAttendance absence) throws Exception;

  /**
   * update emp leave
   * 
   * @param absence
   * @throws Exception
   */
  void updateEmpLeave(EHCMAbsenceAttendance absence) throws Exception;

  /**
   * update absence decision enable falg yes/no
   * 
   * @param absence
   * @param enableFlag
   * @throws Exception
   */
  void updateAbsenceEnableFlag(EHCMAbsenceAttendance absence, boolean enableFlag) throws Exception;

  /**
   * cancel the emp leave
   * 
   * @param absence
   * @param absencetype
   * @param con
   * @param accrual
   * @param leave
   * @throws Exception
   */
  void cancelEmpLeave(EHCMAbsenceAttendance absence, EHCMAbsenceType absencetype, Connection con,
      EHCMAbsenceTypeAccruals accrual, EHCMEmpLeave leave) throws Exception;

  /**
   * check already dependent associated or not
   * 
   * @param absenceType
   * @return
   * @throws Exception
   */
  Boolean chkAlreadyDepAssorNot(EHCMAbsenceType absenceType) throws Exception;

  /**
   * update the dependent and related absence days
   * 
   * @param absence
   * @throws Exception
   */
  void updateDependentRelatedAbsDays(EHCMAbsenceAttendance absence) throws Exception;

  /**
   * get the holidays count in between of the start date and enddate
   * 
   * @param absence
   * @return
   * @throws Exception
   */
  int countofHolidays(Date startDate, Date endDate, String clientIde, boolean isInclude)
      throws Exception;

  /**
   * get enddate excluding holidays
   * 
   * @param absenceDays
   * @param startDate
   * @return
   * @throws Exception
   */
  Date getEndDate(int absenceDays, Date startDate, String clientId, boolean isInclude)
      throws Exception;

  /**
   * getting hiring employ info record
   * 
   * @param employeeOB
   * @return
   * @throws Exception
   */
  EmploymentInfo getHiringEmployInfo(EhcmEmpPerInfo employeeOB) throws Exception;

  /**
   * get check leave approve msg
   * 
   * @param checkAppMsg-
   *          get from leavecalprocess procedure
   * @param absencetype
   * @return
   * @throws Exception
   */
  String getChkLeaveApproveMsg(String checkAppMsg, EHCMAbsenceType absencetype) throws Exception;

  /**
   * 
   * @param absence
   * @throws Exception
   */
  void deleteEmpLeaveLnForDeductedLeave(EHCMAbsenceAttendance absence) throws Exception;

  /**
   * 
   * @param conn
   * @param absenceattend
   * @param absencetype
   * @param startdate
   * @param enddate
   * @param availabledays
   * @param cancelAbsence
   * @return
   * @throws Exception
   */
  BigDecimal getAvailableAndAvaileddays(Connection conn, EHCMAbsenceAttendance absenceattend,
      EHCMAbsenceType absencetype, String startdate, String enddate, Boolean availabledays,
      EHCMAbsenceAttendance cancelAbsence) throws Exception;

  boolean checkExtendAbsenceTypeLeaveIsTakenBeforeIssueDecision(EHCMAbsenceAttendance absenceattend)
      throws Exception;

}
