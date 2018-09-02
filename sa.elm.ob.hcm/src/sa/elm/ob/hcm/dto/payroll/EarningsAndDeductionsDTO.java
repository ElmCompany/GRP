package sa.elm.ob.hcm.dto.payroll;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class EarningsAndDeductionsDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 6244236915631316039L;

  private Map<String, BigDecimal> earnings;
  private Map<String, BigDecimal> deduction;
  private String payrollPeriod;
  private String payrollType;
  private String payrollStatus;
  private BigDecimal basicSalary;
  private BigDecimal totalEarnings;
  private BigDecimal grossSalary;
  private BigDecimal pension;
  private BigDecimal totalDeductions;
  private BigDecimal netSalary;

  public BigDecimal getBasicSalary() {
    return basicSalary;
  }

  public void setBasicSalary(BigDecimal basicSalary) {
    this.basicSalary = basicSalary;
  }

  public BigDecimal getTotalEarnings() {
    return totalEarnings;
  }

  public void setTotalEarnings(BigDecimal totalEarnings) {
    this.totalEarnings = totalEarnings;
  }

  public BigDecimal getGrossSalary() {
    return grossSalary;
  }

  public void setGrossSalary(BigDecimal grossSalary) {
    this.grossSalary = grossSalary;
  }

  public BigDecimal getPension() {
    return pension;
  }

  public void setPension(BigDecimal pension) {
    this.pension = pension;
  }

  public BigDecimal getTotalDeductions() {
    return totalDeductions;
  }

  public void setTotalDeductions(BigDecimal totalDeductions) {
    this.totalDeductions = totalDeductions;
  }

  public BigDecimal getNetSalary() {
    return netSalary;
  }

  public void setNetSalary(BigDecimal netSalary) {
    this.netSalary = netSalary;
  }

  public Map<String, BigDecimal> getEarnings() {
    return earnings;
  }

  public void setEarnings(Map<String, BigDecimal> earnings) {
    this.earnings = earnings;
  }

  public Map<String, BigDecimal> getDeduction() {
    return deduction;
  }

  public void setDeduction(Map<String, BigDecimal> deduction) {
    this.deduction = deduction;
  }

  public String getPayrollPeriod() {
    return payrollPeriod;
  }

  public void setPayrollPeriod(String payrollPeriod) {
    this.payrollPeriod = payrollPeriod;
  }

  public String getPayrollType() {
    return payrollType;
  }

  public void setPayrollType(String payrollType) {
    this.payrollType = payrollType;
  }

  public String getPayrollStatus() {
    return payrollStatus;
  }

  public void setPayrollStatus(String payrollStatus) {
    this.payrollStatus = payrollStatus;
  }

}
