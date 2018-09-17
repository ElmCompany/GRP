package sa.elm.ob.hcm.dao.activiti;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.GenericActivitiData;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAOImpl;

@Repository
public class SelfServiceTransactionDAOImpl implements SelfServiceTransactionDAO {
  private static final Logger log = LoggerFactory.getLogger(EmployeeProfileDAOImpl.class);
  @Autowired
  private EmployeeProfileDAO employeeProfileDAO;

  @Override
  public GenericActivitiData storeDataInTransaction(String userName, String jsonInString,
      String typeofActiviti) {
    // TODO Auto-generated method stub
    try {
      OBContext.setAdminMode();
      GenericActivitiData genericActivitiData = null;
      EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(userName);

      genericActivitiData = OBProvider.getInstance().get(GenericActivitiData.class);
      byte[] byteString = jsonInString.getBytes();
      byteString = jsonInString.getBytes(Charset.forName("UTF-8"));
      byteString = jsonInString.getBytes(StandardCharsets.UTF_8);
      genericActivitiData.setTemporaryData(byteString);
      genericActivitiData.setEmployee(employeeOB);
      genericActivitiData.setTypeOfActiviti(typeofActiviti);
      genericActivitiData.setOrganization(employeeOB.getOrganization());
      genericActivitiData.setClient(employeeOB.getClient());
      genericActivitiData.setCreationDate(new java.util.Date());
      genericActivitiData.setCreatedBy(employeeOB.getCreatedBy());
      genericActivitiData.setUpdated(new java.util.Date());
      genericActivitiData.setUpdatedBy(employeeOB.getUpdatedBy());
      OBDal.getInstance().save(genericActivitiData);
      OBDal.getInstance().flush();
      return genericActivitiData;
    } catch (OBException e) {
      log.error("Error while inserting data into Generic Table for Activiti" + typeofActiviti
          + " of user " + userName);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  @Override
  public GenericActivitiData findTransactionRecord(String recordId) {
    // TODO Auto-generated method stub
    GenericActivitiData genericActivitiData = null;
    OBQuery<GenericActivitiData> genericActivitiDataQry = null;
    List<GenericActivitiData> genericActivitiDataList = new ArrayList<GenericActivitiData>();
    try {
      OBContext.setAdminMode();
      final String query = " as e where e.id=:recordId and e.isProcessed='N' order by e.updated desc";
      genericActivitiDataQry = OBDal.getInstance().createQuery(GenericActivitiData.class, query);
      genericActivitiDataQry.setNamedParameter("recordId", recordId);
      genericActivitiDataQry.setFilterOnReadableClients(false);
      genericActivitiDataQry.setFilterOnReadableOrganization(false);
      genericActivitiDataQry.setMaxResult(1);
      genericActivitiDataList = genericActivitiDataQry.list();
      if (genericActivitiDataList.size() > 0) {
        genericActivitiData = genericActivitiDataList.get(0);
      }
    } catch (OBException e) {
      e.printStackTrace();
      log.error("Error while getting Transaction Data to update profile");
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }

    return genericActivitiData;
  }

}
