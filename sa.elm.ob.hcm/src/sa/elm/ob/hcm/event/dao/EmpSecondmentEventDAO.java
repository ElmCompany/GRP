package sa.elm.ob.hcm.event.dao;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import sa.elm.ob.hcm.EHCMEmpSecondment;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;

/**
 * Interface for all secondment Event related DB Operations
 * 
 * @author Divya on 31/05/2018
 * 
 */
public interface EmpSecondmentEventDAO {

  /**
   * checking secondment is trying to create on same period
   * 
   * @param secondment
   * @return true or false
   * @throws Exception
   */
  boolean chkCrtSecndSamePeriod(EHCMEmpSecondment secondment) throws Exception;

  /**
   * get promotion startdate
   * 
   * @param secondment
   * @return Promotion start Date
   * @throws Exception
   */
  Date getPromotionStartDate(EHCMEmpSecondment secondment) throws Exception;

  /**
   * checking secondment is creating more than one year
   * 
   * @param startDate
   * @param clientId
   * @param period
   * @return true or false
   * @throws Exception
   */
  boolean oneYearDayValidation(Date startDate, String clientId, BigInteger period) throws Exception;

  /**
   * convert hijiri date to gregorian date
   * 
   * @param hijriDate
   *          yyyymmdd
   * @return gregorian date
   * @throws Exception
   */
  String convertToGregorianDate(String hijriDate) throws Exception;

  /**
   * calculating the month between start date and enddate
   * 
   * @param strstartDate
   * @param strendDate
   * @param ClientId
   * @param secondment
   * @param yearflag
   * @param sixyearflag
   * @return integer value of calculate the month
   * @throws Exception
   */
  int calculateMonths(String strstartDate, String strendDate, String ClientId,
      EHCMEmpSecondment secondment, boolean yearflag, boolean sixyearflag) throws Exception;

  /**
   * checking continuous six year validation
   * 
   * @param secondment
   * @return true or false
   * @throws Exception
   */
  Boolean continuousSixYearVal(EHCMEmpSecondment secondment) throws Exception;

  /**
   * get hijiridate + one day
   * 
   * @param hijiriDate
   *          yyyymmdd
   * @param clientId
   * @return hijiridate
   * @throws Exception
   */
  String getOneDayAddHijiriDate(String hijiriDate, String clientId) throws Exception;

  /**
   * Existing Secondment Months Count
   * 
   * @param secondment
   * @param decisionType
   * @return Existing Secondment Months Count
   * @throws Exception
   */
  int existingMonthCount(EHCMEmpSecondment secondment, String decisionType) throws Exception;

  /**
   * current record months
   * 
   * @param secondment
   * @return Current records Months
   * @throws Exception
   */
  int currentMonths(EHCMEmpSecondment secondment) throws Exception;

  /**
   * checking three year validation for secondment
   * 
   * @param secondment
   * @param existingMonth
   * @param currentMonth
   * @return true or false
   * @throws Exception
   */
  Boolean threeYearValidation(EHCMEmpSecondment secondment, int existingMonth, int currentMonth)
      throws Exception;

  /**
   * getting delegation list based on employ info
   * 
   * @param employinfo
   * @return delegation list
   * @throws Exception
   */
  List<EmployeeDelegation> getDelegationList(EmploymentInfo employinfo) throws Exception;

  /**
   * get active delegation new position based on employee
   * 
   * @param secondment
   * @return positionObj
   * @throws Exception
   */
  EhcmPosition getDelegationPosition(EHCMEmpSecondment secondment) throws Exception;

  /**
   * checking promotion day validation while doing secondment
   * 
   * @param promStartDate
   * @param secondStartDate
   * @param clientId
   * @return true or false
   * @throws Exception
   */
  Boolean promotionDayVal(String promStartDate, String secondStartDate, String clientId)
      throws Exception;

  /**
   * get hijiridate - one day
   * 
   * @param hijiriDate
   *          yyyymmdd
   * @param clientId
   * @return hijiridate - one day
   * @throws Exception
   */
  String getOneDayMinusHijiriDate(String hijiriDate, String clientId) throws Exception;

  /**
   * checking promotion validation
   * 
   * @param secondment
   * @return true or false
   * @throws Exception
   */
  Boolean promotionVal(EHCMEmpSecondment secondment) throws Exception;

  /**
   * get current emply info startdate
   * 
   * @param secondment
   * @return employmentInfoObj
   */
  EmploymentInfo getCurrentEmplyInfoStartDate(EHCMEmpSecondment secondment) throws Exception;

  /**
   * year validation
   * 
   * @param secondment
   * @param decisionType
   * @param continuousBlock
   * @param year
   * @return
   * @throws Exception
   */
  Boolean YearValidationForSecondment(EHCMEmpSecondment secondment, String decisionType, int year)
      throws Exception;

  /**
   * three year validation for secondment
   * 
   * @param secondment
   * @param decisionType
   * @param year
   * @return true or false, if false dont allow to create secondment
   * @throws Exception
   */
  Boolean threeYearValForSecondment(EHCMEmpSecondment secondment, String decisionType, int year)
      throws Exception;

  /**
   * three years validation
   * 
   * @param secondment
   * @param decisionType
   * @param year
   * @return
   * @throws Exception
   */
  Boolean threeYearValidationForSecondment(EHCMEmpSecondment secondment, String decisionType,
      int year) throws Exception;

}
