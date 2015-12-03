package com.texastoc.dao;

import java.util.Collections;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class BaseJDBCTemplateDao extends NamedParameterJdbcDaoSupport {

    public static final Map<String,String> EMPTY_PARAM_MAP = Collections.emptyMap();

    protected NamedParameterJdbcTemplate getTemplate() {
        return this.getNamedParameterJdbcTemplate();
    }

}