package sa.elm.ob.hcm.selfservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import sa.elm.ob.hcm.dto.employment.CertificationsDTO;
import sa.elm.ob.hcm.dto.employment.CustodiesDTO;
import sa.elm.ob.hcm.dto.employment.QualificationsDTO;
import sa.elm.ob.hcm.dto.employment.ViewEmplInfoDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.exceptions.SystemException;
import sa.elm.ob.hcm.services.employment.EmploymentInformationService;

@RestController
@RequestMapping("/openerp/hr/employment")
public class EmploymentInformationController {

  @Autowired
  EmploymentInformationService employmentInformationService;

  @RequestMapping(value = "/informations/{username}", method = RequestMethod.GET)
  public ResponseEntity<List<ViewEmplInfoDTO>> getAllEmpInfo(
      @PathVariable("username") String username) throws SystemException, BusinessException {

    List<ViewEmplInfoDTO> empInfo = employmentInformationService
        .getCurrentEmploymentInfoByUserName(username);

    return new ResponseEntity<List<ViewEmplInfoDTO>>(empInfo, HttpStatus.OK);

  }

  @RequestMapping(value = "/qualifications/{username}", method = RequestMethod.GET)
  public ResponseEntity<List<QualificationsDTO>> getAllQualifications(
      @PathVariable("username") String username) {

    List<QualificationsDTO> qualifications = employmentInformationService
        .getQualifications(username);

    return new ResponseEntity<List<QualificationsDTO>>(qualifications, HttpStatus.OK);
  }

  @RequestMapping(value = "/qualification/{recordId}", method = RequestMethod.GET)
  public ResponseEntity<QualificationsDTO> getQualificatioById(
      @PathVariable("recordId") String recordId) {

    QualificationsDTO qualification = employmentInformationService.getQualificationById(recordId);

    return new ResponseEntity<QualificationsDTO>(qualification, HttpStatus.OK);
  }

  @RequestMapping(value = "/qualifications/submit/{username}", method = RequestMethod.POST)
  public ResponseEntity<Boolean> submitQualification(@PathVariable("username") String username,
      @RequestBody QualificationsDTO qualificationsDTO) throws SystemException, BusinessException {

    Boolean qualification = employmentInformationService.submitQualificationWithApproval(username,
        qualificationsDTO);

    return new ResponseEntity<Boolean>(qualification, HttpStatus.OK);
  }

  @RequestMapping(value = "/certifications/{username}", method = RequestMethod.GET)
  public ResponseEntity<List<CertificationsDTO>> getAllCertifications(
      @PathVariable("username") String username) {

    List<CertificationsDTO> certifications = employmentInformationService
        .getCertifications(username);

    return new ResponseEntity<List<CertificationsDTO>>(certifications, HttpStatus.OK);
  }

  @RequestMapping(value = "/certification/{recordId}", method = RequestMethod.GET)
  public ResponseEntity<CertificationsDTO> getCertification(
      @PathVariable("recordId") String recordId) {

    CertificationsDTO certifications = employmentInformationService.getCertificationsById(recordId);

    return new ResponseEntity<CertificationsDTO>(certifications, HttpStatus.OK);
  }

  @RequestMapping(value = "/certifications/submit/{username}", method = RequestMethod.POST)
  public ResponseEntity<Boolean> submitCertification(@PathVariable("username") String username,
      @RequestBody CertificationsDTO certificationsDTO) throws SystemException, BusinessException {

    Boolean certificate = employmentInformationService.submitCertificateWithApproval(username,
        certificationsDTO);

    return new ResponseEntity<Boolean>(certificate, HttpStatus.OK);
  }

  @RequestMapping(value = "/custodies/{username}", method = RequestMethod.GET)
  public ResponseEntity<List<CustodiesDTO>> getAllCustodies(
      @PathVariable("username") String username) {

    List<CustodiesDTO> custodies = employmentInformationService.getAllCustodies(username);

    return new ResponseEntity<List<CustodiesDTO>>(custodies, HttpStatus.OK);
  }
}
