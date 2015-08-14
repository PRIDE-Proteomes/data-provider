package uk.ac.ebi.pride.proteomes.pipeline.provider.generator.enricher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.ac.ebi.pride.archive.repo.assay.service.AssayService;
import uk.ac.ebi.pride.archive.repo.assay.service.AssaySummary;
import uk.ac.ebi.pride.archive.repo.param.service.CvParamSummary;
import uk.ac.ebi.pride.proteomes.db.core.api.assay.Assay;
import uk.ac.ebi.pride.proteomes.db.core.api.assay.AssayProteomesRepository;
import uk.ac.ebi.pride.proteomes.db.core.api.modification.ModificationProteomesRepository;
import uk.ac.ebi.pride.proteomes.db.core.api.param.*;
import uk.ac.ebi.pride.proteomes.db.core.api.peptide.PeptideRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * User: ntoro
 * Date: 06/12/2013
 * Time: 10:29
 */
public class PeptiformAssayEnricher implements ItemProcessor<Assay, Assay> {

    private static final Log log = LogFactory.getLog(PeptiformAssayEnricher.class);

    @Autowired
    @Qualifier("cvParamProteomesRepository")
    private CvParamProteomesRepository cvParamRepository;

    @Autowired
    @Qualifier("assayProteomesRepository")
    private AssayProteomesRepository proteomesAssayRepository;

    @Autowired
    @Qualifier("modificationProteomesRepository")
    private ModificationProteomesRepository modificationRepository;

    @Autowired
    @Qualifier("peptideRepository")
    private PeptideRepository peptideRepository;

    @Autowired
    private AssayService assayService;


    //TODO: Improve the caching mechanism
    private HashMap<String, CvParam> enrichedCvs = new HashMap<String, CvParam>();


    public Assay process(Assay item) throws Exception {


        //If we find or not is independent, we are going to enrich it overwriting the previous information
        final String assayAccession = item.getAssayAccession();

        if (!proteomesAssayRepository.exists(assayAccession)) {
            log.debug("Assay ac: " + assayAccession + "exists in the proteomes database");
        }

        //We retrieve the information from the Archive
        AssaySummary archiveAssay = assayService.findByAccession(assayAccession);

        assert archiveAssay != null;
        log.debug("Assay ac: " + assayAccession);


        //Cell Type
        Set<CellType> cellTypes = cvTerms(archiveAssay.getCellTypes(), cvParamRepository, enrichedCvs, CellType.class);
        if (!cellTypes.isEmpty()) {
            item.setCellTypes(cellTypes);
        }

        //Tissues
        Set<Tissue> tissues = cvTerms(archiveAssay.getTissues(), cvParamRepository, enrichedCvs, Tissue.class);
        if (!tissues.isEmpty()) {
            item.setTissues(tissues);
        }

        //Diseases
        Set<Disease> diseases = cvTerms(archiveAssay.getDiseases(), cvParamRepository, enrichedCvs, Disease.class);
        if (!diseases.isEmpty()) {
            item.setDiseases(diseases);
        }

        return item;

        //propagate the CVs from the assays to the peptide (at least one should be present)
//        for (Assay assay : itemAssays) {
//
//            //Cell Types
//            if (assay.getCellTypes() != null) {
//                if (item.getCellTypes() == null) {
//                    item.setCellTypes(new HashSet<CellType>());
//                }
//                for (CellType cellType : assay.getCellTypes()) {
//                    item.getCellTypes().add(cellType);
//                }
//            }
//
//            if (assay.getTissues() != null) {
//                //Tissues
//                if (item.getTissues() == null) {
//                    item.setTissues(new HashSet<Tissue>());
//                }
//                for (Tissue tissue : assay.getTissues()) {
//                    item.getTissues().add(tissue);
//                }
//            }
//
//            if (assay.getDiseases() != null) {
//                //Diseases
//                if (item.getDiseases() == null) {
//                    item.setDiseases(new HashSet<Disease>());
//                }
//                for (Disease disease : assay.getDiseases()) {
//                    item.getDiseases().add(disease);
//                }
//            }
//        }


//        peptideRepository.save(item);

    }

    @SuppressWarnings("unchecked")
    protected static <T extends CvParam> Set<T> cvTerms(Collection<CvParamSummary> cvParamSummaries,
                                                               CvParamProteomesRepository cvParamRepository,
                                                               HashMap<String, CvParam> enrichedCvs,
                                                               Class<T> clazz) throws IllegalAccessException, InstantiationException {
        HashSet<T> cvParams = new HashSet<T>();
        for (CvParamSummary cvParamSummary : cvParamSummaries) {
            T cvTerm = (T) cvParamRepository.findByCvTerm(cvParamSummary.getAccession());
            if (cvTerm == null) {
                cvTerm = (T) enrichedCvs.get(cvParamSummary.getAccession());
                if (cvTerm == null) {
                        cvTerm = clazz.newInstance();

                    cvTerm.setCvTerm(cvParamSummary.getAccession());
                    cvTerm.setCvName(cvParamSummary.getName());
                    cvTerm.setDescription(cvParamSummary.getName());
                    //This is persisted with the assay (Cascade.ALL)
                    //tissue = (Tissue) cvParamRepository.save(tissue);

                }
            }

            enrichedCvs.put(cvParamSummary.getAccession(), cvTerm);
            cvParams.add(cvTerm);
        }

        return cvParams;
    }
}
