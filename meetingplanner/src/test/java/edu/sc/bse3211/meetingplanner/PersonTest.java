package edu.sc.bse3211.meetingplanner;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;

/**
 * Unit tests for the Person class.
 *
 * Person delegates most logic to its internal Calendar, so these tests
 * confirm that delegation works correctly and that exception messages
 * are enriched with the person's name.
 */
public class PersonTest {

    private Person person;
    private Room dummyRoom;

    @Before
    public void setUp() {
        person = new Person("Acan Brenda");
        dummyRoom = new Room("LLT6A");
    }

    /** Helper: creates a Meeting with full constructor to avoid NPE in toString(). */
    private Meeting makeMeeting(int month, int day, int start, int end, String desc) {
        ArrayList<Person> att = new ArrayList<>();
        att.add(person);
        return new Meeting(month, day, start, end, att, dummyRoom, desc);
    }

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /** TC-PER-01: Constructor should correctly store the person's name. */
    @Test
    public void testConstructor_setsNameCorrectly() {
        assertEquals("Name should be 'Acan Brenda'", "Acan Brenda", person.getName());
    }

    /** Default constructor should produce an empty-string name, not throw. */
    @Test
    public void testDefaultConstructor_nameIsEmptyString() {
        Person p = new Person();
        assertEquals("Default name should be empty string", "", p.getName());
    }

    // -----------------------------------------------------------------------
    // addMeeting
    // -----------------------------------------------------------------------

    /** TC-PER-02: Adding a valid meeting should not throw an exception. */
    @Test
    public void testAddMeeting_validMeeting_noException() {
        try {
            Meeting m = makeMeeting(3, 15, 9, 11, "Stand-up");
            person.addMeeting(m);
        } catch (TimeConflictException e) {
            fail("Valid meeting should not throw exception: " + e.getMessage());
        }
    }

    /** TC-PER-02b: After adding a meeting, isBusy should return true for that slot. */
    @Test
    public void testAddMeeting_thenIsBusy_returnsTrue() {
        try {
            Meeting m = makeMeeting(3, 15, 9, 11, "Stand-up");
            person.addMeeting(m);
            assertTrue("Person should be busy 9-11 on March 15",
                    person.isBusy(3, 15, 9, 11));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /**
     * TC-PER-03: Adding a conflicting meeting should throw TimeConflictException
     * and the message should contain the person's name to aid diagnosis.
     */
    @Test
    public void testAddMeeting_conflictingMeeting_exceptionContainsPersonName() {
        try {
            person.addMeeting(makeMeeting(3, 15, 9, 11, "First Meeting"));
            person.addMeeting(makeMeeting(3, 15, 10, 12, "Conflicting Meeting"));
            fail("Expected TimeConflictException for overlapping meetings");
        } catch (TimeConflictException e) {
            assertTrue("Exception message should contain the person's name",
                    e.getMessage().contains("Acan Brenda"));
        }
    }

    // -----------------------------------------------------------------------
    // isBusy
    // -----------------------------------------------------------------------

    /** TC-PER-04: A fresh person with no meetings should not be busy. */
    @Test
    public void testIsBusy_noMeetings_returnsFalse() {
        try {
            assertFalse("New person should not be busy",
                    person.isBusy(3, 5, 9, 11));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /** isBusy with invalid month should propagate TimeConflictException. */
    @Test(expected = TimeConflictException.class)
    public void testIsBusy_invalidMonth_throwsException() throws TimeConflictException {
        person.isBusy(0, 5, 9, 11);
    }

    // -----------------------------------------------------------------------
    // printAgenda
    // -----------------------------------------------------------------------

    /** TC-PER-05: printAgenda for a month should not return null. */
    @Test
    public void testPrintAgenda_month_returnsNonNull() {
        assertNotNull("printAgenda should return a non-null string",
                person.printAgenda(3));
    }

    /** TC-PER-05b: printAgenda after adding a meeting should contain meeting info. */
    @Test
    public void testPrintAgenda_month_containsMeetingAfterAdd() {
        try {
            person.addMeeting(makeMeeting(3, 15, 9, 11, "Retrospective"));
            String agenda = person.printAgenda(3);
            assertTrue("Agenda should reference month 3", agenda.contains("3"));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /** TC-PER-06: printAgenda for a specific day should contain day reference. */
    @Test
    public void testPrintAgenda_day_containsDayHeader() {
        try {
            person.addMeeting(makeMeeting(3, 15, 9, 11, "Sprint Planning"));
            String agenda = person.printAgenda(3, 15);
            assertTrue("Agenda should contain '3/15'", agenda.contains("3/15"));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // getMeeting / removeMeeting
    // -----------------------------------------------------------------------

    /** getMeeting should return the exact object that was added. */
    @Test
    public void testGetMeeting_returnsAddedMeeting() {
        try {
            Meeting m = makeMeeting(3, 15, 9, 11, "Design Session");
            person.addMeeting(m);
            Meeting retrieved = person.getMeeting(3, 15, 0);
            assertEquals("Retrieved meeting description should match",
                    "Design Session", retrieved.getDescription());
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /** TC-PER-07: removeMeeting should clear the slot so isBusy returns false. */
    @Test
    public void testRemoveMeeting_slotBecomesAvailable() {
        try {
            person.addMeeting(makeMeeting(3, 15, 9, 11, "Workshop"));
            person.removeMeeting(3, 15, 0);
            assertFalse("Slot should be free after removal",
                    person.isBusy(3, 15, 9, 11));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Vacation (full-day block)
    // -----------------------------------------------------------------------

    /** Booking vacation (full-day meeting) should mark the entire day as busy. */
    @Test
    public void testAddVacation_marksFullDayBusy() {
        try {
            // A Meeting(month, day) uses start=0, end=23 — but checkTimes requires start < end,
            // and 0 < 23 is fine.  We add it via the calendar's special day-blocker logic.
            Meeting vacation = new Meeting(6, 10, "Annual Leave");
            person.addMeeting(vacation);
            assertTrue("Entire day should be busy during vacation",
                    person.isBusy(6, 10, 0, 23));
        } catch (TimeConflictException e) {
            fail("Vacation booking should not throw: " + e.getMessage());
        }
    }
}
