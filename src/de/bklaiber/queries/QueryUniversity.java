package de.bklaiber.queries;

import de.bklaiber.inference.Query;

public class QueryUniversity
{

	public static void main(String[] args)
	{
		Query queryUniversity = new Query("ressources/fileQuerys/University.rcl");
		 queryUniversity.query();

	}

}
