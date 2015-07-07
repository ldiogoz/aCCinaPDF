/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exception;

/**
 *
 * @author Diogo
 */
public class KeyStoreNotLoadedException extends Exception {

    public KeyStoreNotLoadedException() {
    }

    public KeyStoreNotLoadedException(String message) {
        super(message);
    }
}
