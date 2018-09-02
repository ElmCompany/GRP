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

public class EmployeeExtendServiceCalloutDAOimpl implements EmployeeExtendServiceCalloutDAO {
  private static final Logger log = LoggerFactory
      .getLogger(EmployeeExtendServiceCalloutDAOimpl.class);

  @Override
  public JSONObject getAuthorizationInfoDetails(String organizationId, Date effectiveDate)
      throws Exception {
    // TODO Auto-generated method stub
    JSONObject rslt = new JSONObject();
    try {
      SQLQuery query = OBDal.getInstance().getSession().createSQLQuery(
          "select authorizedperson,authorizedjobtitle from ehcm_authorizationinfo(?,?)");
      query.setParameter(0, organizationId);
      query.setParameter(1, effectiveDate);
      List list = query.list();
      if (list != null && list.size() > 0) {
        Object[] row = (Object[]) list.get(0);
        if (row != null) {
          rslt.put("authorizedPerson", row[0]);
          rslt.put("authorizedJobTitle", row[1]);
        }
      }
    } catch (OBException e) {
      log.error("Exception while EmployeeExtendServiceCalloutDAOimpl:" + e);
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
    return rslt;
  }

}
