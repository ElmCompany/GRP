package sa.elm.ob.hcm.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EhcmPositionHistory;

public class PositionHistoryEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmPositionHistory.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EhcmPositionHistory positionhist = (EhcmPositionHistory) event.getTargetInstance();
      log.error(" positionhist.event: " + positionhist.getEhcmPosition().getId());
      EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class,
          positionhist.getEhcmPosition().getId());
      log.error(" positionhist.issued: " + position.isSued());
      if (position.isSued()) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_POS_CANNOTDELETE"));
      }
    } catch (OBException e) {
      log.error(" Exception while Delete Position  History: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
