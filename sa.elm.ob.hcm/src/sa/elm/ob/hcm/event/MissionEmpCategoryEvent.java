package sa.elm.ob.hcm.event;

import java.util.Date;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMMisCatPeriod;
import sa.elm.ob.hcm.EHCMMisEmpCategory;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAO;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAOImpl;

/**
 * @author divya on 03/03/2018
 */
public class MissionEmpCategoryEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMMisEmpCategory.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
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

      EHCMMisEmpCategory misEmpCategory = (EHCMMisEmpCategory) event.getTargetInstance();

      final Property gradeclass = entities[0]
          .getProperty(EHCMMisEmpCategory.PROPERTY_GRADECLASSIFICATIONS);

      if (!event.getCurrentState(gradeclass).equals(event.getPreviousState(gradeclass))) {
        if (missionCategoryDAOImpl.checkCategoryAlreadyAdded(
            misEmpCategory.getGradeClassifications().getId(), misEmpCategory)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_EmpCategory_Unique"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while updating Employee Scholarship   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @SuppressWarnings("unused")
  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EmploymentInfo employinfo = null;
      Date startDate = null;
      String decisionType = null;
      EHCMMisEmpCategory misEmpCategory = (EHCMMisEmpCategory) event.getTargetInstance();

      if (missionCategoryDAOImpl.checkCategoryAlreadyAdded(
          misEmpCategory.getGradeClassifications().getId(), misEmpCategory)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_EmpCategory_Unique"));
      }

      missionCategoryDAOImpl.insertMisCatEmployee(misEmpCategory, OBContext.getOBContext().getUser());

    } catch (OBException e) {
      log.error(" Exception while creating Employee Scholarship   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

    finally {
      OBContext.restorePreviousMode();
    }
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      EHCMMisEmpCategory misEmpCategory = (EHCMMisEmpCategory) event.getTargetInstance();
      EHCMMisCatPeriod misCategoryPrd = missionCategoryDAOImpl
          .getRecentEmpCategoryPeriod(misEmpCategory.getEhcmMissionCategory());
      if (misCategoryPrd!=null && missionCategoryDAOImpl.chkAnyEmployeeUsedDaysGrtZero(misCategoryPrd,misEmpCategory)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_MisCat_Period_CantDel"));
      }
      else {
    	  if(misCategoryPrd!=null) {
    		  missionCategoryDAOImpl.deleteMisCatEmployee(misCategoryPrd,misEmpCategory);
    	  }
      }

    } catch (OBException e) {
      log.error(" Exception while scholarship ondelete event : " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
