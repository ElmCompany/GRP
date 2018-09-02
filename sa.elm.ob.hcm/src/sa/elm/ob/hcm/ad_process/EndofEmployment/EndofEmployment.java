package sa.elm.ob.hcm.ad_process.EndofEmployment;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EhcmterminationEmpV;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmempstatus;
import sa.elm.ob.hcm.ehcmpayscaleline;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.hcm.util.UtilityDAO;

public class EndofEmployment implements Process {
  private static final Logger log = Logger.getLogger(EndofEmployment.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Issue the EndofEmployment");

    final String terminationId = (String) bundle.getParams().get("Ehcm_Emp_Termination_ID")
        .toString();
    EHCMEMPTermination termination = OBDal.getInstance().get(EHCMEMPTermination.class,
        terminationId);
    AssingedOrReleaseEmpInPositionDAO positionEmpHist = new AssingedOrReleaseEmpInPositionDAOImpl();
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    EmploymentInfo info = null;
    String employeeId = "";
    String endDate = "21-06-2058";
    EHCMEmpSupervisor supervisorId = null;

    try {
      OBContext.setAdminMode(true);

      employeeId = termination.getEhcmEmpPerinfo().getId();
      EhcmterminationEmpV terminationView = termination.getEhcmEmpPerinfo();

      // check Issued or not
      if (!termination.isSueDecision()) {

        // checking decision overlap
        if (termination.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
            || (termination.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE))) {
          JSONObject decresult = Utility.chkDecisionOverlap(Constants.TERMINATION_OVERLAP,
              sa.elm.ob.utility.util.Utility.formatDate(termination.getTerminationDate()), endDate,
              termination.getEhcmEmpPerinfo().getId(),
              termination.getEhcmTerminationReason().getId(), termination.getId());
          log.debug("decresult:" + decresult);
          if (decresult != null && decresult.has("errorFlag")
              && decresult.getBoolean("errorFlag")) {
            if (termination.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
                || termination.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
                || (termination.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                    && (decresult.has("terminationId") && !decresult.getString("terminationId")
                        .equals(termination.getOriginalDecisionsNo().getId()))
                    || !decresult.has("terminationId"))) {
              if (decresult.has("errormsg")) {
                obError.setType("Error");
                obError.setTitle("Error");
                obError.setMessage(OBMessageUtils.messageBD(decresult.getString("errormsg")));
                bundle.setResult(obError);
                return;
              }
            }
          }
        }
        // Already the same employee is terminated then throw the error
        if (termination.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
          OBQuery<EHCMEMPTermination> empTerminationObj = OBDal.getInstance().createQuery(
              EHCMEMPTermination.class,
              " as e where ehcmEmpPerinfo.id=:employeeId and e.decisionType=:decisionType and issueDecision = 'Y' and enabled = 'Y' ");
          empTerminationObj.setNamedParameter("employeeId",
              termination.getEhcmEmpPerinfo().getId());
          empTerminationObj.setNamedParameter("decisionType",
              DecisionTypeConstants.DECISION_TYPE_CREATE);
          if (empTerminationObj.list().size() > 0) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(
                OBMessageUtils.messageBD(OBMessageUtils.messageBD("Ehcm_Termination_ExistingEmp")));
            bundle.setResult(obError);
            return;
          }
        }

        // update status as Issued and set decision date for all cases
        termination.setSueDecision(true);
        termination.setDecisionDate(new Date());
        termination.setDecisionStatus("I");
        OBDal.getInstance().save(termination);
        OBDal.getInstance().flush();

        // get employment Information for getting the values Location,payroll,payscale
        OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " as e where ehcmEmpPerinfo.id='" + termination.getEhcmEmpPerinfo().getId()
                + "' and e.enabled='Y' order by e.creationDate desc");
        empInfo.setMaxResult(1);
        if (empInfo.list().size() > 0) {
          info = empInfo.list().get(0);
        }

        // Create & update Cases
        if (termination.getDecisionType().equals("CR")
            || termination.getDecisionType().equals("UP")) {
          EmploymentInfo employInfo = null;
          // Create a record in Employement Information Window
          if (termination.getDecisionType().equals("CR")) {
            employInfo = OBProvider.getInstance().get(EmploymentInfo.class);
          } else {
            employInfo = info;
          }
          employInfo.setChangereason("T");
          employInfo.setChangereasoninfo(termination.getEhcmTerminationReason().getSearchKey());

          employInfo.setDepartmentName(termination.getDepartmentCode().getName());
          employInfo.setDeptcode(termination.getDepartmentCode());
          employInfo.setGrade(termination.getGrade());
          ehcmpayscaleline line = OBDal.getInstance().get(ehcmpayscaleline.class,
              termination.getEhcmPayscaleline().getId());
          employInfo.setEhcmPayscale(line.getEhcmPayscale());
          employInfo.setEmpcategory(termination.getEmployeeCategory().getId());
          employInfo.setEmployeeno(terminationView.getEhcmEmpPerinfo().getSearchKey());
          employInfo.setEhcmPayscaleline(line);
          employInfo.setEmploymentgrade(termination.getEmploymentGrade());
          employInfo.setJobcode(termination.getPosition().getEhcmJobs());
          employInfo.setPosition(termination.getPosition());
          employInfo.setJobtitle(termination.getPosition().getJOBName().getJOBTitle());
          employInfo.setLocation(info.getLocation());
          if (info.getEhcmPayrollDefinition() != null)
            employInfo.setEhcmPayrollDefinition(info.getEhcmPayrollDefinition());
          if (termination.getSectionCode() != null) {
            employInfo.setSectionName(termination.getSectionCode().getName());
            employInfo.setSectioncode(termination.getSectionCode());
          }
          employInfo.setEhcmEmpPerinfo(terminationView.getEhcmEmpPerinfo());
          employInfo.setStartDate(termination.getTerminationDate());
          employInfo.setEndDate(null);
          employInfo.setAlertStatus("TE");
          employInfo.setEhcmEmpTermination(termination);
          employInfo.setDecisionNo(termination.getDecisionNo());
          employInfo.setDecisionDate(termination.getDecisionDate());
          supervisorId = UtilityDAO.getSupervisorforEmployee(
              termination.getEhcmEmpPerinfo().getId(), termination.getClient().getId());
          employInfo.setEhcmEmpSupervisor(supervisorId);
          /* secondary */
          employInfo.setSecpositionGrade(info.getSecpositionGrade());
          employInfo.setSecpositionGrade(info.getSecpositionGrade());
          employInfo.setSecjobno(info.getSecjobno());
          employInfo.setSecjobcode(info.getSecjobcode());
          employInfo.setSecjobtitle(info.getSecjobtitle());
          employInfo.setSECDeptCode(info.getSECDeptCode());
          employInfo.setSECDeptName(info.getSECDeptName());
          if (info.getSECSectionCode() != null) {
            employInfo.setSECSectionCode(info.getSECSectionCode());
            employInfo.setSECSectionName(info.getSECSectionName());
          }
          employInfo.setSECLocation(info.getSECLocation());
          employInfo.setSECStartdate(info.getSECStartdate());
          employInfo.setSECEnddate(info.getSECEnddate());
          employInfo.setSECDecisionNo(info.getSECDecisionNo());
          employInfo.setSECDecisionDate(info.getSECDecisionDate());
          employInfo.setSECChangeReason(info.getSECChangeReason());
          employInfo.setSECEmploymentNumber(info.getSECEmploymentNumber());

          OBDal.getInstance().save(employInfo);
          OBDal.getInstance().flush();

          EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
              termination.getEhcmEmpPerinfo().getId());
          person.setEmploymentStatus("TE");
          person.setEnabled(false);
          OBDal.getInstance().save(person);
          OBDal.getInstance().flush();

          if (termination.getDecisionType().equals("CR")) {
            Date dateBefore = null;
            // update the endate and active flag for old hiring record.
            OBQuery<EmploymentInfo> empInfoold = OBDal.getInstance().createQuery(
                EmploymentInfo.class,
                " ehcmEmpPerinfo.id='" + termination.getEhcmEmpPerinfo().getId()
                    + "'  and id not in ('" + employInfo.getId()
                    + "') and enabled='Y' and alertStatus='ACT' order by creationDate desc ");

            empInfoold.setMaxResult(1);
            if (empInfoold.list().size() > 0) {
              EmploymentInfo empinfo = empInfoold.list().get(0);
              empinfo.setUpdated(new java.util.Date());
              empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
              Date startdate = empinfo.getStartDate();
              dateBefore = new Date(
                  termination.getTerminationDate().getTime() - 1 * 24 * 3600 * 1000);
              log.debug("stat:" + startdate);
              log.debug("updateposition.getStartDate():" + empinfo.getStartDate());
              log.debug("updateposition.compareTo():"
                  + startdate.compareTo(termination.getTerminationDate()));
              log.debug("updateposition.dateBefore():" + dateBefore);

              if (startdate.compareTo(termination.getTerminationDate()) == 0)
                empinfo.setEndDate(empinfo.getStartDate());
              else
                empinfo.setEndDate(dateBefore);
              empinfo.setEnabled(false);
              empinfo.setSecpositionGrade(null);
              empinfo.setSecjobno(null);
              empinfo.setSecjobcode(null);
              empinfo.setSecjobtitle(null);
              empinfo.setSECDeptName(null);
              empinfo.setSECDeptCode(null);
              empinfo.setSECSectionCode(null);
              empinfo.setSECSectionName(null);
              empinfo.setSECLocation(null);
              empinfo.setSECStartdate(null);
              empinfo.setSECEnddate(null);
              empinfo.setSECDecisionDate(null);
              empinfo.setSECDecisionNo(null);
              empinfo.setSECChangeReason(null);
              empinfo.setSECEmploymentNumber(null);
              empinfo.setAlertStatus("INACT");
              OBDal.getInstance().save(empinfo);
              OBDal.getInstance().flush();

              // position release
              EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
                  termination.getPosition().getId());
              log.debug("employInfo.getEhcmEmpTransfer:" + termination.getPosition().getJOBNo());
              /*
               * Task No.6797 pos.setAssignedEmployee(null); OBDal.getInstance().save(pos);
               * OBDal.getInstance().flush();
               */

              /*
               * Task No.6797 OBQuery<EmployeeDelegation> del = OBDal.getInstance()
               * .createQuery(EmployeeDelegation.class, " ehcmEmploymentInfo.id='" + empinfo.getId()
               * + "' and enabled='Y' order by creationDate desc "); del.setMaxResult(1); if
               * (del.list().size() > 0) { EmployeeDelegation delegation = del.list().get(0);
               * delegation.setEhcmEmploymentInfo(employInfo); OBDal.getInstance().save(delegation);
               * OBDal.getInstance().flush();
               * 
               * EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class,
               * delegation.getNewPosition().getId()); log.debug( "employInfo.getEhcmEmpTransfer:" +
               * delegation.getNewPosition().getPosition()); position.setDelegatedEmployee(null);
               * OBDal.getInstance().save(position); OBDal.getInstance().flush(); }
               */
              log.debug("getEndDate:" + empinfo.getEndDate());
            }

          }
          log.debug("inside:" + termination.getDecisionType());
          if (termination.getDecisionType().equals("UP")) {
            // update the endate and active flag for old hiring record.
            OBQuery<EmploymentInfo> empInfoold = OBDal.getInstance().createQuery(
                EmploymentInfo.class,
                " ehcmEmpPerinfo.id='" + termination.getEhcmEmpPerinfo().getId()
                    + "'  and id not in ('" + employInfo.getId()
                    + "') and enabled='N' order by creationDate desc ");
            empInfoold.setMaxResult(1);
            log.debug("empInfoold:" + empInfoold.list().size());
            if (empInfoold.list().size() > 0) {
              EmploymentInfo empinfo = empInfoold.list().get(0);
              empinfo.setUpdated(new java.util.Date());
              empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));

              Date startdate = empinfo.getStartDate();
              Date dateBefore = new Date(
                  termination.getTerminationDate().getTime() - 1 * 24 * 3600 * 1000);
              log.debug("stat:" + startdate);
              log.debug("updateposition.getStartDate():" + empinfo.getStartDate());
              log.debug("updateposition.compareTo():"
                  + startdate.compareTo(termination.getTerminationDate()));
              log.debug("updateposition.dateBefore():" + dateBefore);
              if (startdate.compareTo(termination.getTerminationDate()) == 0)
                empinfo.setEndDate(empinfo.getStartDate());
              else
                empinfo.setEndDate(dateBefore);
              OBDal.getInstance().save(empinfo);
              OBDal.getInstance().flush();
            }
          }

          // create employee details
          if (termination.getEhcmEmpPerinfo() != null) {
            EhcmEmpPerInfo employee = null;
            EhcmEmpPerInfo newperson = null;
            EhcmEmpPerInfo cancelemployee = null;
            ehcmempstatus ehcmempstatus = null;

            if (termination.getDecisionType().equals("CR")) {
              log.debug("terminationcreate");
              ehcmempstatus = OBProvider.getInstance().get(ehcmempstatus.class);
              ehcmempstatus.setClient(termination.getClient());
              ehcmempstatus.setOrganization(termination.getOrganization());
              ehcmempstatus.setCreationDate(new java.util.Date());
              ehcmempstatus.setCreatedBy(termination.getCreatedBy());
              ehcmempstatus.setUpdated(new java.util.Date());
              ehcmempstatus.setUpdatedBy(termination.getUpdatedBy());
              ehcmempstatus.setEnabled(false);
              ehcmempstatus.setEhcmEmpPerinfo(terminationView.getEhcmEmpPerinfo());
              ehcmempstatus.setDecisionno(termination.getDecisionNo());
              ehcmempstatus.setEhcmEmpTermination(termination);

              // ehcmempstatus.setStartDate(dao.convertGregorian(termination.getTerminationDate()
              // .toString()));
              ehcmempstatus.setStartDate(termination.getTerminationDate());
              // ehcmempstatus.setTodate(dao.convertGregorian(enddate));
              ehcmempstatus.setStatus("TE");
              OBDal.getInstance().save(ehcmempstatus);
              OBDal.getInstance().flush();
            }
            /*
             * employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
             * log.debug("employee create:" + employee.getId()); get actionn type List<EmployeeVO>
             * actls = dao.getactionType(vars.getClient(), null, "CT"); for (EmployeeVO vo1 : actls)
             * { actTypeId = vo1.getActTypeId(); cancelacction = vo1.getCancelaction(); break; }
             * 
             * newperson = (EhcmEmpPerInfo) DalUtil.copy(employee, false); log.debug("employee:" +
             * employee); newperson.setCreatedBy(OBDal.getInstance().get(User.class,
             * vars.getUser())); newperson.setCreationDate(new java.util.Date());
             * newperson.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
             * newperson.setUpdated(new java.util.Date());
             * newperson.setEhcmActiontype(OBDal.getInstance().get(EhcmActiontype.class,
             * actTypeId)); newperson.setStatus("TE"); newperson.setCancelEmployee(employee); if
             * (cancelacction.equals("Y")) newperson.setPersonType(employee.getEhcmActiontype());
             * newperson.setStartDate(termination.getTerminationDate());
             * OBDal.getInstance().save(newperson); OBDal.getInstance().flush();
             * 
             * // update the ex employee
             * 
             * Date startdate = employee.getStartDate(); Date dateBefore = new
             * Date(termination.getTerminationDate().getTime() - 1 * 24 * 3600 1000);
             * log.debug("stat:" + startdate); log.debug("updateposition.getStartDate():" +
             * employee.getStartDate()); log.debug("updateposition.compareTo():" +
             * startdate.compareTo(termination.getTerminationDate()));
             * log.debug("updateposition.dateBefore():" + dateBefore);
             * 
             * if (startdate.compareTo(termination.getTerminationDate()) == 0)
             * employee.setEndDate(employee.getStartDate()); else employee.setEndDate(dateBefore);
             * employee.setEnabled(true); OBDal.getInstance().save(employee);
             * OBDal.getInstance().flush();
             */

            else if (termination.getDecisionType().equals("UP")) {
              log.debug("update");
              /*
               * OBQuery<EhcmEmpPerInfo> empperson = OBDal.getInstance().createQuery(
               * EhcmEmpPerInfo.class, " cancelEmployee.id='" + employeeId + "'"); if
               * (empperson.list().size() > 0) { perinfo = empperson.list().get(0);
               * 
               * log.debug("perinfo update:" + perinfo.getStatus());
               * 
               * perinfo.setStartDate(termination.getTerminationDate());
               * OBDal.getInstance().save(perinfo); OBDal.getInstance().flush(); }
               */
              OBQuery<ehcmempstatus> empstatus = OBDal.getInstance()
                  .createQuery(ehcmempstatus.class, " ehcmEmpPerinfo.id='" + employeeId + "'");
              empstatus.setMaxResult(1);
              if (empstatus.list().size() > 0) {
                ehcmempstatus employeestatus = empstatus.list().get(0);

                employeestatus.setUpdated(new java.util.Date());
                employeestatus.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
                employeestatus.setDecisionno(termination.getDecisionNo());
                employeestatus.setStartDate(termination.getTerminationDate());
                employeestatus.setStatus("TE");
                // ehcmempstatus.setEhcmEmpTermination(termination);
                employeestatus.setEhcmEmpTermination(termination);
                OBDal.getInstance().save(employeestatus);
                OBDal.getInstance().flush();

              }

              cancelemployee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
              log.debug("cancelemployee update:" + cancelemployee.getStatus());

              Date startdate = cancelemployee.getStartDate();
              Date dateBefore = new Date(
                  termination.getTerminationDate().getTime() - 1 * 24 * 3600 * 1000);
              log.debug("stat:" + startdate);
              log.debug("updateposition.getStartDate():" + cancelemployee.getStartDate());
              log.debug("updateposition.compareTo():"
                  + startdate.compareTo(termination.getTerminationDate()));
              log.debug("updateposition.dateBefore():" + dateBefore);

              if (startdate.compareTo(termination.getTerminationDate()) == 0)
                cancelemployee.setEndDate(cancelemployee.getStartDate());
              else
                cancelemployee.setEndDate(dateBefore);
              cancelemployee.setEnabled(true);
              OBDal.getInstance().save(cancelemployee);
              OBDal.getInstance().flush();

              // update old emp termination as inactive
              EHCMEMPTermination oldTermination = termination.getOriginalDecisionsNo();
              oldTermination.setEnabled(false);
              OBDal.getInstance().save(oldTermination);
              OBDal.getInstance().flush();

            }
          }
          //
        }
        // cancel case
        else if (termination.getDecisionType().equals("CA")) {

          // make suspension as active (Incase Termination was Created from Employee Suspension)
          /*
           * if (termination.getEhcmEmploymentInfo().getEhcmEmpSuspension() != null) {
           * termination.getEhcmEmploymentInfo().getEhcmEmpSuspension().setEnabled(true);
           * OBDal.getInstance().save(termination.getEhcmEmploymentInfo().getEhcmEmpSuspension());
           * OBDal.getInstance().flush(); }
           */
          if (termination.getEhcmEmpSuspension() != null) {
            termination.getEhcmEmpSuspension().setEnabled(true);
            OBDal.getInstance().save(termination.getEhcmEmpSuspension());
            OBDal.getInstance().flush();
          }
          // update the acive flag='Y' and enddate is null for recently update record
          OBQuery<EmploymentInfo> originalemp = OBDal.getInstance().createQuery(
              EmploymentInfo.class, " ehcmEmpPerinfo.id='" + termination.getEhcmEmpPerinfo().getId()
                  + "' and enabled='N' order by creationDate desc ");
          originalemp.setMaxResult(1);
          if (originalemp.list().size() > 0) {
            EmploymentInfo empinfo = originalemp.list().get(0);
            log.debug("getChangereasoncancel12:" + empinfo.getId());
            empinfo.setUpdated(new java.util.Date());
            empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
            empinfo.setEndDate(null);
            empinfo.setEnabled(true);
            empinfo.setAlertStatus("ACT");
            empinfo.setEhcmEmpTransfer(null);
            OBDal.getInstance().save(empinfo);
            OBDal.getInstance().flush();

            OBQuery<EmployeeDelegation> del = OBDal.getInstance().createQuery(
                EmployeeDelegation.class,
                " ehcmEmpPerinfo.id='" + termination.getEhcmEmpPerinfo().getId()
                    + "' and enabled='Y' order by creationDate desc");
            del.setMaxResult(1);
            if (del.list().size() > 0) {
              EmployeeDelegation delegation = del.list().get(0);
              log.debug("delegation:" + delegation.getEhcmEmploymentInfo().getId());
              delegation.setEhcmEmploymentInfo(empinfo);
              log.debug("delegation:" + delegation.getId());
              OBDal.getInstance().save(delegation);
              OBDal.getInstance().flush();
              OBDal.getInstance().refresh(delegation);
              log.debug("delegation:" + delegation.getEhcmEmploymentInfo().getId());

              if (delegation.getNewPosition() != null) {
                EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
                    delegation.getNewPosition().getId());
                empinfo.setSecpositionGrade(objPosition.getGrade());
                empinfo.setSecjobno(objPosition);
                empinfo.setSecjobcode(objPosition.getEhcmJobs());
                empinfo.setSecjobtitle(objPosition.getEhcmJobs().getJOBTitle());
              }
              if (delegation.getNewDepartment() != null) {
                empinfo.setSECDeptName(delegation.getNewDepartment().getName());
                empinfo.setSECDeptCode(delegation.getNewDepartment());
              }
              if (delegation.getNewSection() != null) {
                empinfo.setSECSectionCode(delegation.getNewSection());
                empinfo.setSECSectionName(delegation.getNewSection().getName());
                if (delegation.getNewSection().getEhcmLocation() != null) {
                  empinfo.setSECLocation(
                      delegation.getNewSection().getEhcmLocation().getLocationName());
                }
              }
              if (delegation.getStartDate() != null) {
                empinfo.setSECStartdate(delegation.getStartDate());
              }
              if (delegation.getEndDate() != null) {
                empinfo.setSECEnddate(delegation.getEndDate());
              }
              if (delegation.getDecisionDate() != null) {
                empinfo.setSECDecisionDate(delegation.getDecisionDate());
              }
              empinfo.setSECDecisionNo(delegation.getDecisionNo());
              empinfo.setSECChangeReason(delegation.getDelegationType());
              empinfo.setSECEmploymentNumber(delegation.getEhcmEmpPerinfo().getSearchKey());
              OBDal.getInstance().save(empinfo);
              OBDal.getInstance().flush();

            }
            // assigned position
            EhcmPosition newpos = OBDal.getInstance().get(EhcmPosition.class,
                empinfo.getPosition().getId());
            /*
             * Task No.6797 newpos.setAssignedEmployee(OBDal.getInstance().get(EmployeeView.class,
             * termination.getEhcmEmpPerinfo().getId())); OBDal.getInstance().save(newpos);
             * OBDal.getInstance().flush();
             */

            // delegate position
            /*
             * OBQuery<EmployeeDelegation> delg = OBDal.getInstance().createQuery(
             * EmployeeDelegation.class, " ehcmEmpPerinfo.id='" +
             * termination.getEhcmEmpPerinfo().getId() +
             * "' and enabled='Y' order by creationDate desc"); delg.setMaxResult(1); if
             * (delg.list().size() > 0) { EmployeeDelegation delegation = delg.list().get(0); if
             * (delegation.getNewPosition() != null) { EhcmPosition position =
             * OBDal.getInstance().get(EhcmPosition.class, delegation.getNewPosition().getId());
             * log.debug( "employInfo.getEhcmEmpTransfer:" +
             * delegation.getNewPosition().getPosition());
             * position.setDelegatedEmployee(OBDal.getInstance().get(EmployeeView.class,
             * termination.getEhcmEmpPerinfo().getId())); OBDal.getInstance().save(position);
             * OBDal.getInstance().flush(); OBDal.getInstance().flush(); } }
             */

            // remove the recent record
            OBQuery<EmploymentInfo> employInfo = OBDal.getInstance().createQuery(
                EmploymentInfo.class,
                " ehcmEmpPerinfo.id='" + termination.getEhcmEmpPerinfo().getId()
                    + "'  and enabled='Y' and id not in ('" + empinfo.getId()
                    + "') order by creationDate desc ");
            employInfo.setMaxResult(1);
            if (employInfo.list().size() > 0) {
              EmploymentInfo empInfor = employInfo.list().get(0);

              OBQuery<EmployeeDelegation> delegate = OBDal.getInstance().createQuery(
                  EmployeeDelegation.class,
                  " ehcmEmploymentInfo.id='" + empInfor.getId() + "'  order by creationDate desc");
              if (delegate.list().size() > 0) {
                for (EmployeeDelegation delgate : delegate.list()) {
                  delgate.setEhcmEmploymentInfo(empinfo);
                  OBDal.getInstance().save(delgate);
                  OBDal.getInstance().flush();
                }
              }
              OBDal.getInstance().remove(empInfor);
              OBDal.getInstance().flush();

            }
          }
          // change the status as terminate employment

          EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
              termination.getEhcmEmpPerinfo().getId());
          // update business partner table is in inactive
          if (person.getEhcmActiontype().getCode().equals("HE")) {
            OBQuery<BusinessPartner> partner = OBDal.getInstance().createQuery(
                BusinessPartner.class,
                " ehcmEmpPerinfo.id='" + termination.getEhcmEmpPerinfo().getId() + "'");
            partner.setMaxResult(1);
            if (partner.list().size() > 0) {
              BusinessPartner par = partner.list().get(0);
              par.setActive(true);
              OBDal.getInstance().save(par);
              OBDal.getInstance().flush();
            }
          }
          // delete the terminate employee

          EhcmEmpPerInfo cancelperson = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          log.debug("cancelperson:" + cancelperson.getSearchKey());
          cancelperson.setEndDate(null);
          cancelperson.setEnabled(true);
          OBDal.getInstance().save(cancelperson);
          OBDal.getInstance().flush();

          // remove the employee status
          log.debug("Cancel");
          OBQuery<ehcmempstatus> employeestatus = OBDal.getInstance()
              .createQuery(ehcmempstatus.class, " ehcmEmpPerinfo.id='" + employeeId + "'");
          if (employeestatus.list().size() > 0) {
            ehcmempstatus employee = employeestatus.list().get(0);
            OBDal.getInstance().remove(employee);
            OBDal.getInstance().flush();
          }

          EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class,
              termination.getEhcmEmpPerinfo().getId());
          employee.setEmploymentStatus("AC");
          person.setEnabled(true);
          OBDal.getInstance().save(employee);
          OBDal.getInstance().flush();

          // update old emp termination as inactive
          EHCMEMPTermination oldTermination = termination.getOriginalDecisionsNo();
          oldTermination.setEnabled(false);
          OBDal.getInstance().save(oldTermination);
          OBDal.getInstance().flush();

          termination.setEnabled(false);
          OBDal.getInstance().save(termination);
          OBDal.getInstance().flush();

        }

        if (!termination.getDecisionType().equals("CA")) {

          // update position employee history records with end date for assigned emp
          positionEmpHist.updateEndDateInPositionEmployeeHisotry(
              terminationView.getEhcmEmpPerinfo(), termination.getPosition(),
              termination.getTerminationDate(), null, null, null, null, null, termination, null);
          // update position employee history records with end date for delegated emp
          positionEmpHist.updateEndDateForDelegatedEmployee(terminationView.getEhcmEmpPerinfo(),
              termination.getTerminationDate(), null, termination, termination.getDecisionType());
        } else {
          positionEmpHist.updateEndDateInPositionEmployeeHisotry(
              terminationView.getEhcmEmpPerinfo(), termination.getPosition(), null, null, null,
              null, null, null, termination, null);
          // update position employee history records with end date for delegated emp
          positionEmpHist.updateEndDateForDelegatedEmployee(terminationView.getEhcmEmpPerinfo(),
              null, null, termination, termination.getDecisionType());
        }

        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("Ehcm_Submit_Process"));
        bundle.setResult(obError);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
      }
    } catch (Exception e) {
      e.printStackTrace();
      OBDal.getInstance().rollbackAndClose();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
