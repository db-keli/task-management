package org.example.services;

import org.example.exceptions.InvalidEmailException;
import org.example.exceptions.InvalidRoleException;
import org.example.models.AdminUser;
import org.example.models.RegularUser;
import org.example.models.User;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTest {
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
    }

    @Test
    public void testCreateUser_RegularUser() throws InvalidEmailException {
        User user = userService.createUser("John Doe", "john@example.com", false);

        assertNotNull(user);
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertNotNull(user.getId());
        assertTrue(user instanceof RegularUser);
        assertFalse(user.canManageUsers());
    }

    @Test
    public void testCreateUser_AdminUser() throws InvalidEmailException {
        User user = userService.createUser("Admin User", "admin2@example.com", true);

        assertNotNull(user);
        assertEquals("Admin User", user.getName());
        assertEquals("admin2@example.com", user.getEmail());
        assertTrue(user instanceof AdminUser);
        assertTrue(user.canManageUsers());
    }

    @Test
    public void testCreateUser_InvalidEmail() {
        assertThrows(InvalidEmailException.class,
                () -> userService.createUser("John", "invalid-email", false));

        assertThrows(InvalidEmailException.class,
                () -> userService.createUser("John", "", false));

        assertThrows(InvalidEmailException.class,
                () -> userService.createUser("John", null, false));
    }

    @Test
    public void testAddUser() throws InvalidEmailException {
        User user = userService.createUser("Jane Doe", "jane@example.com", false);
        boolean added = userService.addUser(user);

        assertTrue(added);

        User retrieved = userService.getUserById(user.getId());
        assertNotNull(retrieved);
        assertEquals(user.getId(), retrieved.getId());
    }

    @Test
    public void testAddUser_DuplicateEmail() throws InvalidEmailException {
        User user1 = userService.createUser("User 1", "same@example.com", false);
        userService.addUser(user1);

        User user2 = userService.createUser("User 2", "same@example.com", false);
        assertThrows(InvalidEmailException.class, () -> userService.addUser(user2));
    }

    @Test
    public void testAddUser_NullUser() throws InvalidEmailException {
        boolean added = userService.addUser(null);
        assertFalse(added);
    }

    @Test
    public void testGetUserById_Exists() throws InvalidEmailException {
        User user = userService.createUser("John", "john2@example.com", false);
        userService.addUser(user);

        User retrieved = userService.getUserById(user.getId());
        assertNotNull(retrieved);
        assertEquals(user.getId(), retrieved.getId());
        assertEquals("John", retrieved.getName());
    }

    @Test
    public void testGetUserById_NotExists() {
        User retrieved = userService.getUserById("U999");
        assertNull(retrieved);
    }

    @Test
    public void testGetUserById_Null() {
        User retrieved = userService.getUserById(null);
        assertNull(retrieved);
    }

    @Test
    public void testGetUserByEmail_Exists() throws InvalidEmailException {
        User user = userService.createUser("Test", "test@example.com", false);
        userService.addUser(user);

        User retrieved = userService.getUserByEmail("test@example.com");
        assertNotNull(retrieved);
        assertEquals("test@example.com", retrieved.getEmail());
    }

    @Test
    public void testGetUserByEmail_NotExists() {
        User retrieved = userService.getUserByEmail("notfound@example.com");
        assertNull(retrieved);
    }

    @Test
    public void testGetUserByEmail_Null() {
        User retrieved = userService.getUserByEmail(null);
        assertNull(retrieved);
    }

    @Test
    public void testGetUserByEmail_CaseInsensitive() throws InvalidEmailException {
        User user = userService.createUser("Test", "Case@Example.COM", false);
        userService.addUser(user);

        User retrieved = userService.getUserByEmail("case@example.com");
        assertNotNull(retrieved);
        assertEquals("Case@Example.COM", retrieved.getEmail());
    }

    @Test
    public void testGetAllUsers() throws InvalidEmailException {
        int initialCount = userService.getAllUsers().length;

        User user1 = userService.createUser("User 1", "user1@example.com", false);
        User user2 = userService.createUser("User 2", "user2@example.com", true);

        userService.addUser(user1);
        userService.addUser(user2);

        User[] allUsers = userService.getAllUsers();

        assertEquals(initialCount + 2, allUsers.length);
    }

    @Test
    public void testSwitchUser_Success() throws InvalidEmailException {
        User user = userService.createUser("Test User", "testswitch@example.com", false);
        userService.addUser(user);

        boolean switched = userService.switchUser("testswitch@example.com");
        assertTrue(switched);

        User currentUser = userService.getCurrentUser();
        assertNotNull(currentUser);
        assertEquals(user.getId(), currentUser.getId());
    }

    @Test
    public void testSwitchUser_InvalidEmail() {
        boolean switched = userService.switchUser("notfound@example.com");
        assertFalse(switched);
    }

    @Test
    public void testSwitchUser_NullEmail() {
        boolean switched = userService.switchUser(null);
        assertFalse(switched);
    }

    @Test
    public void testSwitchUser_CaseInsensitive() throws InvalidEmailException {
        User user = userService.createUser("Test", "Switch@Example.COM", false);
        userService.addUser(user);

        boolean switched = userService.switchUser("switch@example.com");
        assertTrue(switched);
        assertEquals(user.getId(), userService.getCurrentUser().getId());
    }

    @Test
    public void testSetCurrentUser() throws InvalidEmailException {
        User user = userService.createUser("Current", "current@example.com", false);
        userService.addUser(user);

        userService.setCurrentUser(user);

        User currentUser = userService.getCurrentUser();
        assertNotNull(currentUser);
        assertEquals(user.getId(), currentUser.getId());
    }

    @Test
    public void testSetCurrentUser_Null() {
        userService.setCurrentUser(null);
        assertNull(userService.getCurrentUser());
    }

    @Test
    public void testGetCurrentUser_Default() {
        User currentUser = userService.getCurrentUser();
        assertNotNull(currentUser);
        assertTrue(currentUser instanceof AdminUser);
    }

    @Test
    public void testDeleteUser_Success() throws InvalidEmailException {
        User user = userService.createUser("Delete Me", "delete@example.com", false);
        userService.addUser(user);

        int countBefore = userService.getAllUsers().length;
        boolean deleted = userService.deleteUser("delete@example.com");

        assertTrue(deleted);
        assertEquals(countBefore - 1, userService.getAllUsers().length);
        assertNull(userService.getUserByEmail("delete@example.com"));
    }

    @Test
    public void testDeleteUser_CurrentUser() throws InvalidEmailException {
        User user = userService.createUser("Current", "current2@example.com", false);
        userService.addUser(user);
        userService.switchUser("current2@example.com");

        boolean deleted = userService.deleteUser("current2@example.com");
        assertFalse(deleted);

        assertNotNull(userService.getUserByEmail("current2@example.com"));
    }

    @Test
    public void testDeleteUser_NotExists() {
        boolean deleted = userService.deleteUser("notfound@example.com");
        assertFalse(deleted);
    }

    @Test
    public void testDeleteUser_Null() {
        boolean deleted = userService.deleteUser(null);
        assertFalse(deleted);
    }

    @Test
    public void testDefaultUsers() {
        User[] allUsers = userService.getAllUsers();
        assertTrue(allUsers.length >= 2);

        boolean hasAdmin = false;
        boolean hasRegular = false;

        for (User user : allUsers) {
            if (user instanceof AdminUser) {
                hasAdmin = true;
            }
            if (user instanceof RegularUser) {
                hasRegular = true;
            }
        }

        assertTrue(hasAdmin);
        assertTrue(hasRegular);
    }

    @Test
    public void testMultipleUsersCreation() throws InvalidEmailException {
        int initialCount = userService.getAllUsers().length;

        for (int i = 1; i <= 10; i++) {
            User user = userService.createUser("User " + i, "user" + i + "@example.com",
                    i % 2 == 0);
            userService.addUser(user);
        }

        User[] allUsers = userService.getAllUsers();
        assertEquals(initialCount + 10, allUsers.length);
    }

    @Test
    public void testEmailValidation_ValidFormats() {
        assertDoesNotThrow(
                () -> userService.createUser("User", "user@domain.com", false));
        assertDoesNotThrow(
                () -> userService.createUser("User", "user.name@domain.com", false));
        assertDoesNotThrow(
                () -> userService.createUser("User", "user+tag@domain.co.uk", false));
    }

    @Test
    public void testEmailValidation_InvalidFormats() {
        assertThrows(InvalidEmailException.class,
                () -> userService.createUser("User", "user", false));
        assertThrows(InvalidEmailException.class,
                () -> userService.createUser("User", "user@", false));
        assertThrows(InvalidEmailException.class,
                () -> userService.createUser("User", "@domain.com", false));
        assertThrows(InvalidEmailException.class,
                () -> userService.createUser("User", "user@domain", false));
    }

    @Test
    public void testValidateEmail_Valid() throws InvalidEmailException {
        assertDoesNotThrow(() -> userService.validateEmail("valid@example.com"));
        assertDoesNotThrow(() -> userService.validateEmail("user.name@example.co.uk"));
        assertDoesNotThrow(() -> userService.validateEmail("user+tag@domain.com"));
    }

    @Test
    public void testValidateEmail_Invalid() {
        assertThrows(InvalidEmailException.class, () -> userService.validateEmail(null));
        assertThrows(InvalidEmailException.class, () -> userService.validateEmail(""));
        assertThrows(InvalidEmailException.class, () -> userService.validateEmail("   "));
        assertThrows(InvalidEmailException.class, () -> userService.validateEmail("notanemail"));
        assertThrows(InvalidEmailException.class, () -> userService.validateEmail("@example.com"));
    }

    @Test
    public void testValidateRole_Valid() throws InvalidRoleException {
        assertTrue(userService.validateRole("admin"));
        assertTrue(userService.validateRole("ADMIN"));
        assertTrue(userService.validateRole("adminuser"));
        assertTrue(userService.validateRole("ADMINUSER"));
        assertFalse(userService.validateRole("regular"));
        assertFalse(userService.validateRole("REGULAR"));
        assertFalse(userService.validateRole("regularuser"));
        assertFalse(userService.validateRole("REGULARUSER"));
    }

    @Test
    public void testValidateRole_Invalid() {
        assertThrows(InvalidRoleException.class, () -> userService.validateRole(null));
        assertThrows(InvalidRoleException.class, () -> userService.validateRole(""));
        assertThrows(InvalidRoleException.class, () -> userService.validateRole("   "));
        assertThrows(InvalidRoleException.class, () -> userService.validateRole("invalid"));
        assertThrows(InvalidRoleException.class, () -> userService.validateRole("superadmin"));
    }

    @Test
    public void testUserIdGeneration() throws InvalidEmailException {
        User user1 = userService.createUser("User 1", "uid1@example.com", false);
        User user2 = userService.createUser("User 2", "uid2@example.com", false);

        assertNotNull(user1.getId());
        assertNotNull(user2.getId());
        assertNotEquals(user1.getId(), user2.getId());
        assertTrue(user1.getId().startsWith("U"));
        assertTrue(user2.getId().startsWith("U"));
    }

    @Test
    public void testUserIdAutoAssignment() throws InvalidEmailException {
        User user = userService.createUser("Test", "autoid@example.com", false);

        assertNotNull(user.getId());
        assertTrue(user.getId().startsWith("U"));

        userService.addUser(user);

        assertNotNull(user.getId());
        assertTrue(user.getId().startsWith("U"));
    }

    @Test
    public void testSwitchUserSequence() throws InvalidEmailException {
        User user1 = userService.createUser("User 1", "seq1@example.com", false);
        User user2 = userService.createUser("User 2", "seq2@example.com", true);

        userService.addUser(user1);
        userService.addUser(user2);

        userService.switchUser("seq1@example.com");
        assertEquals(user1.getId(), userService.getCurrentUser().getId());

        userService.switchUser("seq2@example.com");
        assertEquals(user2.getId(), userService.getCurrentUser().getId());

        userService.setCurrentUser(null);
        assertNull(userService.getCurrentUser());

        userService.switchUser("seq1@example.com");
        assertEquals(user1.getId(), userService.getCurrentUser().getId());
    }

    @Test
    public void testAdminUserPermissions() throws InvalidEmailException {
        User admin = userService.createUser("Admin", "adminperm@example.com", true);
        assertTrue(admin.canManageUsers());
        assertTrue(admin instanceof AdminUser);
    }

    @Test
    public void testRegularUserPermissions() throws InvalidEmailException {
        User regular = userService.createUser("Regular", "regularperm@example.com", false);
        assertFalse(regular.canManageUsers());
        assertTrue(regular instanceof RegularUser);
    }

    @Test
    public void testArrayCapacity() throws InvalidEmailException {
        int initialCount = userService.getAllUsers().length;
        int maxCapacity = 100;
        int usersToAdd = maxCapacity - initialCount;

        for (int i = 0; i < usersToAdd; i++) {
            User user = userService.createUser("User " + i, "capacity" + i + "@example.com", false);
            boolean added = userService.addUser(user);
            if (i < usersToAdd) {
                assertTrue(added);
            }
        }

        User extraUser = userService.createUser("Extra", "extra@example.com", false);
        boolean added = userService.addUser(extraUser);
        assertFalse(added);
    }
}
