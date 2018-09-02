package sa.elm.ob.hcm.ad_process.DecisionBalance;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import sa.elm.ob.hcm.DecisionBalance;
import sa.elm.ob.hcm.DecisionBalanceHeader;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMMiscatEmployee;
import sa.elm.ob.hcm.ad_process.Constants;

public class DecisionBalanceProcess implements Process {
  private static final Logger log = Logger.getLogger(DecisionBalanceHeader.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    final String DecBalHdrId = (String) bundle.getParams().get("Ehcm_Deci_Bal_Hdr_ID").toString();
    DecisionBalanceHeader decisionbalance = OBDal.getInstance().get(DecisionBalanceHeader.class,
        DecBalHdrId);
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    DecisionBalanceDAO decisionBalanceDAO = new DecisionBalanceDAOImpl();
    EHCMAbsenceType absenceType = null;
    String message = null;
    String errormessage = null;
    String DecBalLneId = null, missioncategoryID = null, employeeID = null;
    Date hiredate = new Date(0);
    long usedDays_initial_balance = 0;
    try {
      OBContext.setAdminMode();

      // checking any one line added before process the balance
      if (decisionbalance.getEhcmDecisionBalanceList().size() == 0) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_EmployeeEval_AddLine"));
        bundle.setResult(obError);
        return;
      }

      // setting initial balance for annual leave
      if (decisionbalance.getDecisionType().equals(Constants.ANNUALLEAVEBALANCE)) {
        absenceType = decisionBalanceDAO
            .getAnnualLeaveBalanceFromPayrollReportConfig(decisionbalance.getClient().getId());
        // throwing error if absence type not defined
        if (absenceType == null) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("EHCM_AnnualLeaveNotConfig"));
          bundle.setResult(obError);
          return;
        } else {
          message = decisionBalanceDAO.chkAlreadyOpeningBalanceAddedForTatEmp(decisionbalance,
              absenceType);
          if (StringUtils.isNotEmpty(message)) {
            errormessage = OBMessageUtils.messageBD("EHCM_InitialBalEmpError");
            errormessage = errormessage.replaceAll("%", message);
            obError.setType("Error");
            obError.setTitle("Error");

            obError.setMessage(errormessage);
            bundle.setResult(obError);
            return;
          } else {
            decisionBalanceDAO.chkEmpLeavePresentOrNotAndinsertEmpLeave(decisionbalance,
                absenceType);
          }
        }
      }

      // setting initial balance for all paid leave with selected absence type
      if (decisionbalance.getDecisionType().equals(Constants.ALLPAIDLEAVEBALANCE)) {

        decisionBalanceDAO.chkEmpLeavePresentOrNotAndinsertEmpLeave(decisionbalance, absenceType);
      }

      if (decisionbalance.getDecisionType().equals(Constants.BUSINESSMISSIONBALANCE)) {

        List<DecisionBalance> decisionBalanceLineList = decisionbalance
            .getEhcmDecisionBalanceList();
        for (DecisionBalance decisionBalanceListObject : decisionBalanceLineList) {

          if (decisionBalanceListObject.getDecisionType().equals("BM")) {
            if (!decisionBalanceListObject.getEhcmMissionCategory().getId().equals("")) {

              missioncategoryID = decisionBalanceListObject.getEhcmMissionCategory().getId();
              employeeID = decisionBalanceListObject.getEmployee().getId();
              hiredate = decisionBalanceListObject.getEmployee().getHiredate();
              usedDays_initial_balance = decisionBalanceListObject.getBalance();

              businessMission(missioncategoryID, employeeID, hiredate, usedDays_initial_balance,
                  decisionBalanceListObject);
            }
          }
        }

        // OBDal.getInstance().save(decisionbalanceHeader);
      }

      decisionbalance.setAlertStatus("CO");
      decisionbalance.setProcessDate(new java.util.Date());
      OBDal.getInstance().save(decisionbalance);

      obError.setType("Success");
      obError.setTitle("Success");
      obError.setMessage(OBMessageUtils.messageBD("Efin_BudgetPre_Submit"));
      bundle.setResult(obError);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();

    } catch (Exception e) {
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  public void businessMission(String missioncategoryID, String employeeID, Date hiredate,
      long usedDays_initial_balance, DecisionBalance decisionbalanceLine) {

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
      missionEmployeeListObj.setUseddays(usedDays + usedDays_initial_balance);
      missionEmployeeListObj.setEhcmDecisionBalance(decisionbalanceLine);

    }

  }

}
