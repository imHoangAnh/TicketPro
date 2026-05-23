package com.xxxx.ddd.controller.http;

import com.xxxx.ddd.application.model.OrderDTO;
import com.xxxx.ddd.application.model.auth.AuthenticatedPrincipal;
import com.xxxx.ddd.application.model.command.CreateOrderCommand;
import com.xxxx.ddd.application.model.response.PlaceOrderResponse;
import com.xxxx.ddd.application.service.order.OrderAppException;
import com.xxxx.ddd.application.service.order.OrderAppService;
import com.xxxx.ddd.controller.dto.CreateBookingRequest;
import com.xxxx.ddd.controller.model.enums.ResultUtil;
import com.xxxx.ddd.controller.model.vo.ResultMessage;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderAppService orderAppService;

    public OrderController(OrderAppService orderAppService) {
        this.orderAppService = orderAppService;
    }

    @PostMapping
    public ResponseEntity<ResultMessage<PlaceOrderResponse>> placeOrder(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication
    ) {
        try {
            PlaceOrderResponse response = orderAppService.placeOrder(principal(authentication).userId(), new CreateOrderCommand(
                    request.getTicketTypeId(),
                    request.getQuantity()
            ));
            if (!response.isSuccess()) {
                return ResponseEntity.status(statusForFailureCode(response.getCode())).body(ResultUtil.data(response));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(ResultUtil.data(response));
        } catch (OrderAppException e) {
            return error(e);
        }
    }

    @GetMapping("/my")
    public ResponseEntity<ResultMessage<List<OrderDTO>>> listMyOrders(Authentication authentication) {
        try {
            return ResponseEntity.ok(ResultUtil.data(orderAppService.listMyOrders(principal(authentication).userId())));
        } catch (OrderAppException e) {
            return error(e);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ResultMessage<OrderDTO>> getOrder(@PathVariable Long orderId, Authentication authentication) {
        try {
            AuthenticatedPrincipal principal = principal(authentication);
            return ResponseEntity.ok(ResultUtil.data(orderAppService.getOrder(
                    principal.userId(),
                    isAdmin(principal),
                    orderId
            )));
        } catch (OrderAppException e) {
            return error(e);
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ResultMessage<OrderDTO>> cancelOrder(@PathVariable Long orderId, Authentication authentication) {
        try {
            AuthenticatedPrincipal principal = principal(authentication);
            return ResponseEntity.ok(ResultUtil.data(orderAppService.cancelOrder(
                    principal.userId(),
                    isAdmin(principal),
                    orderId
            )));
        } catch (OrderAppException e) {
            return error(e);
        }
    }

    private static AuthenticatedPrincipal principal(Authentication authentication) {
        return (AuthenticatedPrincipal) authentication.getPrincipal();
    }

    private static boolean isAdmin(AuthenticatedPrincipal principal) {
        return principal.roles().contains("ADMIN");
    }

    private static <T> ResponseEntity<ResultMessage<T>> error(OrderAppException e) {
        HttpStatus status = statusForFailureCode(e.code());
        return ResponseEntity.status(status).body(ResultUtil.error(status.value(), e.getMessage()));
    }

    private static HttpStatus statusForFailureCode(String code) {
        if ("ORDER_FORBIDDEN".equals(code)) {
            return HttpStatus.FORBIDDEN;
        }
        if ("ORDER_NOT_FOUND".equals(code) || "TICKET_TYPE_NOT_FOUND".equals(code) || "EVENT_NOT_FOUND".equals(code)) {
            return HttpStatus.NOT_FOUND;
        }
        if ("OUT_OF_STOCK".equals(code) || "STOCK_CONFLICT".equals(code)) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
