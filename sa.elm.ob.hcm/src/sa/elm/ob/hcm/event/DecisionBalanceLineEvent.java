package sa.elm.ob.hcm.event;

import java.math.BigDecimal;

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
import sa.elm.ob.hcm.ad_process.DecisionBalance.DecisionBalanceDAO;
import sa.elm.ob.hcm.ad_process.DecisionBalance.DecisionBalanceDAOImpl;

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
      Boolean checkUniqueConstraintForDecisionBalLine = false;
      DecisionBalance decisionBalance = (DecisionBalance) event.getTargetInstance();
      DecisionBalanceDAO decisionBalanceDAO = new DecisionBalanceDAOImpl();
      if (decisionBalance.getDecisionType().equals("BM")) {
        if (decisionBalance.getEhcmMissionCategory() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_decision_balance_category"));

        }
      }
      if (decisionBalance.getBalance() == null
          || decisionBalance.getBalance().compareTo(BigDecimal.ZERO) < 0) {
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

      checkUniqueConstraintForDecisionBalLine = decisionBalanceDAO
          .checkUniqueConstraintForDecisionBalLine(decisionBalance);
      if (checkUniqueConstraintForDecisionBalLine) {
        if (decisionBalance.getDecisionType().equals("BM"))
          throw new OBException(OBMessageUtils.messageBD("EHCM_DecBal_UniqueBM"));
        if (decisionBalance.getDecisionType().equals("APLB")) {
          if (decisionBalance.getAbsenceType() != null
              && decisionBalance.getAbsenceType().isSubtype())
            throw new OBException(OBMessageUtils.messageBD("EHCM_DecBal_UniqueABSubTy"));
          else
            throw new OBException(OBMessageUtils.messageBD("EHCM_DecBal_UniqueAB"));
        } else
          throw new OBException(OBMessageUtils.messageBD("EHCM_DecBal_Unique"));
      }

      if (decisionBalance.getDecisionType().equals(Constants.ALLPAIDLEAVEBALANCE)) {

        if ((decisionBalance.getBalance().doubleValue()
            - decisionBalance.getBalance().intValue()) != 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_DecimalValueNotAllowAPLB"));
        }
      }
      if (decisionBalance.getDecisionType().equals(Constants.ANNUALLEAVEBALANCE)) {

        if ((decisionBalance.getBalance().doubleValue()
            - decisionBalance.getBalance().intValue()) != 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_DecimalValueNotAllowALB"));
        }
      }

      if (decisionBalance.getDecisionType().equals(Constants.BUSINESSMISSIONBALANCE)) {

        if ((decisionBalance.getBalance().doubleValue()
            - decisionBalance.getBalance().intValue()) != 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_DecimalValueNotAllowBMB"));
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

      Boolean checkUniqueConstraintForDecisionBalLine = false;
      DecisionBalanceDAO decisionBalanceDAO = new DecisionBalanceDAOImpl();

      if (decisionBalance.getDecisionType().equals("BM")) {
        if (decisionBalance.getEhcmMissionCategory() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_decision_balance_category"));

        }
      }
      if (decisionBalance.getBalance() == null
          || decisionBalance.getBalance().compareTo(BigDecimal.ZERO) < 0) {
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

      checkUniqueConstraintForDecisionBalLine = decisionBalanceDAO
          .checkUniqueConstraintForDecisionBalLine(decisionBalance);
      if (checkUniqueConstraintForDecisionBalLine) {
        if (decisionBalance.getDecisionType().equals("BM"))
          throw new OBException(OBMessageUtils.messageBD("EHCM_DecBal_UniqueBM"));
        if (decisionBalance.getDecisionType().equals("APLB")) {
          if (decisionBalance.getAbsenceType() != null
              && decisionBalance.getAbsenceType().isSubtype())
            throw new OBException(OBMessageUtils.messageBD("EHCM_DecBal_UniqueABSubTy"));
          else
            throw new OBException(OBMessageUtils.messageBD("EHCM_DecBal_UniqueAB"));
        } else
          throw new OBException(OBMessageUtils.messageBD("EHCM_DecBal_Unique"));
      }

      if (decisionBalance.getDecisionType().equals(Constants.ALLPAIDLEAVEBALANCE)) {

        if ((decisionBalance.getBalance().doubleValue()
            - decisionBalance.getBalance().intValue()) != 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_DecimalValueNotAllowAPLB"));
        }
      }
      if (decisionBalance.getDecisionType().equals(Constants.ANNUALLEAVEBALANCE)) {

        if ((decisionBalance.getBalance().doubleValue()
            - decisionBalance.getBalance().intValue()) != 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_DecimalValueNotAllowALB"));
        }
      }

      if (decisionBalance.getDecisionType().equals(Constants.BUSINESSMISSIONBALANCE)) {

        if ((decisionBalance.getBalance().doubleValue()
            - decisionBalance.getBalance().intValue()) != 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_DecimalValueNotAllowBMB"));
        }
      }

    } catch (OBException e) {
      logger.error("Exception While update Decision Balance:" + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

}
