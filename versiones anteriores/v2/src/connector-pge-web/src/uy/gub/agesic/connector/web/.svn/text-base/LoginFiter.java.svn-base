package uy.gub.agesic.connector.web;

import java.io.IOException;

import javax.faces.application.ViewExpiredException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class LoginFiter implements Filter {
	private FilterConfig filterConfig;

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain fc) throws IOException, ServletException {
		try {
			fc.doFilter(request, response);
		} catch (Exception e) {
			HttpSession session = ((HttpServletRequest) request).getSession(false);
			if (session == null) {
				filterConfig.getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
			} else if (e.getCause() instanceof ViewExpiredException) {
				filterConfig.getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
			}
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

}
