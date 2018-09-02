package sa.elm.ob.hcm.event;

import javax.enterprise.event.Observes;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMAuthorizationInfo;
import sa.elm.ob.hcm.event.dao.AuthorizationInfoEventDAO;
import sa.elm.ob.hcm.event.dao.AuthorizationInfoEventDAOImpl;

public class Ehcm_authinfo_dateEvent extends EntityPersistenceEventObserver {
  private static final Logger log = LoggerFactory.getLogger(Ehcm_authinfo_dateEvent.class);
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMAuthorizationInfo.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {

    if (!isValidEvent(event)) {
      return;
    }
    try {

      OBContext.setAdminMode();
      final EHCMAuthorizationInfo obj = (EHCMAuthorizationInfo) event.getTargetInstance();
      AuthorizationInfoEventDAO authorizationInfo = new AuthorizationInfoEventDAOImpl();

      if (authorizationInfo.dateOverLapForEndDate(obj)) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Authenddate"));
      }
    } catch (OBException e) {
      log.error(" Exception while creating authorization info   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"), e);
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
      final EHCMAuthorizationInfo obj = (EHCMAuthorizationInfo) event.getTargetInstance();
      AuthorizationInfoEventDAO authorizationInfo = new AuthorizationInfoEventDAOImpl();

      if (authorizationInfo.dateOverLapForEndDate(obj)) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Authenddate"));
      }
    } catch (OBException e) {
      log.error(" Exception while creating authorization info   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"), e);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  public void onDelete(@Observes EntityDeleteEvent event) {

    if (!isValidEvent(event)) {
      return;
    }
    try {
      int a = 0;
      OBContext.setAdminMode();
      final EHCMAuthorizationInfo obj = (EHCMAuthorizationInfo) event.getTargetInstance();
      AuthorizationInfoEventDAO authorizationInfo = new AuthorizationInfoEventDAOImpl();

      if (authorizationInfo.checkAuthorizationDetailUsedInRecords(obj)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_AuthorizationInfo_CantDelete"));
      }
    } catch (OBException e) {
      log.error(" Exception while creating authorization info   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"), e);
    } finally {
      OBContext.restorePreviousMode();
    }

  }
}
