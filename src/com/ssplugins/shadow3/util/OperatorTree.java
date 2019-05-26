package com.ssplugins.shadow3.util;

import com.ssplugins.shadow3.def.OperatorAction;
import com.ssplugins.shadow3.exception.ShadowException;
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
        return current instanceof ValueNode;
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
    
    public static abstract class Node<T> {
        
        protected T value;
        protected Node parent;
        protected Node[] children;
        
        public Node(T value, Node parent, int children) {
            this.value = value;
            this.parent = parent;
            this.children = new Node[children];
        }
        
        public abstract Object objectValue(Scope scope);
        
        private void unlink() {
            if (parent == null) return;
            for (int i = 0; i < parent.children.length; ++i) {
                if (parent.children[i] == this) {
                    parent.children[i] = null;
                    break;
                }
            }
            parent = null;
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
                    return;
                }
            }
            throw new IllegalStateException("Node has reached maximum capacity.");
        }
        
        public void insertAt(Node other) {
            Node parent = other.parent;
            other.unlink();
            this.link(parent);
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
    
    public static class OpNode extends Node<Operator> {
        
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
        
        @Override
        public Object objectValue(Scope scope) {
            Object[] operands = operandValues(scope);
            Class<?> left = operands[0].getClass();
            Class<?> right = operands[1].getClass();
            OperatorAction action = scope.getContext()
                                         .findOperator(getValue().getSymbol(), left, right)
                                         .orElseThrow(ShadowException.section(getValue(), "OperatorError", "No definition for operands: " + left.getSimpleName() + ", " + right.getSimpleName()));
            //noinspection unchecked (Types are known at this point)
            return action.getAction().apply(operands[0], operands[1]);
        }
        
    }
    
    public static class UnaryOpNode extends Node<Operator> {
        
        public UnaryOpNode(Operator value, Node parent) {
            super(value, parent, 1);
        }
        
        public UnaryOpNode(Operator value) {
            this(value, null);
        }
        
        public Node getChild() {
            return children[0];
        }
        
        @Override
        public Object objectValue(Scope scope) {
            Object operand = getChild().objectValue(scope);
            Class<?> type = operand.getClass();
            OperatorAction action = scope.getContext()
                                         .findOperator(getValue().getSymbol(), Void.class, type)
                                         .orElseThrow(ShadowException.section(getValue(), "OperatorError", "No definition for operand: " + type.getSimpleName()));
            //noinspection unchecked (Type is known at this point)
            return action.getAction().apply(null, type);
        }
        
    }
    
    public static class ValueNode<T> extends Node<T> {
        
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
        
        @Override
        public Object objectValue(Scope scope) {
            return section.toObject(scope);
        }
        
    }
    
}
