package sa.elm.ob.hcm.selfservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Provides utiliy methods for getting user principal related data
 * 
 *
 */
public class SecurityUtils {

  /**
   * Get the logged in user name
   * 
   * @return
   */
  public static String loggedInUserName() {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (null != authentication) {
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      return userPrincipal.getUsername();
    }

    return null;
  }

  /**
   * Get the language selected by user
   * 
   * @return
   */
  public static String getUserLanguage() {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (null != authentication) {
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      return userPrincipal.getUserLanguage();
    }

    return null;

  }
}
