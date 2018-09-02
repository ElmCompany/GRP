package sa.elm.ob.hcm.ad_process.EmpOvertimeTransaction;

import java.util.Date;
import java.util.List;

import sa.elm.ob.hcm.EHCMElmttypeDef;
import sa.elm.ob.hcm.EhcmEmployeeOvertime;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmpayscaleline;

/**
 * 
 * 
 * @author poongodi 15-03-2018
 *
 */
public interface OvertimeTransactionDAO {

  /**
   * 
   * @param overtimeTypeId
   * @param overtimeObj
   * @return
   * @throws Exception
   */
  int getOvertimeTypeRecord(String overtimeTypeId, EhcmEmployeeOvertime overtimeObj)
      throws Exception;

  /**
   * Get Payscale value for the employment.
   * 
   * @param empInfo
   * @param startDate
   * @param endDate
   * @return
   * @throws Exception
   */
  public List<ehcmpayscaleline> getPayscaleValues(EmploymentInfo empInfo, Date startDate,
      Date endDate) throws Exception;

  /**
   * Get formula for overtime
   * 
   * @param elementType
   * @param isBaseCalculation
   * @param overTime
   * @return
   * @throws Exception
   */
  public String getOverTimeFormula(EHCMElmttypeDef elementType, boolean isBaseCalculation,
      EhcmEmployeeOvertime overTime) throws Exception;

}
