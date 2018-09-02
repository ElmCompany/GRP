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
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMPayPmtTypeMethod;

/**
 * @author Priyanka Ranjan on 12/01/2017
 */

public class EhcmPayrollPaymentTypesMethodsEvent extends EntityPersistenceEventObserver {

  /**
   * Event for validate the paymenttypecode, paymenttypecode should be Unique "Payroll Payment Types
   * and Methods" Window
   */

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMPayPmtTypeMethod.ENTITY_NAME) };

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
      EHCMPayPmtTypeMethod CheckPaymentcode = (EHCMPayPmtTypeMethod) event.getTargetInstance();
      final Property code = entities[0].getProperty(EHCMPayPmtTypeMethod.PROPERTY_PAYMENTTYPECODE);
      final Property isdefault = entities[0].getProperty(EHCMPayPmtTypeMethod.PROPERTY_DEFAULT);
      OBQuery<EHCMPayPmtTypeMethod> PayrollPayTypeMethod = OBDal.getInstance().createQuery(
          EHCMPayPmtTypeMethod.class,
          "as e where e.paymenttypecode='" + CheckPaymentcode.getPaymenttypecode()
              + "' and e.client.id='" + CheckPaymentcode.getClient().getId() + "'");

      if ((CheckPaymentcode.getEHCMPayrollPaymentMethodList().size() > 0)
          && (!CheckPaymentcode.isBanktransfer())) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Payroll_check"));
      }

      if (!event.getPreviousState(code).equals(event.getCurrentState(code))) {
        if (PayrollPayTypeMethod.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_payrollPayTypeMethods_Unique_Code"));
        }
      }
      if (CheckPaymentcode.isDefault()) {
        if (!event.getPreviousState(isdefault).equals(event.getCurrentState(isdefault))) {
          OBQuery<EHCMPayPmtTypeMethod> def = OBDal.getInstance()
              .createQuery(EHCMPayPmtTypeMethod.class, "as e where e.default='" + 'Y'
                  + "' and e.client.id='" + CheckPaymentcode.getClient().getId() + "'");
          if (def.list().size() > 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_Pay_Pmt_Type_Method_Default"));
          }
        }
      }
    } catch (OBException e) {
      log.error(" Exception while Updating PayrollPayTypeMethod: " + e);
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
      EHCMPayPmtTypeMethod CheckPaymentcode = (EHCMPayPmtTypeMethod) event.getTargetInstance();
      OBQuery<EHCMPayPmtTypeMethod> PayrollPayTypeMethod = OBDal.getInstance().createQuery(
          EHCMPayPmtTypeMethod.class,
          "as e where e.paymenttypecode='" + CheckPaymentcode.getPaymenttypecode()
              + "' and e.client.id='" + CheckPaymentcode.getClient().getId() + "'");
      if ((CheckPaymentcode.getEHCMPayrollPaymentMethodList().size() > 0)
          && (!CheckPaymentcode.isBanktransfer())) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Payroll_check"));
      }
      if (PayrollPayTypeMethod.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_payrollPayTypeMethods_Unique_Code"));
      }
      if (CheckPaymentcode.isDefault()) {
        OBQuery<EHCMPayPmtTypeMethod> def = OBDal.getInstance()
            .createQuery(EHCMPayPmtTypeMethod.class, "as e where e.default='" + 'Y'
                + "' and e.client.id='" + CheckPaymentcode.getClient().getId() + "'");
        if (def.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_Pay_Pmt_Type_Method_Default"));
        }
      }
    } catch (OBException e) {
      log.error(" Exception while creating PayrollPayTypeMethod: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }

  }
}
