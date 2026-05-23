package com.xxxx.ddd.domain.service;

import com.xxxx.ddd.domain.model.entity.InventoryAllotDetail;

/**
 * DOMAIN SERVICE INTERFACE: Định nghĩa các nghiệp vụ phức tạp trong domain.
 * Nó điều phối các Entity và Repository để hoàn thành một hành động nghiệp vụ có ý nghĩa.
 */
public interface InventoryAllotmentDomainService {

    /**
     * Thực hiện nghiệp vụ phân bổ tồn kho (bao gồm cả khởi tạo và tăng thêm).
     * @param sellerId ID người bán
     * @param skuId ID sản phẩm
     * @param inventoryNum Lượng tồn kho cần phân bổ
     * @param templateId ID của mẫu cấu hình (tùy chọn)
     * @return Aggregate Inventory đã được cập nhật
     */
    InventoryAllotDetail allotInventory(String sellerId, String skuId, int inventoryNum, Long templateId);
}