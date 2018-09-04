package sa.elm.ob.hcm.event.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.EHCMEmpSecondment;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * Implementation for all secondment Event related DB Operations
 * 
 * @author divya on 31/05/2018
 * 
 */
// Job Event DAO Implement file
public class EmpSecondmentEventDAOImpl implements EmpSecondmentEventDAO {
  private static Logger log4j = Logger.getLogger(EmpSecondmentEventDAOImpl.class);
  DateFormat yearFormat = Utility.YearFormat;
  DateFormat dateFormat = Utility.dateFormat;

  public boolean chkCrtSecndSamePeriod(EHCMEmpSecondment secondment) {
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    OBQuery<EmploymentInfo> empInfo = null;
    String hql = null;
    try {

      hql = " and ((to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate,'dd-MM-yyyy') "
          + " and to_date(to_char(coalesce (endDate,startDate),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + " or (to_date(to_char( coalesce (endDate,startDate) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate,'dd-MM-yyyy') "
          + " and to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')))"
          + " and  ehcmEmpSecondment.id is not null ";
      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and client.id=:clientId and enabled ='Y'" + hql);
      empInfo.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      empInfo.setNamedParameter("clientId", secondment.getClient().getId());
      empInfo.setNamedParameter("startDate", Utility.formatDate(secondment.getStartDate()));
      empInfo.setNamedParameter("endDate", Utility.formatDate(secondment.getEndDate()));
      log4j.debug("empInfo:" + empInfo.getWhereAndOrderBy());
      empInfoList = empInfo.list();
      if (empInfoList.size() > 0) {
        return true;
      }
      // return true;
    } catch (Exception e) {
      log4j.error("error while checkJobandJobTitleCombExist", e);
      return false;
    }
    return false;
  }

  public Date getPromotionStartDate(EHCMEmpSecondment secondment) {
    OBQuery<EmploymentInfo> employInfo = null;
    List<EmploymentInfo> employInfoList = new ArrayList<EmploymentInfo>();
    Date startDate = null;
    try {
      // get Promotion StartDate
      employInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e where e.ehcmEmpPerinfo.id=:employeeId  and e.ehcmEmpPromotion.id is not null"
              + " order by e.creationDate desc");
      employInfo.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      employInfo.setMaxResult(1);
      employInfoList = employInfo.list();
      log4j.debug("list Size:" + employInfoList.size());
      if (employInfoList.size() > 0) {
        startDate = employInfoList.get(0).getStartDate();
      }

    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in getPromotionStartDate: ", e);
    }
    return startDate;
  }

  public boolean oneYearDayValidation(Date startDate, String clientId, BigInteger period) {
    String strQuery = "", oneYearDateFromStartDate = "", startDatehijiri = "";
    boolean oneYearDayValidation = true;
    Query query = null;
    BigInteger days = BigInteger.ZERO;
    String startDateGreg = null;
    try {
      startDateGreg = yearFormat.format(startDate);
      startDatehijiri = UtilityDAO.eventConvertTohijriDate(startDateGreg);

      oneYearDateFromStartDate = (Integer.valueOf(startDatehijiri.split("-")[2]) + 1)
          + startDatehijiri.split("-")[1] + startDatehijiri.split("-")[0];

      oneYearDateFromStartDate = convertToGregorianDate(oneYearDateFromStartDate);

      strQuery = " select count(distinct hijri_date )  as total from eut_hijri_dates  where gregorian_date >= ?"
          + " and gregorian_date <=  to_date(?,'yyyy-MM-dd') ";
      query = OBDal.getInstance().getSession().createSQLQuery(strQuery);
      query.setParameter(0, startDate);
      query.setParameter(1, yearFormat.parse(oneYearDateFromStartDate));
      if (query != null && query.list().size() > 0) {
        days = (BigInteger) query.list().get(0);
      }

      if (period.compareTo(days) >= 0)
        oneYearDayValidation = false;
      else
        oneYearDayValidation = true;
      log4j.debug("oneYearDayValidation:" + oneYearDayValidation);

    } catch (final Exception e) {
      log4j.error("Exception in oneyeardaycal() Method : ", e);
      return false;
    }
    return oneYearDayValidation;
  }

  public String convertToGregorianDate(String hijriDate) {
    String gregDate = null;
    try {
      SQLQuery Query = OBDal.getInstance().getSession().createSQLQuery(
          " select to_char(gregorian_date,'YYYY-MM-DD')  from eut_hijri_dates where hijri_date = ? ");
      Query.setParameter(0, hijriDate);
      if (Query.list().size() > 0) {
        Object row = (Object) Query.list().get(0);
        gregDate = (String) row;
      }
    }

    catch (final Exception e) {
      log4j.error("Exception in convertToGregorianDate() Method : ", e);
      return null;
    }
    return gregDate;
  }

  public int calculateMonths(String strstartDate, String strendDate, String ClientId,
      EHCMEmpSecondment secondment, boolean yearflag, boolean sixyearflag) {
    int years = 0;
    int months = 0;
    int days = 0;
    BigInteger maxCurrentDate = BigInteger.ZERO;
    String strNowmonth = "", strQuery = "";
    SQLQuery query = null;

    // create calendar object for birth day
    try {

      // convert birthDate To hijriDate
      String strbirthDate = strstartDate;
      // create calendar object for current day
      String strtodayDate = strendDate;

      int dobyear = Integer.parseInt(strbirthDate.split("-")[2]);
      int dobmonth = Integer.parseInt(strbirthDate.split("-")[1]);
      int dobdate = Integer.parseInt(strbirthDate.split("-")[0]);
      log4j.debug("dobyear>>dobmonth>>dobdate" + dobyear + "-" + dobmonth + "-" + dobdate);
      int nowyear = Integer.parseInt(strtodayDate.split("-")[2]);
      int nowmonth = Integer.parseInt(strtodayDate.split("-")[1]);
      int nowdate = Integer.parseInt(strtodayDate.split("-")[0]);
      log4j.debug("nowyear>>nowmonth>>nowdate" + nowyear + "-" + nowmonth + "-" + nowdate);

      years = nowyear - dobyear;
      months = nowmonth - dobmonth;
      if (months < 0) {
        years--;
        months = 12 - dobmonth + nowmonth;
        if (nowdate < dobdate) {
          months--;
        }
      } else if (months == 0 && nowdate < dobdate) {
        years--;
        months = 11;

      }
      if (nowdate > dobdate) {
        days = nowdate - dobdate;

      } else if (nowdate < dobdate) {
        int today = nowdate;
        nowmonth = nowmonth - 1;
        if (nowmonth < 10) {
          strNowmonth = "0" + String.valueOf(nowmonth);
        } else {
          strNowmonth = String.valueOf(nowmonth);
        }
        // int maxCurrentDate = getDays(ClientId, strNowmonth);
        strNowmonth = nowyear + strNowmonth;
        strQuery = " select count(hijri_date) from ( "
            + " select  max(hijri_date) as hijri_date, gregorian_date from eut_hijri_dates "
            + "  where hijri_date ilike '%" + strNowmonth + "%'  group by  gregorian_date ) a ";
        query = OBDal.getInstance().getSession().createSQLQuery(strQuery);
        log4j.debug("strQuery:" + strQuery);
        if (query != null && query.list().size() > 0) {
          maxCurrentDate = (BigInteger) query.list().get(0);
        }
        days = maxCurrentDate.intValue() - dobdate + today;
        log4j.debug("days" + days);

      } else {
        days = 0;
        if (months == 12) {
          years++;
          months = 0;
        }

      }
      log4j.debug("years" + years);
      log4j.debug("months" + months);
      months = (years * 12) + months;
      log4j.debug("months" + months);
      log4j.debug("days" + days);
      if (yearflag)
        months = years;
      if (sixyearflag) {
        if (days > 0)
          months++;
      }
      log4j.debug("months" + months);

    } catch (Exception e) {
      log4j.error("Exception in calculateMonths", e);
      e.printStackTrace();
    }
    return months;
  }

  public Boolean continuousSixYearVal(EHCMEmpSecondment secondment) {
    boolean daysval = true;
    int month = 0, count = 0;
    String enddate = "", startdate = "";
    OBQuery<EmploymentInfo> info = null;
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    try {
      info = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId order by creationDate asc ");
      info.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      empInfoList = info.list();
      if (empInfoList.size() > 0) {
        for (EmploymentInfo infoObj : empInfoList) {
          EmploymentInfo empinfo = infoObj;
          if (infoObj.getChangereason().equals("SEC")
              || infoObj.getChangereason().equals("EXSEC")) {
            log4j.debug("getChangereason:" + infoObj.getChangereason());
            enddate = UtilityDAO.eventConvertTohijriDate(yearFormat.format(infoObj.getEndDate()));
            enddate = getOneDayAddHijiriDate(
                enddate.split("-")[2] + enddate.split("-")[1] + enddate.split("-")[0],
                secondment.getClient().getId());
            month += calculateMonths(
                UtilityDAO.eventConvertTohijriDate(yearFormat.format(infoObj.getStartDate())),
                enddate, secondment.getClient().getId(), secondment, false, true);
            log4j.debug("month:" + month);
            if (empinfo.equals(info.list().get(info.list().size() - 1))) {
              startdate = yearFormat.format(infoObj.getEndDate());
            }
          }
        }
      }
      if (!secondment.getDecisionType().equals("UP")) {
        enddate = UtilityDAO.eventConvertTohijriDate(yearFormat.format(secondment.getEndDate()));
        enddate = getOneDayAddHijiriDate(
            enddate.split("-")[2] + enddate.split("-")[1] + enddate.split("-")[0],
            secondment.getClient().getId());
        month += calculateMonths(
            UtilityDAO.eventConvertTohijriDate(yearFormat.format(secondment.getStartDate())),
            enddate, secondment.getClient().getId(), secondment, false, true);
        log4j.debug("month final:" + month);
      } else {
        if (startdate.equals(yearFormat.format(secondment.getEndDate()))) {
          count = 1;
          log4j.debug("month final else:" + count);
        }
        startdate = UtilityDAO.eventConvertTohijriDate(startdate);
        enddate = UtilityDAO.eventConvertTohijriDate(yearFormat.format(secondment.getEndDate()));
        enddate = getOneDayAddHijiriDate(
            enddate.split("-")[2] + enddate.split("-")[1] + enddate.split("-")[0],
            secondment.getClient().getId());
        month += calculateMonths(startdate, enddate, secondment.getClient().getId(), secondment,
            false, true);
        log4j.debug("month final else:" + month);

      }

      if (!secondment.getDecisionType().equals("UP")) {
        if (month >= 73) {
          daysval = false;
        } else
          daysval = true;
      } else {
        if (month >= 73 && count == 1) {
          daysval = true;
        } else if (month <= 72)
          daysval = true;
        else
          daysval = false;
      }
    } catch (final Exception e) {
      log4j.error("Exception in sixxyeardayscal() in EmpSeconment event Method : ", e);
      return false;
    }
    return daysval;
  }

  public String getOneDayAddHijiriDate(String hijiriDate, String clientId) {
    Query query = null;
    String strQuery = "", startdate = "";
    try {

      strQuery = " select  hijri_date from eut_hijri_dates  where hijri_date > ?"
          + " order by hijri_date asc limit 1 ";
      query = OBDal.getInstance().getSession().createSQLQuery(strQuery);
      query.setParameter(0, hijiriDate);
      log4j.debug("strQuery:" + strQuery);
      if (query != null && query.list().size() > 0) {
        Object row = query.list().get(0);
        startdate = (String) row;
        startdate = startdate.substring(6, 8) + "-" + startdate.substring(4, 6) + "-"
            + startdate.substring(0, 4);
      }
    } catch (Exception e) {
      log4j.error("Exception in getOneDayAddHijiriDate", e);
      e.printStackTrace();
    }
    return startdate;
  }

  /**
   * 
   * @param secondment
   * @return Existing Secondment Months Count
   */
  public int existingMonthCount(EHCMEmpSecondment secondment, String decisionType) {
    int month = 0;
    String startdate = "", enddate = "";
    String hql = "";
    OBQuery<EmploymentInfo> employInfo = null;
    List<EmploymentInfo> employInfoList = new ArrayList<EmploymentInfo>();
    try {
      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        hql = " and ( e.ehcmEmpSecondment.id<>:secondmentId  or  e.ehcmEmpSecondment.id is null)";
      }

      employInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e where e.ehcmEmpPerinfo.id=:employeeId  " + hql + " order by e.creationDate asc ");
      employInfo.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        employInfo.setNamedParameter("secondmentId", secondment.getOriginalDecisionsNo().getId());
      }

      employInfoList = employInfo.list();
      if (employInfoList.size() > 0) {
        for (EmploymentInfo empInfo : employInfoList) {
          log4j.debug("getChangereason" + empInfo.getChangereason());

          if (empInfo.getChangereason().equals("SEC") || empInfo.getChangereason().equals("COSEC")
              || empInfo.getChangereason().equals("EXSEC")) {
            if (empInfo.getChangereason().equals("SEC")) {
              if (startdate.equals("")) {
                startdate = UtilityDAO
                    .eventConvertTohijriDate(yearFormat.format(empInfo.getStartDate()));
                enddate = UtilityDAO
                    .eventConvertTohijriDate(yearFormat.format(empInfo.getEndDate()));
                enddate = getOneDayAddHijiriDate(
                    enddate.split("-")[2] + enddate.split("-")[1] + enddate.split("-")[0],
                    secondment.getClient().getId());
                month += calculateMonths(startdate, enddate, secondment.getClient().getId(),
                    secondment, false, false);
              } else {
                startdate = UtilityDAO
                    .eventConvertTohijriDate(yearFormat.format(empInfo.getStartDate()));
                enddate = UtilityDAO
                    .eventConvertTohijriDate(yearFormat.format(empInfo.getEndDate()));
                enddate = getOneDayAddHijiriDate(
                    enddate.split("-")[2] + enddate.split("-")[1] + enddate.split("-")[0],
                    secondment.getClient().getId());
                month += calculateMonths(startdate, enddate, secondment.getClient().getId(),
                    secondment, false, false);
              }
              log4j.debug("monthfirstloop" + month);
            } else if (empInfo.getChangereason().equals("EXSEC")) {
              startdate = UtilityDAO
                  .eventConvertTohijriDate(yearFormat.format(empInfo.getStartDate()));
              enddate = UtilityDAO.eventConvertTohijriDate(yearFormat.format(empInfo.getEndDate()));
              enddate = getOneDayAddHijiriDate(
                  enddate.split("-")[2] + enddate.split("-")[1] + enddate.split("-")[0],
                  secondment.getClient().getId());
              month += calculateMonths(startdate, enddate, secondment.getClient().getId(),
                  secondment, false, false);
            }
          }
        }
      }
      log4j.debug("existingMonthCount" + month);
    } catch (final Exception e) {
      log4j.error("Exception in existingMonthCount() in EmpSeconment event Method : ", e);
      return 0;
    }
    return month;
  }

  /**
   * 
   * @param secondment
   * @return Current records Months
   */

  public int currentMonths(EHCMEmpSecondment secondment) {
    int month = 0;
    String startdate = "", enddate = "";
    try {
      if (secondment != null) {
        startdate = UtilityDAO
            .eventConvertTohijriDate(yearFormat.format(secondment.getStartDate()));
        enddate = UtilityDAO.eventConvertTohijriDate(yearFormat.format(secondment.getEndDate()));
        enddate = getOneDayAddHijiriDate(
            enddate.split("-")[2] + enddate.split("-")[1] + enddate.split("-")[0],
            secondment.getClient().getId());
        month = calculateMonths(startdate, enddate, secondment.getClient().getId(), secondment,
            false, true);
      }
      log4j.debug("currentMonths:" + month);
    } catch (final Exception e) {
      log4j.error("Exception in currentMonths() in EmpSeconment event Method : ", e);
      return 0;
    }
    return month;
  }

  /**
   * 
   * @param secondment
   * @param existingMonth
   * @param currentMonth
   * @return if secondment allow then true else false
   */
  public Boolean threeYearValidation(EHCMEmpSecondment secondment, int existingMonth,
      int currentMonth) {
    String lastEndDate = "";
    Date lastGregorianDate = null;
    EmploymentInfo empInfo = null;
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    int remainMonths = 0;
    Boolean validation = Boolean.FALSE;
    try {
      // existing month less than 24 then allow Secondment
      // continuous 36 months
      if (existingMonth >= 0 && existingMonth <= 24) {

        remainMonths = 36 - existingMonth;
        if (remainMonths >= currentMonth) {
          validation = Boolean.TRUE;
        } else {
          validation = Boolean.FALSE;
        }
        // validation = Boolean.TRUE;
      }
      // existing month b/w
      // 25 to 35 then allow only remaining months
      else if (existingMonth >= 25 && existingMonth <= 35) {
        // remain months
        remainMonths = 36 - existingMonth;
        if (remainMonths >= currentMonth) {
          validation = Boolean.TRUE;
        } else {
          validation = Boolean.FALSE;
        }
      }
      // check 3 years gap if already three years completed
      else if (existingMonth == 36) {
        // get end date of last secondment
        OBQuery<EmploymentInfo> infoList = OBDal.getInstance()
            .createQuery(EmploymentInfo.class, " ehcmEmpPerinfo.id='"
                + secondment.getEhcmEmpPerinfo().getId()
                + "'and issecondment='Y' and changereason not in ('COSEC','JWRSEC','SECDLY')  order by creationDate desc ");
        infoList.setMaxResult(1);
        if (infoList.list().size() > 0) {
          empInfo = infoList.list().get(0);
        }
        lastEndDate = UtilityDAO
            .eventConvertTohijriDate(dateYearFormat.format(empInfo.getEndDate()));
        lastEndDate = String.valueOf(Integer.parseInt(lastEndDate.split("-")[2]) + 3)
            + lastEndDate.split("-")[1] + lastEndDate.split("-")[0];
        lastGregorianDate = dateYearFormat.parse(convertToGregorianDate(lastEndDate));
        log4j.debug("sec StartDate:" + secondment.getStartDate());
        log4j.debug("after 3 yrs Date:" + lastGregorianDate);
        if (secondment.getStartDate().compareTo(lastGregorianDate) >= 0) {
          validation = Boolean.TRUE;
        } else {
          validation = Boolean.FALSE;
        }
      }
      // over all six years (72 months)
      else if (existingMonth >= 37 && existingMonth <= 60) {
        validation = Boolean.TRUE;
      }
      // 61 to 71 then allow only remaining months
      else if (existingMonth >= 61 && existingMonth <= 71) {
        // remain months
        remainMonths = 72 - existingMonth;
        if (remainMonths >= currentMonth) {
          validation = Boolean.TRUE;
        } else {
          validation = Boolean.FALSE;
        }
      }
      // 72 dnt allow
      else if (existingMonth == 72) {
        validation = Boolean.FALSE;
      }

    } catch (final Exception e) {
      log4j.error("Exception in threeYearValidation() in EmpSeconment event Method : ", e);
      return Boolean.FALSE;
    }
    return validation;
  }

  public List<EmployeeDelegation> getDelegationList(EmploymentInfo employinfo) {
    OBQuery<EmployeeDelegation> delQry = null;
    List<EmployeeDelegation> delList = new ArrayList<EmployeeDelegation>();
    try {
      delQry = OBDal.getInstance().createQuery(EmployeeDelegation.class,
          "  as e where e.ehcmEmploymentInfo.id=:emplInfoId "
              + " and enabled='Y'  order by creationDate desc");
      delQry.setNamedParameter("emplInfoId", employinfo.getId());
      delList = delQry.list();
      if (delList.size() > 0) {
        return delList;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getActiveEmployInfo " + e.getMessage());
      e.printStackTrace();
    }
    return delList;
  }

  public EhcmPosition getDelegationPosition(EHCMEmpSecondment secondment) {
    OBQuery<EmployeeDelegation> delQry = null;
    List<EmployeeDelegation> delList = new ArrayList<EmployeeDelegation>();
    EhcmPosition position = null;
    try {
      delQry = OBDal.getInstance().createQuery(EmployeeDelegation.class,
          " ehcmEmpPerinfo.id=:employeeId and enabled='Y' order by creationDate desc");
      delQry.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      delQry.setMaxResult(1);
      delList = delQry.list();
      if (delList.size() > 0) {
        EmployeeDelegation delegation = delList.get(0);
        position = OBDal.getInstance().get(EhcmPosition.class, delegation.getNewPosition().getId());
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getActiveEmployInfo " + e.getMessage());
      e.printStackTrace();
    }
    return position;
  }

  public Boolean promotionVal(EHCMEmpSecondment secondment) {
    String secondStartDate = null;
    String promStartDate = null;
    Date promotionStartDate = null;
    boolean validation = true;
    boolean daysval = true;
    int month = 0;
    try {
      secondStartDate = UtilityDAO
          .eventConvertTohijriDate(yearFormat.format(secondment.getStartDate()));

      promotionStartDate = getPromotionStartDate(secondment);

      if (promotionStartDate != null) {
        promStartDate = UtilityDAO.eventConvertTohijriDate(yearFormat.format(promotionStartDate));
        /*
         * validation = UtilityDAO.periodyearValidation(promStartDate, secondStartDate, 1);
         * 
         * month = calculateMonths(promStartDate, secondStartDate, secondment.getClient().getId(),
         * secondment, false, false);
         */
        daysval = promotionDayVal(yearFormat.format(promotionStartDate),
            yearFormat.format(secondment.getStartDate()), secondment.getClient().getId());

        if (!daysval) {
          return false;
        } else {
          return true;
        }
      } else {
        return true;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in promotionVal " + e.getMessage());
      e.printStackTrace();
    }
    return true;
  }

  public Boolean promotionDayVal(String promStartDate, String secondStartDate, String clientId) {
    String startdate = "", endDate = "", secondmentStartdateHiji = "";
    BigInteger totalDays = BigInteger.ZERO;
    BigInteger calDays = BigInteger.ZERO;
    boolean daysVal = true;
    try {
      startdate = promStartDate;
      endDate = secondStartDate;
      totalDays = calculatedays(startdate, endDate, clientId);

      secondmentStartdateHiji = UtilityDAO.eventConvertTohijriDate(promStartDate);
      endDate = (Integer.valueOf(secondmentStartdateHiji.split("-")[2])
          + Constants.YearForSecondmentCreateAfterPromotion) + secondmentStartdateHiji.split("-")[1]
          + secondmentStartdateHiji.split("-")[0];

      endDate = getOneDayMinusHijiriDate(endDate, clientId);
      endDate = convertToGregorianDate(endDate);

      calDays = calculatedays(startdate, endDate, clientId);
      log4j.debug("days1:" + totalDays);
      if (calDays.compareTo(totalDays) >= 0) {
        daysVal = false;
      } else if (calDays.compareTo(totalDays) < 0) {
        daysVal = true;
      }

    }

    catch (final Exception e) {
      log4j.error("Exception in promotiondaysval() in EmpSeconment event Method : ", e);
      return false;
    }
    return daysVal;
  }

  public String getOneDayMinusHijiriDate(String hijiriDate, String clientId) {
    Query query = null;
    String strQuery = "", startdate = "";
    try {

      strQuery = " select  hijri_date from eut_hijri_dates  where hijri_date < ?"
          + " order by hijri_date desc limit 1 ";
      query = OBDal.getInstance().getSession().createSQLQuery(strQuery);
      query.setParameter(0, hijiriDate);
      if (query != null && query.list().size() > 0) {
        Object row = query.list().get(0);
        startdate = (String) row;
      }
    } catch (Exception e) {
      log4j.error("Exception in getOneDayMinusHijiriDate", e);
      e.printStackTrace();
    }
    return startdate;
  }

  /**
   * get current emply info startdate
   * 
   * @param secondment
   * @return employmentInfoObj
   */
  public EmploymentInfo getCurrentEmplyInfoStartDate(EHCMEmpSecondment secondment) {
    OBQuery<EmploymentInfo> info = null;
    EmploymentInfo employInfo = null;
    List<EmploymentInfo> employInfoList = new ArrayList<EmploymentInfo>();
    try {
      info = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and ehcmEmpSecondment.id is null order by creationDate desc ");
      info.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      info.setMaxResult(1);
      employInfoList = info.list();
      if (employInfoList.size() > 0) {
        employInfo = employInfoList.get(0);
      }

    } catch (final Exception e) {
      log4j.error("Exception in eventConvertTohijriDate() Method : ", e);
      return employInfo;
    }
    return employInfo;
  }

  // unused
  public int getSecondmentstartdate(EHCMEmpSecondment secondment) {
    String startdate = "", enddate = "";
    int month = 0;
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      SQLQuery query = OBDal.getInstance().getSession().createSQLQuery(
          " select to_char(startdate,'YYYY-MM-DD') from ehcm_employment_info  where ehcm_emp_perinfo_id  ='"
              + secondment.getEhcmEmpPerinfo().getId()
              + "' and changereason='SEC' order by created desc limit 1");
      if (query != null && query.list().size() > 0) {
        Object row = (Object) query.list().get(0);
        startdate = (String) row;
        startdate = UtilityDAO.eventConvertTohijriDate(startdate);
        if (!startdate.equals("")) {
          enddate = UtilityDAO
              .eventConvertTohijriDate(dateYearFormat.format(secondment.getStartDate()));
          month = calculateMonths(startdate, enddate, secondment.getClient().getId(), secondment,
              true, false);
        }
      }

    }

    catch (final Exception e) {
      log4j.error("Exception in getSecondmentstartdate() in EmpSeconment event Method : ", e);
      return 0;
    }
    return month;
  }

  public int continuosthreeyears(EHCMEmpSecondment secondment) {
    int month = 0;
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    String startdate = "", enddate = "";
    try {
      OBQuery<EmploymentInfo> info = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id='" + secondment.getEhcmEmpPerinfo().getId()
              + "' order by creationDate asc ");
      if (info.list().size() > 0) {
        for (EmploymentInfo in : info.list()) {
          if (in.getChangereason().equals("SEC") || in.getChangereason().equals("COSEC")
              || in.getChangereason().equals("EXSEC")) {
            if (in.getChangereason().equals("SEC")) {
              if (startdate.equals("")) {
                startdate = UtilityDAO
                    .eventConvertTohijriDate(dateYearFormat.format(in.getStartDate()));
                enddate = UtilityDAO
                    .eventConvertTohijriDate(dateYearFormat.format(in.getEndDate()));
                enddate = getOneDayAddHijiriDate(
                    enddate.split("-")[2] + enddate.split("-")[1] + enddate.split("-")[0],
                    secondment.getClient().getId());
                month += calculateMonths(startdate, enddate, secondment.getClient().getId(),
                    secondment, false, false);
              } else {
                startdate = UtilityDAO
                    .eventConvertTohijriDate(dateYearFormat.format(in.getStartDate()));
                enddate = UtilityDAO
                    .eventConvertTohijriDate(dateYearFormat.format(in.getEndDate()));
                enddate = getOneDayAddHijiriDate(
                    enddate.split("-")[2] + enddate.split("-")[1] + enddate.split("-")[0],
                    secondment.getClient().getId());
                month += calculateMonths(startdate, enddate, secondment.getClient().getId(),
                    secondment, false, false);
              }
            } else if (in.getChangereason().equals("EXSEC")) {
              startdate = UtilityDAO
                  .eventConvertTohijriDate(dateYearFormat.format(in.getStartDate()));
              enddate = UtilityDAO.eventConvertTohijriDate(dateYearFormat.format(in.getEndDate()));
              enddate = getOneDayAddHijiriDate(
                  enddate.split("-")[2] + enddate.split("-")[1] + enddate.split("-")[0],
                  secondment.getClient().getId());
              month += calculateMonths(startdate, enddate, secondment.getClient().getId(),
                  secondment, false, false);
            }
          }
        }
        startdate = UtilityDAO
            .eventConvertTohijriDate(dateYearFormat.format(secondment.getStartDate()));
        enddate = UtilityDAO
            .eventConvertTohijriDate(dateYearFormat.format(secondment.getEndDate()));
        enddate = getOneDayAddHijiriDate(
            enddate.split("-")[2] + enddate.split("-")[1] + enddate.split("-")[0],
            secondment.getClient().getId());
        month += calculateMonths(startdate, enddate, secondment.getClient().getId(), secondment,
            false, false);
      }
    } catch (final Exception e) {
      log4j.error("Exception in continuosthreeyears() in EmpSeconment event Method : ", e);
      return 0;
    }
    return month;
  }

  public Boolean dayscal(EHCMEmpSecondment secondment, boolean allownxtthreeyr) {
    boolean daysval = Boolean.TRUE;
    String startdate = "", enddate = "", startdate1 = "", startdatehijiri = "";
    BigInteger days = BigInteger.ZERO;
    BigInteger days1 = BigInteger.ZERO;
    Date extendenddate = null;
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      OBQuery<EmploymentInfo> info = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id='" + secondment.getEhcmEmpPerinfo().getId()
              + "' order by creationDate asc ");
      if (info.list().size() > 0) {
        for (EmploymentInfo in : info.list()) {
          EmploymentInfo empifo = in;
          if (in.getChangereason().equals("SEC") || in.getChangereason().equals("COSEC")
              || in.getChangereason().equals("EXSEC")) {
            if (in.getChangereason().equals("SEC")) {
              if (startdate.equals("") && daysval) {
                startdate = dateYearFormat.format(in.getStartDate());
                startdatehijiri = UtilityDAO
                    .eventConvertTohijriDate(dateYearFormat.format(in.getStartDate()));
                enddate = dateYearFormat.format(in.getEndDate());
                days = days.add(calculatedays(startdate, enddate, secondment.getClient().getId()));
              } else {
                startdate1 = dateYearFormat.format(in.getStartDate());
                enddate = dateYearFormat.format(in.getEndDate());
                days = days.add(calculatedays(startdate1, enddate, secondment.getClient().getId()));
              }
            } else if (in.getChangereason().equals("COSEC")
                && ((info.list().get(info.list().size() - 1)).equals(empifo))) {
              extendenddate = new Date(in.getStartDate().getTime() - 1 * 24 * 3600 * 1000);
              enddate = dateYearFormat.format(extendenddate);
              days = calculatedays(startdate, enddate, secondment.getClient().getId());
            } else if (in.getChangereason().equals("COSEC")
                && (!(info.list().get(info.list().size() - 1)).equals(empifo))) {
              days = BigInteger.ZERO;
              daysval = true;
              /*
               * startdate = ""; startdatehijiri = "";
               */
            } else if (in.getChangereason().equals("EXSEC")) {
              startdate1 = dateYearFormat.format(in.getStartDate());
              enddate = dateYearFormat.format(in.getEndDate());
              days = days.add(calculatedays(startdate1, enddate, secondment.getClient().getId()));
            }
            if (empifo.equals(info.list().get(info.list().size() - 1))) {
              if (startdatehijiri != "") {
                if (!allownxtthreeyr) {
                  startdate1 = dateYearFormat.format(secondment.getStartDate());
                  enddate = dateYearFormat.format(secondment.getEndDate());
                  days = days
                      .add(calculatedays(startdate1, enddate, secondment.getClient().getId()));
                }

                else {
                  enddate = dateYearFormat.format(secondment.getStartDate());
                  days = calculatedays(startdate, enddate, secondment.getClient().getId());
                }

                if (!allownxtthreeyr) {

                  enddate = (Integer.valueOf(startdatehijiri.split("-")[2]) + 3)
                      + startdatehijiri.split("-")[1] + startdatehijiri.split("-")[0];
                  enddate = getOneDayMinusHijiriDate(enddate, secondment.getClient().getId());
                  enddate = convertToGregorianDate(enddate);
                  days1 = calculatedays(startdate, enddate, secondment.getClient().getId());
                  if (days.compareTo(days1) > 0) {
                    daysval = false;
                  } else if (days.compareTo(days1) <= 0) {
                    daysval = true;
                  }
                } else if (allownxtthreeyr) {
                  enddate = (Integer.valueOf(startdatehijiri.split("-")[2]) + 6)
                      + startdatehijiri.split("-")[1] + startdatehijiri.split("-")[0];
                  enddate = getOneDayMinusHijiriDate(enddate, secondment.getClient().getId());
                  enddate = convertToGregorianDate(enddate);
                  days1 = calculatedays(startdate, enddate, secondment.getClient().getId());
                  if (days.compareTo(days1) > 0) {
                    daysval = true;
                  } else if (days.compareTo(days1) <= 0) {
                    daysval = false;
                  }
                }
              }
            }
          }
        }
      }

    } catch (final Exception e) {
      log4j.error("Exception in dayscal() in EmpSeconment event Method : ", e);
      return false;
    }
    return daysval;
  }

  public BigInteger calculatedays(String startdate, String enddate, String clientId) {
    BigInteger days = BigInteger.ZERO;
    Query query = null;
    String strQuery = "";
    try {
      strQuery = " select count(hijri_date) from ( select max(hijri_date) as hijri_date , gregorian_date  from eut_hijri_dates  where gregorian_date >= '"
          + startdate + "' and gregorian_date <= '" + enddate
          + "' group by gregorian_date  order by gregorian_date asc ) a  ";
      query = OBDal.getInstance().getSession().createSQLQuery(strQuery);
      if (query != null && query.list().size() > 0) {
        days = (BigInteger) query.list().get(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return days;
  }

  public String convertTohijriDates(String gregDate) {
    String hijriDate = "";
    try {
      SQLQuery gradeQuery = OBDal.getInstance().getSession()
          .createSQLQuery("select eut_convert_to_hijri(to_char(to_timestamp('" + gregDate
              + "','YYYY-MM-DD HH24:MI:SS'),'YYYY-MM-DD  HH24:MI:SS'))");
      if (gradeQuery.list().size() > 0) {
        Object row = (Object) gradeQuery.list().get(0);
        hijriDate = (String) row;
      }
    }

    catch (final Exception e) {
      log4j.error("Exception in eventConvertTohijriDate() Method : ", e);
      return "0";
    }
    return hijriDate;
  }

  public JSONObject existingSecondmentDayCal(EHCMEmpSecondment secondment, String decisionType) {
    String hql = "";
    OBQuery<EmploymentInfo> employInfo = null;
    List<EmploymentInfo> employInfoList = new ArrayList<EmploymentInfo>();
    String changeReason = null;
    BigDecimal days = BigDecimal.ZERO;
    JSONObject result = new JSONObject();
    Date endDate = null;
    try {

      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        hql = " and ( e.ehcmEmpSecondment.id<>:secondmentId  or  e.ehcmEmpSecondment.id is null)";
      }

      employInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e where e.ehcmEmpPerinfo.id=:employeeId  " + hql + " order by e.creationDate asc ");
      employInfo.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        employInfo.setNamedParameter("secondmentId", secondment.getOriginalDecisionsNo().getId());
      }

      employInfoList = employInfo.list();
      if (employInfoList.size() > 0) {
        for (EmploymentInfo empInfo : employInfoList) {
          log4j.debug("getChangereason" + empInfo.getChangereason());
          changeReason = empInfo.getChangereason();
          if (changeReason.equals(DecisionTypeConstants.CHANGEREASON_SECONDMENT)
              || changeReason.equals(DecisionTypeConstants.CHANGEREASON_EXTEND_SECONDMENT)) { // ||
            days = days.add(BigDecimal.valueOf(sa.elm.ob.hcm.util.UtilityDAO
                .caltheDaysUsingGreDate(empInfo.getStartDate(), empInfo.getEndDate())));
          } else {
            continue;
          }
        }
      }
      days = days.add(sa.elm.ob.hcm.util.Utility
          .balanceDaysInYear(secondment.getEhcmEmpPerinfo().getId(), "SB"));

      result.put("endDate", endDate);
      result.put("days", days);

    } catch (final Exception e) {
      log4j.error("Exception in existingSecondmentDayCal() Method : ", e);
      return result;
    }
    return result;
  }

  public Boolean YearValidationForSecondment(EHCMEmpSecondment secondment, String decisionType,
      int year) {
    BigDecimal existingDays = BigDecimal.ZERO;
    BigDecimal currentDays = BigDecimal.ZERO;
    BigDecimal totalSecondmentDays = BigDecimal.ZERO;
    BigDecimal totalYearDays = BigDecimal.ZERO;
    Boolean yearValidation = Boolean.TRUE;
    JSONObject result = new JSONObject();
    try {
      // existing secondemnt days
      result = existingSecondmentDayCal(secondment, decisionType);
      if (result != null) {
        existingDays = new BigDecimal(result.getString("days"));
      }
      // current record total days
      currentDays = BigDecimal.valueOf(sa.elm.ob.hcm.util.UtilityDAO
          .caltheDaysUsingGreDate(secondment.getStartDate(), secondment.getEndDate()));
      // total days
      totalSecondmentDays = existingDays.add(currentDays);
      if (year > 0) {
        totalYearDays = BigDecimal.valueOf(year)
            .multiply(BigDecimal.valueOf(Constants.NoOfDaysInYear));
      }
      if (totalYearDays.compareTo(totalSecondmentDays) < 0) {
        yearValidation = Boolean.FALSE;
      }
    } catch (final Exception e) {
      log4j.error("Exception in YearValidationForSecondment Method  ", e);
      return Boolean.FALSE;
    }
    return yearValidation;
  }

  public Boolean threeYearValForSecondment(EHCMEmpSecondment secondment, String decisionType,
      int year) {
    int totalYearDays = 0;
    Boolean yearValidation = Boolean.TRUE;
    JSONObject result = new JSONObject();
    Date lastSecondmentEndDate = null;
    EmploymentInfo employinfo = null;
    int days = 0, a = 0;
    try {

      /* current active employment details */

      employinfo = findPrevEmploymentRecord(secondment, decisionType, secondment.getCreationDate());

      while (employinfo != null) {
        if (lastSecondmentEndDate == null) {
          lastSecondmentEndDate = employinfo.getEndDate();
        }
        days += sa.elm.ob.hcm.util.UtilityDAO.caltheDaysUsingGreDate(employinfo.getStartDate(),
            employinfo.getEndDate());
        employinfo = findPrevEmploymentRecord(secondment, decisionType,
            employinfo.getCreationDate());
      }
      result.put("days", days);
      result.put("isContinuous", true);
      result.put("lastSecondmentEndDate", lastSecondmentEndDate);

      if (result.getBoolean("isContinuous")) {
        totalYearDays = year * Constants.NoOfDaysInYear;
        if (result.getInt("days") < totalYearDays) {
          days = sa.elm.ob.hcm.util.UtilityDAO.caltheDaysUsingGreDate(secondment.getStartDate(),
              secondment.getEndDate());
          if ((result.getInt("days") + days) > totalYearDays) {
            yearValidation = Boolean.FALSE;
          }
        } else if (result.getInt("days") == totalYearDays) {
          days = sa.elm.ob.hcm.util.UtilityDAO.caltheDaysUsingGreDate(
              yearFormat.parse(result.getString("lastSecondmentEndDate")),
              secondment.getStartDate());
          totalYearDays = (year + (Constants.SecondmentGapYear)) * Constants.NoOfDaysInYear;
          if ((result.getInt("days") + days) <= totalYearDays) {
            yearValidation = Boolean.FALSE;
          }
        }
      }

    } catch (final Exception e) {
      log4j.error("Exception in YearValidationForSecondment Method  ", e);
      return Boolean.FALSE;
    }
    return yearValidation;
  }

  public EmploymentInfo findPrevEmploymentRecord(EHCMEmpSecondment secondment, String decisionType,
      Date creationDate) {
    String hql = "";
    OBQuery<EmploymentInfo> employInfoQry = null;
    List<EmploymentInfo> employInfoList = new ArrayList<EmploymentInfo>();
    EmploymentInfo employInfo = null;
    try {
      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        hql = " and ( e.ehcmEmpSecondment.id<>:secondmentId  or  e.ehcmEmpSecondment.id is null)";
      }

      employInfoQry = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e where e.ehcmEmpPerinfo.id=:employeeId  " + hql
              + " and e.creationDate < :creationDate" + " and e.ehcmEmpSecondment.id is not null "
              + " and e.changereason in ('SEC','EXSEC')   order by e.creationDate desc ");
      employInfoQry.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        employInfoQry.setNamedParameter("secondmentId",
            secondment.getOriginalDecisionsNo().getId());
      }
      employInfoQry.setNamedParameter("creationDate", creationDate);
      employInfoQry.setMaxResult(1);
      employInfoList = employInfoQry.list();
      if (employInfoList.size() > 0) {
        employInfo = employInfoList.get(0);
      }

    } catch (final Exception e) {
      log4j.error("Exception in findPrevEmploymentRecord Method  ", e);
      return employInfo;
    }
    return employInfo;
  }

  public Boolean threeYearValidationForSecondment(EHCMEmpSecondment secondment, String decisionType,
      int year) {
    String hql = "";
    OBQuery<EmploymentInfo> employInfoQry = null;
    List<EmploymentInfo> employInfoList = new ArrayList<EmploymentInfo>();
    EmploymentInfo employInfo = null;
    int days = 0;
    Boolean isSecondmentStart = false;
    Boolean isSecondmentContinuous = false;
    Boolean yearValidation = Boolean.TRUE;
    int totalDays = 0;
    int currentDays = 0;
    int totalSecondmentDays = 0;
    Date lastSecondmentEndDate = null;
    // int j = 0;
    try {

      totalDays = (year * Constants.NoOfDaysInYear);
      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        hql = " and ( e.ehcmEmpSecondment.id<>:secondmentId  or  e.ehcmEmpSecondment.id is null)";
      }

      employInfoQry = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e where e.ehcmEmpPerinfo.id=:employeeId  " + hql
              + "   order by e.creationDate asc ");
      employInfoQry.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        employInfoQry.setNamedParameter("secondmentId",
            secondment.getOriginalDecisionsNo().getId());
      }
      employInfoList = employInfoQry.list();
      if (employInfoList.size() > 0) {
        for (EmploymentInfo info : employInfoList) {
          if ((info.getChangereason().equals(DecisionTypeConstants.CHANGEREASON_SECONDMENT) || info
              .getChangereason().equals(DecisionTypeConstants.CHANGEREASON_EXTEND_SECONDMENT))
              && days < totalDays) {

            if (!isSecondmentStart) {
              isSecondmentStart = true;
            } else {
              isSecondmentContinuous = true;
            }
            lastSecondmentEndDate = info.getEndDate();
            days += sa.elm.ob.hcm.util.UtilityDAO.caltheDaysUsingGreDate(info.getStartDate(),
                info.getEndDate());
          } else if ((info.getChangereason().equals(DecisionTypeConstants.CHANGEREASON_SECONDMENT)
              || info.getChangereason()
                  .equals(DecisionTypeConstants.CHANGEREASON_EXTEND_SECONDMENT))
              && days == totalDays) {
            isSecondmentContinuous = false;
            days = 0;
            isSecondmentStart = true;
            lastSecondmentEndDate = info.getEndDate();
            days += sa.elm.ob.hcm.util.UtilityDAO.caltheDaysUsingGreDate(info.getStartDate(),
                info.getEndDate());

          } else {
            if (days == totalDays) {
              isSecondmentContinuous = true;
            } else {
              isSecondmentContinuous = false;
              isSecondmentStart = false;
              days = 0;
            }
          }
        }
      }

      if (isSecondmentContinuous) {
        if (days < totalDays) {
          currentDays = sa.elm.ob.hcm.util.UtilityDAO
              .caltheDaysUsingGreDate(secondment.getStartDate(), secondment.getEndDate());
          totalSecondmentDays = currentDays + days;
          if (totalDays < totalSecondmentDays) {
            yearValidation = Boolean.FALSE;
          }
        } else if (days == totalDays) {
          currentDays = sa.elm.ob.hcm.util.UtilityDAO.caltheDaysUsingGreDate(lastSecondmentEndDate,
              secondment.getStartDate());
          totalSecondmentDays = currentDays + days;
          if (((year + Constants.SecondmentGapYear)
              * Constants.NoOfDaysInYear) >= totalSecondmentDays) {
            yearValidation = Boolean.FALSE;
          }
        }
      }

    } catch (final Exception e) {
      log4j.error("Exception in threeYearValidationForSecondment Method  ", e);
      return yearValidation;
    }
    return yearValidation;
  }
}
