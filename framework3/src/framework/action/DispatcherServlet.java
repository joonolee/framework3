/** 
 * @(#)DispatcherServlet.java
 */
package framework.action;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import framework.cache.Cache;
import framework.util.StringUtil;

/** 
 * 컨트롤러 역할을 하는 서블릿으로 모든 클라이언트의 요청을 받아 해당 액션을 실행한다.
 * web.xml 파일에서 서블릿을 매핑하여야 하며 서버 부팅시 한개의 객체를 생성해 놓는다.  
 * 요청에서 추출한 액션키로 routes.properties에서 Controller클래스를 찾아 객체를 생성하여 비지니스 프로세스를 실행한다. 
 */
public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = -6478697606075642071L;
	private static Log _logger = LogFactory.getLog(framework.action.DispatcherServlet.class);
	private static final String[] _DEFAULT_SERVLET_NAMES = new String[] { "default", "WorkerServlet", "FileServlet", "resin-file", "SimpleFileServlet", "_ah_default" };
	private String _defaultServletName = null;

	/**
	 * 서블릿 객체를 초기화 한다.
	 * web.xml에 초기화 파라미터로 등록되어 있는 routes-mapping 값을 찾아 리소스 번들을 생성하는 역할을 한다.
	 * @param config ServletConfig 객체
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ResourceBundle bundle = null;
		try {
			bundle = ResourceBundle.getBundle(config.getInitParameter("routes-mapping"));
			_defaultServletName = StringUtil.nullToBlankString(config.getInitParameter("default-servlet-name"));
			if ("".equals(_defaultServletName)) {
				for (String servletName : _DEFAULT_SERVLET_NAMES) {
					if (getServletContext().getNamedDispatcher(servletName) != null) {
						_defaultServletName = servletName;
						break;
					}
				}
			}
			if (getServletContext().getNamedDispatcher(_defaultServletName) == null) {
				throw new IllegalStateException("defaultServletName Error!");
			}
		} catch (MissingResourceException e) {
			throw new ServletException(e);
		}
		getServletContext().setAttribute("routes-mapping", bundle);
		// Cache
		Cache.init();
	}

	/**
	 * 클라이언트가 Get 방식으로 요청할 경우 processRequest로 처리를 이관한다.
	 * @param request HTTP 클라이언트 요청객체
	 * @param response HTTP 클라이언트 응답객체
	 * @exception java.io.IOException DispatcherServlet에서 IO와 관련된 오류가 발생할 경우 
	 * @exception javax.servlet.ServletException 서블릿과 관련된 오류가 발생할 경우
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		_processRequest(request, response);
	}

	/**
	 * 클라이언트가 Post 방식으로 요청할 경우 processRequest로 처리를 이관한다.
	 * @param request HTTP 클라이언트 요청객체
	 * @param response HTTP 클라이언트 응답객체
	 * @exception java.io.IOException Servlet에서 IO와 관련된 오류가 발생할 경우 
	 * @exception javax.servlet.ServletException 서블릿과 관련된 오류가 발생할 경우
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		_processRequest(request, response);
	}

	/**
	 * 클라이언트가 Put 방식으로 요청할 경우 processRequest로 처리를 이관한다.
	 * @param request HTTP 클라이언트 요청객체
	 * @param response HTTP 클라이언트 응답객체
	 * @exception java.io.IOException DispatcherServlet에서 IO와 관련된 오류가 발생할 경우 
	 * @exception javax.servlet.ServletException 서블릿과 관련된 오류가 발생할 경우
	 */
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		_processRequest(request, response);
	}

	/**
	 * 클라이언트가 Delete 방식으로 요청할 경우 processRequest로 처리를 이관한다.
	 * @param request HTTP 클라이언트 요청객체
	 * @param response HTTP 클라이언트 응답객체
	 * @exception java.io.IOException DispatcherServlet에서 IO와 관련된 오류가 발생할 경우 
	 * @exception javax.servlet.ServletException 서블릿과 관련된 오류가 발생할 경우
	 */
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		_processRequest(request, response);
	}

	//////////////////////////////////////////////////////////////////////////////////////////Private 메소드

	private void _processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Controller controller = null;
			Method actionMethod = null;
			try {
				String routePath = _getRoutePath(request);
				String[] controllerAndAction = _getControllerAndAction(routePath);
				String controllerName = controllerAndAction[0];
				String actionName = controllerAndAction[1];
				Class<?> controllerClass = Class.forName(controllerName);
				controller = (Controller) controllerClass.newInstance();
				actionMethod = controllerClass.getMethod(actionName);
				if (!Modifier.isPublic(actionMethod.getModifiers())) { // Public 메소드만 호출가능
					throw new Exception("This is not a public method.");
				}
			} catch (Exception e) {
				this.getServletContext().getNamedDispatcher(_defaultServletName).forward(request, response);
				return;
			}
			long currTime = 0;
			if (_getLogger().isDebugEnabled()) {
				currTime = System.currentTimeMillis();
				_getLogger().debug("★★★ " + request.getRemoteAddr() + " 로 부터 \"" + request.getMethod() + " " + request.getRequestURI() + "\" 요청이 시작되었습니다");
				_getLogger().debug("ContentLength : " + request.getContentLength() + "bytes");
			}
			controller.execute(this, request, response, actionMethod);
			if (_getLogger().isDebugEnabled()) {
				_getLogger().debug("☆☆☆ " + request.getRemoteAddr() + " 로 부터 \"" + request.getMethod() + " " + request.getRequestURI() + "\" 요청이 종료되었습니다 | duration : " + (System.currentTimeMillis() - currTime) + "ms\n");
			}
		} catch (Exception e) {
			_getLogger().error(e);
			throw new RuntimeException(e);
		}
	}

	private String _getRoutePath(HttpServletRequest request) {
		String path = request.getServletPath() + StringUtil.nullToBlankString(request.getPathInfo());
		String normalizePath = path.replaceAll("/+", "/");
		return normalizePath;
	}

	private String[] _getControllerAndAction(String routePath) {
		try {
			ResourceBundle bundle = (ResourceBundle) getServletContext().getAttribute("routes-mapping");
			String value = ((String) bundle.getObject(routePath)).trim();
			int period = value.lastIndexOf(".");
			return new String[] { value.substring(0, period), value.substring(period + 1) };
		} catch (MissingResourceException e) {
			return null;
		}
	}

	private Log _getLogger() {
		return DispatcherServlet._logger;
	}
}