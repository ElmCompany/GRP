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
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EhcmAddNationality;

public class NationalityEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EhcmAddNationality.ENTITY_NAME) };

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
      EhcmAddNationality nat = (EhcmAddNationality) event.getTargetInstance();
      String natid = nat.getId();
      String countryId = nat.getCountry().getId();
      OBQuery<EhcmAddNationality> natqry = OBDal.getInstance().createQuery(EhcmAddNationality.class,
          "as e where e.country.id='" + countryId + "' and e.id <> '" + natid + "'");
      if (natqry.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("@Ehcm_duplicateNationality@"));
      }
    } catch (OBException e) {
      log.error(" Exception while creating Nationality: " + e);
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
      EhcmAddNationality nat = (EhcmAddNationality) event.getTargetInstance();
      String natid = nat.getId();
      String countryId = nat.getCountry().getId();
      OBQuery<EhcmAddNationality> natqry = OBDal.getInstance().createQuery(EhcmAddNationality.class,
          "as e where e.country.id='" + countryId + "' and e.id <> '" + natid + "'");
      if (natqry.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("@Ehcm_duplicateNationality@"));
      }
    } catch (OBException e) {
      log.error(" Exception while creating Nationality: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
