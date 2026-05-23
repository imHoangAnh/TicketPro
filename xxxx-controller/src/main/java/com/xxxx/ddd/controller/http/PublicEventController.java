package com.xxxx.ddd.controller.http;

import com.xxxx.ddd.application.model.EventDetailDTO;
import com.xxxx.ddd.application.model.EventSummaryDTO;
import com.xxxx.ddd.application.service.event.EventAppException;
import com.xxxx.ddd.application.service.event.EventAppService;
import com.xxxx.ddd.controller.model.enums.ResultUtil;
import com.xxxx.ddd.controller.model.vo.ResultMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class PublicEventController {

    private final EventAppService eventAppService;

    public PublicEventController(EventAppService eventAppService) {
        this.eventAppService = eventAppService;
    }

    @GetMapping
    public ResponseEntity<ResultMessage<List<EventSummaryDTO>>> listActiveEvents() {
        return ResponseEntity.ok(ResultUtil.data(eventAppService.listActiveEvents()));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ResultMessage<EventDetailDTO>> getEvent(@PathVariable Long eventId) {
        try {
            return ResponseEntity.ok(ResultUtil.data(eventAppService.getActiveEventDetail(eventId)));
        } catch (EventAppException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResultUtil.error(404, e.getMessage()));
        }
    }
}
