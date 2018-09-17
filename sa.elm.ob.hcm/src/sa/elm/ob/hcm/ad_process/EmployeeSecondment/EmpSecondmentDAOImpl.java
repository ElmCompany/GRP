package sa.elm.ob.hcm.ad_process.EmployeeSecondment;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEmpSecondment;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmJoiningWorkRequest;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.util.UtilityDAO;

/**
 * This process class used for Employee SecondmentDAO MissionDO Implementation
 * 
 * @author divya 26-07-2018
 *
 */

public class EmpSecondmentDAOImpl implements EmpSecondmentDAO {

  private Connection connection = null;
  private static final Logger log = LoggerFactory.getLogger(EmpSecondmentDAOImpl.class);
  public static final String DECISION_STATUS_ISSUED = "I";

  @Override
  public void updateSecondmentStatus(EHCMEmpSecondment secondment) {
    try {
      secondment.setSueDecision(true);
      secondment.setDecisionDate(new Date());
      secondment.setDecisionStatus(DECISION_STATUS_ISSUED);
      OBDal.getInstance().save(secondment);
      // OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateSecondmentStatus: ", e);
    }
  }

  public EmploymentInfo getEmploymentInfo(EHCMEmpSecondment secondment) {
    EmploymentInfo info = null;
    OBQuery<EmploymentInfo> empInfo = null;
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    String hql = null;
    try {
      // getting values of non secondment records(employement record)
      // on create case
      if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)) {
        hql = " and (e.issecondment='N' or e.changereason='COSEC') ";
      }
      // get employment Information for getting the values Location,payroll,payscale
      else {
        hql = " and e.issecondment='Y' ";
      }

      empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e where ehcmEmpPerinfo.id=:employeeId " + "  and e.enabled='Y' "
              + " order by e.creationDate desc");
      empInfo.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      empInfo.setMaxResult(1);
      empInfoList = empInfo.list();
      if (empInfoList.size() > 0) {
        info = empInfoList.get(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in getEmploymentInfo: ", e);
    }
    return info;
  }

  @Override
  public EmploymentInfo insertEmploymentRecord(EHCMEmpSecondment secondment, EmploymentInfo info,
      boolean isJWR, boolean isSecDelay, EhcmJoiningWorkRequest jWRObj) {
    EmploymentInfo employInfo = null;
    Date dateAfter = null;
    int oneMiliSeconds = 1 * 24 * 3600 * 1000;
    Date dateBefore = null;
    boolean isExtraStep = false;
    try {
      if (!isJWR) {
        if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CREATE)
            || secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)
            || secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          employInfo = OBProvider.getInstance().get(EmploymentInfo.class);
        } else {
          employInfo = info;
        }
      } else {
        employInfo = OBProvider.getInstance().get(EmploymentInfo.class);
      }
      employInfo.setOrganization(secondment.getOrganization());

      // change reason
      if (!isJWR) {
        if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF))
          employInfo.setChangereason(DecisionTypeConstants.CHANGEREASON_CUTOFF_SECONDMENT);
        else if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND))
          employInfo.setChangereason(DecisionTypeConstants.CHANGEREASON_EXTEND_SECONDMENT);
        else if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE))
          employInfo.setChangereason(info.getChangereason());
        else
          employInfo.setChangereason(DecisionTypeConstants.CHANGEREASON_SECONDMENT);
      } else {
        if (isSecDelay) {
          employInfo.setChangereason(DecisionTypeConstants.CHANGEREASON_SECONDMENT_DELAY);
        } else {
          employInfo.setChangereason(DecisionTypeConstants.CHANGEREASON_JWR_SECONDMENT);
        }
      }

      UtilityDAO.insertActEmplymntInfoDetailsInIssueDecision(secondment.getEhcmEmpPerinfo(),
          employInfo, isExtraStep, false);

      if (!isJWR && jWRObj == null) {
        if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          dateAfter = new Date(secondment.getEndDate().getTime() + oneMiliSeconds);
          employInfo.setStartDate(dateAfter);
          employInfo.setEndDate(dateAfter);
          employInfo.setAlertStatus(Constants.EMPSTATUS_INACTIVE);
        } else {
          employInfo.setStartDate(secondment.getStartDate());
          employInfo.setEndDate(secondment.getEndDate());
          employInfo.setAlertStatus(Constants.EMPSTATUS_ACTIVE);
        }
      } else {
        if (isSecDelay) {
          dateBefore = new Date(jWRObj.getJoindate().getTime() - oneMiliSeconds);
          employInfo.setStartDate(jWRObj.getDecisionDate());
          employInfo.setEndDate(dateBefore);
          employInfo.setAlertStatus(Constants.EMPSTATUS_INACTIVE);

        } else {
          employInfo.setStartDate(jWRObj.getJoindate());
          employInfo.setEndDate(null);
          employInfo.setAlertStatus(Constants.EMPSTATUS_ACTIVE);
        }
        employInfo.setJoinworkreq(true);
        employInfo.setEhcmJoinWorkrequest(jWRObj);
      }
      employInfo.setSecondment(true);
      if (!isJWR) {
        employInfo.setEhcmEmpSecondment(secondment);
      }
      employInfo.setDecisionNo(secondment.getDecisionNo());
      employInfo.setDecisionDate(new Date());

      OBDal.getInstance().save(employInfo);
      // OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in insertEmploymentRecord: ", e);
    }
    return employInfo;
  }

  @Override
  public void updateEndDateForOldRecord(EmploymentInfo employInfo, EHCMEmpSecondment secondment,
      VariablesSecureApp vars) {
    Date dateBefore = null;
    Date startDate = null;
    OBQuery<EmploymentInfo> empInfoOld = null;
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    int oneMiliSeconds = 1 * 24 * 3600 * 1000;
    try {
      // update the endate and active flag for old record.
      empInfoOld = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId  and id <>:currentInfoId  "
              + " order by creationDate desc "); // and issecondment='Y' -mail
      empInfoOld.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      empInfoOld.setNamedParameter("currentInfoId", employInfo.getId());
      empInfoOld.setMaxResult(1);
      empInfoList = empInfoOld.list();
      if (empInfoList.size() > 0) {
        EmploymentInfo empinfo = empInfoList.get(0);
        empinfo.setEnabled(false);
        empinfo.setUpdated(new java.util.Date());
        empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        empinfo.setAlertStatus(Constants.EMPSTATUS_INACTIVE);
        startDate = empinfo.getStartDate();
        dateBefore = new Date(secondment.getStartDate().getTime() - oneMiliSeconds);
        if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          empinfo.setEndDate(secondment.getEndDate());
        } else {
          if (startDate.compareTo(secondment.getStartDate()) == 0)
            empinfo.setEndDate(empinfo.getStartDate());
          else
            empinfo.setEndDate(dateBefore);
        }
        OBDal.getInstance().save(empinfo);

      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateEndDateForOldRecord: ", e);
    }
  }

  @Override
  public void updateOldSecondmentActiveFlag(EHCMEmpSecondment secondment, boolean enableFlag) {
    try {
      // update old secondment as inactive
      if (secondment != null) {
        EHCMEmpSecondment oldSecondment = secondment;
        oldSecondment.setEnabled(enableFlag);
        OBDal.getInstance().save(oldSecondment);
      }
      // OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateEndDateForOldRecord: ", e);
    }
  }

  @Override
  public void updateEndDateForOldRecordInUpdate(EmploymentInfo employInfo,
      EHCMEmpSecondment secondment, VariablesSecureApp vars) {
    Date dateBefore = null;
    Date startDate = null;
    OBQuery<EmploymentInfo> empInfoOld = null;
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    int oneMiliSeconds = 1 * 24 * 3600 * 1000;
    try {
      // update the endate and active flag for old hiring record.
      empInfoOld = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId  and id <>:currentInfoId "
              + "  order by creationDate desc "); // and issecondment='Y'-mail
      empInfoOld.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      empInfoOld.setNamedParameter("currentInfoId", employInfo.getId());
      empInfoOld.setMaxResult(1);
      empInfoList = empInfoOld.list();
      if (empInfoList.size() > 0) {
        EmploymentInfo empinfo = empInfoList.get(0);

        empinfo.setUpdated(new java.util.Date());
        empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        startDate = empinfo.getStartDate();
        dateBefore = new Date(secondment.getStartDate().getTime() - oneMiliSeconds);
        if (startDate.compareTo(secondment.getStartDate()) == 0)
          empinfo.setEndDate(empinfo.getStartDate());
        else
          empinfo.setEndDate(dateBefore);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateEndDateForOldRecord: ", e);
    }
  }

  @SuppressWarnings("unused")
  @Override
  public void updateEmploymentStatus(EHCMEmpSecondment secondment, boolean iscancel) {
    try {
      EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
          secondment.getEhcmEmpPerinfo().getId());
      if (!iscancel) {
        if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF)) {
          person.setEmploymentStatus(Constants.EMPLOYMENT_STATUS_ACTIVE);
        } else {
          person.setEmploymentStatus(Constants.EMPLOYMENT_STATUS_SECONDMENT);
        }
      } else {
        if (secondment != null) {
          person.setEmploymentStatus(Constants.EMPLOYMENT_STATUS_SECONDMENT);
        } else {
          person.setEmploymentStatus(Constants.EMPLOYMENT_STATUS_ACTIVE);
        }
      }

      OBDal.getInstance().save(person);
      // OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateEmploymentStatus: ", e);
    }
  }

  @Override
  public EmploymentInfo updateEndDateForOldRecordInCancel(EHCMEmpSecondment secondment,
      VariablesSecureApp vars) {
    OBQuery<EmploymentInfo> prevEmpInfo = null;
    List<EmploymentInfo> prevEmpInfoList = new ArrayList<EmploymentInfo>();
    EmploymentInfo empinfo = null;
    try {
      // update the acive flag='Y' and enddate is null for recently update record
      prevEmpInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId   and (ehcmEmpSecondment.id<>:originalDecisionId "
              + " or  ehcmEmpSecondment.id is null) " + " order by creationDate desc ");// or
                                                                                        // ehcmEmpSecondment.id
                                                                                        // is null)
      prevEmpInfo.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      prevEmpInfo.setNamedParameter("originalDecisionId",
          secondment.getOriginalDecisionsNo().getId());
      prevEmpInfo.setMaxResult(1);
      prevEmpInfoList = prevEmpInfo.list();
      if (prevEmpInfoList.size() > 0) {
        empinfo = prevEmpInfoList.get(0);
        log.debug("getChangereasoncancel12:" + empinfo.getId());
        empinfo.setUpdated(new java.util.Date());
        empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        empinfo.setEnabled(true);
        empinfo.setAlertStatus("ACT");
        // update end date for previous record based on change reason primary key

        // secondment
        if (empinfo.getEhcmEmpSecondment() != null) {
          empinfo.setEndDate(empinfo.getEhcmEmpSecondment().getEndDate());
        }
        // transfer
        else if (empinfo.getEhcmEmpTransfer() != null) {
          if (empinfo.getEhcmEmpTransfer().getEndDate() != null) {
            empinfo.setEndDate(empinfo.getEhcmEmpTransfer().getEndDate());
          } else {
            empinfo.setEndDate(null);
          }
        }
        // hiring, promotion,suspension end, transfer
        else if (empinfo.getChangereason().equals("H") || empinfo.getEhcmEmpPromotion() != null
            || (empinfo.getEhcmEmpSuspension() != null
                && empinfo.getChangereason().equals(Constants.SUSPENSION_END))
            || empinfo.getEhcmEmpTransferSelf() != null
            || empinfo.getChangereason().equals("JWRSEC")) {
          empinfo.setEndDate(null);
        }
        OBDal.getInstance().save(empinfo);
        // OBDal.getInstance().flush();
        return empinfo;
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateEndDateForOldRecord: ", e);
    }
    return empinfo;
  }

  @Override
  public void updateDelegation(EmploymentInfo empinfo, EHCMEmpSecondment secondment) {
    OBQuery<EmployeeDelegation> delQry = null;
    List<EmployeeDelegation> delList = new ArrayList<EmployeeDelegation>();
    try {
      delQry = OBDal.getInstance().createQuery(EmployeeDelegation.class,
          " ehcmEmpPerinfo.id=:employeeId and enabled='Y' order by creationDate desc");
      delQry.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      delQry.setMaxResult(1);
      delList = delQry.list();
      if (delList.size() > 0) {
        EmployeeDelegation delegation = delList.get(0);
        log.debug("delegation:" + delegation.getEhcmEmploymentInfo().getId());
        delegation.setEhcmEmploymentInfo(empinfo);
        log.debug("delegation1:" + delegation.getId());
        OBDal.getInstance().save(delegation);
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
            empinfo.setSECLocation(delegation.getNewSection().getEhcmLocation().getLocationName());
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
        // OBDal.getInstance().flush();

      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateDelegation: ", e);
    }
  }

  @Override
  public void remRecntEmpInfoInCancel(EmploymentInfo empinfo, EHCMEmpSecondment secondment) {
    OBQuery<EmploymentInfo> employInfo = null;
    List<EmploymentInfo> employInfoList = new ArrayList<EmploymentInfo>();
    OBQuery<EmployeeDelegation> delegate = null;
    List<EmployeeDelegation> delegateList = new ArrayList<EmployeeDelegation>();
    try {
      // remove the recent record
      employInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:employeeId  and enabled='Y'  and ehcmEmpSecondment.id =:originalDecisionId"
              + " order by creationDate desc");
      employInfo.setNamedParameter("employeeId", secondment.getEhcmEmpPerinfo().getId());
      employInfo.setNamedParameter("originalDecisionId",
          secondment.getOriginalDecisionsNo().getId());
      employInfo.setMaxResult(1);
      employInfoList = employInfo.list();
      log.debug("list Size:" + employInfoList.size());
      if (employInfoList.size() > 0) {
        EmploymentInfo empInfor = employInfoList.get(0);

        delegate = OBDal.getInstance().createQuery(EmployeeDelegation.class,
            " ehcmEmploymentInfo.id=:employeeInfoId  order by creationDate desc");
        delegate.setNamedParameter("employeeInfoId", empInfor.getId());
        delegateList = delegate.list();
        if (delegateList.size() > 0) {
          for (EmployeeDelegation delgate : delegateList) {
            delgate.setEhcmEmploymentInfo(empinfo);
            OBDal.getInstance().save(delgate);
            // OBDal.getInstance().flush();
          }
        }

        OBDal.getInstance().remove(empInfor);
        // OBDal.getInstance().flush();
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in remRecntEmpInfoInCancel: ", e);
    }
  }

  @Override
  public EmploymentInfo getPreviousEmployInfo(String employeeId) {
    EmploymentInfo previousEmployInfo = null;
    List<EmploymentInfo> prevEmployInfoList = null;
    try {
      OBQuery<EmploymentInfo> prevEmpInfoQry = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e " + " where e.ehcmEmpPerinfo.id=:employeeId  order by e.creationDate desc ");
      prevEmpInfoQry.setNamedParameter("employeeId", employeeId);
      prevEmpInfoQry.setFirstResult(1);
      prevEmpInfoQry.setMaxResult(1);
      prevEmployInfoList = prevEmpInfoQry.list();
      if (prevEmployInfoList.size() > 0) {
        previousEmployInfo = prevEmployInfoList.get(0);
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in getPreviousEmployInfo: ", e);
    }
    return previousEmployInfo;
  }
}
