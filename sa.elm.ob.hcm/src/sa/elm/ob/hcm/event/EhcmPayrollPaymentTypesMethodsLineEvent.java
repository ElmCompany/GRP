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

import sa.elm.ob.hcm.EHCMPayrollPaymentMethod;

/**
 * @author Priyanka Ranjan on 12/01/2017
 */

public class EhcmPayrollPaymentTypesMethodsLineEvent extends EntityPersistenceEventObserver {

  /**
   * Event for validate the Default Value in "Payroll Payment Types and Methods line- Payment
   * Methods" Window
   */

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMPayrollPaymentMethod.ENTITY_NAME) };

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
      EHCMPayrollPaymentMethod Checkdefault = (EHCMPayrollPaymentMethod) event.getTargetInstance();
      final Property isdefault = entities[0].getProperty(EHCMPayrollPaymentMethod.PROPERTY_DEFAULT);
      final Property name = entities[0].getProperty(EHCMPayrollPaymentMethod.PROPERTY_NAME);

      if (!event.getPreviousState(isdefault).equals(event.getCurrentState(isdefault))) {
        if (Checkdefault.isDefault()) {
          OBQuery<EHCMPayrollPaymentMethod> def = OBDal.getInstance().createQuery(
              EHCMPayrollPaymentMethod.class,
              "as e where e.default='" + 'Y' + "' and e.client.id='"
                  + Checkdefault.getClient().getId() + "' and e.ehcmPayPmtTypeMethod.id='"
                  + Checkdefault.getEhcmPayPmtTypeMethod().getId() + "'");
          if (def.list().size() > 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_Default_PayrollPaymentMethod"));
          }
        }
      }
      if (!event.getPreviousState(name).equals(event.getCurrentState(name))) {
        OBQuery<EHCMPayrollPaymentMethod> namealready = OBDal.getInstance().createQuery(
            EHCMPayrollPaymentMethod.class, "as e where e.name='" + Checkdefault.getName()
                + "' and e.client.id='" + Checkdefault.getClient().getId() + "'");
        if (namealready.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_PayrollPmtTypeMeth_Name_Unique"));
        }

      }
    } catch (OBException e) {
      log.error(" Exception while Updating PayrollPayTypeMethodLine: " + e);
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
      EHCMPayrollPaymentMethod Checkdefault = (EHCMPayrollPaymentMethod) event.getTargetInstance();
      if (Checkdefault.isDefault()) {
        OBQuery<EHCMPayrollPaymentMethod> def = OBDal.getInstance().createQuery(
            EHCMPayrollPaymentMethod.class,
            "as e where e.default='" + 'Y' + "' and e.client.id='"
                + Checkdefault.getClient().getId() + "' and e.ehcmPayPmtTypeMethod.id='"
                + Checkdefault.getEhcmPayPmtTypeMethod().getId() + "'");
        if (def.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_Default_PayrollPaymentMethod"));
        }
      }
      OBQuery<EHCMPayrollPaymentMethod> namealready = OBDal.getInstance().createQuery(
          EHCMPayrollPaymentMethod.class, "as e where e.name='" + Checkdefault.getName()
              + "' and e.client.id='" + Checkdefault.getClient().getId() + "'");
      if (namealready.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_PayrollPmtTypeMeth_Name_Unique"));
      }
    } catch (OBException e) {
      log.error(" Exception while creating PayrollPayTypeMethodLine: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }

  }
}
