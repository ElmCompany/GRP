package sa.elm.ob.hcm.selfservice.exceptions;

/**
 * Represents Business Exception
 *
 */
public class BusinessException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -5274530102209709848L;
  private String messageKey = "";

  public String getMessageKey() {
    return messageKey;
  }

  public void setMessageKey(String messageKey) {
    this.messageKey = messageKey;
  }

  public BusinessException(String message) {
    super(message);
  }
}
