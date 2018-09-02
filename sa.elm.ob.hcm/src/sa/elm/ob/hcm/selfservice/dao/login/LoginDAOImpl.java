package sa.elm.ob.hcm.selfservice.dao.login;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.utils.FormatUtilities;
import org.springframework.stereotype.Repository;

@Repository
public class LoginDAOImpl implements LoginDAO {

  @Override
  public User findByUserName(String userName) {

    try {
      OBContext.setAdminMode();
      final OBCriteria<User> obc = OBDal.getInstance().createCriteria(User.class);
      obc.add(Restrictions.eq(User.PROPERTY_USERNAME, userName));
      obc.setFilterOnReadableClients(false);
      obc.setFilterOnReadableOrganization(false);
      final User userOB = (User) obc.uniqueResult();
      return userOB;
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public Boolean validateOldPassword(String userName, String oldPassword) {

    try {
      OBContext.setAdminMode();
      User user = findByUserName(userName);
      // get old password
      String hashedOldPassword = user.getPassword();// value will
                                                    // be hashed code

      String unhashedOldaPassword = FormatUtilities.sha1Base64(oldPassword); // hash the input
                                                                             // password(old)
      if (hashedOldPassword.equals(unhashedOldaPassword)) {
        return true;
      } else {
        return false;
      }
    } catch (ServletException e) {

      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public void updatePassword(String userName, String newPassword) {

    try {
      OBContext.setAdminMode();
      User user = findByUserName(userName);
      // Now change the password
      user.setPassword(FormatUtilities.sha1Base64(newPassword));
      // Update the Password
      OBDal.getInstance().save(user);
      OBDal.getInstance().flush();
    } catch (ServletException e) {
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}
