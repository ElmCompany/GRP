package sa.elm.ob.hcm.event;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

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
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmpSecondment;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.event.dao.EmpSecondmentEventDAO;
import sa.elm.ob.hcm.event.dao.EmpSecondmentEventDAOImpl;
import sa.elm.ob.utility.util.Utility;

public class EmpSecondmentEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMEmpSecondment.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());
  public static String DECISION_TYPE_CREATE = DecisionTypeConstants.DECISION_TYPE_CREATE;
  public static String DECISION_TYPE_UPDATE = DecisionTypeConstants.DECISION_TYPE_UPDATE;
  public static String DECISION_TYPE_CANCEL = DecisionTypeConstants.DECISION_TYPE_CANCEL;
  public static String DECISION_TYPE_CUTOFF = DecisionTypeConstants.DECISION_TYPE_CUTOFF;
  public static String DECISION_TYPE_EXTEND = DecisionTypeConstants.DECISION_TYPE_EXTEND;
  public static String CHANGEREASON_CUTOFF_SECONDMENT = DecisionTypeConstants.CHANGEREASON_CUTOFF_SECONDMENT;
  public static String CHANGEREASON_EXTEND_SECONDMENT = DecisionTypeConstants.CHANGEREASON_EXTEND_SECONDMENT;
  public static String PERIODTYPE_YEAR = "Y";
  public static String PERIODTYPE_MONTH = "M";
  public static String PERIODTYPE_DAY = "D";
  DateFormat yearFormat = Utility.YearFormat;

  EmpSecondmentEventDAO empSecondmentEventDAO = new EmpSecondmentEventDAOImpl();

  // update event
  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();

      final Property decisionType = entities[0]
          .getProperty(EHCMEmpSecondment.PROPERTY_DECISIONTYPE);
      final Property startdate = entities[0].getProperty(EHCMEmpSecondment.PROPERTY_STARTDATE);
      final Property enddate = entities[0].getProperty(EHCMEmpSecondment.PROPERTY_ENDDATE);
      final Property period = entities[0].getProperty(EHCMEmpSecondment.PROPERTY_PERIOD);
      final Property periodtype = entities[0].getProperty(EHCMEmpSecondment.PROPERTY_PERIODTYPE);
      final Property person = entities[0].getProperty(EHCMEmpSecondment.PROPERTY_EHCMEMPPERINFO);
      final Property personinfo = entities[0]
          .getProperty(EHCMEmpSecondment.PROPERTY_EHCMEMPPERINFO);

      String periodType = null;

      Boolean yearValidation = false;
      int currentMonth = 0;

      List<EmployeeDelegation> delList = new ArrayList<EmployeeDelegation>();
      EhcmPosition position = null;
      EmploymentInfo employinfo = null;

      EHCMEmpSecondment secondment = (EHCMEmpSecondment) event.getTargetInstance();
      String decType = secondment.getDecisionType();
      periodType = secondment.getPeriodType();

      /* current active employment details */
      employinfo = sa.elm.ob.hcm.util.UtilityDAO
          .getActiveEmployInfo(secondment.getEhcmEmpPerinfo().getId());
      log.debug("empInfo:" + employinfo);

      if (!event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {

        // cant able to create update/extend/cancel for cutoff /hiring employment

        // Task No.6624
        if (decType.equals(DECISION_TYPE_UPDATE) || decType.equals(DECISION_TYPE_EXTEND)) {
          if ((employinfo.getChangereason().equals("H")
              || employinfo.getChangereason().equals("COSEC")) && employinfo.isEnabled()) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSecondment_CantUpdate"));
          }
        }

        /* cant able to create cutoff for cutoff employment */
        if (decType.equals(DECISION_TYPE_CUTOFF)) {
          if (employinfo.getChangereason().equals(CHANGEREASON_CUTOFF_SECONDMENT)
              && employinfo.isEnabled()) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_CutoffDontAllow"));
          }
        }
        /* cant able to create create secondment for extend employment */
        if (decType.equals(DECISION_TYPE_CREATE)) {
          if (employinfo.getChangereason().equals(CHANGEREASON_EXTEND_SECONDMENT)
              && employinfo.isEnabled()) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSecondment_CantCreate"));
          }
        }
        /* original decision no is mandatory */
        if (!decType.equals(DECISION_TYPE_CREATE)) {
          if (secondment.getOriginalDecisionsNo() == null)
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
        }
        // cutoff the secondment need to check secondment enddate should be less the current
        // employment enddate
        if (decType.equals(DECISION_TYPE_CUTOFF)) {
          if (employinfo.getEndDate() != null) {
            if (secondment.getEndDate().compareTo(employinfo.getEndDate()) > 0) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_DecisionDateLess"));
            }
          }
        }
      }

      /* checking startdate should not be lesser than current employment startdate */
      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))
          || !event.getPreviousState(person).equals(event.getCurrentState(person))) {
        if (decType.equals(DECISION_TYPE_CREATE) || decType.equals(DECISION_TYPE_UPDATE)) {
          EmploymentInfo currentInfo = empSecondmentEventDAO
              .getCurrentEmplyInfoStartDate(secondment);
          if (secondment.getStartDate().compareTo(currentInfo.getStartDate()) <= 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_PromStartDate"));
          }
        }
        if (decType.equals(DECISION_TYPE_CREATE)) {
          if (employinfo.getChangereason().equals(CHANGEREASON_CUTOFF_SECONDMENT)) {
            if (secondment.getStartDate().compareTo(employinfo.getStartDate()) <= 0) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_PromStartDate"));
            }
          }
        }
      }
      /* checking period does not exists more than one year or 12 months */
      if (!event.getPreviousState(periodtype).equals(event.getCurrentState(periodtype))
          || (!event.getPreviousState(period).equals(event.getCurrentState(period)))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))
          || !event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(enddate).equals(event.getCurrentState(enddate))) {

        /* cant able to create secondment for same period */
        if (decType.equals(DECISION_TYPE_CREATE)) {
          if (empSecondmentEventDAO.chkCrtSecndSamePeriod(secondment)) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_CreCant"));
          }
        }
        /* six year validation */
        if (!decType.equals(DECISION_TYPE_CANCEL) && !decType.equals(DECISION_TYPE_CUTOFF)) {

          yearValidation = empSecondmentEventDAO.YearValidationForSecondment(secondment, decType,
              Constants.SecondmentMaxYear);
          if (yearValidation == Boolean.FALSE) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSecMoreThanSixYr"));
          }
        }
        // three year validation
        if (decType.equals(DECISION_TYPE_EXTEND) || decType.equals(DECISION_TYPE_CREATE)
            || decType.equals(DECISION_TYPE_UPDATE)) {
          yearValidation = empSecondmentEventDAO.threeYearValidationForSecondment(secondment,
              decType, Constants.SecondmentBlockYear);

          log.debug("yearValidations:" + yearValidation);
          if (yearValidation == Boolean.FALSE) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_DontAllowThreeYr"));
          }
        }
        /* one year validation */
        if ((new BigDecimal(secondment.getPeriod()).compareTo(BigDecimal.ONE)) > 0
            && periodType.equals(PERIODTYPE_YEAR)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_OneYearMore"));
        }

        if ((new BigDecimal(secondment.getPeriod()).compareTo(new BigDecimal(12))) > 0
            && periodType.equals(PERIODTYPE_MONTH)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_OneYearMore"));
        }

        if (periodType.equals(PERIODTYPE_DAY)
            && !empSecondmentEventDAO.oneYearDayValidation(secondment.getStartDate(),
                secondment.getClient().getId(), BigInteger.valueOf(secondment.getPeriod()))) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_OneYearMore"));
        }
        /* checking for one year validation after promotion */
        if (secondment.getStartDate() != null && (decType.equals(DECISION_TYPE_CREATE)
            || (secondment.getOriginalDecisionsNo() != null && secondment.getOriginalDecisionsNo()
                .getDecisionType().equals(DECISION_TYPE_CREATE)))) {
          if (!empSecondmentEventDAO.promotionVal(secondment)) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_DontAllowProm"));
          }
        }
      }
      // checking delegation or transfer exists
      if (!event.getPreviousState(personinfo).equals(event.getCurrentState(personinfo))) {

        /* checking delegation exists */
        if (secondment.getEhcmEmpPerinfo() != null) {
          delList = empSecondmentEventDAO.getDelegationList(employinfo);
          if (delList.size() > 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_NotAllowDele"));
          }
        }
      }
      if (!event.getPreviousState(person).equals(event.getCurrentState(person))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {

        if (decType.equals(DECISION_TYPE_CANCEL)
            || employinfo.getChangereason().equals(CHANGEREASON_CUTOFF_SECONDMENT)) {
          if (secondment.getEhcmEmpPerinfo() != null) {
            position = empSecondmentEventDAO.getDelegationPosition(secondment);
            if (position != null && position.getDelegatedEmployee() != null) {
              if (!position.getDelegatedEmployee().getId()
                  .equals(secondment.getEhcmEmpPerinfo().getId())) {
                throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_CantCancel"));
              }
            }
          }
        }
      }
      if (!event.getPreviousState(person).equals(event.getCurrentState(person))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))
          || !event.getPreviousState(periodtype).equals(event.getCurrentState(periodtype))
          || (!event.getPreviousState(period).equals(event.getCurrentState(period)))
          || !event.getPreviousState(enddate).equals(event.getCurrentState(enddate))) {
        if (decType.equals(DECISION_TYPE_CUTOFF) && secondment.getOriginalDecisionsNo() != null) {
          position = empSecondmentEventDAO.getDelegationPosition(secondment);
          if (secondment.getEndDate()
              .compareTo(secondment.getOriginalDecisionsNo().getEndDate()) >= 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_CutOffEndDateLTExtEndDate"));
          }
        }
      }

      // enddate should not be less than startdate
      if ((!event.getPreviousState(startdate).equals(event.getCurrentState(startdate)))
          || (event.getPreviousState(enddate) != null
              && (!event.getPreviousState(enddate).equals(event.getCurrentState(enddate))))) {
        if (secondment.getEndDate() != null
            && secondment.getEndDate().compareTo(secondment.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_LevStartDateGreaterEndDate"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while updating Employee secondment  " + e);
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
      OBContext.setAdminMode();
      EmploymentInfo employinfo = null;
      String decisionType = null;
      String periodType = null;
      int currentMonth = 0;
      int month = 0;
      Boolean yearValidation = false;

      EHCMEmpSecondment secondment = (EHCMEmpSecondment) event.getTargetInstance();
      List<EmployeeDelegation> delList = new ArrayList<EmployeeDelegation>();
      decisionType = secondment.getDecisionType();
      periodType = secondment.getPeriodType();
      EhcmPosition position = null;

      /* current active employment details */
      employinfo = sa.elm.ob.hcm.util.UtilityDAO
          .getActiveEmployInfo(secondment.getEhcmEmpPerinfo().getId());

      // cant able to create update/extend/cancel for cutoff /hiring employment
      // Task No.6624
      if (decisionType.equals(DECISION_TYPE_UPDATE) || decisionType.equals(DECISION_TYPE_EXTEND)) {
        if ((employinfo.getChangereason().equals("H")
            || employinfo.getChangereason().equals("COSEC")) && employinfo.isEnabled()) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSecondment_CantUpdate"));
        }
      }

      /* cant able to create secondment for same period */
      if (decisionType.equals(DECISION_TYPE_CREATE)) {
        if (empSecondmentEventDAO.chkCrtSecndSamePeriod(secondment)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_CreCant"));
        }
      }
      /* cant able to create cutoff for cutoff employment */
      if (decisionType.equals(DECISION_TYPE_CUTOFF)) {
        if (employinfo.getChangereason().equals(CHANGEREASON_CUTOFF_SECONDMENT)
            && employinfo.isEnabled()) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_CutoffDontAllow"));
        }
      }
      /* cant able to create create secondment for extend employment */
      if (decisionType.equals(DECISION_TYPE_CREATE)) {
        if (employinfo.getChangereason().equals(CHANGEREASON_EXTEND_SECONDMENT)
            && employinfo.isEnabled()) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSecondment_CantCreate"));
        }
      }
      /* original decision no is mandatory */
      if (!decisionType.equals(DECISION_TYPE_CREATE)) {
        if (secondment.getOriginalDecisionsNo() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
      }
      // checking startdate should not be lesser than current employment startdate
      if (decisionType.equals(DECISION_TYPE_CREATE) || decisionType.equals(DECISION_TYPE_UPDATE)) {
        EmploymentInfo currentInfo = empSecondmentEventDAO.getCurrentEmplyInfoStartDate(secondment);
        if (secondment.getStartDate().compareTo(currentInfo.getStartDate()) <= 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_PromStartDate"));
        }
      }
      if (decisionType.equals(DECISION_TYPE_CREATE)) {
        if (employinfo.getChangereason().equals(CHANGEREASON_CUTOFF_SECONDMENT)) {
          if (secondment.getStartDate().compareTo(employinfo.getStartDate()) <= 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_PromStartDate"));
          }
        }
      }
      // checking period does not exists more than one year or 12 months
      if ((new BigDecimal(secondment.getPeriod()).compareTo(BigDecimal.ONE)) > 0
          && periodType.equals(PERIODTYPE_YEAR)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_OneYearMore"));
      }

      if ((new BigDecimal(secondment.getPeriod()).compareTo(new BigDecimal(12))) > 0
          && periodType.equals(PERIODTYPE_MONTH)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_OneYearMore"));
      }

      if (periodType.equals(PERIODTYPE_DAY)
          && !empSecondmentEventDAO.oneYearDayValidation(secondment.getStartDate(),
              secondment.getClient().getId(), BigInteger.valueOf(secondment.getPeriod()))) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_OneYearMore"));
      }
      // cutoff the secondment need to check secondment enddate should be less the current
      // employment enddate
      if (decisionType.equals(DECISION_TYPE_CUTOFF)) {
        if (employinfo.getEndDate() != null) {
          if (secondment.getEndDate().compareTo(employinfo.getEndDate()) > 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_DecisionDateLess"));
          }
        }
      }
      // checking for one year validation after promotion
      if (secondment.getStartDate() != null && (decisionType.equals(DECISION_TYPE_CREATE)
          || (secondment.getOriginalDecisionsNo() != null && secondment.getOriginalDecisionsNo()
              .getDecisionType().equals(DECISION_TYPE_CREATE)))) {
        if (!empSecondmentEventDAO.promotionVal(secondment)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_DontAllowProm"));
        }
      }
      /* six year validation */
      if (!decisionType.equals(DECISION_TYPE_CANCEL)
          && !decisionType.equals(DECISION_TYPE_CUTOFF)) {
        yearValidation = empSecondmentEventDAO.YearValidationForSecondment(secondment, decisionType,
            Constants.SecondmentMaxYear);
        if (yearValidation == Boolean.FALSE) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSecMoreThanSixYr"));
        }
      }
      /* three year validation */
      if (decisionType.equals(DECISION_TYPE_EXTEND) || decisionType.equals(DECISION_TYPE_CREATE)
          || decisionType.equals(DECISION_TYPE_UPDATE)) {

        yearValidation = empSecondmentEventDAO.threeYearValidationForSecondment(secondment,
            decisionType, Constants.SecondmentBlockYear);
        if (yearValidation == Boolean.FALSE) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_DontAllowThreeYr"));
        }
      }
      /* checking delegation exists */
      if (secondment.getEhcmEmpPerinfo() != null) {
        delList = empSecondmentEventDAO.getDelegationList(employinfo);
        if (delList.size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_NotAllowDele"));
        }
      }
      if (decisionType.equals(DECISION_TYPE_CANCEL)
          || employinfo.getChangereason().equals(CHANGEREASON_CUTOFF_SECONDMENT)) {
        if (secondment.getEhcmEmpPerinfo() != null) {
          position = empSecondmentEventDAO.getDelegationPosition(secondment);
          if (position != null && position.getDelegatedEmployee() != null) {
            if (!position.getDelegatedEmployee().getId()
                .equals(secondment.getEhcmEmpPerinfo().getId())) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_CantCancel"));
            }
          }
        }
      }

      if (decisionType.equals(DECISION_TYPE_CUTOFF)
          && secondment.getOriginalDecisionsNo() != null) {
        position = empSecondmentEventDAO.getDelegationPosition(secondment);
        if (secondment.getEndDate()
            .compareTo(secondment.getOriginalDecisionsNo().getEndDate()) >= 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_CutOffEndDateLTExtEndDate"));
        }
      }

      if (secondment.getEndDate() != null
          && secondment.getEndDate().compareTo(secondment.getStartDate()) == -1) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_LevStartDateGreaterEndDate"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating Employee secondment   " + e);
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
      EHCMEmpSecondment secondment = (EHCMEmpSecondment) event.getTargetInstance();
      if (secondment.getDecisionStatus().equals("I")) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSec_CantDele"));
      }

    } catch (OBException e) {
      log.error(" Exception while secondment ondelete event : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
