package com.rittvic.extractiontool.utils.cmdline;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class ExportArgValidator implements IParameterValidator {
    @Override
    public void validate(String name, String value) throws ParameterException {
        if (!(value.equals("all") || value.equals("merged-only"))) {
            throw new ParameterException("Parameter " + name + " can only be set to \"all\" or \"merged-only\"!");
        }
    }
}
