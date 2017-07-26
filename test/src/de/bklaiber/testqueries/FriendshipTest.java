package de.bklaiber.testqueries;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import de.bklaiber.Utils.QueryReader;
import edu.cs.ai.log4KR.relational.probabilisticConditionalLogic.kbParser.log4KRReader.Log4KRReader;
import edu.cs.ai.log4KR.relational.probabilisticConditionalLogic.syntax.RelationalConditional;

public class FriendshipTest extends AbstractQueryTest {

	@Before
	public void setup() {
		super.setup();

		inference.setKnowledgebase(new Log4KRReader(), new File("test/res/Friendship.rcl"));
		queries = new Vector<RelationalConditional>(QueryReader.readQueries(new File("test/res/Friendship.rcl")));
	}

	/**
	 * Tests if the query can be processed.
	 */
	@Test
	public void queryTest() {

		super.queryTest();
	}

	/*
	@Test
	public void checkGeneralizationReflexive() {
		Vector<String> generalizations = new Vector<String>();
	
		generalizations.addElement("");
		generalizations.addElement("");
	
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
	*/
	/*
		*//**
			 * Tests if the generalization produces the results expected.
			 */

	@Test
	public void checkGeneralization() {
		Vector<String> generalizations = new Vector<String>();

		generalizations.addElement("(likes(U,V))[0.6193173107763108]<U!=V>");
		generalizations.addElement("(likes(U,V))[0.0]<U=V>");

		Vector<RelationalConditional> generalization = new Vector<RelationalConditional>(
				inference.queryConditional(queries.elementAt(0)));
		assertEquals(generalizations.elementAt(0), generalization.elementAt(0).toString());
		assertEquals(generalizations.elementAt(1), generalization.elementAt(1).toString());
		assertEquals(generalizations.size(), generalization.size());

	}

	/**
	 * Tests if the generalization produces the results expected.
	 */

	/**
	 * Tests if the runtime is below expected threshold.
	 */
	@Test
	public void checkRuntimeOfGeneralization() {

		super.checkRuntimeOfGeneralization(60000);

	}

}
