package org.tarent.osiam;

import org.junit.Test;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.GroupRef;
import org.osiam.resources.scim.User;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: mley
 * Date: 03.04.14
 * Time: 14:17
 * To change this template use File | Settings | File Templates.
 */
public class OsiamToolsTest {

    @Test
    public void testIsMember() {
        List<GroupRef> groups = new ArrayList<GroupRef>();
        groups.add(new GroupRef.Builder().setDisplay("groupA").build());
        groups.add(new GroupRef.Builder().setDisplay("groupB").build());
        User u = new User.Builder("user").setGroups(groups).build();

        assertTrue(OsiamTools.isMember(u, "groupA"));
        assertTrue(OsiamTools.isMember(u, "groupB"));
        assertFalse(OsiamTools.isMember(u, "groupC"));
    }

    @Test
    public void testContainsGroup() {
        Group g = new Group.Builder("group").build();
        List<Group> groups = new ArrayList<Group>();
        groups.add(new Group.Builder("othergroup").build());
        assertFalse(OsiamTools.contains(groups, g));
        groups.add(g);

        assertTrue(OsiamTools.contains(groups, g));
    }

    @Test
    public void testContainsUser() {
        User u = new User.Builder("user").build();
        List<User> users = new ArrayList<User>();
        users.add(new User.Builder("otheruser").build());
        assertFalse(OsiamTools.contains(users, u));
        users.add(u);

        assertTrue(OsiamTools.contains(users, u));
    }
}
