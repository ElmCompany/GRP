package sa.elm.ob.hcm.event.dao;

/**
 * Interface for all Payroll Report Config Event related DB Operations
 * 
 * @author Sowmiya N S on 13/06/2018
 * 
 */
public interface PayrollReportConfigDAO {

  /**
   * check only one record is available
   * 
   * @return
   * @throws Exception
   */
  boolean checkAlreadyRecordExist(String clientId) throws Exception;

}
