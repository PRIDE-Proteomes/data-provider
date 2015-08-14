package uk.ac.ebi.pride.proteomes.pipeline.mods;

import uk.ac.ebi.pride.jmztab.model.CVParam;
import uk.ac.ebi.pride.jmztab.model.MZTabUtils;
import uk.ac.ebi.pride.jmztab.model.Param;
import uk.ac.ebi.pride.jmztab.model.SplitList;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.ac.ebi.pride.jmztab.model.MZTabConstants.*;
import static uk.ac.ebi.pride.jmztab.model.MZTabUtils.*;

/**
 * Reporting of modifications in columns of the protein, peptide, small molecule and PSM sections.
 * Modifications or substitutions are modelled using a specific modification object with the following format:
 *
 * {position}{Parameter}-{Modification or Substitution identifier}|{neutral loss}
 *
 * Detail information, please reference 5.8 in mzTab specification v1.0
 *
 * @author qingwei
 * @since 30/01/13
 */
/*
    This is an extension of the original class develop for the mzTab. The original couldn't be reused becasue it doesn't
    support PRIDEMOD as a valid ontology for modifications
 */
public class Modification {
    public enum Type {
        MOD,             // PSI-MOD
        UNIMOD,
        CHEMMOD,
        SUBST,           // to report substitutions of amino acids
        PRDMOD,
        UNKNOWN,          // Unrecognized modification
        NEUTRAL_LOSS
    }

    private Map<Integer, CVParam> positionMap = new TreeMap<Integer, CVParam>();
    private Type type;
    private String accession;
    private CVParam neutralLoss;

    /**
     * Create a modification in columns of the protein, peptide, small molecule and PSM sections.
     * The structure like: {Type:accession}
     *
     * NOTICE: {position} is mandatory. However, if it is not known (e.g. MS1 Peptide Mass Fingerprinting),
     * "null" must be used. Thus, in construct method we not provide position parameter. User can define
     * this by using {@link #addPosition(Integer, CVParam)} method.
     *
     * @param type SHOULD NOT be null.
     * @param accession SHOULD not be empty.
     */
    public Modification(Type type, String accession) {

        if (type == null) {
            throw new NullPointerException("Modification type should not be null!");
        }
        this.type = type;

        if (MZTabUtils.isEmpty(accession)) {
            throw new IllegalArgumentException("Modification accession can not empty!");
        }
        this.accession = accession;
    }

    /**
     * If the software has determined that there are no modifications to a given protein, "0" MUST be used.
     * In this situation, we define a {@link Type#UNKNOWN} modification, which accession is "0".
     */
    public static Modification createNoModification() {
        return new Modification(Type.UNKNOWN, "0");
    }


    /**
     * Check if the position of the modification is ambiguous or not (multiple positions associated to the same modification
     */
    public boolean isAmbiguous() {
        return positionMap.size() > 1;
    }

    /**
     * Add a optional position value for modification. If not set, "null" will report.
     * {position} is mandatory. However, if it is not known (e.g. MS1 Peptide Mass Fingerprinting), 'null'
     * must be used Terminal modifications in proteins and peptides MUST be reported with the position set to
     * 0 (N-terminal) or the amino acid length +1 (C-terminal) respectively. N-terminal modifications that are
     * specifically on one amino acid MUST still be reported at the position 0. This object allows modifications
     * to be assigned to ambiguous locations, but only at the PSM and Peptide level. Ambiguity of modification
     * position MUST NOT be reported at the Protein level. In that case, the modification element can be left empty.
     * Ambiguous positions can be reported by separating the {position} and (optional) {cvParam} by an '|' from
     * the next position. Thereby, it is possible to report reliabilities / scores / probabilities etc. for every
     * potential location.
     *
     * @param id SHOULD be non-negative integer.
     * @param param Ambiguous positions can be reported by separating the {position} and  (optional) {cvParam}
     *              by an '|' from the next position. This value can set null, it MAY be used to report a numerical
     *              value e.g. a probability score associated with the modification or location.
     */
    public void addPosition(Integer id, CVParam param) {
        this.positionMap.put(id, param);
    }

    /**
     * @return the modification position map, the key is position, and value is {@link CVParam}. This value can set null,
     * it MAY be used to report a numerical value e.g. a probability score associated with the modification or location.
     */
    public Map<Integer, CVParam> getPositionMap() {
        return positionMap;
    }

    /**
     * @return Modification enum {@link Type}.
     */
    public Type getType() {
        return type;
    }

    /**
     * @return Modification accession number.
     */
    public String getAccession() {
        return accession;
    }

    /**
     * Neutral losses are reported as cvParams. They are reported in the same way that modification objects are
     * (as separate, comma-separated objects in the modification column). The position for a neutral loss MAY be reported.
     *
     * @return Neutral loss.
     */
    public CVParam getNeutralLoss() {
        return neutralLoss;
    }

    /**
     * Neutral loss is optional. Neutral losses are reported as cvParams. They are reported in the same way that
     * modification objects are (as separate, comma-separated objects in the modification column). The position
     * for a neutral loss MAY be reported.
     *
     * @param neutralLoss can set NULL.
     */
    public void setNeutralLoss(CVParam neutralLoss) {
        this.neutralLoss = neutralLoss;
    }

    /**
     * Allows modify the modification type
     * @param type  MOD,UNIMOD,CHEMMOD,SUBST,PRDMOD,UNKNOWN,NEUTRAL_LOSS
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Allow modifiy the database identifier
     * @param accession Identifier
     */
    public void setAccession(String accession) {
        this.accession = accession;
    }

    /**
     * Print Modification in metadata to String. The following are examples:
     * <ul>
     *     <li>3-MOD:00412, 8-MOD:00412</li>
     *     <li>3|4-MOD:00412, 8-MOD:00412</li>
     *     <li>3|4|8-MOD:00412, 3|4|8-MOD:00412</li>
     *     <li>3[MS,MS:1001876, modification probability, 0.8]|4[MS,MS:1001876, modification probability, 0.2]-MOD:00412, 8-MOD:00412</li>
     *     <li>CHEMMOD:+NH4</li>
     *     <li>CHEMMOD:-18.0913</li>
     *     <li>UNIMOD:18</li>
     *     <li>SUBST:{amino acid}</li>
     *     <li>3-UNIMOD:21, 3-[MS, MS:1001524, fragment neutral loss, 63.998285]</li>
     *     <li>[MS, MS:1001524, fragment neutral loss, 63.998285], 7-UNIMOD:4</li>
     *     <li>5-[MS, MS:1001524, fragment neutral loss, 63.998285], 7-UNIMOD:4</li>
     *     <li>0</li>
     * </ul>
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // no modification.
        if (type == Type.UNKNOWN) {
            return accession;
        }

        Integer id;
        Param param;
        Iterator<Integer> it;
        int count = 0;

        //position part example: 3[MS, MS:1001876, modification probability, 0.8]|4[MS, MS:1001876, modification probability, 0.2]
        if (! positionMap.isEmpty()) {
            it = positionMap.keySet().iterator();
            while (it.hasNext()) {
                id = it.next();
                param = positionMap.get(id);
                if (count++ == 0) {
                    sb.append(id);
                } else {
                    sb.append(BAR).append(id);
                }
                if (param != null) {
                    sb.append(param);
                }
            }
        }

        //example:  -
        if (positionMap.size() > 0) {
            sb.append(MINUS);
        }

        // example: MOD:00412
        if(type != Type.NEUTRAL_LOSS)
            sb.append(type).append(COLON).append(accession);

        // example: [MS, MS:1001524, fragment neutral loss, value]
        if (neutralLoss != null) {
            sb.append(neutralLoss);
        }

        return sb.toString();
    }

    /**
     * Find modification type by name with case-insensitive.
     *
     * @param name SHOULD not be empty.
     * @return If not find, return null value.
     */
    public static Type findType(String name) {
        if (MZTabUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Modification type name should not be empty!");
        }

        Type type;
        try {
            type = Type.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            type = null;
        }

        return type;
    }

    /**
     * Parse the target to {@link uk.ac.ebi.pride.jmztab.model.Modification}
     */
    public static Modification parseModification(String target) {
        target = parseString(target);
        if (target == null) {
            return null;
        }

        // no modification
        if (target.equals("0")) {
            return Modification.createNoModification();
        }

        target = translateMinusToUnicode(target);
        if (target == null) {
            return null;
        }

        target = translateTabToComma(target);
        target = translateMinusToTab(target);
        String[] items = target.split("\\-");
        String modLabel;
        String positionLabel;
        if (items.length > 2) {
            // error
            return null;
        }
        if (items.length == 2) {
            positionLabel = items[0];
            modLabel = items[1];
        } else {
            positionLabel = null;
            modLabel = items[0];
        }

        Modification modification = null;
        Type type;
        String accession;
        CVParam neutralLoss;

        modLabel = translateUnicodeToMinus(modLabel);
        modLabel = translateTabToMinus(modLabel);
        Pattern pattern = Pattern.compile("(MOD|UNIMOD|CHEMMOD|SUBST|PRDMOD):([^\\|]+)(\\|\\[([^,]+)?,([^,]+)?,([^,]+),([^,]*)\\])?");
        Matcher matcher = pattern.matcher(modLabel);
        if (matcher.find()) {
            type = Modification.findType(matcher.group(1));
            accession = matcher.group(2);
            modification = new Modification(type, accession);
            if (positionLabel != null) {
                parseModificationPosition(positionLabel, modification);
            }

            neutralLoss = matcher.group(6) == null ? null : new CVParam(matcher.group(4), matcher.group(5), matcher.group(6), matcher.group(7));
            modification.setNeutralLoss(neutralLoss);
        } else if (parseParam(modLabel) != null) {
            // Check if is a Neutral Loss
            CVParam param = (CVParam) parseParam(modLabel);
            modification = new Modification(Type.NEUTRAL_LOSS, param.getAccession());
            modification.setNeutralLoss(param);
            if (positionLabel != null) {
                parseModificationPosition(positionLabel, modification);
            }

        }

        return modification;
    }

    private static void parseModificationPosition(String target, Modification modification) {
        target = translateTabToComma(target);
        SplitList<String> list = parseStringList(BAR, target);

        Pattern pattern = Pattern.compile("(\\d+)(\\[([^,]+)?,([^,]+)?,([^,]+),([^,]*)\\])?");
        Matcher matcher;
        Integer id;
        CVParam param;
        for (String item : list) {
            matcher = pattern.matcher(item.trim());
            if (matcher.find()) {
                id = new Integer(matcher.group(1));
                param = matcher.group(5) == null ? null : new CVParam(matcher.group(3), matcher.group(4), matcher.group(5), matcher.group(6));
                modification.addPosition(id, param);
            }
        }
    }
}
