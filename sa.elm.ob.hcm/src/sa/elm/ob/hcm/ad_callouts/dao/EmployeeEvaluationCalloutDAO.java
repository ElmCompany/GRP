/*
 * All Rights Reserved By Qualian Technologies Pvt Ltd.
 */
package sa.elm.ob.hcm.ad_callouts.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;

import sa.elm.ob.hcm.EHCMComptypeCompetency;
import sa.elm.ob.hcm.EmploymentInfo;

/**
 * 
 * @author divya -05-02-2018
 *
 */
public class EmployeeEvaluationCalloutDAO {
	private ConnectionProvider conn = null;

	private static final Logger LOG = Logger.getLogger(EmployeeEvaluationCalloutDAO.class);

	public EmployeeEvaluationCalloutDAO(ConnectionProvider con) {
		this.conn = con;
	}

	/**
	 * get Recent employmentInfo
	 * 
	 * @param employeeId
	 * @return employmentInfo Object
	 */
	public static EmploymentInfo getActiveEmployInfo(String employeeId) {

		EmploymentInfo info = null;
		List<EmploymentInfo> employmentInfoList = new ArrayList<EmploymentInfo>();
		try {
			OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class, " as e where e.ehcmEmpPerinfo.id=:employeeId and e.enabled='Y'  order by e.creationDate desc");
			empInfo.setNamedParameter("employeeId", employeeId);
			empInfo.setMaxResult(1);
			employmentInfoList = empInfo.list();
			if(employmentInfoList.size() > 0) {
				info = employmentInfoList.get(0);
			}
			return info;
		}
		catch (Exception e) {
			LOG.error("Exception in getActiveEmployInfo: ", e);
			OBDal.getInstance().rollbackAndClose();
			return info;
		}
		finally {
		}
	}
	
	/**
	 * get Recent employmentInfo
	 * 
	 * @param employeeId
	 * @return employmentInfo Object
	 */
	public static EHCMComptypeCompetency getComTypeComptency(String comptencyTypeId,String competencyId ) {

		EHCMComptypeCompetency comptencyTypeComp = null;
		List<EHCMComptypeCompetency> comptencyList = new ArrayList<EHCMComptypeCompetency>();
		try {
			OBQuery<EHCMComptypeCompetency> comtencyQry = OBDal.getInstance().createQuery(EHCMComptypeCompetency.class, 
					" as e where e.ehcmCompetencyType.id=:competencyTypeId and e.ehcmCompetency.id=:competencyId");
			comtencyQry.setNamedParameter("competencyTypeId", comptencyTypeId);
			comtencyQry.setNamedParameter("competencyId", competencyId);
			comtencyQry.setMaxResult(1);
			comptencyList = comtencyQry.list();
			if(comptencyList.size() > 0) {
				comptencyTypeComp = comptencyList.get(0);
			}
			return comptencyTypeComp;
		}
		catch (Exception e) {
			LOG.error("Exception in getComTypeComptency: ", e);
			OBDal.getInstance().rollbackAndClose();
			return comptencyTypeComp;
		}
		finally {
		}
	}
}