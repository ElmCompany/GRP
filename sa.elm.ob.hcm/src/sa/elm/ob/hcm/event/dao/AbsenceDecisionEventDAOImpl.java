package sa.elm.ob.hcm.event.dao;

import java.math.BigInteger;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMEmpLeaveln;
import sa.elm.ob.utility.util.Utility;

/**
 * Implementation for all absence decision Event related DB Operations
 * 
 * @author divya on 31/05/2018
 * 
 */
// absence decision Event DAO Implement file
public class AbsenceDecisionEventDAOImpl implements AbsenceDecisionEventDAO {
  private static Logger log4j = Logger.getLogger(AbsenceDecisionEventDAOImpl.class);
  DateFormat yearFormat = Utility.YearFormat;
  DateFormat dateFormat = Utility.dateFormat;

  public BigInteger checkAbsenceExistsInSamePeriod(EHCMAbsenceAttendance absence) {
    List<EHCMEmpLeaveln> empLeaveLnQryList = new ArrayList<EHCMEmpLeaveln>();
    OBQuery<EHCMEmpLeaveln> empLevLnQry = null;
    String hql = "";
    BigInteger count = BigInteger.ZERO;
    try {

      if (absence.getEndDate() != null
          || (absence.getDecisionType().equals("EX") && absence.getExtendEnddate() != null)) {
        if (absence.getDecisionType().equals("UP")) {
          hql = " and e.ehcmAbsenceAttendance.id <> :absenceattendanceId  ";
        }
        log4j.debug("dec::" + absence.getDecisionType());
        empLevLnQry = OBDal.getInstance().createQuery(EHCMEmpLeaveln.class,
            " as e where e.ehcmEmpLeave.ehcmEmpPerinfo.id=:employeeId "
                + " and e.enabled='Y'  and ((TO_DATE(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= TO_DATE(:fromdate,'yyyy-MM-dd')"
                + " and TO_DATE(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= TO_DATE(:todate,'yyyy-MM-dd') ) "
                + "  or (TO_DATE(to_char(e.endDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= TO_DATE(:fromdate,'yyyy-MM-dd')"
                + "   and TO_DATE(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy')     <= TO_DATE(:todate,'yyyy-MM-dd')))"
                + " and e.leaveType <>'AC' and e.ehcmAbsenceAttendance.id is not null " + hql);
        empLevLnQry.setNamedParameter("employeeId", absence.getEhcmEmpPerinfo().getId());
        if (absence.getDecisionType().equals("EX")) {
          empLevLnQry.setNamedParameter("fromdate", absence.getExtendStartdate());
          empLevLnQry.setNamedParameter("todate", absence.getExtendEnddate());
        } else {
          empLevLnQry.setNamedParameter("fromdate", absence.getStartDate());
          empLevLnQry.setNamedParameter("todate", absence.getEndDate());
        }

        if (absence.getDecisionType().equals("UP")) {
          empLevLnQry.setNamedParameter("absenceattendanceId",
              absence.getOriginalDecisionNo().getId());
        }
        empLeaveLnQryList = empLevLnQry.list();
        if (empLeaveLnQryList.size() > 0) {
          count = BigInteger.valueOf(empLeaveLnQryList.size());
        }
      }
    } catch (Exception e) {
      log4j.error("error while checkAbsenceExistsInSamePeriod", e);
      return count;
    }
    return count;
  }
}
