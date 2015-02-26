package framework.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

/**
 * HTML, JavaScript, CSS Minify filter
 */
public class MinifyFilter implements Filter {
	private HtmlCompressor _compressor;
	private Pattern _textualMimePattern = Pattern.compile("^$|^text|json$|xml$|html$|javascript$|css$", Pattern.CASE_INSENSITIVE);
	private Pattern _compressibleMimePattern = Pattern.compile("html$|xml$|javascript$|css$", Pattern.CASE_INSENSITIVE);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		MyResponseWrapper resWrapper = null;
		try {
			resWrapper = new MyResponseWrapper((HttpServletResponse) response);
			filterChain.doFilter(request, resWrapper);
			String contentType = _nullToBlankString(resWrapper.getContentType());
			if (_isTextualContentType(contentType)) {
				_minifying(response, resWrapper, contentType);
			} else {
				resWrapper.writeTo(response.getOutputStream());
			}
		} finally {
			if (resWrapper != null) {
				resWrapper.close();
				resWrapper = null;
			}
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		_compressor = new HtmlCompressor();
		_compressor.setCompressCss(true);
		_compressor.setCompressJavaScript(true);
	}

	@Override
	public void destroy() {
	}

	private void _minifying(ServletResponse response, MyResponseWrapper resWrapper, String contentType) throws IOException {
		PrintWriter writer = response.getWriter();
		String content = resWrapper.toString();
		if (_isCompressibleContentType(contentType)) {
			writer.write(_compressor.compress(content));
		} else {
			writer.write(content);
		}
		writer.flush();
	}

	private boolean _isTextualContentType(String contentType) {
		return _textualMimePattern.matcher(contentType).matches();
	}

	private boolean _isCompressibleContentType(String contentType) {
		return _compressibleMimePattern.matcher(contentType).matches();
	}

	private static String _nullToBlankString(String str) {
		String rval = "";
		if (str == null) {
			rval = "";
		} else {
			rval = str;
		}
		return rval;
	}

	class MyResponseWrapper extends HttpServletResponseWrapper {
		private ByteArrayOutputStream _bytes;
		private PrintWriter _writer;

		public MyResponseWrapper(HttpServletResponse p_res) {
			super(p_res);
			_bytes = new ByteArrayOutputStream(8 * 1024);
			_writer = new PrintWriter(_bytes);
		}

		@Override
		public PrintWriter getWriter() {
			return _writer;
		}

		@Override
		public ServletOutputStream getOutputStream() {
			return new MyOutputStream(_bytes);
		}

		@Override
		public String toString() {
			_writer.flush();
			return _bytes.toString();
		}

		public void writeTo(OutputStream os) throws IOException {
			_bytes.writeTo(os);
		}

		public void close() throws IOException {
			_bytes.close();
			_writer.close();
			_bytes = null;
			_writer = null;
		}
	}

	class MyOutputStream extends ServletOutputStream {
		private ByteArrayOutputStream _bytes;

		public MyOutputStream(ByteArrayOutputStream p_bytes) {
			_bytes = p_bytes;
		}

		@Override
		public void write(int p_c) throws IOException {
			_bytes.write(p_c);
		}

		@Override
		public void write(byte[] b) throws IOException {
			_bytes.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			_bytes.write(b, off, len);
		}

		@Override
		public void close() throws IOException {
			_bytes.close();
			super.close();
			_bytes = null;
		}
	}
}