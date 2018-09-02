
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
 * @author Mouli.K
 *
 */
public class PositionHierarchyCalloutDAO {
        private ConnectionProvider conn = null;

        private static final Logger LOG = Logger.getLogger(PositionHierarchyCalloutDAO.class);

        public PositionHierarchyCalloutDAO(ConnectionProvider con) {
                this.conn = con;
        }

        /**
         * get no of subordinates for the particular position
         * 
         * @param positionId
         * @return countofsubordinates
         */
        @SuppressWarnings("unchecked")
        public static int getNoOfSubordinates(String positionId,String positionTreeId) {

                int NoOfSubordinates = 0;
                List  positionList = null;
                try {
                        String sql = " select distinct node.Ehcm_Position_id from ehcm_poshierarchy_posnode node where node.ehcm_poshierarchy_pos_id in ( select  ehcm_poshierarchy_pos_id  from  ehcm_poshierarchy_pos where Ehcm_Position_id='"+positionId+"'  and  Ehcm_Position_Tree_id='"+positionTreeId+"')";
                        SQLQuery query = OBDal.getInstance().getSession().createSQLQuery(sql);
                        positionList = query.list();
                        if(positionList != null && positionList.size() > 0) {
                                NoOfSubordinates = positionList.size();
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