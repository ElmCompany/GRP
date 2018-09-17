package sa.elm.ob.hcm.ad_process.EmployeeSecondment;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEmpSecondment;
import sa.elm.ob.hcm.EhcmJoinLeaveDecNoV;
import sa.elm.ob.hcm.EhcmJoiningWorkRequest;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.ad_process.DecisionTypeConstants;
import sa.elm.ob.hcm.ad_process.EmpExtendService.DAO.ExtendServiceHandlerDAO;
import sa.elm.ob.hcm.util.UtilityDAO;

/**
 * This process class used for Employee Secondment reactivateDAO Implementation
 * 
 * @author divya 11-06-2018
 *
 */

public class EmpSecondmentReactivateDAOImpl implements EmpSecondmentReactivateDAO {

  private Connection connection = null;
  private static final Logger log = LoggerFactory.getLogger(EmpSecondmentReactivateDAOImpl.class);
  public static final String DECISION_STATUS_UNDERPROCESSING = "UP";
  EmpSecondmentDAO empSecondmentDAO = new EmpSecondmentDAOImpl();

  @Override
  public void updateSecondmentStatus(EHCMEmpSecondment secondment) {
    try {
      secondment.setSueDecision(false);
      secondment.setDecisionDate(null);
      secondment.setDecisionStatus(DECISION_STATUS_UNDERPROCESSING);
      OBDal.getInstance().save(secondment);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateSecondmentStatus in EmpSecondmentReactivateDAOImpl: ", e);
    }
  }

  @Override
  public boolean chkEmplyInfoExistAfterSecondment(EHCMEmpSecondment empSecondment) {
    List<EmploymentInfo> empInfoList = null;
    boolean chkEmplyInfoExistAfterSec = false;
    try {
      OBContext.setAdminMode();

      OBQuery<EmploymentInfo> curEmpInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "  ehcmEmpPerinfo.id = :employeeId and ehcmEmpSecondment.id=:empSecondmentId  ");
      curEmpInfoObj.setNamedParameter("employeeId", empSecondment.getEhcmEmpPerinfo().getId());
      curEmpInfoObj.setNamedParameter("empSecondmentId", empSecondment.getId());
      curEmpInfoObj.setMaxResult(1);
      empInfoList = curEmpInfoObj.list();
      if (empInfoList.size() > 0) {

        OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
            "  ehcmEmpPerinfo.id = :employeeId and ( ehcmEmpSecondment.id<>:empSecondmentId  or  ehcmEmpSecondment.id is  null)"
                + " and creationDate > :creationDate " + " order by creationDate desc ");
        empInfoObj.setNamedParameter("employeeId", empSecondment.getEhcmEmpPerinfo().getId());
        empInfoObj.setNamedParameter("empSecondmentId", empSecondment.getId());

        empInfoObj.setNamedParameter("creationDate", empInfoList.get(0).getCreationDate());

        empInfoObj.setMaxResult(1);
        empInfoList = empInfoObj.list();
        if (empInfoList.size() > 0) {
          chkEmplyInfoExistAfterSec = true;
        }
      }

    } catch (Exception e) {
      log.error("Exception chkEmplyInfoExistAfterSecondment in EmpSecondmentReactivateDAOImpl : ",
          e, e);
    }
    return chkEmplyInfoExistAfterSec;
  }

  @Override
  public boolean chkEmplyInfoExistsInCancelCase(EHCMEmpSecondment empSecondment) {
    List<EmploymentInfo> empInfoList = null;
    boolean chkEmplyInfoExistsInCancelCase = false;
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "  ehcmEmpPerinfo.id = :employeeId   and startDate > :startDate "
              + " order by creationDate desc ");
      empInfoObj.setNamedParameter("employeeId", empSecondment.getEhcmEmpPerinfo().getId());
      empInfoObj.setNamedParameter("startDate", empSecondment.getStartDate());

      empInfoObj.setMaxResult(1);
      empInfoList = empInfoObj.list();
      if (empInfoList.size() > 0) {
        chkEmplyInfoExistsInCancelCase = true;
      }

    } catch (Exception e) {
      log.error("Exception chkEmplyInfoExistsInCancelCase in EmpSecondmentReactivateDAOImpl : ", e,
          e);
    }
    return chkEmplyInfoExistsInCancelCase;
  }

  @Override
  public void deleteEmpInfo(EHCMEmpSecondment empSecondment) {
    List<EmploymentInfo> empInfoList = null;
    List<EmploymentInfo> prevEmpInfoList = null;
    EmploymentInfo info = null;
    EmploymentInfo prevEmpinfo = null;
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "  ehcmEmpPerinfo.id = :employeeId  and ehcmEmpSecondment.id=:empSecondmentId "
              + " order by creationDate desc ");
      empInfoObj.setNamedParameter("employeeId", empSecondment.getEhcmEmpPerinfo().getId());
      empInfoObj.setNamedParameter("empSecondmentId", empSecondment.getId());

      empInfoObj.setMaxResult(1);
      empInfoList = empInfoObj.list();
      if (empInfoList.size() > 0) {
        info = empInfoList.get(0);

        OBQuery<EmploymentInfo> prevEmpInfoObj = OBDal.getInstance().createQuery(
            EmploymentInfo.class, "  ehcmEmpPerinfo.id = :employeeId  and id <> :currentInfoId "
                + " order by creationDate desc ");
        prevEmpInfoObj.setNamedParameter("employeeId", empSecondment.getEhcmEmpPerinfo().getId());
        prevEmpInfoObj.setNamedParameter("currentInfoId", info.getId());
        prevEmpInfoObj.setMaxResult(1);
        prevEmpInfoList = prevEmpInfoObj.list();
        if (prevEmpInfoList.size() > 0) {
          prevEmpinfo = prevEmpInfoList.get(0);
          // secondment
          if (prevEmpinfo.getEhcmEmpSecondment() != null) {
            prevEmpinfo.setEndDate(prevEmpinfo.getEhcmEmpSecondment().getEndDate());
          }
          // transfer
          else if (prevEmpinfo.getEhcmEmpTransfer() != null) {
            if (prevEmpinfo.getEhcmEmpTransfer().getEndDate() != null) {
              prevEmpinfo.setEndDate(prevEmpinfo.getEhcmEmpTransfer().getEndDate());
            } else {
              prevEmpinfo.setEndDate(null);
            }
          }
          // hiring, promotion,suspension end, transfer
          else if (prevEmpinfo.getChangereason().equals("H")
              || prevEmpinfo.getEhcmEmpPromotion() != null
              || (prevEmpinfo.getEhcmEmpSuspension() != null
                  && prevEmpinfo.getChangereason().equals(Constants.SUSPENSION_END))
              || prevEmpinfo.getEhcmEmpTransferSelf() != null
              || prevEmpinfo.getChangereason().equals("JWRSEC")) {
            prevEmpinfo.setEndDate(null);
          }
          prevEmpinfo.setUpdated(new java.util.Date());
          prevEmpinfo.setAlertStatus(Constants.EMPSTATUS_ACTIVE);
          prevEmpinfo.setEnabled(true);
        }

        OBDal.getInstance().remove(info);
      }
      if (empSecondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND)) {
        empSecondmentDAO.updateOldSecondmentActiveFlag(empSecondment.getOriginalDecisionsNo(),
            true);
        if (prevEmpinfo.getEhcmEmpSecondment() != null) {
          updateJoinWorkRequestOriginalDecisionNo(info, prevEmpinfo);
        }
      }
      // Update Employment status
      ExtendServiceHandlerDAO.updateEmpRecord(empSecondment.getEhcmEmpPerinfo().getId(),
          info.getId());

    } catch (Exception e) {
      log.error("Exception deleteEmpInfo in EmpSecondmentReactivateDAOImpl : ", e, e);
    }
  }

  @Override
  public void updateEmpInfoInupdateCase(EHCMEmpSecondment empSecondment) {
    List<EmploymentInfo> empInfoList = null;
    List<EmploymentInfo> prevEmpInfoList = null;
    EmploymentInfo info = null;
    EmploymentInfo prevEmpinfo = null;
    int oneMiliSeconds = 1 * 24 * 3600 * 1000;
    Date dateBefore = null;
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "  ehcmEmpPerinfo.id = :employeeId  and ehcmEmpSecondment.id=:empSecondmentId "
              + " order by creationDate desc ");
      empInfoObj.setNamedParameter("employeeId", empSecondment.getEhcmEmpPerinfo().getId());
      empInfoObj.setNamedParameter("empSecondmentId", empSecondment.getId());

      empInfoObj.setMaxResult(1);
      empInfoList = empInfoObj.list();
      if (empInfoList.size() > 0) {
        info = empInfoList.get(0);
        info.setDecisionNo(empSecondment.getOriginalDecisionsNo().getDecisionNo());
        info.setDecisionDate(empSecondment.getOriginalDecisionsNo().getDecisionDate());
        info.setStartDate(empSecondment.getOriginalDecisionsNo().getStartDate());
        info.setEndDate(empSecondment.getOriginalDecisionsNo().getEndDate());
        info.setEhcmEmpSecondment(empSecondment.getOriginalDecisionsNo());
        OBDal.getInstance().save(info);

        OBQuery<EmploymentInfo> prevEmpInfoObj = OBDal.getInstance().createQuery(
            EmploymentInfo.class, "  ehcmEmpPerinfo.id = :employeeId  and id <> :currentInfoId "
                + " order by creationDate desc ");
        prevEmpInfoObj.setNamedParameter("employeeId", empSecondment.getEhcmEmpPerinfo().getId());
        prevEmpInfoObj.setNamedParameter("currentInfoId", info.getId());
        prevEmpInfoObj.setMaxResult(1);
        prevEmpInfoList = prevEmpInfoObj.list();
        if (prevEmpInfoList.size() > 0) {
          prevEmpinfo = prevEmpInfoList.get(0);
          dateBefore = new Date(info.getStartDate().getTime() - oneMiliSeconds);
          prevEmpinfo.setEndDate(dateBefore);
          OBDal.getInstance().save(prevEmpinfo);
        }
      }
      empSecondmentDAO.updateOldSecondmentActiveFlag(empSecondment.getOriginalDecisionsNo(), true);
      if (prevEmpinfo.getEhcmEmpSecondment() != null) {
        updateJoinWorkRequestOriginalDecisionNo(info, prevEmpinfo);
      }
    } catch (Exception e) {
      log.error("Exception updateEmpInfo in EmpSecondmentReactivateDAOImpl : ", e, e);
    }
  }

  @Override
  public void updateExtendRecordInCutOffCase(EHCMEmpSecondment empSecondment) {
    List<EmploymentInfo> empInfoList = null;
    List<EmploymentInfo> prevEmpInfoList = null;
    EmploymentInfo info = null;
    EmploymentInfo prevEmpinfo = null;
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "  ehcmEmpPerinfo.id = :employeeId  and ehcmEmpSecondment.id=:empSecondmentId "
              + " order by creationDate desc ");
      empInfoObj.setNamedParameter("employeeId", empSecondment.getEhcmEmpPerinfo().getId());
      empInfoObj.setNamedParameter("empSecondmentId", empSecondment.getId());

      empInfoObj.setMaxResult(1);
      empInfoList = empInfoObj.list();
      if (empInfoList.size() > 0) {
        info = empInfoList.get(0);

        OBQuery<EmploymentInfo> prevEmpInfoObj = OBDal.getInstance().createQuery(
            EmploymentInfo.class,
            "  ehcmEmpPerinfo.id = :employeeId  and ehcmEmpSecondment.id=:empSecondmentId  "
                + " order by creationDate desc ");
        prevEmpInfoObj.setNamedParameter("employeeId", empSecondment.getEhcmEmpPerinfo().getId());
        prevEmpInfoObj.setNamedParameter("empSecondmentId",
            empSecondment.getOriginalDecisionsNo().getId());
        prevEmpInfoObj.setMaxResult(1);
        prevEmpInfoList = prevEmpInfoObj.list();
        if (prevEmpInfoList.size() > 0) {
          prevEmpinfo = prevEmpInfoList.get(0);
          // secondment
          if (prevEmpinfo.getEhcmEmpSecondment() != null) {
            prevEmpinfo.setEndDate(prevEmpinfo.getEhcmEmpSecondment().getEndDate());
          }
          prevEmpinfo.setUpdated(new java.util.Date());
          prevEmpinfo.setAlertStatus(Constants.EMPSTATUS_ACTIVE);
          prevEmpinfo.setEnabled(true);
          OBDal.getInstance().remove(info);
        }
      }
      empSecondmentDAO.updateOldSecondmentActiveFlag(empSecondment.getOriginalDecisionsNo(), true);
      if (prevEmpinfo.getEhcmEmpSecondment() != null) {
        updateJoinWorkRequestOriginalDecisionNo(info, prevEmpinfo);
      }
    } catch (Exception e) {
      log.error("Exception deleteEmpInfo in EmpSecondmentReactivateDAOImpl : ", e, e);
    }
  }

  @Override
  public void InsertEmpInfoInCancelCase(EHCMEmpSecondment empSecondment, VariablesSecureApp vars) {
    EmploymentInfo info = null;
    EmploymentInfo newEmployInfo = null;
    try {
      OBContext.setAdminMode();
      info = UtilityDAO.getActiveEmployInfo(empSecondment.getEhcmEmpPerinfo().getId());

      // insert a employe Info Records
      newEmployInfo = insertEmploymentRecord(empSecondment, info);

      // Create ,Extend,CutOff Cases
      if (!empSecondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        // update the endate and active flag for old record.
        empSecondmentDAO.updateEndDateForOldRecord(newEmployInfo, empSecondment, vars);
      }
      // update case
      else {
        // update the endate and active flag for old record.
        empSecondmentDAO.updateEndDateForOldRecord(newEmployInfo, empSecondment, vars);
      }

      empSecondmentDAO.updateEmploymentStatus(empSecondment, false);

    } catch (Exception e) {
      log.error("Exception InsertEmpInfoInCancelCase in EmpSecondmentReactivateDAOImpl : ", e, e);
    }
  }

  public EmploymentInfo insertEmploymentRecord(EHCMEmpSecondment secondment, EmploymentInfo info) {
    EmploymentInfo employInfo = null;
    Date dateAfter = null;
    int oneMiliSeconds = 1 * 24 * 3600 * 1000;
    String decisionType = null;
    boolean isExtraStep = false;
    try {
      employInfo = OBProvider.getInstance().get(EmploymentInfo.class);

      employInfo.setOrganization(secondment.getOrganization());

      // change reason
      if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF))
        employInfo.setChangereason(DecisionTypeConstants.CHANGEREASON_CUTOFF_SECONDMENT);
      else if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_EXTEND))
        employInfo.setChangereason(DecisionTypeConstants.CHANGEREASON_EXTEND_SECONDMENT);
      else if (secondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
        decisionType = findOriginalDecisionTypeInUpCase(secondment);
        if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_EXTEND))
          employInfo.setChangereason(DecisionTypeConstants.CHANGEREASON_EXTEND_SECONDMENT);
        else if (decisionType.equals(DecisionTypeConstants.DECISION_TYPE_CUTOFF))
          employInfo.setChangereason(DecisionTypeConstants.CHANGEREASON_CUTOFF_SECONDMENT);
        else
          employInfo.setChangereason(DecisionTypeConstants.CHANGEREASON_SECONDMENT);

      } else
        employInfo.setChangereason(DecisionTypeConstants.CHANGEREASON_SECONDMENT);
      UtilityDAO.insertActEmplymntInfoDetailsInIssueDecision(secondment.getEhcmEmpPerinfo(),
          employInfo, isExtraStep, false);

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
      employInfo.setSecondment(true);
      employInfo.setEhcmEmpSecondment(secondment);
      employInfo.setDecisionNo(secondment.getDecisionNo());
      employInfo.setDecisionDate(secondment.getDecisionDate());

      OBDal.getInstance().save(employInfo);
      // OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in insertEmploymentRecord: ", e);
    }
    return employInfo;
  }

  public String findOriginalDecisionTypeInUpCase(EHCMEmpSecondment secondment) {
    EHCMEmpSecondment empSecondment = null;
    String decisiontype = null;
    try {
      empSecondment = secondment.getOriginalDecisionsNo();
      while (empSecondment != null) {

        if (!empSecondment.getDecisionType().equals(DecisionTypeConstants.DECISION_TYPE_UPDATE)) {
          decisiontype = empSecondment.getDecisionType();
          break;
        } else {
          empSecondment = empSecondment.getOriginalDecisionsNo();
        }

      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in findOriginalDecisionTypeInUpCase: ", e);
    }
    return decisiontype;
  }

  @Override
  public void updateJoinWorkRequestOriginalDecisionNo(EmploymentInfo empInfo,
      EmploymentInfo prevEmpInfo) throws Exception {
    // TODO Auto-generated method stub

    EmploymentInfo employInfoObj = null;
    EmploymentInfo prevEmployInfoObj = null;
    List<EhcmJoiningWorkRequest> joinWorkRequestList = null;
    EhcmJoiningWorkRequest joiningWorkRequestObj = null;
    Date dateAfterInfoDate = null;
    int oneMilliSeconds = 1 * 24 * 3600 * 1000;
    try {

      // get EmpInfoRecord
      employInfoObj = empInfo;
      prevEmployInfoObj = prevEmpInfo;
      OBQuery<EhcmJoiningWorkRequest> jWRQuery = OBDal.getInstance().createQuery(
          EhcmJoiningWorkRequest.class,
          " as e where e.originalDecisionNo.id=:originalDecNoId and e.decisionStatus='UP' ");
      jWRQuery.setNamedParameter("originalDecNoId", empInfo.getId());
      jWRQuery.setMaxResult(1);
      joinWorkRequestList = jWRQuery.list();
      if (joinWorkRequestList.size() > 0) {
        joiningWorkRequestObj = joinWorkRequestList.get(0);

        if (prevEmployInfoObj != null && (prevEmployInfoObj.getChangereason()
            .equals(DecisionTypeConstants.CHANGEREASON_CUTOFF_SECONDMENT)
            || prevEmployInfoObj.getChangereason()
                .equals(DecisionTypeConstants.CHANGEREASON_EXTEND_SECONDMENT)
            || prevEmployInfoObj.getChangereason()
                .equals(DecisionTypeConstants.CHANGEREASON_SECONDMENT))) {
          joiningWorkRequestObj.setOriginalDecisionNo(
              OBDal.getInstance().get(EhcmJoinLeaveDecNoV.class, prevEmployInfoObj.getId()));
          dateAfterInfoDate = new Date(prevEmployInfoObj.getEndDate().getTime() + oneMilliSeconds);
          joiningWorkRequestObj.setDecisionDate(dateAfterInfoDate);
          OBDal.getInstance().save(joiningWorkRequestObj);
          OBDal.getInstance().flush();
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in findOriginalDecisionTypeInUpCase: ", e);
    }
  }
}
