package sa.elm.ob.hcm.dto.profile;

import java.io.Serializable;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class PersonalInformationDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -5798683563239006091L;

  private String firstNameAr;
  private String firstNameEn;
  private String fatherNameEn;
  private String fatherNameAr;
  private String grandFatherNameEn;
  private String grandFatherNameAr;
  private String familyNameAr;
  private String familyNameEn;
  private String nationalId;
  private String dob;
  private String maritalStatus;
  private String title;
  private String gender;

  public String getFirstNameAr() {
    return firstNameAr;
  }

  public void setFirstNameAr(String firstNameAr) {
    this.firstNameAr = firstNameAr;
  }

  public String getFirstNameEn() {
    return firstNameEn;
  }

  public void setFirstNameEn(String firstNameEn) {
    this.firstNameEn = firstNameEn;
  }

  public String getFatherNameEn() {
    return fatherNameEn;
  }

  public void setFatherNameEn(String fatherNameEn) {
    this.fatherNameEn = fatherNameEn;
  }

  public String getFatherNameAr() {
    return fatherNameAr;
  }

  public void setFatherNameAr(String fatherNameAr) {
    this.fatherNameAr = fatherNameAr;
  }

  public String getGrandFatherNameEn() {
    return grandFatherNameEn;
  }

  public void setGrandFatherNameEn(String grandFatherNameEn) {
    this.grandFatherNameEn = grandFatherNameEn;
  }

  public String getGrandFatherNameAr() {
    return grandFatherNameAr;
  }

  public void setGrandFatherNameAr(String grandFatherNameAr) {
    this.grandFatherNameAr = grandFatherNameAr;
  }

  public String getFamilyNameAr() {
    return familyNameAr;
  }

  public void setFamilyNameAr(String familyNameAr) {
    this.familyNameAr = familyNameAr;
  }

  public String getFamilyNameEn() {
    return familyNameEn;
  }

  public void setFamilyNameEn(String familyNameEn) {
    this.familyNameEn = familyNameEn;
  }

  public String getNationalId() {
    return nationalId;
  }

  public void setNationalId(String nationalId) {
    this.nationalId = nationalId;
  }

  public String getDob() {
    return dob;
  }

  public void setDob(String dob) {
    this.dob = dob;
  }

  public String getMaritalStatus() {
    return maritalStatus;
  }

  public void setMaritalStatus(String maritalStatus) {
    this.maritalStatus = maritalStatus;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

}
