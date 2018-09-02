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

import sa.elm.ob.hcm.ehcmprogressionpoint;

public class Gradestpeslineeve extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ehcmprogressionpoint.ENTITY_NAME) };

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
      ehcmprogressionpoint progresspt = (ehcmprogressionpoint) event.getTargetInstance();

      final Property lineno = entities[0].getProperty(ehcmprogressionpoint.PROPERTY_SEQ);
      final Property pointno = entities[0].getProperty(ehcmprogressionpoint.PROPERTY_POINT);

      OBQuery<ehcmprogressionpoint> type = OBDal.getInstance().createQuery(
          ehcmprogressionpoint.class,
          "  seq='" + progresspt.getSeq() + "' and ehcmGradesteps.id = '"
              + progresspt.getEhcmGradesteps().getId() + "'   and client.id ='"
              + progresspt.getClient().getId() + "' ");
      if (!event.getPreviousState(lineno).equals(event.getCurrentState(lineno))) {

        if (type.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Lineno"));

        }
      }
      OBQuery<ehcmprogressionpoint> type1 = OBDal.getInstance().createQuery(
          ehcmprogressionpoint.class,
          "  point='" + progresspt.getPoint() + "' and ehcmGradesteps.id = '"
              + progresspt.getEhcmGradesteps().getId() + "'   and client.id ='"
              + progresspt.getClient().getId() + "' ");
      if (!event.getPreviousState(pointno).equals(event.getCurrentState(pointno))) {
        if (type1.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Pointno"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating gradestepslineevent: " + e);
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
      ehcmprogressionpoint progresspt = (ehcmprogressionpoint) event.getTargetInstance();
      OBQuery<ehcmprogressionpoint> type = OBDal.getInstance().createQuery(
          ehcmprogressionpoint.class,
          "  seq='" + progresspt.getSeq() + "' and ehcmGradesteps.id = '"
              + progresspt.getEhcmGradesteps().getId() + "'   and client.id ='"
              + progresspt.getClient().getId() + "' ");
      if (type.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Lineno"));
      }
      OBQuery<ehcmprogressionpoint> type1 = OBDal.getInstance().createQuery(
          ehcmprogressionpoint.class,
          "  point='" + progresspt.getPoint() + "' and ehcmGradesteps.id = '"
              + progresspt.getEhcmGradesteps().getId() + "'   and client.id ='"
              + progresspt.getClient().getId() + "' ");
      if (type1.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Pointno"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating gradestepslineevent" + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
