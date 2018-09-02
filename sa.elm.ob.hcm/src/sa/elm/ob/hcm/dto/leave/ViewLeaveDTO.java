package sa.elm.ob.hcm.dto.leave;

import java.io.Serializable;
import java.math.BigDecimal;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class ViewLeaveDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -2268480274555879756L;

  private String absenceType;
  private String status;
  private String startDate;
  private String endDate;
  private BigDecimal period;
  private String requestDate;
  private String pendingUser;

  public String getAbsenceType() {
    return absenceType;
  }

  public void setAbsenceType(String absenceType) {
    this.absenceType = absenceType;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
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

  public BigDecimal getPeriod() {
    return period;
  }

  public void setPeriod(BigDecimal period) {
    this.period = period;
  }

  public String getRequestDate() {
    return requestDate;
  }

  public void setRequestDate(String requestDate) {
    this.requestDate = requestDate;
  }

  public String getPendingUser() {
    return pendingUser;
  }

  public void setPendingUser(String pendingUser) {
    this.pendingUser = pendingUser;
  }

}
