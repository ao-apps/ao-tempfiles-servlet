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
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

/**
 * A {@link TempFileContext} that automatically deletes temp files when the
 * {@link ServletRequest} is destroyed.
 */
@WebListener
public class ServletRequestTempFiles implements ServletRequestListener {

	private static final String REQUEST_ATTRIBUTE_NAME = ServletRequestTempFiles.class.getName() + ".instance";

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		ServletRequest request = sre.getServletRequest();
		request.setAttribute(
			REQUEST_ATTRIBUTE_NAME,
			new TempFileContext(
				(File)sre.getServletContext().getAttribute(ServletContext.TEMPDIR)
			)
		);
	}

	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		ServletRequest request = sre.getServletRequest();
		TempFileContext tempFiles = (TempFileContext)request.getAttribute(REQUEST_ATTRIBUTE_NAME);
		if(tempFiles != null) {
			try {
				tempFiles.close();
			} catch(IOException e) {
				sre.getServletContext().log("Error deleting temporary files", e);
			}
		}
	}

	/**
	 * Gets the {@link TempFileContext temp file context} for the given {@link ServletRequest servlet request}.
	 *
	 * @throws  IllegalStateException  if the temp files have not been added to the servlet request.
	 */
	public static TempFileContext getTempFileContext(ServletRequest request) throws IllegalStateException {
		TempFileContext tempFiles = (TempFileContext)request.getAttribute(REQUEST_ATTRIBUTE_NAME);
		if(tempFiles == null) throw new IllegalStateException(ServletRequestTempFiles.class.getName() + " not added to ServletRequest; please use Servlet 3.0+ specification or manually add listener to web.xml.");
		return tempFiles;
	}
}
