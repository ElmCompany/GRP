package sa.elm.ob.hcm.dto.profile;

import java.io.Serializable;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class LoginInformationDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 2731498485210717325L;
  private String loginId;
  private String oldPassword;
  private String newPassword;

  public String getLoginId() {
    return loginId;
  }

  public void setLoginId(String loginId) {
    this.loginId = loginId;
  }

  public String getOldPassword() {
    return oldPassword;
  }

  public void setOldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

}
