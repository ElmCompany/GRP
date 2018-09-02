package sa.elm.ob.hcm.dto.manager;

import java.io.Serializable;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class EmpInfoManagerDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 55441505325598768L;

  private String empNo;
  private String empName;
  private String hireDate;
  private String grade;
  private String netSalary;
  private String status;

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

  public String getHireDate() {
    return hireDate;
  }

  public void setHireDate(String hireDate) {
    this.hireDate = hireDate;
  }

  public String getGrade() {
    return grade;
  }

  public void setGrade(String grade) {
    this.grade = grade;
  }

  public String getNetSalary() {
    return netSalary;
  }

  public void setNetSalary(String netSalary) {
    this.netSalary = netSalary;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

}
