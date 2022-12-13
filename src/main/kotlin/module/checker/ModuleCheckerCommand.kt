package module.checker

import io.micronaut.configuration.picocli.PicocliRunner
import jakarta.inject.Inject
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.ansi
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(
    name = "module-checker", description = ["..."],
    mixinStandardHelpOptions = true
)
class ModuleCheckerCommand : Runnable {

    @Inject
    lateinit var api: GithubApiClient

    @Option(names = ["-v", "--verbose"], description = ["..."])
    private var verbose: Boolean = false

    override fun run() {
        val skipRepos = listOf(
            "micronaut-core",
            "micronaut-starter-ui",
            "micronaut-lambda-todo",
            "micronaut-project-template",
            "micronaut-build-plugins",
            "micronaut-build",
            "micronaut-comparisons",
            "micronaut-crac-tests",
            "micronaut-docs",
            "micronaut-docs-deploy",
            "micronaut-docs-index",
            "micronaut-examples",
            "micronaut-fuzzing",
            "micronaut-guides",
            "micronaut-guides-old",
            "micronaut-guides-poc",
            "micronaut-oauth2",
            "micronaut-profiles",
        )
        val repos = api.fetchRepos(1)
            .filterNotNull()
            .filter { !it.archived }
            .filter { it.name.startsWith("micronaut-") }
            .filter { !skipRepos.contains(it.name) }
        val width = repos.maxOf { it.name.length }
        repos
            .asSequence()
            .sortedBy { it.name }
            .forEach { process(it, width) }
    }

    fun process(repo: GithubRepo, width: Int) {
        val version = micronautVersion(repo)
        val actions = api.actions(QueryBean(repo.name))
        val latestJavaCi = actions?.latestJavaCi()
        println(
            ansi()
                .fg(if (version == "4.0.0-SNAPSHOT") Ansi.Color.GREEN else Ansi.Color.RED)
                .a(repo.name.padEnd(width))
                .a("\t")
                .a(settingsVersion(repo))
                .a("\t")
                .apply {
                    if (latestJavaCi == "success") {
                        it.a("✅")
                    } else if (latestJavaCi == "failure") {
                        it.a("❌")
                    } else {
                        it.a("❔")
                    }
                    it.reset()
                }
                .a("\t")
                .a(version)
        )
    }

    fun settingsVersion(repo: GithubRepo) =
        api.file(QueryBean(repo.name, "settings.gradle"))?.let { settings ->
            Regex("id [\"']io.micronaut.build.shared.settings[\"'] version [\"']([^'\"]+)[\"']").find(settings)?.groups?.get(1)?.value
        } ?: api.file(QueryBean(repo.name, "settings.gradle.kts"))?.let { settings ->
            Regex("id\\(\"io.micronaut.build.shared.settings\"\\) version \"([^\"]+)\"").find(settings)?.groups?.get(1)?.value
        } ?: "🤔"


    fun micronautVersion(repo: GithubRepo) =
        api.file(QueryBean(repo.name, "gradle.properties"))?.let {
            Regex("micronautVersion=(.+)").find(it)?.groups?.get(1)?.value
        } ?: api.file(QueryBean(repo.name, "gradle/libs.versions.toml"))?.let {
            Regex("micronaut[\\s]*=[\\s]*[\"'](.+)[\"']").find(it)?.groups?.get(1)?.value
                ?: "UNKNOWN"
        }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            PicocliRunner.run(ModuleCheckerCommand::class.java, *args)
        }
    }

}
