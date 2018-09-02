package sa.elm.ob.hcm.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;

/**
 * @author Rashika.V.S on 27/07/2018
 */

public class EhcmEligibilityCriteriaLineCallout extends SimpleCallout {

  /**
   * This Callout is responsible for process in Element Eligibility Criteria Line
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    VariablesSecureApp vars = info.vars;
    String clientId = vars.getStringParameter("inpadClientId");
    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String uniqueCode = vars.getStringParameter("inpcValidcombinationId");  
    try {

      if(inpLastFieldChanged.equals("inpcValidcombinationId")){
        if(uniqueCode.equals("")){
          info.addResult("inpcElementvalueId", null);
        }
        else{
          OBQuery<AccountingCombination> record = OBDal.getInstance().createQuery(AccountingCombination.class,
              "as e where id='" + uniqueCode + "' and e.client.id='" + clientId + "'");
          if (record.list().size() > 0) {
            String accountId=record.list().get(0).getAccount().getId();
            info.addResult("inpcElementvalueId", accountId);
          }
        }
      }
    } catch (Exception e) {
      log4j.error("Exception in  Eligibility Criteria Action Type Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
