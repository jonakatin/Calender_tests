package edu.sc.bse3211.meetingplanner;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive unit tests for the Calendar class.
 *
 * Tests cover:
 *  - Normal meeting addition (happy path)
 *  - Invalid/non-existent calendar dates (Feb 29/30/31, Apr 31, Nov 30/31, etc.)
 *  - Month boundary bugs (December blocked by off-by-one in checkTimes)
 *  - Time boundary errors (hour out of range, start >= end)
 *  - Double-booking prevention
 *  - Back-to-back meeting boundary (known defect)
 *  - isBusy, printAgenda, clearSchedule, getMeeting, removeMeeting
 *
 * Known Bugs documented with comments:
 *  BUG-1: checkTimes uses mMonth >= 12 instead of > 12 — blocks December.
 *  BUG-2: Back-to-back meetings (e.g. 9-11 then 11-13) falsely conflict.
 *  BUG-3: November 30 incorrectly pre-blocked in the Calendar constructor.
 */
public class CalendarTest {

    private Calendar calendar;

    @Before
    public void setUp() {
        calendar = new Calendar();
    }

    // -----------------------------------------------------------------------
    // Happy path — valid meeting additions
    // -----------------------------------------------------------------------

    /** TC-CAL-01: A standard valid meeting should be added without exception. */
    @Test
    public void testAddMeeting_validMeeting_noExceptionThrown() {
        try {
            Meeting m = new Meeting(3, 15, 9, 11);
            calendar.addMeeting(m);
            assertTrue("Calendar should be busy after adding a meeting",
                    calendar.isBusy(3, 15, 9, 11));
        } catch (TimeConflictException e) {
            fail("No exception expected for a valid meeting: " + e.getMessage());
        }
    }

    /** TC-CAL-20: Verify printAgenda(month) includes added meeting. */
    @Test
    public void testPrintAgenda_month_containsMeetingInfo() {
        try {
            Meeting m = new Meeting(3, 15, 9, 11);
            m.setDescription("Sprint Planning");
            calendar.addMeeting(m);
            String agenda = calendar.printAgenda(3);
            assertNotNull("Agenda should not be null", agenda);
            assertTrue("Agenda should contain the month number", agenda.contains("3"));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /** TC-CAL-21: Verify printAgenda(month, day) includes correct day header. */
    @Test
    public void testPrintAgenda_day_containsDayHeader() {
        try {
            Meeting m = new Meeting(3, 15, 9, 11);
            calendar.addMeeting(m);
            String agenda = calendar.printAgenda(3, 15);
            assertTrue("Agenda should reference 3/15", agenda.contains("3/15"));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // isBusy — availability checks
    // -----------------------------------------------------------------------

    /** TC-CAL-18: An empty calendar slot should not be busy. */
    @Test
    public void testIsBusy_emptySlot_returnsFalse() {
        try {
            assertFalse("Empty slot should not be busy",
                    calendar.isBusy(3, 10, 9, 11));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /** TC-CAL-19: After adding a meeting, the same slot should be busy. */
    @Test
    public void testIsBusy_afterAddingMeeting_returnsTrue() {
        try {
            calendar.addMeeting(new Meeting(3, 10, 9, 11));
            assertTrue("Slot should be busy after meeting is added",
                    calendar.isBusy(3, 10, 9, 11));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /** TC-CAL-22: clearSchedule should free the day so isBusy returns false. */
    @Test
    public void testClearSchedule_removesAllMeetings() {
        try {
            calendar.addMeeting(new Meeting(3, 10, 9, 11));
            calendar.clearSchedule(3, 10);
            assertFalse("Slot should be free after clearSchedule",
                    calendar.isBusy(3, 10, 9, 11));
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /** TC-CAL-23: getMeeting should return the correct object. */
    @Test
    public void testGetMeeting_returnsCorrectMeeting() {
        try {
            Meeting m = new Meeting(3, 10, 9, 11);
            m.setDescription("Stand-up");
            calendar.addMeeting(m);
            Meeting retrieved = calendar.getMeeting(3, 10, 0);
            assertEquals("Retrieved meeting should match added meeting",
                    "Stand-up", retrieved.getDescription());
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /** TC-CAL-24: removeMeeting by index should remove only that meeting. */
    @Test
    public void testRemoveMeeting_removesCorrectMeeting() {
        try {
            Meeting m1 = new Meeting(3, 10, 8, 9);
            m1.setDescription("First");
            Meeting m2 = new Meeting(3, 10, 14, 16);
            m2.setDescription("Second");
            calendar.addMeeting(m1);
            calendar.addMeeting(m2);
            calendar.removeMeeting(3, 10, 0);
            // After removing index 0, what was index 1 slides to 0
            Meeting remaining = calendar.getMeeting(3, 10, 0);
            assertEquals("Second meeting should remain", "Second", remaining.getDescription());
        } catch (TimeConflictException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Invalid month boundaries
    // -----------------------------------------------------------------------

    /** TC-CAL-06: Month 0 is invalid — should throw TimeConflictException. */
    @Test(expected = TimeConflictException.class)
    public void testCheckTimes_monthZero_throwsException() throws TimeConflictException {
        Calendar.checkTimes(0, 5, 9, 10);
    }

    /**
     * TC-CAL-07: Month 13 is invalid — should throw TimeConflictException.
     * Note: due to BUG-1 (>= 12 instead of > 12) month 12 also throws,
     * so 13 trivially passes this check.
     */
    @Test(expected = TimeConflictException.class)
    public void testCheckTimes_month13_throwsException() throws TimeConflictException {
        Calendar.checkTimes(13, 5, 9, 10);
    }

    /**
     * TC-CAL-08: BUG-1 exposure — Month 12 (December) should be valid,
     * but the off-by-one bug (>= 12) causes it to throw an exception.
     * This test DOCUMENTS the bug: it expects success but will currently fail.
     */
    @Test
    public void testCheckTimes_month12December_shouldBeValid_BUGTEST() {
        // BUG-1: checkTimes uses >= 12 instead of > 12, so December is rejected.
        // This test will FAIL until the bug is fixed. It is intentionally left
        // as @Test (not expected = exception) to make the failure visible.
        try {
            Calendar.checkTimes(12, 1, 9, 10);
            // If we reach here, the bug has been fixed.
        } catch (TimeConflictException e) {
            fail("BUG-1 DETECTED: December (month 12) incorrectly rejected — " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Invalid day boundaries
    // -----------------------------------------------------------------------

    /** TC-CAL-09: Day 0 is invalid. */
    @Test(expected = TimeConflictException.class)
    public void testCheckTimes_dayZero_throwsException() throws TimeConflictException {
        Calendar.checkTimes(3, 0, 9, 10);
    }

    /** TC-CAL-10: Day 32 is invalid. */
    @Test(expected = TimeConflictException.class)
    public void testCheckTimes_day32_throwsException() throws TimeConflictException {
        Calendar.checkTimes(3, 32, 9, 10);
    }

    // -----------------------------------------------------------------------
    // Non-existent calendar dates (blocked by constructor)
    // -----------------------------------------------------------------------

    /** TC-CAL-02: February 29 is pre-blocked — adding a meeting should fail. */
    @Test(expected = TimeConflictException.class)
    public void testAddMeeting_feb29_throwsTimeConflictException() throws TimeConflictException {
        calendar.addMeeting(new Meeting(2, 29, 9, 10));
    }

    /** TC-CAL-03: February 30 is pre-blocked. */
    @Test(expected = TimeConflictException.class)
    public void testAddMeeting_feb30_throwsTimeConflictException() throws TimeConflictException {
        calendar.addMeeting(new Meeting(2, 30, 9, 10));
    }

    /** TC-CAL-04: April 31 is pre-blocked. */
    @Test(expected = TimeConflictException.class)
    public void testAddMeeting_apr31_throwsTimeConflictException() throws TimeConflictException {
        calendar.addMeeting(new Meeting(4, 31, 9, 10));
    }

    /**
     * TC-CAL-05: November 31 is pre-blocked (correct).
     * Also exposes BUG-3: November 30 is also incorrectly blocked.
     */
    @Test(expected = TimeConflictException.class)
    public void testAddMeeting_nov31_throwsTimeConflictException() throws TimeConflictException {
        calendar.addMeeting(new Meeting(11, 31, 9, 10));
    }

    /**
     * BUG-3 exposure: November 30 is a valid date but is pre-blocked
     * by the Calendar constructor. This test documents that defect.
     */
    @Test
    public void testAddMeeting_nov30_shouldBeValid_BUGTEST() {
        // BUG-3: Calendar constructor incorrectly blocks November 30.
        // This test will FAIL until the bug is fixed.
        try {
            calendar.addMeeting(new Meeting(11, 30, 9, 10));
        } catch (TimeConflictException e) {
            fail("BUG-3 DETECTED: November 30 is incorrectly blocked — " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Invalid time boundaries
    // -----------------------------------------------------------------------

    /** TC-CAL-11: start == end should throw (meeting has zero duration). */
    @Test(expected = TimeConflictException.class)
    public void testCheckTimes_startEqualsEnd_throwsException() throws TimeConflictException {
        Calendar.checkTimes(3, 15, 10, 10);
    }

    /** TC-CAL-12: start > end is impossible chronologically. */
    @Test(expected = TimeConflictException.class)
    public void testCheckTimes_startAfterEnd_throwsException() throws TimeConflictException {
        Calendar.checkTimes(3, 15, 14, 9);
    }

    /** TC-CAL-13: Negative start hour is invalid. */
    @Test(expected = TimeConflictException.class)
    public void testCheckTimes_negativeStartHour_throwsException() throws TimeConflictException {
        Calendar.checkTimes(3, 15, -1, 10);
    }

    /** TC-CAL-14: Hour 24 is out of range (valid range is 0–23). */
    @Test(expected = TimeConflictException.class)
    public void testCheckTimes_endHour24_throwsException() throws TimeConflictException {
        Calendar.checkTimes(3, 15, 10, 24);
    }

    // -----------------------------------------------------------------------
    // Double-booking prevention
    // -----------------------------------------------------------------------

    /** TC-CAL-15: Adding a meeting whose start time overlaps an existing meeting should fail. */
    @Test
    public void testAddMeeting_startTimeOverlap_throwsExceptionWithConflictInfo() {
        try {
            Meeting first = new Meeting(5, 20, 9, 11);
            first.setDescription("Team Sync");
            calendar.addMeeting(first);
            // Attempt overlapping meeting
            calendar.addMeeting(new Meeting(5, 20, 10, 12));
            fail("Expected TimeConflictException for overlapping meeting");
        } catch (TimeConflictException e) {
            // Verify the message names the conflicting meeting (correct failure reason)
            assertTrue("Exception should identify the conflicting meeting",
                    e.getMessage().contains("Team Sync") || e.getMessage().contains("Overlap"));
        }
    }

    /** TC-CAL-16: Adding a meeting whose end time overlaps an existing meeting should also fail. */
    @Test(expected = TimeConflictException.class)
    public void testAddMeeting_endTimeOverlap_throwsException() throws TimeConflictException {
        calendar.addMeeting(new Meeting(5, 20, 9, 11));
        calendar.addMeeting(new Meeting(5, 20, 8, 10)); // ends inside existing
    }

    /**
     * TC-CAL-17: BUG-2 exposure — Back-to-back meetings should be allowed.
     * Meeting 1: 9–11, Meeting 2: 11–13. These do not overlap but the
     * inclusive boundary check causes a false conflict.
     */
    @Test
    public void testAddMeeting_backToBackMeetings_shouldSucceed_BUGTEST() {
        // BUG-2: end==start triggers overlap due to <= comparison.
        // This test will FAIL until the bug is fixed.
        try {
            calendar.addMeeting(new Meeting(5, 20, 9, 11));
            calendar.addMeeting(new Meeting(5, 20, 11, 13));
        } catch (TimeConflictException e) {
            fail("BUG-2 DETECTED: Back-to-back meetings falsely conflict — " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Holiday test (preserved from original starter)
    // -----------------------------------------------------------------------

    /** Original starter test: Janan Luwum Day should be bookable and mark calendar busy. */
    @Test
    public void testAddMeeting_holiday() {
        try {
            Meeting janan = new Meeting(2, 16, "Janan Luwum");
            calendar.addMeeting(janan);
            assertTrue("Janan Luwum Day should be marked as busy",
                    calendar.isBusy(2, 16, 0, 23));
        } catch (TimeConflictException e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }
}
