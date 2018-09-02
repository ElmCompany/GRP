package sa.elm.ob.hcm.selfservice.exceptions;
/**
 * Represents the response message
 *
 */

import java.io.Serializable;

public class ResponseMessage implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1218056986967347524L;

  private boolean success;
  private String message;

  /**
   * Parameterized Constructor
   * 
   * @param success
   * @param message
   */
  public ResponseMessage(boolean success, String message) {
    super();
    this.success = success;
    this.message = message;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
