package sa.elm.ob.hcm.ad_callouts.dao;

import org.openbravo.dal.service.OBDal;

import sa.elm.ob.hcm.UpdateJobClassification;
import sa.elm.ob.hcm.UpdateJobs;

/**
 * 
 * @author Priyanka Ranjan on 24/04/2018
 * 
 */
// Update Job Title DAO Implement file
public class UpdateJobTitleDAOImpl implements UpdateJobTitleDAO {

  public void resetclassificationjob(String updateJobClassificationId) {
    UpdateJobClassification jobs = OBDal.getInstance().get(UpdateJobClassification.class,
        updateJobClassificationId);
    if (jobs != null) {
      for (UpdateJobs ln : jobs.getEhcmUpdateJobsList()) {
        if (ln.getEhcmJobs() != null) {
          ln.setEhcmJobs(null);
          OBDal.getInstance().save(ln);
          OBDal.getInstance().flush();
        }
      }
    }

  }

}
