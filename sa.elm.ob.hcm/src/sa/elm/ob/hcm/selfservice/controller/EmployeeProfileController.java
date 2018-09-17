package sa.elm.ob.hcm.selfservice.controller;

import java.io.IOException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import sa.elm.ob.hcm.dto.profile.AddressInformationDTO;
import sa.elm.ob.hcm.dto.profile.ContactInformationDTO;
import sa.elm.ob.hcm.dto.profile.DependentInformationDTO;
import sa.elm.ob.hcm.dto.profile.EmployeeAdditionalInformationDTO;
import sa.elm.ob.hcm.dto.profile.EmployeeProfileDTO;
import sa.elm.ob.hcm.dto.profile.PersonalInformationDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.services.profile.EmployeeProfileService;
import sa.elm.ob.hcm.services.profile.EmployeeProfileUpdateService;

/**
 * Web Controller for Employee Profile
 *
 */
@RestController
@RequestMapping("openerp/hr/profile")
public class EmployeeProfileController {

  @Autowired
  private EmployeeProfileService employeeProfileService;

  @Autowired
  private EmployeeProfileUpdateService employeeProfileUpdateService;

  @RequestMapping(value = "/{username}", method = RequestMethod.GET)
  public ResponseEntity<EmployeeProfileDTO> getEmployeeProfileByUser(
      @PathVariable("username") String username) {
    EmployeeProfileDTO employeeProfile = employeeProfileService.getEmployeeProfileByUser(username);

    return new ResponseEntity<EmployeeProfileDTO>(employeeProfile, HttpStatus.OK);
  }

  @RequestMapping(value = "/dependents/{username}", method = RequestMethod.GET)
  public ResponseEntity<List<DependentInformationDTO>> getEmployeeDependents(
      @PathVariable("username") String username) {

    List<DependentInformationDTO> employeeDependents = employeeProfileService
        .getEmployeeDependents(username);

    return new ResponseEntity<List<DependentInformationDTO>>(employeeDependents, HttpStatus.OK);
  }

  @RequestMapping(value = "/address/{username}", method = RequestMethod.GET)
  public ResponseEntity<AddressInformationDTO> getEmployeeAddress(
      @PathVariable("username") String username) {

    AddressInformationDTO employeeAddress = employeeProfileService.getEmployeeAddress(username);

    return new ResponseEntity<AddressInformationDTO>(employeeAddress, HttpStatus.OK);
  }

  @RequestMapping(value = "/contactInfo/{username}", method = RequestMethod.GET)
  public ResponseEntity<ContactInformationDTO> getContactInformation(
      @PathVariable String username) {

    EmployeeProfileDTO employeeProfile = employeeProfileService.getEmployeeProfileByUser(username);

    ContactInformationDTO contactInfo = new ContactInformationDTO();
    contactInfo.setEmail(employeeProfile.getAdditionalDetails().getEmail());
    contactInfo.setHomeNo(employeeProfile.getAdditionalDetails().getHomeno());
    contactInfo.setMobileNo(employeeProfile.getAdditionalDetails().getMobno());
    contactInfo.setWorkNo(employeeProfile.getAdditionalDetails().getWorkno());

    return new ResponseEntity<ContactInformationDTO>(contactInfo, HttpStatus.OK);
  }

  @RequestMapping(value = "/{username}", method = RequestMethod.POST)
  public ResponseEntity<Boolean> updateEmployeeProfile(@PathVariable String username,
      @RequestBody PersonalInformationDTO pesonalInformationDTO,
      @RequestBody AddressInformationDTO addressInformationDTO,
      @RequestBody DependentInformationDTO dependentInformationDTO)
      throws BusinessException, SystemException {

    Boolean updateEmployeeProfile = employeeProfileUpdateService.updateEmployeeProfileWithWorkflow(
        username, pesonalInformationDTO, addressInformationDTO, dependentInformationDTO);

    return new ResponseEntity<Boolean>(updateEmployeeProfile, HttpStatus.OK);
  }

  @RequestMapping(value = "/dependent/{username}", method = RequestMethod.PUT)
  public ResponseEntity<Boolean> updateEmployeeDependent(@PathVariable String username,
      @RequestBody DependentInformationDTO dependentInformationDTO)
      throws BusinessException, SystemException {
    Boolean updateEmpDep = employeeProfileUpdateService
        .updateEmployeeDependentWithWorkFlow(username, dependentInformationDTO);
    return new ResponseEntity<Boolean>(updateEmpDep, HttpStatus.OK);
  }

  @RequestMapping(value = "/address/{username}", method = RequestMethod.PUT)
  public ResponseEntity<Boolean> updateEmployeeAddress(@PathVariable String username,
      @RequestBody AddressInformationDTO addressInformationDTO)
      throws BusinessException, SystemException {
    Boolean updateEmpAddress = employeeProfileUpdateService
        .updateEmployeeAddressWithWorkFlow(username, addressInformationDTO);
    return new ResponseEntity<Boolean>(updateEmpAddress, HttpStatus.OK);
  }

  @RequestMapping(value = "/personal/{username}", method = RequestMethod.PUT)
  public ResponseEntity<Boolean> updatePersonalInformation(@PathVariable String username,
      @RequestBody PersonalInformationDTO personalInformationDTO)
      throws BusinessException, SystemException {
    Boolean updateEmpPersonalInfo = employeeProfileUpdateService
        .updatePersonalInformationWithWorkFlow(username, personalInformationDTO);
    return new ResponseEntity<Boolean>(updateEmpPersonalInfo, HttpStatus.OK);
  }

  @RequestMapping(value = "/dependent/{username}", method = RequestMethod.POST)
  public ResponseEntity<Boolean> addDependent(@PathVariable String username,
      @RequestBody DependentInformationDTO dependentInformationDTO)
      throws BusinessException, SystemException {
    Boolean addDependent = employeeProfileUpdateService.addDependentWithWorkflow(username,
        dependentInformationDTO);
    return new ResponseEntity<Boolean>(addDependent, HttpStatus.OK);
  }

  @RequestMapping(value = "/{username}/dependent/{nationalId}", method = RequestMethod.DELETE)
  public ResponseEntity<Boolean> removeDependent(@PathVariable String username,
      @PathVariable String nationalId) throws BusinessException, SystemException {
    Boolean removeDependent = employeeProfileUpdateService.removeDependentWithWorkflow(username,
        nationalId);
    return new ResponseEntity<Boolean>(removeDependent, HttpStatus.OK);
  }

  @RequestMapping(value = "/contactInfo/{username}", method = RequestMethod.PUT)
  public ResponseEntity<Boolean> updateContactInformation(@PathVariable String username,
      @RequestBody ContactInformationDTO empContactInfo) throws BusinessException, SystemException {

    EmployeeAdditionalInformationDTO additionalInfo = employeeProfileService
        .getEmployeeProfileByUser(username).getAdditionalDetails();
    additionalInfo.setEmail(empContactInfo.getEmail());
    additionalInfo.setMobno(empContactInfo.getMobileNo());
    additionalInfo.setHomeno(empContactInfo.getHomeNo());
    additionalInfo.setWorkno(empContactInfo.getWorkNo());

    Boolean empContact = employeeProfileUpdateService.updateContactInformation(username,
        additionalInfo);
    return new ResponseEntity<Boolean>(empContact, HttpStatus.OK);
  }

  @RequestMapping(value = "/{username}/dependent/{nationalNo}", method = RequestMethod.GET)
  public ResponseEntity<DependentInformationDTO> getDependentByNationalId(
      @PathVariable("username") String username, @PathVariable String nationalNo)
      throws SystemException, BusinessException {

    DependentInformationDTO dependent = employeeProfileUpdateService
        .getDependentByNationalId(username, nationalNo);

    return new ResponseEntity<DependentInformationDTO>(dependent, HttpStatus.OK);
  }

  @RequestMapping(value = "/photo/{username}", method = RequestMethod.PUT)
  public ResponseEntity<Boolean> updateProfilePhoto(@PathVariable String username,
      @RequestParam("avatar") MultipartFile photo) throws SystemException, BusinessException {

    Boolean updatePhoto = null;
    try {
      updatePhoto = employeeProfileUpdateService.updateProfilePhoto(username,
          Base64.encodeBase64(photo.getBytes()).toString());
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new ResponseEntity<Boolean>(updatePhoto, HttpStatus.OK);
  }

}
