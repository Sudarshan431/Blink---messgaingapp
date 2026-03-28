package com.blink.taskmanager.service.notification;

import com.blink.taskmanager.model.Task;

public interface NotificationService {
    void sendTaskAssignedNotification(Task task);
    void sendTaskStatusChangedNotification(Task task);
}
