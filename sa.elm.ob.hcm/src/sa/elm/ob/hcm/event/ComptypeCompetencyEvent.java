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

import sa.elm.ob.hcm.EHCMComptypeCompetency;
import sa.elm.ob.hcm.event.dao.CompetencyDAO;

/**
 * this process will handle the event of competency Type competency
 * 
 * @author divya-12-02-2018
 *
 */
public class ComptypeCompetencyEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMComptypeCompetency.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(ComptypeCompetencyEvent.class);

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMComptypeCompetency compTypeCompetency = (EHCMComptypeCompetency) event
          .getTargetInstance();
      // check name is unique
      if (CompetencyDAO.chkCompTypeCompetencyUnique(compTypeCompetency)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_CompTypeCompetencyUnique"));
      }
      // check max greater than min
      if (compTypeCompetency.getMaximum().compareTo(compTypeCompetency.getMinimum()) < 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_EmpEval_MaxGrtThanMin"));
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      log.error(" Exception while adding competecnytype: " + e);
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
      final Property competency = entities[0]
          .getProperty(EHCMComptypeCompetency.PROPERTY_EHCMCOMPETENCY);
      final Property max = entities[0].getProperty(EHCMComptypeCompetency.PROPERTY_MAXIMUM);
      final Property min = entities[0].getProperty(EHCMComptypeCompetency.PROPERTY_MINIMUM);
      EHCMComptypeCompetency compTypeCompetency = (EHCMComptypeCompetency) event
          .getTargetInstance();
      // check name is unique
      if (!event.getPreviousState(competency).equals(event.getCurrentState(competency))) {
        if (CompetencyDAO.chkCompTypeCompetencyUnique(compTypeCompetency)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_CompTypeCompetencyUnique"));
        }
      }

      // check max greater than min
      if (!event.getPreviousState(max).equals(event.getCurrentState(max))
          || !event.getPreviousState(min).equals(event.getCurrentState(min))) {
        if (compTypeCompetency.getMaximum().compareTo(compTypeCompetency.getMinimum()) < 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpEval_MaxGrtThanMin"));
        }
      }
    } catch (OBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      log.error(" Exception while update competecnytype : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
