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
public class SignatureFailedException extends Exception {

    public SignatureFailedException() {
    }

    public SignatureFailedException(String message) {
        super(message);
    }
}