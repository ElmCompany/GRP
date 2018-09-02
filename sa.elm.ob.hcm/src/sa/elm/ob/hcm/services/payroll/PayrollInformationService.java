package sa.elm.ob.hcm.services.payroll;

import java.util.List;

import sa.elm.ob.hcm.EhcmChangeBank;
import sa.elm.ob.hcm.dto.payroll.BankDetailsDTO;
import sa.elm.ob.hcm.dto.payroll.EarningsAndDeductionsDTO;
import sa.elm.ob.hcm.dto.payroll.PaySlipDTO;
import sa.elm.ob.hcm.dto.payroll.SalaryCertificateRequestDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

/**
 * Payroll Information Service
 *
 */
public interface PayrollInformationService {

  /**
   * Get All the Information for Pay Slip including Employment Information, Earnings, Deductions and
   * Summary
   * 
   * @param username
   * @return
   */
  PaySlipDTO getPaySlipInformation(String username, String payrollPeriod)
      throws SystemException, BusinessException;

  /**
   * Get only Earnings and Deductions based on Payroll Period.
   * 
   * @param payrollPeriod
   * @return
   */
  EarningsAndDeductionsDTO getEarningsAndDeductionsByPeriod(String username, String payrollPeriod)
      throws SystemException, BusinessException;

  /**
   * Get bank details of employee where payment percentage more than 0%
   * 
   * @param username
   * @return
   */
  List<BankDetailsDTO> getBankDetails(String username) throws SystemException, BusinessException;

  /**
   * @param username
   * @param salaryCertificateRequestDTO
   * @return
   */
  SalaryCertificateRequestDTO submitSalaryCertificateRequest(String username,
      SalaryCertificateRequestDTO salaryCertificateRequestDTO);

  /**
   * Submit change bank request details
   * 
   * @param username
   * @param bankDetailsDTO
   * @return
   */
  Boolean submitChangeBankDetailsRequest(String username, BankDetailsDTO bankDetailsDTO)
      throws BusinessException, SystemException;

  /**
   * Submit change bank request details with approval
   * 
   * @param username
   * @param bankDetailsDTO
   * @return
   */
  Boolean submitChangeBankDetailsRequestWithApproval(String username, BankDetailsDTO bankDetailsDTO)
      throws BusinessException, SystemException;

  /**
   * @param changeBankOB
   */
  void completeChangeBankProcess(EhcmChangeBank changeBankOB);

  /**
   * @param id
   * @return
   */
  EhcmChangeBank getChangeBankById(String id);

}
