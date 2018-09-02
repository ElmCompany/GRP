package sa.elm.ob.hcm.dao.leave;

import java.util.List;

import org.codehaus.jettison.json.JSONObject;

import sa.elm.ob.hcm.EHCMAbsenceAccrual;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

public interface ViewLeaveDAO {
  /**
   * Get the employee personal profile by User
   * 
   * @param username
   * @return
   */
  EhcmEmpPerInfo getEmployeeProfileByUser(String username);

  /**
   * Get the accural details of Employee by user
   * 
   * @param username
   * @return
   */
  EHCMAbsenceAccrual getAbsenceAccural(String username) throws BusinessException, SystemException;

  /**
   * 
   * @param clientId
   * @return Absence Type List
   */
  List<EHCMAbsenceType> getAbsenceType(String clientId);

  /**
   * 
   * @param absenceType
   * @param objEmpInfo
   * @param asOfDate
   * @return return available leave and availed leave for employee
   */
  JSONObject getAvailedAndAvailableDays(EHCMAbsenceType absenceType, EhcmEmpPerInfo objEmpInfo,
      String asOfDate);

}
