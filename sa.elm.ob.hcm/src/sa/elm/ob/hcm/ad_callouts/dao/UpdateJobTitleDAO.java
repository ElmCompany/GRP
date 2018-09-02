package sa.elm.ob.hcm.ad_callouts.dao;

/**
 * Interface for all Update Job Title related DB Operations
 * 
 * @author Priyanka Ranjan on 24/04/2018
 * 
 */
interface UpdateJobTitleDAO {

  /**
   * set blank in classification job field in line
   * 
   * @param updateJobClassificationId
   * @throws Exception
   */
  public void resetclassificationjob(String updateJobClassificationId) throws Exception;
}
