CREATE DATABASE IF NOT EXISTS smart_parking DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_parking;

drop table if exists `user`;
-- 1. 用户表（车主/管理员）
CREATE TABLE user (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',

                      username VARCHAR(64) NOT NULL UNIQUE COMMENT '账号',
                      password VARCHAR(128) NOT NULL COMMENT '密码',
                      nickname VARCHAR(64) COMMENT '昵称',
                      phone VARCHAR(32) COMMENT '手机号',
                      role TINYINT DEFAULT 0 COMMENT '0=车主 1=管理员',
                      status TINYINT DEFAULT 1 COMMENT '0=禁用 1=正常',

                      version INT DEFAULT 0 COMMENT '乐观锁',
                      create_by BIGINT COMMENT '创建人ID',
                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      update_by BIGINT COMMENT '更新人ID',
                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      is_deleted TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',

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
                             status TINYINT DEFAULT 1 COMMENT '0=关闭 1=正常',

                             version INT DEFAULT 0 COMMENT '乐观锁',
                             create_by BIGINT COMMENT '创建人ID',
                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             update_by BIGINT COMMENT '更新人ID',
                             update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             is_deleted TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',

                             INDEX idx_name (name),
                             INDEX idx_location (longitude, latitude)
) COMMENT='停车场信息表';

drop table if exists `parking_space`;
-- 3. 车位表
CREATE TABLE parking_space (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',

                               parking_lot_id BIGINT NOT NULL COMMENT '所属停车场ID',
                               space_no VARCHAR(32) NOT NULL COMMENT '车位编号',
                               type TINYINT DEFAULT 0 COMMENT '0=普通 1=新能源 2=VIP',
                               status TINYINT DEFAULT 1 COMMENT '0=占用 1=空闲',

                               version INT DEFAULT 0 COMMENT '乐观锁',
                               create_by BIGINT COMMENT '创建人ID',
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               update_by BIGINT COMMENT '更新人ID',
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               is_deleted TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',

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
                               type TINYINT NOT NULL COMMENT '0=预约 1=停车',
                               start_time DATETIME COMMENT '开始时间',
                               end_time DATETIME COMMENT '结束时间',
                               duration_minutes INT COMMENT '停车时长（分钟）',
                               amount DECIMAL(8,2) COMMENT '金额',
                               status TINYINT DEFAULT 0 COMMENT '0=待进场 1=已完成 2=已取消',

                               version INT DEFAULT 0 COMMENT '乐观锁',
                               create_by BIGINT COMMENT '创建人ID',
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               update_by BIGINT COMMENT '更新人ID',
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               is_deleted TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',

                               INDEX idx_order_no (order_no),
                               INDEX idx_user_id (user_id),
                               INDEX idx_lot_id (parking_lot_id),
                               INDEX idx_time_range (start_time, end_time)
) COMMENT='停车订单表';
ALTER TABLE parking_order
    ADD COLUMN actual_start_time DATETIME COMMENT '实际进场时间',
    ADD COLUMN actual_end_time DATETIME COMMENT '实际出场时间',
    ADD COLUMN actual_duration_minutes INT COMMENT '实际停车时长',
    ADD COLUMN cancel_reason VARCHAR(256) COMMENT '取消原因',
    ADD COLUMN cancel_time DATETIME COMMENT '取消时间',
    ADD COLUMN payment_id BIGINT COMMENT '关联支付记录ID',
    ADD COLUMN review_score TINYINT COMMENT '评价分数 1-5',
    ADD COLUMN review_content VARCHAR(512) COMMENT '评价内容',
    ADD COLUMN review_time DATETIME COMMENT '评价时间';

drop table if exists `parking_usage_history`;
-- 5. 车位占用历史表（预测用）
CREATE TABLE parking_usage_history (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                       parking_lot_id BIGINT NOT NULL,
                                       occupancy_rate DECIMAL(5,2) NOT NULL COMMENT '占用率 0-128',
                                       record_time DATETIME NOT NULL COMMENT '记录时间',
                                       hour INT NOT NULL COMMENT '小时 0-23',
                                       weekday INT NOT NULL COMMENT '星期 1-7',
                                       is_holiday TINYINT DEFAULT 0 COMMENT '是否节假日',

                                       version INT DEFAULT 0 COMMENT '乐观锁',
                                       create_by BIGINT COMMENT '创建人ID',
                                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       update_by BIGINT COMMENT '更新人ID',
                                       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       is_deleted TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',

                                       INDEX idx_lot_time (parking_lot_id, record_time)
) COMMENT='车位历史占用表';

drop table if exists `parking_prediction`;
-- 6. 预测结果表
CREATE TABLE parking_prediction (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                    parking_lot_id BIGINT NOT NULL,
                                    predict_time DATETIME NOT NULL COMMENT '预测时间点',
                                    occupancy_rate DECIMAL(5,2) NOT NULL COMMENT '预测占用率',

                                    version INT DEFAULT 0 COMMENT '乐观锁',
                                    create_by BIGINT COMMENT '创建人ID',
                                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    update_by BIGINT COMMENT '更新人ID',
                                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    is_deleted TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',

                                    INDEX idx_lot_predict (parking_lot_id, predict_time)
) COMMENT='车位占用率预测结果表';

drop table if exists `user_preference`;
-- 7. 用户偏好表
CREATE TABLE user_preference (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                 user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
                                 prefer_distance INT DEFAULT 1280 COMMENT '偏好最大距离（米）',
                                 prefer_price INT DEFAULT 0 COMMENT '0=便宜优先 1=距离优先',
                                 prefer_type TINYINT DEFAULT 0 COMMENT '偏好车位类型',

                                 version INT DEFAULT 0 COMMENT '乐观锁',
                                 create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 update_by BIGINT COMMENT '更新人ID',
                                 update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 is_deleted TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',

                                 INDEX idx_user_id (user_id)
) COMMENT='用户停车偏好表';

drop table if exists `base_sys_log`;
-- 8. 系统操作日志表
CREATE TABLE base_sys_log (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,

                         user_id BIGINT COMMENT '操作用户',
                         content VARCHAR(255) COMMENT '操作内容',
                         ip VARCHAR(64) COMMENT 'IP地址',

                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                         INDEX idx_user_id (user_id),
                         INDEX idx_create_time (create_time)
) COMMENT='系统操作日志表';
ALTER TABLE base_sys_log
    ADD COLUMN module VARCHAR(64) COMMENT '操作模块（如：用户管理、停车场管理）',
    ADD COLUMN operation VARCHAR(32) COMMENT '操作类型（如：登录、创建、更新、删除）',
    ADD COLUMN status TINYINT DEFAULT 1 COMMENT '0=失败 1=成功',
    ADD COLUMN error_msg VARCHAR(512) COMMENT '错误信息';

drop table if exists `user_plate`;
-- 9. 车牌管理表
CREATE TABLE user_plate (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,

                            user_id BIGINT NOT NULL,
                            plate_number VARCHAR(32) NOT NULL COMMENT '车牌号',
                            is_default TINYINT DEFAULT 0 COMMENT '0=非默认 1=默认',
                            status TINYINT DEFAULT 1 COMMENT '0=禁用 1=正常',

                            version INT DEFAULT 0 COMMENT '乐观锁',
                            create_by BIGINT COMMENT '创建人ID',
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            update_by BIGINT COMMENT '更新人ID',
                            update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            is_deleted TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',

                            UNIQUE KEY uk_user_plate (user_id, plate_number, is_deleted),
                            INDEX idx_plate (plate_number)
) COMMENT='用户车牌表';

drop table if exists `payment_record`;
-- 10. 支付记录表
CREATE TABLE payment_record (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                order_id BIGINT NOT NULL COMMENT '订单ID',
                                payment_no VARCHAR(64) NOT NULL UNIQUE COMMENT '支付流水号',
                                amount DECIMAL(8,2) NOT NULL COMMENT '支付金额',
                                payment_method TINYINT NOT NULL COMMENT '0=微信 1=支付宝 2=余额',
                                payment_status TINYINT DEFAULT 0 COMMENT '0=待支付 1=成功 2=失败',
                                transaction_id VARCHAR(128) COMMENT '第三方交易号',
                                payment_time DATETIME COMMENT '支付完成时间',

                                version INT DEFAULT 0 COMMENT '乐观锁',
                                create_by BIGINT COMMENT '创建人ID',
                                create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                update_by BIGINT COMMENT '更新人ID',
                                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                is_deleted TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',

                                INDEX idx_order_id (order_id),
                                INDEX idx_payment_no (payment_no)
) COMMENT='支付记录表';

drop table if exists `notification`;
-- 11. 消息通知表
CREATE TABLE notification (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,

                              user_id BIGINT NOT NULL COMMENT '接收用户',
                              title VARCHAR(128) NOT NULL COMMENT '标题',
                              content TEXT COMMENT '内容',
                              type TINYINT COMMENT '0=系统通知 1=订单提醒 2=优惠活动',
                              is_read TINYINT DEFAULT 0 COMMENT '0=未读 1=已读',
                              biz_id VARCHAR(64) COMMENT '业务ID',
                              push_status TINYINT DEFAULT 0 COMMENT '0=未推送 1=已推送 2=推送失败',
                              push_time DATETIME COMMENT '推送时间',
                              push_fail_reason VARCHAR(256) COMMENT '推送失败原因',

                              version INT DEFAULT 0 COMMENT '乐观锁',
                              create_by BIGINT COMMENT '创建人ID',
                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              update_by BIGINT COMMENT '更新人ID',
                              update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              is_deleted TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',

                              INDEX idx_user_notify (user_id, is_read, create_time),
                              INDEX idx_push_status (push_status, create_time)
) COMMENT='消息通知表';

drop table if exists `blacklist`;
-- 12. 黑名单表
CREATE TABLE blacklist (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,

                           plate_number VARCHAR(32) NOT NULL COMMENT '车牌号',
                           reason VARCHAR(256) COMMENT '加入原因',
                           expire_time DATETIME COMMENT '过期时间',

                           version INT DEFAULT 0 COMMENT '乐观锁',
                           create_by BIGINT COMMENT '创建人ID',
                           create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           update_by BIGINT COMMENT '更新人ID',
                           update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           is_deleted TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',

                           UNIQUE KEY uk_plate (plate_number, is_deleted)
) COMMENT='黑名单表';

drop table if exists `coupon`;
-- 13. 优惠券表
CREATE TABLE coupon (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,

                        user_id BIGINT COMMENT '所属用户（NULL=通用券）',
                        code VARCHAR(32) UNIQUE COMMENT '优惠码',
                        name VARCHAR(128) NOT NULL COMMENT '优惠券名称',
                        type TINYINT COMMENT '0=满减 1=折扣 2=时长',
                        value DECIMAL(8,2) COMMENT '优惠值',
                        min_amount DECIMAL(8,2) COMMENT '最低消费',
                        start_time DATETIME NOT NULL COMMENT '有效期开始',
                        end_time DATETIME NOT NULL COMMENT '有效期结束',
                        status TINYINT DEFAULT 0 COMMENT '0=未使用 1=已使用 2=已过期',
                        used_time DATETIME COMMENT '使用时间',
                        used_order_id BIGINT COMMENT '使用订单',

                        version INT DEFAULT 0 COMMENT '乐观锁',
                        create_by BIGINT COMMENT '创建人ID',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by BIGINT COMMENT '更新人ID',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        is_deleted TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',

                        INDEX idx_user_id (user_id),
                        INDEX idx_code (code),
                        INDEX idx_time_range (start_time, end_time)
) COMMENT='优惠券表';

drop table if exists `parking_review`;
-- 14. 停车场评价表
CREATE TABLE parking_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL COMMENT '停车场ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT COMMENT '关联订单ID',
    score TINYINT NOT NULL COMMENT '评分 1-5',
    content VARCHAR(512) COMMENT '评价内容',
    
    version INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    
    INDEX idx_lot_id (parking_lot_id),
    INDEX idx_user_id (user_id)
) COMMENT='停车场评价表';