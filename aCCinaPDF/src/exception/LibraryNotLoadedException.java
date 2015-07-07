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
public class LibraryNotLoadedException extends Exception {

    public LibraryNotLoadedException() {
    }

    public LibraryNotLoadedException(String message) {
        super(message);
    }
}
