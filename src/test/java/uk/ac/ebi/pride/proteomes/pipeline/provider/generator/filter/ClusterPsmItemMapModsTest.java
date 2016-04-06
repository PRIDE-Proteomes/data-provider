package uk.ac.ebi.pride.proteomes.pipeline.provider.generator.filter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.pride.proteomes.db.core.api.cluster.ClusterPsm;

/**
 * @author ntoro
 * @since 11/08/15 16:20
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/META-INF/context/data-provider-hsql-test-context.xml"})
@Rollback
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        StepScopeTestExecutionListener.class})
public class ClusterPsmItemMapModsTest {

    @Autowired
    @Qualifier(value = "psmModMapper")
    private ItemProcessor<ClusterPsm,ClusterPsm> itemProcessor;

    @Test
    @Transactional(transactionManager = "proteomesTransactionManager")
    @DirtiesContext
    public void testProcess() throws Exception {

        ClusterPsm item = new ClusterPsm();
        item.setSequence("ELPGHTGYLSCCR");
        item.setModifications("12-MOD:01214,11-MOD:01214");
        item.setAssayAccession("15829");
        item.setProjectAccession("PRD000354");
        item.setTaxid(9606);
        item.setClusterId("4829792");

        ClusterPsm other = itemProcessor.process(item);


        Assert.assertNotNull(other);
        Assert.assertEquals("ELPGHTGYLSCCR", other.getSequence());
        Assert.assertEquals("11-PRDMOD:5,12-PRDMOD:5", other.getModifications());
        Assert.assertEquals("15829", other.getAssayAccession());
        Assert.assertEquals("PRD000354", other.getProjectAccession());
        Assert.assertEquals((Integer) 9606, other.getTaxid());
        Assert.assertEquals("4829792", other.getClusterId());


    }

}
