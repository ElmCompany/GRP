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

import sa.elm.ob.hcm.EHCMOrgClassfication;

public class OrgClassEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMOrgClassfication.ENTITY_NAME) };

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
      EHCMOrgClassfication classtype = (EHCMOrgClassfication) event.getTargetInstance();
      final Property typename = entities[0].getProperty(EHCMOrgClassfication.PROPERTY_ORGCLASSNAME);
      final Property typecode = entities[0].getProperty(EHCMOrgClassfication.PROPERTY_SEARCHKEY);
      final Property Startdate = entities[0].getProperty(EHCMOrgClassfication.PROPERTY_STARTDATE);
      final Property Enddate = entities[0].getProperty(EHCMOrgClassfication.PROPERTY_ENDDATE);
      OBQuery<EHCMOrgClassfication> type = OBDal.getInstance()
          .createQuery(EHCMOrgClassfication.class, "value='" + classtype.getSearchKey()
              + "' or orgclassname = '" + classtype.getOrgclassname() + "' ");
      /*
       * if (!event.getPreviousState(typename).equals(event.getCurrentState(typename)) ||
       * !event.getPreviousState(typecode).equals(event.getCurrentState(typecode))) { if
       * (type.list().size() > 0) { throw new
       * OBException(OBMessageUtils.messageBD("Ehcm_ClassType"));
       * 
       * } }
       */
      Date startDate = classtype.getStartDate();
      Date endDate = classtype.getEndDate();

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
      EHCMOrgClassfication classtype = (EHCMOrgClassfication) event.getTargetInstance();
      OBQuery<EHCMOrgClassfication> type = OBDal.getInstance()
          .createQuery(EHCMOrgClassfication.class, "value='" + classtype.getSearchKey()
              + "' or orgclassname = '" + classtype.getOrgclassname() + "' ");
      if (type.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_ClassType"));
      }
      Date startDate = classtype.getStartDate();
      Date endDate = classtype.getEndDate();
      if (startDate != null && endDate != null) {
        if (endDate.compareTo(startDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Enddate"));
        }
      }
    } catch (OBException e) {
      log.error(" Exception while creating classtype in Organization: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}