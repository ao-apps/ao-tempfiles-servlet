/*
 * ao-tempfiles-servlet - Temporary file management in a Servlet environment.
 * Copyright (C) 2017  AO Industries, Inc.
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
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * A {@link TempFileContext} that automatically deletes temp files when the
 * {@link ServletContext} is destroyed.
 */
@WebListener
public class ServletContextTempFiles implements ServletContextListener {

	private static final String APPLICATION_ATTRIBUTE_NAME = ServletContextTempFiles.class.getName() + ".instance";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext servletContext = sce.getServletContext();
		servletContext.setAttribute(
			APPLICATION_ATTRIBUTE_NAME,
			new TempFileContext(
				(File)servletContext.getAttribute(ServletContext.TEMPDIR)
			)
		);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ServletContext servletContext = sce.getServletContext();
		TempFileContext tempFiles = (TempFileContext)servletContext.getAttribute(APPLICATION_ATTRIBUTE_NAME);
		if(tempFiles != null) {
			try {
				tempFiles.close();
			} catch(IOException e) {
				servletContext.log("Error deleting temporary files", e);
			}
		}
	}

	/**
	 * Gets the {@link TempFileContext temp file context} for the given {@link ServletContext servlet context}.
	 *
	 * @throws  IllegalStateException  if the temp files have not been added to the servlet context.
	 */
	public static TempFileContext getTempFileContext(ServletContext servletContext) throws IllegalStateException {
		TempFileContext tempFiles = (TempFileContext)servletContext.getAttribute(APPLICATION_ATTRIBUTE_NAME);
		if(tempFiles == null) throw new IllegalStateException(ServletContextTempFiles.class.getName() + " not added to ServletContext; please use Servlet 3.0+ specification or manually add listener to web.xml.");
		return tempFiles;
	}
}
