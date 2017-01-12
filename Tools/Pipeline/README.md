# Semantic Profiling Text Mining Pipeline
This directory contains the text mining pipeline used to extract relevant information from the dataset. The pipeline requires Java version 8.0 or better and [GATE](www.gate.ac.uk) version 3.0 or better.

## How to Run
You should first have GATE version 3.0 or better installed on your system. Open the pipeline XGAPP file in the [gate](../Pipeline/gate) directory in your GATE developer environment.

**Note:**
 1. By default, the pipeline assumes that the triplestore where the triples will be added is `/tmp/tdb`.
 2. The pipeline assumes that you have a DBpedia Spotlight locally installed and published on `localhost:2222`.

## License

The text mining pipeline is distributed under the terms of the [GNU LGPL v3.0](https://www.gnu.org/licenses/lgpl-3.0.en.html). You can find a copy of the license in the [pipeline](../Pipeline/gate) folder.

