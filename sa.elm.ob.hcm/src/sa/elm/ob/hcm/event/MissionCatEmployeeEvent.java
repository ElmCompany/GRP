package sa.elm.ob.hcm.event;

import java.util.Date;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMMiscatEmployee;
import sa.elm.ob.hcm.EmploymentInfo;

/**
 * @author divya on 03/03/2018
 */
public class MissionCatEmployeeEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMMiscatEmployee.ENTITY_NAME) };

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

      EHCMMiscatEmployee misCategoryEmp = (EHCMMiscatEmployee) event.getTargetInstance();

    } catch (OBException e) {
      log.error(" Exception while updating MissionCatEmployeeEvent   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @SuppressWarnings("unused")
  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EmploymentInfo employinfo = null;
      Date startDate = null;
      String decisionType = null;
      EHCMMiscatEmployee misCategoryEmp = (EHCMMiscatEmployee) event.getTargetInstance();

    } catch (OBException e) {
      log.error(" Exception while creating MissionCatEmployeeEvent   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

    finally {
      OBContext.restorePreviousMode();
    }
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMMiscatEmployee misCategoryEmp = (EHCMMiscatEmployee) event.getTargetInstance();
      if (misCategoryEmp.getUseddays() > 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_MisCat_Period_CantDel"));
      }

    } catch (OBException e) {
      log.error(" Exception while MissionCatEmployeeEvent ondelete event : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
