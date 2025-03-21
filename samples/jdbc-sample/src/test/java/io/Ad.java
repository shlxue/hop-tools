package io;

import java.time.*;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;

public class Ad {

  static int timesLimit = 220;

  public static void main(String[] args) {
    // showDst("Asia/Shanghai", 1097, 9, 15, 2, 0);
    showDst("America/New_York", 1097, 9, 15, 2, 0);
  }

  private static void showDst(String zone, int year, int month, int dayOfMonth, int hour, int minute) {
    ZoneId zoneId = ZoneId.of(zone);
    // Get the ZoneRules for the specified ZoneId
    ZoneRules zoneRules = zoneId.getRules();

    ZonedDateTime now = ZonedDateTime.of(LocalDateTime.of(year, month, dayOfMonth, hour, minute), zoneId);

    System.out.println("Current time in Asia/Shanghai: " + now);

    // Check if DST is in effect for the current date
    boolean isDST = zoneRules.isDaylightSavings(now.toInstant());
    System.out.println("Is DST in effect? " + isDST);

    // Display the transition rules
    System.out.println("Daylight Saving Time offset: " + zoneRules.getDaylightSavings(now.toInstant()));

    // Display the next DST transition
    System.out.println("Next DST transition: " + zoneRules.nextTransition(now.toInstant()));

    // Display the previous DST transition
    System.out.println("Previous DST transition: " + zoneRules.previousTransition(now.toInstant()));

    ZoneOffsetTransition next = zoneRules.nextTransition(now.toInstant());
    if (next != null && timesLimit > 0) {
      timesLimit--;
      LocalDateTime dateTime = next.getDateTimeAfter();
      int nextYear = dateTime.getYear();
      int nextMonth = dateTime.getMonthValue();
      int nextDay = dateTime.getDayOfMonth();
      if (nextDay > 28) {
        nextDay =1;
        nextMonth++;
      } else {
        nextDay++;
      }
      if (nextMonth > 12) {
        nextMonth = 1;
        nextYear++;
      }
      showDst(zone, nextYear, nextMonth, nextDay, hour, minute);
    }
  }

  private static void m1() {
    ZonedDateTime springForward = ZonedDateTime.of(2023, 3, 12, 2, 0, 0, 0, ZoneId.of("America/New_York"));
    ZonedDateTime fallBack = ZonedDateTime.of(2023, 11, 5, 2, 0, 0, 0, ZoneId.of("America/New_York"));

    System.out.println("Spring Forward: " + springForward);
    System.out.println("Fall Back: " + fallBack);
    springForward = ZonedDateTime.of(2023, 3, 12, 2, 0, 0, 0, ZoneId.of("Asia/Shanghai"));
    fallBack = ZonedDateTime.of(2023, 11, 5, 2, 0, 0, 0, ZoneId.of("Asia/Shanghai"));
    System.out.println("Spring Forward: " + springForward);
    System.out.println("Fall Back: " + fallBack);
    System.out.println(ZoneId.of("America/New_York"));
    System.out.println(ZonedDateTime.of(2025, 3, 9, 2, 30, 0, 0, ZoneId.of("America/New_York")));
    System.out.println(ZonedDateTime.of(2025, 3, 9, 2, 30, 0, 0, ZoneId.of("Asia/Shanghai")));
  }

  private static void m3(String id, int year, int month, int dayOfMonth, int hour, int minute) {
    ZonedDateTime springForward = ZonedDateTime.of(year, month, dayOfMonth, hour, minute, 0, 0, ZoneId.of(id));
    ZonedDateTime fallBack = ZonedDateTime.of(year, month, dayOfMonth, hour, minute, 0, 0, ZoneId.of(id));

    System.out.println("Spring Forward: " + springForward);
    System.out.println("Fall Back: " + fallBack);
  }

}
