package sa.elm.ob.hcm.event;

import java.util.Date;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EhcmActiontype;

public class Ehcm_ActionTypeEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmActiontype.ENTITY_NAME) };

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
      EhcmActiontype action = (EhcmActiontype) event.getTargetInstance();
      Date startDate = action.getStartDate();
      Date endDate = action.getEndDate();

      if (startDate != null && endDate != null) {
        if (endDate.compareTo(startDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("@Ehcm_Enddate@"));
        }

      }
    } catch (OBException e) {
      log.error(" Exception while creating Action Type: " + e);
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
      EhcmActiontype action = (EhcmActiontype) event.getTargetInstance();
      Date startDate = action.getStartDate();
      Date endDate = action.getEndDate();

      if (startDate != null && endDate != null) {
        if (endDate.compareTo(startDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("@Ehcm_Enddate@"));
        }

      }
    } catch (OBException e) {
      log.error(" Exception while creating Action Type: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
