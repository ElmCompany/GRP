package sa.elm.ob.hcm.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DalConnectionProvider;

import sa.elm.ob.hcm.JobClassification;
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.event.dao.JobEventDAOImpl;
import sa.elm.ob.utility.util.Utility;

/**
 * 
 * @author gopalakrishnan on 12/10/2016
 * 
 */

public class JobEvent extends EntityPersistenceEventObserver {
  /**
   * Business Event on Table Ehcm_jobs.
   */
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(Jobs.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  PreparedStatement ps = null, ps1 = null;
  ResultSet rs = null, rs1 = null;
  ConnectionProvider conn = new DalConnectionProvider(false);
  private Logger log = Logger.getLogger(this.getClass());
  JobEventDAOImpl daoimpl = new JobEventDAOImpl();

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    try {
      OBContext.setAdminMode();
      Jobs objJob = (Jobs) event.getTargetInstance();
      final Property enabled = entities[0].getProperty(Jobs.PROPERTY_ACTIVE);
      final Property endDate = entities[0].getProperty(Jobs.PROPERTY_ENDDATE);
      if (event.getCurrentState(endDate) != null) {
        event.setCurrentState(enabled, false);
      }
      Date jobClassStartDate = objJob.getEhcmJobClassification().getStartDate();
      Date startDate = objJob.getStartDate();
      Date enddate = objJob.getEndDate();
      String fDate = Utility.formatDate(startDate);
      String tDate = "";
      Boolean isAllow = true;
      Boolean isExist = false;
      // Check Active job code in the same period
      /*
       * if(enddate == null) { tDate = "21-06-2058"; } else { tDate = Utility.formatDate(enddate); }
       * try { ps1 = conn.
       * getPreparedStatement("select startdate from ehcm_jobs where ehcm_job_classification_id ='"
       * + objJob.getEhcmJobClassification().getId() + "' and value ='" + objJob.getJobCode() +
       * "' and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('" + fDate + "')"
       * +
       * " and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') "
       * + "<= to_date('" + tDate +
       * "','dd-MM-yyyy')) or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') "
       * + ">= to_date('" + fDate +
       * "') and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('" + tDate +
       * "','dd-MM-yyyy'))) "); rs1 = ps1.executeQuery(); if(rs1.next()) { isAllow = Boolean.FALSE;
       * } } catch (Exception e) { // TODO Auto-generated catch block e.printStackTrace(); }
       * if(!isAllow) { throw new OBException(OBMessageUtils.messageBD("EHCM_Job_Conflicts")); }
       */
      // Check more than one Active Jobs for same code
      /*
       * OBQuery<Jobs> jobClass = OBDal.getInstance().createQuery(Jobs.class,
       * "as e where  e.ehcmJobClassification.id = '" + objJob.getEhcmJobClassification().getId() +
       * "'" + " and e.jobCode = '" + objJob.getJobCode() + "' and e.active='Y'");
       * if(jobClass.list().size() > 0) { throw new
       * OBException(OBMessageUtils.messageBD("EHCM_MORE_ACTIVE_JOB").replace("@",
       * objJob.getJobCode())); }
       */
      // check Dates
      if (startDate != null && jobClassStartDate != null) {
        if (startDate.compareTo(jobClassStartDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_JobClassification_StartDate"));
        }
      }
      if (enddate != null && jobClassStartDate != null) {
        if (enddate.compareTo(jobClassStartDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_JobClassification_EndDate"));
        }
      }
      if (startDate != null && enddate != null) {
        if (enddate.compareTo(startDate) == -1) {
          throw new OBException(
              OBMessageUtils.messageBD("Ehcm_Enddate").replace("@", objJob.getJobCode()));
        }
      }
      // Job Code and Job Title should be unique
      if ((!objJob.getJobCode().isEmpty()) && (!objJob.getJOBTitle().isEmpty())) {
        isExist = daoimpl.checkJobCodeandJobTitleCombExist(
            objJob.getEhcmJobClassification().getId(), objJob.getJobCode(), objJob.getJOBTitle(),
            objJob.getClient().getId());
        if (isExist) {
          throw new OBException(OBMessageUtils.messageBD("ehcm_jobs_code_uni"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating Jobs   " + e);
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
      Jobs objJob = (Jobs) event.getTargetInstance();
      final Property enabled = entities[0].getProperty(Jobs.PROPERTY_ACTIVE);
      final Property endDate = entities[0].getProperty(Jobs.PROPERTY_ENDDATE);
      final Property stardate = entities[0].getProperty(Jobs.PROPERTY_STARTDATE);
      final Property jobcode = entities[0].getProperty(Jobs.PROPERTY_JOBCODE);
      final Property jobtitle = entities[0].getProperty(Jobs.PROPERTY_JOBTITLE);
      if (event.getCurrentState(endDate) != null) {
        event.setCurrentState(enabled, false);
      }
      Date jobClassStartDate = objJob.getEhcmJobClassification().getStartDate();
      Date startDate = objJob.getStartDate();
      Date enddate = objJob.getEndDate();
      String fDate = Utility.formatDate(startDate);
      String tDate = "";
      Boolean isAllow = true;
      Boolean isExist = false;
      // Check Active job code in the same period
      /*
       * if(enddate == null) { tDate = "21-06-2058"; } else { tDate = Utility.formatDate(enddate); }
       * if(event.getPreviousState(endDate) != null && event.getCurrentState(endDate) != null) {
       * if(!event.getPreviousState(endDate).equals(event.getCurrentState(endDate))) { try { ps1 =
       * conn.
       * getPreparedStatement("select startdate from ehcm_jobs where ehcm_job_classification_id ='"
       * + objJob.getEhcmJobClassification().getId() + "' and value ='" + objJob.getJobCode() +
       * "' and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('" + fDate + "')"
       * +
       * " and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') "
       * + "<= to_date('" + tDate +
       * "','dd-MM-yyyy')) or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') "
       * + ">= to_date('" + fDate +
       * "') and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('" + tDate +
       * "','dd-MM-yyyy'))) and ehcm_jobs.ehcm_jobs_id not in ('" + objJob.getId() + "')"); rs1 =
       * ps1.executeQuery(); if(rs1.next()) { isAllow = Boolean.FALSE; } } catch (Exception e) { //
       * TODO Auto-generated catch block e.printStackTrace(); } } }
       * if(event.getPreviousState(stardate) != null && event.getCurrentState(stardate) != null) {
       * if(!event.getPreviousState(stardate).equals(event.getCurrentState(stardate))) { try { ps1 =
       * conn.
       * getPreparedStatement("select startdate from ehcm_jobs where ehcm_job_classification_id ='"
       * + objJob.getEhcmJobClassification().getId() + "' and value ='" + objJob.getJobCode() +
       * "' and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('" + fDate + "')"
       * +
       * " and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') "
       * + "<= to_date('" + tDate +
       * "','dd-MM-yyyy')) or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') "
       * + ">= to_date('" + fDate +
       * "') and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('" + tDate +
       * "','dd-MM-yyyy'))) and ehcm_jobs.ehcm_jobs_id not in ('" + objJob.getId() + "') "); rs1 =
       * ps1.executeQuery(); if(rs1.next()) { isAllow = Boolean.FALSE; } } catch (Exception e) { //
       * TODO Auto-generated catch block e.printStackTrace(); } } }
       * 
       * if(!isAllow) { throw new OBException(OBMessageUtils.messageBD("EHCM_Job_Conflicts")); }
       */
      // Check more than one Active Jobs for same code
      /*
       * OBQuery<Jobs> jobClass = OBDal.getInstance().createQuery(Jobs.class,
       * "as e where  e.ehcmJobClassification.id = '" + objJob.getEhcmJobClassification().getId() +
       * "'" + " and e.jobCode = '" + objJob.getJobCode() + "' and e.active='Y' and e.id not in ('"
       * + objJob.getId() + "')"); if(jobClass.list().size() > 0) { throw new
       * OBException(OBMessageUtils.messageBD("EHCM_MORE_ACTIVE_JOB").replace("@",
       * objJob.getJobCode())); }
       */
      // validation with Dates
      if (startDate != null && jobClassStartDate != null) {
        if (startDate.compareTo(jobClassStartDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_JobClassification_StartDate"));
        }
      }
      if (enddate != null && jobClassStartDate != null) {
        if (enddate.compareTo(jobClassStartDate) == -1) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_JobClassification_EndDate"));
        }
      }
      if (startDate != null && enddate != null) {
        if (enddate.compareTo(startDate) == -1) {
          throw new OBException(
              OBMessageUtils.messageBD("Ehcm_Enddate").replace("@", objJob.getJobCode()));
        }
      }

      // Job Code and Job Title should be unique
      if ((!objJob.getJobCode().isEmpty()) && (!objJob.getJOBTitle().isEmpty())) {
        if (!event.getPreviousState(jobcode).equals(event.getCurrentState(jobcode))
            || (!event.getPreviousState(jobtitle).equals(event.getCurrentState(jobtitle)))) {
          isExist = daoimpl.checkJobCodeandJobTitleCombExist(
              objJob.getEhcmJobClassification().getId(), objJob.getJobCode(), objJob.getJOBTitle(),
              objJob.getClient().getId());
          if (isExist) {
            throw new OBException(OBMessageUtils.messageBD("ehcm_jobs_code_uni"));
          }
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating Jobs   " + e);
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
      Jobs objJob = (Jobs) event.getTargetInstance();
      JobClassification objClassification = OBDal.getInstance().get(JobClassification.class,
          objJob.getEhcmJobClassification().getId());
      if (objClassification != null) {
        if (objClassification.getStatus().equals("CO")) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_Already_Processed"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating Jobs   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
