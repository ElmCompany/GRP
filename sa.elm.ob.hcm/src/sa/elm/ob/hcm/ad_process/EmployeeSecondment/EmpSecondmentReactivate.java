package sa.elm.ob.hcm.ad_process.EmployeeSecondment;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONObject;
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

import sa.elm.ob.hcm.EHCMEmpSecondment;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.util.Utility;

/**
 * This class handle the reactivate process for employee secondment
 * 
 * @author divya on 08/05/2018
 */
public class EmpSecondmentReactivate extends DalBaseProcess {

  private static final Logger log = LoggerFactory.getLogger(EmpSecondmentReactivate.class);
  private final OBError obError = new OBError();

  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub

    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);

    log.debug("entering into JoinReqReactivateProcess");
    try {
      OBContext.setAdminMode();
      final String secondmentId = (String) bundle.getParams().get("Ehcm_Emp_Secondment_ID")
          .toString();
      EHCMEmpSecondment secondment = OBDal.getInstance().get(EHCMEmpSecondment.class, secondmentId);
      EHCMEmpSecondment empInfoSecondment = null;
      EmpSecondmentReactivateDAO empSecondmentReactivateDAO = new EmpSecondmentReactivateDAOImpl();
      boolean chkEmplyInfoExistAfterSecondment = false;
      boolean chkEmplyInfoExistsInCancelCase = false;
      if (!secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
        empInfoSecondment = secondment;
        chkEmplyInfoExistAfterSecondment = empSecondmentReactivateDAO
            .chkEmplyInfoExistAfterSecondment(empInfoSecondment);
        if (chkEmplyInfoExistAfterSecondment) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_JoinIsactive_Error"));
          bundle.setResult(obError);
          return;
        }
      } else {
        empInfoSecondment = secondment.getOriginalDecisionsNo();
        chkEmplyInfoExistsInCancelCase = empSecondmentReactivateDAO
            .chkEmplyInfoExistsInCancelCase(empInfoSecondment);
        if (chkEmplyInfoExistsInCancelCase) {
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("Ehcm_JoinIsactive_Error"));
          bundle.setResult(obError);
          return;
        }

        // checking decision overlap
        if (empInfoSecondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
            || (empInfoSecondment.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                || empInfoSecondment.getDecisionType()
                    .equals(DecisionTypeConstants.DECISION_TYPE_EXTEND))) {
          JSONObject result = Utility.chkDecisionOverlap(Constants.SECONDMENT_OVERLAP,
              sa.elm.ob.utility.util.Utility.formatDate(empInfoSecondment.getStartDate()),
              sa.elm.ob.utility.util.Utility.formatDate(empInfoSecondment.getEndDate()),
              empInfoSecondment.getEhcmEmpPerinfo().getId(), null, empInfoSecondment.getId());
          if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
            if (empInfoSecondment.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
                || (empInfoSecondment.getDecisionType()
                    .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                    && (result.has("secondmentId") && !result.getString("secondmentId")
                        .equals(empInfoSecondment.getOriginalDecisionsNo().getId()))
                    || !result.has("secondmentId"))) {
              if (result.has("errormsg")) {
                obError.setType("Error");
                obError.setTitle("Error");
                obError.setMessage(OBMessageUtils.messageBD(result.getString("errormsg")));
                bundle.setResult(obError);
                return;
              }
            }
          }
        }
      }

      if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
          || secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
        empSecondmentReactivateDAO.deleteEmpInfo(secondment);
      } else if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        empSecondmentReactivateDAO.updateEmpInfoInupdateCase(secondment);
      } else if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
        empSecondmentReactivateDAO.updateExtendRecordInCutOffCase(secondment);
      }

      else if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
        empSecondmentReactivateDAO.InsertEmpInfoInCancelCase(secondment.getOriginalDecisionsNo(),
            vars);
        secondment.getOriginalDecisionsNo().setEnabled(true);
        secondment.setEnabled(true);
        OBDal.getInstance().save(secondment);
      }
      empSecondmentReactivateDAO.updateSecondmentStatus(secondment);

      OBError result = OBErrorBuilder.buildMessage(null, "success",
          "@EHCM_EmpSecondment_Reactivate@");
      bundle.setResult(result);
      return;

    }

    catch (Exception e) {
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
