package sa.elm.ob.hcm.dto.payroll;

import java.io.Serializable;

import sa.elm.ob.hcm.EhcmDisciplineAction;
import sa.elm.ob.hcm.EhcmEmpPerInfo;

public class EmploymentGroupDTO implements Serializable {

  /**
   * Used to get Employment Info.
   */
  private static final long serialVersionUID = 2718259636459768958L;

  private String employmentGrpId;
  private String employeeId;
  private String startDate;
  private String endDate;
  private String payDefId;
  private String disciplineId;
  private String payrollLineId;
  private EhcmEmpPerInfo EmployeeInfo;
  private EhcmDisciplineAction disciplineAction;

  public String getPayrollLineId() {
    return payrollLineId;
  }

  public void setPayrollLineId(String payrollLineId) {
    this.payrollLineId = payrollLineId;
  }

  public String getDisciplineId() {
    return disciplineId;
  }

  public void setDisciplineId(String disciplineId) {
    this.disciplineId = disciplineId;
  }

  public EhcmDisciplineAction getDisciplineAction() {
    return disciplineAction;
  }

  public void setDisciplineAction(EhcmDisciplineAction disciplineAction) {
    this.disciplineAction = disciplineAction;
  }

  public EhcmEmpPerInfo getEmployeeInfo() {
    return EmployeeInfo;
  }

  public void setEmployeeInfo(EhcmEmpPerInfo employeeInfo) {
    EmployeeInfo = employeeInfo;
  }

  public String getPayDefId() {
    return payDefId;
  }

  public void setPayDefId(String payDefId) {
    this.payDefId = payDefId;
  }

  public String getEmploymentGrpId() {
    return employmentGrpId;
  }

  public void setEmploymentGrpId(String employmentGrpId) {
    this.employmentGrpId = employmentGrpId;
  }

  public String getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(String employeeId) {
    this.employeeId = employeeId;
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

}
