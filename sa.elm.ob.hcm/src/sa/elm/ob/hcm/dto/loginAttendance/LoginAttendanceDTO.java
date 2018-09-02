package sa.elm.ob.hcm.dto.loginAttendance;

import java.io.Serializable;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class LoginAttendanceDTO extends GenericDTO implements Serializable {

  private static final long serialVersionUID = 8650112753700595708L;

  /**
   * 
   */
  private String userName;
  private String date;
  private String status;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getInTime() {
    return inTime;
  }

  public void setInTime(String inTime) {
    this.inTime = inTime;
  }

  public String getOutTime() {
    return outTime;
  }

  public void setOutTime(String outTime) {
    this.outTime = outTime;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  private String inTime;
  private String outTime;
  private String description;

}
