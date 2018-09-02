package sa.elm.ob.hcm.dto.leave;

import java.io.Serializable;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class RejoinLeaveRequestDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 2917285381978792130L;

  private String letterNo;
  private String letterDate;
  private String absenceType;
  private String absenceReason;
  private String startDate;
  private String endDate;
  private Integer absenceDays;
  private String remarks;
  private String joinDate;
  private String decisionNumber;

  public String getJoinDate() {
    return joinDate;
  }

  public void setJoinDate(String joinDate) {
    this.joinDate = joinDate;
  }

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

  public String getAbsenceType() {
    return absenceType;
  }

  public void setAbsenceType(String absenceType) {
    this.absenceType = absenceType;
  }

  public String getAbsenceReason() {
    return absenceReason;
  }

  public void setAbsenceReason(String absenceReason) {
    this.absenceReason = absenceReason;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public Integer getAbsenceDays() {
    return absenceDays;
  }

  public void setAbsenceDays(Integer absenceDays) {
    this.absenceDays = absenceDays;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String getDecisionNumber() {
    return decisionNumber;
  }

  public void setDecisionNumber(String decisionNumber) {
    this.decisionNumber = decisionNumber;
  }

}
