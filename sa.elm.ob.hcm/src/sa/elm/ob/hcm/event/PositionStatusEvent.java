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

import sa.elm.ob.hcm.EhcmPosStatus;

public class PositionStatusEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmPosStatus.ENTITY_NAME) };

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
      EhcmPosStatus posstatus = (EhcmPosStatus) event.getTargetInstance();
      final Property code = entities[0].getProperty(EhcmPosStatus.PROPERTY_SEARCHKEY);
      final Property name = entities[0].getProperty(EhcmPosStatus.PROPERTY_COMMERCIALNAME);

      OBQuery<EhcmPosStatus> type = OBDal.getInstance().createQuery(EhcmPosStatus.class,
          "  commercialName='" + posstatus.getCommercialName() + "' and client.id ='"
              + posstatus.getClient().getId() + "' ");
      if (!event.getPreviousState(name).equals(event.getCurrentState(name))) {

        if (type.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));

        }
      }
      OBQuery<EhcmPosStatus> type1 = OBDal.getInstance().createQuery(EhcmPosStatus.class,
          "  searchKey='" + posstatus.getSearchKey() + "' and client.id ='"
              + posstatus.getClient().getId() + "' ");
      if (!event.getPreviousState(code).equals(event.getCurrentState(code))) {
        if (type1.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));

        }
      }
      if (posstatus.getEndDate() != null) {
        if (posstatus.getEndDate().compareTo(posstatus.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_todate"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating positionstatus " + e);
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
      EhcmPosStatus posstatus = (EhcmPosStatus) event.getTargetInstance();
      OBQuery<EhcmPosStatus> type = OBDal.getInstance().createQuery(EhcmPosStatus.class,
          " ( commercialName='" + posstatus.getCommercialName() + "' or searchKey = '"
              + posstatus.getSearchKey() + "' ) and client.id ='" + posstatus.getClient().getId()
              + "' ");
      log.debug("sizx:" + type.list().size());

      if (type.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));
      }
      if (posstatus.getEndDate() != null) {
        if (posstatus.getEndDate().compareTo(posstatus.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_todate"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating positionstatus  " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
