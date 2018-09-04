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
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMBenefitAllowance;

public class BenefitsAndAllowanceEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMBenefitAllowance.ENTITY_NAME) };
  private Logger log = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMBenefitAllowance benfAllow = (EHCMBenefitAllowance) event.getTargetInstance();
      if ("UP".equals(benfAllow.getDecisionType()) || "CA".equals(benfAllow.getDecisionType())
          || "HO".equals(benfAllow.getDecisionType())) {
        if (benfAllow.getOriginalDecisionNo() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_OrigDecisionNoCantBeEmpty"));
        }
      }
    } catch (OBException e) {
      log.error("Exception while creating benefit and allowance" + e);
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
      EHCMBenefitAllowance benfAllow = (EHCMBenefitAllowance) event.getTargetInstance();
      if ("UP".equals(benfAllow.getDecisionType()) || "CA".equals(benfAllow.getDecisionType())
          || "HO".equals(benfAllow.getDecisionType())) {
        if (benfAllow.getOriginalDecisionNo() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_OrigDecisionNoCantBeEmpty"));
        }
      }
    } catch (OBException e) {
      log.error("Exception while updating benefit and allowance" + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
