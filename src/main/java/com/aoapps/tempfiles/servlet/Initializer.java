/*
 * ao-tempfiles-servlet - Temporary file management in a Servlet environment.
 * Copyright (C) 2017, 2019, 2020, 2021  AO Industries, Inc.
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
package com.aoapps.tempfiles.servlet;

import com.aoapps.tempfiles.TempFileContext;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
public class Initializer implements
	ServletContextListener,
	ServletRequestListener,
	HttpSessionListener
{

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext servletContext = event.getServletContext();
		assert servletContext.getAttribute(TempFileContextEE.ATTRIBUTE) == null;
		servletContext.setAttribute(
			TempFileContextEE.ATTRIBUTE,
			new TempFileContext(
				(File)servletContext.getAttribute(ServletContext.TEMPDIR)
			)
		);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		ServletContext servletContext = event.getServletContext();
		TempFileContext tempFiles = (TempFileContext)servletContext.getAttribute(TempFileContextEE.ATTRIBUTE);
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
		assert request.getAttribute(TempFileContextEE.ATTRIBUTE) == null;
		request.setAttribute(
			TempFileContextEE.ATTRIBUTE,
			new TempFileContext(
				(File)event.getServletContext().getAttribute(ServletContext.TEMPDIR)
			)
		);
	}

	@Override
	public void requestDestroyed(ServletRequestEvent event) {
		ServletRequest request = event.getServletRequest();
		TempFileContext tempFiles = (TempFileContext)request.getAttribute(TempFileContextEE.ATTRIBUTE);
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
		assert session.getAttribute(TempFileContextEE.SESSION_ATTRIBUTE) == null;
		session.setAttribute(TempFileContextEE.SESSION_ATTRIBUTE,
			new HttpSessionTempFileContext(
				session.getServletContext()
			)
		);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		HttpSessionTempFileContext wrapper = (HttpSessionTempFileContext)session.getAttribute(TempFileContextEE.SESSION_ATTRIBUTE);
		if(wrapper != null) {
			try {
				wrapper.close();
			} catch(IOException e) {
				session.getServletContext().log("Error deleting temporary files", e);
			}
		}
	}
}
