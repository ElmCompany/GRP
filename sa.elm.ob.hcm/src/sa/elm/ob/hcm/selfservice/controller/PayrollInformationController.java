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

import sa.elm.ob.hcm.dto.payroll.BankDetailsDTO;
import sa.elm.ob.hcm.dto.payroll.EarningsAndDeductionsDTO;
import sa.elm.ob.hcm.dto.payroll.PaySlipDTO;
import sa.elm.ob.hcm.dto.payroll.SalaryCertificateRequestDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.services.payroll.PayrollInformationService;

/**
 * Web Controller for Payroll Information
 * 
 *
 */
@RestController
@RequestMapping("openerp/hr/payroll")
public class PayrollInformationController {

  @Autowired
  private PayrollInformationService payrollInformationService;

  @RequestMapping(value = "/payslip/{username}/period/{payrollPeriod}", method = RequestMethod.GET)
  public ResponseEntity<PaySlipDTO> getPaySlipInformation(@PathVariable("username") String username,
      @PathVariable("payrollPeriod") String payrollPeriod)
      throws SystemException, BusinessException {

    PaySlipDTO paySlipDTO = payrollInformationService.getPaySlipInformation(username,
        payrollPeriod);

    return new ResponseEntity<PaySlipDTO>(paySlipDTO, HttpStatus.OK);
  }

  @RequestMapping(value = "/{username}/earningsAndDeductions/{payrollPeriod}", method = RequestMethod.GET)
  public ResponseEntity<EarningsAndDeductionsDTO> getEarningsAndDeductionsByPeriod(
      @PathVariable("username") String username,
      @PathVariable("payrollPeriod") String payrollPeriod)
      throws SystemException, BusinessException {

    EarningsAndDeductionsDTO earningsAndDeductions = payrollInformationService
        .getEarningsAndDeductionsByPeriod(username, payrollPeriod);

    return new ResponseEntity<EarningsAndDeductionsDTO>(earningsAndDeductions, HttpStatus.OK);
  }

  @RequestMapping(value = "/bankDetails/{username}", method = RequestMethod.GET)
  public ResponseEntity<List<BankDetailsDTO>> getBankDetails(
      @PathVariable("username") String username) throws SystemException, BusinessException {

    List<BankDetailsDTO> bankDetails = payrollInformationService.getBankDetails(username);

    return new ResponseEntity<List<BankDetailsDTO>>(bankDetails, HttpStatus.OK);
  }

  @RequestMapping(value = "/request/salaryCertificate/{username}", method = RequestMethod.POST)
  public ResponseEntity<SalaryCertificateRequestDTO> submitSalaryCertificateRequest(
      @PathVariable("username") String username,
      @RequestBody SalaryCertificateRequestDTO salaryCertificateRequestDTO) {

    SalaryCertificateRequestDTO salaryCertificateRequest = payrollInformationService
        .submitSalaryCertificateRequest(username, salaryCertificateRequestDTO);

    return new ResponseEntity<SalaryCertificateRequestDTO>(salaryCertificateRequest, HttpStatus.OK);
  }

  @RequestMapping(value = "/request/changeBankDetails/{username}", method = RequestMethod.POST)
  public ResponseEntity<Boolean> submitChangeBankDetailsRequest(
      @PathVariable("username") String username, @RequestBody BankDetailsDTO bankDetailsDTO)
      throws SystemException, BusinessException {
    Boolean bankDetailsChanged = payrollInformationService
        .submitChangeBankDetailsRequestWithApproval(username, bankDetailsDTO);

    return new ResponseEntity<Boolean>(bankDetailsChanged, HttpStatus.OK);
  }

}
