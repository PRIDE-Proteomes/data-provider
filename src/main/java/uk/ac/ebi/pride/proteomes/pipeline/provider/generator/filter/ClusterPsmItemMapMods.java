package uk.ac.ebi.pride.proteomes.pipeline.provider.generator.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemProcessor;
import uk.ac.ebi.pride.proteomes.db.core.api.cluster.ClusterPsm;
import uk.ac.ebi.pride.proteomes.pipeline.mods.Modification;
import uk.ac.ebi.pridemod.ModReader;
import uk.ac.ebi.pridemod.model.PRIDEModPTM;
import uk.ac.ebi.pridemod.model.PTM;

import java.util.List;

import static uk.ac.ebi.pride.jmztab.model.MZTabUtils.translateCommaToTab;

/**
 * User: ntoro
 * Date: 06/12/2013
 * Time: 10:29
 */
public class ClusterPsmItemMapMods implements ItemProcessor<ClusterPsm, ClusterPsm> {

    private static final Log log = LogFactory.getLog(ClusterPsmItemMapMods.class);

    public static final String SPLIT_CHAR = ":";


    public ClusterPsm process(ClusterPsm item) throws Exception {


        //Now they are preinserted in the DB, but it can be uncommented in the future
        if (item.getModifications() != null && !item.getModifications().isEmpty()) {
            String mappedMods = null;
            String modColumn = item.getModifications();

            //Avoids split neutral losses by the comma
            modColumn = translateCommaToTab(modColumn);
            // 0-MOD:00394,10-MOD:00587  -> (0,MOD:00394)(10,MOD:00587)

            String[] mods = modColumn.split(",");
            assert mods.length > 0;

            mappedMods = mapModifications(mods[0]);
            if (mappedMods == null || mappedMods.isEmpty()) {
                log.debug("The provided modification " + mods[0] + " is not mappable");
                log.debug("The cluster psm " + item.toString() + " will be filtered.");

                return null;
            }

            if (mods.length > 1) {
                for (int i = 1; i < mods.length; i++) {
                    //We assume that the modifications are sorted by position =
                    String aux = mapModifications(mods[i]);
                    if (aux == null || aux.isEmpty()) {
                        log.debug("The provided modification " + mods[i] + " is not mappable");
                        log.debug("The cluster psm " + item.toString() + " will be filtered.");

                        return null;
                    } else {
                        mappedMods = mappedMods + "," + aux;
                    }
                }
            }

            item.setModifications(mappedMods);
        }

        return item;
    }


    /**
     * Transform the modification string to a ModificationLocation. It will filtered neutral losses
     *
     * @param mod raw string from the cluster database (like  0-MOD:00394)
     */
    protected static String mapModifications(String mod) {

        ModReader modReader = ModReader.getInstance();

        uk.ac.ebi.pride.proteomes.pipeline.mods.Modification mzTabMod = Modification.parseModification(mod);

        if (mzTabMod == null) {
            log.warn("Modification not parseable from the original mzTab: " + mod);
            return null;
        } else {
            if (mzTabMod.getNeutralLoss() != null) {
                log.warn("The modification contains a neutral loss: " + mzTabMod.getNeutralLoss() + ". It will be ignored.");
                return null;

            } else {
                Modification.Type type = mzTabMod.getType();

                if (!type.equals(Modification.Type.NEUTRAL_LOSS)) {

                    Double delta;

                    if (type.equals(Modification.Type.MOD) || type.equals(Modification.Type.UNIMOD)) {

                        String accession = mzTabMod.getType() + SPLIT_CHAR + mzTabMod.getAccession();
                        PTM ptm = modReader.getPTMbyAccession(accession);
                        if (ptm == null) {
                            log.debug("The provided modification " + accession + " cannot be found in the PSIMOD or Unimod ontology.");
                            return null;
                        }
                        delta = ptm.getMonoDeltaMass();

                    } else if (type.equals(Modification.Type.CHEMMOD)) {
                        try {
                            delta = Double.parseDouble(mzTabMod.getAccession());
                        } catch (NumberFormatException e) {
                            log.debug("Exception converting CHEMMOD: ", e.getCause());
                            return null;
                        }
                    } else if (type.equals(Modification.Type.PRDMOD)) {

                        //Modification already map
                        return mod;

                    } else {
                        log.debug("Modification with type: " + type + " cannot be mapped");
                        return null;
                    }

                    //We remap the modifications to PRIDE Mod
                    List<PTM> ptms = modReader.getPTMListByMonoDeltaMass(delta);
                    int count = 0;
                    for (PTM byMassPtm : ptms) {   //If more than one we overwrite
                        if (byMassPtm instanceof PRIDEModPTM) {
                            String accValue = byMassPtm.getAccession().split(SPLIT_CHAR)[1];
                            mzTabMod.setAccession(accValue);
                            mzTabMod.setType(uk.ac.ebi.pride.proteomes.pipeline.mods.Modification.Type.PRDMOD);
                            log.debug("Mapped modification: " + mzTabMod.toString());
                            count++;
                        }
                    }

                    if (count == 0) {
                        log.debug("Modification with mass: " + delta + " can not be found in PRIDEMOD");
                        log.debug("The term " + mod + " needs to be added to PRIDEMOD");
                        return null;
                    } else if (count > 1) {
                        log.warn("Modification with mass: " + delta + " is mapped to more that one PRIDEMOD term");
                    }
                }
            }
        }

        return mzTabMod.toString();
    }


}
