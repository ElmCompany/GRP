package sa.elm.ob.hcm.dao.businessTrip;

import java.util.List;

import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.dto.businessTrips.BusinessTripRequestDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

public interface BusinessTripDAO {
  /**
   * Create Business Mission In Transactional Table
   * 
   * @param employeeOB
   * @param businessTripRequestDTO
   * @param decisionType
   * @return
   */
  EHCMEmpBusinessMission createBusinessMission(EhcmEmpPerInfo employeeOB,
      BusinessTripRequestDTO businessTripRequestDTO, String decisionType)
      throws SystemException, BusinessException;

  /**
   * Move Business Mission Data to Business Mission summary
   * 
   * @param businessMissionOB
   * @param decisionType
   */
  void createBusinessMissionSummary(EHCMEmpBusinessMission businessMissionOB, String decisionType);

  /**
   * find original business trip decision numbers
   * 
   * @param username
   * @return
   */
  List<String> getAllOriginalDecisionNoByUserName(String username);
  //
  // /**
  // *
  // * @param username
  // * @param originalDecNo
  // * @return
  // */
  // List<EHCMEmpBusinessMission> getLeaveRequestByUsername(String username, String originalDecNo);
  //
  // /**
  // * find business trip by using user name and decision number
  // *
  // * @param username
  // * @param originalDecNo
  // * @return
  // */
  // List<EHCMEmpBusinessMission> getBusinessTripByUsername(String username, String originalDecNo);

  /**
   * 
   * find business trip by using user name and decision number
   * 
   * @param originalDecNo
   * @return
   */
  List<EHCMEmpBusinessMission> getBusinessTripByDecisionNo(String originalDecNo);

}
