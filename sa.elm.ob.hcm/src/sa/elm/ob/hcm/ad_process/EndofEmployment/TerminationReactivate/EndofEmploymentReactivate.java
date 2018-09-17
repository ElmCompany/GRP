package sa.elm.ob.hcm.ad_process.EndofEmployment.TerminationReactivate;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EhcmterminationEmpV;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;

/**
 * @author poongodi on 28/08/2018
 */
public class EndofEmploymentReactivate extends DalBaseProcess {

  private static final Logger log = LoggerFactory.getLogger(EndofEmploymentReactivate.class);
  private final OBError obError = new OBError();

  @SuppressWarnings("rawtypes")
  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    EndofEmploymentDAO terminationDAOImpl = new EmpTerminationDAOImpl();
    AssingedOrReleaseEmpInPositionDAO positionEmpHist = new AssingedOrReleaseEmpInPositionDAOImpl();
    log.debug("entering into EndofEmploymentReactivate");
    try {
      OBContext.setAdminMode();
      final String empTerminationId = bundle.getParams().get("Ehcm_Emp_Termination_ID").toString();
      EHCMEMPTermination terminationObj = OBDal.getInstance().get(EHCMEMPTermination.class,
          empTerminationId);
      String employeeId = terminationObj.getEhcmEmpPerinfo().getId();
      EhcmterminationEmpV terminationView = terminationObj.getEhcmEmpPerinfo();
      String decisionType = terminationObj.getDecisionType();
      EHCMEMPTermination employmentOldObj = null;
      if (terminationObj.getOriginalDecisionsNo() != null)
        employmentOldObj = OBDal.getInstance().get(EHCMEMPTermination.class,
            terminationObj.getOriginalDecisionsNo().getId());
      Date terminationDate = null;
      int millSec = 1 * 24 * 3600 * 1000;
      boolean chkPositionAvailableOrNot = false;
      boolean chkDelegatePositionAvailableOrNot = false;
      EhcmPosition pos = terminationObj.getPosition();
      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        terminationDate = terminationObj.getTerminationDate();
      } else {
        terminationDate = employmentOldObj.getTerminationDate();
      }
      if (!decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
        Date terminationDateAfter = new Date(terminationDate.getTime() + millSec);
        chkPositionAvailableOrNot = positionEmpHist.chkPositionAvailableOrNot(
            terminationView.getEhcmEmpPerinfo(), pos, terminationDateAfter, null, decisionType,
            false);
        if (terminationObj != null)
          chkDelegatePositionAvailableOrNot = positionEmpHist.chkDelegatePositionAvailableOrNot(
              terminationView.getEhcmEmpPerinfo(), null, terminationObj, terminationDateAfter, null,
              decisionType, true);
        if (chkPositionAvailableOrNot) {
          OBDal.getInstance().rollbackAndClose();
          OBError result = OBErrorBuilder.buildMessage(null, "error", "@Ehcm_Terminate_Pos@");
          bundle.setResult(result);
          return;
        }
        if (chkDelegatePositionAvailableOrNot) {
          OBDal.getInstance().rollbackAndClose();
          OBError result = OBErrorBuilder.buildMessage(null, "error", "@Ehcm_Terminate_Pos@");
          bundle.setResult(result);
          return;
        }
      }
      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        // Remove the employment information record
        terminationDAOImpl.removeEmploymentRecord(empTerminationId, vars, terminationObj);
        // Remove the record from ehcm_empstatus table
        terminationDAOImpl.removeEmpStatusRecord(employeeId, empTerminationId);
        // update the employee table
        terminationDAOImpl.updateEmpRecord(empTerminationId, vars, terminationObj);
        // update the enddate null for pos emp history table
        positionEmpHist.updateEndDateInPositionEmployeeHisotry(terminationView.getEhcmEmpPerinfo(),
            terminationObj.getPosition(), null, null, null, null, null, null, terminationObj, null);
        // update position employee history records with end date for delegated emp
        positionEmpHist.updateEndDateForDelegatedEmployee(terminationView.getEhcmEmpPerinfo(), null,
            null, terminationObj, DecisionTypeConstants.DECISION_TYPE_CANCEL);
      }

      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        // update termination record in employment table
        terminationDAOImpl.updateTerminationRecord(terminationObj, employmentOldObj, vars);

        // Task No. 6899 : update status table "termination field"
        final String EmpOldTerminationId = employmentOldObj.getId();
        terminationDAOImpl.updateEmpStatusRecord(employeeId, employmentOldObj);

        // make isactive as true for the old endofemployment
        EHCMEMPTermination oldTermination = employmentOldObj;
        oldTermination.setEnabled(true);
        OBDal.getInstance().save(oldTermination);
        OBDal.getInstance().flush();

      }

      if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
        // insert record in employment table
        terminationDAOImpl.insertRecordinEmploymentInfo(employmentOldObj, vars);
        // make isactive as true for the old and current endofemployment
        EHCMEMPTermination oldTermination = employmentOldObj;
        oldTermination.setEnabled(true);
        OBDal.getInstance().save(oldTermination);
        OBDal.getInstance().flush();
        EHCMEMPTermination curTermination = terminationObj;
        curTermination.setEnabled(true);
        OBDal.getInstance().save(curTermination);
        OBDal.getInstance().flush();

      }
      if (!decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        // update the enddate null for pos emp history table
        positionEmpHist.updateEndDateInPositionEmployeeHisotry(terminationView.getEhcmEmpPerinfo(),
            employmentOldObj.getPosition(), employmentOldObj.getTerminationDate(), null, null, null,
            null, null, employmentOldObj, null);
        // update position employee history records with end date for delegated emp
        positionEmpHist.updateEndDateForDelegatedEmployee(terminationView.getEhcmEmpPerinfo(),
            employmentOldObj.getTerminationDate(), null, employmentOldObj,
            employmentOldObj.getDecisionType());
      }
      terminationObj.setEhcmReactivate(true);
      terminationObj.setDecisionStatus("UP");
      terminationObj.setSueDecision(false);
      OBError result = OBErrorBuilder.buildMessage(null, "success", "@Ehcm_Endofemp_Reactivate@");
      bundle.setResult(result);
      return;

    } catch (Exception e) {
      e.printStackTrace();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
      OBDal.getInstance().commitAndClose();
    }
  }

}
