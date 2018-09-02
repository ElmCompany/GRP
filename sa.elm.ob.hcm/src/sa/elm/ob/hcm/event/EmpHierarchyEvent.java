package sa.elm.ob.hcm.event;

import java.util.ArrayList;
import java.util.List;

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

import sa.elm.ob.hcm.EHCMEmpHierarchy;
import sa.elm.ob.hcm.event.dao.SupervisorNodeEventDAO;

/**
 * this process will handle the event of emp hierarchy
 * 
 * @author divya-06-02-2018
 *
 */
public class EmpHierarchyEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMEmpHierarchy.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(EmpHierarchyEvent.class);

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      List<EHCMEmpHierarchy> empHierarchyList = new ArrayList<EHCMEmpHierarchy>();
      EHCMEmpHierarchy empHierarchy = (EHCMEmpHierarchy) event.getTargetInstance();
      if (empHierarchy.isPrimaryFlag()) {
        // check already primary hierarchy record exists or not
        OBQuery<EHCMEmpHierarchy> empHierarchyQry = OBDal.getInstance()
            .createQuery(EHCMEmpHierarchy.class, " as e where e.primaryFlag='Y'");
        empHierarchyList = empHierarchyQry.list();
        if (empHierarchyList.size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_SupervisHierarchy_OnePF"));
        }
      }
      // check name is unique
      if (SupervisorNodeEventDAO.checkempHierarchyUnique(empHierarchy)) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradename"));
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error(" Exception while adding hierarchy in supervisor : " + e);
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
      final Property primaryfalg = entities[0].getProperty(EHCMEmpHierarchy.PROPERTY_PRIMARYFLAG);
      final Property name = entities[0].getProperty(EHCMEmpHierarchy.PROPERTY_NAME);
      List<EHCMEmpHierarchy> empHierarchyList = new ArrayList<EHCMEmpHierarchy>();
      EHCMEmpHierarchy empHierarchy = (EHCMEmpHierarchy) event.getTargetInstance();
      if (event.getCurrentState(primaryfalg) != null
          && !event.getCurrentState(primaryfalg).equals(event.getPreviousState(primaryfalg))) {
        if (empHierarchy.isPrimaryFlag()) {
          // check already primary hierarchy record exists or not
          OBQuery<EHCMEmpHierarchy> empHierarchyQry = OBDal.getInstance()
              .createQuery(EHCMEmpHierarchy.class, " as e where e.primaryFlag='Y'");
          empHierarchyList = empHierarchyQry.list();
          if (empHierarchyList.size() > 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_SupervisHierarchy_OnePF"));
          }
        }
      }
      // check name is unique
      if (!event.getPreviousState(name).equals(event.getCurrentState(name))) {
        if (SupervisorNodeEventDAO.checkempHierarchyUnique(empHierarchy)) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradename"));
        }
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      log.error(" Exception while update hierarchy in supervisor : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
