package framework.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import framework.util.StringUtil;

/**
 * SameSite None Filter
 */
public class SameSiteNoneFilter implements Filter {
	private final String SET_COOKIE = "Set-Cookie";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		filterChain.doFilter(request, response);
		if ("https".equals(getScheme((HttpServletRequest) request))) {
			addSameSiteNoneSecureToCookie((HttpServletResponse) response);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	private void addSameSiteNoneSecureToCookie(HttpServletResponse response) {
		boolean firstHeader = true;
		for (String header : response.getHeaders(SET_COOKIE)) {
			if (firstHeader) {
				response.setHeader(SET_COOKIE, String.format("%s; %s", header, "SameSite=None; Secure"));
				firstHeader = false;
				continue;
			}
			response.addHeader(SET_COOKIE, String.format("%s; %s", header, "SameSite=None; Secure"));
		}
	}

	private String getScheme(HttpServletRequest request) {
		String scheme = request.getScheme();
		String proto = request.getHeader("X-Forwarded-Proto"); // AWS ELB를 사용하는 경우 헤더에 프로토콜이 셋팅됨
		if (StringUtil.isNotEmpty(proto)) {
			scheme = proto;
		}
		return scheme;
	}
}