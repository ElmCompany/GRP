package sa.elm.ob.hcm.event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMDeflookupsTypeLn;

public class HcmReferenceLookupDataTypeEvent extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMDeflookupsTypeLn.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final EHCMDeflookupsTypeLn datatypeObj = (EHCMDeflookupsTypeLn) event.getTargetInstance();
    if (datatypeObj.getEhcmDeflookupsType().getReference().equals("CR")) {
      if (datatypeObj.getEhcmDeflookupsType().getDatatype().equals("P")) {

        if (datatypeObj.getMin() == null || datatypeObj.getMaximum() == null) {
          throw new OBException(OBMessageUtils.messageBD("ehcm_mandatory"));

        } else {
          int min = 0;
          int max = 100;
          BigDecimal minvalue = BigDecimal.valueOf(min);
          BigDecimal maxvalue = BigDecimal.valueOf(max);

          if (datatypeObj.getMin().compareTo(minvalue) < 0
              || datatypeObj.getMaximum().compareTo(maxvalue) > 0) {
            throw new OBException(OBMessageUtils.messageBD("ehcm_length"));
          } else if (datatypeObj.getMin().compareTo(datatypeObj.getMaximum()) > 0) {
            throw new OBException(OBMessageUtils.messageBD("ehcm_minvaluegreater"));

          }

        }
      }
    }
    final EHCMDeflookupsTypeLn overlapObj = (EHCMDeflookupsTypeLn) event.getTargetInstance();
    OBQuery<EHCMDeflookupsTypeLn> referenceInfo = null;
    List<EHCMDeflookupsTypeLn> overlapList = new ArrayList<EHCMDeflookupsTypeLn>();
    if (!overlapObj.getEhcmDeflookupsType().isOverlappingallowed()) {
      String whereClause = "as e where e.organization.id = :organisation  "
          + "and ((e.min >= :minimum " + "and  e.maximum <= :maximum) "
          + "or (e.maximum >= :minimum" + " and e.min <= :maximum)) and e.id<>:id  ";
      referenceInfo = OBDal.getInstance().createQuery(EHCMDeflookupsTypeLn.class, whereClause);
      referenceInfo.setNamedParameter("organisation", overlapObj.getOrganization().getId());
      referenceInfo.setNamedParameter("minimum", overlapObj.getMin());
      referenceInfo.setNamedParameter("maximum", overlapObj.getMaximum());
      referenceInfo.setNamedParameter("id", overlapObj.getId());
      overlapList = referenceInfo.list();

      if (overlapList.size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("ehcm_overlap"));

      }
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final EHCMDeflookupsTypeLn datatypeObj = (EHCMDeflookupsTypeLn) event.getTargetInstance();
    if (datatypeObj.getEhcmDeflookupsType().getReference().equals("CR")) {
      if (datatypeObj.getEhcmDeflookupsType().getDatatype().equals("P")) {

        if (datatypeObj.getMin() == null || datatypeObj.getMaximum() == null) {
          throw new OBException(OBMessageUtils.messageBD("ehcm_mandatory"));

        } else {
          int min = 0;
          int max = 100;
          BigDecimal minvalue = BigDecimal.valueOf(min);
          BigDecimal maxvalue = BigDecimal.valueOf(max);

          if (datatypeObj.getMin().compareTo(minvalue) < 0
              || datatypeObj.getMaximum().compareTo(maxvalue) > 0) {
            throw new OBException(OBMessageUtils.messageBD("ehcm_length"));
          } else if (datatypeObj.getMin().compareTo(datatypeObj.getMaximum()) > 0) {
            throw new OBException(OBMessageUtils.messageBD("ehcm_minvaluegreater"));

          }
        }
      }
    }
    final EHCMDeflookupsTypeLn overlapObj = (EHCMDeflookupsTypeLn) event.getTargetInstance();
    OBQuery<EHCMDeflookupsTypeLn> referenceInfo = null;
    List<EHCMDeflookupsTypeLn> overlapList = new ArrayList<EHCMDeflookupsTypeLn>();
    if (!overlapObj.getEhcmDeflookupsType().isOverlappingallowed()) {
      String whereClause = "as e where e.organization.id = :organisation  "
          + "and ((e.min >= :minimum " + "and  e.maximum <= :maximum) "
          + "or (e.maximum >= :minimum" + " and e.min <= :maximum)) and e.id<>:id  ";
      referenceInfo = OBDal.getInstance().createQuery(EHCMDeflookupsTypeLn.class, whereClause);
      referenceInfo.setNamedParameter("organisation", overlapObj.getOrganization().getId());
      referenceInfo.setNamedParameter("minimum", overlapObj.getMin());
      referenceInfo.setNamedParameter("maximum", overlapObj.getMaximum());
      referenceInfo.setNamedParameter("id", overlapObj.getId());
      overlapList = referenceInfo.list();

      if (overlapList.size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("ehcm_overlap"));

      }
    }
  }
}
