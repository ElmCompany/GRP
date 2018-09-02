package sa.elm.ob.hcm.ad_callouts.dao;

import java.util.Date;

import org.codehaus.jettison.json.JSONObject;

public interface EmployeeExtendServiceCalloutDAO {
  JSONObject getAuthorizationInfoDetails(String organizationId, Date effectiveDate)
      throws Exception;

}
