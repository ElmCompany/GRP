package sa.elm.ob.hcm.dto.payroll;

import java.io.Serializable;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class EmployeeInformationDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -6023736031922941495L;

  private String empNumber;
  private String empName;
  private String empType;
  private String hireDate;
  private String department;
  private String section;
  private String grade;
  private String jobTitle;
  private String assignedDept;

  public String getHireDate() {
    return hireDate;
  }

  public void setHireDate(String hireDate) {
    this.hireDate = hireDate;
  }

  public String getEmpNumber() {
    return empNumber;
  }

  public void setEmpNumber(String empNumber) {
    this.empNumber = empNumber;
  }

  public String getEmpName() {
    return empName;
  }

  public void setEmpName(String empName) {
    this.empName = empName;
  }

  public String getEmpType() {
    return empType;
  }

  public void setEmpType(String empType) {
    this.empType = empType;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public String getGrade() {
    return grade;
  }

  public void setGrade(String grade) {
    this.grade = grade;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public String getAssignedDept() {
    return assignedDept;
  }

  public void setAssignedDept(String assignedDept) {
    this.assignedDept = assignedDept;
  }

}
