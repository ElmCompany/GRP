package sa.elm.ob.hcm.selfservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import sa.elm.ob.hcm.dto.profile.LoginInformationDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.service.login.LoginService;

/**
 * Controller for Login Functionalities
 * 
 */
@RestController
@RequestMapping("openerp/hr")
public class LoginController {

  @Autowired
  private LoginService loginService;

  /**
   * Change the user name and password
   * 
   * @param loginInformationDTO
   * @return
   * @throws BusinessException
   */
  @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<Boolean> changePassword(
      @RequestBody LoginInformationDTO loginInformationDTO) throws BusinessException {

    Boolean result = loginService.changePassword(loginInformationDTO);

    return new ResponseEntity<Boolean>(result, HttpStatus.OK);
  }

}
