package com.ssplugins.shadow3.entity;

public class EntityList {
    
    private ShadowEntity first;
    private ShadowEntity last;
    private int size = 0;
    
    private void link(ShadowEntity a, ShadowEntity b) {
        if (a != null) a.setNext(b);
        if (b != null) b.setPrevious(a);
    }
    
    public int size() {
        return size;
    }
    
    public void add(ShadowEntity entity) {
        link(last, entity);
        if (first == null) first = entity;
        last = entity;
        entity.setNext(null);
        ++size;
    }
    
    public void remove(ShadowEntity entity) {
        link(entity.getPrevious(), entity.getNext());
        entity.setPrevious(null);
        entity.setNext(null);
        --size;
    }
    
    public ShadowEntity getFirst() {
        return first;
    }
    
    public ShadowEntity getLast() {
        return last;
    }
    
}
