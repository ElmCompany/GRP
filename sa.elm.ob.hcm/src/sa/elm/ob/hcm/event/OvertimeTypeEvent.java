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
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EhcmOvertimeType;
import sa.elm.ob.hcm.util.UtilityDAO;

/**
 * 
 * @author poongodi on 13-03-2018
 *
 */

public class OvertimeTypeEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmOvertimeType.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      boolean chkExist = false;
      EhcmOvertimeType overtimeType = (EhcmOvertimeType) event.getTargetInstance();
      // Atleast one overtime Type should check
      if (!overtimeType.isWorkingdays() && !overtimeType.isWeekendonedays()
          && !overtimeType.isWeekendtwodays() && !overtimeType.isHajjdays()
          && !overtimeType.isFeterdays() && !overtimeType.isNationalday()) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Days_Empty"));

      }
      // OverTime Type should be unique
      chkExist = UtilityDAO.chkExistOverTimeType(overtimeType);
      if (chkExist) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Days_Unique"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating OvertimeTypeEvent " + e);
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
      boolean chkExist = false;
      boolean chkOvertimeRecord = false;
      EhcmOvertimeType overtimeType = (EhcmOvertimeType) event.getTargetInstance();
      Property workingDay = entities[0].getProperty(EhcmOvertimeType.PROPERTY_WORKINGDAYS);
      Property weekendOneDay = entities[0].getProperty(EhcmOvertimeType.PROPERTY_WEEKENDONEDAYS);
      Property weekendTwoDay = entities[0].getProperty(EhcmOvertimeType.PROPERTY_WEEKENDTWODAYS);
      Property feterDay = entities[0].getProperty(EhcmOvertimeType.PROPERTY_FETERDAYS);
      Property hajjDay = entities[0].getProperty(EhcmOvertimeType.PROPERTY_HAJJDAYS);
      Property nationalDay = entities[0].getProperty(EhcmOvertimeType.PROPERTY_NATIONALDAY);
      Property typeName = entities[0].getProperty(EhcmOvertimeType.PROPERTY_OVERTYPENAME);

      // Atleast need to check one overtime Type
      if (!overtimeType.isWorkingdays() && !overtimeType.isWeekendonedays()
          && !overtimeType.isWeekendtwodays() && !overtimeType.isHajjdays()
          && !overtimeType.isFeterdays() && !overtimeType.isNationalday()) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Days_Empty"));

      }
      // Overtime Type should be unique
      if (event.getPreviousState(typeName).equals(event.getCurrentState(typeName))) {
        chkExist = UtilityDAO.chkExistOverTimeType(overtimeType);
        if (chkExist) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Days_Unique"));

        }
      }
      // should not allow to change if the type is used in Overtime Transaction
      if (!event.getPreviousState(workingDay).equals(event.getCurrentState(workingDay))
          || !event.getPreviousState(weekendOneDay).equals(event.getCurrentState(weekendOneDay))
          || !event.getPreviousState(weekendTwoDay).equals(event.getCurrentState(weekendTwoDay))
          || !event.getPreviousState(feterDay).equals(event.getCurrentState(feterDay))
          || !event.getPreviousState(hajjDay).equals(event.getCurrentState(hajjDay))
          || !event.getPreviousState(nationalDay).equals(event.getCurrentState(nationalDay))) {
        chkOvertimeRecord = UtilityDAO.chkRecordinTransaction(overtimeType);
        if (chkOvertimeRecord) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Days_Change"));
        }

      }

    } catch (OBException e) {
      log.error(" Exception while updating Overtime Type: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
