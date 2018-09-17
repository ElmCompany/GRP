package sa.elm.ob.hcm.process.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;

import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;
import sa.elm.ob.hcm.EHCMEmpLeave;
import sa.elm.ob.hcm.EHCMEmpLeaveln;
import sa.elm.ob.hcm.EHCMLevCarryForward;
import sa.elm.ob.hcm.EHCMLevCarryForwardLn;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ehcmgradeclass;
import sa.elm.ob.hcm.ad_forms.employee.vo.EmployeeVO;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.util.UtilityDAO;

public class LeaveCalculationDAOImpl implements LeaveCalculationDAO {

  private static final Logger log4j = Logger.getLogger(LeaveCalculationDAOImpl.class);

  static DateFormat dateFormat = sa.elm.ob.utility.util.Utility.dateFormat;
  static DateFormat yearFormat = sa.elm.ob.utility.util.Utility.YearFormat;

  /**
   * 
   * @param ClientId
   * @return
   */
  public List<EhcmEmpPerInfo> getEmployeeList(String ClientId, String gradeClassId,
      String currentDate, String gradeList) {
    List<EhcmEmpPerInfo> empList = new ArrayList<EhcmEmpPerInfo>();
    String hql = "";
    try {
      if (gradeClassId != null)
        hql = " and e.gradeClass.id=:gradeId ";

      if (gradeList != null)
        hql = " and e.gradeClass.id not in (:gradeId ) ";

      OBQuery<EhcmEmpPerInfo> empQry = OBDal.getInstance().createQuery(EhcmEmpPerInfo.class,
          " as e  where  e.status ='I' and e.client.id=:clientId  and e.enabled='Y' "
              + "  and e.hiredate <= to_date(:currentDate,'yyyy-MM-dd') " + hql
              + "  order by  e.creationDate asc ");
      empQry.setNamedParameter("clientId", ClientId);
      if (gradeClassId != null)
        empQry.setNamedParameter("gradeId", gradeClassId);
      if (gradeList != null)
        empQry.setNamedParameter("gradeId", gradeList);

      empQry.setNamedParameter("currentDate", currentDate);
      empList = empQry.list();
      return empList;

    } catch (Exception e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getEmployeeList " + e.getMessage());
      e.printStackTrace();
    }
    return empList;
  }

  public List<EHCMAbsenceTypeAccruals> getAccrualList(String ClientId, String startDate,
      String endDate, String accrualResetValue) {
    List<EHCMAbsenceTypeAccruals> absenceTypeAccrualList = new ArrayList<EHCMAbsenceTypeAccruals>();
    String hql = null;
    try {

      log4j.debug("dateFormat:" + yearFormat.format(dateFormat.parse(endDate)));
      hql = "  and to_date(:endDate,'yyyy-MM-dd') between  to_date(to_char(e.startDate ,'dd-MM-yyyy'),'dd-MM-yyyy') "
          + "  and  to_date(to_char(coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') ";

      OBQuery<EHCMAbsenceTypeAccruals> accrualsQry = OBDal.getInstance().createQuery(
          EHCMAbsenceTypeAccruals.class,
          " as e  where  e.client.id=:clientId  and e.absenceType.id in ( select absence.id from EHCM_Absence_Type absence where "
              + " absence.isAccrual='Y' and absence.iscarryforward='Y' ) " + hql
              + "   order by e.gradeClassifications asc  ");
      accrualsQry.setNamedParameter("clientId", ClientId);
      accrualsQry.setNamedParameter("endDate", yearFormat.format(dateFormat.parse(endDate)));
      log4j.debug("accrual:" + accrualsQry.getWhereAndOrderBy());
      absenceTypeAccrualList = accrualsQry.list();

      if (absenceTypeAccrualList.size() > 0) {
        return absenceTypeAccrualList;
      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getAccrualList " + e.getMessage());
      e.printStackTrace();
    }
    return absenceTypeAccrualList;
  }

  /**
   * 
   * @param ClientId
   * @param startDate
   * @param endDate
   * @param accrualResetValue
   * @return
   */
  @SuppressWarnings("null")
  public static void insertCarryForward(String ClientId, String startDate, String endDate,
      String accrualResetValue) {
    List<EHCMAbsenceTypeAccruals> absenceTypeAccrualList = new ArrayList<EHCMAbsenceTypeAccruals>();
    String hql = null;
    JSONObject result = new JSONObject(), json = null;
    JSONArray array = new JSONArray();
    int accrualdays = 0, accrualmonth = 0;
    String gradeId = null;
    Date accrualEndDate = null;
    try {
      hql = " and ((to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
          + " and to_date(to_char(coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + " or (to_date(to_char( coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
          + " and to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')))   ";

      OBQuery<EHCMAbsenceTypeAccruals> accrualsQry = OBDal.getInstance().createQuery(
          EHCMAbsenceTypeAccruals.class,
          " as e  where  e.client.id=:clientId  and e.absenceType.id in ( select absence.id from EHCM_Absence_Type absence where "
              + " absence.isAccrual='Y' ) " + hql
              + "  and e.absenceType.id='EE429C1BEAF145C78152E72C131E2E13'  order by e.gradeClassifications asc  ");
      accrualsQry.setNamedParameter("clientId", ClientId);
      // accrualsQry.setNamedParameter("accrualResetVal", accrualResetValue);
      accrualsQry.setNamedParameter("fromdate", startDate);
      accrualsQry.setNamedParameter("todate", endDate);
      log4j.debug("accrual:" + accrualsQry.getWhereAndOrderBy());
      absenceTypeAccrualList = accrualsQry.list();

      if (absenceTypeAccrualList.size() > 0) {
        for (EHCMAbsenceTypeAccruals accruals : absenceTypeAccrualList) {

          if (accruals.getAbsenceType().getAccrualResetDate().equals("BHY")
              || accruals.getAbsenceType().getAccrualResetDate().equals("BGY")) {

            if (accruals.getEndDate() != null) {
              accrualEndDate = accruals.getEndDate();
            } else {
              accrualEndDate = dateFormat.parse(endDate);
            }
            accrualmonth = UtilityDAO.calculateTotalMonth(
                sa.elm.ob.utility.util.UtilityDAO
                    .convertTohijriDate(yearFormat.format(accruals.getStartDate())), // yearFormat.format(dateFormat.parse(startDate))
                sa.elm.ob.utility.util.UtilityDAO
                    .convertTohijriDate(yearFormat.format(accrualEndDate)));
            if (accruals.getGradeClassifications() != null) {
              gradeId = accruals.getGradeClassifications().getId();
              if (json != null && json.getString("gradeId").equals(gradeId)) {
                json.put("month", Integer.parseInt(json.getString("month")) + accrualmonth);
              } else {
                json = new JSONObject();
                json.put("gradeId", gradeId);
                json.put("month", accrualmonth);
                json.put("startDate",
                    UtilityDAO.getAfterDateInGreg(sa.elm.ob.utility.util.UtilityDAO
                        .convertTohijriDate(yearFormat.format(accrualEndDate))));
                json.put("endDate", endDate);
                json.put("absenceTypeId", accruals.getAbsenceType().getId());
                json.put("creditOn", accruals.getCreditOn());
                array.put(json);
              }
            }

            if (accruals.getCreditOn().equals("M")) {
              calculateAccrualMonths(accruals.getStartDate(),
                  accruals.getEndDate() != null ? accruals.getEndDate() : dateFormat.parse(endDate),
                  new BigDecimal(accrualmonth), accruals, accruals.getGradeClassifications());
            } else if (accruals.getCreditOn().equals("D")) {
              calculateAccrualMonths(accruals.getStartDate(),
                  accruals.getEndDate() != null ? accruals.getEndDate() : dateFormat.parse(endDate),
                  accruals.getDays(), accruals, accruals.getGradeClassifications());
            }
          }
        }
        if (array.length() > 0)
          result.put("gradeList", array);
      }

      if (result != null) {
        JSONArray gradeArray = result.getJSONArray("gradeList");
        if (gradeArray.length() > 0) {
          for (int i = 0; i < gradeArray.length(); i++) {
            json = gradeArray.getJSONObject(i);
            if (json.getString("gradeId") != null
                && Integer.parseInt(json.getString("month")) < 12) {

              OBQuery<EHCMAbsenceTypeAccruals> accrualsblankQry = OBDal.getInstance().createQuery(
                  EHCMAbsenceTypeAccruals.class,
                  " as e  where  e.client.id=:clientId  and e.absenceType.id=:absenceTypeId   and e.gradeClassifications.id is null "
                      + hql + "   order by e.gradeClassifications asc  ");
              accrualsblankQry.setNamedParameter("clientId", ClientId);
              accrualsblankQry.setNamedParameter("absenceTypeId", json.getString("absenceTypeId"));
              accrualsblankQry.setNamedParameter("fromdate", json.getString("startDate"));
              accrualsblankQry.setNamedParameter("todate", json.getString("endDate"));
              log4j.debug("accrual:" + accrualsQry.getWhereAndOrderBy());
              accrualsblankQry.setMaxResult(1);
              absenceTypeAccrualList = accrualsblankQry.list();

              if (absenceTypeAccrualList.size() > 0) {
                accrualmonth = UtilityDAO.calculateTotalMonth(
                    sa.elm.ob.utility.util.UtilityDAO.convertTohijriDate(
                        yearFormat.format(dateFormat.parse(json.getString("startDate")))),
                    sa.elm.ob.utility.util.UtilityDAO.convertTohijriDate(
                        yearFormat.format(dateFormat.parse(json.getString("endDate")))));

                if (json.getString("creditOn").equals("M")) {
                  calculateAccrualMonths(dateFormat.parse(json.getString("startDate")),
                      dateFormat.parse(json.getString("endDate")), new BigDecimal(accrualmonth),
                      absenceTypeAccrualList.get(0),
                      OBDal.getInstance().get(ehcmgradeclass.class, json.getString("gradeId")));
                } else if (json.getString("creditOn").equals("D")) {
                  calculateAccrualMonths(dateFormat.parse(json.getString("startDate")),
                      dateFormat.parse(json.getString("endDate")),
                      absenceTypeAccrualList.get(0).getDays(), absenceTypeAccrualList.get(0),
                      OBDal.getInstance().get(ehcmgradeclass.class, json.getString("gradeId")));
                }
              }
            }
          }
        }
      }

    } catch (

    Exception e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getAccrualListOfEndingYear " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static boolean calculateAccrualMonths(Date startDate, Date endDate, BigDecimal days,
      EHCMAbsenceTypeAccruals accruals, ehcmgradeclass gradeclass) {
    String sql = null;
    Query qry = null;
    String hql = null;
    try {

      EHCMLevCarryForward levCarryForward = insertLeaveCarryforward(startDate, endDate, days,
          accruals, gradeclass);

      int totaldays = sa.elm.ob.hcm.util.Utility.caltheDaysUsingGreDate(startDate, endDate);
      sql = " select round(((cast (?  as decimal) / cast (?  as decimal) ) * "
          + (accruals.getCreditOn().equals("D")
              ? " count(substring(a.hijri_date,1,4)||substring(a.hijri_date,5,2)) ),15) "
              : " 1 ")
          + "as monthaccrualday ,"
          + " count(substring(a.hijri_date,1,4)||substring(a.hijri_date,5,2)),"
          + " substring(a.hijri_date,1,4)||substring(a.hijri_date,5,2) ,min(gregorian_date) as min,max(gregorian_date) as max from "
          + "(select max(gregorian_date) as gregorian_date  ,hijri_date from eut_hijri_dates"
          + " where gregorian_date >= ? " + "and gregorian_date <= ? "
          + "group by hijri_date order by hijri_date desc) a "
          + "group by substring(a.hijri_date,1,4)||substring(a.hijri_date,5,2) order by  min";
      qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      if (accruals.getCreditOn().equals("D"))
        qry.setParameter(0, days);
      qry.setParameter(1, totaldays);
      qry.setParameter(2, startDate);
      qry.setParameter(3, endDate);
      List datelist = qry.list();
      if (datelist != null && datelist.size() > 0) {
        for (Object o : datelist) {
          Object[] row = (Object[]) o;
          startDate = (Date) row[3];
          endDate = (Date) row[4];
          insertLeaveCarryforwardLine(startDate, endDate, new BigDecimal(row[0].toString()),
              accruals, levCarryForward);
        }
      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in chkCurrentDateAsHijiriEnddate " + e.getMessage());
      e.printStackTrace();
    }
    return false;
  }

  public static EHCMLevCarryForward insertLeaveCarryforward(Date startDate, Date endDate,
      BigDecimal days, EHCMAbsenceTypeAccruals accruals, ehcmgradeclass gradeclass) {
    EHCMLevCarryForward leaveCarryforward = null;
    try {
      leaveCarryforward = OBProvider.getInstance().get(EHCMLevCarryForward.class);
      leaveCarryforward.setClient(accruals.getClient());
      leaveCarryforward.setOrganization(accruals.getOrganization());
      leaveCarryforward.setCreationDate(new java.util.Date());
      leaveCarryforward.setCreatedBy(accruals.getCreatedBy());
      leaveCarryforward.setUpdated(new java.util.Date());
      leaveCarryforward.setUpdatedBy(accruals.getCreatedBy());
      leaveCarryforward.setAbsenceType(accruals.getAbsenceType());
      leaveCarryforward.setGradeClassifications(gradeclass != null ? gradeclass : null);
      leaveCarryforward.setEnabled(true);
      leaveCarryforward.setGranteddays(accruals.getDays());
      leaveCarryforward.setStartDate(startDate);
      leaveCarryforward.setEndDate(endDate);
      OBDal.getInstance().save(leaveCarryforward);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in insertLeaveCarryforwar " + e.getMessage());
      e.printStackTrace();
    }
    return leaveCarryforward;
  }

  public static EHCMLevCarryForwardLn insertLeaveCarryforwardLine(Date startDate, Date endDate,
      BigDecimal days, EHCMAbsenceTypeAccruals accruals, EHCMLevCarryForward header) {
    EHCMLevCarryForwardLn leaveCarryforwardLn = null;
    try {

      leaveCarryforwardLn = OBProvider.getInstance().get(EHCMLevCarryForwardLn.class);
      leaveCarryforwardLn.setClient(accruals.getClient());
      leaveCarryforwardLn.setOrganization(accruals.getOrganization());
      leaveCarryforwardLn.setCreationDate(new java.util.Date());
      leaveCarryforwardLn.setCreatedBy(accruals.getCreatedBy());
      leaveCarryforwardLn.setUpdated(new java.util.Date());
      leaveCarryforwardLn.setUpdatedBy(accruals.getCreatedBy());
      leaveCarryforwardLn.setEhcmLevcarryforward(header);
      leaveCarryforwardLn.setEnabled(true);
      leaveCarryforwardLn.setGranteddays(days);
      leaveCarryforwardLn.setStartDate(startDate);
      leaveCarryforwardLn.setEndDate(endDate);
      OBDal.getInstance().save(leaveCarryforwardLn);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in insertLeaveCarryforwar " + e.getMessage());
      e.printStackTrace();
    }
    return leaveCarryforwardLn;
  }

  public boolean chkCurrentDateAsHijiriEnddate(String currentDate, String nxtYearHijiriGerDate) {
    String sql = null;
    Query qry = null;
    String endDate = null;
    try {

      sql = " select to_char(max(gregorian_date),'yyyy-mm-dd') as endofdate from eut_hijri_dates  "
          + " where to_date(to_char(gregorian_date,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:currentDate,'yyyy-MM-dd')"
          + " and  to_date(to_char(gregorian_date,'dd-MM-yyyy'),'dd-MM-yyyy') < to_date(:nextYearHijirGregDate,'yyyy-MM-dd')  ";
      qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      qry.setParameter("currentDate", currentDate);
      qry.setParameter("nextYearHijirGregDate", nxtYearHijiriGerDate);
      if (qry != null && qry.list().size() > 0) {
        List<Object> object = qry.list();
        Object row = object.get(0);
        endDate = (String) row;
        log4j.debug("endDate:" + endDate);
        if (endDate.equals(currentDate)) {
          return true;
        } else
          return false;
      } else
        return false;

    } catch (Exception e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in chkCurrentDateAsHijiriEnddate " + e.getMessage());
      e.printStackTrace();
    }
    return false;
  }

  public static List getLeaveCarryForwardList(String startDate, String endDate, String clientId) {
    List carryforwardList = null;
    String sql = null;
    Query qry = null;
    try {

      sql = " select sum(granteddays),ehcm_gradeclass_id,ehcm_absence_type_id  from ehcm_levcarryforward where  ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
          + " and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + " or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
          + " and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) "
          + " group by ehcm_gradeclass_id,ehcm_absence_type_id  ORDER BY ehcm_gradeclass_id ";

      qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      qry.setParameter("fromdate", startDate);
      qry.setParameter("todate", endDate);
      carryforwardList = qry.list();
      return carryforwardList;

    } catch (Exception e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getEmpLevae " + e.getMessage());
      e.printStackTrace();
    }
    return carryforwardList;
  }

  public EHCMEmpLeave getEmpLeave(EHCMAbsenceType absencetype, EhcmEmpPerInfo employee,
      String startDate) {
    List<EHCMEmpLeave> empLeaveList = new ArrayList<EHCMEmpLeave>();
    EHCMEmpLeave leave = null;
    String hql = "";
    try {

      // get current year leave id
      OBQuery<EHCMEmpLeave> empLeaveQry = OBDal.getInstance().createQuery(EHCMEmpLeave.class,
          " as e where e.absenceType.id=:absenceType and e.ehcmEmpPerinfo.id=:employeeId "
              + "   and to_date(:startDate,'dd-MM-yyyy') between  to_date(to_char(e.startDate ,'dd-MM-yyyy'),'dd-MM-yyyy') "
              + "          and to_date(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy')  " + hql);
      empLeaveQry.setNamedParameter("absenceType", absencetype.getId());
      empLeaveQry.setNamedParameter("employeeId", employee.getId());
      empLeaveQry.setNamedParameter("startDate", startDate);
      empLeaveQry.setMaxResult(1);
      log4j.debug("empleaveQry:" + empLeaveQry.getWhereAndOrderBy());
      empLeaveList = empLeaveQry.list();
      log4j.debug("empLeaveList:" + empLeaveList.size());
      if (empLeaveList.size() > 0) {
        leave = empLeaveList.get(0);
      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getEmpLevae " + e.getMessage());
      e.printStackTrace();
    }
    return leave;
  }

  public EHCMEmpLeave insertEmpLeave(EHCMAbsenceType absencetype, EhcmEmpPerInfo employee,
      String startDate, String endDate, String nextYearStartDate, EHCMEmpLeave prevYearleave,
      EHCMAbsenceTypeAccruals accrual) {
    List<EHCMEmpLeave> empLeaveList = new ArrayList<EHCMEmpLeave>();
    EHCMEmpLeave currentYearleave = null, l1 = null;
    EHCMEmpLeave leave = null;
    JSONObject result = null;
    try {
      /*
       * result = sa.elm.ob.hcm.util.UtilityDAO
       * .getMinMaxStartDateUsingDate(yearFormat.parse(nextYearStartDate));
       */
      result = getStartDateAndEndDate(yearFormat.parse(nextYearStartDate), absencetype,
          employee.getId());

      leave = getEmpLeave(absencetype, employee,
          dateFormat.format(yearFormat.parse(nextYearStartDate)));
      if (leave == null) {// && prevYearleave != null
        leave = OBProvider.getInstance().get(EHCMEmpLeave.class);
        leave.setClient(employee.getClient());
        leave.setOrganization(employee.getOrganization());
        leave.setCreationDate(new java.util.Date());
        leave.setCreatedBy(employee.getCreatedBy());
        leave.setUpdated(new java.util.Date());
        leave.setUpdatedBy(employee.getUpdatedBy());
        leave.setEhcmEmpPerinfo(employee);
        leave.setAbsenceType(absencetype);
        leave.setGradeClassifications(employee.getGradeClass());
        leave.setEnabled(true);
        leave.setOfLeaves(new BigDecimal(0));// accrual.getDays();
        leave.setPooleddays(new BigDecimal(0));
        leave.setAvailabledays(new BigDecimal(0));
        leave.setAvaileddays(new BigDecimal(0));
        leave.setCreditOn(accrual.getCreditOn());
        if (result != null) {
          leave.setStartDate(yearFormat.parse(result.getString("startdate")));
          leave.setEndDate(yearFormat.parse(result.getString("enddate")));
        }
        if (prevYearleave != null && prevYearleave.getSubtype() != null) {
          leave.setSubtype(prevYearleave.getSubtype());
        }
        OBDal.getInstance().save(leave);
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      log4j.error("Exception in getEmpLevae " + e.getMessage());
      e.printStackTrace();
    }
    return leave;
  }

  public int insertEmpLeaveLine(Connection conn, EHCMEmpLeave leave, String startdate,
      String enddate, BigDecimal accrualdays, EHCMEmpLeave prevYearleave,
      String nextYearStartDate) {
    int count = 0;
    BigDecimal leavedays = BigDecimal.ZERO;
    BigDecimal remainingdays = BigDecimal.ZERO;
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = null;
    BigDecimal accrualday = BigDecimal.ZERO;
    String hql = null;
    try {
      accrualday = accrualdays;

      // to chekc already opening balance insert for this leave

      hql = " and ((to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'yyyy-MM-dd') "
          + " and to_date(to_char(coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'yyyy-MM-dd')) "
          + " or (to_date(to_char( coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'yyyy-MM-dd') "
          + " and to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'yyyy-MM-dd')))   ";

      OBQuery<EHCMEmpLeaveln> levln = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class,
          " as e " + "  where e.ehcmEmpLeave.id=:empLeveId and e.leaveAction='OB' " + hql);
      levln.setNamedParameter("empLeveId", leave.getId());
      levln.setNamedParameter("fromdate", startdate);
      levln.setNamedParameter("todate", enddate);
      levln.setMaxResult(1);

      if (levln.list().size() == 0) {

        // insert into emp leaveln remaining balance
        sql = " select  coalesce(sum(leavedays),0) as leavedays , leave_type from  ehcm_emp_leaveln "
            + "  where  ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(?,'yyyy-MM-dd') "
            + "  and to_date(to_char(enddate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(?,'yyyy-MM-dd') ) "
            + "   or (to_date(to_char(enddate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(?,'yyyy-MM-dd')  "
            + "   and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy')     <= to_date(?,'yyyy-MM-dd'))) "
            + " and  ehcm_emp_leave_id in ( select  ehcm_emp_leave_id from ehcm_emp_leave "
            + " where ehcm_emp_perinfo_id= ? and ehcm_absence_type_id= ? ) group by leave_type ";

        st = conn.prepareStatement(sql);
        st.setString(1, startdate);
        st.setString(2, enddate);
        st.setString(3, startdate);
        st.setString(4, enddate);
        st.setString(5, leave.getEhcmEmpPerinfo().getId());
        st.setString(6, leave.getAbsenceType().getId());
        log4j.debug("st count:" + st.toString());
        rs = st.executeQuery();

        /*
         * if (rs.next()) { leavedays = rs.getBigDecimal("leavedays"); log4j.debug("leavedayys " +
         * leavedays); remainingdays = accrualdays.subtract(leavedays); log4j.debug("remainingdays:"
         * + remainingdays); } else remainingdays = accrualdays.subtract(leavedays);
         */
        while (rs.next()) {
          if (rs.getString("leave_type").equals("AC")) {
            accrualday = accrualday.add(rs.getBigDecimal("leavedays"));
          } else if (rs.getString("leave_type").equals("AB")) {
            leavedays = leavedays.add(rs.getBigDecimal("leavedays"));
          }
        }
        log4j.debug("leavedayys " + leavedays);
        remainingdays = accrualday.subtract(leavedays);
        log4j.debug("remainingdays:" + remainingdays);

        EHCMEmpLeaveln leaveln = OBProvider.getInstance().get(EHCMEmpLeaveln.class);
        leaveln.setClient(leave.getClient());
        leaveln.setOrganization(leave.getOrganization());
        leaveln.setEnabled(leave.isEnabled());
        leaveln.setCreationDate(new java.util.Date());
        leaveln.setCreatedBy(OBDal.getInstance().get(User.class, leave.getCreatedBy().getId()));
        leaveln.setUpdated(new java.util.Date());
        leaveln.setUpdatedBy(OBDal.getInstance().get(User.class, leave.getUpdatedBy().getId()));
        leaveln.setEhcmEmpLeave(leave);
        leaveln.setLeavedays(remainingdays);
        leaveln.setStartDate(yearFormat.parse(nextYearStartDate));
        leaveln.setEndDate(yearFormat.parse(nextYearStartDate));
        leaveln.setLeaveAction(Constants.EMPLEAVE_OPENINGBALANCE);
        leaveln.setLeaveType(Constants.EMPLEAVE_ACCRUAL);
        leaveln.setEhcmAbsenceAttendance(null);
        OBDal.getInstance().save(leaveln);
        OBDal.getInstance().flush();
        log4j.debug("leaveln:" + leaveln.getId());

      }
    } catch (final Exception e) {
      log4j.error("Exception in insertEmpLeaveLine", e);
    }
    return count;

  }

  @Override
  public JSONObject getStartDateAndEndDate(Date startDate, EHCMAbsenceType absencetype,
      String employeeId) {
    JSONObject result = new JSONObject();
    try {
      SQLQuery Query = OBDal.getInstance().getSession()
          .createSQLQuery(" select  * from ehcm_getaccrualstartenddate(?,?,?,?,?,?) ");
      log4j.debug("Query:" + Query.toString());
      Query.setParameter(0, employeeId);
      Query.setParameter(1, yearFormat.format(startDate));
      Query.setParameter(2, absencetype.getAccrualResetDate());
      Query.setParameter(3, absencetype.getFrequency());
      Query.setParameter(4, absencetype.getId());
      /*
       * if (absencetype.getSubtype() != null) Query.setParameter(5, absence.getSubtype().getId());
       * else
       */
      Query.setParameter(5, "");
      log4j.debug("Query:" + Query.getQueryString());
      log4j.debug("getEhcmEmpPerinfo:" + employeeId);
      log4j.debug("getStartDate:" + yearFormat.format(startDate));
      log4j.debug("getEndDate:" + absencetype.getAccrualResetDate());
      log4j.debug("getClient:" + absencetype.getFrequency());
      log4j.debug("size:" + Query.getQueryString());
      if (Query.list().size() > 0) {
        log4j.debug("get:" + Query.list().get(0));
        Object[] row = (Object[]) Query.list().get(0);
        log4j.debug("row:" + row);
        result.put("startdate", row[0]);
        result.put("enddate", row[1]);
      }

    } catch (

    final Exception e) {
      log4j.error("Exception in getStartDateAndEndDate() :", e);
      return result;
    }
    return result;
  }

  public List<EmployeeVO> getEmployeeHireAnniversaryHYList(Date nextDayHiji, String nextDayHijiStr,
      String clientId) {
    List<EmployeeVO> empHireDateLs = new ArrayList<EmployeeVO>();
    EmployeeVO eVO = null;
    try {
      SQLQuery Query = OBDal.getInstance().getSession()
          .createSQLQuery(" select  emp.ehcm_emp_perinfo_id,emp.ehcm_gradeclass_id , emp.hiredate,"
              + "substring((select eut_convert_to_hijri(to_char(emp.hiredate,'yyyy-MM-dd')) ),0,6) from ehcm_emp_perinfo as emp "
              + "where substring((select eut_convert_to_hijri(to_char(emp.hiredate,'yyyy-MM-dd')) ),0,6) =:nextDayddMM "
              + "and emp.hiredate < :nextDay  and emp.ad_client_id=:clientId");

      log4j.debug("Query:" + Query.toString());
      Query.setParameter("nextDayddMM", nextDayHijiStr);
      Query.setParameter("nextDay", nextDayHiji);
      Query.setParameter("clientId", clientId);

      log4j.debug("Query:" + Query.getQueryString());
      log4j.debug("size:" + Query.getQueryString());
      List employeeList = Query.list();
      if (employeeList != null && employeeList.size() > 0) {
        for (Object o : employeeList) {
          Object[] row = (Object[]) o;
          eVO = new EmployeeVO();
          eVO.setEmployee(OBDal.getInstance().get(EhcmEmpPerInfo.class, row[0].toString()));
          eVO.setGradeclassId(row[1].toString());
          eVO.setEmpHireDate(yearFormat.parse(row[2].toString()));
          empHireDateLs.add(eVO);
        }
      }

    } catch (

    final Exception e) {
      log4j.error("Exception in getStartDateAndEndDate() :", e);
      return empHireDateLs;
    }
    return empHireDateLs;
  }

  public List<EmployeeVO> getEmployeeHireAnniversaryGYList(Date nextDayGreg, String nextDayGregStr,
      String clientId) {
    List<EmployeeVO> empHireDateLs = new ArrayList<EmployeeVO>();
    EmployeeVO eVO = null;
    try {
      SQLQuery Query = OBDal.getInstance().getSession()
          .createSQLQuery(" select  emp.ehcm_emp_perinfo_id, emp.ehcm_gradeclass_id, emp.hiredate,"
              + " substring(to_char(emp.hiredate,'dd-MM-yyyy'),0,6) from ehcm_emp_perinfo as emp "
              + "where substring(to_char(emp.hiredate,'dd-MM-yyyy'),0,6) =:nextDayddMM "
              + "and emp.hiredate < :nextDay  and emp.ad_client_id=:clientId");

      log4j.debug("Query:" + Query.toString());

      Query.setParameter("nextDayddMM", nextDayGregStr);
      Query.setParameter("nextDay", nextDayGreg);
      Query.setParameter("clientId", clientId);

      log4j.debug("Query:" + Query.getQueryString());
      log4j.debug("size:" + Query.getQueryString());
      List employeeList = Query.list();
      if (employeeList != null && employeeList.size() > 0) {
        for (Object o : employeeList) {
          Object[] row = (Object[]) o;
          eVO = new EmployeeVO();
          eVO.setEmployee(OBDal.getInstance().get(EhcmEmpPerInfo.class, row[0].toString()));
          eVO.setGradeclassId(row[1].toString());
          eVO.setEmpHireDate(yearFormat.parse(row[2].toString()));
          empHireDateLs.add(eVO);
        }
      }

    } catch (

    final Exception e) {
      log4j.error("Exception in getStartDateAndEndDate() :", e);
      return empHireDateLs;
    }
    return empHireDateLs;
  }

  public JSONObject getNextDayGregAndHijiriDate(Date currentDate) {
    JSONObject result = new JSONObject();
    try {
      SQLQuery Query = OBDal.getInstance().getSession().createSQLQuery(
          " select max(hijri_date), to_char(gregorian_date,'dd-MM-yyyy')   from eut_hijri_dates "
              + "where gregorian_date >:currentDate "
              + " group by gregorian_date order by gregorian_date asc " + "limit 1");
      log4j.debug("Query:" + Query.toString());
      Query.setParameter("currentDate", currentDate);
      log4j.debug("Query:" + Query.getQueryString());
      log4j.debug("size:" + Query.getQueryString());
      List employeeList = Query.list();
      if (employeeList != null && employeeList.size() > 0) {
        Object[] row = (Object[]) Query.list().get(0);
        result.put("hijiriDate", row[0].toString());
        result.put("gregorianDate", row[1]);
      }

    } catch (

    final Exception e) {
      log4j.error("Exception in getNextDayGregAndHijiriDate() :", e);
      return result;
    }
    return result;
  }
}
