package sa.elm.ob.hcm.dao.payrollInformation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.axis.utils.StringUtils;
import org.hibernate.SQLQuery;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EHCMPayrollDefinition;
import sa.elm.ob.hcm.EHCMPayrollProcessHdr;
import sa.elm.ob.hcm.EHCMPayrolldefPeriod;
import sa.elm.ob.hcm.EHCMPersonalPaymethd;
import sa.elm.ob.hcm.EHCMPpmBankdetail;
import sa.elm.ob.hcm.EhcmChangeBank;
import sa.elm.ob.hcm.EhcmChangeBankV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.dto.payroll.BankDetailsDTO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.util.MessageKeys;
import sa.elm.ob.utility.util.DateUtils;
import sa.elm.ob.utility.util.Utility;

@Repository
public class PayrollInformationDAOImpl implements PayrollInformationDAO {
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(PayrollInformationDAOImpl.class);
  private static final String OPEN_BRAVO_GREG_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
  @Autowired
  EmployeeProfileDAO employeeProfileDAO;

  @Override
  public EHCMPayrolldefPeriod getPayrollPeriod(EHCMPayrollDefinition payrollDefinition,
      String payrollId) throws SystemException, BusinessException {
    // TODO Auto-generated method stub
    final String userLang = SecurityUtils.getUserLanguage();
    EHCMPayrolldefPeriod payrollPeriod = null;
    OBQuery<EHCMPayrolldefPeriod> payrollPeriodQry = null;
    List<EHCMPayrolldefPeriod> payrllPeriodList = new ArrayList<EHCMPayrolldefPeriod>();
    try {
      OBContext.setAdminMode(true);
      final String query = " as e where e.ehcmPayrollDefinition.id=:payRollDefinitionId and (e.id=:id or e.ehcmPeriod=:periodName)";
      payrollPeriodQry = OBDal.getInstance().createQuery(EHCMPayrolldefPeriod.class, query);
      payrollPeriodQry.setNamedParameter("payRollDefinitionId",
          payrollDefinition == null ? "" : payrollDefinition.getId());
      payrollPeriodQry.setNamedParameter("id", payrollId);
      payrollPeriodQry.setNamedParameter("periodName", payrollId);
      payrollPeriodQry.setMaxResult(1);
      payrollPeriodQry.setFilterOnReadableOrganization(false);
      payrollPeriodQry.setFilterOnReadableClients(false);
      payrllPeriodList = payrollPeriodQry.list();
      if (payrllPeriodList.size() > 0) {
        payrollPeriod = payrllPeriodList.get(0);
      }
      if (payrollPeriod == null) {
        throw new BusinessException(
            Resource.getProperty(MessageKeys.PAYROLL_PERIOD_NOT_AVAILABLE, userLang));
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Error while PayrollPeriod details For payroll period ==>" + payrollId);
      throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }
    return payrollPeriod;
  }

  @Override
  public List<EHCMPayrollProcessHdr> getPayRollProcess(EHCMPayrollDefinition payrollDefinition,
      EHCMPayrolldefPeriod payrollPeriod) throws SystemException, BusinessException {
    // TODO Auto-generated method stub
    final String userLang = SecurityUtils.getUserLanguage();
    OBQuery<EHCMPayrollProcessHdr> payrollProcessQry = null;
    List<EHCMPayrollProcessHdr> payrollProcessList = new ArrayList<EHCMPayrollProcessHdr>();
    try {
      OBContext.setAdminMode();
      final String query = " as e where e.payrollPeriod.id=:periodId and e.payroll.id=:payrollDefinitionId)";
      payrollProcessQry = OBDal.getInstance().createQuery(EHCMPayrollProcessHdr.class, query);
      payrollProcessQry.setNamedParameter("periodId",
          payrollPeriod == null ? "" : payrollPeriod.getId());
      payrollProcessQry.setNamedParameter("payrollDefinitionId",
          payrollDefinition == null ? "" : payrollDefinition.getId());
      payrollProcessQry.setFilterOnReadableClients(false);
      payrollProcessQry.setFilterOnReadableOrganization(false);
      if (payrollProcessQry.list().size() > 0) {
        payrollProcessList = payrollProcessQry.list();
      } else {
        throw new BusinessException(
            Resource.getProperty(MessageKeys.PAYROLL_PROCESS_NOT_AVAILABLE, userLang));
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Error while finding payroll process for payroll period  "
          + payrollPeriod.getEhcmPeriod());
      throw new BusinessException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }
    return payrollProcessList;
  }

  @Override
  public List<EHCMPpmBankdetail> getPaymentBankDetails(String username)
      throws BusinessException, SystemException {
    // TODO Auto-generated method stub
    List<EHCMPpmBankdetail> bankDetailsList = new ArrayList<EHCMPpmBankdetail>();
    final String userLang = SecurityUtils.getUserLanguage();
    // find employee
    try {
      OBContext.setAdminMode();
      EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);
      // find payment method of employees
      List<EHCMPersonalPaymethd> paymentMethodList = employeeOB.getEHCMPersonalPaymethdList();
      if (paymentMethodList.size() == 0) {
        throw new BusinessException(
            Resource.getProperty(MessageKeys.BANK_DETAILS_NOT_AVAILABLE, userLang));
      }
      for (EHCMPersonalPaymethd personalPaymentMethod : paymentMethodList) {
        for (EHCMPpmBankdetail bankDetail : personalPaymentMethod.getEHCMPpmBankdetailList()) {
          if (bankDetail.getPercentage().compareTo(BigDecimal.ZERO) > 0) {
            bankDetailsList.add(bankDetail);
          }
        }
      }
    } catch (Exception e) {
      throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }

    return bankDetailsList;
  }

  @Override
  public EhcmChangeBank createChangeBank(String username, BankDetailsDTO bankDetailsDTO)
      throws BusinessException, SystemException {
    // TODO Auto-generated method stub
    final String userLang = SecurityUtils.getUserLanguage();
    EhcmChangeBank changeBankOB = null;
    try {
      OBContext.setAdminMode();
      // convert effective date from hijri to gregorian
      Date effectiveDate = null;
      if (!StringUtils.isEmpty(bankDetailsDTO.getDateFrom())) {
        effectiveDate = DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
            Utility.convertToGregorian(bankDetailsDTO.getDateFrom()));
      }
      changeBankOB = OBProvider.getInstance().get(EhcmChangeBank.class);
      // fill employee details
      EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);
      fillEmployeeDetailsInChangeBank(changeBankOB, employeeOB);
      // get existing bank details
      EHCMPpmBankdetail existingBankOB = getExistingBankDetails(bankDetailsDTO.getName(),
          employeeOB);
      // pre -validations
      changeBankValidations(existingBankOB, bankDetailsDTO, effectiveDate);
      changeBankOB.setPaymentType(existingBankOB.getEhcmPersonalPaymethd().getCode());
      // changeBankOB.setBankBranch(existingBankOB.); //need to ask that
      // whether bank branch will be string or
      // Table reference
      // fill existing bank to change
      changeBankOB
          .setExistingBank(OBDal.getInstance().get(EhcmChangeBankV.class, existingBankOB.getId()));
      changeBankOB.setStartDate(existingBankOB.getStartDate());
      // fill change bank details
      changeBankOB.setBankCode(existingBankOB.getEfinBank());
      changeBankOB.setBankName(existingBankOB.getEfinBank().getBankname());
      changeBankOB.setEffectiveDate(effectiveDate);
      changeBankOB.setPercentage(existingBankOB.getPercentage());
      // changeBankOB.setDateTo(DateUtils.convertStringToDate(OPEN_BRAVO_GREG_DATE_FORMAT,
      // Utility.convertToGregorian(bankDetailsDTO.getDateFrom()))); //need to ask where to set date
      // to
      // because core change bank work flow did not have date to in change bank process

      if (!StringUtils.isEmpty(bankDetailsDTO.getIban())) {
        changeBankOB.setAccountNumber(bankDetailsDTO.getIban());
      }
      if (!StringUtils.isEmpty(bankDetailsDTO.getAccountName())) {
        changeBankOB.setAccountName(bankDetailsDTO.getAccountName());
      }
      OBDal.getInstance().save(changeBankOB);
      OBDal.getInstance().flush();

    } catch (Exception e) {
      e.printStackTrace();
      throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }
    return changeBankOB;
  }

  /**
   * Change Bank Process Pre-Validations
   * 
   * @param existingBankOB
   * @param bankDetailsDTO
   * @param effectiveDate
   * @throws BusinessException
   * @throws SystemException
   */
  private void changeBankValidations(EHCMPpmBankdetail existingBankOB,
      BankDetailsDTO bankDetailsDTO, Date effectiveDate) throws BusinessException, SystemException {
    // TODO Auto-generated method stub
    final String userLang = SecurityUtils.getUserLanguage();
    if (existingBankOB == null)
      throw new BusinessException(
          Resource.getProperty(MessageKeys.BANK_DETAILS_NOT_AVAILABLE, userLang));
    // validation with dates
    if (existingBankOB.getStartDate().compareTo(effectiveDate) >= 0) {
      throw new BusinessException(Resource.getProperty(MessageKeys.DATE_FROM_NOT_VALID, userLang));
    }
    if (!StringUtils.isEmpty(bankDetailsDTO.getIban())) {
      String checkIBan = checkIban(bankDetailsDTO.getIban());
      if (checkIBan.equals("failed")) {
        throw new BusinessException(
            Resource.getProperty(MessageKeys.NOT_VALID_ACCOUNT_NUMBER, userLang));
      }
    }

  }

  /**
   * Get Existing Bank For Given Bank
   * 
   * @param bankName
   * @return
   */
  private EHCMPpmBankdetail getExistingBankDetails(String bankName, EhcmEmpPerInfo employeeOB) {
    // TODO Auto-generated method stub

    EHCMPpmBankdetail existingBankOB = null;
    try {
      OBContext.setAdminMode();
      OBQuery<EHCMPpmBankdetail> bankQuery = OBDal.getInstance().createQuery(
          EHCMPpmBankdetail.class,
          " as e where e.ehcmPersonalPaymethd.ehcmEmpPerinfo.id=:employeeId and e.efinBank.bankname=:bankName and e.endDate is null");
      bankQuery.setNamedParameter("employeeId", employeeOB.getId());
      bankQuery.setNamedParameter("bankName", bankName);
      bankQuery.setFilterOnReadableClients(false);
      bankQuery.setFilterOnReadableOrganization(false);
      if (bankQuery.list().size() > 0) {
        existingBankOB = bankQuery.list().get(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.debug("Erro while fetching Bank details for Bank--->" + bankName);
    } finally {
      OBContext.restorePreviousMode();
    }

    return existingBankOB;
  }

  private void fillEmployeeDetailsInChangeBank(EhcmChangeBank changeBankOB,
      EhcmEmpPerInfo employeeOB) {
    // TODO Auto-generated method stub
    // find user
    User userOB = employeeProfileDAO.getUserDetailsByUserName(employeeOB.getSearchKey());
    // find employment info by employee id
    EmploymentInfo employmentInfoOB = findEmploymentInfo(employeeOB.getId());
    // fill employee details in change bank
    changeBankOB.setOrganization(employeeOB.getOrganization());
    changeBankOB.setClient(employeeOB.getClient());
    changeBankOB.setCreationDate(new java.util.Date());
    changeBankOB.setCreatedBy(userOB);
    changeBankOB.setUpdated(new java.util.Date());
    changeBankOB.setUpdatedBy(userOB);
    //changeBankOB.setAssignedDepartmen(employmentInfoOB.getAssignedDepartment());
     
    changeBankOB.setEmployeeType(employeeOB.getEhcmActiontype().getPersonType());
    changeBankOB.setEmployeeName(employeeOB.getArabicfullname());
    changeBankOB.setHireDate(employeeOB.getHiredate());
    changeBankOB.setSectionCode(employmentInfoOB.getPosition().getSection());
    changeBankOB.setPosition(employmentInfoOB.getPosition());
    changeBankOB.setGrade(employmentInfoOB.getGrade());
    changeBankOB.setEmploymentGrade(employmentInfoOB.getEmploymentgrade());
    changeBankOB.setGradeClassifications(employeeOB.getGradeClass());
    changeBankOB.setEmployee(employeeOB);
  }

  /**
   * find employment info by employee id
   * 
   * @param employeeId
   * @return
   */
  private EmploymentInfo findEmploymentInfo(String employeeId) {

    EmploymentInfo employmentInfoOB = null;
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id='" + employeeId + "' and enabled='Y' order by creationDate desc ");
      empInfo.setFilterOnReadableClients(false);
      empInfo.setFilterOnReadableOrganization(false);
      if (empInfo.list().size() > 0) {
        employmentInfoOB = empInfo.list().get(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.debug("Erro while fetchin employment info for employee--->" + employeeId);
    } finally {
      OBContext.restorePreviousMode();
    }

    return employmentInfoOB;
  }

  /**
   * 
   * @param accountnumber
   * @return String.
   */
  // Check the account number is Iban
  public String checkIban(String accountnumber) {
    String accnum = null;

    try {
      OBContext.setAdminMode();
      SQLQuery Query = OBDal.getInstance().getSession()
          .createSQLQuery("select ehcm_changebank_account_num(?);");

      Query.setParameter(0, accountnumber);

      if (Query.list().size() > 0) {
        Object row = (Object) Query.list().get(0);
        accnum = (String) row;
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return accnum;
  }

  public EhcmChangeBank getChangeBankById(String id) {

    EhcmChangeBank bank = null;
    try {
      OBContext.setAdminMode();
      bank = OBDal.getInstance().get(EhcmChangeBank.class, id);
    } catch (Exception e) {
      e.printStackTrace();
      log.debug("Erro while fetchin change bank info for id--->" + id);
    } finally {
      OBContext.restorePreviousMode();
    }
    return bank;
  }
}
