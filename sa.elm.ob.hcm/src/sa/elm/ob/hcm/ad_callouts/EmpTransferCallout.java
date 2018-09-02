package sa.elm.ob.hcm.ad_callouts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.Organization;

import sa.elm.ob.hcm.EHCMEmpTransfer;
import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.Jobs;
import sa.elm.ob.utility.util.UtilityDAO;

@SuppressWarnings("serial")
public class EmpTransferCallout extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // TODO Auto-generated method stub

    /* get values */
    VariablesSecureApp vars = info.vars;
    String employeeId = vars.getStringParameter("inpehcmEmpPerinfoId");
    String empName = vars.getStringParameter("inpeMPName");
    String inpdecisionType = vars.getStringParameter("inpdecisionType");
    String inpnewEhcmPositionId = vars.getStringParameter("inpnewEhcmPositionId");
    String inpehcmGradeId = vars.getStringParameter("inpehcmGradeId");
    String inpehcmPositionId = vars.getStringParameter("inpehcmPositionId");
    String inpnewDepartmentId = vars.getStringParameter("inpnewDepartmentId");
    String inptransferType = vars.getStringParameter("inptransferType");
    String inpdepartmentId = vars.getStringParameter("inpdepartmentId");
    String inpLastFieldChanged = vars.getStringParameter("inpLastFieldChanged");
    String inpemploymentgrade = vars.getStringParameter("inpemploymentgrade");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String inpstartdate = vars.getStringParameter("inpstartdate");
    String inpenddate = vars.getStringParameter("inpenddate");

    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = "";
    String employmentInfoId = "";
    log4j.debug("lastfield:" + inpLastFieldChanged);
    try {

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
      /* Employee change */
      if (inpLastFieldChanged.equals("inpehcmEmpPerinfoId")) {
        log4j.debug("entered ehcmempperinfo");
        /* get Employee Details by using employeeId */
        EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
        info.addResult("inpempName", employee.getArabicfullname());
        if (employee.getHiredate() != null) {
          String query = " select eut_convert_to_hijri_timestamp('"
              + dateFormat.format(employee.getHiredate()) + "')";

          st = conn.prepareStatement(query);
          rs = st.executeQuery();
          if (rs.next())
            info.addResult("inphireDate", rs.getString("eut_convert_to_hijri_timestamp"));

        }
        info.addResult("inpempType", employee.getEhcmActiontype().getPersonType());
        /*
         * get Latest active EmploymentInfo by using EmployeeId and set the value based on
         * Employment Info
         */
        if (empinfo != null) {
          employmentInfoId = empinfo.getId();
          log4j.debug("employmentInfoId:" + employmentInfoId);
          info.addResult("inpdepartmentId", empinfo.getPosition().getDepartment().getId());
          inpdepartmentId = empinfo.getPosition().getDepartment().getId();
          if (empinfo.getPosition().getSection() != null) {
            info.addResult("inpsectionId", empinfo.getPosition().getSection().getId());
          } else {
            info.addResult("JSEXECUTE", "form.getFieldFromColumnName('Section_ID').setValue('')");
          }
          info.addResult("inpehcmGradeId", empinfo.getGrade().getId());
          inpehcmGradeId = empinfo.getGrade().getId();
          info.addResult("inpehcmPositionId", empinfo.getPosition().getId());
          log4j.debug("inpehcmPositionId:" + empinfo.getPosition().getJOBNo());
          info.addResult("inpjobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
          info.addResult("inpemploymentgrade", empinfo.getEmploymentgrade().getId());
          inpemploymentgrade = empinfo.getEmploymentgrade().getId();
          info.addResult("inpassignedDept", empinfo.getSECDeptName());

          if (empinfo.getStartDate() != null) {
            String query = " select eut_convert_to_hijri_timestamp('"
                + dateFormat.format(empinfo.getStartDate()) + "')";

            st = conn.prepareStatement(query);
            rs = st.executeQuery();
            if (rs.next())
              info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri_timestamp"));

          }
          inpdecisionType = "CR";
          info.addResult("inpdecisionType", inpdecisionType);
          info.addResult("inporiginalDecisionsNo", "");
          info.addResult("inptransferType", "ID");
          inptransferType = "ID";
          if (inptransferType.equals("ID")) {
            info.addResult("inpnewDepartmentId", empinfo.getPosition().getDepartment().getId());
            if (empinfo.getPosition().getSection() != null) {
              info.addResult("inpnewSectionId", empinfo.getPosition().getSection().getId());
            }
            log4j.debug("inpnewEhcmPositionId:" + inpnewEhcmPositionId);
            inpnewDepartmentId = empinfo.getPosition().getDepartment().getId();
          }
        }
        EHCMEmployeeStatusV employeeStatus = OBDal.getInstance().get(EHCMEmployeeStatusV.class,
            employeeId);
        if (employeeStatus != null)
          info.addResult("inpempStatus", employeeStatus.getStatusvalue());
        else
          info.addResult("inpempStatus", "");
        info.addResult("inpehcmGradeclassId", employee.getGradeClass().getId());
        inpLastFieldChanged = "inpnewEhcmPositionId";
      }
      /* New Department change */
      if (inpLastFieldChanged.equals("inpnewDepartmentId")
          || inpLastFieldChanged.equals("inpehcmEmpPerinfoId")
          || inpLastFieldChanged.equals("inpdecisionType")
          || inpLastFieldChanged.equals("inptransferType")
          || inpLastFieldChanged.equals("inpstartdate")
          || inpLastFieldChanged.equals("inpenddate")) {
        log4j.debug("entered  new department ");
        if (inpLastFieldChanged.equals("inptransferType")) {
          if (inptransferType.equals("OD")) {
            OBQuery<Organization> dept = OBDal.getInstance().createQuery(Organization.class,
                " as e where e.id not in ('" + inpdepartmentId
                    + "') and e.ehcmOrgtyp.level='3'  order by e.creationDate desc");
            log4j.debug("dept.getWhereAndOrderBy():" + dept.getWhereAndOrderBy());
            dept.setMaxResult(1);
            if (dept.list().size() > 0) {
              Organization department = dept.list().get(0);
              log4j.debug("dept.getName():" + department.getName());
              inpnewDepartmentId = department.getId();
            }
            info.addResult("inpnewDepartmentId", inpnewDepartmentId);
          }
          if (inptransferType.equals("ID")) {
            inpnewDepartmentId = inpdepartmentId;
            info.addResult("inpnewDepartmentId", inpdepartmentId);

          }
        }
        if (inpLastFieldChanged.equals("inpdecisionType")) {

          if (inpdecisionType.equals("UP") || inpdecisionType.equals("CA")) {
            if ((empinfo.getChangereason().equals("ID")) || (empinfo.getChangereason().equals("OD"))
                || empinfo.getChangereason().equals("H")) {
              OBQuery<EHCMEmpTransfer> objEmpQuery = OBDal.getInstance()
                  .createQuery(EHCMEmpTransfer.class, "as e where e.ehcmEmpPerinfo.id='"
                      + employeeId
                      + "' and e.enabled='Y'  and e.issueDecision='Y' and e.isjoinworkreq = 'N' order by e.creationDate desc");
              objEmpQuery.setMaxResult(1);
              if (objEmpQuery.list().size() > 0) {
                EHCMEmpTransfer transfer = objEmpQuery.list().get(0);
                log4j.debug("getDecisionNo():" + transfer.getId());
                info.addResult("inporiginalDecisionsNo", transfer.getId());
                info.addResult("inpisjoinworkreq", transfer.isJoinworkreq());
              }
            }

            info.addResult("inptransferType", empinfo.getChangereason());
            if (empinfo.getStartDate() != null) {
              String query = " select eut_convert_to_hijri_timestamp('"
                  + dateFormat.format(empinfo.getStartDate()) + "')";

              st = conn.prepareStatement(query);
              rs = st.executeQuery();
              if (rs.next())
                info.addResult("inpstartdate", rs.getString("eut_convert_to_hijri_timestamp"));

            }
            if (empinfo.getEndDate() != null)
              info.addResult("inpenddate",
                  (UtilityDAO.convertTohijriDate(dateFormat.format(empinfo.getEndDate()))));
          }
          if (inpdecisionType.equals("CA")) {
            if (empinfo.getEndDate() != null) {
              String query = " select eut_convert_to_hijri_timestamp('"
                  + dateFormat.format(empinfo.getEndDate()) + "')";

              st = conn.prepareStatement(query);
              rs = st.executeQuery();
              if (rs.next())
                info.addResult("inpenddate", rs.getString("eut_convert_to_hijri_timestamp"));

            }
            info.addResult("inpnewEhcmPositionId", empinfo.getPosition().getId());
          }
        }

        /*
         * sql +=
         * " select ehcm_positionvalue_v_id  as id from ehcm_positionvalue_v where  ehcm_positionvalue_v.isactive='Y'  and  ehcm_positionvalue_v.ehcm_postransactiontype_id not in (select  ehcm_postransactiontype_id from  ehcm_postransactiontype where value in ('CAPO','TROPO' )) "
         * ; if (inpdecisionType.equals("CR")) sql +=
         * " and  (ehcm_positionvalue_v_id not in ( select ehcm_position_id from ehcm_employment_info info   left join ehcm_emp_perinfo per on per.ehcm_emp_perinfo_id=info.ehcm_emp_perinfo_id where per.status='I' and info.isactive='Y' and ehcm_position_id is not null  )  )  "
         * ;
         * 
         * if (inpdecisionType.equals("UP")) sql +=
         * " and  (ehcm_positionvalue_v_id not in ( select ehcm_position_id from ehcm_employment_info info   left join ehcm_emp_perinfo per on per.ehcm_emp_perinfo_id=info.ehcm_emp_perinfo_id where per.status='I' and info.isactive='Y'  and ehcm_position_id is not null )   or  ehcm_positionvalue_v_id='"
         * + empinfo.getPosition().getId() + "')";
         * 
         * if (inpdecisionType.equals("CA") || inpdecisionType.equals("UP")) sql +=
         * " and ehcm_positionvalue_v_id ='" + empinfo.getPosition().getId() + "'";
         * 
         * if (inptransferType.equals("ID")) sql += "  and ehcm_positionvalue_v.department_id='" +
         * inpdepartmentId + "'"; else sql += "  and ehcm_positionvalue_v.department_id='" +
         * inpnewDepartmentId + "'";
         * 
         * if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) { sql += " and ";
         * } if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) { sql +=
         * " and ( "; }
         * 
         * sql +=
         * "    (ehcm_positionvalue_v_id not in (select  ehcm_position_id   from ehcm_posemp_history where case  when ehcm_posemp_history.enddate is not null then "
         * +
         * " ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= (select eut_convertto_gregorian('"
         * + inpstartdate + "'))  " +
         * "  and to_date(to_char(coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy')  "
         * + "<= to_date(to_char(coalesce ((select eut_convertto_gregorian('" + inpenddate +
         * "')),to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy') " +
         * ",'dd-MM-yyyy'))    or (to_date(to_char( coalesce (enddate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy')   "
         * + "    >= (select eut_convertto_gregorian('" + inpstartdate + "'))   " +
         * "    and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <=  " +
         * "  to_date(to_char(coalesce ((select eut_convertto_gregorian('" + inpenddate + "'))," +
         * "to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy')))   else ehcm_posemp_history.enddate is  null end  )) "
         * ; if (inpdecisionType.equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) { sql +=
         * " or ehcm_positionvalue_v_id ='" + empinfo.getPosition().getId() + "') "; }
         * 
         * if (inpdecisionType.equals("CR")) sql +=
         * " and   ehcm_positionvalue_v_id not in ( select ehcm_position_id from ehcm_position where assigned_employee is not null) "
         * ; if (inpdecisionType.equals("UP")) sql +=
         * " and  ( ehcm_positionvalue_v_id not in ( select ehcm_position_id from ehcm_position where assigned_employee is not null)  or ehcm_positionvalue_v_id ='"
         * + empinfo.getPosition().getId() + "')";
         * 
         * sql +=
         * " and ehcm_positionvalue_v.transaction_status='I'   and  ehcm_positionvalue_v_id in (select ehcm_position_id from ehcm_position where ehcm_grade_id in (  select ehcm_grade_id from ehcm_grade where  seqno >=( select seqno from ehcm_grade where  ehcm_grade_id='"
         * + inpemploymentgrade + "') and ad_client_id ='" + vars.getClient() +
         * "'  order by seqno asc limit 3)) "; sql += "   order by name limit 1"; st =
         * conn.prepareStatement(sql); log4j.debug("st:" + st.toString()); rs = st.executeQuery();
         * if (rs.next()) { inpnewEhcmPositionId = rs.getString("id");
         * info.addResult("inpnewEhcmPositionId", inpnewEhcmPositionId); EhcmPosition pos =
         * OBDal.getInstance().get(EhcmPosition.class, inpnewEhcmPositionId); if
         * (inpnewEhcmPositionId != null && inpnewEhcmPositionId != "") { EhcmPosition position =
         * OBDal.getInstance().get(EhcmPosition.class, inpnewEhcmPositionId);
         * info.addResult("inpnewEhcmPositionId", position.getId()); inpnewEhcmPositionId =
         * position.getId(); log4j.debug("inpdepartmentId:" + inpdepartmentId);
         * info.addResult("inpnewJobTitle", position.getEhcmJobs().getJOBTitle());
         * info.addResult("inpnewJobCode", position.getEhcmJobs().getJobCode());
         * info.addResult("inpnewJobNo", position.getJOBNo()); info.addResult("inpnewPositionCode",
         * position.getGrade().getSearchKey() + "-" + position.getJOBNo()); } } else {
         * info.addResult("inpnewEhcmPositionId", ""); info.addResult("inpnewJobTitle", "");
         * info.addResult("inpnewJobCode", ""); info.addResult("inpnewJobNo", "");
         * info.addResult("inpnewPositionCode", ""); }
         */

        OBQuery<Organization> organization = OBDal.getInstance().createQuery(Organization.class,
            " id in ( select id from Organization where  ehcmAdOrg='" + inpnewDepartmentId
                + "') order by created desc ");

        log4j.debug("getWhereAndOrderBy:" + organization.getWhereAndOrderBy());
        if (organization.list().size() > 0) {
          for (Organization org : organization.list()) {
            info.addResult("inpnewSectionId", org.getId());
          }
        }
      }

      /* new position change */
      if (inpLastFieldChanged.equals("inpnewEhcmPositionId")) {
        log4j.debug("entered new position: " + inpnewEhcmPositionId);
        log4j.debug("inpnewDepartmentId: " + inpnewDepartmentId);
        if (inpnewEhcmPositionId != null && inpnewEhcmPositionId != ""
            && !inpnewEhcmPositionId.equals("null")) {
          EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class, inpnewEhcmPositionId);

          /*
           * OBQuery<EhcmPosition> pos = OBDal.getInstance().createQuery(EhcmPosition.class,
           * " department.id='" + inpnewDepartmentId + "'  and id='" + inpnewEhcmPositionId +
           * "' order by created desc ");
           */

          // EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class, inpnewEhcmPositionId);
          // pos.setMaxResult(1);
          if (pos != null) {
            EhcmPosition position = pos;
            Jobs job = OBDal.getInstance().get(Jobs.class, position.getEhcmJobs().getId());
            info.addResult("inpnewJobCode", job.getJobCode());
            info.addResult("inpnewJobTitle", job.getJOBTitle());
            info.addResult("inpnewJobNo", position.getJOBNo());
            info.addResult("inpnewDepartmentId", position.getDepartment().getId());
            if (position.getSection() != null) {
              info.addResult("inpnewSectionId", position.getSection().getId());
            }
            info.addResult("inpnewPositionCode",
                position.getGrade().getSearchKey() + "-" + position.getJOBNo());
            log4j.debug("entered new  pos.getJOBNo(): " + position.getJOBNo());
          } else {
            info.addResult("inpnewEhcmPositionId", "");
            info.addResult("inpnewJobTitle", "");
            info.addResult("inpnewJobCode", "");
            info.addResult("inpnewJobNo", "");
            info.addResult("inpnewPositionCode", "");
          }
        }

      }

    } catch (Exception e) {
      log4j.error("Exception in EmpTransfer Callout :", e);
      info.addResult("ERROR", OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    }

  }
}
