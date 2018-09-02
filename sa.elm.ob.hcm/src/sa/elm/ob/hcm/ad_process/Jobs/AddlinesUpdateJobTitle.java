package sa.elm.ob.hcm.ad_process.Jobs;

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
import sa.elm.ob.hcm.UpdateJobs;

/**
 * @author Gopalakrishnan on 13/10/2016
 */

public class AddlinesUpdateJobTitle extends DalBaseProcess {

  /**
   * Update Job Title Add Line Process
   */
  private static final Logger log = LoggerFactory.getLogger(AddlinesUpdateJobTitle.class);

  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    final String strupClassificationId = (String) bundle.getParams()
        .get("Ehcm_Upjob_Classification_ID").toString();
    UpdateJobClassification ObjUpdateClass = OBDal.getInstance().get(UpdateJobClassification.class,
        strupClassificationId);
    JobClassification objjobClassfication = OBDal.getInstance().get(JobClassification.class,
        ObjUpdateClass.getEhcmJobClassification().getId());
    // add active record in lines from jobs
    log.debug("entering into update job title add lines Process");
    try {
      OBContext.setAdminMode(true);
      if (!objjobClassfication.getClassificationCode()
          .equals(ObjUpdateClass.getClassificationCode())) {
        OBQuery<Jobs> objJobQuery = OBDal.getInstance().createQuery(Jobs.class,
            "as e where e.ehcmJobClassification.id='" + objjobClassfication.getId()
                + "' and e.active='Y' ");
        if (objJobQuery.list().size() > 0) {
          for (Jobs objJobs : objJobQuery.list()) {
            UpdateJobs objUpdateJobs = OBProvider.getInstance().get(UpdateJobs.class);
            objUpdateJobs.setClient(objJobs.getClient());
            objUpdateJobs.setOrganization(objJobs.getOrganization());
            objUpdateJobs.setJobCode(
                ObjUpdateClass.getClassificationCode().concat(objJobs.getGrade().getSearchKey()));
            objUpdateJobs.setEhcmUpjobClassification(ObjUpdateClass);
            objUpdateJobs.setActive(objJobs.isActive());
            objUpdateJobs.setManual(false);
            objUpdateJobs.setEndDate(objJobs.getEndDate());
            objUpdateJobs.setGrade(objJobs.getGrade());
            objUpdateJobs.setProcessed(objJobs.isProcessed());
            objUpdateJobs.setStartDate(ObjUpdateClass.getClassificationDate());
            objUpdateJobs.setTitle(objJobs.getJOBTitle());
            OBDal.getInstance().save(objUpdateJobs);
          }
        }

      }
      OBDal.getInstance().save(ObjUpdateClass);
      OBError result = OBErrorBuilder.buildMessage(null, "Success",
          OBMessageUtils.messageBD("Efin_BudgetPre_Submit"));
      bundle.setResult(result);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();

    } catch (Exception e) {
      log.error("error in AddlinesUpdateJobTitle:" + e);
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
