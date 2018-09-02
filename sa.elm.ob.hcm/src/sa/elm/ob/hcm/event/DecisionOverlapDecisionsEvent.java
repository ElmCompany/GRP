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

import sa.elm.ob.hcm.EHCMDecisionOverlap;
import sa.elm.ob.hcm.EHCMDecisionOverlapLn;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.event.dao.DecisionOverlapDAO;

/**
 * @author divya on 28/02/2018
 */
public class DecisionOverlapDecisionsEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMDecisionOverlapLn.ENTITY_NAME) };

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
      
      EHCMDecisionOverlapLn decisionOverlapln = (EHCMDecisionOverlapLn) event
              .getTargetInstance();
      final Property decisionType = entities[0]
              .getProperty(EHCMDecisionOverlapLn.PROPERTY_DECISIONTYPE);
      final Property decisionsubtypeType = entities[0]
              .getProperty(EHCMDecisionOverlapLn.PROPERTY_EHCMDECISIONSUBTYPEV);

      if (!event.getCurrentState(decisionType).equals(event.getPreviousState(decisionType))
              ||(event.getCurrentState(decisionsubtypeType) !=null &&  !event.getCurrentState(decisionsubtypeType).equals(event.getPreviousState(decisionsubtypeType))) ){
          Boolean ischeckoverlap= DecisionOverlapDAO.checkDecisionOverlapUnique( decisionOverlapln);
    	  if(ischeckoverlap) {
     	 throw new OBException(OBMessageUtils.messageBD("ehcm_decoverlapln_uniq"));
      }
      }
     
    } catch (OBException e) {
      log.error(" Exception while updating Employee Business Mission   " + e);
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
      EHCMDecisionOverlapLn decisionOverlapln = (EHCMDecisionOverlapLn) event
              .getTargetInstance();
      
     Boolean ischeckoverlap= DecisionOverlapDAO.checkDecisionOverlapUnique( decisionOverlapln);
     
    if(ischeckoverlap) {
    	 throw new OBException(OBMessageUtils.messageBD("ehcm_decoverlapln_uniq"));
     }
     
    } catch (OBException e) {
      log.error(" Exception while creating Business Mission   " + e);
      throw new OBException(e.getMessage());
    }
     catch (Exception e) {
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
      EHCMDecisionOverlapLn decisionOverlap = (EHCMDecisionOverlapLn) event
              .getTargetInstance();
      
      

    } catch (OBException e) {
      log.error(" Exception while Business Mission ondelete event : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
