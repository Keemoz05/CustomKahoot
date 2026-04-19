package com.syed.QuizYa.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "event_sessions")
public class EventSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "session_number", nullable = false)
    private Integer sessionNumber = 1;

    @Column(name = "peak_guest_count")
    private Integer peakGuestCount;

    @Column(name = "total_questions_played")
    private Integer totalQuestionsPlayed;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public Integer getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(Integer sessionNumber) { this.sessionNumber = sessionNumber; }
    public Integer getPeakGuestCount() { return peakGuestCount; }
    public void setPeakGuestCount(Integer peakGuestCount) { this.peakGuestCount = peakGuestCount; }
    public Integer getTotalQuestionsPlayed() { return totalQuestionsPlayed; }
    public void setTotalQuestionsPlayed(Integer totalQuestionsPlayed) { this.totalQuestionsPlayed = totalQuestionsPlayed; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public OffsetDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(OffsetDateTime endedAt) { this.endedAt = endedAt; }
}
