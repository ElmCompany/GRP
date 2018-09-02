package sa.elm.ob.hcm.selfservice.exceptions;

/**
 * Represents System Error
 *
 */
public class SystemException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -1906664839622067395L;
  private String messageKey = "";

  public String getMessageKey() {
    return messageKey;
  }

  public void setMessageKey(String messageKey) {
    this.messageKey = messageKey;
  }

  public SystemException(String message) {
    super(message);
  }

}
