/*
 * Copyright (c) 2015 Christian Chevalley
 * This file is part of Project Ethercis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ethercis.validation.wrappers;

import com.ethercis.validation.ConstraintOccurrences;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.support.basic.*;
import org.openehr.schemas.v1.*;
import org.openehr.schemas.v1.Interval;

/**
 * Created by christian on 7/20/2016.
 */
public class IntervalComparator {

    //lower exclusive, upper exclusive
    private static void isWithinLxUx(Comparable value, Comparable lower, Comparable upper) throws Exception{
        if (value.compareTo(lower) > 0 && value.compareTo(upper) < 0) return;
        throw new IllegalArgumentException("value is not within interval, expected:"+lower+" < "+value+" < "+upper);
    }

    //lower inclusive, upper exclusive
    private static void isWithinLiUx(Comparable value, Comparable lower, Comparable upper) throws Exception {
        if (value.compareTo(lower) >= 0 && value.compareTo(upper) < 0) return;
        throw new IllegalArgumentException("value is not within interval, expected:"+lower+" <= "+value+" < "+upper);
    }

    //lower exclusive, upper inclusive
    private static void isWithinLxUi(Comparable value, Comparable lower, Comparable upper) throws Exception {
        if (value.compareTo(lower) > 0 && value.compareTo(upper) <= 0) return;
        throw new IllegalArgumentException("value is not within interval, expected:"+lower+" < "+value+" <= "+upper);
    }

    //lower inclusive, upper inclusive
    private static void isWithinLiUi(Comparable value, Comparable lower, Comparable upper) throws Exception {
        if (value.compareTo(lower) >= 0 && value.compareTo(upper) <= 0) return;
        throw new IllegalArgumentException("value is not within interval, expected:"+lower+" <= "+value+" <= "+upper);
    }

    private static void  compareWithinInterval(Comparable value, Interval interval, Comparable lower, Comparable upper) throws Exception {
        boolean isLowerIncluded = (interval.isSetLowerIncluded()? interval.getLowerIncluded() : false);
        boolean isUpperIncluded = (interval.isSetUpperIncluded()? interval.getUpperIncluded() : false);

        if (isLowerIncluded && isUpperIncluded)
            isWithinLiUi(value, lower, upper);
        else if (isLowerIncluded && !isUpperIncluded)
            isWithinLiUx(value, lower, upper);
        else if (!isLowerIncluded && isUpperIncluded)
            isWithinLxUi(value, lower, upper);
        else if (!isLowerIncluded && !isUpperIncluded)
            isWithinLxUx(value, lower,upper);
    }

    private static void  compareWithinInterval(Comparable value, ConstraintOccurrences occurrences, Comparable lower, Comparable upper) throws Exception {
        boolean isLowerIncluded = (occurrences.getLowerIncluded() ? occurrences.getLowerIncluded() : false);
        boolean isUpperIncluded = (occurrences.getLowerIncluded() ? occurrences.getUpperIncluded() : false);

        if (isLowerIncluded && isUpperIncluded)
            isWithinLiUi(value, lower, upper);
        else if (isLowerIncluded && !isUpperIncluded)
            isWithinLiUx(value, lower, upper);
        else if (!isLowerIncluded && isUpperIncluded)
            isWithinLxUi(value, lower, upper);
        else if (!isLowerIncluded && !isUpperIncluded)
            isWithinLxUx(value, lower,upper);
    }


    public static void  isWithinBoundaries(Float real, IntervalOfReal intervalOfReal) throws Exception {
        Float lower = (intervalOfReal.isSetLower() ? intervalOfReal.getLower() : Float.MIN_VALUE);
        Float upper = (intervalOfReal.isSetUpper() ? intervalOfReal.getUpper() : Float.MAX_VALUE);

        compareWithinInterval(real, intervalOfReal, lower, upper);
    }

    public static void isWithinBoundaries(Integer integer, IntervalOfInteger intervalOfInteger) throws Exception {
        Integer lower = (intervalOfInteger.isSetLower() ? intervalOfInteger.getLower() : Integer.MIN_VALUE);
        Integer upper = (intervalOfInteger.isSetUpper() ? intervalOfInteger.getUpper() : Integer.MAX_VALUE);

        compareWithinInterval(integer, intervalOfInteger, lower, upper);
    }

    public static void isWithinBoundaries(Integer integer, ConstraintOccurrences occurrences) throws Exception {
        Integer lower = occurrences.getLower();
        Integer upper = occurrences.getUpper();

        compareWithinInterval(integer, occurrences, lower, upper);
    }

    public static void isWithinPrecision(Integer integer, IntervalOfInteger intervalOfInteger) throws Exception {
        if (intervalOfInteger == null)
            return;
        Integer lower = (intervalOfInteger.isSetLower() ? intervalOfInteger.getLower() : Integer.MIN_VALUE);
        Integer upper = (intervalOfInteger.isSetUpper() ? intervalOfInteger.getUpper() : Integer.MAX_VALUE);

        try {
            compareWithinInterval(integer, intervalOfInteger, lower, upper);
        } catch (Exception e){
            throw new IllegalArgumentException("Precision:"+e.getMessage());
        }
    }

    public static void isWithinBoundaries(String rawDate, IntervalOfDate intervalOfDate) throws Exception {

        DateTime valueDate = DateTime.parse(rawDate);

        isWithinBoundaries(valueDate, intervalOfDate);
    }

    public static void isWithinBoundaries(DvDate date, IntervalOfDate intervalOfDate) throws Exception {

        DateTime valueDate = date.getDateTime();

        isWithinBoundaries(valueDate, intervalOfDate);
    }

    public static void isWithinBoundaries(DateTime valueDate, IntervalOfDate intervalOfDate) throws Exception {

        String lower = (intervalOfDate.isSetLower() ? intervalOfDate.getLower() : null);
        String upper = (intervalOfDate.isSetUpper() ? intervalOfDate.getUpper() : null);

        DateTime lowerDate, upperDate;
        //Date massage...
        if (lower != null)
            lowerDate = DateTime.parse(lower);
        else
            lowerDate = new DateTime(Long.MIN_VALUE);

        if (upper != null)
            upperDate = DateTime.parse(upper);
        else
            upperDate = new DateTime(Long.MAX_VALUE);

        compareWithinInterval(valueDate, intervalOfDate, lowerDate, upperDate);
    }

    public static void isWithinBoundaries(String rawDateTime, IntervalOfDateTime intervalOfDateTime) throws Exception {

        DateTime valueDateTime = DateTime.parse(rawDateTime);

        isWithinBoundaries(valueDateTime, intervalOfDateTime);

    }

    public static void isWithinBoundaries(DateTime valueDateTime, IntervalOfDateTime intervalOfDateTime) throws Exception {

        String lower = (intervalOfDateTime.isSetLower() ? intervalOfDateTime.getLower() : null);
        String upper = (intervalOfDateTime.isSetUpper() ? intervalOfDateTime.getUpper() : null);

        DateTime lowerDateTime, upperDateTime;
        //Date massage...
        if (lower != null)
            lowerDateTime = DateTime.parse(lower);
        else
            lowerDateTime = new DateTime(Long.MIN_VALUE);

        if (upper != null)
            upperDateTime = DateTime.parse(upper);
        else
            upperDateTime = new DateTime(Long.MAX_VALUE);

        compareWithinInterval(valueDateTime, intervalOfDateTime, lowerDateTime, upperDateTime);
    }

    public static void  isWithinBoundaries(String rawTime, IntervalOfTime intervalOfTime) throws Exception {
        DateTime valueTime = DateTime.parse(rawTime);
        isWithinBoundaries(valueTime, intervalOfTime);
    }

    public static void  isWithinBoundaries(DateTime valueTime, IntervalOfTime intervalOfTime) throws Exception {

        String lower = (intervalOfTime.isSetLower() ? intervalOfTime.getLower() : null);
        String upper = (intervalOfTime.isSetUpper() ? intervalOfTime.getUpper() : null);

        DateTime lowerTime, upperTime;
        //Date massage...
        if (lower != null)
            lowerTime = DateTime.parse(lower);
        else
            lowerTime = new DateTime(Long.MIN_VALUE);

        if (upper != null)
            upperTime = DateTime.parse(upper);
        else
            upperTime = new DateTime(Long.MAX_VALUE);

        compareWithinInterval(valueTime, intervalOfTime, lowerTime, upperTime);
    }

    public static void isWithinBoundaries(String rawDuration, IntervalOfDuration intervalOfDuration) throws Exception {

        Duration valueDuration = Duration.parse(rawDuration);
        isWithinBoundaries(valueDuration, intervalOfDuration);

    }

    public static void isWithinBoundaries(Duration valueDuration, IntervalOfDuration intervalOfDuration) throws Exception {

        String lower = (intervalOfDuration.isSetLower() ? intervalOfDuration.getLower() : null);
        String upper = (intervalOfDuration.isSetUpper() ? intervalOfDuration.getUpper() : null);

        Duration lowerDuration, upperDuration;
        //Date massage...
        if (lower != null)
            lowerDuration = Duration.parse(lower);
        else
            lowerDuration = new Duration(Long.MIN_VALUE);

        if (upper != null)
            upperDuration = Duration.parse(upper);
        else
            upperDuration = new Duration(Long.MAX_VALUE);

        compareWithinInterval(valueDuration, intervalOfDuration, lowerDuration, upperDuration);
    }

    public static boolean isOptional(IntervalOfInteger intervalOfInteger) {
        try {
            isWithinBoundaries(0, intervalOfInteger);
        } catch (Exception e){
            return false;
        }
        return true;
    }

    public static IntervalOfInteger makeInterval(org.openehr.rm.support.basic.Interval<Integer> integerInterval){
        IntervalOfInteger intervalOfInteger = IntervalOfInteger.Factory.newInstance();
        intervalOfInteger.setLower(integerInterval.getLower());
        intervalOfInteger.setUpper(integerInterval.getUpper());
        intervalOfInteger.setLowerIncluded(integerInterval.isLowerIncluded());
        intervalOfInteger.setUpperIncluded(integerInterval.isUpperIncluded());
        return intervalOfInteger;
    }

    public static String toString(IntervalOfInteger intervalOfInteger){
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("[");
        if (intervalOfInteger.isSetLower())
            stringBuffer.append(intervalOfInteger.getLower());
        else
            stringBuffer.append("*");
        stringBuffer.append("..");
        if (intervalOfInteger.isSetUpper())
            stringBuffer.append(intervalOfInteger.getUpper());
        else
            stringBuffer.append("*");
        stringBuffer.append("]");

        return stringBuffer.toString();
    }

    public static String toString(org.openehr.rm.support.basic.Interval<Integer> intervalOfInteger){
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("[");
        if (intervalOfInteger.getLower() != null && intervalOfInteger.getLower() != Integer.MIN_VALUE)
            stringBuffer.append(intervalOfInteger.getLower());
        else
            stringBuffer.append("*");
        stringBuffer.append("..");
        if (intervalOfInteger.getUpper() != null && intervalOfInteger.getUpper() != Integer.MAX_VALUE)
            stringBuffer.append(intervalOfInteger.getUpper());
        else
            stringBuffer.append("*");
        stringBuffer.append("]");

        return stringBuffer.toString();
    }

}
