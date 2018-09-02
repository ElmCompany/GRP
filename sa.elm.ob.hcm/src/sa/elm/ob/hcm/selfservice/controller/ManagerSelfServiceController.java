package sa.elm.ob.hcm.selfservice.controller;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import sa.elm.ob.hcm.dto.employment.ViewEmplInfoDTO;
import sa.elm.ob.hcm.dto.leave.ViewLeaveAccrualDTO;
import sa.elm.ob.hcm.dto.manager.EmpBusinessMissionManagerDTO;
import sa.elm.ob.hcm.dto.manager.EmpInfoManagerDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.services.manager.ManagerSelfServiceService;

/**
 * Web Controller for Manager Self Service
 * 
 *
 */
@RestController
@RequestMapping("openerp/hr/manager")
public class ManagerSelfServiceController {

  @Autowired
  private ManagerSelfServiceService managerSelfServiceService;

  @RequestMapping(value = "/employees/{username}", method = RequestMethod.GET)
  public ResponseEntity<List<EmpInfoManagerDTO>> getAllEmployeesByManager(
      @PathVariable("username") String username) throws BusinessException, SystemException {

    List<EmpInfoManagerDTO> employeesList = managerSelfServiceService
        .getAllEmployeesByManager(username);

    return new ResponseEntity<List<EmpInfoManagerDTO>>(employeesList, HttpStatus.OK);
  }

  @RequestMapping(value = "/employee/{empNo}", method = RequestMethod.GET)
  public ResponseEntity<ViewEmplInfoDTO> getEmployeeInformationByNumber(
      @PathVariable("empNo") String empNo) {

    ViewEmplInfoDTO employeeInfo = managerSelfServiceService.getEmployeeInformationByNumber(empNo);

    return new ResponseEntity<ViewEmplInfoDTO>(employeeInfo, HttpStatus.OK);
  }

  @RequestMapping(value = "/{username}/leavesAccural", method = RequestMethod.GET)
  public ResponseEntity<List<ViewLeaveAccrualDTO>> getAllEmployeesLeaveAccuralByManagerUsername(
      @PathVariable("username") String mngUsername) throws BusinessException, SystemException {

    List<ViewLeaveAccrualDTO> leavesAccuralList = managerSelfServiceService
        .getAllEmployeesLeaveAccuralByManagerUsername(mngUsername);

    return new ResponseEntity<List<ViewLeaveAccrualDTO>>(leavesAccuralList, HttpStatus.OK);
  }

  @RequestMapping(value = "/{username}/leavesAccural/date/{asOfDate}/empNo/{empNo}", method = RequestMethod.GET)
  public ResponseEntity<List<ViewLeaveAccrualDTO>> getEmployeeLeavesAccuralByDateAndEmpNo(
      @PathVariable("username") String mngUsername, @PathVariable("asOfDate") String asOfDate,
      @PathVariable("empNo") String empNo) throws BusinessException, SystemException {

    List<ViewLeaveAccrualDTO> leavesAccuralList = managerSelfServiceService
        .getEmployeeLeavesAccuralByDateAndEmpNo(mngUsername, asOfDate, empNo);

    return new ResponseEntity<List<ViewLeaveAccrualDTO>>(leavesAccuralList, HttpStatus.OK);
  }

  @RequestMapping(value = "/{username}/businessMissions/date/{asOfDate}", method = RequestMethod.GET)
  public ResponseEntity<List<EmpBusinessMissionManagerDTO>> getAllEmployeesBusinessMissionByManagerAndDate(
      @PathVariable("username") String mngUsername, @PathVariable("asOfDate") String asOfDate)
      throws BusinessException, SystemException, ParseException {

    List<EmpBusinessMissionManagerDTO> businessMissionsList = managerSelfServiceService
        .getEmployeeBusinessMissionsByManagerAndDate(mngUsername, asOfDate);

    return new ResponseEntity<List<EmpBusinessMissionManagerDTO>>(businessMissionsList,
        HttpStatus.OK);
  }

  @RequestMapping(value = "/{username}/businessMissions/date/{asOfDate}/empNo/{empNo}", method = RequestMethod.GET)
  public ResponseEntity<List<EmpBusinessMissionManagerDTO>> getEmployeeBusinessMissionsByDateAndEmpNo(
      @PathVariable("username") String mngUsername, @PathVariable("asOfDate") String asOfDate,
      @PathVariable("empNo") String empNo)
      throws BusinessException, SystemException, ParseException {

    List<EmpBusinessMissionManagerDTO> businessMissionsList = managerSelfServiceService
        .getEmployeeBusinessMissionsByDateAndEmpNo(mngUsername, asOfDate, empNo);

    return new ResponseEntity<List<EmpBusinessMissionManagerDTO>>(businessMissionsList,
        HttpStatus.OK);
  }

}
