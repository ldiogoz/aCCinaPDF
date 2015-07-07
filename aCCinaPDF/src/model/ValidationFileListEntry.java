/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author Toshiba
 */
public class ValidationFileListEntry {

    private ValidationStatus validationStatus;
    private final int numSignatures;
    private final String filename;

    public enum ValidationStatus {

        CERTIFIED,
        ALL_OK,
        WARNING,
        INVALID,
        NOT_SIGNED,
        UNKNOWN
    }

    public ValidationFileListEntry(String filename, int numSignatures, ValidationStatus validationStatus) {
        this.filename = filename;
        this.numSignatures = numSignatures;
        this.validationStatus = validationStatus;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public int getNumSignatures() {
        return numSignatures;
    }

    public String getFilename() {
        return filename;
    }

    public void setValidationStatus(ValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }
    
    @Override
    public String toString(){
        return "(" + numSignatures + ") " + filename;
    }

}
