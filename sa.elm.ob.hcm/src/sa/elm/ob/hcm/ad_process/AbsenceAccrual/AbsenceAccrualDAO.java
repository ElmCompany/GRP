package sa.elm.ob.hcm.ad_process.AbsenceAccrual;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.codehaus.jettison.json.JSONObject;

import sa.elm.ob.hcm.EHCMAbsenceAccrual;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMDeflookupsTypeLn;

/**
 * Interface for all Absence Decision related DB Operations
 * 
 * @author divya -09-04-2018
 *
 */
public interface AbsenceAccrualDAO {
  /**
   * get available and availed days
   * 
   * @param conn
   * @param absenceaccrual
   * @param absencetype
   * @param startdate
   * @param enddate
   * @param availabledays
   * @return
   */
  JSONObject getAvailableAndAvaileddays(Connection conn, EHCMAbsenceAccrual absenceaccrual,
      EHCMAbsenceType absencetype, String startdate, String enddate, Boolean availabledays,
      String subTypeId) throws Exception;

  /**
   * insert absence accrual details
   * 
   * @param conn
   * @param absenceaccrual
   * @param absencetype
   * @param startdate
   * @param enddate
   * @param entitilement
   * @param availeddays
   * @return
   */
  int insertAbsenceAccrualDetails(EHCMAbsenceAccrual absenceaccrual, EHCMAbsenceType absencetype,
      Date startdate, Date enddate, BigDecimal entitilement, BigDecimal availeddays,
      EHCMDeflookupsTypeLn subType) throws Exception;

  /**
   * get startdate and enddate of the absence accrual
   * 
   * @param startDate
   * @param absencetype
   * @param employeeId
   * @return
   */
  JSONObject getStartDateAndEndDate(String startDate, EHCMAbsenceType absencetype,
      String employeeId) throws Exception;

  /**
   * before process delete the previous absence accrual lines
   * 
   * @param absenceaccrualId
   */
  void deletePrevAbsenceAccrual(String absenceaccrualId) throws Exception;

  /**
   * get accrual list from absence type
   * 
   * @param absenceaccrual
   * @return
   */
  List<EHCMAbsenceType> getAccrualList(EHCMAbsenceAccrual absenceaccrual) throws Exception;

  /**
   * get Absence sub type list, if absence type- issubtype checked as 'Yes' then get sub type lsit
   * from reference lookup reference lookup name-Absence Sub Type(AS)
   * 
   * @param absenceType
   * @return
   * @throws Exception
   */
  List<EHCMDeflookupsTypeLn> getAbsenceSubTypeListFromRefLookup(EHCMAbsenceType absenceType)
      throws Exception;

  int calStartDateEndDateAndInsertAbsAccuralDetails(JSONObject availAvailableDaysRes,
      String calculationDate, EHCMAbsenceType absenceType, EHCMAbsenceAccrual absenceAccruals,
      EHCMDeflookupsTypeLn subType) throws Exception;

  JSONObject getAbsenceAccrualList(String clientId, String employeeId, String absenceTypeId,
      String calculationDate, JSONObject searchAttr, Connection conn) throws Exception;

  JSONObject getEmployeeDetails(String employeeId, String lang) throws Exception;

  public String getEmployeeStatusName(String value, String lang) throws Exception;
}