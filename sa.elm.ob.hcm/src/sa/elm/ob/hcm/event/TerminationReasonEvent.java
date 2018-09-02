package sa.elm.ob.hcm.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
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

import sa.elm.ob.hcm.EHCMTerminationReason;

public class TerminationReasonEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMTerminationReason.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      EHCMTerminationReason termreason = (EHCMTerminationReason) event.getTargetInstance();
      final Property code = entities[0].getProperty(EHCMTerminationReason.PROPERTY_SEARCHKEY);
      final Property name = entities[0].getProperty(EHCMTerminationReason.PROPERTY_NAME);
      if (!event.getPreviousState(code).equals(event.getCurrentState(code))
          || !event.getPreviousState(name).equals(event.getCurrentState(name))) {
        OBQuery<EHCMTerminationReason> reason = OBDal.getInstance().createQuery(
            EHCMTerminationReason.class,
            " ( searchKey='" + termreason.getSearchKey() + "' or name='" + termreason.getName()
                + "') and client.id='" + termreason.getClient().getId() + "'");
        if (reason.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));
        }
      }
    } catch (OBException e) {
      log.error(" Exception while updating Termination reason " + e);
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
      EHCMTerminationReason termreason = (EHCMTerminationReason) event.getTargetInstance();
      OBQuery<EHCMTerminationReason> reason = OBDal.getInstance().createQuery(
          EHCMTerminationReason.class, " ( searchKey='" + termreason.getSearchKey() + "' or name='"
              + termreason.getName() + "') and client.id='" + termreason.getClient().getId() + "'");
      if (reason.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));
      }
    } catch (OBException e) {
      log.error(" Exception while creating Termination reason  " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
