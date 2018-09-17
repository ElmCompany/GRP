package sa.elm.ob.hcm.ad_callouts;

import java.math.BigDecimal;
import java.text.DateFormat;
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
import org.openbravo.model.common.geography.City;
import org.openbravo.model.common.geography.Country;

import sa.elm.ob.hcm.EHCMBusMissionSummary;
import sa.elm.ob.hcm.EHCMDeflookupsTypeLn;
import sa.elm.ob.hcm.EHCMEmpBusinessMission;
import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EHCMMisCatPeriod;
import sa.elm.ob.hcm.EHCMMissionCategory;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_callouts.common.UpdateEmpDetailsInCallouts;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.empBusinessMission.EmpBusinessMissionDAOImpl;
import sa.elm.ob.hcm.event.dao.MissionCategoryDAOImpl;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * this process handle the call out for Business Mission when change the employee
 * 
 * @author divya- 14-02-2018
 *
 */
@SuppressWarnings("serial")
public class BusinessMissionCallout extends SimpleCallout {
  private static final Logger log4j = Logger.getLogger(BusinessMissionCallout.class);

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String missionType = vars.getStringParameter("inpmissionType");
    String startDate = vars.getStringParameter("inpstartdate");
    String enddate = vars.getStringParameter("inpenddate");
    String missionDays = vars.getStringParameter("inpmissionDays");
    String clientId = vars.getStringParameter("inpadClientId");
    String inpmissionCategory = vars.getStringParameter("inpmissionCategory");
    String inpbmPaymentAmt = vars.getNumericParameter("inpbmPaymentAmt");
    String inpadvancePercentage = vars.getNumericParameter("inpadvancePercentage");
    String inpadvanceAmount = vars.getNumericParameter("inpadvanceAmount");
    String inpfromCity = vars.getStringParameter("inpfromCity");
    String inptoCity = vars.getStringParameter("inptoCity");
    DateFormat dateFormat = sa.elm.ob.utility.util.Utility.YearFormat;
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String inporiginalDecisionNo = vars.getStringParameter("inporiginalDecisionNo");
    String inpextendMissionDay = vars.getStringParameter("inpextendMissionDay");
    String inpextendStartdate = vars.getStringParameter("inpextendStartdate");
    String inpextendEnddate = vars.getStringParameter("inpextendEnddate");
    String fromCityId = vars.getStringParameter("inpfromCity");
    String toCity = vars.getStringParameter("inptoCity");
    List<EHCMEmpBusinessMission> empBMList = new ArrayList<EHCMEmpBusinessMission>();
    EHCMEmpBusinessMission originalDecisionBusMission = null;
    Date dateafter = null;
    BigDecimal paymentAmount = BigDecimal.ZERO;
    BigDecimal advPercentage = BigDecimal.ZERO;
    BigDecimal advAmount = new BigDecimal(0);
    log4j.info("Business Mission lastfieldChanged : " + lastfieldChanged);
    if (StringUtils.isNotEmpty(inpbmPaymentAmt))
      paymentAmount = new BigDecimal(inpbmPaymentAmt);
    if (StringUtils.isNotEmpty(inpadvancePercentage))
      advPercentage = new BigDecimal(inpadvancePercentage);
    if (StringUtils.isNotEmpty(inpadvanceAmount))
      advAmount = new BigDecimal(inpadvanceAmount);
    String cancelDate = null;
    Boolean decisionTypeFlag = false;
    final String EMPLOYEESTATUS_ACTIVE = "ACT";
    final String EMPLOYEESTATUS_INACTIVE = "INACT";
    final String BUSINESS_MISSION_TYPE_INTERNAL = "INT";
    MissionCategoryDAOImpl missionCategoryDAOImpl = new MissionCategoryDAOImpl();
    int originalDecisionDays = 0;
    EHCMMisCatPeriod originalMisCatPrd = null;
    BigDecimal amt = new BigDecimal(0);

    try {
      UpdateEmpDetailsInCallouts callouts = new UpdateEmpDetailsInCallouts();

      EmploymentInfo empinfo = Utility.getActiveEmployInfo(employeeId);

      EHCMBusMissionSummary busMissionSummary = EmpBusinessMissionDAOImpl
          .getActiveBusMissionSummary(employeeId, inporiginalDecisionNo);

      if (lastfieldChanged.equals("inpehcmEmpPerinfoId")) {
        if (StringUtils.isNotEmpty(employeeId)) {
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
            info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());
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
        } else {
          callouts.SetEmpDetailsNull(info);
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Ehcm_Payscaleline_ID').setValue('')");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Assigned_Dept').setValue('')");
        }
      }
      if (lastfieldChanged.equals("inpmissionType")) {
        EHCMDeflookupsTypeLn scholarshiptypeObj = OBDal.getInstance()
            .get(EHCMDeflookupsTypeLn.class, missionType);
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
        // OBQuery<EHCMEmpBusinessMission> objEmpBMQuery = OBDal.getInstance().createQuery(
        // EHCMEmpBusinessMission.class,
        // "as e where e.employee.id=:employeeId and e.enabled='Y' and e.issueDecision='Y' order by
        // e.creationDate desc");
        // objEmpBMQuery.setNamedParameter("employeeId", employeeId);
        // objEmpBMQuery.setMaxResult(1);
        // empBMList = objEmpBMQuery.list();
        if (inporiginalDecisionNo != null && !inporiginalDecisionNo.equals(null)
            && !inporiginalDecisionNo.equals("null") && inporiginalDecisionNo != "") {
          EHCMEmpBusinessMission empBusinessMission = OBDal.getInstance()
              .get(EHCMEmpBusinessMission.class, inporiginalDecisionNo);
          originalDecisionBusMission = empBusinessMission;
          inporiginalDecisionNo = empBusinessMission.getId();
          // info.addResult("inporiginalDecisionNo", empBusinessMission.getId());
          info.addResult("inpdecisionType", inpdecisionType);
          info.addResult("inpmissionType", empBusinessMission.getMissionType().getId());
          info.addResult("inpmissionCategory", empBusinessMission.getMissionCategory().getId());
          inpmissionCategory = empBusinessMission.getMissionCategory().getId();
          info.addResult("inpfromCountry", empBusinessMission.getFromCountry().getId());
          info.addResult("inpfromCity", empBusinessMission.getFromCity().getId());
          info.addResult("inpnoofdaysBefore",
              empBusinessMission.getFromCity().getEHCMNoDaysBefore());
          info.addResult("inptoCountry", empBusinessMission.getToCountry().getId());
          info.addResult("inptoCity", empBusinessMission.getToCity().getId());
          info.addResult("inpnoofdaysAfter", empBusinessMission.getToCity().getEHCMNoDaysAfter());
          startDate = UtilityDAO
              .convertTohijriDate(dateFormat.format(empBusinessMission.getStartDate()));
          info.addResult("inpstartdate", startDate);
          info.addResult("inpmissionDays", empBusinessMission.getMissionDays());

          missionDays = empBusinessMission.getMissionDays().toString();

          info.addResult("inpenddate",
              (UtilityDAO.convertTohijriDate(dateFormat.format(empBusinessMission.getEndDate()))));
          info.addResult("inpisfoodprovided", empBusinessMission.isFoodProvided());
          info.addResult("inpishousingprovided", empBusinessMission.isHousingProvided());
          info.addResult("inpisroundtrip", empBusinessMission.isRoundTrip());
          info.addResult("inpisticketprovided", empBusinessMission.isTicketsProvided());
          info.addResult("inptaskDescription", empBusinessMission.getTaskDescription());
          decisionTypeFlag = true;
        }
        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
          lastfieldChanged = "inpmissionCategory";
        }
        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Original_Decision_No').setValue('')");
        }
        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {

          if (busMissionSummary != null) {
            dateafter = new Date(busMissionSummary.getEndDate().getTime() + 1 * 24 * 3600 * 1000);
            startDate = UtilityDAO.convertTohijriDate(dateFormat.format(dateafter));
            // lastfieldChanged = "inpstartdate";
            info.addResult("inpextendStartdate", startDate);
          }
        }
        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
          cancelDate = UtilityDAO.convertTohijriDate(dateFormat.format(new Date()));
          info.addResult("inpcanceldate", cancelDate);
        }
        if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {// ||
                                                                                 // inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)
          lastfieldChanged = "inpstartdate";
        }
        if (!inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_BUSINESSMISSION_PAYMENT)) {
          info.addResult("inpbmPaymentAmt", null);
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('ehcm_payrolldef_period_id').setValue('')");
          info.addResult("inpadvancePercentage", "");
          info.addResult("inpadvanceAmount", "");

        }
        if (!inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
          info.addResult("inpextendStartdate", null);
          info.addResult("inpextendEnddate", null);

        }
      }

      if (lastfieldChanged.equals("inpstartdate") || lastfieldChanged.equals("inpmissionDays")) {
        missionDays = missionDays.replaceAll(",", "");
        if (Integer.parseInt(missionDays) > 0) {
          Date endDate = Utility.calculateDateUsingDays(clientId, missionDays, startDate);
          enddate = UtilityDAO.convertTohijriDate(dateFormat.format(endDate));
          info.addResult("inpenddate", enddate);

        } else {
          info.addResult("inpenddate", null);
        }
        if (decisionTypeFlag) {
          lastfieldChanged = "inpmissionCategory";
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

      if (lastfieldChanged.equals("inpbmPaymentAmt")) {
        if ((paymentAmount.compareTo(BigDecimal.ZERO) > 0)
            && advAmount.compareTo(BigDecimal.ZERO) > 0) {
          info.addResult("inpbmNetAmt", paymentAmount.subtract(advAmount));
        } else {
          info.addResult("inpadvancePercentage", new BigDecimal(0));
          info.addResult("inpadvanceAmount", new BigDecimal(0));
          if (paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            info.addResult("inpbmNetAmt", paymentAmount);
          } else {
            info.addResult("inpbmNetAmt", new BigDecimal(0));
          }
        }
      }

      if (lastfieldChanged.equals("inpadvancePercentage")) {
        if (advPercentage.compareTo(new BigDecimal(100)) < 1) {
          if ((paymentAmount.compareTo(BigDecimal.ZERO) > 0)
              && advPercentage.compareTo(BigDecimal.ZERO) > 0) {
            advAmount = paymentAmount.multiply(advPercentage.divide(new BigDecimal(100)));
            info.addResult("inpadvanceAmount", advAmount);
            info.addResult("inpbmNetAmt", paymentAmount.subtract(advAmount));
          } else {
            info.addResult("inpadvancePercentage", new BigDecimal(0));
            info.addResult("inpadvanceAmount", new BigDecimal(0));
            if (paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
              info.addResult("inpbmNetAmt", paymentAmount);
            } else {
              info.addResult("inpbmNetAmt", new BigDecimal(0));
            }
          }
        } else {
          info.addResult("inpadvancePercentage", new BigDecimal(0));
          info.addResult("inpadvanceAmount", new BigDecimal(0));
          if (paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            info.addResult("inpbmNetAmt", paymentAmount);
          } else {
            info.addResult("inpbmNetAmt", new BigDecimal(0));
          }
          info.addResult("ERROR", OBMessageUtils.messageBD("EHCM_BM_AdvancePercent_Invalid"));
        }
      }

      if (lastfieldChanged.equals("inpadvanceAmount")) {
        info.addResult("inpadvancePercentage", null);
        if (advAmount.compareTo(paymentAmount) < 1) {
          if ((paymentAmount.compareTo(BigDecimal.ZERO) > 0)
              && advAmount.compareTo(BigDecimal.ZERO) > 0) {
            info.addResult("inpbmNetAmt", paymentAmount.subtract(advAmount));
          } else {
            info.addResult("inpadvanceAmount", new BigDecimal(0));
            if (paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
              info.addResult("inpbmNetAmt", paymentAmount);
            } else {
              info.addResult("inpbmNetAmt", new BigDecimal(0));
            }
          }
        } else {
          info.addResult("inpadvanceAmount", new BigDecimal(0));
          if (paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            info.addResult("inpbmNetAmt", paymentAmount);
          } else {
            info.addResult("inpbmNetAmt", new BigDecimal(0));
          }
          info.addResult("ERROR", OBMessageUtils.messageBD("EHCM_BM_AdvanceAmt_Invalid"));
        }
      }

      if (lastfieldChanged.equals("inpenddate") || lastfieldChanged.equals("inpenddate")) {
        int noofdays = Utility.calculatetheDays(startDate, enddate);
        info.addResult("inpmissionDays", noofdays);
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

      if (lastfieldChanged.equals("inpfromCity") && StringUtils.isNotEmpty(fromCityId)) {
        City city = OBDal.getInstance().get(City.class, inpfromCity);
        info.addResult("inpnoofdaysBefore", city.getEHCMNoDaysBefore());
      }
      if (lastfieldChanged.equals("inptoCity") && StringUtils.isNotEmpty(toCity)) {
        City city = OBDal.getInstance().get(City.class, inptoCity);
        info.addResult("inpnoofdaysAfter", city.getEHCMNoDaysAfter());
      }
      if (lastfieldChanged.equals("inpmissionCategory")
          || lastfieldChanged.equals("inporiginalDecisionNo")
          || lastfieldChanged.equals("inpehcmEmpPerinfoId")
          || lastfieldChanged.equals("inpmissionDays") || lastfieldChanged.equals("inpdecisionType")
          || lastfieldChanged.equals("inpstartdate")
          || lastfieldChanged.equals("inpmissionCategory")) {

        if (StringUtils.isNotEmpty(inpmissionCategory)) {
          EHCMMissionCategory missCategory = OBDal.getInstance().get(EHCMMissionCategory.class,
              inpmissionCategory);
          Long missionBalanceDays = sa.elm.ob.hcm.util.UtilityDAO.getMissionBalanceDays(clientId,
              missCategory, UtilityDAO.convertToGregorian_tochar(startDate),
              UtilityDAO.convertToGregorian_tochar(enddate), employeeId);
          // get the mission balance days on update decision
          if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
            Long missionBalanceDaysonUpdate = sa.elm.ob.hcm.util.UtilityDAO
                .getMissionBalanceDaysOnUpdate(clientId, missCategory,
                    UtilityDAO.convertToGregorian_tochar(startDate),
                    UtilityDAO.convertToGregorian_tochar(enddate), employeeId,
                    inporiginalDecisionNo);
            info.addResult("inpmissionBalance", missionBalanceDaysonUpdate);
          } else {
            info.addResult("inpmissionBalance", missionBalanceDays); // mission balance days
          }

        } else
          info.addResult("inpmissionBalance", 0);
      }

    } catch (

    Exception e) {
      e.printStackTrace();
      log4j.error("Exception in BusinessMissionCallout  :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
