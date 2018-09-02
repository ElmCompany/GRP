package sa.elm.ob.hcm.event.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.EhcmPayrollReportConfig;

/**
 * 
 * @author Sowmiya N S on 13/06/2018
 * 
 */
// Job Event DAO Implement file
public class PayrollReportConfigDAOImpl implements PayrollReportConfigDAO {
  private static Logger LOG = Logger.getLogger(PayrollReportConfigDAOImpl.class);

  @Override
  public boolean checkAlreadyRecordExist(String clientId) {
    // TODO Auto-generated method stub
    List<EhcmPayrollReportConfig> ls = new ArrayList<EhcmPayrollReportConfig>();
    try {

      OBQuery<EhcmPayrollReportConfig> records = OBDal.getInstance()
          .createQuery(EhcmPayrollReportConfig.class, "as e where e.client.id=:client ");
      records.setNamedParameter("client", clientId);

      ls = records.list();
      LOG.debug("Number of Records : " + ls);
      if (ls.size() > 0) {
        return true;
      }
      // return true;
    } catch (Exception e) {
      LOG.error("error while checkSingleRecord", e);
      return false;
    }
    return false;
  }

}
