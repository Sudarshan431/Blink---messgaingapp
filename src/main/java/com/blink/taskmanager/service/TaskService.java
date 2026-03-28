package com.blink.taskmanager.service;

import com.blink.taskmanager.dto.TaskRequest;
import com.blink.taskmanager.dto.TaskResponse;
import com.blink.taskmanager.exception.ResourceNotFoundException;
import com.blink.taskmanager.model.AppUser;
import com.blink.taskmanager.model.Task;
import com.blink.taskmanager.model.TaskStatus;
import com.blink.taskmanager.repository.TaskRepository;
import com.blink.taskmanager.repository.UserRepository;
import com.blink.taskmanager.service.notification.NotificationService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @CacheEvict(value = {"tasks", "taskPages"}, allEntries = true)
    public TaskResponse createTask(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setAssignee(resolveAssignee(request.getAssigneeId()));

        Task saved = taskRepository.save(task);
        if (saved.getAssignee() != null) {
            notificationService.sendTaskAssignedNotification(saved);
        }

        return toResponse(saved);
    }

    @Cacheable(value = "taskPages", key = "T(String).format('%d|%d|%s|%s|%s|%s|%s', #page, #size, #sortBy, #direction, #status, #assigneeId, #keyword)")
    public Page<TaskResponse> getTasks(int page, int size, String sortBy, String direction,
                                       TaskStatus status, Long assigneeId, String keyword) {
        Sort sort = Sort.by(sortBy);
        sort = "desc".equalsIgnoreCase(direction) ? sort.descending() : sort.ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return taskRepository.search(status, assigneeId, normalizedKeyword, pageable)
                .map(this::toResponse);
    }

    @Cacheable(value = "tasks", key = "#id")
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return toResponse(task);
    }

    @CachePut(value = "tasks", key = "#id")
    @CacheEvict(value = "taskPages", allEntries = true)
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        TaskStatus previousStatus = task.getStatus();
        Long previousAssigneeId = task.getAssignee() != null ? task.getAssignee().getId() : null;

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setAssignee(resolveAssignee(request.getAssigneeId()));

        Task saved = taskRepository.save(task);

        Long currentAssigneeId = saved.getAssignee() != null ? saved.getAssignee().getId() : null;
        if (currentAssigneeId != null && !currentAssigneeId.equals(previousAssigneeId)) {
            notificationService.sendTaskAssignedNotification(saved);
        }
        if (previousStatus != saved.getStatus()) {
            notificationService.sendTaskStatusChangedNotification(saved);
        }

        return toResponse(saved);
    }

    @CacheEvict(value = {"tasks", "taskPages"}, allEntries = true)
    public TaskResponse assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        task.setAssignee(user);
        Task saved = taskRepository.save(task);

        notificationService.sendTaskAssignedNotification(saved);
        return toResponse(saved);
    }

    @CacheEvict(value = {"tasks", "taskPages"}, allEntries = true)
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        taskRepository.delete(task);
    }

    private AppUser resolveAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + assigneeId));
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());

        if (task.getAssignee() != null) {
            response.setAssigneeId(task.getAssignee().getId());
            response.setAssigneeUsername(task.getAssignee().getUsername());
        }

        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
    }
}
