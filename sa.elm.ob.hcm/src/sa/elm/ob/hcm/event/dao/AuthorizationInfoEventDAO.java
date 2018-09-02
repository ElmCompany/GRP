package sa.elm.ob.hcm.event.dao;

import sa.elm.ob.hcm.EHCMAuthorizationInfo;

public interface AuthorizationInfoEventDAO {
  boolean dateOverLapForEndDate(EHCMAuthorizationInfo authorisedinfo) throws Exception;

  boolean checkAuthorizationDetailUsedInRecords(EHCMAuthorizationInfo authorisedinfo)
      throws Exception;
}
