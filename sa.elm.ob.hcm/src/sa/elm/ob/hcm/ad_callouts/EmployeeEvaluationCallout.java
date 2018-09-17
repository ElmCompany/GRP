package sa.elm.ob.hcm.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMComptypeCompetency;
import sa.elm.ob.hcm.EHCMEmpEvaluation;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_callouts.common.UpdateEmpDetailsInCallouts;
import sa.elm.ob.hcm.ad_callouts.dao.EmployeeEvaluationCalloutDAO;

/**
 * this process handle the call out for Employee Evaluation when change the employee or when min,max
 * changed
 * 
 * @author divya
 *
 */
@SuppressWarnings("serial")
public class EmployeeEvaluationCallout extends SimpleCallout {
  private static final Logger log4j = Logger.getLogger(EmployeeEvaluationCallout.class);

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String inpehcmEmpPerinfoId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String inpehcmComptypeCompetencyId = vars.getStringParameter("inpehcmComptypeCompetencyId");
    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpehcmEmpEvaluationId = vars.getStringParameter("inpehcmEmpEvaluationId");
    String inprating = vars.getNumericParameter("inprating");
    String inpmaximum = vars.getNumericParameter("inpmaximum");
    String inpehcmEmpevalCompetencyId = vars.getStringParameter("inpehcmEmpevalCompetencyId");
    BigDecimal rating = BigDecimal.ZERO;
    BigDecimal precentage = BigDecimal.ZERO;
    BigDecimal maximum = BigDecimal.ZERO;
    log4j.debug("inpLastFieldChanged:" + inpLastFieldChanged);
    if (StringUtils.isNotEmpty(inprating))
      rating = new BigDecimal(inprating);

    if (StringUtils.isNotEmpty(inpmaximum))
      maximum = new BigDecimal(inpmaximum);

    try {
      UpdateEmpDetailsInCallouts callouts = new UpdateEmpDetailsInCallouts();
      if (inpLastFieldChanged.equals("inpehcmEmpPerinfoId")) {
        if (StringUtils.isNotEmpty(inpehcmEmpPerinfoId)) {

          EmploymentInfo employinfo = EmployeeEvaluationCalloutDAO
              .getActiveEmployInfo(inpehcmEmpPerinfoId);
          EhcmEmpPerInfo employee = employinfo.getEhcmEmpPerinfo();
          info.addResult("inpehcmGradeId", employinfo.getEmploymentgrade().getId());
          info.addResult("inpempName", employee.getArabicfullname());
        } else {
          info.addResult("inpehcmGradeId", null);
          info.addResult("inpempName", null);

        }
      }

      if (inpLastFieldChanged.equals("inpehcmComptypeCompetencyId")
          || inpLastFieldChanged.equals("inprating")) {
        EHCMComptypeCompetency comptencyTypeComp = OBDal.getInstance()
            .get(EHCMComptypeCompetency.class, inpehcmComptypeCompetencyId);
        if (comptencyTypeComp != null) {
          info.addResult("inpmaximum",
              comptencyTypeComp.getMaximum() != null ? comptencyTypeComp.getMaximum()
                  : new BigDecimal(0));
          info.addResult("inpminimum",
              comptencyTypeComp.getMinimum() != null ? comptencyTypeComp.getMinimum()
                  : new BigDecimal(0));
          log4j.debug("inpehcmCompetencyId:" + comptencyTypeComp.getEhcmCompetency());
          info.addResult("inpehcmCompetencyId", comptencyTypeComp.getEhcmCompetency().getId());

          maximum = comptencyTypeComp.getMaximum();
          if (maximum != null && rating != null)
            precentage = (rating.divide(maximum)).multiply(new BigDecimal(100));
          info.addResult("inppercentage", precentage);
        }
        if (StringUtils.isNotEmpty(inpehcmEmpEvaluationId)
            && StringUtils.isEmpty(inpehcmEmpevalCompetencyId)) {
          EHCMEmpEvaluation empEvaluation = OBDal.getInstance().get(EHCMEmpEvaluation.class,
              inpehcmEmpEvaluationId);
          if (empEvaluation.getStatus().equals("CO")) {
            info.addResult("inprating", null);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in SupervisorCallout  :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
