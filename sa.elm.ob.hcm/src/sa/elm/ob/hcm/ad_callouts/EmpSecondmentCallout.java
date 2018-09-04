package sa.elm.ob.hcm.ad_callouts;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import sa.elm.ob.hcm.EHCMEmpSecondment;
import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.utility.util.UtilityDAO;

public class EmpSecondmentCallout extends SimpleCallout {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String inpnewDepartmentId = vars.getStringParameter("inpnewDepartmentId");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inppromotionType = vars.getStringParameter("inppromotionType");
    String inpperiodType = vars.getStringParameter("inpperiodType");
    String inpperiod = vars.getNumericParameter("inpperiod");
    String inpstartdate = vars.getStringParameter("inpstartdate");
    String inpenddate = vars.getStringParameter("inpenddate");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String secondmentId = vars.getStringParameter("inpehcmEmpSecondmentId");
    String inpclient = vars.getStringParameter("inpadClientId");
    String perioddayenddate = "", employmentInfoId = "";

    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement st = null;
    ResultSet rs = null;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    log4j.debug("lastfieldChanged:" + lastfieldChanged);
    try {

      /* get secondment information */
      EHCMEmpSecondment secondinfo = OBDal.getInstance().get(EHCMEmpSecondment.class, secondmentId);
      /*
       * get Latest active EmploymentInfo by using EmployeeId and set the value based on Employment
       * Info
       */
      EmploymentInfo empinfo = null;
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id='" + employeeId + "' and enabled='Y' order by creationDate desc ");
      log4j.debug("employeeId:" + employeeId);
      log4j.debug("positiontype:" + empInfo.list().size());
      if (empInfo.list().size() > 0) {
        empinfo = empInfo.list().get(0);
      }
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
            String query = " select eut_convert_to_hijri_timestamp('"
                + dateFormat.format(employee.getHiredate()) + "')";

            st = conn.prepareStatement(query);
            rs = st.executeQuery();
            if (rs.next())
              info.addResult("inphireDate", rs.getString("eut_convert_to_hijri_timestamp"));

          }
          if (empinfo != null) {
            employmentInfoId = empinfo.getId();
            info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
            if (empinfo.getPosition().getSection() != null) {
              info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
            } else {
              info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
            }
            info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
            info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
            info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
            info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
            info.addResult("inpassignedDept", empinfo.getSECDeptName());
            info.addResult("inpehcmGradestepsId",
                empinfo.getEhcmPayscale().getEhcmGradesteps().getId());
            info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());

            if (empinfo.getStartDate() != null) {
              String query = " select eut_convert_to_hijri('"
                  + dateFormat.format(empinfo.getStartDate()) + "')";

              st = conn.prepareStatement(query);
              rs = st.executeQuery();
              if (rs.next()) {
                info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri"));
                inpstartdate = rs.getString("eut_convert_to_hijri");
                log4j.debug("inpstartdate:" + rs.getString("eut_convert_to_hijri"));
              }
            }
            inpdecisionType = "CR";
            info.addResult("inpdecisionType", inpdecisionType);
            info.addResult("inporiginalDecisionsNo", "");
            info.addResult("inpgovAgency", "");
          }
        } else {
          info.addResult("inpempName", "");
          info.addResult("inpempStatus", "");
          info.addResult("inpempType", "");
          info.addResult("inphireDate", "");
          info.addResult("inpehcmGradeclassId", "");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Department_ID').setValue('')");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Ehcm_Grade_ID').setValue('')");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Ehcm_Position_ID').setValue('')");
          info.addResult("inpjobTitle", "");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Employmentgrade').setValue('')");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Assigned_Dept').setValue('')");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Ehcm_Payscaleline_ID').setValue('')");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Ehcm_Gradeclass_ID').setValue('')");
        }
      }
      if (lastfieldChanged.equals("inpdecisionType")) {
        if (!inpdecisionType.equals("CR")) {
          if (empinfo.getChangereason().equals("SEC") || empinfo.getChangereason().equals("EXSEC")
              || empinfo.getChangereason().equals("COSEC")) {
            OBQuery<EHCMEmpSecondment> objEmpQuery = OBDal.getInstance()
                .createQuery(EHCMEmpSecondment.class, "as e where e.ehcmEmpPerinfo.id='"
                    + employeeId
                    + "' and e.enabled='Y' and e.issueDecision='Y'  order by e.creationDate desc");
            objEmpQuery.setMaxResult(1);
            if (objEmpQuery.list().size() > 0) {
              EHCMEmpSecondment secondment = objEmpQuery.list().get(0);
              log4j.debug("getDecisionNo():" + secondment.getId());
              info.addResult("inporiginalDecisionsNo", secondment.getId());
              info.addResult("inppaymentType", secondment.getPaymentType().getId());

            }
          }
          if (!inpdecisionType.equals("EX")) {
            if (empinfo.getEhcmEmpSecondment() != null) {
              log4j.debug("getPeriodType():" + empinfo.getEhcmEmpSecondment().getPeriodType());
              log4j.debug("getPeriod():" + empinfo.getEhcmEmpSecondment().getPeriod());
              info.addResult("inpperiodType", empinfo.getEhcmEmpSecondment().getPeriodType());
              info.addResult("inpperiod", empinfo.getEhcmEmpSecondment().getPeriod());
              // addded by gopal
              // task 4011
              if (empinfo.getEhcmEmpSecondment().getDecisionType().equals("CO")) {
                String query = " select eut_convert_to_hijri('"
                    + dateFormat.format(empinfo.getEndDate()) + "')";

                st = conn.prepareStatement(query);
                rs = st.executeQuery();
                if (rs.next()) {
                  inpstartdate = rs.getString("eut_convert_to_hijri");
                  info.addResult("inpenddate", rs.getString("eut_convert_to_hijri"));
                }
              } else {
                String query = " select eut_convert_to_hijri('"
                    + dateFormat.format(empinfo.getEhcmEmpSecondment().getEndDate()) + "')";

                st = conn.prepareStatement(query);
                rs = st.executeQuery();
                if (rs.next()) {
                  inpstartdate = rs.getString("eut_convert_to_hijri");
                  info.addResult("inpenddate", rs.getString("eut_convert_to_hijri"));
                }
              }

            }
          }
        } else {
          info.addResult("inporiginalDecisionsNo", null);
          // info.addResult("inppaymentType", "BASIC");
          /*
           * info.addResult("JSEXECUTE",
           * "form.getFieldFromColumnName('Original_Decisions_No').setValue('')");
           */
        }
        if (!inpdecisionType.equals("EX")) {
          if (empinfo.getStartDate() != null) {
            String query = " select eut_convert_to_hijri('"
                + dateFormat.format(empinfo.getStartDate()) + "')";

            st = conn.prepareStatement(query);
            rs = st.executeQuery();
            if (rs.next()) {
              inpstartdate = rs.getString("eut_convert_to_hijri");
              info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri"));
            }
          }

        } else if (inpdecisionType.equals("EX")) {
          empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class, " ehcmEmpPerinfo.id='"
              + employeeId + "' and enabled='Y' and issecondment='Y' order by creationDate desc ");
          log4j.debug("employeeId:" + employeeId);
          log4j.debug("positiontype:" + empInfo.list().size());
          if (empInfo.list().size() > 0) {
            empinfo = empInfo.list().get(0);
          }
          if (empinfo != null) {
            employmentInfoId = empinfo.getId();
            info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
            if (empinfo.getPosition().getSection() != null) {
              info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
            }
            info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
            info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
            info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
            info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
            info.addResult("inpassignedDept", empinfo.getSECDeptName());
            info.addResult("inpehcmGradestepsId",
                empinfo.getEhcmPayscale().getEhcmGradesteps().getId());
            info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());
          }
          if (empinfo.getEndDate() != null) {
            Date dateafter = new Date(empinfo.getEndDate().getTime() + 1 * 24 * 3600 * 1000);
            String query = " select eut_convert_to_hijri('" + dateFormat.format(dateafter) + "')";

            st = conn.prepareStatement(query);
            rs = st.executeQuery();
            if (rs.next()) {
              info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri"));
              inpstartdate = rs.getString("eut_convert_to_hijri");
            }
            if (inpperiodType.equals("Y")) {
              String startyear = inpstartdate.split("-")[2];
              String startdate = inpstartdate.split("-")[0];
              // int endday = Integer.valueOf(startdate) - 1;
              int endyear = Integer.valueOf(startyear) + Integer.valueOf(inpperiod);
              //
              String enddate = endyear + inpstartdate.split("-")[1] + inpstartdate.split("-")[0];
              log4j.debug("enddate:" + enddate);
              enddate = getOneDayMinusHijiriDate(enddate, inpclient);
              info.addResult("inpenddate", enddate);
              //
              log4j.debug("enddate:" + enddate);
            }

          }
        }
        if (inpdecisionType.equals("CA") || inpdecisionType.equals("UP")) {
          info.addResult("inpgovAgency", empinfo.getToGovernmentAgency());
        }
      }
      if (lastfieldChanged.equals("inpperiodType") || lastfieldChanged.equals("inpperiod")
          || lastfieldChanged.equals("inpstartdate")
          || lastfieldChanged.equals("inpehcmEmpPerinfoId")) {
        String startdate = "", enddate = "", startyear = "", startmonth = "", endMonth = "",
            endYear = "";
        int endyear = 0, endmonth = 0, endday = 0;
        if (StringUtils.isNotEmpty(inpperiodType) && StringUtils.isNotEmpty(inpperiod)) {
          // year calculation
          if (inpperiodType.equals("Y")) {
            startyear = inpstartdate.split("-")[2];
            startdate = inpstartdate.split("-")[0];
            endyear = Integer.valueOf(startyear) + Integer.valueOf(inpperiod);
            enddate = endyear + inpstartdate.split("-")[1] + inpstartdate.split("-")[0];
            // endday = Integer.valueOf(startdate) - 1;
            log4j.debug("enddate:" + enddate);
            enddate = getOneDayMinusHijiriDate(enddate, inpclient);
            // info.addResult("inpenddate", endday + "-" + inpstartdate.split("-")[1] + "-" +
            // endyear);
            inpenddate = enddate;
            info.addResult("inpenddate", enddate);
            log4j.debug("finalendmonth:" + enddate);
          }
          // month calcultion
          else if (inpperiodType.equals("M")) {
            startdate = inpstartdate.split("-")[0];
            // endday = Integer.valueOf(startdate) - 1;
            startmonth = inpstartdate.split("-")[1];
            int years = Integer.valueOf(inpperiod) / 12;
            log4j.debug("years:" + years);
            int months = Integer.valueOf(inpperiod) % 12;
            log4j.debug("months:" + months);
            if (years > 0) {
              startyear = inpstartdate.split("-")[2];
              log4j.debug("startyear:" + startyear);
              endyear = Integer.valueOf(startyear) + years;
              endYear = String.valueOf(endyear);
              log4j.debug("endYear:" + endYear);
            } else if (years == 0) {
              endYear = inpstartdate.split("-")[2];
              log4j.debug("endYearzero:" + endYear);
            }
            if (months > 0) {
              startmonth = inpstartdate.split("-")[1];
              log4j.debug("monthgreat0:" + startmonth);
              int month = 12 - Integer.valueOf(startmonth);
              log4j.debug("month:" + month);
              if (months > month) {
                endmonth = months - month;
                endyear = Integer.valueOf(endYear) + 1;
                endYear = String.valueOf(endyear);
                log4j.debug("endmonth:" + endmonth);
                log4j.debug("endYear:" + endYear);
              } else {
                endmonth = Integer.valueOf(startmonth) + months;
              }
              if (endmonth < 10) {
                endMonth = "0" + endmonth;
              } else
                endMonth = String.valueOf(endmonth);

              log4j.debug("endMonth:" + endMonth);
            } else if (months == 0) {
              endMonth = inpstartdate.split("-")[1];
              log4j.debug("endMonthzero:" + endMonth);
            }
            enddate = endYear + endMonth + inpstartdate.split("-")[0];
            log4j.debug("enddate:" + enddate);
            enddate = getOneDayMinusHijiriDate(enddate, inpclient);
            inpenddate = enddate;
            info.addResult("inpenddate", enddate);
            log4j.debug("inpenddate:" + inpenddate);

          }
          // month calcultion
          else if (inpperiodType.equals("D")) {
            startdate = inpstartdate.split("-")[2] + inpstartdate.split("-")[1]
                + inpstartdate.split("-")[0];
            st = conn.prepareStatement(
                "select hijri_date from (select max(hijri_date)  as hijri_date from eut_hijri_dates where   hijri_date >= ? group by   hijri_date "
                    + "    order by hijri_date asc   limit ? ) dual order by hijri_date desc limit 1   ");
            st.setString(1, startdate);
            // st.setString(2, inpclient);
            st.setInt(2, Integer.valueOf(inpperiod));
            log4j.debug("st:" + st.toString());
            rs = st.executeQuery();
            if (rs.next()) {
              log4j.debug("hijri_date:" + rs.getString("hijri_date"));
              enddate = rs.getString("hijri_date").substring(6, 8) + "-"// (Integer.valueOf(rs.getString("hijri_date").substring(6,
              // 8)) - 1) + "-"
                  + rs.getString("hijri_date").substring(4, 6) + "-"
                  + rs.getString("hijri_date").substring(0, 4);
              inpenddate = enddate;
              info.addResult("inpenddate", enddate);
              perioddayenddate = enddate;
            }

          }
        }
      }
      if (lastfieldChanged.equals("inpenddate")) {
        if (!perioddayenddate.equals(inpenddate)) {
          int years = 0;
          int months = 0;
          int days = 0;
          String strNowmonth = "";
          String startdate = inpstartdate;
          log4j.debug("inpenddate:" + inpenddate);
          String enddate = inpenddate.split("-")[2] + inpenddate.split("-")[1]
              + inpenddate.split("-")[0];
          enddate = getOneDayAddHijiriDate(enddate, inpclient);
          int startyear = Integer.parseInt(startdate.split("-")[2]);
          int startmonth = Integer.parseInt(startdate.split("-")[1]);
          int startday = Integer.parseInt(startdate.split("-")[0]);
          log4j.debug("dobyear>>dobmonth>>dobdate" + startyear + "-" + startmonth + "-" + startday);

          int endyear = Integer.parseInt(enddate.split("-")[2]);
          int endmonth = Integer.parseInt(enddate.split("-")[1]);
          int endday = Integer.parseInt(enddate.split("-")[0]);
          log4j.debug("endday>>" + endyear + "-" + endmonth + "-" + endday);
          years = endyear - startyear;
          months = endmonth - startmonth;
          log4j.debug("yearsfirst:" + years + "-" + months);
          if (months < 0) {
            years--;
            months = 12 - startmonth + endmonth;
            if (endday < startday) {
              months--;
            }
          } else if (months == 0 && endday < startday) {
            years--;
            years = 11;

          }
          log4j.debug("yearsafter less zero" + years + "-" + months);
          if (endday > startday) {
            days = endday - startday;

          } else if (endday < startday) {
            int today = endday;
            endmonth = endmonth - 1;
            if (endmonth < 10) {
              strNowmonth = "0" + String.valueOf(endmonth);
            } else {
              strNowmonth = String.valueOf(endmonth);
            }
            int maxCurrentDate = UtilityDAO.getDays(inpclient, strNowmonth);
            days = maxCurrentDate - startday + today;

          } else {
            days = 0;
            if (months == 12) {
              years++;
              months = 0;
            }
          }
          log4j.debug("years:" + years + "-" + months + "-" + days);
          if (years == 0 && months > 0 && days == 0) {
            info.addResult("inpperiodType", "M");
            info.addResult("inpperiod", months);
          } else if (months == 0 && years > 0 && days == 0) {
            info.addResult("inpperiodType", "Y");
            info.addResult("inpperiod", years);
          } else if (months > 0 && years > 0 && days == 0) {
            months = years * 12 + months;
            info.addResult("inpperiodType", "M");
            info.addResult("inpperiod", months);
          } else if (months == 0 && years == 0 && days == 0) {
            info.addResult("inpperiodType", "D");
            info.addResult("inpperiod", "0");
          }

          else if (months >= 0 && years == 0 && days > 1) {
            st = conn.prepareStatement(
                " select count(distinct hijri_date) as total from eut_hijri_dates  where hijri_date > ? and hijri_date <= ?");
            st.setString(1, inpstartdate.split("-")[2] + inpstartdate.split("-")[1]
                + inpstartdate.split("-")[0]);
            st.setString(2,
                inpenddate.split("-")[2] + inpenddate.split("-")[1] + inpenddate.split("-")[0]);
            log4j.debug("st:" + st.toString());
            rs = st.executeQuery();
            if (rs.next()) {
              info.addResult("inpperiodType", "D");
              info.addResult("inpperiod", (rs.getInt("total") + 1));
              log4j.debug("total:" + (rs.getInt("total") + 1));
            }

          } else if (months == 0 && years == 0 && days == 1) {
            info.addResult("inpperiodType", "D");
            info.addResult("inpperiod", "1");
          }
        }
      }
    } catch (Exception e) {
      log4j.error("Exception in EmpSecondmentCallout Callout", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }

  public String getOneDayMinusHijiriDate(String gregoriandate, String clientId) {
    BigInteger days = BigInteger.ZERO;
    Query query = null;
    String strQuery = "", startdate = "";
    try {

      strQuery = " select  hijri_date from eut_hijri_dates  where hijri_date < '" + gregoriandate
          + "' order by hijri_date desc limit 1 ";
      query = OBDal.getInstance().getSession().createSQLQuery(strQuery);
      log4j.debug("strQuery:" + strQuery);
      if (query != null && query.list().size() > 0) {
        log4j.debug("geto" + query.list().get(0));
        Object row = query.list().get(0);
        startdate = (String) row;
        log4j.debug("startdate" + startdate);
        log4j.debug("substring" + startdate.substring(6, 8));
        startdate = startdate.substring(6, 8) + "-" + startdate.substring(4, 6) + "-"
            + startdate.substring(0, 4);
        log4j.debug("startdate12" + startdate);
      }
    } catch (Exception e) {
      log4j.error("Exception in getOneDayMinusHijiriDate", e);
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
    return startdate;
  }

  public String getOneDayAddHijiriDate(String gregoriandate, String clientId) {
    BigInteger days = BigInteger.ZERO;
    Query query = null;
    String strQuery = "", startdate = "";
    try {

      strQuery = " select  hijri_date from eut_hijri_dates  where hijri_date > '" + gregoriandate
          + "' order by hijri_date asc limit 1 ";
      query = OBDal.getInstance().getSession().createSQLQuery(strQuery);
      log4j.debug("strQuery:" + strQuery);
      if (query != null && query.list().size() > 0) {
        log4j.debug("geto" + query.list().get(0));
        Object row = query.list().get(0);
        startdate = (String) row;
        log4j.debug("startdate" + startdate);
        log4j.debug("substring" + startdate.substring(6, 8));
        startdate = startdate.substring(6, 8) + "-" + startdate.substring(4, 6) + "-"
            + startdate.substring(0, 4);
        log4j.debug("startdate12" + startdate);
      }
    } catch (Exception e) {
      log4j.error("Exception in getOneDayAddHijiriDate", e);
      e.printStackTrace();
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
    return startdate;
  }

  public String convertTohijriDate(String gregDate) {
    String hijriDate = "";
    try {
      SQLQuery gradeQuery = OBDal.getInstance().getSession()
          .createSQLQuery("select eut_convert_to_hijri(to_char(to_timestamp('" + gregDate
              + "','YYYY-MM-DD HH24:MI:SS'),'YYYY-MM-DD  HH24:MI:SS'))");
      if (gradeQuery.list().size() > 0) {
        Object row = (Object) gradeQuery.list().get(0);
        hijriDate = (String) row;
        log4j.debug("ConvertedDate:" + (String) row);
      }
    }

    catch (final Exception e) {
      log4j.error("Exception in convertTohijriDate() Method : ", e);
      return "0";
    }
    return hijriDate;
  }
}
