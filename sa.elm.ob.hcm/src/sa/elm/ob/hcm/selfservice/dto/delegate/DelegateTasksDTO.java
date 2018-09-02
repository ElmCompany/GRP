package sa.elm.ob.hcm.selfservice.dto.delegate;

import java.io.Serializable;

import sa.elm.ob.hcm.dto.GenericDTO;

public class DelegateTasksDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 6862524102581646406L;

  public String id;
  public String fromUsername;
  public String toUsername;
  public String fromDate;
  public String toDate;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFromUsername() {
    return fromUsername;
  }

  public void setFromUsername(String fromUsername) {
    this.fromUsername = fromUsername;
  }

  public String getToUsername() {
    return toUsername;
  }

  public void setToUsername(String toUsername) {
    this.toUsername = toUsername;
  }

  public String getFromDate() {
    return fromDate;
  }

  public void setFromDate(String fromDate) {
    this.fromDate = fromDate;
  }

  public String getToDate() {
    return toDate;
  }

  public void setToDate(String toDate) {
    this.toDate = toDate;
  }

}
