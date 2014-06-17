package org.tarent.osiam;

import org.junit.Before;
import org.junit.Test;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.oauth.AccessToken;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.GroupRef;
import org.osiam.resources.scim.SCIMSearchResult;
import org.osiam.resources.scim.User;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: mley
 * Date: 08.04.14
 * Time: 08:22
 * To change this template use File | Settings | File Templates.
 */
public class OsiamContextTest {

    OsiamContext oc;
    OsiamConnector con;
    User user;

    @Before
    public void setup() {
        con = mock(OsiamConnector.class);
        AccessToken at = mock(AccessToken.class);
        oc = new OsiamContext(con, at);

        user = new User.Builder("user").setId("userid").build();
        List<User> users = new ArrayList<User>();
        users.add(user);
        SCIMSearchResult<User> result = mock(SCIMSearchResult.class);
        when(result.getResources()).thenReturn(users);
        when(con.searchUsers(eq(OsiamContext.createUserFilter("user")), any(AccessToken.class))).thenReturn(result);
    }

    @Test
    public void testGetUser() {
        User u2 = oc.getUser("user");
        assertTrue(u2 == user);
    }

    @Test
    public void testCreateUser() {
        assertNull(oc.createUser(user));

        User newUser = new User.Builder("new user").build();

        when(con.createUser(eq(newUser), any(AccessToken.class))).thenReturn(newUser);

        List<User> users = new ArrayList<User>();
        SCIMSearchResult<User> result = mock(SCIMSearchResult.class);
        when(result.getResources()).thenReturn(users);
        when(con.searchUsers(eq(OsiamContext.createUserFilter("new user")), any(AccessToken.class))).thenReturn(result);


        assertTrue(oc.createUser(newUser) == newUser);
    }

    @Test
    public void testGetGroup() {
        List<Group> groups = new ArrayList<Group>();
        groups.add(new Group.Builder("group").build());
        SCIMSearchResult<Group> result = mock(SCIMSearchResult.class);
        when(result.getResources()).thenReturn(groups);
        when(con.searchGroups(eq(OsiamContext.createGroupFilter("group")), any(AccessToken.class))).thenReturn(result);

        oc.getGroup("group");
    }

    @Test
    public void testAddUserToGroup() {
        List<Group> groups = new ArrayList<Group>();
        SCIMSearchResult<Group> result = mock(SCIMSearchResult.class);
        when(result.getResources()).thenReturn(groups);
        when(con.searchGroups(eq(OsiamContext.createGroupFilter("group")), any(AccessToken.class))).thenReturn(result);

        // adding user to non existing group shall return false
        assertFalse(oc.addUserToGroup("user", "group"));


        groups.add(new Group.Builder("group").build());

        // group exists, and user is not member in group
        assertTrue(oc.addUserToGroup("user", "group"));

        List<GroupRef> groupRefs = new ArrayList<GroupRef>();
        groupRefs.add(new GroupRef.Builder().setDisplay("group").build());
        User groupUser = new User.Builder("user").setGroups(groupRefs).build();

        List<User> users = new ArrayList<User>();
        users.add(groupUser);
        SCIMSearchResult<User> result2 = mock(SCIMSearchResult.class);
        when(result2.getResources()).thenReturn(users);
        when(con.searchUsers(eq(OsiamContext.createUserFilter("user")), any(AccessToken.class))).thenReturn(result2);

        // group exists, but user is already member
        assertFalse(oc.addUserToGroup("user", "group"));
    }

    @Test
    public void testRemoveUserFromGroup() {
        List<Group> groups = new ArrayList<Group>();
        groups.add(new Group.Builder("group").build());
        SCIMSearchResult<Group> result = mock(SCIMSearchResult.class);
        when(result.getResources()).thenReturn(groups);
        when(con.searchGroups(eq(OsiamContext.createGroupFilter("group")), any(AccessToken.class))).thenReturn(result);

        assertFalse(oc.removeUserFromGroup("user", "group"));

        List<GroupRef> groupRefs = new ArrayList<GroupRef>();
        groupRefs.add(new GroupRef.Builder().setDisplay("group").build());
        User groupUser = new User.Builder("user").setId("userid").setGroups(groupRefs).build();

        List<User> users = new ArrayList<User>();
        users.add(groupUser);
        SCIMSearchResult<User> result2 = mock(SCIMSearchResult.class);
        when(result2.getResources()).thenReturn(users);
        when(con.searchUsers(eq(OsiamContext.createUserFilter("user")), any(AccessToken.class))).thenReturn(result2);

        assertTrue(oc.removeUserFromGroup("user", "group"));

    }

    @Test
    public void testCreateGroup() {
        List<Group> groups = new ArrayList<Group>();
        SCIMSearchResult<Group> result = mock(SCIMSearchResult.class);
        when(result.getResources()).thenReturn(groups);
        when(con.searchGroups(eq(OsiamContext.createGroupFilter("group")), any(AccessToken.class))).thenReturn(result);


        Group group = new Group.Builder("group").build();

        when(con.createGroup(eq(group), any(AccessToken.class))).thenReturn(group);

        // if group does not exist, we can create it and new group is returned
        assertEquals(group, oc.createGroup(group));

        groups.add(group);
        // if group already exists, null is returned.
        assertNull(oc.createGroup(group));
    }

    @Test
    public void testGetUsersInGroup() {
        List<User> users = new ArrayList<User>();

        List<GroupRef> groupA = new ArrayList<GroupRef>();
        groupA.add(new GroupRef.Builder().setDisplay("groupA").build());

        List<GroupRef> groupB = new ArrayList<GroupRef>();
        groupB.add(new GroupRef.Builder().setDisplay("groupB").build());

        users.add(new User.Builder("user1").setGroups(groupA).build());
        users.add(new User.Builder("user2").setGroups(groupB).build());

        when(con.getAllUsers(any(AccessToken.class))).thenReturn(new ArrayList(users));

        List<User> result = oc.getUsersWithGroup("groupA", false);
        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getUserName());

        when(con.getAllUsers(any(AccessToken.class))).thenReturn(new ArrayList(users));

        result = oc.getUsersWithGroup("groupA", true);
        assertEquals(1, result.size());
        assertEquals("user2", result.get(0).getUserName());
    }
}
