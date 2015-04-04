package framework.filter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * GZIP Compression filter
 */
public class CompressionFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		if (_isGzipSupported(request)) {
			MyResponseWrapper resWrapper = null;
			try {
				resWrapper = new MyResponseWrapper((HttpServletResponse) response);
				resWrapper.setHeader("Content-Encoding", "gzip");
				filterChain.doFilter(request, resWrapper);
				GZIPOutputStream gzos = resWrapper.getGZIPOutputStream();
				gzos.finish();
			} finally {
				if (resWrapper != null) {
					resWrapper.close();
					resWrapper = null;
				}
			}
		} else {
			filterChain.doFilter(request, response);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	private boolean _isGzipSupported(ServletRequest request) {
		String browserEncodings = ((HttpServletRequest) request).getHeader("Accept-Encoding");
		return ((browserEncodings != null) && (browserEncodings.indexOf("gzip") != -1));
	}

	class MyResponseWrapper extends HttpServletResponseWrapper {
		private MyOutputStream _os;
		private PrintWriter _writer;
		private Object _streamUsed;

		public MyResponseWrapper(HttpServletResponse p_res) {
			super(p_res);
		}

		public void setContentLength(int len) {
		}

		public GZIPOutputStream getGZIPOutputStream() {
			return _os.gzos;
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			if ((_streamUsed != null) && (_streamUsed != _os)) {
				throw new IllegalStateException();
			}
			if (_writer == null) {
				_os = new MyOutputStream(getResponse().getOutputStream());
				OutputStreamWriter osw = new OutputStreamWriter(_os, getResponse().getCharacterEncoding());
				_writer = new PrintWriter(osw);
				_streamUsed = _writer;
			}
			return _writer;
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			if ((_streamUsed != null) && (_streamUsed != _writer)) {
				throw new IllegalStateException();
			}
			if (_os == null) {
				_os = new MyOutputStream(getResponse().getOutputStream());
				_streamUsed = _os;
			}
			return _os;
		}

		public void close() throws IOException {
			_os.close();
			_writer.close();
			_os = null;
			_writer = null;
		}
	}

	class MyOutputStream extends ServletOutputStream {
		GZIPOutputStream gzos;

		public MyOutputStream(ServletOutputStream sos) throws IOException {
			gzos = new GZIPOutputStream(sos);
		}

		@Override
		public void write(int p_c) throws IOException {
			gzos.write(p_c);
		}

		@Override
		public void write(byte[] b) throws IOException {
			gzos.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			gzos.write(b, off, len);
		}

		@Override
		public void close() throws IOException {
			gzos.close();
			super.close();
			gzos = null;
		}
	}
}