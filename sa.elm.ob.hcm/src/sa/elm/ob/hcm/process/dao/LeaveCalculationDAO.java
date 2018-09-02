package sa.elm.ob.hcm.process.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.codehaus.jettison.json.JSONObject;

import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;
import sa.elm.ob.hcm.EHCMEmpLeave;
import sa.elm.ob.hcm.EhcmEmpPerInfo;

/**
 * Interface for all Absence Decision related DB Operations
 * 
 */
public interface LeaveCalculationDAO {

  /**
   * get employee List based on grade if grade or gradelist is null then bringing all the employee
   * or if grade is not null only those grade employee will list or if gradelist is not null then
   * other than this gradelist employee will list
   * 
   * @param absence
   * @return
   * @throws Exception
   */
  List<EhcmEmpPerInfo> getEmployeeList(String ClientId, String gradeClassId, String currentDate,
      String gradeList) throws Exception;

  /**
   * get all accrual list basedon isaccural 'Y' and iscarryforward 'Y'
   * 
   * @param ClientId
   * @param startDate
   * @param endDate
   * @param accrualResetValue
   * @return
   * @throws Exception
   */

  List<EHCMAbsenceTypeAccruals> getAccrualList(String ClientId, String startDate, String endDate,
      String accrualResetValue) throws Exception;

  /**
   * get Employee Leave object based on startdate and enddate
   * 
   * @param absencetype
   * @param employee
   * @param startDate
   * @return
   * @throws Exception
   */

  EHCMEmpLeave getEmpLeave(EHCMAbsenceType absencetype, EhcmEmpPerInfo employee, String startDate)
      throws Exception;

  /**
   * insert emp leave to the next year for adding the opening balance entry
   * 
   * @param absencetype
   * @param employee
   * @param startDate
   * @param endDate
   * @param nextYearStartDate
   * @param prevYearleave
   * @param accrual
   * @return
   */

  EHCMEmpLeave insertEmpLeave(EHCMAbsenceType absencetype, EhcmEmpPerInfo employee,
      String startDate, String endDate, String nextYearStartDate, EHCMEmpLeave prevYearleave,
      EHCMAbsenceTypeAccruals accrual) throws Exception;

  /**
   * insert emp leaveline for adding the opening balance entry
   * 
   * @param conn
   * @param leave
   * @param startdate
   * @param enddate
   * @param accrualdays
   * @param prevYearleave
   * @param nextYearStartDate
   * @return
   * @throws Exception
   */

  int insertEmpLeaveLine(Connection conn, EHCMEmpLeave leave, String startdate, String enddate,
      BigDecimal accrualdays, EHCMEmpLeave prevYearleave, String nextYearStartDate)
      throws Exception;

  boolean chkCurrentDateAsHijiriEnddate(String currentDate, String nxtYearHijiriGerDate)
      throws Exception;

  JSONObject getStartDateAndEndDate(Date startDate, EHCMAbsenceType absencetype, String employeeId)
      throws Exception;

}
