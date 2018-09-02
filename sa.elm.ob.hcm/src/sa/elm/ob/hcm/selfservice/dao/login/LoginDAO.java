package sa.elm.ob.hcm.selfservice.dao.login;

import org.openbravo.model.ad.access.User;

/**
 * DAO operations for User Login
 *
 */
public interface LoginDAO {
  /**
   * Get the user by User Name
   * 
   * @param userName
   * @return
   */
  User findByUserName(String userName);

  /**
   * Check whether the old password is correct
   * 
   * @param userName
   * @param oldPassword
   * @return
   */
  Boolean validateOldPassword(String userName, String oldPassword);

  /**
   * Update the New Password
   * 
   * @param userName
   * @param newPassword
   */
  void updatePassword(String userName, String newPassword);
}
