package sa.elm.ob.hcm.event;

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

import sa.elm.ob.hcm.DecisionBalance;
import sa.elm.ob.hcm.ad_process.Constants;

/**
 * @author Mouli.K
 */

public class DecisionBalanceLineEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(DecisionBalance.ENTITY_NAME) };
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

      DecisionBalance decisionBalance = (DecisionBalance) event.getTargetInstance();

      if (decisionBalance.getEhcmDeciBalHdr().getAlertStatus().equals("CO")) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_decision_balance"));
      }

    } catch (OBException e) {
      logger.error("Exception While Deleting Decision Balance:" + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

  /*
   * public void onSave(@Observes EntityDeleteEvent event) { if (!isValidEvent(event)) { return; }
   * try {
   * 
   * DecisionBalance decisionBalance = (DecisionBalance) event.getTargetInstance();
   * 
   * if (decisionBalance.getEhcmDeciBalHdr().getAlertStatus().equals("C")) { throw new
   * OBException(OBMessageUtils.messageBD("Ehcm_decision_balance")); } else { throw new
   * OBException(OBMessageUtils.messageBD("Ehcm_decision_balance"));
   * 
   * }
   * 
   * } catch (OBException e) { logger.error("Exception While Deleting Decision Balance:" + e); throw
   * new OBException(e.getMessage()); } catch (Exception e) { throw new
   * OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR")); } }
   */

  @SuppressWarnings("unused")
  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      DecisionBalance decisionBalance = (DecisionBalance) event.getTargetInstance();
      if (decisionBalance.getDecisionType().equals("BM")) {
        if (decisionBalance.getEhcmMissionCategory() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_decision_balance_category"));

        }
      }
      if (decisionBalance.getBalance() == null || decisionBalance.getBalance() < 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_decision_balance_Balance"));

      }
      if (decisionBalance.getEhcmDeciBalHdr().getAlertStatus().equals("CO")) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_decision_balance"));
      }

      if (decisionBalance.getDecisionType().equals(Constants.ALLPAIDLEAVEBALANCE)) {
        if (decisionBalance.getAbsenceType() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_DecBalance_AbsMandatory"));
        }
        if (decisionBalance.getAbsenceType() != null
            && decisionBalance.getAbsenceType().getAccrualResetDate()
                .equals(Constants.ACCRUALRESETDATE_LEAVEOCCUR)
            && decisionBalance.getBlockStartdate() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_LeaveOccurAbs_BlockDatMan"));
        }
        if (decisionBalance.getAbsenceType() != null && decisionBalance.getAbsenceType().isSubtype()
            && decisionBalance.getSubType() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_Subtype_Mandatory"));
        }

      }

    } catch (OBException e) {
      logger.error("Exception While Creating Decision Balance:" + e);
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
      DecisionBalance decisionBalance = (DecisionBalance) event.getTargetInstance();
      if (decisionBalance.getDecisionType().equals("BM")) {
        if (decisionBalance.getEhcmMissionCategory() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_decision_balance_category"));

        }
      }
      if (decisionBalance.getBalance() == null || decisionBalance.getBalance() < 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_decision_balance_Balance"));

      }
      if (decisionBalance.getEhcmDeciBalHdr().getAlertStatus().equals("CO")) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_decision_balance"));
      }

      if (decisionBalance.getDecisionType().equals(Constants.ALLPAIDLEAVEBALANCE)) {
        if (decisionBalance.getAbsenceType() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_DecBalance_AbsMandatory"));
        }
        if (decisionBalance.getAbsenceType() != null
            && decisionBalance.getAbsenceType().getAccrualResetDate()
                .equals(Constants.ACCRUALRESETDATE_LEAVEOCCUR)
            && decisionBalance.getBlockStartdate() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_LeaveOccurAbs_BlockDatMan"));
        }
        if (decisionBalance.getAbsenceType() != null && decisionBalance.getAbsenceType().isSubtype()
            && decisionBalance.getSubType() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_Subtype_Mandatory"));
        }

      }

    } catch (OBException e) {
      logger.error("Exception While update Decision Balance:" + e);
      throw new OBException(e.getMessage());
    }
  }

}
