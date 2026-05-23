
package com.xxxx.ddd.domain.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * ENTITY: Đại diện cho một mẫu cấu hình phân (Bucket Configuration Template).
 *
 * Trong DDD, đây là một Entity vì nó có một định danh duy nhất (id) và vòng đời riêng (có thể được tạo, cập nhật, xóa).
 * Nó chứa các quy tắc nghiệp vụ cốt lõi để quyết định cách hệ thống tồn kho sẽ hoạt động.
 * Ví dụ: một sản phẩm "flash sale" có thể dùng một mẫu cấu hình khác với sản phẩm thông thường.
 * Lớp này chỉ chứa dữ liệu, logic nghiệp vụ sẽ được đặt trong Domain Service.
 */
@Data // Lombok: Tự động tạo getters, setters, toString(), equals(), và hashCode().
@Accessors(chain = true) // Lombok: Hỗ trợ fluent API (ví dụ: new Config().setId(1L).setBucketNum(8);)
public class InventoryBucketConfig {

    /**
     * Định danh duy nhất của cấu hình.
     * Tương ứng với cột `id` (PRIMARY KEY).
     */
    private Long id;

    /**
     * Tên của mẫu cấu hình, giúp người vận hành dễ nhận biết.
     * Ví dụ: "Default Template", "Flash Sale Template".
     * Tương ứng với cột `template_name`.
     */
    private String templateName;

    /**
     * Số lượng thùng (buckets) tối đa mà một sản phẩm có thể được chia ra theo mẫu này.
     * Đây là một tham số quan trọng để kiểm soát mức độ song song (concurrency).
     * Tương ứng với cột `bucket_num`.
     */
    private int bucketNum;

    /**
     * Dung lượng (số lượng tồn kho) tối đa mà một thùng phân mảnh có thể chứa.
     * Giúp ngăn một thùng chứa quá nhiều tồn kho, đảm bảo sự phân bổ đều.
     * Tương ứng với cột `max_depth_num`.
     */
    private int maxDepthNum;

    /**
     * Dung lượng tối thiểu cần thiết để một thùng được coi là "hợp lệ" và được kích hoạt (online).
     * Ngăn chặn việc tạo ra các thùng "vụn" với quá ít tồn kho.
     * Tương ứng với cột `min_depth_num`.
     */
    private int minDepthNum;

    /**
     * Ngưỡng tồn kho để kích hoạt việc xem xét thu hẹp (offline) một thùng.
     * Khi tồn kho của một thùng thấp hơn giá trị này, nó sẽ bị đưa vào danh sách ứng viên để thu hồi.
     * Tương ứng với cột `threshold_value`.
     */
    private int thresholdValue;

    /**
     * Tỷ lệ phần trăm tồn kho (1-100) kích hoạt việc mở rộng (scale-up).
     * Ví dụ: 40, nghĩa là khi tồn kho thùng còn dưới 40% dung lượng ban đầu, nó sẽ yêu cầu nạp thêm từ kho trung tâm.
     * Tương ứng với cột `back_source_proportion`.
     */
    private int backSourceProportion;

    /**
     * Lượng tồn kho mặc định được nạp vào mỗi lần mở rộng, khi kho trung tâm còn nhiều hàng.
     * Đây là "bước nhảy" cố định để nhanh chóng bổ sung tồn kho cho các thùng.
     * Tương ứng với cột `back_source_step`.
     */
    private int backSourceStep;

    /**
     * Cờ đánh dấu đây có phải là mẫu cấu hình mặc định cho toàn hệ thống hay không.
     * Chỉ nên có một mẫu mặc định tại một thời điểm.
     * Tương ứng với cột `is_default`.
     */
    private boolean isDefault;

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