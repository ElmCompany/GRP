package sa.elm.ob.hcm.selfservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import sa.elm.ob.hcm.dto.leave.LeaveRequestDTO;
import sa.elm.ob.hcm.dto.leave.RejoinLeaveRequestDTO;
import sa.elm.ob.hcm.dto.leave.ViewLeaveAccrualDTO;
import sa.elm.ob.hcm.dto.leave.ViewLeaveDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.services.leave.LeaveManagementService;

/**
 * Web Controller for Leave Management
 * 
 *
 */
@RestController
@RequestMapping("openerp/hr/leave")
public class LeaveManagementController {

  @Autowired
  private LeaveManagementService leaveManagementService;

  @RequestMapping(value = "/leaves/{username}/asOfDate/{asOfDate}", method = RequestMethod.GET)
  public ResponseEntity<List<ViewLeaveDTO>> viewLeaves(@PathVariable String username,
      @PathVariable String asOfDate) {

    List<ViewLeaveDTO> viewLeaveDTO = leaveManagementService.viewLeaves(username, asOfDate);

    return new ResponseEntity<List<ViewLeaveDTO>>(viewLeaveDTO, HttpStatus.OK);
  }

  @RequestMapping(value = "/leavesAccrual/{username}/asOfDate/{asOfDate}", method = RequestMethod.GET)
  public ResponseEntity<List<ViewLeaveAccrualDTO>> viewLeavesAccrual(@PathVariable String username,
      @PathVariable String asOfDate) throws BusinessException, SystemException {

    List<ViewLeaveAccrualDTO> viewLeaveAccrualDTO = leaveManagementService
        .viewLeavesAccrual(username, asOfDate);

    return new ResponseEntity<List<ViewLeaveAccrualDTO>>(viewLeaveAccrualDTO, HttpStatus.OK);
  }

  @RequestMapping(value = "/leaveRequest/submit/{username}", method = RequestMethod.POST)
  public ResponseEntity<Boolean> submitLeaveRequest(@PathVariable String username,
      @RequestBody LeaveRequestDTO leaveRequestDTO) throws SystemException, BusinessException {

    Boolean savedLeaveRequestDTO = leaveManagementService
        .submitLeaveRequestWithApprovalFlow(username, leaveRequestDTO);

    return new ResponseEntity<Boolean>(savedLeaveRequestDTO, HttpStatus.OK);
  }

  @RequestMapping(value = "/originalDecNo/{username}", method = RequestMethod.GET)
  public ResponseEntity<List<String>> getAllOriginalDecNo(@PathVariable String username) {

    List<String> originalDecNo = leaveManagementService.getAllOriginalDecisionNo(username);

    return new ResponseEntity<List<String>>(originalDecNo, HttpStatus.OK);
  }

  @RequestMapping(value = "/getLeaveRequestByOrigDecNo/{origDecNo}", method = RequestMethod.GET)
  public ResponseEntity<LeaveRequestDTO> getLeaveRequestByOrigDecNo(
      @PathVariable String origDecNo) {

    LeaveRequestDTO leaveRequestDTO = leaveManagementService
        .getLeaveRequestByOriginalDecisionNo(origDecNo);

    return new ResponseEntity<LeaveRequestDTO>(leaveRequestDTO, HttpStatus.OK);
  }

  @RequestMapping(value = "/cutoff/submit/{username}/originalDecNo/{origDecNo}", method = RequestMethod.POST)
  public ResponseEntity<Boolean> submitCutoffLeaveRequest(@PathVariable String username,
      @RequestBody LeaveRequestDTO leaveRequestDTO) throws SystemException, BusinessException {

    Boolean cutoffLeaveRequest = leaveManagementService
        .submitCutoffLeaveRequestWithApproval(username, leaveRequestDTO);

    return new ResponseEntity<Boolean>(cutoffLeaveRequest, HttpStatus.OK);
  }

  @RequestMapping(value = "/cancel/submit/{username}", method = RequestMethod.POST)
  public ResponseEntity<Boolean> submitCancelLeaveRequest(@PathVariable String username,
      @RequestBody LeaveRequestDTO leaveRequestDTO) throws BusinessException, SystemException {

    Boolean cancelLeaveRequest = leaveManagementService
        .submitCancelLeaveRequestWithApproval(username, leaveRequestDTO);

    return new ResponseEntity<Boolean>(cancelLeaveRequest, HttpStatus.OK);
  }

  @RequestMapping(value = "/rejoin/submit/{username}", method = RequestMethod.POST)
  public ResponseEntity<Boolean> submitRejoinLeaveRequest(@PathVariable String username,
      @RequestBody RejoinLeaveRequestDTO leaveRequestDTO) {

    Boolean rejoinLeaveRequest = leaveManagementService
        .submitRejoinLeaveRequestWithApproval(username, leaveRequestDTO);

    return new ResponseEntity<Boolean>(rejoinLeaveRequest, HttpStatus.OK);
  }

  @RequestMapping(value = "/extend/submit/{username}/originalDecNo/{origDecNo}", method = RequestMethod.POST)
  public ResponseEntity<Boolean> submitExtendLeaveRequest(@PathVariable String username,
      @RequestBody LeaveRequestDTO leaveRequestDTO) throws SystemException, BusinessException {

    Boolean cutoffLeaveRequest = leaveManagementService
        .createExtendLeaveRequestWithApproval(username, leaveRequestDTO);

    return new ResponseEntity<Boolean>(cutoffLeaveRequest, HttpStatus.OK);
  }
}
