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

import sa.elm.ob.hcm.EhcmOrder;

public class OrderEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmOrder.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      EhcmOrder record = (EhcmOrder) event.getTargetInstance();
      final Property fields = entities[0].getProperty(EhcmOrder.PROPERTY_FIELDS);
      final Property sequence = entities[0].getProperty(EhcmOrder.PROPERTY_POSITIONS);

      if (!event.getPreviousState(fields).equals(event.getCurrentState(fields))) {
        OBQuery<EhcmOrder> order = OBDal.getInstance().createQuery(EhcmOrder.class,
            "addressStyle.id='" + record.getAddressStyle().getId() + "' and fields='"
                + record.getFields() + "'");
        if (order.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Field_Added"));
        }
      }
      if (!event.getPreviousState(sequence).equals(event.getCurrentState(sequence))) {
        OBQuery<EhcmOrder> position = OBDal.getInstance().createQuery(EhcmOrder.class,
            "addressStyle.id='" + record.getAddressStyle().getId() + "' and positions='"
                + record.getPositions() + "'");
        if (position.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Same_Position"));
        }

      }
    } catch (OBException e) {
      log.error(" Exception while updating Order in address style: " + e);
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
      EhcmOrder record = (EhcmOrder) event.getTargetInstance();
      OBQuery<EhcmOrder> order = OBDal.getInstance().createQuery(EhcmOrder.class,
          "addressStyle.id='" + record.getAddressStyle().getId() + "' and fields='"
              + record.getFields() + "'");
      if (order.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Field_Added"));
      }
      OBQuery<EhcmOrder> position = OBDal.getInstance().createQuery(EhcmOrder.class,
          "addressStyle.id='" + record.getAddressStyle().getId() + "' and positions='"
              + record.getPositions() + "'");
      if (position.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Same_Position"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating Order in address style: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
