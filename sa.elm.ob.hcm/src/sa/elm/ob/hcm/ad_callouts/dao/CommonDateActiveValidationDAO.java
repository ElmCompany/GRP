package sa.elm.ob.hcm.ad_callouts.dao;

/**
 * Interface for all Common date-active validation related DB Operations
 * 
 * @author Priyanka Ranjan on 30/04/2018
 * 
 */

public interface CommonDateActiveValidationDAO {

  /**
   * get current(today) Hijri Date
   * 
   * @return
   * @throws Exception
   */
  public String getCurrentHijriDate() throws Exception;

}
