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

import sa.elm.ob.hcm.EmploymentGroupLines;
import sa.elm.ob.hcm.event.dao.EmploymentGroupEventDAO;
import sa.elm.ob.hcm.event.dao.EmploymentGroupEventDAOImp;

/**
 * @author Gowtham on 29/05/2018
 */
public class EmploymentGrouplnEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EmploymentGroupLines.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());
  EmploymentGroupEventDAO dao = new EmploymentGroupEventDAOImp();

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      boolean Status = false;

      // Value field is mandatory
      EmploymentGroupLines employmentln = (EmploymentGroupLines) event.getTargetInstance();
      if (employmentln.getItem().equals("AGE") || employmentln.getItem().equals("POS")) {
        if (employmentln.getNumericValue() == null || employmentln.getNumericValue() <= 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Emp_Value_Mandatory"));
        }
      } else {
        if (employmentln.getEhcmEmpgrpValueV() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Emp_Value_Mandatory"));
        }
      }

      // age vaidation with client config.
      if (employmentln.getItem().equals("AGE")) {
        Status = dao.checkValidAge(employmentln.getNumericValue());
        if (!Status)
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Clinet_Age"));
      }
      // checking whether the groupcode is 0
      final EmploymentGroupLines code = (EmploymentGroupLines) event.getTargetInstance();
      Long Groupcode = code.getGroupCode();
      if (Groupcode == 0) {

        throw new OBException(OBMessageUtils.messageBD("Ehcm_groupcode_error"));
      }

    } catch (OBException e) {
      log.error(" Exception while Employment line group update event   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
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

      // Value field is mandatory
      EmploymentGroupLines employmentln = (EmploymentGroupLines) event.getTargetInstance();
      if (employmentln.getItem().equals("AGE") || employmentln.getItem().equals("POS")) {
        if (employmentln.getNumericValue() == null || employmentln.getNumericValue() <= 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Emp_Value_Mandatory"));
        }
      } else {
        if (employmentln.getEhcmEmpgrpValueV() == null) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Emp_Value_Mandatory"));
        }
      }

      // age vaidation with client config.
      if (employmentln.getItem().equals("AGE")) {
        Status = dao.checkValidAge(employmentln.getNumericValue());
        if (!Status)
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Clinet_Age"));
      }

      // checking whether the groupcode is 0
      final EmploymentGroupLines code = (EmploymentGroupLines) event.getTargetInstance();
      Long Groupcode = code.getGroupCode();
      if (Groupcode == 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_groupcode_error"));
      }

    } catch (OBException e) {
      if (log.isDebugEnabled())
        log.error(" Exception while employment line save event  " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
