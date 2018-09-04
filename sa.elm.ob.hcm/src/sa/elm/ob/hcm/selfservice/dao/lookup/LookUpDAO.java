package sa.elm.ob.hcm.selfservice.dao.lookup;

import java.util.List;

import org.openbravo.model.common.geography.City;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.Region;

import sa.elm.ob.utility.EUTDeflookupsTypeLn;

/**
 * DAO for Lookup Data Access
 * 
 *
 */
public interface LookUpDAO {
  /**
   * Get All countries
   * 
   * @return
   */
  List<Country> getCountries();

  /**
   * Get All Cities
   * 
   * @return
   */
  List<City> getCitiesByRegion(String regionId);

  /**
   * Get All Cities by Country
   * 
   * @return
   */
  List<City> getCitiesByCountry(String countryId);

  /**
   * Get the cities for a country
   * 
   * @param countryId
   * @return
   */
  List<Region> getRegionsByCountry(String countryId);

  /**
   * Get the Titles
   * 
   * @return
   */
  List<EUTDeflookupsTypeLn> getLookupList(String lookupCode);

  /**
   * @param code
   * @return
   */
  EUTDeflookupsTypeLn findSubLookupByCode(String code);

}
