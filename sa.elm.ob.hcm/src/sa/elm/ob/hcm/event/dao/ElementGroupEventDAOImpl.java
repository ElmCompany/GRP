package sa.elm.ob.hcm.event.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.EhcmElementGroup;
import sa.elm.ob.utility.util.Utility;

/**
 * 
 * @author Priyanka Ranjan on 18/07/2018
 * 
 */
// Element Group event DAO Implement file

public class ElementGroupEventDAOImpl implements ElementGroupEventDAO {
  private static Logger LOG = Logger.getLogger(ElementGroupEventDAOImpl.class);

  @Override
  public boolean isElementGroupProcessed(EhcmElementGroup elementgroup) throws Exception {
    // TODO Auto-generated method stub
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      String sqlquery = "select ehcm_payroll_process_hdr_id from EHCM_Payroll_Process_Hdr payproc "
          + " join EHCM_Payrolldef_Period period on payproc.EHCM_Payrolldef_Period_id=period.EHCM_Payrolldef_Period_id "
          + " join EHCM_Payroll_Definition paydef on paydef.EHCM_Payroll_Definition_id=period.EHCM_Payroll_Definition_id "
          + " join ehcm_element_group elemgr on elemgr.EHCM_Payroll_Definition_id=paydef.EHCM_Payroll_Definition_id "
          + " where payproc.ehcm_payroll_definition_id =:paydefId and "
          + " payproc.ehcm_element_group_id =:elementGroupId and "
          + " (period.start_date <=to_date(:enddate,'yyyy-MM-dd') and period.end_date >=to_date(:enddate,'yyyy-MM-dd')) ";

      Query query = OBDal.getInstance().getSession().createSQLQuery(sqlquery);
      query.setParameter("paydefId", elementgroup.getEhcmPayrollDefinition().getId());
      query.setParameter("elementGroupId", elementgroup.getId());
      query.setParameter("enddate", Utility.formatDate(elementgroup.getEndDate(), dateYearFormat));

      if (query.list().size() > 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (LOG.isDebugEnabled())
        LOG.error("error while check isElementGroupProcessed", e);
      return false;
    }

  }

  @Override
  public boolean isPrimaryCheckElementGroup(EhcmElementGroup elementgroup, String clientId,
      String IsUpdate) throws Exception {

    List<EhcmElementGroup> ls = new ArrayList<EhcmElementGroup>();

    // TODO Auto-generated method stub
    try {
      String sql = null;
      if (IsUpdate.equals("Y")) {
        sql = "as e where e.isPrimary='Y' and e.client.id=:clientId and e.id <>:elementgroup";
      } else {
        sql = "as e where e.isPrimary='Y' and e.client.id=:clientId";
      }

      OBQuery<EhcmElementGroup> count = OBDal.getInstance().createQuery(EhcmElementGroup.class,
          sql);
      count.setNamedParameter("clientId", clientId);
      if (IsUpdate.equals("Y")) {
        count.setNamedParameter("elementgroup", elementgroup.getId());
      }
      ls = count.list();
      LOG.debug("count :" + ls.size());
      if (ls.size() > 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("error while check is primary AlreadyExists", e);
      return false;
    }
  }
}
