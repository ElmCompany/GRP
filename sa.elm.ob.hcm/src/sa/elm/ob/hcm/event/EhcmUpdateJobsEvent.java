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
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DalConnectionProvider;

import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.UpdateJobClassification;
import sa.elm.ob.hcm.UpdateJobs;
import sa.elm.ob.hcm.event.dao.JobEventDAOImpl;
import sa.elm.ob.utility.util.Utility;

public class EhcmUpdateJobsEvent extends EntityPersistenceEventObserver {
  /**
   * Business Event on Table EHCM_Update_Jobs.
   */
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(UpdateJobs.ENTITY_NAME) };
  PreparedStatement ps = null, ps1 = null;
  ResultSet rs = null, rs1 = null;
  ConnectionProvider conn = new DalConnectionProvider(false);
  JobEventDAOImpl daoimpl = new JobEventDAOImpl();

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
      UpdateJobs updjob = (UpdateJobs) event.getTargetInstance();
      final Property jobcode = entities[0].getProperty(UpdateJobs.PROPERTY_JOBCODE);
      final Property jobtitle = entities[0].getProperty(UpdateJobs.PROPERTY_TITLE);
      String strJobclassificationid = updjob.getEhcmUpjobClassification().getEhcmJobClassification()
          .getId();
      String value = updjob.getJobCode();
      Date startDate = updjob.getStartDate();
      Date enddate = updjob.getEndDate();
      String fDate = Utility.formatDate(startDate);
      String tDate = "", query = "";

      Boolean isAllow = true, isPositionAllow = true;
      Boolean isExist = false;
      Boolean isassociated = false;

      // for manual N classification job should be empty
      /*
       * if (!updjob.isManual() && updjob.getEhcmJobs() != null) { throw new
       * OBException(OBMessageUtils.messageBD("EHCM_ClassificationJob_Empty")); }
       */

      if (updjob.getEhcmJobs() != null && updjob.isManual()) {
        if (updjob.getEhcmJobs().getJOBTitle().equals(updjob.getTitle())) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_JobTitle_Exist"));
        }
      }

      // Check Active job code in the same period
      /*
       * if (enddate == null) { tDate = "21-06-2058"; } else { tDate = Utility.formatDate(enddate);
       * } try { query = "select count(pos.ehcm_jobs_id) as jobscount from ehcm_position pos " +
       * " join ehcm_postransactiontype typ on typ.ehcm_postransactiontype_id =pos.ehcm_postransactiontype_id "
       * + " join ehcm_jobs jobs on jobs.ehcm_jobs_id=pos.ehcm_jobs_id " +
       * " where typ.value not in ('CAPO','TROPO') and pos.isactive='Y' " + " and jobs.value='" +
       * updjob.getJobCode() + "'"; log.info("query:" + query); ps =
       * conn.getPreparedStatement(query); rs = ps.executeQuery(); if (rs.next()) { if
       * (rs.getBigDecimal("jobscount").compareTo(BigDecimal.ZERO) == 1) { isPositionAllow =
       * Boolean.FALSE;
       * 
       * } } ps1 = conn.getPreparedStatement(
       * "select startdate from ehcm_update_jobs where ehcm_upjob_classification_id ='" +
       * updjob.getEhcmUpjobClassification().getId() + "' and value ='" + updjob.getJobCode() +
       * "' and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('" + fDate + "')"
       * +
       * " and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') "
       * + "<= to_date('" + tDate +
       * "','dd-MM-yyyy')) or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') "
       * + ">= to_date('" + fDate +
       * "') and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('" + tDate +
       * "','dd-MM-yyyy'))) and ehcm_update_jobs.ismanual='Y' and ehcm_update_jobs_id not in ('" +
       * updjob.getId() + "')"); rs1 = ps1.executeQuery(); if (rs1.next()) { isAllow =
       * Boolean.FALSE; } } catch (Exception e) { // TODO Auto-generated catch block
       * e.printStackTrace(); } if (!isPositionAllow) { throw new
       * OBException(OBMessageUtils.messageBD("EHCM_JOBS_WITH_POSITION")); } if (!isAllow) { throw
       * new OBException(OBMessageUtils.messageBD("EHCM_Job_Conflicts")); }
       */

      /*
       * // more than one active job OBQuery<UpdateJobs> updatejobClass =
       * OBDal.getInstance().createQuery(UpdateJobs.class,
       * "as e where  e.ehcmUpjobClassification.id = '" +
       * updjob.getEhcmUpjobClassification().getId() + "'" + " and e.jobCode = '" + value +
       * "' and e.active='Y' and e.ismanual='Y' and e.id not in ('" + updjob.getId() + "')"); if
       * (updatejobClass.list().size() > 0) { throw new
       * OBException(OBMessageUtils.messageBD("EHCM_MORE_ACTIVE_JOB").replace("@", value)); }
       */

      // update old entry
      if (updjob.getEhcmJobs() != null) {
        OBQuery<UpdateJobs> manualjobClass = OBDal.getInstance().createQuery(UpdateJobs.class,
            "as e where  e.ehcmUpjobClassification.id = '"
                + updjob.getEhcmUpjobClassification().getId() + "'" + " and e.jobCode = '"
                + updjob.getEhcmJobs().getJobCode() + "' and e.title='"
                + updjob.getEhcmJobs().getJOBTitle() + "'  and e.ismanual='N'");
        manualjobClass.setFilterOnActive(false);
        if (manualjobClass.list().size() > 0) {
          for (UpdateJobs lines : manualjobClass.list()) {
            if (updjob.getStartDate().compareTo(lines.getStartDate()) == -1) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_UPDATE_JOBS_DATE"));
            } /*
               * else if (updjob.getStartDate().compareTo(lines.getStartDate()) == 0) { throw new
               * OBException(OBMessageUtils.messageBD("EHCM_UPDATE_JOBS_DATE")); }
               */ else {
              Date dateBefore = new Date(updjob.getStartDate().getTime() - 1 * 24 * 3600 * 1000);
              try {
                ps1 = conn
                    .getPreparedStatement("Update Ehcm_Update_Jobs set isactive = 'N',  enddate= '"
                        + dateBefore + "' where ehcm_update_jobs_id = '" + lines.getId() + "'");
                ps1.executeUpdate();
                ps1.close();

              } catch (Exception e) {
                // TODO Auto-generated catch block
                log.error("Exception in Update Jobs Event", e);
                e.printStackTrace();
              }
            }
          }
        }
      }
      // check with Dates
      Date jobClassStartDate = updjob.getEhcmUpjobClassification().getStartDate();
      // TaskNo.- 6545 while updateing the old job title should not validate this condition
      /*
       * if (startDate != null && jobClassStartDate != null) { if
       * (startDate.compareTo(jobClassStartDate) == -1) { throw new
       * OBException(OBMessageUtils.messageBD("EHCM_JobClassification_StartDate")); } }
       */
      // End Date should not be greater than or equal to Job Classification Start Date
      if (enddate != null && jobClassStartDate != null) {
        if ((enddate.compareTo(jobClassStartDate) == 1)
            || (enddate.compareTo(jobClassStartDate) == 0)) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_UpdateJobTitile_EndDate"));
        }
      }
      if (startDate != null && enddate != null) {
        if (enddate.compareTo(startDate) == -1) {
          throw new OBException(
              OBMessageUtils.messageBD("Ehcm_Enddate").replace("@", updjob.getJobCode()));
        }
      }
      OBQuery<Jobs> jobClass = OBDal.getInstance().createQuery(Jobs.class,
          "as e where  e.ehcmJobClassification.id = '" + strJobclassificationid + "'"
              + " and e.jobCode = '" + value + "'");
      if (jobClass.list().size() > 0) {
        for (Jobs lines : jobClass.list()) {
          if (updjob.getStartDate().compareTo(lines.getStartDate()) < 0) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_UPDATE_JOBS_DATE"));
          }
          /*
           * if(updjob.getStartDate().compareTo(lines.getStartDate())== 0 ) { throw new
           * OBException(OBMessageUtils.messageBD("EHCM_UPDATE_JOBS_DATE")); }
           */
        }

      }
      // Job Code and Job Title should be unique
      if (!updjob.getJobCode().isEmpty() && !updjob.getTitle().isEmpty()) {
        if (!event.getPreviousState(jobcode).equals(event.getCurrentState(jobcode))
            || (!event.getPreviousState(jobtitle).equals(event.getCurrentState(jobtitle)))) {
          isExist = daoimpl.checkUpJobCodeandUpJobTitleCombExist(
              updjob.getEhcmUpjobClassification().getId(), updjob.getJobCode(), updjob.getTitle(),
              updjob.getClient().getId());
          if (isExist) {
            throw new OBException(OBMessageUtils.messageBD("ehcm_jobs_code_uni"));
          }
        }

      }
      // before disable the job.. check job associated with position or not
      if (updjob.getEndDate() != null || !updjob.isActive()) {
        isassociated = daoimpl.checkJobAssociatedinPosition(updjob.getEhcmJobs().getId(),
            updjob.getClient().getId());
        if (isassociated) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_Job_Position"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating UpdateJobEvent: " + e);
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
      UpdateJobs updjob = (UpdateJobs) event.getTargetInstance();
      String strJobclassificationid = updjob.getEhcmUpjobClassification().getEhcmJobClassification()
          .getId();
      String value = updjob.getJobCode();
      Date startDate = updjob.getStartDate();
      Date enddate = updjob.getEndDate();
      Date jobClassStartDate = updjob.getEhcmUpjobClassification().getStartDate();
      String fDate = Utility.formatDate(startDate);
      String tDate = "", query = "";
      Boolean isAllow = true, isPositionAllow = true;
      Boolean isExist = false;
      Boolean isassociated = false;

      if (updjob.getEhcmJobs() != null) {
        if (updjob.getEhcmJobs().getJOBTitle().equals(updjob.getTitle())) {
          throw new OBException(OBMessageUtils.messageBD("Ehcm_JobTitle_Exist"));
        }
      }
      // Check Active job code in the same period
      /*
       * if (enddate == null) { tDate = "21-06-2058"; } else { tDate = Utility.formatDate(enddate);
       * } try { query = "select count(pos.ehcm_jobs_id) as jobscount from ehcm_position pos " +
       * " join ehcm_postransactiontype typ on typ.ehcm_postransactiontype_id =pos.ehcm_postransactiontype_id "
       * + " join ehcm_jobs jobs on jobs.ehcm_jobs_id=pos.ehcm_jobs_id " +
       * " where typ.value not in ('CAPO','TROPO') and pos.isactive='Y' " + " and jobs.value='" +
       * updjob.getJobCode() + "'"; log.info("query:" + query); ps =
       * conn.getPreparedStatement(query); rs = ps.executeQuery(); if (rs.next()) { if
       * (rs.getBigDecimal("jobscount").compareTo(BigDecimal.ZERO) == 1) { isPositionAllow =
       * Boolean.FALSE; } }
       * 
       * ps1 = conn.getPreparedStatement(
       * "select startdate from ehcm_update_jobs where ehcm_upjob_classification_id ='" +
       * updjob.getEhcmUpjobClassification().getId() + "' and value ='" + updjob.getJobCode() +
       * "' and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('" + fDate + "')"
       * +
       * " and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') "
       * + "<= to_date('" + tDate +
       * "','dd-MM-yyyy')) or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') "
       * + ">= to_date('" + fDate +
       * "') and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('" + tDate +
       * "','dd-MM-yyyy'))) and ehcm_update_jobs.ismanual='Y' "); rs1 = ps1.executeQuery(); if
       * (rs1.next()) { isAllow = Boolean.FALSE; } } catch (Exception e) { // TODO Auto-generated
       * catch block e.printStackTrace(); }
       */

      // check more than one active record
      /*
       * OBQuery<UpdateJobs> updatejobClass = OBDal.getInstance().createQuery(UpdateJobs.class,
       * "as e where  e.ehcmUpjobClassification.id = '" +
       * updjob.getEhcmUpjobClassification().getId() + "'" + " and e.jobCode = '" + value +
       * "' and e.active='Y' and e.ismanual='Y'"); if (updatejobClass.list().size() > 0) { throw new
       * OBException(OBMessageUtils.messageBD("EHCM_MORE_ACTIVE_JOB").replace("@", value)); }
       */

      // update old entry
      if (updjob.getEhcmJobs() != null) {
        OBQuery<UpdateJobs> manualjobClass = OBDal.getInstance().createQuery(UpdateJobs.class,
            "as e where  e.ehcmUpjobClassification.id = '"
                + updjob.getEhcmUpjobClassification().getId() + "'" + " and e.jobCode = '"
                + updjob.getEhcmJobs().getJobCode() + "' and e.title= '"
                + updjob.getEhcmJobs().getJOBTitle() + "' and  e.ismanual='N'");
        manualjobClass.setFilterOnActive(false);
        if (manualjobClass.list().size() > 0) {
          for (UpdateJobs lines : manualjobClass.list()) {
            if (updjob.getStartDate().compareTo(lines.getStartDate()) == -1) {
              throw new OBException(OBMessageUtils.messageBD("EHCM_UPDATE_JOBS_DATE"));
            } /*
               * else if (updjob.getStartDate().compareTo(lines.getStartDate()) == 0) { throw new
               * OBException(OBMessageUtils.messageBD("EHCM_UPDATE_JOBS_DATE")); }
               */ else {
              Date dateBefore = new Date(updjob.getStartDate().getTime() - 1 * 24 * 3600 * 1000);
              try {
                if (lines.getStartDate().compareTo(updjob.getStartDate()) == 0) {
                  ps1 = conn.getPreparedStatement(
                      "Update Ehcm_Update_Jobs set isactive = 'N',enddate= '" + lines.getStartDate()
                          + "' where ehcm_update_jobs_id = '" + lines.getId() + "'");
                  ps1.executeUpdate();
                  ps1.close();
                } else {
                  ps1 = conn
                      .getPreparedStatement("Update Ehcm_Update_Jobs set isactive = 'N',enddate= '"
                          + dateBefore + "' where ehcm_update_jobs_id = '" + lines.getId() + "'");
                  ps1.executeUpdate();
                  ps1.close();
                }

              } catch (Exception e) {
                // TODO Auto-generated catch block
                log.error("Exception in Update Jobs Event", e);
                e.printStackTrace();
              }
            }
          }
        }
      }
      if (!isPositionAllow) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_JOBS_WITH_POSITION"));
      }

      if (!isAllow) {
        throw new OBException(OBMessageUtils.messageBD("EHCM_Job_Conflicts"));
      }
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
              OBMessageUtils.messageBD("Ehcm_Enddate").replace("@", updjob.getJobCode()));
        }
      }
      OBQuery<Jobs> jobClass = OBDal.getInstance().createQuery(Jobs.class,
          "as e where  e.ehcmJobClassification.id = '" + strJobclassificationid + "'"
              + " and e.jobCode = '" + value + "'");
      if (jobClass.list().size() > 0) {
        for (Jobs lines : jobClass.list()) {
          if (updjob.getStartDate().compareTo(lines.getStartDate()) == -1) {
            throw new OBException(OBMessageUtils.messageBD("EHCM_UPDATE_JOBS_DATE"));
          }
          /*
           * if(updjob.getStartDate().compareTo(lines.getStartDate())==0 ) { throw new
           * OBException(OBMessageUtils.messageBD("EHCM_UPDATE_JOBS_DATE")); }
           */
        }

      }

      // Job Code and Job Title should be unique
      if (!updjob.getJobCode().isEmpty() && !updjob.getTitle().isEmpty()) {
        isExist = daoimpl.checkUpJobCodeandUpJobTitleCombExist(
            updjob.getEhcmUpjobClassification().getId(), updjob.getJobCode(), updjob.getTitle(),
            updjob.getClient().getId());
        if (isExist) {
          throw new OBException(OBMessageUtils.messageBD("ehcm_jobs_code_uni"));
        }

      }
      // before disable the job.. check job associated with position or not
      if (updjob.getEndDate() != null || !updjob.isActive()) {
        isassociated = daoimpl.checkJobAssociatedinPosition(updjob.getEhcmJobs().getId(),
            updjob.getClient().getId());
        if (isassociated) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_Job_Position"));
        }
      }

    } catch (OBException e) {
      log.error(" Exception while creating UpdateJobEvent: " + e);
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
      UpdateJobs updjob = (UpdateJobs) event.getTargetInstance();
      UpdateJobClassification objupdateJobs = updjob.getEhcmUpjobClassification();
      if (objupdateJobs != null) {
        if (objupdateJobs.getAlertStatus().equals("CO")) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_Already_Processed"));
        }
      }
      if (updjob != null) {
        if (!updjob.isManual()) {
          throw new OBException(OBMessageUtils.messageBD("EHCM_PREV_CLASSIFICATION"));
        }
      }
      // update old entry date as empty
      OBQuery<UpdateJobs> manualjobClass = OBDal.getInstance().createQuery(UpdateJobs.class,
          "as e where  e.ehcmUpjobClassification.id = '"
              + updjob.getEhcmUpjobClassification().getId() + "'" + " and e.jobCode = '"
              + updjob.getJobCode() + "' and  e.ismanual='N'");
      manualjobClass.setFilterOnActive(false);
      if (manualjobClass.list().size() > 0) {
        for (UpdateJobs lines : manualjobClass.list()) {
          try {
            ps1 = conn.getPreparedStatement(
                "Update Ehcm_Update_Jobs set isactive = 'N',enddate=null  where ehcm_update_jobs_id = '"
                    + lines.getId() + "'");
            ps1.executeUpdate();
            ps1.close();
          } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error("Exception in Delete Jobs Event", e);
            e.printStackTrace();
          }
        }
      }
    } catch (OBException e) {
      log.error(" Exception while Deleting Jobs   " + e);
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
