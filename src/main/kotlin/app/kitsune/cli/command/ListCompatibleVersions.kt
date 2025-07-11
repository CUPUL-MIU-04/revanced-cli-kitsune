package app.kitsune.cli.command

import app.kitsune.library.PackageName
import app.kitsune.library.VersionMap
import app.kitsune.library.mostCommonCompatibleVersions
import app.kitsune.patcher.patch.loadPatchesFromJar
import picocli.CommandLine
import java.io.File
import java.util.logging.Logger

@CommandLine.Command(
    name = "list-versions",
    description = [
        "List the most common compatible versions of apps that are compatible " +
            "with the patches from RVP files.",
    ],
)
internal class ListCompatibleVersions : Runnable {
    private val logger = Logger.getLogger(this::class.java.name)

    @CommandLine.Parameters(
        description = ["Paths to RVP files."],
        arity = "1..*",
    )
    private lateinit var patchesFiles: Set<File>

    @CommandLine.Option(
        names = ["-f", "--filter-package-names"],
        description = ["Filter patches by package name."],
    )
    private var packageNames: Set<String>? = null

    @CommandLine.Option(
        names = ["-u", "--count-unused-patches"],
        description = ["Count patches that are not used by default."],
        showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
    )
    private var countUnusedPatches: Boolean = false

    override fun run() {
        fun VersionMap.buildVersionsString(): String {
            if (isEmpty()) return "Any"

            fun buildPatchesCountString(count: Int) = if (count == 1) "1 patch" else "$count patches"

            return entries.joinToString("\n") { (version, count) ->
                "$version (${buildPatchesCountString(count)})"
            }
        }

        fun buildString(entry: Map.Entry<PackageName, VersionMap>) =
            buildString {
                val (name, versions) = entry
                appendLine("Package name: $name")
                appendLine("Most common compatible versions:")
                appendLine(versions.buildVersionsString().prependIndent("\t"))
            }

        val patches = loadPatchesFromJar(patchesFiles)

        patches.mostCommonCompatibleVersions(
            packageNames,
            countUnusedPatches,
        ).entries.joinToString("\n", transform = ::buildString).let(logger::info)
    }
}
