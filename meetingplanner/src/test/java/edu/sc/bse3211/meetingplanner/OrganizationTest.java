package edu.sc.bse3211.meetingplanner;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the Organization class.
 *
 * Organization is the entry point that holds all employees and rooms.
 * Tests verify correct initialisation, successful lookup, and correct
 * exception handling for non-existent resources.
 */
public class OrganizationTest {

    private Organization org;

    @Before
    public void setUp() {
        org = new Organization();
    }

    // -----------------------------------------------------------------------
    // Initialisation
    // -----------------------------------------------------------------------

    /** TC-ORG-01: Organization should initialise exactly 5 employees. */
    @Test
    public void testOrganization_initializesCorrectNumberOfEmployees() {
        assertEquals("Organization should have 5 employees",
                5, org.getEmployees().size());
    }

    /** TC-ORG-02: Organization should initialise exactly 5 rooms. */
    @Test
    public void testOrganization_initializesCorrectNumberOfRooms() {
        assertEquals("Organization should have 5 rooms",
                5, org.getRooms().size());
    }

    /** Employee list should not contain any null entries after construction. */
    @Test
    public void testOrganization_employeeListContainsNoNulls() {
        for (Person p : org.getEmployees()) {
            assertNotNull("No employee entry should be null", p);
            assertNotNull("Employee name should not be null", p.getName());
        }
    }

    /** Room list should not contain any null entries after construction. */
    @Test
    public void testOrganization_roomListContainsNoNulls() {
        for (Room r : org.getRooms()) {
            assertNotNull("No room entry should be null", r);
            assertNotNull("Room ID should not be null", r.getID());
        }
    }

    // -----------------------------------------------------------------------
    // getRoom — happy path
    // -----------------------------------------------------------------------

    /** TC-ORG-03: getRoom with a known ID should return the correct Room. */
    @Test
    public void testGetRoom_existingRoom_returnsCorrectRoom() {
        try {
            Room r = org.getRoom("LLT6A");
            assertNotNull("Room should not be null", r);
            assertEquals("Room ID should be LLT6A", "LLT6A", r.getID());
        } catch (Exception e) {
            fail("Known room should not throw: " + e.getMessage());
        }
    }

    /** All five known rooms should be retrievable by ID. */
    @Test
    public void testGetRoom_allKnownRooms_noneThrow() {
        String[] knownRooms = { "LLT6A", "LLT6B", "LLT3A", "LLT2C", "LAB2" };
        for (String id : knownRooms) {
            try {
                Room r = org.getRoom(id);
                assertEquals("ID should match requested room", id, r.getID());
            } catch (Exception e) {
                fail("Known room '" + id + "' should not throw: " + e.getMessage());
            }
        }
    }

    // -----------------------------------------------------------------------
    // getRoom — error cases
    // -----------------------------------------------------------------------

    /** TC-ORG-04: getRoom with an unknown ID should throw an exception. */
    @Test
    public void testGetRoom_nonExistentRoom_throwsException() {
        try {
            org.getRoom("UNKNOWN_ROOM");
            fail("Expected exception for non-existent room");
        } catch (Exception e) {
            assertTrue("Exception message should describe the problem",
                    e.getMessage().contains("does not exist"));
        }
    }

    /** TC-ORG-07b: getRoom with an empty string ID should throw. */
    @Test
    public void testGetRoom_emptyStringID_throwsException() {
        try {
            org.getRoom("");
            fail("Expected exception for empty room ID");
        } catch (Exception e) {
            // Correct — empty string matches no room
            assertNotNull("Exception should have a message", e.getMessage());
        }
    }

    /** getRoom with a lowercase version of a valid ID should throw (case-sensitive). */
    @Test
    public void testGetRoom_wrongCase_throwsException() {
        try {
            org.getRoom("llt6a"); // lowercase
            fail("Room lookup should be case-sensitive");
        } catch (Exception e) {
            // Expected — IDs are case-sensitive
        }
    }

    // -----------------------------------------------------------------------
    // getEmployee — happy path
    // -----------------------------------------------------------------------

    /** TC-ORG-05: getEmployee with a known name should return the correct Person. */
    @Test
    public void testGetEmployee_existingEmployee_returnsCorrectPerson() {
        try {
            Person p = org.getEmployee("Namugga Martha");
            assertNotNull("Person should not be null", p);
            assertEquals("Name should match", "Namugga Martha", p.getName());
        } catch (Exception e) {
            fail("Known employee should not throw: " + e.getMessage());
        }
    }

    /** All five known employees should be retrievable by name. */
    @Test
    public void testGetEmployee_allKnownEmployees_noneThrow() {
        String[] names = { "Namugga Martha", "Shema Collins", "Acan Brenda",
                           "Kazibwe Julius", "Kukunda Lynn" };
        for (String name : names) {
            try {
                Person p = org.getEmployee(name);
                assertEquals("Name should match", name, p.getName());
            } catch (Exception e) {
                fail("Known employee '" + name + "' should not throw: " + e.getMessage());
            }
        }
    }

    // -----------------------------------------------------------------------
    // getEmployee — error cases
    // -----------------------------------------------------------------------

    /** TC-ORG-06: getEmployee with an unknown name should throw an exception. */
    @Test
    public void testGetEmployee_nonExistentEmployee_throwsException() {
        try {
            org.getEmployee("Nobody Here");
            fail("Expected exception for non-existent employee");
        } catch (Exception e) {
            assertTrue("Exception message should describe the problem",
                    e.getMessage().contains("does not exist"));
        }
    }

    /** TC-ORG-07: getEmployee with null should throw (graceful null handling). */
    @Test
    public void testGetEmployee_nullName_throwsException() {
        try {
            org.getEmployee(null);
            fail("Expected exception for null employee name");
        } catch (Exception e) {
            // Either NullPointerException or a checked exception — both acceptable,
            // but the system should not silently return a value.
            assertNotNull("Exception should be non-null", e);
        }
    }

    /** getEmployee search should be case-sensitive. */
    @Test
    public void testGetEmployee_wrongCase_throwsException() {
        try {
            org.getEmployee("namugga martha"); // all lowercase
            fail("Employee lookup should be case-sensitive");
        } catch (Exception e) {
            // Expected
        }
    }

    // -----------------------------------------------------------------------
    // Integration: look up employee and book a meeting through Organization
    // -----------------------------------------------------------------------

    /** Integration: retrieve a room and person, book a meeting, check availability. */
    @Test
    public void testIntegration_bookMeetingForEmployeeInRoom() {
        try {
            Person employee = org.getEmployee("Shema Collins");
            Room meetingRoom = org.getRoom("LLT3A");

            java.util.ArrayList<Person> attendees = new java.util.ArrayList<>();
            attendees.add(employee);

            Meeting m = new Meeting(3, 20, 10, 12, attendees, meetingRoom, "Kick-off");

            employee.addMeeting(m);
            meetingRoom.addMeeting(m);

            assertTrue("Employee should be busy after booking",
                    employee.isBusy(3, 20, 10, 12));
            assertTrue("Room should be busy after booking",
                    meetingRoom.isBusy(3, 20, 10, 12));

        } catch (Exception e) {
            fail("Integration booking should not throw: " + e.getMessage());
        }
    }
}
