package sa.elm.ob.hcm.ad_process.AbsenceDecision;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.hibernate.SQLQuery;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;
import sa.elm.ob.hcm.EHCMEMPLeaveBlockLn;
import sa.elm.ob.hcm.EHCMEmpLeave;
import sa.elm.ob.hcm.EHCMEmpLeaveln;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.utility.util.Utility;

/**
 * This process class used for Employee Absence reactivateDAO Implementation
 * 
 * @author divya 11-06-2018
 *
 */

public class AbsenceDecisionReactivateDAOImpl implements AbsenceDecisionReactivateDAO {

  private static final Logger log = LoggerFactory.getLogger(AbsenceDecisionReactivateDAOImpl.class);
  public static final String DECISION_STATUS_UNDERPROCESSING = "UP";
  AbsenceIssueDecisionDAO absenceIssueDecisionDAO = new AbsenceIssueDecisionDAOImpl();
  DateFormat yearFormat = Utility.YearFormat;

  @Override
  public void updateAbsenceDecisionStatus(EHCMAbsenceAttendance absence) {
    try {
      absence.setSueDecision(false);
      absence.setDecisionDate(null);
      absence.setDecisionStatus(DECISION_STATUS_UNDERPROCESSING);
      OBDal.getInstance().save(absence);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateAbsenceDecisionStatus in AbsenceDecisionReactivateDAOImpl: ",
          e);
    }
  }

  /**
   * delete the Emp leave record and Emp Leave block record
   */
  @Override
  public void deleteEmpLeaveRecordInCreateCase(EHCMAbsenceAttendance absence) {
    List<EHCMEmpLeaveln> empLeaveLnList = null;
    List<EHCMEMPLeaveBlockLn> empLeaveBlockLnList = null;

    try {

      // delete the emp leave record
      OBQuery<EHCMEmpLeaveln> empLevLnQry = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class,
          " as e where e.ehcmAbsenceAttendance.id=:absenceDecisionId ");
      empLevLnQry.setNamedParameter("absenceDecisionId", absence.getId());
      empLeaveLnList = empLevLnQry.list();
      if (empLeaveLnList.size() > 0) {
        for (EHCMEmpLeaveln empLeaveln : empLeaveLnList) {
          OBDal.getInstance().remove(empLeaveln);
        }
      }

      // delete emp leave block record
      if (absence.getEhcmAbsenceType().getAccrualResetDate().equals("LO")) {
        OBQuery<EHCMEMPLeaveBlockLn> empLeaveBlockLnQry = OBDal.getInstance().createQuery(
            EHCMEMPLeaveBlockLn.class,
            " as e where e.ehcmAbsenceAttendance.id=:absenceDecisionId ");
        empLeaveBlockLnQry.setNamedParameter("absenceDecisionId", absence.getId());
        empLeaveBlockLnList = empLeaveBlockLnQry.list();
        if (empLeaveBlockLnList.size() > 0) {
          for (EHCMEMPLeaveBlockLn empLeaveBlockln : empLeaveBlockLnList) {
            OBDal.getInstance().remove(empLeaveBlockln);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error(
          "Exception in deleteEmpLeaveRecordInCreateCase in AbsenceDecisionReactivateDAOImpl: ", e);
    }
  }

  @Override
  public void updateEmpLeaveRecrdInOtherThanCreateCase(Connection connection,
      EHCMAbsenceAttendance absence, EHCMAbsenceType absencetype, EHCMAbsenceTypeAccruals accrual) {
    try {

      EHCMAbsenceAttendance originalAbsenceDecision = absence.getOriginalDecisionNo();

      // accural reset is leave occurance
      if (absence.getEhcmAbsenceType().getAccrualResetDate().equals("LO")) {
        // delete the emp leave and emp leave block record other than cancel case
        if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL))
          deleteEmpLeaveRecordInCreateCase(absence);

        // check exceptional leave validation & insert emp leave block record
        absenceIssueDecisionDAO.chkexceptionleaveval(connection, originalAbsenceDecision,
            accrual.getId());

        // insert emp leave record
        absenceIssueDecisionDAO.insertLeaveOccuranceEmpLeave(originalAbsenceDecision, absencetype,
            accrual, originalAbsenceDecision.getStartDate(), originalAbsenceDecision.getEndDate());

        // insert emp leaveln record
        absenceIssueDecisionDAO.insertEmpLeaveLine(null, absence.getEhcmAbsenceType(),
            originalAbsenceDecision, originalAbsenceDecision.getAbsenceDays(),
            originalAbsenceDecision.getStartDate(), originalAbsenceDecision.getEndDate());
      }
      // other than leave occurance -ie,Beginning gregorian year,Hijiri Year,Hire Anniversary Date
      else {
        // delete the emp leave and emp leave block record other than cancel case
        if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
          deleteEmpLeaveRecordInCreateCase(absence);
        }
        log.debug("deducted:" + absence.getEhcmAbsenceType().isDeducted());
        // deducted leave
        if (absence.getEhcmAbsenceType().isDeducted()) {
          // reactivate the deducted leave
          deductedLeaveReactivate(absencetype, connection, originalAbsenceDecision, null, accrual,
              absence);
        }
        // other than deducted leave
        else {
          // insert emp leave record
          absenceIssueDecisionDAO.insertLeaveOccuranceEmpLeave(originalAbsenceDecision, absencetype,
              accrual, originalAbsenceDecision.getStartDate(),
              originalAbsenceDecision.getEndDate());
          // insert emp leaveln record
          absenceIssueDecisionDAO.insertEmpLeaveLine(null, absence.getEhcmAbsenceType(),
              originalAbsenceDecision, originalAbsenceDecision.getAbsenceDays(),
              originalAbsenceDecision.getStartDate(), originalAbsenceDecision.getEndDate());
        }
      }
      // update absence decision flag
      if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
        absence.setEnabled(true);
        OBDal.getInstance().save(absence);
      }
      // update absence decision flag
      absenceIssueDecisionDAO.updateAbsenceEnableFlag(originalAbsenceDecision, true);
    } catch (Exception e) {
      e.printStackTrace();
      log.error(
          "Exception in deleteEmpLeaveRecordInCreateCase in AbsenceDecisionReactivateDAOImpl: ", e);
    }
  }

  /**
   * deducted leave reactivate
   * 
   * @param absencetype
   * @param con
   * @param originalDecisionabsence
   * @param leave
   * @param accrual
   * @param absence
   * @return integer
   */
  public int deductedLeaveReactivate(EHCMAbsenceType absencetype, Connection con,
      EHCMAbsenceAttendance originalDecisionabsence, EHCMEmpLeave leave,
      EHCMAbsenceTypeAccruals accrual, EHCMAbsenceAttendance absence) {
    int count = 0;
    BigDecimal availabledays = BigDecimal.ZERO;
    BigDecimal days = BigDecimal.ZERO;
    BigDecimal difference = BigDecimal.ZERO;
    Date dependentLevEndDate = null;
    Date dateafter = null;
    int oneMiliSeconds = 1 * 24 * 3600 * 1000;
    String sql = "";
    try {

      // get availabledays (unused days) - dependent leave type
      availabledays = getAvailableAndAvaileddays(con, originalDecisionabsence, absencetype,
          yearFormat.format(originalDecisionabsence.getStartDate()), null, false, absence);

      log.debug("availableday:" + availabledays);
      // get applied leave days
      days = originalDecisionabsence.getAbsenceDays();

      // compare applied leave days and available leave days
      if (days.compareTo(availabledays) <= 0) {
        availabledays = days;
      }

      // find out the dependent leave end date based on availabledays & startdate , remaining days
      // will added on original absence leave type
      if (availabledays.compareTo(BigDecimal.ZERO) > 0) {
        if (absencetype.isInculdeholiday()) {
          sql = " and coalesce(cal.holiday_type,'WD') not in ('WE1','WE2') ";
        } else {
          sql = " and coalesce(cal.holiday_type,'WD') not in ('WE1','WE2','NH','AD','FE') ";
        }

        SQLQuery Query = OBDal.getInstance().getSession().createSQLQuery(
            " select max(a.gregorian_date) as endofdate from ( select gregorian_date "
                + " from eut_hijri_dates    left join ehcm_holiday_calendar cal on cal.holidaydate= eut_hijri_dates.gregorian_date "
                + "  and (cal.ad_client_id=:clientId or cal.ad_client_id is null)  "
                + " where gregorian_date >=:startDate " + sql
                + "  group by gregorian_date order by gregorian_date asc  limit :limitno ) a ");

        Query.setParameter("clientId", absence.getClient().getId());
        Query.setParameter("startDate", originalDecisionabsence.getStartDate());
        Query.setParameter("limitno", availabledays);
        log.debug("Query2:" + Query.toString());
        if (Query.list().size() > 0) {
          Object row = (Object) Query.list().get(0);
          dependentLevEndDate = (Date) row;
        }
      }

      // if availabledays greater than zero insert emp leave record and emp leave ln record
      if (dependentLevEndDate != null) {
        if (!originalDecisionabsence.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
          absenceIssueDecisionDAO.insertLeaveOccuranceEmpLeave(originalDecisionabsence, absencetype,
              accrual, originalDecisionabsence.getStartDate(), dependentLevEndDate);
        }
        absenceIssueDecisionDAO.insertEmpLeaveLine(null, absencetype, originalDecisionabsence,
            availabledays, originalDecisionabsence.getStartDate(), dependentLevEndDate);
        dateafter = new Date(dependentLevEndDate.getTime() + oneMiliSeconds);
      } else {
        dateafter = originalDecisionabsence.getStartDate();
      }

      // find the difference between applied leave days & availabledays
      difference = originalDecisionabsence.getAbsenceDays().subtract(availabledays);
      if (difference.compareTo(BigDecimal.ZERO) > 0) {
        // deducted leave
        // check deducted leave persented in empleave table or not
        if (!originalDecisionabsence.getDecisionType()
            .equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
          absenceIssueDecisionDAO.insertLeaveOccuranceEmpLeave(originalDecisionabsence,
              originalDecisionabsence.getEhcmAbsenceType(), accrual, dateafter,
              originalDecisionabsence.getEndDate());
        }
        // insert emp leave ln record
        absenceIssueDecisionDAO.insertEmpLeaveLine(null,
            originalDecisionabsence.getEhcmAbsenceType(), originalDecisionabsence, difference,
            dateafter, originalDecisionabsence.getEndDate());
      }
    } catch (final Exception e) {
      log.error("Exception in insertEmpLeaveBlocKLine" + e);
    }
    return count;
  }

  /**
   * get available leave and availed leave days
   * 
   * @param conn
   * @param originalDecisionAbsence
   * @param absencetype
   * @param startdate
   * @param enddate
   * @param availabledays
   * @param absence
   * @return big decimal
   */
  public BigDecimal getAvailableAndAvaileddays(Connection conn,
      EHCMAbsenceAttendance originalDecisionAbsence, EHCMAbsenceType absencetype, String startdate,
      String enddate, Boolean availabledays, EHCMAbsenceAttendance absence) {
    BigDecimal days = BigDecimal.ZERO;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      // call get availed and available leave days for each employee procedure
      st = conn.prepareStatement(
          " select * from  ehcm_getavailed_availablelev(?, ?, ?, ?, ?,?,?,?,?,?) ");
      st.setString(1, originalDecisionAbsence.getEhcmEmpPerinfo().getId());
      st.setString(2, startdate);
      st.setString(3, enddate);
      st.setString(4, absencetype.getId());
      st.setString(5, originalDecisionAbsence.getClient().getId());
      st.setInt(6, 1);
      // other than create pass decision type as update
      if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        st.setString(7, DecisionTypeConstants.DECISION_TYPE_UPDATE);
      } else {
        st.setString(7, absence.getDecisionType());
      }
      st.setString(8, absence.getId());
      st.setString(9, "0");
      if (originalDecisionAbsence.getSubtype() != null)
        st.setString(10, originalDecisionAbsence.getSubtype().getId());
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

  public boolean checkExtendAbsenceTypeLeaveIsTakenBeforeReactivate(
      EHCMAbsenceAttendance absenceattend) {
    List<EHCMAbsenceType> extendAbsencetypeList = null;
    Date dateAfter = null;
    int oneMiliSeconds = 1 * 24 * 3600 * 1000;
    try {

      dateAfter = new Date(absenceattend.getEndDate().getTime() + oneMiliSeconds);
      OBQuery<EHCMAbsenceType> extendAbsencetypeQry = OBDal.getInstance().createQuery(
          EHCMAbsenceType.class, " as e  where e.extendAbsenceType.id=:absenceTypeId ");
      extendAbsencetypeQry.setNamedParameter("absenceTypeId",
          absenceattend.getEhcmAbsenceType().getId());
      extendAbsencetypeList = extendAbsencetypeQry.list();
      if (extendAbsencetypeList.size() > 0) {
        for (EHCMAbsenceType absenceType : extendAbsencetypeList) {
          OBQuery<EHCMEmpLeaveln> leaveLnQry = OBDal.getInstance()
              .createQuery(EHCMEmpLeaveln.class, " "
                  + " as e  where e.ehcmEmpLeave.id in ( select lev.id from EHCM_Emp_Leave lev where lev.ehcmEmpPerinfo.id=:employeeId "
                  + " and lev.absenceType.id=:absenceTypeId )  and  e.startDate=:extendlevEndDate  ");
          leaveLnQry.setNamedParameter("employeeId", absenceattend.getEhcmEmpPerinfo().getId());
          leaveLnQry.setNamedParameter("absenceTypeId", absenceType.getId());
          leaveLnQry.setNamedParameter("extendlevEndDate", dateAfter);
          if (leaveLnQry.list().size() > 0) {
            return true;
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

  public String chkleaveapprove(EHCMAbsenceAttendance absence,
      EHCMAbsenceAttendance currentabsence) {
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
        Query.setParameter(7, currentabsence.getId());
      } else {
        Query.setParameter(6, DecisionTypeConstants.DECISION_TYPE_UPDATE);// absence.getDecisiontype();
        Query.setParameter(7, currentabsence.getId());// absence.getId()
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
}
