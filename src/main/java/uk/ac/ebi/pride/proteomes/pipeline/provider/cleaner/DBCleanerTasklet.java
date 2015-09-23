package uk.ac.ebi.pride.proteomes.pipeline.provider.cleaner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * User: ntoro
 * Date: 02/10/2013
 * Time: 16:25
 */
public class DBCleanerTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(DBCleanerTasklet.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {


        int numRows = 0;
        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.PEP_CV");
        logger.info("Num of rows deleted from PEP_CV: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.PROT_CV");
        logger.info("Num of rows deleted from PROT_CV: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.ASSAY_CV");
        logger.info("Num of rows deleted from ASSAY_CV: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.PEP_ASSAY");
        logger.info("Num of rows deleted from PEP_ASSAY: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.PEP_CLUSTER");
        logger.info("Num of rows deleted from PEP_CLUSTER: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.PEP_MOD");
        logger.info("Num of rows deleted from PEP_MOD: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.PROT_MOD");
        logger.info("Num of rows deleted from PROT_MOD: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.PEP_PROT");
        logger.info("Num of rows deleted from PEP_PROT: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.PROT_PGRP");
        logger.info("Num of rows deleted from PROT_PGRP: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.PROT_GROUP");
        logger.info("Num of rows deleted from PROT_GROUP: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.FEATURE");
        logger.info("Num of rows deleted from FEATURE: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.CLUSTER_PSM");
        logger.info("Num of rows deleted from CLUSTER_PSM: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.PROTEIN");
        logger.info("Num of rows deleted from PROTEIN: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.PEPTIDE");
        logger.info("Num of rows deleted from PEPTIDE: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.PRIDE_CLUSTER");
        logger.info("Num of rows deleted from PRIDE_CLUSTER: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.ASSAY");
        logger.info("Num of rows deleted from ASSAY: " + numRows);

        numRows = jdbcTemplate.update("DELETE FROM PRIDEPROT.CV_PARAM");
        logger.info("Num of rows deleted from CV_PARAM: " + numRows);

        return RepeatStatus.FINISHED;
    }
}
