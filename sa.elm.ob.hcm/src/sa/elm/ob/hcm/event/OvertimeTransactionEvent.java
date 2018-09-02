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

import sa.elm.ob.hcm.EhcmEmployeeOvertime;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;

/**
 * @author poongodi on 08/05/2018
 */
public class OvertimeTransactionEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmEmployeeOvertime.ENTITY_NAME) };

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

      EhcmEmployeeOvertime overtimeObj = (EhcmEmployeeOvertime) event.getTargetInstance();

      if (overtimeObj.getEndDate().compareTo(overtimeObj.getStartDate()) == -1) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Greaterthan_Date"));
      }

      if (overtimeObj.getDecisionType()
          .equals(DecisionTypeConstants.DECISION_TYPE_OVERTIMEPAYMENT)) {
        /*
         * if (overtimeObj.getPayrollPeriod() == null) { throw new
         * OBException(OBMessageUtils.messageBD("Ehcm_payrollPeriod_Mandatory")); }
         */
        if (overtimeObj.getPaymentEndDate() != null) {
          if (overtimeObj.getEndDate().compareTo(overtimeObj.getPaymentEndDate()) == -1) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Greaterthan_Enddate"));
          }
          if (overtimeObj.getPaymentEndDate().compareTo(overtimeObj.getPaymentStartDate()) == -1) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Lesserthan_PaymentStartdate"));
          }
        }
      }

      if ((overtimeObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE))
          || (overtimeObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL))) {
        if (overtimeObj.getOriginalDecisionNo() == null)
          throw new OBException(OBMessageUtils.messageBD("Ehcm_overtime_orgdecisionno"));
      }
    } catch (OBException e) {
      log.error(" Exception while updating OvertimeTransactionEvent  " + e);
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

      EhcmEmployeeOvertime overtimeObj = (EhcmEmployeeOvertime) event.getTargetInstance();

      if (overtimeObj.getEndDate().compareTo(overtimeObj.getStartDate()) == -1) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Greaterthan_Date"));
      }
      if (overtimeObj.getDecisionType()
          .equals(DecisionTypeConstants.DECISION_TYPE_OVERTIMEPAYMENT)) {
        /*
         * if (overtimeObj.getPayrollPeriod() == null) { throw new
         * OBException(OBMessageUtils.messageBD("Ehcm_payrollPeriod_Mandatory")); }
         */
        if (overtimeObj.getPaymentEndDate() != null) {
          if (overtimeObj.getEndDate().compareTo(overtimeObj.getPaymentEndDate()) == -1) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Greaterthan_Enddate"));
          }
          if (overtimeObj.getPaymentEndDate().compareTo(overtimeObj.getPaymentStartDate()) == -1) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Lesserthan_PaymentStartdate"));
          }
        }
      }
      if ((overtimeObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE))
          || (overtimeObj.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL))) {
        if (overtimeObj.getOriginalDecisionNo() == null)
          throw new OBException(OBMessageUtils.messageBD("Ehcm_overtime_orgdecisionno"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating overtime Transaction   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
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
      EhcmEmployeeOvertime overtimeObj = (EhcmEmployeeOvertime) event.getTargetInstance();
      if (overtimeObj.getDecisionStatus().equals("I")) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Already_Processed"));
      }

    } catch (OBException e) {
      log.error(" Exception while Deleting overtime Transaction : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
