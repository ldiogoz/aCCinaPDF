/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listener;

import model.SignatureValidation;

/**
 *
 * @author Toshiba
 */
public interface ValidationListener {

    public void onValidationComplete(SignatureValidation sv);
}
