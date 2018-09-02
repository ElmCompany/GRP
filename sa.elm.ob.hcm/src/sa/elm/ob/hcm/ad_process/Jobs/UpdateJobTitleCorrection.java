package sa.elm.ob.hcm.ad_process.Jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.JobClassification;
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.UpdateJobClassification;

/**
 * @author Gopalakrishnan on 20/10/2016
 */

public class UpdateJobTitleCorrection extends DalBaseProcess {

  /**
   * Update Job Title Correction
   */
  private static final Logger log = LoggerFactory.getLogger(UpdateJobTitleCorrection.class);

  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement ps = null, ps1 = null;
    ResultSet rs = null, rs1 = null;
    String query = "", query1 = "";
    final String strupClassificationId = (String) bundle.getParams()
        .get("Ehcm_Upjob_Classification_ID").toString();
    UpdateJobClassification ObjUpdateClass = OBDal.getInstance().get(UpdateJobClassification.class,
        strupClassificationId);
    JobClassification objjobClassfication = OBDal.getInstance().get(JobClassification.class,
        ObjUpdateClass.getEhcmJobClassification().getId());
    // add active record in lines from jobs
    log.debug("entering into update job title Correction ");
    try {
      OBContext.setAdminMode(true);
      // New Classification Code Correction
      if (!objjobClassfication.getClassificationCode()
          .equals(ObjUpdateClass.getClassificationCode())) {
        String strNewJobclassCode = "";
        // Delete newly created jobs and Job Classification
        query = "select ehcm_jobs_id,ehcm_job_classification_id from ehcm_jobs  where ehcm_upjob_classification_id ='"
            + strupClassificationId + "' ";
        ps = conn.prepareStatement(query);
        rs = ps.executeQuery();
        while (rs.next()) {
          strNewJobclassCode = rs.getString("ehcm_job_classification_id");
          Jobs activeJobs = OBDal.getInstance().get(Jobs.class, rs.getString("ehcm_jobs_id"));
          if (activeJobs.getEhcmPositionList().size() > 0) {
            OBDal.getInstance().rollbackAndClose();
            OBError result = OBErrorBuilder.buildMessage(null, "error",
                OBMessageUtils.messageBD("EHCM_JOBS_WITH_POSITION"));
            bundle.setResult(result);
            return;
          } else {
            // delete job line
            OBDal.getInstance().getConnection().prepareStatement(
                "delete from ehcm_jobs where ehcm_jobs_id='" + rs.getString("ehcm_jobs_id") + "'")
                .executeUpdate();
          }
        }
        rs.close();
        ps.close();
        // delete record from history
        OBDal.getInstance().getConnection()
            .prepareStatement(
                "delete from ehcm_job_class_history where ehcm_job_classification_id='"
                    + strNewJobclassCode + "'")
            .executeUpdate();
        // delete job header
        OBDal.getInstance().getConnection()
            .prepareStatement(
                "delete from ehcm_job_classification where ehcm_job_classification_id='"
                    + strNewJobclassCode + "'")
            .executeUpdate();

        // brings back old entry into active
        for (Jobs objobs : objjobClassfication.getEhcmJobsList()) {
          if (objobs.getEndDate().compareTo(objjobClassfication.getEndDate()) == 0) {
            objobs.setEndDate(null);
            objobs.setActive(true);
            OBDal.getInstance().save(objobs);
          }
        }
        objjobClassfication.setEndDate(null);
        objjobClassfication.setActive(true);
        OBDal.getInstance().save(objjobClassfication);

      }

      // Old Classification code correction
      if (objjobClassfication.getClassificationCode()
          .equals(ObjUpdateClass.getClassificationCode())) {
        // delete new active jobs from job classification jobs
        query = " select upjobs.ehcm_update_jobs_id,jobs.ehcm_jobs_id,upjobs.startdate from ehcm_update_jobs upjobs  "
            + " join ehcm_upjob_classification upcls on upcls.ehcm_upjob_classification_id =upjobs.ehcm_upjob_classification_id  "
            + " and upcls.ehcm_upjob_classification_id='" + strupClassificationId + "' "
            + " join ehcm_job_classification cls on cls.ehcm_job_classification_id = upcls.ehcm_job_classification_id "
            + " and cls.ehcm_job_classification_id='" + objjobClassfication.getId() + "' "
            + " join ehcm_jobs jobs on  jobs.ehcm_job_classification_id=cls.ehcm_job_classification_id   "
            + " and jobs.value=upjobs.value and jobs.isactive='Y' where upjobs.ismanual='Y' "
            + " and jobs.ehcm_upjob_classification_id=upcls.ehcm_upjob_classification_id ";
        ps = conn.prepareStatement(query);
        rs = ps.executeQuery();
        while (rs.next()) {
          Jobs activeJobs = OBDal.getInstance().get(Jobs.class, rs.getString("ehcm_jobs_id"));
          if (activeJobs.getEhcmPositionList().size() > 0) {
            OBDal.getInstance().rollbackAndClose();
            OBError result = OBErrorBuilder.buildMessage(null, "error",
                OBMessageUtils.messageBD("EHCM_JOBS_WITH_POSITION"));
            bundle.setResult(result);
            return;
          } else {
            OBDal.getInstance().getConnection().prepareStatement(
                "delete from ehcm_jobs where ehcm_jobs_id ='" + rs.getString("ehcm_jobs_id") + "' ")
                .executeUpdate();
          }

        }
        rs.close();
        ps.close();
        // bring old entry into active.
        query1 = " select upjobs.ehcm_update_jobs_id,jobs.ehcm_jobs_id,upjobs.startdate from ehcm_update_jobs upjobs "
            + " join ehcm_upjob_classification upcls on upcls.ehcm_upjob_classification_id =upjobs.ehcm_upjob_classification_id  "
            + " and upcls.ehcm_upjob_classification_id='" + strupClassificationId + "' "
            + " join ehcm_job_classification cls on cls.ehcm_job_classification_id = upcls.ehcm_job_classification_id "
            + " and cls.ehcm_job_classification_id='" + objjobClassfication.getId() + "' "
            + " join ehcm_jobs jobs on  jobs.ehcm_job_classification_id=cls.ehcm_job_classification_id  "
            + " and jobs.value=upjobs.value and jobs.isactive='N' where upjobs.ismanual='N' "
            + " and jobs.enddate=upjobs.enddate ";
        ps1 = conn.prepareStatement(query1);
        rs1 = ps1.executeQuery();
        while (rs1.next()) {
          Jobs activeJobs = OBDal.getInstance().get(Jobs.class, rs1.getString("ehcm_jobs_id"));
          activeJobs.setEndDate(null);
          activeJobs.setActive(true);
          OBDal.getInstance().save(activeJobs);
        }
        rs1.close();
        ps1.close();
      }

      ObjUpdateClass.setAlertStatus("DR");
      ObjUpdateClass.setProcessed(false);
      OBDal.getInstance().save(ObjUpdateClass);
      OBError result = OBErrorBuilder.buildMessage(null, "Success",
          OBMessageUtils.messageBD("Efin_BudgetPre_Submit"));
      bundle.setResult(result);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();

    } catch (Exception e) {
      log.error("error in update job title Correction:" + e);
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}
