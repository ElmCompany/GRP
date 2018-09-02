package sa.elm.ob.hcm.ad_process.delinkparent;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;

public class EhcmDelinkProcess implements Process {

  private static final Logger log = LoggerFactory.getLogger(EhcmDelinkProcess.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    try {
      OBContext.setAdminMode();
      Connection con = OBDal.getInstance().getConnection();
      PreparedStatement ps = null;

      final String client = (String) bundle.getContext().getClient();
      // User user = OBDal.getInstance().get(User.class, vars.getUser());
      final String orgId = (String) bundle.getParams().get("AD_Org_ID").toString();

      String query = "DELETE FROM ad_treenode WHERE node_id  = '" + orgId + "' and ad_client_id ='"
          + client + "'";
      log.debug("orglistqry:" + query.toString());
      ps = con.prepareStatement(query);
      ps.executeUpdate();

      log.debug("orgId:" + orgId);
      obError.setType("Success");
      obError.setTitle("Success");
      obError.setMessage(OBMessageUtils.messageBD("@Ehcm_DelinkSucess@"));
      bundle.setResult(obError);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in Delinking Parent from organization :", e);
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}
