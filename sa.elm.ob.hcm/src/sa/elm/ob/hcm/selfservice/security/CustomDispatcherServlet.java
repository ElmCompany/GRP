package sa.elm.ob.hcm.selfservice.security;

import javax.servlet.annotation.WebServlet;

import org.springframework.web.servlet.DispatcherServlet;

/**
 * Custom Dispatcher Servlet
 *
 */
@WebServlet(name = "mvc-dispatcher", urlPatterns = "/")
public class CustomDispatcherServlet extends DispatcherServlet {

  /**
   * 
   */
  private static final long serialVersionUID = -1319768847146717525L;

}
