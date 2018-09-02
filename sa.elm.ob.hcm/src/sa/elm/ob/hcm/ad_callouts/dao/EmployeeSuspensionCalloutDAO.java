package sa.elm.ob.hcm.ad_callouts.dao;

import java.util.Date;

import org.codehaus.jettison.json.JSONObject;

/**
 * 
 * @author anup on 21-06-2018
 *
 */
public interface EmployeeSuspensionCalloutDAO {

  /**
   * this method to fetch details from authorizationInfo tab based on employeeDepartment and date
   * 
   * @param organisationId
   * @param startDate
   * @return authorizationInfoObj
   * @throws Exception
   */
  JSONObject getAuthorizationInfoDetails(String organisationId, Date startDate) throws Exception;

}
