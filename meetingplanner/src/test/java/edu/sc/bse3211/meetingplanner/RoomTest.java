package edu.sc.bse3211.meetingplanner;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;

/**
 * Unit tests for the Room class.
 *
 * Room mirrors Person's structure — both delegate to Calendar.
 * These tests verify correct delegation, ID management, and that
 * conflict messages are enriched with the room's ID.
 */
public class RoomTest {

    private Room room;
    private Person dummyAttendee;

    @Before
    public void setUp() {
        room = new Room("LLT6A");
        dummyAttendee = new Person("Namugga Martha");
    }

    /** Helper: builds a full Meeting to avoid toString() NPE. */
    private Meeting makeMeeting(int month, int day, int start, int end, String desc) {
        ArrayList<Person> att = new ArrayList<>();
        att.add(dummyAttendee);
        return new Meeting(month, day, start, end, att, room, desc);
    }

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /** TC-RM-01: Constructor should correctly store the room ID. */
    @Test
    public void testConstructor_setsIDCorrectly() {
        assertEquals("Room ID should be 'LLT6A'", "LLT6A", room.getID());
    }

    /** Default constructor should produce an empty string ID. */
    @Test
    public void testDefaultConstructor_IDIsEmptyString() {
        Room r = new Room();
        assertEquals("Default room ID should be empty string", "", r.getID());
    }

    // -----------------------------------------------------------------------
    // addMeeting
    // -----------------------------------------------------------------------

    /** TC-RM-02: A valid meeting should be added without throwing. */
    @Test
    public void testAddMeeting_validMeeting_noException() {
        try {
            room.addMeeting(makeMeeting(5, 10, 14, 16, "Board Meeting"));
        } catch (TimeConflictException e) {
            fail("Valid meeting should not throw: " + e.getMessage());
        }
    }

    /**
     * TC-RM-03: A scheduling conflict should throw TimeConflictException
     * and the message should contain the room's ID.
     */
    @Test
    public void testAddMeeting_conflictingMeeting_exceptionContainsRoomID() {
        try {
            room.addMeeting(makeMeeting(5, 10, 14, 16, "Board Meeting"));
            room.addMeeting(makeMeeting(5, 10, 15, 17, "Clashing Meeting"));
            fail("Expected TimeConflictException for overlapping meetings");
        } catch (TimeConflictException e) {
            assertTrue("Exception should identify the room",
                    e.getMessage().contains("LLT6A"));
        }
    }

    // -----------------------------------------------------------------------
    // isBusy
    // -----------------------------------------------------------------------

    /** TC-RM-04: isBusy should return false before any meetings are added. */
    @Test
    public void testIsBusy_noMeetings_returnsFalse() {
        try {
            assertFalse("Empty room should not be busy",
                    room.isBusy(5, 10, 14, 16));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /** TC-RM-04b: isBusy should return true after a meeting is added. */
    @Test
    public void testIsBusy_afterAddingMeeting_returnsTrue() {
        try {
            room.addMeeting(makeMeeting(5, 10, 14, 16, "Board Meeting"));
            assertTrue("Room should be busy 14-16",
                    room.isBusy(5, 10, 14, 16));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /** isBusy with an invalid date should propagate TimeConflictException. */
    @Test(expected = TimeConflictException.class)
    public void testIsBusy_invalidDate_throwsException() throws TimeConflictException {
        room.isBusy(0, 5, 9, 11);
    }

    // -----------------------------------------------------------------------
    // printAgenda
    // -----------------------------------------------------------------------

    /** TC-RM-05: printAgenda(month) should return a non-null string. */
    @Test
    public void testPrintAgenda_month_returnsNonNull() {
        assertNotNull("printAgenda should not return null", room.printAgenda(5));
    }

    /** TC-RM-05b: printAgenda after adding a meeting should contain the month. */
    @Test
    public void testPrintAgenda_month_containsMeetingInfo() {
        try {
            room.addMeeting(makeMeeting(5, 10, 14, 16, "Board Meeting"));
            String agenda = room.printAgenda(5);
            assertTrue("Agenda should contain month 5", agenda.contains("5"));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /** printAgenda(month, day) should contain the correct day header. */
    @Test
    public void testPrintAgenda_day_containsDayHeader() {
        try {
            room.addMeeting(makeMeeting(5, 10, 14, 16, "Board Meeting"));
            String agenda = room.printAgenda(5, 10);
            assertTrue("Agenda should contain '5/10'", agenda.contains("5/10"));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // getMeeting / removeMeeting
    // -----------------------------------------------------------------------

    /** getMeeting should return the object that was added. */
    @Test
    public void testGetMeeting_returnsAddedMeeting() {
        try {
            Meeting m = makeMeeting(5, 10, 14, 16, "Strategy");
            room.addMeeting(m);
            Meeting retrieved = room.getMeeting(5, 10, 0);
            assertEquals("Retrieved meeting should be 'Strategy'",
                    "Strategy", retrieved.getDescription());
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /** TC-RM-06: After removeMeeting, the slot should be free. */
    @Test
    public void testRemoveMeeting_slotBecomesAvailable() {
        try {
            room.addMeeting(makeMeeting(5, 10, 14, 16, "Workshop"));
            room.removeMeeting(5, 10, 0);
            assertFalse("Slot should be free after removal",
                    room.isBusy(5, 10, 14, 16));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Multiple rooms independence
    // -----------------------------------------------------------------------

    /** Two different Room objects should have independent calendars. */
    @Test
    public void testTwoRooms_independentCalendars() {
        Room room2 = new Room("LLT6B");
        try {
            room.addMeeting(makeMeeting(5, 10, 14, 16, "Meeting A"));
            // Same time in a different room should not conflict
            ArrayList<Person> att2 = new ArrayList<>();
            att2.add(dummyAttendee);
            Meeting m2 = new Meeting(5, 10, 14, 16, att2, room2, "Meeting B");
            room2.addMeeting(m2);
            assertTrue("Room 1 should be busy", room.isBusy(5, 10, 14, 16));
            assertTrue("Room 2 should also be busy independently", room2.isBusy(5, 10, 14, 16));
        } catch (TimeConflictException e) {
            fail("Independent rooms should not conflict with each other: " + e.getMessage());
        }
    }
}
