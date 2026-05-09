package com.manthau.commentservice.reaction.remove;

import com.manthau.commentservice.reaction.domain.ReactionId;
import com.manthau.commentservice.reaction.domain.ReactionRepository;
import com.manthau.commentservice.shared.exception.NotFoundException;
import com.manthau.commentservice.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveReactionHandler {

    private final ReactionRepository reactionRepository;

    @Transactional
    public void handle(RemoveReactionRequest req, UserPrincipal user) {
        ReactionId id = new ReactionId(user.userId(), req.targetId(), req.targetType());
        if (!reactionRepository.existsById(id)) {
            throw new NotFoundException("Reaction not found");
        }
        reactionRepository.deleteById(id);
    }
}
