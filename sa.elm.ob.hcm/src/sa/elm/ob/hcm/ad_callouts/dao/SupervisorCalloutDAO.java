/*
 * All Rights Reserved By Qualian Technologies Pvt Ltd.
 */
package sa.elm.ob.hcm.ad_callouts.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;

import sa.elm.ob.hcm.EHCMEmpManagerHist;

/**
 * 
 * @author divya -05-02-2018
 *
 */
public class SupervisorCalloutDAO {
	private ConnectionProvider conn = null;

	private static final Logger LOG = Logger.getLogger(SupervisorCalloutDAO.class);

	public SupervisorCalloutDAO(ConnectionProvider con) {
		this.conn = con;
	}

	/**
	 * get no of subordinates for the particular employee
	 * 
	 * @param employeeId
	 * @return countofsubordinates
	 */
	@SuppressWarnings("unchecked")
	public static int getNoOfSubordinates(String employeeId,String empHierarychyId) {

		int NoOfSubordinates = 0;
		List<EHCMEmpManagerHist> empManagerHistList = new ArrayList<EHCMEmpManagerHist>();
		try {
			//String sql = " select distinct info.ehcm_emp_perinfo_id from ehcm_employment_info info where info.ehcm_emp_supervisor_id in ( select  ehcm_emp_supervisor_id  from  ehcm_emp_supervisor where ehcm_emp_perinfo_id='"+employeeId+"' )";
			String sql = " select distinct node.ehcm_emp_perinfo_id from ehcm_emp_supervisornode node where node.ehcm_emp_supervisor_id in ( select  ehcm_emp_supervisor_id  from  ehcm_emp_supervisor where ehcm_emp_perinfo_id='"+employeeId+"'  and  ehcm_emp_hierarchy_id='"+empHierarychyId+"')";
			SQLQuery query = OBDal.getInstance().getSession().createSQLQuery(sql);
			empManagerHistList = query.list();
			if(empManagerHistList != null && empManagerHistList.size() > 0) {
				NoOfSubordinates = empManagerHistList.size();
			}

			return NoOfSubordinates;
		}
		catch (Exception e) {
			LOG.error("Exception in getNoOfSubordinates: ", e);
			OBDal.getInstance().rollbackAndClose();
			return NoOfSubordinates;
		}
		finally {
		}
	}
}