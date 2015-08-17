# oai-harvester

Quick start (using maven2)
----
Harvests the national archives set naa3 with metadataPrefix oai_ead_full; saves all records to the current working dir


```sh
git clone https://github.com/HuygensING/oai-harvester.git
cd oai-harvester
mvn install
./run.sh 40 http://www.gahetna.nl/archievenoverzicht/oai-pmh oai_ead_full naa3

```
