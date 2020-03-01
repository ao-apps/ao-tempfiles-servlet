/*
 * ao-tempfiles-servlet - Temporary file management in a Servlet environment.
 * Copyright (C) 2017, 2019, 2020  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-tempfiles-servlet.
 *
 * ao-tempfiles-servlet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-tempfiles-servlet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-tempfiles-servlet.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.tempfiles.servlet;

import com.aoindustries.tempfiles.TempFileContext;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Provides {@link TempFileContext temp file contexts} for {@link ServletContext},
 * {@link ServletRequest}, and {@link HttpSession}.
 */
public class TempFileContextEE {

	private static final String ATTRIBUTE = TempFileContext.class.getName();

	@WebListener
	public static class Initializer implements
		ServletContextListener,
		ServletRequestListener,
		HttpSessionListener
	{

		@Override
		public void contextInitialized(ServletContextEvent event) {
			ServletContext servletContext = event.getServletContext();
			assert servletContext.getAttribute(ATTRIBUTE) == null;
			servletContext.setAttribute(
				ATTRIBUTE,
				new TempFileContext(
					(File)servletContext.getAttribute(ServletContext.TEMPDIR)
				)
			);
		}

		@Override
		public void contextDestroyed(ServletContextEvent event) {
			ServletContext servletContext = event.getServletContext();
			TempFileContext tempFiles = (TempFileContext)servletContext.getAttribute(ATTRIBUTE);
			if(tempFiles != null) {
				try {
					tempFiles.close();
				} catch(IOException e) {
					servletContext.log("Error deleting temporary files", e);
				}
			}
		}

		@Override
		public void requestInitialized(ServletRequestEvent event) {
			ServletRequest request = event.getServletRequest();
			assert request.getAttribute(ATTRIBUTE) == null;
			request.setAttribute(
				ATTRIBUTE,
				new TempFileContext(
					(File)event.getServletContext().getAttribute(ServletContext.TEMPDIR)
				)
			);
		}

		@Override
		public void requestDestroyed(ServletRequestEvent event) {
			ServletRequest request = event.getServletRequest();
			TempFileContext tempFiles = (TempFileContext)request.getAttribute(ATTRIBUTE);
			if(tempFiles != null) {
				try {
					tempFiles.close();
				} catch(IOException e) {
					event.getServletContext().log("Error deleting temporary files", e);
				}
			}
		}

		@Override
		public void sessionCreated(HttpSessionEvent event) {
			HttpSession session = event.getSession();
			assert session.getAttribute(SESSION_ATTRIBUTE) == null;
			session.setAttribute(SESSION_ATTRIBUTE,
				new HttpSessionTempFileContext(
					session.getServletContext()
				)
			);
		}

		@Override
		public void sessionDestroyed(HttpSessionEvent event) {
			HttpSession session = event.getSession();
			HttpSessionTempFileContext wrapper = (HttpSessionTempFileContext)session.getAttribute(SESSION_ATTRIBUTE);
			if(wrapper != null) {
				TempFileContext tempFiles = wrapper.tempFiles;
				if(tempFiles != null) {
					wrapper.tempFiles = null;
					try {
						tempFiles.close();
					} catch(IOException e) {
						session.getServletContext().log("Error deleting temporary files", e);
					}
				}
			}
		}
	}

	/**
	 * Gets the {@link TempFileContext temp file context} for the given {@link ServletContext servlet context}.
	 *
	 * @throws  IllegalStateException  if the temp files have not been added to the servlet context.
	 */
	public static TempFileContext get(ServletContext servletContext) throws IllegalStateException {
		TempFileContext tempFiles = (TempFileContext)servletContext.getAttribute(ATTRIBUTE);
		if(tempFiles == null) throw new IllegalStateException(TempFileContextEE.Initializer.class.getName() + " not added to ServletContext; please use Servlet 3.0+ specification or manually add listener to web.xml.");
		return tempFiles;
	}

	/**
	 * Gets the {@link TempFileContext temp file context} for the given {@link ServletRequest servlet request}.
	 *
	 * @throws  IllegalStateException  if the temp files have not been added to the servlet request.
	 */
	public static TempFileContext get(ServletRequest request) throws IllegalStateException {
		TempFileContext tempFiles = (TempFileContext)request.getAttribute(ATTRIBUTE);
		if(tempFiles == null) throw new IllegalStateException(TempFileContextEE.Initializer.class.getName() + " not added to ServletRequest; please use Servlet 3.0+ specification or manually add listener to web.xml.");
		return tempFiles;
	}

	public static final String SESSION_ATTRIBUTE = HttpSessionTempFileContext.class.getName();

	private static class HttpSessionTempFileContext implements Serializable, HttpSessionActivationListener {

		private static final long serialVersionUID = 1L;

		private transient TempFileContext tempFiles;

		private HttpSessionTempFileContext(ServletContext servletContext) {
			tempFiles = new TempFileContext(
				(File)servletContext.getAttribute(ServletContext.TEMPDIR)
			);
		}

		@Override
		public void sessionWillPassivate(HttpSessionEvent event) {
			if(tempFiles != null) {
				try {
					tempFiles.close();
				} catch(IOException e) {
					event.getSession().getServletContext().log("Error deleting temporary files", e);
				}
				tempFiles = null;
			}
		}

		@Override
		public void sessionDidActivate(HttpSessionEvent event) {
			if(tempFiles == null) {
				tempFiles = new TempFileContext(
					(File)event.getSession().getServletContext().getAttribute(ServletContext.TEMPDIR)
				);
			}
		}
	}

	/**
	 * Gets the {@link TempFileContext temp file context} for the given {@link HttpSession session}.
	 * <p>
	 * At this time, temporary files put into the session are deleted when the session is
	 * {@link HttpSessionActivationListener#sessionWillPassivate(javax.servlet.http.HttpSessionEvent) passivated},
	 * at the {@link HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent) end of the session},
	 * or on JVM shutdown.  The temporary files are not {@link Serializable serialized} with the session.
	 * </p>
	 * <p>
	 * TODO: {@link TempFileContext} is not currently {@link Serializable}.  What would it mean to
	 * serialize temp files?  Would the files themselves be wrapped-up into the serialized form?
	 * Would just the filenames be serialized, assuming the underlying temp files are available
	 * to all servlet containers that might get the session?
	 * </p>
	 *
	 * @throws  IllegalStateException  if the temp files have not been added to the session.
	 */
	public static TempFileContext get(HttpSession session) throws IllegalStateException {
		HttpSessionTempFileContext wrapper = (HttpSessionTempFileContext)session.getAttribute(SESSION_ATTRIBUTE);
		if(wrapper == null) throw new IllegalStateException(HttpSessionTempFileContext.class.getName() + " not added to HttpSession; please use Servlet 3.0+ specification or manually add listener to web.xml.");
		TempFileContext tempFiles = wrapper.tempFiles;
		if(tempFiles == null) throw new IllegalStateException(HttpSessionTempFileContext.class.getName() + ".tempFiles is null");
		return tempFiles;
	}

	private TempFileContextEE() {}
}
