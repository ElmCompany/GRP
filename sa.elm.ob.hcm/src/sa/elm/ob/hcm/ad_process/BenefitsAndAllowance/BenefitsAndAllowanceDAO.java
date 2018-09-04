package sa.elm.ob.hcm.ad_process.BenefitsAndAllowance;

import sa.elm.ob.hcm.EHCMBenefitAllowance;

/**
 * Interface for all Benefits and Allowance related DB Operations
 * 
 * @author Kousalya -28-07-2018
 *
 */

public interface BenefitsAndAllowanceDAO {

  /**
   * Reactivate Benefits and Allowance
   * 
   * @param allowance
   * @return boolean
   * @throws Exception
   */
  boolean reactivateEmpBenefitandAllowance(EHCMBenefitAllowance allowance) throws Exception;
}
