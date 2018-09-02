package sa.elm.ob.hcm.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.geography.City;
import org.openbravo.model.common.geography.Country;
import org.openbravo.utils.FileUtility;

import sa.elm.ob.hcm.DecisionBalance;
import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMBusMissionSummary;
import sa.elm.ob.hcm.EHCMDecisionOverlapLn;
import sa.elm.ob.hcm.EHCMDeflookupsTypeLn;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMEmpLeaveln;
import sa.elm.ob.hcm.EHCMEmpScholarship;
import sa.elm.ob.hcm.EHCMEmpSecondment;
import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.EHCMEmpSupervisorNode;
import sa.elm.ob.hcm.EHCMMisCatPeriod;
import sa.elm.ob.hcm.EHCMMiscatEmployee;
import sa.elm.ob.hcm.EHCMMissionCategory;
import sa.elm.ob.hcm.EHCMScholarshipSummary;
import sa.elm.ob.hcm.EhcmDisciplineAction;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmEmployeeOvertime;
import sa.elm.ob.hcm.EhcmJoiningWorkRequest;
import sa.elm.ob.hcm.EhcmOvertimeType;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.empScholarshipTraining.EmpScholarshipTrainingDAO;
import sa.elm.ob.hcm.ad_process.empScholarshipTraining.EmpScholarshipTrainingDAOImpl;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAO;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAOImpl;
import sa.elm.ob.utility.EUT_HijriDates;
import sa.elm.ob.utility.util.Utility;

public class UtilityDAO {
  private Connection connection = null;

  private static final Logger log4j = Logger.getLogger(UtilityDAO.class);
  private static final String EmployeeStatus_Active = "AC";
  private static final String EmployeeStatus_Secondment = "SE";
  private static final String EmployeeStatus_ExtraSteps = "EX";
  private static final String EmployeeStatus_ExtendofService = "EOS";
  private static final String EmployeeStatus_Suspension = "SD";
  private static final String EmployeeStatus_ScholarshipTraining = "SCTR";
  private static final String Employee_Overtime_Hours = "EOT";

  private static final String SECONDMENT = "SEC";
  private static final String EXTEND_SECONDMENT = "EXSEC";
  private static final String CUTOFF_SECONDMENT = "COSEC";

  static DateFormat dateFormat = Utility.dateFormat;
  static DateFormat yeareFormat = Utility.YearFormat;

  public UtilityDAO() {
    connection = getDbConnection();
  }

  /**
   * Get the database connection
   * 
   * @return
   */
  private Connection getDbConnection() {
    return OBDal.getInstance().getConnection();
  }

  /**
   * 
   * @param ClientId
   * @param OrgId
   * @return
   */
  public static JSONObject getExtendPeriodRecord(String ClientId, String OrgId) {
    JSONObject result = new JSONObject();
    // Client configuration for extend period
    Client clientObj = OBDal.getInstance().get(Client.class, ClientId);
    int extendAllowed = 0;
    int maxExtPeriod = 0;
    if (clientObj.getEhcmExtallowed() != null)
      extendAllowed = clientObj.getEhcmExtallowed().intValue();
    if (clientObj.getEhcmMaxextperiod() != null)
      maxExtPeriod = clientObj.getEhcmMaxextperiod().intValue();
    try {
      result.put("extendAllowed", extendAllowed);
      result.put("maxExtPeriod", maxExtPeriod);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getExtendPeriodRecord " + e.getMessage());
      e.printStackTrace();
    }
    return result;
  }

  public static EmploymentInfo getActiveEmployInfo(String employeeId) {
    OBQuery<EmploymentInfo> empInfo = null;
    EmploymentInfo empinfo = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    try {
      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "ehcmEmpPerinfo.id=:employeeId and enabled='Y' order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", employeeId);
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        empinfo = employmentInfo.get(0);
        return empinfo;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getActiveEmployInfo " + e.getMessage());
      e.printStackTrace();
    }
    return empinfo;
  }

  @SuppressWarnings("unused")
  public static EmploymentInfo getRequestedEmployeeInfo(String employeeId, String requestDatepar) {
    OBQuery<EmploymentInfo> empInfo = null;
    EmploymentInfo empinfo = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    try {
      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "as e where e.ehcmEmpPerinfo.id=:employeeId and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:requestDate,'yyyy-MM-dd') and coalesce(e.endDate,to_date('21-06-2058','dd-MM-yyyy')) >= to_date(:requestDate,'dd-MM-yyyy') order by creationDate desc");
      empInfo.setNamedParameter("employeeId", employeeId);
      empInfo.setNamedParameter("requestDate", requestDatepar);
      employmentInfo = empInfo.list();
      if ((employmentInfo.size() > 0) && (employmentInfo != null)) {
        empinfo = employmentInfo.get(0);
        return empinfo;
      }
      if (employmentInfo.size() <= 0) {
        empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            "as e where e.ehcmEmpPerinfo.id=:employeeId and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy')>= to_date(:requestDate,'yyyy-MM-dd')order by startdate asc limit 1");
        empInfo.setNamedParameter("employeeId", employeeId);
        empInfo.setNamedParameter("requestDate", requestDatepar);
        employmentInfo = empInfo.list();
        if (employmentInfo.size() > 0) {
          empinfo = employmentInfo.get(0);
          return empinfo;
        }
      }
      if (employmentInfo.size() <= 0) {
        empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            "as e where e.ehcmEmpPerinfo.id=:employeeId and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy')<= to_date(:requestDate,'yyyy-MM-dd')order by startdate desc limit 1");
        empInfo.setNamedParameter("employeeId", employeeId);
        empInfo.setNamedParameter("requestDate", requestDatepar);
        employmentInfo = empInfo.list();
        if (employmentInfo.size() > 0) {
          empinfo = employmentInfo.get(0);
          return empinfo;
        }
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getRequestedEmployeeInfo " + e.getMessage());
      e.printStackTrace();
    }
    return empinfo;
  }

  public static Country getSaudiArabiaCountryId(String clientId) {
    OBQuery<Country> countryQry = null;
    Country country = null;
    List<Country> countryList = new ArrayList<Country>();
    try {
      countryQry = OBDal.getInstance().createQuery(Country.class,
          " as e where e.iSOCountryCode='SA' ");
      countryList = countryQry.list();
      log4j.debug("countlist:" + countryList.size());
      if (countryList.size() > 0) {
        country = countryList.get(0);
        return country;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getSaudiArabiaCountryId " + e.getMessage());
      e.printStackTrace();
    }
    return country;
  }

  /**
   * Calout function to get the dates by using days +startdate
   * 
   * @param clientId
   * @param days
   * @param startDate-'DD-MM-YYYY'
   *          hijirformat
   * @return
   */
  @SuppressWarnings("unchecked")
  public static Date calculateDateUsingDays(String clientId, String days, String startDate) {
    String sql = null;
    Query qry = null;
    Date enddate = null;
    try {
      log4j.debug("startDate:" + startDate);
      startDate = startDate.split("-")[2] + startDate.split("-")[1] + startDate.split("-")[0];
      sql = "select a.gregorian_date, a.hijri_date from ( select max( gregorian_date) as gregorian_date ,hijri_date from eut_hijri_dates  where   hijri_date >= :startdate   group  by hijri_date "
          + " order by hijri_date asc limit  :limits ) a  order by  a.gregorian_date  desc  ";
      qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      qry.setParameter("startdate", startDate);
      qry.setParameter("limits", Integer.valueOf(days));
      qry.setMaxResults(1);
      if (qry != null && qry.list().size() > 0) {
        List<Object> object = qry.list();
        Object[] row = (Object[]) object.get(0);
        enddate = (Date) row[0];
        log4j.debug("row[0]:" + row[1].toString());
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in calculateDateUsingDays " + e.getMessage());
      e.printStackTrace();
    }
    return enddate;
  }

  public static Date calDateUsingDaysWithGreDate(String clientId, int days, Date startDate) {
    String sql = null;
    Query qry = null;
    Date enddate = null;
    try {
      log4j.debug("startDate:" + startDate);
      sql = "select a.gregorian_date, a.hijri_date from ( select max( gregorian_date) as gregorian_date ,hijri_date from eut_hijri_dates  where   gregorian_date >= :startdate   group  by hijri_date "
          + " order by hijri_date asc limit  :limits ) a  order by  a.gregorian_date  desc  ";
      qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      qry.setParameter("startdate", startDate);
      qry.setParameter("limits", days);
      qry.setMaxResults(1);
      if (qry != null && qry.list().size() > 0) {
        List<Object> object = qry.list();
        Object[] row = (Object[]) object.get(0);
        enddate = (Date) row[0];
        log4j.debug("row[0]:" + row[1].toString());
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in calculateDateUsingDays " + e.getMessage());
      e.printStackTrace();
    }
    return enddate;
  }

  public static EHCMEmpSupervisor getSupervisorforEmployee(String employeeId, String clientId) {
    EHCMEmpSupervisor empSupervisor = null;
    List<EHCMEmpSupervisorNode> superVisornode = new ArrayList<EHCMEmpSupervisorNode>();
    try {
      OBQuery<EHCMEmpSupervisorNode> supervisior = OBDal.getInstance().createQuery(
          EHCMEmpSupervisorNode.class,
          "  as e where e.ehcmEmpPerinfo.id=:employeeId and e.client.id =:clientId");
      supervisior.setNamedParameter("employeeId", employeeId);
      supervisior.setNamedParameter("clientId", clientId);
      superVisornode = supervisior.list();
      if (superVisornode.size() > 0) {
        empSupervisor = superVisornode.get(0).getEhcmEmpSupervisor();
        return empSupervisor;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getSupervisorforEmployee " + e.getMessage());
      e.printStackTrace();
    }
    return empSupervisor;
  }

  public static String getEmployeeEmploymentStatus(EmploymentInfo emplinfo) {
    String status = null;
    try {
      // check empsecondmentid, extrasetpsid,extendid,scholarshipId, suspensionendid is not null or
      // not
      if (emplinfo.getEhcmEmpSecondment() == null || emplinfo.getEhcmEmpExtrastep() == null
          || emplinfo.getEhcmExtendService() == null || emplinfo.getEhcmEmpScholarship() == null
          || emplinfo.getEhcmEmpSuspension() == null) {
        status = EmployeeStatus_Active;
      }
      // secondment
      else if (emplinfo.getEhcmEmpSecondment() != null) {
        if (emplinfo.getEhcmEmpSecondment().getDecisionType().equals("CO"))
          status = EmployeeStatus_Active;
        else
          status = EmployeeStatus_Secondment;

      }
      // Extra Steps
      else if (emplinfo.getEhcmEmpExtrastep() != null) {
        status = EmployeeStatus_ExtraSteps;
      }
      // Scholarship
      else if (emplinfo.getEhcmEmpScholarship() != null) {
        status = EmployeeStatus_ScholarshipTraining;
      }
      // supervisor
      else if (emplinfo.getEhcmEmpSuspension() != null) {
        if (emplinfo.getEhcmEmpSuspension().getSuspensionType().equals("SUE"))
          status = EmployeeStatus_Active;
        else
          status = EmployeeStatus_Suspension;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getEmployeeEmploymentStatus " + e.getMessage());
      e.printStackTrace();
    }
    return status;
  }

  public static JSONObject calculateEmpExtScholarship(String employeeId,
      EHCMEmpScholarship empScholarship) {
    int sumOfExtendEmpScholarship = 0;
    int empScholarshipDays = 0;
    EHCMScholarshipSummary scholarSummary = null;
    List<EHCMScholarshipSummary> scholarSummaryList = new ArrayList<EHCMScholarshipSummary>();
    JSONObject result = new JSONObject();
    Boolean extendnotAllowFlag = false;
    EmpScholarshipTrainingDAO empScholarshipTrainingDAOImpl = new EmpScholarshipTrainingDAOImpl();
    try {
      EHCMScholarshipSummary activeempScholarshipSummaryInfo = empScholarshipTrainingDAOImpl
          .getActiveScholarshipSummary(employeeId, empScholarship.getOriginalDecisionNo().getId());

      OBQuery<EHCMScholarshipSummary> scholarshipSummaryQry = OBDal.getInstance().createQuery(
          EHCMScholarshipSummary.class,
          " as e where e.creationDate <=:creationdate  and  e.employee.id=:employeeId  and e.decisionType in ('CR','UP','EX') order by e.creationDate desc ");
      scholarshipSummaryQry.setNamedParameter("creationdate",
          activeempScholarshipSummaryInfo.getCreationDate());
      scholarshipSummaryQry.setNamedParameter("employeeId", employeeId);
      scholarSummaryList = scholarshipSummaryQry.list();
      if (scholarSummaryList.size() > 0) {
        for (EHCMScholarshipSummary scholarshipSummary : scholarSummaryList) {
          log4j.debug("info changereason" + scholarshipSummary.getDecisionType());
          if (scholarshipSummary.getEhcmEmpScholarship() != null
              && scholarshipSummary.getDecisionType().equals("EX")) {
            sumOfExtendEmpScholarship += scholarshipSummary.getEhcmEmpScholarship().getNoofdays()
                .intValue();
          } else if ((scholarshipSummary.getDecisionType().equals("CR")
              || scholarshipSummary.getDecisionType().equals("UP"))
              && scholarshipSummary.getEhcmEmpScholarship() != null) {
            empScholarshipDays = scholarshipSummary.getEhcmEmpScholarship().getNoofdays()
                .intValue();
            break;
          }
        }
      }
      sumOfExtendEmpScholarship += empScholarship.getNoofdays().intValue();
      if (sumOfExtendEmpScholarship > empScholarshipDays) {
        extendnotAllowFlag = true;
      }
      result.put("sumofExtendEmpScholarship", sumOfExtendEmpScholarship);
      result.put("empScholarshipDays", empScholarshipDays);
      result.put("extendnotAllowFlag", extendnotAllowFlag);
      return result;

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in calculateEmpExtScholarship " + e.getMessage());
      e.printStackTrace();
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in calculateEmpExtScholarship " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in calculateEmpExtScholarship " + e.getMessage());
      e.printStackTrace();
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public static int calculatetheDays(String startDate, String enddate) {
    String sql = null;
    Query qry = null;
    BigInteger count = BigInteger.ZERO;
    int countofDays = 0;
    try {

      startDate = startDate.split("-")[2] + startDate.split("-")[1] + startDate.split("-")[0];
      enddate = enddate.split("-")[2] + enddate.split("-")[1] + enddate.split("-")[0];
      sql = " select COUNT (distinct hijri_date)  from eut_hijri_dates  where   hijri_date >= :startdate   and hijri_date <= :enddate ";
      qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      qry.setParameter("startdate", startDate);
      qry.setParameter("enddate", enddate);

      if (qry != null && qry.list().size() > 0) {
        List<Object> object = qry.list();
        Object row = object.get(0);
        count = (BigInteger) row;
        countofDays = count.intValue();
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in calculatetheDays " + e.getMessage());
      e.printStackTrace();
    }
    return countofDays;
  }

  public static int caltheDaysUsingGreDate(Date startDate, Date enddate) {
    String sql = null;
    Query qry = null;
    BigInteger count = BigInteger.ZERO;
    int countofDays = 0;
    try {

      sql = " select COUNT (distinct hijri_date)  from eut_hijri_dates  where   gregorian_date >= :startdate   and gregorian_date <= :enddate ";
      qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      qry.setParameter("startdate", startDate);
      qry.setParameter("enddate", enddate);

      if (qry != null && qry.list().size() > 0) {
        List<Object> object = qry.list();
        Object row = object.get(0);
        count = (BigInteger) row;
        countofDays = count.intValue();
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in caltheDaysUsingGreDate " + e.getMessage());
      e.printStackTrace();
    }
    return countofDays;
  }

  @SuppressWarnings("unchecked")
  public static JSONObject overlapWithDecisionsDate(String type, String startDate, String endDate,
      String employeeId) {
    List<EHCMScholarshipSummary> empScholarshipList = new ArrayList<EHCMScholarshipSummary>();
    List<EHCMBusMissionSummary> businessMissionSummList = new ArrayList<EHCMBusMissionSummary>();
    List<EHCMEmpLeaveln> leaveLnList = new ArrayList<EHCMEmpLeaveln>();
    JSONObject result = new JSONObject();
    String sql = null;
    String sql1 = null;
    try {
      sql = "  and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate)  "
          + " and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + " or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + " and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')))  ";

      sql1 = " and e.enabled='Y' order by e.creationDate desc  ";

      result.put("errorFlag", false);
      result.put("errormsg", "null");
      if (StringUtils.isEmpty(endDate)) {
        endDate = "21-06-2058";
      }

      // scholarship overlap
      if ((type != null && type.equals(Constants.SCHOLARSHIP_OVERLAP)) || type == null) {
        OBQuery<EHCMScholarshipSummary> empScholarshipQry = OBDal.getInstance().createQuery(
            EHCMScholarshipSummary.class,
            " as e where e.employee.id=:employeeId   " + sql + " " + sql1);
        empScholarshipQry.setNamedParameter("employeeId", employeeId);
        empScholarshipQry.setNamedParameter("fromdate", startDate);
        empScholarshipQry.setNamedParameter("todate", endDate);
        log4j.debug("empScholarshipQry:" + empScholarshipQry.getWhereAndOrderBy());
        empScholarshipQry.setMaxResult(1);
        empScholarshipList = empScholarshipQry.list();
        if (empScholarshipList.size() > 0) {
          EHCMEmpScholarship empScholarShip = empScholarshipList.get(0).getEhcmEmpScholarship();
          if (empScholarShip.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
            result = chkCutOffDecisionOverlap(Constants.SCHOLARSHIP_OVERLAP, empScholarShip.getId(),
                startDate, endDate, employeeId, sql, sql1, null);
            if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
              return result;
            }
          } else {
            result.put("errorFlag", true);
            result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpScholarship_CreCant"));
            result.put("scholarShipId", empScholarShip.getId());
          }
        }
      }
      // leave overlap
      if ((type != null && type.equals(Constants.ABSENCE_OVERLAP)) || type == null) {
        OBQuery<EHCMEmpLeaveln> leaveLnQry = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class,
            " as e where  e.ehcmEmpLeave.id in ( select lev.id from EHCM_Emp_Leave lev "
                + " where  lev.ehcmEmpPerinfo.id=:employeeId )  " + sql
                + " and e.leaveType<>'AC' and e.ehcmDecisionBalance.id is null order by e.creationDate desc  ");
        leaveLnQry.setNamedParameter("employeeId", employeeId);
        leaveLnQry.setNamedParameter("fromdate", startDate);
        leaveLnQry.setNamedParameter("todate", endDate);
        log4j.debug("empScholarshipQry:" + leaveLnQry.getWhereAndOrderBy());
        leaveLnQry.setMaxResult(1);
        leaveLnList = leaveLnQry.list();
        if (leaveLnList.size() > 0) {
          EHCMAbsenceAttendance absenceAttendanceObj = leaveLnList.get(0)
              .getEhcmAbsenceAttendance();
          // if current decision is cutoff
          if (absenceAttendanceObj.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
            result = chkCutOffDecisionOverlap(Constants.ABSENCE_OVERLAP,
                absenceAttendanceObj.getId(), startDate, endDate, employeeId, sql, sql1, null);
            if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
              return result;
            }

          } else {
            log4j.debug("absenceAttendanceObj:" + absenceAttendanceObj);
            result.put("errorFlag", true);
            result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpHad_Lev"));
            result.put("absenceDecisionId", absenceAttendanceObj.getId());
            return result;
          }
        }
      }

      if ((type != null && type.equals(Constants.BUSINESSMISSION_OVERLAP)) || type == null) {
        OBQuery<EHCMBusMissionSummary> businessMissionSummQry = OBDal.getInstance().createQuery(
            EHCMBusMissionSummary.class,
            " as e where e.employee.id=:employeeId   " + sql + " " + sql1);
        businessMissionSummQry.setNamedParameter("employeeId", employeeId);
        businessMissionSummQry.setNamedParameter("fromdate", startDate);
        businessMissionSummQry.setNamedParameter("todate", endDate);
        log4j.debug("empScholarshipQry:" + businessMissionSummQry.getWhereAndOrderBy());
        businessMissionSummList = businessMissionSummQry.list();
        if (businessMissionSummList.size() > 0) {

          EHCMEmpBusinessMission empbusinessMission = businessMissionSummList.get(0)
              .getEhcmEmpBusinessmission();
          // if current decision is cutoff
          if (empbusinessMission.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
            result = chkCutOffDecisionOverlap(Constants.BUSINESSMISSION_OVERLAP,
                empbusinessMission.getId(), startDate, endDate, employeeId, sql, sql1, null);
            if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
              return result;
            }

          } else {
            log4j.debug("empScholarshipList:" + businessMissionSummList.get(0));
            result.put("errorFlag", true);
            result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpBusMission_CreCant"));
            result.put("businessMissionId", empbusinessMission.getId());
            return result;
          }

        }
      }

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in overlapWithDecisionsDate " + e.getMessage());
      e.printStackTrace();
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in overlapWithDecisionsDate " + e.getMessage());
      e.printStackTrace();
    }
    return result;
  }

  public static EmploymentInfo getPromotionEmployeeInfo(String employeeId) {
    OBQuery<EmploymentInfo> empInfo = null;
    EmploymentInfo empinfo = null;
    List<EmploymentInfo> employmentInfo = new ArrayList<EmploymentInfo>();
    try {
      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e where e.ehcmEmpPerinfo.id=:employeeId  and e.enabled='Y' and issecondment='N' order by e.creationDate desc");
      empInfo.setNamedParameter("employeeId", employeeId);
      employmentInfo = empInfo.list();
      if (employmentInfo.size() > 0) {
        empinfo = employmentInfo.get(0);
        return empinfo;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getActiveEmployInfo " + e.getMessage());
      e.printStackTrace();
    }
    return empinfo;
  }

  public static boolean chkDecisionNoUsedInJWR(EHCMEmpScholarship empScholarShip) {
    Boolean isUsedInJWR = false;
    List<EhcmJoiningWorkRequest> jWRList = new ArrayList<EhcmJoiningWorkRequest>();
    List<EHCMScholarshipSummary> schoSummaryList = new ArrayList<EHCMScholarshipSummary>();
    try {
      OBQuery<EhcmJoiningWorkRequest> jWRQry = OBDal.getInstance().createQuery(
          EhcmJoiningWorkRequest.class, " as e where e.originalDecisionNo.id=:originalDecNo ");

      if (empScholarShip != null) {
        OBQuery<EHCMScholarshipSummary> schoSummaryQry = OBDal.getInstance().createQuery(
            EHCMScholarshipSummary.class, " as e where e.ehcmEmpScholarship.id=:empScholarShipId ");
        schoSummaryQry.setNamedParameter("empScholarShipId", empScholarShip.getId());
        schoSummaryQry.setMaxResult(1);
        schoSummaryList = schoSummaryQry.list();

        if (schoSummaryList.size() > 0) {
          jWRQry.setNamedParameter("originalDecNo", schoSummaryList.get(0).getId());
        }
      }
      jWRQry.setMaxResult(1);
      jWRList = jWRQry.list();
      if (jWRList.size() > 0) {
        isUsedInJWR = true;
        return isUsedInJWR;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in chkDecisionNoUsedInJWR " + e.getMessage());
      e.printStackTrace();
    }
    return isUsedInJWR;
  }

  public static EHCMMisCatPeriod getMissionPeriod(String clientId, EHCMMissionCategory missCategory,
      String startDate, String Enddate) {
    BigDecimal missionBal = BigDecimal.ZERO;
    EHCMMisCatPeriod misCatPrd = null;
    List<EHCMMisCatPeriod> misCatPrdList = new ArrayList<EHCMMisCatPeriod>();
    try {
      /*
       * OBQuery<EHCMMisCatPeriod> misCatPeriodQry =
       * OBDal.getInstance().createQuery(EHCMMisCatPeriod.class,
       * " as e where e.ehcmMissionCategory.id=:missCategoryId  " +
       * " and e.enabled='Y' and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate)   and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
       * +
       * " or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate)  and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) "
       * );
       */
      OBQuery<EHCMMisCatPeriod> misCatPeriodQry = OBDal.getInstance().createQuery(
          EHCMMisCatPeriod.class, " as e where e.ehcmMissionCategory.id=:missCategoryId  "
              + " and e.enabled='Y' and  to_date(:fromdate)  between e.startDate and e.endDate and e.client.id=:clientId order by e.creationDate asc ");
      misCatPeriodQry.setNamedParameter("missCategoryId", missCategory.getId());
      misCatPeriodQry.setNamedParameter("fromdate", startDate);
      misCatPeriodQry.setNamedParameter("clientId", clientId);
      // misCatPeriodQry.setNamedParameter("todate", Enddate);
      misCatPeriodQry.setMaxResult(1);
      misCatPeriodQry.setFilterOnReadableClients(false);
      misCatPeriodQry.setFilterOnReadableOrganization(false);
      log4j.debug("misCatPeriodQry:" + misCatPeriodQry.getWhereAndOrderBy());
      if (misCatPeriodQry != null) {
        misCatPrdList = misCatPeriodQry.list();

        if (misCatPrdList.size() > 0) {
          misCatPrd = misCatPrdList.get(0);
        }
      }

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getMissionBalance " + e.getMessage());
      e.printStackTrace();
    }
    return misCatPrd;
  }

  /**
   * Return available mission balance days for employee based on mission category
   * 
   * @param clientId
   * @param missCategory
   * @param convertToGregorian_tochar
   * @param convertToGregorian_tochar2
   * @return
   */
  public static Long getMissionBalanceDays(String clientId, EHCMMissionCategory missCategory,
      String strStartDate, String strEndDate, String employeeId) {
    // TODO Auto-generated method stub
    MissionCategoryDAO missionCategoryDAO = new MissionCategoryDAOImpl();
    EHCMMiscatEmployee misCatEmp = null;
    Long missionBalanceDays = (long) 0;
    try {
      OBContext.setAdminMode();
      EHCMMisCatPeriod missionPeriodOB = getMissionPeriod(clientId, missCategory, strStartDate,
          strEndDate);
      if (null != missionPeriodOB) {
        misCatEmp = missionCategoryDAO.getEmployeeinPeriod(missionPeriodOB, employeeId);
      }
      if (null != misCatEmp) {
        missionBalanceDays = (missionPeriodOB.getDays() - misCatEmp.getUseddays());
      }
    } catch (Exception e) {
      log4j.error("Exception in getGetMissionBalanceDays " + e.getMessage());
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }

    return missionBalanceDays;
  }

  /**
   * Return available mission balance days for employee based on mission category on update process
   * 
   * @param clientId
   * @param missCategory
   * @param convertToGregorian_tochar
   * @param convertToGregorian_tochar2
   * @return
   */
  public static Long getMissionBalanceDaysOnUpdate(String clientId,
      EHCMMissionCategory missCategory, String strStartDate, String strEndDate, String employeeId,
      String inporiginalDecisionNo) {
    // TODO Auto-generated method stub

    Long missionBalanceDays = (long) 0;
    Long originalDecisionMissionBalanceDays = (long) 0;
    EHCMMisCatPeriod originalMisCatPrd = null;
    try {
      OBContext.setAdminMode();
      missionBalanceDays = getMissionBalanceDays(clientId, missCategory, strStartDate, strEndDate,
          employeeId);
      EHCMEmpBusinessMission originalDecBusMission = OBDal.getInstance()
          .get(EHCMEmpBusinessMission.class, inporiginalDecisionNo);
      EHCMMisCatPeriod misCatPrd = sa.elm.ob.hcm.util.UtilityDAO.getMissionPeriod(clientId,
          missCategory, strStartDate, strEndDate);
      if (originalDecBusMission != null) {
        originalMisCatPrd = sa.elm.ob.hcm.util.UtilityDAO.getMissionPeriod(clientId, missCategory,
            sa.elm.ob.utility.util.Utility.formatDate(originalDecBusMission.getStartDate()),
            sa.elm.ob.utility.util.Utility.formatDate(originalDecBusMission.getEndDate()));
      }
      if ((originalDecBusMission != null
          && missCategory.getId().equals(originalDecBusMission.getMissionCategory().getId()))
          && originalMisCatPrd != null && originalMisCatPrd.getId().equals(misCatPrd.getId())) {
        originalDecisionMissionBalanceDays = Long
            .valueOf(originalDecBusMission.getMissionDays().toString());
      }

      missionBalanceDays = missionBalanceDays + originalDecisionMissionBalanceDays;
    } catch (Exception e) {
      log4j.error("Exception in getGetMissionBalanceDays on update " + e.getMessage());
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }
    return missionBalanceDays;
  }

  /**
   * Used to determine the decison no in emp overtime screen
   * 
   * @param employeeId
   * @return
   */

  public static EhcmEmployeeOvertime getOvertimeEmployee(String employeeId) {
    OBQuery<EhcmEmployeeOvertime> objEmpQuery = null;
    EhcmEmployeeOvertime overtimeInfo = null;
    List<EhcmEmployeeOvertime> overtimeList = new ArrayList<EhcmEmployeeOvertime>();
    try {
      objEmpQuery = OBDal.getInstance().createQuery(EhcmEmployeeOvertime.class,
          "as e where e.employee.id=:employeeId and e.enabled='Y' and e.issueDecision='Y'  order by e.creationDate desc");
      objEmpQuery.setNamedParameter("employeeId", employeeId);
      objEmpQuery.setMaxResult(1);
      overtimeList = objEmpQuery.list();
      if (overtimeList.size() > 0) {
        overtimeInfo = overtimeList.get(0);
        return overtimeInfo;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getOvertimeEmployee " + e.getMessage());
      e.printStackTrace();
    }
    return overtimeInfo;
  }

  /**
   * Used to determine the decison no in discipline action
   * 
   * @param employeeId
   * @return
   */

  public static EhcmDisciplineAction getDisciplineActionDecisionNo(String employeeId) {
    OBQuery<EhcmDisciplineAction> objEmpQuery = null;
    EhcmDisciplineAction disciplineInfo = null;
    List<EhcmDisciplineAction> disciplineList = new ArrayList<EhcmDisciplineAction>();
    try {
      objEmpQuery = OBDal.getInstance().createQuery(EhcmDisciplineAction.class,
          "as e where e.employee.id=:employeeId and e.enabled='Y' and e.issueDecision='Y'  order by e.creationDate desc");
      objEmpQuery.setNamedParameter("employeeId", employeeId);
      objEmpQuery.setMaxResult(1);
      disciplineList = objEmpQuery.list();
      if (disciplineList.size() > 0) {
        disciplineInfo = disciplineList.get(0);
        return disciplineInfo;
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getDisciplineActionDecisionNo " + e.getMessage());
      e.printStackTrace();
    }
    return disciplineInfo;
  }

  /**
   * 
   * @param clientId
   * @param number
   * @return
   */
  public static BigDecimal getHoursfromLookup(String clientId, String number) {
    String sql = null;
    Query qry = null;
    BigDecimal hours = null;

    try {
      // Getting the value before decimal point
      sql = "SELECT TRUNC(" + number + ") FROM DUAL ";
      qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      if (qry.list().size() > 0) {
        Object truncate = qry.list().get(0);
        hours = (BigDecimal) truncate;

      }

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getHoursfromLookup " + e.getMessage());
      e.printStackTrace();
    }
    return hours;
  }

  /**
   * 
   * @param startDate
   * @param endDate
   * @param Type
   * @param overtimeId
   * @return
   */
  public static int getDaysFromHolidayCalendar(String startDate, String endDate, String Type,
      String overtimeId, String clientId) {

    int countofDays = 0;
    String sql = null;
    Query qry = null;
    BigInteger count = BigInteger.ZERO;

    try {

      sql = " select count(holidaydate) from ehcm_holiday_calendar where  holidaydate >= '"
          + startDate + "'  and holidaydate <= '" + endDate + "'  "
          + " and holiday_type =:Type and ad_client_id = :clientId ";
      qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      qry.setParameter("Type", Type);
      qry.setParameter("clientId", clientId);
      if (qry != null && qry.list().size() > 0) {
        List<Object> object = qry.list();
        Object row = object.get(0);
        count = (BigInteger) row;
        countofDays = count.intValue();
      }

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getDaysFromHolidayCalendar " + e.getMessage());
      e.printStackTrace();
    }
    return countofDays;
  }

  /**
   * 
   * @param startDate
   * @param endDate
   * @param overtimeId
   * @return
   */

  public static int getWorkingDaysFromHolidayCalendar(String startDate, String endDate,
      String overtimeId, String clientId) {
    int countofDays = 0;
    String sql = null;
    Query qry = null;
    BigInteger count = BigInteger.ZERO;

    try {
      sql = " select count(holidaydate) from ehcm_holiday_calendar where  holidaydate >= '"
          + startDate + "'  and holidaydate <= '" + endDate + "' and ad_client_id = :clientId ";
      qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      qry.setParameter("clientId", clientId);
      if (qry != null && qry.list().size() > 0) {
        List<Object> object = qry.list();
        Object row = object.get(0);
        count = (BigInteger) row;
        countofDays = count.intValue();
      }

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getWorkingDaysFromHolidayCalendar " + e.getMessage());
      e.printStackTrace();
    }
    return countofDays;
  }

  /**
   * 
   * @param overtimeType
   * @return
   */
  public static boolean chkExistOverTimeType(EhcmOvertimeType overtimeType) {
    boolean chkExist = false;
    try {
      OBQuery<EhcmOvertimeType> type = OBDal.getInstance().createQuery(EhcmOvertimeType.class,
          " ( workingdays=:workingdays and weekendonedays=:weekendonedays and weekendtwodays=:weekendtwodays and feterdays=:feterdays and hajjdays=:hajjdays and nationalday=:nationalday   ) and client.id =:client ");
      type.setNamedParameter("workingdays", overtimeType.isWorkingdays());
      type.setNamedParameter("weekendonedays", overtimeType.isWeekendonedays());
      type.setNamedParameter("weekendtwodays", overtimeType.isWeekendtwodays());
      type.setNamedParameter("feterdays", overtimeType.isFeterdays());
      type.setNamedParameter("hajjdays", overtimeType.isHajjdays());
      type.setNamedParameter("nationalday", overtimeType.isNationalday());
      type.setNamedParameter("client", overtimeType.getClient().getId());
      if (type.list().size() > 0) {
        chkExist = true;
      }

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in chkExistOverTimeType " + e.getMessage());
      e.printStackTrace();
    }
    return chkExist;
  }

  /**
   * 
   * @param overtimeType
   * @return
   */

  public static boolean chkRecordinTransaction(EhcmOvertimeType overtimeType) {
    boolean chkExist = false;
    try {
      OBQuery<EhcmEmployeeOvertime> overtimeObj = OBDal.getInstance()
          .createQuery(EhcmEmployeeOvertime.class, " as e where e.ehcmOvertimeType.id=:TypeId ");
      overtimeObj.setNamedParameter("TypeId", overtimeType.getId());
      if (overtimeObj.list().size() > 0) {
        chkExist = true;
      }

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in chkRecordinTransaction " + e.getMessage());
      e.printStackTrace();
    }
    return chkExist;
  }

  /**
   * This function is used to bring the hours from hcm reference lookup window
   * 
   * @param clientId
   * @return
   */
  public static JSONObject gethoursFromReflookup(String clientId) {
    JSONArray jsonArray = new JSONArray();
    JSONObject json = new JSONObject();
    List<EHCMDeflookupsTypeLn> lookupList = new ArrayList<EHCMDeflookupsTypeLn>();
    try {
      OBQuery<EHCMDeflookupsTypeLn> reflookUp = OBDal.getInstance()
          .createQuery(EHCMDeflookupsTypeLn.class, "client.id='" + clientId
              + "' and (ehcmDeflookupsType.reference =:reference and ehcmDeflookupsType.enabled = 'Y') and enabled = 'Y'  ");
      reflookUp.setNamedParameter("reference", Employee_Overtime_Hours);
      lookupList = reflookUp.list();
      if (lookupList != null && lookupList.size() > 0) {
        for (int i = 0; i < lookupList.size(); i++) {
          JSONObject jsonData = new JSONObject();
          jsonData.put("id", lookupList.get(i).getId());
          jsonData.put("name", lookupList.get(i).getName());
          jsonArray.put(jsonData);
        }
      }

      if (jsonArray.length() > 0)
        json.put("data", jsonArray);

    } catch (JSONException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in gethoursFromReflookup " + e.getMessage());
      e.printStackTrace();
    }
    return json;
  }

  /**
   * get all decision overlap line and forming the json object based on current decision type
   * 
   * @param decisionType
   * @param startDate
   * @param endDate
   * @param employeeId
   * @param Type
   * @param currentDecisionId
   * @return
   */
  public static JSONObject chkDecisionOverlap(String decisionType, String startDate, String endDate,
      String employeeId, String Type, String currentDecisionId) {
    List<EHCMDecisionOverlapLn> decisionOverlapList = new ArrayList<EHCMDecisionOverlapLn>();
    JSONObject result = new JSONObject(), json = null;
    JSONArray jsonArray = new JSONArray();
    String condition = null;
    final String susRefId = "1DF68D671A694A7DA14EB8A157BFFF66";
    final String delRefId = "EEA13D2953444F3B86A42420720F2103";
    try {

      // in delegation and suspension we are using reference list value so for that we can check
      // with search key value others we can use id(foreign key)
      if (decisionType.equals(Constants.DELEGATION_OVERLAP)) {
        condition = "  e.ehcmDecisionSubtypeV.id=( select reflist.id from ADList reflist where reflist.reference.id=:delRefId and reflist.searchKey=:typeId )  ";
      } else if (decisionType.equals(Constants.SUSPENSION_OVERLAP)) {
        condition = "  e.ehcmDecisionSubtypeV.id=( select reflist.id from ADList reflist where reflist.reference.id=:susRefId and reflist.searchKey=:typeId )  ";
      } else {
        condition = "   e.ehcmDecisionSubtypeV.id=:typeId ";
      }

      // fetch the decision overlap line
      OBQuery<EHCMDecisionOverlapLn> decOverlaplnQry = OBDal.getInstance().createQuery(
          EHCMDecisionOverlapLn.class,
          " as e where e.ehcmDecisionOverlap.decisionType =:decisionType "
              + "  and ( e.ehcmDecisionSubtypeV is null  or " + condition
              + "  )  order by e.decisionType  , e.ehcmDecisionSubtypeV  asc ");
      decOverlaplnQry.setNamedParameter("decisionType", decisionType);
      if (decisionType.equals(Constants.DELEGATION_OVERLAP))
        decOverlaplnQry.setNamedParameter("delRefId", delRefId);
      else if (decisionType.equals(Constants.SUSPENSION_OVERLAP))
        decOverlaplnQry.setNamedParameter("susRefId", susRefId);
      decOverlaplnQry.setNamedParameter("typeId", Type);
      log4j.error("decOverlaplnQry1 " + decOverlaplnQry.getWhereAndOrderBy());

      decisionOverlapList = decOverlaplnQry.list();

      if (decisionOverlapList.size() > 0) {
        for (EHCMDecisionOverlapLn ln : decisionOverlapList) {
          if (json != null && json.has("decisionType")
              && !json.getString("decisionType").equals(ln.getDecisionType())) {
            json = new JSONObject();
            if (ln.getEhcmDecisionSubtypeV() != null) {
              json.put("decisionSubtypeId", ln.getEhcmDecisionSubtypeV().getId());
            } else {
              json.put("decisionSubtypeId", "");
            }
            json.put("decisionType", ln.getDecisionType());
            json.put("isAllowedOverlap", ln.isAllowedoverlap());
            jsonArray.put(json);
          } else if (json != null && json.has("decisionType")
              && json.getString("decisionType").equals(ln.getDecisionType())) {
            // dont allow to add json object if same decision type added already
          } else {
            json = new JSONObject();
            if (ln.getEhcmDecisionSubtypeV() != null) {
              json.put("decisionSubtypeId", ln.getEhcmDecisionSubtypeV().getId());
            } else {
              json.put("decisionSubtypeId", "");
            }
            json.put("decisionType", ln.getDecisionType());

            json.put("isAllowedOverlap", ln.isAllowedoverlap());
            jsonArray.put(json);
          }
          // result.put("decisionList", jsonArray);
        }

        // move the current processing window decision type as last position using comparator update
        // case
        List<JSONObject> jsonobject = sortDecsionType(jsonArray, decisionType);
        result.put("decisionList", jsonobject);
        // check overlap
        result = chkSubDecisionOverlap(result, startDate, endDate, employeeId, currentDecisionId);
        if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
          return result;
        }
      }

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in chkDecisionOverlap " + e.getMessage());
      e.printStackTrace();
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in chkDecisionOverlap " + e.getMessage());
      e.printStackTrace();
    }
    return result;
  }

  /**
   * check decision is overlapping based on decision overlap line
   * 
   * @param decisionResult
   * @param startDate
   * @param endDate
   * @param employeeId
   * @param currentDecisionId
   * @return
   */
  public static JSONObject chkSubDecisionOverlap(JSONObject decisionResult, String startDate,
      String endDate, String employeeId, String currentDecisionId) {
    List<EHCMBusMissionSummary> businessMissionSummList = new ArrayList<EHCMBusMissionSummary>();
    List<EHCMEmpLeaveln> leaveLnList = new ArrayList<EHCMEmpLeaveln>();
    List<EHCMScholarshipSummary> empScholarshipList = new ArrayList<EHCMScholarshipSummary>();
    List<EhcmEmployeeOvertime> empOvertimeList = new ArrayList<EhcmEmployeeOvertime>();
    List<EmploymentInfo> empDelegationList = new ArrayList<EmploymentInfo>();
    List<EmploymentInfo> secondmentList = new ArrayList<EmploymentInfo>();
    List<EmploymentInfo> empSuspensionList = new ArrayList<EmploymentInfo>();
    List<EmploymentInfo> empTerminationList = new ArrayList<EmploymentInfo>();
    List<EhcmDisciplineAction> discliplineList = new ArrayList<EhcmDisciplineAction>();
    JSONObject result = new JSONObject(), json = null;
    JSONArray jsonArray = new JSONArray();
    String sql = null;
    String sql1 = "";
    String decisionSubTypesql = "";
    String delegationsql = null;
    try {

      sql = "  and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate)  "
          + " and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + " or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + " and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')))  ";

      sql1 = "   order by e.creationDate desc  ";// and e.enabled='Y' and e.id <>:currentDecisionId

      delegationsql = "  and ((to_date(to_char(e.sECStartdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate)  "
          + " and to_date(to_char(coalesce (e.sECEnddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + " or (to_date(to_char( coalesce (e.sECEnddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + " and to_date(to_char(e.sECStartdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')))  ";

      result.put("errorFlag", false);
      result.put("errormsg", "null");
      if (StringUtils.isEmpty(endDate)) {
        endDate = "21-06-2058";
      }

      if (decisionResult != null) {
        JSONArray decisionarray = decisionResult.getJSONArray("decisionList");
        for (int i = 0; i < decisionarray.length(); i++) {
          json = decisionarray.getJSONObject(i);
          if (json.has("isAllowedOverlap") && json.getBoolean("isAllowedOverlap")) {
            continue;
          } else {
            // scholarship overlap
            if (StringUtils.isNotEmpty(json.getString("decisionType"))) {
              if (json.getString("decisionType").equals(Constants.SCHOLARSHIP_OVERLAP)) {
                OBQuery<EHCMScholarshipSummary> empScholarshipQry = OBDal.getInstance().createQuery(
                    EHCMScholarshipSummary.class,
                    " as e where e.employee.id=:employeeId   " + sql + " " + sql1);
                empScholarshipQry.setNamedParameter("employeeId", employeeId);
                empScholarshipQry.setNamedParameter("fromdate", startDate);
                empScholarshipQry.setNamedParameter("todate", endDate);
                // empScholarshipQry.setNamedParameter("currentDecisionId", currentDecisionId);
                log4j.debug("empScholarshipQry:" + empScholarshipQry.getWhereAndOrderBy());
                empScholarshipQry.setMaxResult(1);
                empScholarshipList = empScholarshipQry.list();
                if (empScholarshipList.size() > 0) {
                  EHCMEmpScholarship empScholarship = empScholarshipList.get(0)
                      .getEhcmEmpScholarship();
                  // if current decision is cutoff
                  if (empScholarshipList.get(0).getDecisionType()
                      .equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
                    result = chkCutOffDecisionOverlap(json.getString("decisionType"),
                        empScholarship.getId(), startDate, endDate, employeeId, sql, sql1,
                        currentDecisionId);
                    if (result != null && result.has("errorFlag")
                        && result.getBoolean("errorFlag")) {
                      return result;
                    }

                  } else {
                    result.put("errorFlag", true);
                    result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpScholarship_CreCant"));
                    result.put("scholarShipId", empScholarship.getId());
                    return result;
                  }
                }
              }
              // leave overlap
              else if (json.getString("decisionType").equals(Constants.ABSENCE_OVERLAP)) {

                OBQuery<EHCMEmpLeaveln> leavelnQry = OBDal.getInstance().createQuery(
                    EHCMEmpLeaveln.class,
                    " as e where  e.ehcmEmpLeave.id in ( select lev.id from EHCM_Emp_Leave lev where  lev.ehcmEmpPerinfo.id=:employeeId )  "
                        + sql
                        + " and e.leaveType<>'AC' and e.ehcmAbsenceAttendance.id is not null ");// and
                leavelnQry.setNamedParameter("employeeId", employeeId);
                leavelnQry.setNamedParameter("fromdate", startDate);
                leavelnQry.setNamedParameter("todate", endDate);
                log4j.debug("empScholarshipQry:" + leavelnQry.getWhereAndOrderBy());
                leavelnQry.setMaxResult(1);
                leaveLnList = leavelnQry.list();
                if (leaveLnList.size() > 0) {
                  EHCMAbsenceAttendance absAttendanceObj = leaveLnList.get(0)
                      .getEhcmAbsenceAttendance();
                  // if current decision is cutoff
                  if (absAttendanceObj.getDecisionType()
                      .equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
                    result = chkCutOffDecisionOverlap(Constants.ABSENCE_OVERLAP,
                        absAttendanceObj.getId(), startDate, endDate, employeeId, sql, sql1,
                        currentDecisionId);
                    if (result != null && result.has("errorFlag")
                        && result.getBoolean("errorFlag")) {
                      return result;
                    }

                  } else {
                    result.put("errorFlag", true);
                    result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpHad_Lev"));
                    result.put("absenceDecisionId", absAttendanceObj.getId());
                    return result;
                  }

                }
              }
              // BUSINESS MISSION OVERLAP
              else if (json.getString("decisionType").equals(Constants.BUSINESSMISSION_OVERLAP)) {
                OBQuery<EHCMBusMissionSummary> businessMissionSummQry = OBDal.getInstance()
                    .createQuery(EHCMBusMissionSummary.class,
                        " as e where e.employee.id=:employeeId   " + sql + " " + sql1);
                businessMissionSummQry.setNamedParameter("employeeId", employeeId);
                businessMissionSummQry.setNamedParameter("fromdate", startDate);
                businessMissionSummQry.setNamedParameter("todate", endDate);
                // businessMissionSummQry.setNamedParameter("currentDecisionId", currentDecisionId);
                log4j.debug("empScholarshipQry:" + businessMissionSummQry.getWhereAndOrderBy());
                businessMissionSummQry.setMaxResult(1);
                businessMissionSummList = businessMissionSummQry.list();
                if (businessMissionSummList.size() > 0) {
                  EHCMEmpBusinessMission empbusinessMission = businessMissionSummList.get(0)
                      .getEhcmEmpBusinessmission();
                  // if current decision is cutoff
                  if (empbusinessMission.getDecisionType()
                      .equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
                    result = chkCutOffDecisionOverlap(json.getString("decisionType"),
                        empbusinessMission.getId(), startDate, endDate, employeeId, sql, sql1,
                        currentDecisionId);
                    if (result != null && result.has("errorFlag")
                        && result.getBoolean("errorFlag")) {
                      return result;
                    }

                  } else {
                    log4j.debug("empScholarshipList:" + businessMissionSummList.get(0));
                    result.put("errorFlag", true);
                    result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpBusMission_CreCant"));
                    result.put("businessMissionId", empbusinessMission.getId());
                    return result;
                  }
                }
              }
              // delegation overlap
              else if (json.getString("decisionType").equals(Constants.DELEGATION_OVERLAP)) {

                OBQuery<EmploymentInfo> empDelegationQry = OBDal.getInstance().createQuery(
                    EmploymentInfo.class,
                    " as e where e.ehcmEmpPerinfo.id=:employeeId   " + delegationsql + " " + sql1);
                // OBQuery<EmployeeDelegation> empDelegationQry =
                // OBDal.getInstance().createQuery(EmployeeDelegation.class, " as e where
                // e.ehcmEmpPerinfo.id=:employeeId " + sql + " " + decisionSubTypesql + sql1);
                empDelegationQry.setNamedParameter("employeeId", employeeId);
                empDelegationQry.setNamedParameter("fromdate", startDate);
                empDelegationQry.setNamedParameter("todate", endDate);
                // empDelegationQry.setNamedParameter("currentDecisionId", currentDecisionId);
                empDelegationQry.setMaxResult(1);
                log4j.debug("empScholarshipQry:" + empDelegationQry.getWhereAndOrderBy());
                empDelegationList = empDelegationQry.list();
                if (empDelegationList.size() > 0) {
                  EmploymentInfo delegation = empDelegationList.get(0);
                  log4j.debug("empScholarshipList:" + delegation.getEhcmEmpDelegationList().size());
                  result.put("errorFlag", true);
                  result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpDelegation_CreCant"));
                  if (delegation.getEhcmEmpDelegationList().size() > 0) {
                    result.put("delegationId",
                        delegation.getEhcmEmpDelegationList().get(0).getId());
                  }

                  return result;
                }
              }
              // overtime overlap
              else if (json.getString("decisionType").equals(Constants.OVERTIME_OVERLAP)) {
                OBQuery<EhcmEmployeeOvertime> empOvertimeQry = OBDal.getInstance().createQuery(
                    EhcmEmployeeOvertime.class,
                    " as e where e.employee.id=:employeeId  and e.id <>:currentDecisionId " + sql
                        + " " + decisionSubTypesql + sql1);
                empOvertimeQry.setNamedParameter("employeeId", employeeId);
                empOvertimeQry.setNamedParameter("fromdate", startDate);
                empOvertimeQry.setNamedParameter("todate", endDate);
                empOvertimeQry.setNamedParameter("currentDecisionId", currentDecisionId);
                log4j.debug("empOvertimeQry:" + empOvertimeQry.getWhereAndOrderBy());
                empOvertimeQry.setMaxResult(1);
                empOvertimeList = empOvertimeQry.list();
                if (empOvertimeList.size() > 0) {
                  log4j.debug("empScholarshipList:" + empOvertimeList.get(0));
                  result.put("errorFlag", true);
                  result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpOvertime_CreCant"));
                  result.put("overTimeId", empOvertimeList.get(0).getId());
                  return result;
                }
              }

              // secondment overlap
              else if (json.getString("decisionType").equals(Constants.SECONDMENT_OVERLAP)) {

                OBQuery<EmploymentInfo> secondmentQry = OBDal.getInstance().createQuery(
                    EmploymentInfo.class,
                    "  as e where e.ehcmEmpPerinfo.id=:employeeId  and e.changereason in ('SEC','EXSEC','COSEC')   "
                        + sql + " " + sql1);// and e.changereason in ('SEC','EXSEC')
                secondmentQry.setNamedParameter("employeeId", employeeId);
                secondmentQry.setNamedParameter("fromdate", startDate);
                secondmentQry.setNamedParameter("todate", endDate);
                // secondmentQry.setNamedParameter("currentDecisionId", currentDecisionId);
                secondmentQry.setMaxResult(1);
                log4j.debug("secondmentQry:" + secondmentQry.getWhereAndOrderBy());
                secondmentList = secondmentQry.list();
                if (secondmentList.size() > 0) {
                  EHCMEmpSecondment empSecondment = secondmentList.get(0).getEhcmEmpSecondment();
                  log4j.debug("empSecondment:" + empSecondment);
                  if (empSecondment.getDecisionType()
                      .equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
                    result = chkCutOffDecisionOverlap(json.getString("decisionType"),
                        empSecondment.getId(), startDate, endDate, employeeId, sql, sql1,
                        currentDecisionId);
                    if (result != null && result.has("errorFlag")
                        && result.getBoolean("errorFlag")) {
                      return result;
                    }

                  } else {
                    result.put("errorFlag", true);
                    result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpSecondment_CreCant"));
                    result.put("secondmentId", empSecondment.getId());
                    return result;
                  }
                }
              }
              // suspension overlap
              else if (json.getString("decisionType").equals(Constants.SUSPENSION_OVERLAP)) {
                OBQuery<EmploymentInfo> empSuspensionQry = OBDal.getInstance().createQuery(
                    EmploymentInfo.class,
                    "  as e where e.ehcmEmpPerinfo.id=:employeeId  and e.changereason in ('SUS','SUE')   "
                        + sql + " " + sql1);
                empSuspensionQry.setNamedParameter("employeeId", employeeId);
                empSuspensionQry.setNamedParameter("fromdate", startDate);
                empSuspensionQry.setNamedParameter("todate", endDate);
                // empSuspensionQry.setNamedParameter("currentDecisionId", currentDecisionId);
                log4j.debug("empSuspensionQry:" + empSuspensionQry.getWhereAndOrderBy());
                empSuspensionList = empSuspensionQry.list();
                if (empSuspensionList.size() > 0) {
                  EmployeeSuspension suspension = empSuspensionList.get(0).getEhcmEmpSuspension();
                  if (!suspension.getSuspensionType().equals(Constants.SUSPENSION_END)) {
                    log4j.debug("empSuspensionList:" + suspension);
                    result.put("errorFlag", true);
                    result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpSuspension_CreCant"));
                    result.put("suspensionId", suspension.getId());
                    return result;
                  } else if (suspension.getSuspensionType().equals(Constants.SUSPENSION_END)) {
                    result = chkCutOffDecisionOverlap(json.getString("decisionType"),
                        suspension.getOriginalDecisionNo().getId(), startDate, endDate, employeeId,
                        sql, sql1, currentDecisionId);
                    if (result != null && result.has("errorFlag")
                        && result.getBoolean("errorFlag")) {
                      return result;
                    }
                  }
                }
              }
              // termination overlap
              else if (json.getString("decisionType").equals(Constants.TERMINATION_OVERLAP)) {
                // OBQuery<EHCMEMPTermination> empTerminationQry =
                // OBDal.getInstance().createQuery(EHCMEMPTermination.class,
                // " as e where e.employee.id=:employeeId and
                // (to_date(to_char(e.terminationDate,'dd-MM-yyyy'),'dd-MM-yyyy') between
                // to_date(:fromdate) and to_date(:todate,'dd-MM-yyyy')) " + decisionSubTypesql +
                // sql1);
                OBQuery<EmploymentInfo> empTerminationQry = OBDal.getInstance().createQuery(
                    EmploymentInfo.class,
                    "  as e where e.ehcmEmpPerinfo.id=:employeeId  and e.changereason in ('T')   "
                        + sql + " " + sql1);
                empTerminationQry.setNamedParameter("employeeId", employeeId);
                empTerminationQry.setNamedParameter("fromdate", startDate);
                empTerminationQry.setNamedParameter("todate", endDate);
                // empTerminationQry.setNamedParameter("currentDecisionId", currentDecisionId);
                log4j.debug("empTerminationQry:" + empTerminationQry.getWhereAndOrderBy());
                empTerminationList = empTerminationQry.list();
                if (empTerminationList.size() > 0) {
                  EmploymentInfo empTermination = empTerminationList.get(0);
                  log4j.debug("empTerminationList:" + empTermination);
                  result.put("errorFlag", true);
                  result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpTermination_CreCant"));
                  if (empTermination.getEhcmEmpTermination() != null)
                    result.put("terminationId", empTermination.getEhcmEmpTermination().getId());
                  return result;
                }
              }
              // Disclipline overlap
              else if (json.getString("decisionType").equals(Constants.DISCLIPLINE_OVERLAP)) {
                OBQuery<EhcmDisciplineAction> empDiscliplineQry = OBDal.getInstance().createQuery(
                    EhcmDisciplineAction.class,
                    " as e where e.employee.id=:employeeId  and e.id <>:"
                        + "currentDecisionId and e.id not in (select originalDecisionNo from ehcm_discipline_action where originalDecisionNo is "
                        + "not null ) and e.decisionType not in ('CA') and e.decisionStatus = 'I'   "
                        + sql + " " + sql1);
                empDiscliplineQry.setNamedParameter("employeeId", employeeId);
                empDiscliplineQry.setNamedParameter("fromdate", startDate);
                empDiscliplineQry.setNamedParameter("todate", endDate);
                empDiscliplineQry.setNamedParameter("currentDecisionId", currentDecisionId);
                log4j.debug("empDiscliplineQry:" + empDiscliplineQry.getWhereAndOrderBy());
                log4j.debug("decisionSubTypesql:" + decisionSubTypesql);

                empDiscliplineQry.setMaxResult(1);
                discliplineList = empDiscliplineQry.list();
                if (discliplineList.size() > 0) {
                  log4j.debug("Disclipline:" + discliplineList.get(0));
                  result.put("errorFlag", true);
                  result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpDisclipline_CreCant"));
                  result.put("discliplineId", discliplineList.get(0).getId());
                  return result;
                }
              }
            }
          }
        }
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in chkSubDecisionOverlap " + e.getMessage());
      e.printStackTrace();
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in chkSubDecisionOverlap " + e.getMessage());
      e.printStackTrace();
    }
    return result;
  }

  /**
   * check current record is cutoff record then check with cutoff original decision record and chk
   * overlap
   * 
   * @param decisionType
   * @param originalDecisionId
   * @param startDate
   * @param endDate
   * @param employeeId
   * @param sql
   * @param sql1
   * @param currentDecisionId
   * @return
   */
  public static JSONObject chkCutOffDecisionOverlap(String decisionType, String originalDecisionId,
      String startDate, String endDate, String employeeId, String sql, String sql1,
      String currentDecisionId) {
    List<EHCMEmpScholarship> empScholarshipList = new ArrayList<EHCMEmpScholarship>();
    List<EHCMEmpSecondment> empSecondmentList = new ArrayList<EHCMEmpSecondment>();
    List<EmploymentInfo> empSuspensionList = new ArrayList<EmploymentInfo>();
    List<EHCMEmpBusinessMission> empbusinessMissionList = new ArrayList<EHCMEmpBusinessMission>();
    List<EHCMAbsenceAttendance> empAbsenceAttendanceList = new ArrayList<EHCMAbsenceAttendance>();
    JSONObject result = new JSONObject();
    try {
      if (decisionType.equals(Constants.SCHOLARSHIP_OVERLAP)) {
        OBQuery<EHCMEmpScholarship> empScholarshipQry = OBDal.getInstance().createQuery(
            EHCMEmpScholarship.class,
            " as e where e.employee.id=:employeeId  and e.id=:originalDecId " + sql + " " + sql1);
        empScholarshipQry.setNamedParameter("employeeId", employeeId);
        empScholarshipQry.setNamedParameter("originalDecId", originalDecisionId);
        empScholarshipQry.setNamedParameter("fromdate", startDate);
        empScholarshipQry.setNamedParameter("todate", endDate);
        // empScholarshipQry.setNamedParameter("currentDecisionId", currentDecisionId);
        empScholarshipQry.setMaxResult(1);
        empScholarshipList = empScholarshipQry.list();
        if (empScholarshipList.size() > 0) {
          result.put("errorFlag", true);
          result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpScholarship_CreCant"));
          result.put("scholarShipId", empScholarshipList.get(0).getId());
          return result;
        }
      } else if (decisionType.equals(Constants.BUSINESSMISSION_OVERLAP)) {
        OBQuery<EHCMEmpBusinessMission> empbusinessMissionQry = OBDal.getInstance().createQuery(
            EHCMEmpBusinessMission.class,
            " as e where e.employee.id=:employeeId  and e.id=:originalDecId " + sql + " " + sql1);
        empbusinessMissionQry.setNamedParameter("employeeId", employeeId);
        empbusinessMissionQry.setNamedParameter("originalDecId", originalDecisionId);
        empbusinessMissionQry.setNamedParameter("fromdate", startDate);
        empbusinessMissionQry.setNamedParameter("todate", endDate);
        // empbusinessMissionQry.setNamedParameter("currentDecisionId", currentDecisionId);
        empbusinessMissionQry.setMaxResult(1);
        empbusinessMissionList = empbusinessMissionQry.list();
        if (empbusinessMissionList.size() > 0) {
          result.put("errorFlag", true);
          result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpBusMission_CreCant"));
          result.put("businessMissionId", empbusinessMissionList.get(0).getId());
          return result;
        }
      }

      else if (decisionType.equals(Constants.SECONDMENT_OVERLAP)) {
        OBQuery<EHCMEmpSecondment> empSecondmentQry = OBDal.getInstance().createQuery(
            EHCMEmpSecondment.class,
            " as e where e.ehcmEmpPerinfo.id=:employeeId  and e.id=:originalDecId " + sql + " "
                + sql1);
        empSecondmentQry.setNamedParameter("employeeId", employeeId);
        empSecondmentQry.setNamedParameter("originalDecId", originalDecisionId);
        empSecondmentQry.setNamedParameter("fromdate", startDate);
        empSecondmentQry.setNamedParameter("todate", endDate);
        // empSecondmentQry.setNamedParameter("currentDecisionId", currentDecisionId);
        empSecondmentQry.setMaxResult(1);
        empSecondmentList = empSecondmentQry.list();
        if (empSecondmentList.size() > 0) {
          result.put("errorFlag", true);
          result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpSecondment_CreCant"));
          result.put("secondmentId", empSecondmentList.get(0).getId());
          return result;
        }
      } else if (decisionType.equals(Constants.SUSPENSION_OVERLAP)) {
        OBQuery<EmploymentInfo> empSuspensionQry = OBDal.getInstance().createQuery(
            EmploymentInfo.class,
            "  as e where e.ehcmEmpPerinfo.id=:employeeId  and e.changereason in ('SUS')  and e.ehcmEmpSuspension.id=:originalDecId  "
                + sql + " " + sql1);
        empSuspensionQry.setNamedParameter("employeeId", employeeId);
        empSuspensionQry.setNamedParameter("originalDecId", originalDecisionId);
        empSuspensionQry.setNamedParameter("fromdate", startDate);
        empSuspensionQry.setNamedParameter("todate", endDate);
        // empSuspensionQry.setNamedParameter("currentDecisionId", currentDecisionId);
        empSuspensionQry.setMaxResult(1);
        empSuspensionList = empSuspensionQry.list();
        if (empSuspensionList.size() > 0) {
          result.put("errorFlag", true);
          result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpSuspension_CreCant"));
          result.put("suspensionId", empSuspensionList.get(0).getId());
          return result;
        }
      } else if (decisionType.equals(Constants.ABSENCE_OVERLAP)) {
        OBQuery<EHCMAbsenceAttendance> empAbsenceAttndQry = OBDal.getInstance().createQuery(
            EHCMAbsenceAttendance.class, "  as e where e.ehcmEmpPerinfo.id=:employeeId  "
                + "  and e.id=:originalDecId  " + sql + " " + sql1);
        empAbsenceAttndQry.setNamedParameter("employeeId", employeeId);
        empAbsenceAttndQry.setNamedParameter("originalDecId", originalDecisionId);
        empAbsenceAttndQry.setNamedParameter("fromdate", startDate);
        empAbsenceAttndQry.setNamedParameter("todate", endDate);
        empAbsenceAttndQry.setMaxResult(1);
        empAbsenceAttendanceList = empAbsenceAttndQry.list();
        if (empAbsenceAttendanceList.size() > 0) {
          result.put("errorFlag", true);
          result.put("errormsg", OBMessageUtils.messageBD("EHCM_EmpHad_Lev"));
          result.put("absenceDecisionId", empAbsenceAttendanceList.get(0).getId());
          return result;
        }
      }

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in chkCutOffDecisionOverlap " + e.getMessage());
      e.printStackTrace();
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in chkCutOffDecisionOverlap " + e.getMessage());
      e.printStackTrace();
    }
    return result;
  }

  /**
   * comparator to move the current decision type as last position in json object
   * 
   * @param jsonArray
   * @param decisionType
   * @return
   */
  public static List<JSONObject> sortDecsionType(JSONArray jsonArray, String decisionType) {
    List<JSONObject> jsonValues = new ArrayList<JSONObject>();
    try {

      for (int i = 0; i < jsonArray.length(); i++) {
        jsonValues.add(jsonArray.getJSONObject(i));
      }

      Collections.sort(jsonValues, new Comparator<JSONObject>() {
        @Override
        public int compare(JSONObject o1, JSONObject o2) {
          try {
            if (o1.getString("decisionType").equals(decisionType)
                && !o2.getString("decisionType").equals(decisionType)) {
              return 1;
            } else if (!o1.getString("decisionType").equals(decisionType)
                && o2.getString("decisionType").equals(decisionType)) {
              return -1;
            }
            return 0;
          } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          return 1;
        }

      });
      return jsonValues;
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in chkSubDecisionOverlap " + e.getMessage());
      e.printStackTrace();
    }
    return jsonValues;
  }

  /**
   * 
   * @param hijriDate
   *          format dd-MM-yyyy
   * @return gregorian date yyyy-MM-dd
   */
  public static String convertToGregorian_tochar(String hijriDate) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String gregDate = "";
    try {
      st = OBDal.getInstance().getConnection()
          .prepareStatement("select to_char(eut_convertto_gregorian('" + hijriDate
              + "'),'yyyy-MM-dd') as eut_convertto_gregorian");
      log4j.debug(st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        gregDate = rs.getString("eut_convertto_gregorian");
      }
    }

    catch (final Exception e) {
      log4j.error("Exception in convertToGregorian_tochar() Method : ", e);
      return "0";
    }
    return gregDate;
  }

  /**
   * 
   * @param startDate
   * @param endDate
   * @param ClientId
   * @return
   */

  public static JSONObject CalculateAge(Date startDate, Date endDate, String ClientId) {

    int years = 0;
    int months = 0;
    int days = 0;
    String strNowmonth = "";
    JSONObject result = new JSONObject();
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {

      // converts birthDate To hijriDate
      String strStartDate = convertTohijriDate(dateYearFormat.format(startDate));
      // create calendar object for current day
      String strEndDate = convertTohijriDate(dateYearFormat.format(endDate));

      int dobyear = Integer.parseInt(strStartDate.split("-")[2]);
      int dobmonth = Integer.parseInt(strStartDate.split("-")[1]);
      int dobdate = Integer.parseInt(strStartDate.split("-")[0]);
      log4j.debug("dobyear>>dobmonth>>dobdate" + dobyear + "-" + dobmonth + "-" + dobdate);
      int nowyear = Integer.parseInt(strEndDate.split("-")[2]);
      int nowmonth = Integer.parseInt(strEndDate.split("-")[1]);
      int nowdate = Integer.parseInt(strEndDate.split("-")[0]);

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
        int maxCurrentDate = getDays(ClientId, nowyear + strNowmonth);
        days = maxCurrentDate - dobdate + today;

      } else {
        days = 0;
        if (months == 12) {
          years++;
          months = 0;
        }

      }

    } catch (Exception e) {
      log4j.error("Exception in CalculateAge", e);
      e.printStackTrace();
    }
    try {
      result.put("Years", String.valueOf(years));
      result.put("Months", String.valueOf(months));
      result.put("Days", String.valueOf(days));
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return result;

    // return String.valueOf(years) + " Years" + String.valueOf(months) + " Months";
  }

  /**
   * 
   * @param clientId
   * @param monthyear
   * @return
   */

  public static int getDays(String clientId, String monthyear) {
    PreparedStatement st = null;
    ResultSet rs = null;
    int total = 0;
    try {
      st = OBDal.getInstance().getConnection().prepareStatement(
          " select count(*) as total from eut_hijri_dates  where hijri_date ilike '%" + monthyear
              + "%' group by  ad_org_id limit 1 ");
      // st.setString(1, clientId);
      log4j.debug("getDays:" + st.toString());
      rs = st.executeQuery();
      if (rs.next()) {
        if (rs.getInt("total") > 0) {
          total = rs.getInt("total");
        }
      }
    } catch (final SQLException e) {
      log4j.error("Exception in getDays", e);
      return 0;
    } catch (final Exception e) {
      log4j.error("Exception in getDays", e);
      return 0;
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in checkEmpAlreadyExists", e);
      }
    }
    return total;
  }

  /**
   * 
   * @param gregDate
   * @return
   */

  public static String convertTohijriDate(String gregDate) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String hijriDate = "";
    try {
      st = OBDal.getInstance().getConnection()
          .prepareStatement("select eut_convert_to_hijri(to_char(to_timestamp('" + gregDate
              + "','YYYY-MM-DD HH24:MI:SS'),'YYYY-MM-DD  HH24:MI:SS'))");
      rs = st.executeQuery();
      if (rs.next()) {
        hijriDate = rs.getString("eut_convert_to_hijri");
      }
    }

    catch (final Exception e) {
      log4j.error("Exception in convertTohijriDate() Method : ", e);
      return "0";
    }
    return hijriDate;
  }

  /**
   * before one day in hijiri date format(dd-MM-yyyy)
   * 
   * @param gregoriandate
   * @param clientId
   * @return gregorian date(dd-MM-yyyy)
   */
  public static String getBeforeDateInGreg(String hijiriDate) {
    List<EUT_HijriDates> hijiriDateList = new ArrayList<EUT_HijriDates>();
    String beforeOneDay = null;
    try {

      OBQuery<EUT_HijriDates> hijiriDateQry = OBDal.getInstance().createQuery(EUT_HijriDates.class,
          " as e where e.hijriDate < :hijiridate" + " order by e.hijriDate desc ");
      hijiriDateQry.setNamedParameter("hijiridate",
          hijiriDate.split("-")[2] + hijiriDate.split("-")[1] + hijiriDate.split("-")[0]);
      hijiriDateQry.setMaxResult(1);
      hijiriDateList = hijiriDateQry.list();
      if (hijiriDateList.size() > 0) {
        beforeOneDay = dateFormat.format(hijiriDateList.get(0).getGregorianDate());
      }
    } catch (Exception e) {
      log4j.error("Exception in getBeforeDateInGreg", e);
      e.printStackTrace();
    }
    return beforeOneDay;
  }

  /**
   * before one day in gregDate date format(dd-MM-yyyy)
   * 
   * @param gregoriandate
   * @param clientId
   * @return gregorian date(dd-MM-yyyy)
   */
  public static String getBeforeDateInGregUsingGregDate(String gregDate) {
    List<EUT_HijriDates> hijiriDateList = new ArrayList<EUT_HijriDates>();
    String beforeOneDay = null;
    try {

      OBQuery<EUT_HijriDates> hijiriDateQry = OBDal.getInstance().createQuery(EUT_HijriDates.class,
          " as e where e.gregorianDate < to_date(:gregDate,'yyyy-MM-dd')"
              + " order by e.gregorianDate desc ");
      hijiriDateQry.setNamedParameter("gregDate", gregDate);
      hijiriDateQry.setMaxResult(1);
      hijiriDateList = hijiriDateQry.list();
      if (hijiriDateList.size() > 0) {
        beforeOneDay = dateFormat.format(hijiriDateList.get(0).getGregorianDate());
      }
    } catch (Exception e) {
      log4j.error("Exception in getBeforeDateInGreg", e);
      e.printStackTrace();
    }
    return beforeOneDay;
  }

  public static String getAfterDateInGreg(String hijiriDate) {
    List<EUT_HijriDates> hijiriDateList = new ArrayList<EUT_HijriDates>();
    String afterOneDay = null;
    try {

      OBQuery<EUT_HijriDates> hijiriDateQry = OBDal.getInstance().createQuery(EUT_HijriDates.class,
          " as e where e.hijriDate > :hijiridate" + " order by e.hijriDate asc ");
      hijiriDateQry.setNamedParameter("hijiridate",
          hijiriDate.split("-")[2] + hijiriDate.split("-")[1] + hijiriDate.split("-")[0]);
      hijiriDateQry.setMaxResult(1);
      hijiriDateList = hijiriDateQry.list();
      if (hijiriDateList.size() > 0) {
        afterOneDay = dateFormat.format(hijiriDateList.get(0).getGregorianDate());
      }
    } catch (Exception e) {
      log4j.error("Exception in getAfterDateInGreg", e);
      e.printStackTrace();
    }
    return afterOneDay;
  }

  public static JSONObject getMinMaxStartDateUsingDate(Date gregorianDate) {
    String hijiriDate = null, year = null;
    JSONObject result = new JSONObject();
    try {
      log4j.debug("gregor:" + yeareFormat.format(gregorianDate));
      hijiriDate = UtilityDAO.convertTohijriDate(yeareFormat.format(gregorianDate));
      year = hijiriDate.split("-")[2];
      result = getMinAndMaxDateInYear(year);
      return result;

    } catch (Exception e) {
      log4j.error("Exception in getBeforeDateInGreg", e);
      e.printStackTrace();
    }
    return result;
  }

  public static JSONObject getMinAndMaxDateInYear(String year) {
    String sql = null;
    JSONObject result = new JSONObject();
    String minhijiriDate = null;
    String maxhijiriDate = null;
    try {
      sql = " select min(hijri_date) as minhijiri , max(hijri_date) as maxhijiri,to_char(min(gregorian_date),'yyyy-MM-dd') as mingreg ,to_char(max(gregorian_date),'yyyy-MM-dd') as maxgreg from eut_hijri_dates where hijri_date ilike ? ";
      SQLQuery query = OBDal.getInstance().getSession().createSQLQuery(sql);
      query.setParameter(0, "%" + year + "%");
      log4j.debug("where1 :" + query.toString());
      List datelist = query.list();
      if (datelist != null && datelist.size() > 0) {
        Object[] row = (Object[]) datelist.get(0);
        minhijiriDate = row[0].toString();
        maxhijiriDate = row[1].toString();

        result.put("minhijiridate", minhijiriDate);
        result.put("maxhijiridate", maxhijiriDate);
        result.put("mingregdate", row[2]);
        result.put("maxgregdate", row[3]);
      }
    } catch (final Exception e) {
      log4j.error("Exception in getMinAndMaxDateInYear() :", e);
      return result;
    }
    return result;
  }

  public static JSONObject getMinstartDateandEnddateInMonth(String startDate) {
    JSONObject result = null;
    String yearmonth = null;
    String sql = null;
    try {
      yearmonth = startDate.split("-")[1] + startDate.split("-")[2];
      sql = " select min(hijri_date) as minhijiri , max(hijri_date) as maxhijiri,"
          + " to_char(min(gregorian_date),'yyyy-MM-dd') as mingreg ,to_char(max(gregorian_date),'yyyy-MM-dd') as maxgreg "
          + " from eut_hijri_dates where hijri_date ilike ? ";
      SQLQuery query = OBDal.getInstance().getSession().createSQLQuery(sql);
      query.setParameter(0, "%" + yearmonth + "%");
      log4j.debug("where1 :" + query.toString());
      List datelist = query.list();
      if (datelist != null && datelist.size() > 0) {
        Object[] row = (Object[]) datelist.get(0);

        result.put("minhijiridate", row[0].toString());
        result.put("maxhijiridate", row[1].toString());
        result.put("mingregdate", row[2]);
        result.put("maxgregdate", row[3]);
      }

    } catch (final Exception e) {
      log4j.error("Exception in getMinstartDateandEnddateInMonth", e);
    }
    return result;
  }

  public static int calculateTotalMonth(String startHijiriDate, String endHijiriDate) {
    int totalMonth = 0;
    String sql = null;
    try {
      sql = " select count(totaldays) from    (select substring(hijri_date,1,4)|| substring(hijri_date,5,2)     from eut_hijri_dates "
          + "    where  hijri_date  >=?    and hijri_date < ? "
          + "    group by  substring(hijri_date,1,4)|| substring(hijri_date,5,2)) totaldays ";

      SQLQuery query = OBDal.getInstance().getSession().createSQLQuery(sql);
      query.setParameter(0, startHijiriDate.split("-")[2] + startHijiriDate.split("-")[1]
          + startHijiriDate.split("-")[0]);
      query.setParameter(1,
          endHijiriDate.split("-")[2] + endHijiriDate.split("-")[1] + endHijiriDate.split("-")[0]);
      log4j.debug("where1 :" + query.toString());
      List datelist = query.list();
      if (datelist != null && datelist.size() > 0) {
        Object row = (Object) datelist.get(0);
        totalMonth = Integer.valueOf(row.toString());
        return totalMonth;

      }
    } catch (final Exception e) {
      log4j.error("Exception in calculateTotalMonth", e);
      return 0;
    }
    return totalMonth;
  }

  /**
   * Provides the work image as a byte array for the indicated parameters.
   * 
   * @param empId
   *          The organization id used to get the prof pic
   * @return The image requested
   */
  public static byte[] getEmployeeImage(String empId) {
    byte[] imageByte;
    try {
      Image img = Utility.getObject(EhcmEmpPerInfo.class, empId).getWorkAdImage() == null
          ? Utility.getObject(EhcmEmpPerInfo.class, empId).getCIVAdImage()
          : Utility.getObject(EhcmEmpPerInfo.class, empId).getWorkAdImage();
      OBContext.setAdminMode(true);
      try {
        imageByte = img.getBindaryData();
      } finally {
        OBContext.restorePreviousMode();
      }
    } catch (Exception e) {
      log4j.error("Could not load work prof pic from database: " + e);
      imageByte = getBlankImage();
    }
    return imageByte;
  }

  private static byte[] getBlankImage() {
    try {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      String defaultImagePath = "web/images/blank.gif";
      new FileUtility(OBConfigFileProvider.getInstance().getServletContext().getRealPath("/"),
          defaultImagePath, false, true).dumpFile(bout);
      bout.close();
      return bout.toByteArray();
    } catch (IOException ex) {
      log4j.error("Could not load blank image.");
      return new byte[0];
    }
  }

  /**
   * 
   * @param object
   * @return converted json string
   */
  public String convertObjectTojsonString(Object object) {
    // TODO Auto-generated method stub
    String jsonInString = "";
    ObjectMapper mapper = new ObjectMapper();
    try {
      jsonInString = mapper.writeValueAsString(object);
    } catch (JsonGenerationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JsonMappingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return jsonInString;
  }

  /**
   * get the details by using country code
   * 
   * @param countryCode
   * @return
   */
  public static Country getCountryOB(String countryCode) {
    // TODO Auto-generated method stub
    Country countryOB = null;
    try {
      OBContext.setAdminMode();
      OBQuery<Country> countryQry = OBDal.getInstance().createQuery(Country.class,
          "as e where e.name=:name");
      countryQry.setNamedParameter("name", countryCode);
      countryQry.setFilterOnReadableClients(false);
      countryQry.setFilterOnReadableOrganization(false);
      if (countryQry.list().size() > 0) {
        countryOB = countryQry.list().get(0);
      }

    } catch (Exception e) {
      log4j.error("Exception in while getting country", e);
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }
    return countryOB;
  }

  /**
   * 
   * @param cityCode
   * @return
   */
  public static City getCityOB(String cityCode) {
    // TODO Auto-generated method stub
    City cityOB = null;
    try {
      OBContext.setAdminMode();
      OBQuery<City> cityQry = OBDal.getInstance().createQuery(City.class,
          "as e where e.name=:name");
      cityQry.setNamedParameter("name", cityCode);
      cityQry.setFilterOnReadableClients(false);
      cityQry.setFilterOnReadableOrganization(false);
      if (cityQry.list().size() > 0) {
        cityOB = cityQry.list().get(0);
      }

    } catch (Exception e) {
      log4j.error("Exception in while getting city", e);
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }
    return cityOB;
  }

  /**
   * Get Top LevelParent Department Id By Using DepartmentId
   * 
   * @param employeeDepartmentId
   * @return
   */
  public static String getTopLevelParentDepartmentId(String employeeDepartmentId) {
    // TODO Auto-generated method stub
    // find parent org
    Organization deptOB = OBDal.getInstance().get(Organization.class, employeeDepartmentId);
    if (deptOB.getEhcmParentOrg() != null) {
      return getTopLevelParentDepartmentId(deptOB.getEhcmParentOrg().getId());
    } else {
      return employeeDepartmentId;
    }
  }

  public static int getbalanceDaysInYear(String EmployeeId, String DecisionType) {
    int totalYearDays = 0;
    List<DecisionBalance> decbalance = new ArrayList<DecisionBalance>();
    try {
      OBQuery<DecisionBalance> decisionbalance = OBDal.getInstance().createQuery(
          DecisionBalance.class,
          " as e where e.employee.id=:employeeId and e.decisionType =:decisonType and ehcmDeciBalHdr.alertStatus = 'C' ");
      decisionbalance.setNamedParameter("employeeId", EmployeeId);
      decisionbalance.setNamedParameter("decisonType", DecisionType);

      decbalance = decisionbalance.list();
      if (decbalance.size() > 0) {
        DecisionBalance secondmentbalance = decbalance.get(0);
        if (secondmentbalance.getBalance() > 0 && !secondmentbalance.getBalance().equals(null)) {
          totalYearDays = (int) ((secondmentbalance.getBalance()) * Constants.NoOfDaysInYear);
          return totalYearDays;
        }
      }
    } catch (final Exception e) {
      log4j.error("Exception in getbalanceDaysInYear", e);

    }
    return totalYearDays;

  }

  public static DecisionBalance getInitialBaanceObjforEmployee(String EmployeeId,
      String DecisionType) {
    List<DecisionBalance> decbalance = new ArrayList<DecisionBalance>();
    DecisionBalance initialBalanceObj = null;
    try {
      OBQuery<DecisionBalance> decisionbalance = OBDal.getInstance().createQuery(
          DecisionBalance.class,
          " as e where e.employee.id=:employeeId and e.decisionType =:decisonType and ehcmDeciBalHdr.alertStatus = 'CO' ");
      decisionbalance.setNamedParameter("employeeId", EmployeeId);
      decisionbalance.setNamedParameter("decisonType", DecisionType);

      decbalance = decisionbalance.list();
      if (decbalance.size() > 0) {
        initialBalanceObj = decbalance.get(0);

      }
    } catch (final Exception e) {
      log4j.error("Exception in getInitialBaanceObjforEmployee", e);

    }
    return initialBalanceObj;

  }
}
