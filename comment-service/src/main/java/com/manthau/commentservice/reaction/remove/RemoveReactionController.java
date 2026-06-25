package com.manthau.commentservice.reaction.remove;

import com.manthau.commentservice.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RemoveReactionController {

    private final RemoveReactionHandler handler;

    @DeleteMapping("/api/reactions")
    public ResponseEntity<Void> remove(@Valid @RequestBody RemoveReactionRequest req) {
        handler.handle(req, UserPrincipal.current());
        return ResponseEntity.noContent().build();
    }
}
