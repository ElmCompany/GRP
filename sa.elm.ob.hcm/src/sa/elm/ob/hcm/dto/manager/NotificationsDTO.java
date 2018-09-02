package sa.elm.ob.hcm.dto.manager;

import java.io.Serializable;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class NotificationsDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -8947346705501595277L;

  private String subject;
  private String from;
  private String letterNo;
  private String requestDate;
  private String status;

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getLetterNo() {
    return letterNo;
  }

  public void setLetterNo(String letterNo) {
    this.letterNo = letterNo;
  }

  public String getRequestDate() {
    return requestDate;
  }

  public void setRequestDate(String requestDate) {
    this.requestDate = requestDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

}
