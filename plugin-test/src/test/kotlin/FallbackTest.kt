import com.instamotor.mirakle.BuildConfig
import org.gradle.testkit.runner.GradleRunner
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.FileWriter
import java.io.PrintWriter

object FallbackTest : Spek({
    BuildConfig.TESTED_GRADLE_VERSIONS.forEach { gradleVersion ->
        describe("project with gradle version $gradleVersion") {
            val folder by temporaryFolder()

            beforeEachTest {
                GradleRunner.create()
                        .withProjectDir(folder.root)
                        .withGradleVersion(gradleVersion)
                        .withArguments("wrapper")
                        .build()
            }

            val buildFile by memoized { folder.newFile("build.gradle.kts") }
            fun buildFileWriter() = PrintWriter(FileWriter(buildFile, true))

            describe("mirakle init with fallback and unresolvable host") {
                beforeEachTest {
                    folder.newFile("mirakle_init.gradle")
                            .outputStream()
                            .let(::PrintWriter)
                            .use { it.append(MIRAKLE_INIT_WITH_FALLBACK_AND_UNRESOLVABLE_HOST) }
                }

                val testResult by memoized {
                    GradleRunner.create()
                            .withProjectDir(folder.root)
                            .withGradleVersion(gradleVersion)
                            .forwardOutput()
                            .withArguments("-I", "mirakle_init.gradle", "tasks")
                            .test()
                }

                it("build should not fail") {
                    testResult.assertBuildSuccessful()
                }

                it("fallback task should be executed") {
                    testResult.assertTaskSucceed("fallback")
                }

                it("executeOnRemote and downloadFromRemote should be skipped") {
                    testResult.assertTaskSkipped("executeOnRemote")
                    testResult.assertTaskSkipped("downloadFromRemote")
                }
            }

            describe("successfully configured mirakle with fallback") {
                beforeEachTest {
                    folder.newFile("mirakle_init.gradle")
                            .outputStream()
                            .let(::PrintWriter)
                            .use {
                                it.append(MIRAKLE_INIT_WITH_FOLDER(folder.root.canonicalPath).replace("fallback false", "fallback true"))
                            }
                }

                val gradleRunner by memoized {
                    GradleRunner.create()
                            .withProjectDir(folder.root)
                            .withGradleVersion(gradleVersion)
                            .forwardOutput()
                            .withArguments("-I", "mirakle_init.gradle", "tasks")
                }

                val buildFile by memoized { folder.newFile("build.gradle.kts") }
                fun buildFileWriter() = PrintWriter(FileWriter(buildFile, true))

                on("exception occurred on remote side") {
                    buildFileWriter().use { it.write(THROW) }
                    val testResult = gradleRunner.testFail()

                    it("should fail") {
                        testResult.assertBuildFailed()
                    }

                    it("should execute download task") {
                        testResult.assertTaskSucceed("downloadFromRemote")
                    }

                    it("should skip fallback task") {
                        testResult.assertTaskSkipped("fallback")
                    }
                }
            }
        }
    }
})