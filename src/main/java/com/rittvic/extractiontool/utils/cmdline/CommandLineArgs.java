package com.rittvic.extractiontool.utils.cmdline;

import com.beust.jcommander.Parameter;

public class CommandLineArgs {
    @Parameter(names = {"-i", "-input"},
            description = "Path to the directory with grade reports (e.g., -i path/to/grades)",
            order = 1,
            required = true)
    public String gradeDir;

    @Parameter(names = {"-o", "-output"},
            description = "Path to the output directory for exported files (e.g., -o path/to/data)",
            order = 2)
    public String outputDir = "./output";

    @Parameter(names = {"-e","-export"},
            description = """
                    Set export mode
                          all = export every individual terms, and merged terms
                          merged-only = export only the merged terms""",
            order = 3,
            validateWith = ExportArgValidator.class)
    public String exportOption = "all";

    @Parameter(names = "--help", description = "Show all usages", order = 4, help = true)
    public boolean help;

}
