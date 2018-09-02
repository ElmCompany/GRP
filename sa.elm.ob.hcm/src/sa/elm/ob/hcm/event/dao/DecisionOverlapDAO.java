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
import sa.elm.ob.hcm.EHCMDecisionOverlap;
import sa.elm.ob.hcm.EHCMDecisionOverlapLn;
import sa.elm.ob.hcm.EHCMEmpEvalCompetency;

/**
 * 
 * @author divya -19-03-2018
 *
 */
public class DecisionOverlapDAO {
	private ConnectionProvider conn = null;

	private static final Logger LOG = Logger.getLogger(DecisionOverlapDAO.class);

	public DecisionOverlapDAO(ConnectionProvider con) {
		this.conn = con;
	}

	/**
	 * check competency name & organization is unique or not
	 * 
	 * @param empCompetency
	 * @return
	 */
	public static boolean checkDecisionOverlapUnique(EHCMDecisionOverlapLn decisionOverlapln) {
		List<EHCMDecisionOverlapLn> decisionOverlaplnList = new ArrayList<EHCMDecisionOverlapLn>();
		try {
			// check name is unique
			OBQuery<EHCMDecisionOverlapLn> decisionOverlaplnListyQry = OBDal.getInstance().createQuery(EHCMDecisionOverlapLn.class, "  as e where  e.decisionType=:decisionType  and  e.client.id=:clientId  and e.ehcmDecisionOverlap.id=:headerId ");
			decisionOverlaplnListyQry.setNamedParameter("decisionType", decisionOverlapln.getDecisionType());
			decisionOverlaplnListyQry.setNamedParameter("clientId", decisionOverlapln.getClient().getId());
			decisionOverlaplnListyQry.setNamedParameter("headerId", decisionOverlapln.getEhcmDecisionOverlap().getId());
			decisionOverlaplnList = decisionOverlaplnListyQry.list();
			
			if(decisionOverlaplnList.size() > 0) {
				for(EHCMDecisionOverlapLn ln : decisionOverlaplnList) {
/*				if(ln.getEhcmDecisionSubtypeV()!=null && decisionOverlapln.getEhcmDecisionSubtypeV()==null) {
						return true;
					}
					if(ln.getEhcmDecisionSubtypeV()==null && decisionOverlapln.getEhcmDecisionSubtypeV()!=null) {
						return true;
					}
*/					if(ln.getEhcmDecisionSubtypeV()==null && decisionOverlapln.getEhcmDecisionSubtypeV()==null) {
						return true;
					}
					if(ln.getEhcmDecisionSubtypeV()!=null && decisionOverlapln.getEhcmDecisionSubtypeV()!=null && !decisionOverlapln.getId().equals(ln.getId())   && ln.getEhcmDecisionSubtypeV().getId().equals(decisionOverlapln.getEhcmDecisionSubtypeV().getId())) {
						return true;
					}
				}
				return false;
			}
			else
				return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			LOG.error("Exception in checkDecisionOverlapUnique: ", e);
			OBDal.getInstance().rollbackAndClose();
		}
		finally {
		}
		return false;
	}

}
	