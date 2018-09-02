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
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMCompetency;
import sa.elm.ob.hcm.event.dao.CompetencyDAO;

/**
 * this process will handle the event of competency
 * 
 * @author divya-12-02-2018
 *
 */
public class CompetencyEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMCompetency.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(CompetencyEvent.class);

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMCompetency empCompetency = (EHCMCompetency) event.getTargetInstance();
      // check name is unique
      if (CompetencyDAO.checkCompetencyUnique(empCompetency)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Competency_Unique"));
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      log.error(" Exception while adding competency: " + e);
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
      final Property name = entities[0].getProperty(EHCMCompetency.PROPERTY_NAME);
      EHCMCompetency empCompetency = (EHCMCompetency) event.getTargetInstance();
      // check name is unique
      if (!event.getPreviousState(name).equals(event.getCurrentState(name))) {
        if (CompetencyDAO.checkCompetencyUnique(empCompetency)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_Competency_Unique"));
        }
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      log.error(" Exception while update competecny : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
