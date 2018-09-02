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

import sa.elm.ob.hcm.ehcmgradesteps;

public class GradestepsEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ehcmgradesteps.ENTITY_NAME) };

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
      ehcmgradesteps gradesteps = (ehcmgradesteps) event.getTargetInstance();

      final Property typename = entities[0].getProperty(ehcmgradesteps.PROPERTY_NAME);

      OBQuery<ehcmgradesteps> type = OBDal.getInstance().createQuery(ehcmgradesteps.class,
          "  name='" + gradesteps.getName() + "' and client.id ='" + gradesteps.getClient().getId()
              + "' ");
      if (!event.getPreviousState(typename).equals(event.getCurrentState(typename))) {

        if (type.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradename"));

        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating gradestepsevent: " + e);
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
      ehcmgradesteps gradesteps = (ehcmgradesteps) event.getTargetInstance();
      OBQuery<ehcmgradesteps> type = OBDal.getInstance().createQuery(ehcmgradesteps.class,
          "  name='" + gradesteps.getName() + "' and client.id ='" + gradesteps.getClient().getId()
              + "' ");
      if (type.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradename"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating gradestepsevent" + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
