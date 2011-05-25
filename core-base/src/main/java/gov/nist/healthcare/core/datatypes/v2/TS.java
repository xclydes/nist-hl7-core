/*
 * NIST Healthcare Core
 * TS.java Mar 03, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.datatypes.v2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TS {
    private String dateTime;

    public TS() {
        dateTime = null;
    }

    public TS(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        dateTime = sdf.format(date);
    }

    public TS(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((dateTime == null) ? 0 : dateTime.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TS other = (TS) obj;
        if (dateTime == null) {
            if (other.dateTime != null) {
                return false;
            }
        } else if (!dateTime.equalsIgnoreCase(other.dateTime)) {
            return false;
        }
        return true;
    }

    public String getDateTime() {
        return dateTime;
    }

    public Date getDateTimeasDate() throws ParseException {
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        date = sdf.parse(dateTime);
        return date;
    }

    public Date getDateTimeAsDate(SimpleDateFormat sdf) throws ParseException {
        Date date = null;
        if (sdf != null) {
            date = sdf.parse(dateTime);
        }
        return date;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Check if the given String is a valid TS that follows this pattern :
     * YYYYMMDD[HH[:MM[:SS[.S[S[S[S]]]]]]][+/-ZZZZ]
     * 
     * @param dateTime
     * @return true if the given String is a valid TS
     */
    public static boolean isValidTS(String dateTime) {
        if (dateTime == null) {
            return false;
        }
        String regex = "([12]\\d{3}" + "((0[1-9]|1[0-2])"
                + "((0[1-9]|[12]\\d|3[01])" + "(([01]\\d|2[0-3])" + "([0-5]\\d"
                + "([0-5]\\d" + "(\\.\\d\\d?\\d?\\d?)?)?)?)?))"
                + "((\\+|\\-)([01]\\d|2[0-3])[0-5]\\d)?)?";
        return dateTime.matches(regex);
    }

    /**
     * Converts the given dateTime String in a SimpleDateFormat object
     * 
     * @return null if dateTime is not valid, a SimpleDateFormat object
     *         otherwise
     * @throws ParseException
     */
    public SimpleDateFormat getSimpleDateFormat() throws ParseException {
        if (dateTime == null) {
            return null;
        }
        String regex = "" + "(?:([12]\\d{3})" + "(?:(0[1-9]|1[0-2])"
                + "(?:(0[1-9]|[12]\\d|3[01])" + "(?:([01]\\d|2[0-3])"
                + "(?:([0-5]\\d)" + "(?:([0-5]\\d)"
                + "(?:\\.(\\d\\d?\\d?\\d?))?)?)?)?))"
                + "(?:(\\+|\\-)([01]\\d|2[0-3])([0-5]\\d))?)?";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(dateTime);
        if (m.matches()) {
            // String year = m.group(1);
            // String month = m.group(2);
            // String day = m.group(3);
            String hours = m.group(4);
            String minutes = m.group(5);
            String seconds = m.group(6);
            String millis = m.group(7);
            String tz1 = m.group(8);
            String tz2 = m.group(9);
            String tz3 = m.group(10);
            StringBuffer buf = new StringBuffer();

            /* Date */
            buf.append("yyyy");
            buf.append("MM");
            buf.append("dd");

            /* Time */
            if (hours != null) {
                buf.append("HH");
                if (minutes != null) {
                    buf.append("mm");
                    if (seconds != null) {
                        buf.append("ss");
                        if (millis != null) {
                            buf.append(".");
                            for (int i = 0; i < millis.length(); i++) {
                                buf.append("S");
                            }
                        }
                    }
                }
            }
            /* Time zone */
            if (tz1 != null && tz2 != null && tz3 != null) {
                buf.append("Z");
            }
            return new SimpleDateFormat(buf.toString());
        }
        throw new ParseException("Impossible to parse TS : " + dateTime, 0);
    }

    public Date getNext() throws ParseException {
        Calendar c1 = Calendar.getInstance();
        Date d = getDateTimeAsDate(getSimpleDateFormat());
        if (d == null) {
            return null;
        }
        c1.setTime(d);
        String regex = "" + "(?:([12]\\d{3})" + "(?:(0[1-9]|1[0-2])"
                + "(?:(0[1-9]|[12]\\d|3[01])" + "(?:([01]\\d|2[0-3])"
                + "(?:([0-5]\\d)" + "(?:([0-5]\\d)"
                + "(?:\\.(\\d\\d?\\d?\\d?))?)?)?)?))"
                + "(?:(\\+|\\-)([01]\\d|2[0-3])([0-5]\\d))?)?";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(dateTime);
        if (m.matches()) {
            // String year = m.group(1);
            String month = m.group(2);
            String day = m.group(3);
            String hours = m.group(4);
            String minutes = m.group(5);
            String seconds = m.group(6);
            String millis = m.group(7);
            // String tz1 = m.group(8);
            // String tz2 = m.group(9);
            // String tz3 = m.group(10);
            if (month == null) {
                c1.add(Calendar.YEAR, 1);
            } else if (day == null) {
                c1.add(Calendar.MONTH, 1);
            } else if (hours == null) {
                c1.add(Calendar.DAY_OF_YEAR, 1);
            } else if (minutes == null) {
                c1.add(Calendar.HOUR, 1);
            } else if (seconds == null) {
                c1.add(Calendar.MINUTE, 1);
            } else if (millis == null) {
                c1.add(Calendar.SECOND, 1);
            }
        }
        return c1.getTime();
    }

}
