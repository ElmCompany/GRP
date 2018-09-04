package sa.elm.ob.hcm.ad_process.DecisionBalance;

import java.math.BigDecimal;
import java.util.Date;

import sa.elm.ob.hcm.DecisionBalance;
import sa.elm.ob.hcm.DecisionBalanceHeader;
import sa.elm.ob.hcm.EHCMAbsenceType;

public interface DecisionBalanceDAO {

  /**
   * check employee leave header already exist or not, if not then add header abd line alos insert
   * emp leave header for each absence type based on start date and enddate
   * 
   * @param decisionBalanceHeaderObj
   * @param absencetype
   * @return
   * @throws Exception
   */
  void chkEmpLeavePresentOrNotAndinsertEmpLeave(DecisionBalanceHeader decisionBalanceHeader,
      EHCMAbsenceType absenceType) throws Exception;

  /**
   * get Annual Leave absence Type object from Payroll Report Configuration
   * 
   * @param clientId
   * @return
   * @throws Exception
   */
  EHCMAbsenceType getAnnualLeaveBalanceFromPayrollReportConfig(String clientId) throws Exception;

  String chkAlreadyOpeningBalanceAddedForTatEmp(DecisionBalanceHeader decisionBalanceHeader,
      EHCMAbsenceType absenceType) throws Exception;

  void businessMission(String missioncategoryID, String employeeID, Date hiredate,
      BigDecimal usedDays_initial_balance, DecisionBalance decisionbalanceLine) throws Exception;

  /**
   * 
   * @param decisionBalance
   * @return
   * @throws Exception
   */
  Boolean checkUniqueConstraintForDecisionBalLine(DecisionBalance decisionBalance) throws Exception;

}
