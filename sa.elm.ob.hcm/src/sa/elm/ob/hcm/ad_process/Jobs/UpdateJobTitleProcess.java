package sa.elm.ob.hcm.ad_process.Jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.JobClassification;
import sa.elm.ob.hcm.JobClassificationHistory;
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.UpdateJobClassification;
import sa.elm.ob.hcm.UpdateJobs;

/**
 * @author Gopalakrishnan on 13/10/2016
 */

public class UpdateJobTitleProcess extends DalBaseProcess {

  /**
   * Update job Title Process
   */
  private static final Logger log = LoggerFactory.getLogger(UpdateJobTitleProcess.class);

  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement ps = null, ps1 = null;
    ResultSet rs = null, rs1 = null;
    String query = "", query1 = "", query2 = "";
    final String strupClassificationId = (String) bundle.getParams()
        .get("Ehcm_Upjob_Classification_ID").toString();
    UpdateJobClassification ObjUpdateClass = OBDal.getInstance().get(UpdateJobClassification.class,
        strupClassificationId);
    JobClassification objjobClassfication = OBDal.getInstance().get(JobClassification.class,
        ObjUpdateClass.getEhcmJobClassification().getId());
    log.debug("entering into JobClassificationComplete");
    try {
      OBContext.setAdminMode(true);
      if (ObjUpdateClass.getEhcmUpdateJobsList().size() == 0) {
        OBError result = OBErrorBuilder.buildMessage(null, "error",
            OBMessageUtils.messageBD("EHCM_NO_LINES"));
        bundle.setResult(result);
        return;
      }
      OBQuery<UpdateJobs> manualjobClass = OBDal.getInstance().createQuery(UpdateJobs.class,
          "as e where  e.ehcmUpjobClassification.id = '" + ObjUpdateClass.getId() + "'"
              + "  and e.ismanual='Y'");
      if (manualjobClass.list().size() == 0) {
        OBError result = OBErrorBuilder.buildMessage(null, "error",
            OBMessageUtils.messageBD("EHCM_NO_NEW_POSITION"));
        bundle.setResult(result);
        return;
      }
      // update old update job title correction as "No"
      OBDal.getInstance().getConnection().prepareStatement(
          "update ehcm_upjob_classification set iscorrection='N' where ehcm_job_classification_id='"
              + objjobClassfication.getId() + "' and ehcm_upjob_classification_id not in ('"
              + strupClassificationId + "') and status='CO' ")
          .executeUpdate();

      // check jobs are associated with position
      for (Jobs lineJobs : ObjUpdateClass.getEhcmJobsList()) {
        query = "select count(pos.ehcm_jobs_id) as jobscount from ehcm_position pos "
            + " join ehcm_postransactiontype typ on typ.ehcm_postransactiontype_id =pos.ehcm_postransactiontype_id "
            + " join ehcm_jobs jobs on jobs.ehcm_jobs_id=pos.ehcm_jobs_id "
            + " where typ.value not in ('CAPO','TROPO') and pos.isactive='Y' " + " and jobs.value='"
            + lineJobs.getJobCode() + "'";
        log.info("query:" + query);
        ps = conn.prepareStatement(query);
        rs = ps.executeQuery();
        if (rs.next()) {
          if (rs.getBigDecimal("jobscount").compareTo(BigDecimal.ZERO) == 1) {
            // throw new OBException(OBMessageUtils.messageBD("EHCM_JOBS_WITH_POSITION"));
            OBError result = OBErrorBuilder.buildMessage(null, "error",
                OBMessageUtils.messageBD("EHCM_JOBS_WITH_POSITION"));
            bundle.setResult(result);
            return;
          }
        }
      }
      // New Classification code against already Existing Classification Code ,Create New Entry in
      // Job classification
      if (!objjobClassfication.getClassificationCode()
          .equals(ObjUpdateClass.getClassificationCode())) {
        // Before creating new job classification code ,check jobs are associated with position
        query = "select count(job.ehcm_jobs_id) as jobscount from ehcm_jobs job where job.ehcm_jobs_id in (select pos.ehcm_jobs_id from ehcm_position pos "
            + " join ehcm_postransactiontype typ on typ.ehcm_postransactiontype_id =pos.ehcm_postransactiontype_id "
            + " where typ.value not in ('CAPO','TROPO') and pos.isactive='Y') "
            + " and job.ehcm_job_classification_id='" + objjobClassfication.getId() + "' ";
        ps = conn.prepareStatement(query);
        rs = ps.executeQuery();
        if (rs.next()) {
          if (rs.getBigDecimal("jobscount").compareTo(BigDecimal.ZERO) == 1) {
            // throw new OBException(OBMessageUtils.messageBD("EHCM_JOBS_WITH_POSITION"));
            OBError result = OBErrorBuilder.buildMessage(null, "error",
                OBMessageUtils.messageBD("EHCM_JOBS_WITH_POSITION"));
            bundle.setResult(result);
            return;
          }
        }
        rs.close();
        ps.close();

        for (Jobs oldJobs : objjobClassfication.getEhcmJobsList()) {
          if (ObjUpdateClass.getStartDate().compareTo(oldJobs.getStartDate()) <= 0) {
            OBError result = OBErrorBuilder.buildMessage(null, "error",
                OBMessageUtils.messageBD("EHCM_OLD_JOBS_STARTDATE").replace("@",
                    "<b>" + oldJobs.getJobCode() + "</b>"));
            bundle.setResult(result);
            return;
          }
        }

        Date dateBefore = new Date(ObjUpdateClass.getStartDate().getTime() - 1 * 24 * 3600 * 1000);
        if (ObjUpdateClass.getStartDate().compareTo(objjobClassfication.getStartDate()) <= 0) {
          OBError result = OBErrorBuilder.buildMessage(null, "error",
              OBMessageUtils.messageBD("EHCM_UPDATE_JOB_STARTDATE").replace("@",
                  "<b>" + objjobClassfication.getClassificationCode() + "</b>"));
          bundle.setResult(result);
          return;
        }
        objjobClassfication.setEndDate(dateBefore);
        OBDal.getInstance().save(objjobClassfication);
        JobClassification cloneJobsClassfication = OBProvider.getInstance()
            .get(JobClassification.class);
        // make entry in classification
        cloneJobsClassfication.setClient(ObjUpdateClass.getClient());
        cloneJobsClassfication.setOrganization(ObjUpdateClass.getOrganization());
        cloneJobsClassfication.setStatus("CO");
        cloneJobsClassfication.setCorrection(false);
        cloneJobsClassfication.setClassificationCode(ObjUpdateClass.getClassificationCode());
        cloneJobsClassfication.setClassificationDate(ObjUpdateClass.getClassificationDate());
        cloneJobsClassfication.setActive(ObjUpdateClass.isActive());
        cloneJobsClassfication.setEndDate(ObjUpdateClass.getEndDate());
        cloneJobsClassfication.setGroupSeqCode(ObjUpdateClass.getGroupSequenceCode());
        cloneJobsClassfication.setGroupSeqName(ObjUpdateClass.getGroupSequenceName());
        cloneJobsClassfication.setEhcmJobGroup(ObjUpdateClass.getJobGroup());
        cloneJobsClassfication.setMainGroupCode(ObjUpdateClass.getMainGroupCode());
        cloneJobsClassfication.setMainGroupName(ObjUpdateClass.getMainGroupName());
        cloneJobsClassfication.setMcsletterDate(ObjUpdateClass.getMCSLetterDate());
        cloneJobsClassfication.setMcsletterNo(ObjUpdateClass.getMCSLetterNo());
        cloneJobsClassfication.setProcessed(ObjUpdateClass.isProcessed());
        cloneJobsClassfication.setStartDate(ObjUpdateClass.getStartDate());
        cloneJobsClassfication.setSUBGroupCode(ObjUpdateClass.getSubGroupCode());
        cloneJobsClassfication.setSUBGroupName(ObjUpdateClass.getSubGroupName());
        OBDal.getInstance().save(cloneJobsClassfication);
        // Make Entry in jobs
        OBQuery<UpdateJobs> objJobsList = OBDal.getInstance().createQuery(UpdateJobs.class,
            "as e where e.ehcmUpjobClassification.id='" + ObjUpdateClass.getId()
                + "' and e.ismanual='Y'");
        for (UpdateJobs objJobs : objJobsList.list()) {
          Jobs objCloneJobs = OBProvider.getInstance().get(Jobs.class);
          objCloneJobs.setClient(objJobs.getClient());
          objCloneJobs.setOrganization(objJobs.getOrganization());
          objCloneJobs.setJobCode(objJobs.getJobCode());
          objCloneJobs.setEhcmJobClassification(cloneJobsClassfication);
          objCloneJobs.setActive(objJobs.isActive());
          objCloneJobs.setEndDate(objJobs.getEndDate());
          objCloneJobs.setGrade(objJobs.getGrade());
          objCloneJobs.setProcessed(objJobs.isProcessed());
          objCloneJobs.setStartDate(objJobs.getStartDate());
          objCloneJobs.setEhcmUpjobClassification(ObjUpdateClass);
          objCloneJobs.setJOBTitle(objJobs.getTitle());
          OBDal.getInstance().save(objCloneJobs);
        }

        /*
         * make entry in history .Added for task no 3762
         */

        OBQuery<JobClassificationHistory> historyQuery = OBDal.getInstance().createQuery(
            JobClassificationHistory.class,
            "as e where e.ehcmJobClassification.id ='" + objjobClassfication.getId() + "'");
        if (historyQuery.list().size() > 0) {
          for (JobClassificationHistory objJobHistory : historyQuery.list()) {
            JobClassificationHistory objJobHistoryClone = (JobClassificationHistory) DalUtil
                .copy(objJobHistory, false);
            objJobHistoryClone.setEhcmJobClassification(cloneJobsClassfication);
            OBDal.getInstance().save(objJobHistoryClone);
          }
          JobClassificationHistory objCloneHistory = OBProvider.getInstance()
              .get(JobClassificationHistory.class);
          objCloneHistory.setAlertStatus(objjobClassfication.getStatus());
          objCloneHistory.setClassificationCode(objjobClassfication.getClassificationCode());
          objCloneHistory.setDescription(objjobClassfication.getDescription());
          objCloneHistory.setEhcmJobClassification(cloneJobsClassfication);
          objCloneHistory.setEnabled(true);
          objCloneHistory.setClassificationDate(objjobClassfication.getClassificationDate());
          objCloneHistory.setEndDate(objjobClassfication.getEndDate());
          objCloneHistory.setGroupSequenceCode(objjobClassfication.getGroupSeqCode());
          objCloneHistory.setGroupSequenceName(objjobClassfication.getGroupSeqName());
          objCloneHistory.setJobGroup(objjobClassfication.getEhcmJobGroup());
          objCloneHistory.setMainGroupCode(objjobClassfication.getMainGroupCode());
          objCloneHistory.setMainGroupName(objjobClassfication.getMainGroupName());
          objCloneHistory.setMCSLetterDate(objjobClassfication.getMcsletterDate());
          objCloneHistory.setMCSLetterNo(objjobClassfication.getMcsletterNo());
          objCloneHistory.setStartDate(objjobClassfication.getStartDate());
          objCloneHistory.setSubGroupCode(objjobClassfication.getSUBGroupCode());
          objCloneHistory.setSubGroupName(objjobClassfication.getSUBGroupName());
          OBDal.getInstance().save(objCloneHistory);
        } else {
          JobClassificationHistory objCloneHistory = OBProvider.getInstance()
              .get(JobClassificationHistory.class);
          objCloneHistory.setAlertStatus(objjobClassfication.getStatus());
          objCloneHistory.setClassificationCode(objjobClassfication.getClassificationCode());
          objCloneHistory.setDescription(objjobClassfication.getDescription());
          objCloneHistory.setEhcmJobClassification(cloneJobsClassfication);
          objCloneHistory.setEnabled(true);
          objCloneHistory.setEndDate(objjobClassfication.getEndDate());
          objCloneHistory.setClassificationDate(objjobClassfication.getClassificationDate());
          objCloneHistory.setGroupSequenceCode(objjobClassfication.getGroupSeqCode());
          objCloneHistory.setGroupSequenceName(objjobClassfication.getGroupSeqName());
          objCloneHistory.setJobGroup(objjobClassfication.getEhcmJobGroup());
          objCloneHistory.setMainGroupCode(objjobClassfication.getMainGroupCode());
          objCloneHistory.setMainGroupName(objjobClassfication.getMainGroupName());
          objCloneHistory.setMCSLetterDate(objjobClassfication.getMcsletterDate());
          objCloneHistory.setMCSLetterNo(objjobClassfication.getMcsletterNo());
          objCloneHistory.setStartDate(objjobClassfication.getStartDate());
          objCloneHistory.setSubGroupCode(objjobClassfication.getSUBGroupCode());
          objCloneHistory.setSubGroupName(objjobClassfication.getSUBGroupName());
          OBDal.getInstance().save(objCloneHistory);
        }
      } else {
        // Map Already Exists Code in Job Classification
        query1 = " select upjobs.ehcm_update_jobs_id,jobs.ehcm_jobs_id,upjobs.startdate,jobs.startdate as jobdate,upjobs.ismanual as ismanual from ehcm_update_jobs upjobs "
            + " join ehcm_upjob_classification upcls on upcls.ehcm_upjob_classification_id =upjobs.ehcm_upjob_classification_id "
            + " and upcls.ehcm_upjob_classification_id='" + strupClassificationId + "' "
            + " join ehcm_job_classification cls on cls.ehcm_job_classification_id = upcls.ehcm_job_classification_id "
            + " and cls.ehcm_job_classification_id='" + objjobClassfication.getId() + "' "
            + " join ehcm_jobs jobs on  jobs.ehcm_job_classification_id=cls.ehcm_job_classification_id "
            + " and jobs.Ehcm_Jobs_ID=upjobs.Ehcm_Jobs_ID and jobs.isactive='Y' "; // where
                                                                                   // upjobs.ismanual='Y'
        ps = conn.prepareStatement(query1);
        rs = ps.executeQuery();
        while (rs.next()) {
          Jobs objJobs = OBDal.getInstance().get(Jobs.class, rs.getString("ehcm_jobs_id"));
          Date date = rs.getDate("startdate");// intialize your date to any date
          Date dateBefore = new Date(date.getTime() - 1 * 24 * 3600 * 1000);
          /*
           * if (dateBefore.compareTo(objJobs.getStartDate()) == -1) { OBError result =
           * OBErrorBuilder.buildMessage(null, "error",
           * OBMessageUtils.messageBD("EHCM_UPDATE_JOBS_DATE")); bundle.setResult(result); return; }
           */
          /*
           * objJobs.setActive(false); objJobs.setEndDate(dateBefore);
           */

          if (rs.getDate("startdate").compareTo(rs.getDate("jobdate")) == 0) {
            OBDal.getInstance().getConnection()
                .prepareStatement(
                    "Update ehcm_jobs set isactive = 'N', enddate= '" + rs.getDate("jobdate")
                        + "' where ehcm_jobs_id = '" + rs.getString("ehcm_jobs_id") + "'")
                .executeUpdate();
          } else {
            OBDal.getInstance().getConnection()
                .prepareStatement("Update ehcm_jobs set isactive = 'N', enddate= '" + dateBefore
                    + "' where ehcm_jobs_id = '" + rs.getString("ehcm_jobs_id") + "'")
                .executeUpdate();
          }
          if (rs.getString("ismanual").equals("Y")) {
            UpdateJobs cloneUpJobs = OBDal.getInstance().get(UpdateJobs.class,
                rs.getString("ehcm_update_jobs_id"));
            Jobs cloneJobs = OBProvider.getInstance().get(Jobs.class);
            cloneJobs.setOrganization(objjobClassfication.getOrganization());
            cloneJobs.setClient(objjobClassfication.getClient());
            cloneJobs.setEhcmJobClassification(objjobClassfication);
            cloneJobs.setActive(true);
            cloneJobs.setGrade(cloneUpJobs.getGrade());
            cloneJobs.setJOBTitle(cloneUpJobs.getTitle());
            cloneJobs.setProcessed(true);
            cloneJobs.setEhcmUpjobClassification(ObjUpdateClass);
            cloneJobs.setStartDate(cloneUpJobs.getStartDate());
            cloneJobs.setJobCode(cloneUpJobs.getJobCode());
            OBDal.getInstance().save(cloneJobs);
          }
        }
        // Find newly Created updated Jobs and insert the new records in Jobs
        query2 = " select upjobs.ehcm_update_jobs_id from ehcm_update_jobs upjobs "
            + " where  upjobs.ehcm_upjob_classification_id='" + strupClassificationId
            + "' and upjobs.ismanual='Y' and upjobs.Ehcm_Jobs_ID is null";
        /*
         * upjobs.value not in (select jobs.value from ehcm_jobs jobs " +
         * " join ehcm_job_classification cls on cls.ehcm_job_classification_id=jobs.ehcm_job_classification_id "
         * + " where cls.ehcm_job_classification_id='" + objjobClassfication.getId() + "' ) and
         */
        ps1 = conn.prepareStatement(query2);
        rs1 = ps1.executeQuery();
        while (rs1.next()) {
          UpdateJobs objUpjobs = OBDal.getInstance().get(UpdateJobs.class,
              rs1.getString("ehcm_update_jobs_id"));
          Jobs cloneJobs = OBProvider.getInstance().get(Jobs.class);
          cloneJobs.setOrganization(objjobClassfication.getOrganization());
          cloneJobs.setClient(objjobClassfication.getClient());
          cloneJobs.setEhcmJobClassification(objjobClassfication);
          cloneJobs.setActive(true);
          cloneJobs.setGrade(objUpjobs.getGrade());
          cloneJobs.setJOBTitle(objUpjobs.getTitle());
          cloneJobs.setProcessed(true);
          cloneJobs.setEhcmUpjobClassification(ObjUpdateClass);
          cloneJobs.setStartDate(objUpjobs.getStartDate());
          cloneJobs.setJobCode(objUpjobs.getJobCode());
          OBDal.getInstance().save(cloneJobs);
        }
        rs1.close();
        ps1.close();
        // update Job Classification Mcs NO.,main group code,main group name,sub group code,sub
        // group name,gropu seq code and group seq name

        // objjobClassfication.setMcsletterNo(ObjUpdateClass.getMCSLetterNo());//( TaskNo-6589
        // point-5)
        objjobClassfication.setMainGroupCode(ObjUpdateClass.getMainGroupCode());
        objjobClassfication.setMainGroupName(ObjUpdateClass.getMainGroupName());
        objjobClassfication.setSUBGroupCode(ObjUpdateClass.getSubGroupCode());
        objjobClassfication.setSUBGroupName(ObjUpdateClass.getSubGroupName());
        objjobClassfication.setGroupSeqCode(ObjUpdateClass.getGroupSequenceCode());
        objjobClassfication.setGroupSeqName(ObjUpdateClass.getGroupSequenceName());
        OBDal.getInstance().save(objjobClassfication);
      }
      ObjUpdateClass.setAlertStatus("CO");
      ObjUpdateClass.setProcessed(true);
      OBDal.getInstance().save(ObjUpdateClass);
      OBError result = OBErrorBuilder.buildMessage(null, "Success",
          OBMessageUtils.messageBD("Efin_BudgetPre_Submit"));
      bundle.setResult(result);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();

    } catch (Exception e) {
      log.error("error in UpdateJobTitleProcess:" + e);
      OBDal.getInstance().rollbackAndClose();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);

    } finally {
      OBContext.restorePreviousMode();
    }

  }

}
