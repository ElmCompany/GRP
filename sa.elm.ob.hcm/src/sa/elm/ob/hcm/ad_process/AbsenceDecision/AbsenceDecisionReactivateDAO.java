package sa.elm.ob.hcm.ad_process.AbsenceDecision;

import java.sql.Connection;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;

/**
 * Interface for all Employee Absence reactivate related DB Operations
 * 
 * @author divya 13-06-2018
 *
 */
public interface AbsenceDecisionReactivateDAO {

  /**
   * update absence decision as under processing while reactivate
   * 
   * @param absence
   * @throws Exception
   */
  void updateAbsenceDecisionStatus(EHCMAbsenceAttendance absence) throws Exception;

  /**
   * delete the emp leave record and emp leave block record
   * 
   * @param absence
   * @throws Exception
   */
  void deleteEmpLeaveRecordInCreateCase(EHCMAbsenceAttendance absence) throws Exception;

  /**
   * update emp leave record other than create case based on original decision no
   * 
   * @param connection
   * @param absence
   * @param absencetype
   * @param accrual
   * @throws Exception
   */
  void updateEmpLeaveRecrdInOtherThanCreateCase(Connection connection,
      EHCMAbsenceAttendance absence, EHCMAbsenceType absencetype, EHCMAbsenceTypeAccruals accrual)
      throws Exception;

  /**
   * check absence type act as extend absence type and the corresponding leave is taken after this
   * leave ex: for maternity leave - delivery leave acted as extend absence type so before
   * reactivate the delivery leave need to chk any maternity leave taken for that employee.if
   * present then dont allow to reactivate the delivery leave
   * 
   * @param absenceattend
   * @return true or false
   * @throws Exception
   */
  boolean checkExtendAbsenceTypeLeaveIsTakenBeforeReactivate(EHCMAbsenceAttendance absenceattend)
      throws Exception;

  /**
   * check sufficient leave balance is exists or not and absence rules satisfied or not
   * 
   * @param absence
   * @return
   * @throws Exception
   */
  String chkleaveapprove(EHCMAbsenceAttendance absence, EHCMAbsenceAttendance currentabsence)
      throws Exception;

}
