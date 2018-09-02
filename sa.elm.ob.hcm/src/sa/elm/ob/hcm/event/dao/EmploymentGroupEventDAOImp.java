package sa.elm.ob.hcm.event.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.system.Client;

import sa.elm.ob.hcm.EHCMPayrollProcessHdr;
import sa.elm.ob.hcm.EhcmEmploymentGroup;

/**
 * 
 * @author Gowtham on 29/05/2018
 * 
 */
// Employment Group DAO Implement file
public class EmploymentGroupEventDAOImp implements EmploymentGroupEventDAO {
  private static Logger LOG = Logger.getLogger(EmploymentGroupEventDAOImp.class);

  public boolean checkValidEnddate(EhcmEmploymentGroup empGroup) {
    try {
      Date payrollDate = null;
      Date currentDate = empGroup.getEndDate();
      List<EHCMPayrollProcessHdr> payrollList = new ArrayList<EHCMPayrollProcessHdr>();
      OBQuery<EHCMPayrollProcessHdr> payroll = OBDal.getInstance().createQuery(
          EHCMPayrollProcessHdr.class,
          " as e join e.payrollPeriod p  where e.ehcmEmploymentGroup.id=:empGrpID order by p.endDate desc");
      payroll.setNamedParameter("empGrpID", empGroup.getId());
      payroll.setMaxResult(1);
      payrollList = payroll.list();
      if (payrollList.size() > 0) {
        payrollDate = payrollList.get(0).getPayrollPeriod().getEndDate();
        if (currentDate.compareTo(payrollDate) > 0) {
          return true;
        } else
          return false;
      } else
        return true;

    } catch (Exception e) {
      e.printStackTrace();
      if (LOG.isDebugEnabled())
        LOG.error("error while checkvalidtodate in employment group", e);
      return false;
    }
  }

  public boolean checkValidAge(long age) {
    try {
      Client client = OBDal.getInstance().get(Client.class,
          OBContext.getOBContext().getCurrentClient().getId());
      if ((client.getEhcmMaxempage() != null && age > client.getEhcmMaxempage())
          || (client.getEhcmMinempage() != null && age < client.getEhcmMinempage())) {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (LOG.isDebugEnabled())
        LOG.error("error while checkagevaidation in employment group", e);
      return false;
    }
    return true;

  }

}
