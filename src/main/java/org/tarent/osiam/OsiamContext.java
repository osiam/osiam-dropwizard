package org.tarent.osiam;

import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.oauth.AccessToken;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.GroupRef;
import org.osiam.resources.scim.SCIMSearchResult;
import org.osiam.resources.scim.UpdateGroup;
import org.osiam.resources.scim.UpdateUser;
import org.osiam.resources.scim.User;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import static org.tarent.osiam.OsiamTools.isMember;

/**
 * OSIAM Context class. Wraps the OSIAM connector and AccessToken for convenience.
 */
public class OsiamContext {

    private final OsiamConnector connector;
    private AccessToken accessToken;

    /**
     * Creates a new OSIAM context
     *
     * @param connector OSIAM connector
     * @param at        AccessToken
     */
    public OsiamContext(OsiamConnector connector, AccessToken at) {
        this.connector = connector;
        this.accessToken = at;
    }

    /**
     * Creates a filter expression to search for username.
     *
     * @param userName username
     * @return filter expression
     */
    public static String createUserFilter(String userName) {
        try {
            return "filter=" + URLEncoder.encode("userName eq \"" + userName + "\"", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new OsiamDropwizardRuntimeException(e);
        }
    }

    /**
     * Creates a filter expression to search for a group.
     *
     * @param groupName group name
     * @return filter expression
     */
    public static String createGroupFilter(String groupName) {
        try {
            return "filter=" + URLEncoder.encode("displayName eq \"" + groupName + "\"", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new OsiamDropwizardRuntimeException(e);
        }
    }

    /**
     * Gets the access token
     *
     * @return AccessToken
     */
    public AccessToken getAccessToken() {
        return accessToken;
    }

    /**
     * Gets the current user
     *
     * @return User object
     */
    public User getCurrentUser() {
        return connector.getCurrentUser(accessToken);
    }

    /**
     * Gets all users.
     *
     * @return List of users
     */
    public List<User> getAllUsers() {
        return connector.getAllUsers(accessToken);
    }

    /**
     * Gets the user with the username
     *
     * @param username username
     * @return User object or null if no or more than one user with that username is found
     */
    public User getUser(String username) {

        SCIMSearchResult<User> searchResult = connector.searchUsers(createUserFilter(username), accessToken);
        List<User> result = searchResult.getResources();
        if (result.size() == 1) {
            return result.get(0);
        }


        return null;
    }

    /**
     * Creates a new user.
     *
     * @param user user to create
     * @return User object or null if user with that username already exists or username in User object is null
     */
    public User createUser(User user) {

        if (getUser(user.getUserName()) != null) {
            return null;
        }

        return connector.createUser(user, accessToken);
    }

    /**
     * Update an existing user.
     *
     * @param user User object
     * @return updated user
     */
    public User updateUser(String userName, UpdateUser user) {
        User oldUser = getUser(userName);
        return connector.updateUser(oldUser.getId(), user, accessToken);
    }

    /**
     * Deletes a user.
     *
     * @param username user name
     */
    public void deleteUser(String username) {
        connector.deleteUser(getUser(username).getId(), accessToken);
    }

    /**
     * Gets all groups
     *
     * @return List of Group objects
     */
    public List<Group> getAllGroups() {
        return connector.getAllGroups(accessToken);
    }

    /**
     * Gets a single group object
     *
     * @param groupName group name
     * @return Group object or null if group does not exist or more than one group with that name exist.
     */
    public Group getGroup(String groupName) {
        List<Group> result = connector.searchGroups(createGroupFilter(groupName), accessToken).getResources();
        if (result.size() == 1) {
            return result.get(0);
        }

        return null;
    }

    /**
     * Adds a user to a group
     *
     * @param username  user name
     * @param groupName group name
     */
    public boolean addUserToGroup(String username, String groupName) {
        User user = getUser(username);
        Group group = getGroup(groupName);
        if (!isMember(user, groupName) && group != null) {
            UpdateGroup ug = new UpdateGroup.Builder().addMember(user.getId()).build();
            connector.updateGroup(group.getId(), ug, accessToken);
            return true;
        }
        return false;
    }

    /**
     * Removes a user from a group
     *
     * @param username  user name
     * @param groupName group name
     */
    public boolean removeUserFromGroup(String username, String groupName) {
        User user = getUser(username);
        Group group = getGroup(groupName);
        if (isMember(user, groupName)) {
            UpdateGroup ug = new UpdateGroup.Builder().deleteMember(user.getId()).build();
            connector.updateGroup(group.getId(), ug, accessToken);
            return true;
        }
        return false;
    }

    /**
     * Creates a new Group.
     *
     * @param group new group to create
     * @return newly created group or null if group exists or display name is null
     */
    public Group createGroup(Group group) {
        if (group.getDisplayName() == null) {
            return null;
        }
        if (getGroup(group.getDisplayName()) != null) {
            return null;
        }
        return connector.createGroup(group, accessToken);
    }

    /**
     * Gets all users in a group or all users not in a group.
     *
     * @param groupName name of group
     * @param invert    invert the search
     * @return List of User
     */
    public List<User> getUsersWithGroup(String groupName, boolean invert) {
        List<User> users = getAllUsers();

        for (Iterator<User> i = users.iterator(); i.hasNext(); ) {
            User u = i.next();
            boolean groupFound = false;
            for (GroupRef g : u.getGroups()) {
                if (groupName.equals(g.getDisplay())) {
                    groupFound = true;
                    break;
                }
            }


            if ((!groupFound && !invert) || (groupFound && invert)) {
                i.remove();
            }
        }

        return users;
    }

}
