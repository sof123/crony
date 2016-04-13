package com.github.emmanueltouzery.crony;

import java.time.Month;
import java.time.ZonedDateTime;

import javaslang.Function1;
import javaslang.collection.Array;
import javaslang.collection.Map;
import javaslang.collection.Seq;
import javaslang.collection.Set;
import javaslang.collection.Stream;
import javaslang.control.Validation;

/**
 * Part of the cron specification describing the month of the year.
 */
public class MonthSpec {

    /**
     * The months of the year at which the spec triggers.
     * Empty means any month is accepted.
     */
    public final Set<Month> months;

    private MonthSpec(Set<Month> months) {
        this.months = months;
    }

    /**
     * Build a {@link MonthSpec} from the list of months of the year
     * that it matches.
     * @param months Accepted months of the year, or empty set for 'accept all'
     * @return a new {@link MonthSpec} or an error message.
     */
    public static Validation<String, MonthSpec> build(Set<Month> months) {
        return Validation.valid(new MonthSpec(months));
    }

    private static final Map<String, Integer> monthMap = Array.of(
        "jan", "feb", "mar", "apr", "may", "jun",
        "jul", "aug", "sep", "oct", "nov", "dec")
        .zip(Stream.from(1)).toMap(Function1.identity());

    /*package*/ static Validation<String, MonthSpec> parse(String cronSpec) {
        Function1<Integer, Validation<String, Month>> parseMonth = item ->
            Javaslang.tryValidation(() -> Month.of(item),
                                    String.format("Invalid month: %d", item));
        return SpecItemParser.parseSpecItem(cronSpec.toLowerCase(), 12, monthMap)
            .flatMap(intSet -> Javaslang.sequenceS(intSet.map(parseMonth)))
            .map(Seq::toSet)
            .flatMap(MonthSpec::build);
    }

    /*package*/ Set<String> monthsFormattedSet() {
        return months
            .map(month -> Array.of(Month.values()).indexOf(month)+1)
            .map(Object::toString);
    }

    /*package*/ boolean isMatch(ZonedDateTime dateTime) {
        return months.isEmpty() || months.contains(dateTime.getMonth());
    }
}
