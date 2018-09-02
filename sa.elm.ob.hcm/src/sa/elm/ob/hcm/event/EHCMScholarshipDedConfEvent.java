package sa.elm.ob.hcm.event;

import java.math.BigDecimal;
import java.util.List;

import javax.enterprise.event.Observes;

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

import sa.elm.ob.hcm.EHCMScholarshipDedConf;

public class EHCMScholarshipDedConfEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMScholarshipDedConf.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    // TODO Auto-generated method stub
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMScholarshipDedConf ScholarshipDedConf = (EHCMScholarshipDedConf) event
          .getTargetInstance();
      Long MinSchloarDays = (long) 0;
      BigDecimal deductionpercentage = new BigDecimal("0");
      // Deduction Percentage and Minimum Scholarship Days Should not be zero
      if (ScholarshipDedConf.getMinimumScholarshipDays().compareTo(MinSchloarDays) == 0
          || ScholarshipDedConf.getDeductionPercentage().compareTo(deductionpercentage) == 0) {
        if (ScholarshipDedConf.getMinimumScholarshipDays().compareTo(MinSchloarDays) == 0
            && ScholarshipDedConf.getDeductionPercentage().compareTo(deductionpercentage) == 0) {
          throw new OBException(
              OBMessageUtils.messageBD("EHCM_Scholarship_Deductpercentage_Schldays_Not_Zero"));
        } else if (ScholarshipDedConf.getDeductionPercentage()
            .compareTo(deductionpercentage) == 0) {
          throw new OBException(
              OBMessageUtils.messageBD("EHCM_Scholarship_Deductpercentage_Not_Zero"));
        } else if (ScholarshipDedConf.getMinimumScholarshipDays().compareTo(MinSchloarDays) == 0) {
          throw new OBException(
              OBMessageUtils.messageBD("EHCM_Scholarship_Min_Schlr_Days_Not_Zero"));
        }
      }
      // Deduction Percentage and Minimum Scholarship Days Should not allow negative number
      if (ScholarshipDedConf.getMinimumScholarshipDays().compareTo(MinSchloarDays) == -1
          || ScholarshipDedConf.getDeductionPercentage().compareTo(deductionpercentage) == -1) {
        if (ScholarshipDedConf.getMinimumScholarshipDays().compareTo(MinSchloarDays) == -1
            && ScholarshipDedConf.getDeductionPercentage().compareTo(deductionpercentage) == -1) {
          throw new OBException(
              OBMessageUtils.messageBD("EHCM_Scholarship_Deductpercentage_Schldays_Not_Zero"));
        } else if (ScholarshipDedConf.getDeductionPercentage()
            .compareTo(deductionpercentage) == -1) {
          throw new OBException(
              OBMessageUtils.messageBD("EHCM_Scholarship_Deductpercentage_Not_Zero"));
        } else if (ScholarshipDedConf.getMinimumScholarshipDays().compareTo(MinSchloarDays) == -1) {
          throw new OBException(
              OBMessageUtils.messageBD("EHCM_Scholarship_Min_Schlr_Days_Not_Zero"));
        }
      }
      // Deduction Percentage with 100
      BigDecimal percentage = new BigDecimal("100");
      if (ScholarshipDedConf.getDeductionPercentage().compareTo(percentage) == 1) {
        throw new OBException(
            OBMessageUtils.messageBD("EHCM_Scholarship_Deductpercentage_Gt_hundred"));
      }
      // Should allow to save more than one record
      List<EHCMScholarshipDedConf> checkerecordList = null;
      OBQuery<EHCMScholarshipDedConf> checkerecord = OBDal.getInstance().createQuery(
          EHCMScholarshipDedConf.class, "as e where e.client.id=:clientId and e.id <>:currentId");
      checkerecord.setNamedParameter("clientId", ScholarshipDedConf.getClient().getId());
      checkerecord.setNamedParameter("currentId", ScholarshipDedConf.getId());
      checkerecordList = checkerecord.list();
      if (checkerecordList.size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Scholarship_Ded_Conf_One_Record"));

      }
    } catch (OBException e) {
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
      EHCMScholarshipDedConf ScholarshipDedConf = (EHCMScholarshipDedConf) event
          .getTargetInstance();
      Long MinSchloarDays = (long) 0;
      BigDecimal deductionpercentage = new BigDecimal("0");
      // Deduction Percentage and Minimum Scholarship Days Should not be zero
      if (ScholarshipDedConf.getMinimumScholarshipDays().compareTo(MinSchloarDays) == 0
          || ScholarshipDedConf.getDeductionPercentage().compareTo(deductionpercentage) == 0) {
        if (ScholarshipDedConf.getMinimumScholarshipDays().compareTo(MinSchloarDays) == 0
            && ScholarshipDedConf.getDeductionPercentage().compareTo(deductionpercentage) == 0) {
          throw new OBException(
              OBMessageUtils.messageBD("EHCM_Scholarship_Deductpercentage_Schldays_Not_Zero"));
        } else if (ScholarshipDedConf.getDeductionPercentage()
            .compareTo(deductionpercentage) == 0) {
          throw new OBException(
              OBMessageUtils.messageBD("EHCM_Scholarship_Deductpercentage_Not_Zero"));
        } else if (ScholarshipDedConf.getMinimumScholarshipDays().compareTo(MinSchloarDays) == 0) {
          throw new OBException(
              OBMessageUtils.messageBD("EHCM_Scholarship_Min_Schlr_Days_Not_Zero"));
        }
      }
      // Deduction Percentage and Minimum Scholarship Days Should not allow negative number
      if (ScholarshipDedConf.getMinimumScholarshipDays().compareTo(MinSchloarDays) == -1
          || ScholarshipDedConf.getDeductionPercentage().compareTo(deductionpercentage) == -1) {
        if (ScholarshipDedConf.getMinimumScholarshipDays().compareTo(MinSchloarDays) == -1
            && ScholarshipDedConf.getDeductionPercentage().compareTo(deductionpercentage) == -1) {
          throw new OBException(
              OBMessageUtils.messageBD("EHCM_Scholarship_Deductpercentage_Schldays_Not_Zero"));
        } else if (ScholarshipDedConf.getDeductionPercentage()
            .compareTo(deductionpercentage) == -1) {
          throw new OBException(
              OBMessageUtils.messageBD("EHCM_Scholarship_Deductpercentage_Not_Zero"));
        } else if (ScholarshipDedConf.getMinimumScholarshipDays().compareTo(MinSchloarDays) == -1) {
          throw new OBException(
              OBMessageUtils.messageBD("EHCM_Scholarship_Min_Schlr_Days_Not_Zero"));
        }
      }
      // Deduction Percentage with 100
      BigDecimal percentage = new BigDecimal("100");
      if (ScholarshipDedConf.getDeductionPercentage().compareTo(percentage) == 1) {
        throw new OBException(
            OBMessageUtils.messageBD("EHCM_Scholarship_Deductpercentage_Gt_hundred"));
      }
      // Should allow to save more than one record
      List<EHCMScholarshipDedConf> checkerecordList = null;
      OBQuery<EHCMScholarshipDedConf> checkerecord = OBDal.getInstance()
          .createQuery(EHCMScholarshipDedConf.class, "as e where e.client.id=:clientId");
      checkerecord.setNamedParameter("clientId", ScholarshipDedConf.getClient().getId());
      checkerecordList = checkerecord.list();
      if (checkerecordList.size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Scholarship_Ded_Conf_One_Record"));
      }
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
