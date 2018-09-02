package sa.elm.ob.hcm.event;

import java.sql.PreparedStatement;
import java.util.Date;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DalConnectionProvider;

import sa.elm.ob.hcm.EhcmAddressStyle;
import sa.elm.ob.hcm.Ehcm_Location;

public class AddressstyleEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmAddressStyle.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      ConnectionProvider conn = new DalConnectionProvider(false);
      PreparedStatement ps = null;
      OBContext.setAdminMode();
      EhcmAddressStyle addrstyle = (EhcmAddressStyle) event.getTargetInstance();
      Date startDate = addrstyle.getStartingDate();
      Date endDate = addrstyle.getEndingDate();

      if (startDate != null && endDate != null) {
        if (endDate.compareTo(startDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Enddate"));
        }
      }
      if (!addrstyle.isAddress1Ck() && !addrstyle.isAddress2Ck() && !addrstyle.isStreetCk()
          && !addrstyle.isDistrictCk() && !addrstyle.isPostboxCk() && !addrstyle.isCityCk()
          && !addrstyle.isPostcodeCk() && !addrstyle.isCountryCk()) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_addressfield_mandatory"));
      }
      OBQuery<Ehcm_Location> location = OBDal.getInstance().createQuery(Ehcm_Location.class,
          "addressStyle.id='" + addrstyle.getId() + "'");
      if (location.list().size() > 0) {
        if (!addrstyle.isAddress1Ck()) {
          ps = conn.getPreparedStatement(
              "update ehcm_location set address1='' where ehcm_address_style_id='"
                  + addrstyle.getId() + "'");
          ps.executeUpdate();
        }
        if (!addrstyle.isAddress2Ck()) {
          ps = conn.getPreparedStatement(
              "update ehcm_location set address2='' where ehcm_address_style_id='"
                  + addrstyle.getId() + "'");
          ps.executeUpdate();
        }
        if (!addrstyle.isStreetCk()) {
          ps = conn.getPreparedStatement(
              "update ehcm_location set street='' where ehcm_address_style_id='" + addrstyle.getId()
                  + "'");
          ps.executeUpdate();
        }
        if (!addrstyle.isDistrictCk()) {
          ps = conn.getPreparedStatement(
              "update ehcm_location set district='' where ehcm_address_style_id='"
                  + addrstyle.getId() + "'");
          ps.executeUpdate();
        }
        if (!addrstyle.isPostboxCk()) {
          ps = conn.getPreparedStatement(
              "update ehcm_location set postbox='' where ehcm_address_style_id='"
                  + addrstyle.getId() + "'");
          ps.executeUpdate();
        }
        if (!addrstyle.isCountryCk()) {
          ps = conn.getPreparedStatement(
              "update ehcm_location set c_country_id=null where ehcm_address_style_id='"
                  + addrstyle.getId() + "'");
          ps.executeUpdate();
        }
        if (!addrstyle.isCityCk()) {
          ps = conn.getPreparedStatement(
              "update ehcm_location set c_city_id=null where ehcm_address_style_id='"
                  + addrstyle.getId() + "'");
          ps.executeUpdate();
        }
        if (!addrstyle.isPostcodeCk()) {
          ps = conn.getPreparedStatement(
              "update ehcm_location set postalcode='' where ehcm_address_style_id='"
                  + addrstyle.getId() + "'");
          ps.executeUpdate();
        }
        if (!addrstyle.isActive()) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_check_isactive"));
        }
      }
    } catch (OBException e) {
      log.error(" Exception while creating Address Style: " + e);
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
      EhcmAddressStyle addrstyle = (EhcmAddressStyle) event.getTargetInstance();
      Date startDate = addrstyle.getStartingDate();
      Date endDate = addrstyle.getEndingDate();

      if (startDate != null && endDate != null) {
        if (endDate.compareTo(startDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Enddate"));
        }
      }
      if (!addrstyle.isAddress1Ck() && !addrstyle.isAddress2Ck() && !addrstyle.isStreetCk()
          && !addrstyle.isDistrictCk() && !addrstyle.isPostboxCk() && !addrstyle.isCityCk()
          && !addrstyle.isPostcodeCk() && !addrstyle.isCountryCk()) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_addressfield_mandatory"));
      }
    } catch (OBException e) {
      log.error(" Exception while creating Address Style: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
