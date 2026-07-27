package br.com.locaweb.relatorioclientes.instagramcheck.controller;

import br.com.locaweb.relatorioclientes.instagramcheck.model.PerfilInstagram;
import br.com.locaweb.relatorioclientes.instagramcheck.repository.PerfilInstagramRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/instagramcheck/perfis")
public class PerfilInstagramApiController {

    private final PerfilInstagramRepository repository;

    public PerfilInstagramApiController(PerfilInstagramRepository repository) {
        this.repository = repository;
    }

    // ---------- Listar tudo ----------
    @GetMapping
    public List<PerfilInstagram> listar() {
        return repository.findAll();
    }

    // ---------- Importar (merge sem duplicar / sem apagar progresso) ----------
    public static class ImportRowDTO {
        public String username;
        public String nome;
        public String unidade;
        public String cidade;
        public Integer seguidores;
        public String motivo;
        public String status;
    }

    public static class ImportResultDTO {
        public int adicionados;
        public int atualizados;

        public ImportResultDTO(int adicionados, int atualizados) {
            this.adicionados = adicionados;
            this.atualizados = atualizados;
        }
    }

    @PostMapping("/importar")
    @Transactional("instagramTransactionManager")
    public ImportResultDTO importar(@RequestBody List<ImportRowDTO> linhas) {
        int adicionados = 0;
        int atualizados = 0;

        for (ImportRowDTO linha : linhas) {
            if (linha.username == null || linha.username.isBlank()) continue;
            String username = linha.username.trim().toLowerCase();
            String statusNormalizado = normalizeStatus(linha.status);
            String motivoNormalizado = normalizeMotivo(linha.motivo);

            Optional<PerfilInstagram> existenteOpt = repository.findByUsername(username);
            if (existenteOpt.isPresent()) {
                PerfilInstagram existente = existenteOpt.get();
                boolean mudou = false;
                if (isBlank(existente.getNome()) && !isBlank(linha.nome)) {
                    existente.setNome(linha.nome.trim());
                    mudou = true;
                }
                if (isBlank(existente.getUnidade()) && !isBlank(linha.unidade)) {
                    existente.setUnidade(linha.unidade.trim());
                    mudou = true;
                }
                if (isBlank(existente.getCidade()) && !isBlank(linha.cidade)) {
                    existente.setCidade(linha.cidade.trim());
                    mudou = true;
                }
                // Só preenche seguidores/motivo/status se o perfil ainda não tiver progresso
                // (nunca sobrescreve uma checagem já feita ao vivo no sistema).
                if (existente.getSeguidores() == null && linha.seguidores != null) {
                    existente.setSeguidores(linha.seguidores);
                    mudou = true;
                }
                if (existente.getMotivo() == null && motivoNormalizado != null) {
                    existente.setMotivo(motivoNormalizado);
                    mudou = true;
                }
                if ("pendente".equals(existente.getStatus()) && !"pendente".equals(statusNormalizado)) {
                    existente.setStatus(statusNormalizado);
                    existente.setCheckedEm(LocalDateTime.now());
                    mudou = true;
                }
                if (mudou) {
                    repository.save(existente);
                    atualizados++;
                }
            } else {
                PerfilInstagram novo = new PerfilInstagram();
                novo.setUsername(username);
                novo.setNome(blankToNull(linha.nome));
                novo.setUnidade(blankToNull(linha.unidade));
                novo.setCidade(blankToNull(linha.cidade));
                novo.setSeguidores(linha.seguidores);
                novo.setMotivo(motivoNormalizado);
                novo.setStatus(statusNormalizado);
                if (!"pendente".equals(statusNormalizado)) {
                    novo.setCheckedEm(LocalDateTime.now());
                }
                repository.save(novo);
                adicionados++;
            }
        }
        return new ImportResultDTO(adicionados, atualizados);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String blankToNull(String s) {
        if (isBlank(s)) return null;
        return s.trim();
    }

    private String stripAccents(String s) {
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private String normalizeStatus(String raw) {
        if (isBlank(raw)) return "pendente";
        String s = stripAccents(raw.trim().toLowerCase());
        if (s.equals("aprovado")) return "aprovado";
        if (s.equals("reprovado")) return "reprovado";
        return "pendente";
    }

    private String normalizeMotivo(String raw) {
        if (isBlank(raw)) return null;
        String s = raw.trim();
        if (s.equals("—") || s.equals("-")) return null;
        String norm = stripAccents(s.toLowerCase());
        switch (norm) {
            case "privado": return "Privado";
            case "inapropriado": return "Inapropriado";
            case "influenciador": return "Influenciador";
            case "adequado": return "Adequado";
            default: return null;
        }
    }

    // ---------- Atualização parcial (seguidores, publica, status, motivo) ----------
    @PatchMapping("/{username}")
    @Transactional("instagramTransactionManager")
    public ResponseEntity<PerfilInstagram> atualizar(@PathVariable String username, @RequestBody Map<String, Object> updates) {
        Optional<PerfilInstagram> opt = repository.findByUsername(username.trim().toLowerCase());
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        PerfilInstagram perfil = opt.get();

        if (updates.containsKey("seguidores")) {
            Object v = updates.get("seguidores");
            perfil.setSeguidores(v == null ? null : Integer.valueOf(String.valueOf(v)));
        }
        if (updates.containsKey("publica")) {
            Object v = updates.get("publica");
            perfil.setPublica(v == null ? null : Boolean.valueOf(String.valueOf(v)));
        }
        if (updates.containsKey("motivo")) {
            Object v = updates.get("motivo");
            perfil.setMotivo(v == null ? null : String.valueOf(v));
        }
        if (updates.containsKey("status")) {
            Object v = updates.get("status");
            perfil.setStatus(v == null ? "pendente" : String.valueOf(v));
            perfil.setCheckedEm(LocalDateTime.now());
        }

        repository.save(perfil);
        return ResponseEntity.ok(perfil);
    }

    // ---------- Remover um perfil ----------
    @DeleteMapping("/{username}")
    @Transactional("instagramTransactionManager")
    public ResponseEntity<Void> remover(@PathVariable String username) {
        repository.findByUsername(username.trim().toLowerCase()).ifPresent(repository::delete);
        return ResponseEntity.noContent().build();
    }

    // ---------- Limpar tudo ----------
    @DeleteMapping
    @Transactional("instagramTransactionManager")
    public ResponseEntity<Void> limparTudo() {
        repository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
