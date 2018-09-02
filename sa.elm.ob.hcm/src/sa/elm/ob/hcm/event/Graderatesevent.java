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

import sa.elm.ob.hcm.ehcmgraderates;

public class Graderatesevent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ehcmgraderates.ENTITY_NAME) };

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
      ehcmgraderates graderates = (ehcmgraderates) event.getTargetInstance();
      final Property typename = entities[0].getProperty(ehcmgraderates.PROPERTY_COMMERCIALNAME);

      if (graderates.getEndDate() != null) {
        if (graderates.getEndDate().compareTo(graderates.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EndDateStartDateComp"));
        }
      }

      OBQuery<ehcmgraderates> type = OBDal.getInstance().createQuery(ehcmgraderates.class,
          "  name='" + graderates.getCommercialName() + "' and client.id ='"
              + graderates.getClient().getId() + "' ");
      if (!event.getPreviousState(typename).equals(event.getCurrentState(typename))) {

        if (type.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));

        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating orgtype in graderatesevent: " + e);
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
      ehcmgraderates graderates = (ehcmgraderates) event.getTargetInstance();

      if (graderates.getEndDate() != null) {
        if (graderates.getEndDate().compareTo(graderates.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EndDateStartDateComp"));
        }
      }

      OBQuery<ehcmgraderates> type = OBDal.getInstance().createQuery(ehcmgraderates.class,
          "  name='" + graderates.getCommercialName() + "' and client.id ='"
              + graderates.getClient().getId() + "' ");
      if (type.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating graderatesevent" + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
