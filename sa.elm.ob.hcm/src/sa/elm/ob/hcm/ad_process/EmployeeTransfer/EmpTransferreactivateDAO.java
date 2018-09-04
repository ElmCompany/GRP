package sa.elm.ob.hcm.ad_process.EmployeeTransfer;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.EHCMEmpSupervisorNode;
import sa.elm.ob.hcm.EHCMEmpTransfer;
import sa.elm.ob.hcm.EhcmJoiningWorkRequest;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.hcm.properties.Resource;

public class EmpTransferreactivateDAO {
  private Connection connection = null;
  private static final Logger log = LoggerFactory.getLogger(EmpTransferIssueDecisionDAO.class);

  public EmpTransferreactivateDAO() {
    connection = getDbConnection();
  }

  private Connection getDbConnection() {
    return OBDal.getInstance().getConnection();
  }

  public static void deleteEmpInfo(EHCMEmpTransfer empTransfer, String decisionType) {
    List<EmploymentInfo> empInfoList = null;
    List<EmploymentInfo> prevEmpInfoList = null;
    EmploymentInfo info = null;
    EmploymentInfo prevEmpinfo = null;
    EmployeeDelegation delegation = null;
    try {
      OBContext.setAdminMode();
      AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
      OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "  ehcmEmpPerinfo.id = :employeeId  order by creationDate desc ");
      empInfoObj.setNamedParameter("employeeId", empTransfer.getEhcmEmpPerinfo().getId());

      empInfoObj.setMaxResult(2);
      empInfoList = empInfoObj.list();

      if (empInfoList.size() > 0) {
        info = empInfoList.get(0);
        prevEmpinfo = empInfoList.get(1);

        if (decisionType.equals("CR")) {

          OBQuery<EmployeeDelegation> empDelegation = OBDal.getInstance().createQuery(
              EmployeeDelegation.class,
              "ehcmEmploymentInfo.id = :infoId order by creationDate desc");
          empDelegation.setNamedParameter("infoId", info.getId());
          List<EmployeeDelegation> empDelegationList = empDelegation.list();
          if (empDelegationList.size() > 0) {
            delegation = empDelegationList.get(0);
            delegation.setEhcmEmploymentInfo(prevEmpinfo);
            OBDal.getInstance().save(delegation);
          }
          EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
              empTransfer.getNEWEhcmPosition().getId());

          EmploymentInfo recentEmployeInfo = assingedOrReleaseEmpInPositionDAO
              .getRecentEmploymentInfo(empTransfer.getEhcmEmpPerinfo(), null, empTransfer, null);

          assingedOrReleaseEmpInPositionDAO
              .deletePositionEmployeeHisotry(empTransfer.getEhcmEmpPerinfo(), pos);

          assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
              empTransfer.getEhcmEmpPerinfo(), recentEmployeInfo.getPosition(), null, null, null,
              null, null, null, null, recentEmployeInfo);
          OBDal.getInstance().remove(info);
          prevEmpinfo.setEnabled(true);
          prevEmpinfo.setAlertStatus("ACT");
          prevEmpinfo.setUpdated(new java.util.Date());

          if (prevEmpinfo.getChangereason().equals("H") || prevEmpinfo.getEhcmEmpPromotion() != null
              || (prevEmpinfo.getEhcmEmpSuspension() != null
                  && prevEmpinfo.getChangereason().equals(Constants.SUSPENSION_END))
              || prevEmpinfo.getChangereason().equals("JWRSEC")) {
            prevEmpinfo.setEndDate(null);
          } else if (prevEmpinfo.getEhcmEmpTransfer() != null) {
            if (prevEmpinfo.getEhcmEmpTransfer().getEndDate() != null) {
              prevEmpinfo.setEndDate(prevEmpinfo.getEhcmEmpTransfer().getEndDate());
            } else {
              prevEmpinfo.setEndDate(null);
            }
          } else if (prevEmpinfo.getEhcmEmpTransferSelf() != null) {
            if (prevEmpinfo.getEhcmEmpTransferSelf().getEndDate() != null) {
              prevEmpinfo.setEndDate(prevEmpinfo.getEhcmEmpTransferSelf().getEndDate());
            } else {
              prevEmpinfo.setEndDate(null);
            }
          } else if (prevEmpinfo.getEhcmEmpSuspension() != null) {
            if (prevEmpinfo.getEhcmEmpSuspension().getEndDate() != null) {
              prevEmpinfo.setEndDate(prevEmpinfo.getEhcmEmpSuspension().getEndDate());
            } else {
              prevEmpinfo.setEndDate(null);
            }
          }
        }
      }
    }

    catch (Exception e) {

    }
  }

  public static EmploymentInfo updateEmpInfo(EHCMEmpTransfer empTransfer, EmploymentInfo oldempInfo,
      EmploymentInfo currentInfo, VariablesSecureApp vars, String decisionType, String lang,
      Date JoinStartDate, EhcmJoiningWorkRequest joinReqId) {
    Date dateafter = null;
    String transferType = null;
    EmploymentInfo employInfo = currentInfo;
    EmploymentInfo prevemployInfo = oldempInfo;
    EhcmJoiningWorkRequest joinRequestId = null;
    EHCMEmpSupervisor supervisorId = null;
    try {

      if (decisionType.equals("UP")) {
        // employInfo.setChangereason(transferType);
        employInfo.setChangereason(empTransfer.getTransferType());
        employInfo.setEhcmPayscale(prevemployInfo.getEhcmPayscale());
        employInfo.setEmpcategory(empTransfer.getGradeClass().getId());
        employInfo.setEmployeeno(empTransfer.getEhcmEmpPerinfo().getSearchKey());
        employInfo.setEhcmPayscaleline(prevemployInfo.getEhcmPayscaleline());
        employInfo.setLocation(prevemployInfo.getLocation());
        if (prevemployInfo.getEhcmPayrollDefinition() != null)
          employInfo.setEhcmPayrollDefinition(prevemployInfo.getEhcmPayrollDefinition());
        employInfo.setEhcmEmpPerinfo(empTransfer.getEhcmEmpPerinfo());
        if (JoinStartDate == null)
          employInfo.setStartDate(empTransfer.getStartDate());
        else
          employInfo.setStartDate(JoinStartDate);
        employInfo.setEndDate(empTransfer.getEndDate());
        employInfo.setAlertStatus("ACT");
        employInfo.setEhcmEmpTransfer(empTransfer);
        employInfo.setDecisionNo(empTransfer.getDecisionNo());
        employInfo.setDecisionDate(empTransfer.getDecisionDate());
        employInfo.setEmploymentgrade(prevemployInfo.getEmploymentgrade());
        if (prevemployInfo.getToGovernmentAgency() != null)
          employInfo.setToGovernmentAgency(prevemployInfo.getToGovernmentAgency());

        if (empTransfer.getNewDepartmentCode() != null) {
          employInfo.setDepartmentName(empTransfer.getNewDepartmentCode().getName());
          employInfo.setDeptcode(empTransfer.getNewDepartmentCode());
        } else {
          employInfo.setDepartmentName(empTransfer.getDepartmentCode().getName());
          employInfo.setDeptcode(empTransfer.getDepartmentCode());
        }
        if (empTransfer.getNEWEhcmPosition() != null) {
          employInfo.setGrade(empTransfer.getNEWEhcmPosition().getGrade());
          employInfo.setJobcode(empTransfer.getNEWEhcmPosition().getEhcmJobs());
          employInfo.setPosition(OBDal.getInstance().get(EhcmPosition.class,
              empTransfer.getNEWEhcmPosition().getId()));
          employInfo.setJobtitle(empTransfer.getNEWJobTitle());
        } else {
          employInfo.setGrade(empTransfer.getGrade());
          employInfo.setJobcode(empTransfer.getPosition().getEhcmJobs());
          employInfo.setPosition(empTransfer.getPosition());
          employInfo.setJobtitle(empTransfer.getTitle());
        }

        if (empTransfer.getNewSectionCode() != null) {
          employInfo.setSectionName(empTransfer.getNewSectionCode().getName());
          employInfo.setSectioncode(empTransfer.getNewSectionCode());
        } else {
          if (empTransfer.getSectionCode() != null) {
            employInfo.setSectionName(empTransfer.getSectionCode().getName());
            employInfo.setSectioncode(empTransfer.getSectionCode());
          }
        }

        /* secondary */

        employInfo.setSecpositionGrade(oldempInfo.getSecpositionGrade());
        employInfo.setSecpositionGrade(oldempInfo.getSecpositionGrade());
        employInfo.setSecjobno(oldempInfo.getSecjobno());
        employInfo.setSecjobcode(oldempInfo.getSecjobcode());
        employInfo.setSecjobtitle(oldempInfo.getSecjobtitle());
        employInfo.setSECDeptCode(oldempInfo.getSECDeptCode());
        employInfo.setAssignedDepartment(oldempInfo.getAssignedDepartment());
        employInfo.setSECDeptName(oldempInfo.getSECDeptName());
        if (oldempInfo.getSECSectionCode() != null) {
          employInfo.setSECSectionCode(oldempInfo.getSECSectionCode());
          employInfo.setSECSectionName(oldempInfo.getSECSectionName());
        }
        employInfo.setSECLocation(oldempInfo.getSECLocation());

        employInfo.setSECStartdate(oldempInfo.getSECStartdate());
        employInfo.setSECEnddate(oldempInfo.getSECEnddate());
        employInfo.setSECDecisionNo(oldempInfo.getSECDecisionNo());
        employInfo.setSECDecisionDate(oldempInfo.getSECDecisionDate());

        employInfo.setSECChangeReason(oldempInfo.getSECChangeReason());
        employInfo.setSECEmploymentNumber(oldempInfo.getSECEmploymentNumber());
        OBQuery<EHCMEmpSupervisorNode> supervisior = OBDal.getInstance().createQuery(
            EHCMEmpSupervisorNode.class,
            "  as e where e.ehcmEmpPerinfo.id=:employeeId and e.client.id =:client");
        supervisior.setNamedParameter("employeeId", empTransfer.getEhcmEmpPerinfo().getId());
        supervisior.setNamedParameter("client", empTransfer.getClient().getId());
        List<EHCMEmpSupervisorNode> node = supervisior.list();
        if (node.size() > 0) {
          supervisorId = node.get(0).getEhcmEmpSupervisor();
          employInfo.setEhcmEmpSupervisor(supervisorId);
        }
        if (empTransfer.isJoinworkreq()) {
          employInfo.setJoinworkreq(true);
          employInfo.setEhcmJoinWorkrequest(joinReqId);
        }

        OBDal.getInstance().save(employInfo);
        OBDal.getInstance().flush();
        // update assigned employee in employee position

        // update for old info record as inactive
        EmpTransferIssueDecisionDAO.updateEndDateforOldRecord(employInfo, vars,
            empTransfer.getOriginalDecisionsNo(), decisionType, joinReqId);

        if (decisionType.equals("UP"))
          EmpTransferIssueDecisionDAO.updateOldEmpTransferInAct(empTransfer);

        OBDal.getInstance().flush();
        return employInfo;

      }
    } catch (Exception e) {

    }
    return employInfo;

  }

  public static EmploymentInfo insertEmploymentInfo(EHCMEmpTransfer empTransfer,
      EmploymentInfo oldempInfo, VariablesSecureApp vars, String decisionType, String lang,
      Date JoinStartDate, EhcmJoiningWorkRequest joinReqId) throws Exception {
    // TODO Auto-generated method stub
    Date dateafter = null;
    String transferType = null;
    EmploymentInfo employInfo = null;
    EhcmJoiningWorkRequest joinRequestId = null;
    EHCMEmpSupervisor supervisorId = null;
    try {

      // dateafter = new Date(empTransfer.getEndDate().getTime() + 1 * 24 * 3600 * 1000);

      // Create a record in Employement Information Window
      if (decisionType.equals("CR"))
        employInfo = OBProvider.getInstance().get(EmploymentInfo.class);
      else if (decisionType.equals("UP"))
        employInfo = oldempInfo;
      transferType = Resource.getProperty("hcm.employinfo.outside.department", lang);

      if (decisionType.equals("UP")) {
        employInfo.setUpdated(new java.util.Date());
        employInfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      }

      // employInfo.setChangereason(transferType);
      employInfo.setChangereason(empTransfer.getTransferType());
      employInfo.setEhcmPayscale(oldempInfo.getEhcmPayscale());
      employInfo.setEmpcategory(empTransfer.getGradeClass().getId());
      employInfo.setEmployeeno(empTransfer.getEhcmEmpPerinfo().getSearchKey());
      employInfo.setEhcmPayscaleline(oldempInfo.getEhcmPayscaleline());
      employInfo.setLocation(oldempInfo.getLocation());
      if (oldempInfo.getEhcmPayrollDefinition() != null)
        employInfo.setEhcmPayrollDefinition(oldempInfo.getEhcmPayrollDefinition());
      employInfo.setEhcmEmpPerinfo(empTransfer.getEhcmEmpPerinfo());
      if (JoinStartDate == null)
        employInfo.setStartDate(empTransfer.getStartDate());
      else
        employInfo.setStartDate(JoinStartDate);
      employInfo.setEndDate(empTransfer.getEndDate());
      employInfo.setAlertStatus("ACT");
      employInfo.setEhcmEmpTransfer(empTransfer);
      employInfo.setDecisionNo(empTransfer.getDecisionNo());
      employInfo.setDecisionDate(empTransfer.getDecisionDate());
      employInfo.setEmploymentgrade(oldempInfo.getEmploymentgrade());
      if (oldempInfo.getToGovernmentAgency() != null)
        employInfo.setToGovernmentAgency(oldempInfo.getToGovernmentAgency());

      if (empTransfer.getNewDepartmentCode() != null) {
        employInfo.setDepartmentName(empTransfer.getNewDepartmentCode().getName());
        employInfo.setDeptcode(empTransfer.getNewDepartmentCode());
      } else {
        employInfo.setDepartmentName(empTransfer.getDepartmentCode().getName());
        employInfo.setDeptcode(empTransfer.getDepartmentCode());
      }
      if (empTransfer.getNEWEhcmPosition() != null) {
        employInfo.setGrade(empTransfer.getNEWEhcmPosition().getGrade());
        employInfo.setJobcode(empTransfer.getNEWEhcmPosition().getEhcmJobs());
        employInfo.setPosition(
            OBDal.getInstance().get(EhcmPosition.class, empTransfer.getNEWEhcmPosition().getId()));
        employInfo.setJobtitle(empTransfer.getNEWJobTitle());
      } else {
        employInfo.setGrade(empTransfer.getGrade());
        employInfo.setJobcode(empTransfer.getPosition().getEhcmJobs());
        employInfo.setPosition(empTransfer.getPosition());
        employInfo.setJobtitle(empTransfer.getTitle());
      }

      if (empTransfer.getNewSectionCode() != null) {
        employInfo.setSectionName(empTransfer.getNewSectionCode().getName());
        employInfo.setSectioncode(empTransfer.getNewSectionCode());
      } else {
        if (empTransfer.getSectionCode() != null) {
          employInfo.setSectionName(empTransfer.getSectionCode().getName());
          employInfo.setSectioncode(empTransfer.getSectionCode());
        }
      }

      /* secondary */

      employInfo.setSecpositionGrade(oldempInfo.getSecpositionGrade());
      employInfo.setSecpositionGrade(oldempInfo.getSecpositionGrade());
      employInfo.setSecjobno(oldempInfo.getSecjobno());
      employInfo.setSecjobcode(oldempInfo.getSecjobcode());
      employInfo.setSecjobtitle(oldempInfo.getSecjobtitle());
      employInfo.setSECDeptCode(oldempInfo.getSECDeptCode());
      employInfo.setAssignedDepartment(oldempInfo.getAssignedDepartment());
      employInfo.setSECDeptName(oldempInfo.getSECDeptName());
      if (oldempInfo.getSECSectionCode() != null) {
        employInfo.setSECSectionCode(oldempInfo.getSECSectionCode());
        employInfo.setSECSectionName(oldempInfo.getSECSectionName());
      }
      employInfo.setSECLocation(oldempInfo.getSECLocation());

      employInfo.setSECStartdate(oldempInfo.getSECStartdate());
      employInfo.setSECEnddate(oldempInfo.getSECEnddate());
      employInfo.setSECDecisionNo(oldempInfo.getSECDecisionNo());
      employInfo.setSECDecisionDate(oldempInfo.getSECDecisionDate());

      employInfo.setSECChangeReason(oldempInfo.getSECChangeReason());
      employInfo.setSECEmploymentNumber(oldempInfo.getSECEmploymentNumber());
      OBQuery<EHCMEmpSupervisorNode> supervisior = OBDal.getInstance().createQuery(
          EHCMEmpSupervisorNode.class,
          "  as e where e.ehcmEmpPerinfo.id=:employeeId and e.client.id =:client");
      supervisior.setNamedParameter("employeeId", empTransfer.getEhcmEmpPerinfo().getId());
      supervisior.setNamedParameter("client", empTransfer.getClient().getId());
      List<EHCMEmpSupervisorNode> node = supervisior.list();
      if (node.size() > 0) {
        supervisorId = node.get(0).getEhcmEmpSupervisor();
        employInfo.setEhcmEmpSupervisor(supervisorId);
      }
      if (empTransfer.isJoinworkreq()) {
        employInfo.setJoinworkreq(true);
        employInfo.setEhcmJoinWorkrequest(joinReqId);
      }

      OBDal.getInstance().save(employInfo);
      OBDal.getInstance().flush();

      // update for old info record as inactive
      EmpTransferIssueDecisionDAO.updateEndDateforOldRecord(employInfo, vars, empTransfer,
          decisionType, joinReqId);

      if (decisionType.equals("UP"))
        EmpTransferIssueDecisionDAO.updateOldEmpTransferInAct(empTransfer);

      OBDal.getInstance().flush();
      return employInfo;
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in insertEmploymentInfo in EmpTransferIssueDecisionDAO: ", e);
    }
    return employInfo;
  }
}
