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

import sa.elm.ob.hcm.ehcmgradeclass;

public class GradeclassEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ehcmgradeclass.ENTITY_NAME) };

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
      ehcmgradeclass gradeclass = (ehcmgradeclass) event.getTargetInstance();
      final Property typecode = entities[0].getProperty(ehcmgradeclass.PROPERTY_SEARCHKEY);
      final Property typename = entities[0].getProperty(ehcmgradeclass.PROPERTY_NAME);

      if (gradeclass.getEndDate() != null) {
        if (gradeclass.getEndDate().compareTo(gradeclass.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EndDateStartDateComp"));
        }
      }

      OBQuery<ehcmgradeclass> type = OBDal.getInstance().createQuery(ehcmgradeclass.class,
          "  name='" + gradeclass.getName() + "' and client.id ='" + gradeclass.getClient().getId()
              + "' ");
      if (!event.getPreviousState(typename).equals(event.getCurrentState(typename))) {

        if (type.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));

        }
      }
      OBQuery<ehcmgradeclass> type1 = OBDal.getInstance().createQuery(ehcmgradeclass.class,
          "  searchKey='" + gradeclass.getSearchKey() + "' and client.id ='"
              + gradeclass.getClient().getId() + "' ");
      if (!event.getPreviousState(typecode).equals(event.getCurrentState(typecode))) {
        if (type1.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));

        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating gradeclass " + e);
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
      ehcmgradeclass gradeclass = (ehcmgradeclass) event.getTargetInstance();

      if (gradeclass.getEndDate() != null) {
        if (gradeclass.getEndDate().compareTo(gradeclass.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EndDateStartDateComp"));
        }
      }

      OBQuery<ehcmgradeclass> type = OBDal.getInstance().createQuery(ehcmgradeclass.class,
          " ( name='" + gradeclass.getName() + "' or searchKey = '" + gradeclass.getSearchKey()
              + "' ) and client.id ='" + gradeclass.getClient().getId() + "' ");
      if (type.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating gradeclass  " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
