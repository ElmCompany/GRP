package sa.elm.ob.hcm.selfservice.aspect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import sa.elm.ob.hcm.selfservice.security.UserPrincipal;
import sa.elm.ob.hcm.util.SelfServiceConstants;

public class LanguageInterceptor extends HandlerInterceptorAdapter {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal();
    userPrincipal.setUserLanguage(request.getHeader(SelfServiceConstants.ACCEPT_LANG_HEADER));

    return super.preHandle(request, response, handler);
  }

}
