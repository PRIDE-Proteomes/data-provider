package uk.ac.ebi.pride.proteomes.pipeline.provider.generator.mapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.pride.proteomes.db.core.api.cluster.ClusterPsm;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User: ntoro
 * Date: 03/12/2013
 * Time: 13:52
 */
public class PsmsClusterRowMapper implements RowMapper<ClusterPsm>, InitializingBean {

    private static final Log log = LogFactory.getLog(PsmsClusterRowMapper.class);

    private static final String SEQUENCE_COLUMN = "sequence";
    private static final String TAXID_COLUMN = "taxid";
    private static final String MOD_COLUMN = "mods";
    private static final String ASSAY_COLUMN = "assay_accession";
    private static final String PROJECT_COLUMN = "project_accession";
    private static final String CLUSTER_ID_COLUMN = "cluster_id";


    public ClusterPsm mapRow(ResultSet rs, int rowNum) throws SQLException {

        ClusterPsm item = new ClusterPsm();
        item.setSequence(rs.getString(SEQUENCE_COLUMN));
        item.setTaxid(rs.getInt(TAXID_COLUMN));
        item.setModifications(rs.getString(MOD_COLUMN));
        item.setAssayAccession(rs.getString(ASSAY_COLUMN));
        item.setProjectAccession(rs.getString(PROJECT_COLUMN));
        item.setClusterId(rs.getString(CLUSTER_ID_COLUMN));


        return item;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
