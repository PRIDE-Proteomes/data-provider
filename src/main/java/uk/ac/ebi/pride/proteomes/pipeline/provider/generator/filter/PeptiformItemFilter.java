package uk.ac.ebi.pride.proteomes.pipeline.provider.generator.filter;

import org.springframework.batch.item.ItemProcessor;
import uk.ac.ebi.pride.proteomes.db.core.api.peptide.PeptideRepository;
import uk.ac.ebi.pride.proteomes.db.core.api.peptide.Peptiform;

import javax.annotation.Resource;

/**
 * User: ntoro
 * Date: 06/12/2013
 * Time: 10:29
 */
public class PeptiformItemFilter implements ItemProcessor<Peptiform, Peptiform> {

    @Resource
    private PeptideRepository peptideRepository;

    private Peptiform previousItem=null;

    public Peptiform process(Peptiform item) throws Exception {

        if (item.equals(previousItem)) {
            previousItem = item;
            return null;
        } else {
            previousItem = item;
            return peptideRepository.findByPeptideRepresentation(item.getPeptideRepresentation()) == null ? item : null;
        }
    }
}
