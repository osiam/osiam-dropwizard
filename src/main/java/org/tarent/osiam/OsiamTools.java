package org.tarent.osiam;

import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.GroupRef;
import org.osiam.resources.scim.User;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mley
 * Date: 03.04.14
 * Time: 09:32
 * To change this template use File | Settings | File Templates.
 */
public final class OsiamTools {

    /**
     * Private constructor.
     */
    private OsiamTools() {

    }

    /**
     * Test if a user is member in a group
     *
     * @param u         user
     * @param groupName group name
     * @return true of user is member of the group
     */
    public static boolean isMember(User u, String groupName) {
        for (GroupRef g : u.getGroups()) {
            if (groupName.equals(g.getDisplay())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if the list contains a user with the same username
     *
     * @param users List of Users
     * @param user  user
     * @return true if the List contains a user with the same username
     */
    public static boolean contains(List<User> users, User user) {
        for (User u2 : users) {
            if (user.getUserName().equals(u2.getUserName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if the List of groups contains a group with the same group name
     *
     * @param list List of Groups
     * @param g    group
     * @return true if List of groups contains a group with the same name
     */
    public static boolean contains(List<Group> list, Group g) {
        for (Group g2 : list) {
            if (g.getDisplayName().equals(g2.getDisplayName())) {
                return true;
            }
        }
        return false;
    }
}
