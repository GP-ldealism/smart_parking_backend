CREATE DATABASE IF NOT EXISTS smart_parking_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_parking_platform;

drop table if exists `user`;
-- 1. 用户表
CREATE TABLE user (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
                      username VARCHAR(64) NOT NULL UNIQUE COMMENT '账号',
                      password VARCHAR(128) NOT NULL COMMENT '密码',
                      nickname VARCHAR(64) COMMENT '昵称',
                      phone VARCHAR(32) COMMENT '手机号',
                      role TINYINT DEFAULT 0 COMMENT '角色 0车主 1管理员',
                      status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',

                      version INT DEFAULT 0 COMMENT '乐观锁',
                      create_by BIGINT COMMENT '创建人ID',
                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      update_by BIGINT COMMENT '更新人ID',
                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      is_deleted TINYINT DEFAULT 0 COMMENT '有效标识 0未删除 1已删除',

                      INDEX idx_username (username),
                      INDEX idx_phone (phone)
) COMMENT='系统用户表';

drop table if exists `parking_lot`;
-- 2. 停车场信息表
CREATE TABLE parking_lot (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
                             name VARCHAR(128) NOT NULL COMMENT '停车场名称',
                             address VARCHAR(256) NOT NULL COMMENT '地址',
                             longitude DECIMAL(10,6) COMMENT '经度',
                             latitude DECIMAL(10,6) COMMENT '纬度',
                             total_space INT NOT NULL COMMENT '总车位',
                             free_space INT DEFAULT 0 COMMENT '空闲车位',
                             rate DECIMAL(5,2) NOT NULL COMMENT '每小时费率',
                             open_time VARCHAR(64) COMMENT '开放时间',
                             status TINYINT DEFAULT 1 COMMENT '状态 0关闭 1正常',

                             version INT DEFAULT 0 COMMENT '乐观锁',
                             create_by BIGINT COMMENT '创建人ID',
                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             update_by BIGINT COMMENT '更新人ID',
                             update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             is_deleted TINYINT DEFAULT 0 COMMENT '有效标识 0未删除 1已删除',

                             INDEX idx_name (name),
                             INDEX idx_location (longitude, latitude)
) COMMENT='停车场信息表';

drop table if exists `parking_space`;
-- 3. 车位表
CREATE TABLE parking_space (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
                               parking_lot_id BIGINT NOT NULL COMMENT '所属停车场ID',
                               space_no VARCHAR(32) NOT NULL COMMENT '车位编号',
                               type TINYINT DEFAULT 0 COMMENT '车位类型 0普通 1新能源 2VIP 3其他',
                               status TINYINT DEFAULT 1 COMMENT '车位状态 0占用 1空闲',

                               version INT DEFAULT 0 COMMENT '乐观锁',
                               create_by BIGINT COMMENT '创建人ID',
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               update_by BIGINT COMMENT '更新人ID',
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               is_deleted TINYINT DEFAULT 0 COMMENT '有效标识 0未删除 1已删除',

                               INDEX idx_lot_id (parking_lot_id),
                               INDEX idx_status (status),
                               UNIQUE KEY uk_lot_space (parking_lot_id, space_no, is_deleted)
) COMMENT='车位信息表';

drop table if exists `parking_order`;
-- 4. 停车订单表
CREATE TABLE parking_order (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
                               order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
                               user_id BIGINT NOT NULL COMMENT '用户ID',
                               parking_lot_id BIGINT NOT NULL COMMENT '停车场ID',
                               space_no VARCHAR(32) COMMENT '车位号',
                               plate_number VARCHAR(32) COMMENT '车牌号',
                               type TINYINT NOT NULL COMMENT '订单类型 0预约 1停车',
                               start_time DATETIME COMMENT '开始时间',
                               end_time DATETIME COMMENT '结束时间',
                               duration_minutes INT COMMENT '停车时长（分钟）',
                               amount DECIMAL(8,2) COMMENT '金额',
                               status TINYINT DEFAULT 0 COMMENT '订单状态 0进行中 1已完成 2已取消',

                               version INT DEFAULT 0 COMMENT '乐观锁',
                               create_by BIGINT COMMENT '创建人ID',
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               update_by BIGINT COMMENT '更新人ID',
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               is_deleted TINYINT DEFAULT 0 COMMENT '有效标识 0未删除 1已删除',

                               INDEX idx_order_no (order_no),
                               INDEX idx_user_id (user_id),
                               INDEX idx_lot_id (parking_lot_id),
                               INDEX idx_time_range (start_time, end_time)
) COMMENT='停车订单表';
ALTER TABLE parking_order
    ADD COLUMN review_score TINYINT COMMENT '评价分数 1-5',
    ADD COLUMN review_content VARCHAR(512) COMMENT '评价内容',
    ADD COLUMN review_time DATETIME COMMENT '评价时间';
ALTER TABLE parking_order ADD COLUMN coupon_id BIGINT COMMENT '关联优惠券ID';
alter table parking_order add actual_amount DECIMAL(8,2) COMMENT '实付金额';

drop table if exists `parking_usage_history`;
-- 5. 车位占用历史表（预测用）
CREATE TABLE parking_usage_history (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       parking_lot_id BIGINT NOT NULL COMMENT '停车场ID',
                                       occupancy_rate DECIMAL(5,2) NOT NULL COMMENT '占用率 0-100.00',
                                       record_time DATETIME NOT NULL COMMENT '记录时间',
                                       hour INT NOT NULL COMMENT '小时 0-23',
                                       weekday INT NOT NULL COMMENT '星期 1-7（周一=1 周日=7）',
                                       is_holiday TINYINT DEFAULT 0 COMMENT '是否节假日 0否 1是',

                                       version INT DEFAULT 0 COMMENT '乐观锁',
                                       create_by BIGINT COMMENT '创建人ID',
                                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       update_by BIGINT COMMENT '更新人ID',
                                       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       is_deleted TINYINT DEFAULT 0 COMMENT '有效标识 0未删除 1已删除',

                                       INDEX idx_lot_time (parking_lot_id, record_time)
) COMMENT='车位历史占用表';

drop table if exists `parking_prediction`;
-- 6. 预测结果表
CREATE TABLE parking_prediction (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    parking_lot_id BIGINT NOT NULL COMMENT '停车场ID',
                                    predict_time DATETIME NOT NULL COMMENT '预测时间点',
                                    occupancy_rate DECIMAL(5,2) NOT NULL COMMENT '预测占用率 0-100.00',
                                    confidence DECIMAL(5,4) COMMENT '预测置信度 0-1',
                                    model_version VARCHAR(32) COMMENT '模型版本',
                                    actual_rate DECIMAL(5,2) COMMENT '实际占用率（回填用）',
                                    error_rate DECIMAL(5,4) COMMENT '预测误差',

                                    version INT DEFAULT 0 COMMENT '乐观锁',
                                    create_by BIGINT COMMENT '创建人ID',
                                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    update_by BIGINT COMMENT '更新人ID',
                                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    is_deleted TINYINT DEFAULT 0 COMMENT '有效标识 0未删除 1已删除',

                                    INDEX idx_lot_predict (parking_lot_id, predict_time)
) COMMENT='车位占用率预测结果表';

drop table if exists `user_preference`;
-- 7. 用户偏好表
CREATE TABLE user_preference (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
                                 prefer_distance INT DEFAULT 1280 COMMENT '偏好最大距离（米）',
                                 prefer_price INT DEFAULT 0 COMMENT '价格偏好 0便宜优先 1距离优先',
                                 prefer_type TINYINT DEFAULT 0 COMMENT '车位类型偏好 0普通 1新能源 2VIP 3其他',

                                 version INT DEFAULT 0 COMMENT '乐观锁',
                                 create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 update_by BIGINT COMMENT '更新人ID',
                                 update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 is_deleted TINYINT DEFAULT 0 COMMENT '有效标识 0未删除 1已删除',

                                 INDEX idx_user_id (user_id)
) COMMENT='用户停车偏好表';

drop table if exists `base_sys_log`;
-- 8. 系统操作日志表
CREATE TABLE base_sys_log (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT COMMENT '操作用户',
                              module VARCHAR(64) COMMENT '操作模块 用户管理 停车场管理 订单管理 等',
                              operation VARCHAR(32) COMMENT '操作类型 登录 创建 更新 删除 查询',
                              content VARCHAR(255) COMMENT '操作内容',
                              ip VARCHAR(64) COMMENT 'IP地址',
                              status TINYINT DEFAULT 1 COMMENT '操作状态 0失败 1成功',
                              error_msg VARCHAR(512) COMMENT '错误信息',

                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                              INDEX idx_user_id (user_id),
                              INDEX idx_create_time (create_time)
) COMMENT='系统操作日志表';

drop table if exists `user_plate`;
-- 9. 车牌管理表
CREATE TABLE user_plate (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            user_id BIGINT NOT NULL COMMENT '用户ID',
                            plate_number VARCHAR(32) NOT NULL COMMENT '车牌号',
                            is_default TINYINT DEFAULT 0 COMMENT '是否默认 0非默认 1默认',
                            status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',

                            version INT DEFAULT 0 COMMENT '乐观锁',
                            create_by BIGINT COMMENT '创建人ID',
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            update_by BIGINT COMMENT '更新人ID',
                            update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            is_deleted TINYINT DEFAULT 0 COMMENT '有效标识 0未删除 1已删除',

                            UNIQUE KEY uk_user_plate (user_id, plate_number, is_deleted),
                            INDEX idx_plate (plate_number)
) COMMENT='用户车牌表';

drop table if exists `notification`;
-- 10. 消息通知表
CREATE TABLE notification (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL COMMENT '接收用户ID',
                              title VARCHAR(128) NOT NULL COMMENT '标题',
                              content TEXT COMMENT '内容',
                              type TINYINT COMMENT '通知类型 0系统通知 1订单提醒 2优惠活动',
                              is_read TINYINT DEFAULT 0 COMMENT '是否已读 0未读 1已读',
                              biz_id VARCHAR(64) COMMENT '业务ID',
                              push_status TINYINT DEFAULT 0 COMMENT '推送状态 0未推送 1已推送 2推送失败',
                              push_time DATETIME COMMENT '推送时间',
                              push_fail_reason VARCHAR(256) COMMENT '推送失败原因',

                              version INT DEFAULT 0 COMMENT '乐观锁',
                              create_by BIGINT COMMENT '创建人ID',
                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              update_by BIGINT COMMENT '更新人ID',
                              update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              is_deleted TINYINT DEFAULT 0 COMMENT '有效标识 0未删除 1已删除',

                              INDEX idx_user_notify (user_id, is_read, create_time),
                              INDEX idx_push_status (push_status, create_time)
) COMMENT='消息通知表';

drop table if exists `blacklist`;
-- 11. 黑名单表
CREATE TABLE blacklist (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           plate_number VARCHAR(32) NOT NULL COMMENT '车牌号',
                           reason VARCHAR(256) COMMENT '加入原因',
                           expire_time DATETIME COMMENT '过期时间（NULL=永久）',

                           version INT DEFAULT 0 COMMENT '乐观锁',
                           create_by BIGINT COMMENT '创建人ID',
                           create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           update_by BIGINT COMMENT '更新人ID',
                           update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           is_deleted TINYINT DEFAULT 0 COMMENT '有效标识 0未删除 1已删除',

                           UNIQUE KEY uk_plate (plate_number, is_deleted)
) COMMENT='黑名单表';

drop table if exists `coupon`;
-- 12. 优惠券表
CREATE TABLE coupon (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT COMMENT '所属用户ID（NULL=通用券）',
                        code VARCHAR(32) UNIQUE COMMENT '优惠码',
                        name VARCHAR(128) NOT NULL COMMENT '优惠券名称',
                        type TINYINT COMMENT '优惠类型 0满减 1折扣 2时长',
                        value DECIMAL(8,2) COMMENT '优惠值（满减金额/折扣比例/时长分钟）',
                        min_amount DECIMAL(8,2) COMMENT '最低消费金额',
                        start_time DATETIME NOT NULL COMMENT '有效期开始时间',
                        end_time DATETIME NOT NULL COMMENT '有效期结束时间',
                        status TINYINT DEFAULT 0 COMMENT '优惠券状态 0未使用 1已使用 2已过期',
                        used_time DATETIME COMMENT '使用时间',
                        used_order_id BIGINT COMMENT '使用订单ID',

                        version INT DEFAULT 0 COMMENT '乐观锁',
                        create_by BIGINT COMMENT '创建人ID',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by BIGINT COMMENT '更新人ID',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        is_deleted TINYINT DEFAULT 0 COMMENT '有效标识 0未删除 1已删除',

                        INDEX idx_user_id (user_id),
                        INDEX idx_code (code),
                        INDEX idx_time_range (start_time, end_time)
) COMMENT='优惠券表';

