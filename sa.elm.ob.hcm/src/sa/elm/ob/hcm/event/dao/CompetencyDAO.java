/*
 * All Rights Reserved By Qualian Technologies Pvt Ltd.
 */
package sa.elm.ob.hcm.event.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;

import sa.elm.ob.hcm.EHCMCompetency;
import sa.elm.ob.hcm.EHCMCompetencyType;
import sa.elm.ob.hcm.EHCMComptypeCompetency;
import sa.elm.ob.hcm.EHCMEmpEvalCompetency;

/**
 * 
 * @author divya -12-02-2018
 *
 */
public class CompetencyDAO {
	private ConnectionProvider conn = null;

	private static final Logger LOG = Logger.getLogger(CompetencyDAO.class);

	public CompetencyDAO(ConnectionProvider con) {
		this.conn = con;
	}

	/**
	 * check competency name & organization is unique or not
	 * 
	 * @param empCompetency
	 * @return
	 */
	public static boolean checkCompetencyUnique(EHCMCompetency empCompetency) {
		List<EHCMCompetency> empCompetencyQryList = new ArrayList<EHCMCompetency>();
		try {
			// check name is unique
			OBQuery<EHCMCompetency> empCompetencyQry = OBDal.getInstance().createQuery(EHCMCompetency.class, "  name=:name  and organization.id=:orgId and client.id =:clientId");
			empCompetencyQry.setNamedParameter("name", empCompetency.getName());
			empCompetencyQry.setNamedParameter("orgId", empCompetency.getOrganization().getId());
			empCompetencyQry.setNamedParameter("clientId", empCompetency.getClient().getId());
			empCompetencyQryList = empCompetencyQry.list();
			if(empCompetencyQryList.size() > 0) {
				return true;
			}
			else
				return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			LOG.error("Exception in checkCompetencyUnique: ", e);
			OBDal.getInstance().rollbackAndClose();
		}
		finally {
		}
		return false;
	}

	/**
	 * check competency type name & organization is unique or not
	 * 
	 * @param empCompetencytype
	 * @return
	 */
	public static boolean checkCompetencyTypeUnique(EHCMCompetencyType empCompetencytype) {
		List<EHCMCompetencyType> empCompetencyQryList = new ArrayList<EHCMCompetencyType>();
		try {
			// check name is unique
			OBQuery<EHCMCompetencyType> empCompetencyTypeQry = OBDal.getInstance().createQuery(EHCMCompetencyType.class, "  name=:name  and organization.id=:orgId and client.id =:clientId");
			empCompetencyTypeQry.setNamedParameter("name", empCompetencytype.getName());
			empCompetencyTypeQry.setNamedParameter("orgId", empCompetencytype.getOrganization().getId());
			empCompetencyTypeQry.setNamedParameter("clientId", empCompetencytype.getClient().getId());
			empCompetencyQryList = empCompetencyTypeQry.list();
			if(empCompetencyQryList.size() > 0) {
				return true;
			}
			else
				return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			LOG.error("Exception in checkCompetencyTypeUnique: ", e);
			OBDal.getInstance().rollbackAndClose();
		}
		finally {
		}
		return false;
	}

	/**
	 * check employee evaluation competency & competency type should be unique
	 * 
	 * @param empEvalCompetency
	 * @return
	 */
	public static boolean chkEmpEvalCompetencyUnique(EHCMEmpEvalCompetency empEvalCompetency) {
		List<EHCMEmpEvalCompetency> empEvalCompetencyList = new ArrayList<EHCMEmpEvalCompetency>();
		try {
			// check name is unique
			if(empEvalCompetency.getEhcmCompetency() != null && empEvalCompetency.getEhcmComptypeCompetency() != null) {
				OBQuery<EHCMEmpEvalCompetency> empEvalCompetencyQry = OBDal.getInstance().createQuery(EHCMEmpEvalCompetency.class, " as e  where e.ehcmCompetency.id=:competencyId  and ehcmComptypeCompetency.id=:compCompetencytypeId and e.ehcmEmpevaluationEmp.id=:empEvaluationId ");
				empEvalCompetencyQry.setNamedParameter("competencyId", empEvalCompetency.getEhcmCompetency().getId());
				empEvalCompetencyQry.setNamedParameter("compCompetencytypeId", empEvalCompetency.getEhcmComptypeCompetency().getId());
				empEvalCompetencyQry.setNamedParameter("empEvaluationId", empEvalCompetency.getEhcmEmpevaluationEmp().getId());
				empEvalCompetencyList = empEvalCompetencyQry.list();
				if(empEvalCompetencyList.size() > 0) {
					return true;
				}
				else
					return false;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			LOG.error("Exception in chkEmpEvalCompetencyUnique: ", e);
			OBDal.getInstance().rollbackAndClose();
		}
		finally {
		}
		return false;
	}

	/**
	 * check competency is unique or not in Competency type and competency
	 * window
	 * 
	 * @param compTypeCompetency
	 * @return
	 */
	public static boolean chkCompTypeCompetencyUnique(EHCMComptypeCompetency compTypeCompetency) {
		List<EHCMComptypeCompetency> competencyQryList = new ArrayList<EHCMComptypeCompetency>();
		try {
			// check name is unique
			if(compTypeCompetency.getEhcmCompetency() != null) {
				OBQuery<EHCMComptypeCompetency> competencyTypeQry = OBDal.getInstance().createQuery(EHCMComptypeCompetency.class, "  ehcmCompetency.id=:competencyId  and ehcmCompetencyType.id=:competencyTypeId ");
				competencyTypeQry.setNamedParameter("competencyId", compTypeCompetency.getEhcmCompetency().getId());
				competencyTypeQry.setNamedParameter("competencyTypeId", compTypeCompetency.getEhcmCompetencyType().getId());
				competencyQryList = competencyTypeQry.list();
				if(competencyQryList.size() > 0) {
					return true;
				}
				else
					return false;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			LOG.error("Exception in chkCompTypeCompetencyUnique: ", e);
			OBDal.getInstance().rollbackAndClose();
		}
		finally {
		}
		return false;
	}
}