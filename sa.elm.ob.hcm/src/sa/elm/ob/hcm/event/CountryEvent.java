package sa.elm.ob.hcm.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.geography.Country;

import sa.elm.ob.hcm.event.dao.MissionCategoryDAO;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAOImpl;

public class CountryEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = { ModelProvider.getInstance().getEntity(Country.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());
  MissionCategoryDAOImpl missionCategoryDAOImpl = new MissionCategoryDAOImpl();
  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();
      Country country = (Country) event.getTargetInstance();
      final Property noofdaysAfter = entities[0].getProperty(Country.PROPERTY_EHCMNOOFDAYSAFTER);
      final Property noofdaysBefore = entities[0].getProperty(Country.PROPERTY_EHCMNOOFDAYSBEFORE);
      final Property category = entities[0].getProperty(Country.PROPERTY_EHCMCATEGORY);
      if (event.getCurrentState(noofdaysBefore) != null && !event.getCurrentState(noofdaysBefore)
          .equals(event.getPreviousState(noofdaysBefore))) {
    	  missionCategoryDAOImpl.updateNoofDaysAfterBefore(country.getId(), null,
            country.getEhcmNoofdaysBefore(), null);
      }
      if (event.getCurrentState(noofdaysAfter) != null
          && !event.getCurrentState(noofdaysAfter).equals(event.getPreviousState(noofdaysAfter))) {
    	  missionCategoryDAOImpl.updateNoofDaysAfterBefore(country.getId(),
            country.getEhcmNoofdaysAfter(), null, null);
      }
      if (event.getCurrentState(category) != null
          && !event.getCurrentState(category).equals(event.getPreviousState(category))) {
    	  missionCategoryDAOImpl.updateNoofDaysAfterBefore(country.getId(), null, null,
            country.getEhcmCategory());
      }
    } catch (OBException e) {
      log.error(" Exception while creating Region: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
