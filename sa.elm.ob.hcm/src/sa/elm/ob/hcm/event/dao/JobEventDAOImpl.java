package sa.elm.ob.hcm.event.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.UpdateJobs;

/**
 * 
 * @author Priyanka Ranjan on 23/04/2018
 * 
 */
// Job Event DAO Implement file
public class JobEventDAOImpl implements JobEventDAO {
  private static Logger LOG = Logger.getLogger(JobEventDAOImpl.class);

  public boolean checkJobCodeandJobTitleCombExist(String classification, String jobcode,
      String jobtitle, String clientId) {
    List<Jobs> ls = new ArrayList<Jobs>();
    try {
      OBQuery<Jobs> jobClass = OBDal.getInstance().createQuery(Jobs.class,
          "as e where  e.ehcmJobClassification.id=:classificationId and e.jobCode=:jobCode and e.jOBTitle=:jobTitle and e.client.id=:clientId and e.active = 'Y'");
      jobClass.setNamedParameter("classificationId", classification);
      jobClass.setNamedParameter("jobCode", jobcode);
      jobClass.setNamedParameter("jobTitle", jobtitle);
      jobClass.setNamedParameter("clientId", clientId);

      ls = jobClass.list();
      if (ls.size() > 0) {
        return true;
      }
      // return true;
    } catch (Exception e) {
      LOG.error("error while checkJobandJobTitleCombExist", e);
      return false;
    }
    return false;
  }

  public boolean checkUpJobCodeandUpJobTitleCombExist(String upjobclassification, String jobcode,
      String jobtitle, String clientId) {
    List<UpdateJobs> ls = new ArrayList<UpdateJobs>();
    try {
      OBQuery<UpdateJobs> jobClass = OBDal.getInstance().createQuery(UpdateJobs.class,
          "as e where  e.ehcmUpjobClassification.id=:upjobclassificationId and e.jobCode=:jobCode and e.title=:jobTitle and e.client.id=:clientId and e.active = 'Y'");
      jobClass.setNamedParameter("upjobclassificationId", upjobclassification);
      jobClass.setNamedParameter("jobCode", jobcode);
      jobClass.setNamedParameter("jobTitle", jobtitle);
      jobClass.setNamedParameter("clientId", clientId);
      LOG.debug("jobClass : " + jobClass.getWhereAndOrderBy());

      ls = jobClass.list();
      if (ls.size() > 0) {
        return true;
      }
      // return true;
    } catch (Exception e) {
      LOG.error("error while checkUpJobCodeandUpJobTitleCombExist", e);
      return false;
    }
    return false;
  }

  public boolean checkJobAssociatedinPosition(String jobId, String clientId) {
    List<EhcmPosition> ls = new ArrayList<EhcmPosition>();
    try {
      OBQuery<EhcmPosition> position = OBDal.getInstance().createQuery(EhcmPosition.class,
          "as e where  e.ehcmJobs.id=:ehcmJobId and issued='Y' and e.client.id=:clientId ");
      position.setNamedParameter("ehcmJobId", jobId);
      position.setNamedParameter("clientId", clientId);
      LOG.debug("position : " + position.getWhereAndOrderBy());

      ls = position.list();
      if (ls.size() > 0) {
        return true;
      }
      // return true;
    } catch (Exception e) {
      LOG.error("error while checkJobAssociatedinPosition", e);
      return false;
    }
    return false;
  }

}
