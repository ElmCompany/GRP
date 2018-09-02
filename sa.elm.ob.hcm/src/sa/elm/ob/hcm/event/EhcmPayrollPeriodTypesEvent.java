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

import sa.elm.ob.hcm.EHCMPayrollPeriodTypes;

/**
 * @author Priyanka Ranjan on 12/01/2017
 */

public class EhcmPayrollPeriodTypesEvent extends EntityPersistenceEventObserver {

  /**
   * Event for validate the periodtypecode, periodtypecode should be Unique "Payroll Period Types"
   * Window
   */

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMPayrollPeriodTypes.ENTITY_NAME) };

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
      EHCMPayrollPeriodTypes Checkcode = (EHCMPayrollPeriodTypes) event.getTargetInstance();
      final Property code = entities[0].getProperty(EHCMPayrollPeriodTypes.PROPERTY_PERIODTYPECODE);

      OBQuery<EHCMPayrollPeriodTypes> PayrollPeriodTypes = OBDal.getInstance().createQuery(
          EHCMPayrollPeriodTypes.class,
          "as e where e.periodtypecode='" + Checkcode.getPeriodtypecode() + "' and e.client.id='"
              + Checkcode.getClient().getId() + "'");
      if (!event.getPreviousState(code).equals(event.getCurrentState(code))) {
        if (PayrollPeriodTypes.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_PayrollPeriodTypes_Unique_Code"));
        }
      }
    } catch (OBException e) {
      log.error(" Exception while Updating PayrollPeriodTypes: " + e);
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
      EHCMPayrollPeriodTypes Checkcode = (EHCMPayrollPeriodTypes) event.getTargetInstance();
      OBQuery<EHCMPayrollPeriodTypes> PayrollPeriodTypes = OBDal.getInstance().createQuery(
          EHCMPayrollPeriodTypes.class,
          "as e where e.periodtypecode='" + Checkcode.getPeriodtypecode() + "' and e.client.id='"
              + Checkcode.getClient().getId() + "'");
      if (PayrollPeriodTypes.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_PayrollPeriodTypes_Unique_Code"));
      }
    } catch (OBException e) {
      log.error(" Exception while creating PayrollPeriodTypes: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }

  }
}
