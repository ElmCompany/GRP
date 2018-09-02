package sa.elm.ob.hcm.ad_callouts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.ad_callouts.dao.CommonDateActiveValidationDAOImpl;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * @author Priyanka Ranjan on 20/01/2017
 */

public class EhcmElementTypeDefinitionCallout extends SimpleCallout {

  /**
   * This Callout is for process of "Element Type Definition" Window
   */
  // this callout for common validations of startdate,enddate and isactive also
  private static final long serialVersionUID = 1L;
  public static final String Competency_Tab_ID = "1790D9F4A79843619CAE49DF5749DC3C";
  public static final String Job_Tab_ID = "87E6CB8ABF73484D91F475056D8EF55B";
  public static final String Job_Group_Tab_ID = "FB5826D76D044D0690C74B535B18D5A1";
  public static final String Org_Type_Tab_ID = "2C87866C939641A78DF634136D90C376";
  public static final String Region_Tab_ID = "136";
  public static final String Org_Category_Tab_ID = "FDA201F4EC0D4164ADF312E4A421AB2F";
  public static final String PayScale_Line_Tab_ID = "A2E3BB52F4A841F6A44AC64F63FE98A5";
  public static final String UpdateJobTitle_Jobs_Tab_ID = "E0BB450CBF6749CB9B4A9BC47CAF0D9D";
  public static final String Address_Style_Tab_ID = "C1598520C1574DB58B46A9C962186A84";
  public static final String Regions_Header_Tab_ID = "F23593FFDC374D43A7C72A52E9F00E09";
  public static final String Regions_Location_Tab_ID = "89F7313C2DB84AB6A9B337BA84E44031";
  public static final String Element_Group_Tab_ID = "FDD57840A6704F54A4DE24257844EC8C";
  public static final String Grade_Class_Tab_ID = "9E8CFCB847634AE9BE01B5E6BAAA3535";
  public static final String GradeRate_Value_Tab_ID = "12E39742E28644A8ADE39000529FF36A";
  public static final String GradeRate_Header_Tab_ID = "FF56167EEF194AD094C4F486085CE8D2";
  public static final String Grade_Header_Tab_ID = "3292A1A391D94BBCA3FA1BD486C2BC63";

  CommonDateActiveValidationDAOImpl dao = new CommonDateActiveValidationDAOImpl();

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    VariablesSecureApp vars = info.vars;

    String inpTabId = vars.getStringParameter("inpTabId");

    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpisactive = vars.getStringParameter("inpisactive");
    String inpenddate = vars.getStringParameter("inpenddate");
    String inpindirectresult = vars.getStringParameter("inpindirectresult");
    String inpmultientry = vars.getStringParameter("inpmultientry");
    String ehcmEndDate = vars.getStringParameter("inpemEhcmEnddate");
    String inpendDate = vars.getStringParameter("inpendDate");
    String endDate = UtilityDAO.convertToGregorian(inpenddate);

    try {
      // update the "End Date" by session date if we uncheck the enabled flag
      if (inpLastFieldChanged.equals("inpisactive")) {
        if (inpisactive.equals("N")) {
          String date = dao.getCurrentHijriDate();
          if (!date.isEmpty()) {
            if (Competency_Tab_ID.equals(inpTabId) || Job_Tab_ID.equals(inpTabId)
                || Job_Group_Tab_ID.equals(inpTabId)) {
              info.addResult("inpenddate", date);
            } else if (Region_Tab_ID.equals(inpTabId)) {
              info.addResult("inpemEhcmEnddate", date);
            } else {
              info.addResult("inpendDate", date);
            }
          }
        } else {
          if (Competency_Tab_ID.equals(inpTabId) || Job_Tab_ID.equals(inpTabId)
              || Job_Group_Tab_ID.equals(inpTabId)) {
            info.addResult("inpenddate", null);
          } else if (Region_Tab_ID.equals(inpTabId)) {
            info.addResult("inpemEhcmEnddate", null);
          } else {
            info.addResult("inpendDate", null);
          }
        }
      }

      if (Competency_Tab_ID.equals(inpTabId) || Job_Tab_ID.equals(inpTabId)
          || Job_Group_Tab_ID.equals(inpTabId) || Org_Type_Tab_ID.equals(inpTabId)
          || Org_Category_Tab_ID.equals(inpTabId) || PayScale_Line_Tab_ID.equals(inpTabId)
          || UpdateJobTitle_Jobs_Tab_ID.equals(inpTabId) || Address_Style_Tab_ID.equals(inpTabId)
          || Regions_Header_Tab_ID.equals(inpTabId) || Regions_Location_Tab_ID.equals(inpTabId)) {
        // while changing end date set active flag with 'Y' or 'N'
        if (inpLastFieldChanged.equals("inpenddate")) {
          if (inpenddate == null || inpenddate.equals("")) {
            info.addResult("inpisactive", true);
          } else {
            info.addResult("inpisactive", false);
          }
        }
      } else if (Element_Group_Tab_ID.equals(inpTabId)) {
        if (inpLastFieldChanged.equals("inpendDate")) {
          if (inpendDate == null || inpendDate.equals("")) {
            info.addResult("inpisactive", true);
          } else {
            info.addResult("inpisactive", false);
          }
        }
      } else if (Grade_Class_Tab_ID.equals(inpTabId) || GradeRate_Value_Tab_ID.equals(inpTabId)
          || GradeRate_Header_Tab_ID.equals(inpTabId) || Grade_Header_Tab_ID.equals(inpTabId)) {
        // while changing end date set active flag with 'Y' or 'N'
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        String currentDate = dateFormat.format(now);
        if (inpLastFieldChanged.equals("inpenddate")) {
          if (inpenddate == null || inpenddate.equals("")) {
            info.addResult("inpisactive", true);
          } else {
            info.addResult("inpisactive", false);
            // resetting the end date to current date if future date is chosen
            if (endDate != null && !endDate.equals("")) {
              Date endDate1 = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
              if (now.compareTo(endDate1) == -1) {
                info.addResult("inpenddate", UtilityDAO.convertTohijriDate(currentDate));
              }
            }
          }
        }
      }
      // Region table
      if (inpLastFieldChanged.equals("inpemEhcmEnddate")) {
        if (ehcmEndDate == null || ehcmEndDate.equals("")) {
          info.addResult("inpisactive", true);
        } else {
          info.addResult("inpisactive", false);
        }
      }
      // while changing the type then End Date should be blank
      if (inpLastFieldChanged.equals("inpprocngtype")) {
        info.addResult("inpendDate", null);
      }
      // when Indirect Result is in selected position Multiple Entries should also be in selected
      // position
      if (inpLastFieldChanged.equals("inpindirectresult")) {
        if (inpindirectresult.equals("Y")) {
          info.addResult("inpmultientry", "Y");
        } else {
          info.addResult("inpmultientry", "N");
        }
      }
      // when Multiple Entries is in deselected position Indirect Result should also be in
      // deselected
      // position
      if (inpLastFieldChanged.equals("inpmultientry")) {
        if (inpmultientry.equals("N")) {
          info.addResult("inpindirectresult", "N");
        }
      }

    } catch (Exception e) {
      log4j.error("Exception in Element Type Definition Action Type Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
