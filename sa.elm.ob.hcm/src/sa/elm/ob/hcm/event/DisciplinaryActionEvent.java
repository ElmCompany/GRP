package sa.elm.ob.hcm.event;

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
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EhcmDisciplineAction;
import sa.elm.ob.hcm.EmploymentInfo;

/**
 * 
 * @author poongodi on 04-05-2018
 *
 */

public class DisciplinaryActionEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmDisciplineAction.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());
  private static final String Deduction_Annual_Leave = "DALB";
  private static final String Deduction_Salary = "DFS";

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      final Property decisionType = entities[0]
          .getProperty(EhcmDisciplineAction.PROPERTY_DECISIONTYPE);
      final Property startdate = entities[0].getProperty(EhcmDisciplineAction.PROPERTY_STARTDATE);

      EhcmDisciplineAction empAction = (EhcmDisciplineAction) event.getTargetInstance();
      DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      String dateString = "2058-06-21";
      Date defaultEndDate = sdf.parse(dateString);
      Date endDate = null;
      if (empAction.getEndDate() == null) {
        endDate = defaultEndDate;
      } else {
        endDate = empAction.getEndDate();
      }
      EmploymentInfo empinfo = sa.elm.ob.hcm.util.UtilityDAO
          .getActiveEmployInfo(empAction.getEmployee().getId());
      // checking startdate should not be lesser than current employment startdate
      if (!event.getPreviousState(startdate).equals(event.getCurrentState(startdate))
          || !event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (empAction.getDecisionType().equals("CR") || empAction.getDecisionType().equals("UP")) {
          if (empAction.getStartDate().compareTo(empinfo.getStartDate()) == -1
              || empAction.getStartDate().compareTo(empinfo.getStartDate()) == 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_PromStartDate"));
          }
        }

      }
      if (empAction.getActionTaken().equals(Deduction_Annual_Leave)
          || empAction.getActionTaken().equals(Deduction_Salary)) {
        if (empAction.getNoOfDays() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_noofDays_Mandatory"));
        }
      }
      if (empAction.getActionTaken().equals(Deduction_Salary)) {
        if (empAction.getAmount() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Amount_Mandatory"));
        }
      }
      // original decision no is mandatory for the case update or cancel
      if (!event.getPreviousState(decisionType).equals(event.getCurrentState(decisionType))) {
        if (empAction.getDecisionType().equals("CA") || empAction.getDecisionType().equals("UP")) {
          if (empAction.getOriginalDecisionNo() == null)
            throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
        }
      }
      if (empAction.getEndDate() != null)
        if (endDate.compareTo(empAction.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EndDate_greaterorequal_StartDate"));
        }

    } catch (OBException e) {
      log.error(" Exception while updating DisciplinaryAction: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
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
      EhcmDisciplineAction empAction = (EhcmDisciplineAction) event.getTargetInstance();
      EmploymentInfo empinfo = sa.elm.ob.hcm.util.UtilityDAO
          .getActiveEmployInfo(empAction.getEmployee().getId());
      // checking startdate should not be lesser than current employment startdate
      if (empAction.getDecisionType().equals("CR") || empAction.getDecisionType().equals("UP")) {
        if (empAction.getStartDate().compareTo(empinfo.getStartDate()) == -1
            || empAction.getStartDate().compareTo(empinfo.getStartDate()) == 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_PromStartDate"));
        }
      }
      if (empAction.getActionTaken().equals(Deduction_Annual_Leave)
          || empAction.getActionTaken().equals(Deduction_Salary)) {
        if (empAction.getNoOfDays() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_noofDays_Mandatory"));
        }
      }
      if (empAction.getActionTaken().equals(Deduction_Salary)) {
        if (empAction.getAmount() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Amount_Mandatory"));
        }
      }
      // original decision no is mandatory for the decision type is update or cancel
      if (empAction.getDecisionType().equals("CA") || empAction.getDecisionType().equals("UP")) {
        if (empAction.getOriginalDecisionNo() == null)
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpTras_OrgDecNo"));
      }
      if (empAction.getEndDate() != null)
        if (empAction.getEndDate().compareTo(empAction.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EndDate_greaterorequal_StartDate"));
        }

    } catch (OBException e) {
      log.error(" Exception while creating DisciplinaryAction " + e);
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
      EhcmDisciplineAction empAction = (EhcmDisciplineAction) event.getTargetInstance();
      if (empAction.getDecisionStatus().equals("I")) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Already_Processed"));
      }

    } catch (OBException e) {
      log.error(" Exception while Deleting DisciplinaryAction : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
