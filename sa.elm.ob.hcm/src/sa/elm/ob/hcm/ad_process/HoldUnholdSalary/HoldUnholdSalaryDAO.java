package sa.elm.ob.hcm.ad_process.HoldUnholdSalary;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEarnDeductElm;
import sa.elm.ob.hcm.EHCMHoldUnHoldSalary;
import sa.elm.ob.hcm.EHCMPayrollDefinition;
import sa.elm.ob.hcm.EHCMPayrollProcessHdr;
import sa.elm.ob.hcm.EHCMPayrolldefPeriod;
import sa.elm.ob.hcm.EhcmEmpPerInfo;

public class HoldUnholdSalaryDAO {

  private Connection connection = null;
  private static final Logger log = LoggerFactory.getLogger(HoldUnholdSalaryDAO.class);
  private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  public HoldUnholdSalaryDAO() {
    connection = getDbConnection();
  }

  private Connection getDbConnection() {
    return OBDal.getInstance().getConnection();
  }

  public static EHCMPayrolldefPeriod getPreviousPayrollPeriod(String periodStartingDate,
      EHCMPayrolldefPeriod payrollPrd) {
    try {
      String whereClause = "e where to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') < to_date(:startDate, 'dd-MM-yyyy') "
          + "and e. ehcmPayrollDefinition.id = :payrollDef order by e.startDate desc";

      OBQuery<EHCMPayrolldefPeriod> payrollPeriod = OBDal.getInstance()
          .createQuery(EHCMPayrolldefPeriod.class, whereClause);
      payrollPeriod.setNamedParameter("startDate", periodStartingDate);
      payrollPeriod.setNamedParameter("payrollDef", payrollPrd.getEhcmPayrollDefinition().getId());
      payrollPeriod.setFilterOnActive(false);
      payrollPeriod.setMaxResult(1);
      if (payrollPeriod.list().size() > 0) {
        return payrollPeriod.list().get(0);
      } else {
        return null;
      }
    } catch (Exception e) {
      log.error("Error in HoldUnHoldSalaryDAO.java : getPreviousPayrollPeriod() ");
      e.printStackTrace();
      return null;
    }
  }

  public static String getLastPayrollProcessedStartDate(EhcmEmpPerInfo empPerInfo) {
    try {
      String whereClause = " e where e.elementType.ehcmElementCatgry.type='02' and e.employeeDetails.employee.id=:employeePerInfoId order by e.employeeDetails.payrollDetails.payrollPeriod.startDate desc ";

      OBQuery<EHCMEarnDeductElm> earnDedElm = OBDal.getInstance()
          .createQuery(EHCMEarnDeductElm.class, whereClause);
      earnDedElm.setNamedParameter("employeePerInfoId", empPerInfo.getId());
      earnDedElm.setMaxResult(1);
      if (earnDedElm.list().size() > 0) {
        return earnDedElm.list().get(0).getEmployeeDetails().getPayrollDetails().getPayrollPeriod()
            .getStartDate().toString();
      } else {
        return null;
      }
    } catch (Exception e) {
      log.error("Error in HoldUnHoldSalaryDAO.java : getLastPayrollProcessedStartDate() ");
      e.printStackTrace();
      return null;
    }
  }

  public static boolean isPayrollProcessedInHoldUnholdPeriod(String periodStartingDate,
      EHCMPayrollDefinition payrollDef) {
    try {
      String whereClause = " e where e.payroll.id=:payrollDef and e.payrollPeriod.startDate>=to_date(:startDate, 'dd-MM-yyyy')";

      OBQuery<EHCMPayrollProcessHdr> payrollProcess = OBDal.getInstance()
          .createQuery(EHCMPayrollProcessHdr.class, whereClause);
      payrollProcess.setNamedParameter("payrollDef", payrollDef.getId());
      payrollProcess.setNamedParameter("startDate", periodStartingDate);
      if (payrollProcess.list().size() > 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      log.error("Error in HoldUnHoldSalaryDAO.java : isPayrollProcessedInHoldUnholdPeriod() ");
      e.printStackTrace();
      return false;
    }
  }

  public static boolean hasFutureHoldRequest(EhcmEmpPerInfo empPerInfo, String periodStartingDate) {
    try {
      String whereClause = " e where e.ehcmEmpPerinfo.id=:empId and e.requestType='HS' and  e.payrollPeriod.startDate>=to_date(:startDate, 'dd-MM-yyyy')";

      OBQuery<EHCMHoldUnHoldSalary> holdSlryReq = OBDal.getInstance()
          .createQuery(EHCMHoldUnHoldSalary.class, whereClause);
      holdSlryReq.setNamedParameter("empId", empPerInfo.getId());
      holdSlryReq.setNamedParameter("startDate", periodStartingDate);
      if (holdSlryReq.list().size() > 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      log.error("Error in HoldUnHoldSalaryDAO.java : hasFutureHoldRequest() ");
      e.printStackTrace();
      return false;
    }
  }

}
