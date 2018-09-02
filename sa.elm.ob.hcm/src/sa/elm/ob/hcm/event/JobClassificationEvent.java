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
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.JobClassification;
import sa.elm.ob.hcm.Jobs;

/**
 * 
 * @author gopalakrishnan on 12/10/2016
 * 
 */

public class JobClassificationEvent extends EntityPersistenceEventObserver {
  /**
   * Business Event on Table Ehcm_Job_Classification
   */
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(JobClassification.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  private Logger log = Logger.getLogger(this.getClass());

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      JobClassification objJobclass = (JobClassification) event.getTargetInstance();
      final Property enabled = entities[0].getProperty(JobClassification.PROPERTY_ACTIVE);
      final Property endDate = entities[0].getProperty(JobClassification.PROPERTY_ENDDATE);
      if (event.getCurrentState(endDate) != null) {
        event.setCurrentState(enabled, false);
      }
      Date startDate = objJobclass.getStartDate();
      Date enddate = objJobclass.getEndDate();
      // CHECK Start date should be less than its jobs start date
      OBQuery<Jobs> jobsQuery = OBDal.getInstance().createQuery(Jobs.class,
          "as e where e.ehcmJobClassification.id='" + objJobclass.getId() + "' and e.startDate < '"
              + objJobclass.getStartDate() + "'");
      if (jobsQuery.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_JOB_CLASS_DATE"));
      }
      if (startDate != null && enddate != null) {
        if (enddate.compareTo(startDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Enddate"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating JobsClassfication  " + e);
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
      JobClassification objJobclass = (JobClassification) event.getTargetInstance();
      final Property enabled = entities[0].getProperty(JobClassification.PROPERTY_ACTIVE);
      final Property endDate = entities[0].getProperty(JobClassification.PROPERTY_ENDDATE);
      final Property classcode = entities[0]
          .getProperty(JobClassification.PROPERTY_CLASSIFICATIONCODE);
      final Property mcsLetterNo = entities[0].getProperty(JobClassification.PROPERTY_MCSLETTERNO);

      if (!event.getCurrentState(classcode).equals(event.getPreviousState(classcode))) {
        // Update Lines job code while changing classification code
        OBQuery<Jobs> jobsQuery = OBDal.getInstance().createQuery(Jobs.class,
            "as e where e.ehcmJobClassification.id='" + objJobclass.getId() + "'");
        if (jobsQuery.list().size() > 0) {
          for (Jobs objLines : jobsQuery.list()) {
            objLines.setJobCode(event.getCurrentState(classcode).toString()
                .concat(objLines.getGrade().getSearchKey()));
            OBDal.getInstance().save(objLines);
          }
        }
      }

      if (event.getCurrentState(endDate) != null) {
        event.setCurrentState(enabled, false);
      }
      Date startDate = objJobclass.getStartDate();
      Date enddate = objJobclass.getEndDate();

      // CHECK Start date should be less than its jobs start date
      OBQuery<Jobs> jobsQuery = OBDal.getInstance().createQuery(Jobs.class,
          "as e where e.ehcmJobClassification.id='" + objJobclass.getId() + "' and e.startDate < '"
              + objJobclass.getStartDate() + "'");
      if (jobsQuery.list().size() > 0) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_JOB_CLASS_DATE"));
      }
      if (startDate != null && enddate != null) {
        if (enddate.compareTo(startDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Enddate"));
        }
      }

      JobClassification jobclass = OBDal.getInstance().get(JobClassification.class,
          objJobclass.getId());
      if (jobclass.getEhcmJobsList().size() > 0) {
        if (event.getCurrentState(enabled).toString().equals("false")) {
          // while Disable the jobs header disable jobs lines
          for (Jobs objJob : jobclass.getEhcmJobsList()) {
            if (objJob.getEndDate() == null) {
              objJob.setEndDate(jobclass.getEndDate());
            }
            objJob.setActive(false);
            OBDal.getInstance().save(objJob);
          }
          /*
           * if(jobclass.getEhcmUpjobClassificationList().size() > 0) { //while Disable the jobs
           * header disable update the jobs header for (UpdateJobClassification objUpdateJobs :
           * jobclass.getEhcmUpjobClassificationList()) { if(objUpdateJobs.getEndDate()==null){
           * objUpdateJobs.setEndDate(jobclass.getEndDate()); } objUpdateJobs.setActive(false);
           * OBDal.getInstance().save(objUpdateJobs); } }
           */
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating JobClassification  " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      JobClassification objClassification = (JobClassification) event.getTargetInstance();
      if (objClassification.getStatus().equals("CO")) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Already_Processed"));
      }
    } catch (OBException e) {
      log.error(" Exception while creating JobsClassification   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
