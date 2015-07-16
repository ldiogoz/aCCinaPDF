/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Toshiba
 */
public class TreeNodeWithState extends DefaultMutableTreeNode {

    private final State state;

    public enum State {

        CERTIFIED,
        VALID,
        VALID_WARNING,
        CERTIFIED_WARNING,
        WARNING,
        INVALID,
        NOT_SIGNED,
        EXCLAMATION_MARK
    };

    public TreeNodeWithState(State state) {
        super();
        this.state = state;
    }

    public TreeNodeWithState(Object userObject, State state) {
        super(userObject);
        this.state = state;
    }

    public TreeNodeWithState(Object userObject, boolean allowsChildren, State state) {
        super(userObject, allowsChildren);
        this.state = state;
    }

    public State getState() {
        return state;
    }
}
