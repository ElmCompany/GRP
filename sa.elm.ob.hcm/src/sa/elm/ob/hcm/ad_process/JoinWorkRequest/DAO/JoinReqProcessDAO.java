package sa.elm.ob.hcm.ad_process.JoinWorkRequest.DAO;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmJoiningWorkRequest;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EhcmSuspensionEmpV;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmempstatus;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;

/**
 * 
 * @author Poongodi 01/03/2018
 *
 */
public class JoinReqProcessDAO {
  private static final Logger LOG = LoggerFactory.getLogger(JoinReqProcessDAO.class);

  /**
   * 
   * @param joinProcess
   * @param employmentId
   * @param info
   * @return
   */
  @SuppressWarnings("rawtypes")
  public static boolean chkPeriodExist(EhcmJoiningWorkRequest joinProcess, String employmentId,
      EmploymentInfo info) {
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    boolean chkPeriod = false;
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " id not in ('" + employmentId
              + "') and ehcmEmpPerinfo.id = :employeeId and creationDate > '"
              + info.getCreationDate() + "' order by creationDate desc ");
      empInfoObj.setNamedParameter("employeeId", joinProcess.getEmployee().getId());
      empInfoObj.setMaxResult(1);
      empInfoList = empInfoObj.list();
      if (empInfoList.size() > 0) {
        EmploymentInfo empinfo = empInfoList.get(0);
        if (empinfo.getEhcmEmpSecondment() != null) {
          if (joinProcess.getJoindate().compareTo(empinfo.getStartDate()) > 0) {
            chkPeriod = true;
          }
        }
      }

    } catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Exception chkPeriodExist in joinrequest : ", e, e);
      }
      OBDal.getInstance().rollbackAndClose();

    } finally {
      OBContext.restorePreviousMode();
    }
    return chkPeriod;
  }

  public static boolean chkAnyEmployInfoExistsAfter(EhcmJoiningWorkRequest joinProcess,
      EmploymentInfo info) {
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    boolean chkAnyEmployInfoExistsAfter = false;
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " id <> :employmentId  and ehcmEmpPerinfo.id = :employeeId and creationDate > :creationDate "
              + " order by creationDate desc ");
      empInfoObj.setNamedParameter("employmentId", info.getId());
      empInfoObj.setNamedParameter("employeeId", joinProcess.getEmployee().getId());
      empInfoObj.setNamedParameter("creationDate", info.getCreationDate());

      empInfoObj.setMaxResult(1);
      empInfoList = empInfoObj.list();
      if (empInfoList.size() > 0) {
        chkAnyEmployInfoExistsAfter = true;
      }

    } catch (Exception e) {
      LOG.error("Exception chkAnyEmployInfoExistsAfter in joinrequest : ", e, e);
    }
    return chkAnyEmployInfoExistsAfter;
  }

  public static boolean chkIsActiveRecord(EhcmJoiningWorkRequest joinProcess) {
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    boolean chkIsActiveRecord = false;
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id = :employeeId and ehcmJoinWorkrequest.id =:joinReqId and enabled = 'N' ");
      empInfoObj.setNamedParameter("employeeId", joinProcess.getEmployee().getId());
      empInfoObj.setNamedParameter("joinReqId", joinProcess.getId());
      empInfoObj.setMaxResult(1);
      empInfoList = empInfoObj.list();
      if (empInfoList.size() > 0) {
        chkIsActiveRecord = true;
      }

    } catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Exception chkIsActiveRecord in joinrequest : ", e, e);
      }
      OBDal.getInstance().rollbackAndClose();

    } finally {
      OBContext.restorePreviousMode();
    }
    return chkIsActiveRecord;
  }

  public static boolean chkEmplyInfoExistAfterJWR(EhcmJoiningWorkRequest joinProcess,
      EmploymentInfo info) {
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    boolean chkEmplyInfoExistAfterJWR = false;
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
          "  ehcmEmpPerinfo.id = :employeeId and (ehcmJoinWorkrequest.id <> :joinReqId"
              + " or ehcmJoinWorkrequest.id  is null)  and creationDate > :creationDate "
              + " order by creationDate desc ");
      empInfoObj.setNamedParameter("employeeId", joinProcess.getEmployee().getId());
      empInfoObj.setNamedParameter("joinReqId", joinProcess.getId());
      empInfoObj.setNamedParameter("creationDate", info.getCreationDate());

      empInfoObj.setMaxResult(1);
      empInfoList = empInfoObj.list();
      if (empInfoList.size() > 0) {
        chkEmplyInfoExistAfterJWR = true;
      }

    } catch (Exception e) {
      LOG.error("Exception chkEmplyInfoExistAfterJWR in joinrequest : ", e, e);
    }
    return chkEmplyInfoExistAfterJWR;
  }

  /**
   * 
   * @param objSuspension
   * @param oldSuspension
   * @param oldTermination
   * @param vars
   * @return
   * @throws Exception
   */
  // Used to reactivate the suspension record
  public static int cancelEmploymentRecord(EmployeeSuspension objSuspension,
      EmployeeSuspension oldSuspension, EHCMEMPTermination oldTermination, VariablesSecureApp vars)
      throws Exception {
    // TODO Auto-generated method stub
    EhcmSuspensionEmpV suspensionView = objSuspension.getEhcmEmpPerinfo();
    EhcmEmpPerInfo employee = suspensionView.getEmployee();
    int count = 0;
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
    Boolean chkPositionAvailableOrNot = false;
    Boolean chkDelegatePositionAvailableOrNot = false;
    try {
      // release employee from suspension
      if (objSuspension.getSuspensionType().equals("SUS")) {
        employee.setEmploymentStatus("AC");
      } else {
        employee.setEmploymentStatus("SD");
      }
      OBDal.getInstance().save(employee);
      if (objSuspension.getSuspensionType().equals("SUE")
          && objSuspension.getSuspensionType().equals("SUE") && oldTermination != null) {
        // delete newly created employee
        EHCMEMPTermination objExistTermination = oldTermination;
        objExistTermination.setEhcmEmploymentInfo(null);
        OBDal.getInstance().save(objExistTermination);
        OBDal.getInstance().flush();

        // remove linked employment
        EmploymentInfo oldEmployment = null;
        OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ehcmEmpPerinfo.id=:employeeId and enabled='Y'  and (alertStatus='ACT' or alertStatus='T' ) and ehcmEmpSuspension.id='"
                + objSuspension.getId() + "'order by creationDate desc ");
        empInfo.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
        empInfo.setMaxResult(1);

        if (empInfo.list().size() > 0) {
          oldEmployment = empInfo.list().get(0);
          OBDal.getInstance().remove(oldEmployment);
          OBDal.getInstance().flush();
        }
        // delete empstatus

        OBQuery<ehcmempstatus> employeestatus = OBDal.getInstance().createQuery(ehcmempstatus.class,
            " ehcmEmpPerinfo.id=:employeeId");
        employeestatus.setNamedParameter("employeeId", objSuspension.getEhcmEmpPerinfo().getId());
        if (employeestatus.list().size() > 0) {
          ehcmempstatus employe = employeestatus.list().get(0);
          OBDal.getInstance().remove(employe);
          OBDal.getInstance().flush();
        }

        // delete termination

        OBDal.getInstance().getConnection()
            .prepareStatement("delete from ehcm_emp_termination where ehcm_emp_termination_id='"
                + oldTermination.getId() + "'")
            .executeUpdate();

      } else {
        // remove old employment
        OBQuery<EmploymentInfo> empInfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " ehcmEmpPerinfo.id=:empId and enabled='Y'  and (alertStatus='ACT' or alertStatus='T' ) and ehcmEmpSuspension.id='"
                + objSuspension.getId() + "'order by creationDate desc ");
        empInfo.setNamedParameter("empId", objSuspension.getEhcmEmpPerinfo().getId());
        empInfo.setMaxResult(1);
        if (empInfo.list().size() > 0) {
          EmploymentInfo oldEmployment = empInfo.list().get(0);
          OBDal.getInstance().remove(oldEmployment);
          OBDal.getInstance().flush();
        }
      }
      // Activate previous record in employment(not belongs to this suspension)
      OBQuery<EmploymentInfo> preEmpInfoQry = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id=:empId order by creationDate desc ");
      preEmpInfoQry.setNamedParameter("empId", objSuspension.getEhcmEmpPerinfo().getId());
      preEmpInfoQry.setMaxResult(1);
      if (preEmpInfoQry.list().size() > 0) {
        EmploymentInfo preEmpinfo = preEmpInfoQry.list().get(0);
        preEmpinfo.setEnabled(true);
        preEmpinfo.setAlertStatus("ACT");
        preEmpinfo.setEndDate(null);
        if (preEmpinfo.getEhcmEmpSuspension() != null) {
          EmployeeSuspension preSuspension = preEmpinfo.getEhcmEmpSuspension();
          preSuspension.setEnabled(true);
          OBDal.getInstance().save(preSuspension);
        }
        OBDal.getInstance().save(preEmpinfo);

        // reassign the position to this employee(termination)
        if (objSuspension.getSuspensionType().equals("SUE")
            && objSuspension.getSuspensionType().equals("SUE") && oldTermination != null) {
          EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
              preEmpinfo.getEhcmEmpSuspension().getPosition().getId());
          EhcmPosition oldDelegatedPosition = null;

          chkPositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO.chkPositionAvailableOrNot(
              suspensionView.getEmployee(), pos, objSuspension.getEndDate(), null,
              objSuspension.getDecisionType(), false);

          chkDelegatePositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO
              .chkDelegatePositionAvailableOrNot(suspensionView.getEmployee(),
                  objSuspension.getOriginalDecisionNo(), null, objSuspension.getEndDate(), null,
                  objSuspension.getDecisionType(), true);

          OBQuery<EhcmPosition> oldPositionQry = OBDal.getInstance().createQuery(EhcmPosition.class,
              "as e where e.jOBNo='" + preEmpinfo.getSecjobno() + "' and e.client.id='"
                  + preEmpinfo.getClient().getId() + "' order by creationDate desc");
          oldPositionQry.setMaxResult(1);
          if (oldPositionQry.list().size() > 0) {
            oldDelegatedPosition = oldPositionQry.list().get(0);
          }
          if (chkPositionAvailableOrNot) {
            // if (pos.getAssignedEmployee() != null) {
            count = 1;

          } /*
             * else if (oldDelegatedPosition != null && oldDelegatedPosition.getDelegatedEmployee()
             * != null) {
             */
          else if (chkDelegatePositionAvailableOrNot) {
            count = 2;

          } else {
            if (pos != null) {
              /*
               * Task no 6797 pos.setAssignedEmployee(OBDal.getInstance().get(EmployeeView.class,
               * objSuspension.getEhcmEmpPerinfo().getId())); OBDal.getInstance().save(pos);
               */
              // update position employee history records with end date
              assingedOrReleaseEmpInPositionDAO.updateEndDateInPositionEmployeeHisotry(
                  suspensionView.getEmployee(), objSuspension.getPosition(), null, null, null, null,
                  null, objSuspension, null, null);
            }

            // update position employee history records with end date for delegated emp
            assingedOrReleaseEmpInPositionDAO.updateEndDateForDelegatedEmployee(
                suspensionView.getEmployee(), objSuspension.getEndDate(), objSuspension, null,
                objSuspension.getDecisionType());

            /*
             * Task No. 6797 if (oldDelegatedPosition != null) {
             * oldDelegatedPosition.setDelegatedEmployee(OBDal.getInstance().get(EmployeeView.class,
             * objSuspension.getEhcmEmpPerinfo().getId()));
             * OBDal.getInstance().save(oldDelegatedPosition); }
             */

            OBDal.getInstance().flush();
          }
        }

      }
      // // Cancel Suspension Disabled
      // objSuspension.setEnabled(false);
      // OBDal.getInstance().save(objSuspension);
      // make old suspension as active
      oldSuspension.setEnabled(true);
      OBDal.getInstance().save(oldSuspension);
      OBDal.getInstance().flush();

    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("Exception in cancelEmploymentRecord in joinreqDAO: ", e);
    }
    return count;
  }

  public static void deleteEmployeInfo(EhcmJoiningWorkRequest joinProcess) {
    List<EmploymentInfo> empInfoList = new ArrayList<EmploymentInfo>();
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> empInfoObj = OBDal.getInstance().createQuery(EmploymentInfo.class,
          " ehcmEmpPerinfo.id = :employeeId and ehcmJoinWorkrequest.id =:joinReqId  ");
      empInfoObj.setNamedParameter("employeeId", joinProcess.getEmployee().getId());
      empInfoObj.setNamedParameter("joinReqId", joinProcess.getId());
      empInfoList = empInfoObj.list();
      if (empInfoList.size() > 0) {
        for (EmploymentInfo info : empInfoList) {
          OBDal.getInstance().remove(info);
        }
      }

    } catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Exception deleteEmployeInfo in joinrequest : ", e, e);
      }
      OBDal.getInstance().rollbackAndClose();

    } finally {
    }
  }
}
