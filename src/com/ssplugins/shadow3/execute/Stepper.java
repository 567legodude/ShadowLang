package com.ssplugins.shadow3.execute;

import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.entity.Block;
import com.ssplugins.shadow3.entity.EntityList;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.util.Schema;

import java.util.Iterator;
import java.util.function.Consumer;

public class Stepper {
    
    private Stepper parent;
    private ShadowContext context;
    
    private Scope scope;
    private EntityList content;
    private boolean run;
    private Schema<ShadowEntity> skipSchema;
    
    private Iterator<ShadowEntity> iterator;
    
    public Stepper(Stepper parent, ShadowContext context, Block block) {
        this.parent = parent;
        this.context = context;
        this.content = block.getContents();
        iterator = content.iterator();
    }
    
    public void run() {
        run(null);
    }
    
    public void run(Consumer<Stepper> callback) {
        if (scope == null) scope = new Scope(context, this);
        run = true;
        while (run) {
            ShadowEntity next = iterator.next();
            if (skipSchema != null) {
                if (!skipSchema.test(next)) {
                    skipSchema = null;
                    next.execute(this, scope, null);
                }
            }
            else next.execute(this, scope, null);
            if (!iterator.hasNext()) {
                run = false;
                if (callback != null) callback.accept(this);
            }
        }
    }
    
    public void restart() {
        run = true;
        iterator = content.iterator();
    }
    
    public void breakBlock() {
        run = false;
    }
    
    public Stepper getParent() {
        return parent;
    }
    
    public void setScope(Scope scope) {
        this.scope = scope;
    }
    
    public void setSkipSchema(Schema<ShadowEntity> skipSchema) {
        this.skipSchema = skipSchema;
    }
    
}
