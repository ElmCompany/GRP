package sa.elm.ob.hcm.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.DecisionBalanceHeader;

/**
 * @author Mouli.K
 */

public class DecisionBalanceHeaderEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(DecisionBalanceHeader.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {

      DecisionBalanceHeader decisionBalanceHeader = (DecisionBalanceHeader) event
          .getTargetInstance();

      if (decisionBalanceHeader.getAlertStatus().equals("CO")) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_decision_balance"));
      }

    } catch (OBException e) {
      logger.error("Exception While Deleting Decision Balance:" + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

}
