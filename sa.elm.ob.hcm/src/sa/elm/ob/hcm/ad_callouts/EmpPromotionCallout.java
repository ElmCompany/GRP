package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.Organization;

import sa.elm.ob.hcm.EHCMEmpPromotion;
import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.hcm.ad_callouts.common.UpdateEmpDetailsInCallouts;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

@SuppressWarnings("serial")
public class EmpPromotionCallout extends SimpleCallout {

  @SuppressWarnings("unchecked")
  protected void execute(CalloutInfo info) {
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String inppromotionType = vars.getStringParameter("inppromotionType");
    String lastfieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpnewDepartmentId = vars.getStringParameter("inpnewDepartmentId");
    String inpnewEhcmPositionId = vars.getStringParameter("inpnewEhcmPositionId");
    String inpdepartmentId = vars.getStringParameter("inpdepartmentId");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String inpnewGradeId = vars.getStringParameter("inpnewGradeId");
    String inpemploymentgrade = vars.getStringParameter("inpemploymentgrade");
    String inpstartdate = vars.getStringParameter("inpstartdate");

    log4j.debug("lastfiled:" + lastfieldChanged);
    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = "";
    String employmentInfoId = "";
    DateFormat dateFormat = sa.elm.ob.utility.util.Utility.YearFormat;
    try {
      /*
       * get Latest active EmploymentInfo by using EmployeeId and set the value based on Employment
       * Info
       * 
       */
      EmploymentInfo empinfo = null;
      empinfo = Utility.getActiveEmployInfo(employeeId);
      UpdateEmpDetailsInCallouts callouts = new UpdateEmpDetailsInCallouts();
      if (lastfieldChanged.equals("inpehcmEmpPerinfoId")) {
        if (StringUtils.isNotEmpty(employeeId)) {
          /* get Employee Details by using employeeId */
          EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          info.addResult("inpempName", employee.getArabicfullname());
          log4j.debug("employee:" + employee);

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
            employmentInfoId = empinfo.getId();
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
            info.addResult("inpehcmGradestepsId",
                empinfo.getEhcmPayscale().getEhcmGradesteps().getId());
            info.addResult("inpehcmPayscalelineId", empinfo.getEhcmPayscaleline().getId());
            log4j.debug("inpehcmPayscalelineId:" + empinfo.getEhcmPayscaleline().getId());
            if (empinfo.getStartDate() != null) {
              info.addResult("inpstartdate",
                  (UtilityDAO.convertTohijriDate(dateFormat.format(empinfo.getStartDate()))));
            }
            inpdecisionType = DecisionTypeConstants.DECISION_TYPE_CREATE;
            info.addResult("inpdecisionType", inpdecisionType);
            info.addResult("inporiginalDecisionsNo", "");
            info.addResult("inpnewDepartmentId", empinfo.getPosition().getDepartment().getId());
            info.addResult("inppromotionType", Constants.PROMOTION);
            /*
             * info.addResult("JSEXECUTE",
             * "form.getFieldFromColumnName('NEW_Department_ID').setValue('')");
             * info.addResult("JSEXECUTE",
             * "form.getFieldFromColumnName('NEW_Section_ID').setValue('')");
             */
            /*
             * info.addResult("inpnewDepartmentId", empinfo.getPosition().getDepartment().getId());
             * if (empinfo.getPosition() != null && empinfo.getPosition().getSection() != null) {
             * info.addResult("inpnewSectionId", empinfo.getPosition().getSection().getId()); }
             */
            if (inppromotionType.equals(Constants.PROMOTION)) {
              /*
               * info.addResult("JSEXECUTE",
               * "form.getFieldFromColumnName('NEW_Department_ID').setValue('')");
               */

              inpnewDepartmentId = empinfo.getPosition().getDepartment().getId();
              if (empinfo.getPosition() != null && empinfo.getPosition().getSection() != null) {
                info.addResult("inpnewSectionId", empinfo.getPosition().getSection().getId());
              } else {
                info.addResult("JSEXECUTE",
                    "form.getFieldFromColumnName('NEW_Section_ID').setValue('')");
              }

              info.addResult("inpnewEhcmPositionId", empinfo.getPosition().getId());
              inpnewEhcmPositionId = empinfo.getPosition().getId();
              info.addResult("inpnewJobTitle", empinfo.getPosition().getEhcmJobs().getJOBTitle());
              info.addResult("inpnewJobCode", empinfo.getPosition().getEhcmJobs().getJobCode());
              info.addResult("inpnewJobNo", empinfo.getPosition().getJOBNo());
              info.addResult("inpnewPositionCode", empinfo.getPosition().getGrade().getSearchKey()
                  + "-" + empinfo.getPosition().getJOBNo());
            }

          }
        } else {
          callouts.SetEmpDetailsNull(info);
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('Ehcm_Payscaleline_ID').setValue('')");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Assigned_Dept').setValue('')");

        }
      }
      /* Transfer Type change */
      if (lastfieldChanged.equals("inppromotionType")) {
        if (inppromotionType.equals(Constants.PROMOTIONTRANSFER)) {// inppromotionType.equals(Constants.PROMOTION)
          inpnewDepartmentId = inpdepartmentId;
          info.addResult("inpnewDepartmentId", inpdepartmentId);
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('NEW_Department_ID').setValue('')");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').setValue('')");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('NEW_Ehcm_Position_ID').setValue('')");

        }

      }
      /* new grade change */
      if (lastfieldChanged.equals("inpnewGradeId") || lastfieldChanged.equals("inpehcmEmpPerinfoId")
          || lastfieldChanged.equals("inppromotionType")
          || lastfieldChanged.equals("inpnewDepartmentId")
          || lastfieldChanged.equals("inpdecisionType")) {
        if (StringUtils.isNotEmpty(employeeId)) {
          log4j.debug("entered new position: " + inpnewEhcmPositionId);
          log4j.debug("inpnewDepartmentId: " + inpnewDepartmentId);
          if (empinfo != null)
            inpdepartmentId = empinfo.getPosition().getDepartment().getId();

          if (lastfieldChanged.equals("inpdecisionType")) {

            if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                || inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
              if ((empinfo.getChangereason().equals(Constants.PROMOTION))
                  || (empinfo.getChangereason().equals(Constants.PROMOTIONTRANSFER))) {
                OBQuery<EHCMEmpPromotion> objEmpQuery = OBDal.getInstance()
                    .createQuery(EHCMEmpPromotion.class, "as e where e.ehcmEmpPerinfo.id='"
                        + employeeId
                        + "' and e.enabled='Y' and e.issueDecision='Y' and e.isJoinWorkRequest = 'N' order by e.creationDate desc");
                objEmpQuery.setMaxResult(1);
                if (objEmpQuery.list().size() > 0) {
                  EHCMEmpPromotion promotion = objEmpQuery.list().get(0);
                  log4j.debug("getDecisionNo():" + promotion.getId());
                  info.addResult("inporiginalDecisionsNo", promotion.getId());
                }
              }

              info.addResult("inpnewGradeId", empinfo.getEmploymentgrade().getId());
              inpnewGradeId = empinfo.getEmploymentgrade().getId();
              if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                  || inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)) {
                if (empinfo.getStartDate() != null) {
                  info.addResult("inpstartdate",
                      (UtilityDAO.convertTohijriDate(dateFormat.format(empinfo.getStartDate()))));
                }
              }
            }
            if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL)
                || inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
              info.addResult("inppromotionType", empinfo.getChangereason());
            }
          }
          OBQuery<Organization> organization = OBDal.getInstance().createQuery(Organization.class,
              " id in ( select id from Organization where  ehcmAdOrg='" + inpnewDepartmentId
                  + "') order by created desc ");

          log4j.debug("getWhereAndOrderBy:" + organization.getWhereAndOrderBy());
          if (organization.list().size() > 0) {
            for (Organization org : organization.list()) {
              info.addResult("inpnewSectionId", org.getId());
              log4j.debug("inpnewSectionId:" + org.getId());
            }
          }
          /*
           * if (inppromotionType.equals(Constants.PROMOTIONTRANSFER)) { sql +=
           * " select ehcm_positionvalue_v_id  as id from ehcm_positionvalue_v where  ehcm_positionvalue_v.isactive='Y'  and  ehcm_positionvalue_v.ehcm_postransactiontype_id not in (select  ehcm_postransactiontype_id from  ehcm_postransactiontype where value in ('CAPO','TROPO' )) "
           * ; if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) sql +=
           * " and  (ehcm_positionvalue_v_id not in ( select ehcm_position_id from ehcm_employment_info info   left join ehcm_emp_perinfo per on per.ehcm_emp_perinfo_id=info.ehcm_emp_perinfo_id where per.status='I' and info.isactive='Y' and ehcm_position_id is not null  )  )  "
           * ;
           * 
           * if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) sql +=
           * " and  (ehcm_positionvalue_v_id not in ( select ehcm_position_id from ehcm_employment_info info   left join ehcm_emp_perinfo per on per.ehcm_emp_perinfo_id=info.ehcm_emp_perinfo_id where per.status='I' and info.isactive='Y'  and ehcm_position_id is not null  )   or  ehcm_positionvalue_v_id='"
           * + empinfo.getPosition().getId() + "')";
           * 
           * if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CANCEL) ||
           * inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) sql +=
           * " and ehcm_positionvalue_v_id ='" + empinfo.getPosition().getId() + "'";
           * 
           * if (inppromotionType.equals(Constants.PROMOTION)) sql +=
           * "  and ehcm_positionvalue_v.department_id='" + inpdepartmentId + "'"; else sql +=
           * "  and ehcm_positionvalue_v.department_id='" + inpnewDepartmentId + "'";
           * 
           * if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) { sql +=
           * " and "; } if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
           * sql += " and ( "; }
           * 
           * sql +=
           * "    (ehcm_positionvalue_v_id not in (select  ehcm_position_id   from ehcm_posemp_history where case  when ehcm_posemp_history.enddate is not null then "
           * +
           * "        ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= (select eut_convertto_gregorian('"
           * + inpstartdate + "')) " +
           * " and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') "
           * + "  <= (select to_date('21-06-2058','dd-MM-yyyy')))" +
           * "  or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') "
           * + " >= (select eut_convertto_gregorian('" + inpstartdate + "') )" +
           * " and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= (select to_date('21-06-2058','dd-MM-yyyy'))) )  "
           * + " else ehcm_posemp_history.enddate is  null end  )) ";
           * 
           * if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) { sql +=
           * " or ehcm_positionvalue_v_id ='" + empinfo.getPosition().getId() + "') "; }
           * 
           * if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) sql +=
           * " and   ehcm_positionvalue_v_id not in ( select ehcm_position_id from ehcm_position where assigned_employee is not null) "
           * ; if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) sql +=
           * " and  ( ehcm_positionvalue_v_id not in ( select ehcm_position_id from ehcm_position where assigned_employee is not null)  or ehcm_positionvalue_v_id ='"
           * + empinfo.getPosition().getId() + "')";
           * 
           * 
           * sql +=
           * " and ehcm_positionvalue_v.transaction_status='I'   and  ehcm_positionvalue_v_id in (select ehcm_position_id from ehcm_position where ehcm_grade_id in (  select ehcm_grade_id from ehcm_grade where  seqno >=( select seqno from ehcm_grade where  ehcm_grade_id='"
           * + inpnewGradeId + "')  order by seqno asc limit 3))  order by name limit 1";
           * 
           * sql +=
           * " and  ehcm_positionvalue_v_id in ( select ehcm_position_id from ehcm_position where ehcm_grade_id='"
           * + inpnewGradeId + "')  ";
           * 
           * st = conn.prepareStatement(sql); log4j.debug("st:" + st.toString()); rs =
           * st.executeQuery(); if (rs.next()) { inpnewEhcmPositionId = rs.getString("id");
           * info.addResult("inpnewEhcmPositionId", inpnewEhcmPositionId); EhcmPosition pos =
           * OBDal.getInstance().get(EhcmPosition.class, inpnewEhcmPositionId); if
           * (inpnewEhcmPositionId != null && inpnewEhcmPositionId != "") { Jobs job =
           * OBDal.getInstance().get(Jobs.class, pos.getEhcmJobs().getId());
           * info.addResult("inpnewJobCode", job.getJobCode()); info.addResult("inpnewJobTitle",
           * job.getJOBTitle()); info.addResult("inpnewJobNo", pos.getJOBNo());
           * info.addResult("inpnewDepartmentId", pos.getDepartment().getId()); if (pos.getSection()
           * != null) { info.addResult("inpnewSectionId", pos.getSection().getId()); }
           * 
           * info.addResult("inpnewPositionCode", pos.getGrade().getSearchKey() + "-" +
           * pos.getJOBNo()); log4j.debug("entered new  pos.getJOBNo(): " + pos.getJOBNo()); } }
           * else { info.addResult("inpnewEhcmPositionId", ""); info.addResult("inpnewJobTitle",
           * ""); info.addResult("inpnewJobCode", ""); info.addResult("inpnewJobNo", "");
           * info.addResult("inpnewPositionCode", ""); } }
           */ else if (inppromotionType.equals(Constants.PROMOTION)) {
            /*
             * info.addResult("JSEXECUTE",
             * "form.getFieldFromColumnName('NEW_Department_ID').setValue('')");
             * info.addResult("JSEXECUTE",
             * "form.getFieldFromColumnName('NEW_Section_ID').setValue('')");
             */

            info.addResult("inpnewDepartmentId", empinfo.getPosition().getDepartment().getId());
            inpnewDepartmentId = empinfo.getPosition().getDepartment().getId();
            if (empinfo.getPosition() != null && empinfo.getPosition().getSection() != null) {
              info.addResult("inpnewSectionId", empinfo.getPosition().getSection().getId());
            } else {
              info.addResult("JSEXECUTE",
                  "form.getFieldFromColumnName('NEW_Section_ID').setValue('')");
            }

            info.addResult("inpnewEhcmPositionId", empinfo.getPosition().getId());
            inpnewEhcmPositionId = empinfo.getPosition().getId();
            log4j.debug("inpnewEhcmPositionId:" + empinfo.getPosition().getId());
            info.addResult("inpnewJobTitle", empinfo.getPosition().getEhcmJobs().getJOBTitle());
            info.addResult("inpnewJobCode", empinfo.getPosition().getEhcmJobs().getJobCode());
            info.addResult("inpnewJobNo", empinfo.getPosition().getJOBNo());
            info.addResult("inpnewPositionCode", empinfo.getPosition().getGrade().getSearchKey()
                + "-" + empinfo.getPosition().getJOBNo());
          }
        } else {
          callouts.SetEmpDetailsNull(info);
        }
      }
      if (lastfieldChanged.equals("inpnewEhcmPositionId")) {
        log4j.debug("new:" + inpnewEhcmPositionId);
        if (inpnewEhcmPositionId != null && inpnewEhcmPositionId != "") {
          EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class, inpnewEhcmPositionId);
          Jobs job = OBDal.getInstance().get(Jobs.class, pos.getEhcmJobs().getId());
          info.addResult("inpnewJobCode", job.getJobCode());
          info.addResult("inpnewJobTitle", job.getJOBTitle());
          info.addResult("inpnewJobNo", pos.getJOBNo());
          // if (inppromotionType.equals(Constants.PROMOTIONTRANSFER)) {
          info.addResult("inpnewDepartmentId", pos.getDepartment().getId());
          if (pos.getSection() != null) {
            info.addResult("inpnewSectionId", pos.getSection().getId());
          } else {
            info.addResult("JSEXECUTE",
                "form.getFieldFromColumnName('NEW_Section_ID').setValue('')");
          }
          // }
          info.addResult("inpnewPositionCode",
              pos.getGrade().getSearchKey() + "-" + pos.getJOBNo());
        } else {
          info.addResult("inpnewEhcmPositionId", "");
          info.addResult("inpnewJobTitle", "");
          info.addResult("inpnewJobCode", "");
          info.addResult("inpnewJobNo", "");
          info.addResult("inpnewPositionCode", "");
          info.addResult("JSEXECUTE", "form.getFieldFromColumnName('NEW_Section_ID').setValue('')");
          info.addResult("JSEXECUTE",
              "form.getFieldFromColumnName('NEW_Department_ID').setValue('')");
        }
      }
    } catch (

    Exception e) {
      log4j.error("Exception in EmpPormotion Callout", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }
  }
}
