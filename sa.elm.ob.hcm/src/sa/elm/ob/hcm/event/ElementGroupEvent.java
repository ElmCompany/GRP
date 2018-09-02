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
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EhcmElementGroup;
import sa.elm.ob.hcm.event.dao.ElementGroupEventDAO;
import sa.elm.ob.hcm.event.dao.ElementGroupEventDAOImpl;

public class ElementGroupEvent extends EntityPersistenceEventObserver {
  /*
  * 
  */
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmElementGroup.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());
  Date currentDate = new Date();
  Property active = entities[0].getProperty(EhcmElementGroup.PROPERTY_ACTIVE);
  Property startDate = entities[0].getProperty(EhcmElementGroup.PROPERTY_STARTDATE);
  Property enddate = entities[0].getProperty(EhcmElementGroup.PROPERTY_ENDDATE);
  ElementGroupEventDAO EmpDaoImp = new ElementGroupEventDAOImpl();
  final Property EhcmElementGroupId = entities[0].getProperty(EhcmElementGroup.PROPERTY_ID);

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EhcmElementGroup elementGroup = (EhcmElementGroup) event.getTargetInstance();
      String EhcmElementGroupId = elementGroup.getId();
      String clientId = elementGroup.getClient().getId();
      Boolean isprocessed = false;
      Boolean isprimary = false;
      String IsUpdate = "Y";
      if (elementGroup.getEndDate() != null) {
        if (event.getCurrentState(startDate) != event.getPreviousState(startDate)
            || event.getCurrentState(enddate) != event.getPreviousState(enddate)) {
          if (elementGroup.getEndDate().compareTo(elementGroup.getStartDate()) < 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_StartDateGreThan_EndDate"));
          }
        }
      }

      if (elementGroup.isPrimary()) {
        isprimary = EmpDaoImp.isPrimaryCheckElementGroup(elementGroup, clientId, IsUpdate);
        if (isprimary) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_Isprimary_unique"));
        }
      }
      // for the given period element group is already processed , cannot deactivate
      if (elementGroup.getEndDate() != null || !elementGroup.isActive()) {
        isprocessed = EmpDaoImp.isElementGroupProcessed(elementGroup);
        if (isprocessed) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_ElementGroup_processed_withPeriod"));
        }

      }
    } catch (OBException e) {
      log.error(" Exception in Element group event " + e);
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
      Boolean isprimary = false;
      String IsUpdate = "N";
      OBContext.setAdminMode();
      EhcmElementGroup elementGroup = (EhcmElementGroup) event.getTargetInstance();
      String clientId = elementGroup.getClient().getId();
      if (elementGroup.getEndDate() != null) {
        if (elementGroup.getEndDate().compareTo(elementGroup.getStartDate()) < 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_StartDateGreThan_EndDate"));
        }
      }
      if (elementGroup.isPrimary()) {
        isprimary = EmpDaoImp.isPrimaryCheckElementGroup(elementGroup, clientId, IsUpdate);
        if (isprimary) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_Isprimary_unique"));

        }
      }
    } catch (OBException e) {
      log.error(" Exception in Element group event : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
