package sa.elm.ob.hcm.ad_process.EmpExtraStep;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import sa.elm.ob.hcm.EhcmEmployeeExtraStep;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.EmpExtraStep.DAO.ExtraStepDecisionReactivateDAO;
import sa.elm.ob.hcm.util.Utility;

public class ExtraStepDecisionReactivate implements Process {
  private static final Logger log = Logger.getLogger(ExtraStepDecisionReactivate.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    final String extraStepDecisionId = (String) bundle.getParams().get("Ehcm_Emp_Extrastep_ID")
        .toString();
    EhcmEmployeeExtraStep extraStepDecision = OBDal.getInstance().get(EhcmEmployeeExtraStep.class,
        extraStepDecisionId);
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    EmploymentInfo info = null;
    String decisionType = extraStepDecision.getDecisionType();
    String lang = vars.getLanguage();
    int count = 0;
    try {
      OBContext.setAdminMode(true);
      if (extraStepDecision.getDecisionType().equals("CR")) {
        ExtraStepDecisionReactivateDAO.deleteEmpInfo(extraStepDecision, decisionType);
        extraStepDecision.setDecisionStatus("UP");
        extraStepDecision.setDecisionDate(null);
        extraStepDecision.setSueDecision(false);
      }
      if (extraStepDecision.getDecisionType().equals("UP")) {
        List<EmploymentInfo> empInfoList = null;
        List<EmploymentInfo> prevEmpInfoList = null;
        EmploymentInfo prevEmpinfo = null;
        info = Utility.getActiveEmployInfo(extraStepDecision.getEhcmEmpPerinfo().getId());
        EhcmEmployeeExtraStep prevExtrastep = extraStepDecision.getOriginalDecisionNo();
        OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
            "  ehcmEmpPerinfo.id = :employeeId and (ehcmEmpExtrastep.id <>:currentId or ehcmEmpExtrastep.id is null) order by creationDate desc ");
        empInfoObj.setNamedParameter("employeeId", extraStepDecision.getEhcmEmpPerinfo().getId());
        empInfoObj.setNamedParameter("currentId", extraStepDecision.getId());

        empInfoObj.setMaxResult(1);
        empInfoList = empInfoObj.list();

        if (empInfoList.size() > 0) {
          prevEmpinfo = empInfoList.get(0);
        }

        ExtraStepDecisionReactivateDAO.updateEmpInfo(prevExtrastep, prevEmpinfo, info, vars, "UP",
            lang, null, null);
        extraStepDecision.setEnabled(true);
        extraStepDecision.setDecisionStatus("UP");
        extraStepDecision.setSueDecision(false);
        extraStepDecision.setDecisionType("UP");
      }
      if (extraStepDecision.getDecisionType().equals("CA")) {
        EhcmEmployeeExtraStep decisionProcess = OBDal.getInstance().get(EhcmEmployeeExtraStep.class,
            extraStepDecisionId);
        // If the employee already reached the grade step,then throw the error.
        if (decisionProcess.getDecisionType().equals("CR")) {
          if (decisionProcess.getEhcmPayscaleline().getId() == decisionProcess.getNewgradepoint()
              .getId()) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("Ehcm_Empgrade_Exist"));
            bundle.setResult(obError);
            return;
          }
        }
        info = Utility.getActiveEmployInfo(extraStepDecision.getEhcmEmpPerinfo().getId());
        ExtraStepDecisionReactivateDAO
            .insertLineinEmploymentInfo(extraStepDecision.getOriginalDecisionNo(), vars, "CR");
        extraStepDecision.setDecisionType("CA");
        extraStepDecision.setDecisionStatus("UP");
        extraStepDecision.setSueDecision(false);
        extraStepDecision.setDecisionDate(null);
      }
      obError.setType("Success");
      obError.setTitle("Success");
      obError.setMessage(OBMessageUtils.messageBD("Ehcm_Submit_Process"));
      bundle.setResult(obError);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}
