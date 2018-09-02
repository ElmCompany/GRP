package sa.elm.ob.hcm.dto.employment;

import java.io.Serializable;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class ViewEmplInfoDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -1982223435403973301L;

  private String reason;
  private String startDate;
  private String endDate;
  private String decisionNo;
  private String department;
  private String grade;
  private String jobNo;
  private String step;
  private String jobTitle;
  private String status;

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
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

  public String getDecisionNo() {
    return decisionNo;
  }

  public void setDecisionNo(String decisionNo) {
    this.decisionNo = decisionNo;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getGrade() {
    return grade;
  }

  public void setGrade(String grade) {
    this.grade = grade;
  }

  public String getJobNo() {
    return jobNo;
  }

  public void setJobNo(String jobNo) {
    this.jobNo = jobNo;
  }

  public String getStep() {
    return step;
  }

  public void setStep(String step) {
    this.step = step;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

}
