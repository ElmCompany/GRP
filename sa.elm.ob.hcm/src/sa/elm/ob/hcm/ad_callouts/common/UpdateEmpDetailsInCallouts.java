package sa.elm.ob.hcm.ad_callouts.common;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

public class UpdateEmpDetailsInCallouts extends SimpleCallout {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public CalloutInfo SetEmpDetailsNull(CalloutInfo info) {
    try {
      info.addResult("inpempName", "");
      info.addResult("inpempStatus", "");
      info.addResult("inpempType", "");
      info.addResult("inphireDate", "");
      info.addResult("inpehcmGradeclassId", null);
      info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Department_ID').setValue('')");
      info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
      info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Ehcm_Grade_ID').setValue('')");
      info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Ehcm_Position_ID').setValue('')");
      info.addResult("inpjobTitle", "");
      info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Employmentgrade').setValue('')");
      /*
       * info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Assigned_Dept').setValue('')");
       */
      // info.addResult("JSEXECUTE",
      // "form.getFieldFromColumnName('Ehcm_Payscaleline_ID').setValue('')");
      info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Ehcm_Gradeclass_ID').setValue('')");
      /*
       * info.addResult("JSEXECUTE",
       * "form.getFieldFromColumnName('Assigned_Department_ID').setValue('')");
       */
      info.addResult("inpemployeeName", "");
      info.addResult("inpemployeeStatus", "");
      info.addResult("inpemployeeType", "");
      info.addResult("inpauthorisedPerson", "");
      info.addResult("inpauthorisesPersonJob", "");
      info.addResult("inpauthorizePersonTitle", "");
      info.addResult("inpehcmAuthorizePersonId", "");
    } catch (Exception e) {
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

    return info;
  }

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub

  }
}
