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

import sa.elm.ob.hcm.EHCMEventGroup;

/**
 * @author Priyanka Ranjan on 18/01/2017
 */

public class EhcmEventGroupHeaderEvent extends EntityPersistenceEventObserver {

  /**
   * Event for validate the Unique record with code and name in "Event Group" Window header
   */

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMEventGroup.ENTITY_NAME) };

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
      EHCMEventGroup Checkrecord = (EHCMEventGroup) event.getTargetInstance();
      final Property code = entities[0].getProperty(EHCMEventGroup.PROPERTY_EVENTGROUPCODE);
      final Property name = entities[0].getProperty(EHCMEventGroup.PROPERTY_EVENTGROUPNAME);
      OBQuery<EHCMEventGroup> uniquecode = OBDal.getInstance().createQuery(EHCMEventGroup.class,
          "as e where e.eventGroupCode='" + Checkrecord.getEventGroupCode() + "' and e.client.id='"
              + Checkrecord.getClient().getId() + "'");
      if (!event.getPreviousState(code).equals(event.getCurrentState(code))) {
        if (uniquecode.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Evtgrp_UniCode"));
        }
      }
      OBQuery<EHCMEventGroup> uniquename = OBDal.getInstance().createQuery(EHCMEventGroup.class,
          "as e where e.eventGroupName='" + Checkrecord.getEventGroupName() + "' and e.client.id='"
              + Checkrecord.getClient().getId() + "'");
      if (!event.getPreviousState(name).equals(event.getCurrentState(name))) {
        if (uniquename.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Evtgrp_UniName"));
        }
      }
    } catch (OBException e) {
      log.error(" Exception while Updating EventGroup header: " + e);
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
      EHCMEventGroup Checkrecord = (EHCMEventGroup) event.getTargetInstance();
      OBQuery<EHCMEventGroup> uniquecode = OBDal.getInstance().createQuery(EHCMEventGroup.class,
          "as e where e.eventGroupCode='" + Checkrecord.getEventGroupCode() + "' and e.client.id='"
              + Checkrecord.getClient().getId() + "'");
      if (uniquecode.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Evtgrp_UniCode"));
      }
      OBQuery<EHCMEventGroup> uniquename = OBDal.getInstance().createQuery(EHCMEventGroup.class,
          "as e where e.eventGroupName='" + Checkrecord.getEventGroupName() + "' and e.client.id='"
              + Checkrecord.getClient().getId() + "'");
      if (uniquename.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Evtgrp_UniName"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating EventGroup header: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}
