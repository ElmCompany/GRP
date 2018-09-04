package sa.elm.ob.hcm.event.dao;

import java.math.BigDecimal;

import org.openbravo.model.ad.access.User;

import sa.elm.ob.hcm.EHCMMisCatPeriod;
import sa.elm.ob.hcm.EHCMMisEmpCategory;
import sa.elm.ob.hcm.EHCMMiscatEmployee;
import sa.elm.ob.hcm.EHCMMissionCategory;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ehcmgradeclass;

/**
 * Interface for all Mission category related DB Operations
 * 
 * @author divya -23-02-2018
 *
 */
public interface MissionCategoryDAO {

  /**
   * get Recent Period Record
   * 
   * @param misEmpCategoy
   * @return
   */
  EHCMMisCatPeriod getRecentEmpCategoryPeriod(EHCMMissionCategory misEmpCategoy) throws Exception;

  /**
   * insert employee for recent period if category added in mission category
   * 
   * @param misEmpCategoy
   * @param user
   */
  void insertMisCatEmployee(EHCMMisEmpCategory misEmpCategoy, User user) throws Exception;

  /**
   * insert employee if period added in mission category period
   * 
   * @param misCategoyPrd
   * @param user
   */
  void insertMisCatEmployeeUsingPrd(EHCMMisCatPeriod misCategoyPrd, User user,
      Boolean refreshEmployee) throws Exception;

  /**
   * check mission period already exists
   * 
   * @param misCatPerd
   * @return
   */
  Boolean checkMisPerdAlrdyExistorNot(EHCMMisCatPeriod misCatPerd) throws Exception;

  /**
   * check category already added or not
   * 
   * @param categoryId
   * @param misEmpCategoy
   * @return
   */
  boolean checkCategoryAlreadyAdded(String categoryId, EHCMMisEmpCategory misEmpCategoy)
      throws Exception;

  /**
   * get Employee obj in particular period
   * 
   * @param misEmpCategoyPeriod
   * @param employeeId
   * @return
   */
  EHCMMiscatEmployee getEmployeeinPeriod(EHCMMisCatPeriod misEmpCategoyPeriod, String employeeId)
      throws Exception;

  /**
   * chk employee is used in business mission category based on used days >0
   * 
   * @param misEmpCategoyPeriod
   * @param misEmpCategory
   * @return
   */
  boolean chkAnyEmployeeUsedDaysGrtZero(EHCMMisCatPeriod misEmpCategoyPeriod,
      EHCMMisEmpCategory misEmpCategory) throws Exception;

  /**
   * in country window if we change no of after and before then update to city no of after & before
   * 
   * @param countryId
   * @param noofdaysAfter
   * @param noofdaysBefore
   * @param category
   */
  void updateNoofDaysAfterBefore(String countryId, Long noofdaysAfter, Long noofdaysBefore,
      String category) throws Exception;

  /**
   * if category will delete then remove all mission category employee for that categoy from the
   * recent period
   * 
   * @param misEmpCategoyPeriod
   * @param misEmpCategory
   */

  void deleteMisCatEmployee(EHCMMisCatPeriod misEmpCategoyPeriod, EHCMMisEmpCategory misEmpCategory)
      throws Exception;

  /**
   * get maximum used days for the mission category period
   * 
   * @param misPeriod
   * @return
   */
  BigDecimal getMaxUsedDaysForMisPerd(EHCMMisCatPeriod misPeriod) throws Exception;

  /**
   * add employee in mission category employee tab
   * 
   * @param misCategory
   * @param user
   * @return
   * @throws Exception
   */
  boolean addNewEmployeesToRecentPeriod(EHCMMissionCategory misCategory, User user)
      throws Exception;

  /**
   * 
   * @param misCategory
   * @param user
   * @param gradeClass
   * @param employee
   * @return
   * @throws Exception
   */
  boolean addNewEmployeesToAllPeriodGreaterThanOfEmpStartDate(EHCMMissionCategory misCategory,
      User user, ehcmgradeclass gradeClass, EhcmEmpPerInfo employee) throws Exception;

}
