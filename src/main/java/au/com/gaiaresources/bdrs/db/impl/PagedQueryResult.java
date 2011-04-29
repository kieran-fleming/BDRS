package au.com.gaiaresources.bdrs.db.impl;

import java.util.List;

public class PagedQueryResult<T extends PersistentImpl> {
    private List<T> list;
    private int count;

    public List<T> getList() {
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