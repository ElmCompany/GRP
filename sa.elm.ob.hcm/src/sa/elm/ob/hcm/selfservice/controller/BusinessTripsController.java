package sa.elm.ob.hcm.selfservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import sa.elm.ob.hcm.dto.businessTrips.BusinessTripRequestDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.services.businessTrips.BusinessTripsService;

/**
 * Web Controller for Business Trip/Mission/Training
 * 
 *
 */
@RestController
@RequestMapping("openerp/hr/businessTrips")
public class BusinessTripsController {

  @Autowired
  private BusinessTripsService businessTripsService;

  @RequestMapping(value = "/{username}", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<List<String>> getAllOriginalDecisionNoByUsername(
      @PathVariable("username") String username) {

    List<String> originalDecNo = businessTripsService.getAllOriginalDecisionNoByUsername(username);

    return new ResponseEntity<List<String>>(originalDecNo, HttpStatus.OK);
  }

  @RequestMapping(value = "/decisionNo/{originalDecNo}", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<BusinessTripRequestDTO> getBusinessTripRequestByOrginalDecNo(
      @PathVariable("originalDecNo") String originalDecNo) {

    BusinessTripRequestDTO businessTripRequestDTO = businessTripsService
        .getBusinessTripRequestByOrginalDecNo(originalDecNo);

    return new ResponseEntity<BusinessTripRequestDTO>(businessTripRequestDTO, HttpStatus.OK);
  }

  @RequestMapping(value = "/submit/{username}", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<Boolean> submitBusinessTripRequest(
      @PathVariable("username") String username,
      @RequestBody BusinessTripRequestDTO businessTripRequestDTO)
      throws BusinessException, SystemException {

    Boolean businessTripRequest = businessTripsService
        .submitBusinessTripRequestWithApprovalFlow(username, businessTripRequestDTO);

    return new ResponseEntity<Boolean>(businessTripRequest, HttpStatus.OK);
  }

  @RequestMapping(value = "/submit/cancel/{username}/decisionNo/{originalDecNo}", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<Boolean> submitCancelBusinessTripRequest(
      @PathVariable("username") String username,
      @PathVariable("originalDecNo") String originalDecNo)
      throws BusinessException, SystemException {

    Boolean businessTripRequest = businessTripsService
        .submitCancelBusinessTripRequestWithWorkflow(username, originalDecNo);

    return new ResponseEntity<Boolean>(businessTripRequest, HttpStatus.OK);
  }

  @RequestMapping(value = "/submit/payment/{username}/decisionNo/{originalDecNo}", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<Boolean> submitPaymentBusinessTripRequest(
      @PathVariable("username") String username,
      @PathVariable("originalDecNo") String originalDecNo)
      throws BusinessException, SystemException {

    Boolean businessTripRequest = businessTripsService
        .submitPaymentBusinessTripRequestWithWorkflow(username, originalDecNo, null);

    return new ResponseEntity<Boolean>(businessTripRequest, HttpStatus.OK);
  }

}
