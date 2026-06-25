package com.manthau.commentservice.reaction.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionRepository extends JpaRepository<Reaction, ReactionId> {}
