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

import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;

public class AbsenceTypeEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMAbsenceType.ENTITY_NAME) };
  private Logger log = Logger.getLogger(this.getClass());

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
      EHCMAbsenceType abstype = (EHCMAbsenceType) event.getTargetInstance();
      if (!abstype.isAccrual()) {
        OBQuery<EHCMAbsenceTypeAccruals> accrual = OBDal.getInstance().createQuery(
            EHCMAbsenceTypeAccruals.class, " absenceType.id='" + abstype.getId() + "'");
        if (accrual.list().size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("@EHCM_AbsType_Accrual@"));
        }
      }
    } catch (OBException e) {
      log.error("Exception while creating absence type" + e);
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
      EHCMAbsenceType abstype = (EHCMAbsenceType) event.getTargetInstance();
      log.debug("abstype.isAccrual():" + abstype.isAccrual());
      final Property accrual = entities[0].getProperty(EHCMAbsenceType.PROPERTY_ISACCRUAL);
      if (!event.getCurrentState(accrual).equals(event.getPreviousState(accrual))) {
        if (!abstype.isAccrual()) {
          OBQuery<EHCMAbsenceTypeAccruals> accruals = OBDal.getInstance().createQuery(
              EHCMAbsenceTypeAccruals.class, " absenceType.id='" + abstype.getId() + "'");
          log.debug("abstype.size():" + accruals.list().size());
          if (accruals.list().size() > 0) {
            throw new OBException(OBMessageUtils.messageBD("@EHCM_AbsType_Accrual@"));
          }
        }
      }
    } catch (OBException e) {
      log.error("Exception while updating absence type" + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
