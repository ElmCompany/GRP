package sa.elm.ob.hcm.ad_process.BenefitsAndAllowance;

import java.sql.Connection;

import org.openbravo.dal.service.OBDal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenefitsAndAllowanceDAO {

  private Connection connection = null;
  private static final Logger log = LoggerFactory.getLogger(BenefitsAndAllowanceDAO.class);

  public BenefitsAndAllowanceDAO() {
    connection = getDbConnection();
  }

  private Connection getDbConnection() {
    return OBDal.getInstance().getConnection();
  }
}
