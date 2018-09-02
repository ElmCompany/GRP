package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.Organization;

import sa.elm.ob.hcm.JobClassification;
import sa.elm.ob.hcm.Jobs;

@SuppressWarnings("serial")
public class PositionCallout extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String inpdepartment = vars.getStringParameter("inpdepartmentId");
    String inpsection = vars.getStringParameter("inpsectionId");
    // String inpjobClassification = vars.getStringParameter("inpehcmJobClassificationId");
    String inpjobsId = vars.getStringParameter("inpehcmJobsId");
    String inpjobName = vars.getStringParameter("inpjobName");
    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement st = null;
    ResultSet rs = null;
    log4j.debug("lastfield:" + inpLastFieldChanged);
    try {

      if (inpLastFieldChanged.equals("inpdepartmentId")) {
        if (inpdepartment.isEmpty()) {
          info.addResult("inpdeptname", "");
        } else {
          Organization dept = OBDal.getInstance().get(Organization.class, inpdepartment);
          log4j.debug("dept.getName():" + dept.getName());
          info.addResult("inpdeptname", dept.getName());
        }
      }
      if (inpLastFieldChanged.equals("inpsectionId")) {
        if (inpsection.isEmpty()) {
          info.addResult("inpsectionname", "");
        } else {
          Organization sect = OBDal.getInstance().get(Organization.class, inpsection);
          log4j.debug("sect.getName():" + sect.getName());
          info.addResult("inpsectionname", sect.getName());
        }
      }
      /*
       * if(inpLastFieldChanged.equals("inpehcmJobClassificationId")) { JobClassification
       * jobClassification = OBDal.getInstance().get(JobClassification.class, inpjobClassification);
       * if(jobClassification.getMcsletterDate() != null) { String query =
       * " select eut_convert_to_hijri_timestamp('" +
       * dateFormat.format(jobClassification.getMcsletterDate()) + "')";
       * 
       * st = conn.prepareStatement(query); rs = st.executeQuery(); if(rs.next())
       * info.addResult("inpmcsLetterDate", rs.getString("eut_convert_to_hijri_timestamp"));
       * 
       * } info.addResult("inpmainGroupCode", jobClassification.getMainGroupCode());
       * info.addResult("inpmainGroupName", jobClassification.getMainGroupName());
       * info.addResult("inpsubGroupCode", jobClassification.getSUBGroupCode());
       * info.addResult("inpsubGroupName", jobClassification.getSUBGroupName());
       * info.addResult("inpgroupSeqCode", jobClassification.getGroupSeqCode());
       * info.addResult("inpgroupSeqName", jobClassification.getGroupSeqName());
       * 
       * }
       */

      if (inpLastFieldChanged.equals("inpehcmJobsId")) {
        if (inpjobsId.isEmpty()) {
          info.addResult("inpjobName", "");
          info.addResult("inpmainGroupCode", "");
          info.addResult("inpmainGroupName", "");
          info.addResult("inpsubGroupCode", "");
          info.addResult("inpsubGroupName", "");
          info.addResult("inpgroupSeqCode", "");
          info.addResult("inpgroupSeqName", "");
        } else {
          Jobs job = OBDal.getInstance().get(Jobs.class, inpjobsId);
          // log.debug("job:" + job.getJOBTitle());
          info.addResult("inpjobName", inpjobsId);

          JobClassification jobClassification = OBDal.getInstance().get(JobClassification.class,
              job.getEhcmJobClassification().getId());
          info.addResult("inpmainGroupCode", jobClassification.getMainGroupCode());
          info.addResult("inpmainGroupName", jobClassification.getMainGroupName());
          info.addResult("inpsubGroupCode", jobClassification.getSUBGroupCode());
          info.addResult("inpsubGroupName", jobClassification.getSUBGroupName());
          info.addResult("inpgroupSeqCode", jobClassification.getGroupSeqCode());
          info.addResult("inpgroupSeqName", jobClassification.getGroupSeqName());
        }
      }

      if (inpLastFieldChanged.equals("inpjobName")) {
        if (inpjobName.isEmpty()) {
          info.addResult("inpehcmJobsId", "");
          info.addResult("inpmainGroupCode", "");
          info.addResult("inpmainGroupName", "");
          info.addResult("inpsubGroupCode", "");
          info.addResult("inpsubGroupName", "");
          info.addResult("inpgroupSeqCode", "");
          info.addResult("inpgroupSeqName", "");
        } else {
          log4j.debug("inpjobName:" + inpjobName);
          Jobs job = OBDal.getInstance().get(Jobs.class, inpjobName);
          info.addResult("inpehcmJobsId", job.getId());

          JobClassification jobClassification = OBDal.getInstance().get(JobClassification.class,
              job.getEhcmJobClassification().getId());
          info.addResult("inpmainGroupCode", jobClassification.getMainGroupCode());
          info.addResult("inpmainGroupName", jobClassification.getMainGroupName());
          info.addResult("inpsubGroupCode", jobClassification.getSUBGroupCode());
          info.addResult("inpsubGroupName", jobClassification.getSUBGroupName());
          info.addResult("inpgroupSeqCode", jobClassification.getGroupSeqCode());
          info.addResult("inpgroupSeqName", jobClassification.getGroupSeqName());
        }
      }

      if (inpLastFieldChanged.equals("inpehcmJobsId")) {

      }

    } catch (Exception e) {
      log4j.error("Exception in position Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }
}
