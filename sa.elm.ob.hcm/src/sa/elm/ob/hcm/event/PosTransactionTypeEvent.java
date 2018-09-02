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

import sa.elm.ob.hcm.EhcmPosTransactionType;

public class PosTransactionTypeEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmPosTransactionType.ENTITY_NAME) };

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
      EhcmPosTransactionType posTranType = (EhcmPosTransactionType) event.getTargetInstance();
      final Property code = entities[0].getProperty(EhcmPosTransactionType.PROPERTY_SEARCHKEY);
      final Property name = entities[0].getProperty(EhcmPosTransactionType.PROPERTY_COMMERCIALNAME);

      log.debug("code:" + code);
      log.debug("name:" + name);

      OBQuery<EhcmPosTransactionType> type = OBDal.getInstance().createQuery(
          EhcmPosTransactionType.class, "  commercialName='" + posTranType.getCommercialName()
              + "' and client.id ='" + posTranType.getClient().getId() + "' ");
      if (!event.getPreviousState(name).equals(event.getCurrentState(name))) {

        if (type.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));

        }
      }
      OBQuery<EhcmPosTransactionType> type1 = OBDal.getInstance()
          .createQuery(EhcmPosTransactionType.class, "  searchKey='" + posTranType.getSearchKey()
              + "' and client.id ='" + posTranType.getClient().getId() + "' ");
      if (!event.getPreviousState(code).equals(event.getCurrentState(code))) {
        if (type1.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));

        }
      }
      if (posTranType.getEndDate() != null) {
        if (posTranType.getEndDate().compareTo(posTranType.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_todate"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating Position Transaction Type  " + e);
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
      EhcmPosTransactionType posTranType = (EhcmPosTransactionType) event.getTargetInstance();
      OBQuery<EhcmPosTransactionType> type = OBDal.getInstance().createQuery(
          EhcmPosTransactionType.class,
          " ( commercialName='" + posTranType.getCommercialName() + "' or searchKey = '"
              + posTranType.getSearchKey() + "' ) and client.id ='"
              + posTranType.getClient().getId() + "' ");
      log.debug("sizx:" + type.list().size());

      if (type.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));
      }
      if (posTranType.getEndDate() != null) {
        if (posTranType.getEndDate().compareTo(posTranType.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_todate"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating Position Transaction Type   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
