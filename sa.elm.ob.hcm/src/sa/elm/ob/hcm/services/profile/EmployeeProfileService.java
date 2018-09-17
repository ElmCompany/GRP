package sa.elm.ob.hcm.services.profile;

import java.util.List;

import sa.elm.ob.hcm.dto.profile.AddressInformationDTO;
import sa.elm.ob.hcm.dto.profile.DependentInformationDTO;
import sa.elm.ob.hcm.dto.profile.EmployeeProfileDTO;
import sa.elm.ob.hcm.dto.profile.PersonalInformationDTO;

public interface EmployeeProfileService {
  /**
   * Get Employee Profile by User
   * 
   * @param username
   * @return
   */
  EmployeeProfileDTO getEmployeeProfileByUser(String username);

  /**
   * Get all the dependents of the user
   * 
   * @return
   */
  List<DependentInformationDTO> getEmployeeDependents(String username);

  /**
   * Get employee Address
   * 
   * @return
   */
  AddressInformationDTO getEmployeeAddress(String username);

  /**
   * 
   * @param username
   * @return the employee personal information
   */
  PersonalInformationDTO getPersonalInformation(String username);

}
