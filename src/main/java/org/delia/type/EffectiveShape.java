package org.delia.type;

/**
 * Shape.INTEGER can be different sizes, specified by the sizeof rule.
 * EffectiveShape is the Java type used to store an INTEGER shape value:
 *  EFFECTIVE_INT - Java int
 *  EFFECTIVE_LONG - Java long
 *
 *  The effective shape must be large enough to hold the specified INTEGER.
 *
 *  Shape.Date can be sql DATE or TIME using isDateOnly or isTimeOnly rule
 */
public enum EffectiveShape {
    EFFECTIVE_INT,
    EFFECTIVE_LONG,

    //for Date types
    EFFECTIVE_DATE_ONLY,
    EFFECTIVE_TIME_ONLY
}
