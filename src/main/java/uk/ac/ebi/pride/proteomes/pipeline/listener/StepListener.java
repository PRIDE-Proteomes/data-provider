package uk.ac.ebi.pride.proteomes.pipeline.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import java.sql.Timestamp;

/**
 * User: ntoro
 * Date: 08/10/2013
 * Time: 14:20
 */
public class StepListener implements StepExecutionListener {

    private static final Log log = LogFactory.getLog(StepListener.class);


    public void beforeStep(StepExecution stepExecution) {
        log.info("StepExecutionListener - " + stepExecution.getStepName() + " begins at: "
                + new Timestamp(System.currentTimeMillis()));
    }

    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("StepExecutionListener - " + stepExecution.getStepName() + " ends at: "
                + new Timestamp(System.currentTimeMillis()));
        return null;
    }

}
