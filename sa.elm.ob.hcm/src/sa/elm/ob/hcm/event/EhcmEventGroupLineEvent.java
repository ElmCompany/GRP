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

import sa.elm.ob.hcm.EHCMEvgrpDatetrack;

/**
 * @author Priyanka Ranjan on 19/01/2017
 */

public class EhcmEventGroupLineEvent extends EntityPersistenceEventObserver {

  /**
   * Event for validate the Unique record with combination of Update Type,Table,Column in "Date
   * Tracked Event " Event Group Window Line
   */

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMEvgrpDatetrack.ENTITY_NAME) };

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
      EHCMEvgrpDatetrack Checkrecord = (EHCMEvgrpDatetrack) event.getTargetInstance();
      final Property updatetype = entities[0].getProperty(EHCMEvgrpDatetrack.PROPERTY_UPDATETYPE);
      final Property table = entities[0].getProperty(EHCMEvgrpDatetrack.PROPERTY_TABLE);
      final Property column = entities[0].getProperty(EHCMEvgrpDatetrack.PROPERTY_COLUMN);
      if (!event.getPreviousState(updatetype).equals(event.getCurrentState(updatetype))
          || !event.getPreviousState(table).equals(event.getCurrentState(table))
          || !event.getPreviousState(column).equals(event.getCurrentState(column))) {
        OBQuery<EHCMEvgrpDatetrack> lineuniquecode = OBDal.getInstance().createQuery(
            EHCMEvgrpDatetrack.class,
            "as e where e.updateType='" + Checkrecord.getUpdateType() + "' and e.client.id='"
                + Checkrecord.getClient().getId() + "' and e.table='"
                + Checkrecord.getTable().getId() + "' and e.column='"
                + Checkrecord.getColumn().getId() + "' and e.ehcmEventGroup='"
                + Checkrecord.getEhcmEventGroup().getId() + "'");
        if (lineuniquecode.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_evtgrp_DTE_Unique"));
        }
      }
    } catch (OBException e) {
      log.error(" Exception while Updating Date Tracked Event: " + e);
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
      EHCMEvgrpDatetrack Checkrecord = (EHCMEvgrpDatetrack) event.getTargetInstance();
      OBQuery<EHCMEvgrpDatetrack> lineuniquecode = OBDal.getInstance().createQuery(
          EHCMEvgrpDatetrack.class,
          "as e where e.updateType='" + Checkrecord.getUpdateType() + "' and e.client.id='"
              + Checkrecord.getClient().getId() + "' and e.table='" + Checkrecord.getTable().getId()
              + "' and e.column='" + Checkrecord.getColumn().getId() + "' and e.ehcmEventGroup='"
              + Checkrecord.getEhcmEventGroup().getId() + "'");
      if (lineuniquecode.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_evtgrp_DTE_Unique"));
      }
    } catch (OBException e) {
      log.error(" Exception while creating Date Tracked Event: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
