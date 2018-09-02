package sa.elm.ob.hcm.selfservice.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint
implements AuthenticationEntryPoint{

 @Override
 public void commence(
   HttpServletRequest request,
   HttpServletResponse response, 
   AuthenticationException authException) throws IOException {

    response.sendError( HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized" );
 }
 
 @Bean
 HttpStatusReturningLogoutSuccessHandler httpStatusReturningLogoutSuccessHandler () {
	 
	 return new HttpStatusReturningLogoutSuccessHandler(HttpStatus.ACCEPTED);
 }
}
