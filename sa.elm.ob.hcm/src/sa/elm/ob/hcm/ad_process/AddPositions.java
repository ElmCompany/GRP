package sa.elm.ob.hcm.ad_process;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.PositionTree;
import sa.elm.ob.hcm.PositionTreenode;
import sa.elm.ob.utility.util.Utility;

public class AddPositions implements Process {

  private static final Logger log = Logger.getLogger(AddPositions.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    try {
      OBContext.setAdminMode();

      OBQuery<EhcmPosition> positionsQuery = null;
      List<EhcmPosition> positions = null;
      Boolean allowInsert = Boolean.FALSE;

      obError.setType("Error");
      obError.setTitle("Error");

      final String strPositionHeaderId = bundle.getParams().get("Ehcm_Position_Tree_ID").toString();
      log.debug("Header Id: " + strPositionHeaderId);
      PositionTree positionTree = Utility.getObject(PositionTree.class, strPositionHeaderId);

      positionsQuery = OBDal.getInstance().createQuery(EhcmPosition.class,
          " client.id = '" + positionTree.getClient().getId() + "'  and transactionStatus = 'I'");
      PositionTreenode treenode = null;

      if (positionsQuery != null) {
        positions = positionsQuery.list();
        if (positions.size() > 0) {
          allowInsert = Boolean.TRUE;

          if (positionTree.getEhcmPositionTreenodeList().size() > 0) {
            OBDal.getInstance().getConnection()
                .prepareStatement(
                    " delete from ehcm_position_treenode  where ehcm_position_tree_id = '"
                        + positionTree.getId() + "'")
                .executeUpdate();
          }

          for (EhcmPosition position : positions) {
            treenode = OBProvider.getInstance().get(PositionTreenode.class);

            treenode.setClient(positionTree.getClient());
            treenode.setOrganization(positionTree.getOrganization());
            treenode.setCreatedBy(OBContext.getOBContext().getUser());
            treenode.setUpdatedBy(OBContext.getOBContext().getUser());
            treenode.setPosition(position);
            treenode.setPositionTree(positionTree);

            OBDal.getInstance().save(treenode);
          }
        } else {
          obError.setMessage(OBMessageUtils.messageBD("EHCM_NoPositions"));
        }
      } else {
        obError.setMessage(OBMessageUtils.messageBD("EHCM_NoPositions"));
      }

      if (allowInsert) {
        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_Position_Succes"));
      }
      bundle.setResult(obError);
      OBDal.getInstance().commitAndClose();
      OBDal.getInstance().flush();
    } catch (Exception e) {
      obError.setMessage(e.getMessage());
      bundle.setResult(obError);
      log.error("Exception while adding positions: " + e);
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}