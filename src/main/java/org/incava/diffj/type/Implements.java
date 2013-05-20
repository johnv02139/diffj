package org.incava.diffj.type;

import net.sourceforge.pmd.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.ast.SimpleNode;
import org.incava.ijdk.text.Message;

/**
 * Compares implements.
 */
public class Implements extends Supers {
    public static final Message IMPLEMENTED_TYPE_REMOVED = new Message("implemented type removed: {0}");
    public static final Message IMPLEMENTED_TYPE_ADDED = new Message("implemented type added: {0}");
    public static final Message IMPLEMENTED_TYPE_CHANGED = new Message("implemented type changed from {0} to {1}");

    public Implements(ASTClassOrInterfaceDeclaration decl) {
        super(decl);
    }

    protected Class<? extends SimpleNode> getPmdClass() {
        return net.sourceforge.pmd.ast.ASTImplementsList.class;
    }

    protected Message getAddedMessage() {
        return IMPLEMENTED_TYPE_ADDED;
    }

    protected Message getChangedMessage() {
        return IMPLEMENTED_TYPE_CHANGED;
    }

    protected Message getRemovedMessage() {
        return IMPLEMENTED_TYPE_REMOVED;
    }
}
