package sa.elm.ob.hcm.dao.profile;

import org.openbravo.model.ad.access.User;

import sa.elm.ob.hcm.EhcmEmpPerInfo;

public interface EmployeeProfileDAO {
  /**
   * Get the employee personal profile by User
   * 
   * @param username
   * @return
   */
  EhcmEmpPerInfo getEmployeeProfileByUser(String username);

  /**
   * Get the User Details By Employee UserName
   * 
   * @param username
   * @return
   */
  User getUserDetailsByUserName(String username);
}
