package sa.elm.ob.hcm.dto.payroll;

import java.io.Serializable;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class PaySlipDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -5816847325017880128L;

  private EmployeeInformationDTO empInfo;
  private EarningsAndDeductionsDTO salaryDetails;

  public EmployeeInformationDTO getEmpInfo() {
    return empInfo;
  }

  public void setEmpInfo(EmployeeInformationDTO empInfo) {
    this.empInfo = empInfo;
  }

  public EarningsAndDeductionsDTO getSalaryDetails() {
    return salaryDetails;
  }

  public void setSalaryDetails(EarningsAndDeductionsDTO salaryDetails) {
    this.salaryDetails = salaryDetails;
  }
}
