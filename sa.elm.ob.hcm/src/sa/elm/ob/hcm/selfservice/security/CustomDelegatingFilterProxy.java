package sa.elm.ob.hcm.selfservice.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openbravo.base.session.OBPropertiesProvider;
import org.springframework.security.core.context.SecurityContextImpl;

import sa.elm.ob.hcm.util.SelfServiceConstants;

@WebFilter (
	filterName = "springSecurityFilterChain",
	urlPatterns = "/openerp/*")
public class CustomDelegatingFilterProxy extends org.springframework.web.filter.DelegatingFilterProxy {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		
		// Set the CORS Settings
		setResponseCorsSettings(httpServletResponse);
		// Set the user language
		setUserLanguage(httpServletRequest);
		// Let the parent class proceed with normal functionality
		super.doFilter(httpServletRequest, httpServletResponse, filterChain);
		
	}
	
	/**
	 * Set the CORS Request Settings
	 * @param httpServletResponse
	 */
	private void setResponseCorsSettings (HttpServletResponse httpServletResponse) {
		//Get the origins from properties file
		String allowedOrigins = getProperty(SelfServiceConstants.ALLOWED_ORIGINS_KEY);
		
		httpServletResponse.setHeader(SelfServiceConstants.ALLOWED_ORIGINS,	allowedOrigins);
		httpServletResponse.setHeader(SelfServiceConstants.ALLOWED_METHODS, SelfServiceConstants.ALLOWED_METHODS_VALUE);
		httpServletResponse.setHeader(SelfServiceConstants.ALLOWED_MAX_AGE, SelfServiceConstants.ALLOWED_MAX_AGE_VALUE);
		httpServletResponse.setHeader(SelfServiceConstants.ALLOWED_HEADERS, SelfServiceConstants.ALLOWED_HEADERS_VALUE);
		httpServletResponse.setHeader(SelfServiceConstants.ALLOWED_CREDENTIALS, SelfServiceConstants.ALLOWED_CREDENTIALS_VALUE);
		
	}
	
	/**
	 * Sets the User Language and sets in Spring Security User Principal
	 * For Internationalization this setting will be accessed later so that
	 * Arabic or English Response can be sent
	 * @param httpServletRequest
	 */
	private void setUserLanguage (HttpServletRequest httpServletRequest) {
		
		HttpSession session = httpServletRequest.getSession(false);
		
		if (null != session) {
			SecurityContextImpl sci = (SecurityContextImpl) session.getAttribute(SelfServiceConstants.SPRING_SECURITY_CONTEXT);
			// If user authenticated set the language in Spring Security Context
			if (sci != null) {
			        UserPrincipal userPrincipal = (UserPrincipal) sci.getAuthentication().getPrincipal();
			        // Set the Language in the user principal
			        userPrincipal.setUserLanguage(httpServletRequest.getHeader(SelfServiceConstants.ACCEPT_LANG_HEADER));
			}
		}

	}

	/**
	 * Get Property value from property file
	 * @param key
	 * @return
	 */
	private String getProperty (String key) {
		
		return OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty(key);
	}

}