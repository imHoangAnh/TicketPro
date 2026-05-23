package com.xxxx.ddd.application.service.order;

import com.xxxx.ddd.application.model.PagedOrdersDTO;
import com.xxxx.ddd.application.model.TicketOrderDTO;
import com.xxxx.ddd.application.model.response.PlaceOrderResponse;
import com.xxxx.ddd.domain.model.entity.TickerOrder;

import java.util.List;

public interface TicketOrderAppService {

    boolean decreaseStockLevel1(Long ticketTypeId, int quantity);
    boolean decreaseStockLevel2(Long ticketTypeId, int quantity);
    boolean decreaseStockLevel3CAS(Long ticketTypeId, int quantity);
    PlaceOrderResponse placeOrderCAS(Long ticketTypeId, int quantity);

    int getStockAvailable(Long ticketTypeId);

    // order..
    List<TicketOrderDTO> findAll(String yearMonth);
    boolean insertOrder(String yearMonth, TickerOrder tickerOrder);
    TicketOrderDTO findByOrderNumber(String yearMonth, String orderNumber);

    /**
     * Hủy đơn hàng và hoàn lại tồn kho trong Database + Redis
     *
     * @param userId ID của người dùng thực hiện hủy
     * @param orderNumber Mã đơn hàng (VD: OKX-SGN-1-171204...)
     * @return true nếu hủy thành công, ngược lại false
     */
    boolean cancelOrder(Long userId, String orderNumber);

    PagedOrdersDTO findPage(String yearMonth, long lastId, int limit);
}
