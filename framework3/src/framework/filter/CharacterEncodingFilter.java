package framework.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * 요청과 응답을 인코딩 하는 필터
 */
public class CharacterEncodingFilter implements Filter {
	private String encoding = null;
	private boolean force = true;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		if (this.force || (request.getCharacterEncoding() == null)) {
			if (this.encoding != null) {
				request.setCharacterEncoding(this.encoding);
			}
		}
		if (this.force || (response.getCharacterEncoding() == null)) {
			if (this.encoding != null) {
				response.setCharacterEncoding(this.encoding);
			}
		}
		filterChain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.encoding = filterConfig.getInitParameter("encoding");
		String force = filterConfig.getInitParameter("force");
		if (force == null) {
			this.force = true;
		} else if (force.equalsIgnoreCase("true")) {
			this.force = true;
		} else if (force.equalsIgnoreCase("yes")) {
			this.force = true;
		} else {
			this.force = false;
		}
	}

	@Override
	public void destroy() {
	}
}