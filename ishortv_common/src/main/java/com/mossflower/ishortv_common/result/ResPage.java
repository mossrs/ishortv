package com.mossflower.ishortv_common.result;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author z's'b
 * @version 1.0
 */
@Data
public class ResPage<K, T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long currentPage;
    private Long pageSize;
    private Long totalPage;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private List<T> items;


    public ResPage() {
    }

    public ResPage(Page<K> page, List<T> items) {
        setCurrentPage(page.getCurrent());
        setPageSize(page.getSize());
        setTotalPage(page.getPages());
        setHasNext(page.hasNext());
        setHasPrevious(page.hasPrevious());
        setItems(items);
    }


}
