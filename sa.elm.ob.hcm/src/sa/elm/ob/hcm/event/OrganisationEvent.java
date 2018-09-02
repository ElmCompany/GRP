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
import org.openbravo.model.common.enterprise.Organization;

public class OrganisationEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(Organization.ENTITY_NAME) };

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
      OBContext.setAdminMode();
      Organization org = (Organization) event.getTargetInstance();
      final Property parentOrg = entities[0].getProperty(Organization.PROPERTY_EHCMPARENTORG);
      /*
       * if (org.getEhcmOrgtyp() != null) { Long level = org.getEhcmOrgtyp().getLevel();
       * 
       * if ((level != 1 && level != 2) && org.getEhcmAdOrg() == null) { throw new
       * OBException("@Ehcm_Orgparent@"); } }
       */
      if (event.getCurrentState(parentOrg) != null
          && !event.getCurrentState(parentOrg).equals(event.getPreviousState(parentOrg))) {
        if (org.getId().equals(org.getEhcmParentOrg().getId())) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_OrgSetSameAsParentOrg"));

        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating Organization: " + e);
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
      Organization org = (Organization) event.getTargetInstance();
      /*
       * if(org.getEhcmOrgtyp()!=null) { Long level = org.getEhcmOrgtyp().getLevel();
       * 
       * if ((level != 1 && level != 2) && org.getEhcmAdOrg()==null ) { throw new
       * OBException("@Ehcm_Orgparent@"); } }
       */
    } catch (OBException e) {
      log.error(" Exception while creating Organization: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
