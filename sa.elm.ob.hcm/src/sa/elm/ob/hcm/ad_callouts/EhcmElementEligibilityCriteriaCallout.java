package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;

import sa.elm.ob.hcm.EHCMElmttypeDef;

/**
 * @author Priyanka Ranjan on 23/01/2017
 */

public class EhcmElementEligibilityCriteriaCallout extends SimpleCallout {

  /**
   * This Callout is responsible for process of Element Eligibility Criteria
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    VariablesSecureApp vars = info.vars;

    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String elementcode = vars.getStringParameter("inpelementcode");
    String elementname = vars.getStringParameter("inpelementname");
    String clientId = vars.getStringParameter("inpadClientId");
    String uniqueCode = vars.getStringParameter("inpcValidcombinationId");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      // load all values from Element Type Definiton window while select element code
      if (inpLastFieldChanged.equals("inpelementcode")) {
        OBQuery<EHCMElmttypeDef> record = OBDal.getInstance().createQuery(EHCMElmttypeDef.class,
            "as e where e.id='" + elementcode + "' and e.client.id='" + clientId + "'");
        record.setMaxResult(1);
        if (record.list().size() > 0) {
          EHCMElmttypeDef typdef = record.list().get(0);

          info.addResult("inpelementname", typdef.getId());
          info.addResult("inpreportingname", typdef.getReportingName());
          info.addResult("inpeledescription", typdef.getDescription());
          // set element classification name
          String queryclassname = "select name from ad_ref_list where ad_reference_id ='139E3FAE50A342B58AC29EE739FBF90C'"
              + " and value='" + typdef.getElementClassification() + "' ";
          st = conn.prepareStatement(queryclassname);
          rs = st.executeQuery();
          if (rs.next()) {
            info.addResult("inpelementclassi", rs.getString("name"));
          }
          // set element category name
          info.addResult("inpelementcatgry", typdef.getEhcmElementCatgry().getName());

          String query = " select eut_convert_to_hijri_timestamp('"
              + dateFormat.format(typdef.getStartDate()) + "')";
          st = conn.prepareStatement(query);
          rs = st.executeQuery();
          if (rs.next())
            info.addResult("inpstartDate", rs.getString("eut_convert_to_hijri_timestamp"));
          if (typdef.getEndDate() != null) {
            String query1 = " select eut_convert_to_hijri_timestamp('"
                + dateFormat.format(typdef.getEndDate()) + "')";
            st = conn.prepareStatement(query1);
            rs = st.executeQuery();
            if (rs.next())
              info.addResult("inpendDate", rs.getString("eut_convert_to_hijri_timestamp"));
          } else {
            info.addResult("inpendDate", null);
          }
        }
      }
      // load all values from Element Type Definiton window while select element name
      if (inpLastFieldChanged.equals("inpelementname")) {
        OBQuery<EHCMElmttypeDef> record = OBDal.getInstance().createQuery(EHCMElmttypeDef.class,
            "as e where e.id='" + elementname + "' and e.client.id='" + clientId + "'");
        record.setMaxResult(1);

        if (record.list().size() > 0) {
          EHCMElmttypeDef typdef = record.list().get(0);
          info.addResult("inpelementcode", typdef.getId());
          info.addResult("inpreportingname", typdef.getReportingName());
          info.addResult("inpeledescription", typdef.getDescription());
          // set element classification name
          String queryclassname = "select name from ad_ref_list where ad_reference_id ='139E3FAE50A342B58AC29EE739FBF90C'"
              + " and value='" + typdef.getElementClassification() + "' ";
          st = conn.prepareStatement(queryclassname);
          rs = st.executeQuery();
          if (rs.next()) {
            info.addResult("inpelementclassi", rs.getString("name"));
          }
          // set element category name
          info.addResult("inpelementcatgry", typdef.getEhcmElementCatgry().getName());

          String query = " select eut_convert_to_hijri_timestamp('"
              + dateFormat.format(typdef.getStartDate()) + "')";

          st = conn.prepareStatement(query);
          rs = st.executeQuery();
          if (rs.next())
            info.addResult("inpstartDate", rs.getString("eut_convert_to_hijri_timestamp"));
          if (typdef.getEndDate() != null) {
            String query1 = " select eut_convert_to_hijri_timestamp('"
                + dateFormat.format(typdef.getEndDate()) + "')";
            st = conn.prepareStatement(query1);
            rs = st.executeQuery();
            if (rs.next())
              info.addResult("inpendDate", rs.getString("eut_convert_to_hijri_timestamp"));
          } else {
            info.addResult("inpendDate", null);
          }
        }
      }
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
      log4j.error("Exception in Element Eligibility Criteria Action Type Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
