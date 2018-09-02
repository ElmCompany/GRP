package sa.elm.ob.hcm.ad_callouts.dao;

import java.util.Date;

import org.codehaus.jettison.json.JSONObject;

public interface EmpJoinWorkReqCalloutDAO {
  JSONObject getAuthorizationInfoDetails(String organizationId, Date joindate) throws Exception;
}
