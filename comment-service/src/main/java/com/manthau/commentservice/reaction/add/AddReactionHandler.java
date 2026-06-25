package com.manthau.commentservice.reaction.add;

import com.manthau.commentservice.reaction.domain.Reaction;
import com.manthau.commentservice.reaction.domain.ReactionId;
import com.manthau.commentservice.reaction.domain.ReactionRepository;
import com.manthau.commentservice.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddReactionHandler {

    private final ReactionRepository reactionRepository;

    @Transactional
    public void handle(AddReactionRequest req, UserPrincipal user) {
        ReactionId id = new ReactionId(user.userId(), req.targetId(), req.targetType());

        // upsert: replace existing reaction type if already reacted
        Reaction reaction = reactionRepository.findById(id)
                .orElse(Reaction.builder().id(id).build());

        reaction.setType(req.type());
        reactionRepository.save(reaction);
    }
}
