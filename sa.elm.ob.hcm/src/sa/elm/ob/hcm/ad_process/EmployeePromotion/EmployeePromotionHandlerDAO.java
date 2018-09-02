package sa.elm.ob.hcm.ad_process.EmployeePromotion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEmpPromotion;
import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.EHCMEmpSupervisorNode;
import sa.elm.ob.hcm.EhcmJoiningWorkRequest;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmpayscaleline;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;

/**
 *
 * 
 * @author poongodi 01-03-2018
 *
 */

public class EmployeePromotionHandlerDAO {

  private Connection connection = null;
  private static final Logger log = LoggerFactory.getLogger(EmployeePromotionHandlerDAO.class);

  public EmployeePromotionHandlerDAO() {
    connection = getDbConnection();
  }

  /**
   * Get the database connection
   * 
   * @return
   */
  private Connection getDbConnection() {
    return OBDal.getInstance().getConnection();
  }

  public static EmploymentInfo insertEmploymentInfo(EHCMEmpPromotion promotion, EmploymentInfo info,
      VariablesSecureApp vars, String decisionType, String lang, Date JoinStartDate,
      EhcmJoiningWorkRequest joinReqId) throws Exception {
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
    log.debug("Enntered in Handler");
    String promotionType = "", employmentInfoId = "";
    PreparedStatement st = null;
    ResultSet rs = null;
    EmploymentInfo employInfo = null;
    Long seqno = 0L;
    EHCMEmpSupervisor supervisorId = null;
    int millSec = 1 * 24 * 3600 * 1000;
    Date dateBeforeForassign = null;
    EHCMEmpPromotion empPromotionForAssign = null;
    try {

      // Create & update Cases
      if (decisionType.equals("CR") || decisionType.equals("UP")) {
        // Create a record in Employement Information Window
        if (decisionType.equals("CR")) {
          employInfo = OBProvider.getInstance().get(EmploymentInfo.class);
        } else {
          employInfo = info;
        }
        employInfo.setChangereason(promotion.getPromotionType());
        if (promotion.getNewDepartment() != null) {
          employInfo.setDepartmentName(promotion.getNewDepartment().getName());
          employInfo.setDeptcode(promotion.getNewDepartment());
        } else {
          employInfo.setDepartmentName(promotion.getDepartmentCode().getName());
          employInfo.setDeptcode(promotion.getDepartmentCode());
        }
        if (promotion.getNEWGrade() != null) {
          employInfo.setGrade(promotion.getNewPosition().getGrade());
          ehcmpayscaleline line = OBDal.getInstance().get(ehcmpayscaleline.class,
              promotion.getNEWEhcmPayscaleline().getId());
          employInfo.setEhcmPayscale(line.getEhcmPayscale());
          employInfo.setEmpcategory(promotion.getEmployeeCategory().getId());
          employInfo.setEmployeeno(promotion.getEhcmEmpPerinfo().getSearchKey());
          employInfo.setEhcmPayscaleline(line);
          employInfo.setEmploymentgrade(promotion.getNEWGrade());

        }
        if (promotion.getNewPosition() != null) {
          /*
           * employInfo.setJobcode(OBDal.getInstance().get(Jobs.class, promotion.getNewJobCode()));
           */
          employInfo.setPosition(
              OBDal.getInstance().get(EhcmPosition.class, promotion.getNewPosition().getId()));
          employInfo.setJobcode(employInfo.getPosition().getEhcmJobs());
          employInfo.setJobtitle(promotion.getNewJobTitle());
        } else {
          /* employInfo.setGrade(promotion.getGrade()); */
          employInfo.setJobcode(promotion.getPosition().getEhcmJobs());
          employInfo.setPosition(promotion.getPosition());
          employInfo.setJobtitle(promotion.getTitle());
        }
        if (info.getToGovernmentAgency() != null)
          employInfo.setToGovernmentAgency(info.getToGovernmentAgency());
        employInfo.setLocation(info.getLocation());
        if (info.getEhcmPayrollDefinition() != null)
          employInfo.setEhcmPayrollDefinition(info.getEhcmPayrollDefinition());
        if (promotion.getNewSectionCode() != null) {
          employInfo.setSectionName(promotion.getNewSectionCode().getName());
          employInfo.setSectioncode(promotion.getNewSectionCode());
        } else {
          if (promotion.getSectionCode() != null) {
            employInfo.setSectionName(promotion.getSectionCode().getName());
            employInfo.setSectioncode(promotion.getSectionCode());
          } else {
            employInfo.setSectionName(null);
            employInfo.setSectioncode(null);
          }
        }
        employInfo.setEhcmEmpPerinfo(promotion.getEhcmEmpPerinfo());
        if (!promotion.isJoinWorkRequest()) {
          employInfo.setStartDate(promotion.getStartDate());
        } else {
          employInfo.setStartDate(joinReqId.getJoindate());
        }
        employInfo.setEndDate(null);
        employInfo.setAlertStatus("ACT");
        employInfo.setEhcmEmpPromotion(promotion);
        employInfo.setDecisionNo(promotion.getDecisionNo());
        employInfo.setDecisionDate(promotion.getDecisionDate());

        /* secondary */
        employInfo.setSecpositionGrade(info.getSecpositionGrade());
        employInfo.setSecpositionGrade(info.getSecpositionGrade());
        employInfo.setSecjobno(info.getSecjobno());
        employInfo.setSecjobcode(info.getSecjobcode());
        employInfo.setSecjobtitle(info.getSecjobtitle());
        employInfo.setSECDeptCode(info.getSECDeptCode());
        employInfo.setSECDeptName(info.getSECDeptName());
        employInfo.setSECSectionCode(info.getSECSectionCode());
        employInfo.setSECSectionName(info.getSECSectionName());
        employInfo.setSECLocation(info.getSECLocation());
        employInfo.setSECStartdate(info.getSECStartdate());
        employInfo.setSECEnddate(info.getSECEnddate());
        employInfo.setSECDecisionNo(info.getSECDecisionNo());
        employInfo.setSECDecisionDate(info.getSECDecisionDate());
        employInfo.setSECChangeReason(info.getSECChangeReason());
        employInfo.setSECEmploymentNumber(info.getSECEmploymentNumber());
        OBQuery<EHCMEmpSupervisorNode> supervisior = OBDal.getInstance().createQuery(
            EHCMEmpSupervisorNode.class,
            "  as e where e.ehcmEmpPerinfo.id=:employeeId and e.client.id =:client");
        supervisior.setNamedParameter("employeeId", promotion.getEhcmEmpPerinfo().getId());
        supervisior.setNamedParameter("client", promotion.getClient().getId());
        List<EHCMEmpSupervisorNode> node = supervisior.list();
        if (node.size() > 0) {
          supervisorId = node.get(0).getEhcmEmpSupervisor();
          employInfo.setEhcmEmpSupervisor(supervisorId);
        }

        if (promotion.isJoinWorkRequest()) {
          employInfo.setJoinworkreq(true);
          employInfo.setEhcmJoinWorkrequest(joinReqId);
        }

        OBDal.getInstance().save(employInfo);
        OBDal.getInstance().flush();

        // update employee information in position window
        if (promotion.getNewPosition() != null) {
          EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
              promotion.getPosition().getId());
          log.debug("employInfo.getEhcmEmpTransfer:" + promotion.getPosition().getJOBNo());
          /*
           * Task No.6797 pos.setAssignedEmployee(null); OBDal.getInstance().save(pos);
           * OBDal.getInstance().flush();
           */

          EhcmPosition newpos = OBDal.getInstance().get(EhcmPosition.class,
              promotion.getNewPosition().getId());
          /*
           * Task No.6797 newpos.setAssignedEmployee( OBDal.getInstance().get(EmployeeView.class,
           * promotion.getEhcmEmpPerinfo().getId())); OBDal.getInstance().save(newpos);
           * OBDal.getInstance().flush();
           */

          if (promotion.getOriginalDecisionsNo() != null) {
            EhcmPosition currentPos = assingedOrReleaseEmpInPositionDAO
                .revertOldValuesAndGetOldestPosition(promotion.getEhcmEmpPerinfo(), null, promotion,
                    null);
            if (!currentPos.getId().equals(promotion.getNewPosition().getId())) {
              if (!promotion.getPosition().getId().equals(promotion.getNewPosition().getId())
                  && !currentPos.getId().equals(promotion.getPosition().getId())) {
                assingedOrReleaseEmpInPositionDAO
                    .deletePositionEmployeeHisotry(promotion.getEhcmEmpPerinfo(), pos);
              }

              dateBeforeForassign = new Date(promotion.getStartDate().getTime() - millSec);

              assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
                  promotion.getEhcmEmpPerinfo(), currentPos, dateBeforeForassign, null, promotion,
                  null, null, null, null, null);

              assingedOrReleaseEmpInPositionDAO.insertPositionEmployeeHisotry(promotion.getClient(),
                  promotion.getOrganization(), promotion.getEhcmEmpPerinfo(), null,
                  promotion.getStartDate(), null, promotion.getDecisionNo(),
                  promotion.getDecisionDate(), newpos, vars, null, promotion, null);
            } else {
              /*
               * if (empTransfer.getEndDate() != null) {
               * assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
               * empTransfer.getEhcmEmpPerinfo(), currentPos, empTransfer.getEndDate(),
               * empTransfer); }
               */
            }

          } else {

            dateBeforeForassign = new Date(promotion.getStartDate().getTime() - millSec);

            assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
                promotion.getEhcmEmpPerinfo(), pos, dateBeforeForassign, null, promotion, null,
                null, null, null, null);

            assingedOrReleaseEmpInPositionDAO.insertPositionEmployeeHisotry(promotion.getClient(),
                promotion.getOrganization(), promotion.getEhcmEmpPerinfo(), null,
                promotion.getStartDate(), null, promotion.getDecisionNo(),
                promotion.getDecisionDate(), newpos, vars, null, promotion, null);

          }
        }

        log.debug("employInfo.getEhcmEmpTransfer:" + employInfo.getEhcmEmpPromotion().getId());
        log.debug("getChangereason:" + employInfo.getChangereason());

        if (decisionType.equals("CR")) {
          // update the endate and active flag for old hiring record.
          OBQuery<EmploymentInfo> empInfoold = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId() + "'  and id not in ('"
                  + employInfo.getId() + "') and enabled='Y' order by creationDate desc ");
          empInfoold.setMaxResult(1);
          if (empInfoold.list().size() > 0) {
            EmploymentInfo empinfo = empInfoold.list().get(0);
            empinfo.setUpdated(new java.util.Date());
            empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
            Date startdate = empinfo.getStartDate();
            Date dateBefore = new Date(promotion.getStartDate().getTime() - millSec);
            if (!promotion.isJoinWorkRequest()) {
              dateBefore = new Date(promotion.getStartDate().getTime() - millSec);
              if (startdate.compareTo(promotion.getStartDate()) == 0)
                empinfo.setEndDate(promotion.getStartDate());
              else
                empinfo.setEndDate(dateBefore);
            } else {
              dateBefore = new Date(joinReqId.getJoindate().getTime() - millSec);

              if (startdate.compareTo(joinReqId.getJoindate()) == 0)
                empinfo.setEndDate(empinfo.getStartDate());
              else
                empinfo.setEndDate(dateBefore);
            }

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

            OBQuery<EmployeeDelegation> del = OBDal.getInstance().createQuery(
                EmployeeDelegation.class,
                " ehcmEmploymentInfo.id='" + empinfo.getId() + "' and enabled='Y' ");
            del.setMaxResult(1);
            if (del.list().size() > 0) {
              EmployeeDelegation delegation = del.list().get(0);
              delegation.setEhcmEmploymentInfo(employInfo);
              OBDal.getInstance().save(delegation);
              OBDal.getInstance().flush();
            }
            log.debug("getEndDate:" + empinfo.getEndDate());
          }
        }
      }

    } catch (

    Exception e) {
      e.printStackTrace();
      log.error("Exception in insertEmploymentInfo in empPromotion: ", e);
    }
    return employInfo;
  }

  public static void updateEnddateinEmpInfo(EHCMEmpPromotion promotion, EmploymentInfo employInfo,
      VariablesSecureApp vars) throws Exception {
    // TODO Auto-generated method stub
    try {
      // update the endate and active flag for old hiring record.
      OBQuery<EmploymentInfo> empInfoold = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId() + "'  and id not in ('"
              + employInfo.getId()
              + "') and enabled='N' and issecondment='N' order by creationDate desc ");
      empInfoold.setMaxResult(1);
      if (empInfoold.list().size() > 0) {
        EmploymentInfo empinfo = empInfoold.list().get(0);
        empinfo.setUpdated(new java.util.Date());
        empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        Date startdate = empinfo.getStartDate();
        Date dateBefore = new Date(promotion.getStartDate().getTime() - 1 * 24 * 3600 * 1000);
        log.debug("stat:" + startdate);
        log.debug("updateposition.getStartDate():" + empinfo.getStartDate());
        log.debug("updateposition.compareTo():" + startdate.compareTo(promotion.getStartDate()));
        log.debug("updateposition.dateBefore():" + dateBefore);
        if (startdate.compareTo(promotion.getStartDate()) == 0)
          empinfo.setEndDate(empinfo.getStartDate());
        else
          empinfo.setEndDate(dateBefore);
      }

      // update old promotion as inactive
      EHCMEmpPromotion oldPromotion = promotion.getOriginalDecisionsNo();
      oldPromotion.setEnabled(false);
      OBDal.getInstance().save(oldPromotion);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateEnddateinEmpInfo ", e);
    }
  }

  public static void CancelinPromotion(EHCMEmpPromotion promotion, VariablesSecureApp vars)
      throws Exception {
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();

    // TODO Auto-generated method stub
    try {
      // update the acive flag='Y' and enddate is null for recently update record
      OBQuery<EmploymentInfo> originalemp = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId()
              + "' and enabled='N' and issecondment='N' order by creationDate desc ");
      originalemp.setMaxResult(1);
      if (originalemp.list().size() > 0) {
        EmploymentInfo empinfo = originalemp.list().get(0);
        log.debug("getChangereasoncancel12:" + empinfo.getId());
        empinfo.setUpdated(new java.util.Date());
        empinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
        empinfo.setEndDate(null);
        empinfo.setEnabled(true);
        empinfo.setAlertStatus("ACT");
        // empinfo.setEhcmEmpTransfer(null);
        if (empinfo.getPosition() != null) {
          EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
              promotion.getPosition().getId());
          /*
           * Task No.6797 pos.setAssignedEmployee(null); OBDal.getInstance().save(pos);
           * OBDal.getInstance().flush();
           */
          /*
           * EhcmPosition currentPos = assingedOrReleaseEmpInPositionDAO.getRecentPosition(
           * promotion.getEhcmEmpPerinfo(), promotion.getOriginalDecisionsNo(), null, null);
           */
          EmploymentInfo recentEmployeInfo = assingedOrReleaseEmpInPositionDAO
              .getRecentEmploymentInfo(promotion.getEhcmEmpPerinfo(),
                  promotion.getOriginalDecisionsNo(), null, null);

          assingedOrReleaseEmpInPositionDAO
              .deletePositionEmployeeHisotry(promotion.getEhcmEmpPerinfo(), pos);

          EhcmPosition newpos = OBDal.getInstance().get(EhcmPosition.class,
              empinfo.getPosition().getId());
          /*
           * newpos.setAssignedEmployee( OBDal.getInstance().get(EmployeeView.class,
           * promotion.getEhcmEmpPerinfo().getId())); OBDal.getInstance().save(newpos);
           * OBDal.getInstance().flush();
           */

          assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
              promotion.getEhcmEmpPerinfo(), recentEmployeInfo.getPosition(), null, null, null,
              null, null, null, null, recentEmployeInfo);
        }
        OBDal.getInstance().save(empinfo);
        OBDal.getInstance().flush();

        OBQuery<EmployeeDelegation> del = OBDal.getInstance().createQuery(EmployeeDelegation.class,
            " ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId()
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
              empinfo
                  .setSECLocation(delegation.getNewSection().getEhcmLocation().getLocationName());
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
        // remove the recent record
        OBQuery<EmploymentInfo> employInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ehcmEmpPerinfo.id='" + promotion.getEhcmEmpPerinfo().getId()
                + "'  and enabled='Y' and issecondment='N' and id not in ('" + empinfo.getId()
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
      // update old promotion as inactive
      EHCMEmpPromotion oldPromotion = promotion.getOriginalDecisionsNo();
      oldPromotion.setEnabled(false);
      OBDal.getInstance().save(oldPromotion);
      OBDal.getInstance().flush();

      promotion.setEnabled(false);
      OBDal.getInstance().save(promotion);
      OBDal.getInstance().flush();

    } catch (

    Exception e) {
      e.printStackTrace();
      log.error("Exception in CancelinPromotion ", e);
    }
  }
}
