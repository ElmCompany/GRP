package sa.elm.ob.hcm.event.dao;

import sa.elm.ob.hcm.EhcmElementGroup;

/**
 * Interface for Element Group Event related DB Operations
 * 
 * @author Priyanka Ranjan on 18/07/2018
 * 
 */
public interface ElementGroupEventDAO {

  /**
   * check element group already processed with given period (end date)
   * 
   * @param elementgroup
   * @return
   * @throws Exception
   */
  boolean isElementGroupProcessed(EhcmElementGroup elementgroup) throws Exception;

  boolean isPrimaryCheckElementGroup(EhcmElementGroup elementgroup, String clientId,
      String IsUpdate) throws Exception;
}
