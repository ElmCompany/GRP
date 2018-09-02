package sa.elm.ob.hcm.event;

import java.sql.SQLException;
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
import sa.elm.ob.hcm.UpdateJobClassification;
import sa.elm.ob.hcm.UpdateJobs;

public class EhcmUpdateJobsTitleEvent extends EntityPersistenceEventObserver {
  /**
   * Business Event on Table Ehcm_Upjob_Classification.
   */
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(UpdateJobClassification.ENTITY_NAME) };

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
      UpdateJobClassification updjob = (UpdateJobClassification) event.getTargetInstance();
      Date startDate = updjob.getStartDate();
      Date enddate = updjob.getEndDate();
      Date jobClassStartDate = updjob.getEhcmJobClassification().getStartDate();
      Date updateJobsClassDate = updjob.getClassificationDate();
      final Property classcode = entities[0]
          .getProperty(JobClassification.PROPERTY_CLASSIFICATIONCODE);
      final Property propStartDate = entities[0].getProperty(JobClassification.PROPERTY_STARTDATE);

      // Update Lines job code while changing classification code
      if (!event.getCurrentState(classcode).equals(event.getPreviousState(classcode))) {
        if (!classcode.equals(updjob.getEhcmJobClassification().getClassificationCode())) {
          OBQuery<UpdateJobs> jobsQuery = OBDal.getInstance().createQuery(UpdateJobs.class,
              "as e where e.ehcmUpjobClassification.id='" + updjob.getId() + "' ");
          if (jobsQuery.list().size() > 0) {
            for (UpdateJobs objLines : jobsQuery.list()) {
              objLines.setStartDate(updjob.getStartDate());
              objLines.setManual(true);
              objLines.setJobCode(event.getCurrentState(classcode).toString()
                  .concat(objLines.getGrade().getSearchKey()));
              OBDal.getInstance().save(objLines);
            }
          }
        }
      }
      // update start date in line when header start date changed
      if (!event.getCurrentState(propStartDate).equals(event.getPreviousState(propStartDate))) {
        OBQuery<UpdateJobs> jobsQuery = OBDal.getInstance().createQuery(UpdateJobs.class,
            "as e where e.ehcmUpjobClassification.id='" + updjob.getId() + "' and e.ismanual='Y' ");
        if (jobsQuery.list().size() > 0) {
          for (UpdateJobs objLines : jobsQuery.list()) {
            objLines.setStartDate(updjob.getStartDate());
            OBDal.getInstance().save(objLines);
          }
        }
      }
      // new classification date need to be grater than old classification date.
      if (!event.getCurrentState(classcode).toString()
          .equals(updjob.getEhcmJobClassification().getClassificationCode())) {
        if (updateJobsClassDate
            .compareTo(updjob.getEhcmJobClassification().getClassificationDate()) <= 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_JobClass_Great_ClassDate"));
        }
      }
      if (startDate != null && jobClassStartDate != null) {
        if (startDate.compareTo(jobClassStartDate) < 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_JobClassification_StartDate"));
        }
      }
      if (enddate != null && jobClassStartDate != null) {
        if (enddate.compareTo(jobClassStartDate) <= 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_JobClassification_EndDate"));
        }
      }
      if (startDate != null && enddate != null) {
        if (enddate.compareTo(startDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Enddate"));
        }
      }
      /*
       * // check mcs letter number exists in update job classification OBQuery<JobClassification>
       * updatejobsQuery = OBDal.getInstance().createQuery( JobClassification.class,
       * "as e where e.mcsletterNo='" + updjob.getMCSLetterNo() + "' and e.client.id='" +
       * updjob.getClient().getId() + "' "); if (updatejobsQuery.list().size() > 0) { throw new
       * OBException(OBMessageUtils.messageBD("EHCM_MCSNO_ExistsinJob")); }
       */

    } catch (OBException e) {
      log.error(" Exception while creating UpdateJobTitleEvent: " + e);
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
      UpdateJobClassification updjob = (UpdateJobClassification) event.getTargetInstance();
      Date startDate = updjob.getStartDate();
      Date enddate = updjob.getEndDate();
      Date jobClassStartDate = updjob.getEhcmJobClassification().getStartDate();
      Date updateJobsClassDate = updjob.getClassificationDate();
      // new classification date need to be grater than old classification date.
      if (!updjob.getClassificationCode()
          .equals(updjob.getEhcmJobClassification().getClassificationCode())) {
        if (updateJobsClassDate
            .compareTo(updjob.getEhcmJobClassification().getClassificationDate()) <= 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_JobClass_Great_ClassDate"));
        }
      }
      if (startDate != null && jobClassStartDate != null) {
        if (startDate.compareTo(jobClassStartDate) < 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_JobClassification_StartDate"));
        }
      }
      if (enddate != null && jobClassStartDate != null) {
        if (enddate.compareTo(jobClassStartDate) <= 0) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_JobClassification_EndDate"));
        }
      }
      if (startDate != null && enddate != null) {
        if (enddate.compareTo(startDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_Enddate"));
        }
      }
      /*
       * // check mcs letter number exists in update job classification OBQuery<JobClassification>
       * updatejobsQuery = OBDal.getInstance().createQuery( JobClassification.class,
       * "as e where e.mcsletterNo='" + updjob.getMCSLetterNo() + "' and e.client.id='" +
       * updjob.getClient().getId() + "' "); if (updatejobsQuery.list().size() > 0) { throw new
       * OBException(OBMessageUtils.messageBD("EHCM_MCSNO_ExistsinJob")); }
       */
    } catch (OBException e) {
      log.error(" Exception while creating UpdateJobTitleEvent: " + e);
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
      UpdateJobClassification updjob = (UpdateJobClassification) event.getTargetInstance();
      if (updjob.getAlertStatus().equals("CO")) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Already_Processed"));
      }
      if (updjob.getAlertStatus().equals("DR")) {
        OBQuery<UpdateJobClassification> oldUpdateJobsQry = OBDal.getInstance()
            .createQuery(UpdateJobClassification.class,
                "as e where e.id not in ('" + updjob.getId() + "') and e.ehcmJobClassification.id='"
                    + updjob.getEhcmJobClassification().getId()
                    + "' order by e.creationDate desc ");
        oldUpdateJobsQry.setMaxResult(2);
        if (oldUpdateJobsQry.list().size() > 1) {
          // Enable Correction in recent records
          UpdateJobClassification oldUpdateJobs = oldUpdateJobsQry.list().get(0);
          oldUpdateJobs.setCorrection(true);
          // update mcs no in job classification
          JobClassification objJobClass = OBDal.getInstance().get(JobClassification.class,
              updjob.getEhcmJobClassification().getId());
          objJobClass.setMcsletterNo(oldUpdateJobs.getMCSLetterNo());
          OBDal.getInstance().save(objJobClass);
          OBDal.getInstance().save(oldUpdateJobs);
        }
        if (oldUpdateJobsQry.list().size() == 1) {
          UpdateJobClassification oldUpdateJobs = oldUpdateJobsQry.list().get(0);
          // update mcs no in job classification
          JobClassification objJobClass = OBDal.getInstance().get(JobClassification.class,
              updjob.getEhcmJobClassification().getId());
          objJobClass.setMcsletterNo(oldUpdateJobs.getMCSLetterNo());
          OBDal.getInstance().save(objJobClass);
        }
        try {
          OBDal.getInstance().getConnection()
              .prepareStatement("delete from ehcm_update_jobs where ehcm_upjob_classification_id='"
                  + updjob.getId() + "'")
              .executeUpdate();
        } catch (SQLException e) {
          // TODO Auto-generated catch block
          log.error("error while deleting update job title ", e);
        }
      }
    } catch (OBException e) {
      log.error(" Exception while Deleting UpdateJobs   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}