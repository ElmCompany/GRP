package sa.elm.ob.hcm.dao.profile;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EhcmEmpPerInfo;

@Repository
public class EmployeeProfileDAOImpl implements EmployeeProfileDAO {
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(EmployeeProfileDAOImpl.class);

  @Override
  public EhcmEmpPerInfo getEmployeeProfileByUser(String username) {
    EhcmEmpPerInfo objEmpPerInfo = null;
    OBQuery<EhcmEmpPerInfo> empInfoQry = null;
    List<EhcmEmpPerInfo> empInfoList = new ArrayList<EhcmEmpPerInfo>();
    try {
      OBContext.setAdminMode(true);
      final String query = " as e where e.searchKey=:value  ";
      empInfoQry = OBDal.getInstance().createQuery(EhcmEmpPerInfo.class, query);
      empInfoQry.setNamedParameter("value", username);
      empInfoQry.setFilterOnReadableClients(false);
      empInfoQry.setFilterOnReadableOrganization(false);
      empInfoQry.setMaxResult(1);
      empInfoList = empInfoQry.list();
      if (empInfoList.size() > 0) {
        objEmpPerInfo = empInfoList.get(0);
      }
    } catch (OBException e) {
      log.error("Error while fetching employee personal data-->emp user name -->" + username);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return objEmpPerInfo;
  }

  @Override
  public User getUserDetailsByUserName(String username) {
    User userOB = null;
    OBQuery<User> userQry = null;
    List<User> userList = new ArrayList<User>();
    try {
      OBContext.setAdminMode(true);
      final String query = " as e where e.username=:userName  ";
      userQry = OBDal.getInstance().createQuery(User.class, query);
      userQry.setNamedParameter("userName", username);
      userQry.setFilterOnReadableClients(false);
      userQry.setFilterOnReadableOrganization(false);
      userQry.setMaxResult(1);
      userList = userQry.list();
      if (userList.size() > 0) {
        userOB = userList.get(0);
      }
    } catch (OBException e) {
      log.error("Error while fetching user details for-->emp user name -->" + username);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return userOB;
  }

}
