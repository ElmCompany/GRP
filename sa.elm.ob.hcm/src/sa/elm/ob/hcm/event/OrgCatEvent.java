package sa.elm.ob.hcm.event;

import java.util.Date;

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
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMOrgCategory;

public class OrgCatEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMOrgCategory.ENTITY_NAME) };

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
      EHCMOrgCategory cattype = (EHCMOrgCategory) event.getTargetInstance();
      final Property typename = entities[0].getProperty(EHCMOrgCategory.PROPERTY_ORGCATNAME);
      final Property typecode = entities[0].getProperty(EHCMOrgCategory.PROPERTY_SEARCHKEY);
      final Property Startdate = entities[0].getProperty(EHCMOrgCategory.PROPERTY_STARTDATE);
      final Property Enddate = entities[0].getProperty(EHCMOrgCategory.PROPERTY_ENDDATE);

      OBQuery<EHCMOrgCategory> type = OBDal.getInstance().createQuery(EHCMOrgCategory.class,
          "value=:value and client.id =:client ");
      type.setNamedParameter("value", cattype.getSearchKey());
      type.setNamedParameter("client", cattype.getClient().getId());
      if (!event.getPreviousState(typecode).equals(event.getCurrentState(typecode))) {
        if (type.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_CatTypeCode"));

        }
      }
      OBQuery<EHCMOrgCategory> type1 = OBDal.getInstance().createQuery(EHCMOrgCategory.class,
          "orgcatname =:orgcatname  and client.id =:client");
      type1.setNamedParameter("orgcatname", cattype.getOrgcatname());
      type1.setNamedParameter("client", cattype.getClient().getId());
      if (!event.getPreviousState(typename).equals(event.getCurrentState(typename))) {
        if (type1.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_CatTypeName"));

        }
      }
      Date startDate = cattype.getStartDate();
      Date endDate = cattype.getEndDate();

      if (startDate != null && endDate != null) {
        if (endDate.compareTo(startDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Enddate"));
        }

      }
    } catch (OBException e) {
      log.error(" Exception while creating orgtype in Organization: " + e);
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
      EHCMOrgCategory cattype = (EHCMOrgCategory) event.getTargetInstance();
      // Organization type code should be unique
      OBQuery<EHCMOrgCategory> typecode = OBDal.getInstance().createQuery(EHCMOrgCategory.class,
          "value=:value  and client.id =:client");
      typecode.setNamedParameter("value", cattype.getSearchKey());
      typecode.setNamedParameter("client", cattype.getClient().getId());
      if (typecode.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_CatTypeCode"));
      }
      // Organization type name should be unique
      OBQuery<EHCMOrgCategory> typename = OBDal.getInstance().createQuery(EHCMOrgCategory.class,
          "orgcatname =:name and client.id =:client");
      typename.setNamedParameter("name", cattype.getOrgcatname());
      typename.setNamedParameter("client", cattype.getClient().getId());
      if (typename.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_CatTypeName"));
      }
      Date startDate = cattype.getStartDate();
      Date endDate = cattype.getEndDate();
      if (startDate != null && endDate != null) {
        if (endDate.compareTo(startDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Enddate"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating classtype in Organization: " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
