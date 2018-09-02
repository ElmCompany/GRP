package sa.elm.ob.hcm.selfservice.dto.lookup;

import java.io.Serializable;

/**
 * DTO for holding lookupValues
 * 
 */
public class LookUpDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 6132713761434061156L;
  private String id;
  private String description;

  public LookUpDTO(String id, String description) {
    super();
    this.id = id;
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
