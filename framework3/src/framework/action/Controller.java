/** 
 * @(#)Controller.java
 */
package framework.action;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import framework.config.Config;
import framework.db.ConnectionManager;

/** 
 * 비지니스 로직을 처리하는 클래스가 상속받아야 할 추상클래스이다.
 * 뷰페이지(jsp 페이지)가 실행되기 전에 클라이언트에서 서버로 전송된 데이터를 편리하게 업무로직에 반영하기 
 * 위한 전처리(Pre-processing)모듈이다. 하나의 서비스에 대해 여러개의 업무로직을 컴포넌트 형태로 제작하여 등록할 수 있다. 
 * 작성된 Controller는 routes.properties에 등록한다.
 */
public abstract class Controller {
	private Map<String, ConnectionManager> _connMgrMap = new HashMap<String, ConnectionManager>();

	/**
	 * Controller를 호출한 서블릿 객체
	 */
	protected HttpServlet servlet = null;

	/**
	 * HTTP 클라이언트 요청객체
	 */
	protected HttpServletRequest request = null;

	/**
	 * 요청파라미터의 값을 담는 해시테이블
	 */
	protected Params params = null;

	/**
	 * 쿠키값을 담는 해시테이블
	 */
	protected Params cookies = null;

	/**
	 * 클라이언트의 세션 객체
	 */
	protected HttpSession session = null;

	/**
	 * HTTP 클라이언트 응답객체
	 */
	protected HttpServletResponse response = null;

	/**
	 * 응답객체의 PrintWriter 객체
	 */
	protected PrintWriter out = null;

	/**
	 * Controller의 로거객체
	 */
	protected static Log logger = LogFactory.getLog(framework.action.Controller.class);

	/** 
	 * 클라이언트에서 서비스를 호출할 때 요청파라미터 action에 설정된 값을 참고하여 해당 메소드를 실행한다.
	 * 정의되지 않은 메소드를 호출할 경우 로그에 오류메시지가 기록되며 메소드 실행을 마친 후 데이터베이스 컨넥을 자동으로 닫아준다.
	 * <br>
	 * ex) action이 search 일때 => search() 메소드가 호출된다.
	 * @param servlet 서블릿 객체
	 * @param request 클라이언트에서 요청된 Request객체
	 * @param response 클라이언트로 응답할 Response객체
	 * @param method 메소드
	 * @throws Exception
	 */
	public void execute(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response, Method method) throws Exception {
		this.servlet = servlet;
		this.request = request;
		this.params = Params.getParams(request);
		this.cookies = Params.getParamsFromCookie(request);
		this.session = request.getSession();
		this.response = response;
		this.out = response.getWriter();
		long currTime = 0;
		if (logger.isDebugEnabled()) {
			currTime = System.currentTimeMillis();
			logger.debug("Start");
			logger.debug(this.params.toString());
			logger.debug(this.cookies.toString());
		}
		try {
			before();
			method.invoke(this, (Object[]) null);
			after();
		} finally {
			_destroy();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("End | duration : " + (System.currentTimeMillis() - currTime) + " msec");
		}
	}

	/**
	 * 액션메소드가 호출되기 직전에 호출된다.
	 * 컨트롤러 클래스에서 오버라이드 하면 자동 호출된다.
	 */
	protected void before() {
	}

	/**
	 * 액션메소드가 호출되고난 직후에 호출된다.
	 * 컨트롤러 클래스에서 오버라이드 하면 자동 호출된다.
	 */
	protected void after() {
	}

	/**
	 * 요청을 JSP페이지로 포워드(Forward) 한다.
	 * 작성된 JSP페이지는 routes.properties에 등록한다.
	 * <br>
	 * ex) 키가 search-jsp 인 JSP페이지로 포워딩 할 경우 => render("search-jsp")
	 * @param jsp routes.properties 파일에 등록된 JSP 페이지의 키
	 */
	protected void render(String jsp) {
		try {
			Router router = new Router(jsp, true);
			router.route(servlet, request, response);
		} catch (Exception e) {
			logger.error("Render Error!", e);
		}
	}

	/** 
	 * 요청을 JSP페이지로 재지향(Redirect) 한다.
	 * 작성된 JSP페이지는  routes.properties에 등록한다.
	 * <br>
	 * ex) 키가 search-jsp 인 JSP페이지로 재지향 할 경우 => redirect("search-jsp")
	 * @param key routes.properties 파일에 등록된 JSP 페이지의 키
	 */
	protected void redirect(String key) {
		try {
			Router router = new Router(key, false);
			router.route(servlet, request, response);
		} catch (Exception e) {
			logger.error("Redirect Error!", e);
		}
	}

	/** 
	 * 데이타베이스 연결관리자(컨넥션 매니저) 객체를 리턴한다.
	 * <br>
	 * config.properties에 datasource가 등록되어 있으면 JNDI에 등록되어있는 데이타소스에서 컨넥션을 생성한다.
	 * datasource가 등록되어 있지 않는 경우 연결정보를 바탕으로 jdbc 컨넥션을 생성한다.
	 * 업무명이 default에 해당하는 설정파일 정보를 이용하여 컨넥션을 생성한다.
	 * 생성된 컨넥션의 autoCommit 속성은 false 로 셋팅된다.
	 * @return 연결관리자(컨넥션 매니저) 객체
	 */
	protected ConnectionManager getConnectionManager() {
		return getConnectionManager("default");
	}

	/** 
	 * 데이타베이스 연결관리자(컨넥션 매니저) 객체를 리턴한다.
	 * <br>
	 * config.properties에 datasource가 등록되어 있으면 JNDI에 등록되어있는 데이타소스에서 컨넥션을 생성한다.
	 * datasource가 등록되어 있지 않는 경우 연결정보를 바탕으로 jdbc 컨넥션을 생성한다.
	 * 파라미터로 넘겨진 업무명에 해당하는 설정파일 정보를 이용하여 컨넥션을 생성한다.
	 * 생성된 컨넥션의 autoCommit 속성은 false 로 셋팅된다.
	 * @param serviceName 서비스명(업무명)
	 * @return 연결관리자(컨넥션 매니저) 객체
	 */
	protected ConnectionManager getConnectionManager(String serviceName) {
		if (!_connMgrMap.containsKey(serviceName)) {
			String dsName = null;
			String jdbcDriver = null;
			String jdbcUrl = null;
			String jdbcUid = null;
			String jdbcPw = null;
			try {
				dsName = getConfig().getString("jdbc." + serviceName + ".datasource");
			} catch (Exception e) {
				// 설정파일에 데이타소스가 정의되어있지 않으면 실행
				jdbcDriver = getConfig().getString("jdbc." + serviceName + ".driver");
				jdbcUrl = getConfig().getString("jdbc." + serviceName + ".url");
				jdbcUid = getConfig().getString("jdbc." + serviceName + ".uid");
				jdbcPw = getConfig().getString("jdbc." + serviceName + ".pwd");
			}
			try {
				ConnectionManager connMgr = new ConnectionManager(dsName, this);
				if (dsName != null) {
					connMgr.connect();
				} else {
					connMgr.connect(jdbcDriver, jdbcUrl, jdbcUid, jdbcPw);
				}
				connMgr.setAutoCommit(false);
				_connMgrMap.put(serviceName, connMgr);
			} catch (Exception e) {
				logger.error("DB Connection Error!", e);
			}
		}
		return _connMgrMap.get(serviceName);
	}

	/** 
	 * 설정정보를 가지고 있는 객체를 생성하여 리턴한다.
	 * @return config.properties의 설정정보를 가지고 있는 객체
	 */
	protected Config getConfig() {
		return Config.getInstance();
	}

	/** 
	 * 세션객체에서 해당 키에 해당하는 오브젝트를 리턴한다.
	 * <br>
	 * ex) 세션에서 result라는 키로 오브젝트를 리턴받는 경우 => Object obj = getSessionAttribute("result")
	 * @param key 세션객체의 조회키
	 * @return 세션객체에서 얻어온 오브젝트
	 */
	protected Object getSessionAttribute(String key) {
		return session.getAttribute(key);
	}

	/**
	 * 응답객체를 클라이언트에게 전송하기 전에 컨텐츠타입을 설정한다. 
	 * <br>
	 * ex1) xml파일을 전송 하는 경우 => setContentType("text/xml; charset=utf-8")
	 * <br>
	 * ex2) 텍스트 파일을 전송하는 경우 => setContentType("text/plain; charset=euc-kr")
	 * @param contentType 응답객체에 설정할 컨텐츠 타입
	 */
	protected void setContentType(String contentType) {
		response.setContentType(contentType);
	}

	/** 
	 * 요청객체에 키,값 속성을 설정한다.
	 * Controller에서 처리한 결과를 뷰 로 넘길때 요청객체에 속성을 설정하여 라우팅한다.
	 * <br>
	 * ex) rs라는 RecordSet 객체를 result 라는 키로 요청객체에 설정하는 경우 => setAttribute("result", re) 
	 * @param key 속성의 키 문자열
	 * @param value 속성의 값 객체
	 */
	protected void setAttribute(String key, Object value) {
		request.setAttribute(key, value);
	}

	/** 
	 * 세션객체에 키,값 속성을 설정한다.
	 * Controller에서 처리한 결과를 세션에 저장한다.
	 * <br>
	 * ex) userinfo 라는 사용자정보객체를 userinfo 라는 키로 세션객체에 설정하는 경우 => setSessionAttribute("userinfo", userinfo)
	 * @param key 속성의 키 문자열
	 * @param value 속성의 값 객체
	 */
	protected void setSessionAttribute(String key, Object value) {
		session.setAttribute(key, value);
	}

	//////////////////////////////////////////////////////////////////////////////////////////Private 메소드

	private void _destroy() {
		ConnectionManager connMgr = null;
		for (String key : _connMgrMap.keySet()) {
			connMgr = _connMgrMap.get(key);
			if (connMgr != null) {
				connMgr.release();
				connMgr = null;
			}
		}
		_connMgrMap.clear();
		params = null;
		out = null;
	}
}