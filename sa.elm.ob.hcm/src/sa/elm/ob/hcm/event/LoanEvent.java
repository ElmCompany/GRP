package sa.elm.ob.hcm.event;

import java.math.BigDecimal;
import java.text.ParseException;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMLoanTransaction;

/**
 * 
 * @author Gokul
 *
 */
public class LoanEvent extends EntityPersistenceEventObserver {

  private Logger log = Logger.getLogger(this.getClass());

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMLoanTransaction.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) throws ParseException {
    if (!isValidEvent(event)) {
      return;
    }
    final EHCMLoanTransaction loan = (EHCMLoanTransaction) event.getTargetInstance();
    BigDecimal initial_amount = loan.getLoanInitialBal();
    BigDecimal original_amount = loan.getLoanOriginalAmount();
    String loantype = loan.getLoanType();
    BigDecimal installmentamount = loan.getInstallmentAmount();
    try {
      if ((initial_amount == null) && (original_amount.compareTo(BigDecimal.ZERO) <= 0)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Loan_amountevent"));
      }
      if (installmentamount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_loan_installment_amount"));
      }
      if (loantype.equals("HO")) {
        if (loan.getHoldDate() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_loan_hold_date"));
        }
        if (loan.getHoldDuration() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_loan_hold_duration"));
        }
        if (loan.getHoldDurationType() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_loan_hold_duration_type"));
        }
      }
      if (!loantype.equals("CR")) {
        if (loan.getOriginalDecisionNo() == null) {
          throw new OBException(OBMessageUtils.messageBD("ehcm_loan_sec_check"));
        }
      }
    } catch (OBException e) {
      log.error(" Exception while creating Loan Transactions: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

  public void onSave(@Observes EntityNewEvent event) throws ParseException {
    if (!isValidEvent(event)) {
      return;
    }
    final EHCMLoanTransaction loan = (EHCMLoanTransaction) event.getTargetInstance();
    BigDecimal initial_amount = loan.getLoanInitialBal();
    BigDecimal original_amount = loan.getLoanOriginalAmount();
    String loantype = loan.getLoanType();
    BigDecimal installmentamount = loan.getInstallmentAmount();
    try {
      if (((initial_amount == null) || (initial_amount.compareTo(BigDecimal.ZERO) <= 0))
          && (original_amount.compareTo(BigDecimal.ZERO) <= 0)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Loan_amountevent"));
      }
      if (installmentamount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_loan_installment_amount"));
      }
      if (loantype.equals("HO")) {
        if (loan.getHoldDate() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_loan_hold_date"));
        }
        if (loan.getHoldDuration() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_loan_hold_duration"));
        }
        if (loan.getHoldDurationType() == null) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_loan_hold_duration_type"));
        }
      }
      if (!loantype.equals("CR")) {
        if (loan.getOriginalDecisionNo() == null) {
          throw new OBException(OBMessageUtils.messageBD("ehcm_loan_sec_check"));
        }
      }
    } catch (OBException e) {
      log.error(" Exception while creating Loan Transactions: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final EHCMLoanTransaction loan = (EHCMLoanTransaction) event.getTargetInstance();
    String status = loan.getDecisionStatus();
    try {
      if ((status.equals("I"))) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Issue_delete"));
      }
    } catch (OBException e) {
      log.error(" Exception while creating Loan Transactions: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
