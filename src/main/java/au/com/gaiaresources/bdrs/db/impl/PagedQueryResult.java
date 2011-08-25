package au.com.gaiaresources.bdrs.db.impl;

import java.util.Collections;
import java.util.List;

public class PagedQueryResult<T extends PersistentImpl> {
    private List<T> list;
    private int count;

    public List<T> getList() {
        // Protect against returning a null list
        if (this.list == null) {
            return Collections.EMPTY_LIST;
        }
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}