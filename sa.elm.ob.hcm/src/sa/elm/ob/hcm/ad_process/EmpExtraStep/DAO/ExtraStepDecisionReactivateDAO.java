package sa.elm.ob.hcm.ad_process.EmpExtraStep.DAO;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.EHCMEmpSupervisorNode;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmEmployeeExtraStep;
import sa.elm.ob.hcm.EhcmJoiningWorkRequest;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmpayscaleline;
import sa.elm.ob.hcm.ad_process.EmpExtendService.DAO.ExtendServiceHandlerDAO;
import sa.elm.ob.hcm.util.UtilityDAO;

public class ExtraStepDecisionReactivateDAO {
  private Connection connection = null;
  private static final Logger log = LoggerFactory.getLogger(ExtraStepDecisionReactivateDAO.class);
  private static final Logger LOG = LoggerFactory.getLogger(ExtraStepHandlerDAO.class);

  public ExtraStepDecisionReactivateDAO() {
    connection = getDbConnection();
  }

  private Connection getDbConnection() {
    return OBDal.getInstance().getConnection();
  }

  public static void deleteEmpInfo(EhcmEmployeeExtraStep empExtraStep, String decisionType) {
    List<EmploymentInfo> empInfoList = null;
    EmploymentInfo info = null;
    EmploymentInfo prevEmpinfo = null;
    EmployeeDelegation delegation = null;
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "  ehcmEmpPerinfo.id = :employeeId  order by creationDate desc ");
      empInfoObj.setNamedParameter("employeeId", empExtraStep.getEhcmEmpPerinfo().getId());

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
          OBDal.getInstance().remove(info);
          prevEmpinfo.setEnabled(true);
          prevEmpinfo.setAlertStatus("ACT");
          prevEmpinfo.setUpdated(new java.util.Date());
          ExtendServiceHandlerDAO.updateEmpRecord(empExtraStep.getEhcmEmpPerinfo().getId(),
              info.getId());

          if (prevEmpinfo.getChangereason().equals("H") || prevEmpinfo.getEhcmEmpPromotion() != null
              || prevEmpinfo.getEhcmEmpSuspension() != null
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

    catch (OBException e) {
      throw new OBException(e.getMessage());
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static int insertLineinEmploymentInfo(EhcmEmployeeExtraStep extraStepProcess,
      VariablesSecureApp vars, String decisionType) {
    EmploymentInfo info = null;
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    int count = 0;
    boolean isExtraStep = true;
    int oneDay = 1 * 24 * 3600 * 1000;
    try {
      OBContext.setAdminMode();

      // get employment Information by passing the corresponding employee id.
      OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e where ehcmEmpPerinfo.id=:employeeId and e.enabled='Y' and e.ehcmEmpExtrastep.id is not null order by e.creationDate desc");
      empInfo.setNamedParameter("employeeId", extraStepProcess.getEhcmEmpPerinfo().getId());
      empInfo.setMaxResult(1);
      empInfoList = empInfo.list();
      if (empInfoList.size() > 0) {
        info = empInfoList.get(0);
      }
      // on create case
      if (decisionType.equals("CR")) {
        empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " as e where ehcmEmpPerinfo.id=:employeeId and e.enabled='Y' and (e.ehcmEmpExtrastep.id is null) order by e.creationDate desc");
        empInfo.setNamedParameter("employeeId", extraStepProcess.getEhcmEmpPerinfo().getId());
        empInfo.setMaxResult(1);
        empInfoList = empInfo.list();
        if (empInfoList.size() > 0) {
          info = empInfoList.get(0);
        }
      }

      if (decisionType.equals("CR") || decisionType.equals("UP")) {
        EmploymentInfo employInfo = null;
        if (decisionType.equals("CR")) {
          employInfo = OBProvider.getInstance().get(EmploymentInfo.class);
        } else {
          employInfo = info;
        }

        if (decisionType.equals("CR"))
          employInfo.setChangereason("ES");
        else
          employInfo.setChangereason("ES");
        UtilityDAO.insertActEmplymntInfoDetailsInIssueDecision(extraStepProcess.getEhcmEmpPerinfo(),
            employInfo, isExtraStep, false);
        ehcmpayscaleline line = OBDal.getInstance().get(ehcmpayscaleline.class,
            extraStepProcess.getNewgradepoint().getId());
        employInfo.setEhcmPayscale(line.getEhcmPayscale());
        employInfo.setEhcmPayscaleline(line);
        employInfo.setStartDate(extraStepProcess.getStartDate());
        employInfo.setEhcmEmpExtrastep(extraStepProcess);
        employInfo.setDecisionNo(extraStepProcess.getDecisionNo());
        employInfo.setDecisionDate(extraStepProcess.getDecisionDate());

        // Update the enddate for old hiring record.
        if (decisionType.equals("CR")) {
          Date dateBefore = null;
          OBQuery<EmploymentInfo> empInfoold = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " as e where ehcmEmpPerinfo.id=:employeeId  and e.enabled='Y' order by e.creationDate desc");
          empInfoold.setNamedParameter("employeeId", extraStepProcess.getEhcmEmpPerinfo().getId());
          empInfoold.setMaxResult(1);
          EmploymentInfo empinfo = empInfoold.list().get(0);
          empinfo.setUpdated(new java.util.Date());
          empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));

          if (empinfo.getEndDate() == null) {
            Date startdate = empinfo.getStartDate();
            dateBefore = new Date(extraStepProcess.getStartDate().getTime() - oneDay);

            if (startdate.compareTo(extraStepProcess.getStartDate()) == 0)
              empinfo.setEndDate(empinfo.getStartDate());
            else
              empinfo.setEndDate(dateBefore);

          }
          empinfo.setAlertStatus("INACT");
          empinfo.setEnabled(false);

          OBDal.getInstance().save(empinfo);
          OBDal.getInstance().flush();

        }
        OBDal.getInstance().save(employInfo);
        OBDal.getInstance().flush();

        EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
            extraStepProcess.getEhcmEmpPerinfo().getId());
        if (decisionType.equals("Co")) {
          person.setEmploymentStatus("AC");
        } else {
          person.setEmploymentStatus("ES");
        }
        OBDal.getInstance().save(person);
        OBDal.getInstance().flush();

      }
      count = 1;

    }

    catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Exception while inserting lines in employment tab  : ", e, e);
      }
      OBDal.getInstance().rollbackAndClose();

    } finally {
      OBContext.restorePreviousMode();
    }
    return count;

  }

  public static EmploymentInfo updateEmpInfo(EhcmEmployeeExtraStep empExtraStep,
      EmploymentInfo oldempInfo, EmploymentInfo currentInfo, VariablesSecureApp vars,
      String decisionType, String lang, Date JoinStartDate, EhcmJoiningWorkRequest joinReqId) {

    EmploymentInfo employInfo = currentInfo;
    EmploymentInfo prevemployInfo = oldempInfo;
    EHCMEmpSupervisor supervisorId = null;
    try {

      if (decisionType.equals("UP")) {
        // employInfo.setChangereason(transferType);
        employInfo.setChangereason("ES");
        employInfo.setEhcmPayscale(prevemployInfo.getEhcmPayscale());
        employInfo.setEmpcategory(empExtraStep.getGradeClassifications().getId());
        employInfo.setEmployeeno(empExtraStep.getEhcmEmpPerinfo().getSearchKey());
        employInfo.setEhcmPayscaleline(prevemployInfo.getEhcmPayscaleline());
        employInfo.setLocation(prevemployInfo.getLocation());
        employInfo.setEndDate(null);
        Date dateBefore = new Date(empExtraStep.getStartDate().getTime() - 1 * 24 * 3600 * 1000);
        if (prevemployInfo.getChangereason().equals("H")
            || prevemployInfo.getEhcmEmpPromotion() != null
            || prevemployInfo.getEhcmEmpSuspension() != null
            || prevemployInfo.getChangereason().equals("JWRSEC")) {
          prevemployInfo.setEndDate(dateBefore);
        } else if (prevemployInfo.getEhcmEmpTransfer() != null) {
          if (prevemployInfo.getEhcmEmpTransfer().getEndDate() != null) {
            prevemployInfo.setEndDate(prevemployInfo.getEhcmEmpTransfer().getEndDate());
          } else {
            prevemployInfo.setEndDate(dateBefore);
          }
        } else if (prevemployInfo.getEhcmEmpTransferSelf() != null) {
          if (prevemployInfo.getEhcmEmpTransferSelf().getEndDate() != null) {
            prevemployInfo.setEndDate(prevemployInfo.getEhcmEmpTransferSelf().getEndDate());
          } else {
            prevemployInfo.setEndDate(dateBefore);
          }
        } else if (prevemployInfo.getEhcmEmpSuspension() != null) {
          if (prevemployInfo.getEhcmEmpSuspension().getEndDate() != null) {
            prevemployInfo.setEndDate(prevemployInfo.getEhcmEmpSuspension().getEndDate());
          } else {
            prevemployInfo.setEndDate(dateBefore);
          }
        } else {
          prevemployInfo.setEndDate(dateBefore);
        }

        if (prevemployInfo.getEhcmPayrollDefinition() != null)
          employInfo.setEhcmPayrollDefinition(prevemployInfo.getEhcmPayrollDefinition());
        employInfo.setEhcmEmpPerinfo(empExtraStep.getEhcmEmpPerinfo());
        if (JoinStartDate == null)
          employInfo.setStartDate(empExtraStep.getStartDate());
        else
          employInfo.setStartDate(JoinStartDate);
        employInfo.setAlertStatus("ACT");
        employInfo.setEhcmEmpExtrastep(empExtraStep);
        employInfo.setDecisionNo(empExtraStep.getDecisionNo());
        employInfo.setDecisionDate(empExtraStep.getDecisionDate());
        employInfo.setEmploymentgrade(prevemployInfo.getEmploymentgrade());
        if (prevemployInfo.getToGovernmentAgency() != null)
          employInfo.setToGovernmentAgency(prevemployInfo.getToGovernmentAgency());

        employInfo.setDepartmentName(empExtraStep.getDepartmentCode().getName());
        employInfo.setDeptcode(empExtraStep.getDepartmentCode());

        employInfo.setGrade(empExtraStep.getGrade());
        employInfo.setJobcode(empExtraStep.getPosition().getEhcmJobs());
        employInfo.setPosition(empExtraStep.getPosition());
        employInfo.setJobtitle(empExtraStep.getTitle());

        if (empExtraStep.getSectionCode() != null) {
          employInfo.setSectionName(empExtraStep.getSectionCode().getName());
          employInfo.setSectioncode(empExtraStep.getSectionCode());
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
      supervisior.setNamedParameter("employeeId", empExtraStep.getEhcmEmpPerinfo().getId());
      supervisior.setNamedParameter("client", empExtraStep.getClient().getId());
      List<EHCMEmpSupervisorNode> node = supervisior.list();
      if (node.size() > 0) {
        supervisorId = node.get(0).getEhcmEmpSupervisor();
        employInfo.setEhcmEmpSupervisor(supervisorId);
      }
      OBDal.getInstance().save(employInfo);
      OBDal.getInstance().flush();

      OBDal.getInstance().flush();
      return employInfo;

    } catch (Exception e) {

    }
    return employInfo;

  }
}
