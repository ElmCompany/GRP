package sa.elm.ob.hcm.event.dao;

/**
 * Interface for all Job Event related DB Operations
 * 
 * @author Priyanka Ranjan on 23/04/2018
 * 
 */
public interface JobEventDAO {

  /**
   * check job code and job title combination already exist or not in Job Classification & Jobs
   * 
   * @param classification
   * @param jobcode
   * @param jobtitle
   * @param clientId
   * @return
   * @throws Exception
   */
  boolean checkJobCodeandJobTitleCombExist(String classification, String jobcode, String jobtitle,
      String clientId) throws Exception;

  /**
   * check job code and job title combination already exist or not in Update Job Title
   * 
   * @param upjobclassification
   * @param jobcode
   * @param jobtitle
   * @param clientId
   * @return
   * @throws Exception
   */
  boolean checkUpJobCodeandUpJobTitleCombExist(String upjobclassification, String jobcode,
      String jobtitle, String clientId) throws Exception;

  /**
   * check job is associated in position with issued
   * 
   * @param jobId
   * @param clientId
   * @return
   * @throws Exception
   */
  boolean checkJobAssociatedinPosition(String jobId, String clientId) throws Exception;
}
