package sa.elm.ob.hcm.event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMDeflookupsType;
import sa.elm.ob.hcm.EHCMDeflookupsTypeLn;

public class HcmReferenceLookupOverlappingEvent extends EntityPersistenceEventObserver {
  private static final Logger log = Logger.getLogger(HcmReferenceLookupOverlappingEvent.class);
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMDeflookupsType.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final EHCMDeflookupsType ehcmDefLookType = (EHCMDeflookupsType) event.getTargetInstance();
    List<EHCMDeflookupsTypeLn> ehcmDefLooTypeLineList = new ArrayList<EHCMDeflookupsTypeLn>();
    List<EHCMDeflookupsType> ehcmDefLookTypeList = new ArrayList<EHCMDeflookupsType>();
    OBQuery<EHCMDeflookupsType> competencyQuery = null;
    if ((ehcmDefLookType.getReference().equals("CR"))
        || (ehcmDefLookType.getReference().equals("EOT"))) {
      if (ehcmDefLookType.getDatatype() == null) {
        throw new OBException(OBMessageUtils.messageBD("ehcm_datatype_mandatory"));
      }
    }
    if (ehcmDefLookType.getReference().equals("CR")) {
      competencyQuery = OBDal.getInstance().createQuery(EHCMDeflookupsType.class,
          " client.id = '" + ehcmDefLookType.getClient().getId() + "'  and reference = 'CR'");
      ehcmDefLookTypeList = competencyQuery.list();
      if (ehcmDefLookTypeList.size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("ehcm_competency_rating_repeat"));
      }

      if (ehcmDefLookType.getDatatype().equals("P")) {

        if (!ehcmDefLookType.isOverlappingallowed()) {

          ehcmDefLooTypeLineList = ehcmDefLookType.getEHCMDeflookupsTypeLnList();
          if (ehcmDefLooTypeLineList.size() > 0) {

            for (int i = 0; i < ehcmDefLooTypeLineList.size(); i++) {
              EHCMDeflookupsTypeLn obj = ehcmDefLooTypeLineList.get(i);
              BigDecimal min = obj.getMin();
              BigDecimal max = obj.getMaximum();

              for (int j = i + 1; j <= ehcmDefLooTypeLineList.size() - 1; j++) {
                EHCMDeflookupsTypeLn obj1 = ehcmDefLooTypeLineList.get(j);
                BigDecimal min1 = obj1.getMin();
                BigDecimal max1 = obj1.getMaximum();

                if (((min.compareTo(min1) >= 0) && (max.compareTo(max1) <= 0))
                    || ((max.compareTo(min1) >= 0) && (min.compareTo(max1) <= 0))) {
                  throw new OBException(OBMessageUtils.messageBD("ehcm_isoverlap"));

                }

              }
            }
          }

        }

      }
    }

  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final EHCMDeflookupsType ehcmDefLookType = (EHCMDeflookupsType) event.getTargetInstance();
    final Property enabled = entities[0].getProperty(EHCMDeflookupsType.PROPERTY_ENABLED);
    List<EHCMDeflookupsTypeLn> ehcmDefLooTypeLineList = new ArrayList<EHCMDeflookupsTypeLn>();
    List<EHCMDeflookupsType> ehcmDefLookTypeList = new ArrayList<EHCMDeflookupsType>();
    OBQuery<EHCMDeflookupsType> competencyQuery = null;
    if ((ehcmDefLookType.getReference().equals("CR"))
        || (ehcmDefLookType.getReference().equals("EOT"))) {
      if (ehcmDefLookType.getDatatype() == null) {
        throw new OBException(OBMessageUtils.messageBD("ehcm_datatype_mandatory"));
      }
    }
    if (ehcmDefLookType.getReference().equals("CR")) {

      competencyQuery = OBDal.getInstance().createQuery(EHCMDeflookupsType.class,
          " client.id = '" + ehcmDefLookType.getClient().getId()
              + "'  and reference = 'CR' and id <> '" + ehcmDefLookType.getId() + "' ");
      ehcmDefLookTypeList = competencyQuery.list();
      if (ehcmDefLookTypeList.size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("ehcm_competency_rating_repeat"));
      }

      if (ehcmDefLookType.getDatatype().equals("P")) {

        if (!ehcmDefLookType.isOverlappingallowed()) {

          ehcmDefLooTypeLineList = ehcmDefLookType.getEHCMDeflookupsTypeLnList();
          if (ehcmDefLooTypeLineList.size() > 0) {

            for (int i = 0; i < ehcmDefLooTypeLineList.size(); i++) {
              EHCMDeflookupsTypeLn obj = ehcmDefLooTypeLineList.get(i);
              BigDecimal min = obj.getMin();
              BigDecimal max = obj.getMaximum();

              for (int j = i + 1; j <= ehcmDefLooTypeLineList.size() - 1; j++) {
                EHCMDeflookupsTypeLn obj1 = ehcmDefLooTypeLineList.get(j);
                BigDecimal min1 = obj1.getMin();
                BigDecimal max1 = obj1.getMaximum();

                if (((min.compareTo(min1) >= 0) && (max.compareTo(max1) <= 0))
                    || ((max.compareTo(min1) >= 0) && (min.compareTo(max1) <= 0))) {
                  throw new OBException(OBMessageUtils.messageBD("ehcm_isoverlap"));

                }

              }
            }
          }

        }
      }
    }
  }

}
