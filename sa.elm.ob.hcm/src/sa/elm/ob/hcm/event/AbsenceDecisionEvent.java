package sa.elm.ob.hcm.event;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeRules;
import sa.elm.ob.hcm.EHCMEmpLeaveln;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.AbsenceDecision.AbsenceIssueDecisionDAO;
import sa.elm.ob.hcm.ad_process.AbsenceDecision.AbsenceIssueDecisionDAOImpl;
import sa.elm.ob.hcm.event.dao.AbsenceDecisionEventDAO;
import sa.elm.ob.hcm.event.dao.AbsenceDecisionEventDAOImpl;
import sa.elm.ob.hcm.util.UtilityDAO;
import sa.elm.ob.utility.util.Utility;

public class AbsenceDecisionEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMAbsenceAttendance.ENTITY_NAME) };
  AbsenceIssueDecisionDAO absenceIssueDecisionDAO = new AbsenceIssueDecisionDAOImpl();
  AbsenceDecisionEventDAO absenceDecisionEventDAO = new AbsenceDecisionEventDAOImpl();

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());

  public void onUpdate(@Observes EntityUpdateEvent event) {
    String chkapp = "";
    BigInteger count = BigInteger.ZERO;
    OBQuery<EHCMEmpLeaveln> leave = null;
    String sql = "";
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      EHCMAbsenceAttendance absence = (EHCMAbsenceAttendance) event.getTargetInstance();
      final Property employee = entities[0]
          .getProperty(EHCMAbsenceAttendance.PROPERTY_EHCMEMPPERINFO);
      final Property absencetype = entities[0]
          .getProperty(EHCMAbsenceAttendance.PROPERTY_EHCMABSENCETYPE);
      final Property startdate = entities[0].getProperty(EHCMAbsenceAttendance.PROPERTY_STARTDATE);
      final Property endDate = entities[0].getProperty(EHCMAbsenceAttendance.PROPERTY_ENDDATE);
      final Property decisiontype = entities[0]
          .getProperty(EHCMAbsenceAttendance.PROPERTY_DECISIONTYPE);
      final Property absencedays = entities[0]
          .getProperty(EHCMAbsenceAttendance.PROPERTY_ABSENCEDAYS);
      final Property subType = entities[0].getProperty(EHCMAbsenceAttendance.PROPERTY_SUBTYPE);
      String enddate = Utility.formatDate(absence.getEndDate());
      SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
      int countOfHoliday = 0;
      int calDays = 0;

      // checking absence start date should be greater than employee hiring startdate or not
      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(employee).equals(event.getCurrentState(employee))) {
        EmploymentInfo employinfo = sa.elm.ob.hcm.util.Utility
            .getHiringEmployInfo(absence.getEhcmEmpPerinfo().getId());// sa.elm.ob.hcm.util.Utility
        // .getActiveEmployInfo(absence.getEhcmEmpPerinfo().getId());
        if (absence.getStartDate().compareTo(employinfo.getStartDate()) == -1
            || absence.getStartDate().compareTo(employinfo.getStartDate()) == 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_StartDate"));
        }
      }

      // original decision no is mandatory
      if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        if (absence.getOriginalDecisionNo() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
      }
      // subtype is mandatory when given absence type issubtype checkbox marked as
      // 'Yes'
      if (absence.getEhcmAbsenceType().isSubtype()) {
        if (absence.getSubtype() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_Subtype_Mandatory"));
        }
      }

      // enddate should not be less than startdate
      if ((!event.getPreviousState(startdate).equals(event.getCurrentState(startdate)))
          || (event.getPreviousState(endDate) != null
              && (!event.getPreviousState(endDate).equals(event.getCurrentState(endDate))))) {
        log.debug("chkleaveapprove update");
        if (absence.getEndDate() != null
            && absence.getEndDate().compareTo(absence.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_LevStartDateGreaterEndDate"));
        }

        if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
            && !absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          count = absenceDecisionEventDAO.checkAbsenceExistsInSamePeriod(absence);
          if (count.compareTo(BigInteger.ZERO) > 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_AlreadyLeaveTaken"));
          }
        }

      }
      /*
       * if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))) { if
       * (absence.getStartDate().compareTo(absence.getEhcmEmpPerinfo().getHiredate()) <= 0) { throw
       * new OBException(OBMessageUtils.messageBD("EHCM_AbsstdateGreatHire")); } }
       */
      if (!event.getPreviousState(employee).equals(event.getCurrentState(employee))
          || !event.getPreviousState(absencetype).equals(event.getCurrentState(absencetype))) {
        if (absence.getEhcmAbsenceType().getGender() != null && (!absence.getEhcmAbsenceType()
            .getGender().equals(absence.getEhcmEmpPerinfo().getGender()))) {
          log.debug("getEhcmAbsenceType:" + absence.getEhcmEmpPerinfo().getGender());
          log.debug("getEhcmAbsenceType12:" + absence.getEhcmAbsenceType().getGender());

          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsTypeGenDiffEmpGen"));
        }
      }
      if (!event.getPreviousState(employee).equals(event.getCurrentState(employee))
          || !event.getPreviousState(absencetype).equals(event.getCurrentState(absencetype))
          || !event.getPreviousState(startdate).equals(event.getCurrentState(startdate))) {
        if (absence.getEhcmAbsenceType().getExtendAbsenceType() != null) {
          Date levstartdate = new Date(absence.getStartDate().getTime() - 1 * 24 * 3600 * 1000);
          enddate = dateYearFormat.format(levstartdate);
          sql = " as e where e.ehcmEmpLeave.id in ( select e.id from EHCM_Emp_Leave e where   e.ehcmEmpPerinfo.id='"
              + absence.getEhcmEmpPerinfo().getId() + "'  and e.absenceType.id='"
              + absence.getEhcmAbsenceType().getId() + "')";
          leave = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class, sql);
          if (leave.list().size() == 0) {
            sql = " as e where e.ehcmEmpLeave.id in ( select e.id from EHCM_Emp_Leave e where   e.ehcmEmpPerinfo.id='"
                + absence.getEhcmEmpPerinfo().getId() + "'  and e.absenceType.id='"
                + absence.getEhcmAbsenceType().getExtendAbsenceType().getId() + "')";
            leave = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class, sql);
            if (leave.list().size() == 0) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_AbsDelLevMat_Ext"));
            }
            sql += " and e.endDate='" + enddate + "'";
            leave = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class, sql);
            if (leave.list().size() == 0) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_AbsDec_MatLevapp"));
            }
          }
        }
      }
      if (!event.getPreviousState(decisiontype).equals(event.getCurrentState(decisiontype))
          || !event.getPreviousState(absencedays).equals(event.getCurrentState(absencedays))
          || (event.getPreviousState(endDate) != null
              && (!event.getPreviousState(endDate).equals(event.getCurrentState(endDate))))) {

        if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          if (absence.getEndDate() != null) {
            if (absence.getEndDate().compareTo(absence.getOriginalDecisionNo().getEndDate()) >= 0) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_AbsDecOrgDecEndDate"));
            }
          }
        }
        if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          OBQuery<EHCMAbsenceAttendance> objAttendQuery = OBDal.getInstance().createQuery(
              EHCMAbsenceAttendance.class,
              "as e where e.ehcmEmpPerinfo.id='" + absence.getEhcmEmpPerinfo().getId()
                  + "' and e.ehcmAbsenceType.id='" + absence.getEhcmAbsenceType().getId()
                  + "' and e.decisionStatus='I' and e.enabled='Y'  order by e.creationDate desc");
          objAttendQuery.setMaxResult(1);
          if (objAttendQuery.list().size() == 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_AbsDec_CantUpExCoCA"));
          }
        }
      }
      if ((!event.getPreviousState(absencedays).equals(event.getCurrentState(absencedays)))
          && (absence.getAbsenceDays().compareTo(BigDecimal.ZERO) == 0)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_AbsDecZero"));
      }
      if (event.getCurrentState(endDate) == null) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_AbsEndDateMat"));
      }
      if ((!event.getPreviousState(decisiontype).equals(event.getCurrentState(decisiontype)))
          || (!event.getPreviousState(employee).equals(event.getCurrentState(employee)))
          || (!event.getPreviousState(absencetype).equals(event.getCurrentState(absencetype)))) {
        if (absence.getEhcmAbsenceType() != null
            && absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          OBQuery<EHCMAbsenceType> abstype = OBDal.getInstance().createQuery(EHCMAbsenceType.class,
              "as  e where e.extendAbsenceType.id='" + absence.getEhcmAbsenceType().getId() + "'");
          if (abstype.list().size() > 0) {
            for (EHCMAbsenceType type : abstype.list()) {
              OBQuery<EHCMAbsenceAttendance> absatt = OBDal.getInstance().createQuery(
                  EHCMAbsenceAttendance.class,
                  " as e where e.ehcmAbsenceType.id='" + type.getId()
                      + "' and e.ehcmEmpPerinfo.id='" + absence.getEhcmEmpPerinfo().getId()
                      + "' and e.enabled='Y' ");
              if (absatt.list().size() > 0) {
                throw new OBException(OBMessageUtils.messageBD("EHCM_AbsExtLevTypTaken"));
              }
            }
          }
        }
      }
      if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
          && !absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        if ((!event.getPreviousState(startdate).equals(event.getCurrentState(startdate)))
            || (event.getPreviousState(endDate) != null
                && (!event.getPreviousState(endDate).equals(event.getCurrentState(endDate))))
            || (!event.getPreviousState(decisiontype).equals(event.getCurrentState(decisiontype)))
            || (!event.getPreviousState(employee).equals(event.getCurrentState(employee)))
            || (!event.getPreviousState(absencetype).equals(event.getCurrentState(absencetype)))
            || (!event.getPreviousState(absencedays).equals(event.getCurrentState(absencedays)))
            || (absence.getEhcmAbsenceType().isSubtype() && event.getPreviousState(subType) != null
                && event.getPreviousState(subType).equals(event.getCurrentState(subType)))) {

          // check exceptional leave
          // chk already leave block exists for particular absence startdate and enddate
          if (absenceIssueDecisionDAO.chkEmpLeavePresentInTwoBlk(absence)) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_AbsExcLevDiffBlock"));
          }

          chkapp = chkleaveapprove(absence);
          if (!chkapp.equals("Success")) {
            throw new OBException(chkapp);
          }
        }
      }
      // if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
      // || !event.getPreviousState(employee).equals(event.getCurrentState(employee))) {
      // EmploymentInfo employinfo = absenceIssueDecisionDAO
      // .getHiringEmployInfo(absence.getEhcmEmpPerinfo());// sa.elm.ob.hcm.util.Utility
      // // .getActiveEmployInfo(absence.getEhcmEmpPerinfo().getId());
      // if (absence.getStartDate().compareTo(employinfo.getStartDate()) == -1
      // || absence.getStartDate().compareTo(employinfo.getStartDate()) == 0) {
      // throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_StartDate"));
      // }
      // }

      if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
          && !absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)
          && !absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
        if (absence.getOriginalDecisionNo() != null && absence.getOriginalDecisionNo()
            .getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSecondment_CantUpdate"));
        }
      }

      // cant able to create cutoff for cutoff employment
      if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        if (absence.getOriginalDecisionNo() != null && absence.getOriginalDecisionNo()
            .getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsCutOffCant"));
        }
      }

      if (!event.getPreviousState(absencedays).equals(event.getCurrentState(absencedays))) {
        if ((absence.getAbsenceDays().doubleValue() - (absence.getAbsenceDays().intValue())) != 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_DecimalValueNotAllow"));
        }
      }
      if (!event.getPreviousState(absencedays).equals(event.getCurrentState(absencedays))
          || !event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || (event.getPreviousState(endDate) != null
              && (!event.getPreviousState(endDate).equals(event.getCurrentState(endDate))))) {
        calDays = UtilityDAO.caltheDaysUsingGreDate(absence.getStartDate(), absence.getEndDate());

        countOfHoliday = absenceIssueDecisionDAO.countofHolidays(absence.getStartDate(),
            absence.getEndDate(), absence.getClient().getId(),
            absence.getEhcmAbsenceType().isInculdeholiday());

        if (calDays == countOfHoliday) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsHoliday_NotAllow"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while updating Absence Decion   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"), e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void onSave(@Observes EntityNewEvent event) {
    String chkapp = "";
    BigInteger count = BigInteger.ZERO;
    OBQuery<EHCMEmpLeaveln> leave = null;
    String sql = "";
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    int countOfHoliday = 0;
    int calDays = 0;
    if (!isValidEvent(event)) {
      return;
    }
    try {

      OBContext.setAdminMode();
      EHCMAbsenceAttendance absence = (EHCMAbsenceAttendance) event.getTargetInstance();
      log.debug("chkleaveapprove save");
      String enddate = Utility.formatDate(absence.getEndDate());

      // checking absence start date should be greater than employee hiring startdate or not
      EmploymentInfo employinfo = sa.elm.ob.hcm.util.Utility
          .getHiringEmployInfo(absence.getEhcmEmpPerinfo().getId()); // sa.elm.ob.hcm.util.Utility
      // .getActiveEmployInfo(absence.getEhcmEmpPerinfo().getId());

      if (absence.getStartDate().compareTo(employinfo.getStartDate()) == -1
          || absence.getStartDate().compareTo(employinfo.getStartDate()) == 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_StartDate"));
      }

      // original decision no is mandatory
      if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        if (absence.getOriginalDecisionNo() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
      }

      // subtype is mandatory when given absence type issubtype checkbox marked as
      // 'Yes'
      if (absence.getEhcmAbsenceType().isSubtype()) {
        if (absence.getSubtype() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_Subtype_Mandatory"));
        }
      }
      //
      if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        OBQuery<EHCMAbsenceAttendance> objAttendQuery = OBDal.getInstance().createQuery(
            EHCMAbsenceAttendance.class,
            "as e where e.ehcmEmpPerinfo.id='" + absence.getEhcmEmpPerinfo().getId()
                + "' and e.ehcmAbsenceType.id='" + absence.getEhcmAbsenceType().getId()
                + "' and e.decisionStatus='I' and e.enabled='Y'  order by e.creationDate desc");
        objAttendQuery.setFilterOnReadableClients(false);
        objAttendQuery.setFilterOnReadableOrganization(false);
        objAttendQuery.setMaxResult(1);
        if (objAttendQuery.list().size() == 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsDec_CantUpExCoCA"));
        }
      }
      // enddate should not be less than startdate
      if (absence.getEndDate() != null
          && absence.getEndDate().compareTo(absence.getStartDate()) == -1) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_LevStartDateGreaterEndDate"));
      }
      // in cutoff enddate should be less than original decision enddate
      if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        if (absence.getEndDate() != null) {
          if (absence.getEndDate().compareTo(absence.getOriginalDecisionNo().getEndDate()) >= 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_AbsDecOrgDecEndDate"));
          }
        }
      }

      // in create, update and extend case, checking leave is exists in same period or
      // not
      if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
          && !absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        count = absenceDecisionEventDAO.checkAbsenceExistsInSamePeriod(absence);
        if (count.compareTo(BigInteger.ZERO) > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AlreadyLeaveTaken"));
        }
      }
      /*
       * // if given absence start date less than employee startdate then throw error if
       * (absence.getStartDate().compareTo(absence.getEhcmEmpPerinfo().getHiredate()) <= 0) { throw
       * new OBException(OBMessageUtils.messageBD("EHCM_AbsstdateGreatHire")); }
       */

      // if given absence start date less than employee hiredate then throw error
      if (absence.getStartDate().compareTo(absence.getEhcmEmpPerinfo().getHiredate()) <= 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_AbsstdateGreatHire"));
      }

      // if absence type gender is differ from employee gender then throw error
      if (absence.getEhcmAbsenceType().getGender() != null && (!absence.getEhcmAbsenceType()
          .getGender().equals(absence.getEhcmEmpPerinfo().getGender()))) {
        log.debug("getEhcmAbsenceType:" + absence.getEhcmEmpPerinfo().getGender());
        log.debug("getEhcmAbsenceType12:" + absence.getEhcmAbsenceType().getGender());
        throw new OBException(OBMessageUtils.messageBD("EHCM_AbsTypeGenDiffEmpGen"));
      }

      // if absence type associated with extend absence type then before apply the
      // leave need to
      // check
      // whether extend absence type is taken or not
      if (absence.getEhcmAbsenceType().getExtendAbsenceType() != null) {
        Date levstartdate = new Date(absence.getStartDate().getTime() - 1 * 24 * 3600 * 1000);
        enddate = dateYearFormat.format(levstartdate);
        sql = " as e where e.ehcmEmpLeave.id in ( select e.id from EHCM_Emp_Leave e where   e.ehcmEmpPerinfo.id='"
            + absence.getEhcmEmpPerinfo().getId() + "'  and e.absenceType.id='"
            + absence.getEhcmAbsenceType().getId() + "')";
        leave = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class, sql);
        log.debug("st:" + leave.getWhereAndOrderBy());
        if (leave.list().size() == 0) {
          sql = " as e where e.ehcmEmpLeave.id in ( select e.id from EHCM_Emp_Leave e where   e.ehcmEmpPerinfo.id='"
              + absence.getEhcmEmpPerinfo().getId() + "'  and e.absenceType.id='"
              + absence.getEhcmAbsenceType().getExtendAbsenceType().getId() + "')";
          leave = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class, sql);
          log.debug("st:" + leave.getWhereAndOrderBy());
          if (leave.list().size() == 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_AbsDelLevMat_Ext"));
          }
          sql += " and e.endDate='" + enddate + "'";
          leave = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class, sql);
          log.debug("st:" + leave.getWhereAndOrderBy());
          if (leave.list().size() == 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_AbsDec_MatLevapp"));
          }
        }

      }

      if (absence.getEhcmAbsenceType() != null
          && absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        OBQuery<EHCMAbsenceType> abstype = OBDal.getInstance().createQuery(EHCMAbsenceType.class,
            "as  e where e.extendAbsenceType.id='" + absence.getEhcmAbsenceType().getId() + "'");
        if (abstype.list().size() > 0) {
          for (EHCMAbsenceType absencetype : abstype.list()) {
            OBQuery<EHCMAbsenceAttendance> absatt = OBDal.getInstance().createQuery(
                EHCMAbsenceAttendance.class,
                " as e where e.ehcmAbsenceType.id='" + absencetype.getId()
                    + "' and e.ehcmEmpPerinfo.id='" + absence.getEhcmEmpPerinfo().getId()
                    + "' and e.enabled='Y' ");
            if (absatt.list().size() > 0) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_AbsExtLevTypTaken"));
            }
          }
        }
      }
      if (absence.getAbsenceDays().compareTo(BigDecimal.ZERO) == 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_AbsDecZero"));
      }
      // enddate is mandatory
      if (absence.getEndDate() == null) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_AbsEndDateMat"));
      }
      if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
          && !absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        // check exceptional leave
        // chk already leave block exists for particular absence startdate and enddate
        if (absenceIssueDecisionDAO.chkEmpLeavePresentInTwoBlk(absence)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsExcLevDiffBlock"));
        }
        chkapp = chkleaveapprove(absence);
        if (!chkapp.equals("Success")) {
          throw new OBException(chkapp);
        }
      }

      // EmploymentInfo employinfo = absenceIssueDecisionDAO
      // .getHiringEmployInfo(absence.getEhcmEmpPerinfo()); // sa.elm.ob.hcm.util.Utility
      // // .getActiveEmployInfo(absence.getEhcmEmpPerinfo().getId());
      //
      // if (absence.getStartDate().compareTo(employinfo.getStartDate()) == -1
      // || absence.getStartDate().compareTo(employinfo.getStartDate()) == 0) {
      // throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTransfer_StartDate"));
      // }

      if (!absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
          && !absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)
          && !absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
        if (absence.getOriginalDecisionNo() != null && absence.getOriginalDecisionNo()
            .getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSecondment_CantUpdate"));
        }
      }

      // cant able to create cutoff for cutoff employment
      if (absence.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        if (absence.getOriginalDecisionNo() != null && absence.getOriginalDecisionNo()
            .getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsCutOffCant"));
        }
      }
      if ((absence.getAbsenceDays().doubleValue() - (absence.getAbsenceDays().intValue())) != 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_DecimalValueNotAllow"));
      }

      calDays = UtilityDAO.caltheDaysUsingGreDate(absence.getStartDate(), absence.getEndDate());

      countOfHoliday = absenceIssueDecisionDAO.countofHolidays(absence.getStartDate(),
          absence.getEndDate(), absence.getClient().getId(),
          absence.getEhcmAbsenceType().isInculdeholiday());

      if (calDays == countOfHoliday) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_AbsHoliday_NotAllow"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating Absence Decion   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public BigInteger calculatedays(String startdate, String enddate, String clientId) {
    BigInteger days = BigInteger.ZERO;
    Query query = null;
    String strQuery = "";
    try {
      strQuery = " select count(*) as total from eut_hijri_dates  where gregorian_date >= '"
          + startdate + "' and gregorian_date <= '" + enddate + "'";
      query = OBDal.getInstance().getSession().createSQLQuery(strQuery);
      log.debug("strQuery:" + query);
      log.debug("size123:" + query.list().size());
      if (query != null && query.list().size() > 0) {
        log.debug("geto" + query.list().get(0));
        // Object row = query.list().get(0);
        days = (BigInteger) query.list().get(0);
      }
    } catch (Exception e) {
      log.error("Exception in calculatedays", e);
      e.printStackTrace();
    }
    return days;
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

  public String chkleaveapprove(EHCMAbsenceAttendance absence) {
    String message = "", chkappmessage = "";
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {

      SQLQuery Query = OBDal.getInstance().getSession()
          .createSQLQuery(" select ehcm_levcalprocess(?, ?, ?, ?, ?,?,?,?,?);");
      log.debug("Query:" + Query.toString());
      Query.setParameter(0, absence.getEhcmEmpPerinfo().getId());
      Query.setParameter(1, dateYearFormat.format(absence.getStartDate()));
      Query.setParameter(2, dateYearFormat.format(absence.getEndDate()));
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
      log.debug("getStartDate:" + dateYearFormat.format(absence.getStartDate()));
      log.debug("getEndDate:" + dateYearFormat.format(absence.getEndDate()));
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

      chkappmessage = OBMessageUtils.messageBD(message);
      log.debug("chkapp:" + message.contains("EHCM_LevNotAvailable"));
      if (!chkappmessage.equals("Success")) {
        if (message.equals("EHCM_LLTF")) {
          OBQuery<EHCMAbsenceTypeRules> rule = OBDal.getInstance()
              .createQuery(EHCMAbsenceTypeRules.class, " code='LLTF'");
          rule.setMaxResult(1);
          if (rule.list().size() > 0) {
            EHCMAbsenceTypeRules absrule = rule.list().get(0);
            log.debug("getCondition:" + absrule.getCondition().split("<=")[1].toString());
            String input = absrule.getCondition().split("<=")[1].toString();
            log.debug("input:" + input);
            message = OBMessageUtils.messageBD(message);
            message = message.replace("%", input);
            return OBMessageUtils.messageBD(message);
          }
        } else if (message.contains("EHCM_LevNotAvailable")) {
          String output = message.split("-")[1];
          log.debug("output:" + output);
          message = OBMessageUtils.messageBD(message.split("-")[0]);
          message = message.replace("%", output);
          log.debug("chkapp:" + message);
          return OBMessageUtils.messageBD(message);
        } else {
          String output = message.split("_")[1];
          log.debug("output:" + output);
          message = OBMessageUtils.messageBD("EHCM_AbsDecisionLevApp_Error");
          message = message.replace("%", output);
          log.debug("chkapp:" + message);
          return OBMessageUtils.messageBD(message);
        }
      }
      return OBMessageUtils.messageBD(message);

    } catch (final Exception e) {
      e.printStackTrace();
      log.error("Exception in chkleaveapprove() Method : ", e);
    }
    return message;
  }
}
