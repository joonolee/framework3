/* 
 * @(#)CharacterEncodingFilter.java
 * 요청과 응답을 인코딩 하는 필터
 */
package framework.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class CharacterEncodingFilter implements Filter {
	private String _encoding = null;
	private boolean _forceEncoding = false;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		if (this._encoding != null && (this._forceEncoding || request.getCharacterEncoding() == null)) {
			request.setCharacterEncoding(this._encoding);
			if (this._forceEncoding) {
				response.setCharacterEncoding(this._encoding);
			}
		}
		filterChain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this._encoding = filterConfig.getInitParameter("encoding");
		this._forceEncoding = Boolean.valueOf(filterConfig.getInitParameter("forceEncoding"));
	}

	@Override
	public void destroy() {
	}
}
