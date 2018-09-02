package sa.elm.ob.hcm.ad_forms.personalpaymentmethod.dao;

import java.math.BigDecimal;
import java.util.List;

import sa.elm.ob.hcm.ad_forms.personalpaymentmethod.vo.PersonalPaymentMethodVO;

/**
 * Interface for all Personal Payment Method related DB Operations
 * 
 * @author Priyanka Ranjan 23-03-2018
 *
 */

public interface PaymentMethodDAO {
  /**
   * 
   * @param clientId
   * @return
   * @throws Exception
   */
  List<PersonalPaymentMethodVO> getpaymenttypecode(String clientId) throws Exception;

  /**
   * 
   * @param clientId
   * @return
   * @throws Exception
   */
  List<PersonalPaymentMethodVO> getcurrency(String clientId, String paycode) throws Exception;

  /**
   * 
   * @param clientId
   * @param PersonalPaymethdId
   * @return
   * @throws Exception
   */
  List<PersonalPaymentMethodVO> getbankdetaillist(String clientId, String PersonalPaymethdId,
      String searchFlag, PersonalPaymentMethodVO vo, String sortColName, String sortColType)
      throws Exception;

  /**
   * 
   * @param clientId
   * @return
   * @throws Exception
   */
  List<PersonalPaymentMethodVO> getbankname(String clientId) throws Exception;

  /**
   * 
   * @param clientId
   * @param userId
   * @param vo
   * @return
   * @throws Exception
   */
  String addPerPayMethod(String clientId, String userId, PersonalPaymentMethodVO vo)
      throws Exception;

  /**
   * 
   * @param clientId
   * @param userId
   * @param vo
   * @param perpaymethodId
   * @return
   * @throws Exception
   */
  String updateaddPerPayMethod(String clientId, String userId, PersonalPaymentMethodVO vo,
      String perpaymethodId) throws Exception;

  /**
   * 
   * @param clientId
   * @param EmployeeId
   * @return
   * @throws Exception
   */
  List<PersonalPaymentMethodVO> GetPaymentMethodList(String clientId, String EmployeeId,
      String searchFlag, PersonalPaymentMethodVO vo, String sortColName, String sortColType)
      throws Exception;

  /**
   * 
   * @param payrollpaytypemethodId
   * @return
   * @throws Exception
   */
  List<PersonalPaymentMethodVO> Getpayrollpaymentdetailrecords(String payrollpaytypemethodId)
      throws Exception;

  /**
   * 
   * @param clientId
   * @param PersonalPaymethdId
   * @param searchFlag
   * @param vo
   * @return
   * @throws Exception
   */
  int getBankdetailCount(String clientId, String PersonalPaymethdId, String searchFlag,
      PersonalPaymentMethodVO vo) throws Exception;

  /**
   * 
   * @param clientId
   * @param EmployeeId
   * @param searchFlag
   * @param vo
   * @return
   * @throws Exception
   */
  int getPaymentMethodCount(String clientId, String EmployeeId, String searchFlag,
      PersonalPaymentMethodVO vo) throws Exception;

  /**
   * Check Percentage Validation (total percentage for all employee payment method MUST = 100%)
   * 
   * @param percentange
   * @param paymentMethodId
   * @param clientId
   * @param bankDetailId
   * @return
   * @throws Exception
   */
  boolean checkPercentageValidation(BigDecimal percentange, String paymentMethodId, String clientId,
      String bankDetailId) throws Exception;

  /**
   * Check Already Default PersonalPayementMethod Exist
   * 
   * @param employeeId
   * @param perpaymethodId
   * @param clientId
   * @return
   * @throws Exception
   */
  boolean checkDefaultPersonalPaymentMethodAlreadyExists(String employeeId, String perpaymethodId,
      String clientId) throws Exception;

  /**
   * Check Already PersonalPayementMethod Exist
   * 
   * @param employeeId
   * @param perpaymethodId
   * @param clientId
   * @param payCode
   * @param currency
   * @return
   * @throws Exception
   */
  boolean checkPersonalPaymentMethodAlreadyExists(String employeeId, String perpaymethodId,
      String clientId, String payCode, String currency) throws Exception;

  /**
   * Delete Personal payment method
   * 
   * @param personalPaymethdId
   * @return
   * @throws Exception
   */
  boolean deletePersonalPayment(String personalPaymethdId) throws Exception;

}
