package sa.elm.ob.hcm.services.profile;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openbravo.dal.service.OBDal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sa.elm.ob.hcm.EHCMEmpAddress;
import sa.elm.ob.hcm.EhcmDependents;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ehcmempstatusv;
import sa.elm.ob.hcm.ad_forms.employee.dao.EmployeeDAO;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.dto.profile.AddressInformationDTO;
import sa.elm.ob.hcm.dto.profile.DependentInformationDTO;
import sa.elm.ob.hcm.dto.profile.EmployeeAdditionalInformationDTO;
import sa.elm.ob.hcm.dto.profile.EmployeeProfileDTO;
import sa.elm.ob.hcm.dto.profile.PersonalInformationDTO;
import sa.elm.ob.utility.util.DateUtils;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

@Service
public class EmployeeProfileServiceImpl implements EmployeeProfileService {

  private static final String OPEN_BRAVO_DATE_FORMAT = "dd-MM-yyyy";
  private static final String OPEN_BRAVO_YEAR_DATE_FORMAT = "yyyy-MM-dd";

  @Autowired
  private EmployeeProfileDAO employeeProfileDAO;
  private Connection connection = OBDal.getInstance().getConnection();

  @Override
  public EmployeeProfileDTO getEmployeeProfileByUser(String username) {
    EhcmEmpPerInfo employeePersonalInfo = employeeProfileDAO.getEmployeeProfileByUser(username);
    // Fill the DTO's
    EmployeeProfileDTO employeeProfileDTO = new EmployeeProfileDTO();
    PersonalInformationDTO personalInformationDTO = getPersonalInformation(employeePersonalInfo);
    AddressInformationDTO addressInformationDTO = getAddressInformation(employeePersonalInfo);
    EmployeeAdditionalInformationDTO additionalInformationDTO = getAdditionalInformation(
        employeePersonalInfo);
    List<DependentInformationDTO> dependentsList = getDependentInformation(employeePersonalInfo);

    employeeProfileDTO.setBasicDetails(personalInformationDTO);
    employeeProfileDTO.setAddress(addressInformationDTO);
    employeeProfileDTO.setDependents(dependentsList);
    employeeProfileDTO.setAdditionalDetails(additionalInformationDTO);

    return employeeProfileDTO;
  }

  /**
   * Fill the employeePersonalInfo Additional information
   * 
   * @param employeePersonalInfo
   * @return
   */
  private EmployeeAdditionalInformationDTO getAdditionalInformation(
      EhcmEmpPerInfo employeePersonalInfo) {
    EmployeeDAO employeeDAO = new EmployeeDAO(connection);
    EmployeeAdditionalInformationDTO additionalInformationDTO = new EmployeeAdditionalInformationDTO();
    additionalInformationDTO.setEmployeeId(employeePersonalInfo.getSearchKey());
    additionalInformationDTO.setEmpNo(Utility.nullToEmpty(employeePersonalInfo.getSearchKey()));
    additionalInformationDTO.setEmpName(Utility.nullToEmpty(employeePersonalInfo.getName()));
    additionalInformationDTO
        .setFourthName(Utility.nullToEmpty(employeePersonalInfo.getFourthname()));
    additionalInformationDTO
        .setGradfatName(Utility.nullToEmpty(employeePersonalInfo.getGrandfathername()));
    additionalInformationDTO
        .setFamilyName(Utility.nullToEmpty(employeePersonalInfo.getFamilyname()));
    additionalInformationDTO.setFatName(Utility.nullToEmpty(employeePersonalInfo.getFathername()));
    additionalInformationDTO
        .setEmpArabicName(Utility.nullToEmpty(employeePersonalInfo.getArabicname()));
    additionalInformationDTO
        .setArbfatName(Utility.nullToEmpty(employeePersonalInfo.getArabicfatname()));
    additionalInformationDTO
        .setArbgradfatName(Utility.nullToEmpty(employeePersonalInfo.getArbgrafaname()));
    additionalInformationDTO
        .setArbfamilyName(Utility.nullToEmpty(employeePersonalInfo.getArabicfamilyname()));
    additionalInformationDTO
        .setArbfourthName(Utility.nullToEmpty(employeePersonalInfo.getArbfouname()));
    additionalInformationDTO
        .setNationalCode(Utility.nullToEmpty(employeePersonalInfo.getNationalityIdentifier()));
    additionalInformationDTO
        .setActTypeId(Utility.nullToEmpty(employeePersonalInfo.getEhcmActiontype() == null ? ""
            : employeePersonalInfo.getEhcmActiontype().getId()));
    additionalInformationDTO
        .setGradeclassId(Utility.nullToEmpty(employeePersonalInfo.getGradeClass() == null ? ""
            : employeePersonalInfo.getGradeClass().getId()));
    additionalInformationDTO.setHiredate(Utility.nullToEmpty(UtilityDAO.convertTohijriDate(DateUtils
        .convertDateToString(OPEN_BRAVO_YEAR_DATE_FORMAT, employeePersonalInfo.getHiredate()))));

    if (employeePersonalInfo.getGovhiredate() != null
        && !employeePersonalInfo.getGovhiredate().equals("")) {
      additionalInformationDTO.setGovhiredate(
          UtilityDAO.convertTohijriDate(DateUtils.convertDateToString(OPEN_BRAVO_YEAR_DATE_FORMAT,
              employeePersonalInfo.getGovhiredate())));
    } else {
      additionalInformationDTO.setGovhiredate("");
    }
    additionalInformationDTO
        .setLetterno(Utility.nullToEmpty(employeePersonalInfo.getMcsletterno()));
    additionalInformationDTO
        .setDecisionno(Utility.nullToEmpty(employeePersonalInfo.getDecisionno()));
    if (employeePersonalInfo.getMcsletterdate() != null) {
      additionalInformationDTO.setLetterdate(
          UtilityDAO.convertTohijriDate(DateUtils.convertDateToString(OPEN_BRAVO_YEAR_DATE_FORMAT,
              employeePersonalInfo.getMcsletterdate())));
    } else {
      additionalInformationDTO.setLetterdate("");
    }
    if (employeePersonalInfo.getDecisiondate() != null) {
      additionalInformationDTO.setDecisiondate(
          UtilityDAO.convertTohijriDate(DateUtils.convertDateToString(OPEN_BRAVO_YEAR_DATE_FORMAT,
              employeePersonalInfo.getDecisiondate())));
    } else {
      additionalInformationDTO.setDecisiondate("");
    }
    additionalInformationDTO
        .setTitleId(Utility.nullToEmpty(employeePersonalInfo.getEhcmTitletype() == null ? ""
            : employeePersonalInfo.getEhcmTitletype().getId()));
    additionalInformationDTO.setGender(Utility.nullToEmpty(employeePersonalInfo.getGender()));
    additionalInformationDTO.setMobno(Utility.nullToEmpty(employeePersonalInfo.getMobno()));
    additionalInformationDTO.setHomeno(Utility.nullToEmpty(employeePersonalInfo.getHomeno()));
    additionalInformationDTO.setWorkno(Utility.nullToEmpty(employeePersonalInfo.getWorkno()));
    additionalInformationDTO.setOffice(Utility.nullToEmpty(employeePersonalInfo.getOfficename()));
    additionalInformationDTO.setLocation(Utility.nullToEmpty(employeePersonalInfo.getLocation()));
    additionalInformationDTO.setBloodtype(Utility.nullToEmpty(employeePersonalInfo.getBloodtype()));
    additionalInformationDTO
        .setTownofbirth(Utility.nullToEmpty(employeePersonalInfo.getTownbirth()));
    additionalInformationDTO
        .setCountryId(Utility.nullToEmpty(employeePersonalInfo.getCountry() == null ? ""
            : employeePersonalInfo.getCountry().getId()));
    additionalInformationDTO.setCityId(Utility.nullToEmpty(
        employeePersonalInfo.getCity() == null ? "" : employeePersonalInfo.getCity().getId()));
    additionalInformationDTO
        .setMaritalstauts(Utility.nullToEmpty(employeePersonalInfo.getMarialstatus()));
    additionalInformationDTO
        .setReligionId(Utility.nullToEmpty(employeePersonalInfo.getEhcmReligion() == null ? ""
            : employeePersonalInfo.getEhcmReligion().getId()));
    additionalInformationDTO
        .setNationalId(Utility.nullToEmpty(employeePersonalInfo.getEhcmAddnationality() == null ? ""
            : employeePersonalInfo.getEhcmAddnationality().getId()));
    additionalInformationDTO.setHeight(Utility.nullToEmpty(employeePersonalInfo.getHeight()));
    additionalInformationDTO.setWeight(Utility.nullToEmpty(employeePersonalInfo.getWeight()));
    additionalInformationDTO.setEmail(Utility.nullToEmpty(employeePersonalInfo.getEmail()));

    if (employeePersonalInfo.getDob() != null) {
      additionalInformationDTO.setDob(Utility.nullToEmpty(UtilityDAO.convertTohijriDate(DateUtils
          .convertDateToString(OPEN_BRAVO_YEAR_DATE_FORMAT, employeePersonalInfo.getDob()))));
    }
    additionalInformationDTO.setCivimg(employeePersonalInfo.getCIVAdImage());
    additionalInformationDTO.setWrkimg(employeePersonalInfo.getWorkAdImage());

    if (employeePersonalInfo.getStatus().equals("C")
        || employeePersonalInfo.getStatus().equals("TE")) {
      ehcmempstatusv statusv = OBDal.getInstance().get(ehcmempstatusv.class,
          employeePersonalInfo.getId());
      additionalInformationDTO.setDecisionno(Utility.nullToEmpty(statusv.getDecisionno()));
      additionalInformationDTO.setStatus(Utility.nullToEmpty(statusv.getAlertStatus()));
      if (statusv.getStartDate() != null) {
        additionalInformationDTO.setStartdate(UtilityDAO.convertTohijriDate(
            DateUtils.convertDateToString(OPEN_BRAVO_YEAR_DATE_FORMAT, statusv.getStartDate())));
      } else {
        additionalInformationDTO.setStartdate("");
      }
      if (statusv.getTodate() != null) {
        additionalInformationDTO.setEnddate(UtilityDAO.convertTohijriDate(
            DateUtils.convertDateToString(OPEN_BRAVO_YEAR_DATE_FORMAT, statusv.getTodate())));
      } else {
        additionalInformationDTO.setEnddate("");
      }

    } else {
      additionalInformationDTO
          .setDecisionno(Utility.nullToEmpty(employeePersonalInfo.getDecisionno()));
      additionalInformationDTO.setStatus(Utility.nullToEmpty(employeePersonalInfo.getStatus()));
      additionalInformationDTO.setStartdate(Utility.nullToEmpty(
          UtilityDAO.convertTohijriDate(DateUtils.convertDateToString(OPEN_BRAVO_YEAR_DATE_FORMAT,
              employeePersonalInfo.getStartDate()))));
      if (employeePersonalInfo.getEndDate() != null) {
        additionalInformationDTO.setEnddate(UtilityDAO.convertTohijriDate(DateUtils
            .convertDateToString(OPEN_BRAVO_YEAR_DATE_FORMAT, employeePersonalInfo.getEndDate())));
      } else {
        additionalInformationDTO.setEnddate("");
      }

    }
    if (additionalInformationDTO.getStatus().equals("C")) {
      additionalInformationDTO
          .setPersonType("EX-" + employeeDAO.getPersonType(employeePersonalInfo.getClient().getId(),
              employeePersonalInfo.getSearchKey()));
    }

    return additionalInformationDTO;

  }

  /**
   * Fill the personal information
   * 
   * @param employeePersonalInfo
   * @return
   */
  private PersonalInformationDTO getPersonalInformation(EhcmEmpPerInfo employeePersonalInfo) {
    PersonalInformationDTO personalInformationDTO = new PersonalInformationDTO();

    personalInformationDTO.setFirstNameEn(employeePersonalInfo.getName());
    personalInformationDTO.setFirstNameAr(employeePersonalInfo.getArabicname());
    personalInformationDTO.setFatherNameAr(employeePersonalInfo.getArabicfatname());
    personalInformationDTO.setFatherNameEn(employeePersonalInfo.getFathername());
    personalInformationDTO.setGrandFatherNameAr(employeePersonalInfo.getArbgrafaname());
    personalInformationDTO.setGrandFatherNameEn(employeePersonalInfo.getGrandfathername());
    personalInformationDTO.setFamilyNameAr(employeePersonalInfo.getArabicfamilyname());
    personalInformationDTO.setFamilyNameEn(employeePersonalInfo.getFamilyname());
    if (null != employeePersonalInfo.getDob()) {
      personalInformationDTO.setDob(
          DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, employeePersonalInfo.getDob()));
    }
    personalInformationDTO.setTitle(employeePersonalInfo.getLookupTitle().getEnglishName());
    personalInformationDTO.setNationalId(employeePersonalInfo.getNationalityIdentifier());
    personalInformationDTO
        .setMaritalStatus(employeePersonalInfo.getLookupMaritalStatus().getEnglishName());
    personalInformationDTO.setGender(employeePersonalInfo.getLookupGender().getEnglishName());

    return personalInformationDTO;

  }

  /**
   * Get the address information
   * 
   * @param employeePersonalInfo
   * @return
   */
  private AddressInformationDTO getAddressInformation(EhcmEmpPerInfo employeePersonalInfo) {

    AddressInformationDTO addressInformationDTO = new AddressInformationDTO();

    if (null != employeePersonalInfo.getEHCMEmpAddressList()
        && employeePersonalInfo.getEHCMEmpAddressList().size() > 0) {
      EHCMEmpAddress ehcmEmpAddress = employeePersonalInfo.getEHCMEmpAddressList().get(0);// Need to
                                                                                          // check
                                                                                          // if
                                                                                          // there
                                                                                          // can be
                                                                                          // multiple
                                                                                          // addresses
      addressInformationDTO.setCountry(ehcmEmpAddress.getCountry().getName());
      // addressInformationDTO.setRegion(ehcmEmpAddress.get); Need to confirm from
      // where to get the
      // region
      addressInformationDTO.setCity(ehcmEmpAddress.getCity().getName());
      addressInformationDTO.setDistrict(ehcmEmpAddress.getSECDistrict());
      addressInformationDTO.setStreet(ehcmEmpAddress.getStreet());
      addressInformationDTO.setPostBox(ehcmEmpAddress.getPostBox());
      addressInformationDTO.setPostalCode(ehcmEmpAddress.getPostalCode());
      addressInformationDTO.setAddressLine1(ehcmEmpAddress.getAddressLine1());
      addressInformationDTO.setAddressLine2(ehcmEmpAddress.getAddressLine2());

    }

    return addressInformationDTO;
  }

  /**
   * Get the List of Dependents
   * 
   * @param employeePersonalInfo
   * @return
   */
  private List<DependentInformationDTO> getDependentInformation(
      EhcmEmpPerInfo employeePersonalInfo) {
    List<EhcmDependents> ehcmDependentsList = employeePersonalInfo.getEhcmDependentsList();

    return mapDependentInformation(ehcmDependentsList);
  }

  /**
   * Convert dependent information from Domain to DTO's
   * 
   * @param ehcmDependentsList
   * @return
   */
  private List<DependentInformationDTO> mapDependentInformation(
      List<EhcmDependents> ehcmDependentsList) {

    List<DependentInformationDTO> dependentsList = new ArrayList<DependentInformationDTO>();
    DependentInformationDTO dependentInformationDTO = null;

    for (EhcmDependents ehcmDependent : ehcmDependentsList) {

      dependentInformationDTO = new DependentInformationDTO();
      dependentInformationDTO.setRelationship(ehcmDependent.getRelationship());
      dependentInformationDTO.setFirstNameAr(ehcmDependent.getFirstName());
      dependentInformationDTO.setFatherNameAr(ehcmDependent.getFathername());
      dependentInformationDTO.setGrandFatherNameAr(ehcmDependent.getGrandfather());
      dependentInformationDTO.setFamilyNameAr(ehcmDependent.getFamily());
      dependentInformationDTO.setNationalId(ehcmDependent.getNationalidentifier());
      if (null != ehcmDependent.getDob()) {
        dependentInformationDTO
            .setDob(DateUtils.convertDateToString(OPEN_BRAVO_DATE_FORMAT, ehcmDependent.getDob()));
      }
      dependentInformationDTO.setGender(ehcmDependent.getLookupGender().getEnglishName());

      dependentsList.add(dependentInformationDTO);

    }

    return dependentsList;

  }

  @Override
  public AddressInformationDTO getEmployeeAddress(String username) {
    // Call Employee Personal Information and return the address information
    EmployeeProfileDTO employeeProfileDTO = getEmployeeProfileByUser(username);

    return employeeProfileDTO.getAddress();
  }

  /**
   * Get List of Dependents
   */
  @Override
  public List<DependentInformationDTO> getEmployeeDependents(String username) {

    EmployeeProfileDTO employeeProfileDTO = getEmployeeProfileByUser(username);

    return employeeProfileDTO.getDependents();
  }

  /**
   * 
   * @param username
   * @return the personal information
   */
  public PersonalInformationDTO getPersonalInformation(String username) {
    EmployeeProfileDTO employeeProfileDTO = getEmployeeProfileByUser(username);
    return employeeProfileDTO.getBasicDetails();
  }

}
