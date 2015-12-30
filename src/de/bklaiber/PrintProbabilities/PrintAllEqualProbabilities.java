package de.bklaiber.PrintProbabilities;

import java.util.ArrayList;

import de.bklaiber.Types.ProbabilityConditional;

/**
 * @author birgit klaiber
 *
 */
public class PrintAllEqualProbabilities implements
		PrintConditionalProbabilities
{

	private ArrayList<ProbabilityConditional> listOfConditionals;
	

	/**
	 * @param listOfConditionals
	 */
	public PrintAllEqualProbabilities(
			ArrayList<ProbabilityConditional> listOfConditionals)
	{
		this.listOfConditionals = listOfConditionals;
		
	}// endofconstructor

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder probCond = new StringBuilder();
		boolean withoutProb = false;

		ProbabilityConditional probabilityConditional = listOfConditionals
				.get(0);
		
		probCond.append(probabilityConditional.relationalToString(withoutProb));
	

		return probCond.toString();
	}

}//endofPrintAllEqualProbabilities