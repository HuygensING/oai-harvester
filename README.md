# oai-harvester

Quick start (using maven2)
----
Harvests the national archives set naa3 with metadataPrefix oai_ead_full


```sh
git clone https://github.com/HuygensING/oai-harvester.git
cd oai-harvester
mvn install
./run.sh 40 \ # number of parallel threads
  http://www.gahetna.nl/archievenoverzicht/oai-pmh \ # OAI repository URL
  oai_ead_full \ # metadataPrefix
  naa3 \ # set name

```
