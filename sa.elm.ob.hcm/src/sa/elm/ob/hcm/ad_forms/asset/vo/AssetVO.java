package sa.elm.ob.hcm.ad_forms.asset.vo;

import java.math.BigDecimal;

public class AssetVO {

  private String assetId;
  private String personid;
  private String assetname;
  private String startdate;
  private String enddate;
  private String letterdate;
  private String letterno;
  private String decisionno;
  private String description;
  private BigDecimal Balance;
  private String status;
  private String flag;

  public BigDecimal getBalance() {
    return Balance;
  }

  public void setBalance(BigDecimal balance) {
    Balance = balance;
  }

  public String getAssetId() {
    return assetId;
  }

  public void setAssetId(String assetId) {
    this.assetId = assetId;
  }

  public String getPersonid() {
    return personid;
  }

  public void setPersonid(String personid) {
    this.personid = personid;
  }

  public String getAssetname() {
    return assetname;
  }

  public void setAssetname(String assetname) {
    this.assetname = assetname;
  }

  public String getStartdate() {
    return startdate;
  }

  public void setStartdate(String startdate) {
    this.startdate = startdate;
  }

  public String getEnddate() {
    return enddate;
  }

  public void setEnddate(String enddate) {
    this.enddate = enddate;
  }

  public String getLetterdate() {
    return letterdate;
  }

  public void setLetterdate(String letterdate) {
    this.letterdate = letterdate;
  }

  public String getLetterno() {
    return letterno;
  }

  public void setLetterno(String letterno) {
    this.letterno = letterno;
  }

  public String getDecisionno() {
    return decisionno;
  }

  public void setDecisionno(String decisionno) {
    this.decisionno = decisionno;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
  
  public String getFlag() {
    return flag;
  }

  public void setFlag(String flag) {
    this.flag = flag;
  }

}
