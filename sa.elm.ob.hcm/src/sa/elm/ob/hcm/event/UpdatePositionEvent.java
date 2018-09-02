package sa.elm.ob.hcm.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EhcmUpdatePosition;

/*
 * Author J.Divya on 15/10/2016
 * 
 */

/*
 * Event for not allow to delete the issued records.
 */
public class UpdatePositionEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmUpdatePosition.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());

  public void onSave(@Observes EntityNewEvent event) {
    String active = "";
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EhcmUpdatePosition upposition = (EhcmUpdatePosition) event.getTargetInstance();

      if (upposition.getEhcmPosition() == null) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_UpPos_WrongPositon"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating Position  " + e);
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
      EhcmUpdatePosition updateposition = (EhcmUpdatePosition) event.getTargetInstance();

      if (updateposition.isSueDecision()) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_POS_CANNOTDELETE"));
      }

    } catch (OBException e) {
      log.error(" Exception while Delete the Update Position : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
