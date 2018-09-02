package sa.elm.ob.hcm.ad_process.Jobs;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
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
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.UpdateJobClassification;

/**
 * @author Gopalakrishnan on 11/10/2016
 */

public class JobClassificationComplete extends DalBaseProcess {

  /**
   * Jobs Classification Completion Process
   */
  private static final Logger log = LoggerFactory.getLogger(JobClassificationComplete.class);

  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    final String strClassificationId = (String) bundle.getParams().get("Ehcm_Job_Classification_ID")
        .toString();
    JobClassification objClassfication = OBDal.getInstance().get(JobClassification.class,
        strClassificationId);
    log.debug("entering into JobClassificationComplete");
    try {
      OBContext.setAdminMode(true);
      if (objClassfication.getEhcmJobsList().size() == 0) {
        OBError result = OBErrorBuilder.buildMessage(null, "error",
            OBMessageUtils.messageBD("EHCM_NO_LINES"));
        bundle.setResult(result);
        return;
      }
      // check mcs letter number exists in update job classification
      OBQuery<UpdateJobClassification> updatejobsQuery = OBDal.getInstance().createQuery(
          UpdateJobClassification.class,
          "as e where e.mCSLetterNo='" + objClassfication.getMcsletterNo() + "' and e.client.id='"
              + objClassfication.getClient().getId() + "' ");
      if (updatejobsQuery.list().size() > 0) {
        OBError result = OBErrorBuilder.buildMessage(null, "error",
            OBMessageUtils.messageBD("EHCM_McsNo_Update"));
        bundle.setResult(result);
        return;
      }
      // update line status
      for (Jobs jobs : objClassfication.getEhcmJobsList()) {
        jobs.setProcessed(true);
        OBDal.getInstance().save(jobs);
      }
      // make entry in Update Job Title
      UpdateJobClassification objUpdateClass = OBProvider.getInstance()
          .get(UpdateJobClassification.class);
      objUpdateClass.setClient(objClassfication.getClient());
      objUpdateClass.setOrganization(objClassfication.getOrganization());
      objUpdateClass.setAlertStatus("CO");
      objUpdateClass.setClassificationCode(objClassfication.getClassificationCode());
      objUpdateClass.setClassificationDate(objClassfication.getClassificationDate());
      objUpdateClass.setDescription(objClassfication.getDescription());
      objUpdateClass.setCorrection(false);
      objUpdateClass.setEhcmJobClassification(objClassfication);
      objUpdateClass.setActive(objClassfication.isActive());
      objUpdateClass.setEndDate(objClassfication.getEndDate());
      objUpdateClass.setGroupSequenceCode(objClassfication.getGroupSeqCode());
      objUpdateClass.setGroupSequenceName(objClassfication.getGroupSeqName());
      objUpdateClass.setJobGroup(objClassfication.getEhcmJobGroup());
      objUpdateClass.setMainGroupCode(objClassfication.getMainGroupCode());
      objUpdateClass.setMainGroupName(objClassfication.getMainGroupName());
      objUpdateClass.setMCSLetterDate(objClassfication.getMcsletterDate());
      objUpdateClass.setMCSLetterNo(objClassfication.getMcsletterNo());
      objUpdateClass.setProcessed(objClassfication.isProcessed());
      objUpdateClass
          .setStartDate(new Date(objClassfication.getStartDate().getTime() + 1 * 24 * 3600 * 1000));
      objUpdateClass.setSubGroupCode(objClassfication.getSUBGroupCode());
      objUpdateClass.setSubGroupName(objClassfication.getSUBGroupName());
      OBDal.getInstance().save(objUpdateClass);
      // Make Entry in update jobs
      for (Jobs objJobs : objClassfication.getEhcmJobsList()) {
        objJobs.setEhcmUpjobClassification(objUpdateClass);
        OBDal.getInstance().save(objJobs);
      }
      objClassfication.setProcessed(true);
      objClassfication.setStatus("CO");
      OBDal.getInstance().save(objClassfication);
      OBError result = OBErrorBuilder.buildMessage(null, "Success",
          OBMessageUtils.messageBD("Efin_BudgetPre_Submit"));
      bundle.setResult(result);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();

    } catch (Exception e) {
      log.error("error in JobClassificationComplete:" + e);
      OBDal.getInstance().rollbackAndClose();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}
