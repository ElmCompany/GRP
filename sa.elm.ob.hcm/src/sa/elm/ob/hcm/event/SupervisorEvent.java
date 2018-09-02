package sa.elm.ob.hcm.event;

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
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.event.dao.SupervisorNodeEventDAO;

/**
 * 
 * @author divya-05-02-2018
 *
 */
public class SupervisorEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMEmpSupervisor.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(SupervisorEvent.class);

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMEmpSupervisor supervisor = (EHCMEmpSupervisor) event.getTargetInstance();
      // check whether any other manager is present on same period
      if (supervisor.getEndDate() != null) {
        if (supervisor.getEndDate().compareTo(supervisor.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EndDateStartDateComp"));
        }
      }
      if (SupervisorNodeEventDAO.checkMangerAlreadyAdded(supervisor.getEmployee().getId(),
          supervisor)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_EmpEvla_Employee_Unique"));
      }

    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error(" Exception while adding employee for  Manager: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMEmpSupervisor supervisor = (EHCMEmpSupervisor) event.getTargetInstance();
      final Property employee = entities[0].getProperty(EHCMEmpSupervisor.PROPERTY_EMPLOYEE);
      // check whether any other manager is present on same period
      if (supervisor.getEndDate() != null) {
        if (supervisor.getEndDate().compareTo(supervisor.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EndDateStartDateComp"));
        }
      }
      if (!event.getCurrentState(employee).equals(event.getPreviousState(employee))) {
        if (SupervisorNodeEventDAO.checkMangerAlreadyAdded(supervisor.getEmployee().getId(),
            supervisor)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpEvla_Employee_Unique"));
        }
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error(" Exception while adding employee for  Manager: " + e);
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
      EHCMEmpSupervisor supervisor = (EHCMEmpSupervisor) event.getTargetInstance();
      if (!supervisor.getAlertStatus().equals("DR")) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_CantDeleteProcessed"));
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error(" Exception while deleting  supervisor : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
