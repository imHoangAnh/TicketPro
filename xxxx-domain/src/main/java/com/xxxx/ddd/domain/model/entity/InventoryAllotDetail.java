package com.xxxx.ddd.domain.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.Date;

/**
 * ENTITY: Đại diện cho một bản ghi lịch sử phân bổ/nhập kho.
 *
 * Mỗi bản ghi là một bằng chứng không thể thay đổi về một lần tăng tồn kho.
 * Nó có định danh (id) và có contraint UNIQUE trên `inventor_no` để đảm bảo tính lũy đẳng (Idempotency).
 */
@Data
@NoArgsConstructor // Lombok: Tạo constructor không tham số, cần thiết cho các framework như JPA
@Accessors(chain = true)
public class InventoryAllotDetail {

    /**
     * Định danh duy nhất của bản ghi lịch sử.
     * Tương ứng với cột `id` (PRIMARY KEY).
     */
    private Long id;

    /**
     * ID của sản phẩm (SKU) được nhập kho.
     * Tương ứng với cột `sku_id`.
     */
    private String skuId;

    /**
     * Mã nghiệp vụ duy nhất cho lần nhập kho này (Idempotency Key).
     * Đây là "chìa khóa" để chống trùng lặp yêu cầu.
     * Ví dụ: Mã phiếu nhập kho từ hệ thống WMS.
     * Tương ứng với cột `inventor_no` (UNIQUE KEY).
     */
    private String inventorNo;

    /**
     * ID của người bán (seller) sở hữu sản phẩm này.
     * Tương ứng với cột `seller_id`.
     */
    private String sellerId;

    /**
     * Số lượng tồn kho được thay đổi trong lần nhập kho này.
     * Tương ứng với cột `inventor_num`.
     */
    private int inventorNum;

    // --- Các trường metadata chung ---

    /**
     * Phiên bản của bản ghi, dùng cho cơ chế khóa lạc quan (Optimistic Locking).
     * Tương ứng với cột `version_id`.
     */
    private int versionId;

    /**
     * Cờ xóa mềm. 1 = đã xóa, 0 = đang hoạt động.
     * Tương ứng với cột `del_flag`.
     */
    private int delFlag;

    /**
     * ID của người tạo bản ghi.
     * Tương ứng với cột `create_user`.
     */
    private int createUser;

    /**
     * Thời gian tạo bản ghi.
     * Tương ứng với cột `create_time`.
     */
    private Date createTime;

    /**
     * ID của người cập nhật bản ghi lần cuối.
     * Tương ứng với cột `update_user`.
     */
    private int updateUser;

    /**
     * Thời gian cập nhật bản ghi lần cuối.
     * Tương ứng với cột `update_time`.
     */
    private Date updateTime;
}