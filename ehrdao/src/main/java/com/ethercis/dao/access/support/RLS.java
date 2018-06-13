package com.ethercis.dao.access.support;

/**
 * Created by christian on 6/5/2018.
 */
public class RLS {
    private boolean priorityToUser = true; // if set, set the session owner to user, if present, regardless of role.
    private String user;
    private String[] roles;

    public RLS(boolean priorityToUser, String user, String[] roles) {
        this.priorityToUser = priorityToUser;
        this.user = user;
        this.roles = roles;
    }

    public void setRole(String user, String[] roles) {
        //get the session user if any
//        I_SessionClientProperties properties = queryUnit.getParameters();
//        if (properties.getClientProperty(Constants.TOKEN_USER_SESSION) != null) {
//            String user = queryUnit.getParameters().getClientProperty(Constants.TOKEN_USER_SESSION).getStringValue();
//            //set the session user accordingly
//            try {
//                domainAccess.getContext().query("SET ROLE '" + user + "'").execute();
//            }
//            catch (Exception e){
//
//            }
//        }
    }

    public void resetRole() {
        //reset session user if any
//        resourceService.getDomainAccess().getContext().query("RESET ROLE").execute();
    }


}
