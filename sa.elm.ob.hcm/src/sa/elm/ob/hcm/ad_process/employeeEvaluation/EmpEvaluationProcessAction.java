package sa.elm.ob.hcm.ad_process.employeeEvaluation;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMDeflookupsTypeLn;
import sa.elm.ob.hcm.EHCMEmpEvalCompetency;
import sa.elm.ob.hcm.EHCMEmpEvaluation;
import sa.elm.ob.hcm.EHCMEmpEvaluation_Emp;

/**
 * This Process will handle the Employee Evaluation process action
 * 
 * @author Divya-12-02-2018
 *
 */
public class EmpEvaluationProcessAction implements Process {
  private static final Logger log4j = LoggerFactory.getLogger(EmpEvaluationProcessAction.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String lang = vars.getLanguage();

    final String empEvaluationId = bundle.getParams().get("Ehcm_Emp_Evaluation_ID").toString();
    final String clientId = (String) bundle.getContext().getClient();
    final String userId = (String) bundle.getContext().getUser();

    EHCMEmpEvaluation empEvaluationHeadObj = OBDal.getInstance().get(EHCMEmpEvaluation.class,
        empEvaluationId);
    final String orgId = empEvaluationHeadObj.getOrganization().getId();
    final String roleId = (String) bundle.getContext().getRole();
    EHCMDeflookupsTypeLn hcmReferenceObj = null;

    Boolean errorFlag = false;
    List<EHCMDeflookupsTypeLn> ehcmDeflookupsTypeLn = new ArrayList<EHCMDeflookupsTypeLn>();
    List<EHCMEmpEvaluation_Emp> empEvaluationEmpList = new ArrayList<EHCMEmpEvaluation_Emp>();
    List<EHCMEmpEvalCompetency> empEvaluationComList = new ArrayList<EHCMEmpEvalCompetency>();
    EmployeeEvaluationDAOImpl empEvaluationDAOImpl = new EmployeeEvaluationDAOImpl();
    try {
      OBContext.setAdminMode();
      OBQuery<EHCMEmpEvaluation_Emp> empEvaluationEmpQry = OBDal.getInstance().createQuery(
          EHCMEmpEvaluation_Emp.class,
          " as e where e.ehcmEmpEvaluation.id='" + empEvaluationId + "'");
      empEvaluationEmpList = empEvaluationEmpQry.list();

      OBQuery<EHCMEmpEvalCompetency> empEvaluationComQry = OBDal.getInstance().createQuery(
          EHCMEmpEvalCompetency.class,
          " as e where e.ehcmEmpevaluationEmp.id in "
              + " ( select e.id from EHCM_EmpEvaluation_Emp e where  e.ehcmEmpEvaluation.id='"
              + empEvaluationId + "') ");
      empEvaluationComList = empEvaluationComQry.list();

      if (empEvaluationEmpList.size() > 0 && empEvaluationComList.size() > 0) {
        if (empEvaluationHeadObj.getAction().equals("CO")) {
          empEvaluationDAOImpl.updateEmpEvaluationStatus(empEvaluationHeadObj,
              empEvaluationHeadObj.getAction());
          errorFlag = false;
          for (EHCMEmpEvaluation_Emp e : empEvaluationEmpList) {
            OBQuery<EHCMDeflookupsTypeLn> ehcmDeflookupsTypeLnquery = OBDal.getInstance()
                .createQuery(EHCMDeflookupsTypeLn.class, "as e where e.min <= :overallrating "
                    + " and e.maximum >= :overallrating order by updated desc limit 1 ");
            ehcmDeflookupsTypeLnquery.setNamedParameter("overallrating", e.getOverallrating());
            ehcmDeflookupsTypeLnquery.setMaxResult(1);
            ehcmDeflookupsTypeLn = ehcmDeflookupsTypeLnquery.list();
            if (ehcmDeflookupsTypeLn != null && ehcmDeflookupsTypeLn.size() > 0) {
              hcmReferenceObj = ehcmDeflookupsTypeLn.get(0);
              e.setEhcmDeflookupsTypeln(hcmReferenceObj);
              errorFlag = false;

            } else {
              errorFlag = true;
              obError.setType("Error");
              obError.setTitle("Error");
              obError.setMessage(OBMessageUtils.messageBD("ehcm_competency"));

            }

          }
        } else {
          empEvaluationDAOImpl.updateEmpEvaluationStatus(empEvaluationHeadObj,
              empEvaluationHeadObj.getAction());
          errorFlag = false;
          for (EHCMEmpEvaluation_Emp e : empEvaluationEmpList) {
            OBQuery<EHCMDeflookupsTypeLn> ehcmDeflookupsTypeLnquery = OBDal.getInstance()
                .createQuery(EHCMDeflookupsTypeLn.class,
                    "as e where e.min < :overallrating " + " and e.maximum > :overallrating ");
            ehcmDeflookupsTypeLnquery.setNamedParameter("overallrating", e.getOverallrating());
            ehcmDeflookupsTypeLn = ehcmDeflookupsTypeLnquery.list();
            if (ehcmDeflookupsTypeLn != null && ehcmDeflookupsTypeLn.size() > 0) {
              hcmReferenceObj = ehcmDeflookupsTypeLn.get(0);
              e.setEhcmDeflookupsTypeln(null);

            }

          }
        }
        if (!errorFlag) {
          obError.setType("Success");
          obError.setTitle("Success");
          if (empEvaluationHeadObj.getAction().equals("RE")) {
            obError.setMessage(OBMessageUtils.messageBD("EHCM_EmployeeEval_Com"));
          } else {
            obError.setMessage(OBMessageUtils.messageBD("EHCM_EmployeeEval_React"));
          }
        } else if (errorFlag) {
          OBDal.getInstance().rollbackAndClose();
          obError.setType("Error");
          obError.setTitle("Error");
          obError.setMessage(OBMessageUtils.messageBD("ehcm_competency"));

          // obError.setMessage(OBMessageUtils.messageBD("EHCM_EmployeeEval_NotCom"));
        }
        bundle.setResult(obError);
        OBDal.getInstance().flush();
      } else {
        obError.setType("Error");
        obError.setTitle("Error");
        if (empEvaluationEmpList.size() == 0)
          obError.setMessage(OBMessageUtils.messageBD("EHCM_EmpEval_OneEmployee"));
        else if (empEvaluationComList.size() == 0)
          obError.setMessage(OBMessageUtils.messageBD("EHCM_EmpEval_OneComptency"));
        bundle.setResult(obError);
      }

    } catch (Exception e) {
      bundle.setResult(obError);
      OBDal.getInstance().rollbackAndClose();
      if (log4j.isErrorEnabled()) {
        log4j.error("exception :", e);
      }
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}