package sa.elm.ob.hcm.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMPayrollProcessHdr;
import sa.elm.ob.hcm.event.dao.PayrollProcessEventDAO;

/**
 * 
 * @author Gokul 05/09/2018
 *
 */

public class PayrollProcessEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMPayrollProcessHdr.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());
  PayrollProcessEventDAO dao = new PayrollProcessEventDAO();

  public void onDelete(@Observes EntityDeleteEvent event) {

    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMPayrollProcessHdr payrollHdr = (EHCMPayrollProcessHdr) event.getTargetInstance();
      String payroll = payrollHdr.getPayroll().getId();
      String payrollPeriod = payrollHdr.getPayrollPeriod().getId();
      String elementGroup = payrollHdr.getElementGroup().getId();
      Boolean isExist = true;
      isExist = dao.deleteHeader(payroll, payrollPeriod, elementGroup);

    } catch (OBException e) {
      log.error(" Exception while Payroll process event   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      log.error(" Exception Payroll Process event   " + e);
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }

}
