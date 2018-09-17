package sa.elm.ob.hcm.services.businessTrips;

import java.util.List;

import sa.elm.ob.hcm.dto.businessTrips.BusinessPaymentDTO;
import sa.elm.ob.hcm.dto.businessTrips.BusinessTripRequestDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

/**
 * Business Trip/Mission/Training Service Interface
 *
 */

public interface BusinessTripsService {

  /**
   * Submit Business Trip Request By Using name
   * 
   * @param username
   * @param businessTripRequestDTO
   * @return
   */
  Boolean submitBusinessTripRequest(String username, BusinessTripRequestDTO businessTripRequestDTO)
      throws BusinessException, SystemException;

  /**
   * Submit Business Trip Request By Using name with Approval work flow
   * 
   * @param username
   * @param businessTripRequestDTO
   * @return
   */
  Boolean submitBusinessTripRequestWithApprovalFlow(String username,
      BusinessTripRequestDTO businessTripRequestDTO) throws BusinessException, SystemException;

  /**
   * @param username
   * @return
   */
  List<String> getAllOriginalDecisionNoByUsername(String username);

  /**
   * @param originalDecNo
   * @return
   */
  BusinessTripRequestDTO getBusinessTripRequestByOrginalDecNo(String originalDecNo);

  /**
   * @param username
   * @param originalDecNo
   */
  Boolean submitCancelBusinessTripRequest(String username, String originalDecNo)
      throws BusinessException, SystemException;

  /**
   * @param username
   * @param originalDecNo
   * @return
   * @throws BusinessException
   * @throws SystemException
   */
  Boolean submitCancelBusinessTripRequestWithWorkflow(String username, String originalDecNo)
      throws BusinessException, SystemException;

  /**
   * @param username
   * @param originalDecNo
   */
  Boolean submitPaymentBusinessTripRequest(String username, String originalDecNo,
      BusinessPaymentDTO businessPaymentDTO) throws BusinessException, SystemException;

  /**
   * @param username
   * @param originalDecNo
   * @param businessPaymentDTO
   * @return
   * @throws BusinessException
   * @throws SystemException
   */
  Boolean submitPaymentBusinessTripRequestWithWorkflow(String username, String originalDecNo)
      throws BusinessException, SystemException;

}
