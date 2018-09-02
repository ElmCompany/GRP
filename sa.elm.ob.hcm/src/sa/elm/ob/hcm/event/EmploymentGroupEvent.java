package sa.elm.ob.hcm.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EhcmEmploymentGroup;
import sa.elm.ob.hcm.event.dao.EmploymentGroupEventDAO;
import sa.elm.ob.hcm.event.dao.EmploymentGroupEventDAOImp;

/**
 * @author Gowtham on 29/05/2018
 */
public class EmploymentGroupEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmEmploymentGroup.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());
  EmploymentGroupEventDAO EmpDaoImp = new EmploymentGroupEventDAOImp();

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      boolean Status = false;

      // end date should be greater than last payroll end date.
      EhcmEmploymentGroup employment = (EhcmEmploymentGroup) event.getTargetInstance();
      if (employment.getEndDate() != null) {
        Status = EmpDaoImp.checkValidEnddate(employment);
        if (!Status) {
          throw new OBException(OBMessageUtils.messageBD("ehcm_enddate>payroll_date"));
        }
      }
    } catch (OBException e) {
      if (log.isDebugEnabled())
        log.error(" Exception while Employment group update event   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @SuppressWarnings("unused")
  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      boolean Status = false;

      // end date should be greater than last payroll end date.
      EhcmEmploymentGroup employment = (EhcmEmploymentGroup) event.getTargetInstance();
      if (employment.getEndDate() != null) {
        Status = EmpDaoImp.checkValidEnddate(employment);
        if (!Status) {
          throw new OBException(OBMessageUtils.messageBD("ehcm_enddate>payroll_date"));
        }
      }
    } catch (OBException e) {
      if (log.isDebugEnabled())
        log.error(" Exception while employment save event  " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
