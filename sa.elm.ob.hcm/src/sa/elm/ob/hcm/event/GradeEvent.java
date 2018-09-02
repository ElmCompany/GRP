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
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.ehcmgrade;

public class GradeEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ehcmgrade.ENTITY_NAME) };

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
      ehcmgrade grade = (ehcmgrade) event.getTargetInstance();
      final Property typecode = entities[0].getProperty(ehcmgrade.PROPERTY_SEARCHKEY);
      final Property typename = entities[0].getProperty(ehcmgrade.PROPERTY_COMMERCIALNAME);
      final Property sequencetype = entities[0].getProperty(ehcmgrade.PROPERTY_SEQUENCENUMBER);

      if (grade.getEndDate() != null) {
        if (grade.getEndDate().compareTo(grade.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EndDateStartDateComp"));
        }
      }

      OBQuery<ehcmgrade> type = OBDal.getInstance().createQuery(ehcmgrade.class,
          "  name=:name and client.id =:client ");
      type.setNamedParameter("name", grade.getCommercialName());
      type.setNamedParameter("client", grade.getClient().getId());
      if (!event.getPreviousState(typename).equals(event.getCurrentState(typename))) {

        if (type.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));

        }
      }
      OBQuery<ehcmgrade> type1 = OBDal.getInstance().createQuery(ehcmgrade.class,
          "  searchKey=:value and client.id =:client ");
      type1.setNamedParameter("value", grade.getSearchKey());
      type1.setNamedParameter("client", grade.getClient().getId());
      if (!event.getPreviousState(typecode).equals(event.getCurrentState(typecode))) {
        if (type1.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));

        }
      }
      OBQuery<ehcmgrade> seqtype = OBDal.getInstance().createQuery(ehcmgrade.class,
          " sequenceNumber =:seqno and client.id =:client ");
      seqtype.setNamedParameter("seqno", grade.getSequenceNumber());
      seqtype.setNamedParameter("client", grade.getClient().getId());
      if (!event.getPreviousState(sequencetype).equals(event.getCurrentState(sequencetype))) {
        if (seqtype.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Lineno"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating grade event: " + e);
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
      ehcmgrade grade = (ehcmgrade) event.getTargetInstance();

      if (grade.getEndDate() != null) {
        if (grade.getEndDate().compareTo(grade.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EndDateStartDateComp"));
        }
      }

      OBQuery<ehcmgrade> type = OBDal.getInstance().createQuery(ehcmgrade.class,
          " ( name=:name or searchKey =:value ) and client.id =:client ");
      type.setNamedParameter("name", grade.getCommercialName());
      type.setNamedParameter("value", grade.getSearchKey());
      type.setNamedParameter("client", grade.getClient().getId());
      if (type.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Gradeclass"));
      }
      OBQuery<ehcmgrade> type1 = OBDal.getInstance().createQuery(ehcmgrade.class,
          " sequenceNumber =:seqno and client.id =:client ");
      type1.setNamedParameter("seqno", grade.getSequenceNumber());
      type1.setNamedParameter("client", grade.getClient().getId());
      if (type1.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("Ehcm_Lineno"));
      }

    } catch (OBException e) {
      log.error(" Exception while creating grade event " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
