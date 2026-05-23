package com.xxxx.ddd.domain.respository;

import com.xxxx.ddd.domain.model.entity.InventoryAllotDetail;

/**
 * REPOSITORY INTERFACE (PORT): "Hợp đồng" cho việc lưu trữ lịch sử nhập kho.
 */
public interface AllotmentLogRepository {
    /**
     * Lưu một bản ghi chi tiết về việc nhập kho.
     * @param detail đối tượng chi tiết cần lưu.
     */
    void save(InventoryAllotDetail detail);
}
