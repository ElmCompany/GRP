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

import sa.elm.ob.hcm.EHCMElementCatgry;

/**
 * @author Priyanka Ranjan on 19/01/2017
 */

public class EhcmElementCategoryEvent extends EntityPersistenceEventObserver {

  /**
   * Event for validate the Unique record with code and name in "Element Category" Window and also
   * default record should be only one
   */

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMElementCatgry.ENTITY_NAME) };

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
      EHCMElementCatgry Checkrecord = (EHCMElementCatgry) event.getTargetInstance();
      final Property code = entities[0].getProperty(EHCMElementCatgry.PROPERTY_CODE);
      final Property name = entities[0].getProperty(EHCMElementCatgry.PROPERTY_NAME);
      final Property isdefault = entities[0].getProperty(EHCMElementCatgry.PROPERTY_DEFAULT);
      OBQuery<EHCMElementCatgry> uniquecode = OBDal.getInstance()
          .createQuery(EHCMElementCatgry.class, "as e where e.code='" + Checkrecord.getCode()
              + "' and e.client.id='" + Checkrecord.getClient().getId() + "'");
      if (!event.getPreviousState(code).equals(event.getCurrentState(code))) {
        if (uniquecode.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_ElmtCatgry_UniCode"));
        }
      }
      OBQuery<EHCMElementCatgry> uniquename = OBDal.getInstance()
          .createQuery(EHCMElementCatgry.class, "as e where e.name='" + Checkrecord.getName()
              + "' and e.client.id='" + Checkrecord.getClient().getId() + "'");
      if (!event.getPreviousState(name).equals(event.getCurrentState(name))) {
        if (uniquename.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_ElmtCatgry_UniName"));
        }
      }
      if (Checkrecord.isDefault()) {
        OBQuery<EHCMElementCatgry> isdefaultrecord = OBDal.getInstance()
            .createQuery(EHCMElementCatgry.class, "as e where e.default='" + 'Y'
                + "' and e.client.id='" + Checkrecord.getClient().getId() + "'");
        if (!event.getPreviousState(isdefault).equals(event.getCurrentState(isdefault))) {
          if (isdefaultrecord.list().size() > 0) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_ElmtCtgry_Default"));
          }
        }
      }

    } catch (OBException e) {
      log.error(" Exception while Updating Element Category: " + e);
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
      EHCMElementCatgry Checkrecord = (EHCMElementCatgry) event.getTargetInstance();
      OBQuery<EHCMElementCatgry> uniquecode = OBDal.getInstance()
          .createQuery(EHCMElementCatgry.class, "as e where e.code='" + Checkrecord.getCode()
              + "' and e.client.id='" + Checkrecord.getClient().getId() + "'");
      if (uniquecode.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_ElmtCatgry_UniCode"));
      }
      OBQuery<EHCMElementCatgry> uniquename = OBDal.getInstance()
          .createQuery(EHCMElementCatgry.class, "as e where e.name='" + Checkrecord.getName()
              + "' and e.client.id='" + Checkrecord.getClient().getId() + "'");
      if (uniquename.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_ElmtCatgry_UniName"));
      }
      if (Checkrecord.isDefault()) {
        OBQuery<EHCMElementCatgry> isdefaultrecord = OBDal.getInstance()
            .createQuery(EHCMElementCatgry.class, "as e where e.default='" + 'Y'
                + "' and e.client.id='" + Checkrecord.getClient().getId() + "'");
        if (isdefaultrecord.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_ElmtCtgry_Default"));
        }
      }
    } catch (OBException e) {
      log.error(" Exception while creating Element Category: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}
