/*
 * ao-tempfiles-servlet - Temporary file management in a Servlet environment.
 * Copyright (C) 2017, 2019, 2020, 2021, 2022, 2025, 2026  AO Industries, Inc.
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
 * along with ao-tempfiles-servlet.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aoapps.tempfiles.servlet;

import com.aoapps.servlet.attribute.ScopeEE;
import com.aoapps.tempfiles.TempFileContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import java.io.IOException;

/**
 * Initializes {@link TempFileContextEE}.
 */
@WebListener
public class Initializer implements
    ServletContextListener,
    ServletRequestListener,
    HttpSessionListener {

  @Override
  public void contextInitialized(ServletContextEvent event) {
    ServletContext servletContext = event.getServletContext();
    assert TempFileContextEE.ATTRIBUTE.context(servletContext).get() == null;
    TempFileContextEE.ATTRIBUTE.context(servletContext).set(
        new TempFileContext(
            ScopeEE.Application.TEMPDIR.context(servletContext).get()
        )
    );
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    ServletContext servletContext = event.getServletContext();
    TempFileContext tempFiles = TempFileContextEE.ATTRIBUTE.context(servletContext).get();
    if (tempFiles != null) {
      try {
        tempFiles.close();
      } catch (IOException e) {
        servletContext.log("Error deleting temporary files", e);
      }
    }
  }

  @Override
  public void requestInitialized(ServletRequestEvent event) {
    ServletRequest request = event.getServletRequest();
    assert TempFileContextEE.ATTRIBUTE.context(request).get() == null;
    TempFileContextEE.ATTRIBUTE.context(request).set(
        new TempFileContext(
            ScopeEE.Application.TEMPDIR.context(event.getServletContext()).get()
        )
    );
  }

  @Override
  public void requestDestroyed(ServletRequestEvent event) {
    ServletRequest request = event.getServletRequest();
    TempFileContext tempFiles = TempFileContextEE.ATTRIBUTE.context(request).get();
    if (tempFiles != null) {
      try {
        tempFiles.close();
      } catch (IOException e) {
        event.getServletContext().log("Error deleting temporary files", e);
      }
    }
  }

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    HttpSession session = event.getSession();
    assert TempFileContextEE.SESSION_ATTRIBUTE_INT.context(session).get() == null;
    TempFileContextEE.SESSION_ATTRIBUTE_INT.context(session).set(
        new HttpSessionTempFileContext(
            session.getServletContext()
        )
    );
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    HttpSession session = event.getSession();
    HttpSessionTempFileContext wrapper = TempFileContextEE.SESSION_ATTRIBUTE_INT.context(session).get();
    if (wrapper != null) {
      try {
        wrapper.close();
      } catch (IOException e) {
        session.getServletContext().log("Error deleting temporary files", e);
      }
    }
  }
}
