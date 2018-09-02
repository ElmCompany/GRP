package sa.elm.ob.hcm.event;

import java.util.Date;

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

import sa.elm.ob.hcm.EHCMorgtype;

public class OrganizationTypeEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMorgtype.ENTITY_NAME) };

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
      EHCMorgtype orgtype = (EHCMorgtype) event.getTargetInstance();
      final Property typename = entities[0].getProperty(EHCMorgtype.PROPERTY_ORGTYPENAME);
      final Property typecode = entities[0].getProperty(EHCMorgtype.PROPERTY_SEARCHKEY);
      OBQuery<EHCMorgtype> type = OBDal.getInstance().createQuery(EHCMorgtype.class,
          "orgtypename = '" + orgtype.getOrgtypename() + "'");
      OBQuery<EHCMorgtype> code = OBDal.getInstance().createQuery(EHCMorgtype.class,
          "value='" + orgtype.getSearchKey() + "'");
      // check name is duplicating or not
      if (!event.getPreviousState(typename).equals(event.getCurrentState(typename))) {
        if (type.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_OrgType"));
        }
      }
      // check code is duplicating or not
      if (!event.getPreviousState(typecode).equals(event.getCurrentState(typecode))) {
        if (code.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_OrgType"));
        }
      }
      Date startDate = orgtype.getStartDate();
      Date endDate = orgtype.getEndDate();
      if (startDate != null && endDate != null) {
        if (endDate.compareTo(startDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Enddate"));
        }

      }

    } catch (OBException e) {
      log.error(" Exception while creating orgtype in Organization: " + e);
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
      EHCMorgtype orgtype = (EHCMorgtype) event.getTargetInstance();
      OBQuery<EHCMorgtype> type = OBDal.getInstance().createQuery(EHCMorgtype.class, "value='"
          + orgtype.getSearchKey() + "' or orgtypename = '" + orgtype.getOrgtypename() + "' ");
      if (type.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_OrgType"));
      }
      Date startDate = orgtype.getStartDate();
      Date endDate = orgtype.getEndDate();
      if (startDate != null && endDate != null) {
        if (endDate.compareTo(startDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Enddate"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating orgtype in Organization: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}