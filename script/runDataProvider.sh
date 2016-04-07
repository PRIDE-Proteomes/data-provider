#!/bin/sh


##### VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="proteomes-data-provider"
# the job parameters that are going to be passed on to the job (build below)
JOB_PARAMETERS="-next"
# memory limit
MEMORY_LIMIT=15000
# LSF email notification
JOB_EMAIL="ntoro@ebi.ac.uk"
# LSF command to run
COMMAND="-jar ${project.build.finalName}.jar launch-data-provider-job.xml proteomesDataProviderJob"
#COMMAND="-jar ${project.build.finalName}.jar launch-data-provider-job.xml proteomesDataProviderJob"

CLUSTER_ENV=""

PROTEOMES_ENV=""

# OUTPUT
STD_ERR="output/data-provider-stderr.txt"
STD_OUT="output/data-provider-stdout.txt"

# QUEUE
QUEUE="production-rh6"

##### FUNCTIONS
printUsage() {
    echo "Description: Data provider pipeline extracts all the peptiforms from the PRIDE Cluster resource and write them in the PRIDE Proteomes pipeline after and enrichment phase."
    echo ""
    echo "Usage: ./runDataProvider.sh [-e|--email] "

    echo "     Example: ./runDataProvider.sh -e ntoro@ebi.ac.uk -p test -c prod"
    echo "     (required) proteomes: Proteomes database environment -> prod | dev | test"
    echo "     (required) cluster:  Cluster database environment -> prod | dev | test"
    echo "     (optional) email: Email to send LSF notification"
    echo "     (optional) job-params: Allows to add the run.id to relaunch a previous unfinished job (e.g. run.id(long)=4 )"

}


##### PARSE the provided parameters
while [ "$1" != "" ]; do
    case $1 in
      "-e" | "--email")
        shift
        JOB_EMAIL=$1
        ;;
      "-p" | "--proteomes")
        shift
        PROTEOMES_ENV=$1
        ;;
      "-c" | "--cluster")
        shift
        CLUSTER_ENV=$1
        ;;
      "-j" | "--job-params")
        shift
        JOB_PARAMETERS=$1
        ;;
    esac
    shift
done


##### CHECK the provided arguments
if [ -z ${CLUSTER_ENV} ]; then
         echo "Need to enter a valid cluster environment"
         echo ""
         printUsage
         exit 1
else
    case ${CLUSTER_ENV} in
    "prod")
        echo "Cluster environment selected: prod"
        ;;
    "dev")
        echo "Cluster environment selected: dev"
        ;;
    "test")
        echo "Cluster environment selected: test"
        ;;
    * )
        echo "Need to enter a valid cluster environment"
        printUsage
        exit 1
    esac
fi

if [ -z ${PROTEOMES_ENV} ]; then
         echo "Need to enter a valid proteomes environment"
         echo ""
         printUsage
         exit 1
else
    case ${PROTEOMES_ENV} in
    "prod")
        echo "Proteomes environment selected: prod"
        ;;
    "dev")
        echo "Proteomes environment selected: dev"
        ;;
    "test")
        echo "Proteomes environment selected: test"
        ;;
    * )
        echo "Need to enter a valid proteomes environment"
        printUsage
        exit 1
    esac
fi

##### RUN it on the production LSF cluster #####
##### NOTE: you can change LSF group to modify the number of jobs can be run concurrently #####
bsub  -q ${QUEUE} -e ${STD_ERR} -o ${STD_OUT} -M ${MEMORY_LIMIT} -q production-rh6 -J ${JOB_NAME} -N -u ${JOB_EMAIL} /nfs/pride/work/java/jdk1.8.0_65/bin/java -Xmx${MEMORY_LIMIT}m -DCLUSTER_ENVIRONMENT=${CLUSTER_ENV} -DPROTEOMES_ENVIRONMENT=${PROTEOMES_ENV} ${COMMAND} ${JOB_PARAMETERS}
