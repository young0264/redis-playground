package org.example.redis.week1.spring.controller;

import org.example.redis.week1.spring.service.TodoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todo")
public class TodoController {
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    public void addTodo(@RequestParam String task) {
        todoService.addTodo(task);
    }

    @GetMapping
    public List<String> getTodos() {
        return todoService.getTodos();
    }

    @DeleteMapping
    public String removeTodo() {
        return todoService.removeTodo();
    }
}