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

import sa.elm.ob.hcm.ehcmpayscale;

public class Payscaleevent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ehcmpayscale.ENTITY_NAME) };

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
      ehcmpayscale payscale = (ehcmpayscale) event.getTargetInstance();

      final Property typename = entities[0].getProperty(ehcmpayscale.PROPERTY_COMMERCIALNAME);
      final Property gradename = entities[0].getProperty(ehcmpayscale.PROPERTY_EHCMGRADE);
      final Property gradesteps = entities[0].getProperty(ehcmpayscale.PROPERTY_EHCMGRADESTEPS);

      OBQuery<ehcmpayscale> type = OBDal.getInstance().createQuery(ehcmpayscale.class,
          "  commercialName='" + payscale.getCommercialName() + "' and client.id ='"
              + payscale.getClient().getId() + "' ");
      if (!event.getPreviousState(typename).equals(event.getCurrentState(typename))) {

        if (type.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradename"));

        }
      }
      OBQuery<ehcmpayscale> type1 = OBDal.getInstance().createQuery(ehcmpayscale.class,
          "  ehcmGrade.id='" + payscale.getEhcmGrade().getId() + "' and ehcmGradesteps.id = '"
              + payscale.getEhcmGradesteps().getId() + "'  and client.id ='"
              + payscale.getClient().getId() + "' ");
      if (!event.getPreviousState(gradename).equals(event.getCurrentState(gradename))
          && !event.getPreviousState(gradesteps).equals(event.getCurrentState(gradesteps))) {
        if (type1.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradestepsname"));
        }
      }
      // Unique grade constraint validation
      OBQuery<ehcmpayscale> gradeval = OBDal.getInstance().createQuery(ehcmpayscale.class,
          "  ehcmGrade='" + payscale.getEhcmGrade() + "' and client.id ='"
              + payscale.getClient().getId() + "' ");
      if (!event.getPreviousState(gradename).equals(event.getCurrentState(gradename))) {

        if (gradeval.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_payscale_grade_unique"));

        }
      }
    } catch (OBException e) {
      log.error(" Exception while creating payscaleevent: " + e);
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
      ehcmpayscale payscale = (ehcmpayscale) event.getTargetInstance();
      OBQuery<ehcmpayscale> type = OBDal.getInstance().createQuery(ehcmpayscale.class,
          "  commercialName='" + payscale.getCommercialName() + "' and client.id ='"
              + payscale.getClient().getId() + "' ");
      if (type.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradename"));
      }
      OBQuery<ehcmpayscale> type1 = OBDal.getInstance().createQuery(ehcmpayscale.class,
          "  ehcmGrade.id='" + payscale.getEhcmGrade().getId() + "' and ehcmGradesteps.id = '"
              + payscale.getEhcmGradesteps().getId() + "'  and client.id ='"
              + payscale.getClient().getId() + "' ");
      if (type1.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradestepsname"));
      }
      OBQuery<ehcmpayscale> gradeval = OBDal.getInstance().createQuery(ehcmpayscale.class,
          "  ehcmGrade='" + payscale.getEhcmGrade() + "' and client.id ='"
              + payscale.getClient().getId() + "' ");

      if (gradeval.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_payscale_grade_unique"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating payscaleevent" + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
