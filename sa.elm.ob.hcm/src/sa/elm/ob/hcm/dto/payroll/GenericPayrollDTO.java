package sa.elm.ob.hcm.dto.payroll;

import java.io.Serializable;

import sa.elm.ob.hcm.EHCMPpmBankdetail;

public class GenericPayrollDTO implements Serializable {

  /**
   * Main DTO for payroll process. which is used other payroll DTO's.
   */
  private static final long serialVersionUID = -6453639227681337577L;

  private EmploymentGroupDTO employmentGroup;
  private BankDetailsDTO bankDetails;
  private EHCMPpmBankdetail bankdetailOB;

  public EHCMPpmBankdetail getBankdetailOB() {
    return bankdetailOB;
  }

  public void setBankdetailOB(EHCMPpmBankdetail bankdetailOB) {
    this.bankdetailOB = bankdetailOB;
  }

  public BankDetailsDTO getBankDetails() {
    return bankDetails;
  }

  public void setBankDetails(BankDetailsDTO bankDetails) {
    this.bankDetails = bankDetails;
  }

  public EmploymentGroupDTO getEmploymentGroup() {
    return employmentGroup;
  }

  public void setEmploymentGroup(EmploymentGroupDTO employmentGroup) {
    this.employmentGroup = employmentGroup;
  }

}
