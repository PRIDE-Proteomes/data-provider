package uk.ac.ebi.pride.proteomes.pipeline.provider.generator.enricher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import uk.ac.ebi.pride.proteomes.db.core.api.assay.Assay;

/**
 * @author ntoro
 * @since 13/08/15 14:27
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/META-INF/context/data-provider-hsql-test-context.xml"})
@TransactionConfiguration(transactionManager = "proteomesTransactionManager", defaultRollback = true)
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        StepScopeTestExecutionListener.class})
public class PeptiformAssayEnricherTest {

    @Autowired
    @Qualifier(value = "peptiformAssayEnricher")
    private ItemProcessor<Assay,Assay> itemProcessor;

    private static final String ASSAY_ACCESSION = "Q8NEZ4";
    private static final String PROJECT_ACCESSION = "Q8IZP9";
    public static final int TAXID = 9606;

    @Test
    public void testProcess() throws Exception {

//        Assay item = new Assay();
//        item.setAssayAccession(ASSAY_ACCESSION);
//        item.setProjectAccession(PROJECT_ACCESSION);
//        item.setTaxid(TAXID);
//
//        Assay other = itemProcessor.process(item);


        //As Q8NEZ4 doesn't have any feature the item returned is null to avoid write it again in the db becasue there is no new information
//        Assert.assertNotNull(other);
//        Assert.assertEquals(PROJECT_ACCESSION, other.getProjectAccession());
//        Assert.assertEquals(ASSAY_ACCESSION, other.getAssayAccession());
//        Assert.assertEquals((Integer)TAXID, other.getTaxid());
//        Assert.assertEquals("", other.getTissues().toString());
//        Assert.assertEquals("", other.getCellTypes().toString());
//        Assert.assertEquals("", other.getDiseases().toString());


    }

    @Test
    public void testCvTerms() throws Exception {

    }
}