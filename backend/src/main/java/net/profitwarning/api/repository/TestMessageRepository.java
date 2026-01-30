package net.profitwarning.api.repository;

import net.profitwarning.api.model.TestMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestMessageRepository extends JpaRepository<TestMessage, Long> {
}
