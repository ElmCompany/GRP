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
import org.openbravo.model.ad.domain.Preference;

import sa.elm.ob.hcm.EHCMEmpSupervisorNode;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ad_forms.employment.dao.EmploymentDAO;
import sa.elm.ob.hcm.event.dao.SupervisorNodeEventDAO;

/**
 * 
 * @author divya-05-02-2018
 *
 */
public class SupervisorNodeEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMEmpSupervisorNode.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(SupervisorNodeEvent.class);
  EhcmEmpPerInfo oldEmployee = null;
  String preferenceName = "EHCM_EmpInfo_Update";

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMEmpSupervisorNode supervisorNode = (EHCMEmpSupervisorNode) event.getTargetInstance();
      final Property employee = entities[0]
          .getProperty(EHCMEmpSupervisorNode.PROPERTY_EHCMEMPPERINFO);

      // update manager history
      if (!event.getCurrentState(employee).equals(event.getPreviousState(employee))) {
        // check employee already associated with some other manager
        Preference preference = EmploymentDAO.getPreference(preferenceName);
        if (preference == null && SupervisorNodeEventDAO.checkMangAssociatedorNo(
            supervisorNode.getEhcmEmpPerinfo().getId(), supervisorNode.getEhcmEmpSupervisor(),
            supervisorNode)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSupervisor_AlreadyAssociate"));
        }

        oldEmployee = (EhcmEmpPerInfo) event.getPreviousState(employee);
        if (supervisorNode.getEhcmEmpSupervisor() != null
            && supervisorNode.getEhcmEmpSupervisor().getEhcmEmpHierarchy().isPrimaryFlag())
          SupervisorNodeEventDAO.updateEmpInfoSupvisorDetail(supervisorNode, oldEmployee.getId());
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error(" Exception while updating employee for  Manager: " + e);
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

      EHCMEmpSupervisorNode supervisorNode = (EHCMEmpSupervisorNode) event.getTargetInstance();

      Preference preference = EmploymentDAO.getPreference(preferenceName);
      if (preference == null && SupervisorNodeEventDAO.checkMangAssociatedorNo(
          supervisorNode.getEhcmEmpPerinfo().getId(), supervisorNode.getEhcmEmpSupervisor(),
          supervisorNode)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_EmpSupervisor_AlreadyAssociate"));
      }
      // save manager
      if (supervisorNode.getEhcmEmpSupervisor() != null
          && supervisorNode.getEhcmEmpSupervisor().getEhcmEmpHierarchy().isPrimaryFlag())
        SupervisorNodeEventDAO.updateEmpInfoSupvisorDetail(supervisorNode, null);
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
      EHCMEmpSupervisorNode supervisorNode = (EHCMEmpSupervisorNode) event.getTargetInstance();
      // delete manager
      SupervisorNodeEventDAO.updateCaseEmpInfoSupvisorDetail(supervisorNode,
          supervisorNode.getEhcmEmpPerinfo().getId(), null);
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error(" Exception while deleting  supervisor node: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
