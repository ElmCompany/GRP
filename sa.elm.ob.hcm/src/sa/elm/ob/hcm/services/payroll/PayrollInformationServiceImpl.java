package sa.elm.ob.hcm.services.payroll;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbravo.dal.core.OBContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sa.elm.ob.hcm.EHCMEarnDeductElm;
import sa.elm.ob.hcm.EHCMEarnDeductEmp;
import sa.elm.ob.hcm.EHCMPayrollDefinition;
import sa.elm.ob.hcm.EHCMPayrollProcessHdr;
import sa.elm.ob.hcm.EHCMPayrollProcessLne;
import sa.elm.ob.hcm.EHCMPayrolldefPeriod;
import sa.elm.ob.hcm.EHCMPpmBankdetail;
import sa.elm.ob.hcm.EhcmChangeBank;
import sa.elm.ob.hcm.EhcmChangeBankV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.ChangeBank.ChangeBankProcessDAO;
import sa.elm.ob.hcm.dao.managerSelfService.ManagerSelfServiceDAO;
import sa.elm.ob.hcm.dao.payrollInformation.PayrollInformationDAO;
import sa.elm.ob.hcm.dao.profile.EmployeeProfileDAO;
import sa.elm.ob.hcm.dto.payroll.BankDetailsDTO;
import sa.elm.ob.hcm.dto.payroll.EarningsAndDeductionsDTO;
import sa.elm.ob.hcm.dto.payroll.EmployeeInformationDTO;
import sa.elm.ob.hcm.dto.payroll.PaySlipDTO;
import sa.elm.ob.hcm.dto.payroll.SalaryCertificateRequestDTO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.selfservice.security.SecurityUtils;
import sa.elm.ob.hcm.services.workflow.WorkflowUtilityService;
import sa.elm.ob.hcm.util.MessageKeys;
import sa.elm.ob.utility.util.DateUtils;
import sa.elm.ob.utility.util.UtilityDAO;

@Service("payrollInformationService")
public class PayrollInformationServiceImpl implements PayrollInformationService {

  private static final String OPEN_BRAVO_YEAR_DATE_FORMAT = "yyyy-MM-dd";
  private final String CHANGE_BANK_DETAILS_WORKFLOW_KEY = "changeBankDetailsWorkflow";
  @Autowired
  private EmployeeProfileDAO employeeProfileDAO;

  @Autowired
  private PayrollInformationDAO payrollInformationDAO;

  @Autowired
  ManagerSelfServiceDAO managerSelfServiceDAO;

  @Autowired
  WorkflowUtilityService workflowUtilityService;

  @Override
  public PaySlipDTO getPaySlipInformation(String username, String payrollId)
      throws SystemException, BusinessException {

    PaySlipDTO paySlip = new PaySlipDTO();
    EmployeeInformationDTO employeeInformationDTO = getEmployeeInformation(username, payrollId);
    EarningsAndDeductionsDTO earningsAndDeductionsDTO = getEarningsAndDeductionsByPeriod(username,
        payrollId);
    paySlip.setEmpInfo(employeeInformationDTO);
    paySlip.setSalaryDetails(earningsAndDeductionsDTO);
    return paySlip;
  }

  /**
   * Retrieve Employee Information by userName
   * 
   * @param username
   * @return
   */
  private EmployeeInformationDTO getEmployeeInformation(String username, String payrollId)
      throws SystemException, BusinessException {

    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);
    EmploymentInfo ehcmEmploymentInfo = managerSelfServiceDAO.findActiveEmploymentInfo(employeeOB);
    EHCMPayrollDefinition payrollDefinition = ehcmEmploymentInfo.getEhcmPayrollDefinition();
    EHCMPayrolldefPeriod payrollPeriod = payrollInformationDAO.getPayrollPeriod(payrollDefinition,
        payrollId);
    List<EHCMPayrollProcessHdr> payrollProcess = payrollInformationDAO
        .getPayRollProcess(payrollDefinition, payrollPeriod);
    return mapEmployeeInformationDTO(employeeOB, ehcmEmploymentInfo, payrollProcess.get(0));
  }

  /**
   * Fill Employee details with payroll details
   * 
   * @param employeeOB
   * @param ehcmEmploymentInfo
   * @param payrollProcess
   * @return
   */
  private EmployeeInformationDTO mapEmployeeInformationDTO(EhcmEmpPerInfo employeeOB,
      EmploymentInfo ehcmEmploymentInfo, EHCMPayrollProcessHdr payrollProcess) {
    // TODO Auto-generated method stub
    EmployeeInformationDTO employeeInformationDTO = new EmployeeInformationDTO();
    try {
      OBContext.setAdminMode();
      employeeInformationDTO.setAssignedDept(ehcmEmploymentInfo.getAssignedDepartment() == null ? ""
          : ehcmEmploymentInfo.getAssignedDepartment().getName());
      employeeInformationDTO.setDepartment(employeeOB.getDeptCode());
      employeeInformationDTO.setEmpName(employeeOB.getName());
      employeeInformationDTO.setEmpNumber(employeeOB.getSearchKey());
      employeeInformationDTO.setEmpType(employeeOB.getPersonType().getPersonType());
      employeeInformationDTO.setGrade(ehcmEmploymentInfo.getGrade().getCommercialName());
      employeeInformationDTO.setHireDate(UtilityDAO.convertTohijriDate(
          DateUtils.convertDateToString(OPEN_BRAVO_YEAR_DATE_FORMAT, employeeOB.getHiredate())));
      employeeInformationDTO.setJobTitle(ehcmEmploymentInfo.getJobtitle());
      employeeInformationDTO.setSection(ehcmEmploymentInfo.getSectionName());
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }

    return employeeInformationDTO;
  }

  @Override
  public EarningsAndDeductionsDTO getEarningsAndDeductionsByPeriod(String username,
      String payrollPeriod) throws SystemException, BusinessException {
    // TODO Auto-generated method stub
    EhcmEmpPerInfo employeeOB = employeeProfileDAO.getEmployeeProfileByUser(username);
    EmploymentInfo ehcmEmploymentInfo = managerSelfServiceDAO.findActiveEmploymentInfo(employeeOB);
    EHCMPayrollDefinition payrollDefinition = ehcmEmploymentInfo.getEhcmPayrollDefinition();
    EHCMPayrolldefPeriod payrollPeriodOB = payrollInformationDAO.getPayrollPeriod(payrollDefinition,
        payrollPeriod);
    List<EHCMPayrollProcessHdr> payrollProcess = payrollInformationDAO
        .getPayRollProcess(payrollDefinition, payrollPeriodOB);
    EarningsAndDeductionsDTO earningsAndDeductionsDTO = mapEarningsAndDeductions(payrollProcess,
        employeeOB);
    earningsAndDeductionsDTO.setPayrollPeriod(payrollPeriod);
    earningsAndDeductionsDTO
        .setPayrollType(payrollDefinition.getEhcmPayrollPeriodTypes().getPeriodtypename());
    return earningsAndDeductionsDTO;
  }

  /**
   * Fill Earnings and Deduction Details
   * 
   * @param payrollProcess
   * @param employeeOB
   * @return
   */
  private EarningsAndDeductionsDTO mapEarningsAndDeductions(
      List<EHCMPayrollProcessHdr> payrollProcess, EhcmEmpPerInfo employeeOB) {
    // TODO Auto-generated method stub
    EarningsAndDeductionsDTO earningsAndDeductionsDTO = new EarningsAndDeductionsDTO();
    HashMap<String, BigDecimal> earingsMap = new HashMap<String, BigDecimal>();
    HashMap<String, BigDecimal> deductionMap = new HashMap<String, BigDecimal>();
    BigDecimal basicSalary = BigDecimal.ZERO;
    BigDecimal totalEarning = BigDecimal.ZERO;
    BigDecimal grossSalary = BigDecimal.ZERO;
    BigDecimal pension = BigDecimal.ZERO;
    BigDecimal totalDeduction = BigDecimal.ZERO;
    BigDecimal netSalary = BigDecimal.ZERO;
    String payrollStatus = "";

    for (EHCMPayrollProcessHdr payrollProcessHdr : payrollProcess) {
      // find payroll definition
      payrollStatus = payrollProcessHdr.getStatus();

      // iterate employee lines in payroll
      for (EHCMPayrollProcessLne processLine : payrollProcessHdr.getEHCMPayrollProcessLneList())
        if (processLine.getEmployee().equals(employeeOB)) {
          // find employee earnings and deductions summary
          basicSalary = processLine.getBasic().add(basicSalary);
          totalEarning = processLine.getGrossSalary().add(totalEarning);
          grossSalary = processLine.getGrossSalary().add(grossSalary);
          pension = processLine.getPension().add(pension);
          totalDeduction = processLine.getTotalDeduction().add(totalDeduction);
          netSalary = processLine.getNetSalary().add(netSalary);
          EHCMEarnDeductEmp employeeEaringAndDeduction = processLine
              .getEarningAndDeductionDetails();
          if (employeeEaringAndDeduction != null) {
            // find earnings and deductions
            for (EHCMEarnDeductElm elmementEaringAndDedutionOB : employeeEaringAndDeduction
                .getEHCMEarnDeductElmList()) {
              // fill earning details in map
              if (!elmementEaringAndDedutionOB.isDeduction()) {
                if (!earingsMap.isEmpty()) {
                  for (String element : earingsMap.keySet()) {
                    if (element.equals(elmementEaringAndDedutionOB.getElementType().getName())) {
                      earingsMap.put(element, elmementEaringAndDedutionOB.getCalculatedValue()
                          .add(earingsMap.get(element)));
                    } else {
                      earingsMap.put(element, elmementEaringAndDedutionOB.getCalculatedValue()
                          .add(earingsMap.get(element)));
                    }
                  }
                } else {
                  earingsMap.put(elmementEaringAndDedutionOB.getElementType().getName(),
                      elmementEaringAndDedutionOB.getCalculatedValue());
                }

              } else { // fill deduction details in map
                if (!deductionMap.isEmpty()) {
                  for (String element : deductionMap.keySet()) {
                    if (element.equals(elmementEaringAndDedutionOB.getElementType().getName())) {
                      deductionMap.put(element, elmementEaringAndDedutionOB.getCalculatedValue()
                          .add(earingsMap.get(element)));
                    } else {
                      deductionMap.put(element, elmementEaringAndDedutionOB.getCalculatedValue()
                          .add(earingsMap.get(element)));
                    }
                  }
                } else {
                  deductionMap.put(elmementEaringAndDedutionOB.getElementType().getName(),
                      elmementEaringAndDedutionOB.getCalculatedValue());
                }
              }
            }
          }
        }
    }
    earningsAndDeductionsDTO.setEarnings(earingsMap);
    earningsAndDeductionsDTO.setDeduction(deductionMap);
    earningsAndDeductionsDTO.setBasicSalary(basicSalary);
    earningsAndDeductionsDTO.setGrossSalary(grossSalary);
    earningsAndDeductionsDTO.setNetSalary(netSalary);
    earningsAndDeductionsDTO.setPension(pension);
    earningsAndDeductionsDTO.setTotalDeductions(totalDeduction);
    earningsAndDeductionsDTO.setTotalEarnings(totalEarning);
    earningsAndDeductionsDTO.setPayrollStatus(payrollStatus);
    return earningsAndDeductionsDTO;
  }

  @Override
  public List<BankDetailsDTO> getBankDetails(String username)
      throws BusinessException, SystemException {
    // TODO Auto-generated method stub
    final String userLang = SecurityUtils.getUserLanguage();
    List<EHCMPpmBankdetail> bankDetailsList = payrollInformationDAO.getPaymentBankDetails(username);
    if (bankDetailsList.size() == 0) {
      throw new BusinessException(
          Resource.getProperty(MessageKeys.BANK_DETAILS_NOT_AVAILABLE, userLang));
    }
    return mapBankDetails(bankDetailsList);
  }

  /**
   * Fill Bank Details into BankDetails DTO
   * 
   * @param bankDetailsList
   * @return
   */
  private List<BankDetailsDTO> mapBankDetails(List<EHCMPpmBankdetail> bankDetailsList) {
    // TODO Auto-generated method stub
    List<BankDetailsDTO> bankDetailsDTOList = new ArrayList<BankDetailsDTO>();
    BankDetailsDTO bankDetailsDTO = null;
    try {
      OBContext.setAdminMode();
      for (EHCMPpmBankdetail bankdetail : bankDetailsList) {
        bankDetailsDTO = new BankDetailsDTO();
        bankDetailsDTO.setAccountName(bankdetail.getAccountName());
        bankDetailsDTO.setBankId(bankdetail.getEfinBank().getBankCode());
        bankDetailsDTO.setBranch(bankdetail.getBankBranch().getBranchName());
        if (bankdetail.getStartDate() != null)
          bankDetailsDTO.setDateFrom(UtilityDAO.convertTohijriDate(DateUtils
              .convertDateToString(OPEN_BRAVO_YEAR_DATE_FORMAT, bankdetail.getStartDate())));
        if (bankdetail.getEndDate() != null)
          bankDetailsDTO.setDateTo(UtilityDAO.convertTohijriDate(
              DateUtils.convertDateToString(OPEN_BRAVO_YEAR_DATE_FORMAT, bankdetail.getEndDate())));
        // bankDetailsDTO.setEHCMPpmBankdetail(bankdetail);
        bankDetailsDTO.setIban(bankdetail.getAccountNumber());
        bankDetailsDTO.setName(bankdetail.getEfinBank().getBankname());
        bankDetailsDTOList.add(bankDetailsDTO);
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }
    return bankDetailsDTOList;
  }

  @Override
  public SalaryCertificateRequestDTO submitSalaryCertificateRequest(String username,
      SalaryCertificateRequestDTO salaryCertificateRequestDTO) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Boolean submitChangeBankDetailsRequest(String username, BankDetailsDTO bankDetailsDTO)
      throws BusinessException, SystemException {
    // TODO Auto-generated method stub
    EhcmChangeBank changeBankOB = createChangeBankDetails(username, bankDetailsDTO);
    // Make Transaction completed
    completeChangeBankProcess(changeBankOB);
    return true;
  }

  private EhcmChangeBank createChangeBankDetails(String username, BankDetailsDTO bankDetailsDTO)
      throws SystemException, BusinessException {
    final String userLang = SecurityUtils.getUserLanguage();
    EhcmChangeBank changeBankOB = null;
    try {
      OBContext.setAdminMode();
      // create record into change bank table (Transaction Table)
      changeBankOB = payrollInformationDAO.createChangeBank(username, bankDetailsDTO);
      if (changeBankOB == null) {
        throw new BusinessException(
            Resource.getProperty(MessageKeys.CHANGE_BANK_NOT_CREATED, userLang));
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new SystemException(Resource.getProperty(MessageKeys.ERROR, userLang));
    } finally {
      OBContext.restorePreviousMode();
    }
    return changeBankOB;
  }

  /**
   * Insert Data into Bank Details under Employee Payment Method
   * 
   * @param changeBankOB
   */
  public void completeChangeBankProcess(EhcmChangeBank changeBankOB) {

    EhcmChangeBankV changebankviewOB = changeBankOB.getExistingBank();
    // ChangeBankProcessDAO.changeProcess(changeBankOB);
    ChangeBankProcessDAO.insertEmployeeBankDetail(changeBankOB, changebankviewOB);
    ChangeBankProcessDAO.updateEffectivedate(changeBankOB, changebankviewOB);
  }

  @Override
  public Boolean submitChangeBankDetailsRequestWithApproval(String username,
      BankDetailsDTO bankDetailsDTO) throws BusinessException, SystemException {
    final String userLang = SecurityUtils.getUserLanguage();
    EhcmChangeBank changeBankOB = createChangeBankDetails(username, bankDetailsDTO);
    if (changeBankOB == null) {
      throw new BusinessException(
          Resource.getProperty(MessageKeys.CHANGE_BANK_NOT_CREATED, userLang));
    }

    Map<String, Object> variablesMap = new HashMap<String, Object>();
    variablesMap.put("username", username);
    variablesMap.put("changeBankId", changeBankOB.getId());

    workflowUtilityService.startWorkflow(CHANGE_BANK_DETAILS_WORKFLOW_KEY, variablesMap);
    return true;
  }

  public EhcmChangeBank getChangeBankById(String id) {
    return payrollInformationDAO.getChangeBankById(id);
  }

}
