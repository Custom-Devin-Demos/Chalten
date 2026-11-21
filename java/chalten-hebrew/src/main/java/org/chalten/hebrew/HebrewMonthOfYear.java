package org.chalten.hebrew;

import org.chalten.core.CalendarDate;
import org.chalten.core.CalendarMonthOfYear;
import org.chalten.measure.TimeQuantity;

/** Smalltalk {@code HebrewMonthOfYear}: a month of a specific Hebrew year. */
public final class HebrewMonthOfYear implements CalendarMonthOfYear<HebrewMonthOfYear, HebrewDate, HebrewYear> {

    private final HebrewYear year;
    private final HebrewMonth month;
    private final HebrewDate[] dates;

    private HebrewMonthOfYear(HebrewYear year, HebrewMonth month) {
        this.year = year;
        this.month = month;
        this.dates = new HebrewDate[numberOfDays().numericalValue().intValueExact()];
    }

    /** Smalltalk {@code HebrewMonthOfYear class>>year:month:}. */
    public static HebrewMonthOfYear year(HebrewYear year, HebrewMonth month) {
        return new HebrewMonthOfYear(year, month);
    }

    @Override
    public HebrewYear year() {
        return year;
    }

    @Override
    public HebrewMonth month() {
        return month;
    }

    private int position() {
        return month.positionIn(year);
    }

    @Override
    public TimeQuantity numberOfDays() {
        if (month.isVariableLengthMonth()) {
            if (year.isDeficientYear()) {
                return month.numberOfDaysInDeficientYear();
            }
            if (year.isRegularYear()) {
                return month.numberOfDaysInRegularYear();
            }
            if (year.isCompleteYear()) {
                return month.numberOfDaysInCompleteYear();
            }
        }
        return month.numberOfDaysInRegularYear();
    }

    /**
     * Smalltalk {@code dateAt:}.
     *
     * @throws org.chalten.measure.InstanceCreationFailedException {@code <n> is an invalid day number in <month>}
     */
    @Override
    public HebrewDate dateAt(int number) {
        CalendarDate.assertIsValid(number, this);
        synchronized (dates) {
            HebrewDate date = dates[number - 1];
            if (date == null) {
                date = HebrewDate.monthOfYear(this, number);
                dates[number - 1] = date;
            }
            return date;
        }
    }

    @Override
    public HebrewDate twentyNinth() {
        return dateAt(29);
    }

    @Override
    public HebrewMonthOfYear next() {
        return next(TimeQuantity.of(1, HebrewCalendar.month()));
    }

    @Override
    public HebrewMonthOfYear previous() {
        return previous(TimeQuantity.of(1, HebrewCalendar.month()));
    }

    /** Smalltalk {@code next:}, for quantities of Hebrew months. */
    @Override
    public HebrewMonthOfYear next(TimeQuantity relativeTimeQuantity) {
        if (relativeTimeQuantity.isZero()) {
            return this;
        }
        if (relativeTimeQuantity.isNegative()) {
            return previous(relativeTimeQuantity.abs());
        }
        TimeQuantity remainingMonthsInYear =
                year.numberOfMonths().minus(TimeQuantity.of(position(), HebrewCalendar.month()));
        if (relativeTimeQuantity.compareTo(remainingMonthsInYear) <= 0) {
            return year.monthAt(inMonths(relativeTimeQuantity) + position());
        }
        HebrewYear currentYear = year.next();
        TimeQuantity remainingOffset = relativeTimeQuantity.minus(remainingMonthsInYear);
        while (remainingOffset.compareTo(currentYear.numberOfMonths()) > 0) {
            remainingOffset = remainingOffset.minus(currentYear.numberOfMonths());
            currentYear = currentYear.next();
        }
        return currentYear.monthAt(inMonths(remainingOffset));
    }

    /** Smalltalk {@code previous:}, for quantities of Hebrew months. */
    @Override
    public HebrewMonthOfYear previous(TimeQuantity relativeTimeQuantity) {
        if (relativeTimeQuantity.isZero()) {
            return this;
        }
        if (relativeTimeQuantity.isNegative()) {
            return next(relativeTimeQuantity.abs());
        }
        TimeQuantity remainingMonthsInYear = TimeQuantity.of(position() - 1, HebrewCalendar.month());
        if (relativeTimeQuantity.compareTo(remainingMonthsInYear) <= 0) {
            return year.monthAt(position() - inMonths(relativeTimeQuantity));
        }
        HebrewYear currentYear = year.previous();
        TimeQuantity remainingOffset = relativeTimeQuantity.minus(remainingMonthsInYear);
        while (remainingOffset.compareTo(currentYear.numberOfMonths()) > 0) {
            remainingOffset = remainingOffset.minus(currentYear.numberOfMonths());
            currentYear = currentYear.previous();
        }
        return currentYear.monthAt(inMonths(currentYear.numberOfMonths().minus(remainingOffset)) + 1);
    }

    private static int inMonths(TimeQuantity quantity) {
        return quantity.convertTo(HebrewCalendar.month()).numericalValue().intValueExact();
    }

    @Override
    public int compareTo(HebrewMonthOfYear other) {
        int byYear = year.compareTo(other.year);
        return byYear != 0 ? byYear : Integer.compare(month.positionIn(year), other.month.positionIn(year));
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof HebrewMonthOfYear other && year.equals(other.year) && month == other.month;
    }

    @Override
    public int hashCode() {
        return 31 * year.hashCode() + month.hashCode();
    }

    @Override
    public String toString() {
        return month + ", " + year;
    }
}
