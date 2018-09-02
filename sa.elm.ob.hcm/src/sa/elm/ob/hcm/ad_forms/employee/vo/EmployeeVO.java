package sa.elm.ob.hcm.ad_forms.employee.vo;

import java.util.Date;

import org.openbravo.model.ad.utility.Image;

import sa.elm.ob.hcm.EhcmEmpPerInfo;

public class EmployeeVO {

  private String employeeId;
  private String categoryId;
  private String categorycode;
  private String countryId;
  private String countryName;
  private String cityId;
  private String cityName;
  private String actTypeId;
  private String actTypeValue;
  private String personType;
  private String actTypeName;
  private String nationalId;
  private String nationalCode;
  private String religionId;
  private String religionCode;
  private String status;
  private String OrgId;
  private String active;
  private String saluatation;
  private String empNo;
  private String empName;
  private String empArabicName;
  private String perStatus;
  private String hireDate;
  private String orgName;
  private String fatName;
  private String gradfatName;
  private String fourthName;
  private String familyName;
  private String arbfatName;
  private String arbgradfatName;
  private String arbfourthName;
  private String arbfamilyName;
  private String gradeclassId;
  private String startdate;
  private String enddate;
  private String hiredate;
  private String govhiredate;
  private String letterdate;
  private String decisiondate;
  private String letterno;
  private String decisionno;
  private String bloodtype;
  private String maritalstauts;
  private String mobno;
  private String workno;
  private String homeno;
  private String email;
  private String office;
  private String location;
  private String townofbirth;
  private String dob;
  private String gender;
  private String titleId;
  private String titleName;
  private String height;
  private String weight;
  private String EmployeeCategory;
  private String changereason;
  private String employmentstatus;
  private int delegationcount;
  private String cancelempId;
  private String cancelaction;
  private String cancelpersontype;
  private String shortstatus;
  private String adRefListId;
  private String marrieDate;

  public String getMarrieDate() {
    return marrieDate;
  }

  public void setMarrieDate(String marrieDate) {
    this.marrieDate = marrieDate;
  }

  public String getEmployeeStatus() {
    return employeeStatus;
  }

  public void setEmployeeStatus(String employeeStatus) {
    this.employeeStatus = employeeStatus;
  }

  private String id;
  private EhcmEmpPerInfo employee;
  private Date empHireDate;
  private String employeeStatus;

  public EhcmEmpPerInfo getEmployee() {
    return employee;
  }

  public void setEmployee(EhcmEmpPerInfo employee) {
    this.employee = employee;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getShortstatus() {
    return shortstatus;
  }

  public void setShortstatus(String shortstatus) {
    this.shortstatus = shortstatus;
  }

  public String getCancelpersontype() {
    return cancelpersontype;
  }

  public void setCancelpersontype(String cancelpersontype) {
    this.cancelpersontype = cancelpersontype;
  }

  public String getCancelaction() {
    return cancelaction;
  }

  public void setCancelaction(String cancelaction) {
    this.cancelaction = cancelaction;
  }

  public String getCancelempId() {
    return cancelempId;
  }

  public void setCancelempId(String cancelempId) {
    this.cancelempId = cancelempId;
  }

  public String getEmployeeCategory() {
    return EmployeeCategory;
  }

  public void setEmployeeCategory(String employeeCategory) {
    EmployeeCategory = employeeCategory;
  }

  private Image civimg;
  private Image wrkimg;
  private String AddressStyleId;
  private String AddressStyleName;
  private boolean value;
  private boolean result;
  private String AddressId;
  private String preEmpId;
  private String empPosition;
  private String otherdetails;
  private String position;
  private String deptname;
  private String isdefault;

  public String getIsdefault() {
    return isdefault;
  }

  public void setIsdefault(String isdefault) {
    this.isdefault = isdefault;
  }

  public String getDeptname() {
    return deptname;
  }

  public void setDeptname(String deptname) {
    this.deptname = deptname;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public String getPreEmpId() {
    return preEmpId;
  }

  public void setPreEmpId(String preEmpId) {
    this.preEmpId = preEmpId;
  }

  public String getEmpPosition() {
    return empPosition;
  }

  public void setEmpPosition(String empPosition) {
    this.empPosition = empPosition;
  }

  public String getOtherdetails() {
    return otherdetails;
  }

  public void setOtherdetails(String otherdetails) {
    this.otherdetails = otherdetails;
  }

  public boolean isResult() {
    return result;
  }

  public void setResult(boolean result) {
    this.result = result;
  }

  public String getAddressId() {
    return AddressId;
  }

  public void setAddressId(String addressId) {
    AddressId = addressId;
  }

  public String getAddressStyleId() {
    return AddressStyleId;
  }

  public void setAddressStyleId(String addressStyleId) {
    AddressStyleId = addressStyleId;
  }

  public String getAddressStyleName() {
    return AddressStyleName;
  }

  public void setAddressStyleName(String addressStyleName) {
    AddressStyleName = addressStyleName;
  }

  public String getWrkimg() {
    if (wrkimg != null) {
      return wrkimg.getId();
    } else {
      return null;
    }
  }

  public void setWrkimg(Image wrkimg) {
    this.wrkimg = wrkimg;
  }

  public String getCivimg() {
    if (civimg != null) {
      return civimg.getId();
    } else {
      return null;
    }
  }

  public void setCivimg(Image civimg) {
    this.civimg = civimg;
  }

  public String getStartdate() {
    return startdate;
  }

  public void setStartdate(String startdate) {
    this.startdate = startdate;
  }

  public String getEnddate() {
    return enddate;
  }

  public void setEnddate(String enddate) {
    this.enddate = enddate;
  }

  public String getHiredate() {
    return hiredate;
  }

  public void setHiredate(String hiredate) {
    this.hiredate = hiredate;
  }

  public String getGovhiredate() {
    return govhiredate;
  }

  public void setGovhiredate(String govhiredate) {
    this.govhiredate = govhiredate;
  }

  public String getLetterdate() {
    return letterdate;
  }

  public void setLetterdate(String letterdate) {
    this.letterdate = letterdate;
  }

  public String getDecisiondate() {
    return decisiondate;
  }

  public void setDecisiondate(String decisiondate) {
    this.decisiondate = decisiondate;
  }

  public String getLetterno() {
    return letterno;
  }

  public void setLetterno(String letterno) {
    this.letterno = letterno;
  }

  public String getDecisionno() {
    return decisionno;
  }

  public void setDecisionno(String decisionno) {
    this.decisionno = decisionno;
  }

  public String getBloodtype() {
    return bloodtype;
  }

  public void setBloodtype(String bloodtype) {
    this.bloodtype = bloodtype;
  }

  public String getMaritalstauts() {
    return maritalstauts;
  }

  public void setMaritalstauts(String maritalstauts) {
    this.maritalstauts = maritalstauts;
  }

  public String getMobno() {
    return mobno;
  }

  public void setMobno(String mobno) {
    this.mobno = mobno;
  }

  public String getWorkno() {
    return workno;
  }

  public void setWorkno(String workno) {
    this.workno = workno;
  }

  public String getHomeno() {
    return homeno;
  }

  public void setHomeno(String homeno) {
    this.homeno = homeno;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getOffice() {
    return office;
  }

  public void setOffice(String office) {
    this.office = office;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getTownofbirth() {
    return townofbirth;
  }

  public void setTownofbirth(String townofbirth) {
    this.townofbirth = townofbirth;
  }

  public String getDob() {
    return dob;
  }

  public void setDob(String dob) {
    this.dob = dob;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public String getTitleId() {
    return titleId;
  }

  public void setTitleId(String titleId) {
    this.titleId = titleId;
  }

  public String getTitleName() {
    return titleName;
  }

  public void setTitleName(String titleName) {
    this.titleName = titleName;
  }

  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  public String getWeight() {
    return weight;
  }

  public void setWeight(String weight) {
    this.weight = weight;
  }

  public String getFatName() {
    return fatName;
  }

  public void setFatName(String fatName) {
    this.fatName = fatName;
  }

  public String getGradfatName() {
    return gradfatName;
  }

  public void setGradfatName(String gradfatName) {
    this.gradfatName = gradfatName;
  }

  public String getFourthName() {
    return fourthName;
  }

  public void setFourthName(String fourthName) {
    this.fourthName = fourthName;
  }

  public String getFamilyName() {
    return familyName;
  }

  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }

  public String getArbfatName() {
    return arbfatName;
  }

  public void setArbfatName(String arbfatName) {
    this.arbfatName = arbfatName;
  }

  public String getArbgradfatName() {
    return arbgradfatName;
  }

  public void setArbgradfatName(String arbgradfatName) {
    this.arbgradfatName = arbgradfatName;
  }

  public String getArbfourthName() {
    return arbfourthName;
  }

  public void setArbfourthName(String arbfourthName) {
    this.arbfourthName = arbfourthName;
  }

  public String getArbfamilyName() {
    return arbfamilyName;
  }

  public void setArbfamilyName(String arbfamilyName) {
    this.arbfamilyName = arbfamilyName;
  }

  public String getOrgName() {
    return orgName;
  }

  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  public String getOrgId() {
    return OrgId;
  }

  public void setOrgId(String orgId) {
    OrgId = orgId;
  }

  public String getActive() {
    return active;
  }

  public void setActive(String active) {
    this.active = active;
  }

  public String getSaluatation() {
    return saluatation;
  }

  public void setSaluatation(String saluatation) {
    this.saluatation = saluatation;
  }

  public String getEmpNo() {
    return empNo;
  }

  public void setEmpNo(String empNo) {
    this.empNo = empNo;
  }

  public String getEmpName() {
    return empName;
  }

  public void setEmpName(String empName) {
    this.empName = empName;
  }

  public String getEmpArabicName() {
    return empArabicName;
  }

  public void setEmpArabicName(String empArabicName) {
    this.empArabicName = empArabicName;
  }

  public String getPerStatus() {
    return perStatus;
  }

  public void setPerStatus(String perStatus) {
    this.perStatus = perStatus;
  }

  public String getHireDate() {
    return hireDate;
  }

  public void setHireDate(String hireDate) {
    this.hireDate = hireDate;
  }

  public String getNationalId() {
    return nationalId;
  }

  public void setNationalId(String nationalId) {
    this.nationalId = nationalId;
  }

  public String getNationalCode() {
    return nationalCode;
  }

  public void setNationalCode(String nationalCode) {
    this.nationalCode = nationalCode;
  }

  public String getActTypeId() {
    return actTypeId;
  }

  public void setActTypeId(String actTypeId) {
    this.actTypeId = actTypeId;
  }

  public String getActTypeValue() {
    return actTypeValue;
  }

  public void setActTypeValue(String actTypeValue) {
    this.actTypeValue = actTypeValue;
  }

  public String getPersonType() {
    return personType;
  }

  public void setPersonType(String personType) {
    this.personType = personType;
  }

  public String getCityId() {
    return cityId;
  }

  public void setCityId(String cityId) {
    this.cityId = cityId;
  }

  public String getCityName() {
    return cityName;
  }

  public void setCityName(String cityName) {
    this.cityName = cityName;
  }

  public String getCountryId() {
    return countryId;
  }

  public void setCountryId(String countryId) {
    this.countryId = countryId;
  }

  public String getCountryName() {
    return countryName;
  }

  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }

  public String getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(String employeeId) {
    this.employeeId = employeeId;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public String getCategorycode() {
    return categorycode;
  }

  public void setCategorycode(String categorycode) {
    this.categorycode = categorycode;
  }

  public String getActTypeName() {
    return actTypeName;
  }

  public void setActTypeName(String actTypeName) {
    this.actTypeName = actTypeName;
  }

  public String getReligionId() {
    return religionId;
  }

  public void setReligionId(String religionId) {
    this.religionId = religionId;
  }

  public String getReligionCode() {
    return religionCode;
  }

  public void setReligionCode(String religionCode) {
    this.religionCode = religionCode;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getGradeclassId() {
    return gradeclassId;
  }

  public void setGradeclassId(String gradeclassId) {
    this.gradeclassId = gradeclassId;
  }

  public boolean isValue() {
    return value;
  }

  public void setValue(boolean value) {
    this.value = value;
  }

  public String getChangereason() {
    return changereason;
  }

  public String getEmploymentstatus() {
    return employmentstatus;
  }

  public int getDelegationcount() {
    return delegationcount;
  }

  public void setChangereason(String changereason) {
    this.changereason = changereason;
  }

  public void setEmploymentstatus(String employmentstatus) {
    this.employmentstatus = employmentstatus;
  }

  public void setDelegationcount(int delegationcount) {
    this.delegationcount = delegationcount;
  }

  public Date getEmpHireDate() {
    return empHireDate;
  }

  public void setEmpHireDate(Date empHireDate) {
    this.empHireDate = empHireDate;
  }

  public String getAdRefListId() {
    return adRefListId;
  }

  public void setAdRefListId(String adRefListId) {
    this.adRefListId = adRefListId;
  }
}
