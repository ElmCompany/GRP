package sa.elm.ob.hcm.ad_reports.positionTransaction;

public class PositionTransactionsDetailsVO {

  private String gradeId;
  private String gradeName;
  private String orgId;
  private String orgName;
  private String positionId;
  private String jobNo;

  public String getPositionId() {
    return positionId;
  }

  public void setPositionId(String positionId) {
    this.positionId = positionId;
  }

  public String getJobNo() {
    return jobNo;
  }

  public void setJobNo(String jobNo) {
    this.jobNo = jobNo;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getOrgName() {
    return orgName;
  }

  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  public String getGradeId() {
    return gradeId;
  }

  public void setGradeId(String gradeId) {
    this.gradeId = gradeId;
  }

  public String getGradeName() {
    return gradeName;
  }

  public void setGradeName(String gradeName) {
    this.gradeName = gradeName;
  }

}
