package com.xxxx.ddd.application.service.ticket.impl;

import com.xxxx.ddd.application.mapper.TicketDetailMapper;
import com.xxxx.ddd.application.mapper.TicketMapper;
import com.xxxx.ddd.application.model.TicketDTO;
import com.xxxx.ddd.application.model.command.CreateTicketDetailCommand;
import com.xxxx.ddd.application.model.command.CreateTicketCommand;
import com.xxxx.ddd.application.model.command.UpdateTicketCommand;
import com.xxxx.ddd.application.port.cache.KeyValueCachePort;
import com.xxxx.ddd.application.service.ticket.TicketAppService;
import com.xxxx.ddd.domain.model.entity.Ticket;
import com.xxxx.ddd.domain.model.entity.TicketDetail;
import com.xxxx.ddd.domain.service.TicketDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TicketAppServiceImpl implements TicketAppService {

    private static final String TICKET_CACHE_PREFIX = "PRO_TICKET:";
    private static final String TICKET_ITEM_CACHE_PREFIX = "PRO_TICKET:ITEM:";

    @Autowired
    private TicketDomainService ticketDomainService;

    @Autowired
    private KeyValueCachePort cache;

    @Override
    public TicketDTO createTicket(CreateTicketCommand createRequest, CreateTicketDetailCommand createDetailRequest) {
        // 1. Convert Command → Entity (via Mapper)
        Ticket ticket = TicketMapper.toEntity(createRequest);
        TicketDetail ticketDetail = TicketDetailMapper.toEntity(createDetailRequest);

        // 2. Call Domain Service (với business logic validation + persist)
        Ticket createdTicket = ticketDomainService.createTicket(ticket, ticketDetail);

        // 3. Write-Through Cache: set Redis ngay sau khi DB thành công
        this.cacheTicket(createdTicket);
        this.cacheTicketDetail(ticketDetail); // ticketDetail đã có ID từ JPA save

        log.info("Created & cached ticket ID: {}, ticketDetail ID: {}",
                createdTicket.getId(), ticketDetail.getId());

        // 4. Convert Entity → DTO
        return TicketMapper.toDTO(createdTicket);
    }

    @Override
    public TicketDTO getTicketById(Long ticketId) {
        Ticket ticket = ticketDomainService.getTicketById(ticketId);
        TicketDTO dto = TicketMapper.toDTO(ticket);
        
        // Enrich with price and stock from first detail
        java.util.List<com.xxxx.ddd.domain.model.entity.TicketDetail> details = 
            ticketDetailRepository.findByActivityId(ticketId);
        if (!details.isEmpty()) {
            com.xxxx.ddd.domain.model.entity.TicketDetail firstDetail = details.get(0);
            dto.setPriceOriginal(firstDetail.getPriceOriginal());
            dto.setPriceFlash(firstDetail.getPriceFlash());
            dto.setStockAvailable(firstDetail.getStockAvailable());
            dto.setStockInitial(firstDetail.getStockInitial());
        }
        
        return dto;
    }

    @Override
    public TicketDTO updateTicket(Long ticketId, UpdateTicketCommand updateRequest) {
        Ticket updatedTicket = new Ticket();
        updatedTicket.setName(updateRequest.getTitle());
        updatedTicket.setDescription(updateRequest.getDescription());
        updatedTicket.setStartTime(updateRequest.getValidFrom());
        updatedTicket.setEndTime(updateRequest.getValidTo());

        Ticket ticket = ticketDomainService.updateTicket(ticketId, updatedTicket);

        // Update cache sau khi update DB
        this.cacheTicket(ticket);

        return TicketMapper.toDTO(ticket);
    }

    @Override
    public TicketDTO activeTicket(Long ticketId) {
        Ticket ticket = ticketDomainService.activeTicket(ticketId);
        this.cacheTicket(ticket);
        return TicketMapper.toDTO(ticket);
    }

    @Override
    public TicketDTO inactiveTicket(Long ticketId) {
        Ticket ticket = ticketDomainService.inactiveTicket(ticketId);
        this.cacheTicket(ticket);
        return TicketMapper.toDTO(ticket);
    }

    @Override
    public void deleteTicket(Long ticketId) {
        ticketDomainService.deleteTicket(ticketId);
        // Xóa cache khi soft delete
        this.evictTicketCache(ticketId);
    }

    @Autowired
    private com.xxxx.ddd.domain.respository.TicketDetailRepository ticketDetailRepository;

    @Override
    public List<TicketDTO> getAllActiveTickets() {
        log.info("App Service: Getting all active tickets");
        List<Ticket> tickets = ticketDomainService.getAllActiveTickets();
        
        return tickets.stream()
                .map(ticket -> {
                    TicketDTO dto = TicketMapper.toDTO(ticket);
                    // Lấy chi tiết vé để lấy giá và số lượng (Lấy cái đầu tiên làm đại diện)
                    java.util.List<com.xxxx.ddd.domain.model.entity.TicketDetail> details = 
                        ticketDetailRepository.findByActivityId(ticket.getId());
                    if (!details.isEmpty()) {
                        com.xxxx.ddd.domain.model.entity.TicketDetail firstDetail = details.get(0);
                        dto.setPriceOriginal(firstDetail.getPriceOriginal());
                        dto.setPriceFlash(firstDetail.getPriceFlash());
                        dto.setStockAvailable(firstDetail.getStockAvailable());
                        dto.setStockInitial(firstDetail.getStockInitial());
                    }
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // ========== CACHE METHODS ==========

    /**
     * Cache Ticket entity vào Redis
     */
    private void cacheTicket(Ticket ticket) {
        try {
            log.info("Caching ticket ID: {}, name: {}", ticket.getId(), ticket.getName());
            if (ticket != null && ticket.getId() != null) {
                cache.setObject(TICKET_CACHE_PREFIX + ticket.getId(), ticket);
            }
        } catch (Exception e) {
            // Cache failure không được block business flow
            log.warn("Failed to cache ticket ID: {}, error: {}", ticket.getId(), e.getMessage());
        }
    }

    /**
     * Cache TicketDetail entity vào Redis
     * Sử dụng cùng key convention với TicketDetailCacheService
     */
    private void cacheTicketDetail(TicketDetail ticketDetail) {
        try {
            if (ticketDetail != null && ticketDetail.getId() != null) {
                cache.setObject(TICKET_ITEM_CACHE_PREFIX + ticketDetail.getId(), ticketDetail);
            }
        } catch (Exception e) {
            log.warn("Failed to cache ticketDetail ID: {}, error: {}", ticketDetail.getId(), e.getMessage());
        }
    }

    /**
     * Xóa cache Ticket khi delete
     */
    private void evictTicketCache(Long ticketId) {
        try {
            cache.delete(TICKET_CACHE_PREFIX + ticketId);
            // Lưu ý: không xóa TICKET_ITEM vì không biết detail ID từ ticket ID
            // Cần query hoặc maintain mapping nếu muốn xóa cả detail cache
        } catch (Exception e) {
            log.warn("Failed to evict cache for ticket ID: {}, error: {}", ticketId, e.getMessage());
        }
    }
}
