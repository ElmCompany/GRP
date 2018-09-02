package sa.elm.ob.hcm.event;

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

import sa.elm.ob.hcm.EhcmJoiningWorkRequest;

/**
 * @author poongodi on 17/02/2018
 */
public class JoinWorkReasonEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmJoiningWorkRequest.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EhcmJoiningWorkRequest joinLeave = (EhcmJoiningWorkRequest) event.getTargetInstance();
      // check if the join date less than decision date then throw the error
      if (joinLeave.getJoindate() != null && joinLeave.getDecisionDate() != null) {
        if (joinLeave.getJoindate().compareTo(joinLeave.getDecisionDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_JoinLeave_JoinDate"));
        }

      }
    } catch (OBException e) {
      log.error(" Exception while creating JoinWorkReasonEvent   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      EhcmJoiningWorkRequest joinLeave = (EhcmJoiningWorkRequest) event.getTargetInstance();
      final Property joinDate = entities[0].getProperty(EhcmJoiningWorkRequest.PROPERTY_JOINDATE);
      final Property decDate = entities[0]
          .getProperty(EhcmJoiningWorkRequest.PROPERTY_DECISIONDATE);
      // check if the join date less than decision date then throw the error
      if (joinLeave.getJoindate() != null && joinLeave.getDecisionDate() != null) {
        if (!event.getPreviousState(joinDate).equals(event.getCurrentState(joinDate))
            || !event.getPreviousState(decDate).equals(event.getCurrentState(decDate))) {
          if (joinLeave.getJoindate().compareTo(joinLeave.getDecisionDate()) == -1) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_JoinLeave_JoinDate"));
          }
        }
      }
    }

    catch (OBException e) {
      log.error(" Exception while updating JoinWorkReasonEvent   " + e);
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
      EhcmJoiningWorkRequest joinReasonObj = (EhcmJoiningWorkRequest) event.getTargetInstance();
      if (joinReasonObj.getDecisionStatus().equals("I")) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_join_CantDele"));
      }

    } catch (OBException e) {
      log.error(" Exception in joinreason ondelete event : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
