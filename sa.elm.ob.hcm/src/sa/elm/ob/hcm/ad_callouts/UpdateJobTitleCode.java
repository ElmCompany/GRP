package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.UpdateJobClassification;
import sa.elm.ob.hcm.ehcmgrade;

/**
 * 
 * @author gopalakrishnan on 12/10/2016
 * 
 */

@SuppressWarnings("serial")
public class UpdateJobTitleCode extends SimpleCallout {
  /**
   * Callout to update code on update job tab in update job title window.
   */

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    String inpActive = info.getStringParameter("inpenabled", null);

    try {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      Date now = new Date();
      Connection conn = OBDal.getInstance().getConnection();
      PreparedStatement st = null;
      ResultSet rs = null;
      VariablesSecureApp vars = info.vars;
      String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
      String inpGrade = vars.getStringParameter("inpehcmGradeId");
      String inpJobClassificationId = vars.getStringParameter("inpehcmUpjobClassificationId");
      if (inpLastFieldChanged.equals("inpenabled")) {
        String query = " select eut_convert_to_hijri_timestamp('" + dateFormat.format(now) + "')";
        st = conn.prepareStatement(query);
        rs = st.executeQuery();
        if (rs.next()) {
          if (inpActive.equals("N")) {
            info.addResult("inpenddate", rs.getString("eut_convert_to_hijri_timestamp"));
          } else {
            info.addResult("inpenddate", null);
          }
        }
      }
      if (inpLastFieldChanged.equals("inpehcmGradeId")) {
        ehcmgrade objGrade = OBDal.getInstance().get(ehcmgrade.class, inpGrade);
        UpdateJobClassification objClass = OBDal.getInstance().get(UpdateJobClassification.class,
            inpJobClassificationId);
        info.addResult("inpvalue",
            objClass.getClassificationCode().concat(objGrade.getSearchKey()));
      }
    } catch (Exception e) {
      log4j.error("Exception in UpdateJobTitleCode Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }
}
