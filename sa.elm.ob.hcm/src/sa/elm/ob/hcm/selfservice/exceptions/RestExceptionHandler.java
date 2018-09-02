package sa.elm.ob.hcm.selfservice.exceptions;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Generic Exception Handler for Self Services Rest Services
 *
 */
@ControllerAdvice
public class RestExceptionHandler {

  /**
   * Handles all the business exceptions thrown and sets a common message as per the selected
   * language
   * 
   * @param e
   * @return
   * @throws IOException
   */
  @ExceptionHandler
  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseMessage handleBusinessException(BusinessException e) throws IOException {
    ResponseMessage responseMessage = new ResponseMessage(false, e.getMessage());

    // TO-DO Write the common code to get the internationalized message

    return responseMessage;
  }

  /**
   * Handles all the System exceptions thrown and sets a common message as per the selected language
   * 
   * @param e
   * @return
   * @throws IOException
   */
  @ExceptionHandler
  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseMessage handleSystemException(SystemException e) throws IOException {
    ResponseMessage responseMessage = new ResponseMessage(false, e.getMessage());

    // TO-DO Write the common code to get the internationalized message

    return responseMessage;
  }

  /**
   * Handles all the general exceptions thrown and sets a common message as per the selected
   * language
   * 
   * @param e
   * @return
   * @throws IOException
   */
  @ExceptionHandler
  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseMessage handleException(Exception e) throws IOException {
    ResponseMessage responseMessage = new ResponseMessage(false, e.getMessage());

    // TO-DO Write the common code to get the internationalized message

    return responseMessage;
  }

}
