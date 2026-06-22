package com.jubensha.booking.service;

import com.jubensha.booking.entity.Script;
import com.jubensha.booking.repository.ScriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScriptService {

    private final ScriptRepository scriptRepository;

    public List<Script> getAllScripts() {
        return scriptRepository.findAll();
    }

    public Script getScriptById(Long id) {
        return scriptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("剧本不存在: " + id));
    }

    public Script createScript(Script script) {
        return scriptRepository.save(script);
    }

    public Script updateScript(Long id, Script script) {
        Script existing = getScriptById(id);
        existing.setName(script.getName());
        existing.setTotalPlayers(script.getTotalPlayers());
        existing.setMinMale(script.getMinMale());
        existing.setMinFemale(script.getMinFemale());
        existing.setPrice(script.getPrice());
        existing.setDuration(script.getDuration());
        existing.setDescription(script.getDescription());
        return scriptRepository.save(existing);
    }

    public void deleteScript(Long id) {
        scriptRepository.deleteById(id);
    }
}
