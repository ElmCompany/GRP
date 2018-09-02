package sa.elm.ob.hcm.dao.activiti;

import sa.elm.ob.hcm.GenericActivitiData;

public interface CommonActivitiDAO {
  /**
   * Store employee update data in transaction table before it moves approval
   * 
   * @param userName
   * @param jsonInString
   * @param decisionType
   */
  GenericActivitiData storeDataInTransaction(String userName, String jsonInString,
      String typeofActiviti);

  /**
   * Find out the transaction table record
   * 
   * @param userName
   * @param requestType
   * @return
   */
  GenericActivitiData findTransactionRecord(String recordId);
}
