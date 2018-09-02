package sa.elm.ob.hcm.dto.profile;

import java.io.Serializable;
import java.util.List;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class EmployeeProfileDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1028229235461117560L;

  private PersonalInformationDTO basicDetails;
  private AddressInformationDTO address;

  private EmployeeAdditionalInformationDTO additionalDetails;
  private List<DependentInformationDTO> dependents;

  public EmployeeAdditionalInformationDTO getAdditionalDetails() {
    return additionalDetails;
  }

  public void setAdditionalDetails(EmployeeAdditionalInformationDTO additionalDetails) {
    this.additionalDetails = additionalDetails;
  }

  public PersonalInformationDTO getBasicDetails() {
    return basicDetails;
  }

  public void setBasicDetails(PersonalInformationDTO basicDetails) {
    this.basicDetails = basicDetails;
  }

  public AddressInformationDTO getAddress() {
    return address;
  }

  public void setAddress(AddressInformationDTO address) {
    this.address = address;
  }

  public List<DependentInformationDTO> getDependents() {
    return dependents;
  }

  public void setDependents(List<DependentInformationDTO> dependents) {
    this.dependents = dependents;
  }

}
