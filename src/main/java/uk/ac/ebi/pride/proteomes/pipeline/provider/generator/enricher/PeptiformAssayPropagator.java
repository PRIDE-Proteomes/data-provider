package uk.ac.ebi.pride.proteomes.pipeline.provider.generator.enricher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemProcessor;
import uk.ac.ebi.pride.proteomes.db.core.api.assay.Assay;
import uk.ac.ebi.pride.proteomes.db.core.api.param.CellType;
import uk.ac.ebi.pride.proteomes.db.core.api.param.Disease;
import uk.ac.ebi.pride.proteomes.db.core.api.param.Tissue;
import uk.ac.ebi.pride.proteomes.db.core.api.peptide.Peptiform;

import java.util.HashSet;

/**
 * User: ntoro
 * Date: 06/12/2013
 * Time: 10:29
 */
public class PeptiformAssayPropagator implements ItemProcessor<Peptiform, Peptiform> {

    private static final Log log = LogFactory.getLog(PeptiformAssayPropagator.class);

    public Peptiform process(Peptiform item) throws Exception {

        // propagate the CVs from the assays to the peptide (at least one should be present)
        if (item.getAssays() != null && !item.getAssays().isEmpty()) {
            for (Assay assay : item.getAssays()) {

                //Cell Types
                if (assay.getCellTypes() != null) {
                    if (item.getCellTypes() == null) {
                        item.setCellTypes(new HashSet<CellType>());
                    }
                    item.getCellTypes().addAll(assay.getCellTypes());
                }

                if (assay.getTissues() != null) {
                    //Tissues
                    if (item.getTissues() == null) {
                        item.setTissues(new HashSet<Tissue>());
                    }
                    item.getTissues().addAll(assay.getTissues());

                }

                if (assay.getDiseases() != null) {
                    //Diseases
                    if (item.getDiseases() == null) {
                        item.setDiseases(new HashSet<Disease>());
                    }
                    item.getDiseases().addAll(assay.getDiseases());
                }
            }
        }

        return item;

    }

}
