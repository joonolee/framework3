package framework.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * GZIP Compression filter
 */
public class GZIPFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		MyResponseWrapper resWrapper = null;
		try {
			resWrapper = new MyResponseWrapper((HttpServletResponse) response);
			filterChain.doFilter(request, resWrapper);
			String contentType = nullToBlankString(resWrapper.getContentType());
			if (isTextualContentType(contentType)) {
				write(request, response, resWrapper);
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
	}

	@Override
	public void destroy() {
	}

	private void write(ServletRequest request, ServletResponse response, MyResponseWrapper resWrapper) throws IOException, UnsupportedEncodingException {
		if (isGzipSupported(request)) {
			resWrapper.setHeader("Content-Encoding", "gzip");
			GZIPOutputStream gzos = new GZIPOutputStream(response.getOutputStream());
			OutputStreamWriter osw = new OutputStreamWriter(gzos, response.getCharacterEncoding());
			PrintWriter writer = new PrintWriter(osw);
			writer.print(resWrapper.toString());
			writer.flush();
			gzos.finish();
		} else {
			PrintWriter writer = response.getWriter();
			writer.print(resWrapper.toString());
			writer.flush();
		}
	}

	private boolean isGzipSupported(ServletRequest request) {
		String browserEncodings = ((HttpServletRequest) request).getHeader("Accept-Encoding");
		return ((browserEncodings != null) && (browserEncodings.indexOf("gzip") != -1));
	}

	private boolean isTextualContentType(String contentType) {
		return "".equals(contentType) || contentType.contains("text") || contentType.contains("json") || contentType.contains("xml");
	}

	private static String nullToBlankString(String str) {
		String rval = "";
		if (str == null) {
			rval = "";
		} else {
			rval = str;
		}
		return rval;
	}

	class MyResponseWrapper extends HttpServletResponseWrapper {
		private ByteArrayOutputStream bytes;
		private PrintWriter writer;

		public MyResponseWrapper(HttpServletResponse res) throws IOException {
			super(res);
			bytes = new ByteArrayOutputStream(8 * 1024);
			writer = new PrintWriter(bytes);
		}

		@Override
		public PrintWriter getWriter() {
			return writer;
		}

		@Override
		public ServletOutputStream getOutputStream() {
			return new MyOutputStream(bytes);
		}

		@Override
		public String toString() {
			writer.flush();
			return bytes.toString();
		}

		public void writeTo(OutputStream os) throws IOException {
			bytes.writeTo(os);
		}

		public void close() throws IOException {
			bytes.close();
			writer.close();
			bytes = null;
			writer = null;
		}
	}

	class MyOutputStream extends ServletOutputStream {
		private ByteArrayOutputStream bytes;

		public MyOutputStream(ByteArrayOutputStream bytes) {
			this.bytes = bytes;
		}

		@Override
		public void write(int c) throws IOException {
			bytes.write(c);
		}

		@Override
		public void write(byte[] b) throws IOException {
			bytes.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			bytes.write(b, off, len);
		}

		@Override
		public void close() throws IOException {
			bytes.close();
			super.close();
			bytes = null;
		}

		@Override
		public boolean isReady() {
			return false;
		}

		@Override
		public void setWriteListener(WriteListener arg0) {
		}
	}
}