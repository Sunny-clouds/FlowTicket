package com.onik.flowticket.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页返回对象。
 *
 * @param <T> 分页记录的数据类型
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    private Long total;
    private List<T> rows;

}
