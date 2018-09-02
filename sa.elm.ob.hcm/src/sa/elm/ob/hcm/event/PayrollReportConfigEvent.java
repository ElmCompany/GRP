package sa.elm.ob.hcm.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EhcmPayrollReportConfig;
import sa.elm.ob.hcm.event.dao.PayrollReportConfigDAO;
import sa.elm.ob.hcm.event.dao.PayrollReportConfigDAOImpl;

/**
 * Event Payroll Report Config Event
 * 
 * @author Sowmiya N S on 13/06/2018
 * 
 */

public class PayrollReportConfigEvent extends EntityPersistenceEventObserver {
  /**
   * Business Event on window Payroll report configuration
   */
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmPayrollReportConfig.ENTITY_NAME) };
  PayrollReportConfigDAO daoimpl = new PayrollReportConfigDAOImpl();

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger LOG = Logger.getLogger(this.getClass());

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EhcmPayrollReportConfig report = (EhcmPayrollReportConfig) event.getTargetInstance();
      boolean ismorethanone = false;
      ismorethanone = daoimpl.checkAlreadyRecordExist(report.getClient().getId());
      if (ismorethanone) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_PayrollReportConfig"));
      }
    } catch (OBException e) {
      LOG.error(" Exception while creating PayrollReportConfigEvent: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }

  }
}