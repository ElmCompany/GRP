package sa.elm.ob.hcm.ad_callouts.dao;

import java.util.Date;

import org.codehaus.jettison.json.JSONObject;

/**
 * 
 * @author anup on 21-06-2018
 *
 */
public interface EndofEmploymentCalloutDAO {

  /**
   * this method to fetch details from authorizationInfo tab based on employeeDepartment and date
   * 
   * @param organisationId
   * @param terminationDate
   * @return authorizationInfoObj
   * @throws Exception
   */
  JSONObject getAuthorizationInfoDetails(String organisationId, Date terminationDate)
      throws Exception;

}