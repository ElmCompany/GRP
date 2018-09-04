package sa.elm.ob.hcm.ad_process.DecisionBalance;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.DecisionBalance;
import sa.elm.ob.hcm.DecisionBalanceHeader;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMEMPLeaveBlockLn;
import sa.elm.ob.hcm.EHCMEmpLeave;
import sa.elm.ob.hcm.EHCMEmpLeaveBlock;
import sa.elm.ob.hcm.EHCMEmpLeaveln;
import sa.elm.ob.hcm.EHCMMiscatEmployee;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmPayrollReportConfig;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.process.dao.LeaveCalculationDAO;
import sa.elm.ob.hcm.process.dao.LeaveCalculationDAOImpl;
import sa.elm.ob.utility.util.UtilityDAO;

public class DecisionBalanceDAOImpl implements DecisionBalanceDAO {

  private static final Logger log4j = LoggerFactory.getLogger(DecisionBalanceDAOImpl.class);
  static DateFormat yearFormat = sa.elm.ob.utility.util.Utility.YearFormat;
  static DateFormat dateFormat = sa.elm.ob.utility.util.Utility.dateFormat;
  LeaveCalculationDAO leaveCalculationDAO = new LeaveCalculationDAOImpl();

  @Override
  public void chkEmpLeavePresentOrNotAndinsertEmpLeave(DecisionBalanceHeader decisionBalanceHeader,
      EHCMAbsenceType absencetype) throws Exception {
    // TODO Auto-generated method stub
    List<EHCMEmpLeave> empLeaveList = null;
    List<DecisionBalance> decBalanceList = null;
    EHCMAbsenceType absenceType = null;
    JSONObject result = null;
    Date startDate = null;
    try {
      absenceType = absencetype;
      if (decisionBalanceHeader != null) {
        decBalanceList = decisionBalanceHeader.getEhcmDecisionBalanceList();
        for (DecisionBalance decisionBalanceEmp : decBalanceList) {
          if (decisionBalanceEmp.getBalance().compareTo(BigDecimal.ZERO) != 0) {

            // all paid leaves
            if (absenceType == null) {
              absenceType = decisionBalanceEmp.getAbsenceType();
            }

            if (absenceType.getAccrualResetDate().equals(Constants.ACCRUALRESETDATE_LEAVEOCCUR)) {
              startDate = decisionBalanceEmp.getBlockStartdate();
            } else {
              startDate = decisionBalanceEmp.getEmployee().getHiredate();
            }

            OBQuery<EHCMEmpLeave> empLeaveQry = OBDal.getInstance().createQuery(EHCMEmpLeave.class,
                " as e where e.ehcmEmpPerinfo.id =:employeeId  and e.absenceType.id=:absenceTypeId "
                    + " and :hiredate between e.startDate and e.endDate");

            empLeaveQry.setNamedParameter("employeeId", decisionBalanceEmp.getEmployee().getId());
            empLeaveQry.setNamedParameter("absenceTypeId", absenceType.getId());
            empLeaveQry.setNamedParameter("hiredate", startDate);
            empLeaveList = empLeaveQry.list();
            if (empLeaveList.size() > 0) {
              result = leaveCalculationDAO.getStartDateAndEndDate(
                  empLeaveList.get(0).getStartDate(), absenceType,
                  decisionBalanceEmp.getEmployee().getId());
              InsertEmpLeaveLn(absenceType, decisionBalanceEmp, empLeaveList.get(0), result);
            } else {
              result = leaveCalculationDAO.getStartDateAndEndDate(startDate, absenceType,
                  decisionBalanceEmp.getEmployee().getId());
              InsertEmpLeave(absenceType, decisionBalanceEmp, result);
            }
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      log4j.error(
          "Exception in chkEmpLeavePresentOrNotAndinsertEmpLeave in DecisionBalanceDAOImpl: ", e);
    }
  }

  @Override
  public String chkAlreadyOpeningBalanceAddedForTatEmp(DecisionBalanceHeader decisionBalanceHeader,
      EHCMAbsenceType absenceType) throws Exception {
    // TODO Auto-generated method stub
    List<EHCMEmpLeaveln> empLeavelnList = null;
    List<DecisionBalance> decBalanceList = null;
    String hql = "";
    String message = "";
    Date hireYearEndDate = null;
    try {

      hql = "and to_date(:hireDate,'yyyy-MM-dd') between to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') and "
          + " to_date(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy') ";

      if (decisionBalanceHeader != null) {
        decBalanceList = decisionBalanceHeader.getEhcmDecisionBalanceList();
        for (DecisionBalance decisionBalanceEmp : decBalanceList) {
          if (decisionBalanceEmp.getBalance().compareTo(BigDecimal.ZERO) != 0) {

            hireYearEndDate = getHireYearLastDate(decisionBalanceEmp.getEmployee().getHiredate());

            OBQuery<EHCMEmpLeaveln> levln = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class,
                " as e " + " join e.ehcmEmpLeave lev join lev.ehcmEmpPerinfo as emp"
                    + "  where lev.id in ( select lev.id from EHCM_Emp_Leave lev  where lev.absenceType.id=:absenceTypeId"
                    + " and  lev.ehcmEmpPerinfo.id =:empId  )" + "   and e.leaveAction='OB' "
                    + hql);

            levln.setNamedParameter("absenceTypeId", absenceType.getId());
            levln.setNamedParameter("empId", decisionBalanceEmp.getEmployee().getId());
            levln.setNamedParameter("hireDate", hireYearEndDate);
            empLeavelnList = levln.list();
            if (empLeavelnList.size() > 0) {
              for (EHCMEmpLeaveln lev : empLeavelnList) {
                if (StringUtils.isNotEmpty(message)) {
                  message = message + ","
                      + lev.getEhcmEmpLeave().getEhcmEmpPerinfo().getSearchKey();
                } else {
                  message = lev.getEhcmEmpLeave().getEhcmEmpPerinfo().getSearchKey();
                }

              }
            }
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      log4j.error(
          "Exception in chkEmpLeavePresentOrNotAndinsertEmpLeave in DecisionBalanceDAOImpl: ", e);
    }
    return message;
  }

  @Override
  public EHCMAbsenceType getAnnualLeaveBalanceFromPayrollReportConfig(String clientId)
      throws Exception {
    // TODO Auto-generated method stub
    EHCMAbsenceType absenceType = null;
    List<EhcmPayrollReportConfig> payrollReportList = null;
    try {
      OBQuery<EhcmPayrollReportConfig> payrollReportConfig = OBDal.getInstance()
          .createQuery(EhcmPayrollReportConfig.class, " as e " + " where e.client.id=:clientId ");
      payrollReportConfig.setNamedParameter("clientId", clientId);
      payrollReportConfig.setMaxResult(1);
      payrollReportList = payrollReportConfig.list();
      if (payrollReportList.size() > 0) {
        EhcmPayrollReportConfig payrollReport = payrollReportList.get(0);
        if (payrollReport.getEhcmAbsenceType() != null) {
          absenceType = payrollReport.getEhcmAbsenceType();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in getAnnualLeaveBalance in DecisionBalanceDAOImpl: ", e);
    }
    return absenceType;
  }

  public EHCMEmpLeave InsertEmpLeave(EHCMAbsenceType absenceType,
      DecisionBalance decisionBalanceEmp, JSONObject result) throws Exception {
    // TODO Auto-generated method stub
    EHCMEmpLeave leave = null;
    try {

      EhcmEmpPerInfo employee = decisionBalanceEmp.getEmployee();
      leave = OBProvider.getInstance().get(EHCMEmpLeave.class);
      leave.setClient(decisionBalanceEmp.getClient());
      leave.setOrganization(decisionBalanceEmp.getOrganization());
      leave.setCreationDate(new java.util.Date());
      leave.setCreatedBy(OBContext.getOBContext().getUser());
      leave.setUpdated(new java.util.Date());
      leave.setUpdatedBy(OBContext.getOBContext().getUser());
      leave.setEhcmEmpPerinfo(employee);
      leave.setAbsenceType(absenceType);
      leave.setGradeClassifications(employee.getGradeClass());
      leave.setEnabled(true);
      leave.setOfLeaves(new BigDecimal(0));
      leave.setPooleddays(new BigDecimal(0));
      leave.setAvailabledays(new BigDecimal(0));
      leave.setAvaileddays(new BigDecimal(0));
      leave.setCreditOn("D");

      if (decisionBalanceEmp.getSubType() != null) {
        leave.setSubtype(decisionBalanceEmp.getSubType());
      }

      if (result != null) {
        leave.setStartDate(yearFormat.parse(result.getString("startdate")));
        leave.setEndDate(yearFormat.parse(result.getString("enddate")));
      }

      /*
       * leave.setStartDate(employee.getHiredate()); leave.setEndDate(employee.getHiredate());
       */
      OBDal.getInstance().save(leave);

      InsertEmpLeaveLn(absenceType, decisionBalanceEmp, leave, result);

    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in InsertEmpLeave in DecisionBalanceDAOImpl: ", e);
    }
    return leave;
  }

  public EHCMEmpLeaveln InsertEmpLeaveLn(EHCMAbsenceType absenceType,
      DecisionBalance decisionBalanceEmp, EHCMEmpLeave empleave, JSONObject result)
      throws Exception {
    // TODO Auto-generated method stub
    EHCMEmpLeave leave = null;
    EHCMEmpLeaveln leaveln = null;
    String hql = "";
    List<EHCMEmpLeaveBlock> levBlockList = null;
    try {
      leave = empleave;
      leaveln = OBProvider.getInstance().get(EHCMEmpLeaveln.class);
      leaveln.setClient(leave.getClient());
      leaveln.setOrganization(leave.getOrganization());
      leaveln.setEnabled(true);
      leaveln.setCreationDate(new java.util.Date());
      leaveln.setCreatedBy(OBContext.getOBContext().getUser());
      leaveln.setUpdated(new java.util.Date());
      leaveln.setUpdatedBy(OBContext.getOBContext().getUser());
      leaveln.setEhcmEmpLeave(leave);
      leaveln.setLeavedays(decisionBalanceEmp.getBalance());

      if (result != null) {
        if (decisionBalanceEmp.getBlockStartdate() != null) {
          leaveln.setStartDate(decisionBalanceEmp.getBlockStartdate());
        } else {
          leaveln.setStartDate(yearFormat.parse(result.getString("startdate")));
        }
        leaveln.setEndDate(yearFormat.parse(result.getString("enddate")));
      }

      /*
       * leaveln.setStartDate(decisionBalanceEmp.getEmployee().getHiredate());
       * leaveln.setEndDate(decisionBalanceEmp.getEmployee().getHiredate());
       */
      if (decisionBalanceEmp.getDecisionType().equals("APLB")) {
        leaveln.setLeaveAction(DecisionTypeConstants.DECISION_TYPE_CREATE);
        leaveln.setLeaveType(Constants.EMPLEAVE_ABSENCE);
      } else {
        leaveln.setLeaveAction(Constants.EMPLEAVE_OPENINGBALANCE);
        leaveln.setLeaveType(Constants.EMPLEAVE_ACCRUAL);
      }
      leaveln.setEhcmAbsenceAttendance(null);
      leaveln.setEhcmDecisionBalance(decisionBalanceEmp);
      OBDal.getInstance().save(leaveln);
      log4j.debug("leaveln:" + leaveln.getId());

      // check absence type accrual reset is leave Occruance
      if (absenceType.getAccrualResetDate().equals(Constants.ACCRUALRESETDATE_LEAVEOCCUR)) {

        // check already leave block exists or not
        if (absenceType.isSubtype() && decisionBalanceEmp.getSubType() != null) {
          hql = " and e.subtype.id=:subtypeId ";
        }

        OBQuery<EHCMEmpLeaveBlock> levBlockQry = OBDal.getInstance().createQuery(
            EHCMEmpLeaveBlock.class,
            " as e where e.ehcmEmpPerinfo.id =:employeeId  and e.absenceType.id=:absenceTypeId "
                + " and :blockstartdate between e.startDate and e.endDate " + hql);
        levBlockQry.setNamedParameter("employeeId", decisionBalanceEmp.getEmployee().getId());
        levBlockQry.setNamedParameter("absenceTypeId", absenceType.getId());
        levBlockQry.setNamedParameter("blockstartdate", decisionBalanceEmp.getBlockStartdate());
        if (absenceType.isSubtype() && decisionBalanceEmp.getSubType() != null) {
          levBlockQry.setNamedParameter("subtypeId", decisionBalanceEmp.getSubType().getId());
        }
        levBlockList = levBlockQry.list();
        if (levBlockList.size() > 0) {
          /*
           * // calculate the 5 years enddate // form start date to 5 years date as enddate
           * hijiblockStartDate = UtilityDAO
           * .convertTohijriDate(yearFormat.format(decisionBalanceEmp.getBlockStartdate()));
           * hijiFiveYrEndDate = hijiblockStartDate.split("-")[0] + "-" +
           * hijiblockStartDate.split("-")[1] + "-" +
           * ((Integer.valueOf(hijiblockStartDate.split("-")[2]) + 5)); GregFiveYrEndDate =
           * UtilityDAO.convertToGregorian(hijiFiveYrEndDate); endDate =
           * getMaxEndDate(decisionBalanceEmp.getBlockStartdate(),
           * yearFormat.parse(GregFiveYrEndDate));
           */
          insertEmpLeaveBlocKLine(levBlockList.get(0), decisionBalanceEmp, result);
        } else {
          InsertEmpLeaveBlock(absenceType, decisionBalanceEmp, result);
        }
      }

    } catch (

    Exception e) {
      e.printStackTrace();
      log4j.error("Exception in InsertEmpLeaveLn in DecisionBalanceDAOImpl: ", e);
    }
    return leaveln;
  }

  public EHCMEmpLeave InsertEmpLeaveBlock(EHCMAbsenceType absenceType,
      DecisionBalance decisionBalanceEmp, JSONObject result) throws Exception {
    // TODO Auto-generated method stub
    EHCMEmpLeave leave = null;
    String hijiblockStartDate = null;
    String hijiFiveYrEndDate = null;
    String GregFiveYrEndDate = null;
    Date endDate = null;
    try {

      EhcmEmpPerInfo employee = decisionBalanceEmp.getEmployee();
      EHCMEmpLeaveBlock leaveblk = OBProvider.getInstance().get(EHCMEmpLeaveBlock.class);
      leaveblk.setClient(decisionBalanceEmp.getClient());
      leaveblk.setOrganization(decisionBalanceEmp.getOrganization());
      leaveblk.setEnabled(true);
      leaveblk.setCreationDate(new java.util.Date());
      leaveblk.setCreatedBy(OBContext.getOBContext().getUser());
      leaveblk.setUpdated(new java.util.Date());
      leaveblk.setUpdatedBy(OBContext.getOBContext().getUser());
      leaveblk.setEhcmEmpPerinfo(employee);
      leaveblk.setAbsenceType(absenceType);
      leaveblk.setAccrualdays(new BigDecimal(0));
      if (decisionBalanceEmp.getSubType() != null) {
        leaveblk.setSubtype(decisionBalanceEmp.getSubType());
      }
      if (result != null) {
        leaveblk.setStartDate(yearFormat.parse(result.getString("startdate")));
        leaveblk.setEndDate(yearFormat.parse(result.getString("enddate")));
      }

      /*
       * // calculate the 5 years enddate // form start date to 5 years date as enddate
       * hijiblockStartDate = UtilityDAO
       * .convertTohijriDate(yearFormat.format(leaveblk.getStartDate())); hijiFiveYrEndDate =
       * hijiblockStartDate.split("-")[0] + "-" + hijiblockStartDate.split("-")[1] + "-" +
       * ((Integer.valueOf(hijiblockStartDate.split("-")[2]) + 5)); GregFiveYrEndDate =
       * UtilityDAO.convertToGregorian(hijiFiveYrEndDate); endDate =
       * getMaxEndDate(leaveblk.getStartDate(), yearFormat.parse(GregFiveYrEndDate)); if (endDate !=
       * null) { leaveblk.setEndDate(endDate); }
       */

      OBDal.getInstance().save(leaveblk);

      insertEmpLeaveBlocKLine(leaveblk, decisionBalanceEmp, result);

    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in InsertEmpLeave in DecisionBalanceDAOImpl: ", e);
    }
    return leave;
  }

  public int insertEmpLeaveBlocKLine(EHCMEmpLeaveBlock header, DecisionBalance decisionBalanceEmp,
      JSONObject result) {
    int count = 0;
    try {
      EHCMEMPLeaveBlockLn lvblkln = OBProvider.getInstance().get(EHCMEMPLeaveBlockLn.class);
      lvblkln.setClient(header.getClient());
      lvblkln.setOrganization(header.getOrganization());
      lvblkln.setCreationDate(new java.util.Date());
      lvblkln.setCreatedBy(header.getCreatedBy());
      lvblkln.setUpdated(new java.util.Date());
      lvblkln.setUpdatedBy(header.getUpdatedBy());
      lvblkln.setEnabled(true);
      lvblkln.setEhcmAbsenceAttendance(null);
      lvblkln.setEhcmDecisionBalance(decisionBalanceEmp);
      lvblkln.setEhcmEmpLeaveblock(header);
      lvblkln.setLeavedays(decisionBalanceEmp.getBalance());
      if (result != null) {
        if (decisionBalanceEmp.getBlockStartdate() != null) {
          lvblkln.setStartDate(decisionBalanceEmp.getBlockStartdate());
        } else {
          lvblkln.setStartDate(yearFormat.parse(result.getString("startdate")));
        }
        lvblkln.setEndDate(yearFormat.parse(result.getString("enddate")));
      }
      log4j.debug("lev blokc line :" + header.getId());
      OBDal.getInstance().save(lvblkln);
      count = 1;
    } catch (final Exception e) {
      log4j.error("Exception in insertEmpLeaveBlocKLine in DecisionBalanceDAOImpl" + e);
    }
    return count;
  }

  @SuppressWarnings("rawtypes")
  public Date getHireYearLastDate(Date hireDate) throws Exception {
    // TODO Auto-generated method stub
    String sql = null;
    int year = 0;
    Date hireYearLastDate = null;
    String nextyearStartDate = null;
    try {
      year = Integer
          .parseInt(UtilityDAO.convertTohijriDate(yearFormat.format(hireDate)).split("-")[2]) + 1;
      nextyearStartDate = String.valueOf(year) + "0101";

      sql = " select max(hijri_date) ,gregorian_date from eut_hijri_dates where hijri_date='"
          + nextyearStartDate + "'"
          + " group by gregorian_date order by gregorian_date desc limit 1 ";

      Query qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      List datelist = qry.list();
      if (datelist != null && datelist.size() > 0) {
        Object[] row = (Object[]) datelist.get(0);
        hireYearLastDate = (Date) row[1];
      }
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in getHireYearLastDate in DecisionBalanceDAOImpl: ", e);
    }
    return hireYearLastDate;
  }

  /**
   * GET max end date
   * 
   * @param startDate
   * @param endDate
   * @return
   */
  @SuppressWarnings("unchecked")
  public Date getMaxEndDate(Date startDate, Date enddate) {
    String sql = null;
    Query qry = null;
    Date endDate = null;
    try {
      endDate = enddate;
      sql = " select max(e.hijri_date) ,e.gregorian_date from eut_hijri_dates e where gregorian_date >=:startdate "
          + " and gregorian_date <:enddate group by e.gregorian_date order by e.gregorian_date desc ";
      qry = OBDal.getInstance().getSession().createSQLQuery(sql);
      qry.setParameter("startdate", startDate);
      qry.setParameter("enddate", endDate);
      if (qry != null && qry.list().size() > 0) {
        List<Object> object = qry.list();
        Object[] row = (Object[]) object.get(0);
        endDate = (Date) row[1];
        return endDate;
      }
    } catch (final Exception e) {
      log4j.error("Exception in getMaxEndDate" + e);
    }
    return endDate;
  }

  public void businessMission(String missioncategoryID, String employeeID, Date hiredate,
      BigDecimal usedDays_initial_balance, DecisionBalance decisionbalanceLine) {

    OBQuery<EHCMMiscatEmployee> missionEmployeeObj = OBDal.getInstance().createQuery(
        EHCMMiscatEmployee.class,
        " as a  join a.ehcmMiscatPeriod b "
            + " join b.ehcmMissionCategory c  where c.id=:PmissioncategoryID and "
            + " :Phiredate between  b.startDate and b.endDate and a.employee.id =:PemployeeID");

    missionEmployeeObj.setNamedParameter("PmissioncategoryID", missioncategoryID);
    missionEmployeeObj.setNamedParameter("Phiredate", hiredate);
    missionEmployeeObj.setNamedParameter("PemployeeID", employeeID);

    if (missionEmployeeObj.list().size() > 0) {
      EHCMMiscatEmployee missionEmployeeListObj = missionEmployeeObj.list().get(0);
      long usedDays = missionEmployeeListObj.getUseddays();
      missionEmployeeListObj.setUseddays(usedDays + (usedDays_initial_balance.longValue()));
      missionEmployeeListObj.setEhcmDecisionBalance(decisionbalanceLine);

    }

  }

  @Override
  public Boolean checkUniqueConstraintForDecisionBalLine(DecisionBalance decisionBalance) {
    Boolean checkUniqueConstraintFailForDecisionBalLine = false;
    List<DecisionBalance> decisionBalanceList = null;
    String hql = "";
    // int A = 0;
    try {

      if (decisionBalance.getDecisionType().equals("BM"))
        hql = " and e.ehcmMissionCategory.id=:missionCategoryId ";

      if (decisionBalance.getDecisionType().equals("APLB"))
        hql = " and e.absenceType.id=:absenceTypeId ";

      if (decisionBalance.getAbsenceType() != null && decisionBalance.getAbsenceType().isSubtype())
        hql += " and e.subType.id=:subTypeId ";

      OBQuery<DecisionBalance> decisionBalQry = OBDal.getInstance().createQuery(
          DecisionBalance.class,
          " as e where e.employee.id=:employeeId and e.decisionType=:decisionType and e.client.id=:clientId  and e.id<>:currentId "
              + hql);

      decisionBalQry.setNamedParameter("employeeId", decisionBalance.getEmployee().getId());
      decisionBalQry.setNamedParameter("decisionType", decisionBalance.getDecisionType());
      decisionBalQry.setNamedParameter("clientId", decisionBalance.getClient().getId());
      decisionBalQry.setNamedParameter("currentId", decisionBalance.getId());

      if (decisionBalance.getDecisionType().equals("BM"))
        decisionBalQry.setNamedParameter("missionCategoryId",
            decisionBalance.getEhcmMissionCategory().getId());

      if (decisionBalance.getDecisionType().equals("APLB"))
        decisionBalQry.setNamedParameter("absenceTypeId", decisionBalance.getAbsenceType().getId());

      if (decisionBalance.getAbsenceType() != null && decisionBalance.getAbsenceType().isSubtype())
        decisionBalQry.setNamedParameter("subTypeId", decisionBalance.getSubType().getId());

      decisionBalanceList = decisionBalQry.list();
      if (decisionBalanceList.size() > 0) {
        checkUniqueConstraintFailForDecisionBalLine = true;
      }

    } catch (Exception e) {
      log4j.error("Exception in checkUniqueConstraintForDecisionBalLine" + e);
    }
    return checkUniqueConstraintFailForDecisionBalLine;
  }

}
