package com.manthau.commentservice.reaction.add;

import com.manthau.commentservice.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AddReactionController {

    private final AddReactionHandler handler;

    @PostMapping("/api/reactions")
    public ResponseEntity<Void> add(@Valid @RequestBody AddReactionRequest req) {
        handler.handle(req, UserPrincipal.current());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
