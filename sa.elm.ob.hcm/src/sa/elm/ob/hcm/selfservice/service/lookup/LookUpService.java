package sa.elm.ob.hcm.selfservice.service.lookup;

import java.util.List;

import sa.elm.ob.hcm.selfservice.dto.lookup.LookUpDTO;

/**
 * Responsible for Returning lookup data
 *
 */
public interface LookUpService {

  /**
   * Get Countries
   * 
   * @return
   */
  List<LookUpDTO> getCountries();

  /**
   * Get the Cities by Region
   * 
   * @param countryId
   * @param regionId
   * @return
   */
  List<LookUpDTO> getCitiesByRegion(String regionId);

  /**
   * Get the Cities by Country
   * 
   * @param countryId
   * @param regionId
   * @return
   */
  List<LookUpDTO> getCitiesByCountry(String countryId);

  /**
   * Get Regions By Country
   * 
   * @return
   */
  List<LookUpDTO> getRegionsByCountry(String countryId);

  /**
   * Get the List of Titles
   * 
   * @return
   */
  List<LookUpDTO> getLookupList(String lookupCode);

}
