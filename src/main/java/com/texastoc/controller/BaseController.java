package com.texastoc.controller;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

public abstract class BaseController {
    
    static final Logger logger = Logger.getLogger(BaseController.class);
    
    protected static final String USER_LOGGED_IN = "userLoggedIn";
    protected static final String USER_READ_ONLY = "userReadOnly";
    
    protected boolean isLoggedIn(final HttpServletRequest request) {
        return request.getSession().getAttribute(USER_LOGGED_IN) != null;
    }

    protected boolean isNotLoggedIn(final HttpServletRequest request) {
        return request.getSession().getAttribute(USER_LOGGED_IN) == null;
    }

    protected String getLoggedIn(final HttpServletRequest request) {
        return (String)request.getSession().getAttribute(USER_LOGGED_IN);
    }

    protected boolean isAdminLoggedIn(final HttpServletRequest request) {
         Object obj = request.getSession().getAttribute(USER_LOGGED_IN);
         if (obj !=  null) {
             String loggedin = (String)obj;
             if (StringUtils.equals(loggedin, "admin")) {
                 return true;
             }
         }
         return false;
    }

    protected boolean isReadOnly(final HttpServletRequest request) {
        return request.getSession().getAttribute(USER_READ_ONLY) != null;
    }


    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ModelAndView handleParameterErrors(final Exception exception, final HttpServletRequest request,
            final HttpServletResponse resp) {
        logger.warn(exception.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        logger.warn(sw.toString());
        return new ModelAndView("error", "message", exception.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public ModelAndView handleDefaultErrors(final Exception exception, final HttpServletRequest request,
            final HttpServletResponse resp) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        logger.warn(sw.toString());
        return new ModelAndView("error", "message", exception.getMessage());
    }
}
