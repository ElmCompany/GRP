package sa.elm.ob.hcm.ad_process.EmployeeSuspension;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.SuspensionReason;

/**
 * @author Mouli.K
 */
public class EmployeeSuspenseReactivate implements Process {
  private static final Logger log = LoggerFactory.getLogger(EmployeeSuspenseIssuance.class);

  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Issue the Suspension");
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    final String suspensionId = (String) bundle.getParams().get("Ehcm_Emp_Suspension_ID")
        .toString();
    EmployeeSuspension objSuspension = OBDal.getInstance().get(EmployeeSuspension.class,
        suspensionId);
    EmployeeSuspension oldSuspension = null;
    EmployeeSuspension oldSuspensionoldSus = null;
    Boolean errorflag = false;
    EHCMEMPTermination oldTermination = null;
    SuspensionReason susReason = null;

    EmploymentInfo oldEmployment = null;

    if (objSuspension.getOriginalDecisionNo() != null) {
      oldSuspension = objSuspension.getOriginalDecisionNo();
      OBQuery<EHCMEMPTermination> oldTerminationQry = OBDal.getInstance().createQuery(
          EHCMEMPTermination.class,
          "as e where e.ehcmEmpSuspension.id='" + oldSuspension.getId() + "'");
      if (oldTerminationQry.list().size() > 0) {
        oldTermination = oldTerminationQry.list().get(0);
      }
      if (oldSuspension.getOriginalDecisionNo() != null) {
        oldSuspensionoldSus = oldSuspension.getOriginalDecisionNo();
      }
    }

    try {
      OBContext.setAdminMode(true);

      if (oldSuspension != null) {

        // Both Current and Previous Suspension Type is SUS

        if (oldSuspension.getSuspensionType().equals("SUS")
            && objSuspension.getSuspensionType().equals("SUS")) {
          if (oldSuspension.getDecisionType().equals("CR")
              || oldSuspension.getDecisionType().equals("UP")) {
            if (objSuspension.getDecisionType().equals("CR")
                || objSuspension.getDecisionType().equals("UP")) {
              EmpSuspensionReactiveDAO.updateEmploymentRecordSUS(objSuspension, oldSuspension,
                  oldTermination, vars);
            } else {
              EmpSuspensionReactiveDAO.insertEmploymentRecordSUS(objSuspension, oldSuspension,
                  oldTermination, vars);
            }
          } else {
            EmpSuspensionReactiveDAO.cancelEmploymentRecordSUS(objSuspension, oldSuspension,
                oldTermination, vars);
          }
        }

        // Both Current and Previous Suspension Type is SUE

        if (oldSuspension.getSuspensionType().equals("SUE")
            && objSuspension.getSuspensionType().equals("SUE")) {
          if (oldSuspension.getDecisionType().equals("CR")
              || oldSuspension.getDecisionType().equals("UP")) {
            if (objSuspension.getDecisionType().equals("CR")
                || objSuspension.getDecisionType().equals("UP")) {

              EmpSuspensionReactiveDAO.updEmpRcdSUE(objSuspension, oldSuspension, oldTermination,
                  vars);

            } else {

              EmpSuspensionReactiveDAO.insertEmploymentRecordSUE(objSuspension, oldSuspension,
                  oldTermination, vars);
            }
          } else {
            EmpSuspensionReactiveDAO.cncEmpRcdSUE(objSuspension, oldSuspension, oldTermination,
                vars);
          }
        }

        // Both Current and Previous Suspension Type is in SUE and SUS

        if (oldSuspension.getSuspensionType().equals("SUS")
            && objSuspension.getSuspensionType().equals("SUE")) {
          if (objSuspension.getDecisionType().equals("CR")
              || objSuspension.getDecisionType().equals("UP")) {

            EmpSuspensionReactiveDAO.cancelEmploymentRecordSUS(objSuspension, oldSuspension,
                oldTermination, vars);

          }

        }

        if (errorflag.equals(false)) {
          oldSuspension.setEnabled(true);
          objSuspension.setEnabled(false);
          objSuspension.setDecisionStatus("UP");
          objSuspension.setSueDecision(false);
          OBDal.getInstance().save(objSuspension);
          OBDal.getInstance().save(oldSuspension);
          OBDal.getInstance().flush();
        }
      } else {

        int count = EmpSuspensionHandlerDAO.cancelEmploymentRecord(objSuspension, objSuspension,
            oldTermination, vars);
        if (count == 1) {
          OBDal.getInstance().rollbackAndClose();
          OBError result = OBErrorBuilder.buildMessage(null, "error",
              "@Ehcm_Terminated_position(not available)@");
          bundle.setResult(result);
          return;
        } else if (count == 2) {
          OBDal.getInstance().rollbackAndClose();
          OBError result = OBErrorBuilder.buildMessage(null, "error",
              "@Ehcm_Terminated_position(not available)@");
          bundle.setResult(result);
          return;
        }
        objSuspension.setEnabled(true);
        objSuspension.setDecisionStatus("UP");
        objSuspension.setSueDecision(false);
        OBDal.getInstance().save(objSuspension);
        OBDal.getInstance().flush();
      }
      if (errorflag.equals(false)) {
        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_Submit_Process"));
        bundle.setResult(obError);
      } else {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_Submit_Process_Error"));
        bundle.setResult(obError);
      }
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();

    } catch (Exception e) {
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      Throwable t = DbUtility.getUnderlyingSQLException(e);
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), t.getMessage());
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}