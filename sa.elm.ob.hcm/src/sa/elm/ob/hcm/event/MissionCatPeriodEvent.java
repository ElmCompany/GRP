package sa.elm.ob.hcm.event;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
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
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAO;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAOImpl;
import sa.elm.ob.hcm.util.Utility;

/**
 * @author divya on 03/03/2018
 */
public class MissionCatPeriodEvent extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(EHCMMisCatPeriod.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());
  MissionCategoryDAOImpl missionCategoryDAOImpl = new MissionCategoryDAOImpl();
  DateFormat YearFormat = sa.elm.ob.utility.util.Utility.YearFormat;
	 DateFormat dateFormat = sa.elm.ob.utility.util.Utility.dateFormat;
  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    try {
      OBContext.setAdminMode();

      EHCMMisCatPeriod misCategoryPrd = (EHCMMisCatPeriod) event.getTargetInstance();
      final Property startdate = entities[0].getProperty(EHCMMisCatPeriod.PROPERTY_STARTDATE);
      final Property enddate = entities[0].getProperty(EHCMMisCatPeriod.PROPERTY_ENDDATE);
      final Property usedDays = entities[0].getProperty(EHCMMisCatPeriod.PROPERTY_DAYS);
      if (!event.getCurrentState(startdate).equals(event.getPreviousState(startdate))
          || (event.getCurrentState(enddate) != null
              && !event.getCurrentState(enddate).equals(event.getPreviousState(enddate)))) {
        if (missionCategoryDAOImpl.checkMisPerdAlrdyExistorNot(misCategoryPrd)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_MisCat_EmpPerd"));

        }
        // checking enddate should not be lesser than startdate
        if (misCategoryPrd.getEndDate() != null
            && misCategoryPrd.getEndDate().compareTo(misCategoryPrd.getStartDate()) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_ScholarShipEndStartDateComp"));
        }
        if(event.getCurrentState(enddate) != null
              && !event.getCurrentState(enddate).equals(event.getPreviousState(enddate)) && (((Date) event.getCurrentState(enddate)).compareTo((Date) event.getPreviousState(enddate)) <=0) ) {
       JSONObject result= missionCategoryDAOImpl.chkAnyEmpFallOnChangePeriod(sa.elm.ob.utility.util.Utility.formatDate((Date) event.getCurrentState(enddate)),sa.elm.ob.utility.util.Utility.formatDate((Date) event.getPreviousState(enddate)),misCategoryPrd.getEhcmMissionCategory() );
        
       if(result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
				 throw new OBException(OBMessageUtils.messageBD(result.getString("errormsg")));
		}
        }
        else if(!event.getCurrentState(startdate).equals(event.getPreviousState(startdate))  && (((Date) event.getCurrentState(startdate)).compareTo((Date) event.getPreviousState(startdate)) >=0)){
        	  JSONObject result= missionCategoryDAOImpl.chkAnyEmpFallOnChangePeriod(sa.elm.ob.utility.util.Utility.formatDate((Date) event.getPreviousState(startdate)),sa.elm.ob.utility.util.Utility.formatDate((Date) event.getCurrentState(startdate)),misCategoryPrd.getEhcmMissionCategory() );
              
              if(result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
       				 throw new OBException(OBMessageUtils.messageBD(result.getString("errormsg")));
       		}
        }
        
      }
      if (event.getCurrentState(usedDays)!=null && !event.getCurrentState(usedDays).equals(event.getPreviousState(usedDays))){
          BigDecimal maxUsedDays= 	missionCategoryDAOImpl.getMaxUsedDaysForMisPerd (misCategoryPrd) ;
          if(new BigDecimal(event.getCurrentState(usedDays).toString()).compareTo(maxUsedDays) < 0){
       	   throw new OBException(OBMessageUtils.messageBD("EHCM_MisCatPeriodDays"));
          }
           }
      
      if (!event.getCurrentState(startdate).equals(event.getPreviousState(startdate))
          || (event.getCurrentState(enddate) != null
              && !event.getCurrentState(enddate).equals(event.getPreviousState(enddate))) ||
          event.getCurrentState(usedDays)!=null && !event.getCurrentState(usedDays).equals(event.getPreviousState(usedDays))){
      if(misCategoryPrd.getStartDate()!=null && misCategoryPrd.getEndDate()!=null && new BigDecimal(misCategoryPrd.getDays()).compareTo(BigDecimal.ZERO) >0) {
  	    int CalculateDays=Utility.caltheDaysUsingGreDate(misCategoryPrd.getStartDate(), misCategoryPrd.getEndDate());
  	    if(CalculateDays < Integer.parseInt(misCategoryPrd.getDays().toString())) {
  	    	 throw new OBException(OBMessageUtils.messageBD("EHCM_MisPrdDayGrtThnPrdDate"));
  	    }
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
      EHCMMisCatPeriod misCategoryPrd = (EHCMMisCatPeriod) event.getTargetInstance();

      if (missionCategoryDAOImpl.checkMisPerdAlrdyExistorNot(misCategoryPrd)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_MisCat_EmpPerd"));
      }
      // checking enddate should not be lesser than startdate
      if (misCategoryPrd.getEndDate() != null
          && misCategoryPrd.getEndDate().compareTo(misCategoryPrd.getStartDate()) == -1) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_ScholarShipEndStartDateComp"));
      }
      
      if(misCategoryPrd.getStartDate()!=null && misCategoryPrd.getEndDate()!=null && new BigDecimal(misCategoryPrd.getDays()).compareTo(BigDecimal.ZERO) >0) {
    	    int CalculateDays=Utility.caltheDaysUsingGreDate(misCategoryPrd.getStartDate(), misCategoryPrd.getEndDate());
    	    if(CalculateDays < Integer.parseInt(misCategoryPrd.getDays().toString())) {
    	    	 throw new OBException(OBMessageUtils.messageBD("EHCM_MisPrdDayGrtThnPrdDate"));
    	    }
      }
      missionCategoryDAOImpl.insertMisCatEmployeeUsingPrd(misCategoryPrd,
          OBContext.getOBContext().getUser(),false);

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
      EHCMMisCatPeriod misCategoryPrd = (EHCMMisCatPeriod) event.getTargetInstance();
      if (missionCategoryDAOImpl.chkAnyEmployeeUsedDaysGrtZero(misCategoryPrd,null)) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_MisCat_Period_CantDel"));
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
