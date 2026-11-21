package org.chalten.islamic;

import org.chalten.core.CalendarDate;
import org.chalten.core.CalendarMonthOfYear;
import org.chalten.measure.TimeQuantity;

/** Smalltalk {@code IslamicMonthOfYear}: a month of a specific Islamic year. */
public final class IslamicMonthOfYear implements CalendarMonthOfYear<IslamicMonthOfYear, IslamicDate, IslamicYear> {

    private final IslamicYear year;
    private final IslamicMonth month;
    private final IslamicDate[] dates;

    private IslamicMonthOfYear(IslamicYear year, IslamicMonth month) {
        this.year = year;
        this.month = month;
        this.dates = new IslamicDate[numberOfDays().numericalValue().intValueExact()];
    }

    /** Smalltalk {@code IslamicMonthOfYear class>>year:month:}. */
    public static IslamicMonthOfYear year(IslamicYear year, IslamicMonth month) {
        return new IslamicMonthOfYear(year, month);
    }

    @Override
    public IslamicYear year() {
        return year;
    }

    @Override
    public IslamicMonth month() {
        return month;
    }

    @Override
    public TimeQuantity numberOfDays() {
        return year.isLeapYear() ? month.numberOfDaysInLeapYear() : month.numberOfDaysInCommonYear();
    }

    @Override
    public IslamicDate dateAt(int number) {
        CalendarDate.assertIsValid(number, this);
        synchronized (dates) {
            IslamicDate date = dates[number - 1];
            if (date == null) {
                date = IslamicDate.monthOfYear(this, number);
                dates[number - 1] = date;
            }
            return date;
        }
    }

    @Override
    public IslamicDate twentyNinth() {
        return dateAt(29);
    }

    @Override
    public IslamicMonthOfYear next() {
        return next(TimeQuantity.of(1, IslamicCalendar.month()));
    }

    @Override
    public IslamicMonthOfYear previous() {
        return previous(TimeQuantity.of(1, IslamicCalendar.month()));
    }

    /** Smalltalk {@code next:}, for quantities of Islamic years or months. */
    @Override
    public IslamicMonthOfYear next(TimeQuantity relativeTimeQuantity) {
        if (relativeTimeQuantity.isZero()) {
            return this;
        }
        if (relativeTimeQuantity.isNegative()) {
            return previous(relativeTimeQuantity.abs());
        }
        TimeQuantity years = relativeTimeQuantity.convertTo(IslamicCalendar.year());
        TimeQuantity wholeYears = years.truncated();
        TimeQuantity remaining = years.minus(wholeYears);
        if (remaining.isZero()) {
            return year.next(wholeYears).monthAt(month.positionInYear());
        }
        IslamicMonth newMonth = month.next(remaining.convertTo(IslamicCalendar.month()));
        IslamicYear newYear = newMonth.positionInYear() < month.positionInYear()
                ? year.next(wholeYears.plus(TimeQuantity.of(1, IslamicCalendar.year())))
                : year.next(wholeYears);
        return newYear.monthAt(newMonth.positionInYear());
    }

    /** Smalltalk {@code previous:}, for quantities of Islamic years or months. */
    @Override
    public IslamicMonthOfYear previous(TimeQuantity relativeTimeQuantity) {
        if (relativeTimeQuantity.isZero()) {
            return this;
        }
        if (relativeTimeQuantity.isNegative()) {
            return next(relativeTimeQuantity.abs());
        }
        TimeQuantity years = relativeTimeQuantity.convertTo(IslamicCalendar.year());
        TimeQuantity wholeYears = years.truncated();
        TimeQuantity remaining = years.minus(wholeYears);
        if (remaining.isZero()) {
            return year.previous(wholeYears).monthAt(month.positionInYear());
        }
        IslamicMonth newMonth = month.previous(remaining.convertTo(IslamicCalendar.month()));
        IslamicYear newYear = newMonth.positionInYear() > month.positionInYear()
                ? year.previous(wholeYears.plus(TimeQuantity.of(1, IslamicCalendar.year())))
                : year.previous(wholeYears);
        return newYear.monthAt(newMonth.positionInYear());
    }

    @Override
    public int compareTo(IslamicMonthOfYear other) {
        int byYear = year.compareTo(other.year);
        return byYear != 0 ? byYear : Integer.compare(month.positionInYear(), other.month.positionInYear());
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof IslamicMonthOfYear other && year.equals(other.year) && month == other.month;
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
