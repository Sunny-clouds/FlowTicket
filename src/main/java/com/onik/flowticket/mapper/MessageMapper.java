package com.onik.flowticket.mapper;

import com.onik.flowticket.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息表 Mapper。
 */
@Mapper
public interface MessageMapper {
    /**
     * 新增一条站内消息通知。
     */
    void insert(Message message);

    /**
     * 查询当前用户收到的站内消息。
     */
    List<Message> selectByReceiverId(@Param("receiverId") Long receiverId);

    /**
     * 查询当前用户未读消息数量。
     */
    Long countUnread(@Param("receiverId") Long receiverId);

    /**
     * 将单条消息标记为已读，只允许接收人本人操作。
     */
    int markRead(@Param("id") Long id, @Param("receiverId") Long receiverId);

    /**
     * 将当前用户的全部未读消息标记为已读。
     */
    int markAllRead(@Param("receiverId") Long receiverId);
}
