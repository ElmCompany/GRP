package sa.elm.ob.hcm.event;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EhcmPosition;

public class PositionEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmPosition.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());

  public void onUpdate(@Observes EntityUpdateEvent event) {
    String active = "";
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EhcmPosition position = (EhcmPosition) event.getTargetInstance();
      final Property grade = entities[0].getProperty(EhcmPosition.PROPERTY_GRADE);
      final Property jobno = entities[0].getProperty(EhcmPosition.PROPERTY_JOBNO);
      final Property isactive = entities[0].getProperty(EhcmPosition.PROPERTY_ACTIVE);
      List<EhcmPosition> posList = new ArrayList<EhcmPosition>();
      if (position.isActive()) {
        active = "Y";
      } else
        active = "N";
      if (!event.getPreviousState(grade).equals(event.getCurrentState(grade))
          || (!event.getPreviousState(jobno).equals(event.getCurrentState(jobno)))
          || (!event.getPreviousState(isactive).equals(event.getCurrentState(isactive)))) {

        OBQuery<EhcmPosition> type = OBDal.getInstance().createQuery(EhcmPosition.class,
            " ( grade.id=:grade " + " and  jOBNo = :jobNo and active='" + active
                + "' ) and client.id =:clientId ");
        type.setNamedParameter("grade", position.getGrade().getId());
        type.setNamedParameter("jobNo", position.getJOBNo());
        type.setNamedParameter("clientId", position.getClient().getId());
        posList = type.list();
        if (posList.size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_PosUnique"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating Position  " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void onSave(@Observes EntityNewEvent event) {
    String active = "";
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EhcmPosition position = (EhcmPosition) event.getTargetInstance();
      List<EhcmPosition> posList = new ArrayList<EhcmPosition>();
      if (position.isActive()) {
        active = "Y";
      } else
        active = "N";
      OBQuery<EhcmPosition> type = OBDal.getInstance().createQuery(EhcmPosition.class,
          " ( grade.id=:grade " + " and  jOBNo = :jobNo and active='" + active
              + "' ) and client.id =:clientId ");
      type.setNamedParameter("grade", position.getGrade().getId());
      type.setNamedParameter("jobNo", position.getJOBNo());
      type.setNamedParameter("clientId", position.getClient().getId());
      posList = type.list();
      if (posList.size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_PosUnique"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating Position  " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EhcmPosition position = (EhcmPosition) event.getTargetInstance();

      if (position.isSued()) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_POS_CANNOTDELETE"));
      }

    } catch (OBException e) {
      log.error(" Exception while Delete the Position " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
