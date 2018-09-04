package sa.elm.ob.hcm.ad_process.EmployeeDelegation;

import java.text.DateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.hcm.util.Utility;
import sa.elm.ob.scm.EscmLocation;

public class EmpDelegationIssuance implements Process {
  private static final Logger log = Logger.getLogger(EmpDelegationIssuance.class);
  private final OBError obError = new OBError();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Issue the position");
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    final String delegationId = (String) bundle.getParams().get("Ehcm_Emp_Delegation_ID")
        .toString();
    EmployeeDelegation objDelegation = OBDal.getInstance().get(EmployeeDelegation.class,
        delegationId);
    log.debug("transferId:" + delegationId);
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
    DateFormat yearFormat = sa.elm.ob.utility.util.Utility.YearFormat;
    try {
      OBContext.setAdminMode(true);
      log.debug("isSueDecision:" + objDelegation.isSueDecision());
      log.debug("getDecisionType12:" + objDelegation.getDecisionType());
      if (!objDelegation.isSueDecision()) {

        // checking position is available or not

        if (objDelegation.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {

          String delegationQry = "select e.id from Ehcm_Emp_Delegation as e "
              + " where e.ehcmEmpPerinfo.id=:employee and e.enabled='Y' "
              + " and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
              + " and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
              + " or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
              + " and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')))  and e.id<>:delegationId  ";

          Query query = OBDal.getInstance().getSession().createQuery(delegationQry);
          query.setParameter("employee", objDelegation.getEhcmEmpPerinfo().getId());
          query.setParameter("fromdate",
              sa.elm.ob.utility.util.Utility.formatDate(objDelegation.getStartDate()));
          query.setParameter("todate",
              sa.elm.ob.utility.util.Utility.formatDate(objDelegation.getEndDate()));
          query.setParameter("delegationId", objDelegation.getId());

          if (query.list().size() > 0) {
            obError.setType("Error");
            obError.setTitle("Error");
            obError.setMessage(OBMessageUtils.messageBD("Ehcm_Delegation_Not_Possible"));
            bundle.setResult(obError);
            return;
          }
        }

        // checking decision overlap
        if (objDelegation.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
            || (objDelegation.getDecisionType()
                .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE))) {
          JSONObject result = Utility.chkDecisionOverlap(Constants.DELEGATION_OVERLAP,
              sa.elm.ob.utility.util.Utility.formatDate(objDelegation.getStartDate()),
              sa.elm.ob.utility.util.Utility.formatDate(objDelegation.getEndDate()),
              objDelegation.getEhcmEmpPerinfo().getId(), objDelegation.getDelegationType(),
              objDelegation.getId());
          if (result != null && result.has("errorFlag") && result.getBoolean("errorFlag")) {
            if (objDelegation.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
                || (objDelegation.getDecisionType()
                    .equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)
                    && (result.has("delegationId") && !result.getString("delegationId")
                        .equals(objDelegation.getOriginalDecisionNo().getId()))
                    || !result.has("delegationId"))) {
              if (result.has("errormsg")) {
                obError.setType("Error");
                obError.setTitle("Error");
                obError.setMessage(OBMessageUtils.messageBD(result.getString("errormsg")));
                bundle.setResult(obError);
                return;
              }
            }
          }
        }

        // update status as Issued and set decision date
        objDelegation.setSueDecision(true);
        objDelegation.setDecisionDate(new Date());
        objDelegation.setDecisionStatus("I");
        OBDal.getInstance().save(objDelegation);
        OBDal.getInstance().flush();
        // Create Delegation
        if (objDelegation.getDecisionType().equals("CR")) {
          // update secondary info in employment information
          OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id=:employeeId and startDate<=:start and "
                  + "(endDate>=:start or endDate is null) order by creationDate desc ");
          empInfo.setNamedParameter("employeeId", objDelegation.getEhcmEmpPerinfo().getId());
          empInfo.setNamedParameter("start", objDelegation.getStartDate());
          empInfo.setMaxResult(1);
          if (empInfo.list().size() > 0) {
            EmploymentInfo empinfo = empInfo.list().get(0);
            if (objDelegation.getNewPosition() != null) {
              EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
                  objDelegation.getNewPosition().getId());
              empinfo.setSecpositionGrade(objPosition.getGrade());
              empinfo.setSecjobno(objPosition);
              empinfo.setSecjobcode(objPosition.getEhcmJobs());
              empinfo.setSecjobtitle(objPosition.getEhcmJobs().getJOBTitle());
            }
            if (objDelegation.getNewDepartment() != null) {
              empinfo.setSECDeptName(objDelegation.getNewDepartment().getName());
              empinfo.setSECDeptCode(objDelegation.getNewDepartment());
              empinfo.setAssignedDepartment(objDelegation.getNewDepartment());
              if (objDelegation.getNewDepartment().getEhcmEscmLoc() != null) {
                EscmLocation loc = OBDal.getInstance().get(EscmLocation.class,
                    objDelegation.getNewDepartment().getEhcmEscmLoc().getId());
                empinfo.setSECLocation(loc.getLocationName());
              }
            }
            if (objDelegation.getNewSection() != null) {
              empinfo.setSECSectionCode(objDelegation.getNewSection());
              empinfo.setSECSectionName(objDelegation.getNewSection().getName());

              // if (objDelegation.getNewSection().getEhcmLocation() != null) {
              // empinfo.setSECLocation(
              // objDelegation.getNewSection().getEhcmLocation().getLocationName());
              // }
            }
            if (objDelegation.getStartDate() != null) {
              empinfo.setSECStartdate(objDelegation.getStartDate());
            }
            if (objDelegation.getEndDate() != null) {
              empinfo.setSECEnddate(objDelegation.getEndDate());
            }
            if (objDelegation.getDecisionDate() != null) {
              empinfo.setSECDecisionDate(objDelegation.getDecisionDate());
            }
            empinfo.setSECDecisionNo(objDelegation.getDecisionNo());
            empinfo.setSECChangeReason(objDelegation.getDelegationType());
            empinfo.setSECEmploymentNumber(objDelegation.getEhcmEmpPerinfo().getSearchKey());
            OBDal.getInstance().save(empinfo);

          }

          // EhcmEmpPerInfo objEmployee = objDelegation.getEhcmEmpPerinfo();
          if (objDelegation.getNewPosition() != null) {
            // EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
            // objDelegation.getNewPosition().getId());
            /*
             * Task No.6797 objPosition.setDelegatedEmployee(
             * OBDal.getInstance().get(EmployeeView.class, objEmployee.getId()));
             * OBDal.getInstance().save(objPosition);
             */

            EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class,
                objDelegation.getNewPosition().getId());
            assingedOrReleaseEmpInPositionDAO.insertPositionEmployeeHisotry(
                objDelegation.getClient(), objDelegation.getOrganization(),
                objDelegation.getEhcmEmpPerinfo(), objDelegation, objDelegation.getStartDate(),
                objDelegation.getEndDate(), objDelegation.getDecisionNo(),
                objDelegation.getDecisionDate(), position, vars, null, null, null);
          }

        }
        // Update Delegation
        else if (objDelegation.getDecisionType().equals("UP")) {

          // update secondary info in employment information
          OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id=:employeeId and startDate<=:start and "
                  + "(endDate>=:start or endDate is null) order by creationDate desc ");
          empInfo.setNamedParameter("employeeId", objDelegation.getEhcmEmpPerinfo().getId());
          empInfo.setNamedParameter("start", objDelegation.getStartDate());
          empInfo.setMaxResult(1);
          if (empInfo.list().size() > 0) {
            EmploymentInfo empinfo = empInfo.list().get(0);
            if (objDelegation.getNewPosition() != null) {
              EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
                  objDelegation.getNewPosition().getId());
              empinfo.setSecpositionGrade(objPosition.getGrade());
              empinfo.setSecjobno(objPosition);
              empinfo.setSecjobcode(objPosition.getEhcmJobs());
              empinfo.setSecjobtitle(objPosition.getEhcmJobs().getJOBTitle());
            }
            if (objDelegation.getNewDepartment() != null) {
              empinfo.setSECDeptName(objDelegation.getNewDepartment().getName());
              empinfo.setSECDeptCode(objDelegation.getNewDepartment());
              empinfo.setAssignedDepartment(objDelegation.getNewDepartment());
              if (objDelegation.getNewDepartment().getEhcmEscmLoc() != null) {
                EscmLocation loc = OBDal.getInstance().get(EscmLocation.class,
                    objDelegation.getNewDepartment().getEhcmEscmLoc().getId());
                empinfo.setSECLocation(loc.getLocationName());
              }
            }
            if (objDelegation.getNewSection() != null) {
              empinfo.setSECSectionCode(objDelegation.getNewSection());
              empinfo.setSECSectionName(objDelegation.getNewSection().getName());
              // if (objDelegation.getNewSection().getEhcmLocation() != null) {
              // empinfo.setSECLocation(
              // objDelegation.getNewSection().getEhcmLocation().getLocationName());
              // }
            }
            if (objDelegation.getStartDate() != null) {
              empinfo.setSECStartdate(objDelegation.getStartDate());
            }
            if (objDelegation.getEndDate() != null) {
              empinfo.setSECEnddate(objDelegation.getEndDate());
            }
            if (objDelegation.getDecisionDate() != null) {
              empinfo.setSECDecisionDate(objDelegation.getDecisionDate());
            }
            empinfo.setSECDecisionNo(objDelegation.getDecisionNo());
            empinfo.setSECChangeReason(objDelegation.getDelegationType());
            empinfo.setSECEmploymentNumber(objDelegation.getEhcmEmpPerinfo().getSearchKey());
            OBDal.getInstance().save(empinfo);

          }

          // update old delegation as inactive
          EmployeeDelegation oldDelegetaion = objDelegation.getOriginalDecisionNo();
          // release employee from old delegation
          if (objDelegation.getNewPosition() != null
              && !objDelegation.getNewPosition().equals(oldDelegetaion.getNewPosition())
              || (objDelegation.getStartDate().compareTo(oldDelegetaion.getStartDate()) != 0)
              || (objDelegation.getEndDate().compareTo(oldDelegetaion.getEndDate()) != 0)) {
            if (oldDelegetaion.getNewPosition() != null) {
              EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
                  oldDelegetaion.getNewPosition().getId());
              // Task No.6797 objPosition.setDelegatedEmployee(null);
              assingedOrReleaseEmpInPositionDAO
                  .deletePositionEmployeeHisotry(oldDelegetaion.getEhcmEmpPerinfo(), objPosition);

            }

            EhcmEmpPerInfo objEmployee = objDelegation.getEhcmEmpPerinfo();
            if (objDelegation.getNewPosition() != null) {
              EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
                  objDelegation.getNewPosition().getId());
              /*
               * Task No.6797 objPosition.setDelegatedEmployee(
               * OBDal.getInstance().get(EmployeeView.class, objEmployee.getId()));
               * OBDal.getInstance().save(objPosition);
               */

              assingedOrReleaseEmpInPositionDAO.insertPositionEmployeeHisotry(
                  objDelegation.getClient(), objDelegation.getOrganization(),
                  objDelegation.getEhcmEmpPerinfo(), objDelegation, objDelegation.getStartDate(),
                  objDelegation.getEndDate(), objDelegation.getDecisionNo(),
                  objDelegation.getDecisionDate(), objPosition, vars, null, null, null);
            }
          }

          oldDelegetaion.setEnabled(false);
          OBDal.getInstance().save(oldDelegetaion);
          OBDal.getInstance().flush();
        }
        // Cancel Delegation
        else if (objDelegation.getDecisionType().equals("CA")) {

          // clear secondary info in employment information
          OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id=:employeeId and startDate<=:start and "
                  + "(endDate>=:start or endDate is null)  order by creationDate desc ");
          empInfo.setNamedParameter("employeeId", objDelegation.getEhcmEmpPerinfo().getId());
          empInfo.setNamedParameter("start", objDelegation.getStartDate());

          log.debug(objDelegation.getEhcmEmpPerinfo().getId());
          log.debug(objDelegation.getOriginalDecisionNo().getId());

          OBQuery<EmployeeDelegation> empDelegation = OBDal.getInstance().createQuery(
              EmployeeDelegation.class,
              " ehcmEmpPerinfo.id=:employeeId and decisionStatus='I' and enabled='Y' and "
                  + "(originalDecisionNo.id !=:decNo or id !=:decNo) and id!=:delId  order by creationDate desc ");
          empDelegation.setNamedParameter("employeeId", objDelegation.getEhcmEmpPerinfo().getId());
          empDelegation.setNamedParameter("decNo", objDelegation.getOriginalDecisionNo().getId());
          empDelegation.setNamedParameter("delId", objDelegation.getId());

          empDelegation.setMaxResult(1);
          empInfo.setMaxResult(1);
          EmploymentInfo empinfo = empInfo.list().get(0);
          if (empInfo.list().size() > 0 && empDelegation.list().size() == 0) {
            empinfo.setSecpositionGrade(null);
            empinfo.setSecjobno(null);
            empinfo.setSecjobcode(null);
            empinfo.setSecjobtitle(null);
            empinfo.setSECDeptName(null);
            empinfo.setSECDeptCode(null);
            empinfo.setAssignedDepartment(null);
            empinfo.setSECSectionCode(null);
            empinfo.setSECSectionName(null);
            empinfo.setSECLocation(null);
            empinfo.setSECStartdate(null);
            empinfo.setSECEnddate(null);
            empinfo.setSECDecisionDate(null);
            empinfo.setSECDecisionNo(null);
            empinfo.setSECChangeReason(null);
            empinfo.setSECEmploymentNumber(null);
            OBDal.getInstance().save(empinfo);

          } else if (empInfo.list().size() > 0 && empDelegation.list().size() > 0) {
            objDelegation = OBDal.getInstance().get(EmployeeDelegation.class,
                empDelegation.list().get(0).getId());
            if (objDelegation.getNewPosition() != null) {
              EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
                  objDelegation.getNewPosition().getId());
              empinfo.setSecpositionGrade(objPosition.getGrade());
              empinfo.setSecjobno(objPosition);
              empinfo.setSecjobcode(objPosition.getEhcmJobs());
              empinfo.setSecjobtitle(objPosition.getEhcmJobs().getJOBTitle());
            }
            if (objDelegation.getNewDepartment() != null) {
              empinfo.setSECDeptName(objDelegation.getNewDepartment().getName());
              empinfo.setSECDeptCode(objDelegation.getNewDepartment());
              empinfo.setAssignedDepartment(objDelegation.getNewDepartment());
              if (objDelegation.getNewDepartment().getEhcmEscmLoc() != null) {
                EscmLocation loc = OBDal.getInstance().get(EscmLocation.class,
                    objDelegation.getNewDepartment().getEhcmEscmLoc().getId());
                empinfo.setSECLocation(loc.getLocationName());
              }
            }
            if (objDelegation.getNewSection() != null) {
              empinfo.setSECSectionCode(objDelegation.getNewSection());
              empinfo.setSECSectionName(objDelegation.getNewSection().getName());
              // if (objDelegation.getNewSection().getEhcmLocation() != null) {
              // empinfo.setSECLocation(
              // objDelegation.getNewSection().getEhcmLocation().getLocationName());
              // }
            }
            if (objDelegation.getStartDate() != null) {
              empinfo.setSECStartdate(objDelegation.getStartDate());
            }
            if (objDelegation.getEndDate() != null) {
              empinfo.setSECEnddate(objDelegation.getEndDate());
            }
            if (objDelegation.getDecisionDate() != null) {
              empinfo.setSECDecisionDate(objDelegation.getDecisionDate());
            }
            empinfo.setSECDecisionNo(objDelegation.getDecisionNo());
            empinfo.setSECChangeReason(objDelegation.getDelegationType());
            empinfo.setSECEmploymentNumber(objDelegation.getEhcmEmpPerinfo().getSearchKey());
            OBDal.getInstance().save(empinfo);
            OBDal.getInstance().save(empinfo);
            objDelegation = OBDal.getInstance().get(EmployeeDelegation.class, delegationId);
          }

          /*
           * Task No.6797 if (objDelegation.getNewPosition() != null) { EhcmPosition objPosition =
           * OBDal.getInstance().get(EhcmPosition.class, objDelegation.getNewPosition().getId());
           * objPosition.setDelegatedEmployee(null); OBDal.getInstance().save(objPosition);
           * OBDal.getInstance().flush(); }
           */

          // update old delegation as inactive
          EmployeeDelegation oldDelegetaion = objDelegation.getOriginalDecisionNo();
          oldDelegetaion.setEnabled(false);
          OBDal.getInstance().save(oldDelegetaion);
          OBDal.getInstance().flush();
          // release employee from old delegation
          if (oldDelegetaion.getNewPosition() != null) {
            EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
                oldDelegetaion.getNewPosition().getId());
            // Task No.6797 objPosition.setDelegatedEmployee(null);
            assingedOrReleaseEmpInPositionDAO
                .deletePositionEmployeeHisotry(objDelegation.getEhcmEmpPerinfo(), objPosition);
          }
          objDelegation.setEnabled(false);
          OBDal.getInstance().save(objDelegation);
          OBDal.getInstance().flush();
        }
      }
      obError.setType("Success");
      obError.setTitle("Success");
      obError.setMessage(OBMessageUtils.messageBD("Ehcm_Submit_Process"));
      bundle.setResult(obError);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    } catch (Exception e) {
      bundle.setResult(obError);
      log.error("exception :", e);
      OBDal.getInstance().rollbackAndClose();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
