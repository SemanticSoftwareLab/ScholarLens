# ScholarLens
This repository contains the ScholarLens system.

ScholarLens provides an automatic workflow, entirely composed of open source tools, to generate semantic profiles for scholars. The profiles represent the competences of scholar, automatically extracted from their available publications. The output of the workflow is a knowledge base of semantic profiles, modelled using the Resource Description Framework (RDF) and inter-linked with the web of Open Linked Data (LOD).

## Repository Structure
* **Tools**
 * [Analysis](../master/Tools/Analysis) *contains the commandline tool to analyze the results from surveys.*
 * [Pipeline](../master/Tools/Pipeline) *contains the text mining pipeline used to extract user competence records.*
 * [ProfileMaker](../master/Tools/ProfileMaker) *contains the commandline tool to generate human-readable scholarly profiles.*
* **Knowledge-base**
 * [triples.zip](../master/Knowledge-base/triples.zip) *contains the RDF-based knowledge base of semantic scholarly profiles.*
 * [LICENSE](../master/Knowledge-base/LICENSE) *is the knowledge base license file.*
* **Evaluation**
 * Templates *contains the custom LimeSurvey-compatible templates used to create profiles.*
 * Results *contains the exported results from the evaluation surveys.*
