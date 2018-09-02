package sa.elm.ob.hcm.dto.leave;

import java.io.Serializable;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class ViewLeaveAccrualDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 2447323922668045405L;

  private String empNo;
  private String empName;
  private String absenceType;
  private String startDate;
  private String endDate;
  private Integer leaves;
  private Integer balance;
  private String originalDecNo;

  public String getOriginalDecNo() {
    return originalDecNo;
  }

  public void setOriginalDecNo(String originalDecNo) {
    this.originalDecNo = originalDecNo;
  }

  public String getEmpName() {
    return empName;
  }

  public void setEmpName(String empName) {
    this.empName = empName;
  }

  public String getEmpNo() {
    return empNo;
  }

  public void setEmpNo(String empNo) {
    this.empNo = empNo;
  }

  public String getAbsenceType() {
    return absenceType;
  }

  public void setAbsenceType(String absenceType) {
    this.absenceType = absenceType;
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

  public Integer getLeaves() {
    return leaves;
  }

  public void setLeaves(Integer leaves) {
    this.leaves = leaves;
  }

  public Integer getBalance() {
    return balance;
  }

  public void setBalance(Integer balance) {
    this.balance = balance;
  }

}
