package sa.elm.ob.hcm.selfservice.dto.approvalaction;

import java.io.Serializable;

import sa.elm.ob.hcm.dto.GenericDTO;

public class ApprovalActionDTO extends GenericDTO implements Serializable {

  private static final long serialVersionUID = -1689390637891209453L;

  /**
   * 
   */
  public String decisionrecord;
  public String requester;
  public String approver;
  public String action;
  public String description;
  public String decisionType;

  public String getDecisionrecord() {
    return decisionrecord;
  }

  public void setDecisionrecord(String decisionrecord) {
    this.decisionrecord = decisionrecord;
  }

  public String getRequester() {
    return requester;
  }

  public void setRequester(String requester) {
    this.requester = requester;
  }

  public String getApprover() {
    return approver;
  }

  public void setApprover(String approver) {
    this.approver = approver;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDecisionType() {
    return decisionType;
  }

  public void setDecisionType(String decisionType) {
    this.decisionType = decisionType;
  }

}
