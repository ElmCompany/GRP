package sa.elm.ob.hcm.dto.profile;

import java.io.Serializable;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class ContactInformationDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -7962248231041751287L;

  private String email;
  private String mobileNo;
  private String homeNo;
  private String workNo;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getMobileNo() {
    return mobileNo;
  }

  public void setMobileNo(String mobileNo) {
    this.mobileNo = mobileNo;
  }

  public String getHomeNo() {
    return homeNo;
  }

  public void setHomeNo(String homeNo) {
    this.homeNo = homeNo;
  }

  public String getWorkNo() {
    return workNo;
  }

  public void setWorkNo(String workNo) {
    this.workNo = workNo;
  }

}
