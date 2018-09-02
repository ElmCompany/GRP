package sa.elm.ob.hcm.event;

import java.math.BigDecimal;
import java.util.List;

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

import sa.elm.ob.hcm.EHCMAbsencePayment;

/**
 * 
 * This Business Event class for absence Type payment Tab
 */
public class AbsenceTypePaymentEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMAbsencePayment.ENTITY_NAME) };
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
      EHCMAbsencePayment absencePayment = (EHCMAbsencePayment) event.getTargetInstance();
      if (absencePayment.getAbsenceType().getAccrualResetDate().equals("LO")) {
        if (absencePayment.getPayrollElement().getEhcmElementCatgry().getCode().equals("02")
            && (absencePayment.getMin() == null
                || absencePayment.getMin().compareTo(BigDecimal.ZERO) == 0
                || absencePayment.getMax() == null
                || absencePayment.getMax().compareTo(BigDecimal.ZERO) == 0)) {
          throw new OBException(OBMessageUtils.messageBD("@EHCM_AbsPaymentMinMaxGrtZero@"));
        }

        if (absencePayment.getMin().compareTo(absencePayment.getMax()) > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsPaymentMinLessThanMax"));
        }

        List<EHCMAbsencePayment> overlapList = getOverlapAbsencePaymentList(absencePayment);

        if (overlapList.size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsPaymentMinMaxOverlap"));
        }

      }
    } catch (OBException e) {
      log.error("Exception while creating absence type Action" + e);
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
      EHCMAbsencePayment absencePayment = (EHCMAbsencePayment) event.getTargetInstance();
      if (absencePayment.getAbsenceType().getAccrualResetDate().equals("LO")) {
        if (absencePayment.getPayrollElement().getEhcmElementCatgry().getCode().equals("02")
            && (absencePayment.getMin() == null
                || absencePayment.getMin().compareTo(BigDecimal.ZERO) == 0
                || absencePayment.getMax() == null
                || absencePayment.getMax().compareTo(BigDecimal.ZERO) == 0)) {
          throw new OBException(OBMessageUtils.messageBD("@EHCM_AbsPaymentMinMaxGrtZero@"));
        }

        if (absencePayment.getMin().compareTo(absencePayment.getMax()) > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsPaymentMinLessThanMax"));
        }

        List<EHCMAbsencePayment> overlapList = getOverlapAbsencePaymentList(absencePayment);

        if (overlapList.size() > 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_AbsPaymentMinMaxOverlap"));
        }
      }
    } catch (OBException e) {
      log.error("Exception while creating absence type Action" + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public List<EHCMAbsencePayment> getOverlapAbsencePaymentList(EHCMAbsencePayment absencePayment) {
    List<EHCMAbsencePayment> overlapList = null;
    try {
      OBQuery<EHCMAbsencePayment> absencePaymentQry = null;

      String whereClause = "as e where e.absenceType.id = :absenceTypeId  "
          + (absencePayment.getSubType() == null ? "" : " and e.subType.id<>:subTypeId  ")
          + "and ((e.min >= :minimum " + "and  e.max <= :maximum) " + "or (e.max >= :minimum"
          + " and e.min <= :maximum)) and e.id<>:id   and e.payrollElement.id=:payrollElementId ";
      absencePaymentQry = OBDal.getInstance().createQuery(EHCMAbsencePayment.class, whereClause);
      absencePaymentQry.setNamedParameter("absenceTypeId", absencePayment.getAbsenceType().getId());
      if (absencePayment.getSubType() != null)
        absencePaymentQry.setNamedParameter("subTypeId", absencePayment.getSubType().getId());
      absencePaymentQry.setNamedParameter("minimum", absencePayment.getMin());
      absencePaymentQry.setNamedParameter("maximum", absencePayment.getMax());
      absencePaymentQry.setNamedParameter("id", absencePayment.getId());
      absencePaymentQry.setNamedParameter("payrollElementId",
          absencePayment.getPayrollElement().getId());
      overlapList = absencePaymentQry.list();
    } catch (final Exception e) {
      log.error("Exception in getOverlapAbsencePaymentList() Method : ", e);
      return overlapList;
    }
    return overlapList;
  }
}
