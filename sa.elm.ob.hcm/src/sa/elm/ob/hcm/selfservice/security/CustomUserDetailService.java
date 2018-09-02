package sa.elm.ob.hcm.selfservice.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUserDetailService implements UserDetailsService {
  public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
    UserDetails user = null;
    if (userName.equalsIgnoreCase("admin")) {
      user = getAdminUser(userName);
    } else if (userName.equalsIgnoreCase("dba")) {
      user = getDBAUser(userName);
    } else if (userName.equalsIgnoreCase("user")) {
      user = getUserUser(userName);
    }

    if (user == null) {
      throw new UsernameNotFoundException("Invalid user : " + userName);
    }

    return user;
  }

  private UserDetails getAdminUser(String username) {
    Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
    // Don't change the order
    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_DBA"));
    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

    return new User(username, "adminpassword", true, true, true, true, grantedAuthorities);
  }

  private UserDetails getDBAUser(String username) {
    Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
    // Don't change the order
    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_DBA"));
    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    return new User(username, "dbapassword", true, true, true, true, grantedAuthorities);
  }

  private UserDetails getUserUser(String username) {
    Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    return new User(username, "userpassword", true, true, true, true, grantedAuthorities);
  }
}