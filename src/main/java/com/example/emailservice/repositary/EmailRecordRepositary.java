package com.example.emailservice.repositary;

import com.example.emailservice.model.EmailRecord;
import com.example.emailservice.model.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmailRecordRepositary extends JpaRepository<EmailRecord,String> {
    Optional<EmailRecord> findByIdAndClientId(String id,String clientId);

    @Query("SELECT e FROM EmailRecord e "
            + "WHERE e.emailStatus = :emailStatus AND e.attempts < :maxAttempts "
            + "ORDER BY e.createdAt ASC")
    List<EmailRecord> findPendingEmails(@Param("emailStatus") EmailStatus emailStatus,
                                        @Param("maxAttempts") int maxAttempts);
}
