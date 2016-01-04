package se.milu.maltparser;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by Michelle on 2015-12-09.
 */
public class SeedExpansionComparerTest {

    @Test
    public void testCompareHumanSeedListAdj() throws Exception {
        
        SeedExpansionComparer seedExpansionComparer = new SeedExpansionComparer(new File("posAdjAdv.txt"), new File("negAdjAdv.txt"));
        seedExpansionComparer.compareHumanSeedList(new File("Humanclassified_adjadv.txt"));

    }

    @Test
    public void testCompareHumanSeedListVerb() throws Exception {

        SeedExpansionComparer seedExpansionComparerVerbs = new SeedExpansionComparer(new File("posVerbs.txt"), new File("negVerbs.txt"));
        seedExpansionComparerVerbs.compareHumanSeedList(new File("Humanclassified_verbs.txt"));

    }
}