package de.immotool.haeusertool.docs;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/files")
public class FileController {

    private final DocumentService service;
    private final DocumentRepository repo;

    public FileController(DocumentService service, DocumentRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    /**
     * Download eines Dokuments.
     * Beispiel: GET /files/123  (lädt als Anhang)
     *           GET /files/123?inline=true  (versucht inline anzuzeigen, z.B. PDF im Browser)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id,
                                             @RequestParam(name = "inline", defaultValue = "false") boolean inline) {
        var doc = repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Dokument nicht gefunden: " + id));

        Resource resource = service.loadAsResource(id);
        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Datei nicht vorhanden: " + doc.getStoragePath());
        }

        // Content-Type sauber setzen (Fallback auf application/octet-stream)
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            if (doc.getContentType() != null && !doc.getContentType().isBlank()) {
                mediaType = MediaType.parseMediaType(doc.getContentType());
            }
        } catch (Exception ignore) { /* bei ungültigem MIME-Typ fallback nutzen */ }

        // Content-Disposition: attachment (Download) oder inline (Browser-Vorschau)
        String dispositionType = inline ? "inline" : "attachment";
        ContentDisposition cd = ContentDisposition.builder(dispositionType)
                .filename(doc.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(cd);
        headers.setContentType(mediaType);
        if (doc.getSizeBytes() != null && doc.getSizeBytes() >= 0) {
            headers.setContentLength(doc.getSizeBytes());
        }

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    /**
     * Bequemer Alias für Inline-Anzeige: /files/{id}/inline
     */
    @GetMapping("/{id}/inline")
    public ResponseEntity<Resource> viewInline(@PathVariable Long id) {
        return download(id, true);
    }
}

