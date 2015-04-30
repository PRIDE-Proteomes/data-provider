package uk.ac.ebi.pride.proteomes.pipeline.provider.generator.mapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.pride.jmztab.model.MZTabUtils;
import uk.ac.ebi.pride.proteomes.db.core.api.assay.Assay;
import uk.ac.ebi.pride.proteomes.db.core.api.cluster.Cluster;
import uk.ac.ebi.pride.proteomes.db.core.api.modification.ModificationLocation;
import uk.ac.ebi.pride.proteomes.db.core.api.peptide.Peptiform;
import uk.ac.ebi.pride.proteomes.db.core.api.utils.PeptideUtils;
import uk.ac.ebi.pride.proteomes.db.core.api.utils.ScoreUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * User: ntoro
 * Date: 03/12/2013
 * Time: 13:52
 */
public class PeptiformClusterRowMapper implements RowMapper<Peptiform>, InitializingBean {

    private static final Log log = LogFactory.getLog(PeptiformClusterRowMapper.class);

    private static final String SEQUENCE_COLUMN = "sequence";
    private static final String TAXID_COLUMN = "taxid";
    private static final String MOD_COLUMN = "mods";
    private static final String ASSAY_ACC_COLUMN = "assay_accession";
    private static final String PROJECT_ACC_COLUMN = "project_accession";
    private static final String CLUSTER_ID_COLUMN = "cluster_id";
    public static final String SPLIT_CHAR = ":";


    public Peptiform mapRow(ResultSet rs, int rowNum) throws SQLException {

        Peptiform peptiform = new Peptiform();
        peptiform.setSequence(rs.getString(SEQUENCE_COLUMN));
        peptiform.setTaxid(rs.getInt(TAXID_COLUMN));


        //Assay and Project
        Assay assay = new Assay();
        assay.setAssayAccession(rs.getString(ASSAY_ACC_COLUMN));
        assay.setProjectAccession(rs.getString(PROJECT_ACC_COLUMN));

        //For now we are not allowing multispecies assays
        //TODO: allow multispecies at assay level
        assay.setTaxid(rs.getInt(TAXID_COLUMN));

        if(peptiform.getAssays()==null){
            Set<Assay> assaySet = new HashSet<Assay>();
            peptiform.setAssays(assaySet);
        }

        peptiform.getAssays().add(assay);


        //PeptideModifications
        if(peptiform.getModificationLocations()==null){
            TreeSet<ModificationLocation> modificationLocations = new TreeSet<ModificationLocation>();
            peptiform.setModificationLocations(modificationLocations);
        }

        String modColumn = rs.getString(MOD_COLUMN);
        parseModifications(peptiform, modColumn);

        //Cluster Id
        Cluster cluster = new Cluster();
        cluster.setClusterId(rs.getLong(CLUSTER_ID_COLUMN));

        if(peptiform.getClusters()==null){
            Set<Cluster> clusterSet = new HashSet<Cluster>();
            peptiform.setClusters(clusterSet);
        }

        peptiform.getClusters().add(cluster);


        peptiform.setScore(ScoreUtils.defaultScore());

        String representation = PeptideUtils.peptideRepresentationGenerator(peptiform);
        peptiform.setPeptideRepresentation(representation);

        return peptiform;
    }


    public void afterPropertiesSet() throws Exception {

    }

    /**
     * Transform the modification string to a ModificationLocation. It will filtered neutral losses
     *
     * @param peptiform to add the modification
     * @param modColumn raw string from the database (like  0-MOD:00394,10-MOD:00587)
     */
    protected static void parseModifications(Peptiform peptiform, String modColumn) {



        if(modColumn!= null) {


            modColumn = MZTabUtils.translateCommaToTab(modColumn);
            // 0-MOD:00394,10-MOD:00587  -> (0,MOD:00394)(10,MOD:00587)

            String[] mods = modColumn.split(",");

            for (String mod : mods) {
                uk.ac.ebi.pride.proteomes.pipeline.mods.Modification mzTabMod = uk.ac.ebi.pride.proteomes.pipeline.mods.Modification.parseModification(mod);

                if (mzTabMod == null) {
                    log.warn("Modification not parseable: " + mod);
                }
                else{
                    if (mzTabMod.getNeutralLoss() != null){
                        log.warn("The modification contains a neutral loss: " + mzTabMod.getNeutralLoss() + ". It will be ignored.");
                    }
                    else {
                        ModificationLocation pepMod = new ModificationLocation();
                        pepMod.setModId(mzTabMod.getType() + SPLIT_CHAR + mzTabMod.getAccession());

                        if (mzTabMod.getPositionMap() != null && !mzTabMod.getPositionMap().isEmpty()) {
                            pepMod.setPosition(mzTabMod.getPositionMap().entrySet().iterator().next().getKey());
                        }
                        else {
                            log.warn("The modification doesn't contain a position: " + mod + ". It will be reported as -1");
                            pepMod.setPosition(-1);
                        }

                        peptiform.getModificationLocations().add(pepMod);

                    }
                }
            }
        }
    }
}
