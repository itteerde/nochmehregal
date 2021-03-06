package de.bklaiber.testqueries;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collection;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import de.bklaiber.Utils.QueryReader;
import de.bklaiber.inference.CanonicalMinimumGeneralization;
import edu.cs.ai.log4KR.relational.probabilisticConditionalLogic.kbParser.log4KRReader.Log4KRReader;
import edu.cs.ai.log4KR.relational.probabilisticConditionalLogic.syntax.RelationalConditional;

/**
 * Tests how <code>Inference</code> works for the birds example from...
 * 
 * 
 * @author bklaiber
 *
 */
public class BirdsTest extends AbstractQueryTest {

	/**
	 * Setup the knowledgebase for all further testing. As the knowledgebase
	 * does not change for the method provided by the <code>Inference</code>
	 * component it is set up.
	 */
	@Before
	public void setup() {
		super.setup();

		inference.setKnowledgebase(new Log4KRReader(), new File("test/res/Birds.rcl"));

		queries = new Vector<RelationalConditional>(QueryReader.readQueries(new File("test/res/Birds.rcl")));

	}

	/**
	 * Tests if the query can be processed.
	 */
	@Test
	public void queryTest() {

		super.queryTest();
	}

	@Test
	public void checkGeneralizationNegative() {
		Vector<String> generalizations = new Vector<String>();

		generalizations.addElement("(flies(X))[0.6636035435403175]<X!=Tweety>");
		generalizations.addElement("(flies(Tweety))[0.008344314635674319]");

		Collection<RelationalConditional> groundInstances = inference.ground(queries.elementAt(0));
		Collection<Collection<RelationalConditional>> classifiedClasses = inference.classify(queries.elementAt(0),
				groundInstances);
		CanonicalMinimumGeneralization generalization = (CanonicalMinimumGeneralization) inference.getGeneralization();

		Vector<RelationalConditional> generalizedClasses = new Vector<RelationalConditional>(
				generalization.generalizeNegative(queries.elementAt(0), classifiedClasses));

		assertEquals(generalizations.elementAt(0), generalizedClasses.elementAt(0).toString());
		assertEquals(generalizations.elementAt(1), generalizedClasses.elementAt(1).toString());

		assertEquals(generalizations.size(), generalizedClasses.size());

	}

	@Test
	public void checkGeneralizationPositive() {
		Vector<String> generalizations = new Vector<String>();

		generalizations.addElement("(flies(X))[0.6636035435403175]<((X=Sylvester + X=Kirby) + X=Bully)>");
		generalizations.addElement("(flies(Tweety))[0.008344314635674319]");

		Collection<RelationalConditional> groundInstances = inference.ground(queries.elementAt(0));
		Collection<Collection<RelationalConditional>> classifiedClasses = inference.classify(queries.elementAt(0),
				groundInstances);
		CanonicalMinimumGeneralization generalization = (CanonicalMinimumGeneralization) inference.getGeneralization();

		Vector<RelationalConditional> generalizedClasses = new Vector<RelationalConditional>(
				generalization.generalizePositive(queries.elementAt(0), classifiedClasses));

		assertEquals(generalizations.elementAt(0), generalizedClasses.elementAt(0).toString());
		assertEquals(generalizations.elementAt(1), generalizedClasses.elementAt(1).toString());

		assertEquals(generalizations.size(), generalizedClasses.size());

	}

	//Tests if the generalization produces the results expected.

	@Test
	public void checkGeneralization() {
		Vector<String> generalizations = new Vector<String>();

		generalizations.addElement("(flies(X))[0.6636035435403175]<X!=Tweety>");
		generalizations.addElement("(flies(Tweety))[0.008344314635674319");

		Vector<RelationalConditional> generalization = new Vector<RelationalConditional>(
				inference.queryConditional(queries.elementAt(0)));
		assertEquals(generalizations.elementAt(0), generalization.elementAt(0).toString());
		assertEquals(generalizations.elementAt(1), generalization.elementAt(1).toString());

		assertEquals(generalizations.size(), generalization.size());

	}

	/**
	 * Tests if the runtime is below expected threshold.
	 */
	@Test
	public void checkRuntimeOfGeneralization() {

		super.checkRuntimeOfGeneralization(60000);

	}

}
