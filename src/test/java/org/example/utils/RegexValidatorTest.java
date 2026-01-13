package org.example.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.example.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

public class RegexValidatorTest {

    @Test
    public void testValidTaskIds() {
        assertTrue(RegexValidator.isValidTaskId("T001"));
        assertTrue(RegexValidator.isValidTaskId("T123"));
        assertTrue(RegexValidator.isValidTaskId("T999"));
    }

    @Test
    public void testInvalidTaskIds() {
        assertFalse(RegexValidator.isValidTaskId(null));
        assertFalse(RegexValidator.isValidTaskId(""));
        assertFalse(RegexValidator.isValidTaskId("T1"));
        assertFalse(RegexValidator.isValidTaskId("T0001"));
        assertFalse(RegexValidator.isValidTaskId("P001"));
        assertFalse(RegexValidator.isValidTaskId("001"));
        assertFalse(RegexValidator.isValidTaskId("TABC"));
        assertFalse(RegexValidator.isValidTaskId("t001"));
        assertFalse(RegexValidator.isValidTaskId("T 001"));
    }

    @Test
    public void testValidateTaskIdThrows() {
        assertDoesNotThrow(() -> RegexValidator.validateTaskId("T001"));
        assertThrows(ValidationException.class, () -> RegexValidator.validateTaskId(null));
        assertThrows(ValidationException.class, () -> RegexValidator.validateTaskId("T1"));
        assertThrows(ValidationException.class, () -> RegexValidator.validateTaskId("INVALID"));
    }

    @Test
    public void testValidProjectIds() {
        assertTrue(RegexValidator.isValidProjectId("P001"));
        assertTrue(RegexValidator.isValidProjectId("P456"));
        assertTrue(RegexValidator.isValidProjectId("P999"));
    }

    @Test
    public void testInvalidProjectIds() {
        assertFalse(RegexValidator.isValidProjectId(null));
        assertFalse(RegexValidator.isValidProjectId(""));
        assertFalse(RegexValidator.isValidProjectId("P1"));
        assertFalse(RegexValidator.isValidProjectId("P0001"));
        assertFalse(RegexValidator.isValidProjectId("T001"));
        assertFalse(RegexValidator.isValidProjectId("p001"));
    }

    @Test
    public void testValidateProjectIdThrows() {
        assertDoesNotThrow(() -> RegexValidator.validateProjectId("P001"));
        assertThrows(ValidationException.class, () -> RegexValidator.validateProjectId(null));
        assertThrows(ValidationException.class, () -> RegexValidator.validateProjectId("P1"));
    }

    @Test
    public void testValidUserIds() {
        assertTrue(RegexValidator.isValidUserId("U001"));
        assertTrue(RegexValidator.isValidUserId("U789"));
        assertTrue(RegexValidator.isValidUserId("U999"));
    }

    @Test
    public void testInvalidUserIds() {
        assertFalse(RegexValidator.isValidUserId(null));
        assertFalse(RegexValidator.isValidUserId(""));
        assertFalse(RegexValidator.isValidUserId("U1"));
        assertFalse(RegexValidator.isValidUserId("U0001"));
        assertFalse(RegexValidator.isValidUserId("P001"));
        assertFalse(RegexValidator.isValidUserId("u001"));
    }

    @Test
    public void testValidateUserIdThrows() {
        assertDoesNotThrow(() -> RegexValidator.validateUserId("U001"));
        assertThrows(ValidationException.class, () -> RegexValidator.validateUserId(null));
        assertThrows(ValidationException.class, () -> RegexValidator.validateUserId("U1"));
    }

    @Test
    public void testValidTaskNames() {
        assertTrue(RegexValidator.isValidTaskName("Task 1"));
        assertTrue(RegexValidator.isValidTaskName("Design-Homepage"));
        assertTrue(RegexValidator.isValidTaskName("Fix_bug_123"));
        assertTrue(RegexValidator.isValidTaskName("a"));
        assertTrue(RegexValidator.isValidTaskName("Task Name With Spaces"));
        assertTrue(RegexValidator.isValidTaskName("Task123"));
        assertTrue(RegexValidator.isValidTaskName("A".repeat(100)));
    }

    @Test
    public void testInvalidTaskNames() {
        assertFalse(RegexValidator.isValidTaskName(null));
        assertFalse(RegexValidator.isValidTaskName(""));
        assertFalse(RegexValidator.isValidTaskName("   "));
        assertFalse(RegexValidator.isValidTaskName("a".repeat(101)));
        assertFalse(RegexValidator.isValidTaskName("Task@Name"));
        assertFalse(RegexValidator.isValidTaskName("Task#Name"));
        assertFalse(RegexValidator.isValidTaskName("Task$Name"));
    }

    @Test
    public void testValidateTaskNameThrows() {
        assertDoesNotThrow(() -> RegexValidator.validateTaskName("Valid Task"));
        assertThrows(ValidationException.class, () -> RegexValidator.validateTaskName(null));
        assertThrows(ValidationException.class, () -> RegexValidator.validateTaskName(""));
        assertThrows(ValidationException.class, () -> RegexValidator.validateTaskName("a".repeat(101)));
    }

    @Test
    public void testValidProjectNames() {
        assertTrue(RegexValidator.isValidProjectName("Project 1"));
        assertTrue(RegexValidator.isValidProjectName("Web-App"));
        assertTrue(RegexValidator.isValidProjectName("Mobile_App_v2"));
        assertTrue(RegexValidator.isValidProjectName("P"));
        assertTrue(RegexValidator.isValidProjectName("A".repeat(100)));
    }

    @Test
    public void testInvalidProjectNames() {
        assertFalse(RegexValidator.isValidProjectName(null));
        assertFalse(RegexValidator.isValidProjectName(""));
        assertFalse(RegexValidator.isValidProjectName("   "));
        assertFalse(RegexValidator.isValidProjectName("a".repeat(101)));
        assertFalse(RegexValidator.isValidProjectName("Project@Name"));
    }

    @Test
    public void testValidateProjectNameThrows() {
        assertDoesNotThrow(() -> RegexValidator.validateProjectName("Valid Project"));
        assertThrows(ValidationException.class, () -> RegexValidator.validateProjectName(null));
        assertThrows(ValidationException.class, () -> RegexValidator.validateProjectName(""));
    }

    @Test
    public void testValidUserNames() {
        assertTrue(RegexValidator.isValidUserName("John Doe"));
        assertTrue(RegexValidator.isValidUserName("Jane"));
        assertTrue(RegexValidator.isValidUserName("Mary Jane"));
        assertTrue(RegexValidator.isValidUserName("A"));
        assertTrue(RegexValidator.isValidUserName("A".repeat(50)));
    }

    @Test
    public void testInvalidUserNames() {
        assertFalse(RegexValidator.isValidUserName(null));
        assertFalse(RegexValidator.isValidUserName(""));
        assertFalse(RegexValidator.isValidUserName("   "));
        assertFalse(RegexValidator.isValidUserName("A".repeat(51)));
        assertFalse(RegexValidator.isValidUserName("John123"));
        assertFalse(RegexValidator.isValidUserName("John_Doe"));
        assertFalse(RegexValidator.isValidUserName("John-Doe"));
    }

    @Test
    public void testValidateUserNameThrows() {
        assertDoesNotThrow(() -> RegexValidator.validateUserName("John Doe"));
        assertThrows(ValidationException.class, () -> RegexValidator.validateUserName(null));
        assertThrows(ValidationException.class, () -> RegexValidator.validateUserName("John123"));
    }

    @Test
    public void testValidEmails() {
        assertTrue(RegexValidator.isValidEmail("user@domain.com"));
        assertTrue(RegexValidator.isValidEmail("john.doe@example.com"));
        assertTrue(RegexValidator.isValidEmail("admin+test@site.org"));
        assertTrue(RegexValidator.isValidEmail("user_123@test-site.co.uk"));
    }

    @Test
    public void testInvalidEmails() {
        assertFalse(RegexValidator.isValidEmail(null));
        assertFalse(RegexValidator.isValidEmail(""));
        assertFalse(RegexValidator.isValidEmail("   "));
        assertFalse(RegexValidator.isValidEmail("user"));
        assertFalse(RegexValidator.isValidEmail("user@"));
        assertFalse(RegexValidator.isValidEmail("@domain.com"));
        assertFalse(RegexValidator.isValidEmail("user@domain"));
        assertFalse(RegexValidator.isValidEmail("user domain@test.com"));
    }

    @Test
    public void testValidateEmailThrows() {
        assertDoesNotThrow(() -> RegexValidator.validateEmail("user@domain.com"));
        assertThrows(ValidationException.class, () -> RegexValidator.validateEmail(null));
        assertThrows(ValidationException.class, () -> RegexValidator.validateEmail("invalid"));
    }

    @Test
    public void testValidPositiveNumbers() {
        assertTrue(RegexValidator.isValidPositiveNumber("0"));
        assertTrue(RegexValidator.isValidPositiveNumber("1"));
        assertTrue(RegexValidator.isValidPositiveNumber("123"));
        assertTrue(RegexValidator.isValidPositiveNumber("123.45"));
        assertTrue(RegexValidator.isValidPositiveNumber("0.5"));
    }

    @Test
    public void testInvalidPositiveNumbers() {
        assertFalse(RegexValidator.isValidPositiveNumber(null));
        assertFalse(RegexValidator.isValidPositiveNumber(""));
        assertFalse(RegexValidator.isValidPositiveNumber("   "));
        assertFalse(RegexValidator.isValidPositiveNumber("-1"));
        assertFalse(RegexValidator.isValidPositiveNumber("abc"));
        assertFalse(RegexValidator.isValidPositiveNumber("12.34.56"));
    }

    @Test
    public void testValidIntegers() {
        assertTrue(RegexValidator.isValidInteger("0"));
        assertTrue(RegexValidator.isValidInteger("1"));
        assertTrue(RegexValidator.isValidInteger("123"));
        assertTrue(RegexValidator.isValidInteger("999999"));
    }

    @Test
    public void testInvalidIntegers() {
        assertFalse(RegexValidator.isValidInteger(null));
        assertFalse(RegexValidator.isValidInteger(""));
        assertFalse(RegexValidator.isValidInteger("   "));
        assertFalse(RegexValidator.isValidInteger("-1"));
        assertFalse(RegexValidator.isValidInteger("12.5"));
        assertFalse(RegexValidator.isValidInteger("abc"));
    }

    @Test
    public void testValidateBudget() {
        assertDoesNotThrow(() -> RegexValidator.validateBudget(0));
        assertDoesNotThrow(() -> RegexValidator.validateBudget(100.5));
        assertDoesNotThrow(() -> RegexValidator.validateBudget(1000000));
        assertThrows(ValidationException.class, () -> RegexValidator.validateBudget(-1));
        assertThrows(ValidationException.class, () -> RegexValidator.validateBudget(-0.01));
    }

    @Test
    public void testValidateTeamSize() {
        assertDoesNotThrow(() -> RegexValidator.validateTeamSize(1));
        assertDoesNotThrow(() -> RegexValidator.validateTeamSize(100));
        assertThrows(ValidationException.class, () -> RegexValidator.validateTeamSize(0));
        assertThrows(ValidationException.class, () -> RegexValidator.validateTeamSize(-1));
    }

    @Test
    public void testMatchesPattern() {
        assertTrue(RegexValidator.matchesPattern("test123", "^test\\d+$"));
        assertTrue(RegexValidator.matchesPattern("ABC", "^[A-Z]+$"));
        assertFalse(RegexValidator.matchesPattern("abc", "^[A-Z]+$"));
        assertFalse(RegexValidator.matchesPattern(null, "^test$"));
        assertFalse(RegexValidator.matchesPattern("test", null));
    }

    @Test
    public void testValidatePattern() {
        assertDoesNotThrow(() -> RegexValidator.validatePattern("test123", "^test\\d+$", "field"));
        assertThrows(ValidationException.class, 
            () -> RegexValidator.validatePattern("invalid", "^test\\d+$", "field"));
    }

    @Test
    public void testEdgeCases() {
        assertFalse(RegexValidator.isValidTaskId("T00"));
        assertFalse(RegexValidator.isValidTaskId("T0001"));
        assertFalse(RegexValidator.isValidProjectId("P00"));
        assertFalse(RegexValidator.isValidProjectId("P0001"));
        assertFalse(RegexValidator.isValidUserId("U00"));
        assertFalse(RegexValidator.isValidUserId("U0001"));
        
        assertTrue(RegexValidator.isValidTaskName("1"));
        assertTrue(RegexValidator.isValidProjectName("1"));
        
        assertTrue(RegexValidator.isValidEmail("a@b.co"));
    }

    @Test
    public void testBoundaryConditions() {
        assertTrue(RegexValidator.isValidTaskName("A".repeat(100)));
        assertFalse(RegexValidator.isValidTaskName("A".repeat(101)));
        
        assertTrue(RegexValidator.isValidProjectName("A".repeat(100)));
        assertFalse(RegexValidator.isValidProjectName("A".repeat(101)));
        
        assertTrue(RegexValidator.isValidUserName("A".repeat(50)));
        assertFalse(RegexValidator.isValidUserName("A".repeat(51)));
    }

    @Test
    public void testSpecialCharacters() {
        assertTrue(RegexValidator.isValidTaskName("Task-Name"));
        assertTrue(RegexValidator.isValidTaskName("Task_Name"));
        assertTrue(RegexValidator.isValidTaskName("Task Name"));
        
        assertFalse(RegexValidator.isValidTaskName("Task@Name"));
        assertFalse(RegexValidator.isValidTaskName("Task#Name"));
        assertFalse(RegexValidator.isValidTaskName("Task!Name"));
        
        assertTrue(RegexValidator.isValidEmail("user+tag@domain.com"));
        assertTrue(RegexValidator.isValidEmail("user.name@domain.com"));
        assertTrue(RegexValidator.isValidEmail("user_name@domain.com"));
    }
}
