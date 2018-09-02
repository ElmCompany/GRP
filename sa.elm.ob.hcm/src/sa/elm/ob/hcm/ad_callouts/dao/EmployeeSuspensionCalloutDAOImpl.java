package sa.elm.ob.hcm.ad_callouts.dao;

import java.util.Date;
import java.util.List;

import org.codehaus.jettison.json.JSONObject;
import org.hibernate.SQLQuery;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmployeeSuspensionCalloutDAOImpl implements EmployeeSuspensionCalloutDAO {
  private static final Logger LOG = LoggerFactory.getLogger(EmployeeSuspensionCalloutDAOImpl.class);

  @SuppressWarnings("rawtypes")
  @Override
  public JSONObject getAuthorizationInfoDetails(String organisationId, Date startDate)
      throws Exception {
    // TODO Auto-generated method stub
    JSONObject result = new JSONObject();
    try {

      SQLQuery query = OBDal.getInstance().getSession().createSQLQuery(
          "select authorizedperson,authorizedjobtitle from ehcm_authorizationinfo(?,?)");
      query.setParameter(0, organisationId);
      query.setParameter(1, startDate);
      List list = query.list();
      if (list != null && list.size() > 0) {
        Object[] row = (Object[]) list.get(0);

        if (row != null) {
          result.put("authorizedPerson", row[0]);
          result.put("authorizedJobTitle", row[1]);
        }
      }

    } catch (OBException e) {
      LOG.error("Exception while EmployeeSuspensionDaoimpl:" + e);
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

    return result;
  }
}
