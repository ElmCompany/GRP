package sa.elm.ob.hcm.selfservice.dto.tasks;

import java.io.Serializable;

public class TaskDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -4836004731752816729L;

  private String taskId;
  private String subject;
  private String requester;
  private String letterNo;
  private String requestDate;
  private String status;
  private String identifierId;

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getRequester() {
    return requester;
  }

  public void setRequester(String requester) {
    this.requester = requester;
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

  public String getIdentifierId() {
    return identifierId;
  }

  public void setIdentifierId(String identifierId) {
    this.identifierId = identifierId;
  }

}
