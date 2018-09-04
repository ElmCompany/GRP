package sa.elm.ob.hcm.ad_process.EmployeeDelegation;

/**
 * 
 * @author Kiruthika
 * 
 */
import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
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

      EmployeeDelegation objDelegation = OBDal.getInstance().get(EmployeeDelegation.class,
          delegationId);

      OBQuery<EmployeeDelegation> delegation = OBDal.getInstance().createQuery(
          EmployeeDelegation.class,
          " ehcmEmpPerinfo.id='" + objDelegation.getEhcmEmpPerinfo().getId() + "' and id != '"
              + objDelegation.getId() + "' order by creationDate desc ");
      delegation.setMaxResult(1);

      if (delegation.list().size() > 0) {
        EmployeeDelegation empDel = delegation.list().get(0);
        OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ehcmEmpPerinfo.id='" + empDel.getEhcmEmpPerinfo().getId()
                + "' and enabled='Y'  and alertStatus='ACT' order by creationDate desc ");

        if (!empDel.getDecisionType().equals("CA")) {
          empInfo.setMaxResult(1);
          if (empInfo.list().size() > 0) {
            EmploymentInfo empinfo = empInfo.list().get(0);
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
            OBDal.getInstance().save(empinfo);
          }
        } else {
          empInfo.setMaxResult(1);
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
        result = OBErrorBuilder.buildMessage(null, "success", "@Ehcm_EmpDelegationReactivated@");
        bundle.setResult(result);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
      }
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
