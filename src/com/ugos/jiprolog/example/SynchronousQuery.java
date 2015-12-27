package com.ugos.jiprolog.example;

import java.io.IOException;

import com.ugos.jiprolog.engine.JIPEngine;
import com.ugos.jiprolog.engine.JIPQuery;
import com.ugos.jiprolog.engine.JIPSyntaxErrorException;
import com.ugos.jiprolog.engine.JIPTerm;
import com.ugos.jiprolog.engine.JIPVariable;

public class SynchronousQuery {
    // main
    public static void main(String args[])
    {
        // New instance of prolog engine
        JIPEngine jip = new JIPEngine();

        JIPTerm queryTerm = null;

        // parse query
        try
        {
            // consult file
            jip.consultFile("familyrelationships.pl");

            queryTerm = jip.getTermParser().parseTerm("father(Father, Child)");
        }
        catch(JIPSyntaxErrorException ex)
        {
            ex.printStackTrace();
            System.exit(0); // needed to close threads by AWT if shareware
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            System.exit(0); // needed to close threads by AWT if shareware
        }

        // open Query
        JIPQuery jipQuery = jip.openSynchronousQuery(queryTerm);
        JIPTerm solution;

        // Loop while there is another solution
        while (jipQuery.hasMoreChoicePoints())
        {
            solution = jipQuery.nextSolution();
            System.out.println(solution);

            JIPVariable[] vars = solution.getVariables();
            for (JIPVariable var : vars) {
                if (!var.isAnonymous()) {
                    System.out.print(var.getName() + " = " + var.toString(jip) + " ");
                    System.out.println();
                }
            }
        }
    }
}
