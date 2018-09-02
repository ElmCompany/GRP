package sa.elm.ob.hcm.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EhcmPosition;

@SuppressWarnings("serial")
public class CancelPositionCallout extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;

    String inpehcmGradeId = vars.getStringParameter("inpehcmGradeId");
    String inpjobNo = vars.getStringParameter("inpjobNo");
    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    log4j.debug("lastfield:" + inpLastFieldChanged);
    try {

      /*
       * if(inpLastFieldChanged.equals("inpehcmPositionId")) { EhcmPosition position =
       * OBDal.getInstance().get(EhcmPosition.class, inpjobPositon); if(position.getMCSLetterDate()
       * != null) {
       * 
       * String query = " select eut_convert_to_hijri_timestamp('" +
       * dateFormat.format(position.getMCSLetterDate()) + "')";
       * 
       * st = conn.prepareStatement(query); rs = st.executeQuery(); if(rs.next())
       * info.addResult("inpmcsLetterDate", rs.getString("eut_convert_to_hijri_timestamp")); }
       * info.addResult("inpmcsLetterNo", position.getMCSLetterNo());
       * info.addResult("inpdepartmentId", position.getDepartment().getId());
       * info.addResult("inpsectionId", position.getSection().getId());
       * info.addResult("inpdeptname", position.getSectionname()); info.addResult("inpsectionname",
       * position.getDeptname()); info.addResult("inpehcmJobsId", position.getEhcmJobs().getId());
       * info.addResult("inpehcmGradeId", position.getGrade().getId()); info.addResult("inpjobName",
       * position.getEhcmJobs().getId()); info.addResult("inpjobNo", position.getJOBNo());
       * info.addResult("inpdecisionNo", position.getDecisionNo());
       * 
       * }
       */
      if (inpLastFieldChanged.equals("inpjobNo")) {
        OBQuery<EhcmPosition> position = OBDal.getInstance().createQuery(EhcmPosition.class,
            " id='" + inpjobNo + "' and grade.id='" + inpehcmGradeId + "'");
        log4j.debug("posi:" + position.getWhereAndOrderBy());
        log4j.debug("list:" + position.list().size());
        if (position.list().size() > 0) {
          EhcmPosition pos = position.list().get(0);
          log4j.debug("getid:" + pos.getId());
          info.addResult("inpehcmPositionId", pos.getId());

          /*
           * if(pos.getMCSLetterDate() != null) {
           * 
           * String query = " select eut_convert_to_hijri_timestamp('" +
           * dateFormat.format(pos.getMCSLetterDate()) + "')";
           * 
           * st = conn.prepareStatement(query); rs = st.executeQuery(); if(rs.next())
           * info.addResult("inpmcsLetterDate", rs.getString("eut_convert_to_hijri_timestamp")); }
           * info.addResult("inpmcsLetterNo", pos.getMCSLetterNo());
           */

          info.addResult("inpdepartmentId", pos.getDepartment().getId());
          if (pos.getSection() != null) {
            info.addResult("inpsectionId", pos.getSection().getId());
            info.addResult("inpsectionname", pos.getSectionname());
          } else {
            info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
            info.addResult("inpsectionname", null);
          }

          info.addResult("inpdeptname", pos.getDeptname());
          info.addResult("inpehcmJobsId", pos.getEhcmJobs().getId());
          info.addResult("inpjobName", pos.getEhcmJobs().getId());
          // info.addResult("inpdecisionNo", pos.getDecisionNo());

        } else {
          info.addResult("inpehcmPositionId", null);
          info.addResult("inpdepartmentId", null);
          info.addResult("inpsectionId", null);
          info.addResult("inpdeptname", null);
          info.addResult("inpsectionname", null);
          info.addResult("inpehcmJobsId", null);
          info.addResult("inpjobName", null);
          // info.addResult("inpdecisionNo", null);
        }

      }
      if (inpLastFieldChanged.equals("inpehcmGradeId")) {
        OBQuery<EhcmPosition> position = OBDal.getInstance().createQuery(EhcmPosition.class,
            " id='" + inpjobNo + "' and grade.id='" + inpehcmGradeId + "'");
        log4j.debug("posi:" + position.getWhereAndOrderBy());
        log4j.debug("list:" + position.list().size());
        if (position.list().size() > 0) {
          EhcmPosition pos = position.list().get(0);
          log4j.debug("getid:" + pos.getId());
          info.addResult("inpehcmPositionId", pos.getId());

          /*
           * if(pos.getMCSLetterDate() != null) {
           * 
           * String query = " select eut_convert_to_hijri_timestamp('" +
           * dateFormat.format(pos.getMCSLetterDate()) + "')";
           * 
           * st = conn.prepareStatement(query); rs = st.executeQuery(); if(rs.next())
           * info.addResult("inpmcsLetterDate", rs.getString("eut_convert_to_hijri_timestamp")); }
           * info.addResult("inpmcsLetterNo", pos.getMCSLetterNo());
           */

          info.addResult("inpdepartmentId", pos.getDepartment().getId());
          if (pos.getSection() != null) {
            info.addResult("inpsectionId", pos.getSection().getId());
            info.addResult("inpsectionname", pos.getSectionname());
          } else {
            info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
            info.addResult("inpsectionname", null);
          }
          info.addResult("inpdeptname", pos.getDeptname());
          info.addResult("inpehcmJobsId", pos.getEhcmJobs().getId());
          info.addResult("inpjobName", pos.getEhcmJobs().getId());
          // info.addResult("inpdecisionNo", pos.getDecisionNo());
        } else {
          info.addResult("inpehcmPositionId", null);
          info.addResult("inpdepartmentId", null);
          info.addResult("inpsectionId", null);
          info.addResult("inpdeptname", null);
          info.addResult("inpsectionname", null);
          info.addResult("inpehcmJobsId", null);
          info.addResult("inpjobName", null);
          // info.addResult("inpdecisionNo", null);
        }

      }
    } catch (Exception e) {
      log4j.error("Exception in Cancel Position Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }
}
