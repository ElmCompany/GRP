package sa.elm.ob.hcm.event.dao;

import java.math.BigInteger;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;

/**
 * Interface for all absence decision Event related DB Operations
 * 
 * @author Divya on 31/05/2018
 * 
 */
public interface AbsenceDecisionEventDAO {

  BigInteger checkAbsenceExistsInSamePeriod(EHCMAbsenceAttendance absence) throws Exception;
}
