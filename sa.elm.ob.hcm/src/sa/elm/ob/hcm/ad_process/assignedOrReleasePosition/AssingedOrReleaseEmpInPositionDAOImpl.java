package sa.elm.ob.hcm.ad_process.assignedOrReleasePosition;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.EHCMEmpPromotion;
import sa.elm.ob.hcm.EHCMEmpTransfer;
import sa.elm.ob.hcm.EHCMEmpTransferSelf;
import sa.elm.ob.hcm.EHCMPosEmpHistory;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmployeeDelegation;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.utility.util.Utility;

/**
 * This process class used for Assigned or Release employee in Position DAO Implementation
 * 
 * @author divya 30-07-2018
 *
 */

public class AssingedOrReleaseEmpInPositionDAOImpl implements AssingedOrReleaseEmpInPositionDAO {

  private static final Logger log = LoggerFactory
      .getLogger(AssingedOrReleaseEmpInPositionDAOImpl.class);

  @Override
  public void insertPositionEmployeeHisotry(Client client, Organization org,
      EhcmEmpPerInfo employee, EmployeeDelegation empDelegation, Date startDate, Date endDate,
      String decisionNo, Date decisionDate, EhcmPosition position, VariablesSecureApp vars,
      EHCMEmpTransfer empTransfer, EHCMEmpPromotion empPromotion,
      EHCMEmpTransferSelf empTransferSelf) throws Exception {
    // TODO Auto-generated method stub

    try {
      EHCMPosEmpHistory posEmpHisotry = OBProvider.getInstance().get(EHCMPosEmpHistory.class);
      posEmpHisotry.setClient(client);
      posEmpHisotry.setOrganization(org);
      posEmpHisotry.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      posEmpHisotry.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
      posEmpHisotry.setEmployee(employee);
      posEmpHisotry.setDecisionDate(decisionDate);
      posEmpHisotry.setDecisionNo(decisionNo);
      posEmpHisotry.setEhcmEmpDelegation(empDelegation);
      posEmpHisotry.setStartDate(startDate);
      posEmpHisotry.setEndDate(endDate);
      posEmpHisotry.setPosition(position);
      if (empDelegation != null) {
        posEmpHisotry.setEhcmEmpDelegation(empDelegation);
        posEmpHisotry.setDelegated(true);
      }
      if (empTransfer != null) {
        posEmpHisotry.setEhcmEmpTransfer(empTransfer);
      }
      if (empPromotion != null) {
        posEmpHisotry.setEhcmEmpPromotion(empPromotion);
      }
      if (empTransferSelf != null) {
        posEmpHisotry.setEhcmEmpTransferSelf(empTransferSelf);
      }
      OBDal.getInstance().save(posEmpHisotry);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in insertPositionEmployeeHisotry: ", e);
    }
  }

  @Override
  public void deletePositionEmployeeHisotry(EhcmEmpPerInfo employee, EhcmPosition postion)
      throws Exception {
    // TODO Auto-generated method stub
    List<EHCMPosEmpHistory> posEmpHistoryList = null;
    try {
      OBQuery<EHCMPosEmpHistory> posEmpHistoryQry = OBDal.getInstance().createQuery(
          EHCMPosEmpHistory.class,
          " as e where e.employee.id=:employeeId  and e.position.id=:postionId  ORDER BY e.creationDate desc ");
      posEmpHistoryQry.setNamedParameter("employeeId", employee.getId());
      posEmpHistoryQry.setNamedParameter("postionId", postion.getId());
      posEmpHistoryQry.setMaxResult(1);
      posEmpHistoryList = posEmpHistoryQry.list();
      if (posEmpHistoryList.size() > 0) {
        EHCMPosEmpHistory posEmpHistoryObj = posEmpHistoryList.get(0);
        OBDal.getInstance().remove(posEmpHistoryObj);
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in deletePositionEmployeeHisotry: ", e);
    }

  }

  @Override
  public void updateEndDateForDelegatedEmployee(EhcmEmpPerInfo employee, Date enddate,
      EmployeeSuspension suspension, EHCMEMPTermination endofemployment, String decisionType)
      throws Exception {
    // int a = 0;
    // TODO Auto-generated method stub
    List<EHCMPosEmpHistory> posEmpHistoryList = null;
    String hql = "";
    try {
      if (!decisionType.equals("CR")) {
        revertOldValuesForSuspensionOrTermination(employee, suspension, endofemployment, enddate);
      }

      if (decisionType.equals("CA")) {
        if (suspension != null) {
          hql = " and e.ehcmEmpSuspension.id =:empSuspensionId ";
        }
        if (endofemployment != null) {
          hql = " and e.ehcmEmpTermination.id =:endofEmploymentId ";
        }
      }
      if (!decisionType.equals("CA")) {
        OBQuery<EHCMPosEmpHistory> posEmpHistoryQry = OBDal.getInstance().createQuery(
            EHCMPosEmpHistory.class,
            " as e where e.employee.id=:employeeId and e.isdelegated = 'Y' and "
                + " to_date(:end,'yyyy-MM-dd') between e.startDate and e.endDate " + hql);
        posEmpHistoryQry.setNamedParameter("employeeId", employee.getId());
        posEmpHistoryQry.setNamedParameter("end", enddate);
        /*
         * if (decisionType.equals("CA")) { if (suspension != null) {
         * posEmpHistoryQry.setNamedParameter("empSuspensionId",
         * suspension.getOriginalDecisionNo().getId()); } if (endofemployment != null) {
         * posEmpHistoryQry.setNamedParameter("endofEmploymentId",
         * endofemployment.getOriginalDecisionsNo().getId()); } }
         */
        posEmpHistoryQry.setMaxResult(1);
        posEmpHistoryList = posEmpHistoryQry.list();
        if (posEmpHistoryList.size() > 0) {
          EHCMPosEmpHistory posEmpHistoryObj = posEmpHistoryList.get(0);
          /*
           * if (decisionType.equals("CA")) { if (posEmpHistoryObj.getEhcmEmpDelegation() != null)
           * posEmpHistoryObj.setEndDate(posEmpHistoryObj.getEhcmEmpDelegation().getEndDate()); }
           * else {
           */
          posEmpHistoryObj.setEndDate(enddate);
          if (suspension != null) {
            posEmpHistoryObj.setEhcmEmpSuspension(suspension);
            posEmpHistoryObj.getEhcmEmpDelegation().setEhcmEmpSuspension(suspension);
          }
          if (endofemployment != null) {
            posEmpHistoryObj.setEhcmEmpTermination(endofemployment);
            posEmpHistoryObj.getEhcmEmpDelegation().setEhcmEmpTermination(endofemployment);

          }

          // }
          OBDal.getInstance().save(posEmpHistoryObj);
        } else {
          OBQuery<EHCMPosEmpHistory> posEmpHistory = OBDal.getInstance().createQuery(
              EHCMPosEmpHistory.class,
              " as e where e.employee.id=:employeeId and e.isdelegated = 'Y' and "
                  + " to_date(:end,'yyyy-MM-dd') < e.startDate ");
          posEmpHistory.setNamedParameter("employeeId", employee.getId());
          posEmpHistory.setNamedParameter("end", enddate);

          posEmpHistory.setMaxResult(1);
          posEmpHistoryList = posEmpHistory.list();
          if (posEmpHistoryList.size() > 0) {
            for (EHCMPosEmpHistory posEmpHistoryObject : posEmpHistoryList) {
              // if (decisionType.equals("CR")) {
              EHCMPosEmpHistory posEmpHistoryObj = posEmpHistoryObject;
              posEmpHistoryObj.getEhcmEmpDelegation().setDecisionStatus("CA");
              if (suspension != null) {
                posEmpHistoryObj.getEhcmEmpDelegation().setEhcmEmpSuspension(suspension);
              }
              if (endofemployment != null) {
                posEmpHistoryObj.getEhcmEmpDelegation().setEhcmEmpTermination(endofemployment);
              }

              OBDal.getInstance().remove(posEmpHistoryObj);
              // OBDal.getInstance().save(posEmpHistory);
            }

          }
        } /*
           * else { // update back to delegated employee in position employee history while update
           * or cancel // the suspension end for end date less than with delegated startdate
           * updateCancelledDelegatedEmpPosition(employee, enddate,
           * suspension.getOriginalDecisionNo(), endofemployment.getOriginalDecisionsNo()); if
           * (decisionType.equals("UP")) { updateEndDateForDelegatedEmployee(employee, enddate,
           * suspension, endofemployment, "CR"); } } }
           */
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateEndDateForDelegatedEmployee: ", e);
    }

  }

  public void revertOldValuesForSuspensionOrTermination(EhcmEmpPerInfo employee,
      EmployeeSuspension empSuspension, EHCMEMPTermination endofEmployment, Date enddate)
      throws Exception {
    // TODO Auto-generated method stub
    List<EHCMPosEmpHistory> posEmpHistoryList = null;
    String hql = "";
    try {
      OBContext.setAdminMode();

      if (empSuspension != null) {
        hql = " and e.ehcmEmpSuspension.id=:suspensionId ";
      }

      if (endofEmployment != null) {
        hql = " and e.ehcmEmpTermination.id=:terminationId ";
      }
      OBQuery<EHCMPosEmpHistory> posEmpHistoryQry = OBDal.getInstance().createQuery(
          EHCMPosEmpHistory.class,
          " as e where e.employee.id=:employeeId and e.isdelegated = 'Y' " + hql);
      posEmpHistoryQry.setNamedParameter("employeeId", employee.getId());
      if (empSuspension != null) {
        posEmpHistoryQry.setNamedParameter("suspensionId",
            empSuspension.getOriginalDecisionNo().getId());
      }
      if (endofEmployment != null) {
        posEmpHistoryQry.setNamedParameter("terminationId",
            endofEmployment.getOriginalDecisionsNo().getId());
      }
      posEmpHistoryList = posEmpHistoryQry.list();
      if (posEmpHistoryList.size() > 0) {
        for (EHCMPosEmpHistory empHisotry : posEmpHistoryList) {
          EHCMPosEmpHistory posEmpHistoryObj = empHisotry;
          if (posEmpHistoryObj.getEhcmEmpDelegation() != null) {
            posEmpHistoryObj.setEndDate(posEmpHistoryObj.getEhcmEmpDelegation().getEndDate());
            if (empSuspension != null && empSuspension.getDecisionType().equals("CA")) {
              posEmpHistoryObj.setEhcmEmpSuspension(null);
            }
            if (endofEmployment != null && endofEmployment.getDecisionType().equals("CA")) {
              posEmpHistoryObj.setEhcmEmpTermination(null);
            }
            OBDal.getInstance().save(posEmpHistoryObj);
          }
        }
      } else {
        updateCancelledDelegatedEmpPosition(employee, enddate,
            empSuspension == null ? null : empSuspension.getOriginalDecisionNo(),
            endofEmployment == null ? null : endofEmployment.getOriginalDecisionsNo(),
            (empSuspension == null
                ? (endofEmployment == null ? null : endofEmployment.getDecisionType())
                : empSuspension.getDecisionType()));
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateCancelledDelegatedEmpPosition: ", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public void updateCancelledDelegatedEmpPosition(EhcmEmpPerInfo employee, Date enddate,
      EmployeeSuspension empSuspension, EHCMEMPTermination ehcmempTermination, String decisionType)
      throws Exception {
    // TODO Auto-generated method stub
    List<EmployeeDelegation> empdelegatedlist = null;
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String hql = "";
    try {
      if (empSuspension != null) {
        hql = " and e.ehcmEmpSuspension.id=:suspensionId ";
      }

      if (ehcmempTermination != null) {
        hql = " and e.ehcmEmpTermination.id=:terminationId ";
      }

      OBQuery<EmployeeDelegation> delegate = OBDal.getInstance()
          .createQuery(EmployeeDelegation.class, "as e where e.ehcmEmpPerinfo.id=:employeeId " + hql
              + " and decisionStatus ='CA' and e.client.id=:clientId ");

      // and ( (to_date(:end,'yyyy-MM-dd') < e.startDate) "
      // + " or to_date(:end,'yyyy-MM-dd') between e.startDate and e.endDate)"
      delegate.setNamedParameter("employeeId", employee.getId());
      // delegate.setNamedParameter("end", enddate);
      if (empSuspension != null) {
        delegate.setNamedParameter("suspensionId", empSuspension.getId());
      }
      if (ehcmempTermination != null) {
        delegate.setNamedParameter("terminationId", ehcmempTermination.getId());
      }
      delegate.setNamedParameter("clientId", employee.getClient().getId());
      empdelegatedlist = delegate.list();
      if (empdelegatedlist.size() > 0) {
        for (EmployeeDelegation empdelegationobj : empdelegatedlist) {
          EhcmPosition position = OBDal.getInstance().get(EhcmPosition.class,
              empdelegationobj.getNewPosition().getId());
          empdelegationobj.setDecisionStatus("I");
          if (empSuspension != null && decisionType != null && decisionType.equals("CA")) {
            empdelegationobj.setEhcmEmpSuspension(null);
          }
          if (ehcmempTermination != null && decisionType != null && decisionType.equals("CA")) {
            empdelegationobj.setEhcmEmpTermination(null);
          }
          insertPositionEmployeeHisotry(empdelegationobj.getClient(),
              empdelegationobj.getOrganization(), empdelegationobj.getEhcmEmpPerinfo(),
              empdelegationobj, empdelegationobj.getStartDate(), empdelegationobj.getEndDate(),
              empdelegationobj.getDecisionNo(), empdelegationobj.getDecisionDate(), position, vars,
              null, null, null);
          OBDal.getInstance().save(empdelegationobj);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateCancelledDelegatedEmpPosition: ", e);
    }
  }

  @Override
  public boolean checkDelegatedRecordwithGreaterthanStartDate(EhcmEmpPerInfo employee, Date enddate)
      throws Exception {
    // TODO Auto-generated method stub
    List<EHCMPosEmpHistory> posemplist = null;
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    try {
      OBQuery<EHCMPosEmpHistory> posEmpHistory = OBDal.getInstance().createQuery(
          EHCMPosEmpHistory.class,
          " as e where e.employee.id=:employeeId and e.isdelegated = 'Y' and "
              + " to_date(:end,'yyyy-MM-dd') < e.startDate ");
      posEmpHistory.setNamedParameter("employeeId", employee.getId());
      posEmpHistory.setNamedParameter("end", enddate);
      posemplist = posEmpHistory.list();
      if (posemplist.size() > 0) {
        return true;
      }
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateCancelledDelegatedEmpPosition: ", e);
    }
    return false;
  }

  @Override
  public void updateEndDateInPositionEmployeeHisotry(EhcmEmpPerInfo employee, EhcmPosition postion,
      Date date, EHCMEmpTransfer empTransfer, EHCMEmpPromotion empPromotion,
      EHCMEmpTransferSelf empTransferSelf, EmployeeDelegation empDelegation,
      EmployeeSuspension suspension, EHCMEMPTermination endofemployment,
      EmploymentInfo recentEmployeInfo) throws Exception {
    // TODO Auto-generated method stub
    List<EHCMPosEmpHistory> posEmpHistoryList = null;
    String hql = "";

    try {

      if (empDelegation != null) {
        hql = " and e.isdelegated = 'Y' ";
      } else {
        hql = " and e.isdelegated = 'N' ";
      }
      OBQuery<EHCMPosEmpHistory> posEmpHistoryQry = OBDal.getInstance().createQuery(
          EHCMPosEmpHistory.class,
          " as e where e.employee.id=:employeeId  and e.position.id=:postionId " + hql
              + "   ORDER BY e.creationDate desc ");
      posEmpHistoryQry.setNamedParameter("employeeId", employee.getId());
      posEmpHistoryQry.setNamedParameter("postionId", postion.getId());
      posEmpHistoryQry.setMaxResult(1);
      posEmpHistoryList = posEmpHistoryQry.list();
      if (posEmpHistoryList.size() > 0) {
        EHCMPosEmpHistory posEmpHistoryObj = posEmpHistoryList.get(0);

        posEmpHistoryObj.setEndDate(date);
        if (empTransfer != null) {
          posEmpHistoryObj.setEhcmEmpTransfer(empTransfer);
          posEmpHistoryObj.setDecisionDate(empTransfer.getDecisionDate());
          posEmpHistoryObj.setDecisionNo(empTransfer.getDecisionNo());
        }
        if (empPromotion != null) {
          posEmpHistoryObj.setEhcmEmpPromotion(empPromotion);
          posEmpHistoryObj.setDecisionDate(empPromotion.getDecisionDate());
          posEmpHistoryObj.setDecisionNo(empPromotion.getDecisionNo());
        }
        if (empTransferSelf != null) {
          posEmpHistoryObj.setEhcmEmpTransferSelf(empTransferSelf);
          posEmpHistoryObj.setDecisionDate(empTransferSelf.getDecisionDate());
          posEmpHistoryObj.setDecisionNo(empTransferSelf.getDecisionNo());
        }
        if (empDelegation != null) {
          posEmpHistoryObj.setEhcmEmpDelegation(empDelegation);
          posEmpHistoryObj.setDecisionDate(empDelegation.getDecisionDate());
          posEmpHistoryObj.setDecisionNo(empDelegation.getDecisionNo());
        }
        if (suspension != null) {
          posEmpHistoryObj.setEhcmEmpSuspension(suspension);
          posEmpHistoryObj.setDecisionDate(suspension.getDecisionDate());
          posEmpHistoryObj.setDecisionNo(suspension.getDecisionNo());
        }
        if (endofemployment != null) {
          posEmpHistoryObj.setEhcmEmpTermination(endofemployment);
          posEmpHistoryObj.setDecisionDate(endofemployment.getDecisionDate());
          posEmpHistoryObj.setDecisionNo(endofemployment.getDecisionNo());
        }
        if (recentEmployeInfo != null) {
          if (recentEmployeInfo.getEhcmEmpTransfer() != null) {
            posEmpHistoryObj.setEhcmEmpTransfer(recentEmployeInfo.getEhcmEmpTransfer());
            posEmpHistoryObj.setEndDate(recentEmployeInfo.getEhcmEmpTransfer().getEndDate());
          }
          if (recentEmployeInfo.getEhcmEmpTransferSelf() != null) {
            posEmpHistoryObj.setEhcmEmpTransferSelf(recentEmployeInfo.getEhcmEmpTransferSelf());
            posEmpHistoryObj.setEndDate(recentEmployeInfo.getEhcmEmpTransferSelf().getEndDate());
          }
        }
        OBDal.getInstance().save(posEmpHistoryObj);
      }
      OBDal.getInstance().flush();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in updateEndDate: ", e);
    }

  }

  @Override
  public EhcmPosition revertOldValuesAndGetOldestPosition(EhcmEmpPerInfo employee,
      EHCMEmpTransfer empTransfer, EHCMEmpPromotion empPromotion,
      EHCMEmpTransferSelf empTransferSelf) throws Exception {
    // TODO Auto-generated method stub
    List<EHCMPosEmpHistory> posEmpHistoryList = null;
    String hql = "";
    EhcmPosition position = null;
    EhcmPosition oldestPos = null;
    try {

      if (empTransfer != null) {
        hql = " and e.ehcmEmpTransfer.id=:empTransferId ";
      }
      if (empPromotion != null) {
        hql = " and e.ehcmEmpPromotion.id=:empPromotionId ";
      }
      if (empTransferSelf != null) {
        hql = " and e.ehcmEmpTransferSelf.id=:empTransferSelfId ";
      }
      OBQuery<EHCMPosEmpHistory> posEmpHistoryQry = OBDal.getInstance().createQuery(
          EHCMPosEmpHistory.class,
          " as e where e.employee.id=:employeeId  " + hql + " ORDER BY e.creationDate asc");
      posEmpHistoryQry.setNamedParameter("employeeId", employee.getId());
      if (empTransfer != null) {
        posEmpHistoryQry.setNamedParameter("empTransferId",
            empTransfer.getOriginalDecisionsNo().getId());
      }
      if (empPromotion != null) {
        posEmpHistoryQry.setNamedParameter("empPromotionId",
            empPromotion.getOriginalDecisionsNo().getId());
      }
      if (empTransferSelf != null) {
        posEmpHistoryQry.setNamedParameter("empTransferSelfId",
            empTransferSelf.getOriginalDecisionsNo().getId());
      }
      posEmpHistoryList = posEmpHistoryQry.list();
      if (posEmpHistoryList.size() > 0) {
        for (EHCMPosEmpHistory posHistory : posEmpHistoryList) {
          position = posHistory.getPosition();

          if (posHistory.equals(posEmpHistoryList.get(0))) {
            oldestPos = position;
            updateEndDateInPositionEmployeeHisotry(employee, position, null, empTransfer,
                empPromotion, empTransferSelf, null, null, null, null);
          } else {
            deletePositionEmployeeHisotry(employee, position);
          }

        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in revertOldValues: ", e);
    }
    return oldestPos;
  }

  public EhcmPosition getCurrentPositionOfEmployee(EhcmEmpPerInfo employee,
      EHCMEmpPromotion empPromotion, EHCMEmpTransfer empTransfer) throws Exception {
    // TODO Auto-generated method stub
    List<EmploymentInfo> empInfoList = null;
    EhcmPosition position = null;
    String hql = "";
    try {

      if (empPromotion != null) {
        hql = " and  (e.ehcmEmpPromotion.id<>:promotionId  or e.ehcmEmpPromotion.id is null  ) ";
      }
      if (empTransfer != null) {
        hql = " and  (e.ehcmEmpTransfer.id<>:transferId  or e.ehcmEmpTransfer.id is null  ) ";
      }
      OBQuery<EmploymentInfo> empInforQry = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e where e.ehcmEmpPerinfo.id=:employeeId  " + hql + " ORDER BY e.creationDate desc");
      empInforQry.setNamedParameter("employeeId", employee.getId());
      if (empPromotion != null) {
        empInforQry.setNamedParameter("promotionId", empPromotion.getId());
      }
      if (empTransfer != null) {
        empInforQry.setNamedParameter("transferId", empTransfer.getId());
      }
      empInforQry.setFilterOnActive(false);
      empInforQry.setMaxResult(1);
      empInfoList = empInforQry.list();
      if (empInfoList.size() > 0) {
        position = empInfoList.get(0).getPosition();
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in getCurrentPositionOfEmployee: ", e);
    }
    return position;
  }

  @Override
  public EhcmPosition getRecentPosition(EhcmEmpPerInfo employee, EHCMEmpPromotion promotion,
      EHCMEmpTransfer empTransfer, EHCMEmpTransferSelf empTransferSelf) throws Exception {
    // TODO Auto-generated method stub
    List<EHCMPosEmpHistory> posEmpHistoryList = null;
    String hql = "";
    EhcmPosition recentPos = null;
    try {

      if (empTransfer != null) {
        hql = " and e.ehcmEmpTransfer.id=:empTransferId ";
      }
      if (promotion != null) {
        hql = " and e.ehcmEmpPromotion.id=:empPromotionId ";
      }
      if (empTransferSelf != null) {
        hql = " and e.ehcmEmpTransferSelf.id=:empTransferSelfId ";
      }
      OBQuery<EHCMPosEmpHistory> posEmpHistoryQry = OBDal.getInstance().createQuery(
          EHCMPosEmpHistory.class,
          " as e where e.employee.id=:employeeId  " + hql + " ORDER BY e.creationDate asc");
      posEmpHistoryQry.setNamedParameter("employeeId", employee.getId());
      if (empTransfer != null) {
        posEmpHistoryQry.setNamedParameter("empTransferId", empTransfer.getId());
      }
      if (promotion != null) {
        posEmpHistoryQry.setNamedParameter("empPromotionId",
            promotion.getOriginalDecisionsNo().getId());
      }
      if (empTransferSelf != null) {
        posEmpHistoryQry.setNamedParameter("empTransferSelfId",
            empTransferSelf.getOriginalDecisionsNo().getId());
      }
      posEmpHistoryQry.setMaxResult(1);
      posEmpHistoryList = posEmpHistoryQry.list();
      if (posEmpHistoryList.size() > 0) {
        recentPos = posEmpHistoryList.get(0).getPosition();
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in revertOldValues: ", e);
    }
    return recentPos;
  }

  @Override
  public Boolean chkPositionAvailableOrNot(EhcmEmpPerInfo employee, EhcmPosition position,
      Date startDate, Date endDate, String decisionType, Boolean isdelegated) throws Exception {
    // TODO Auto-generated method stub
    List<EHCMPosEmpHistory> posEmpHistoryList = null;
    String startdate = null;
    String enddate = null;
    Boolean chkPositionAvailableOrNot = false;
    String hql = "";
    try {
      if (decisionType.equals("UP"))
        hql = " and e.employee.id<>:employeeId ";

      if (endDate == null) {
        enddate = "21-06-2058";
      } else {
        enddate = Utility.formatDate(endDate);
      }
      /*
       * if (isdelegated) { hql += " and e.isdelegated='Y' "; } else { hql +=
       * " and e.isdelegated='N' "; }
       */

      startdate = Utility.formatDate(startDate);
      OBQuery<EHCMPosEmpHistory> posEmpHistQry = OBDal.getInstance()
          .createQuery(EHCMPosEmpHistory.class, " as e" + "  where e.position.id=:positionId "
              + " and ((to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
              + " and to_date(to_char(coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
              + " or (to_date(to_char( coalesce (endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate,'dd-MM-yyyy') "
              + " and to_date(to_char(startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')))  "
              + hql);
      posEmpHistQry.setNamedParameter("positionId", position.getId());
      if (decisionType.equals("UP"))
        posEmpHistQry.setNamedParameter("employeeId", employee.getId());
      posEmpHistQry.setNamedParameter("fromdate", startdate);
      posEmpHistQry.setNamedParameter("todate", enddate);
      posEmpHistoryList = posEmpHistQry.list();
      if (posEmpHistoryList.size() > 0) {
        chkPositionAvailableOrNot = true;
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in chkPositionAvailableOrNot: ", e);
    }
    return chkPositionAvailableOrNot;
  }

  @Override
  public Boolean chkDelegatePositionAvailableOrNot(EhcmEmpPerInfo employee,
      EmployeeSuspension empSuspension, EHCMEMPTermination ehcmempTermination, Date StartDate,
      Date Enddate, String decisionType, Boolean isdelegated) throws Exception {
    // TODO Auto-generated method stub
    List<EmployeeDelegation> empDelegationList = null;
    Boolean chkPositionAvailableOrNot = false;
    String hql = "";
    try {
      if (empSuspension != null)
        hql = " and e.ehcmEmpSuspension.id=:suspensionId ";
      if (ehcmempTermination != null)
        hql = " and e.ehcmEmpTermination.id=:terminationId ";

      OBQuery<EmployeeDelegation> empDelegationQry = OBDal.getInstance().createQuery(
          EmployeeDelegation.class, " as e" + " where  e.ehcmEmpPerinfo.id=:employeeId " + hql);

      empDelegationQry.setNamedParameter("employeeId", employee.getId());
      if (empSuspension != null)
        empDelegationQry.setNamedParameter("suspensionId", empSuspension.getId());
      if (ehcmempTermination != null)
        empDelegationQry.setNamedParameter("terminationId", ehcmempTermination.getId());
      empDelegationList = empDelegationQry.list();
      if (empDelegationList.size() > 0) {
        for (EmployeeDelegation employeeDelegation : empDelegationList) {
          if (employeeDelegation.getNewPosition() != null) {
            EhcmPosition delegatePos = OBDal.getInstance().get(EhcmPosition.class,
                employeeDelegation.getNewPosition().getId());

            chkPositionAvailableOrNot = chkPositionAvailableOrNot(employee, delegatePos, StartDate,
                Enddate, decisionType, isdelegated);
            if (chkPositionAvailableOrNot) {
              return chkPositionAvailableOrNot;
            }
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in chkPositionAvailableOrNot: ", e);
    }
    return chkPositionAvailableOrNot;
  }

  @Override
  public EmploymentInfo getRecentEmploymentInfo(EhcmEmpPerInfo employee, EHCMEmpPromotion promotion,
      EHCMEmpTransfer empTransfer, EHCMEmpTransferSelf empTransferSelf) throws Exception {
    // TODO Auto-generated method stub
    List<EmploymentInfo> employInfoList = null;
    String hql = "";
    EmploymentInfo recentEmployInfo = null;
    try {

      if (empTransfer != null) {
        hql = " and (e.ehcmEmpTransfer.id<>:empTransferId  or e.ehcmEmpTransfer.id is null)";
      }
      if (promotion != null) {
        hql = " and (e.ehcmEmpPromotion.id<>:empPromotionId  or e.ehcmEmpPromotion.id is null) ";
      }
      if (empTransferSelf != null) {
        hql = " and( e.ehcmEmpTransferSelf.id<>:empTransferSelfId or e.ehcmEmpTransferSelf.id is null) ";
      }
      OBQuery<EmploymentInfo> employInfoQry = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " as e where e.ehcmEmpPerinfo.id=:employeeId " + hql + " order by e.creationDate desc ");
      employInfoQry.setNamedParameter("employeeId", employee.getId());
      if (empTransfer != null) {
        employInfoQry.setNamedParameter("empTransferId", empTransfer.getId());
      }
      if (promotion != null) {
        employInfoQry.setNamedParameter("empPromotionId", promotion.getId());
      }
      if (empTransferSelf != null) {
        employInfoQry.setNamedParameter("empTransferSelfId", empTransferSelf.getId());
      }
      employInfoQry.setMaxResult(1);
      employInfoList = employInfoQry.list();
      if (employInfoList.size() > 0) {
        recentEmployInfo = employInfoList.get(0);
      }

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Exception in getRecentEmploymentInfo: ", e);
    }
    return recentEmployInfo;
  }
}
