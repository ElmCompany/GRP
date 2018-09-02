package sa.elm.ob.hcm.ad_callouts;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.geography.Country;

import sa.elm.ob.hcm.EHCMDeflookupsTypeLn;
import sa.elm.ob.hcm.EHCMEmpScholarship;
import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EHCMScholarshipSummary;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.empScholarshipTraining.EmpScholarshipTrainingDAO;
import sa.elm.ob.hcm.ad_process.empScholarshipTraining.EmpScholarshipTrainingDAOImpl;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * this process handle the call out for Scholarship and Training when change the employee
 * 
 * @author divya- 14-02-2018
 *
 */
@SuppressWarnings("serial")
public class ScholarshipTrainingCallout extends SimpleCallout {
  private static final Logger log4j = Logger.getLogger(ScholarshipTrainingCallout.class);

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String scholarshipType = vars.getStringParameter("inpscholarshipType");
    String startDate = vars.getStringParameter("inpstartdate");
    String enddate = vars.getStringParameter("inpenddate");
    String noofDays = vars.getStringParameter("inpnoofdays");
    String clientId = vars.getStringParameter("inpadClientId");
    String inppaymentAmt = vars.getNumericParameter("inppaymentAmt");
    String inpadvancePercentage = vars.getNumericParameter("inpadvancePercentage");
    String inpadvanceAmount = vars.getNumericParameter("inpadvanceAmount");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");

    List<EHCMEmpScholarship> empScholarshipList = new ArrayList<EHCMEmpScholarship>();
    Date dateafter = null;
    BigDecimal paymentAmount = BigDecimal.ZERO;
    BigDecimal advPercentage = BigDecimal.ZERO;
    BigDecimal advAmount = new BigDecimal(0);

    if (StringUtils.isNotEmpty(inppaymentAmt))
      paymentAmount = new BigDecimal(inppaymentAmt);
    if (StringUtils.isNotEmpty(inpadvancePercentage))
      advPercentage = new BigDecimal(inpadvancePercentage);
    if (StringUtils.isNotEmpty(inpadvanceAmount))
      advAmount = new BigDecimal(inpadvanceAmount);
    Date startdate = null;
    String cancelDate = null;
    final String EMPLOYEESTATUS_ACTIVE = "ACT";
    final String EMPLOYEESTATUS_INACTIVE = "INACT";
    final String BUSINESS_MISSION_TYPE_INTERNAL = "INT";
    String inporiginalDecisionNo = vars.getStringParameter("inporiginalDecisionNo");
    String inpextendMissionDay = vars.getStringParameter("inpextendMissionDay");
    String inpextendStartdate = vars.getStringParameter("inpextendStartdate");
    String inpextendEnddate = vars.getStringParameter("inpextendEnddate");
    BigDecimal amt = new BigDecimal(0);
    EmpScholarshipTrainingDAO empScholarshipTrainingDAOImpl = new EmpScholarshipTrainingDAOImpl();
    try {
      EmploymentInfo empinfo = Utility.getActiveEmployInfo(employeeId);
      EHCMScholarshipSummary scholarshipSummary = empScholarshipTrainingDAOImpl
          .getActiveScholarshipSummary(employeeId, inporiginalDecisionNo);
      if (scholarshipSummary != null)
        startdate = scholarshipSummary.getStartDate();
      if (scholarshipSummary == null || (scholarshipSummary != null && scholarshipSummary
          .getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE))) {
        if (empinfo != null)
          startdate = empinfo.getStartDate();
      }

      if (lastfieldChanged.equals("inpehcmEmpPerinfoId") && StringUtils.isNotEmpty(employeeId)) {

        /* get Employee Details by using employeeId */
        EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
        info.addResult("inpempName", employee.getArabicfullname());
        EHCMEmployeeStatusV employeeStatus = OBDal.getInstance().get(EHCMEmployeeStatusV.class,
            employeeId);
        if (employeeStatus != null)
          info.addResult("inpempStatus", employeeStatus.getStatusvalue());
        else
          info.addResult("inpempStatus", "");
        info.addResult("inpehcmGradeclassId", employee.getGradeClass().getId());
        info.addResult("inpempType", employee.getEhcmActiontype().getPersonType());
        if (employee.getHiredate() != null) {
          info.addResult("inphireDate",
              (UtilityDAO.convertTohijriDate(dateFormat.format(employee.getHiredate()))));
        }
        if (empinfo != null) {
          info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
          if (empinfo.getPosition() != null && empinfo.getPosition().getSection() != null) {
            info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
          } else {
            info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
          }
          info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
          info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
          info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
          info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
          info.addResult("inpassignedDept", empinfo.getSECDeptName());
        }

        /*
         * if(empinfo.getStartDate() != null) { info.addResult("inpstartdate",
         * UtilityDAO.convertTohijriDate(dateFormat.format(startdate)));
         * info.addResult("inpenddate",
         * UtilityDAO.convertTohijriDate(dateFormat.format(startdate))); }
         */
        info.addResult("inpdecisionType", DecisionTypeConstants.DECISION_TYPE_CREATE);
        info.addResult("JSEXECUTE",
            "form.getFieldFromColumnName('Original_Decision_No').setValue('')");
        info.addResult("inpcanceldate", null);

      }
      if (lastfieldChanged.equals("inpscholarshipType")) {
        EHCMDeflookupsTypeLn scholarshiptypeObj = OBDal.getInstance()
            .get(EHCMDeflookupsTypeLn.class, scholarshipType);
        if (scholarshiptypeObj != null
            && scholarshiptypeObj.getSearchKey().equals(BUSINESS_MISSION_TYPE_INTERNAL)) {
          Country country = Utility.getSaudiArabiaCountryId(clientId);
          if (country != null) {
            info.addResult("inptoCountry", country.getId());
          }
        }
      }
      if (lastfieldChanged.equals("inporiginalDecisionNo")
          && !inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
          || lastfieldChanged.equals("inpdecisionType")) {
        // OBQuery<EHCMEmpScholarship> objEmpScholarshipQuery = OBDal.getInstance().createQuery(
        // EHCMEmpScholarship.class,
        // "as e where e.employee.id=:employeeId and e.enabled='Y' and e.issueDecision='Y' order by
        // e.creationDate desc");
        // objEmpScholarshipQuery.setNamedParameter("employeeId", employeeId);
        // objEmpScholarshipQuery.setMaxResult(1);
        // empScholarshipList = objEmpScholarshipQuery.list();
        if (inporiginalDecisionNo != null && !inporiginalDecisionNo.equals(null)
            && !inporiginalDecisionNo.equals("null") && inporiginalDecisionNo != "") {
          EHCMEmpScholarship empscholarShip = OBDal.getInstance().get(EHCMEmpScholarship.class,
              inporiginalDecisionNo);
          info.addResult("inporiginalDecisionNo", empscholarShip.getId());
          info.addResult("inpdecisionType", inpdecisionType);
          info.addResult("inpscholarshipType", empscholarShip.getScholarshipType().getId());
          info.addResult("inpscholarshipCategory", empscholarShip.getScholarshipCategory().getId());
          info.addResult("inpfromCountry", empscholarShip.getFromCountry().getId());
          info.addResult("inpfromCity", empscholarShip.getFromCity().getId());
          info.addResult("inptoCountry", empscholarShip.getCountry().getId());
          info.addResult("inptoCity", empscholarShip.getCity().getId());
          startDate = UtilityDAO
              .convertTohijriDate(dateFormat.format(empscholarShip.getStartDate()));
          info.addResult("inpstartdate", startDate);

          info.addResult("inpnoofdays", empscholarShip.getNoofdays());
          noofDays = empscholarShip.getNoofdays().toString();

          info.addResult("inpenddate",
              (UtilityDAO.convertTohijriDate(dateFormat.format(empscholarShip.getEndDate()))));
          info.addResult("inpisfoodprovided", empscholarShip.isFoodprovided());
          info.addResult("inpishousingprovided", empscholarShip.isHousingprovided());
          info.addResult("inpisroundtrip", empscholarShip.isRoundtrip());
          info.addResult("inpisticketprovided", empscholarShip.isTicketprovided());
          info.addResult("inptaskDescription", empscholarShip.getTaskDescription());
          info.addResult("inpcourseName", empscholarShip.getCourseName());
          info.addResult("inpcourseLocation", empscholarShip.getCourseLocation());
          if (empscholarShip.getCourseStartdate() != null)
            info.addResult("inpcourseStartdate", (UtilityDAO
                .convertTohijriDate(dateFormat.format(empscholarShip.getCourseStartdate()))));
          if (empscholarShip.getCourseEnddate() != null)
            info.addResult("inpcourseEnddate", (UtilityDAO
                .convertTohijriDate(dateFormat.format(empscholarShip.getCourseEnddate()))));
        }
        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Original_Decision_No').setValue('')");
        }

        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {

          if (scholarshipSummary != null) {
            dateafter = new Date(scholarshipSummary.getEndDate().getTime() + 1 * 24 * 3600 * 1000);
            startDate = UtilityDAO.convertTohijriDate(dateFormat.format(dateafter));
            // lastfieldChanged = "inpstartdate";
            info.addResult("inpextendStartdate", startDate);
          }
        }
        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
          cancelDate = UtilityDAO.convertTohijriDate(dateFormat.format(new Date()));
          info.addResult("inpcanceldate", cancelDate);

        }
        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) { // inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)
          lastfieldChanged = "inpstartdate";
        }
        if (!inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_SCHOLARSHIP_PAYMENT)) {
          info.addResult("inppaymentAmt", "");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('ehcm_payrolldef_period_id').setValue('')");
          info.addResult("inpadvancePercentage", "");
          info.addResult("inpadvanceAmount", "");
        }
      }

      if (lastfieldChanged.equals("inpstartdate") || lastfieldChanged.equals("inpnoofdays")) {
        noofDays = noofDays.replaceAll(",", "");
        if (Integer.parseInt(noofDays) > 0) {
          Date endDate = Utility.calculateDateUsingDays(clientId, noofDays, startDate);
          info.addResult("inpenddate", (UtilityDAO.convertTohijriDate(dateFormat.format(endDate))));
        } else {
          info.addResult("inpenddate", null);
        }
      }
      if (lastfieldChanged.equals("inpextendMissionDay")) {
        inpextendMissionDay = inpextendMissionDay.replaceAll(",", "");
        if (StringUtils.isNotEmpty(inpextendMissionDay)) {
          if (Integer.parseInt(inpextendMissionDay) > 0) {
            if (StringUtils.isNotEmpty(inpextendStartdate)) {
              Date endDate = Utility.calculateDateUsingDays(clientId, inpextendMissionDay,
                  inpextendStartdate);
              enddate = UtilityDAO.convertTohijriDate(dateFormat.format(endDate));
              info.addResult("inpextendEnddate", enddate);
              info.addResult("inpenddate", enddate);
              lastfieldChanged = "inpenddate";
            } else {
              info.addResult("inpextendEnddate", null);
            }
          } else {
            info.addResult("inpextendEnddate", null);
          }
        }

      }
      // newwwww
      if (lastfieldChanged.equals("inppaymentAmt")) {
        if ((paymentAmount.compareTo(BigDecimal.ZERO) > 0)
            && advAmount.compareTo(BigDecimal.ZERO) > 0) {
          info.addResult("inpnetAmt", paymentAmount.subtract(advAmount));
        } else {
          info.addResult("inpadvancePercentage", new BigDecimal(0));
          info.addResult("inpadvanceAmount", new BigDecimal(0));
          if (paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            info.addResult("inpnetAmt", paymentAmount);
          } else {
            info.addResult("inpnetAmt", new BigDecimal(0));
          }
        }
      }

      if (lastfieldChanged.equals("inpadvancePercentage")
          || lastfieldChanged.equals("inppaymentAmt")) {
        if (advPercentage.compareTo(new BigDecimal(100)) < 1) {
          if ((paymentAmount.compareTo(BigDecimal.ZERO) > 0)
              && advPercentage.compareTo(BigDecimal.ZERO) > 0) {
            advAmount = paymentAmount.multiply(advPercentage.divide(new BigDecimal(100)));
            info.addResult("inpadvanceAmount", advAmount);
            info.addResult("inpnetAmt", paymentAmount.subtract(advAmount));
          } else {
            info.addResult("inpadvancePercentage", new BigDecimal(0));
            info.addResult("inpadvanceAmount", new BigDecimal(0));
            if (paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
              info.addResult("inpnetAmt", paymentAmount);
            } else {
              info.addResult("inpnetAmt", new BigDecimal(0));
            }
          }
        } else {
          info.addResult("inpadvancePercentage", new BigDecimal(0));
          info.addResult("inpadvanceAmount", new BigDecimal(0));
          if (paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            info.addResult("inpnetAmt", paymentAmount);
          } else {
            info.addResult("inpnetAmt", new BigDecimal(0));
          }
          info.addResult("ERROR", OBMessageUtils.messageBD("EHCM_BM_AdvancePercent_Invalid"));
        }
      }

      if (lastfieldChanged.equals("inpadvanceAmount")) {
        info.addResult("inpadvancePercentage", null);
        if (advAmount.compareTo(paymentAmount) < 1) {
          if ((paymentAmount.compareTo(BigDecimal.ZERO) > 0)
              && advAmount.compareTo(BigDecimal.ZERO) > 0) {
            info.addResult("inpnetAmt", paymentAmount.subtract(advAmount));
          } else {
            info.addResult("inpadvanceAmount", new BigDecimal(0));
            if (paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
              info.addResult("inpnetAmt", paymentAmount);
            } else {
              info.addResult("inpnetAmt", new BigDecimal(0));
            }
          }
        } else {
          info.addResult("inpadvanceAmount", new BigDecimal(0));
          if (paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            info.addResult("inpnetAmt", paymentAmount);
          } else {
            info.addResult("inpnetAmt", new BigDecimal(0));
          }
          info.addResult("ERROR", OBMessageUtils.messageBD("EHCM_BM_AdvanceAmt_Invalid"));
        }
      }

      if (lastfieldChanged.equals("inpenddate") || lastfieldChanged.equals("inpenddate")) {
        int noofdays = Utility.calculatetheDays(startDate, enddate);
        info.addResult("inpnoofdays", noofdays);
      }
      if (lastfieldChanged.equals("inpextendEnddate")) {
        if (StringUtils.isNotEmpty(inpextendStartdate)) {
          int noofdays = Utility.calculatetheDays(inpextendStartdate, inpextendEnddate);
          info.addResult("inpextendMissionDay", noofdays);
          info.addResult("inpenddate", inpextendEnddate);
        } else {
          info.addResult("inpextendMissionDay", "");
        }
        int noofday = Utility.calculatetheDays(startDate, inpextendEnddate);
        info.addResult("inpmissionDays", noofday);

      }
    } catch (Exception e) {
      e.printStackTrace();
      log4j.error("Exception in ScholarshipTrainingCallout  :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
