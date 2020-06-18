// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.HashSet;
import java.util.ArrayList;


public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long reqEventDuration = request.getDuration();
    Collection<String> reqAttendees = request.getAttendees();
    ArrayList<TimeRange> queryResults = new ArrayList<TimeRange>();
    queryResults.add(TimeRange.WHOLE_DAY);
    // queryResults.add(TimeRange.fromStartDuration(12, 13));
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
        queryResults.clear();
    }
    for (Event event : events) {

        boolean noCommonAttendees = true;
        for (String requestAttendee : reqAttendees) {
            for (String eventAttendee : event.getAttendees()) {
                if (eventAttendee == requestAttendee) noCommonAttendees = false;
            }
        }
        if (noCommonAttendees) continue;

        for (int i = 0; i<queryResults.size(); i++) {
            TimeRange timeSlot = queryResults.get(i);
            if (timeSlot.equals(event.getWhen())) {
                queryResults.remove(i);
            }
            else {
                if (timeSlot.contains(event.getWhen().start()) && timeSlot.contains(event.getWhen().end())) {
                    if (timeSlot.start() == event.getWhen().start()) {
                        queryResults.set(i, TimeRange.fromStartEnd(event.getWhen().end(), timeSlot.end(), false));
                    }
                    else {
                        queryResults.set(i, TimeRange.fromStartEnd(timeSlot.start(), event.getWhen().start(), false));
                        queryResults.add(TimeRange.fromStartEnd(event.getWhen().end(), timeSlot.end(), false));
                    }
                }
                else if (timeSlot.contains(event.getWhen().start())) {
                    queryResults.set(i, TimeRange.fromStartEnd(timeSlot.start(), event.getWhen().start(), false));
                }
                else if (timeSlot.contains(event.getWhen().end())) {
                    queryResults.set(i, TimeRange.fromStartEnd(event.getWhen().end(), timeSlot.end(), false));
                }
            }
        }
    }
    return queryResults;
  }
//   private Collection<TimeRange> allFreeTimes(Collection<Event> events) {

//   }
}
