package sa.elm.ob.hcm.event.dao;

import sa.elm.ob.hcm.EhcmEmploymentGroup;

/**
 * Interface for all Employment Group related DB Operations
 * 
 * @author Gowtham on 29/05/2018
 * 
 */
public interface EmploymentGroupEventDAO {

  /**
   * check enddate is greater than last payroll processed.
   * 
   * @param empGroup
   * @return
   * @throws Exception
   */
  boolean checkValidEnddate(EhcmEmploymentGroup empGroup) throws Exception;

  /**
   * check valid age with client configuration.
   * 
   * @param age
   * @return
   * @throws Exception
   */
  boolean checkValidAge(long age) throws Exception;
}
