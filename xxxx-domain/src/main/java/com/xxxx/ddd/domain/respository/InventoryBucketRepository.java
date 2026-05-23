package com.xxxx.ddd.domain.respository;

import com.xxxx.ddd.domain.model.entity.InventoryBucketConfig;

import java.util.Optional;

/**
 * REPOSITORY INTERFACE (PORT): "Hợp đồng" cho việc lưu trữ và truy xuất Aggregate Inventory.
 * Lớp Domain chỉ định nghĩa "cần gì", không quan tâm "làm thế nào".
 * Lớp Infrastructure sẽ cung cấp "cách làm" bằng cách triển khai interface này.
 */
public interface InventoryBucketRepository {
    /**
     * Tìm cấu hình nghiệp vụ theo ID.
     * @param templateId ID của mẫu cấu hình
     * @return Optional chứa cấu hình nếu tìm thấy.
     */
    Optional<InventoryBucketConfig> findById(Long templateId);

    /**
     * Tìm cấu hình được đánh dấu là mặc định.
     * @return Optional chứa cấu hình mặc định nếu có.
     */
    Optional<InventoryBucketConfig> findDefault();
}
