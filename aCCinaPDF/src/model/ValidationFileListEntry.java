/*
 *   Copyright 2015 Luís Diogo Zambujo, Micael Sousa Farinha and Miguel Frade
 *
 *   This file is part of aCCinaPDF.
 *
 *   aCCinaPDF is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   aCCinaPDF is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with aCCinaPDF.  If not, see <http://www.gnu.org/licenses/>.
 *
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
