package app.kitsune.cli.command

import app.kitsune.library.ApkUtils
import app.kitsune.library.ApkUtils.applyTo
import app.kitsune.library.Options
import app.kitsune.library.Options.setOptions
import app.kitsune.library.installation.installer.*
import app.kitsune.library.setOptions
import app.kitsune.patcher.Patcher
import app.kitsune.patcher.PatcherConfig
import app.kitsune.patcher.patch.Patch
import app.kitsune.patcher.patch.PatchBundle
import app.kitsune.patcher.patch.loadPatchesFromJar
import com.reandroid.apkeditor.BundleMerger
import kotlinx.coroutines.runBlocking
import picocli.CommandLine
import picocli.CommandLine.ArgGroup
import picocli.CommandLine.Help.Visibility.ALWAYS
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Spec
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.logging.Logger

@CommandLine.Command(
    name = "patch",
    description = ["Patch an APK file with Kitsune custom patches support."],
)
internal object PatchCommand : Runnable {
    private val logger = Logger.getLogger(this::class.java.name)

    @Spec
    private lateinit var spec: CommandSpec

    @CommandLine.Option(
        names = ["--no-validate"],
        description = ["Disable all patch validations for custom patches"],
        showDefaultValue = ALWAYS
    )
    private var noValidate: Boolean = false

    @CommandLine.Option(
        names = ["--allow-unsigned"],
        description = ["Allow loading unsigned patch bundles"],
        showDefaultValue = ALWAYS
    )
    private var allowUnsigned: Boolean = false

    // ... [Mantén todos los otros campos existentes] ...

    override fun run() {
        // Configuración inicial modificada para Kitsune
        if (noValidate || allowUnsigned) {
            System.setProperty("revanced.patcher.verify", "false")
            logger.warning("Custom patches validation is disabled")
        }

        // ... [Mantén el resto del código de configuración inicial] ...

        logger.info("Loading patches with Kitsune modifications")
        val patches = loadPatchesWithKitsuneSupport(patchesFiles)

        val patcherTemporaryFilesPath = temporaryFilesPath.resolve("patcher")

        val (packageName, patcherResult) = Patcher(
            KitsunePatcherConfig(
                apk,
                patcherTemporaryFilesPath,
                aaptBinaryPath?.path,
                validatePatches = !noValidate,
                allowUnsigned = allowUnsigned
            )
        ).use { patcher ->
            // ... [Mantén el resto del código de patching] ...
        }

        // ... [Mantén el resto del código de guardado e instalación] ...
    }

    /**
     * Modified patch loading with Kitsune support
     */
    private fun loadPatchesWithKitsuneSupport(patchesFiles: Set<File>): Set<Patch<*>> {
        return patchesFiles.flatMap { file ->
            try {
                val bundle = PatchBundle(
                    file,
                    verifySignature = !allowUnsigned
                )
                bundle.patches.onEach { patch ->
                    when {
                        patch.isUnsupported && allowUnsigned -> 
                            logger.warning("Loaded unsigned patch: ${patch.name}")
                        patch.isUnsupported -> 
                            logger.severe("Rejected unsigned patch: ${patch.name}")
                        else -> 
                            logger.fine("Loaded official patch: ${patch.name}")
                    }
                }
            } catch (e: Exception) {
                logger.severe("Failed to load patches from ${file.name}: ${e.message}")
                emptyList()
            }
        }.toSet()
    }

    /**
     * Kitsune-specific PatcherConfig
     */
    class KitsunePatcherConfig(
        apk: File,
        resourceCachePath: File,
        aaptBinaryPath: String?,
        validatePatches: Boolean,
        val allowUnsigned: Boolean
    ) : PatcherConfig(
        apk,
        resourceCachePath,
        aaptBinaryPath,
        validatePatches = validatePatches
    ) {
        init {
            System.setProperty("revanced.patcher.unsigned.allowed", allowUnsigned.toString())
        }
    }

    /**
     * Modified patch filtering for Kitsune
     */
    private fun Set<Patch<*>>.filterPatchSelection(
        packageName: String,
        packageVersion: String,
    ): Set<Patch<*>> = buildSet {
        this@filterPatchSelection.forEach { patch ->
            val patchName = patch.name ?: return@forEach
            
            // Skip compatibility checks if noValidate is true
            if (!noValidate) {
                patch.compatiblePackages?.let { packages ->
                    val compatible = packages.any { (name, versions) -> 
                        name == packageName && 
                        (versions?.isEmpty() == true || versions.contains(packageVersion))
                    }
                    if (!compatible) {
                        logger.fine("\"$patchName\" skipped (incompatible)")
                        return@forEach
                    }
                }
            }
            
            add(patch)
            logger.fine("\"$patchName\" added")
        }
    }

    // ... [Mantén todos los otros métodos existentes] ...
}