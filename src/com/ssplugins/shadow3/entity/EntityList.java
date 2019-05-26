package com.ssplugins.shadow3.entity;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class EntityList implements Iterable<ShadowEntity> {
    
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
    
    @Override
    public Iterator<ShadowEntity> iterator() {
        return new Iterator<ShadowEntity>() {
            private ShadowEntity current = first;
            
            @Override
            public boolean hasNext() {
                return current != null;
            }
    
            @Override
            public ShadowEntity next() {
                ShadowEntity c = current;
                current = current.getNext();
                return c;
            }
        };
    }
    
    public Stream<ShadowEntity> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this.iterator(), Spliterator.IMMUTABLE), false);
    }
    
}
