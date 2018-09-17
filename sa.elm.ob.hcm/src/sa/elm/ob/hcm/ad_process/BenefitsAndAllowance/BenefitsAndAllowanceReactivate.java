package sa.elm.ob.hcm.ad_process.BenefitsAndAllowance;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMBenefitAllowance;
import sa.elm.ob.utility.util.Utility;

/**
 * @author Kousalya on 27/08/2018
 */
public class BenefitsAndAllowanceReactivate extends DalBaseProcess {

  private static final Logger log = LoggerFactory.getLogger(BenefitsAndAllowanceReactivate.class);
  private final OBError obError = new OBError();

  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    BenefitsAndAllowanceDAOImpl empAllowanceDAOImpl = new BenefitsAndAllowanceDAOImpl();
    final String allowanceId = (String) bundle.getParams().get("Ehcm_Benefit_Allowance_ID")
        .toString();
    EHCMBenefitAllowance allowance = Utility.getObject(EHCMBenefitAllowance.class, allowanceId);

    try {
      if (empAllowanceDAOImpl.checkPayrollProcessed(allowance, false)) {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_PayrollProcessed"));
        bundle.setResult(obError);
        return;
      } else {
        if (empAllowanceDAOImpl.reactivateEmpBenefitandAllowance(allowance)) {
          OBError result = OBErrorBuilder.buildMessage(null, "success",
              "@Ehcm_BenefitsandAllowance_Reactivate@");
          bundle.setResult(result);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    }
  }
}