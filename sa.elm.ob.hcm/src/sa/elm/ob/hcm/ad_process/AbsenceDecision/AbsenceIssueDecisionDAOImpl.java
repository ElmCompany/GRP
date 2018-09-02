package sa.elm.ob.hcm.ad_process.AbsenceDecision;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;
import sa.elm.ob.hcm.EHCMAbsenceTypeAction;
import sa.elm.ob.hcm.EHCMAbsenceTypeRules;
import sa.elm.ob.hcm.EHCMEMPLeaveBlockLn;
import sa.elm.ob.hcm.EHCMEmpLeave;
import sa.elm.ob.hcm.EHCMEmpLeaveBlock;
import sa.elm.ob.hcm.EHCMEmpLeaveln;
import sa.elm.ob.hcm.EHCMHolidayCalendar;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmgradeclass;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * This process class used for Absence DecisionDAO Implementation
 * 
 * @author divya 09-04-2018
 *
 */
@Repository
public class AbsenceIssueDecisionDAOImpl implements AbsenceIssueDecisionDAO {

  private static final Logger log = LoggerFactory.getLogger(AbsenceIssueDecisionDAOImpl.class);
  DateFormat yearFormat = Utility.YearFormat;
  DateFormat dateFormat = Utility.dateFormat;

  /**
   * Get the database connection
   * 
   * @return
   */

  public Boolean chkEmpLeavePresentInTwoBlk(EHCMAbsenceAttendance absence) throws Exception {
    List<EHCMEmpLeaveBlock> levblkList = null;
    String hql = "";
    try {

      hql = " and e.enabled='Y' and  e.ehcmEmpPerinfo.id=:employeeId and e.absenceType.id=:absenceTypeId ";

      if (absence.getSubtype() != null) {
        hql += " and e.subtype.id=:subTypeId ";
      }

      OBQuery<EHCMEmpLeaveBlock> levblok = OBDal.getInstance().createQuery(EHCMEmpLeaveBlock.class,
          " as e where e.enabled='Y' and  e.ehcmEmpPerinfo.id=:employeeId and e.absenceType.id=:absenceTypeId "
              + "  and ((:startDate between TO_DATE(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy')  and  TO_DATE(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy')) "
              + " and  :endDate between TO_DATE(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') "
              + "  and  TO_DATE(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy')) " + hql);

      levblok.setNamedParameter("employeeId", absence.getEhcmEmpPerinfo().getId());
      levblok.setNamedParameter("absenceTypeId", absence.getEhcmAbsenceType().getId());
      levblok.setNamedParameter("startDate", absence.getStartDate());
      levblok.setNamedParameter("endDate", absence.getEndDate());

      if (absence.getSubtype() != null) {
        levblok.setNamedParameter("subTypeId", absence.getSubtype().getId());
      }
      log.debug(" firstchk :" + levblok.getWhereAndOrderBy());
      levblkList = levblok.list();
      if (levblkList.size() > 1) {
        return true;
      } else
        return false;

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in chkEmpLeavePresentInTwoBlk in AbsenceIssueDecisionDAOImpl: ", e);
    }
    return false;
  }

  public List<EHCMAbsenceTypeAccruals> getAbsenceAccrual(EHCMAbsenceAttendance absence,
      boolean empgrdclssdef) throws Exception {
    List<EHCMAbsenceTypeAccruals> accrualList = null;
    try {
      OBContext.setAdminMode();
      OBQuery<EHCMAbsenceTypeAccruals> accrualsQry = OBDal.getInstance()
          .createQuery(EHCMAbsenceTypeAccruals.class, " as e where e.absenceType.id=:absenceTypeId "
              + " and to_date(:startdate,'dd-MM-yyyy') between to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') and "
              + " to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') "
              + " and (e.gradeClassifications.id =:gradeclass  or "
              + " e.gradeClassifications is null ) order by e.gradeClassifications asc ");

      accrualsQry.setNamedParameter("absenceTypeId", absence.getEhcmAbsenceType().getId());
      accrualsQry.setNamedParameter("startdate", Utility.formatDate(absence.getStartDate()));
      accrualsQry.setNamedParameter("gradeclass",
          absence.getEhcmEmpPerinfo().getGradeClass().getId());
      accrualsQry.setFilterOnReadableClients(false);
      accrualsQry.setFilterOnReadableOrganization(false);
      accrualList = accrualsQry.list();
      return accrualList;
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in chkAbsenceGradeClassDefineOrNot in AbsenceIssueDecisionDAOImpl: ", e);
      throw new Exception(
          "Exception in chkAbsenceGradeClassDefineOrNot in AbsenceIssueDecisionDAOImpl");
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public List<EHCMEmpLeave> getEmployeeLeave(EHCMAbsenceAttendance absence,
      EHCMAbsenceType absencetype) throws Exception {
    List<EHCMEmpLeave> empLeaveList = null;
    String hql = "";
    try {

      if (absence.getSubtype() != null) {
        hql = " and e.subtype.id=:subTypeId ";
      }
      /*
       * hql =
       * " and ((to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
       * +
       * " and to_date(to_char(coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
       * +
       * " or (to_date(to_char( coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
       * +
       * " and to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')))  "
       * ;
       * 
       * OBQuery<EHCMEmpLeave> empleaveQry = OBDal.getInstance().createQuery(EHCMEmpLeave.class,
       * " ehcmEmpPerinfo.id=:employeeId and absenceType.id=:absenceTypeId and gradeClassifications.id=:gradeclassId "
       * + hql); empleaveQry.setNamedParameter("employeeId", absence.getEhcmEmpPerinfo().getId());
       * empleaveQry.setNamedParameter("absenceTypeId", absencetype.getId());
       * empleaveQry.setNamedParameter("gradeclassId",
       * absence.getEhcmEmpPerinfo().getGradeClass().getId());
       * empleaveQry.setNamedParameter("fromdate", Utility.formatDate(absence.getStartDate()));
       * empleaveQry.setNamedParameter("todate", Utility.formatDate(absence.getEndDate()));
       * log.debug("empleave" + empleaveQry.getWhereAndOrderBy()); empleaveQry.setMaxResult(1);
       * empLeaveList = empleaveQry.list();
       */

      OBQuery<EHCMEmpLeave> empLeaveQry = OBDal.getInstance().createQuery(EHCMEmpLeave.class,
          " as e where e.absenceType.id=:absenceType and e.ehcmEmpPerinfo.id=:employeeId "
              + "   and to_date(:startDate,'yyyy-MM-dd') between  to_date(to_char(e.startDate ,'dd-MM-yyyy'),'dd-MM-yyyy') "
              + "          and to_date(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy')  " + hql);
      empLeaveQry.setNamedParameter("absenceType", absencetype.getId());
      empLeaveQry.setNamedParameter("employeeId", absence.getEhcmEmpPerinfo().getId());
      empLeaveQry.setNamedParameter("startDate", absence.getStartDate());
      if (absence.getSubtype() != null) {
        empLeaveQry.setNamedParameter("subTypeId", absence.getSubtype().getId());
      }
      empLeaveQry.setMaxResult(1);
      empLeaveList = empLeaveQry.list();

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in getEmployeeLeave in AbsenceIssueDecisionDAOImpl: ", e);
    }
    return empLeaveList;
  }

  public void updateAbsenceDecision(EHCMAbsenceAttendance absence) {
    try {
      absence.setSueDecision(true);
      absence.setDecisionDate(new Date());
      absence.setDecisionStatus("I");
      OBDal.getInstance().save(absence);
      // OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateAbsenceDecision in AbsenceIssueDecisionDAOImpl: ", e);
    }
  }

  public String chkleaveapprove(EHCMAbsenceAttendance absence) {
    String message = "";
    try {

      SQLQuery Query = OBDal.getInstance().getSession()
          .createSQLQuery(" select ehcm_levcalprocess(?, ?, ?, ?, ?,?,?,?,?);");
      log.debug("Query:" + Query.toString());
      Query.setParameter(0, absence.getEhcmEmpPerinfo().getId());
      Query.setParameter(1, yearFormat.format(absence.getStartDate()));
      Query.setParameter(2, yearFormat.format(absence.getEndDate()));
      Query.setParameter(3, absence.getEhcmAbsenceType().getId());
      Query.setParameter(4, absence.getClient().getId());
      Query.setParameter(5, absence.getAbsenceDays());
      if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        Query.setParameter(6, DecisionTypeConstants.DECISION_TYPE_UPDATE);
        Query.setParameter(7, absence.getOriginalDecisionNo().getId());
      } else {
        Query.setParameter(6, absence.getDecisionType());
        Query.setParameter(7, absence.getId());
      }
      if (absence.getSubtype() != null)
        Query.setParameter(8, absence.getSubtype().getId());
      else
        Query.setParameter(8, "");
      log.debug("Query:" + Query.getQueryString());
      log.debug("Query:" + Query.getNamedParameters());
      log.debug("getEhcmEmpPerinfo:" + absence.getEhcmEmpPerinfo().getId());
      log.debug("getStartDate:" + yearFormat.format(absence.getStartDate()));
      log.debug("getEndDate:" + yearFormat.format(absence.getEndDate()));
      log.debug("getClient:" + absence.getClient().getId());
      log.debug("getAbsenceDays:" + absence.getAbsenceDays());
      log.debug("absence.getId:" + absence.getId());
      log.debug("getEhcmAbsenceType:" + absence.getEhcmAbsenceType().getId());
      log.debug("size:" + Query.list().size());
      if (Query.list().size() > 0) {
        log.debug("get:" + Query.list().get(0));
        Object row = (Object) Query.list().get(0);
        log.debug("row:" + row);
        message = (String) row;
        log.debug("Query:" + Query.toString());
      }
      log.debug("chkleaveapprove" + message);
    } catch (final Exception e) {
      e.printStackTrace();
      log.error("Exception in chkleaveapprove() Method : ", e);
    }
    return message;
  }

  public BigDecimal getAvailableAndAvaileddays(Connection conn, EHCMAbsenceAttendance absenceattend,
      EHCMAbsenceType absencetype, String startdate, String enddate, Boolean availabledays,
      EHCMAbsenceAttendance cancelAbsence) {
    BigDecimal days = BigDecimal.ZERO;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = conn.prepareStatement(
          " select * from  ehcm_getavailed_availablelev(?, ?, ?, ?, ?,?,?,?,?,?) ");
      st.setString(1, absenceattend.getEhcmEmpPerinfo().getId());
      st.setString(2, startdate);
      st.setString(3, enddate);
      st.setString(4, absencetype.getId());
      st.setString(5, absenceattend.getClient().getId());
      st.setInt(6, 1);
      // in case of cr,ex,up now if we are doing cancel once again it will come back
      // to ex , so we
      // need to consider current cancel original decision type to get the
      // availabledays

      if (cancelAbsence != null) {
        st.setString(7, cancelAbsence.getOriginalDecisionNo().getDecisionType());
      } else {
        if (!absenceattend.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          st.setString(7, DecisionTypeConstants.DECISION_TYPE_UPDATE);
        } else {
          st.setString(7, absenceattend.getDecisionType());
        }
      }
      if (!absenceattend.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        st.setString(8, absenceattend.getOriginalDecisionNo().getId());
      } else {
        st.setString(8, absenceattend.getId());
      }
      st.setBoolean(9, availabledays);
      if (absenceattend.getSubtype() != null)
        st.setString(10, absenceattend.getSubtype().getId());
      else
        st.setString(10, "");
      log.debug("st1" + st.toString());

      rs = st.executeQuery();
      if (rs.next()) {
        days = new BigDecimal(rs.getInt("p_availableleavedays"));
      }
      log.debug("days" + days);
    } catch (final Exception e) {
      e.printStackTrace();
      log.error("Exception in getavailableandavaileddays() Method : ", e);
    }
    return days;
  }

  public BigInteger calculatedays(String startdate, String enddate, String clientId) {
    BigInteger days = BigInteger.ZERO;
    Query query = null;
    String strQuery = "";
    try {
      strQuery = " select count(a.hijri_date) from ( select max(hijri_date) as hijri_date,gregorian_date"
          + "  from eut_hijri_dates  where gregorian_date >=to_date(:startDate,'yyyy-MM-dd') "
          + " and gregorian_date <=to_date(:endDate, 'yyyy-MM-dd' )group by gregorian_date) a  ";
      query = OBDal.getInstance().getSession().createSQLQuery(strQuery);
      query.setParameter("startDate", startdate);
      query.setParameter("endDate", enddate);
      log.debug("strQuery:" + query);
      log.debug("size123:" + query.list().size());
      if (query != null && query.list().size() > 0) {
        log.debug("geto" + query.list().get(0));
        days = (BigInteger) query.list().get(0);
      }
    } catch (Exception e) {
      log.error("Exception in calculatedays", e);
      e.printStackTrace();
    }
    return days;
  }

  public JSONObject getLeaveList(EHCMAbsenceAttendance absence, EHCMAbsenceType absenceType,
      BigDecimal absencedays, Date StartDate, Date EndDate) {
    JSONObject result = null, json = null;
    List<EHCMEmpLeave> empLeaveList = null;
    EhcmEmpPerInfo employee = null;
    String oneDayAfter = null;
    JSONObject leavelist = new JSONObject();
    JSONArray jsonArray = new JSONArray();

    // Date StartDate = null;
    // Date EndDate = null;

    String startDate = null;
    String endDate = null;

    int startYear = 0, endYear = 0;
    int diff = 0;
    String hql = "";
    try {

      if (absence.getSubtype() != null) {
        hql = " and e.subtype.id=:subTypeId ";
      }

      employee = absence.getEhcmEmpPerinfo();
      // StartDate = startdate;// absence.getStartDate();

      if (StartDate != null) {
        startDate = UtilityDAO.convertTohijriDate(yearFormat.format(absence.getStartDate()));
        startYear = Integer.parseInt(startDate.split("-")[2]);
      }
      if (EndDate != null) {
        endDate = UtilityDAO.convertTohijriDate(yearFormat.format(absence.getEndDate()));
        endYear = Integer.parseInt(endDate.split("-")[2]);
      } else {
        endYear = startYear;
      }
      diff = (endYear - startYear) + 1;
      for (int i = 0; i < diff; i++) {
        if (StartDate != null) {
          result = sa.elm.ob.hcm.util.UtilityDAO.getMinMaxStartDateUsingDate(StartDate);
        }
        OBQuery<EHCMEmpLeave> empLeaveQry = OBDal.getInstance().createQuery(EHCMEmpLeave.class,
            " as e where e.absenceType.id=:absenceType and e.ehcmEmpPerinfo.id=:employeeId "
                + "   and to_date(:startDate,'yyyy-MM-dd') between  to_date(to_char(e.startDate ,'dd-MM-yyyy'),'dd-MM-yyyy') "
                + "          and to_date(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy')  " + hql);
        empLeaveQry.setNamedParameter("absenceType", absenceType.getId());
        empLeaveQry.setNamedParameter("employeeId", employee.getId());
        empLeaveQry.setNamedParameter("startDate", StartDate);
        if (absence.getSubtype() != null) {
          empLeaveQry.setNamedParameter("subTypeId", absence.getSubtype().getId());
        }

        empLeaveQry.setMaxResult(1);
        empLeaveList = empLeaveQry.list();
        if (empLeaveList.size() > 0) {
          if (diff > 1) {
            json = new JSONObject();
            json.put("id", empLeaveList.get(0).getId());
            if (i < diff - 1) {
              json.put("days", sa.elm.ob.hcm.util.Utility.caltheDaysUsingGreDate(StartDate,
                  empLeaveList.get(0).getEndDate()));
              json.put("startdate", yearFormat.format(StartDate));
              json.put("enddate", yearFormat.format(empLeaveList.get(0).getEndDate()));
            } else {
              json.put("days",
                  sa.elm.ob.hcm.util.Utility.caltheDaysUsingGreDate(StartDate, EndDate));
              json.put("startdate", yearFormat.format(StartDate));
              json.put("enddate", yearFormat.format(EndDate));
            }
            jsonArray.put(json);
          } else {
            json = new JSONObject();
            json.put("id", empLeaveList.get(0).getId());
            json.put("days", sa.elm.ob.hcm.util.Utility.caltheDaysUsingGreDate(StartDate, EndDate));
            json.put("startdate", yearFormat.format(StartDate));
            json.put("enddate", yearFormat.format(EndDate));
            jsonArray.put(json);
          }
          leavelist.put("leavelist", jsonArray);
          if (diff > 1 && (diff - 1) > i) {
            oneDayAfter = UtilityDAO
                .convertTohijriDate(yearFormat.format(empLeaveList.get(0).getEndDate()));
            oneDayAfter = sa.elm.ob.hcm.util.UtilityDAO.getAfterDateInGreg(oneDayAfter);
            StartDate = dateFormat.parse(oneDayAfter);
          }
        }
      }
    } catch (Exception e) {
      log.error("Exception in getLeaveList", e);
      e.printStackTrace();
    }
    return leavelist;

  }

  public EHCMEmpLeave insertEmpLeave(EHCMAbsenceAttendance absence, EHCMAbsenceType absencetype,
      EHCMAbsenceTypeAccruals accrual, Date absStartDate, Date absEnddate) {
    EHCMEmpLeave leave = null;
    Date StartDate = null;
    String startDate = null;
    String endDate = null;
    int startYear = 0, endYear = 0;
    int diff = 0;
    JSONObject result = null;
    List<EHCMEmpLeave> empLeaveList = null;
    EhcmEmpPerInfo employee = null;
    String oneDayAfter = null;
    String hql = "";
    try {

      if (absence.getSubtype() != null) {
        hql = " and e.subtype.id=:subTypeId ";
      }

      employee = absence.getEhcmEmpPerinfo();
      StartDate = absStartDate;
      if (absStartDate != null) {
        startDate = UtilityDAO.convertTohijriDate(yearFormat.format(absStartDate));
        startYear = Integer.parseInt(startDate.split("-")[2]);
      }
      if (absEnddate != null) {
        endDate = UtilityDAO.convertTohijriDate(yearFormat.format(absEnddate));
        endYear = Integer.parseInt(endDate.split("-")[2]);
      } else {
        endYear = startYear;
      }
      diff = (endYear - startYear) + 1;
      for (int i = 0; i < diff; i++) {
        if (StartDate != null) {
          // result =
          // sa.elm.ob.hcm.util.UtilityDAO.getMinMaxStartDateUsingDate(StartDate);
          result = getStartDateAndEndDate(StartDate, absEnddate, absence, absencetype);
        }

        // check already empleave exists or not
        OBQuery<EHCMEmpLeave> empLeaveQry = OBDal.getInstance().createQuery(EHCMEmpLeave.class,
            " as e where e.absenceType.id=:absenceType and e.ehcmEmpPerinfo.id=:employeeId "
                + "   and to_date(:startDate,'yyyy-MM-dd') between  to_date(to_char(e.startDate ,'dd-MM-yyyy'),'dd-MM-yyyy') "
                + "          and to_date(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy')  " + hql);
        empLeaveQry.setNamedParameter("absenceType", absencetype.getId());
        empLeaveQry.setNamedParameter("employeeId", employee.getId());
        empLeaveQry.setNamedParameter("startDate", StartDate);
        if (absence.getSubtype() != null) {
          empLeaveQry.setNamedParameter("subTypeId", absence.getSubtype().getId());
        }
        empLeaveQry.setMaxResult(1);
        empLeaveList = empLeaveQry.list();
        if (empLeaveList.size() == 0) {
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
          if (accrual != null)
            leave.setCreditOn(accrual.getCreditOn());
          else
            leave.setCreditOn(Constants.EMPLEAVE_CREDITON_ONETIME);
          if (result != null) {
            leave.setStartDate(yearFormat.parse(result.getString("startdate")));// mingregdate

            if (result.getString("enddate") != null) {
              /*
               * oneDayBefore = sa.elm.ob.hcm.util.UtilityDAO
               * .getBeforeDateInGregUsingGregDate(result.getString("enddate"));
               * leave.setEndDate(dateFormat.parse(oneDayBefore));// maxgregdate
               */ leave.setEndDate(yearFormat.parse(result.getString("enddate")));// maxgregdate
            }
          }
          if (absence.getSubtype() != null) {
            leave.setSubtype(absence.getSubtype());
          }
          OBDal.getInstance().save(leave);
        }
        if (diff > 1 && (diff - 1) > i) {
          oneDayAfter = UtilityDAO
              .convertTohijriDate(yearFormat.format(yearFormat.parse(result.getString("enddate"))));// maxgregdate
          oneDayAfter = sa.elm.ob.hcm.util.UtilityDAO.getAfterDateInGreg(oneDayAfter);
          StartDate = dateFormat.parse(oneDayAfter);
        }
      }
    } catch (final Exception e) {
      log.error("Exception in insertEmpLeave" + e);
    }
    return leave;

  }

  public EHCMEmpLeave insertLeaveOccuranceEmpLeave(EHCMAbsenceAttendance absence,
      EHCMAbsenceType absencetype, EHCMAbsenceTypeAccruals accrual, Date absStartDate,
      Date absEnddate) {
    EHCMEmpLeave leave = null;
    Date StartDate = null;
    int diff = 0;
    JSONObject result = null;
    List<EHCMEmpLeave> empLeaveList = null;
    EhcmEmpPerInfo employee = null;
    String oneDayAfter = null;
    String hql = "";
    Date leaveEndDate = null;
    try {
      if (absence.getSubtype() != null) {
        hql = " and e.subtype.id=:subTypeId ";
      }

      employee = absence.getEhcmEmpPerinfo();
      StartDate = absStartDate;
      result = getStartDateAndEndDate(StartDate, absEnddate, absence, absencetype);

      if (result != null) {
        if (result.getString("enddate") != null) {
          diff++;
          while (diff == 1 || (diff > 1 && absEnddate.compareTo(leaveEndDate) > 0)) {
            diff++;
            // check already empleave exists or not
            OBQuery<EHCMEmpLeave> empLeaveQry = OBDal.getInstance().createQuery(EHCMEmpLeave.class,
                " as e where e.absenceType.id=:absenceType and e.ehcmEmpPerinfo.id=:employeeId "
                    + "   and to_date(:startDate,'yyyy-MM-dd') between  to_date(to_char(e.startDate ,'dd-MM-yyyy'),'dd-MM-yyyy') "
                    + "          and to_date(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy')  "
                    + hql);
            empLeaveQry.setNamedParameter("absenceType", absencetype.getId());
            empLeaveQry.setNamedParameter("employeeId", employee.getId());
            empLeaveQry.setNamedParameter("startDate", StartDate);
            if (absence.getSubtype() != null) {
              empLeaveQry.setNamedParameter("subTypeId", absence.getSubtype().getId());
            }
            empLeaveQry.setMaxResult(1);
            empLeaveList = empLeaveQry.list();
            if (empLeaveList.size() == 0) {
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
              if (accrual != null)
                leave.setCreditOn(accrual.getCreditOn());
              else
                leave.setCreditOn(Constants.EMPLEAVE_CREDITON_ONETIME);
              if (result != null) {
                leave.setStartDate(yearFormat.parse(result.getString("startdate")));

                if (result.getString("enddate") != null) {
                  leave.setEndDate(yearFormat.parse(result.getString("enddate")));
                }
              }
              if (absence.getSubtype() != null) {
                leave.setSubtype(absence.getSubtype());
              }
              OBDal.getInstance().save(leave);
              OBDal.getInstance().flush();// issue if loop execute only one time
              leaveEndDate = leave.getEndDate();
            } else {
              leave = empLeaveList.get(0);
              if (accrual != null)
                leave.setCreditOn(accrual.getCreditOn());
              else
                leave.setCreditOn(Constants.EMPLEAVE_CREDITON_ONETIME);

              if (result != null) {
                leave.setStartDate(yearFormat.parse(result.getString("startdate")));

                if (result.getString("enddate") != null) {
                  leave.setEndDate(yearFormat.parse(result.getString("enddate")));
                }
              }
              leaveEndDate = leave.getEndDate();

            }
            if (absEnddate.compareTo(leaveEndDate) > 0) {
              diff++;
              oneDayAfter = UtilityDAO.convertTohijriDate(
                  yearFormat.format(yearFormat.parse(result.getString("enddate"))));// maxgregdate
              oneDayAfter = sa.elm.ob.hcm.util.UtilityDAO.getAfterDateInGreg(oneDayAfter);
              StartDate = dateFormat.parse(oneDayAfter);
              result = getStartDateAndEndDate(StartDate, absEnddate, absence, absencetype);

            }

          }
        }
      }

    } catch (final Exception e) {
      log.error("Exception in insertEmpLeave" + e);
    }
    return leave;

  }

  public JSONObject getLeaveOccuranceList(EHCMAbsenceAttendance absence,
      EHCMAbsenceType absenceType, BigDecimal absencedays, Date StartDate, Date EndDate) {
    JSONObject result = null, json = null;
    List<EHCMEmpLeave> empLeaveList = null;
    EhcmEmpPerInfo employee = null;
    String oneDayAfter = null;
    JSONObject leavelist = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    Date leaveEndDate = null;

    int diff = 0;
    String hql = "";
    try {

      if (absence.getSubtype() != null) {
        hql = " and e.subtype.id=:subTypeId ";
      }

      employee = absence.getEhcmEmpPerinfo();

      result = getStartDateAndEndDate(StartDate, EndDate, absence, absenceType);

      if (result != null && result.getString("enddate") != null) {
        diff++;
        while (diff == 1
            || (diff > 1 && leaveEndDate != null && EndDate.compareTo(leaveEndDate) > 0)) {
          OBQuery<EHCMEmpLeave> empLeaveQry = OBDal.getInstance().createQuery(EHCMEmpLeave.class,
              " as e where e.absenceType.id=:absenceType and e.ehcmEmpPerinfo.id=:employeeId "
                  + "   and to_date(:startDate,'yyyy-MM-dd') between  to_date(to_char(e.startDate ,'dd-MM-yyyy'),'dd-MM-yyyy') "
                  + "          and to_date(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy')  " + hql);
          empLeaveQry.setNamedParameter("absenceType", absenceType.getId());
          empLeaveQry.setNamedParameter("employeeId", employee.getId());
          empLeaveQry.setNamedParameter("startDate", StartDate);
          if (absence.getSubtype() != null) {
            empLeaveQry.setNamedParameter("subTypeId", absence.getSubtype().getId());
          }

          empLeaveQry.setMaxResult(1);
          log.debug("leave qry :" + empLeaveQry.getWhereAndOrderBy());
          empLeaveList = empLeaveQry.list();
          if (empLeaveList.size() > 0) {
            leaveEndDate = empLeaveList.get(0).getEndDate();
            if (EndDate.compareTo(leaveEndDate) > 0) {
              json = new JSONObject();
              json.put("id", empLeaveList.get(0).getId());
              if (EndDate.compareTo(leaveEndDate) > 0) {
                json.put("days", getEmpLevDateCountBasOnIsIncludeHolday(StartDate,
                    empLeaveList.get(0).getEndDate(), absenceType));
                // sa.elm.ob.hcm.util.Utility.caltheDaysUsingGreDate(StartDate,
                // empLeaveList.get(0).getEndDate()));
                json.put("startdate", yearFormat.format(StartDate));
                json.put("enddate", yearFormat.format(empLeaveList.get(0).getEndDate()));
              } else {
                json.put("days",
                    getEmpLevDateCountBasOnIsIncludeHolday(StartDate, EndDate, absenceType));
                // sa.elm.ob.hcm.util.Utility.caltheDaysUsingGreDate(StartDate, EndDate));
                json.put("startdate", yearFormat.format(StartDate));
                json.put("enddate", yearFormat.format(EndDate));
              }
              jsonArray.put(json);
            } else {
              diff++;
              json = new JSONObject();
              json.put("id", empLeaveList.get(0).getId());
              json.put("days",
                  getEmpLevDateCountBasOnIsIncludeHolday(StartDate, EndDate, absenceType));
              // sa.elm.ob.hcm.util.Utility.caltheDaysUsingGreDate(StartDate, EndDate));
              json.put("startdate", yearFormat.format(StartDate));
              json.put("enddate", yearFormat.format(EndDate));
              jsonArray.put(json);
            }
            leavelist.put("leavelist", jsonArray);
            if (EndDate.compareTo(leaveEndDate) > 0) {
              oneDayAfter = UtilityDAO
                  .convertTohijriDate(yearFormat.format(empLeaveList.get(0).getEndDate()));
              oneDayAfter = sa.elm.ob.hcm.util.UtilityDAO.getAfterDateInGreg(oneDayAfter);
              StartDate = dateFormat.parse(oneDayAfter);
              diff++;
              result = getStartDateAndEndDate(StartDate, EndDate, absence, absenceType);
            }
          } else {
            diff++;
          }
        }
      }
    } catch (Exception e) {
      log.error("Exception in getLeaveList", e);
      e.printStackTrace();
    }
    return leavelist;

  }

  public static int getEmpLevDateCountBasOnIsIncludeHolday(Date startDate, Date endDate,
      EHCMAbsenceType absenceType) {
    JSONArray jsonArray = new JSONArray();
    String sql = null, holidaysql = "";
    Query qry = null;
    int days = 0;
    try {
      /*
       * if (!absenceType.isInculdeholiday()) { holidaysql =
       * " where holidaytype not in ('WE','HD') "; } else { holidaysql =
       * " where holidaytype not in ('WE') "; }
       */
      if (!absenceType.isInculdeholiday()) {
        holidaysql = " where holidaytype  in ('WE','WD') ";
      }

      sql = " select count(hijri_date) as count from ( select max(hijri_date) as hijri_date, gregorian_date,(case when coalesce(holiday_type,'WD') not in ('WD','WE2','WE1') "
          + " then 'HD'  when  coalesce(holiday_type,'WD')  in ('WE2','WE1')  then 'WE' else "
          + " coalesce(holiday_type,'WD')  end) as holidaytype  " + " from eut_hijri_dates hij "
          + " left join ehcm_holiday_calendar cal on cal.holidaydate= hij.gregorian_date "
          + " and (cal.ad_client_id=:clientId or cal.ad_client_id is null) "
          + " where gregorian_date>=:startDate "
          + " and gregorian_date<=:endDate   group by gregorian_date,holiday_type  ) a "
          + holidaysql;
      qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      qry.setParameter("clientId", absenceType.getClient().getId());
      qry.setParameter("startDate", startDate);
      qry.setParameter("endDate", endDate);
      log.debug("leave qry :" + sql.toString());
      List datelist = qry.list();
      if (datelist != null && datelist.size() > 0) {
        Object row = (Object) datelist.get(0);
        days = ((BigInteger) row).intValue();
      }
    } catch (Exception e) {
      log.error("Exception in getLeaveList", e);
      e.printStackTrace();
    }
    return days;
  }

  public void updateEmpLeave(EHCMAbsenceAttendance absence) {
    List<EHCMEmpLeaveln> linelist = null;
    try {

      OBQuery<EHCMEmpLeaveln> lnQry = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class,
          " ehcmAbsenceAttendance.id='" + absence.getOriginalDecisionNo().getId() + "'");
      linelist = lnQry.list();
      if (linelist.size() > 0) {
        for (EHCMEmpLeaveln ln : linelist) {
          OBDal.getInstance().remove(ln);
        }
      }

      OBDal.getInstance().flush();
      // flush is important (test case: create absence decision for
      // employee now update the create decision while doing
      // update removing old leave lines,if leavedays is zero then header emp leave record also
      // deleting then only while going insertempleaveline , it will getleavelist
      // if header not then it will insert a new header line

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateEmpLeave in AbsenceIssueDecisionDAOImpl: ", e);
    }
  }

  public int insertEmpLeaveLine(EHCMEmpLeave header, EHCMAbsenceType absenceType,
      EHCMAbsenceAttendance absence, BigDecimal absencedays, Date startdate, Date enddate) {
    int count = 0;
    JSONObject json = null;
    JSONObject leavelist = null;
    try {

      if (absence.getEhcmAbsenceType().getAccrualResetDate().equals("LO")) {
        leavelist = getLeaveOccuranceList(absence, absenceType, absencedays, startdate, enddate);

      } else {
        leavelist = getLeaveOccuranceList(absence, absenceType, absencedays, startdate, enddate);
      }
      if (leavelist != null && leavelist.length() > 0) {
        JSONArray jsonArray = leavelist.getJSONArray("leavelist");
        if (jsonArray.length() > 0) {
          for (int i = 0; i < jsonArray.length(); i++) {
            json = jsonArray.getJSONObject(i);
            EHCMEmpLeaveln leaveln = OBProvider.getInstance().get(EHCMEmpLeaveln.class);
            leaveln.setClient(absence.getClient());
            leaveln.setOrganization(absence.getOrganization());
            leaveln.setEnabled(absence.isEnabled());
            leaveln.setCreationDate(new java.util.Date());
            leaveln.setEnabled(true);
            leaveln
                .setCreatedBy(OBDal.getInstance().get(User.class, absence.getCreatedBy().getId()));
            leaveln.setUpdated(new java.util.Date());
            leaveln
                .setUpdatedBy(OBDal.getInstance().get(User.class, absence.getUpdatedBy().getId()));
            leaveln
                .setEhcmEmpLeave(OBDal.getInstance().get(EHCMEmpLeave.class, json.getString("id")));
            leaveln.setLeavedays(new BigDecimal(json.getString("days")));
            leaveln.setStartDate(yearFormat.parse(json.getString("startdate")));
            leaveln.setEndDate(yearFormat.parse(json.getString("enddate")));
            leaveln.setLeaveAction(absence.getDecisionType());
            leaveln.setEhcmAbsenceAttendance(absence);
            leaveln.setLeaveType(Constants.EMPLEAVE_ABSENCE);
            OBDal.getInstance().save(leaveln);
          }
        }
      }

      // OBDal.getInstance().flush();

      /* update leave header table */
      // updateEmpLeaveHeader(header, absence, absencedays);
      // OBDal.getInstance().flush();
      count = 1;
    } catch (final Exception e) {
      log.error("Exception in insertEmpLeaveLine" + e);
      count = 0;
    }
    return count;
  }

  public void updateEmpLeaveHeader(EHCMEmpLeave header, EHCMAbsenceAttendance absence,
      BigDecimal absencedays) {
    try {
      if (header != null) {
        header.setAvailabledays(header.getAvailabledays().subtract(absencedays));
        log.debug("getAvaileddays:" + header.getAvaileddays());
        header.setAvaileddays(header.getAvaileddays().add(absencedays));
        log.debug("getAbsenceDays:" + absence.getAbsenceDays());
        log.debug("leave:" + header.getAvaileddays());
        OBDal.getInstance().save(header);
      }
    } catch (final Exception e) {
      log.error("Exception in updateEmpLeaveHeader" + e);
    }
  }

  /**
   * insert emp leave block
   * 
   * @param conn
   * @param absence
   * @param startdate
   * @param accrualId
   * @return
   */
  public EHCMEmpLeaveBlock insertEmpLeaveBlocK(Connection conn, EHCMAbsenceAttendance absence,
      Date startdate, String accrualId) {
    EHCMEmpLeaveBlock leaveblk = null;
    String hijiAbsStartDate = null;
    String hijiFiveYrEndDate = null;
    String GregFiveYrEndDate = null;
    Date endDate = null;
    try {
      EHCMAbsenceTypeAccruals acccrual = OBDal.getInstance().get(EHCMAbsenceTypeAccruals.class,
          accrualId);
      log.debug("acccrual:" + acccrual.getDays());
      leaveblk = OBProvider.getInstance().get(EHCMEmpLeaveBlock.class);
      leaveblk.setClient(absence.getClient());
      leaveblk.setOrganization(absence.getOrganization());
      leaveblk.setEnabled(absence.isEnabled());
      leaveblk.setCreationDate(new java.util.Date());
      leaveblk.setCreatedBy(OBDal.getInstance().get(User.class, absence.getCreatedBy().getId()));
      leaveblk.setUpdated(new java.util.Date());
      leaveblk.setUpdatedBy(OBDal.getInstance().get(User.class, absence.getUpdatedBy().getId()));
      leaveblk.setEhcmEmpPerinfo(absence.getEhcmEmpPerinfo());
      leaveblk.setAbsenceType(absence.getEhcmAbsenceType());
      leaveblk.setAccrualdays(acccrual.getDays());
      leaveblk.setStartDate(absence.getStartDate());
      log.debug("getStartDate:" + leaveblk.getStartDate());

      // calculate the 5 years enddate
      // form start date to 5 years date as enddate
      hijiAbsStartDate = UtilityDAO.convertTohijriDate(yearFormat.format(leaveblk.getStartDate()));
      hijiFiveYrEndDate = hijiAbsStartDate.split("-")[0] + "-" + hijiAbsStartDate.split("-")[1]
          + "-" + ((Integer.valueOf(hijiAbsStartDate.split("-")[2]) + 5));
      GregFiveYrEndDate = UtilityDAO.convertToGregorian(hijiFiveYrEndDate);
      log.debug("hijirienddate:" + GregFiveYrEndDate);
      endDate = getMaxEndDate(absence.getStartDate(), yearFormat.parse(GregFiveYrEndDate));
      if (endDate != null) {
        leaveblk.setEndDate(endDate);
      }
      OBDal.getInstance().save(leaveblk);
      // OBDal.getInstance().flush();

    } catch (final Exception e) {
      log.error("Exception in insertEmpLeaveBlocK" + e);
    }
    return leaveblk;
  }

  /**
   * GET max end date
   * 
   * @param startDate
   * @param endDate
   * @return
   */
  @SuppressWarnings("unchecked")
  public Date getMaxEndDate(Date startDate, Date enddate) {
    String sql = null;
    Query qry = null;
    Date endDate = null;
    try {
      endDate = enddate;
      sql = " select max(e.hijri_date) ,e.gregorian_date from eut_hijri_dates e where gregorian_date >=:startdate "
          + " and gregorian_date <:enddate group by e.gregorian_date order by e.gregorian_date desc ";
      qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      qry.setParameter("startdate", startDate);
      qry.setParameter("enddate", endDate);
      if (qry != null && qry.list().size() > 0) {
        List<Object> object = qry.list();
        Object[] row = (Object[]) object.get(0);
        endDate = (Date) row[1];
        return endDate;
      }
    } catch (final Exception e) {
      log.error("Exception in getMaxEndDate" + e);
    }
    return endDate;
  }

  public int updateEmpLeaveBlock(EHCMEmpLeaveBlock header, EHCMAbsenceAttendance absence,
      Date startdate, Date enddate) {
    int count = 0;
    String hijistrtDate = null;
    String hijiEndDate = null;
    try {
      header.setStartDate(startdate);
      log.debug("startdate :" + header.getStartDate());

      hijistrtDate = UtilityDAO.eventConvertTohijriDate(yearFormat.format(header.getStartDate()));
      hijiEndDate = hijistrtDate.split("-")[0] + "-" + hijistrtDate.split("-")[1] + "-"
          + (Integer.valueOf(hijistrtDate.split("-")[2])
              + Integer.valueOf(absence.getEhcmAbsenceType().getFrequency()));

      if (StringUtils.isNotEmpty(hijiEndDate)) {
        hijiEndDate = yearFormat.format(
            dateFormat.parse(sa.elm.ob.hcm.util.UtilityDAO.getBeforeDateInGreg(hijiEndDate)));
      }

      header.setEndDate(yearFormat.parse(hijiEndDate));

      log.debug("lev blokc line :" + header.getId());
      OBDal.getInstance().save(header);
      // OBDal.getInstance().flush();
    } catch (final Exception e) {
      log.error("Exception in updateEmpLeaveBlock" + e);
    }
    return count;
  }

  public int insertEmpLeaveBlocKLine(EHCMEmpLeaveBlock header, EHCMAbsenceAttendance absence,
      BigDecimal absencedays, Date startdate, Date enddate) {
    int count = 0;
    try {
      EHCMEMPLeaveBlockLn lvblkln = OBProvider.getInstance().get(EHCMEMPLeaveBlockLn.class);
      lvblkln.setClient(absence.getClient());
      lvblkln.setOrganization(absence.getOrganization());
      lvblkln.setCreationDate(new java.util.Date());
      lvblkln.setCreatedBy(absence.getCreatedBy());
      lvblkln.setUpdated(new java.util.Date());
      lvblkln.setUpdatedBy(absence.getUpdatedBy());
      lvblkln.setEnabled(true);
      lvblkln.setEhcmAbsenceAttendance(absence);
      lvblkln.setEhcmEmpLeaveblock(header);
      lvblkln.setLeavedays(absencedays);
      lvblkln.setStartDate(startdate);
      lvblkln.setEndDate(enddate);
      log.debug("lev blokc line :" + header.getId());
      OBDal.getInstance().save(lvblkln);
      count = 1;
    } catch (final Exception e) {
      log.error("Exception in insertEmpLeaveBlocKLine" + e);
    }
    return count;
  }

  public int deductedLeave(EHCMAbsenceType absencetype, Connection con,
      EHCMAbsenceAttendance absence, EHCMEmpLeave leave, EHCMAbsenceTypeAccruals accrual,
      EHCMAbsenceAttendance cancelAbsence) {
    int count = 0;
    BigDecimal availabledays = BigDecimal.ZERO;
    BigDecimal days = BigDecimal.ZERO;
    BigDecimal difference = BigDecimal.ZERO;
    Date dependentLevEndDate = null;
    Date dateafter = null;
    int oneMiliSeconds = 1 * 24 * 3600 * 1000;
    String sql = "";
    try {

      if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
          || absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)
          || absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
          || (cancelAbsence != null && (absence.getDecisionType()
              .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
              || absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)))) {
        if (cancelAbsence != null) {
          deleteEmpLeaveLnForDeductedLeave(cancelAbsence);
        } else {
          deleteEmpLeaveLnForDeductedLeave(absence);
        }
      }

      // get availabledays (unused days)

      availabledays = getAvailableAndAvaileddays(con, absence, absencetype,
          yearFormat.format(absence.getStartDate()), null, false, cancelAbsence);

      log.debug("availableday:" + availabledays);
      // get applied leave days
      days = absence.getAbsenceDays();

      if (days.compareTo(availabledays) <= 0) {
        availabledays = days;
      }

      // find out the dependent leave end date based on availabledays & startdate ,
      // remaining days
      // will added on original absence leave type
      if (availabledays.compareTo(BigDecimal.ZERO) > 0) {
        if (absencetype.isInculdeholiday()) {
          sql = " and coalesce(cal.holiday_type,'WD') not in ('WE1','WE2') ";
        } else {
          sql = " and coalesce(cal.holiday_type,'WD') not in ('WE1','WE2','NH','AD','FE') ";
        }

        SQLQuery Query = OBDal.getInstance().getSession().createSQLQuery(
            " select max(a.gregorian_date) as endofdate from ( select gregorian_date "
                + " from eut_hijri_dates    left join ehcm_holiday_calendar cal on cal.holidaydate= eut_hijri_dates.gregorian_date   "
                + "  and (cal.ad_client_id=:clientId or cal.ad_client_id is null)"
                + "  where gregorian_date >=:startDate " + sql
                + "  group by gregorian_date order by gregorian_date asc  limit :limitno ) a ");
        Query.setParameter("clientId", absence.getClient().getId());
        Query.setParameter("startDate", absence.getStartDate());
        Query.setParameter("limitno", availabledays);
        log.debug("Query2:" + Query.toString());
        if (Query.list().size() > 0) {
          Object row = (Object) Query.list().get(0);
          dependentLevEndDate = (Date) row;
        }
      }

      if (dependentLevEndDate != null) {
        if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
          insertLeaveOccuranceEmpLeave(absence, absencetype, accrual, absence.getStartDate(),
              dependentLevEndDate);
        }
        insertEmpLeaveLine(null, absencetype, absence, availabledays, absence.getStartDate(),
            dependentLevEndDate);
        dateafter = new Date(dependentLevEndDate.getTime() + oneMiliSeconds);
      } else {
        dateafter = absence.getStartDate();
      }

      difference = absence.getAbsenceDays().subtract(availabledays);
      if (difference.compareTo(BigDecimal.ZERO) > 0) {
        // deducted leave
        // check deducted leave persented in empleave table or not
        if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
          insertLeaveOccuranceEmpLeave(absence, absence.getEhcmAbsenceType(), accrual, dateafter,
              absence.getEndDate());// deducted leave
        }
        insertEmpLeaveLine(null, absence.getEhcmAbsenceType(), absence, difference, dateafter,
            absence.getEndDate());
      }
    } catch (final Exception e) {
      log.error("Exception in insertEmpLeaveBlocKLine" + e);
    }
    return count;
  }

  public String chkexceptionleaveval(Connection con, EHCMAbsenceAttendance absence,
      String absencetypeAccral) {
    String message = "";
    BigInteger days = BigInteger.ZERO;
    EHCMAbsenceTypeAccruals acccrual = null;
    List<EHCMEMPLeaveBlockLn> lvblklnList = new ArrayList<EHCMEMPLeaveBlockLn>();
    List<EHCMEmpLeaveBlock> alreadyLevblokList = new ArrayList<EHCMEmpLeaveBlock>();
    List<EHCMEmpLeaveBlock> startDateLvblkList = new ArrayList<EHCMEmpLeaveBlock>();
    List<EHCMEmpLeaveBlock> enddateblockList = new ArrayList<EHCMEmpLeaveBlock>();
    EHCMEmpLeaveBlock strtdateblk = null;
    EHCMEmpLeaveBlock leaveblk = null;
    String hql = "";

    try {

      hql = "  e.enabled='Y' and  e.ehcmEmpPerinfo.id=:employeeId and e.absenceType.id=:absenceTypeId ";

      if (absence.getSubtype() != null) {
        hql += " and e.subtype.id=:subTypeId ";
      }

      if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
        // chk already leave block exists for particular absence startdate and enddate
        OBQuery<EHCMEmpLeaveBlock> alreadyLevblokQry = OBDal.getInstance()
            .createQuery(EHCMEmpLeaveBlock.class, " as e where " + hql
                + " and ((to_date(:startDate,'yyyy-MM-dd') between TO_DATE(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') "
                + "  and  TO_DATE(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy')) "
                + " and (to_date(:endDate,'yyyy-MM-dd') between TO_DATE(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy')  "
                + " and  TO_DATE(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy')))");
        alreadyLevblokQry.setNamedParameter("employeeId", absence.getEhcmEmpPerinfo().getId());
        alreadyLevblokQry.setNamedParameter("absenceTypeId", absence.getEhcmAbsenceType().getId());
        if (absence.getSubtype() != null) {
          alreadyLevblokQry.setNamedParameter("subTypeId", absence.getSubtype().getId());
        }
        alreadyLevblokQry.setNamedParameter("startDate", absence.getStartDate());
        alreadyLevblokQry.setNamedParameter("endDate", absence.getEndDate());
        alreadyLevblokList = alreadyLevblokQry.list();
        // if count is more than two then throw error if two different block is present
        // for
        // corresponding leaave startdate and enddate

        if (alreadyLevblokList.size() > 1) {
          OBDal.getInstance().rollbackAndClose();
          message = "@EHCM_AbsExcLevDiffBlock@";
          return message;
        }

        // if count is one then use the same block for update the availeddays
        else if (alreadyLevblokList.size() == 1) {
          leaveblk = alreadyLevblokList.get(0);
        }

        // else create a record in employee leave block
        else {

          // need to chk startdate only present any block if present means update the
          // corresponding
          // leave balance in that block

          OBQuery<EHCMEmpLeaveBlock> startDateLevblkQry = OBDal.getInstance()
              .createQuery(EHCMEmpLeaveBlock.class, " as e where  " + hql
                  + "  and to_date(:startDate,'yyyy-MM-dd') between TO_DATE(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') "
                  + " and  TO_DATE(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy') ");

          startDateLevblkQry.setNamedParameter("employeeId", absence.getEhcmEmpPerinfo().getId());
          startDateLevblkQry.setNamedParameter("absenceTypeId",
              absence.getEhcmAbsenceType().getId());
          if (absence.getSubtype() != null) {
            startDateLevblkQry.setNamedParameter("subTypeId", absence.getSubtype().getId());
          }
          startDateLevblkQry.setNamedParameter("startDate", absence.getStartDate());
          startDateLvblkList = startDateLevblkQry.list();
          if (startDateLvblkList.size() > 0) {
            strtdateblk = startDateLvblkList.get(0);
          }

          // chk if enddate is present in any block throw the error
          OBQuery<EHCMEmpLeaveBlock> endDateBlockQry = OBDal.getInstance()
              .createQuery(EHCMEmpLeaveBlock.class, " as e where  " + hql
                  + "  and (TO_DATE(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= :endDate ) ");
          endDateBlockQry.setNamedParameter("employeeId", absence.getEhcmEmpPerinfo().getId());
          endDateBlockQry.setNamedParameter("absenceTypeId", absence.getEhcmAbsenceType().getId());
          if (absence.getSubtype() != null) {
            endDateBlockQry.setNamedParameter("subTypeId", absence.getSubtype().getId());
          }
          endDateBlockQry.setNamedParameter("endDate", absence.getEndDate());
          enddateblockList = endDateBlockQry.list();
          if (enddateblockList.size() > 0) {
            OBDal.getInstance().rollbackAndClose();
            message = "@EHCM_AbsExcLevDiffBlock@";
            return message;
          }
        }
      }
      if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
          || absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)
          || absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
          || absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {

        OBQuery<EHCMEMPLeaveBlockLn> lvblklnQry = OBDal.getInstance().createQuery(
            EHCMEMPLeaveBlockLn.class, " ehcmAbsenceAttendance.id=:originalDecisionNoId");
        lvblklnQry.setNamedParameter("originalDecisionNoId",
            absence.getOriginalDecisionNo().getId());
        lvblklnList = lvblklnQry.list();

        if (lvblklnList.size() > 0) {
          for (EHCMEMPLeaveBlockLn line : lvblklnList) {
            OBDal.getInstance().remove(line);
          }
        }
      }

      if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {

        // if count is one then use the same block for update the availeddays
        if (leaveblk != null) {
          if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
              || absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)
              || absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
            if (absence.getOriginalDecisionNo() != null && absence.getOriginalDecisionNo()
                .getStartDate().compareTo(absence.getStartDate()) != 0) {
              // update the leave block header
              updateEmpLeaveBlock(leaveblk, absence, absence.getStartDate(), absence.getEndDate());
            }
          }

          insertEmpLeaveBlocKLine(leaveblk, absence, absence.getAbsenceDays(),
              absence.getStartDate(), absence.getEndDate());
        }
        // else create a record in employee leave block
        else {

          if (strtdateblk != null) {
            days = calculatedays(yearFormat.format(absence.getStartDate()),
                yearFormat.format(strtdateblk.getEndDate()), absence.getClient().getId());

            insertEmpLeaveBlocKLine(strtdateblk, absence, new BigDecimal(days),
                absence.getStartDate(), strtdateblk.getEndDate());
          }
          if (StringUtils.isNotEmpty(absencetypeAccral)) { // because of sick leave
            acccrual = OBDal.getInstance().get(EHCMAbsenceTypeAccruals.class, absencetypeAccral);
          }

          leaveblk = insertEmpLeaveBlock(absence, acccrual, startDateLvblkList);

          if (startDateLvblkList.size() == 0) {
            insertEmpLeaveBlocKLine(leaveblk, absence, absence.getAbsenceDays(),
                absence.getStartDate(), absence.getEndDate());
          }
          // remaining days need to create a new emp leave block
          else {
            insertEmpLeaveBlocKLine(leaveblk, absence,
                absence.getAbsenceDays().subtract(new BigDecimal(days)), leaveblk.getStartDate(),
                absence.getEndDate());
          }

        }
      }
      // OBDal.getInstance().flush();
      message = "Success";
    } catch (final Exception e) {
      e.printStackTrace();
      log.error("Exception in chkexceptionleaveval() Method : ", e);
    }
    return message;

  }

  public String existingchkexceptionleaveval(Connection con, EHCMAbsenceAttendance absence,
      String absencetypeAccral) {
    String message = "";
    BigInteger days = BigInteger.ZERO;
    PreparedStatement ps = null;
    ResultSet rs = null;
    Boolean deleteflag = false;
    EHCMEmpLeaveBlock block = null;
    try {
      if (absence.getDecisionType().equals("UP") || absence.getDecisionType().equals("CO")
          || absence.getDecisionType().equals("CA")) {
        OBQuery<EHCMEMPLeaveBlockLn> lvblkln = OBDal.getInstance().createQuery(
            EHCMEMPLeaveBlockLn.class,
            " ehcmAbsenceAttendance.id='" + absence.getOriginalDecisionNo().getId() + "'");
        log.debug("lvblkln up" + lvblkln.list().size());
        if (lvblkln.list().size() > 0) {
          for (EHCMEMPLeaveBlockLn line : lvblkln.list()) {
            line.setEnabled(false);
            OBDal.getInstance().save(line);
            OBDal.getInstance().flush();
            if (absence.getDecisionType().equals("UP") || absence.getDecisionType().equals("CO")) {
              log.debug("line.getLeavedays(). up:" + line.getLeavedays());
              log.debug("line before up:" + line.getEhcmEmpLeaveblock().getAvaileddays());
              if (line.getLeavedays()
                  .compareTo(line.getEhcmEmpLeaveblock().getAvaileddays()) == 0) {
                line.getEhcmEmpLeaveblock().setEnabled(false);
                OBDal.getInstance().save(line.getEhcmEmpLeaveblock());
                OBDal.getInstance().flush();
              } else {
                line.getEhcmEmpLeaveblock().setAvaileddays(
                    line.getEhcmEmpLeaveblock().getAvaileddays().subtract(line.getLeavedays()));
                OBDal.getInstance().save(line.getEhcmEmpLeaveblock());
                OBDal.getInstance().flush();
              }
              log.debug("line.getAvaileddays().  after up:"
                  + line.getEhcmEmpLeaveblock().getAvaileddays());
            }
          }
        }
      }
      if (absence.getDecisionType().equals("CA")) {
        OBQuery<EHCMEMPLeaveBlockLn> lvblkln1 = OBDal.getInstance()
            .createQuery(EHCMEMPLeaveBlockLn.class, " ehcmAbsenceAttendance.id='"
                + absence.getOriginalDecisionNo().getId() + "' order by creationDate asc");
        log.debug("size:" + lvblkln1.list().size());
        if (lvblkln1.list().size() > 0) {
          for (EHCMEMPLeaveBlockLn line : lvblkln1.list()) {
            EHCMEMPLeaveBlockLn levblockln = line;
            block = levblockln.getEhcmEmpLeaveblock();
            log.debug("deleteflag 12:" + levblockln.getEhcmEmpLeaveblock().getAvaileddays());
            log.debug("deleteflag 123:" + levblockln.getLeavedays());
            if (levblockln.getEhcmEmpLeaveblock().getAvaileddays()
                .compareTo(levblockln.getLeavedays()) == 0) {
              log.debug("deleteflag:" + deleteflag);
              deleteflag = true;
            }
            OBDal.getInstance().remove(line);
            OBDal.getInstance().flush();
            log.debug("getAvaileddays blk:" + block.getAvaileddays());
            OBDal.getInstance().refresh(block);
            log.debug("getAvaileddays after:" + block.getAvaileddays());
            if (deleteflag) {
              OBDal.getInstance().remove(block);
              OBDal.getInstance().flush();
              log.debug("completed:");
            }
          }
        }
        if (absence.getOriginalDecisionNo().getOriginalDecisionNo() != null) {
          OBQuery<EHCMEMPLeaveBlockLn> lvblkln = OBDal.getInstance()
              .createQuery(EHCMEMPLeaveBlockLn.class, " ehcmAbsenceAttendance.id='"
                  + absence.getOriginalDecisionNo().getOriginalDecisionNo().getId() + "'");
          log.debug("lvblkln ca:" + lvblkln.list().size());
          if (lvblkln.list().size() > 0) {
            for (EHCMEMPLeaveBlockLn line : lvblkln.list()) {
              log.debug("line can:" + line.getLeavedays());
              line.setEnabled(true);
              OBDal.getInstance().save(line);
              OBDal.getInstance().flush();
              log.debug(
                  "line getEhcmEmpLeaveblock:" + line.getEhcmEmpLeaveblock().getAvaileddays());
              if (line.getLeavedays()
                  .compareTo(line.getEhcmEmpLeaveblock().getAvaileddays()) == 0) {
                line.getEhcmEmpLeaveblock().setEnabled(true);
              } else {
                line.getEhcmEmpLeaveblock().setAvaileddays(
                    line.getEhcmEmpLeaveblock().getAvaileddays().add(line.getLeavedays()));
                line.getEhcmEmpLeaveblock().setEnabled(true);
              }
              log.debug("line getEhcmEmpLeaveblock after:"
                  + line.getEhcmEmpLeaveblock().getAvaileddays());
              OBDal.getInstance().save(line.getEhcmEmpLeaveblock());
              OBDal.getInstance().flush();
            }
          }
        }

      }
      if (!absence.getDecisionType().equals("CA")) {
        // chk already leave block exists for particular absence startdate and enddate
        OBQuery<EHCMEmpLeaveBlock> levblok = OBDal.getInstance().createQuery(
            EHCMEmpLeaveBlock.class,
            " as e where e.enabled='Y' and  e.ehcmEmpPerinfo.id='"
                + absence.getEhcmEmpPerinfo().getId() + "' and e.absenceType.id='"
                + absence.getEhcmAbsenceType().getId()

                + "'  and (('" + absence.getStartDate()
                + "' between TO_DATE(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy')  and  TO_DATE(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy')) and  '"
                + absence.getEndDate()
                + "' between TO_DATE(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy')  and  TO_DATE(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy'))");
        /*
         * if count is more than two then throw error if two different block is present for
         * corresponding leaave startdate and enddate
         */
        log.debug(" firstchk :" + levblok.getWhereAndOrderBy());
        log.debug("levblok:" + levblok.list().size());
        if (levblok.list().size() > 1) {
          OBDal.getInstance().rollbackAndClose();
          message = "@EHCM_AbsExcLevDiffBlock@";
          return message;
        }
        // if count is one then use the same block for update the availeddays
        else if (levblok.list().size() == 1) {
          log.debug("getAbsenceDays size 1:" + absence.getAbsenceDays());
          EHCMEmpLeaveBlock levblk = levblok.list().get(0);
          insertEmpLeaveBlocKLine(levblk, absence, absence.getAbsenceDays(), absence.getStartDate(),
              absence.getEndDate());
          OBDal.getInstance().flush();
        }
        // else create a record in employee leave block
        else {
          /*
           * need to chk startdate only present any block if present means update the corresponding
           * leave balance in that block
           */
          OBQuery<EHCMEmpLeaveBlock> startdatelvblk = OBDal.getInstance().createQuery(
              EHCMEmpLeaveBlock.class,
              " as e where   e.enabled='Y' and  e.ehcmEmpPerinfo.id='"
                  + absence.getEhcmEmpPerinfo().getId() + "' and e.absenceType.id='"
                  + absence.getEhcmAbsenceType().getId() + "' and '" + absence.getStartDate()
                  + "' between TO_DATE(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') and  TO_DATE(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy') ");
          EHCMEmpLeaveBlock strtdateblk = null;
          log.debug("getWhereAndOrderBy startdatelvblk :" + startdatelvblk.getWhereAndOrderBy());
          log.debug("startdatelvblk days :" + startdatelvblk.list().size());
          if (startdatelvblk.list().size() > 0) {
            strtdateblk = startdatelvblk.list().get(0);
            /*
             * get a days difference between absence startdate to leave block enddate and update
             * already existed leave block
             */
            log.debug("startdatelvblk days :" + yearFormat.format(absence.getStartDate()) + "---"
                + yearFormat.format(strtdateblk.getEndDate()));
            days = calculatedays(yearFormat.format(absence.getStartDate()),
                yearFormat.format(strtdateblk.getEndDate()), absence.getClient().getId());
            log.debug("startdatelvblk days :" + days);

            insertEmpLeaveBlocKLine(strtdateblk, absence, new BigDecimal(days),
                absence.getStartDate(), strtdateblk.getEndDate());
          }
          // chk if enddate is present in any block throw the error
          OBQuery<EHCMEmpLeaveBlock> enddateblock = OBDal.getInstance().createQuery(
              EHCMEmpLeaveBlock.class,
              " as e where   e.enabled='Y' and e.ehcmEmpPerinfo.id='"
                  + absence.getEhcmEmpPerinfo().getId() + "' and e.absenceType.id='"
                  + absence.getEhcmAbsenceType().getId()
                  + "' and (TO_DATE(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= '"
                  + absence.getEndDate() + "') ");
          log.debug("getWhereAndOrderBy enddateblock :" + enddateblock.getWhereAndOrderBy());
          log.debug("enddateblock size :" + enddateblock.list().size());
          if (enddateblock.list().size() > 0) {
            OBDal.getInstance().rollbackAndClose();
            message = "@EHCM_AbsExcLevDiffBlock@";
            return message;
          }
          OBDal.getInstance().flush();

          // remaining days need to create a new emp leave block
          if (StringUtils.isNotEmpty(absencetypeAccral)) {
            EHCMAbsenceTypeAccruals acccrual = OBDal.getInstance()
                .get(EHCMAbsenceTypeAccruals.class, absencetypeAccral);
            EHCMEmpLeaveBlock leaveblk = OBProvider.getInstance().get(EHCMEmpLeaveBlock.class);
            leaveblk.setClient(absence.getClient());
            leaveblk.setOrganization(absence.getOrganization());
            leaveblk.setEnabled(absence.isEnabled());
            leaveblk.setCreationDate(new java.util.Date());
            leaveblk
                .setCreatedBy(OBDal.getInstance().get(User.class, absence.getCreatedBy().getId()));
            leaveblk.setUpdated(new java.util.Date());
            leaveblk
                .setUpdatedBy(OBDal.getInstance().get(User.class, absence.getUpdatedBy().getId()));
            leaveblk.setEhcmEmpPerinfo(absence.getEhcmEmpPerinfo());
            leaveblk.setAbsenceType(absence.getEhcmAbsenceType());
            leaveblk.setAccrualdays(acccrual.getDays());

            if (startdatelvblk.list().size() == 0)// && preyearBlock.list().size() == 0
              leaveblk.setStartDate(absence.getStartDate());

            else if (startdatelvblk.list().size() > 0) {
              log.debug("enddate:" + strtdateblk.getEndDate());
              Date dateafter = new Date(strtdateblk.getEndDate().getTime() + 1 * 24 * 3600 * 1000);
              leaveblk.setStartDate(dateafter);
              log.debug("dateafter:" + dateafter);
            }

            // to calculate the 5 years enddate form start date to 5 years date as enddate
            String hijistrtDate = UtilityDAO
                .convertTohijriDate(yearFormat.format(leaveblk.getStartDate()));
            log.debug("startdatelvblk day:" + days);
            String enddate = hijistrtDate.split("-")[0] + "-" + hijistrtDate.split("-")[1] + "-"
                + ((Integer.valueOf(hijistrtDate.split("-")[2]) + 5));
            String hijirienddate = UtilityDAO.convertToGregorian(enddate);

            if (StringUtils.isEmpty(hijirienddate)) {
              hijirienddate = sa.elm.ob.hcm.util.UtilityDAO.getBeforeDateInGreg(enddate);
            }

            ps = con.prepareStatement(
                " select to_char(max(gregorian_date),'yyyy-mm-dd') as endofdate from eut_hijri_dates where gregorian_date >= '"
                    + absence.getStartDate() + "' and gregorian_date <'" + hijirienddate + "'");
            log.debug("enddate:" + ps.toString());
            rs = ps.executeQuery();
            if (rs.next()) {
              leaveblk.setEndDate(rs.getDate("endofdate"));
            }
            OBDal.getInstance().save(leaveblk);
            OBDal.getInstance().flush();
            if (startdatelvblk.list().size() == 0)
              insertEmpLeaveBlocKLine(leaveblk, absence, absence.getAbsenceDays(),
                  absence.getStartDate(), absence.getEndDate());
            else
              insertEmpLeaveBlocKLine(leaveblk, absence,
                  absence.getAbsenceDays().subtract(new BigDecimal(days)), leaveblk.getStartDate(),
                  absence.getEndDate());
            OBDal.getInstance().flush();
          }
        }
        message = "Success";
      }
    } catch (final Exception e) {
      e.printStackTrace();
      log.error("Exception in chkexceptionleaveval() Method : ", e);
    }
    return message;

  }

  public void updateAbsenceEnableFlag(EHCMAbsenceAttendance absence, boolean enableFlag) {
    try {
      absence.setEnabled(enableFlag);
      OBDal.getInstance().save(absence);
    }

    catch (final Exception e) {
      log.error("Exception in updateAbsenceEnableFlag() Method : ", e);
    }
  }

  public EHCMEmpLeaveBlock insertEmpLeaveBlock(EHCMAbsenceAttendance absence,
      EHCMAbsenceTypeAccruals accrual, List<EHCMEmpLeaveBlock> startDateLvblkList) {
    EHCMEmpLeaveBlock strtdateblk = null;
    int onemilliSeconds = 1 * 24 * 3600 * 1000;
    BigInteger days = BigInteger.ZERO;
    EHCMEmpLeaveBlock leaveblk = null;
    String hijirienddate = null;
    try {
      if (startDateLvblkList.size() > 0) {
        strtdateblk = startDateLvblkList.get(0);
      }

      leaveblk = OBProvider.getInstance().get(EHCMEmpLeaveBlock.class);
      leaveblk.setClient(absence.getClient());
      leaveblk.setOrganization(absence.getOrganization());
      leaveblk.setEnabled(absence.isEnabled());
      leaveblk.setCreationDate(new java.util.Date());
      leaveblk.setCreatedBy(OBDal.getInstance().get(User.class, absence.getCreatedBy().getId()));
      leaveblk.setUpdated(new java.util.Date());
      leaveblk.setUpdatedBy(OBDal.getInstance().get(User.class, absence.getUpdatedBy().getId()));
      leaveblk.setEhcmEmpPerinfo(absence.getEhcmEmpPerinfo());
      leaveblk.setAbsenceType(absence.getEhcmAbsenceType());
      leaveblk.setAccrualdays(accrual == null ? new BigDecimal(0) : accrual.getDays());
      if (absence.getSubtype() != null) {
        leaveblk.setSubtype(absence.getSubtype());
      }

      if (startDateLvblkList.size() == 0)// && preyearBlock.list().size() == 0
        leaveblk.setStartDate(absence.getStartDate());

      else if (startDateLvblkList.size() > 0) {
        Date dateafter = new Date(strtdateblk.getEndDate().getTime() + onemilliSeconds);
        leaveblk.setStartDate(dateafter);
        log.debug("dateafter:" + dateafter);
      }

      // to calculate the 5 years enddate form start date to 5 years date as enddate
      String hijistrtDate = UtilityDAO
          .convertTohijriDate(yearFormat.format(leaveblk.getStartDate()));
      log.debug("startdatelvblk day:" + days + "-" + absence.getEhcmAbsenceType().getFrequency());
      String enddate = hijistrtDate.split("-")[0] + "-" + hijistrtDate.split("-")[1] + "-"
          + ((Integer.valueOf(hijistrtDate.split("-")[2])
              + Integer.valueOf(absence.getEhcmAbsenceType().getFrequency())));
      // String hijirienddate = UtilityDAO.convertToGregorian(enddate);

      log.debug("enddate:" + sa.elm.ob.hcm.util.UtilityDAO.getBeforeDateInGreg(enddate));
      if (StringUtils.isNotEmpty(enddate)) {
        hijirienddate = yearFormat
            .format(dateFormat.parse(sa.elm.ob.hcm.util.UtilityDAO.getBeforeDateInGreg(enddate)));
      }
      leaveblk.setEndDate(yearFormat.parse(hijirienddate));

      OBDal.getInstance().save(leaveblk);
    }

    catch (final Exception e) {
      log.error("Exception in updateAbsenceEnableFlag() Method : ", e);
    }
    return leaveblk;
  }

  public void cancelEmpLeave(EHCMAbsenceAttendance absence, EHCMAbsenceType absencetype,
      Connection con, EHCMAbsenceTypeAccruals accrual, EHCMEmpLeave leave) {
    List<EHCMEmpLeaveln> empLeaveLnList = new ArrayList<EHCMEmpLeaveln>();
    try {

      OBQuery<EHCMEmpLeaveln> ln = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class,
          " ehcmAbsenceAttendance.id=:originalDecisionNo ");
      ln.setNamedParameter("originalDecisionNo", absence.getOriginalDecisionNo().getId());
      empLeaveLnList = ln.list();
      log.debug("size:" + ln.list().size());

      if (absence.getEhcmAbsenceType().isDeducted()) {
        /*
         * block cancel if (absence.getOriginalDecisionNo().getDecisionType().equals("CR") ||
         * absence.getOriginalDecisionNo().getDecisionType().equals("EX") ||
         * absence.getOriginalDecisionNo().getDecisionType().equals("UP")) {
         */
        if (empLeaveLnList.size() > 0) {
          for (EHCMEmpLeaveln empleaveln : empLeaveLnList) {
            /* update the emp table */
            OBDal.getInstance().remove(empleaveln);
          }
        }
        // }

        /*
         * block cancel else if (absence.getOriginalDecisionNo().getDecisionType().equals("CO")) {//
         * || // absence.getOriginalDecisionNo().getDecisionType().equals("UP")
         * deductedLeave(absencetype, con, absence.getOriginalDecisionNo().getOriginalDecisionNo(),
         * leave, accrual, absence); }
         */
      } else {
        if (empLeaveLnList.size() > 0) {
          for (EHCMEmpLeaveln empleaveln : empLeaveLnList) {
            /* update the emp table */
            OBDal.getInstance().remove(empleaveln);
          }
        }
        /*
         * block cancel if (absence.getOriginalDecisionNo().getDecisionType().equals("CO")) { // ||
         * absence.getOriginalDecisionNo().getDecisionType().equals("UP") insertEmpLeaveLine(null,
         * absence.getEhcmAbsenceType(), absence.getOriginalDecisionNo().getOriginalDecisionNo(),
         * absence.getOriginalDecisionNo().getOriginalDecisionNo().getAbsenceDays(),
         * absence.getOriginalDecisionNo().getOriginalDecisionNo().getStartDate(),
         * absence.getOriginalDecisionNo().getOriginalDecisionNo().getEndDate()); }
         */

        // startdate change
        /*
         * empleaveln.setLeavedays(
         * absence.getOriginalDecisionNo().getOriginalDecisionNo().getAbsenceDays());
         * empleaveln.setEhcmAbsenceAttendance(
         * absence.getOriginalDecisionNo().getOriginalDecisionNo()); empleaveln.setLeaveAction(
         * absence.getOriginalDecisionNo().getOriginalDecisionNo().getDecisionType());
         * empleaveln.setStartDate(absence.getOriginalDecisionNo().getStartDate());
         * empleaveln.setEndDate(absence.getOriginalDecisionNo().getEndDate());
         * OBDal.getInstance().save(empleaveln);
         */
        // startdate change
        // OBDal.getInstance().flush();
        // }
        // }
        // }
      }
    } catch (

    final Exception e) {
      log.error("Exception in cancelEmpLeave() Method : ", e);
    }
  }

  public void deleteEmpLeaveLnForDeductedLeave(EHCMAbsenceAttendance absence) {
    List<EHCMEmpLeaveln> empLeaveLnList = new ArrayList<EHCMEmpLeaveln>();
    try {
      OBQuery<EHCMEmpLeaveln> empLeaveLnQry = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class,
          " as e where e.ehcmAbsenceAttendance.id=:originalDecisionNo ");
      empLeaveLnQry.setNamedParameter("originalDecisionNo",
          absence.getOriginalDecisionNo().getId());
      empLeaveLnList = empLeaveLnQry.list();
      if (empLeaveLnList.size() > 0) {
        for (EHCMEmpLeaveln ln : empLeaveLnList) {
          OBDal.getInstance().remove(ln);
        }
      }
    } catch (final Exception e) {
      log.error("Exception in deleteEmpLeaveLnForDeductedLeave() Method : ", e);
    }
  }

  public void updateEmpLeaveForDeductedLeave(EHCMAbsenceAttendance absence,
      EHCMAbsenceType absenceType, BigDecimal leavedays, Date startDate, Date endDate,
      Boolean isdelete) {
    List<EHCMEmpLeaveln> empLeaveLnList = new ArrayList<EHCMEmpLeaveln>();
    List<EHCMEmpLeave> empLeaveList = new ArrayList<EHCMEmpLeave>();
    try {

      OBQuery<EHCMEmpLeaveln> ln = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class,
          " as e where e.ehcmAbsenceAttendance.id=:originalDecisionNo "
              + " and e.ehcmEmpLeave.id in ( select e.id from EHCM_Emp_Leave e where e.ehcmEmpPerinfo.id=:employeeId and "
              + " e.absenceType.id=:absenceTypeId ) ");
      ln.setNamedParameter("originalDecisionNo", absence.getOriginalDecisionNo().getId());
      ln.setNamedParameter("employeeId", absence.getEhcmEmpPerinfo().getId());
      ln.setNamedParameter("absenceTypeId", absenceType.getId());
      ln.setMaxResult(1);
      log.debug("where:" + ln.getWhereAndOrderBy());
      empLeaveLnList = ln.list();
      if (empLeaveLnList.size() > 0) {
        EHCMEmpLeaveln leaveln = empLeaveLnList.get(0);
        if (!isdelete) {
          leaveln.setUpdated(absence.getUpdated());
          leaveln.setUpdatedBy(absence.getUpdatedBy());
          leaveln.setStartDate(startDate);
          leaveln.setEndDate(endDate);
          if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
            leaveln.setLeaveAction(absence.getDecisionType());
          }
          if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
            leaveln
                .setEhcmAbsenceAttendance(absence.getOriginalDecisionNo().getOriginalDecisionNo());
          } else {
            leaveln.setEhcmAbsenceAttendance(absence);
          }
          leaveln.setLeavedays(leavedays);
          leaveln.setLeaveType(Constants.EMPLEAVE_ABSENCE);
          OBDal.getInstance().save(leaveln);
          // OBDal.getInstance().flush();
        } else {
          OBDal.getInstance().remove(leaveln);
        }
      } else {
        empLeaveList = getEmployeeLeave(absence, absenceType);
        if (empLeaveList.size() > 0) {
          EHCMEmpLeave leave = empLeaveList.get(0);
          insertEmpLeaveLine(leave, absence.getEhcmAbsenceType(), absence, leavedays, startDate,
              endDate);
        }

      }

    } catch (final Exception e) {
      log.error("Exception in updateEmpLeaveForDeductedLeave() Method : ", e);
    }
  }

  public Boolean chkAlreadyDepAssorNot(EHCMAbsenceType absenceType) {
    List<EHCMAbsenceTypeAction> absenceTypeActionList = new ArrayList<EHCMAbsenceTypeAction>();
    try {
      OBQuery<EHCMAbsenceTypeAction> absenceTypeActionQry = OBDal.getInstance()
          .createQuery(EHCMAbsenceTypeAction.class, " as e where e.absenceType.id=:absenceTypeId ");
      absenceTypeActionQry.setNamedParameter("absenceTypeId", absenceType.getId());
      absenceTypeActionQry.setMaxResult(1);
      absenceTypeActionList = absenceTypeActionQry.list();
      if (absenceTypeActionList.size() > 0) {
        return true;
      } else
        return false;

    }

    catch (final Exception e) {
      log.error("Exception in chkAlreadyDepAssorNot() Method : ", e);
    }
    return false;
  }

  public boolean chkAbsenceAccrualExistsOrNot(EHCMAbsenceTypeAccruals currentAccruals) {
    List<EHCMAbsenceTypeAccruals> accrualList = new ArrayList<EHCMAbsenceTypeAccruals>();

    try {

      //
      accrualList = chkAbsenceAccrualExists(currentAccruals, true);
      if (accrualList.size() > 0) {
        return true;
      } else {
        if (currentAccruals.getGradeClassifications() != null) {
          accrualList = chkAbsenceAccrualExists(currentAccruals, false);
          if (accrualList.size() > 0) {
            return true;
          } else {
            return false;
          }
        }
        return false;
      }
      //
      /*
       * accrualList = chkAbsenceAccrualExists(currentAccruals, false); if (accrualList.size() > 0)
       * { if (accrualList.get(0).getGradeClassifications() == null &&
       * currentAccruals.getGradeClassifications() != null) { accrualGradeBlankList =
       * chkAbsenceAccrualExists(currentAccruals, true); if (accrualGradeBlankList.size() > 0) {
       * return true; } else { return true;// false }
       * 
       * } else { return true; } } else { return false; }
       */
      //
    }

    catch (final Exception e) {
      log.error("Exception in chkAbsenceAccrualExistsOrNot() Method : ", e);
      return false;
    }
  }

  public List<EHCMAbsenceTypeAccruals> chkAbsenceAccrualExists(
      EHCMAbsenceTypeAccruals currentAccruals, boolean isblankGrade) {
    String hql = null;
    String fdate = null;
    String tdate = null;
    List<EHCMAbsenceTypeAccruals> accrualList = new ArrayList<EHCMAbsenceTypeAccruals>();
    try {
      fdate = Utility.formatDate(currentAccruals.getStartDate());
      if (currentAccruals.getEndDate() != null) {
        tdate = Utility.formatDate(currentAccruals.getEndDate());

      } else {
        tdate = "21-06-2058";
      }
      if (isblankGrade) {
        hql = "  and e.gradeClassifications.id is null and e.creationDate < :creationDate ";
      }

      else if (!isblankGrade) {
        hql = " and (e.gradeClassifications.id=:gradeclass)";
      }
      hql += " and ((to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
          + " and to_date(to_char(coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + " or (to_date(to_char( coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
          + " and to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')))  ";

      OBQuery<EHCMAbsenceTypeAccruals> accrualQry = OBDal.getInstance().createQuery(
          EHCMAbsenceTypeAccruals.class,
          " as e where e.absenceType.id=:absenceTypeId and e.enabled='Y' and e.id <>:accrualId "
              + hql);
      accrualQry.setNamedParameter("absenceTypeId", currentAccruals.getAbsenceType().getId());
      accrualQry.setNamedParameter("accrualId", currentAccruals.getId());
      if (isblankGrade) {
        accrualQry.setNamedParameter("creationDate", currentAccruals.getCreationDate());
      }
      if (!isblankGrade) {
        accrualQry.setNamedParameter("gradeclass",
            currentAccruals.getGradeClassifications().getId());
      }
      accrualQry.setNamedParameter("fromdate", fdate);
      accrualQry.setNamedParameter("todate", tdate);
      log.debug("accrual:" + accrualQry.getWhereAndOrderBy());
      log.debug("currentAccruals:" + currentAccruals.getCreationDate());
      accrualQry.setMaxResult(1);
      accrualList = accrualQry.list();
      return accrualList;
    }

    catch (final Exception e) {
      log.error("Exception in chkAbsenceAccrualExists() Method : ", e);
      return accrualList;
    }
  }

  public boolean chkPartGradLeaveAlreadyTakenForThatPeriod(Date startdate, Date enddate,
      EHCMAbsenceType absenceType, ehcmgradeclass gradeclass, boolean isStartDateChange,
      boolean isdelete) {
    String hql = "";
    int onemilliSeconds = 1 * 24 * 3600 * 1000;
    List<EHCMEmpLeaveln> empleavelnList = new ArrayList<EHCMEmpLeaveln>();
    try {
      hql += " and ((to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
          + " and to_date(to_char(coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + " or (to_date(to_char( coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
          + " and to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')))  ";

      if (gradeclass != null) {
        hql += " and e.ehcmAbsenceAttendance.id in ( select abs.id from EHCM_Absence_Attendance abs where abs.gradeClassifications.id=:gradeclass) ";
      }
      if (!isdelete) {
        if (isStartDateChange) {
          enddate = new Date(enddate.getTime() - onemilliSeconds);
        } else {
          startdate = new Date(startdate.getTime() + onemilliSeconds);
        }
      }
      OBQuery<EHCMEmpLeaveln> leavelnQry = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class,
          " as e where e.ehcmEmpLeave.id in ( select a.id from  EHCM_Emp_Leave a where a.absenceType.id=:absenceTypeId ) "
              + hql);
      leavelnQry.setNamedParameter("absenceTypeId", absenceType.getId());
      leavelnQry.setNamedParameter("fromdate", Utility.formatDate(startdate));// dateafter
      leavelnQry.setNamedParameter("todate", Utility.formatDate(enddate));

      if (gradeclass != null)
        leavelnQry.setNamedParameter("gradeclass", gradeclass.getId());
      log.debug("leavelnQry:" + leavelnQry.getWhereAndOrderBy());
      log.debug("fromdate:" + Utility.formatDate(startdate));
      log.debug("todate:" + Utility.formatDate(enddate));
      empleavelnList = leavelnQry.list();
      if (empleavelnList.size() > 0) {
        return true;
      } else {
        return false;
      }

    }

    catch (final Exception e) {
      log.error("Exception in chkPartGradLeaveAlreadyTakenForThatPeriod() Method : ", e);
      return true;
    }
  }

  public boolean chkBlankGradLeaveAlreadyTakenForThatPeriod(Date startdate, Date enddate,
      EHCMAbsenceType absenceType, ehcmgradeclass gradeclass, boolean isStartDateChange,
      boolean isdelete) {
    String hql = "";
    Date Enddate = null;
    int onemilliSeconds = 1 * 24 * 3600 * 1000;
    List<EHCMAbsenceTypeAccruals> absenceAccrualList = new ArrayList<EHCMAbsenceTypeAccruals>();
    try {
      Enddate = enddate;
      if (!isdelete) {
        if (isStartDateChange) {
          enddate = new Date(enddate.getTime() - onemilliSeconds);
        } else {
          startdate = new Date(startdate.getTime() + onemilliSeconds);
        }
      }

      hql += " and (to_date(:startDate,'dd-MM-yyyy') between TO_DATE(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') "
          + " and  TO_DATE(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy')) ";

      // fist in between date bring distinct grade class
      SQLQuery Query = OBDal.getInstance().getSession().createSQLQuery(
          " select distinct ehcm_gradeclass_id from ehcm_absence_attendance where  ehcm_absence_attendance_id in ("
              + " select ehcm_absence_attendance_id from ehcm_emp_leaveln "
              + "    where ehcm_emp_leave_id in "
              + "    ( select ehcm_emp_leave_id from ehcm_emp_leave "
              + " where ehcm_absence_type_id=:absenceTypeId) "
              + "  and ehcm_absence_attendance_id is not null"
              + "  and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
              + "and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') "
              + "       <= to_date(:todate,'dd-MM-yyyy')) "
              + "or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') "
              + "    >= to_date(:fromdate,'dd-MM-yyyy') "
              + "and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) and isactive='Y'  )  ");
      Query.setParameter("absenceTypeId", absenceType.getId());
      Query.setParameter("fromdate", Utility.formatDate(startdate));// dateafter
      Query.setParameter("todate", Utility.formatDate(enddate));
      log.debug("dateafter:" + Utility.formatDate(startdate));
      log.debug("enddate:" + Utility.formatDate(enddate));
      log.debug("Query1:" + Query.toString());
      log.debug("Query:" + Query.list().size());
      if (Query.list().size() > 0) {
        for (Object o : Query.list()) {
          Object row = (Object) o;

          // chk each grade class separately define in absence accrual based on enddate,
          // if exists
          // then need to chk
          // next loop or else return true
          OBQuery<EHCMAbsenceTypeAccruals> absenceAccrual = OBDal.getInstance()
              .createQuery(EHCMAbsenceTypeAccruals.class, " as e "
                  + "  where  e.absenceType.id=:absenceTypeId  and e.gradeClassifications.id=:gradeclass "
                  + hql);
          absenceAccrual.setNamedParameter("absenceTypeId", absenceType.getId());
          absenceAccrual.setNamedParameter("gradeclass", (String) row);
          if (isStartDateChange)
            absenceAccrual.setNamedParameter("startDate", Utility.formatDate(enddate));
          else
            absenceAccrual.setNamedParameter("startDate", Utility.formatDate(startdate));
          log.debug("leavelnQry:" + absenceAccrual.getWhereAndOrderBy());
          log.debug("enddate:" + Utility.formatDate(enddate));
          absenceAccrualList = absenceAccrual.list();
          if (absenceAccrualList.size() > 0) {
            continue;
          } else {
            return true;
          }
        }
      } else {
        return false;
      }

    } catch (final Exception e) {
      log.error("Exception in chkBlankGradLeaveAlreadyTakenForThatPeriod() Method : ", e);
      return true;
    }
    return false;
  }

  public String convertToGregorianDate(String hijriDate) {
    String gregDate = "";
    try {
      SQLQuery Query = OBDal.getInstance().getSession().createSQLQuery(
          "select to_char(gregorian_date,'YYYY-MM-DD')  from eut_hijri_dates where hijri_date ='"
              + hijriDate + "'");
      log.debug("Query:" + Query.toString());
      if (Query.list().size() > 0) {
        Object row = (Object) Query.list().get(0);
        gregDate = (String) row;
        log.debug("ConvertedDate:" + (String) row);
      }
    }

    catch (final Exception e) {
      log.error("Exception in convertToGregorianDate() Method : ", e);
      return "0";
    }
    return gregDate;
  }

  public String convertTohijriDate(String gregDate) {
    String hijriDate = "";
    try {
      SQLQuery gradeQuery = OBDal.getInstance().getSession()
          .createSQLQuery("select eut_convert_to_hijri(to_char(to_timestamp('" + gregDate
              + "','YYYY-MM-DD HH24:MI:SS'),'YYYY-MM-DD  HH24:MI:SS'))");
      if (gradeQuery.list().size() > 0) {
        Object row = (Object) gradeQuery.list().get(0);
        hijriDate = (String) row;
        log.debug("ConvertedDate:" + (String) row);
      }
    }

    catch (final Exception e) {
      log.error("Exception in convertTohijriDate() Method : ", e);
      return "0";
    }
    return hijriDate;
  }

  public JSONObject getStartDateAndEndDate(Date startDate, Date endDate,
      EHCMAbsenceAttendance absence, EHCMAbsenceType absencetype) {
    JSONObject result = new JSONObject();
    try {
      SQLQuery Query = OBDal.getInstance().getSession()
          .createSQLQuery(" select  * from ehcm_getaccrualstartenddate(?,?,?,?,?,?) ");
      log.debug("Query:" + Query.toString());
      Query.setParameter(0, absence.getEhcmEmpPerinfo().getId());
      Query.setParameter(1, yearFormat.format(startDate));
      Query.setParameter(2, absence.getEhcmAbsenceType().getAccrualResetDate());
      Query.setParameter(3, absence.getEhcmAbsenceType().getFrequency());
      Query.setParameter(4, absencetype.getId());
      if (absence.getSubtype() != null)
        Query.setParameter(5, absence.getSubtype().getId());
      else
        Query.setParameter(5, "");
      log.debug("Query:" + Query.getQueryString());
      log.debug("getEhcmEmpPerinfo:" + absence.getEhcmEmpPerinfo().getId());
      log.debug("getStartDate:" + yearFormat.format(startDate));
      log.debug("getEndDate:" + absence.getEhcmAbsenceType().getAccrualResetDate());
      log.debug("getClient:" + absence.getEhcmAbsenceType().getFrequency());
      log.debug("size:" + Query.getQueryString());
      if (Query.list().size() > 0) {
        log.debug("get:" + Query.list().get(0));
        Object[] row = (Object[]) Query.list().get(0);
        log.debug("row:" + row);
        result.put("startdate", row[0]);
        result.put("enddate", row[1]);
      }

    } catch (

    final Exception e) {
      log.error("Exception in getStartDateAndEndDate() :", e);
      return result;
    }
    return result;
  }

  public void updateDependentRelatedAbsDays(EHCMAbsenceAttendance absence) {
    List<EHCMEmpLeaveln> empLeaveLnList = new ArrayList<EHCMEmpLeaveln>();
    try {
      OBContext.setAdminMode();
      OBQuery<EHCMEmpLeaveln> ln = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class,
          " as e where e.ehcmAbsenceAttendance.id=:attendanceId ");
      ln.setNamedParameter("attendanceId", absence.getId());
      log.debug("where:" + ln.getWhereAndOrderBy());
      empLeaveLnList = ln.list();
      if (empLeaveLnList.size() > 0) {
        for (EHCMEmpLeaveln emplevln : empLeaveLnList) {
          if (absence.getEhcmAbsenceType().getId()
              .equals(emplevln.getEhcmEmpLeave().getAbsenceType().getId())) {
            absence.setRelatedAbsdays(emplevln.getLeavedays());
          } else {
            absence.setDependentAbsdays(emplevln.getLeavedays());
          }
          OBDal.getInstance().save(absence);
        }
      }

    } catch (final Exception e) {
      log.error("Exception in updateDependentRelatedAbsDays() Method : ", e);
    }

  }

  public int countofHolidays(Date startDate, Date endDate, String clientId, boolean isInclude) {
    List<EHCMHolidayCalendar> holidayLnList = new ArrayList<EHCMHolidayCalendar>();
    int holidayCount = 0;
    String hql = "";
    try {

      if (!isInclude) {
        hql = " and e.holidayType  not in ('WE1','WE2') ";
      }

      OBQuery<EHCMHolidayCalendar> holidyaCalLn = OBDal.getInstance().createQuery(
          EHCMHolidayCalendar.class,
          " as e where e.holidaydate >=:startdate and e.holidaydate<=:enddate and e.client.id=:clientId"
              + hql);
      holidyaCalLn.setNamedParameter("startdate", startDate);
      holidyaCalLn.setNamedParameter("enddate", endDate);
      holidyaCalLn.setNamedParameter("clientId", clientId);
      log.debug("holidyaCalLn:" + holidyaCalLn.getWhereAndOrderBy());
      holidayLnList = holidyaCalLn.list();
      if (holidayLnList.size() > 0) {
        holidayCount = holidayLnList.size();
      }

    } catch (final Exception e) {
      log.error("Exception in countofHolidays() Method : ", e);
    }
    return holidayCount;
  }

  public Date getEndDate(int absenceDays, Date startDate, String clientId, boolean isInclude) {
    Date EndDate = null;
    String sql = "";
    try {
      if (!isInclude) {
        sql = " and  gregorian_date not in ( select  holidaydate from ehcm_holiday_calendar  cal "
            + " where  cal.holidaydate >=:startdate"
            + "  and cal.ad_client_id=:clientId and cal.holiday_type not in ('WE1','WE2')  ) ";
      }

      /*
       * if (isInclude) { sql = " and cal.holiday_type in ('WE1','WE2')  "; }
       */

      /*
       * SQLQuery Query = OBDal.getInstance().getSession().createSQLQuery("" +
       * "  select a.hijri_date ,a.gregorian_date  from ( select max(hijri_date)as hijri_date,gregorian_date from eut_hijri_dates dat "
       * + " where gregorian_date not in ( select  holidaydate from ehcm_holiday_calendar  cal " +
       * "                             where  cal.holidaydate >=:startdate" +
       * " and cal.ad_client_id=:clientId " + sql + " ) " +
       * "                             and  dat.gregorian_date >=:startdate" +
       * " group by gregorian_date  order by gregorian_date asc limit :limit offset :offset )a " +
       * "        limit 1 ");
       */
      SQLQuery Query = OBDal.getInstance().getSession().createSQLQuery(""
          + "  select a.hijri_date ,a.gregorian_date  from ( select max(hijri_date)as hijri_date,gregorian_date from eut_hijri_dates dat "
          + " where  dat.gregorian_date >=:startdate " + sql
          + " group by gregorian_date  order by gregorian_date asc limit :limit offset :offset )a "
          + "        limit 1 ");
      log.debug("Query:" + Query.toString());
      Query.setParameter("startdate", startDate);
      if (!isInclude) {
        Query.setParameter("clientId", clientId);
      }
      Query.setParameter("limit", absenceDays);
      Query.setParameter("offset", absenceDays - 1);
      log.debug("Query:" + Query.list().size());
      if (Query.list().size() > 0) {
        List<Object> object = Query.list();
        Object[] row = (Object[]) Query.list().get(0);
        EndDate = (Date) row[1];
      }

    } catch (final Exception e) {
      log.error("Exception in getEndDate() Method : ", e);
    }
    return EndDate;
  }

  public EmploymentInfo getHiringEmployInfo(EhcmEmpPerInfo employeeOB) {
    EmploymentInfo empinfo = null;
    try {
      for (EmploymentInfo empInfoOB : employeeOB.getEhcmEmploymentInfoList()) {
        if (empInfoOB.getChangereason().equals("H")) {
          empinfo = empInfoOB;
        }
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in getActiveEmployInfo " + e.getMessage());
      e.printStackTrace();
    }
    return empinfo;
  }

  public String getChkLeaveApproveMsg(String checkAppMessage, EHCMAbsenceType absencetype) {
    String checkAppMessageBD = "";
    String checkAppMsg = null;
    List<EHCMAbsenceTypeRules> absenceRuleList = new ArrayList<EHCMAbsenceTypeRules>();
    try {
      checkAppMsg = checkAppMessage;
      checkAppMessageBD = OBMessageUtils.messageBD(checkAppMsg);
      log.debug("chkappmessages:" + checkAppMessageBD);
      if (!checkAppMessageBD.equals("Success")) {
        // leave less than 5 or 7 days then throw the error msg with corresponding input
        // in
        // absence Type Rules
        if (checkAppMsg.equals("EHCM_LLTF")) {
          OBQuery<EHCMAbsenceTypeRules> ruleQry = OBDal.getInstance().createQuery(
              EHCMAbsenceTypeRules.class,
              " as e where e.code=:code and e.absenceType.id=:absenceTypeId ");
          ruleQry.setNamedParameter("code", "LLTF");
          ruleQry.setNamedParameter("absenceTypeId", absencetype.getId());
          ruleQry.setMaxResult(1);
          absenceRuleList = ruleQry.list();
          if (absenceRuleList.size() > 0) {
            EHCMAbsenceTypeRules absrule = absenceRuleList.get(0);
            log.debug("getCondition:" + absrule.getCondition().split("<=")[1].toString());
            String input = absrule.getCondition().split("<=")[1].toString();
            log.debug("input:" + input);
            checkAppMsg = OBMessageUtils.messageBD(checkAppMsg);
            checkAppMsg = checkAppMsg.replace("%", input);
          }
        } else if (checkAppMsg.contains("EHCM_LevNotAvailable")) {
          String output = checkAppMsg.split("-")[1];
          log.debug("output:" + output);
          checkAppMsg = OBMessageUtils.messageBD(checkAppMsg.split("-")[0]);
          checkAppMsg = checkAppMsg.replace("%", output);
          log.debug("chkapp:" + checkAppMsg);
        } else {
          String output = checkAppMsg.split("_")[1];
          log.debug("output:" + output);
          checkAppMsg = OBMessageUtils.messageBD("EHCM_AbsDecisionLevApp_Error");
          checkAppMsg = checkAppMsg.replace("%", output);
          log.debug("chkapp:" + checkAppMsg);
        }
      }
      return checkAppMsg;
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error("Exception in getChkLeaveApproveMsg " + e.getMessage());
      e.printStackTrace();
    }
    return checkAppMsg;
  }

  public boolean checkExtendAbsenceTypeLeaveIsTakenBeforeIssueDecision(
      EHCMAbsenceAttendance absenceattend) {
    Date dateBefore = null;
    int oneMiliSeconds = 1 * 24 * 3600 * 1000;
    try {

      dateBefore = new Date(absenceattend.getStartDate().getTime() - oneMiliSeconds);

      if (absenceattend.getEhcmAbsenceType().getExtendAbsenceType() != null) {
        if (absenceattend.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {

          OBQuery<EHCMEmpLeaveln> existAbsenceTypeleaveLnQry = OBDal.getInstance()
              .createQuery(EHCMEmpLeaveln.class, " "
                  + " as e  where e.ehcmEmpLeave.id in ( select lev.id from EHCM_Emp_Leave lev where lev.ehcmEmpPerinfo.id=:employeeId "
                  + " and lev.absenceType.id=:absenceTypeId )  and e.ehcmAbsenceAttendance.id<>:absenceAttendID ");
          existAbsenceTypeleaveLnQry.setNamedParameter("employeeId",
              absenceattend.getEhcmEmpPerinfo().getId());
          existAbsenceTypeleaveLnQry.setNamedParameter("absenceTypeId",
              absenceattend.getEhcmAbsenceType().getId());
          existAbsenceTypeleaveLnQry.setNamedParameter("absenceAttendID", absenceattend.getId());
          if (existAbsenceTypeleaveLnQry.list().size() == 0) {

            OBQuery<EHCMEmpLeaveln> leaveLnQry = OBDal.getInstance()
                .createQuery(EHCMEmpLeaveln.class, " "
                    + " as e  where e.ehcmEmpLeave.id in ( select lev.id from EHCM_Emp_Leave lev where lev.ehcmEmpPerinfo.id=:employeeId "
                    + " and lev.absenceType.id=:absenceTypeId )  and  e.endDate=:leaveEndDate  ");
            leaveLnQry.setNamedParameter("employeeId", absenceattend.getEhcmEmpPerinfo().getId());
            leaveLnQry.setNamedParameter("absenceTypeId",
                absenceattend.getEhcmAbsenceType().getExtendAbsenceType().getId());
            leaveLnQry.setNamedParameter("leaveEndDate", dateBefore);
            if (leaveLnQry.list().size() == 0) {
              return true;
            }
          }
        }
      }
    }

    catch (final Exception e) {
      e.printStackTrace();
      log.error("Exception in checkExtendAbsenceTypeLeaveIsTakenBeforeReactivate() Method : ", e);
    }
    return false;
  }

}
