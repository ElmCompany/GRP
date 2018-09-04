/*
 * All Rights Reserved By Qualian Technologies Pvt Ltd.
 */
package sa.elm.ob.hcm.event.dao;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.geography.City;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EHCMBusMissionSummary;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMMisCatPeriod;
import sa.elm.ob.hcm.EHCMMisEmpCategory;
import sa.elm.ob.hcm.EHCMMiscatEmployee;
import sa.elm.ob.hcm.EHCMMissionCategory;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.ehcmgradeclass;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.hcm.util.UtilityDAO;

/**
 * 
 * @author divya -03-03-2018
 *
 */
@Repository
public class MissionCategoryDAOImpl implements MissionCategoryDAO {

  private static final Logger LOG = Logger.getLogger(MissionCategoryDAOImpl.class);
  DateFormat YearFormat = sa.elm.ob.utility.util.Utility.YearFormat;
  DateFormat dateFormat = sa.elm.ob.utility.util.Utility.dateFormat;

  /**
   * get Recent Period Record
   * 
   * @param misEmpCategoy
   * @return
   */
  public EHCMMisCatPeriod getRecentEmpCategoryPeriod(EHCMMissionCategory misEmpCategoy) {

    EHCMMisCatPeriod empMiscatPeriod = null;
    List<EHCMMisCatPeriod> empMiscatPeriodList = new ArrayList<EHCMMisCatPeriod>();
    try {
      OBQuery<EHCMMisCatPeriod> empMiscatPeriodQry = OBDal.getInstance()
          .createQuery(EHCMMisCatPeriod.class, " as e where "
              + " to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') = ( select "
              + " max(to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy')) "
              + " from EHCM_MisCat_Period e where e.ehcmMissionCategory.id=:misCatId ) and  e.ehcmMissionCategory.id=:misCatId     ");
      empMiscatPeriodQry.setNamedParameter("misCatId", misEmpCategoy.getId());
      empMiscatPeriodQry.setNamedParameter("misCatId", misEmpCategoy.getId());
      empMiscatPeriodList = empMiscatPeriodQry.list();
      if (empMiscatPeriodList.size() > 0) {
        empMiscatPeriod = empMiscatPeriodList.get(0);
      }
      return empMiscatPeriod;
    } catch (Exception e) {
      LOG.error("Exception in getRecentEmpCategoryPeriod: ", e);
      OBDal.getInstance().rollbackAndClose();
      return empMiscatPeriod;
    } finally {
    }
  }

  /**
   * insert employee for recent period if category added in mission category
   * 
   * @param misEmpCategoy
   * @param user
   */
  public void insertMisCatEmployee(EHCMMisEmpCategory misEmpCategoy, User user) {

    EHCMMisCatPeriod empMiscatPeriod = null;
    EHCMMiscatEmployee misCatEmployee = null;
    List<EhcmEmpPerInfo> empList = new ArrayList<EhcmEmpPerInfo>();
    try {
      empMiscatPeriod = getRecentEmpCategoryPeriod(misEmpCategoy.getEhcmMissionCategory());
      if (empMiscatPeriod != null) {
        OBQuery<EhcmEmpPerInfo> empQry = OBDal.getInstance().createQuery(EhcmEmpPerInfo.class,
            " as e where e.gradeClass.id=:gradeclassId  and e.client.id=:clientId");
        empQry.setNamedParameter("gradeclassId", misEmpCategoy.getGradeClassifications().getId());
        empQry.setNamedParameter("clientId", misEmpCategoy.getClient().getId());
        if (empQry != null) {
          empList = empQry.list();
          if (empList.size() > 0) {
            for (EhcmEmpPerInfo emp : empList) {
              misCatEmployee = OBProvider.getInstance().get(EHCMMiscatEmployee.class);
              misCatEmployee.setClient(misEmpCategoy.getClient());
              misCatEmployee.setOrganization(misEmpCategoy.getOrganization());
              misCatEmployee.setCreatedBy(user);
              misCatEmployee.setCreationDate(new java.util.Date());
              misCatEmployee.setUpdated(new java.util.Date());
              misCatEmployee.setUpdatedBy(user);
              misCatEmployee.setEmployee(emp);
              misCatEmployee.setEhcmMiscatPeriod(empMiscatPeriod);
              OBDal.getInstance().save(misCatEmployee);
            }
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Exception in insertMisCatEmployee: ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
    }
  }

  /**
   * insert employee if period added in mission category period
   * 
   * @param misCategoyPrd
   * @param user
   */
  public void insertMisCatEmployeeUsingPrd(EHCMMisCatPeriod misCategoyPrd, User user,
      Boolean refreshEmployee) {

    EHCMMiscatEmployee misCatEmployee = null;
    List<EhcmEmpPerInfo> empList = new ArrayList<EhcmEmpPerInfo>();
    String refreshhql = "";
    try {

      if (refreshEmployee) {
        refreshhql = " and e.id not in ( select misscatemp.employee.id  from  EHCM_Miscat_Employee misscatemp  where misscatemp.ehcmMiscatPeriod.id=:misscatPerId )  ";
      }
      if (misCategoyPrd != null) {
        for (EHCMMisEmpCategory empCatgry : misCategoyPrd.getEhcmMissionCategory()
            .getEHCMMisEmpCategoryList()) {
          OBQuery<EhcmEmpPerInfo> empQry = OBDal.getInstance().createQuery(EhcmEmpPerInfo.class,
              " as e where e.gradeClass.id=:gradeclassId  and e.client.id=:clientId " + refreshhql);
          empQry.setNamedParameter("gradeclassId", empCatgry.getGradeClassifications().getId());
          empQry.setNamedParameter("clientId", empCatgry.getClient().getId());
          if (refreshEmployee) {
            empQry.setNamedParameter("misscatPerId", misCategoyPrd.getId());
          }
          if (empQry != null) {
            empList = empQry.list();
            if (empList.size() > 0) {
              for (EhcmEmpPerInfo emp : empList) {
                misCatEmployee = OBProvider.getInstance().get(EHCMMiscatEmployee.class);
                misCatEmployee.setClient(misCategoyPrd.getClient());
                misCatEmployee.setOrganization(misCategoyPrd.getOrganization());
                misCatEmployee.setCreatedBy(user);
                misCatEmployee.setCreationDate(new java.util.Date());
                misCatEmployee.setUpdated(new java.util.Date());
                misCatEmployee.setUpdatedBy(user);
                misCatEmployee.setEmployee(emp);
                misCatEmployee.setEhcmMiscatPeriod(misCategoyPrd);
                OBDal.getInstance().save(misCatEmployee);
              }
            }
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Exception in insertMisCatEmployee: ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
    }
  }

  /**
   * check mission period already exists
   * 
   * @param misCatPerd
   * @return
   */
  public Boolean checkMisPerdAlrdyExistorNot(EHCMMisCatPeriod misCatPerd) {

    Boolean misCatPrdExists = false;
    String todate = null;
    String fromdate = null;
    List<EHCMMisCatPeriod> misCatPerdList = new ArrayList<EHCMMisCatPeriod>();
    try {

      fromdate = sa.elm.ob.utility.util.Utility.formatDate(misCatPerd.getStartDate());
      if (misCatPerd.getEndDate() == null) {
        todate = "21-06-2058";
      } else {
        todate = sa.elm.ob.utility.util.Utility.formatDate(misCatPerd.getEndDate());
      }
      OBQuery<EHCMMisCatPeriod> misCatPerdQry = OBDal.getInstance()
          .createQuery(EHCMMisCatPeriod.class, " as e where e.ehcmMissionCategory.id=:misCatId "
              + " and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
              + " and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
              + " or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
              + " and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) and e.id <> :id  ");

      misCatPerdQry.setNamedParameter("misCatId", misCatPerd.getEhcmMissionCategory().getId());
      misCatPerdQry.setNamedParameter("fromdate", fromdate);
      misCatPerdQry.setNamedParameter("todate", todate);
      misCatPerdQry.setNamedParameter("id", misCatPerd.getId());
      LOG.debug("empManagerHistQry:" + misCatPerdQry.getWhereAndOrderBy());
      misCatPerdList = misCatPerdQry.list();
      if (misCatPerdList.size() > 0) {
        misCatPrdExists = true;
      } else {
        misCatPrdExists = false;
      }

      return misCatPrdExists;
    } catch (Exception e) {
      LOG.error("Exception in checkMisPerdAlrdyExistorNot: ", e);
      OBDal.getInstance().rollbackAndClose();
      return misCatPrdExists;
    } finally {
    }
  }

  /**
   * check category already added or not
   * 
   * @param categoryId
   * @param misEmpCategoy
   * @return
   */
  public boolean checkCategoryAlreadyAdded(String categoryId, EHCMMisEmpCategory misEmpCategoy) {
    List<EHCMMisEmpCategory> misEmpCatList = new ArrayList<EHCMMisEmpCategory>();
    try {
      OBContext.setAdminMode();
      OBQuery<EHCMMisEmpCategory> misEmpCatQry = OBDal.getInstance().createQuery(
          EHCMMisEmpCategory.class,
          " as e where e.gradeClassifications.id=:categoryId and e.ehcmMissionCategory.id=:miscatId and e.id <>:id ");
      misEmpCatQry.setNamedParameter("categoryId", categoryId);
      misEmpCatQry.setNamedParameter("miscatId", misEmpCategoy.getEhcmMissionCategory().getId());
      misEmpCatQry.setNamedParameter("id", misEmpCategoy.getId());
      misEmpCatList = misEmpCatQry.list();
      if (misEmpCatList.size() > 0) {
        return true;
      } else
        return false;
    } catch (Exception e) {
      LOG.error("Exception in checkCategoryAlreadyAdded: ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
    }
    return false;
  }

  /**
   * get Employee obj in particular period
   * 
   * @param misEmpCategoyPeriod
   * @param employeeId
   * @return
   */
  public EHCMMiscatEmployee getEmployeeinPeriod(EHCMMisCatPeriod misEmpCategoyPeriod,
      String employeeId) {
    List<EHCMMiscatEmployee> misCatEmpList = new ArrayList<EHCMMiscatEmployee>();
    EHCMMiscatEmployee misCatEmp = null;
    try {
      OBContext.setAdminMode();
      OBQuery<EHCMMiscatEmployee> misCatEmpQry = OBDal.getInstance().createQuery(
          EHCMMiscatEmployee.class,
          " as e where e.employee.id=:employeeId and e.ehcmMiscatPeriod.id=:miscatPerdId ");
      misCatEmpQry.setNamedParameter("employeeId", employeeId);
      misCatEmpQry.setNamedParameter("miscatPerdId", misEmpCategoyPeriod.getId());
      misCatEmpQry.setFilterOnActive(false);
      misCatEmpQry.setFilterOnReadableClients(false);
      misCatEmpQry.setFilterOnReadableOrganization(false);
      misCatEmpList = misCatEmpQry.list();

      if (misCatEmpList.size() > 0) {
        misCatEmp = misCatEmpList.get(0);
      }
      return misCatEmp;
    } catch (Exception e) {
      LOG.error("Exception in getEmployeeinPeriod: ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
      OBContext.restorePreviousMode();
    }
    return misCatEmp;
  }

  /**
   * chk employee is used in business mission category based on used days >0
   * 
   * @param misEmpCategoyPeriod
   * @param misEmpCategory
   * @return
   */
  public boolean chkAnyEmployeeUsedDaysGrtZero(EHCMMisCatPeriod misEmpCategoyPeriod,
      EHCMMisEmpCategory misEmpCategory) {
    List<EHCMMiscatEmployee> misCatEmpList = new ArrayList<EHCMMiscatEmployee>();
    EHCMMiscatEmployee misCatEmp = null;
    boolean chkempUsedDays = false;
    try {
      OBContext.setAdminMode();
      OBQuery<EHCMMiscatEmployee> misCatEmpQry = OBDal.getInstance().createQuery(
          EHCMMiscatEmployee.class, " as e where  e.ehcmMiscatPeriod.id=:miscatPerdId  "
              + (misEmpCategory != null ? " and e.employee.gradeClass.id =:gradeclassId " : " "));
      misCatEmpQry.setNamedParameter("miscatPerdId", misEmpCategoyPeriod.getId());
      if (misEmpCategory != null)
        misCatEmpQry.setNamedParameter("gradeclassId",
            misEmpCategory.getGradeClassifications().getId());
      misCatEmpQry.setFilterOnActive(false);
      misCatEmpList = misCatEmpQry.list();
      if (misCatEmpList.size() > 0) {
        for (EHCMMiscatEmployee emp : misCatEmpList) {
          if (emp.getUseddays() > 0) {
            chkempUsedDays = true;
            break;
          }
        }
      }
      return chkempUsedDays;
    } catch (Exception e) {
      LOG.error("Exception in chkAnyEmployeeUsedDaysGrtZero: ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
    }
    return chkempUsedDays;
  }

  /**
   * in country window if we change no of after and before then update to city no of after & before
   * 
   * @param countryId
   * @param noofdaysAfter
   * @param noofdaysBefore
   * @param category
   */
  public void updateNoofDaysAfterBefore(String countryId, Long noofdaysAfter, Long noofdaysBefore,
      String category) {
    List<City> cityList = new ArrayList<City>();
    try {
      OBContext.setAdminMode();
      OBQuery<City> cityQry = OBDal.getInstance().createQuery(City.class,
          " as e where e.country.id=:countyrId ");
      cityQry.setNamedParameter("countyrId", countryId);
      cityList = cityQry.list();
      if (cityList.size() > 0) {
        for (City city : cityList) {
          if (noofdaysAfter != null)
            city.setEHCMNoDaysAfter(noofdaysAfter);
          if (noofdaysBefore != null)
            city.setEHCMNoDaysBefore(noofdaysBefore);
          if (category != null)
            city.setEhcmCategory(category);
          OBDal.getInstance().save(city);
        }
      }
    } catch (Exception e) {
      LOG.error("Exception in chkAnyEmployeeUsedDaysGrtZero: ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
    }
  }

  /**
   * if category will delete then remove all mission category employee for that categoy from the
   * recent period
   * 
   * @param misEmpCategoyPeriod
   * @param misEmpCategory
   */

  public void deleteMisCatEmployee(EHCMMisCatPeriod misEmpCategoyPeriod,
      EHCMMisEmpCategory misEmpCategory) {
    List<EHCMMiscatEmployee> misCatEmpList = new ArrayList<EHCMMiscatEmployee>();
    try {
      OBContext.setAdminMode();
      OBQuery<EHCMMiscatEmployee> misCatEmpQry = OBDal.getInstance().createQuery(
          EHCMMiscatEmployee.class,
          " as e where  e.ehcmMiscatPeriod.id=:miscatPerdId   and e.employee.gradeClass.id =:gradeclassId ");
      misCatEmpQry.setNamedParameter("miscatPerdId", misEmpCategoyPeriod.getId());
      misCatEmpQry.setNamedParameter("gradeclassId",
          misEmpCategory.getGradeClassifications().getId());
      misCatEmpQry.setFilterOnActive(false);
      misCatEmpList = misCatEmpQry.list();
      if (misCatEmpList.size() > 0) {
        for (EHCMMiscatEmployee emp : misCatEmpList) {
          OBDal.getInstance().remove(emp);
        }
      }
    } catch (Exception e) {
      LOG.error("Exception in deleteMisCatEmployee: ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
    }
  }

  /**
   * refresh the Employee will add the employee which is not added in recent misssion category
   * period
   * 
   * @param misEmpCategory
   * @param user
   * @return
   */
  public boolean addNewEmployeesToRecentPeriod(EHCMMissionCategory misCategory, User user) {
    EHCMMisCatPeriod missCatPeriod = null;
    List<EHCMMisCatPeriod> empMiscatPeriodList = new ArrayList<EHCMMisCatPeriod>();

    try {
      OBContext.setAdminMode();
      // get Recent Period
      missCatPeriod = getRecentEmpCategoryPeriod(misCategory);

      if (missCatPeriod != null) {
        insertMisCatEmployeeUsingPrd(missCatPeriod, user, true);
        return true;
      }
    } catch (Exception e) {
      LOG.error("Exception in addNewEmployeesToRecentPeriod: ", e);
      OBDal.getInstance().rollbackAndClose();
      return false;
    } finally {
    }
    return false;
  }

  @Override
  public boolean addNewEmployeesToAllPeriodGreaterThanOfEmpStartDate(
      EHCMMissionCategory misCategory, User user, ehcmgradeclass gradeClass,
      EhcmEmpPerInfo employee) {
    List<EhcmEmpPerInfo> empList = new ArrayList<EhcmEmpPerInfo>();
    EHCMMiscatEmployee misCatEmployee = null;
    String refreshhql = "";
    int a = 0;
    try {
      OBContext.setAdminMode();
      // get Recent Period
      refreshhql = " and e.id not in ( select misscatemp.employee.id  from  EHCM_Miscat_Employee misscatemp  where misscatemp.ehcmMiscatPeriod.id=:misscatPerId ) ";

      if (employee != null) {
        refreshhql += " and e.id=:employeeId ";
      }
      for (EHCMMisCatPeriod empCatgryPeriod : misCategory.getEHCMMisCatPeriodList()) {

        for (EHCMMisEmpCategory empCategory : misCategory.getEHCMMisEmpCategoryList()) {
          if ((gradeClass != null
              && (gradeClass.getId().equals(empCategory.getGradeClassifications().getId())))
              || gradeClass == null) {
            OBQuery<EhcmEmpPerInfo> empQry = OBDal.getInstance().createQuery(EhcmEmpPerInfo.class,
                " as e where e.gradeClass.id=:gradeclassId  and e.client.id=:clientId  and ( e.startDate <=:periodstartDate or e.startDate <=:periodendDate )  and e.status<>'UP' and e.enabled='Y'"
                    + refreshhql);
            empQry.setNamedParameter("gradeclassId", empCategory.getGradeClassifications().getId());
            empQry.setNamedParameter("clientId", empCategory.getClient().getId());
            empQry.setNamedParameter("periodstartDate", empCatgryPeriod.getStartDate());
            empQry.setNamedParameter("periodendDate", empCatgryPeriod.getEndDate());
            empQry.setNamedParameter("misscatPerId", empCatgryPeriod.getId());
            if (employee != null) {
              empQry.setNamedParameter("employeeId", employee.getId());
            }
            if (empQry != null) {
              empList = empQry.list();
              if (empList.size() > 0) {
                for (EhcmEmpPerInfo emp : empList) {
                  misCatEmployee = OBProvider.getInstance().get(EHCMMiscatEmployee.class);
                  misCatEmployee.setClient(empCatgryPeriod.getClient());
                  misCatEmployee.setOrganization(empCatgryPeriod.getOrganization());
                  misCatEmployee.setCreatedBy(user);
                  misCatEmployee.setCreationDate(new java.util.Date());
                  misCatEmployee.setUpdated(new java.util.Date());
                  misCatEmployee.setUpdatedBy(user);
                  misCatEmployee.setEmployee(emp);
                  misCatEmployee.setEhcmMiscatPeriod(empCatgryPeriod);
                  OBDal.getInstance().save(misCatEmployee);
                }
              }
            }
          }
        }
      }
      return true;
    } catch (Exception e) {
      LOG.error("Exception in addNewEmployeesToAllPeriodGreaterThanOfEmpStartDate: ", e);
      OBDal.getInstance().rollbackAndClose();
      return false;
    } finally {
    }
  }

  public List<EHCMMisCatPeriod> getAllMissionCatPeriodListForEmployeeBasOnStrtDate(
      EhcmEmpPerInfo person, EHCMMissionCategory missionCategory) {
    List<EHCMMisCatPeriod> empMiscatPeriodList = new ArrayList<EHCMMisCatPeriod>();

    try {
      OBContext.setAdminMode();
      OBQuery<EHCMMisCatPeriod> missCatPeriodQry = OBDal.getInstance().createQuery(
          EHCMMisCatPeriod.class, " as e where e.ehcmMissionCategory.id=:missionCategoryId "
              + " and (  startdate >=:empStartDate   or enddate >=:empStartDate    ) ");
      missCatPeriodQry.setNamedParameter("missionCategoryId", missionCategory.getId());
      missCatPeriodQry.setNamedParameter("empStartDate", person.getStartDate());
      empMiscatPeriodList = missCatPeriodQry.list();
      if (empMiscatPeriodList.size() > 0) {
        return empMiscatPeriodList;
      }
    } catch (Exception e) {
      LOG.error("Exception in addNewEmployeesToRecentPeriod: ", e);
      OBDal.getInstance().rollbackAndClose();
      return empMiscatPeriodList;
    } finally {
    }
    return empMiscatPeriodList;
  }

  /**
   * get maximum used days for the mission category period
   * 
   * @param misPeriod
   * @return
   */
  @SuppressWarnings("unchecked")
  public BigDecimal getMaxUsedDaysForMisPerd(EHCMMisCatPeriod misPeriod) {
    String sql = null;
    Query qry = null;
    BigDecimal maxUsedDays = BigDecimal.ZERO;
    try {
      OBContext.setAdminMode();
      // get Recent Period
      sql = " select max(useddays) as maxuseddays from ehcm_miscat_employee  where ehcm_miscat_period_id=:misCatPeriod ";
      qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      qry.setParameter("misCatPeriod", misPeriod.getId());

      if (qry != null && qry.list().size() > 0) {
        List<Object> object = qry.list();
        Object row = object.get(0);
        maxUsedDays = (BigDecimal) row;
        return maxUsedDays;
      } else
        return maxUsedDays;
    } catch (Exception e) {
      LOG.error("Exception in getMaxUsedDaysForMisPerd: ", e);
      OBDal.getInstance().rollbackAndClose();
      return maxUsedDays;
    } finally {
    }
  }

  public boolean repeatMissionCatPeriod(String clientId, User user) {
    List<EHCMMissionCategory> misCategoryList = new ArrayList<EHCMMissionCategory>();
    EHCMMisCatPeriod missCatPeriod = null;
    EHCMMisEmpCategory missionEmpCategory = null;
    Date currentDate = new Date();
    EHCMMisCatPeriod newmissCatPeriod = null;

    try {
      OBContext.setAdminMode();

      // fetch all mission category for the client
      OBQuery<EHCMMissionCategory> misCategoryQry = OBDal.getInstance()
          .createQuery(EHCMMissionCategory.class, " as e where e.client.id=:clientId ");
      misCategoryQry.setNamedParameter("clientId", clientId);
      misCategoryList = misCategoryQry.list();
      if (misCategoryList.size() > 0) {
        for (EHCMMissionCategory mission : misCategoryList) {
          // get recent mission category period for each mission
          missCatPeriod = getRecentEmpCategoryPeriod(mission);
          if (missCatPeriod != null) {
            if (missCatPeriod.isRepeat() && missCatPeriod.getEndDate()
                .compareTo(YearFormat.parse(YearFormat.format(currentDate))) == 0) {
              newmissCatPeriod = insertRepeatMisCatPeriod(user, missCatPeriod);
              return true;
              // insert employee
              // insertMisCatEmployeeUsingPrd( newmissCatPeriod, user, false);
            }
          }
        }
      }

    } catch (Exception e) {
      LOG.error("Exception in getMaxUsedDaysForMisPerd: ", e);
      OBDal.getInstance().rollbackAndClose();
      return false;
    } finally {
    }
    return false;
  }

  public EHCMMisCatPeriod insertRepeatMisCatPeriod(User user, EHCMMisCatPeriod oldMisCatPeriod) {
    EHCMMisCatPeriod misCatPeriod = null;
    int dayone = 2;
    Date oldPeriodendDatePlus = null;
    Date calEnddate = null;
    int diff = 0;
    try {
      // get startdate based on previous mission category period enddte +1;
      oldPeriodendDatePlus = Utility.calDateUsingDaysWithGreDate(
          oldMisCatPeriod.getClient().getId(), dayone, oldMisCatPeriod.getEndDate());

      // calculate the difference
      diff = Utility.caltheDaysUsingGreDate(oldMisCatPeriod.getStartDate(),
          oldMisCatPeriod.getEndDate());

      // calculate the enddate based on previous period enddate+1 and diff of previous period
      // startdate and enddate
      calEnddate = Utility.calDateUsingDaysWithGreDate(oldMisCatPeriod.getClient().getId(), diff,
          oldPeriodendDatePlus);

      misCatPeriod = OBProvider.getInstance().get(EHCMMisCatPeriod.class);
      misCatPeriod.setClient(oldMisCatPeriod.getClient());
      misCatPeriod.setOrganization(oldMisCatPeriod.getOrganization());
      misCatPeriod.setCreatedBy(user);
      misCatPeriod.setCreationDate(new java.util.Date());
      misCatPeriod.setUpdated(new java.util.Date());
      misCatPeriod.setUpdatedBy(user);
      misCatPeriod.setEhcmMissionCategory(oldMisCatPeriod.getEhcmMissionCategory());
      misCatPeriod.setStartDate(oldPeriodendDatePlus);
      misCatPeriod.setEndDate(calEnddate);
      misCatPeriod.setDays(oldMisCatPeriod.getDays());
      misCatPeriod.setRepeat(true);
      OBDal.getInstance().save(misCatPeriod);
    } catch (Exception e) {
      LOG.error("Exception in insertRepeatMisCatPeriod: ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
    }
    return misCatPeriod;
  }

  public JSONObject chkAnyEmpFallOnChangePeriod(String startDate, String endDate,
      EHCMMissionCategory misCategory) {
    JSONObject result = new JSONObject();
    String sql = null;
    String sql1 = null;
    List<EHCMBusMissionSummary> businessMissionSummList = new ArrayList<EHCMBusMissionSummary>();
    try {

      sql = "  where ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate)  "
          + " and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + " or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + " and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) and e.ehcmEmpBusinessmission.missionCategory.id=:misCatId  ";

      sql1 = "   order by e.creationDate desc  ";

      OBQuery<EHCMBusMissionSummary> businessMissionSummQry = OBDal.getInstance()
          .createQuery(EHCMBusMissionSummary.class, " as e    " + sql + sql1);
      businessMissionSummQry.setNamedParameter("fromdate", startDate);
      businessMissionSummQry.setNamedParameter("todate", endDate);
      businessMissionSummQry.setNamedParameter("misCatId", misCategory.getId());
      businessMissionSummQry.setMaxResult(1);
      businessMissionSummList = businessMissionSummQry.list();
      if (businessMissionSummList.size() > 0) {
        EHCMEmpBusinessMission empbusinessMission = businessMissionSummList.get(0)
            .getEhcmEmpBusinessmission();
        // if current decision is cutoff
        if (empbusinessMission.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {

          result = UtilityDAO.chkCutOffDecisionOverlap(Constants.BUSINESSMISSION_OVERLAP,
              empbusinessMission.getId(), startDate, endDate,
              empbusinessMission.getEmployee().getId(), sql, sql1, empbusinessMission.getId());
          if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
            result.put("errormsg", OBMessageUtils.messageBD("EHCM_MisCatPeriodCantChagDate"));
            return result;
          }

        } else {
          result.put("errorFlag", true);
          result.put("errormsg", OBMessageUtils.messageBD("EHCM_MisCatPeriodCantChagDate"));
          result.put("businessMissionId", empbusinessMission.getId());
          return result;
        }
      }
    } catch (Exception e) {
      LOG.error("Exception in chkAnyEmpFallOnChangePeriod: ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
    }
    return result;
  }
}