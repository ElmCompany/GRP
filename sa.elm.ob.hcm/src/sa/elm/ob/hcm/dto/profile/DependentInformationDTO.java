package sa.elm.ob.hcm.dto.profile;

import java.io.Serializable;

public class DependentInformationDTO extends PersonalInformationDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -6415082365996951448L;
  private String relationship;

  public String getRelationship() {
    return relationship;
  }

  public void setRelationship(String relationship) {
    this.relationship = relationship;
  }

}
