package sa.elm.ob.hcm.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.DalConnectionProvider;

import sa.elm.ob.hcm.PositionTree;

/**
 * @author Priyanka Ranjan on 14/11/2016
 */

public class AlreadyPrimaryRecordCallout extends SimpleCallout {

  /**
   * Callout to check primary record it should be only one
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    ConnectionProvider conn = new DalConnectionProvider(false);
    String language = OBContext.getOBContext().getLanguage().getLanguage();
    VariablesSecureApp vars = info.vars;
    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpehcmPositionTreeId = info.getStringParameter("inpehcmPositionTreeId", null);
    String inpadClientId = info.getStringParameter("inpadClientId", null);
    try {
      if (inpLastFieldChanged.equals("inpehcmPrimary")) {

        OBQuery<PositionTree> obQueryprimary = OBDal.getInstance().createQuery(PositionTree.class,
            "ehcmPrimary='Y' and id!='" + inpehcmPositionTreeId + "' and client='" + inpadClientId
                + "'");
        if (obQueryprimary.list().size() > 0) {

          info.addResult("ERROR", String
              .format(Utility.messageBD(conn, "Ehcm_Position_Tree_Primary_Record", language)));
          info.addResult("inpehcmPrimary", "N");

        }
      }
    }

    catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in AlreadyPrimaryRecordCallout Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      // TODO: handle exception
    }

  }
}
