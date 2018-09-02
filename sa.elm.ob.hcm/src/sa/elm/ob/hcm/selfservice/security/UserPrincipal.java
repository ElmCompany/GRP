package sa.elm.ob.hcm.selfservice.security;

import java.util.ArrayList;
import java.util.Collection;

import org.openbravo.model.ad.access.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Represents the Custom User Principal for Self Service
 *
 */
public class UserPrincipal implements UserDetails {

  /**
   * 
   */
  private static final long serialVersionUID = 1871160778910621052L;
  private User user;
  private String userLanguage;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // Need to make this code dynamic
    Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
    // Don't change the order
    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_DBA"));
    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

    return grantedAuthorities;
  }

  @Override
  public String getPassword() {

    return user.getPassword();
  }

  @Override
  public String getUsername() {

    return user.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public boolean isEnabled() {
    // TODO Auto-generated method stub
    return true;
  }

  public String getUserLanguage() {
    return userLanguage;
  }

  public void setUserLanguage(String userLanguage) {
    this.userLanguage = userLanguage;
  }

  public UserPrincipal(User user) {
    this.user = user;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
