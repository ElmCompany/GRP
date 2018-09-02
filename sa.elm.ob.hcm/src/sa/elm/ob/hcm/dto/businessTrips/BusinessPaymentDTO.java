package sa.elm.ob.hcm.dto.businessTrips;

import java.io.Serializable;

import sa.elm.ob.hcm.dto.GenericDTO;

public class BusinessPaymentDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -3607441924476444240L;
  private Integer paymentAmount;
  private Integer advancePercentage;
  private Integer advanceAmount;
  private Integer paymentPeriod;
  public Integer getPaymentAmount() {
    return paymentAmount;
  }
  public void setPaymentAmount(Integer paymentAmount) {
    this.paymentAmount = paymentAmount;
  }
  public Integer getAdvancePercentage() {
    return advancePercentage;
  }
  public void setAdvancePercentage(Integer advancePercentage) {
    this.advancePercentage = advancePercentage;
  }
  public Integer getAdvanceAmount() {
    return advanceAmount;
  }
  public void setAdvanceAmount(Integer advanceAmount) {
    this.advanceAmount = advanceAmount;
  }
  public Integer getPaymentPeriod() {
    return paymentPeriod;
  }
  public void setPaymentPeriod(Integer paymentPeriod) {
    this.paymentPeriod = paymentPeriod;
  }

}
