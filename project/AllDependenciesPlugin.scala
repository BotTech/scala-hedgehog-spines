import sbt.Keys._
import sbt._

// TODO: Share this between the build and meta-build.
object AllDependenciesPlugin extends AutoPlugin {

  object autoImport {

    val allDependenciesFile =
      taskKey[File]("The file to save inter-project and library dependencies.")

    val saveAllDependencies =
      taskKey[File](
        "Saves all the inter-project and library dependencies to a file."
      )
  }

  import autoImport._

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    allDependenciesFile := (LocalRootProject / target).value / "dependencies.txt",
    // TODO: Aggregate this once there is more than one project.
    saveAllDependencies := saveAllDependenciesTask.value
  )

  private def saveAllDependenciesTask: Def.Initialize[Task[File]] =
    Def.task {
      val dir  = (LocalRootProject / target).value
      val file = dir / "dependencies.txt"
      val deps = allDependencies.value
      IO.writeLines(file, deps.distinct.map(_.toString).sorted)
      file
    }
}
