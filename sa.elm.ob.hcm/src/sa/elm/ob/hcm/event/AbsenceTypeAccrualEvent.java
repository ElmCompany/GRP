package sa.elm.ob.hcm.event;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;
import sa.elm.ob.hcm.ad_process.AbsenceDecision.AbsenceIssueDecisionDAOImpl;
import sa.elm.ob.utility.util.Utility;

public class AbsenceTypeAccrualEvent extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMAbsenceTypeAccruals.ENTITY_NAME) };
  private Logger log = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      String sql = "", strQuery = "";
      int query = 0;
      SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
      DateFormat dateFormat = Utility.dateFormat;
      Date prevStartDate = null;
      Date currentStartDate = null;
      Date prevEndDate = null;
      Date currentEndDate = null;
      String hijiriEndDate = "21-06-2058";
      OBQuery<EHCMAbsenceTypeAccruals> absaccrual = null;
      OBContext.setAdminMode();
      EHCMAbsenceTypeAccruals accruals = (EHCMAbsenceTypeAccruals) event.getTargetInstance();
      final Property enddate = entities[0].getProperty(EHCMAbsenceTypeAccruals.PROPERTY_ENDDATE);
      final Property startdate = entities[0]
          .getProperty(EHCMAbsenceTypeAccruals.PROPERTY_STARTDATE);
      final Property days = entities[0].getProperty(EHCMAbsenceTypeAccruals.PROPERTY_DAYS);

      final Property gradeclass = entities[0]
          .getProperty(EHCMAbsenceTypeAccruals.PROPERTY_GRADECLASSIFICATIONS);
      AbsenceIssueDecisionDAOImpl absenceIssueDecisionDAOImpl = new AbsenceIssueDecisionDAOImpl();
      //
/*
      String fdate = Utility.formatDate(accruals.getStartDate());
      String tdate = null;
      if (accruals.getEndDate() != null) {
        tdate = Utility.formatDate(accruals.getEndDate());

      } else {
        tdate = "21-06-2058";
      }
      if ((event.getPreviousState(gradeclass) != null
          && !event.getPreviousState(gradeclass).equals(event.getCurrentState(gradeclass)))
          || (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate)))
          || (event.getPreviousState(enddate) != null
              && !event.getPreviousState(enddate).equals(event.getCurrentState(enddate)))) {
        sql = " absenceType.id='" + accruals.getAbsenceType().getId() + "'  and id <> '"
            + accruals.getId() + "' ";
        if (accruals.getGradeClassifications() != null)
          sql += "  and gradeClassifications.id='" + accruals.getGradeClassifications().getId()
              + "'";

        else
          sql += " and gradeClassifications.id is  null ";

        absaccrual = OBDal.getInstance().createQuery(EHCMAbsenceTypeAccruals.class,
            sql + "  and endDate is null ");
        absaccrual.setMaxResult(1);
        log.debug("abseaccrual  " + absaccrual.list().size());
        log.debug("abseaccrual  " + absaccrual.getWhereAndOrderBy());
        if (absaccrual.list().size() > 0) {
          EHCMAbsenceTypeAccruals accrual = absaccrual.list().get(0);
          EHCMAbsenceType type = accrual.getAbsenceType();
          if (accrual.getStartDate().compareTo(accruals.getStartDate()) >= 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrual_PeriodConflict"));
          }

          Date datebefore = new Date(accruals.getStartDate().getTime() - 1 * 24 * 3600 * 1000);
          log.debug("datebefore  " + datebefore);
          accrual.setEndDate(datebefore);
          OBDal.getInstance().save(accrual);
          OBDal.getInstance().refresh(type);

        } else {

          sql += " and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('" + fdate
              + "') and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
              + tdate
              + "','dd-MM-yyyy')) or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
              + fdate + "') and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
              + tdate + "','dd-MM-yyyy'))) ";
          log.debug("list sql  " + sql);
          absaccrual = OBDal.getInstance().createQuery(EHCMAbsenceTypeAccruals.class, sql);
          absaccrual.setMaxResult(1);
          log.debug("list 12  " + absaccrual.list().size());
          if (absaccrual.list().size() > 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrual_PeriodConflict"));
          }
        }
      }
*/
      //
      if ((event.getPreviousState(gradeclass) != null
          && !event.getPreviousState(gradeclass).equals(event.getCurrentState(gradeclass)))
          || (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate)))
          || (event.getPreviousState(enddate) != null
              && !event.getPreviousState(enddate).equals(event.getCurrentState(enddate)))) {
        if (absenceIssueDecisionDAOImpl.chkAbsenceAccrualExistsOrNot(accruals)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrual_PeriodConflict"));
        }
      }

      if (!event.getCurrentState(startdate).equals(event.getPreviousState(startdate))
          || (event.getCurrentState(enddate) != null
              && !event.getCurrentState(enddate).equals(event.getPreviousState(enddate)))) {
        if (accruals.getEndDate() != null
            && accruals.getEndDate().compareTo(accruals.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_ScholarShipEndStartDateComp"));
        }
      }
      if (!event.getCurrentState(days).equals(event.getPreviousState(days))) {
        if (accruals.getDays().compareTo(BigDecimal.ZERO) == 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrualDayZero"));
        }
      }

      // end date change check with employee leave if previous end date greater than to current
      // enddate
      if (event.getPreviousState(enddate) == null || event.getCurrentState(enddate) == null
          || (event.getCurrentState(enddate) != null
              && !event.getCurrentState(enddate).equals(event.getPreviousState(enddate)))) {

        // if currentEndDate enddate is blank then set the currentEndDate as
        // HijiriEnddate("20-06-2058")
        if (event.getCurrentState(enddate) != null) {
          currentEndDate = (Date) event.getCurrentState(enddate);
        } else {
          currentEndDate = dateFormat.parse(hijiriEndDate);
        }
        // if previousEnddate is blank then set the previousEnddate as HijiriEnddate("20-06-2058")
        if (event.getPreviousState(enddate) != null) {
          prevEndDate = (Date) event.getPreviousState(enddate);
        } else {
          prevEndDate = dateFormat.parse(hijiriEndDate);
        }
        // if current end date is lesser than previous enddate ie, changing enddate from 29-01-1439
        // to 25-01-1439 then chk leave already taken for this period
        if (currentEndDate != null && prevEndDate != null
            && currentEndDate.compareTo(prevEndDate) < 0) {

          // if changing the accrual which having grade class ( ex:grade f) then chk only for that
          // grade leave already taken or not
          if (accruals.getGradeClassifications() != null) {
            if (absenceIssueDecisionDAOImpl.chkPartGradLeaveAlreadyTakenForThatPeriod(
                currentEndDate, prevEndDate, accruals.getAbsenceType(),
                accruals.getGradeClassifications(), false, false)) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrualCantUpDate"));
            }
          }
          // if changing the accrual which does not have grade class ( ex:blank grade) then chk all
          // the grade leave already taken or not
          else {
            if (absenceIssueDecisionDAOImpl.chkBlankGradLeaveAlreadyTakenForThatPeriod(
                currentEndDate, prevEndDate, accruals.getAbsenceType(),
                accruals.getGradeClassifications(), false, false)) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrualCantUpDate"));
            }
          }
        }
        // if changing the accrual which having grade class ( ex:grade f) and current end date is
        // greater than previous enddate ie, changing enddate from 29-01-1439 to empty
        if (accruals.getGradeClassifications() != null
            && currentEndDate.compareTo(prevEndDate) > 0) {

          if (absenceIssueDecisionDAOImpl.chkBlankGradLeaveAlreadyTakenForThatPeriod(prevEndDate,
              currentEndDate, accruals.getAbsenceType(), accruals.getGradeClassifications(), false,
              false)) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrualCantUpDate"));
          }
        }
      }
      // start date change check with employee leave if previous start date lesser than to current
      // startdate
      if (!event.getCurrentState(startdate).equals(event.getPreviousState(startdate))) {
        currentStartDate = (Date) event.getCurrentState(startdate);
        prevStartDate = (Date) event.getPreviousState(startdate);

        // if previous start date lesser than current start date ie, changing start date from
        // 01-01-1439 to 12-01-1439 then chk leave already taken for this period

        if (prevStartDate != null && currentStartDate != null
            && prevStartDate.compareTo(currentStartDate) < 0) {
          // if changing the accrual which having grade class ( ex:grade f) then chk only for that
          // grade leave already taken or not
          if (accruals.getGradeClassifications() != null) {
            if (absenceIssueDecisionDAOImpl.chkPartGradLeaveAlreadyTakenForThatPeriod(prevStartDate,
                currentStartDate, accruals.getAbsenceType(), accruals.getGradeClassifications(),
                true, false)) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrualCantUpDate"));
            }
          }
          // if changing the accrual which does not have grade class ( ex:blank grade) then chk all
          // the grade leave already taken or not
          else {
            if (absenceIssueDecisionDAOImpl.chkBlankGradLeaveAlreadyTakenForThatPeriod(
                prevStartDate, currentStartDate, accruals.getAbsenceType(),
                accruals.getGradeClassifications(), true, false)) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrualCantUpDate"));
            }
          }
        }
      }

    } catch (OBException e) {
      log.error(" Exception while updating Absence type accruals   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      String sql = "", strQuery = "";
      int query = 0;
      AbsenceIssueDecisionDAOImpl absenceIssueDecisionDAOImpl = new AbsenceIssueDecisionDAOImpl();
      SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
      OBQuery<EHCMAbsenceTypeAccruals> absaccrual = null;
      OBContext.setAdminMode();
      EHCMAbsenceTypeAccruals accruals = (EHCMAbsenceTypeAccruals) event.getTargetInstance();
      if (absenceIssueDecisionDAOImpl.chkAbsenceAccrualExistsOrNot(accruals)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrual_PeriodConflict"));
      }
      //
      /*
      String fdate = Utility.formatDate(accruals.getStartDate());
      String tdate = null;
      if (accruals.getEndDate() != null) {
        tdate = Utility.formatDate(accruals.getEndDate());

      } else {
        tdate = "21-06-2058";
      }

      sql = " absenceType.id='" + accruals.getAbsenceType().getId() + "'  and id <> '"
          + accruals.getId() + "' ";
      if (accruals.getGradeClassifications() != null)
        sql += "  and ( gradeClassifications.id='" + accruals.getGradeClassifications().getId()
            + "' or  gradeClassifications.id is null  )";

      else
        sql += " and gradeClassifications.id is null  ";

      absaccrual = OBDal.getInstance().createQuery(EHCMAbsenceTypeAccruals.class,
          sql + "  and endDate is null ");
      absaccrual.setMaxResult(1);
      log.debug("list  " + absaccrual.list().size());
      if (absaccrual.list().size() > 0) {
        EHCMAbsenceTypeAccruals accrual = absaccrual.list().get(0);
        EHCMAbsenceType type = accrual.getAbsenceType();
        if (accrual.getStartDate().compareTo(accruals.getStartDate()) >= 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrual_PeriodConflict"));
        }

        Date datebefore = new Date(accruals.getStartDate().getTime() - 1 * 24 * 3600 * 1000);
        log.debug("datebefore  " + datebefore);
        log.debug("accrual  " + accrual);
        accrual.setEndDate(datebefore);
        OBDal.getInstance().save(accrual);
        OBDal.getInstance().refresh(type);

      } else {

        sql += " and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('" + fdate
            + "') and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
            + tdate
            + "','dd-MM-yyyy')) or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
            + fdate + "') and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
            + tdate + "','dd-MM-yyyy'))) ";
        absaccrual = OBDal.getInstance().createQuery(EHCMAbsenceTypeAccruals.class, sql);
        absaccrual.setMaxResult(1);
        log.debug("list 12  " + absaccrual.list().size());
        if (absaccrual.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrual_PeriodConflict"));
        }
      }
      */
      //
      if (accruals.getEndDate() != null
          && accruals.getEndDate().compareTo(accruals.getStartDate()) == -1) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_ScholarShipEndStartDateComp"));
      }

      if (accruals.getDays().compareTo(BigDecimal.ZERO) == 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrualDayZero"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating Absence type accruals   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      AbsenceIssueDecisionDAOImpl absenceIssueDecisionDAOImpl = new AbsenceIssueDecisionDAOImpl();
      EHCMAbsenceTypeAccruals accruals = (EHCMAbsenceTypeAccruals) event.getTargetInstance();
      Date Enddate = accruals.getEndDate();
      String hijiriEndDate = "21-06-2058";
      DateFormat dateFormat = Utility.dateFormat;

      if (Enddate == null) {
        Enddate = dateFormat.parse(hijiriEndDate);
      }
      if (accruals.getGradeClassifications() != null) {

        if (absenceIssueDecisionDAOImpl.chkPartGradLeaveAlreadyTakenForThatPeriod(
            accruals.getStartDate(), Enddate, accruals.getAbsenceType(),
            accruals.getGradeClassifications(), false, true)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrual_CantDelete"));
        }
      }
      // if changing the accrual which does not have grade class ( ex:blank grade) then chk all
      // the grade leave already taken or not
      else {
        if (absenceIssueDecisionDAOImpl.chkBlankGradLeaveAlreadyTakenForThatPeriod(
            accruals.getStartDate(), Enddate, accruals.getAbsenceType(),
            accruals.getGradeClassifications(), false, true)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsAccrual_CantDelete"));
        }
      }
    } catch (OBException e) {
      log.error("Exception while creating absence type" + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
