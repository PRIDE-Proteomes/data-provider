package uk.ac.ebi.pride.proteomes.pipeline.provider.generator.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemProcessor;
import uk.ac.ebi.pride.proteomes.db.core.api.modification.Modification;
import uk.ac.ebi.pride.proteomes.db.core.api.modification.ModificationLocation;
import uk.ac.ebi.pride.proteomes.db.core.api.modification.ModificationProteomesRepository;
import uk.ac.ebi.pride.proteomes.db.core.api.peptide.Peptiform;
import uk.ac.ebi.pridemod.ModReader;
import uk.ac.ebi.pridemod.model.PRIDEModPTM;
import uk.ac.ebi.pridemod.model.PTM;

import javax.annotation.Resource;

/**
 * User: ntoro
 * Date: 06/12/2013
 * Time: 10:29
 */
public class PeptiformItemInvalidModFilter implements ItemProcessor<Peptiform, Peptiform> {

    private static final Log log = LogFactory.getLog(PeptiformItemInvalidModFilter.class);


    @Resource
    private ModificationProteomesRepository modificationRepository;


    public static final String SPLIT_CHAR = ":";


    public Peptiform process(Peptiform item) throws Exception {

        ModReader modReader = ModReader.getInstance();

        //Now they are preinserted in the DB, but it can be uncommented in the future
        if (!item.getModificationLocations().isEmpty()) {
            for (ModificationLocation modificationLocation : item.getModificationLocations()) {
                String accession = modificationLocation.getModId();

                if (!modificationRepository.exists(accession)) {

                    if (accession != null && !accession.isEmpty()) {
                        //We discover the "type" of modification (UNIMOD, MOD, CHEMMOD)

                        String[] splittedAccession = accession.split(SPLIT_CHAR);
                        if (splittedAccession.length != 2) {

                            log.warn("The provided modification " + accession + " is not properly formated");
                            log.warn("The peptiform " + item + " will be filtered.");

                            return null;

                        }

                        String type = splittedAccession[0];

                        //At this point the modifications have been remapped to PRIMODs
                        assert uk.ac.ebi.pride.proteomes.pipeline.mods.Modification.findType(type).equals(uk.ac.ebi.pride.proteomes.pipeline.mods.Modification.Type.PRDMOD);

                        PTM ptm = modReader.getPTMbyAccession(accession);
                        if (ptm == null || !(ptm instanceof PRIDEModPTM)) {
                            log.warn("The provided modification " + accession + " cannot be found in the PRDMOD ontology.");
                            log.warn("The peptiform " + item + " will be filtered.");

                            return null;
                        }

                        PRIDEModPTM prideModPTM = ((PRIDEModPTM) ptm);
                        Modification modification = new Modification();
                        modification.setModId(prideModPTM.getAccession());
                        modification.setModName(prideModPTM.getName());
                        modification.setDescription(prideModPTM.getDescription());
                        modification.setMonoDelta(prideModPTM.getMonoDeltaMass());
                        modification.setBiologicalSignificant(prideModPTM.isBiologicalSignificance());
                        modificationRepository.save(modification);

                    }
                }
            }
        }
        return item;
    }
}
