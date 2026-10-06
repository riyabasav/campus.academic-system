package com.campus.events.repository;

import com.campus.events.model.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Optional<EventRegistration> findByTicketCode(String ticketCode);
    boolean existsByEventIdAndStudentEmailIgnoreCase(Long eventId, String studentEmail);
    List<EventRegistration> findByStudentEmailIgnoreCase(String studentEmail);
}
