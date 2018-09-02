package sa.elm.ob.hcm.services.selfservicetransactions;

import sa.elm.ob.hcm.dto.profile.AddressInformationDTO;
import sa.elm.ob.hcm.dto.profile.DependentInformationDTO;
import sa.elm.ob.hcm.dto.profile.PersonalInformationDTO;

/**
 * This Service for serving Transaction Table which is used to save new employee requests
 * 
 *
 */
public interface SelfServiceTransactionService {

  AddressInformationDTO getAddress(String recordId);

  DependentInformationDTO getDependent(String recordId);

  PersonalInformationDTO getPersonalInformation(String recordId);

  Object getRequestDetails(String recordId);

}
