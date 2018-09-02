/*
 * All Rights Reserved By Qualian Technologies Pvt Ltd.
 */
package sa.elm.ob.hcm.event.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.springframework.web.util.Log4jConfigListener;

import sa.elm.ob.hcm.EHCMEmpHierarchy;
import sa.elm.ob.hcm.EHCMEmpManagerHist;
import sa.elm.ob.hcm.EHCMEmpSupervisor;
import sa.elm.ob.hcm.EHCMEmpSupervisorNode;
import sa.elm.ob.hcm.EhcmEmploymentGroup;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.utility.util.Utility;

/**
 * 
 * @author divya -05-02-2018
 *
 */
public class SupervisorNodeEventDAO {
	private ConnectionProvider conn = null;

	private static final Logger LOG = Logger.getLogger(SupervisorNodeEventDAO.class);

	public SupervisorNodeEventDAO(ConnectionProvider con) {
		this.conn = con;
	}

	/**
	 * check manager already exists for employee with same peroid
	 * 
	 * @param superVisorNodeObj
	 * @return true or false- if manager associated for employee with same
	 *         period then true else false
	 */
	public static Boolean checkManagerAlrdyExistorNot(EHCMEmpSupervisorNode superVisorNodeObj) {

		Boolean managerExists = false;
		String todate = null;
		String fromdate = null;
		List<EHCMEmpManagerHist> empManagerHistList = new ArrayList<EHCMEmpManagerHist>();
		try {

			fromdate = Utility.formatDate(superVisorNodeObj.getEhcmEmpSupervisor().getStartDate());
			if(superVisorNodeObj.getEhcmEmpSupervisor().getEndDate() == null) {
				todate = "21-06-2058";
			}
			else {
				todate = Utility.formatDate(superVisorNodeObj.getEhcmEmpSupervisor().getEndDate());
			}
			OBQuery<EHCMEmpManagerHist> empManagerHistQry = OBDal.getInstance()
					.createQuery(EHCMEmpManagerHist.class, " as e where e.employee.id=:employeeId " + " and ((to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) "
							+ " and to_date(to_char(coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy')) "
							+ " or (to_date(to_char( coalesce (e.endDate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date(:fromdate) " + " and to_date(to_char(e.startDate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date(:todate,'dd-MM-yyyy'))) and e.manager.id= :managerId    ");

			empManagerHistQry.setNamedParameter("employeeId", superVisorNodeObj.getEhcmEmpPerinfo().getId());
			empManagerHistQry.setNamedParameter("fromdate", fromdate);
			empManagerHistQry.setNamedParameter("todate", todate);
			empManagerHistQry.setNamedParameter("managerId", superVisorNodeObj.getEhcmEmpSupervisor().getEmployee().getId());
			LOG.debug("empManagerHistQry:" + empManagerHistQry.getWhereAndOrderBy());
			empManagerHistList = empManagerHistQry.list();
			if(empManagerHistList.size() > 0) {
				managerExists = true;
			}
			else {
				managerExists = false;
			}

			return managerExists;
		}
		catch (Exception e) {
			LOG.error("Exception in checkManagerAlrdyExistorNot: ", e);
			OBDal.getInstance().rollbackAndClose();
			return managerExists;
		}
		finally {
		}
	}

	/**
	 * compare Employee Starting Manager History Date with Current Supervisor
	 * Record Start Date
	 * 
	 * @param superVisorNodeObj
	 * @return
	 */
	public static Boolean compOldestEmpMangHistDatewithCurrentStartDate(EHCMEmpSupervisorNode superVisorNodeObj) {

		Boolean compOldestEmpMangHistDatewithCurrentStartDate = false;
		List<EHCMEmpManagerHist> empManagerHistList = new ArrayList<EHCMEmpManagerHist>();
		try {

			OBQuery<EHCMEmpManagerHist> empManagerHistQry = OBDal.getInstance().createQuery(EHCMEmpManagerHist.class, " as e where e.employee.id=:employeeId  order by e.creationDate asc    ");
			empManagerHistQry.setNamedParameter("employeeId", superVisorNodeObj.getEhcmEmpPerinfo().getId());
			LOG.debug("empManagerHistQry:" + empManagerHistQry.getWhereAndOrderBy());
			empManagerHistQry.setMaxResult(1);
			empManagerHistList = empManagerHistQry.list();
			if(empManagerHistList.size() > 0) {
				EHCMEmpManagerHist firstEmpManagerHist = empManagerHistList.get(0);
				if(superVisorNodeObj.getEhcmEmpSupervisor().getStartDate().compareTo(firstEmpManagerHist.getStartDate()) < 0) {
					compOldestEmpMangHistDatewithCurrentStartDate = true;
				}
			}
			else {
				compOldestEmpMangHistDatewithCurrentStartDate = false;
			}

			return compOldestEmpMangHistDatewithCurrentStartDate;
		}
		catch (Exception e) {
			LOG.error("Exception in chkEmpMangHistStDatLssThnCurDt: ", e);
			OBDal.getInstance().rollbackAndClose();
			return compOldestEmpMangHistDatewithCurrentStartDate;
		}
		finally {
		}
	}

	/**
	 * while insert the supervisor node update the all employment info
	 * supervisorId for that employee
	 * 
	 * @param supervisorNode
	 * @param oldEmployeeId
	 */
	public static void updateEmpInfoSupvisorDetail(EHCMEmpSupervisorNode supervisorNode, String oldEmployeeId) {
		List<EmploymentInfo> employInfoList = new ArrayList<EmploymentInfo>();
		OBQuery<EmploymentInfo> emplyInfoQry = null;
		try {
			OBContext.setAdminMode();
			emplyInfoQry = OBDal.getInstance().createQuery(EmploymentInfo.class, " as e where e.ehcmEmpPerinfo.id=:employeeId ");
			if(supervisorNode.getEhcmEmpPerinfo() != null) {
				emplyInfoQry = OBDal.getInstance().createQuery(EmploymentInfo.class, " as e where e.ehcmEmpPerinfo.id=:employeeId ");
				emplyInfoQry.setNamedParameter("employeeId", supervisorNode.getEhcmEmpPerinfo().getId());
				employInfoList = emplyInfoQry.list();
				if(employInfoList.size() > 0) {
					for (EmploymentInfo employInfo : employInfoList) {
						employInfo.setEhcmEmpSupervisor(supervisorNode.getEhcmEmpSupervisor());
						OBDal.getInstance().save(employInfo);
					}
				}
			}
			if(oldEmployeeId != null) {
				updateCaseEmpInfoSupvisorDetail(supervisorNode, oldEmployeeId, emplyInfoQry);
			}
		}
		catch (Exception e) {
			LOG.error("Exception in updateEmpInfoSupvisorDetail: ", e);
			OBDal.getInstance().rollbackAndClose();
		}
		finally {
		}
	}

	/**
	 * while delete the supervisor node update the employment info supervisorId
	 * set as null for that employee
	 * 
	 * @param supervisorNode
	 * @param oldEmployeeId
	 * @param emplyInfoQry
	 */
	public static void updateCaseEmpInfoSupvisorDetail(EHCMEmpSupervisorNode supervisorNode, String oldEmployeeId, OBQuery<EmploymentInfo> emplyInfoQry) {
		List<EmploymentInfo> employInfoList = new ArrayList<EmploymentInfo>();
		try {
			OBContext.setAdminMode();
			if(emplyInfoQry == null) {
				emplyInfoQry = OBDal.getInstance().createQuery(EmploymentInfo.class, " as e where e.ehcmEmpPerinfo.id=:employeeId ");
			}
			emplyInfoQry.setNamedParameter("employeeId", oldEmployeeId);
			employInfoList = emplyInfoQry.list();
			LOG.debug("size:" + employInfoList.size());
			if(employInfoList.size() > 0) {

				for (EmploymentInfo employInfo : employInfoList) {
					if(employInfo.getEhcmEmpSupervisor() != null && employInfo.getEhcmEmpSupervisor().equals(supervisorNode.getEhcmEmpSupervisor())) {
						employInfo.setEhcmEmpSupervisor(null);
						OBDal.getInstance().save(employInfo);
					}
				}
			}
		}
		catch (Exception e) {
			LOG.error("Exception in updateEmpInfoSupvisorDetail: ", e);
			OBDal.getInstance().rollbackAndClose();
		}
		finally {
		}
	}

	/**
	 * check manager already associated for that employee or not
	 * 
	 * @param employeeId
	 * @param empSuperVisor
	 * @param empCurrentSupervisorNode
	 * @return
	 */
	public static boolean checkMangAssociatedorNo(String employeeId, EHCMEmpSupervisor empSuperVisor, EHCMEmpSupervisorNode empCurrentSupervisorNode) {
		List<EHCMEmpSupervisorNode> supervisorNodeList = new ArrayList<EHCMEmpSupervisorNode>();
		try {
			OBContext.setAdminMode();
			OBQuery<EHCMEmpSupervisorNode> supervisorNodeQry = OBDal.getInstance().createQuery(EHCMEmpSupervisorNode.class, " as e where e.ehcmEmpPerinfo.id=:employeeId "
					+ " and e.ehcmEmpSupervisor.id in ( select e.id from EHCM_Emp_Supervisor e where e.ehcmEmpHierarchy.id=:hierarchyId ) ");
			supervisorNodeQry.setNamedParameter("employeeId", employeeId);
			supervisorNodeQry.setNamedParameter("hierarchyId", empSuperVisor.getEhcmEmpHierarchy().getId());
			supervisorNodeList = supervisorNodeQry.list();
			if(supervisorNodeList.size() > 0) {
				EHCMEmpSupervisorNode superVisorNode = supervisorNodeList.get(0);
				if(!superVisorNode.getEhcmEmpSupervisor().equals(empSuperVisor) || !superVisorNode.equals(empCurrentSupervisorNode)) {
					return true;
				}
				else
					return false;
			}
			else
				return false;
		}
		catch (Exception e) {
			LOG.error("Exception in updateEmpInfoSupvisorDetail: ", e);
			OBDal.getInstance().rollbackAndClose();
		}
		finally {
		}
		return false;
	}

	public static boolean checkMangerAlreadyAdded(String employeeId, EHCMEmpSupervisor empSuperVisor) {
		List<EHCMEmpSupervisor> supervisorList = new ArrayList<EHCMEmpSupervisor>();
		try {
			OBContext.setAdminMode();
			OBQuery<EHCMEmpSupervisor> supervisorQry = OBDal.getInstance().createQuery(EHCMEmpSupervisor.class, " as e where e.employee.id=:employeeId "
					+ " and e.ehcmEmpHierarchy.id=:hierarchyId ");
			supervisorQry.setNamedParameter("employeeId", employeeId);
			supervisorQry.setNamedParameter("hierarchyId", empSuperVisor.getEhcmEmpHierarchy().getId());
			supervisorList = supervisorQry.list();
			if(supervisorList.size() > 0) {
				return true;
			}
			else
				return false;
		}
		catch (Exception e) {
			LOG.error("Exception in checkMangerAlreadyAdded: ", e);
			OBDal.getInstance().rollbackAndClose();
		}
		finally {
		}
		return false;
	}

	public static boolean checkempHierarchyUnique(EHCMEmpHierarchy empHierarchy) {
		List<EHCMEmpHierarchy> empHierarchyList = new ArrayList<EHCMEmpHierarchy>();
		try {
			// check name is unique
			OBQuery<EHCMEmpHierarchy> empHierarchyQry = OBDal.getInstance().createQuery(EHCMEmpHierarchy.class, "  name=:name and client.id =:clientId");
			empHierarchyQry.setNamedParameter("name", empHierarchy.getName());
			empHierarchyQry.setNamedParameter("clientId", empHierarchy.getClient().getId());
			empHierarchyList = empHierarchyQry.list();
			if(empHierarchyList.size() > 0) {
				return true;
			}
			else
				return false;
		}
		catch (Exception e) {
			LOG.error("Exception in checkempHierarchyUnique: ", e);
			OBDal.getInstance().rollbackAndClose();
		}
		finally {
		}
		return false;
	}
}