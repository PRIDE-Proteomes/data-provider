package uk.ac.ebi.pride.proteomes.pipeline.provider.generator.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemProcessor;
import uk.ac.ebi.pride.proteomes.db.core.api.cluster.ClusterPsm;
import uk.ac.ebi.pride.proteomes.pipeline.mods.Modification;
import uk.ac.ebi.pride.utilities.pridemod.ModReader;
import uk.ac.ebi.pride.utilities.pridemod.model.PRIDEModPTM;
import uk.ac.ebi.pride.utilities.pridemod.model.PTM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

        if (item.getModifications() != null && !item.getModifications().isEmpty()) {
            String modColumn = item.getModifications();

            //Avoids split neutral losses by the comma
            modColumn = translateCommaToTab(modColumn);
            // 0-MOD:00394,10-MOD:00587  -> (0,MOD:00394)(10,MOD:00587)

            String[] mods = modColumn.split(",");
            assert mods.length > 0;

            List<Modification> auxList = new ArrayList<Modification>();

            for (String mod : mods) {
                Modification modification = mapModifications(mod);

                if (modification != null) {
                    auxList.add(modification);
                } else {
                    log.debug("The provided modification " + mod + " is not mappable");
                    log.debug("The cluster psm " + item.toString() + " will be filtered.");

                    return null;
                }
            }

            //We sort the modifications in the same way (they don't come sorted from mzTab)
            Collections.sort(auxList, new Comparator<Modification>() {
                @Override
                public int compare(Modification o1, Modification o2) {
                    int aux;

                    if (o1 != null && o2 != null) {
                        int sizeMap1 = o1.getPositionMap().size();
                        int sizeMap2 = o2.getPositionMap().size();

                        //If the modification is ambiguous we are going to use only the first position to sort.
                        aux = Integer.compare(sizeMap1, sizeMap2);
                        if (aux == 0) { //We don't have anything to compare if there is no position associated to the mod
                            if (sizeMap1 != 0 && sizeMap2 != 0) {
                                aux = o1.getPositionMap().entrySet().iterator().next().getKey().compareTo(o2.getPositionMap().entrySet().iterator().next().getKey());
                            }
                        }
                    } else if (o1 != null) {
                        aux = -1;
                    } else if (o2 != null) {
                        aux = 1;
                    } else {
                        aux = 0;
                    }

                    return aux;
                }
            });


            // We assume that the modifications are sorted by position and
            // there aren't null values (they are filtered in the insertion)
            String mappedMods = auxList.get(0).toString();
            assert mappedMods != null;

            if (auxList.size() > 1) {
                for (int i = 1; i < auxList.size(); i++) {
                    String aux = auxList.get(i).toString();
                    assert aux != null;
                    mappedMods = mappedMods + "," + aux;
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
    protected static Modification mapModifications(String mod) {

        ModReader modReader = ModReader.getInstance();

        Modification mzTabMod = Modification.parseModification(mod);

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
                        return mzTabMod;

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
                            mzTabMod.setType(Modification.Type.PRDMOD);
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

        return mzTabMod;
    }

//NOTE: Version using anchor protein. It cannot be used because Unimod doesn't contain information about if the modification is biological relevant or not
//    /**
//     * Transform the modification string to a ModificationLocation. It will filtered neutral losses
//     *
//     * @param mod raw string from the cluster database (like  0-MOD:00394)
//     */
//    protected static Modification mapModifications(String mod) {
//
//        ModReader modReader = ModReader.getInstance();
//
//        Modification mzTabMod = Modification.parseModification(mod);
//
//        if (mzTabMod == null) {
//            log.warn("Modification not parseable from the original mzTab: " + mod);
//            return null;
//        } else {
//            if (mzTabMod.getNeutralLoss() != null) {
//                log.warn("The modification contains a neutral loss: " + mzTabMod.getNeutralLoss() + ". It will be ignored.");
//                return null;
//
//            } else {
//                Modification.Type type = mzTabMod.getType();
//
//                if (!type.equals(Modification.Type.NEUTRAL_LOSS)) {
//
//                    List<PTM> ptms;
//                    String accession;
//
//                    if (type.equals(Modification.Type.PRDMOD)) {
//                        //Modification already map
//                        return mzTabMod;
//
//                    } else if (type.equals(Modification.Type.MOD) || type.equals(Modification.Type.UNIMOD)) {
//
//                        accession = mzTabMod.getType() + SPLIT_CHAR + mzTabMod.getAccession();
//                        ptms = modReader.getAnchorModification(accession);
//
//                    } else if (type.equals(Modification.Type.CHEMMOD)) {
//                        try {
//                            accession = mzTabMod.getAccession();
//                            Double delta = Double.parseDouble(accession);
//                            //We remap the modifications to PRIDE Mod
//                            ptms = modReader.getAnchorMassModification(delta, null);
//
//                        } catch (NumberFormatException e) {
//                            log.debug("Exception converting CHEMMOD: ", e.getCause());
//                            return null;
//                        }
//                    } else {
//                        log.debug("Modification with type: " + type + " cannot be mapped");
//                        return null;
//                    }
//
//                    int count = 0;
//                    for (PTM ptm : ptms) {   //If more than one we overwrite
//                        if (ptm instanceof UniModPTM) {
//                            String accValue = ptm.getAccession().split(SPLIT_CHAR)[1];
//                            mzTabMod.setAccession(accValue);
//                            mzTabMod.setType(Modification.Type.PRDMOD);
//                            log.debug("Mapped modification: " + mzTabMod.toString());
//                            count++;
//                        }
//                    }
//
//                    if ( ptms.isEmpty()) {
//                        log.debug("The provided modification " + accession + " cannot be remmap to Unimod ontology.");
//                        return null;
//                    }
//                    if (ptms.size() > 1) {
//                        log.debug("The provided modification " + accession + " maps to more that one term in Unimod. Only the last one is taken into account.");
//                        return null;
//                    }
//
//
//                }
//            }
//        }
//
//        return mzTabMod;
//    }
}
