package com.jubensha.booking.controller;

import com.jubensha.booking.entity.Script;
import com.jubensha.booking.service.ScriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scripts")
@RequiredArgsConstructor
public class ScriptController {

    private final ScriptService scriptService;

    @GetMapping
    public ResponseEntity<List<Script>> getAllScripts() {
        return ResponseEntity.ok(scriptService.getAllScripts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Script> getScriptById(@PathVariable Long id) {
        return ResponseEntity.ok(scriptService.getScriptById(id));
    }

    @PostMapping
    public ResponseEntity<Script> createScript(@RequestBody Script script) {
        return ResponseEntity.ok(scriptService.createScript(script));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Script> updateScript(@PathVariable Long id, @RequestBody Script script) {
        return ResponseEntity.ok(scriptService.updateScript(id, script));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScript(@PathVariable Long id) {
        scriptService.deleteScript(id);
        return ResponseEntity.noContent().build();
    }
}
