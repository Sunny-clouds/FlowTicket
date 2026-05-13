package com.onik.flowticket.mapper;

import com.onik.flowticket.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息表 Mapper。
 */
@Mapper
public interface MessageMapper {
    /**
     * 新增一条站内消息通知。
     */
    void insert(Message message);
}
