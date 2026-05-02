package edu.sc.bse3211.meetingplanner;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;

/**
 * Unit tests for the Meeting class.
 *
 * Known bugs exposed:
 *  BUG-4: toString() throws NullPointerException when attendees/room are null
 *         (partial constructors do not initialise these fields).
 *  BUG-5: addAttendee() throws NullPointerException on partial constructors
 *         because the attendees ArrayList is null.
 */
public class MeetingTest {

    // -----------------------------------------------------------------------
    // Constructor tests
    // -----------------------------------------------------------------------

    /** TC-MTG-01: Full constructor should correctly set all fields. */
    @Test
    public void testFullConstructor_setsAllFields() {
        ArrayList<Person> attendees = new ArrayList<>();
        attendees.add(new Person("Kazibwe Julius"));
        Room room = new Room("LLT6A");

        Meeting m = new Meeting(5, 15, 9, 11, attendees, room, "Sprint Review");

        assertEquals("Month should be 5", 5, m.getMonth());
        assertEquals("Day should be 15", 15, m.getDay());
        assertEquals("Start time should be 9", 9, m.getStartTime());
        assertEquals("End time should be 11", 11, m.getEndTime());
        assertEquals("Description should match", "Sprint Review", m.getDescription());
        assertEquals("Room should be LLT6A", "LLT6A", m.getRoom().getID());
        assertEquals("Should have 1 attendee", 1, m.getAttendees().size());
    }

    /** TC-MTG-02: Default constructor should not throw any exception. */
    @Test
    public void testDefaultConstructor_noException() {
        Meeting m = new Meeting();
        assertNotNull("Meeting object should be instantiated", m);
    }

    /** TC-MTG-03: Vacation constructor (month, day) should set start=0, end=23. */
    @Test
    public void testVacationConstructor_setsFullDayTimes() {
        Meeting m = new Meeting(5, 10);
        assertEquals("Vacation start should be 0 (midnight)", 0, m.getStartTime());
        assertEquals("Vacation end should be 23 (11 PM)", 23, m.getEndTime());
        assertEquals("Month should be 5", 5, m.getMonth());
        assertEquals("Day should be 10", 10, m.getDay());
    }

    /** TC-MTG-03b: Vacation constructor with description should also set full day. */
    @Test
    public void testVacationConstructorWithDescription_setsFullDayAndDescription() {
        Meeting m = new Meeting(5, 10, "Uganda Martyrs Day");
        assertEquals("Start should be 0", 0, m.getStartTime());
        assertEquals("End should be 23", 23, m.getEndTime());
        assertEquals("Description should match", "Uganda Martyrs Day", m.getDescription());
    }

    /** TC-MTG-06: setDescription and getDescription should be consistent. */
    @Test
    public void testSetDescription_getDescriptionReturnsSetValue() {
        Meeting m = new Meeting();
        m.setDescription("Retrospective");
        assertEquals("Description should be 'Retrospective'", "Retrospective", m.getDescription());
    }

    // -----------------------------------------------------------------------
    // Setter / getter roundtrip tests
    // -----------------------------------------------------------------------

    @Test
    public void testSetMonth_getMonthReturnsCorrectValue() {
        Meeting m = new Meeting();
        m.setMonth(7);
        assertEquals(7, m.getMonth());
    }

    @Test
    public void testSetDay_getDayReturnsCorrectValue() {
        Meeting m = new Meeting();
        m.setDay(22);
        assertEquals(22, m.getDay());
    }

    @Test
    public void testSetStartTime_getStartTimeReturnsCorrectValue() {
        Meeting m = new Meeting();
        m.setStartTime(8);
        assertEquals(8, m.getStartTime());
    }

    @Test
    public void testSetEndTime_getEndTimeReturnsCorrectValue() {
        Meeting m = new Meeting();
        m.setEndTime(17);
        assertEquals(17, m.getEndTime());
    }

    @Test
    public void testSetRoom_getRoomReturnsCorrectRoom() {
        Meeting m = new Meeting();
        Room r = new Room("LAB2");
        m.setRoom(r);
        assertEquals("LAB2", m.getRoom().getID());
    }

    // -----------------------------------------------------------------------
    // Attendee management
    // -----------------------------------------------------------------------

    /** TC-MTG-07: removeAttendee should remove exactly the specified person. */
    @Test
    public void testRemoveAttendee_removesCorrectPerson() {
        ArrayList<Person> attendees = new ArrayList<>();
        Person p1 = new Person("Acan Brenda");
        Person p2 = new Person("Shema Collins");
        attendees.add(p1);
        attendees.add(p2);
        Room room = new Room("LLT6A");
        Meeting m = new Meeting(3, 10, 9, 11, attendees, room, "Design Review");

        m.removeAttendee(p1);

        assertFalse("Acan Brenda should be removed", m.getAttendees().contains(p1));
        assertTrue("Shema Collins should still be present", m.getAttendees().contains(p2));
    }

    /**
     * TC-MTG-04: BUG-5 — addAttendee() on a Meeting created with the partial
     * constructor (month, day, start, end) throws NullPointerException because
     * the attendees list is never initialised.
     */
    @Test
    public void testAddAttendee_onPartialConstructor_causesNPE_BUGTEST() {
        // BUG-5: attendees is null for partial constructors.
        // This test documents the defect. It will FAIL (produce error, not pass)
        // until the constructor is fixed to initialise the list.
        Meeting m = new Meeting(3, 10, 9, 11);
        Person p = new Person("Kukunda Lynn");
        try {
            m.addAttendee(p);
            assertEquals("Attendee should be added after fix", 1, m.getAttendees().size());
        } catch (NullPointerException e) {
            fail("BUG-5 DETECTED: addAttendee throws NullPointerException on partial constructor — "
                    + e.getClass().getSimpleName());
        }
    }

    /**
     * TC-MTG-05: BUG-4 — toString() on a Meeting with null attendees/room
     * throws NullPointerException. Any Meeting not using the full constructor
     * will trigger this.
     */
    @Test
    public void testToString_withNullAttendees_causesNPE_BUGTEST() {
        // BUG-4: toString() calls room.getID() and iterates attendees without null checks.
        // This test will FAIL (error) until toString() is made null-safe.
        Meeting m = new Meeting(3, 10, 9, 11);
        m.setDescription("Quick Check");
        try {
            String result = m.toString();
            assertNotNull("toString should return a non-null string", result);
        } catch (NullPointerException e) {
            fail("BUG-4 DETECTED: toString() throws NullPointerException when room/attendees are null");
        }
    }

    // -----------------------------------------------------------------------
    // toString happy path (full constructor)
    // -----------------------------------------------------------------------

    /** toString with full constructor should return a formatted non-null string. */
    @Test
    public void testToString_fullConstructor_returnsFormattedString() {
        ArrayList<Person> attendees = new ArrayList<>();
        attendees.add(new Person("Namugga Martha"));
        Room room = new Room("LLT3A");
        Meeting m = new Meeting(3, 10, 9, 11, attendees, room, "Kickoff");

        String result = m.toString();

        assertNotNull("toString result should not be null", result);
        assertTrue("Result should contain the room ID", result.contains("LLT3A"));
        assertTrue("Result should contain the description", result.contains("Kickoff"));
        assertTrue("Result should contain the attendee name", result.contains("Namugga Martha"));
    }
}
