package framework.action;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
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
	private static final Log logger = LogFactory.getLog(DispatcherServlet.class);
	private static final String[] DEFAULT_SERVLET_NAMES = new String[] { "default", "WorkerServlet", "ResourceServlet", "FileServlet", "resin-file", "SimpleFileServlet", "_ah_default" };

	/**
	 * 서블릿 객체를 초기화 한다.
	 * web.xml에 초기화 파라미터로 등록되어 있는 routes-mapping, views-mapping 값을 찾아 리소스 번들을 생성하는 역할을 한다.
	 * @param config ServletConfig 객체
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			// default servlet 검색
			String defaultServletName = StringUtil.nullToBlankString(config.getInitParameter("default-servlet-name"));
			if ("".equals(defaultServletName)) {
				for (String servletName : DEFAULT_SERVLET_NAMES) {
					if (getServletContext().getNamedDispatcher(servletName) != null) {
						defaultServletName = servletName;
						break;
					}
				}
			}
			RequestDispatcher dispatcher = getServletContext().getNamedDispatcher(defaultServletName);
			if (dispatcher != null) {
				getServletContext().setAttribute("default-servlet-dispatcher", dispatcher);
				logger.info("Default Servlet을 찾았습니다. (" + defaultServletName + ")");
			} else {
				logger.info("Default Servlet을 찾을 수 없습니다.");
			}
			// routes 번들 설정
			ResourceBundle routesBundle = ResourceBundle.getBundle(config.getInitParameter("routes-mapping"));
			getServletContext().setAttribute("routes-mapping", routesBundle);
			// views 번들 설정
			ResourceBundle viewsBundle = ResourceBundle.getBundle(config.getInitParameter("views-mapping"));
			getServletContext().setAttribute("views-mapping", viewsBundle);
		} catch (MissingResourceException e) {
			throw new ServletException(e);
		}
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
		processRequest(request, response);
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
		processRequest(request, response);
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
		processRequest(request, response);
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
		processRequest(request, response);
	}

	//////////////////////////////////////////////////////////////////////////////////////////Private 메소드

	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Controller controller = null;
			Method action = null;
			try {
				String routePath = getRoutePath(request);
				String[] controllerAction = getControllerAction(routePath);
				String controllerName = controllerAction[0];
				String actionName = controllerAction[1];
				Class<?> controllerClass = Class.forName(controllerName);
				controller = (Controller) controllerClass.newInstance();
				action = controllerClass.getMethod(actionName);
				if (!isActionMethod(action)) {
					throw new Exception("호출할 수 없는 메소드입니다.");
				}
			} catch (Throwable e) {
				RequestDispatcher dispatcher = (RequestDispatcher) getServletContext().getAttribute("default-servlet-dispatcher");
				if (dispatcher != null) {
					dispatcher.forward(request, response);
				} else {
					response.sendError(404);
					response.setContentType("text/plain;charset=utf-8");
				}
				return;
			}
			long currTime = 0;
			if (logger.isDebugEnabled()) {
				currTime = System.currentTimeMillis();
				logger.debug("★★★ " + request.getRemoteAddr() + " 로 부터 \"" + request.getMethod() + " " + request.getRequestURI() + "\" 요청이 시작되었습니다");
				logger.debug("ContentLength : " + request.getContentLength() + "bytes");
			}
			controller.execute(this, request, response, action);
			if (logger.isDebugEnabled()) {
				logger.debug("☆☆☆ " + request.getRemoteAddr() + " 로 부터 \"" + request.getMethod() + " " + request.getRequestURI() + "\" 요청이 종료되었습니다 | duration : " + (System.currentTimeMillis() - currTime) + "ms\n");
			}
		} catch (Throwable e) {
			logger.error("", e);
			throw new ServletException(e);
		}
	}

	private String getRoutePath(HttpServletRequest request) {
		String path = request.getServletPath() + StringUtil.nullToBlankString(request.getPathInfo());
		String normalizePath = path.replaceAll("/+", "/");
		return normalizePath;
	}

	private String[] getControllerAction(String routePath) {
		try {
			ResourceBundle routesBundle = (ResourceBundle) getServletContext().getAttribute("routes-mapping");
			String value = ((String) routesBundle.getObject(routePath)).trim();
			int pos = value.lastIndexOf(".");
			return new String[] { value.substring(0, pos), value.substring(pos + 1) };
		} catch (MissingResourceException e) {
			return null;
		}
	}

	private boolean isActionMethod(Method method) {
		if (method.isAnnotationPresent(Before.class)) {
			return false;
		}
		if (method.isAnnotationPresent(After.class)) {
			return false;
		}
		if (method.isAnnotationPresent(Finally.class)) {
			return false;
		}
		if (method.isAnnotationPresent(Catch.class)) {
			return false;
		}
		return true;
	}
}