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
 * @author Gopalakrishnan on 21/10/2016
 */

public class JobClassificationCorrection extends DalBaseProcess {

  /**
   * Job Classification Correction
   */
  private static final Logger log = LoggerFactory.getLogger(JobClassificationCorrection.class);

  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    String query = "";
    final String strupClassificationId = (String) bundle.getParams()
        .get("Ehcm_Job_Classification_ID").toString();
    JobClassification objjobClassfication = OBDal.getInstance().get(JobClassification.class,
        strupClassificationId);
    // add active record in lines from jobs
    log.debug("entering into Job Classification Correction ");
    try {
      OBContext.setAdminMode(true);

      // Check Jobs are have different MCS Letter Number
      query = " select job.ehcm_job_classification_id from ehcm_job_classification  job "
          + " where job.ehcm_job_classification_id ='" + strupClassificationId + "' "
          + " group by job.ehcm_job_classification_id having (select count(distinct ehcm_upjob_classification_id) from ehcm_jobs "
          + " where ehcm_job_classification_id =job.ehcm_job_classification_id) > 1";
      ps = conn.prepareStatement(query);
      rs = ps.executeQuery();
      if (rs.next()) {
        OBDal.getInstance().rollbackAndClose();
        OBError result = OBErrorBuilder.buildMessage(null, "error",
            OBMessageUtils.messageBD("EHCM_JOB_MCS_LETTER"));
        bundle.setResult(result);
        return;
      }
      rs.close();
      ps.close();
      // check position defined for jobs
      for (Jobs objJobs : objjobClassfication.getEhcmJobsList()) {
        if (objJobs.getEhcmPositionList().size() > 0) {
          OBDal.getInstance().rollbackAndClose();
          OBError result = OBErrorBuilder.buildMessage(null, "error",
              OBMessageUtils.messageBD("EHCM_JOBS_WITH_POSITION"));
          bundle.setResult(result);
          return;
        }
      }
      // all validation success . Delete entry from Update job title
      for (Jobs objJobs : objjobClassfication.getEhcmJobsList()) {
        objJobs.setEhcmUpjobClassification(null);
        objJobs.setProcessed(false);
        OBDal.getInstance().save(objJobs);
        OBDal.getInstance().flush();
        OBDal.getInstance().refresh(objJobs);
      }
      for (UpdateJobClassification objUpdateJob : objjobClassfication
          .getEhcmUpjobClassificationList()) {
        OBDal.getInstance().getConnection()
            .prepareStatement("Delete from ehcm_update_jobs where ehcm_upjob_classification_id='"
                + objUpdateJob.getId() + "'")
            .executeUpdate();
        OBDal.getInstance().getConnection()
            .prepareStatement(
                "Delete from Ehcm_Upjob_Classification where ehcm_upjob_classification_id='"
                    + objUpdateJob.getId() + "'")
            .executeUpdate();
      }
      objjobClassfication.setStatus("DR");
      objjobClassfication.setProcessed(false);
      OBError result = OBErrorBuilder.buildMessage(null, "Success",
          OBMessageUtils.messageBD("Efin_BudgetPre_Submit"));
      bundle.setResult(result);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();

    } catch (Exception e) {
      log.error("error in Job Classification Correction:" + e);
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
