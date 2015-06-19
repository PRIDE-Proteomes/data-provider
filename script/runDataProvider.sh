#!/bin/sh


##### VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="data-provider"
# the job parameters that are going to be passed on to the job (build below)
JOB_PARAMETERS=""
# memory limit
MEMORY_LIMIT=15000
# LSF email notification
JOB_EMAIL="ntoro@ebi.ac.uk"

##### FUNCTIONS
printUsage() {
    echo "Description: Data provider pipeline extracts all the peptiforms from the PRIDE Cluster resource and write them in the PRIDE Proteomes pipeline after and enrichment phase."
    echo ""
    echo "Usage: ./runDataProvider.sh [-e|--email] "
    echo "     Example: ./runDataProvider.sh -e ntoro@ebi.ac.uk"
    echo "     (optional) email   :  Email to send LSF notification"
}


##### PARSE the provided parameters
while [ "$1" != "" ]; do
    case $1 in
      "-e" | "--email")
        shift
        JOB_EMAIL=$1
        ;;
    esac
    shift
done


##### RUN it on the production LSF cluster #####
##### NOTE: you can change LSF group to modify the number of jobs can be run concurrently #####
bsub -e error.txt -o output.txt -M ${MEMORY_LIMIT} -q production-rh6 -J ${JOB_NAME} -N -u ${JOB_EMAIL} java -Xmx${MEMORY_LIMIT}m -jar pride-proteomes-data-provider-1.0.0-SNAPSHOT.jar