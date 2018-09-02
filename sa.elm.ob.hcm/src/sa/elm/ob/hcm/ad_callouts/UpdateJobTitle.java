package sa.elm.ob.hcm.ad_callouts;

import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.JobClassification;
import sa.elm.ob.hcm.ad_callouts.dao.UpdateJobTitleDAOImpl;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author gopalakrishnan on 13/10/2016
 * 
 */

@SuppressWarnings("serial")
public class UpdateJobTitle extends SimpleCallout {
  /**
   * Callout to populate fields in update job Title window.
   */

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy-MM-dd");
    UpdateJobTitleDAOImpl daoimpl = new UpdateJobTitleDAOImpl();
    try {
      VariablesSecureApp vars = info.vars;
      String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
      String inpJobClassificationId = vars.getStringParameter("inpehcmJobClassificationId");
      String inpclassificationCode = vars.getStringParameter("inpvalue");
      String inpUpjobClassificationId = vars.getStringParameter("inpehcmUpjobClassificationId");
      String clsDate = "";
      String mcsDate = "";
      // Populate all the fields in update job title
      if (inpLastFieldChanged.equals("inpehcmJobClassificationId")) {

        JobClassification objJobclass = OBDal.getInstance().get(JobClassification.class,
            inpJobClassificationId);
        // removed task no 3753 point 5
        /*
         * if(objJobclass.getStartDate() != null) { startDate =
         * UtilityDAO.convertTohijriDate(yearFormat.format(dateFormat.parse(dateFormat.format(
         * objJobclass.getStartDate())))); } info.addResult("inpstartdate", startDate);
         */
        if (objJobclass != null) {
          if (objJobclass.getClassificationDate() != null) {
            clsDate = UtilityDAO.convertTohijriDate(yearFormat
                .format(dateFormat.parse(dateFormat.format(objJobclass.getClassificationDate()))));
            info.addResult("inpclassificationDate", clsDate);
          }
          info.addResult("inpehcmJobGroupId", objJobclass.getEhcmJobGroup().getId());
          info.addResult("inpvalue", objJobclass.getClassificationCode());
          // info.addResult("inpmcsletterNo", objJobclass.getMcsletterNo());
          if (objJobclass.getMcsletterDate() != null) {
            mcsDate = UtilityDAO.convertTohijriDate(yearFormat
                .format(dateFormat.parse(dateFormat.format(objJobclass.getMcsletterDate()))));
            info.addResult("inpmcsletterDate", mcsDate);
          }
          info.addResult("inpmainGroupCode", objJobclass.getMainGroupCode());
          info.addResult("inpmainGroupName", objJobclass.getMainGroupName());
          info.addResult("inpsubGroupCode", objJobclass.getSUBGroupCode());
          info.addResult("inpsubGroupName", objJobclass.getSUBGroupName());
          info.addResult("inpgroupSeqName", objJobclass.getGroupSeqName());
          info.addResult("inpgroupSeqCode", objJobclass.getGroupSeqCode());
        }
      }

      if (inpLastFieldChanged.equals("inpvalue")) {

        JobClassification objJobclass = OBDal.getInstance().get(JobClassification.class,
            inpJobClassificationId);
        if (!objJobclass.getClassificationCode().equals(inpclassificationCode)) {
          info.addResult("inpcode", "N");
          // set empty value for field(1) classification job
          info.addResult("JSEXECUTE",
              "grid.view.childTabSet.tabs[0].pane.formFields[1].setValue('')");
          // hide field(1) classification job
          info.addResult("JSEXECUTE", "grid.view.childTabSet.tabs[0].pane.formFields[1].hide()");
        } else {
          info.addResult("inpcode", "Y");
          // set empty value for field(1) classification job
          info.addResult("JSEXECUTE",
              "grid.view.childTabSet.tabs[0].pane.formFields[1].setValue('')");
          // show field(1) classification job
          info.addResult("JSEXECUTE", "grid.view.childTabSet.tabs[0].pane.formFields[1].show()");
        }
        // set blank value in classification job field in line while changing classification code
        // in header
        daoimpl.resetclassificationjob(inpUpjobClassificationId);
      }

    } catch (Exception e) {
      log4j.error("Exception in UpdateJobCode Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }
}
