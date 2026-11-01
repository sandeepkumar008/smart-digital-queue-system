package com.smartqueue.dto;

public class CounterActionDto {
    private Long counterId;
    private Long ticketId;
    private String action; // CALL_NEXT, COMPLETE, NO_SHOW, CANCEL, SET_OFFLINE, SET_AVAILABLE

    public CounterActionDto() {}

    public CounterActionDto(Long counterId, Long ticketId, String action) {
        this.counterId = counterId;
        this.ticketId = ticketId;
        this.action = action;
    }

    public Long getCounterId() {
        return counterId;
    }

    public void setCounterId(Long counterId) {
        this.counterId = counterId;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
