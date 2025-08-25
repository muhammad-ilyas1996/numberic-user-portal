package com.medicalbillinguserportal.commonpersistence.utils;

import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

public class SpecificationUtility {

    public static Specification equalsValue(String columnName, Object value) {
        return (transaction, query, builder) -> builder.equal(transaction.get(columnName), value);
    }

    public static Specification containsValue(String columnName, String string) {
        return (transaction, query, builder) -> builder.like(builder.lower(transaction.get(columnName)), "%"+string.toLowerCase()+"%");
    }


    public static Specification greaterThanOrEqualTo(String columnName, Date date) {
        return (transaction, query, builder) -> builder.greaterThanOrEqualTo(transaction.get(columnName), date);
    }

    public static Specification lessThanOrEqualTo(String columnName, Date date) {
        return (transaction, query, builder) -> builder.lessThanOrEqualTo(transaction.get(columnName), date);
    }
}
