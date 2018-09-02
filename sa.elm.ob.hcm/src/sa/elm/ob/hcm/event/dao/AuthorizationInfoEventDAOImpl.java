package sa.elm.ob.hcm.event.dao;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMAuthorizationInfo;
import sa.elm.ob.hcm.EHCMEMPTermination;
import sa.elm.ob.hcm.EhcmExtendService;
import sa.elm.ob.hcm.EhcmJoiningWorkRequest;
import sa.elm.ob.hcm.EmployeeSuspension;
import sa.elm.ob.utility.util.Utility;

public class AuthorizationInfoEventDAOImpl implements AuthorizationInfoEventDAO {
  private static final Logger log = LoggerFactory.getLogger(AuthorizationInfoEventDAOImpl.class);

  public boolean dateOverLapForEndDate(EHCMAuthorizationInfo authorisedinfo) {
    List<EHCMAuthorizationInfo> dateList = new ArrayList<EHCMAuthorizationInfo>();
    OBQuery<EHCMAuthorizationInfo> authinfo = null;
    DateFormat dateFormat = Utility.dateFormat;
    Date endate = (Date) authorisedinfo.getEndDate();
    int flag = 1;
    try {
      if (authorisedinfo.getStartDate().compareTo(authorisedinfo.getEndDate()) < 0) {
        String whereClause = "e where e.organization.id = :organisation  "
            + "and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate,'dd-MM-yyyy') "
            + "and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy')) "
            + "or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:startDate,'dd-MM-yyyy') "
            + "and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:endDate,'dd-MM-yyyy'))) and e.id<>:authoinfoid  order by e.startDate desc ";
        authinfo = OBDal.getInstance().createQuery(EHCMAuthorizationInfo.class, whereClause);
        authinfo.setNamedParameter("organisation", authorisedinfo.getOrganization().getId());
        authinfo.setNamedParameter("startDate", dateFormat.format(authorisedinfo.getStartDate()));
        authinfo.setNamedParameter("authoinfoid", authorisedinfo.getId());

        authinfo.setNamedParameter("endDate", (authorisedinfo.getEndDate() == null ? "21-06-2058"
            : dateFormat.format(authorisedinfo.getEndDate())));

        dateList = authinfo.list();
        if (dateList.size() > 0) {
          return true;
        }
      } else {
        flag = 0;
        throw new OBException(OBMessageUtils.messageBD("ehcm_daterror"));

      }
      // return true;
    } catch (Exception e) {
      if (flag == 0) {
        throw new OBException(OBMessageUtils.messageBD("ehcm_daterror"));

      } else {
        log.error("error while checkJobandJobTitleCombExist", e);
        return false;
      }
    }
    return false;
  }

  @Override
  public boolean checkAuthorizationDetailUsedInRecords(EHCMAuthorizationInfo authorisedinfo)
      throws Exception {
    // TODO Auto-generated method stub
    String hql = "";
    String hql1 = "";
    String hql2 = "";
    try {
      hql = " and e.authorisedPerson=:authorizationPerson and e.authorisedPersonJobTitle=:authorizationJob and e.departmentCode.id=:deptId ";

      hql1 = " and e.authorizedPerson=:authorizationPerson and e.authorizedPersonJobTitle=:authorizationJob and e.departmentCode.id=:deptId ";

      hql2 = " and e.ehcmAuthorizePerson=:authorizationPerson and e.authorizePersonTitle=:authorizationJob and e.departmentCode.id=:deptId ";

      // join work Request
      OBQuery<EhcmJoiningWorkRequest> joinWorkReqQry = OBDal.getInstance()
          .createQuery(EhcmJoiningWorkRequest.class, " as e  where e.client.id=:clientId  " + hql);
      joinWorkReqQry.setNamedParameter("clientId", authorisedinfo.getClient().getId());
      joinWorkReqQry.setNamedParameter("authorizationPerson", authorisedinfo.getAuthorizedPerson());
      joinWorkReqQry.setNamedParameter("authorizationJob", authorisedinfo.getAuthorizedJobtitle());
      joinWorkReqQry.setNamedParameter("deptId", authorisedinfo.getOrganization().getId());
      if (joinWorkReqQry.list().size() > 0) {
        return true;
      }

      // absence Decision
      OBQuery<EHCMAbsenceAttendance> absenceDecQry = OBDal.getInstance()
          .createQuery(EHCMAbsenceAttendance.class, " as e  where e.client.id=:clientId  " + hql1);
      absenceDecQry.setNamedParameter("clientId", authorisedinfo.getClient().getId());
      absenceDecQry.setNamedParameter("authorizationPerson", authorisedinfo.getAuthorizedPerson());
      absenceDecQry.setNamedParameter("authorizationJob", authorisedinfo.getAuthorizedJobtitle());
      absenceDecQry.setNamedParameter("deptId", authorisedinfo.getOrganization().getId());
      if (absenceDecQry.list().size() > 0) {
        return true;
      }

      // End of Employment
      OBQuery<EHCMEMPTermination> empTerminationQry = OBDal.getInstance()
          .createQuery(EHCMEMPTermination.class, " as e  where e.client.id=:clientId  " + hql2);
      empTerminationQry.setNamedParameter("clientId", authorisedinfo.getClient().getId());
      empTerminationQry.setNamedParameter("authorizationPerson",
          authorisedinfo.getAuthorizedPerson());
      empTerminationQry.setNamedParameter("authorizationJob",
          authorisedinfo.getAuthorizedJobtitle());
      empTerminationQry.setNamedParameter("deptId", authorisedinfo.getOrganization().getId());
      if (empTerminationQry.list().size() > 0) {
        return true;
      }

      // Extend of Service
      OBQuery<EhcmExtendService> extendSerQry = OBDal.getInstance()
          .createQuery(EhcmExtendService.class, " as e  where e.client.id=:clientId  " + hql);
      extendSerQry.setNamedParameter("clientId", authorisedinfo.getClient().getId());
      extendSerQry.setNamedParameter("authorizationPerson", authorisedinfo.getAuthorizedPerson());
      extendSerQry.setNamedParameter("authorizationJob", authorisedinfo.getAuthorizedJobtitle());
      extendSerQry.setNamedParameter("deptId", authorisedinfo.getOrganization().getId());
      if (extendSerQry.list().size() > 0) {
        return true;
      }

      // employee Suspension
      OBQuery<EmployeeSuspension> suspensionQry = OBDal.getInstance()
          .createQuery(EmployeeSuspension.class, " as e  where e.client.id=:clientId  " + hql);
      suspensionQry.setNamedParameter("clientId", authorisedinfo.getClient().getId());
      suspensionQry.setNamedParameter("authorizationPerson", authorisedinfo.getAuthorizedPerson());
      suspensionQry.setNamedParameter("authorizationJob", authorisedinfo.getAuthorizedJobtitle());
      suspensionQry.setNamedParameter("deptId", authorisedinfo.getOrganization().getId());
      if (suspensionQry.list().size() > 0) {
        return true;
      }
    }

    catch (Exception e) {
      log.error("error while checkAuthorizationDetailUsedInRecords", e);
      return true;
    }
    return false;
  }

}
