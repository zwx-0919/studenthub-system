package com.it.zwx.studenthub_system.service;

import java.util.List;
import java.util.Map;

public interface ReminderService {
    void pushLoginReminders(Integer userId);

    Map<String, List<String>> getUpcomingReminderText(Integer userId);
}

