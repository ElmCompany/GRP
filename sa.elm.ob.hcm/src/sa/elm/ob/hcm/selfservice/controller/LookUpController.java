package sa.elm.ob.hcm.selfservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import sa.elm.ob.hcm.selfservice.dto.lookup.LookUpDTO;
import sa.elm.ob.hcm.selfservice.service.lookup.LookUpService;
import sa.elm.ob.hcm.util.LookUpCodesConstants;

/**
 * Rest Controller for all the Look Up's
 * 
 *
 */
@RestController
@RequestMapping("openerp/hr/lookup")
public class LookUpController {

  @Autowired
  private LookUpService lookUpService;

  @RequestMapping(value = "/country", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getCountries() {

    List<LookUpDTO> countries = lookUpService.getCountries();

    return new ResponseEntity<List<LookUpDTO>>(countries, HttpStatus.OK);
  }

  @RequestMapping(value = "/region/{id}/cities", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getCitiesByRegion(@PathVariable("id") String regionId) {

    List<LookUpDTO> cities = lookUpService.getCitiesByRegion(regionId);

    return new ResponseEntity<List<LookUpDTO>>(cities, HttpStatus.OK);
  }

  @RequestMapping(value = "/country/{id}/cities", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getCitiesByCountry(@PathVariable("id") String countryId) {

    List<LookUpDTO> cities = lookUpService.getCitiesByCountry(countryId);

    return new ResponseEntity<List<LookUpDTO>>(cities, HttpStatus.OK);
  }

  @RequestMapping(value = "/country/{id}/regions", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getRegions(@PathVariable("id") String countryId) {

    List<LookUpDTO> regions = lookUpService.getRegionsByCountry(countryId);

    return new ResponseEntity<List<LookUpDTO>>(regions, HttpStatus.OK);
  }

  @RequestMapping(value = "/titles", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getTitles() {

    List<LookUpDTO> titles = lookUpService.getLookupList(LookUpCodesConstants.LOOKUP_TITLE_CODE);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

  @RequestMapping(value = "/genders", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getGenders() {

    List<LookUpDTO> titles = lookUpService.getLookupList(LookUpCodesConstants.LOOKUP_GENDER_CODE);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

  @RequestMapping(value = "/martialStatus", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getMartialStatus() {

    List<LookUpDTO> titles = lookUpService
        .getLookupList(LookUpCodesConstants.LOOKUP_MARTIAL_STATUS_CODE);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

  @RequestMapping(value = "/relationship", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getRelationship() {

    List<LookUpDTO> titles = lookUpService
        .getLookupList(LookUpCodesConstants.LOOKUP_RELATIONSHIP_CODE);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

  @RequestMapping(value = "/absenceType", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getAbsenceType() {

    List<LookUpDTO> titles = lookUpService
        .getLookupList(LookUpCodesConstants.LOOKUP_ABSENCE_TYPE_CODE);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

  @RequestMapping(value = "/absenceType/{code}/absenceReasons", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getAbsenceReasons(@PathVariable String code) {

    List<LookUpDTO> titles = lookUpService.getLookupList(code);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

  @RequestMapping(value = "/educationLevel", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getEducationLevel() {

    List<LookUpDTO> titles = lookUpService
        .getLookupList(LookUpCodesConstants.LOOKUP_EDUCATION_LEVEL_CODE);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

  @RequestMapping(value = "/bankName", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getBankName() {

    List<LookUpDTO> titles = lookUpService
        .getLookupList(LookUpCodesConstants.LOOKUP_BANK_NAME_CODE);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

  @RequestMapping(value = "/certificateType", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getCertificateType() {

    List<LookUpDTO> titles = lookUpService
        .getLookupList(LookUpCodesConstants.LOOKUP_CERTIFICATE_TYPE_CODE);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

  @RequestMapping(value = "/certificateLanguage", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getCertificateLanguage() {

    List<LookUpDTO> titles = lookUpService
        .getLookupList(LookUpCodesConstants.LOOKUP_CERTIFICATE_LANGUAGE_CODE);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

  @RequestMapping(value = "/salaryInclude", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getSalaryInclude() {

    List<LookUpDTO> titles = lookUpService
        .getLookupList(LookUpCodesConstants.LOOKUP_SALARY_INCLUDE_CODE);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

  @RequestMapping(value = "/missionCategory", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getMissionCategory() {

    List<LookUpDTO> titles = lookUpService
        .getLookupList(LookUpCodesConstants.LOOKUP_MISSION_CATEGORY_CODE);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

  @RequestMapping(value = "/missionType", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getMissionType() {

    List<LookUpDTO> titles = lookUpService
        .getLookupList(LookUpCodesConstants.LOOKUP_MISSION_TYPE_CODE);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

  @RequestMapping(value = "/notificationStatus", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getNotificationStatus() {

    List<LookUpDTO> titles = lookUpService
        .getLookupList(LookUpCodesConstants.LOOKUP_NOTIFICATION_STATUS_CODE);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

  @RequestMapping(value = "/serviceType", method = RequestMethod.GET)
  public ResponseEntity<List<LookUpDTO>> getServiceType() {

    List<LookUpDTO> titles = lookUpService
        .getLookupList(LookUpCodesConstants.LOOKUP_SERVICE_TYPE_CODE);

    return new ResponseEntity<List<LookUpDTO>>(titles, HttpStatus.OK);
  }

}
