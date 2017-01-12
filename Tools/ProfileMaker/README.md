# ProfileMaker Tool Manual
ProfileMaker is a commandline tool written in Java to generate a human-readable version of semantic scholarly profiles.

ProfileMaker accepts a [TDB](https://jena.apache.org/documentation/tdb/)-based dataset of user profiles and generates one of the following output formats:

1. A LaTeX document that represents the profile in a tabular format.
2. A [LimeSurvey](https://www.limesurvey.org) input file that can be directly imported into a LimeSurvey installation.

## How To Run
ProfileMaker project contains an ANT [build file](../ProfileMaker/build.xml) that will automatically resolve all of its dependencies using Maven.

The ProfileMaker tool needs two input arguments to function properly:
* the `dataset` variable should point to the absolute path of a tdb-based triple containing profile triples. The default path is `/tmp/tdb`.
* the `mode` indicates the output generation mode. You can choose between `survey` or `latex`. The default value is `survey`.

To run the ProfileMaker using commandline, make sure you have [Ant](http://ant.apache.org) installed on your system. Enter the following command in your console:

```
$ ant run
```

After resolving the dependencies and compiling the tool, it will ask you to input the arguments explained above:

```
run:
  [input] Please enter the absolute path of the triplestore: (Press enter to use the defaults) [/tmp/tdb]
    
  [input] Please indicate an export mode (survey or latex): (Press enter to the use defaults) [survey]
```

The output files are stored in the root directory of the ProfileMaker project.
