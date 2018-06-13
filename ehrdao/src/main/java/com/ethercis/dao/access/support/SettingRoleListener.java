package com.ethercis.dao.access.support;

import org.jooq.ExecuteContext;
import org.jooq.impl.DefaultExecuteListener;

/**
 * Created by christian on 6/5/2018.
 */
public class SettingRoleListener extends DefaultExecuteListener {

    private boolean priorityToUser = true; // if set, set the session owner to user, if present, regardless of role.
    private String user;
    private String[] roles;

    public SettingRoleListener(boolean priorityToUser, String user, String[] roles) {
        this.priorityToUser = priorityToUser;
        this.user = user;
        this.roles = roles;
    }

    @Override
    public void renderEnd(ExecuteContext executeContext) {
        executeContext.sql(executeContext.sql());
    }

}
