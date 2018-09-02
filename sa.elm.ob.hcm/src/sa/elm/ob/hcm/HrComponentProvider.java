package sa.elm.ob.hcm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.KernelConstants;

@ApplicationScoped
@ComponentProvider.Qualifier(HrComponentProvider.PROVIDER)
public class HrComponentProvider extends BaseComponentProvider {

  public static final String PROVIDER = "EHCM_Provider";

  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    return null;
  }

  @Override
  public List<ComponentResource> getGlobalComponentResources() {

    List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    /*
     * globalResources.add(createStaticResource("web/sa.elm.ob.hcm/js/NewButtonDisable.js", true));
     */
    globalResources.add(createStaticResource("web/sa.elm.ob.hcm/js/addressstyle.js", true));
    globalResources
        .add(createStaticResource("web/sa.elm.ob.hcm/js/updatepositionnewvalues.js", true));
    globalResources.add(
        createStaticResource("web/sa.elm.ob.hcm/js/EhcmEleEligibiltyCriteriaNotCosted.js", true));
    globalResources
        .add(createStaticResource("web/sa.elm.ob.hcm/js/EmploymentgroupEmployee.js", true));
    globalResources
        .add(createStyleSheetResource(
            "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
                + KernelConstants.SKIN_DEFAULT + "/sa.elm.ob.hcm/absenceType_variables.css",
            false));
    globalResources.add(createStaticResource("web/sa.elm.ob.hcm/js/AbsenceTypeVariables.js", true));
    globalResources
        .add(createStyleSheetResource("web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_DEFAULT + "/sa.elm.ob.hcm/print_pdf.css", false));
    globalResources.add(createStaticResource("web/sa.elm.ob.hcm/js/PrintReport.js", true));
    globalResources.add(createStaticResource("web/sa.elm.ob.hcm/js/IrtabDisable.js", false));
    globalResources
        .add(createStyleSheetResource("web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_DEFAULT + "/sa.elm.ob.hcm/overtime_decree.css", false));
    globalResources.add(createStaticResource("web/sa.elm.ob.hcm/js/OvertimeDecree.js", true));

    globalResources
        .add(createStyleSheetResource("web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_DEFAULT + "/sa.elm.ob.hcm/end_of_employment.css", false));
    globalResources.add(createStaticResource("web/sa.elm.ob.hcm/js/EndOfEmployment.js", true));

    globalResources
        .add(createStyleSheetResource("web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_DEFAULT + "/sa.elm.ob.hcm/promotion_decision.css", false));
    globalResources.add(createStaticResource("web/sa.elm.ob.hcm/js/PromotionDecision.js", true));
    globalResources
        .add(createStyleSheetResource(
            "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
                + KernelConstants.SKIN_DEFAULT + "/sa.elm.ob.hcm/elementFormula_variables.css",
            false));
    globalResources
        .add(createStaticResource("web/sa.elm.ob.hcm/js/ElementFormulaVariables.js", true));
    globalResources
        .add(createStaticResource("web/sa.elm.ob.hcm/js/MinMaxContractService.js", true));
    return globalResources;
  }
}
