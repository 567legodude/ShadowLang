package com.ssplugins.shadow3.util;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.compile.GenerateContext;
import com.ssplugins.shadow3.compile.JavaComponent;
import com.ssplugins.shadow3.def.OperatorType;
import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.parsing.Token;
import com.ssplugins.shadow3.parsing.TokenLine;
import com.ssplugins.shadow3.section.Operator;
import com.ssplugins.shadow3.section.ShadowSection;

public class OperatorTree {
    
    private TokenLine line;
    
    private Node root;
    private Node current;
    
    public OperatorTree(TokenLine line) {
        this.line = line;
    }
    
    private void ensure(boolean condition, Token token, String msg) {
        if (!condition) throw new ShadowParseError(line, token.getIndex(), msg);
    }
    
    private String nodeName(Node node) {
        if (node instanceof OpNode) {
            return "binary operator";
        }
        else if (node instanceof UnaryOpNode) {
            return "unary operator";
        }
        else if (node instanceof ValueNode) {
            return "value";
        }
        return "unknown type";
    }
    
    private int precedence(Operator operator) {
        return operator.getOrder().getPrecedence();
    }
    
    private boolean comparePrecedence(Node a, Node b) {
        if (!(a instanceof OpNode && b instanceof OpNode)) return false;
        if (((Operator) b.value).isLeftToRight()) {
            return precedence(((Operator) a.value)) >= precedence(((Operator) b.value));
        }
        else {
            return precedence(((Operator) a.value)) > precedence(((Operator) b.value));
        }
    }
    
    public void insert(Node node, Token token) {
        if (current == null) {
            ensure(!(node instanceof OpNode), token, "Expected value or unary operator, found binary operator.");
            setRoot(node);
            current = node;
        }
        else if (current instanceof UnaryOpNode && ((UnaryOpNode) current).getChild() == null) {
            ensure(node instanceof ValueNode, token, "Expected value, found " + nodeName(node));
            current.addChild(node);
        }
        else if (current instanceof OpNode) {
            ensure(!(node instanceof OpNode), token, "Expected value or unary operator, found binary operator.");
            current.addChild(node);
            current = node;
        }
        else if (!(node instanceof OpNode)) {
            throw new ShadowParseError(line, token.getIndex(), "Expected binary operator, found " + nodeName(node));
        }
        else {
            while (comparePrecedence(current.parent, node)) {
                current = current.parent;
            }
            node.insertAt(current);
            if (current == root) {
                root = node;
            }
            current = node;
        }
    }
    
    public boolean expectingUnary() {
        return current == null || current instanceof OpNode;
    }
    
    public boolean isFinished() {
        return current instanceof ValueNode || current instanceof UnaryOpNode;
    }
    
    public Object getValue(Scope scope) {
        if (root == null) return null;
        return root.objectValue(scope);
    }
    
    public void placeRoot(Node node) {
        if (root != null) node.addChild(root);
        root = node;
    }
    
    public Node getRoot() {
        return root;
    }
    
    public void setRoot(Node root) {
        this.root = root;
    }
    
    public static abstract class Node<T> implements JavaComponent {
        
        protected T value;
        protected Node parent;
        protected Node[] children;
        
        public Node(T value, Node parent, int children) {
            this.value = value;
            this.parent = parent;
            this.children = new Node[children];
        }
        
        public abstract Object objectValue(Scope scope);
        
        public abstract Class<?> returnType(CompileScope scope);
        
        private void replace(Node a, Node b) {
            for (int i = 0; i < children.length; ++i) {
                if (children[i] == a) {
                    a.parent = null;
                    children[i] = b;
                    b.parent = this;
                    break;
                }
            }
        }
        
        private void link(Node parent) {
            this.parent = parent;
            if (parent != null) {
                parent.addChild(this);
            }
        }
        
        public void addChild(Node node) {
            if (node == null) throw new IllegalArgumentException("Null node.");
            for (int i = 0; i < children.length; ++i) {
                if (children[i] == null) {
                    children[i] = node;
                    node.parent = this;
                    return;
                }
            }
            throw new IllegalStateException("Node has reached maximum capacity.");
        }
        
        public void insertAt(Node other) {
            Node parent = other.parent;
            if (parent != null) parent.replace(other, this);
            this.addChild(other);
        }
        
        public T getValue() {
            return value;
        }
        
        public Node getParent() {
            return parent;
        }
        
        public Node[] getChildren() {
            return children;
        }
        
    }
    
    @SuppressWarnings("unchecked")
    public static class OpNode extends Node<Operator> {
        
        private OperatorType def;
        
        public OpNode(Operator value, Node parent) {
            super(value, parent, 2);
        }
        
        public OpNode(Operator value) {
            this(value, null);
        }
        
        public Object[] operandValues(Scope scope) {
            Object[] result = new Object[2];
            Node[] children = getChildren();
            if (children[0] != null) result[0] = children[0].objectValue(scope);
            if (children[1] != null) result[1] = children[1].objectValue(scope);
            return result;
        }
        
        private OperatorType getType(ShadowContext context, Class<?> left, Class<?> right) {
            return context.findOperator(getValue().getSymbol(), left, right)
                          .orElseThrow(ShadowCodeException.section(getValue(), "OperatorError", "No matching definition for operands: " + left.getSimpleName() + ", " + right.getSimpleName()));
        }
        
        private void findType(CompileScope scope) {
            if (def != null) return;
            Node[] c = getChildren();
            Class left = c[0].returnType(scope);
            Class right = c[1].returnType(scope);
            def = scope.getContext().findOperator(getValue().getSymbol(), left, right)
                       .orElseThrow(ShadowCodeException.section(getValue(), "OperatorError", "No matching definition for operands: " + left.getSimpleName() + ", " + right.getSimpleName()));
        }
        
        @Override
        public Object objectValue(Scope scope) {
            Object[] operands = operandValues(scope);
            OperatorType type = def;
            if (type == null) {
                Class<?> left = operands[0].getClass();
                Class<?> right = operands[1].getClass();
                type = getType(scope.getContext(), left, right);
            }
            //noinspection unchecked (Types are known at this point)
            return type.getAction().execute(operands[0], operands[1], type.getLeftType(), type.getRightType());
        }
        
        @Override
        public Class<?> returnType(CompileScope scope) {
            Node[] c = getChildren();
            OperatorType type = getType(scope.getContext(), c[0].returnType(scope), c[1].returnType(scope));
            this.def = type;
            return type.getOutputType();
        }
        
        @Override
        public String getGeneration(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method) {
            findType(context.getScope());
            Node[] c = getChildren();
            String left = c[0] == null ? "null" : c[0].getGeneration(context, type, method);
            String right = c[1] == null ? "null" : c[1].getGeneration(context, type, method);
            String generate = def.getGenerator().generate(left, right, def.getLeftType(), def.getRightType(), type, method);
//            return "(" + generate + ")";
            return generate;
        }
        
    }
    
    @SuppressWarnings("unchecked")
    public static class UnaryOpNode extends Node<Operator> {
        
        private OperatorType def;
        
        public UnaryOpNode(Operator value, Node parent) {
            super(value, parent, 1);
        }
        
        public UnaryOpNode(Operator value) {
            this(value, null);
        }
        
        public Node getChild() {
            return children[0];
        }
        
        private OperatorType getType(ShadowContext context, Class<?> right) {
            return context.findOperator(getValue().getSymbol(), Void.class, right)
                          .orElseThrow(ShadowCodeException.section(getValue(), "OperatorError", "No matching definition for operand: " + right.getSimpleName()));
        }
        
        private void findType(CompileScope scope) {
            if (def != null) return;
            Node[] c = getChildren();
            Class right = c[0].returnType(scope);
            def = scope.getContext().findOperator(getValue().getSymbol(), Void.class, right)
                       .orElseThrow(ShadowCodeException.section(getValue(), "OperatorError", "No matching definition for operand: " + right.getSimpleName()));
        }
        
        @Override
        public Object objectValue(Scope scope) {
            Object operand = getChild().objectValue(scope);
            OperatorType type = def;
            if (type == null) {
                Class<?> opType = operand.getClass();
                type = getType(scope.getContext(), opType);
            }
            //noinspection unchecked (Type is known at this point)
            return type.getAction().execute(null, operand, Void.class, type.getRightType());
        }
        
        @Override
        public Class<?> returnType(CompileScope scope) {
            Node[] c = getChildren();
            OperatorType type = getType(scope.getContext(), c[0].returnType(scope));
            this.def = type;
            return type.getOutputType();
        }
        
        @Override
        public String getGeneration(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method) {
            findType(context.getScope());
            Node[] c = getChildren();
            String right = c[0] == null ? "null" : c[0].getGeneration(context, type, method);
            String generate = def.getGenerator().generate(null, right, Void.class, def.getRightType(), type, method);
//            return "(" + generate + ")";
            return generate;
        }
        
    }
    
    public static abstract class ValueNode<T> extends Node<T> {
        
        public ValueNode(T value, Node parent) {
            super(value, parent, 0);
        }
        
        public ValueNode(T value) {
            this(value, null);
        }
        
        @Override
        public Object objectValue(Scope scope) {
            return getValue();
        }
        
        @Override
        public Class<?> returnType(CompileScope scope) {
            return null;
        }
        
        @Override
        public String getGeneration(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method) {
            return null;
        }
        
    }
    
    public static class SectionNode extends ValueNode<Object> {
        
        private ShadowSection section;
        
        public SectionNode(ShadowSection section, Node parent) {
            super(null, parent);
            this.section = section;
        }
        
        public SectionNode(ShadowSection section) {
            this(section, null);
        }
        
        public ShadowSection getSection() {
            return section;
        }
        
        @Override
        public Object objectValue(Scope scope) {
            return section.toObject(scope);
        }
        
        @Override
        public Class<?> returnType(CompileScope scope) {
            return section.getReturnType(scope);
        }
        
        @Override
        public String getGeneration(GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method) {
            return section.getGeneration(context, type, method);
        }
        
    }
    
}
