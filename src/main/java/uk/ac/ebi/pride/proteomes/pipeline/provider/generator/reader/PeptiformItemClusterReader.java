package uk.ac.ebi.pride.proteomes.pipeline.provider.generator.reader;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import uk.ac.ebi.pride.proteomes.db.core.api.cluster.Cluster;
import uk.ac.ebi.pride.proteomes.db.core.api.peptide.Peptiform;
import uk.ac.ebi.pride.proteomes.db.core.api.utils.ScoreUtils;


/**
 * User: ntoro
 * Date: 05/12/2013
 * Time: 10:28
 */
public class PeptiformItemClusterReader implements ItemStreamReader<Peptiform>, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(PeptiformItemClusterReader.class);

    // signals there are no resources to read -> just return null on first read

    Peptiform result;


    private JdbcCursorItemReader<? extends Peptiform> delegate;


    // We assume for this reader that the records are sorted by sequence, so when de sequence + mods + species are different we create a new
    // Peptiform and released the previous one
    public Peptiform read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        //First element
        Peptiform aux = null;
        Peptiform current = delegate.read();

        //First element
        if (result == null) {
            result = current;
        }

        do {
            //Last element
            if (current == null) {
                if (result != null) {
                    log.debug(result.toString());
                }
                aux = result;
                result = null;
            } else {
                if (result.getPeptideRepresentation().equals(current.getPeptideRepresentation())) {
                    updateItem(current);
                    current = delegate.read();
                } else {

                    result.setScore(ScoreUtils.defaultScore());

//                    String representation = PeptideUtils.peptideRepresentationGenerator(result);
//                    result.setPeptideRepresentation(representation);

                    aux = result;
                    result = current;
                }
            }

        } while (aux == null && result != null);


        if (aux != null) {
            log.debug(aux.toString());
        }

        return aux;


    }

    //We need to group the assays and the cluster Ids
    private void updateItem(Peptiform currentPepVariant) {

        assert result != null;
        assert currentPepVariant.getAssays().size() == 1;
        assert currentPepVariant.getClusters().size() == 1;

        //We assume that the previous step is retrieving the assays and cluster one by one
        for (uk.ac.ebi.pride.proteomes.db.core.api.assay.Assay assay : currentPepVariant.getAssays()) {
            result.getAssays().add(assay);
        }

        for (Cluster cluster : currentPepVariant.getClusters()) {
            result.getClusters().add(cluster);
        }

    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(delegate, "An AbstractPagingItemReader as delegate must be set");
    }

    public void open(ExecutionContext executionContext) throws ItemStreamException {
        delegate.open(executionContext);
    }

    public void update(ExecutionContext executionContext) throws ItemStreamException {
        delegate.update(executionContext);
    }

    public void close() throws ItemStreamException {
        delegate.close();
    }

    public void setDelegate(JdbcCursorItemReader<? extends Peptiform> delegate) {
        this.delegate = delegate;
    }
}
