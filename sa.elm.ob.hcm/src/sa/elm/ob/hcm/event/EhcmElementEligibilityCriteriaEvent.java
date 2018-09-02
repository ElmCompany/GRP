package sa.elm.ob.hcm.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMElementECriteria;

/**
 * @author Rashika.V.S on 16/07/2018
 */

public class EhcmElementEligibilityCriteriaEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMElementECriteria.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMElementECriteria eligibilityCriteria = (EHCMElementECriteria) event.getTargetInstance();
      String elementCode = eligibilityCriteria.getCode().getId();
      String elementId = eligibilityCriteria.getId();
      OBQuery<EHCMElementECriteria> equery = OBDal.getInstance()
          .createQuery(EHCMElementECriteria.class, "code.id ='" + elementCode + "' and id !='" + elementId + "'");
      if (equery.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_elmntEcriteria_elmntCode"));
      }

    } catch (OBException e) {
      logger.error("Exception While Updating Element Eligibility Criteria:" + e);
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
      OBContext.setAdminMode();
      EHCMElementECriteria eligibilityCriteria = (EHCMElementECriteria) event.getTargetInstance();
      String elementCode = eligibilityCriteria.getCode().getId();
      OBQuery<EHCMElementECriteria> equery = OBDal.getInstance()
          .createQuery(EHCMElementECriteria.class, "code.id ='" + elementCode + "'");
      if (equery.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_elmntEcriteria_elmntCode"));
      }

    } catch (OBException e) {
      logger.error("Exception While Updating Element Eligibility Criteria:" + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}
