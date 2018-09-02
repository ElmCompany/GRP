package sa.elm.ob.hcm.selfservice.service.login;

import sa.elm.ob.hcm.dto.profile.LoginInformationDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;

public interface LoginService {

  /**
   * Validate the Old Password
   * 
   * @param userName
   * @param oldPassword
   * @return
   */
  Boolean validateOldPassword(LoginInformationDTO loginInformationDTO) throws BusinessException;

  /**
   * Change the password
   * 
   * @param loginInformationDTO
   * 
   */
  Boolean changePassword(LoginInformationDTO loginInformationDTO) throws BusinessException;
}
