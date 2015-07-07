/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listener;

/**
 *
 * @author Toshiba
 */
public interface SignatureListener {

    public void onSignatureComplete(String filename, boolean valid, String message);
}
