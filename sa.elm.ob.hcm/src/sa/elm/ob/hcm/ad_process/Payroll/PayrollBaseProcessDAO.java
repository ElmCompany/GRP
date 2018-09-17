package sa.elm.ob.hcm.ad_process.Payroll;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

import sa.elm.ob.hcm.EHCMAbsencePayment;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMBenefitAllowance;
import sa.elm.ob.hcm.EHCMEarnDeductElm;
import sa.elm.ob.hcm.EHCMEarnDeductElmRef;
import sa.elm.ob.hcm.EHCMEarnDeductEmp;
import sa.elm.ob.hcm.EHCMEarnDeductPayroll;
import sa.elm.ob.hcm.EHCMElementFormulaHdr;
import sa.elm.ob.hcm.EHCMElementFormulaLne;
import sa.elm.ob.hcm.EHCMEligbltyCriteria;
import sa.elm.ob.hcm.EHCMElmttypeDef;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMEmpScholarship;
import sa.elm.ob.hcm.EHCMEmpSecondment;
import sa.elm.ob.hcm.EHCMHoldUnHoldSalary;
import sa.elm.ob.hcm.EHCMLoanTransaction;
import sa.elm.ob.hcm.EHCMPayrollDefinition;
import sa.elm.ob.hcm.EHCMPayrollProcessHdr;
import sa.elm.ob.hcm.EHCMPayrollProcessLne;
import sa.elm.ob.hcm.EHCMPayrolldefPeriod;
import sa.elm.ob.hcm.EHCMPayscalePointV;
import sa.elm.ob.hcm.EHCMPpmBankdetail;
import sa.elm.ob.hcm.EHCMScholarshipDedConf;
import sa.elm.ob.hcm.EHCMticketordertransaction;
import sa.elm.ob.hcm.EhcmDependents;
import sa.elm.ob.hcm.EhcmDisciplineAction;
import sa.elm.ob.hcm.EhcmElementGroup;
import sa.elm.ob.hcm.EhcmElementGroupLine;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmEmployeeOvertime;
import sa.elm.ob.hcm.EhcmLoanHistory;
import sa.elm.ob.hcm.EhcmPayrollGlobalValue;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.EmploymentGroupLines;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ehcmgradeclass;
import sa.elm.ob.hcm.ehcmgraderatelines;
import sa.elm.ob.hcm.ehcmgraderates;
import sa.elm.ob.hcm.ehcmgradesteps;
import sa.elm.ob.hcm.ehcmpayscale;
import sa.elm.ob.hcm.ehcmpayscaleline;
import sa.elm.ob.hcm.ehcmprogressionpoint;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.AbsenceDecision.AbsenceIssueDecisionDAOImpl;
import sa.elm.ob.hcm.dto.payroll.BankDetailsDTO;
import sa.elm.ob.hcm.dto.payroll.EmploymentGroupDTO;
import sa.elm.ob.hcm.dto.payroll.GenericPayrollDTO;
import sa.elm.ob.hcm.util.PayrollConstants;
import sa.elm.ob.hcm.util.UtilityDAO;
import sa.elm.ob.utility.util.Utility;

public class PayrollBaseProcessDAO {
  private Connection conn = null;
  VariablesSecureApp vars = null;
  private static Logger log = Logger.getLogger(PayrollBaseProcessDAO.class);

  public PayrollBaseProcessDAO(Connection con) {
    this.conn = con;
  }

  public static void getGlobalPreDefinedElements(String clientId) {
    try {
      // Global Values
      List<EhcmPayrollGlobalValue> globalValueList = getGlobalValues(clientId);
      for (EhcmPayrollGlobalValue globalValue : globalValueList) {
        if (globalValue.getType().equalsIgnoreCase("C")) {
          PayrollBaseProcess.payRollComponents.put(globalValue.getCode(),
              "'" + globalValue.getCharValue() + "'");
        } else if (globalValue.getType().equalsIgnoreCase("D")) {
          PayrollBaseProcess.payRollComponents.put(globalValue.getCode(),
              "new Date('" + globalValue.getDateValue() + "').toDateString()");
        } else {
          PayrollBaseProcess.payRollComponents.put(globalValue.getCode(),
              globalValue.getNumericValue());
        }
      }
    } catch (JSONException gge) {
      log.error("Error in PayrollProcess.java : getGlobalPreDefinedElements() ");
      gge.printStackTrace();
      PayrollBaseProcess.errorFlagMajor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Global Pre Defined Elements For Payroll";
    }
  }

  public static List<EhcmPayrollGlobalValue> getGlobalValues(String clientId) {
    List<EhcmPayrollGlobalValue> globalValueList = new ArrayList<EhcmPayrollGlobalValue>();
    try {
      String whereClause = "e where e.client.id=:clientId)";

      OBQuery<EhcmPayrollGlobalValue> globalValueQry = OBDal.getInstance()
          .createQuery(EhcmPayrollGlobalValue.class, whereClause);
      globalValueQry.setNamedParameter("clientId", clientId);
      globalValueList = globalValueQry.list();
      return globalValueList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getGlobalValues() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Global Values";
      return globalValueList;
    }
  }

  public static EHCMEarnDeductPayroll getEarningDeductionPayrollDetails(
      EHCMPayrollProcessHdr payrollProcessHdr, String clientId, String orgId, String userId) {
    try {
      EHCMEarnDeductPayroll earnDeduPayroll = getEarnDeductPayrollDetails(payrollProcessHdr);
      if (!PayrollBaseProcess.errorFlagMajor) {
        if (earnDeduPayroll == null) {
          earnDeduPayroll = createEarnDeductPayrollDetails(payrollProcessHdr, clientId, orgId,
              userId);
        }
      }
      return earnDeduPayroll;
    } catch (Exception rebpe) {
      log.error("Error in PayrollBaseProcessDAO.java : getEarningDeductionPayrollDetails() ");
      PayrollBaseProcess.errorFlagMajor = true;
      PayrollBaseProcess.errorMessage = "Error while getting earning and deduction payroll details";
      return null;
    }
  }

  public static EHCMEarnDeductPayroll getEarnDeductPayrollDetails(
      EHCMPayrollProcessHdr payProcessHdr) {
    EHCMEarnDeductPayroll earnDedPayrollDetails = null;
    try {
      String whereClause = " e where e.payrollPeriod.id = :periodId and e.elementGroup.id = :elementGroupId ";

      OBQuery<EHCMEarnDeductPayroll> earnDeduPayQry = OBDal.getInstance()
          .createQuery(EHCMEarnDeductPayroll.class, whereClause);
      earnDeduPayQry.setNamedParameter("periodId", payProcessHdr.getPayrollPeriod().getId());
      earnDeduPayQry.setNamedParameter("elementGroupId", payProcessHdr.getElementGroup().getId());
      earnDeduPayQry.setMaxResult(1);
      if (earnDeduPayQry.list().size() > 0) {
        earnDedPayrollDetails = earnDeduPayQry.list().get(0);
      }
      return earnDedPayrollDetails;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : getEarnDeductPayroll() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMajor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Earning Deduction Payroll Detail for Period "
          + payProcessHdr.getPayrollPeriod().getEhcmPeriod() + " Element Group "
          + payProcessHdr.getElementGroup().getName();
      return earnDedPayrollDetails;
    }
  }

  public static EHCMEarnDeductPayroll createEarnDeductPayrollDetails(
      EHCMPayrollProcessHdr payProcessHdr, String clientId, String orgId, String userId) {
    try {
      EHCMEarnDeductPayroll earnDeduPayDetail = OBProvider.getInstance()
          .get(EHCMEarnDeductPayroll.class);
      earnDeduPayDetail.setClient(OBDal.getInstance().get(Client.class, clientId));
      earnDeduPayDetail.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
      earnDeduPayDetail.setEnabled(true);
      earnDeduPayDetail.setCreatedBy(OBDal.getInstance().get(User.class, userId));
      earnDeduPayDetail.setCreationDate(new java.util.Date());
      earnDeduPayDetail.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
      earnDeduPayDetail.setUpdated(new java.util.Date());
      earnDeduPayDetail.setPayroll(payProcessHdr.getPayroll());
      earnDeduPayDetail.setPayrollPeriod(payProcessHdr.getPayrollPeriod());
      earnDeduPayDetail.setElementGroup(payProcessHdr.getElementGroup());
      OBDal.getInstance().save(earnDeduPayDetail);
      return earnDeduPayDetail;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : createEarnDeductPayrollDetails() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMajor = true;
      PayrollBaseProcess.errorMessage = "Error while creating Earning And Deduction Payroll Details for Period "
          + payProcessHdr.getPayrollPeriod().getEhcmPeriod() + " Element Group "
          + payProcessHdr.getElementGroup().getName();
      return null;
    }
  }

  public static List<EhcmEmpPerInfo> getEmployeesForPayrollProcess(EHCMPayrollDefinition payroll,
      EhcmEmpPerInfo employee, String periodStartDate, String periodEndDate, Session session) {
    try {
      String hqlString = " select distinct e.ehcmEmpPerinfo from Ehcm_Employment_Info e where e.ehcmPayrollDefinition.id = :payDefId and "
          + "((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) ";

      if (employee != null) {
        hqlString = hqlString + "and e.ehcmEmpPerinfo.id = :employeeId ";
      }

      final Query empQuery = session.createQuery(hqlString);
      empQuery.setParameter("payDefId", payroll.getId());
      empQuery.setParameter("fromdate", periodStartDate);
      empQuery.setParameter("todate", periodEndDate);
      if (employee != null) {
        empQuery.setParameter("employeeId", employee.getId());
      }

      @SuppressWarnings("unchecked")
      List<EhcmEmpPerInfo> empList = empQuery.list();
      return empList;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : getEmployeesForPayrollProcess() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMajor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Employees of Payroll Process : "
          + payroll.getPayrollName();
      return null;
    }
  }

  public static EHCMEarnDeductEmp getEarningDeductionEmployeeDetails(
      EHCMEarnDeductPayroll earnDedPayRollDetail, EhcmEmpPerInfo empPerInfo,
      EmploymentInfo latestEmployment, String startDate, String endDate, String clientId,
      String orgId, String userId) {
    try {
      EHCMEarnDeductEmp earnDeduEmployee = getEarnDeductEmployeeDetails(earnDedPayRollDetail,
          empPerInfo);

      if (!PayrollBaseProcess.errorFlagMinor && latestEmployment != null) {
        if (earnDeduEmployee == null) {
          earnDeduEmployee = createEarnDeductEmployeeDetails(empPerInfo, latestEmployment,
              earnDedPayRollDetail, clientId, orgId, userId);
        }
      }
      return earnDeduEmployee;
    } catch (Exception rebpe) {
      log.error("Error in PayrollBaseProcessDAO.java : getEarningDeductionPayrollDetails() ");
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting earning and deduction payroll details";
      return null;
    }
  }

  public static EHCMEarnDeductEmp getEarnDeductEmployeeDetails(
      EHCMEarnDeductPayroll earnDedPayRollDetail, EhcmEmpPerInfo employeeInfo) {
    EHCMEarnDeductEmp earnDedEmployeeDetails = null;
    try {
      String whereClause = " e where e.payrollDetails.id= :earnDedPayrollId and e.employee.id = :employeeId ";

      OBQuery<EHCMEarnDeductEmp> earnDeduEmpQry = OBDal.getInstance()
          .createQuery(EHCMEarnDeductEmp.class, whereClause);
      earnDeduEmpQry.setNamedParameter("earnDedPayrollId", earnDedPayRollDetail.getId());
      earnDeduEmpQry.setNamedParameter("employeeId", employeeInfo.getId());
      if (earnDeduEmpQry.list().size() > 0) {
        earnDedEmployeeDetails = earnDeduEmpQry.list().get(0);
      }
      return earnDedEmployeeDetails;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : getEarnDeductEmployeeDetails() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Earning Deduction Employee Detail for Employee "
          + employeeInfo.getName();
      return earnDedEmployeeDetails;
    }
  }

  public static EmploymentInfo getLatestEmploymentInPayPeriod(EhcmEmpPerInfo empPerInfo,
      String periodStartDate, String periodEndDate) {
    EmploymentInfo employInfo = null;
    try {
      // Note should not check active flag because active flag set to false after new employment
      String whereClause = " e where e.ehcmEmpPerinfo.id = :empPerInfo and "
          + "e.changereason in ('H', 'PR', 'PRT', 'OD',  'ID', 'ES', 'EOS', 'SEC', 'SUS', 'SUE','JWRSEC','EXSEC','COSEC','SECDLY') and "
          + "((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) "
          + "order by e.startDate desc ";

      OBQuery<EmploymentInfo> employmentQry = OBDal.getInstance().createQuery(EmploymentInfo.class,
          whereClause);
      employmentQry.setNamedParameter("empPerInfo", empPerInfo.getId());
      employmentQry.setNamedParameter("fromdate", periodStartDate);
      employmentQry.setNamedParameter("todate", periodEndDate);
      employmentQry.setMaxResult(1);
      List<EmploymentInfo> employmentList = employmentQry.list();
      if (employmentList.size() > 0) {
        employInfo = employmentList.get(0);
        return employInfo;
      }

    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getLatestEmploymentInPayPeriod() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMajor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Latest employment of Employee "
          + empPerInfo.getName();
      return null;
    }
    return employInfo;
  }

  public static EHCMEarnDeductEmp createEarnDeductEmployeeDetails(EhcmEmpPerInfo employeeInfo,
      EmploymentInfo employementInfo, EHCMEarnDeductPayroll earnDedPayroll, String clientId,
      String orgId, String userId) {
    try {
      EHCMEarnDeductEmp earnDeduEmpDetail = OBProvider.getInstance().get(EHCMEarnDeductEmp.class);
      earnDeduEmpDetail.setClient(OBDal.getInstance().get(Client.class, clientId));
      earnDeduEmpDetail.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
      earnDeduEmpDetail.setEnabled(true);
      earnDeduEmpDetail.setCreatedBy(OBDal.getInstance().get(User.class, userId));
      earnDeduEmpDetail.setCreationDate(new java.util.Date());
      earnDeduEmpDetail.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
      earnDeduEmpDetail.setUpdated(new java.util.Date());
      earnDeduEmpDetail.setEmployee(employeeInfo);
      earnDeduEmpDetail.setPayrollDetails(earnDedPayroll);
      earnDeduEmpDetail.setEmployeeType(employeeInfo.getEhcmActiontype().getPersonType());
      earnDeduEmpDetail.setEmployeeName(employeeInfo.getArabicfullname());
      earnDeduEmpDetail.setHireDate(employeeInfo.getHiredate());
      earnDeduEmpDetail.setDepartmentCode(employementInfo.getPosition().getDepartment());
      if (employementInfo.getPosition() != null
          && employementInfo.getPosition().getSection() != null) {
        earnDeduEmpDetail.setSectionCode(employementInfo.getPosition().getSection());
      }
      earnDeduEmpDetail.setGrade(employementInfo.getGrade());
      earnDeduEmpDetail.setPosition(employementInfo.getPosition());
      earnDeduEmpDetail.setJobTitle(employementInfo.getPosition().getJOBName().getJOBTitle());
      earnDeduEmpDetail.setAssignedDepartment(employementInfo.getSECDeptName());
      earnDeduEmpDetail.setEmploymentGrade(employementInfo.getEmploymentgrade());
      earnDeduEmpDetail.setGradePoint(OBDal.getInstance().get(EHCMPayscalePointV.class,
          employementInfo.getEhcmPayscaleline().getId()));
      earnDeduEmpDetail.setEmployeeCategory(employeeInfo.getGradeClass());
      if (employeeInfo.isEnabled()) {
        earnDeduEmpDetail.setEmployeeStatus(Constants.EMPSTATUS_ACTIVE);
      } else {
        earnDeduEmpDetail.setEmployeeStatus(Constants.EMPSTATUS_INACTIVE);
      }
      OBDal.getInstance().save(earnDeduEmpDetail);
      return earnDeduEmpDetail;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : createEarnDeductEmployeeDetails() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while creating Earning And Deduction Employee Details for Employee "
          + employeeInfo.getName();
      return null;
    }
  }

  public static EHCMPayrollProcessLne getPayrollProcessLineForEmployee(
      EHCMPayrollProcessHdr payrollProcessHdr, EHCMEarnDeductPayroll earnDedPayRollDetail,
      EhcmEmpPerInfo empPerInfo, EmploymentInfo latestEmployment, String startDate, String endDate,
      String clientId, String orgId, String userId) {
    // EHCMPayrollProcessLne payrollProcessLne = null;
    try {
      EHCMPayrollProcessLne payrollProcessLne = getPayrollProcessLine(payrollProcessHdr,
          empPerInfo);
      if (!PayrollBaseProcess.errorFlagMajor && latestEmployment != null) {
        if (payrollProcessLne == null) {
          payrollProcessLne = createPayrollProcessLine(empPerInfo, latestEmployment,
              payrollProcessHdr, latestEmployment, clientId, orgId, userId);
        }
        // payrollProcessLne = createPayrollProcessLine(empPerInfo, latestEmployment,
        // payrollProcessHdr, latestEmployment, clientId, orgId, userId);
      }
      return payrollProcessLne;
    } catch (Exception rebpe) {
      log.error("Error in PayrollBaseProcessDAO.java : getEarningDeductionPayrollDetails() ");
      PayrollBaseProcess.errorFlagMajor = true;
      PayrollBaseProcess.errorMessage = "Error while getting earning and deduction payroll details";
      return null;
    }
  }

  public static EHCMPayrollProcessLne getPayrollProcessLine(EHCMPayrollProcessHdr payProcessHdr,
      EhcmEmpPerInfo employeeInfo) {
    EHCMPayrollProcessLne earnDedEmployeeDetails = null;
    try {
      String whereClause = " e where e.payrollProcessHeader.id = :processHeaderId and e.employee.id = :employeeId ";

      OBQuery<EHCMPayrollProcessLne> payrollLineQry = OBDal.getInstance()
          .createQuery(EHCMPayrollProcessLne.class, whereClause);
      payrollLineQry.setNamedParameter("processHeaderId", payProcessHdr.getId());
      payrollLineQry.setNamedParameter("employeeId", employeeInfo.getId());
      if (payrollLineQry.list().size() > 0) {
        earnDedEmployeeDetails = payrollLineQry.list().get(0);
      }
      return earnDedEmployeeDetails;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : getPayrollProcessLine() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMajor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Payroll Process Line for Employee "
          + employeeInfo.getName();
      return earnDedEmployeeDetails;
    }
  }

  public static EHCMPayrollProcessLne createPayrollProcessLine(EhcmEmpPerInfo employeeInfo,
      EmploymentInfo employementInfo, EHCMPayrollProcessHdr payrollProcessHdr,
      EmploymentInfo latestEmployment, String clientId, String orgId, String userId) {
    try {
      EHCMPayrollProcessLne payrollProcessLne = OBProvider.getInstance()
          .get(EHCMPayrollProcessLne.class);
      payrollProcessLne.setClient(OBDal.getInstance().get(Client.class, clientId));
      payrollProcessLne.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
      payrollProcessLne.setEnabled(true);
      payrollProcessLne.setCreatedBy(OBDal.getInstance().get(User.class, userId));
      payrollProcessLne.setCreationDate(new java.util.Date());
      payrollProcessLne.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
      payrollProcessLne.setUpdated(new java.util.Date());
      payrollProcessLne.setEmployee(employeeInfo);
      payrollProcessLne.setPayrollProcessHeader(payrollProcessHdr);
      payrollProcessLne.setEmployeeType(employeeInfo.getEhcmActiontype().getPersonType());
      payrollProcessLne.setEmployeeName(employeeInfo.getArabicfullname());
      payrollProcessLne.setHireDate(employeeInfo.getHiredate());
      payrollProcessLne.setDepartmentCode(employementInfo.getPosition().getDepartment());
      if (employementInfo.getPosition() != null
          && employementInfo.getPosition().getSection() != null) {
        payrollProcessLne.setSectionCode(employementInfo.getPosition().getSection());
      }
      payrollProcessLne.setGrade(employementInfo.getGrade());
      payrollProcessLne.setPosition(employementInfo.getPosition());
      payrollProcessLne.setJobTitle(employementInfo.getPosition().getJOBName().getJOBTitle());
      payrollProcessLne.setAssignedDepartment(employementInfo.getSECDeptName());
      payrollProcessLne.setEmploymentGrade(employementInfo.getEmploymentgrade());
      payrollProcessLne.setGradePoint(OBDal.getInstance().get(EHCMPayscalePointV.class,
          employementInfo.getEhcmPayscaleline().getId()));
      payrollProcessLne.setEmployeeCategory(employeeInfo.getGradeClass());
      OBDal.getInstance().save(payrollProcessLne);
      return payrollProcessLne;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : createPayrollProcessLine() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMajor = true;
      PayrollBaseProcess.errorMessage = "Error while creating Payroll Process Line for Employee "
          + employeeInfo.getName();
      return null;
    }
  }

  public static List<EhcmElementGroupLine> getElementsInElementGroup(
      EhcmElementGroup elementGroup) {
    try {

      String whereClause = " e where e.ehcmElementGroup.id= :elementGroupId order by e.ehcmElmttypeDef.priority ";

      OBQuery<EhcmElementGroupLine> elementGrpLneQry = OBDal.getInstance()
          .createQuery(EhcmElementGroupLine.class, whereClause);
      elementGrpLneQry.setNamedParameter("elementGroupId", elementGroup.getId());

      @SuppressWarnings("unchecked")
      List<EhcmElementGroupLine> elemtGrpLineLst = elementGrpLneQry.list();
      return elemtGrpLineLst;
    } catch (Exception egl) {
      log.error("Error in PayrollBaseProcess.java : getElementsInElementGroup() ");
      egl.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Elements in Element Group : "
          + elementGroup.getName();
      return null;
    }
  }

  public static boolean isEarnDeductElementFromDiffProcess(EHCMPayrollProcessLne payProcessLine,
      EHCMEarnDeductEmp earnDeductEmployeeDetail, EHCMElmttypeDef element) {
    try {
      String whereClause = " e where e.payrollProcessLine.id <> :payProcessLineId and e.employeeDetails.id = :employeeDetailsId and e.elementType.id = :elementId order by e.creationDate desc ";

      OBQuery<EHCMEarnDeductElm> earnDeduEleQry = OBDal.getInstance()
          .createQuery(EHCMEarnDeductElm.class, whereClause);
      earnDeduEleQry.setNamedParameter("payProcessLineId", payProcessLine.getId());
      earnDeduEleQry.setNamedParameter("employeeDetailsId", earnDeductEmployeeDetail.getId());
      earnDeduEleQry.setNamedParameter("elementId", element.getId());
      if (earnDeduEleQry.list().size() > 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : isEarnDeductElementFromDiffProcess() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while checking Earning Deduction Element is from Different Process "
          + element.getName();
      return false;
    }
  }

  public static EHCMEarnDeductElm getEarnDeductElementDetails(EHCMPayrollProcessLne payProcessLine,
      EHCMEarnDeductEmp earnDeductEmployeeDetail, EHCMElmttypeDef element) {
    EHCMEarnDeductElm earnDedElementDetails = null;
    try {
      String whereClause = " e where e.payrollProcessLine.id = :payProcessLineId and e.employeeDetails.id = :employeeDetailsId and e.elementType.id = :elementId order by e.creationDate desc ";

      OBQuery<EHCMEarnDeductElm> earnDeduEleQry = OBDal.getInstance()
          .createQuery(EHCMEarnDeductElm.class, whereClause);
      earnDeduEleQry.setNamedParameter("payProcessLineId", payProcessLine.getId());
      earnDeduEleQry.setNamedParameter("employeeDetailsId", earnDeductEmployeeDetail.getId());
      earnDeduEleQry.setNamedParameter("elementId", element.getId());
      if (earnDeduEleQry.list().size() > 0) {
        earnDedElementDetails = earnDeduEleQry.list().get(0);
      }
      return earnDedElementDetails;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : getEarnDeductElementDetails() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Earning Deduction Element Detail for Element "
          + element.getName();
      return earnDedElementDetails;
    }
  }

  public static List<EmploymentInfo> getEmploymentsOfEmployee(EhcmEmpPerInfo empPerInfo,
      String periodStartDate, String periodEndDate) {
    List<EmploymentInfo> employmentList = new ArrayList<EmploymentInfo>();
    try {
      // Note should not check active flag because active flag set to false after new employment
      String whereClause = " e where e.ehcmEmpPerinfo.id = :empPerInfo and "
          + "e.changereason in ('H', 'PR', 'PRT', 'OD',  'ID', 'ES', 'EOS', 'SEC', 'SUS', 'SUE','JWRSEC','EXSEC','COSEC','SECDLY') and "
          + "((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) "
          + "order by e.startDate ";

      OBQuery<EmploymentInfo> employmentQry = OBDal.getInstance().createQuery(EmploymentInfo.class,
          whereClause);
      employmentQry.setNamedParameter("empPerInfo", empPerInfo.getId());
      employmentQry.setNamedParameter("fromdate", periodStartDate);
      employmentQry.setNamedParameter("todate", periodEndDate);
      employmentList = employmentQry.list();
      return employmentList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getEmploymentsOfEmployee() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting employments of Employee "
          + empPerInfo.getName();
      return employmentList;
    }
  }

  public static boolean checkElementEligibleForEmployment(EHCMElmttypeDef element,
      EhcmPosition position, Organization department, Jobs job, ehcmgrade grade,
      ehcmgradeclass gradeClass) {
    try {
      // Payroll and Location Validation Missing
      String whereClause = " e where e.ehcmElementECriteria.code.id=:elementId and e.enabled = 'Y' "
          + "and (e.position is null or e.position.id=:positionId) "
          + "and (e.department is null or e.department.id=:departmentId) "
          + "and (e.ehcmJobs is null or e.ehcmJobs.id=:jobId) and (e.grade is null or e.grade.id=:gradeId) "
          + "and (e.gradeClassifications is null or e.gradeClassifications.id=:gradeClassId) ";

      OBQuery<EHCMEligbltyCriteria> elementEligiblityQry = OBDal.getInstance()
          .createQuery(EHCMEligbltyCriteria.class, whereClause);
      elementEligiblityQry.setNamedParameter("elementId", element.getId());
      elementEligiblityQry.setNamedParameter("positionId", position.getId());
      elementEligiblityQry.setNamedParameter("departmentId", department.getId());
      elementEligiblityQry.setNamedParameter("jobId", job.getId());
      elementEligiblityQry.setNamedParameter("gradeId", grade.getId());
      elementEligiblityQry.setNamedParameter("gradeClassId", gradeClass.getId());

      List<EHCMEligbltyCriteria> elementEligiblityList = elementEligiblityQry.list();

      if (elementEligiblityList.size() > 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception ccefe) {
      log.error("Error in PayrollBaseProcess.java : checkElementEligibleForEmployment() ");
      ccefe.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while checking Element '" + element.getName()
          + "' eligibility for Employment '";
      return false;
    }
  }

  public static boolean checkElementEligibleForEmployment(EHCMElmttypeDef element,
      EhcmPosition position, Organization department, Jobs job, ehcmgrade grade,
      ehcmgradeclass gradeClass, EHCMPayrollDefinition payroll, String filterDate) {
    try {
      // Location Validation Missing
      String whereClause = " e where e.ehcmElementECriteria.code.id=:elementId and e.enabled = 'Y' "
          + "and (e.position is null or e.position.id=:positionId) "
          + "and (e.department is null or e.department.id=:departmentId) "
          + "and (e.ehcmJobs is null or e.ehcmJobs.id=:jobId) and (e.grade is null or e.grade.id=:gradeId) "
          + "and (e.gradeClassifications is null or e.gradeClassifications.id=:gradeClassId) "
          + "and (e.ehcmPayrollDefinition is null or e.ehcmPayrollDefinition.id=:payrollId) "
          + "and to_date(:filterDate,'dd-MM-yyyy') <= to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') "
          + "and to_date(:filterDate,'dd-MM-yyyy') >= e.startDate ";

      OBQuery<EHCMEligbltyCriteria> elementEligiblityQry = OBDal.getInstance()
          .createQuery(EHCMEligbltyCriteria.class, whereClause);
      elementEligiblityQry.setNamedParameter("elementId", element.getId());
      elementEligiblityQry.setNamedParameter("positionId", position.getId());
      elementEligiblityQry.setNamedParameter("departmentId", department.getId());
      elementEligiblityQry.setNamedParameter("jobId", job.getId());
      elementEligiblityQry.setNamedParameter("gradeId", grade.getId());
      elementEligiblityQry.setNamedParameter("gradeClassId", gradeClass.getId());
      if (payroll != null) {
        elementEligiblityQry.setNamedParameter("payrollId", payroll.getId());
      } else {
        elementEligiblityQry.setNamedParameter("payrollId", null);
      }
      elementEligiblityQry.setNamedParameter("filterDate", filterDate);

      List<EHCMEligbltyCriteria> elementEligiblityList = elementEligiblityQry.list();

      if (elementEligiblityList.size() > 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception ccefe) {
      log.error("Error in PayrollBaseProcess.java : checkElementEligibleForEmployment() ");
      ccefe.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while checking Element '" + element.getName()
          + "' eligibility for Employment '";
      return false;
    }
  }

  public static EHCMEligbltyCriteria getElementEligiblityForEmployment(EHCMElmttypeDef element,
      EhcmPosition position, Organization department, Jobs job, ehcmgrade grade,
      ehcmgradeclass gradeClass, EHCMPayrollDefinition payroll, String startDate, String endDate) {
    try {
      // Location Validation Missing
      String whereClause = " e where e.ehcmElementECriteria.code.id=:elementId and e.enabled = 'Y' "
          + "and (e.position is null or e.position.id=:positionId) "
          + "and (e.department is null or e.department.id=:departmentId) "
          + "and (e.ehcmJobs is null or e.ehcmJobs.id=:jobId) and (e.grade is null or e.grade.id=:gradeId) "
          + "and (e.gradeClassifications is null or e.gradeClassifications.id=:gradeClassId) "
          // + "and (e.ehcmEscmLocation is null or e.ehcmEscmLocation=:locationId) "
          + "and (e.ehcmPayrollDefinition is null or e.ehcmPayrollDefinition.id=:payrollId) "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) "
          + "order by e.startDate ";

      OBQuery<EHCMEligbltyCriteria> elementEligiblityQry = OBDal.getInstance()
          .createQuery(EHCMEligbltyCriteria.class, whereClause);
      elementEligiblityQry.setNamedParameter("elementId", element.getId());
      elementEligiblityQry.setNamedParameter("positionId", position.getId());
      elementEligiblityQry.setNamedParameter("departmentId", department.getId());
      elementEligiblityQry.setNamedParameter("jobId", job.getId());
      elementEligiblityQry.setNamedParameter("gradeId", grade.getId());
      elementEligiblityQry.setNamedParameter("gradeClassId", gradeClass.getId());
      // elementEligiblityQry.setNamedParameter("locationId", gradeClass.getId());
      if (payroll != null) {
        elementEligiblityQry.setNamedParameter("payrollId", payroll.getId());
      } else {
        elementEligiblityQry.setNamedParameter("payrollId", null);
      }
      elementEligiblityQry.setNamedParameter("fromdate", startDate);
      elementEligiblityQry.setNamedParameter("todate", endDate);
      elementEligiblityQry.setMaxResult(1);

      List<EHCMEligbltyCriteria> elementEligiblityList = elementEligiblityQry.list();

      if (elementEligiblityList.size() > 0) {
        return elementEligiblityList.get(0);
      } else {
        return null;
      }
    } catch (Exception ccefe) {
      log.error("Error in PayrollBaseProcess.java : getElementEligiblityForEmployment() ");
      ccefe.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Element '" + element.getName()
          + "' eligibility for Employment '";
      return null;
    }
  }

  public static List<EHCMBenefitAllowance> getAllowanceDecisionForEmployee(
      EhcmEmpPerInfo empPerInfo, EHCMElmttypeDef element, String periodStartDate,
      String periodEndDate) {
    List<EHCMBenefitAllowance> AllowanceList = new ArrayList<EHCMBenefitAllowance>();
    try {
      String whereClause = " e where e.employee.id = :employeeId and e.elementType.id = :elementId "
          + "and e.decisionType<>'CA' and e.issueDecision='Y' "
          + "and e.id not in(select a.originalDecisionNo from EHCM_Benefit_Allowance a where a.originalDecisionNo is not null and a.issueDecision='Y' and a.employee.id = :employeeId) "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) "
          + "order by e.startDate ";

      OBQuery<EHCMBenefitAllowance> allowanceQry = OBDal.getInstance()
          .createQuery(EHCMBenefitAllowance.class, whereClause);
      allowanceQry.setNamedParameter("employeeId", empPerInfo.getId());
      allowanceQry.setNamedParameter("elementId", element.getId());
      allowanceQry.setNamedParameter("startDate", periodStartDate);
      allowanceQry.setNamedParameter("endDate", periodEndDate);
      AllowanceList = allowanceQry.list();
      return AllowanceList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getAllowanceDecisionForEmployee() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Allowance Decision of Employee "
          + empPerInfo.getName() + " and Element " + element.getName();
      return AllowanceList;
    }
  }

  public static BigDecimal getPayScaleValue(EmploymentInfo empInfo, Date startDate, Date endDate,
      BigDecimal processingDays, BigDecimal differenceDays, Date payrollEndDate) {
    BigDecimal totalPayScaleValue = BigDecimal.ZERO;
    try {
      String empmtPayStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String empmtPayEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      String whereClause = "e where e.ehcmPayscale.ehcmGrade.id=:gradeId "
          + "and e.ehcmProgressionpt.id = :pointsId "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) "
          + "order by e.startDate ";

      OBQuery<ehcmpayscaleline> payScaleQry = OBDal.getInstance()
          .createQuery(ehcmpayscaleline.class, whereClause);
      payScaleQry.setNamedParameter("gradeId", empInfo.getEmploymentgrade().getId());
      payScaleQry.setNamedParameter("pointsId",
          empInfo.getEhcmPayscaleline().getEhcmProgressionpt().getId());
      payScaleQry.setNamedParameter("startDate", empmtPayStartDate);
      payScaleQry.setNamedParameter("endDate", empmtPayEndDate);
      payScaleQry.setFilterOnActive(false);
      List<ehcmpayscaleline> payScaleLneList = payScaleQry.list();
      for (ehcmpayscaleline payScaleLne : payScaleLneList) {
        if (!PayrollBaseProcess.errorFlagMinor) {
          log.info("PayScale Days applicable");
          JSONObject payScalePeriodJSON = PayrollBaseProcess.getOverlapingDateRange(
              payScaleLne.getStartDate(), payScaleLne.getEndDate(), startDate, endDate);

          if (payScalePeriodJSON != null) {
            BigDecimal days = new BigDecimal(payScalePeriodJSON.getLong("days"));

            // If pay scale is for payroll end date, Check and add extra days in month
            Date payScaleEndDate = PayrollConstants.dateFormat
                .parse(payScalePeriodJSON.getString("endDate"));
            if (differenceDays.compareTo(BigDecimal.ZERO) > 0
                && payScaleEndDate.compareTo(payrollEndDate) == 0) {
              days = days.add(differenceDays);
            }

            BigDecimal payScaleAmount = payScaleLne.getAmount();
            BigDecimal perDayPayScale = payScaleAmount.divide(processingDays, 6,
                BigDecimal.ROUND_HALF_UP);
            BigDecimal payScaleCalDays = perDayPayScale.multiply(days).setScale(6,
                BigDecimal.ROUND_HALF_UP);
            totalPayScaleValue = totalPayScaleValue.add(payScaleCalDays);

            log.info("Pay Scale value ===> " + payScaleCalDays);
          }
        } else {
          break;
        }
      }

      if (!PayrollBaseProcess.errorFlagMinor) {
        log.info("Total Pay Scale value ===> " + totalPayScaleValue);
        return totalPayScaleValue;
      } else {
        log.info("Total Pay Scale value ===> " + 0);
        return BigDecimal.ZERO;
      }
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getPayScaleValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while calculating Pay Scale value for Employment Grade "
          + empInfo.getEmploymentgrade().getSearchKey();
      return BigDecimal.ZERO;
    }
  }

  public static List<ehcmpayscaleline> getApplicablePayScaleList(EmploymentInfo employment,
      Date startDate, Date endDate, BigDecimal processingDays) {
    List<ehcmpayscaleline> payScaleList = new ArrayList<ehcmpayscaleline>();
    try {
      String dbFormattedStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String dbFormattedEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      String whereClause = "e where e.ehcmPayscale.ehcmGrade.id=:gradeId "
          + "and e.ehcmProgressionpt.id = :pointsId "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) "
          + "order by e.startDate ";

      OBQuery<ehcmpayscaleline> payScaleQry = OBDal.getInstance()
          .createQuery(ehcmpayscaleline.class, whereClause);
      payScaleQry.setNamedParameter("gradeId", employment.getEmploymentgrade().getId());
      payScaleQry.setNamedParameter("pointsId",
          employment.getEhcmPayscaleline().getEhcmProgressionpt().getId());
      payScaleQry.setNamedParameter("startDate", dbFormattedStartDate);
      payScaleQry.setNamedParameter("endDate", dbFormattedEndDate);
      payScaleQry.setFilterOnActive(false);
      List<ehcmpayscaleline> payScaleLneList = payScaleQry.list();
      for (ehcmpayscaleline payScaleLne : payScaleLneList) {
        if (!PayrollBaseProcess.errorFlagMinor) {
          log.info("PayScale Days applicable");
          JSONObject payScalePeriodJSON = PayrollBaseProcess.getOverlapingDateRange(
              payScaleLne.getStartDate(), payScaleLne.getEndDate(), startDate, endDate);

          if (payScalePeriodJSON != null) {
            Date applicablePayScaleStartDate = PayrollConstants.dateFormat
                .parse(payScalePeriodJSON.getString("startDate"));
            Date applicablePayScaleEndDate = PayrollConstants.dateFormat
                .parse(payScalePeriodJSON.getString("endDate"));

            BigDecimal payScaleAmount = payScaleLne.getAmount();
            BigDecimal oneDayPayScale = payScaleAmount.divide(processingDays, 6,
                BigDecimal.ROUND_HALF_UP);

            ehcmpayscaleline payScaleObj = new ehcmpayscaleline();
            payScaleObj.setStartDate(applicablePayScaleStartDate);
            payScaleObj.setEndDate(applicablePayScaleEndDate);
            payScaleObj.setAmount(oneDayPayScale);

            payScaleList.add(payScaleObj);
          }
        }
      }
      return payScaleList;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : getApplicablePayScaleList() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Applicable Payscale List for Employment Grade "
          + employment.getEmploymentgrade().getSearchKey();
      return payScaleList;
    }
  }

  public static BigDecimal getLatestPayScaleValueInEmployment(EmploymentInfo empInfo,
      Date startDate, Date endDate) {
    BigDecimal payScaleAmount = BigDecimal.ZERO;
    try {
      String empmtPayStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String empmtPayEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      String whereClause = "e where e.ehcmPayscale.ehcmGrade.id=:gradeId "
          + "and e.ehcmProgressionpt.id = :pointsId "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) order by e.startDate desc";

      OBQuery<ehcmpayscaleline> payScaleQry = OBDal.getInstance()
          .createQuery(ehcmpayscaleline.class, whereClause);
      payScaleQry.setNamedParameter("gradeId", empInfo.getEmploymentgrade().getId());
      payScaleQry.setNamedParameter("pointsId",
          empInfo.getEhcmPayscaleline().getEhcmProgressionpt().getId());
      payScaleQry.setNamedParameter("startDate", empmtPayStartDate);
      payScaleQry.setNamedParameter("endDate", empmtPayEndDate);
      payScaleQry.setFilterOnActive(false);
      payScaleQry.setMaxResult(1);

      if (payScaleQry.list().size() > 0) {
        ehcmpayscaleline payScaleLne = payScaleQry.list().get(0);
        payScaleAmount = payScaleLne.getAmount();
      }
      return payScaleAmount;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : getLatestPayScaleValueInEmployment() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Latest Pay Scale value for Employment Grade "
          + empInfo.getEmploymentgrade().getSearchKey();
      return payScaleAmount;
    }
  }

  public static BigDecimal getGradeRateValue(EHCMElmttypeDef elmTypDef, ehcmgraderates gradeRate,
      ehcmgrade grade, Date startDate, Date endDate, BigDecimal processingDays,
      BigDecimal differenceDays, Date payrollEndDate) {
    BigDecimal totalGradeRateValue = BigDecimal.ZERO;
    try {
      String dbFormattedStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String dbFormattedEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      String whereClause = "e where e.ehcmGraderates.id = :gradeRateId and e.grade.id=:gradeId "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')))";

      OBQuery<ehcmgraderatelines> gradeLineQry = OBDal.getInstance()
          .createQuery(ehcmgraderatelines.class, whereClause);
      gradeLineQry.setNamedParameter("gradeRateId", gradeRate.getId());
      gradeLineQry.setNamedParameter("gradeId", grade.getId());
      gradeLineQry.setNamedParameter("startDate", dbFormattedStartDate);
      gradeLineQry.setNamedParameter("endDate", dbFormattedEndDate);
      gradeLineQry.setFilterOnActive(false);

      List<ehcmgraderatelines> gradeLineList = gradeLineQry.list();
      for (ehcmgraderatelines gradeRateLne : gradeLineList) {
        log.info("GradeRate Days applicable");
        JSONObject gradeRatePeriodJSON = PayrollBaseProcess.getOverlapingDateRange(
            gradeRateLne.getStartDate(), gradeRateLne.getEndDate(), startDate, endDate);

        if (gradeRatePeriodJSON != null) {
          BigDecimal gradeRateDays = new BigDecimal(gradeRatePeriodJSON.getLong("days"));

          // If Grade Rate is for payroll end date, Check and add extra days in month
          Date gradeRateEndDate = PayrollConstants.dateFormat
              .parse(gradeRatePeriodJSON.getString("endDate"));
          if (differenceDays.compareTo(BigDecimal.ZERO) > 0
              && gradeRateEndDate.compareTo(payrollEndDate) == 0) {
            gradeRateDays = gradeRateDays.add(differenceDays);
          }

          BigDecimal gradeRateValue = gradeRateLne.getSearchKey();

          BigDecimal perDayGradeRate = BigDecimal.ZERO;
          BigDecimal GradeRateCalDays = BigDecimal.ZERO;
          // Validate Monthly or Day Rate
          if (gradeRateLne.getEhcmGraderates().getDuration().equalsIgnoreCase("PM")) {
            perDayGradeRate = gradeRateValue.divide(processingDays, 6, BigDecimal.ROUND_HALF_UP);
          } else {
            perDayGradeRate = gradeRateValue;
          }

          GradeRateCalDays = perDayGradeRate.multiply(gradeRateDays).setScale(6,
              BigDecimal.ROUND_HALF_UP);
          totalGradeRateValue = totalGradeRateValue.add(GradeRateCalDays);

          log.info("Grade Rate value ===> " + GradeRateCalDays);
        }
      }

      log.info("Total Grade Rate value ===> " + totalGradeRateValue);
      return totalGradeRateValue;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getGradeRateValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while calculating grade rate value for "
          + elmTypDef.getName();
      return BigDecimal.ZERO;
    }
  }

  public static List<ehcmgraderatelines> getApplicableGradeRateList(EHCMElmttypeDef elmTypDef,
      ehcmgraderates gradeRate, ehcmgrade grade, Date startDate, Date endDate,
      BigDecimal processingDays) {
    List<ehcmgraderatelines> gradeRateList = new ArrayList<ehcmgraderatelines>();
    try {
      String dbFormattedStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String dbFormattedEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      String whereClause = "e where e.ehcmGraderates.id = :gradeRateId and e.grade.id=:gradeId "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')))";

      OBQuery<ehcmgraderatelines> gradeLineQry = OBDal.getInstance()
          .createQuery(ehcmgraderatelines.class, whereClause);
      gradeLineQry.setNamedParameter("gradeRateId", gradeRate.getId());
      gradeLineQry.setNamedParameter("gradeId", grade.getId());
      gradeLineQry.setNamedParameter("startDate", dbFormattedStartDate);
      gradeLineQry.setNamedParameter("endDate", dbFormattedEndDate);
      gradeLineQry.setFilterOnActive(false);

      List<ehcmgraderatelines> gradeLineList = gradeLineQry.list();
      for (ehcmgraderatelines gradeRateLne : gradeLineList) {
        log.info("GradeRate Days applicable");
        JSONObject gradeRatePeriodJSON = PayrollBaseProcess.getOverlapingDateRange(
            gradeRateLne.getStartDate(), gradeRateLne.getEndDate(), startDate, endDate);

        if (gradeRatePeriodJSON != null) {
          Date applicableGradeRateStartDate = PayrollConstants.dateFormat
              .parse(gradeRatePeriodJSON.getString("startDate"));
          Date applicableGradeRateEndDate = PayrollConstants.dateFormat
              .parse(gradeRatePeriodJSON.getString("endDate"));

          BigDecimal gradeRateValue = gradeRateLne.getSearchKey();
          BigDecimal perDayGradeRate;
          if (gradeRateLne.getEhcmGraderates().getDuration().equalsIgnoreCase("PM")) {
            perDayGradeRate = gradeRateValue.divide(processingDays, 6, BigDecimal.ROUND_HALF_UP);
          } else {
            perDayGradeRate = gradeRateValue;
          }

          ehcmgraderatelines gradeRateLineObj = new ehcmgraderatelines();
          gradeRateLineObj.setStartDate(applicableGradeRateStartDate);
          gradeRateLineObj.setEndDate(applicableGradeRateEndDate);
          gradeRateLineObj.setSearchKey(perDayGradeRate);
          gradeRateList.add(gradeRateLineObj);
        }
      }
      return gradeRateList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getApplicableGradeRateList() ", e);
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting applicable grade rate list for "
          + elmTypDef.getName();
      return gradeRateList;
    }
  }

  public static BigDecimal getLatestGradeRateValue(EHCMElmttypeDef elmTypDef,
      ehcmgraderates gradeRate, ehcmgrade grade, Date startDate, Date endDate,
      BigDecimal processingDays) {
    BigDecimal gradeRateValue = BigDecimal.ZERO;
    try {
      String dbFormattedStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String dbFormattedEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      String whereClause = "e where e.ehcmGraderates.id = :gradeRateId and e.grade.id=:gradeId "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) order by e.startDate desc ";

      OBQuery<ehcmgraderatelines> gradeLineQry = OBDal.getInstance()
          .createQuery(ehcmgraderatelines.class, whereClause);
      gradeLineQry.setNamedParameter("gradeRateId", gradeRate.getId());
      gradeLineQry.setNamedParameter("gradeId", grade.getId());
      gradeLineQry.setNamedParameter("startDate", dbFormattedStartDate);
      gradeLineQry.setNamedParameter("endDate", dbFormattedEndDate);
      gradeLineQry.setFilterOnActive(false);
      gradeLineQry.setMaxResult(1);

      if (gradeLineQry.list().size() > 0) {
        ehcmgraderatelines gradeRateLne = gradeLineQry.list().get(0);
        gradeRateValue = gradeRateLne.getSearchKey();
        // Validate Monthly or Day Rate
        if (!gradeRateLne.getEhcmGraderates().getDuration().equalsIgnoreCase("PM")) {
          gradeRateValue = gradeRateValue.multiply(processingDays);
        }
      }
      return gradeRateValue;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : getLatestGradeRateValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while calculating grade rate value for "
          + elmTypDef.getName();
      return BigDecimal.ZERO;
    }
  }

  public static BigDecimal calculateElementValue(EHCMElmttypeDef elementType, ehcmgrade grade,
      Date startDate, Date endDate, BigDecimal processingDays, BigDecimal differenceDays,
      Date payrollEndDate, boolean isBaseCalculation, EHCMEmpBusinessMission bMission) {
    BigDecimal elementCalculatedValue;

    if (!PayrollBaseProcess.errorFlagMinor) {
      try {
        // No need of date validation so bring latest formula
        String whereClause = "e where elementType.id= :empTypeId "
            + "and e.enabled = 'Y' order by e.creationDate desc ";
        OBQuery<EHCMElementFormulaHdr> formulaHdrQry = OBDal.getInstance()
            .createQuery(EHCMElementFormulaHdr.class, whereClause);
        formulaHdrQry.setNamedParameter("empTypeId", elementType.getId());
        formulaHdrQry.setMaxResult(1);
        List<EHCMElementFormulaHdr> formulaHdrList = formulaHdrQry.list();
        EHCMElementFormulaHdr formulaHdr = formulaHdrList.get(0);

        if (!PayrollBaseProcess.errorFlagMinor) {
          String formula = generateFormulaFromLines(formulaHdr, elementType, grade, startDate,
              endDate, processingDays, differenceDays, payrollEndDate, isBaseCalculation, bMission);

          if (!StringUtils.isEmpty(formula)) {
            String appliedFormula = applyPredefinedValues(formula.toString());
            log.info("Applied Formula ===>");
            log.info(appliedFormula);

            elementCalculatedValue = calculateFormulaValue(elementType, appliedFormula);

            log.info("Calculated Value ===>" + elementCalculatedValue);
          } else {
            elementCalculatedValue = BigDecimal.ZERO;
          }
        } else {
          elementCalculatedValue = BigDecimal.ZERO;
        }

        return elementCalculatedValue;
      } catch (Exception cev) {
        log.error("Error in PayrollProcess.java : calculateElementValue() ");
        cev.printStackTrace();
        PayrollBaseProcess.errorFlagMinor = true;
        PayrollBaseProcess.errorMessage = "Error while calculating value for element "
            + elementType.getName();
        return BigDecimal.ZERO;
      }
    } else {
      return BigDecimal.ZERO;
    }
  }

  public static String generateFormulaFromLines(EHCMElementFormulaHdr formulaHdr,
      EHCMElmttypeDef elementType, ehcmgrade grade, Date startDate, Date endDate,
      BigDecimal processingDays, BigDecimal differenceDays, Date payrollEndDate,
      boolean isBaseCalculation, EHCMEmpBusinessMission bMission) {
    StringBuffer completeFormula = new StringBuffer();
    try {
      String whereClause = "  e where e.element.id=:formulaHdrId and e.enabled = 'Y' order by e.priority";
      OBQuery<EHCMElementFormulaLne> formulaLneQry = OBDal.getInstance()
          .createQuery(EHCMElementFormulaLne.class, whereClause);
      formulaLneQry.setNamedParameter("formulaHdrId", formulaHdr.getId());
      List<EHCMElementFormulaLne> formulaLneList = formulaLneQry.list();
      for (EHCMElementFormulaLne formulaLne : formulaLneList) {
        String formula = formulaLne.getFormula();
        String condition = !StringUtils.isEmpty(formulaLne.getCondition())
            ? formulaLne.getCondition().replace("=", "==")
            : "true";

        condition = condition.replace(">==", ">=");
        condition = condition.replace("<==", "<=");

        // Checking and Apply Multi Rate Value
        if (grade != null && elementType.getElementSource() != null
            && elementType.getElementSource().equalsIgnoreCase("MGR")) {

          BigDecimal gradeRate;
          if (isBaseCalculation) {
            // Fetching latest grade rate value based on grade rate in formula
            gradeRate = getLatestGradeRateValue(elementType, formulaLne.getGradeRate(), grade,
                startDate, endDate, processingDays);
          } else {
            // Fetching grade rate value based on grade rate in formula
            gradeRate = getGradeRateValue(elementType, formulaLne.getGradeRate(), grade, startDate,
                endDate, processingDays, differenceDays, payrollEndDate);
          }

          // Calculate Business Mission Based Muliti Rate Elements
          if (bMission != null) {
            // Business Mission Days Before Rate
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            c.add(Calendar.DAY_OF_MONTH,
                new BigDecimal(bMission.getNoofdaysBefore()).negate().intValue());
            Date daysBfStartDate = c.getTime();
            c.setTime(startDate);
            c.add(Calendar.DAY_OF_MONTH, -1);
            Date daysBfEndDate = c.getTime();
            BigDecimal daysBeforeRate = PayrollBaseProcessDAO.getGradeRateValue(elementType,
                formulaLne.getGradeRate(), grade, daysBfStartDate, daysBfEndDate, processingDays,
                BigDecimal.ZERO, payrollEndDate);

            // Business Mission Days After Rate
            c.setTime(endDate);
            c.add(Calendar.DAY_OF_MONTH, bMission.getNoofdaysAfter().intValue());
            Date daysAfStartDate = c.getTime();
            c.setTime(endDate);
            c.add(Calendar.DAY_OF_MONTH, 1);
            Date daysAfEndDate = c.getTime();
            BigDecimal daysAfterRate = PayrollBaseProcessDAO.getGradeRateValue(elementType,
                formulaLne.getGradeRate(), grade, daysAfStartDate, daysAfEndDate, processingDays,
                BigDecimal.ZERO, payrollEndDate);

            formula = formula.replace(PayrollConstants.ELEMENT_BUSINESS_MISSION_DAYS_BEFORE_RATE,
                daysBeforeRate.toString());
            formula = formula.replace(PayrollConstants.ELEMENT_BUSINESS_MISSION_DAYS_AFTER_RATE,
                daysAfterRate.toString());
          }

          // Replace grade rate in formula and condition
          formula = formula.replace(PayrollConstants.ELEMENT_GRADERATE_CODE, gradeRate.toString());
          if (!StringUtils.isEmpty(condition)) {
            condition = condition.replace(PayrollConstants.ELEMENT_GRADERATE_CODE,
                gradeRate.toString());
          }
        }

        if (StringUtils.isEmpty(completeFormula.toString())) {
          completeFormula.append("(" + condition + ") ? (" + formula + ")");
        } else {
          completeFormula.append(": (" + condition + ") ? (" + formula + ")");
        }
      }

      if (completeFormula.length() > 0) {
        completeFormula.append(": 0");
      }

      log.info("Formula ===> ");
      log.info(completeFormula.toString());

      return completeFormula.toString();
    } catch (Exception gffl) {
      log.error("Error in PayrollProcess.java : generateFormulaFromLines() ");
      gffl.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while generaing formula for "
          + formulaHdr.getElementType().getName();
      return null;
    }
  }

  public static String applyPredefinedValues(String formula) {
    String appliedFormula = formula;
    try {
      java.util.Iterator<?> componentCodes = PayrollBaseProcess.payRollComponents.keys();
      while (componentCodes.hasNext()) {
        String code = (String) componentCodes.next();
        appliedFormula = appliedFormula.replace(code,
            PayrollBaseProcess.payRollComponents.getString(code));
      }

      // apply calculated element values
      java.util.Iterator<?> elementCodes = PayrollBaseProcess.calculatedElementValue.keys();
      while (elementCodes.hasNext()) {
        String elementcode = (String) elementCodes.next();
        appliedFormula = appliedFormula.replace(elementcode,
            PayrollBaseProcess.calculatedElementValue.getString(elementcode));
      }
      return appliedFormula;
    } catch (JSONException e) {
      log.error("Error while in PayrollProcess.java : applyPredefinedValues() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while applying predefined values";
      return "";
    }
  }

  public static BigDecimal calculateFormulaValue(EHCMElmttypeDef elementType,
      String appliedFormula) {
    try {
      ScriptEngineManager mgr = new ScriptEngineManager();
      ScriptEngine engine = mgr.getEngineByName("JavaScript");
      BigDecimal elementCalculatedValue = new BigDecimal(engine.eval(appliedFormula).toString())
          .setScale(2, BigDecimal.ROUND_HALF_UP);
      return elementCalculatedValue;
    } catch (Exception e) {
      PayrollBaseProcess.errorFlagMinor = true;
      if (e.getMessage() != null) {
        int index = e.getMessage().indexOf("is not defined");
        if (e.getMessage().startsWith("ReferenceError:") && index > 0) {
          String missingElement = e.getMessage().substring(0, index).replace("ReferenceError: ",
              "");
          PayrollBaseProcess.errorMessage = String.format(
              OBMessageUtils.messageBD("EHCM_Payroll_CompNotDefined"), missingElement,
              elementType.getName());
        } else {
          PayrollBaseProcess.errorMessage = String.format(
              OBMessageUtils.messageBD("EHCM_Payroll_InvalidFormula"), elementType.getName());
        }
      } else {
        PayrollBaseProcess.errorMessage = "Error while calculating Formula Value";
      }
      return BigDecimal.ZERO;
    }
  }

  public static List<EHCMEmpBusinessMission> getBusinessMissionForEmployee(
      EhcmEmpPerInfo empPerInfo, EHCMPayrolldefPeriod payrollPeriod,
      EHCMPayrollProcessLne payrollProcessLne) {
    List<EHCMEmpBusinessMission> businessMissionList = new ArrayList<EHCMEmpBusinessMission>();
    try {
      String whereClause = " e where e.employee.id = :employeeId and e.payrollPeriod.id = :payrollPeriodId "
          + "and e.decisionType='BP' and e.issueDecision='Y' and (e.payrollProcessLine.id = null or e.payrollProcessLine.id =:payrollline ) order by e.startDate ";

      OBQuery<EHCMEmpBusinessMission> businessMissionQry = OBDal.getInstance()
          .createQuery(EHCMEmpBusinessMission.class, whereClause);
      businessMissionQry.setNamedParameter("employeeId", empPerInfo.getId());
      businessMissionQry.setNamedParameter("payrollPeriodId", payrollPeriod.getId());
      businessMissionQry.setNamedParameter("payrollline", payrollProcessLne.getId());

      businessMissionList = businessMissionQry.list();
      return businessMissionList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getBusinessMissionForEmployee() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Business Mission of Employee "
          + empPerInfo.getName();
      return businessMissionList;
    }
  }

  public static BigDecimal getFirstStepGradeValue(EmploymentInfo empInfo, Date startDate,
      Date endDate, BigDecimal processingDays, BigDecimal differenceDays, Date payrollEndDate) {
    BigDecimal totalPayScaleValue = BigDecimal.ZERO;
    try {
      // Get First Step of Grade
      ehcmprogressionpoint firstStepOfGrade = getFirstStepGrade(empInfo);

      if (firstStepOfGrade != null) {
        String empmtPayStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
        String empmtPayEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

        String whereClause = "e where e.ehcmPayscale.ehcmGrade.id=:gradeId "
            + "and e.ehcmProgressionpt.id = :pointsId  "// and e.enabled = 'Y'
            + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
            + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
            + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
            + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) ";

        OBQuery<ehcmpayscaleline> payScaleQry = OBDal.getInstance()
            .createQuery(ehcmpayscaleline.class, whereClause);
        payScaleQry.setNamedParameter("gradeId", empInfo.getEmploymentgrade().getId());
        payScaleQry.setNamedParameter("pointsId", firstStepOfGrade.getId());
        payScaleQry.setNamedParameter("startDate", empmtPayStartDate);
        payScaleQry.setNamedParameter("endDate", empmtPayEndDate);
        payScaleQry.setFilterOnActive(false);
        List<ehcmpayscaleline> payScaleLneList = payScaleQry.list();
        for (ehcmpayscaleline payScaleLne : payScaleLneList) {
          if (!PayrollBaseProcess.errorFlagMinor) {
            log.info("First Step Grade Days applicable");
            JSONObject payScalePeriodJSON = PayrollBaseProcess.getOverlapingDateRange(
                payScaleLne.getStartDate(), payScaleLne.getEndDate(), startDate, endDate);

            if (payScalePeriodJSON != null) {
              BigDecimal days = new BigDecimal(payScalePeriodJSON.getLong("days"));

              // If pay scale is for payroll end date, Check and add extra days in month
              Date payScaleEndDate = PayrollConstants.dateFormat
                  .parse(payScalePeriodJSON.getString("endDate"));
              if (differenceDays.compareTo(BigDecimal.ZERO) > 0
                  && payScaleEndDate.compareTo(payrollEndDate) == 0) {
                days = days.add(differenceDays);
              }

              BigDecimal payScaleAmount = payScaleLne.getAmount();
              BigDecimal perDayPayScale = payScaleAmount.divide(processingDays, 6,
                  BigDecimal.ROUND_HALF_UP);
              BigDecimal payScaleCalDays = perDayPayScale.multiply(days).setScale(6,
                  BigDecimal.ROUND_HALF_UP);
              totalPayScaleValue = totalPayScaleValue.add(payScaleCalDays);
              log.info("Pay Scale value ===> " + payScaleCalDays);
            }
          } else {
            break;
          }
        }

        if (!PayrollBaseProcess.errorFlagMinor) {
          log.info("Total First Step Grade value ===> " + totalPayScaleValue);
          return totalPayScaleValue;
        } else {
          log.info("Total First Step Grade value ===> " + 0);
          return BigDecimal.ZERO;
        }
      } else {
        log.info("Total First Step Grade value ===> " + 0);
        return BigDecimal.ZERO;
      }
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getFirstStepGradeValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while calculating First Step Grade value for Employment Grade "
          + empInfo.getEmploymentgrade().getSearchKey();
      return BigDecimal.ZERO;
    }
  }

  public static BigDecimal getLatestFirstStepGradeValue(EmploymentInfo empInfo, Date startDate,
      Date endDate) {
    BigDecimal totalPayScaleValue = BigDecimal.ZERO;
    try {
      // Get First Step of Grade
      ehcmprogressionpoint firstStepOfGrade = getFirstStepGrade(empInfo);

      if (firstStepOfGrade != null) {
        String empmtPayStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
        String empmtPayEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

        String whereClause = "e where e.ehcmPayscale.ehcmGrade.id=:gradeId "
            + "and e.ehcmProgressionpt.id = :pointsId  " // and e.enabled = 'Y'
            + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
            + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
            + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
            + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) "
            + "order by e.startDate desc";

        OBQuery<ehcmpayscaleline> payScaleQry = OBDal.getInstance()
            .createQuery(ehcmpayscaleline.class, whereClause);
        payScaleQry.setNamedParameter("gradeId", empInfo.getEmploymentgrade().getId());
        payScaleQry.setNamedParameter("pointsId", firstStepOfGrade.getId());
        payScaleQry.setNamedParameter("startDate", empmtPayStartDate);
        payScaleQry.setNamedParameter("endDate", empmtPayEndDate);
        payScaleQry.setMaxResult(1);
        payScaleQry.setFilterOnActive(false);
        if (payScaleQry.list().size() > 0) {
          ehcmpayscaleline payScaleLne = payScaleQry.list().get(0);
          totalPayScaleValue = payScaleLne.getAmount();
        }

        if (!PayrollBaseProcess.errorFlagMinor) {
          log.info("Latest First Step Grade value ===> " + totalPayScaleValue);
          return totalPayScaleValue;
        } else {
          log.info("Latest First Step Grade value ===> " + 0);
          return BigDecimal.ZERO;
        }
      } else {
        log.info("Latest First Step Grade value ===> " + 0);
        return BigDecimal.ZERO;
      }
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getFirstStepGradeValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while calculating First Step Grade value for Employment Grade "
          + empInfo.getEmploymentgrade().getSearchKey();
      return BigDecimal.ZERO;
    }
  }

  public static ehcmprogressionpoint getFirstStepGrade(EmploymentInfo empInfo) {
    ehcmprogressionpoint progPt = null;
    try {
      String whereClause = "e where e.ehcmGrade.id=:gradeId ";

      // Get Grade Steps
      OBQuery<ehcmpayscale> payScaleQry = OBDal.getInstance().createQuery(ehcmpayscale.class,
          whereClause);
      payScaleQry.setNamedParameter("gradeId", empInfo.getEmploymentgrade().getId());
      payScaleQry.setMaxResult(1);

      List<ehcmpayscale> payScaleList = payScaleQry.list();

      if (payScaleList.size() > 0) {
        ehcmpayscale payScale = payScaleList.get(0);
        ehcmgradesteps gradeSteps = payScale.getEhcmGradesteps();

        whereClause = "e where ehcmGradesteps.id = :gradeStepId order by seq ";

        // Get Grade Steps
        OBQuery<ehcmprogressionpoint> progPtQry = OBDal.getInstance()
            .createQuery(ehcmprogressionpoint.class, whereClause);
        progPtQry.setNamedParameter("gradeStepId", gradeSteps.getId());
        progPtQry.setMaxResult(1);

        List<ehcmprogressionpoint> progPtList = progPtQry.list();

        if (payScaleList.size() > 0) {
          progPt = progPtList.get(0);
        }
      }

      return progPt;

    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getFirstStepGrade() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting First Step Grade for Employment Grade "
          + empInfo.getEmploymentgrade().getSearchKey();
      return progPt;
    }
  }

  /**
   * 
   * @param earnDeductEmployeeDetail
   * @param element
   * @param earnDedelm
   * @param payrollProcessLine
   * @param clientId
   * @param orgId
   * @param userId
   * @return
   */
  public static EHCMEarnDeductElm insertEarnDeductElementDetails(
      EHCMEarnDeductEmp earnDeductEmployeeDetail, EHCMElmttypeDef element,
      EHCMEarnDeductElm earnDedelm, EHCMPayrollProcessLne payrollProcessLine, String clientId,
      String orgId, String userId) {
    BigDecimal absenceConfigMinAmount = BigDecimal.ZERO;
    BigDecimal absenceMinAmountValue = BigDecimal.ZERO;
    BigDecimal totalAbsPaymentValue = BigDecimal.ZERO;
    Boolean leaveTypeOtherThanMinAmount = false;
    try {
      EHCMEarnDeductElm earnDeduElementDetail = OBProvider.getInstance()
          .get(EHCMEarnDeductElm.class);
      earnDeduElementDetail.setClient(OBDal.getInstance().get(Client.class, clientId));
      earnDeduElementDetail.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
      earnDeduElementDetail.setEnabled(true);
      earnDeduElementDetail.setCreatedBy(OBDal.getInstance().get(User.class, userId));
      earnDeduElementDetail.setCreationDate(new java.util.Date());
      earnDeduElementDetail.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
      earnDeduElementDetail.setUpdated(new java.util.Date());
      earnDeduElementDetail.setEmployeeDetails(earnDeductEmployeeDetail);
      earnDeduElementDetail.setElementType(element);
      earnDeduElementDetail.setBaseValue(earnDedelm.getBaseValue());
      // earnDeduElementDetail.setCalculatedValue(earnDedelm.getCalculatedValue());
      earnDeduElementDetail.setCalculatedValue(
          earnDedelm.getCalculatedValue().subtract(earnDedelm.getAbsencevalue()));
      earnDeduElementDetail.setPayrollProcessLine(payrollProcessLine);
      earnDeduElementDetail.setDeduction(earnDedelm.isDeduction());
      earnDeduElementDetail.setEhcmLoanTransaction(earnDedelm.getEhcmLoanTransaction());
      earnDeduElementDetail.setEhcmDisciplineAction(earnDedelm.getEhcmDisciplineAction());
      earnDeduElementDetail.setBusinessMission(earnDedelm.getBusinessMission());
      earnDeduElementDetail.setEhcmEmpOvertime(earnDedelm.getEhcmEmpOvertime());
      earnDeduElementDetail.setEhcmEmpScholarship(earnDedelm.getEhcmEmpScholarship());
      earnDeduElementDetail.setBenefitsAndAllowance(earnDedelm.getBenefitsAndAllowance());
      earnDeduElementDetail.setTicketOrders(earnDedelm.getTicketOrders());
      OBDal.getInstance().save(earnDeduElementDetail);

      for (Map.Entry<String, List<EHCMEarnDeductElmRef>> elementsRefToSave : PayrollBaseProcess.elementRefMap
          .entrySet()) {
        log.info("PayrollBaseProcess.elementRefMap:" + PayrollBaseProcess.elementRefMap);
        if (earnDedelm.getId().equals(elementsRefToSave.getKey())) {

          List<EHCMEarnDeductElmRef> earnDedElmRefList = elementsRefToSave.getValue();

          for (EHCMEarnDeductElmRef earnDedElmRef : earnDedElmRefList) {
            EHCMEarnDeductElmRef earnDeduElementRef = OBProvider.getInstance()
                .get(EHCMEarnDeductElmRef.class);
            earnDeduElementRef.setClient(OBDal.getInstance().get(Client.class, clientId));
            earnDeduElementRef.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
            earnDeduElementRef.setEnabled(true);
            earnDeduElementRef.setCreatedBy(OBDal.getInstance().get(User.class, userId));
            earnDeduElementRef.setCreationDate(new java.util.Date());
            earnDeduElementRef.setUpdatedBy(OBDal.getInstance().get(User.class, userId));
            earnDeduElementRef.setUpdated(new java.util.Date());
            earnDeduElementRef.setPayrollElement(element);
            earnDeduElementRef.setBaseValue(earnDedElmRef.getBaseValue());
            earnDeduElementRef.setCalculatedValue(earnDedElmRef.getCalculatedValue());
            // earnDeduElementRef.setPayrollProcessLine(payrollProcessLine);
            earnDeduElementRef.setDeduction(earnDedElmRef.isDeduction());
            earnDeduElementRef.setEhcmLoanTransaction(earnDedElmRef.getEhcmLoanTransaction());
            earnDeduElementRef.setEhcmDisciplineAction(earnDedElmRef.getEhcmDisciplineAction());
            earnDeduElementRef.setBusinessMission(earnDedElmRef.getBusinessMission());
            earnDeduElementRef.setOvertimeTransaction(earnDedElmRef.getOvertimeTransaction());
            earnDeduElementRef.setEhcmEmpScholarship(earnDedElmRef.getEhcmEmpScholarship());
            earnDeduElementRef.setBenefitsAndAllowance(earnDedElmRef.getBenefitsAndAllowance());
            earnDeduElementRef.setTicketOrders(earnDedElmRef.getTicketOrders());
            earnDeduElementRef.setElementDetails(earnDeduElementDetail);
            OBDal.getInstance().save(earnDeduElementRef);
            earnDeduElementDetail.getEHCMEarnDeductElmRefList().add(earnDeduElementRef);
            OBDal.getInstance().save(earnDeduElementDetail);
          }
        }
      }

      // absence payment calculation
      if (PayrollBaseProcess.absPaymentComponents != null
          && PayrollBaseProcess.absPaymentComponents.length() > 0) {

        // for allowance
        /*
         * if (PayrollBaseProcess.absPaymentComponents.has("totalAbsPaymentValue") &&
         * PayrollBaseProcess.absPaymentComponents.has("elementId") && element.getId()
         * .equals(PayrollBaseProcess.absPaymentComponents.getString("elementId"))) {
         * totalAbsPaymentValue = new BigDecimal(
         * PayrollBaseProcess.absPaymentComponents.getString("totalAbsPaymentValue"));
         * earnDeduElementDetail.setCalculatedValue(
         * earnDeduElementDetail.getCalculatedValue().subtract(totalAbsPaymentValue)); }
         */
        // check any other leave present other than min amount
        if (PayrollBaseProcess.absPaymentComponents.has("leaveTypeOtherThanMinAmount")) {
          leaveTypeOtherThanMinAmount = PayrollBaseProcess.absPaymentComponents
              .getBoolean("leaveTypeOtherThanMinAmount");
        }
        // min amount validation
        if (PayrollBaseProcess.absPaymentComponents.has("minAmountList")) {
          JSONArray jsonminAmountArray = PayrollBaseProcess.absPaymentComponents
              .getJSONArray("minAmountList");
          if (jsonminAmountArray.length() > 0) {
            for (int j = 0; j < jsonminAmountArray.length(); j++) {
              absenceConfigMinAmount = BigDecimal.ZERO;
              absenceMinAmountValue = BigDecimal.ZERO;
              JSONObject minAmountJson = jsonminAmountArray.getJSONObject(j);
              if (element.getId().equals(minAmountJson.getString("elementTypeDefId"))) {
                if (minAmountJson.has("minAmountValue")) {
                  absenceConfigMinAmount = new BigDecimal(
                      minAmountJson.getString("minAmountValue"));
                }
                if (minAmountJson.has("minAmountAbsPayValue")) {
                  absenceMinAmountValue = new BigDecimal(
                      minAmountJson.getString("minAmountAbsPayValue"));
                }
                if (earnDeduElementDetail.getCalculatedValue().subtract(absenceMinAmountValue)
                    .compareTo(absenceConfigMinAmount) < 0 && !leaveTypeOtherThanMinAmount
                    && !element.getType().equalsIgnoreCase("NREC")) {
                  earnDeduElementDetail.setCalculatedValue(absenceConfigMinAmount);
                } else {
                  earnDeduElementDetail.setCalculatedValue(
                      earnDeduElementDetail.getCalculatedValue().subtract(absenceMinAmountValue));
                }
              }

            }
          }
        }
      }
      earnDedelm.setCalculatedValue(earnDeduElementDetail.getCalculatedValue());
      return earnDeduElementDetail;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : insertEarnDeductElementDetails() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while Inserting Earning And Deduction Element Details for Element "
          + element.getName();
      return null;
    }
  }

  public static void deleteEarnDeductElements(EHCMPayrollProcessLne payProcessLine,
      EHCMEarnDeductEmp earnDeductEmployeeDetail) {
    try {
      Query queryDelete = OBDal.getInstance().getSession()
          .createQuery("delete from " + EHCMEarnDeductElm.ENTITY_NAME
              + " e where e.payrollProcessLine.id = '" + payProcessLine.getId()
              + "' and e.employeeDetails.id = '" + earnDeductEmployeeDetail.getId() + "'");
      queryDelete.executeUpdate();
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : deleteEarnDeductElements() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while deleting Earning and Deduction Element";
    }
  }

  /**
   * Delete employee who all are not satisfy in employment group.
   * 
   * @param payProcessHrd
   * @param employeeList
   */
  public static void deletePayrollEmployee(EHCMPayrollProcessHdr payProcessHrd,
      List<GenericPayrollDTO> employeeList) {
    boolean match = false;
    String empId = "";
    try {
      OBQuery<EHCMPayrollProcessLne> lineQry = OBDal.getInstance()
          .createQuery(EHCMPayrollProcessLne.class, " payrollProcessHeader.id =:hdrId");
      lineQry.setNamedParameter("hdrId", payProcessHrd.getId());
      List<EHCMPayrollProcessLne> lineList = lineQry.list();

      for (EHCMPayrollProcessLne line : lineList) {
        empId = line.getEmployee().getId();
        match = false;

        for (GenericPayrollDTO empDTO : employeeList) {
          if (empId.equals(empDTO.getEmploymentGroup().getEmployeeId())) {
            match = true;
            break;
          }
        }
        if (!match) {
          Query queryDelete = OBDal.getInstance().getSession()
              .createQuery("delete from " + EHCMPayrollProcessLne.ENTITY_NAME
                  + " e where e.payrollProcessHeader.id =:hdrID and e.employee.id=:empID");
          queryDelete.setParameter("hdrID", payProcessHrd.getId());
          queryDelete.setParameter("empID", empId);
          queryDelete.executeUpdate();
        }
      }
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : deletePayrollEmployee() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while deleting Earning and Deduction Element";
    }
  }

  /**
   * This method is used to return list of employees based on employment group
   * 
   * @param payrollDTO
   *          --EmploymentGroupDTO -- EmploymentGrpID,startDate,endDate,PayDefID
   * @return List<GenericPayrollDTO> --EmploymentGroupDTO -- employeeId, Employee Obj.
   */
  public static List<GenericPayrollDTO> getEmployeesFromEmploymntGrp(GenericPayrollDTO payrollDTO) {
    String EmploymentGrpID = payrollDTO.getEmploymentGroup().getEmploymentGrpId();
    String startDate = payrollDTO.getEmploymentGroup().getStartDate();
    String endDate = payrollDTO.getEmploymentGroup().getEndDate();
    List<GenericPayrollDTO> GenericPayrollList = new ArrayList<>();
    long groupCode = 0;
    int lineNo = 0, linesize = 0;
    String whereclause = "";
    String operator = "";
    try {

      OBQuery<EmploymentGroupLines> empGrpLnList = OBDal.getInstance().createQuery(
          EmploymentGroupLines.class, " ehcmEmploymentGroup.id =:empgrpId order by groupCode asc");
      empGrpLnList.setNamedParameter("empgrpId", EmploymentGrpID);
      List<EmploymentGroupLines> empGrpLn = empGrpLnList.list();

      if (empGrpLn != null && empGrpLn.size() > 0) {
        linesize = empGrpLn.size();
        for (EmploymentGroupLines line : empGrpLn) {
          lineNo = lineNo + 1;
          if (line.getOperator().equals("EQ")) {
            operator = " = ";
          } else if (line.getOperator().equals("NE")) {
            operator = " != ";
          } else if (line.getOperator().equals("LT")) {
            operator = " < ";
          } else if (line.getOperator().equals("LE")) {
            operator = " <= ";
          } else if (line.getOperator().equals("GT")) {
            operator = " > ";
          } else if (line.getOperator().equals("GE")) {
            operator = " >= ";
          }

          // if group code same then "or" else "and"
          if (line.getGroupCode() == groupCode) {
            whereclause = whereclause + " or ";
          } else if (lineNo == 1) {
            whereclause = whereclause + " and (";
          } else {
            whereclause = whereclause + " ) and (";
          }

          // form the condition based on item.
          if (line.getItem().equals("ORG")) {
            whereclause = whereclause + " e.organization.id " + operator + " '"
                + line.getEhcmEmpgrpValueV().getId() + "' ";
          } else if (line.getItem().equals("LOC")) {
            whereclause = whereclause + " e.position.department.ehcmEscmLoc.id " + operator + " '"
                + line.getEhcmEmpgrpValueV().getId() + "' ";
          } else if (line.getItem().equals("REG")) {
            whereclause = whereclause + " e.position.department.ehcmCRegion.id " + operator + " '"
                + line.getEhcmEmpgrpValueV().getId() + "' ";
          } else if (line.getItem().equals("DEP")) {
            whereclause = whereclause + " e.position.department.id " + operator + " '"
                + line.getEhcmEmpgrpValueV().getId() + "' ";
          } else if (line.getItem().equals("GC")) {
            whereclause = whereclause + " e.ehcmEmpPerinfo.gradeClass.id " + operator + " '"
                + line.getEhcmEmpgrpValueV().getId() + "' ";
          } else if (line.getItem().equals("GD")) {
            whereclause = whereclause + " e.employmentgrade.id " + operator + " '"
                + line.getEhcmEmpgrpValueV().getId() + "' ";
          } else if (line.getItem().equals("JT")) {
            whereclause = whereclause + " e.position.ehcmJobs.id " + operator + " '"
                + line.getEhcmEmpgrpValueV().getId() + "' ";
          } else if (line.getItem().equals("EMP")) {
            whereclause = whereclause + " e.ehcmEmpPerinfo.id " + operator + " '"
                + line.getEhcmEmpgrpValueV().getId() + "' ";
          } else if (line.getItem().equals("ER")) {
            whereclause = whereclause + " e.changereason " + operator + " '"
                + line.getEhcmEmpgrpValueV().getId() + "' ";
          } else if (line.getItem().equals("AGE")) { // ask
            whereclause = whereclause
                + " cast(substr(:todate,7,10)as int)-cast(to_char(e.ehcmEmpPerinfo.dob,'yyyy') as int) "
                + operator + " '" + line.getNumericValue() + "' ";
          } else if (line.getItem().equals("POS")) { // ask
            whereclause = whereclause
                + " cast(substr(:todate,7,10)as int)-cast(to_char(e.ehcmEmpPerinfo.hiredate,'yyyy') as int)  "
                + operator + " '" + line.getNumericValue() + "' ";
          }

          // finally append closing bracket to end condition.
          if (lineNo == linesize) {
            whereclause = whereclause + " ) ";
          }

          groupCode = line.getGroupCode();

        }

        String hqlString = " select distinct e.ehcmEmpPerinfo from Ehcm_Employment_Info e where e.ehcmEmpPerinfo.status<>'UP' and  ";
        // if payroll definition is associated then vaidate.
        if (StringUtils.isNotEmpty(payrollDTO.getEmploymentGroup().getPayDefId())) {
          hqlString = hqlString + " e.ehcmPayrollDefinition.id = :payDefId and ";
        }
        hqlString = hqlString
            + "((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
            + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
            + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
            + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) ";

        hqlString = hqlString + whereclause;
        final Query empQuery = OBDal.getInstance().getSession().createQuery(hqlString);
        empQuery.setParameter("fromdate", startDate);
        empQuery.setParameter("todate", endDate);
        if (StringUtils.isNotEmpty(payrollDTO.getEmploymentGroup().getPayDefId())) {
          empQuery.setParameter("payDefId", payrollDTO.getEmploymentGroup().getPayDefId());
        }

        @SuppressWarnings("unchecked")
        List<EhcmEmpPerInfo> empList = empQuery.list();
        if (empList != null && empList.size() > 0) {
          for (EhcmEmpPerInfo emp : empList) {
            GenericPayrollDTO DTO = new GenericPayrollDTO();
            EmploymentGroupDTO EmpGroup = new EmploymentGroupDTO();
            EmpGroup.setEmployeeId(emp.getId());
            EmpGroup.setEmployeeInfo(emp);
            DTO.setEmploymentGroup(EmpGroup);
            GenericPayrollList.add(DTO);
          }
        }
      }
      return GenericPayrollList;

    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.error("Error in PayrollBaseProcess.java : getEmployeesFromEmploymntGrp() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMajor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Employees of Payroll Process in employment group : ";
    }

    return GenericPayrollList;
  }

  /**
   * 
   * @param payrollDTO
   *          --EmploymentGroupDTO --employeeID,payrollDefID,startDate,endDate
   * @return List<GenericPayrollDTO> --EmploymentGroupDTO -- employeeId, Employee Obj.
   */
  public static List<GenericPayrollDTO> getEmployeesOfPayrollProcess(GenericPayrollDTO payrollDTO) {
    String employeeID = payrollDTO.getEmploymentGroup().getEmployeeId();
    String payrollDefID = payrollDTO.getEmploymentGroup().getPayDefId();
    String startDate = payrollDTO.getEmploymentGroup().getStartDate();
    String endDate = payrollDTO.getEmploymentGroup().getEndDate();
    List<GenericPayrollDTO> GenericPayrollList = new ArrayList<>();

    try {
      String hqlString = " select distinct e.ehcmEmpPerinfo from Ehcm_Employment_Info e where e.ehcmEmpPerinfo.status<>'UP' and e.ehcmPayrollDefinition.id = :payDefId and "
          + "((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) ";

      if (StringUtils.isNotEmpty(employeeID)) {
        hqlString = hqlString + "and e.ehcmEmpPerinfo.id = :employeeId ";
      }

      final Query empQuery = OBDal.getInstance().getSession().createQuery(hqlString);
      empQuery.setParameter("payDefId", payrollDefID);
      empQuery.setParameter("fromdate", startDate);
      empQuery.setParameter("todate", endDate);
      if (StringUtils.isNotEmpty(employeeID)) {
        empQuery.setParameter("employeeId", employeeID);
      }

      @SuppressWarnings("unchecked")
      List<EhcmEmpPerInfo> empList = empQuery.list();

      if (empList != null && empList.size() > 0) {
        for (EhcmEmpPerInfo emp : empList) {
          GenericPayrollDTO DTO = new GenericPayrollDTO();
          EmploymentGroupDTO EmpGroup = new EmploymentGroupDTO();
          EmpGroup.setEmployeeId(emp.getId());
          EmpGroup.setEmployeeInfo(emp);
          DTO.setEmploymentGroup(EmpGroup);
          GenericPayrollList.add(DTO);
        }
      }
      return GenericPayrollList;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : getEmployeesForPayrollProcess() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMajor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Employees of Payroll Process : ";
    }
    return GenericPayrollList;
  }

  /**
   * Get bank details info for employee
   * 
   * @param bankDTO
   *          --BankDetailsDTO -- Employeeid,startdate,enddate
   * @return GenericPayrollDTO --BankDetailsDTO --bankid
   */
  public static GenericPayrollDTO getBankDetails(GenericPayrollDTO bankDTO) {
    GenericPayrollDTO DTO = null;
    try {
      OBQuery<EHCMPpmBankdetail> bankdetails = OBDal.getInstance().createQuery(
          EHCMPpmBankdetail.class,
          "as e join e.efinBank as b join e.ehcmPersonalPaymethd p where p.ehcmEmpPerinfo.id =:empId and "
              + "((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
              + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
              + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
              + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) "
              + "order by e.startDate desc ");
      bankdetails.setNamedParameter("empId", bankDTO.getBankDetails().getEmployeeId());
      bankdetails.setNamedParameter("fromdate", bankDTO.getBankDetails().getDateFrom());
      bankdetails.setNamedParameter("todate", bankDTO.getBankDetails().getDateTo());
      bankdetails.setMaxResult(1);
      List<EHCMPpmBankdetail> bankDetailList = bankdetails.list();
      if (bankDetailList != null && bankDetailList.size() > 0) {
        DTO = new GenericPayrollDTO();
        BankDetailsDTO bankDto = new BankDetailsDTO();
        DTO.setBankdetailOB(bankdetails.list().get(0));
        bankDto.setBankId(bankdetails.list().get(0).getId());
        DTO.setBankDetails(bankDto);
      }
      return DTO;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error("Error in PayrollBaseProcess.java : getBankDetails() ");
      }
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting BankDetils of employee ";
      return null;
    }
  }

  // get disciplinary action for an employee.
  /**
   * used to get disciplinary action for an employee.
   * 
   * @param payrollDTO
   *          --EmploymentGroupDTO --employeeID,startDate,endDate
   * @return List<GenericPayrollDTO> --EmploymentGroupDTO -- DisciplineId, Discipline Obj.
   */
  public static List<GenericPayrollDTO> getDisciplinaryAction(GenericPayrollDTO payrollDTO) {
    List<GenericPayrollDTO> GenericPayrollList = new ArrayList<>();
    String employeeId = payrollDTO.getEmploymentGroup().getEmployeeId();
    String startDate = payrollDTO.getEmploymentGroup().getStartDate();
    String endDate = payrollDTO.getEmploymentGroup().getEndDate();
    String payrollId = payrollDTO.getEmploymentGroup().getPayrollLineId();

    List<EhcmDisciplineAction> disciplinaryList = new ArrayList<EhcmDisciplineAction>();
    try {

      OBQuery<EhcmDisciplineAction> disciplineAction = OBDal.getInstance().createQuery(
          EhcmDisciplineAction.class,
          " as e where e.employee.id = :empId and e.decisionType<>'CA' and e.decisionStatus ='I' and (e.payrollProcessLine.id = null or e.payrollProcessLine.id =:payrollline ) "
              + "and e.id not in(select a.originalDecisionNo.id from ehcm_discipline_action a where a.originalDecisionNo is not null and a.decisionStatus='I' and a.employee.id=:empId) "
              + "and e.effectiveDate <= to_date(:enddate,'dd-MM-yyyy') and e.effectiveDate >= to_date(:startdate,'dd-MM-yyyy') ");
      disciplineAction.setNamedParameter("empId", employeeId);
      disciplineAction.setNamedParameter("enddate", endDate);
      disciplineAction.setNamedParameter("startdate", startDate);
      disciplineAction.setNamedParameter("payrollline", payrollId);
      disciplinaryList = disciplineAction.list();
      if (disciplinaryList != null && disciplinaryList.size() > 0) {
        for (EhcmDisciplineAction disciplinaction : disciplinaryList) {
          GenericPayrollDTO DTO = new GenericPayrollDTO();
          EmploymentGroupDTO EmpGroup = new EmploymentGroupDTO();
          EmpGroup.setDisciplineId(disciplinaction.getId());
          EmpGroup.setDisciplineAction(disciplinaction);
          DTO.setEmploymentGroup(EmpGroup);
          GenericPayrollList.add(DTO);
        }
      }
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : getDisciplinaryAction() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Disciplinary action of employee: ";
    }

    return GenericPayrollList;
  }

  /**
   * Get loan transaction list for an employee.
   * 
   * @param periodStartDate
   * @param EmpID
   * @return
   */
  public static List<EHCMLoanTransaction> getLoanTransaction(Date periodStartDate, String EmpID) {
    List<EHCMLoanTransaction> loanTransactionList = null;
    try {
      OBQuery<EHCMLoanTransaction> loanTransaction = OBDal.getInstance().createQuery(
          EHCMLoanTransaction.class,
          " as e where e.employee.id =:empID and e.decisionStatus='I' and e.remamount > 0 "
              + " and e.id not in(select a.originalDecisionNo.id from EHCM_Loan_Transaction a where a.originalDecisionNo is not null and a.decisionStatus='I' and a.employee.id =:empID ) "
              + " and e.firstInstallmentPeriod.startDate <= :periodDate");
      loanTransaction.setNamedParameter("empID", EmpID);
      loanTransaction.setNamedParameter("periodDate", periodStartDate);
      loanTransactionList = loanTransaction.list();
      log.info("loan qryyyy:" + loanTransaction.getWhereAndOrderBy());
      return loanTransactionList;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : getLoanTransaction ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting loan transaction of employee: ";
    }
    return loanTransactionList;
  }

  public static boolean isEarnDeductEmpHasOtherProcessElement(EHCMPayrollProcessLne payProcessLine,
      EHCMEarnDeductEmp earnDeductEmployeeDetail) {
    try {
      String whereClause = " e where e.payrollProcessLine.id <> :payProcessLineId and e.employeeDetails.id = :employeeDetailsId order by e.creationDate desc ";

      OBQuery<EHCMEarnDeductElm> earnDeduEleQry = OBDal.getInstance()
          .createQuery(EHCMEarnDeductElm.class, whereClause);
      earnDeduEleQry.setNamedParameter("payProcessLineId", payProcessLine.getId());
      earnDeduEleQry.setNamedParameter("employeeDetailsId", earnDeductEmployeeDetail.getId());
      if (earnDeduEleQry.list().size() > 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : isEarnDeductEmpHasOtherProcessElement() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while checking Earning Deduction Employee has element from Different Process "
          + earnDeductEmployeeDetail.getEmployeeName();
      return false;
    }
  }

  /**
   * Insert loan history object
   * 
   * @param loanobj
   * @param payrollProcessLne
   * @param amount
   * @return
   */
  public static boolean insertLoanHistory(EHCMLoanTransaction loanobj,
      EHCMPayrollProcessLne payrollProcessLne, BigDecimal amount) {
    try {
      EhcmLoanHistory loanHistory = OBProvider.getInstance().get(EhcmLoanHistory.class);
      loanHistory.setClient(loanobj.getClient());
      loanHistory.setOrganization(loanobj.getOrganization());
      loanHistory.setEnabled(true);
      loanHistory.setCreatedBy(OBContext.getOBContext().getUser());
      loanHistory.setUpdatedBy(OBContext.getOBContext().getUser());
      loanHistory.setPayrollPeriod(payrollProcessLne.getPayrollProcessHeader().getPayrollPeriod());
      loanHistory.setPayrollProcessLine(payrollProcessLne);
      loanHistory.setEhcmLoanTransaction(loanobj);
      loanHistory.setAmount(amount);
      OBDal.getInstance().save(loanHistory);
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : insertLoanHistory() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while insert loan history ";
      return false;
    }
    return true;
  }

  /**
   * get loan history object if already processed for same period.
   * 
   * @param loanobj
   * @param payrollProcessLne
   * @return
   */
  public static EhcmLoanHistory getLoanhistoryObj(EHCMLoanTransaction loanobj,
      EHCMPayrollProcessLne payrollProcessLne) {
    EhcmLoanHistory loanHistory = null;
    try {
      List<EhcmLoanHistory> loanList = null;
      OBQuery<EhcmLoanHistory> loanHistoryList = OBDal.getInstance().createQuery(
          EhcmLoanHistory.class,
          "ehcmLoanTransaction.id=:loanId and payrollProcessLine.id=:payrollLnId");
      loanHistoryList.setNamedParameter("loanId", loanobj.getId());
      loanHistoryList.setNamedParameter("payrollLnId", payrollProcessLne.getId());
      loanList = loanHistoryList.list();
      if (loanList.size() > 0) {
        loanHistory = loanList.get(0);
      }
      return loanHistory;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : getLoanhistoryObj() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting loan history ";
      return loanHistory;
    }
  }

  /**
   * Get Overtime transaction for an employee
   * 
   * @param empPerInfo
   * @param payrollPeriod
   * @param payrollProcessLne
   * @return
   */
  public static List<EhcmEmployeeOvertime> getOvertimeForEmployee(EhcmEmpPerInfo empPerInfo,
      EHCMPayrolldefPeriod payrollPeriod, EHCMPayrollProcessLne payrollProcessLne) {
    List<EhcmEmployeeOvertime> overTimeList = new ArrayList<EhcmEmployeeOvertime>();
    try {
      String whereClause = " e where e.employee.id = :employeeId and e.payrollPeriod.id = :payrollPeriodId "
          + "and e.decisionType='OP' and e.issueDecision='Y' and (e.payrollProcessLine.id = null or e.payrollProcessLine.id =:payrollline ) order by e.startDate ";

      OBQuery<EhcmEmployeeOvertime> overTimeQry = OBDal.getInstance()
          .createQuery(EhcmEmployeeOvertime.class, whereClause);
      overTimeQry.setNamedParameter("employeeId", empPerInfo.getId());
      overTimeQry.setNamedParameter("payrollPeriodId", payrollPeriod.getId());
      overTimeQry.setNamedParameter("payrollline", payrollProcessLne.getId());

      overTimeList = overTimeQry.list();
      return overTimeList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getOvertimeForEmployee() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Overtime of Employee "
          + empPerInfo.getName();
      return overTimeList;
    }
  }

  public static List<EhcmEmployeeOvertime> getOvertimeForEmployee(EhcmEmpPerInfo empPerInfo,
      EHCMPayrollProcessLne payrollProcessLne, String startDate, String endDate) {
    List<EhcmEmployeeOvertime> overTimeList = new ArrayList<EhcmEmployeeOvertime>();
    try {
      String whereClause = " e where e.employee.id = :employeeId "
          + "and e.decisionType='OP' and e.issueDecision='Y'"
          + "and (e.payrollProcessLine.id = null or e.payrollProcessLine.id =:payrollline) "
          + "and e.paymentStartDate <= to_date(:enddate,'dd-MM-yyyy') and e.paymentStartDate >= to_date(:startdate,'dd-MM-yyyy') "
          + "order by e.paymentStartDate ";

      OBQuery<EhcmEmployeeOvertime> overTimeQry = OBDal.getInstance()
          .createQuery(EhcmEmployeeOvertime.class, whereClause);
      overTimeQry.setNamedParameter("employeeId", empPerInfo.getId());
      overTimeQry.setNamedParameter("payrollline", payrollProcessLne.getId());
      overTimeQry.setNamedParameter("startdate", startDate);
      overTimeQry.setNamedParameter("enddate", endDate);

      overTimeList = overTimeQry.list();
      return overTimeList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getOvertimeForEmployee() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Overtime of Employee "
          + empPerInfo.getName();
      return overTimeList;
    }
  }

  @SuppressWarnings("rawtypes")
  public static BigDecimal calculateAbsencePaymentValue(EmploymentInfo employment, Date startDate,
      Date endDate, BigDecimal processingDays, BigDecimal differenceDays, Date payrollEndDate,
      boolean isBaseCalculation, JSONObject absEmploymentPeriodJSON,
      EHCMElmttypeDef payrollElement) {
    BigDecimal totalAbsPaymentValue = BigDecimal.ZERO;
    BigDecimal payscale = BigDecimal.ZERO;
    BigDecimal oneDayPayScale = BigDecimal.ZERO;
    BigDecimal days = BigDecimal.ZERO;
    try {

      // Pay Scale
      String empmtPayStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String empmtPayEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      String whereClause = "e where e.ehcmPayscale.ehcmGrade.id=:gradeId "
          + "and e.ehcmProgressionpt.id = :pointsId "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) "
          + "order by e.startDate ";

      OBQuery<ehcmpayscaleline> payScaleQry = OBDal.getInstance()
          .createQuery(ehcmpayscaleline.class, whereClause);
      payScaleQry.setNamedParameter("gradeId", employment.getEmploymentgrade().getId());
      payScaleQry.setNamedParameter("pointsId",
          employment.getEhcmPayscaleline().getEhcmProgressionpt().getId());
      payScaleQry.setNamedParameter("startDate", empmtPayStartDate);
      payScaleQry.setNamedParameter("endDate", empmtPayEndDate);
      payScaleQry.setFilterOnActive(false);
      List<ehcmpayscaleline> payScaleLneList = payScaleQry.list();
      for (ehcmpayscaleline payScaleLne : payScaleLneList) {
        if (!PayrollBaseProcess.errorFlagMinor) {
          log.info("PayScale Days applicable");
          JSONObject payScalePeriodJSON = PayrollBaseProcess.getOverlapingDateRange(
              payScaleLne.getStartDate(), payScaleLne.getEndDate(), startDate, endDate);

          if (payScalePeriodJSON != null) {
            days = new BigDecimal(payScalePeriodJSON.getLong("days"));

            // If pay scale is for payroll end date, Check and add extra days in month
            Date payScaleEndDate = PayrollConstants.dateFormat
                .parse(payScalePeriodJSON.getString("endDate"));
            if (differenceDays.compareTo(BigDecimal.ZERO) > 0
                && payScaleEndDate.compareTo(payrollEndDate) == 0) {
              days = days.add(differenceDays);
            }

            BigDecimal payScaleAmount = payScaleLne.getAmount();
            oneDayPayScale = payScaleAmount.divide(processingDays, 6, BigDecimal.ROUND_HALF_UP);
            payscale = oneDayPayScale.multiply(days).setScale(6, BigDecimal.ROUND_HALF_UP);

            // calculate absence payment value other than leave occurance(block wise)absence type
            JSONObject leaveJson = getEmpLeaveListBasedonPayPeriod(employment,
                PayrollConstants.dateFormat.parse(payScalePeriodJSON.getString("startDate")),
                PayrollConstants.dateFormat.parse(payScalePeriodJSON.getString("endDate")));

            totalAbsPaymentValue = totalAbsPaymentValue
                .add(calAbsencePaymentValueOtherThanLeaveOccuranceType(leaveJson, oneDayPayScale,
                    payscale, payrollElement));

            JSONObject leaveListForLeaveOccurance = getJSONObjectForLeaveOccuranceBlockAbsPaymentValue(
                employment,
                PayrollConstants.dateFormat.parse(payScalePeriodJSON.getString("startDate")),
                PayrollConstants.dateFormat.parse(payScalePeriodJSON.getString("endDate")));

            totalAbsPaymentValue = totalAbsPaymentValue
                .add(calculateLeaveOccuranceBlockAbsPaymentValue(employment, oneDayPayScale,
                    leaveListForLeaveOccurance, payrollElement));

          }
        } else {
          break;
        }
      }

      /*
       * if (PayrollBaseProcess.absPaymentComponents.has("totalAbsPaymentValue")) {
       * PayrollBaseProcess.absPaymentComponents.put("totalAbsPaymentValue", new BigDecimal(
       * PayrollBaseProcess.absPaymentComponents.getString("totalAbsPaymentValue"))
       * .add(totalAbsPaymentValue)); } else {
       * PayrollBaseProcess.absPaymentComponents.put("totalAbsPaymentValue", totalAbsPaymentValue);
       * }
       */

      return totalAbsPaymentValue;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : calculateAbsencePaymentValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while calculateAbsencePaymentValue ";
      return totalAbsPaymentValue;
    }
  }

  @SuppressWarnings("rawtypes")
  public static BigDecimal calAbsencePaymentValueOtherThanLeaveOccuranceType(JSONObject leaveJson,
      BigDecimal oneDayPayScale, BigDecimal payscale, EHCMElmttypeDef payrollElement) {
    EHCMAbsenceType absenceType = null;
    JSONObject json = null;
    EHCMAbsencePayment absPaymentObj = null;
    BigDecimal leaveDays = BigDecimal.ZERO;
    BigDecimal absPaymentValue = BigDecimal.ZERO;
    BigDecimal totalAbsPaymentValue = BigDecimal.ZERO;
    BigDecimal calAbsPaymentValue = BigDecimal.ZERO;
    BigDecimal totalDaysAbsPaymentValue = BigDecimal.ZERO;
    JSONObject minAmountJson = null;
    JSONArray jsonMinAmountarray = new JSONArray();
    try {
      if (leaveJson != null && leaveJson.length() > 0) {
        JSONArray jsonArray = leaveJson.getJSONArray("leaveList");
        if (jsonArray.length() > 0) {
          for (int i = 0; i < jsonArray.length(); i++) {
            json = jsonArray.getJSONObject(i);
            // get absence payment configuration object
            if (json.has("absenceTypeId")) {
              absenceType = OBDal.getInstance().get(EHCMAbsenceType.class,
                  json.getString("absenceTypeId"));
              absPaymentObj = getAbsencePaymentConfigValue(absenceType.getId(), payrollElement);
              if (json.has("leaveDays")) {
                leaveDays = new BigDecimal(json.getString("leaveDays"));
              }

              if (absenceType.getCategory().equals("PL") && absPaymentObj != null) {
                if (absPaymentObj.getMin().compareTo(BigDecimal.ZERO) == 0
                    && absPaymentObj.getMax().compareTo(BigDecimal.ZERO) == 0) {

                  if (absPaymentObj.getMINAmount().compareTo(BigDecimal.ZERO) == 0) {
                    if (absPaymentObj.getCalculatedValue().compareTo(BigDecimal.ONE) != 0
                        && absPaymentObj.getPayrollElement() != null && absPaymentObj
                            .getPayrollElement().getId().equals(payrollElement.getId())) {
                      PayrollBaseProcess.absPaymentComponents.put("leaveTypeOtherThanMinAmount",
                          true);
                      absPaymentValue = absPaymentObj.getCalculatedValue();
                      if (absPaymentValue.compareTo(BigDecimal.ZERO) == 0) {
                        totalAbsPaymentValue = totalAbsPaymentValue
                            .add(leaveDays.multiply(oneDayPayScale));
                      } else {
                        totalDaysAbsPaymentValue = leaveDays.multiply(oneDayPayScale);
                        calAbsPaymentValue = totalDaysAbsPaymentValue
                            .subtract(totalDaysAbsPaymentValue.multiply(absPaymentValue));
                        totalAbsPaymentValue = totalAbsPaymentValue.add(calAbsPaymentValue);

                      }
                    }
                  } else {

                    absPaymentValue = absPaymentObj.getCalculatedValue();
                    if (absPaymentValue.compareTo(BigDecimal.ZERO) == 0) {
                      calAbsPaymentValue = leaveDays.multiply(oneDayPayScale);
                      // totalAbsPaymentValue = totalAbsPaymentValue.add(calAbsPaymentValue);
                    } else {
                      totalDaysAbsPaymentValue = leaveDays.multiply(oneDayPayScale);
                      calAbsPaymentValue = totalDaysAbsPaymentValue
                          .subtract(totalDaysAbsPaymentValue.multiply(absPaymentValue));
                      // totalAbsPaymentValue = totalAbsPaymentValue.add(calAbsPaymentValue);
                    }
                    if (!payrollElement.getType().equalsIgnoreCase("NREC")) {
                      if (PayrollBaseProcess.absPaymentComponents != null
                          && PayrollBaseProcess.absPaymentComponents.length() > 0
                          && PayrollBaseProcess.absPaymentComponents.has("minAmountList")) {
                        JSONArray jsonminAmountArray = PayrollBaseProcess.absPaymentComponents
                            .getJSONArray("minAmountList");
                        if (jsonminAmountArray.length() > 0) {
                          for (int j = 0; j < jsonminAmountArray.length(); j++) {
                            minAmountJson = jsonminAmountArray.getJSONObject(j);
                            if (minAmountJson.getString("absenceTypeId")
                                .equals(absenceType.getId())) {
                              minAmountJson.put("minAmountAbsPayValue",
                                  new BigDecimal(minAmountJson.getString("minAmountAbsPayValue"))
                                      .add(calAbsPaymentValue));
                            } else {
                              minAmountJson = new JSONObject();
                              minAmountJson.put("absenceTypeId", absenceType.getId());
                              minAmountJson.put("minAmountValue", absPaymentObj.getMINAmount());
                              minAmountJson.put("minAmountAbsPayValue", calAbsPaymentValue);
                              minAmountJson.put("elementTypeDefId",
                                  absPaymentObj.getPayrollElement().getId());
                              jsonMinAmountarray.put(minAmountJson);
                              PayrollBaseProcess.absPaymentComponents.put("minAmountList",
                                  jsonMinAmountarray);
                            }
                          }
                        }
                      } else {

                        minAmountJson = new JSONObject();
                        minAmountJson.put("absenceTypeId", absenceType.getId());
                        minAmountJson.put("minAmountValue", absPaymentObj.getMINAmount());
                        minAmountJson.put("minAmountAbsPayValue", calAbsPaymentValue);
                        minAmountJson.put("elementTypeDefId",
                            absPaymentObj.getPayrollElement().getId());
                        jsonMinAmountarray.put(minAmountJson);
                        PayrollBaseProcess.absPaymentComponents.put("minAmountList",
                            jsonMinAmountarray);
                      }
                    } else {
                      totalAbsPaymentValue = totalAbsPaymentValue.add(calAbsPaymentValue);
                    }
                    //
                    /*
                     * if ((payscale.subtract(calAbsPaymentValue))
                     * .compareTo(absPaymentObj.getMINAmount()) < 0) { totalAbsPaymentValue =
                     * totalAbsPaymentValue .add(payscale.subtract(absPaymentObj.getMINAmount())); }
                     * else { totalAbsPaymentValue = totalAbsPaymentValue.add(calAbsPaymentValue); }
                     */
                  }
                }
              } else {
                if (absenceType.getCategory().equals("UPL")) {
                  PayrollBaseProcess.absPaymentComponents.put("leaveTypeOtherThanMinAmount", true);
                  totalAbsPaymentValue = totalAbsPaymentValue
                      .add(leaveDays.multiply(oneDayPayScale));
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      log.error(
          "Error in PayrollBaseProcessDAO.java : calAbsencePaymentValueOtherThanLeaveOccuranceType() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while calAbsencePaymentValueOtherThanLeaveOccuranceType ";
      return totalAbsPaymentValue;
    }
    return totalAbsPaymentValue;
  }

  public static EHCMAbsencePayment getAbsencePaymentConfigValue(String absenceTypeId,
      EHCMElmttypeDef payrollElement) {
    List<EHCMAbsencePayment> absPaymentList = null;
    EHCMAbsencePayment absPaymentObj = null;
    try {

      if (absenceTypeId != null) {
        OBQuery<EHCMAbsencePayment> absencePaymentQry = OBDal.getInstance().createQuery(
            EHCMAbsencePayment.class,
            " as e where e.absenceType.id=:absenceTypeId and e.payrollElement.id=:payrollElementId  ");
        absencePaymentQry.setNamedParameter("absenceTypeId", absenceTypeId);
        absencePaymentQry.setNamedParameter("payrollElementId", payrollElement.getId());
        absencePaymentQry.setMaxResult(1);
        absPaymentList = absencePaymentQry.list();
        if (absPaymentList.size() > 0) {
          absPaymentObj = absPaymentList.get(0);
          return absPaymentObj;
        }
      }

    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : getAbsencePaymentConfigValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getAbsencePaymentConfigValue ";
      return absPaymentObj;
    }
    return absPaymentObj;
  }

  @SuppressWarnings("rawtypes")
  public static JSONObject getEmpLeaveListBasedonPayPeriod(EmploymentInfo employment,
      Date payPeriodStartdate, Date payPeriodEnddate) {
    Query query = null;
    JSONObject leaveResult = new JSONObject(), json = null;
    JSONArray jsonArray = new JSONArray();

    List leaveList = null;
    Date payPeriodStartDate = null;
    Date payPeriodEndDate = null;
    Date leaveStartDate = null;
    Date leaveEndDate = null;
    int leaveDays = 0;

    DateFormat yearFormat = Utility.YearFormat;
    try {
      payPeriodEndDate = payPeriodEnddate;
      payPeriodStartDate = payPeriodStartdate;

      String sqlString = " select hdr.ehcm_absence_type_id, hdr.ehcm_emp_perinfo_id,hdr.subtype, ln.startdate,ln.enddate,ln.leavedays,ln.ehcm_emp_leaveln_id ,ln.ehcm_absence_attendance_id from ehcm_emp_leaveln ln "
          + " join ehcm_emp_leave hdr on hdr.ehcm_emp_leave_id= ln.ehcm_emp_leave_id "
          + " where hdr.ehcm_emp_perinfo_id=:employeeId "
          + " and ln.leave_type<>'AC' and ln.ehcm_decision_balance_id is null  "
          + " and ((to_date(to_char(ln.startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startdate) "
          + " and to_date(to_char(coalesce (ln.enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') "
          + "      <= to_date(:enddate,'dd-MM-yyyy')) "
          + " or (to_date(to_char( coalesce (ln.enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy')  "
          + "    >= to_date(:startdate) "
          + " and to_date(to_char(ln.startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:enddate,'dd-MM-yyyy'))) "
          + " and  hdr.ehcm_absence_type_id in (select abs.ehcm_absence_type_id from ehcm_absence_type abs where abs.accrual_reset_date<>'LO' ) "
          + "  and        hdr.ehcm_absence_type_id in ( select ehcm_absence_type_id from ehcm_absence_payment  where ad_client_id= :clientId)      "
          + " order by hdr.ehcm_absence_type_id ";
      // need to add absence attendance id startdate condition
      query = OBDal.getInstance().getSession().createSQLQuery(sqlString);
      query.setParameter("employeeId", employment.getEhcmEmpPerinfo().getId());
      query.setParameter("startdate", Utility.formatDate(payPeriodStartDate));
      query.setParameter("enddate", Utility.formatDate(payPeriodEndDate));
      query.setParameter("clientId", employment.getClient().getId());
      leaveList = query.list();
      if (leaveList != null && leaveList.size() > 0) {
        for (Object leaveLnObj : leaveList) {
          Object[] leaveln = (Object[]) leaveLnObj;

          if (leaveln[0] != null) {
            json = new JSONObject();
            json.put("leaveLnId", leaveln[6].toString());
            json.put("absenceAttendanceId", leaveln[7].toString());
            EHCMAbsenceType absenceType = OBDal.getInstance().get(EHCMAbsenceType.class,
                leaveln[0].toString());

            // get dependent list
            if (absenceType.getEHCMAbsenceTypeActionList().size() > 0) {
              absenceType = absenceType.getEHCMAbsenceTypeActionList().get(0).getDependent();
            }
            json.put("absenceTypeId", leaveln[0].toString());

            if (leaveln[3] != null) {
              leaveStartDate = (Date) leaveln[3];
            }
            if (leaveln[4] != null) {
              leaveEndDate = (Date) leaveln[4];
            }
            // start date calculation
            if (payPeriodStartdate.compareTo(leaveStartDate) <= 0) {
              json.put("startDate", leaveStartDate);
            } else {
              json.put("startDate", yearFormat.format(payPeriodStartdate));
            }
            // end date calculation
            if (payPeriodEndDate.compareTo(leaveEndDate) <= 0) {
              json.put("endDate", yearFormat.format(payPeriodEndDate));
            } else {
              json.put("endDate", leaveEndDate);
            }
            // calculate the total leave days
            leaveDays = AbsenceIssueDecisionDAOImpl.getEmpLevDateCountBasOnIsIncludeHolday(
                yearFormat.parse(json.getString("startDate")),
                yearFormat.parse(json.getString("endDate")), absenceType);
            json.put("leaveDays", leaveDays);
            jsonArray.put(json);
          }
        }
        leaveResult.put("leaveList", jsonArray);
      }

    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : getEmpLeaveListBasedonPayPeriod() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getEmpLeaveListBasedonPayPeriod ";
      return leaveResult;
    }
    return leaveResult;
  }

  @SuppressWarnings("rawtypes")
  public static BigDecimal getTotalLeaveDaysForPerivousDate(EmploymentInfo employment,
      Date startDate, Date endDate, String leaveBlockHdId, Date payPeriodStartdate,
      Date payPeriodEnddate, EHCMAbsenceType absenceType) {
    BigDecimal prevTotalLeaveDays = BigDecimal.ZERO;
    BigDecimal leaveDays = BigDecimal.ZERO;
    Query leaveBlockQuery = null;
    List totalLeaveBlockDaysList = null;
    Date leaveEndDate = null;
    Date leaveStartDate = null;
    String calEndDate = null;
    Date calEnddate = null;
    DateFormat yearFormat = Utility.YearFormat;
    DateFormat dateFormat = Utility.dateFormat;

    try {

      String totalLeaveBlockDaysCalSql = " select  "
          + "          levblkln.startdate,levblkln.enddate,levblkln.leavedays,levblkln.leavedays  "
          + "      from ehcm_emp_leaveblockln levblkln "
          + "      join ehcm_emp_leaveblock levblkhd on levblkhd.ehcm_emp_leaveblock_id= levblkln.ehcm_emp_leaveblock_id "
          + "      where levblkhd.ehcm_emp_leaveblock_id=:leaveBlockId "
          + "      and levblkln.startdate  < to_date(:startdate)  ";

      leaveBlockQuery = OBDal.getInstance().getSession().createSQLQuery(totalLeaveBlockDaysCalSql);
      leaveBlockQuery.setParameter("leaveBlockId", leaveBlockHdId);
      leaveBlockQuery.setParameter("startdate", Utility.formatDate(startDate));
      // totalLeaveBlockDaysList = leaveBlockQuery.list();

      totalLeaveBlockDaysList = getTotalLeaveDaysForPerivousDate(startDate, endDate, leaveBlockHdId,
          absenceType);

      if (totalLeaveBlockDaysList != null && totalLeaveBlockDaysList.size() > 0) {
        for (Object empTotalLeaveBlockDays : totalLeaveBlockDaysList) {
          Object[] empTotalLeaveBlockDaysObj = (Object[]) empTotalLeaveBlockDays;
          if (empTotalLeaveBlockDaysObj[0] != null) {
            leaveStartDate = (Date) empTotalLeaveBlockDaysObj[0];
          }
          if (empTotalLeaveBlockDaysObj[1] != null) {
            leaveEndDate = (Date) empTotalLeaveBlockDaysObj[1];
          }
          if (empTotalLeaveBlockDaysObj[2] != null) {
            leaveDays = (BigDecimal) empTotalLeaveBlockDaysObj[2];
          }

          if (leaveStartDate.compareTo(startDate) == 0) {
            break;
          } else if (endDate.compareTo(leaveEndDate) > 0) {
            prevTotalLeaveDays = prevTotalLeaveDays.add(leaveDays);
          } else {
            calEndDate = UtilityDAO.getBeforeDateInGregUsingGregDate(yearFormat.format(startDate));
            if (calEndDate != null) {
              calEnddate = dateFormat.parse(calEndDate);
            }
            leaveDays = BigDecimal.valueOf(AbsenceIssueDecisionDAOImpl
                .getEmpLevDateCountBasOnIsIncludeHolday(leaveStartDate, calEnddate, absenceType));
            prevTotalLeaveDays = prevTotalLeaveDays.add(leaveDays);

          }
        }

      }
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : getTotalLeaveDaysForPerivousDate() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getTotalLeaveDaysForPerivousDate ";
      return prevTotalLeaveDays;
    }
    return prevTotalLeaveDays;
  }

  @SuppressWarnings("rawtypes")
  public static List getTotalLeaveDaysForPerivousDate(Date startDate, Date endDate,
      String leaveBlockHdId, EHCMAbsenceType absenceType) {
    Query leaveBlockQuery = null;
    List totalLeaveBlockDaysList = null;

    try {
      String totalLeaveBlockDaysCalSql = " select  "
          + "          levblkln.startdate,levblkln.enddate,levblkln.leavedays,levblkln.leavedays  "
          + "      from ehcm_emp_leaveblockln levblkln "
          + "      join ehcm_emp_leaveblock levblkhd on levblkhd.ehcm_emp_leaveblock_id= levblkln.ehcm_emp_leaveblock_id "
          + "      where levblkhd.ehcm_emp_leaveblock_id=:leaveBlockId "
          + "      and levblkln.startdate  <= to_date(:enddate)  ";

      leaveBlockQuery = OBDal.getInstance().getSession().createSQLQuery(totalLeaveBlockDaysCalSql);
      leaveBlockQuery.setParameter("leaveBlockId", leaveBlockHdId);
      leaveBlockQuery.setParameter("enddate", Utility.formatDate(endDate));
      totalLeaveBlockDaysList = leaveBlockQuery.list();
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : getTotalLeaveDaysForPerivousDate() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getTotalLeaveDaysForPerivousDate ";
      return totalLeaveBlockDaysList;
    }
    return totalLeaveBlockDaysList;
  }

  @SuppressWarnings("rawtypes")
  public static BigDecimal getLeaveOccuranceBlockAbsPaymentValue(EmploymentInfo employment,
      Date payPeriodStartDate, Date payPeriodEndDate, BigDecimal oneDayPayScaleValue) {
    JSONObject absPaymentConfigJson = new JSONObject();
    List empLeaveBlockLnList = null;

    BigDecimal absPaymentValue = BigDecimal.ZERO;
    BigDecimal prevTotalLeaveDays = BigDecimal.ZERO;
    BigDecimal onedayPayScaleValue = BigDecimal.ZERO;
    BigDecimal tempLeaveDays = BigDecimal.ZERO;
    BigDecimal minDays = BigDecimal.ZERO;
    BigDecimal currentPayPeriodLeaveDays = BigDecimal.ZERO;
    BigDecimal totalAbsPaymentValue = BigDecimal.ZERO;
    BigDecimal calAbsPaymentValue = BigDecimal.ZERO;

    BigDecimal totalDaysAbsPaymentValue = BigDecimal.ZERO;
    BigDecimal originalLeaveDays = BigDecimal.ZERO;
    Date leaveStartDate = null;
    Date leaveEndDate = null;
    Date payPeriodStartdate = null;
    Date payPeriodEnddate = null;
    Date startDate = null;
    Date endDate = null;

    Query leaveBlockQuery = null;

    JSONObject result = new JSONObject();
    try {

      payPeriodStartdate = payPeriodStartDate;
      payPeriodEnddate = payPeriodEndDate;
      onedayPayScaleValue = oneDayPayScaleValue;

      // calculating block wise leave total Days
      String periodWiseLeaveBlockDays = " select  levblkhd.ehcm_emp_perinfo_id, levblkhd.ehcm_absence_type_id, levblkhd.subtype, "
          + " levblkln.startdate,levblkln.enddate,levblkln.leavedays, levblkhd.ehcm_emp_leaveblock_id "
          + " from ehcm_emp_leaveblockln levblkln "
          + " join ehcm_emp_leaveblock levblkhd on levblkhd.ehcm_emp_leaveblock_id= levblkln.ehcm_emp_leaveblock_id "
          + " where  levblkhd.ehcm_emp_perinfo_id=:employeeId  "
          + " and ((to_date(to_char(levblkln.startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startdate) "
          + " and to_date(to_char(coalesce (levblkln.enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') "
          + "      <= to_date(:enddate,'dd-MM-yyyy')) "
          + " or (to_date(to_char( coalesce (levblkln.enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy')  "
          + "    >= to_date(:startdate) "
          + " and to_date(to_char(levblkln.startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:enddate,'dd-MM-yyyy'))) "
          + "  and  levblkhd.ehcm_absence_type_id in (select abs.ehcm_absence_type_id from ehcm_absence_type abs where abs.accrual_reset_date='LO' ) "
          + "   and        levblkhd.ehcm_absence_type_id in ( select ehcm_absence_type_id from ehcm_absence_payment  where ad_client_id= :clientId)  "
          + " order by levblkhd.ehcm_absence_type_id , levblkhd.subtype   ";
      // need to add absence attendance id startdate condition
      leaveBlockQuery = OBDal.getInstance().getSession().createSQLQuery(periodWiseLeaveBlockDays);
      leaveBlockQuery.setParameter("employeeId", employment.getEhcmEmpPerinfo().getId());
      leaveBlockQuery.setParameter("startdate", Utility.formatDate(payPeriodStartdate));
      leaveBlockQuery.setParameter("enddate", Utility.formatDate(payPeriodEnddate));
      leaveBlockQuery.setParameter("clientId", employment.getClient().getId());

      empLeaveBlockLnList = leaveBlockQuery.list();
      if (empLeaveBlockLnList != null && empLeaveBlockLnList.size() > 0) {
        for (Object empLeaveBlockLn : empLeaveBlockLnList) {
          Object[] empLeaveBlockLnObj = (Object[]) empLeaveBlockLn;

          currentPayPeriodLeaveDays = (BigDecimal) empLeaveBlockLnObj[5];
          EHCMAbsenceType absenceType = OBDal.getInstance().get(EHCMAbsenceType.class,
              empLeaveBlockLnObj[1].toString());

          // dependent absenc etype
          if (absenceType.getEHCMAbsenceTypeActionList().size() > 0) {
            absenceType = absenceType.getEHCMAbsenceTypeActionList().get(0).getDependent();
          }

          if (empLeaveBlockLnObj[3] != null) {
            leaveStartDate = (Date) empLeaveBlockLnObj[3];
          }
          if (empLeaveBlockLnObj[4] != null) {
            leaveEndDate = (Date) empLeaveBlockLnObj[4];
          }

          // start date calculation
          if (payPeriodStartdate.compareTo(leaveStartDate) <= 0) {
            startDate = leaveStartDate;
          } else {
            startDate = payPeriodStartdate;
          }
          // end date calculation
          if (payPeriodEndDate.compareTo(leaveEndDate) <= 0) {
            endDate = payPeriodEndDate;
          } else {
            endDate = leaveEndDate;
          }
          // calculate the total leave days
          originalLeaveDays = BigDecimal.valueOf(AbsenceIssueDecisionDAOImpl
              .getEmpLevDateCountBasOnIsIncludeHolday(startDate, endDate, absenceType));

          // previous leave total days
          prevTotalLeaveDays = getTotalLeaveDaysForPerivousDate(employment, startDate, endDate,
              empLeaveBlockLnObj[6].toString(), payPeriodStartdate, payPeriodEnddate, absenceType);

          // absence configuration
          absPaymentConfigJson = getAbsencePaymentConfig(empLeaveBlockLnObj[1].toString(),
              (empLeaveBlockLnObj[2] == null ? null : empLeaveBlockLnObj[2].toString()),
              employment.getClient().getId(), null);
          for (;;) {
            if (originalLeaveDays.compareTo(BigDecimal.ZERO) == 0) {
              break;
            } else {
              // get absence payment config based on previous totaldays+current leave days
              result = findAbsPaymentValueBasedOnLeaveDaysCount(absPaymentConfigJson,
                  prevTotalLeaveDays.add(originalLeaveDays));

              if (result != null && result.has("absPaymentValue") && result.has("minDays")) {
                minDays = new BigDecimal(result.getString("minDays"));
                absPaymentValue = new BigDecimal(result.getString("absPaymentValue"));

                PayrollBaseProcess.absPaymentComponents.put("leaveTypeOtherThanMinAmount", true);

                if (((prevTotalLeaveDays.add(originalLeaveDays)).subtract(minDays))
                    .compareTo(originalLeaveDays) >= 0) {
                  if (absPaymentValue.compareTo(BigDecimal.ONE) != 0) {
                    totalDaysAbsPaymentValue = originalLeaveDays.multiply(onedayPayScaleValue);

                    calAbsPaymentValue = totalDaysAbsPaymentValue
                        .subtract(totalDaysAbsPaymentValue.multiply(absPaymentValue));
                    totalAbsPaymentValue = totalAbsPaymentValue.add(calAbsPaymentValue);
                  }
                  originalLeaveDays = BigDecimal.ZERO;
                } else {

                  PayrollBaseProcess.absPaymentComponents.put("leaveTypeOtherThanMinAmount", true);

                  tempLeaveDays = (prevTotalLeaveDays.add(originalLeaveDays)).subtract(minDays);
                  if (absPaymentValue.compareTo(BigDecimal.ONE) != 0) {

                    totalDaysAbsPaymentValue = tempLeaveDays.multiply(onedayPayScaleValue);
                    calAbsPaymentValue = totalDaysAbsPaymentValue
                        .subtract(totalDaysAbsPaymentValue.multiply(absPaymentValue));
                    totalAbsPaymentValue = totalAbsPaymentValue.add(calAbsPaymentValue);
                  }
                  originalLeaveDays = originalLeaveDays.subtract(tempLeaveDays);
                }
              }
            }
          }

        }
      }
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : getLeaveOccuranceBlockAbsPaymentValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getAbsencePaymentConfigValue ";
      return totalAbsPaymentValue;
    }
    return totalAbsPaymentValue;
  }

  public static BigDecimal calculateLeaveOccuranceBlockAbsPaymentValue(EmploymentInfo employment,
      BigDecimal oneDayPayScaleValue, JSONObject leaveJson, EHCMElmttypeDef payrollElement) {
    JSONObject leaveOccuranceListJSON = new JSONObject(), json = null,
        absPaymentConfigJson = new JSONObject(), result = new JSONObject();
    BigDecimal originalLeaveDays = BigDecimal.ZERO;
    BigDecimal prevTotalLeaveDays = BigDecimal.ZERO;
    BigDecimal minDays = BigDecimal.ZERO;
    BigDecimal absPaymentValue = BigDecimal.ZERO;
    BigDecimal calAbsPaymentValue = BigDecimal.ZERO;
    BigDecimal totalDaysAbsPaymentValue = BigDecimal.ZERO;
    BigDecimal onedayPayScaleValue = BigDecimal.ZERO;
    BigDecimal totalAbsPaymentValue = BigDecimal.ZERO;
    BigDecimal tempLeaveDays = BigDecimal.ZERO;

    try {

      leaveOccuranceListJSON = leaveJson;
      onedayPayScaleValue = oneDayPayScaleValue;
      if (leaveOccuranceListJSON != null && leaveOccuranceListJSON.length() > 0) {
        JSONArray jsonArray = leaveJson.getJSONArray("leaveListForLeaveOccurance");
        if (jsonArray.length() > 0) {
          for (int i = 0; i < jsonArray.length(); i++) {
            json = jsonArray.getJSONObject(i);
            // absence configuration
            absPaymentConfigJson = getAbsencePaymentConfig(json.getString("absenceTypeId"),
                (json.has("subTypeId") ? json.getString("subTypeId") : null),
                employment.getClient().getId(), payrollElement.getId());
            originalLeaveDays = new BigDecimal(json.getString("originalLeaveDays"));
            prevTotalLeaveDays = new BigDecimal(json.getString("prevTotalLeaveDays"));
            if (absPaymentConfigJson != null && absPaymentConfigJson.length() > 0) {
              for (;;) {
                if (originalLeaveDays.compareTo(BigDecimal.ZERO) == 0) {
                  break;
                } else {

                  if (absPaymentConfigJson.has("isBlock")) {
                    if (absPaymentConfigJson.getBoolean("isBlock")) {
                      // get absence payment config based on previous totaldays+current leave days
                      result = findAbsPaymentValueBasedOnLeaveDaysCount(absPaymentConfigJson,
                          prevTotalLeaveDays.add(originalLeaveDays));

                      if (result != null && result.has("absPaymentValue")
                          && result.has("minDays")) {
                        minDays = new BigDecimal(result.getString("minDays"));
                        absPaymentValue = new BigDecimal(result.getString("absPaymentValue"));

                        PayrollBaseProcess.absPaymentComponents.put("leaveTypeOtherThanMinAmount",
                            true);

                        if (((prevTotalLeaveDays.add(originalLeaveDays)).subtract(minDays))
                            .compareTo(originalLeaveDays) >= 0) {
                          if (absPaymentValue.compareTo(BigDecimal.ONE) != 0) {
                            totalDaysAbsPaymentValue = originalLeaveDays
                                .multiply(onedayPayScaleValue);

                            calAbsPaymentValue = totalDaysAbsPaymentValue
                                .subtract(totalDaysAbsPaymentValue.multiply(absPaymentValue));
                            totalAbsPaymentValue = totalAbsPaymentValue.add(calAbsPaymentValue);
                          }
                          originalLeaveDays = BigDecimal.ZERO;
                        } else {

                          PayrollBaseProcess.absPaymentComponents.put("leaveTypeOtherThanMinAmount",
                              true);

                          tempLeaveDays = (prevTotalLeaveDays.add(originalLeaveDays))
                              .subtract(minDays);
                          if (absPaymentValue.compareTo(BigDecimal.ONE) != 0) {

                            totalDaysAbsPaymentValue = tempLeaveDays.multiply(onedayPayScaleValue);
                            calAbsPaymentValue = totalDaysAbsPaymentValue
                                .subtract(totalDaysAbsPaymentValue.multiply(absPaymentValue));
                            totalAbsPaymentValue = totalAbsPaymentValue.add(calAbsPaymentValue);
                          }
                          originalLeaveDays = originalLeaveDays.subtract(tempLeaveDays);
                        }
                      }
                    } else {
                      if (absPaymentConfigJson != null && absPaymentConfigJson.length() > 0) {
                        JSONArray absencePaymentConfigJsonArray = absPaymentConfigJson
                            .getJSONArray("absencePayList");
                        if (absencePaymentConfigJsonArray.length() > 0) {
                          for (int j = 0; j < absencePaymentConfigJsonArray.length(); j++) {
                            JSONObject absPaymentObj = absencePaymentConfigJsonArray
                                .getJSONObject(j);
                            BigDecimal absencePaymentCalValue = new BigDecimal(
                                absPaymentObj.getString("absPaymentValue"));
                            if (absencePaymentCalValue.compareTo(BigDecimal.ONE) != 0) {
                              PayrollBaseProcess.absPaymentComponents
                                  .put("leaveTypeOtherThanMinAmount", true);
                              if (absencePaymentCalValue.compareTo(BigDecimal.ZERO) == 0) {
                                totalAbsPaymentValue = totalAbsPaymentValue
                                    .add(originalLeaveDays.multiply(onedayPayScaleValue));
                                originalLeaveDays = BigDecimal.ZERO;
                              } else {
                                totalDaysAbsPaymentValue = originalLeaveDays
                                    .multiply(onedayPayScaleValue);
                                calAbsPaymentValue = totalDaysAbsPaymentValue.subtract(
                                    totalDaysAbsPaymentValue.multiply(absencePaymentCalValue));
                                totalAbsPaymentValue = totalAbsPaymentValue.add(calAbsPaymentValue);
                                originalLeaveDays = BigDecimal.ZERO;
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : getLeaveOccuranceBlockAbsPaymentValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getAbsencePaymentConfigValue ";
      return totalAbsPaymentValue;
    }
    return totalAbsPaymentValue;
  }

  public static JSONObject getAbsencePaymentConfig(String absenceTypeId, String subTypeId,
      String clientId, String elementTypeId) {
    List<EHCMAbsencePayment> absPaymentList = null;
    String subTypehql = "";
    JSONObject resultAbsPayConfig = new JSONObject(), json = null;
    JSONArray jsonArray = new JSONArray();
    BigDecimal lastMaxDays = BigDecimal.ZERO;
    try {

      if (subTypeId != null) {
        subTypehql = " and (e.subType.id=:subTypeId  or e.subType is null )";
      }
      if (absenceTypeId != null) {
        OBQuery<EHCMAbsencePayment> absencePaymentQry = OBDal.getInstance().createQuery(
            EHCMAbsencePayment.class,
            " as e where e.absenceType.id=:absenceTypeId  and e.client.id=:clientId " + subTypehql
                + "  and e.payrollElement.id =:elementTypeId order by e.min asc");
        absencePaymentQry.setNamedParameter("absenceTypeId", absenceTypeId);
        if (subTypeId != null) {
          absencePaymentQry.setNamedParameter("subTypeId", subTypeId);
        }
        absencePaymentQry.setNamedParameter("clientId", clientId);
        absencePaymentQry.setNamedParameter("elementTypeId", elementTypeId);
        absPaymentList = absencePaymentQry.list();
        if (absPaymentList.size() > 0) {
          for (EHCMAbsencePayment absencePayment : absPaymentList) {

            if ((absencePayment.getMin().compareTo(BigDecimal.ZERO) == 0)
                || absencePayment.getMax().compareTo(BigDecimal.ZERO) == 0) {
              resultAbsPayConfig.put("isBlock", false);
            } else {
              resultAbsPayConfig.put("isBlock", true);
            }

            json = new JSONObject();
            json.put("absPaymentValue", absencePayment.getCalculatedValue());

            json.put("minAmount", absencePayment.getMINAmount());
            json.put("min", absencePayment.getMin());
            json.put("max", absencePayment.getMax());
            if (absencePayment.getPeriodType() != null
                && absencePayment.getPeriodType().equals("M")) {
              json.put("minDays", absencePayment.getMin().subtract(BigDecimal.ONE));
              lastMaxDays = absencePayment.getMax()
                  .multiply(BigDecimal.valueOf(Constants.NoOfDaysInMonths));
              json.put("maxDays", absencePayment.getMax());

            } else {
              json.put("minDays", absencePayment.getMin().subtract(BigDecimal.ONE));
              lastMaxDays = absencePayment.getMax();
              json.put("maxDays", absencePayment.getMax());
            }
            json.put("payRollElement",
                absencePayment.getPayrollElement().getEhcmElementCatgry().getType());
            jsonArray.put(json);
          }
          resultAbsPayConfig.put("absencePayList", jsonArray);
        }
      }

    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : getAbsencePaymentConfigValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getAbsencePaymentConfigValue ";
      return resultAbsPayConfig;
    }
    return resultAbsPayConfig;
  }

  //
  public static JSONObject findAbsPaymentValueBasedOnLeaveDaysCount(JSONObject absPaymentConfgJSON,
      BigDecimal totalLeaveDays) {
    JSONObject absPaymentConfigJson = null, json = null, result = new JSONObject();
    try {
      absPaymentConfigJson = absPaymentConfgJSON;

      if (absPaymentConfigJson != null && absPaymentConfigJson.length() > 0) {
        JSONArray jsonArray = absPaymentConfigJson.getJSONArray("absencePayList");
        if (jsonArray.length() > 0) {
          for (int i = 0; i < jsonArray.length(); i++) {
            json = jsonArray.getJSONObject(i);
            if ((new BigDecimal(json.getString("minDays")).compareTo(totalLeaveDays) <= 0)
                && (totalLeaveDays.compareTo(new BigDecimal(json.getString("maxDays"))) <= 0)) {
              result.put("maxDays", json.getString("maxDays"));
              result.put("minDays", json.getString("minDays"));
              result.put("absPaymentValue", json.getString("absPaymentValue"));
              break;
            }
          }
        }
      }

    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : getAbsencePaymentConfigValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getAbsencePaymentConfigValue ";
      return result;
    }
    return result;
  }

  @SuppressWarnings("rawtypes")
  public static JSONObject getJSONObjectForLeaveOccuranceBlockAbsPaymentValue(
      EmploymentInfo employment, Date payPeriodStartDate, Date payPeriodEndDate) {
    List empLeaveBlockLnList = null;

    BigDecimal prevTotalLeaveDays = BigDecimal.ZERO;
    BigDecimal currentPayPeriodLeaveDays = BigDecimal.ZERO;

    BigDecimal originalLeaveDays = BigDecimal.ZERO;
    Date leaveStartDate = null;
    Date leaveEndDate = null;
    Date payPeriodStartdate = null;
    Date payPeriodEnddate = null;
    Date startDate = null;
    Date endDate = null;

    Query leaveBlockQuery = null;

    JSONObject result = new JSONObject(), json = null;
    JSONArray jsonArray = new JSONArray();
    try {

      payPeriodStartdate = payPeriodStartDate;
      payPeriodEnddate = payPeriodEndDate;

      // calculating block wise leave total Days
      String periodWiseLeaveBlockDays = " select  levblkhd.ehcm_emp_perinfo_id, levblkhd.ehcm_absence_type_id, levblkhd.subtype, "
          + " levblkln.startdate,levblkln.enddate,levblkln.leavedays, levblkhd.ehcm_emp_leaveblock_id "
          + " from ehcm_emp_leaveblockln levblkln "
          + " join ehcm_emp_leaveblock levblkhd on levblkhd.ehcm_emp_leaveblock_id= levblkln.ehcm_emp_leaveblock_id "
          + " where  levblkhd.ehcm_emp_perinfo_id=:employeeId  "
          + " and ((to_date(to_char(levblkln.startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startdate) "
          + " and to_date(to_char(coalesce (levblkln.enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') "
          + "      <= to_date(:enddate,'dd-MM-yyyy')) "
          + " or (to_date(to_char( coalesce (levblkln.enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy')  "
          + "    >= to_date(:startdate) "
          + " and to_date(to_char(levblkln.startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:enddate,'dd-MM-yyyy'))) "
          + "  and  levblkhd.ehcm_absence_type_id in (select abs.ehcm_absence_type_id from ehcm_absence_type abs where abs.accrual_reset_date='LO' ) "
          + "   and        levblkhd.ehcm_absence_type_id in ( select ehcm_absence_type_id from ehcm_absence_payment  where ad_client_id= :clientId)  "
          + " order by levblkhd.ehcm_absence_type_id , levblkhd.subtype   ";
      // need to add absence attendance id startdate condition
      leaveBlockQuery = OBDal.getInstance().getSession().createSQLQuery(periodWiseLeaveBlockDays);
      leaveBlockQuery.setParameter("employeeId", employment.getEhcmEmpPerinfo().getId());
      leaveBlockQuery.setParameter("startdate", Utility.formatDate(payPeriodStartdate));
      leaveBlockQuery.setParameter("enddate", Utility.formatDate(payPeriodEnddate));
      leaveBlockQuery.setParameter("clientId", employment.getClient().getId());

      empLeaveBlockLnList = leaveBlockQuery.list();
      if (empLeaveBlockLnList != null && empLeaveBlockLnList.size() > 0) {
        for (Object empLeaveBlockLn : empLeaveBlockLnList) {
          Object[] empLeaveBlockLnObj = (Object[]) empLeaveBlockLn;

          currentPayPeriodLeaveDays = (BigDecimal) empLeaveBlockLnObj[5];
          EHCMAbsenceType absenceType = OBDal.getInstance().get(EHCMAbsenceType.class,
              empLeaveBlockLnObj[1].toString());

          // dependent absenc etype
          if (absenceType.getEHCMAbsenceTypeActionList().size() > 0) {
            absenceType = absenceType.getEHCMAbsenceTypeActionList().get(0).getDependent();
          }

          if (empLeaveBlockLnObj[3] != null) {
            leaveStartDate = (Date) empLeaveBlockLnObj[3];
          }
          if (empLeaveBlockLnObj[4] != null) {
            leaveEndDate = (Date) empLeaveBlockLnObj[4];
          }

          // start date calculation
          if (payPeriodStartdate.compareTo(leaveStartDate) <= 0) {
            startDate = leaveStartDate;
          } else {
            startDate = payPeriodStartdate;
          }
          // end date calculation
          if (payPeriodEndDate.compareTo(leaveEndDate) <= 0) {
            endDate = payPeriodEndDate;
          } else {
            endDate = leaveEndDate;
          }
          // calculate the total leave days
          originalLeaveDays = BigDecimal.valueOf(AbsenceIssueDecisionDAOImpl
              .getEmpLevDateCountBasOnIsIncludeHolday(startDate, endDate, absenceType));

          // previous leave total days
          prevTotalLeaveDays = getTotalLeaveDaysForPerivousDate(employment, startDate, endDate,
              empLeaveBlockLnObj[6].toString(), payPeriodStartdate, payPeriodEnddate, absenceType);

          json = new JSONObject();
          json.put("absenceTypeId", absenceType.getId());
          json.put("subTypeId",
              (empLeaveBlockLnObj[2] == null ? null : empLeaveBlockLnObj[2].toString()));
          json.put("prevTotalLeaveDays", prevTotalLeaveDays);
          json.put("currentPayPeriodLeaveDays", currentPayPeriodLeaveDays);
          json.put("originalLeaveDays", originalLeaveDays);
          jsonArray.put(json);
        }
        result.put("leaveListForLeaveOccurance", jsonArray);
      }
    } catch (Exception e) {
      log.error(
          "Error in PayrollBaseProcessDAO.java : getJSONObjectForLeaveOccuranceBlockAbsPaymentValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getAbsencePaymentConfigValue ";
      return result;
    }
    return result;
  }

  public static void calculateAbsenceDeductedValueForAllowance(EmploymentInfo employment,
      Date startDate, Date endDate, BigDecimal processingDays, BigDecimal differenceDays,
      Date periodEndDate, boolean isBaseCalculation, EHCMBenefitAllowance allowance,
      JSONObject allowanceComponents, EHCMElmttypeDef payrollElement, BigDecimal days) {
    BigDecimal oneDayValue = BigDecimal.ZERO;
    BigDecimal totalValue = BigDecimal.ZERO;
    BigDecimal percent = BigDecimal.ZERO;
    BigDecimal totalAbsPaymentValue = BigDecimal.ZERO;

    try {

      if (allowanceComponents != null && allowanceComponents.length() > 0) {
        if (allowanceComponents.has("isFixedAmount")) {
          if (allowanceComponents.getBoolean("isFixedAmount")) {
            oneDayValue = new BigDecimal(allowanceComponents.getString("perDayFixedAmt"));
            totalValue = new BigDecimal(allowanceComponents.getString("fixedAmount"));
          } else {
            if (allowanceComponents.has("isPercentBasic")) {
              if (allowanceComponents.has("percent")) {
                percent = new BigDecimal(allowanceComponents.getString("percent"));
              }
              if (allowanceComponents.getBoolean("isPercentBasic")) {
                totalValue = new BigDecimal(allowanceComponents.getString("payscale"));
                oneDayValue = (totalValue.divide(days, 6, BigDecimal.ROUND_HALF_UP))
                    .multiply(percent);
              } else {
                totalValue = new BigDecimal(allowanceComponents.getString("firstStepGradeVal"));
                oneDayValue = (totalValue.divide(days, 6, BigDecimal.ROUND_HALF_UP))
                    .multiply(percent);
              }
            }
          }
        }
      }

      // calculate absence payment value other than leave occurance(block wise)absence type
      JSONObject leaveJson = getEmpLeaveListBasedonPayPeriod(employment, startDate, endDate);

      totalAbsPaymentValue = totalAbsPaymentValue
          .add(calAbsencePaymentValueOtherThanLeaveOccuranceType(leaveJson, oneDayValue, totalValue,
              payrollElement));

      JSONObject leaveListForLeaveOccurance = getJSONObjectForLeaveOccuranceBlockAbsPaymentValue(
          employment, startDate, endDate);

      totalAbsPaymentValue = totalAbsPaymentValue.add(calculateLeaveOccuranceBlockAbsPaymentValue(
          employment, oneDayValue, leaveListForLeaveOccurance, payrollElement));

      /*
       * if (PayrollBaseProcess.absPaymentComponents.has("totalAbsPaymentValue")) {
       * PayrollBaseProcess.absPaymentComponents.put("totalAbsPaymentValue", new BigDecimal(
       * PayrollBaseProcess.absPaymentComponents.getString("totalAbsPaymentValue"))
       * .add(totalAbsPaymentValue)); } else {
       */
      PayrollBaseProcess.absPaymentComponents.put("totalAbsPaymentValue", totalAbsPaymentValue);
      // }

    } catch (Exception e) {
      log.error(
          "Error in PayrollBaseProcessDAO.java : getJSONObjectForLeaveOccuranceBlockAbsPaymentValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getAbsencePaymentConfigValue ";
    }
  }

  public static BigDecimal calculateAbsencePaymentValueForGradeRates(EHCMElmttypeDef payrollElement,
      ehcmgraderates gradeRate, EmploymentInfo employment, ehcmgrade grade, Date startDate,
      Date endDate, BigDecimal processingDays, BigDecimal differenceDays, Date payrollEndDate,
      boolean isBaseCalculation) {
    BigDecimal totalAbsPaymentValue = BigDecimal.ZERO;
    BigDecimal gradeRateValue = BigDecimal.ZERO;
    BigDecimal perDayGradeRate = BigDecimal.ZERO;
    BigDecimal days = BigDecimal.ZERO;
    try {

      // grade rate
      String dbFormattedStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String dbFormattedEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      String whereClause = "e where e.ehcmGraderates.id = :gradeRateId and e.grade.id=:gradeId "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')))";

      OBQuery<ehcmgraderatelines> gradeLineQry = OBDal.getInstance()
          .createQuery(ehcmgraderatelines.class, whereClause);
      gradeLineQry.setNamedParameter("gradeRateId", gradeRate.getId());
      gradeLineQry.setNamedParameter("gradeId", grade.getId());
      gradeLineQry.setNamedParameter("startDate", dbFormattedStartDate);
      gradeLineQry.setNamedParameter("endDate", dbFormattedEndDate);
      gradeLineQry.setFilterOnActive(false);

      List<ehcmgraderatelines> gradeLineList = gradeLineQry.list();
      for (ehcmgraderatelines gradeRateLne : gradeLineList) {
        log.info("GradeRate Days applicable");
        JSONObject gradeRatePeriodJSON = PayrollBaseProcess.getOverlapingDateRange(
            gradeRateLne.getStartDate(), gradeRateLne.getEndDate(), startDate, endDate);

        if (gradeRatePeriodJSON != null) {
          BigDecimal gradeRateDays = new BigDecimal(gradeRatePeriodJSON.getLong("days"));

          // If Grade Rate is for payroll end date, Check and add extra days in month
          Date gradeRateEndDate = PayrollConstants.dateFormat
              .parse(gradeRatePeriodJSON.getString("endDate"));
          if (differenceDays.compareTo(BigDecimal.ZERO) > 0
              && gradeRateEndDate.compareTo(payrollEndDate) == 0) {
            gradeRateDays = gradeRateDays.add(differenceDays);
          }

          gradeRateValue = gradeRateLne.getSearchKey();
          perDayGradeRate = BigDecimal.ZERO;
          // Validate Monthly or Day Rate
          if (gradeRateLne.getEhcmGraderates().getDuration().equalsIgnoreCase("PM")) {
            perDayGradeRate = gradeRateValue.divide(processingDays, 6, BigDecimal.ROUND_HALF_UP);
          } else {
            perDayGradeRate = gradeRateValue;
          }

          // calculate absence payment value other than leave occurance(block wise)absence type
          JSONObject leaveJson = getEmpLeaveListBasedonPayPeriod(employment,
              PayrollConstants.dateFormat.parse(gradeRatePeriodJSON.getString("startDate")),
              PayrollConstants.dateFormat.parse(gradeRatePeriodJSON.getString("endDate")));

          totalAbsPaymentValue = totalAbsPaymentValue
              .add(calAbsencePaymentValueOtherThanLeaveOccuranceType(leaveJson, perDayGradeRate,
                  gradeRateValue, payrollElement));

          JSONObject leaveListForLeaveOccurance = getJSONObjectForLeaveOccuranceBlockAbsPaymentValue(
              employment,
              PayrollConstants.dateFormat.parse(gradeRatePeriodJSON.getString("startDate")),
              PayrollConstants.dateFormat.parse(gradeRatePeriodJSON.getString("endDate")));

          totalAbsPaymentValue = totalAbsPaymentValue
              .add(calculateLeaveOccuranceBlockAbsPaymentValue(employment, perDayGradeRate,
                  leaveListForLeaveOccurance, payrollElement));
        }
      }

      /*
       * if (PayrollBaseProcess.absPaymentComponents.has("totalAbsPaymentValue")) {
       * PayrollBaseProcess.absPaymentComponents.put("totalAbsPaymentValue", new BigDecimal(
       * PayrollBaseProcess.absPaymentComponents.getString("totalAbsPaymentValue"))
       * .add(totalAbsPaymentValue)); } else {
       * PayrollBaseProcess.absPaymentComponents.put("totalAbsPaymentValue", totalAbsPaymentValue);
       * }
       */

      return totalAbsPaymentValue;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : calculateAbsencePaymentValue() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while calculateAbsencePaymentValueForGradeRates ";
      return totalAbsPaymentValue;
    }
  }

  /**
   * Get basic element from element type definition
   * 
   * @return
   */
  public static List<EHCMElmttypeDef> getBasicElement() {
    List<EHCMElmttypeDef> payrollBasicElement = null;
    try {
      OBQuery<EHCMElmttypeDef> payrollBasicElementList = OBDal.getInstance().createQuery(
          EHCMElmttypeDef.class,
          " baseProcess='E' and ehcmElementCatgry.id in(select e.id from EHCM_Element_Catgry e where e.type='02')");
      payrollBasicElement = payrollBasicElementList.list();
      return payrollBasicElement;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : getBasicElement() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getBasicElement ";
      return payrollBasicElement;
    }
  }

  /**
   * Get scholarship & training records for an employee.
   * 
   * @param empPerInfo
   * @param payrollPeriod
   * @param payrollProcessLne
   * @return
   */
  public static List<EHCMEmpScholarship> getScholarshipForEmployee(EhcmEmpPerInfo empPerInfo,
      EHCMPayrolldefPeriod payrollPeriod, EHCMPayrollProcessLne payrollProcessLne) {
    List<EHCMEmpScholarship> scholarshipList = new ArrayList<EHCMEmpScholarship>();
    try {
      String whereClause = " e where e.employee.id = :employeeId and e.payrollPeriod.id = :payrollPeriodId "
          + "and e.decisionType='SP' and e.issueDecision='Y' and (e.payrollProcessLine.id is null or e.payrollProcessLine.id =:payrollline ) order by e.startDate ";

      OBQuery<EHCMEmpScholarship> scholarshipQry = OBDal.getInstance()
          .createQuery(EHCMEmpScholarship.class, whereClause);
      scholarshipQry.setNamedParameter("employeeId", empPerInfo.getId());
      scholarshipQry.setNamedParameter("payrollPeriodId", payrollPeriod.getId());
      scholarshipQry.setNamedParameter("payrollline", payrollProcessLne.getId());

      scholarshipList = scholarshipQry.list();
      return scholarshipList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getScholarshipForEmployee() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting scholarship of Employee "
          + empPerInfo.getName();
      return scholarshipList;
    }
  }

  /**
   * Get Ticket Order records for an employee.
   * 
   * @param empPerInfo
   * @param payrollPeriod
   * @param payrollProcessLne
   * @return
   */
  @SuppressWarnings("unchecked")
  public static List<EHCMticketordertransaction> getTicketOrdersForEmployee(
      EhcmEmpPerInfo empPerInfo, EHCMPayrolldefPeriod payrollPeriod,
      EHCMPayrollProcessLne payrollProcessLne) {
    List<EHCMticketordertransaction> ticketList = null;
    try {

      String fromQuery = " select e from EHCM_ticketordertransaction as e left join e.businessMission bm left join e.ehcmEmpScholarship sh "
          + "where e.employee.id = :employeeId "
          + "and e.paymentPeriod.id = :payrollPeriodId and e.decisionType='TP' and e.issueDecision='Y' "
          + "and (e.payrollProcessLine.id is null or e.payrollProcessLine.id = :payrollline) "
          + "and ((bm.id is not null and bm.ticketsProvided='N') or (sh.id is not null and sh.isticketprovided ='N'))";

      Query hqlQuery = OBDal.getInstance().getSession().createQuery(fromQuery);
      hqlQuery.setParameter("employeeId", empPerInfo.getId());
      hqlQuery.setParameter("payrollPeriodId", payrollPeriod.getId());
      hqlQuery.setParameter("payrollline", payrollProcessLne.getId());
      ticketList = hqlQuery.list();
      return ticketList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getTicketOrdersForEmployee() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Ticket Orders of Employee "
          + empPerInfo.getName();
      return null;
    }
  }

  public static List<EHCMEmpScholarship> getScholarshipBusinessForEmployee(
      EhcmEmpPerInfo empPerInfo, String startDate, String endDate,
      BigDecimal scholarshipRewardDays) {
    List<EHCMEmpScholarship> scholarshipList = null;
    try {

      String whereClause = " e where e.employee.id = :employeeId "
          + "and e.decisionType='SP' and e.issueDecision='Y' " + "and e.noofdays >= "
          + scholarshipRewardDays
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) "
          + "order by e.startDate ";

      OBQuery<EHCMEmpScholarship> scholarshipQry = OBDal.getInstance()
          .createQuery(EHCMEmpScholarship.class, whereClause);
      scholarshipQry.setNamedParameter("employeeId", empPerInfo.getId());
      scholarshipQry.setNamedParameter("startDate", startDate);
      scholarshipQry.setNamedParameter("endDate", endDate);
      scholarshipList = scholarshipQry.list();
      return scholarshipList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getScholarshipBusinessForEmployee() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Scholarship Business of Employee "
          + empPerInfo.getName();
      return null;
    }
  }

  /**
   * Check whether the employee's salary is holded
   * 
   * @param emp
   * @param periodStartDate
   * @return
   */
  public static boolean isEmpSalaryHolded(EhcmEmpPerInfo emp, String periodStartDate) {
    try {
      String whereClause = " e join e.payrollPeriod sd "
          + "left join e.holdEndPeriod ed where e.requestType='HS' and e.ehcmEmpPerinfo.id=:empId and e.processed='Y' "
          + "and to_date(:periodStartDate,'dd-MM-yyyy') >= to_date(to_char(sd.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') "
          + "and (to_date(:periodStartDate,'dd-MM-yyyy') < to_date(to_char(coalesce(ed.startDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy'))";

      OBQuery<EHCMHoldUnHoldSalary> holdQry = OBDal.getInstance()
          .createQuery(EHCMHoldUnHoldSalary.class, whereClause);
      holdQry.setNamedParameter("empId", emp.getId());
      holdQry.setNamedParameter("periodStartDate", periodStartDate);
      holdQry.setFilterOnReadableOrganization(false);
      holdQry.setFilterOnReadableClients(false);
      holdQry.setFilterOnActive(false);
      if (holdQry.list().size() > 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcess.java : isEmpSalaryHolded() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while checking Employee Salary is holded for employee : "
          + emp.getName();
      return false;
    }
  }

  /**
   * Get No of Childrens
   * 
   * @param emp
   * @return
   */
  public static int getNoOfChildrens(EhcmEmpPerInfo emp) {
    try {
      // No need of date validation so bring latest formula
      String whereClause = "e where e.ehcmEmpPerinfo.id=:employeeId and e.relationship in ('S', 'D') ";
      OBQuery<EhcmDependents> dependentsQry = OBDal.getInstance().createQuery(EhcmDependents.class,
          whereClause);
      dependentsQry.setNamedParameter("employeeId", emp.getId());
      List<EhcmDependents> dependentList = dependentsQry.list();
      return dependentList.size();
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : getNoOfChildrens() ", e);
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting no of childrens for employee "
          + emp.getName();
      return 0;
    }
  }

  /**
   * Get Scholarship Deduction Configuration
   * 
   * @param absenceTypeId
   * @param payrollElement
   * @return
   */
  public static EHCMScholarshipDedConf getScholarshipDedConfig() {
    List<EHCMScholarshipDedConf> EHCMScholarshipDedConfList = null;
    EHCMScholarshipDedConf scholDedObj = null;
    try {
      OBQuery<EHCMScholarshipDedConf> scholarshipDedConfQry = OBDal.getInstance()
          .createQuery(EHCMScholarshipDedConf.class, " order by creationDate desc ");
      scholarshipDedConfQry.setMaxResult(1);
      EHCMScholarshipDedConfList = scholarshipDedConfQry.list();
      if (EHCMScholarshipDedConfList.size() > 0) {
        scholDedObj = EHCMScholarshipDedConfList.get(0);
      }
      return scholDedObj;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : getScholarshipDedConfig() ");
      e.printStackTrace();
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Scholarship Deduction Configuration ";
      return null;
    }
  }

  public static BigDecimal calculateScholarshipDeductionValue(EmploymentInfo employment,
      Date startDate, Date endDate, BigDecimal perDayValue, BigDecimal minDays,
      BigDecimal differenceDays, Date payrollEndDate) {
    BigDecimal totalScholarshipDeductionValue = BigDecimal.ZERO;
    try {
      // Pay Scale
      String dbFormattedStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String dbFormattedEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      // Get No of Scholarship days in the pay scale duration
      BigDecimal totalScholarshipDays = BigDecimal.ZERO;
      List<EHCMEmpScholarship> scholarshipList = getScholarshipBusinessForEmployee(
          employment.getEhcmEmpPerinfo(), dbFormattedStartDate, dbFormattedEndDate, minDays);

      if (!PayrollBaseProcess.errorFlagMinor) {
        for (EHCMEmpScholarship scholarship : scholarshipList) {
          JSONObject scholarshipJSON = PayrollBaseProcess.getOverlapingDateRange(
              scholarship.getStartDate(), scholarship.getEndDate(), startDate, endDate);
          if (scholarshipJSON != null) {

            BigDecimal scholarshipDays = new BigDecimal(scholarshipJSON.getLong("days"));

            // If Scholarship enddate is payroll end date, Check and add extra days in month
            Date scholarshipEndDate = PayrollConstants.dateFormat
                .parse(scholarshipJSON.getString("endDate"));
            if (differenceDays.compareTo(BigDecimal.ZERO) > 0
                && scholarshipEndDate.compareTo(payrollEndDate) == 0) {
              scholarshipDays = scholarshipDays.add(differenceDays);
            }

            log.info("Applicable Scholarship Days in Payscale ==> " + scholarshipDays);
            totalScholarshipDays = totalScholarshipDays.add(scholarshipDays);
          }
        }

        log.info("Total Scholarship Days in Payscale ==> " + totalScholarshipDays);
        if (totalScholarshipDays.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal calculatedScholarshipValue = perDayValue.multiply(totalScholarshipDays)
              .setScale(6, BigDecimal.ROUND_HALF_UP);
          totalScholarshipDeductionValue = totalScholarshipDeductionValue
              .add(calculatedScholarshipValue);
        }
      }
      return totalScholarshipDeductionValue;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : calculateScholarshipDeductionValue() ", e);
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while calculating Scholarship Deduction Value ";
      return totalScholarshipDeductionValue;
    }
  }

  public static BigDecimal calculateScholarshipDeductionValues(EmploymentInfo employment,
      Date startDate, Date endDate, BigDecimal processingDays, BigDecimal differenceDays,
      Date payrollEndDate, EHCMElmttypeDef payrollElement, BigDecimal minDays) {
    BigDecimal totalScholarshipDeductionValue = BigDecimal.ZERO;
    BigDecimal payscale = BigDecimal.ZERO;
    BigDecimal oneDayPayScale = BigDecimal.ZERO;
    BigDecimal days = BigDecimal.ZERO;
    try {
      // Pay Scale
      String empmtPayStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String empmtPayEndDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      String whereClause = "e where e.ehcmPayscale.ehcmGrade.id=:gradeId "
          + "and e.ehcmProgressionpt.id = :pointsId "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) "
          + "order by e.startDate ";

      OBQuery<ehcmpayscaleline> payScaleQry = OBDal.getInstance()
          .createQuery(ehcmpayscaleline.class, whereClause);
      payScaleQry.setNamedParameter("gradeId", employment.getEmploymentgrade().getId());
      payScaleQry.setNamedParameter("pointsId",
          employment.getEhcmPayscaleline().getEhcmProgressionpt().getId());
      payScaleQry.setNamedParameter("startDate", empmtPayStartDate);
      payScaleQry.setNamedParameter("endDate", empmtPayEndDate);
      payScaleQry.setFilterOnActive(false);
      List<ehcmpayscaleline> payScaleLneList = payScaleQry.list();
      for (ehcmpayscaleline payScaleLne : payScaleLneList) {
        if (!PayrollBaseProcess.errorFlagMinor) {
          log.info("PayScale Days applicable");
          JSONObject payScalePeriodJSON = PayrollBaseProcess.getOverlapingDateRange(
              payScaleLne.getStartDate(), payScaleLne.getEndDate(), startDate, endDate);

          if (payScalePeriodJSON != null) {
            Date applicablePayScaleStartDate = PayrollConstants.dateFormat
                .parse(payScalePeriodJSON.getString("startDate"));
            Date applicablePayScaleEndDate = PayrollConstants.dateFormat
                .parse(payScalePeriodJSON.getString("endDate"));
            days = new BigDecimal(payScalePeriodJSON.getLong("days"));

            String dbApplicablePayScaleStartDate = sa.elm.ob.utility.util.Utility
                .formatDate(applicablePayScaleStartDate);
            String dbApplicablePayScaleEndDate = sa.elm.ob.utility.util.Utility
                .formatDate(applicablePayScaleEndDate);

            // If pay scale is for payroll end date, Check and add extra days in month
            Date payScaleEndDate = PayrollConstants.dateFormat
                .parse(payScalePeriodJSON.getString("endDate"));
            if (differenceDays.compareTo(BigDecimal.ZERO) > 0
                && payScaleEndDate.compareTo(payrollEndDate) == 0) {
              days = days.add(differenceDays);
            }

            BigDecimal payScaleAmount = payScaleLne.getAmount();
            oneDayPayScale = payScaleAmount.divide(processingDays, 6, BigDecimal.ROUND_HALF_UP);
            payscale = oneDayPayScale.multiply(days).setScale(6, BigDecimal.ROUND_HALF_UP);

            // Get No of Scholarship days in the pay scale duration
            BigDecimal totalScholarshipDays = BigDecimal.ZERO;
            List<EHCMEmpScholarship> scholarshipList = getScholarshipBusinessForEmployee(
                employment.getEhcmEmpPerinfo(), dbApplicablePayScaleStartDate,
                dbApplicablePayScaleEndDate, minDays);

            if (!PayrollBaseProcess.errorFlagMinor) {
              for (EHCMEmpScholarship scholarship : scholarshipList) {
                JSONObject scholarshipJSON = PayrollBaseProcess.getOverlapingDateRange(
                    scholarship.getStartDate(), scholarship.getEndDate(),
                    applicablePayScaleStartDate, applicablePayScaleEndDate);
                if (scholarshipJSON != null) {
                  BigDecimal scholarshipDays = new BigDecimal(scholarshipJSON.getLong("days"));
                  log.info("Applicable Scholarship Days in Payscale ==> " + scholarshipDays);
                  totalScholarshipDays = totalScholarshipDays.add(scholarshipDays);
                }
              }

              log.info("Total Scholarship Days in Payscale ==> " + totalScholarshipDays);
              if (totalScholarshipDays.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal calculatedScholarshipValue = oneDayPayScale
                    .multiply(totalScholarshipDays).setScale(6, BigDecimal.ROUND_HALF_UP);
                totalScholarshipDeductionValue = totalScholarshipDeductionValue
                    .add(calculatedScholarshipValue);
              }
            }

          }
        } else {
          break;
        }
      }

      return totalScholarshipDeductionValue;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : calculateScholarshipDeductionValue() ", e);
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while calculating Scholarship Deduction Value ";
      return totalScholarshipDeductionValue;
    }
  }

  public static BigDecimal calculateSuspensionValue(EmploymentInfo employment, Date startDate,
      Date endDate, BigDecimal perDayValue, BigDecimal differenceDays, Date payrollEndDate) {
    BigDecimal totalSuspensionValue = BigDecimal.ZERO;
    try {
      String dbFormattedStartDate = sa.elm.ob.utility.util.Utility.formatDate(startDate);
      String dbFormattedEndtDate = sa.elm.ob.utility.util.Utility.formatDate(endDate);

      // Get No of Scholarship days in the pay scale duration
      BigDecimal totalSuspensionDays = BigDecimal.ZERO;
      List<EmployeeSuspension> suspensionList = getSuspensionForEmployee(
          employment.getEhcmEmpPerinfo(), dbFormattedStartDate, dbFormattedEndtDate);

      if (!PayrollBaseProcess.errorFlagMinor) {
        for (EmployeeSuspension suspensionObj : suspensionList) {
          JSONObject suspensionJSON = PayrollBaseProcess.getOverlapingDateRange(
              suspensionObj.getStartDate(), suspensionObj.getExpectedEndDate(), startDate, endDate);
          if (suspensionJSON != null) {
            BigDecimal suspensionDays = new BigDecimal(suspensionJSON.getLong("days"));

            // If suspension enddate is payroll end date, Check and add extra days in month
            Date suspensionEndDate = PayrollConstants.dateFormat
                .parse(suspensionJSON.getString("endDate"));
            if (differenceDays.compareTo(BigDecimal.ZERO) > 0
                && suspensionEndDate.compareTo(payrollEndDate) == 0) {
              suspensionDays = suspensionDays.add(differenceDays);
            }
            log.info("Applicable Suspension Days in Payscale ==> " + suspensionDays);

            totalSuspensionDays = totalSuspensionDays.add(suspensionDays);
          }
        }
        log.info("Total Suspension Days in Payscale ==> " + totalSuspensionDays);
        if (totalSuspensionDays.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal calculatedSuspensionValue = perDayValue.multiply(totalSuspensionDays)
              .setScale(6, BigDecimal.ROUND_HALF_UP);
          totalSuspensionValue = totalSuspensionValue.add(calculatedSuspensionValue);
        }
      }
      return totalSuspensionValue;
    } catch (Exception e) {
      log.error("Error in PayrollBaseProcessDAO.java : calculateSuspensionValue() ", e);
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while calculating Suspension Value ";
      return totalSuspensionValue;
    }
  }

  public static List<EmployeeSuspension> getSuspensionForEmployee(EhcmEmpPerInfo empPerInfo,
      String startDate, String endDate) {
    List<EmployeeSuspension> suspensionList = new ArrayList<EmployeeSuspension>();
    try {
      String whereClause = " e where e.ehcmEmpPerinfo.id= :employeeId and e.decisionType<>'CA' and e.suspensionType = 'SUS' "
          + "and e.issueDecision='Y' and e.id not in(select a.originalDecisionNo from Ehcm_Emp_Suspension a where a.originalDecisionNo is not null) "
          + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(coalesce(e.expectedEndDate, to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
          + "or (to_date(to_char( coalesce (e.expectedEndDate, to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
          + "and to_date(to_char(e.startDate,'dd-MM-yyyy'), 'dd-MM-yyyy') <= to_date(:endDate, 'dd-MM-yyyy'))) "
          + "order by e.startDate ";

      OBQuery<EmployeeSuspension> suspensionQry = OBDal.getInstance()
          .createQuery(EmployeeSuspension.class, whereClause);
      suspensionQry.setNamedParameter("employeeId", empPerInfo.getId());
      suspensionQry.setNamedParameter("startDate", startDate);
      suspensionQry.setNamedParameter("endDate", endDate);
      suspensionList = suspensionQry.list();
      return suspensionList;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getSuspensionForEmployee() ", e);
      PayrollBaseProcess.errorFlagMinor = true;
      PayrollBaseProcess.errorMessage = "Error while getting Suspension of Employee "
          + empPerInfo.getName();
      return suspensionList;
    }
  }

  /**
   * Get secondment period along with payroll period
   * 
   * @param elementGrp
   * @param element
   * @param employee
   * @param periodStartDate
   * @param periodEndDate
   * @return
   */
  public static JSONArray getSecondmentDetails(EhcmElementGroup elementGrp, EHCMElmttypeDef element,
      EhcmEmpPerInfo employee, Date periodStartDate, Date periodEndDate) {
    JSONArray jsonArray = new JSONArray();
    JSONObject secJson = null;
    Calendar c = Calendar.getInstance();
    boolean needAct = false;
    try {

      String empmtPayStartDate = sa.elm.ob.utility.util.Utility.formatDate(periodStartDate);
      String empmtPayEndDate = sa.elm.ob.utility.util.Utility.formatDate(periodEndDate);

      OBQuery<EHCMEmpSecondment> secondmentList = OBDal.getInstance().createQuery(
          EHCMEmpSecondment.class,
          "e where e.ehcmEmpPerinfo.id=:empID and e.decisionStatus=:status and  e.decisionType<>'CA'"
              + "and e.id not in(select a.originalDecisionsNo.id from EHCM_Emp_Secondment a where a.originalDecisionsNo is not null and a.decisionStatus='I' and a.ehcmEmpPerinfo.id=:empID) "
              + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
              + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
              + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate) "
              + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) "
              + "order by e.startDate ");
      secondmentList.setNamedParameter("empID", employee.getId());
      secondmentList.setNamedParameter("status", "I");
      secondmentList.setNamedParameter("startDate", empmtPayStartDate);
      secondmentList.setNamedParameter("endDate", empmtPayEndDate);
      List<EHCMEmpSecondment> a = secondmentList.list();
      for (EHCMEmpSecondment secondment : a) {
        needAct = false;
        Date tempEndDate = null;
        // elementGrp.getEhcmElementGroupLineList().contains(element);

        // check element is present in secondement element group.
        for (EhcmElementGroupLine line : secondment.getPaymentType()
            .getEhcmElementGroupLineList()) {
          if (line.getEhcmElmttypeDef().equals(element)) {
            break;
          }
        }

        if (periodStartDate.compareTo(secondment.getStartDate()) < 0) {
          secJson = new JSONObject();
          // if sec > perstart
          secJson.put("STARTDATE",
              sa.elm.ob.utility.util.Utility.formatDate(periodStartDate, "yyyy-MM-dd"));

          c.setTime(secondment.getStartDate());
          c.add(Calendar.DAY_OF_MONTH, -1);
          Date endDate = c.getTime();
          secJson.put("ENDDATE", sa.elm.ob.utility.util.Utility.formatDate(endDate, "yyyy-MM-dd"));
          secJson.put("ELMGRP", elementGrp.getId());
          needAct = true;
          jsonArray.put(secJson);
        }

        if (periodStartDate.compareTo(secondment.getStartDate()) >= 0) {
          // if per > sec
          secJson = new JSONObject();

          secJson.put("STARTDATE",
              sa.elm.ob.utility.util.Utility.formatDate(periodStartDate, "yyyy-MM-dd"));
          if (periodEndDate.compareTo(secondment.getEndDate()) >= 0) {
            secJson.put("ENDDATE", secondment.getEndDate());
            tempEndDate = secondment.getEndDate();
          } else {
            secJson.put("ENDDATE",
                sa.elm.ob.utility.util.Utility.formatDate(periodEndDate, "yyyy-MM-dd"));
            tempEndDate = periodEndDate;
          }
          secJson.put("ELMGRP", secondment.getPaymentType().getId());
          needAct = false;
          jsonArray.put(secJson);
        }

        if (needAct) {
          // set actual sec
          secJson = new JSONObject();

          secJson.put("STARTDATE", secondment.getStartDate().toString());
          if (periodEndDate.compareTo(secondment.getEndDate()) >= 0) {
            secJson.put("ENDDATE", secondment.getEndDate().toString());
            tempEndDate = secondment.getEndDate();
          } else {
            secJson.put("ENDDATE",
                sa.elm.ob.utility.util.Utility.formatDate(periodEndDate, "yyyy-MM-dd"));
            tempEndDate = periodEndDate;
          }
          secJson.put("ELMGRP", secondment.getPaymentType().getId());
          jsonArray.put(secJson);
        }
        c.setTime(tempEndDate);
        c.add(Calendar.DAY_OF_MONTH, 1);
        Date nextDate = c.getTime();
        periodStartDate = nextDate;

      }
      if (periodStartDate.compareTo(periodEndDate) <= 0) {
        secJson = new JSONObject();
        secJson.put("STARTDATE",
            sa.elm.ob.utility.util.Utility.formatDate(periodStartDate, "yyyy-MM-dd"));
        secJson.put("ENDDATE",
            sa.elm.ob.utility.util.Utility.formatDate(periodEndDate, "yyyy-MM-dd"));
        secJson.put("ELMGRP", elementGrp.getId());
        secJson.put("ELIGIBLE", true);
        jsonArray.put(secJson);
      }
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : getSecondmentDetails ", e);
      e.printStackTrace();
    }
    return jsonArray;
  }

  /**
   * check element is present in secondement element group.
   * 
   * @param elementGrp
   * @param element
   * @return
   */
  public static boolean isElementInElementGrp(String elementGrp, EHCMElmttypeDef element) {
    boolean isContain = false;
    try {
      EhcmElementGroup elementGroup = OBDal.getInstance().get(EhcmElementGroup.class, elementGrp);

      for (EhcmElementGroupLine line : elementGroup.getEhcmElementGroupLineList()) {
        if (line.getEhcmElmttypeDef().equals(element)) {
          isContain = true;
          break;
        }
      }
      return isContain;
    } catch (Exception e) {
      log.error("Error in PayrollProcess.java : isElementInElementGrp ", e);
      e.printStackTrace();
    }
    return isContain;
  }

}
