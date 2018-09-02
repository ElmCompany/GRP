package sa.elm.ob.hcm.event;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import javax.enterprise.event.Observes;

import org.apache.commons.lang.StringUtils;
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

import sa.elm.ob.hcm.EHCMDeflookupsTypeLn;
import sa.elm.ob.hcm.util.UtilityDAO;

/**
 * 
 * @author poongodi on 12/03/2018
 *
 */

public class EhcmReferencelookupEvent extends EntityPersistenceEventObserver {

  private Logger log = Logger.getLogger(this.getClass());
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMDeflookupsTypeLn.ENTITY_NAME) };
  private static final String ReferenceType = "EOT";

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      boolean chkNumeric = false;
      String number = "";
      BigDecimal maxHours = BigDecimal.ZERO;
      boolean negativeFlag = true;
      int minutes = 59;
      int hours = 23;
      EHCMDeflookupsTypeLn lookupLine = (EHCMDeflookupsTypeLn) event.getTargetInstance();
      final Property oldName = entities[0].getProperty(EHCMDeflookupsTypeLn.PROPERTY_NAME);
      if (lookupLine.getEhcmDeflookupsType().getReference().equals(ReferenceType)) {
        // To Change name into decimal points
        DecimalFormat f = new DecimalFormat("##.00");
        String Name = lookupLine.getName();
        String newName = f.format(new BigDecimal(Name));
        // Checking negative values
        negativeFlag = chkisnegative(lookupLine.getName());
        if (!negativeFlag) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Restrict_Negative"));
        } else {
          // Check only numeric values
          chkNumeric = isNumeric(lookupLine.getName());
          if (!chkNumeric) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Numbers_Only"));
          }
          // if minutes reached greater than 59 then throw the error
          if (Integer.parseInt(newName.substring(newName.indexOf(".")).substring(1)) > minutes) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Invalid_Minutes"));
          }
          // If hours greater than 23 then throw the error
          maxHours = UtilityDAO.getHoursfromLookup(lookupLine.getClient().getId(), newName);

          if (maxHours.compareTo(new BigDecimal(hours)) > 0) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Invalid_Time"));
          }

        }
        // Set as decimal value in name field
        if (newName.equals("false") || StringUtils.isEmpty(newName)) {
          throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
        } else {
          event.setCurrentState(oldName, newName);
        }
      }

    } catch (OBException e) {
      log.debug("exception while creating reference lookup" + e);
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
      boolean chkNumeric = false;
      String number = "";
      BigDecimal maxHours = BigDecimal.ZERO;
      boolean negativeFlag = true;
      int minutes = 59;
      int hours = 23;
      EHCMDeflookupsTypeLn lookupLine = (EHCMDeflookupsTypeLn) event.getTargetInstance();
      final Property oldName = entities[0].getProperty(EHCMDeflookupsTypeLn.PROPERTY_NAME);
      if (lookupLine.getEhcmDeflookupsType().getReference().equals(ReferenceType)) {
        // To Change name into decimal points
        DecimalFormat f = new DecimalFormat("##.00");
        String Name = lookupLine.getName();
        String newName = f.format(new BigDecimal(Name));
        // Checking negative values
        negativeFlag = chkisnegative(lookupLine.getName());
        if (!negativeFlag) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Restrict_Negative"));
        } else {
          // Check only numeric values
          chkNumeric = isNumeric(lookupLine.getName());
          if (!chkNumeric) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Numbers_Only"));
          }
          // if minutes reached greater than 59 then throw the error
          if (Integer.parseInt(newName.substring(newName.indexOf(".")).substring(1)) > minutes) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Invalid_Minutes"));
          }
          // If hours greater than 23 then throw the error
          maxHours = UtilityDAO.getHoursfromLookup(lookupLine.getClient().getId(), newName);

          if (maxHours.compareTo(new BigDecimal(hours)) > 0) {
            throw new OBException(OBMessageUtils.messageBD("Ehcm_Invalid_Time"));
          }
        }
        // Set as decimal value in name field
        if (newName.equals("false") || StringUtils.isEmpty(newName)) {
          throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
        } else {
          event.setCurrentState(oldName, newName);
        }
      }

    } catch (OBException e) {
      log.debug("exception while updating lookup" + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  public static boolean isNumeric(String str) {
    try {
      if (str.equals("") && str.isEmpty()) {
        str = "0";
      }
      double d = Double.parseDouble(str);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  public static boolean chkisnegative(String cell) {
    try {
      if (cell.equals("") && cell.isEmpty()) {
        cell = "0";
      }
      if (new BigDecimal(cell).compareTo(BigDecimal.ZERO) < 0) {
        return false;
      } else {
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();

    }
    return true;
  }

}
