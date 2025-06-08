package app.kitsune.cli.command.utility

import picocli.CommandLine

@CommandLine.Command(
    name = "utility",
    description = ["Commands for utility purposes."],
    subcommands = [InstallCommand::class, UninstallCommand::class],
)
internal object UtilityCommand
