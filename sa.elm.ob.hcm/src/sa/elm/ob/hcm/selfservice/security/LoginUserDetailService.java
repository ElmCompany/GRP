package sa.elm.ob.hcm.selfservice.security;

import org.openbravo.model.ad.access.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import sa.elm.ob.hcm.selfservice.dao.login.LoginDAO;

/**
 * User Detail Service for Spring Security Authentication
 *
 */
@Service
public class LoginUserDetailService implements UserDetailsService {

  @Autowired
  private LoginDAO loginDAO;

  @Override
  public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
    // Get the user by user name
    User user = loginDAO.findByUserName(userName);
    // If user not found then throw exception
    if (null == user) {
      throw new UsernameNotFoundException(userName);
    }
    // Now create the User Details and return
    return new UserPrincipal(user);
  }

}
