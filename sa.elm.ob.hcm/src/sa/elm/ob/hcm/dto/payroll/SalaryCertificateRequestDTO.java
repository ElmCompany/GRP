package sa.elm.ob.hcm.dto.payroll;

import java.io.Serializable;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class SalaryCertificateRequestDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1122131458011939247L;

  private String letterNo;
  private String letterDate;
  private String certificateType;
  private String language;
  private String noOfCopies;
  private Boolean includeSalary;
  private String remarks;

  public String getLetterNo() {
    return letterNo;
  }

  public void setLetterNo(String letterNo) {
    this.letterNo = letterNo;
  }

  public String getLetterDate() {
    return letterDate;
  }

  public void setLetterDate(String letterDate) {
    this.letterDate = letterDate;
  }

  public String getCertificateType() {
    return certificateType;
  }

  public void setCertificateType(String certificateType) {
    this.certificateType = certificateType;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getNoOfCopies() {
    return noOfCopies;
  }

  public void setNoOfCopies(String noOfCopies) {
    this.noOfCopies = noOfCopies;
  }

  public Boolean isIncludeSalary() {
    return includeSalary;
  }

  public void setIncludeSalary(Boolean includeSalary) {
    this.includeSalary = includeSalary;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

}
