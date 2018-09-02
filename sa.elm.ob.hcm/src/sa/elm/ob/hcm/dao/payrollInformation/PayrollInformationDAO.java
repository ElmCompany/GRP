package sa.elm.ob.hcm.dao.payrollInformation;

import java.util.List;

import sa.elm.ob.hcm.EHCMPayrollDefinition;
import sa.elm.ob.hcm.EHCMPayrollProcessHdr;
import sa.elm.ob.hcm.EHCMPayrolldefPeriod;
import sa.elm.ob.hcm.EHCMPpmBankdetail;
import sa.elm.ob.hcm.EhcmChangeBank;
import sa.elm.ob.hcm.dto.payroll.BankDetailsDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;

public interface PayrollInformationDAO {
  /**
   * Find Payroll period details for payroll definition
   * 
   * @param payrollDefinition
   * @param payrollId
   * @return
   */
  EHCMPayrolldefPeriod getPayrollPeriod(EHCMPayrollDefinition payrollDefinition, String payrollId)
      throws SystemException, BusinessException;

  /**
   * Find Payroll Process for Given Period and Payroll definition
   * 
   * @param payrollDefinition
   * @param payrollPeriod
   * @return
   */
  List<EHCMPayrollProcessHdr> getPayRollProcess(EHCMPayrollDefinition payrollDefinition,
      EHCMPayrolldefPeriod payrollPeriod) throws BusinessException, SystemException;

  List<EHCMPpmBankdetail> getPaymentBankDetails(String username)
      throws BusinessException, SystemException;

  /**
   * Insert Change Bank Request
   * 
   * @param username
   * @param bankDetailsDTO
   * @return
   */
  EhcmChangeBank createChangeBank(String username, BankDetailsDTO bankDetailsDTO)
      throws BusinessException, SystemException;

  /**
   * @param id
   * @return
   */
  EhcmChangeBank getChangeBankById(String id);

}
