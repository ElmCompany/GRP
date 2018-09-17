package sa.elm.ob.hcm.ad_process.EmployeeDelegation;

/**
 * 
 * @author Kiruthika
 * 
 */
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.scm.EscmLocation;

/**
 * This class is used to reactivate employee delegation
 */
public class EmpDelegationReactivate implements Process {
  private static final Logger log = Logger.getLogger(EmpDelegationReactivate.class);

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    final String delegationId = (String) bundle.getParams().get("Ehcm_Emp_Delegation_ID")
        .toString();
    OBError result;

    try {
      OBContext.setAdminMode(true);

      HttpServletRequest request = RequestContext.get().getRequest();
      VariablesSecureApp vars = new VariablesSecureApp(request);
      AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();

      EmployeeDelegation objDelegation = OBDal.getInstance().get(EmployeeDelegation.class,
          delegationId);

      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId and "
              + "((startDate >=:start and to_date(to_char(coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <=:end) or"
              + " (startDate <=:end and to_date(to_char(coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') >=:start))"
              + "order by creationDate desc ");
      empInfo.setNamedParameter("employeeId", objDelegation.getEhcmEmpPerinfo().getId());
      empInfo.setNamedParameter("start", objDelegation.getStartDate());
      empInfo.setNamedParameter("end", objDelegation.getEndDate());

      if (!objDelegation.getDecisionType().equals("CR")) {

        OBQuery<EmployeeDelegation> delegation = OBDal.getInstance().createQuery(
            EmployeeDelegation.class,
            " ehcmEmpPerinfo.id=:empPerInfoId and id = :originalDecisionNo");
        delegation.setNamedParameter("empPerInfoId", objDelegation.getEhcmEmpPerinfo().getId());
        delegation.setNamedParameter("originalDecisionNo",
            objDelegation.getOriginalDecisionNo().getId());

        EmployeeDelegation empDel = delegation.list().get(0);

        if (empInfo.list().size() > 0) {
          for (int empInfoList = 0; empInfoList < empInfo.list().size(); empInfoList++) {
            EmploymentInfo empinfo = empInfo.list().get(empInfoList);
            if (empDel.getNewPosition() != null) {
              EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
                  empDel.getNewPosition().getId());
              empinfo.setSecpositionGrade(objPosition.getGrade());
              empinfo.setSecjobno(objPosition);
              empinfo.setSecjobcode(objPosition.getEhcmJobs());
              empinfo.setSecjobtitle(objPosition.getEhcmJobs().getJOBTitle());
            }
            if (empDel.getNewDepartment() != null) {
              empinfo.setSECDeptName(empDel.getNewDepartment().getName());
              empinfo.setSECDeptCode(empDel.getNewDepartment());
              empinfo.setAssignedDepartment(empDel.getNewDepartment());
              if (empDel.getNewDepartment().getEhcmEscmLoc() != null) {
                EscmLocation loc = OBDal.getInstance().get(EscmLocation.class,
                    empDel.getNewDepartment().getEhcmEscmLoc().getId());
                empinfo.setSECLocation(loc.getLocationName());
              }
            }
            if (empDel.getNewSection() != null) {
              empinfo.setSECSectionCode(empDel.getNewSection());
              empinfo.setSECSectionName(empDel.getNewSection().getName());
            }
            if (empDel.getStartDate() != null) {
              empinfo.setSECStartdate(empDel.getStartDate());
            }
            if (empDel.getEndDate() != null) {
              empinfo.setSECEnddate(empDel.getEndDate());
            }
            if (empDel.getDecisionDate() != null) {
              empinfo.setSECDecisionDate(empDel.getDecisionDate());
            }
            empinfo.setSECDecisionNo(empDel.getDecisionNo());
            empinfo.setSECChangeReason(empDel.getDelegationType());
            empinfo.setSECEmploymentNumber(empDel.getEhcmEmpPerinfo().getSearchKey());
            empDel.setReactivate(true);
            OBDal.getInstance().save(empinfo);
          }
        }
      } else {
        // Previous delegation
        OBQuery<EmployeeDelegation> preDelegation = OBDal.getInstance().createQuery(
            EmployeeDelegation.class,
            " ehcmEmpPerinfo.id=:empPerInfoId and created < :created order by created desc limit 1");
        preDelegation.setNamedParameter("empPerInfoId", objDelegation.getEhcmEmpPerinfo().getId());
        preDelegation.setNamedParameter("created", objDelegation.getCreationDate());

        if (preDelegation.list().size() > 0) {

          EmployeeDelegation preEmpDel = preDelegation.list().get(0);

          if (empInfo.list().size() > 0) {

            EmploymentInfo empinfo = empInfo.list().get(0);
            if (preEmpDel.getNewPosition() != null) {
              EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
                  preEmpDel.getNewPosition().getId());
              empinfo.setSecpositionGrade(objPosition.getGrade());
              empinfo.setSecjobno(objPosition);
              empinfo.setSecjobcode(objPosition.getEhcmJobs());
              empinfo.setSecjobtitle(objPosition.getEhcmJobs().getJOBTitle());
            }
            if (preEmpDel.getNewDepartment() != null) {
              empinfo.setSECDeptName(preEmpDel.getNewDepartment().getName());
              empinfo.setSECDeptCode(preEmpDel.getNewDepartment());
              empinfo.setAssignedDepartment(preEmpDel.getNewDepartment());
              if (preEmpDel.getNewDepartment().getEhcmEscmLoc() != null) {
                EscmLocation loc = OBDal.getInstance().get(EscmLocation.class,
                    preEmpDel.getNewDepartment().getEhcmEscmLoc().getId());
                empinfo.setSECLocation(loc.getLocationName());
              }
            }
            if (preEmpDel.getNewSection() != null) {
              empinfo.setSECSectionCode(preEmpDel.getNewSection());
              empinfo.setSECSectionName(preEmpDel.getNewSection().getName());
            }
            if (preEmpDel.getStartDate() != null) {
              empinfo.setSECStartdate(preEmpDel.getStartDate());
            }
            if (preEmpDel.getEndDate() != null) {
              empinfo.setSECEnddate(preEmpDel.getEndDate());
            }
            if (preEmpDel.getDecisionDate() != null) {
              empinfo.setSECDecisionDate(preEmpDel.getDecisionDate());
            }
            empinfo.setSECDecisionNo(preEmpDel.getDecisionNo());
            empinfo.setSECChangeReason(preEmpDel.getDelegationType());
            empinfo.setSECEmploymentNumber(preEmpDel.getEhcmEmpPerinfo().getSearchKey());
            preEmpDel.setReactivate(true);
            OBDal.getInstance().save(empinfo);
          }
        } else {
          if (empInfo.list().size() > 0) {

            EmploymentInfo empinfo = empInfo.list().get(0);

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
          }
        }
      }
      objDelegation.setDecisionStatus("UP");
      objDelegation.setSueDecision(false);

      if (objDelegation.getDecisionType().equals("CR")) {
        if (objDelegation.getNewPosition() != null) {

          EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class,
              objDelegation.getNewPosition().getId());
          assingedOrReleaseEmpInPositionDAO
              .deletePositionEmployeeHisotry(objDelegation.getEhcmEmpPerinfo(), position);
        }
      } else if (objDelegation.getDecisionType().equals("UP")) {

        if (objDelegation.getNewPosition() != null) {
          EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
              objDelegation.getNewPosition().getId());
          assingedOrReleaseEmpInPositionDAO
              .deletePositionEmployeeHisotry(objDelegation.getEhcmEmpPerinfo(), objPosition);
        }

        EmployeeDelegation oldDelegetaion = objDelegation.getOriginalDecisionNo();
        if (objDelegation.getNewPosition() != null
            && !objDelegation.getNewPosition().equals(oldDelegetaion.getNewPosition())
            || (objDelegation.getStartDate().compareTo(oldDelegetaion.getStartDate()) != 0)
            || (objDelegation.getEndDate().compareTo(oldDelegetaion.getEndDate()) != 0)) {
          if (oldDelegetaion.getNewPosition() != null) {
            EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
                oldDelegetaion.getNewPosition().getId());

            assingedOrReleaseEmpInPositionDAO.insertPositionEmployeeHisotry(
                oldDelegetaion.getClient(), oldDelegetaion.getOrganization(),
                oldDelegetaion.getEhcmEmpPerinfo(), oldDelegetaion, oldDelegetaion.getStartDate(),
                oldDelegetaion.getEndDate(), oldDelegetaion.getDecisionNo(),
                oldDelegetaion.getDecisionDate(), objPosition, vars, null, null, null);
          }
        }
      } else if (objDelegation.getDecisionType().equals("CA")) {
        EmployeeDelegation oldDelegetaion = objDelegation.getOriginalDecisionNo();

        if (oldDelegetaion.getNewPosition() != null) {
          EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
              oldDelegetaion.getNewPosition().getId());

          assingedOrReleaseEmpInPositionDAO.insertPositionEmployeeHisotry(
              oldDelegetaion.getClient(), oldDelegetaion.getOrganization(),
              oldDelegetaion.getEhcmEmpPerinfo(), oldDelegetaion, oldDelegetaion.getStartDate(),
              oldDelegetaion.getEndDate(), oldDelegetaion.getDecisionNo(),
              oldDelegetaion.getDecisionDate(), objPosition, vars, null, null, null);
        }
      }
      result = OBErrorBuilder.buildMessage(null, "success", "@Ehcm_EmpDelegationReactivated@");
      bundle.setResult(result);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    } catch (Exception e) {
      log.error("exception :", e);
      OBDal.getInstance().rollbackAndClose();
      e.printStackTrace();
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}