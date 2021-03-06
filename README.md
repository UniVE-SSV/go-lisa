# GoLiSA
The Go static analyzer implementing the substring relational abstract domain (POPL 2022)

## How to use the static analyzer
The main class is [GoLiSA](go-lisa/src/main/java/it/unive/golisa/cli/GoLiSA.java) and it expects three paramaters:
- `input_file.go`: the Go file to be analyzed
- `output_dir`: the output directory
- `domain`: can be either `-tarsis` or `-rsubs` (default: `-tarsis`)

Golang test cases (contaning the Go examples reported in the paper) are available [here](go-lisa/go-testcases/popl).

## How to build the project ##
GoLiSA comes as a Gradle 6.0 project. For development with Eclipse, please install the [Gradle IDE Pack](https://marketplace.eclipse.org/content/gradle-ide-pack) plugin from the Eclipse marketplace, and make sure to import the project into the workspace as a Gradle project.
