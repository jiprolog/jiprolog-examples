package com.ugos.jiprolog.example;

import java.io.IOException;

import com.ugos.jiprolog.engine.JIPEngine;
import com.ugos.jiprolog.engine.JIPErrorEvent;
import com.ugos.jiprolog.engine.JIPEvent;
import com.ugos.jiprolog.engine.JIPEventListener;
import com.ugos.jiprolog.engine.JIPQuery;
import com.ugos.jiprolog.engine.JIPSyntaxErrorException;
import com.ugos.jiprolog.engine.JIPTerm;
import com.ugos.jiprolog.engine.JIPVariable;

public class AsynchronousQuery implements JIPEventListener
{
    private int m_nQueryHandle;
    private boolean end = false;

    // main
    public static void main(String args[])
    {
    	AsynchronousQuery app = new AsynchronousQuery();

        app.start();
    }

    public synchronized void start()
    {
        // New instance of prolog engine
        JIPEngine jip = new JIPEngine();

        // add listeners
        jip.addEventListener(this);

        // consult file
        try
        {
            // consult file
            jip.consultFile("familyrelationships.pl");
        }
        catch(JIPSyntaxErrorException ex)
        {
            ex.printStackTrace();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }

        JIPTerm query = null;

        // parse query
        try
        {
            query = jip.getTermParser().parseTerm("father(Father, Child)");
        }
        catch(JIPSyntaxErrorException ex)
        {
            ex.printStackTrace();
            System.exit(0);
        }

        // open Query
        synchronized(jip)
        {
            // It's better to have the first call under syncronization
            // to avoid that listeners is called before the method
            // openQuery returns the handle.
            m_nQueryHandle = jip.openQuery(query);
        }

        if(!end)
        {
        	try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    // open event occurred
	@Override
    public void openNotified(JIPEvent e)
    {
        // syncronization is required to avoid that listeners is
        // called before the method openQuery returns the handle.
        synchronized(e.getSource())
        {
            if(m_nQueryHandle == e.getQueryHandle())
            {
                System.out.println("open");
            }
        }
    }

    // more event occurred
	@Override
    public void moreNotified(JIPEvent e)
    {
        // syncronization is required to avoid that listeners is
        // called before the method openQuery returns the handle.
        synchronized(e.getSource())
        {
            if(m_nQueryHandle == e.getQueryHandle())
            {
                System.out.println("more");
            }
        }
    }

    // A solution event occurred
	@Override
    public void solutionNotified(JIPEvent e)
    {
        synchronized(e.getSource())
        {
            if(m_nQueryHandle == e.getQueryHandle())
            {
                System.out.println("solution:");
                System.out.println(e.getTerm());
                JIPTerm solution = e.getTerm();
                JIPVariable[] vars = solution.getVariables();
                for (JIPVariable var : vars) {
                    if (!var.isAnonymous()) {
                        System.out.print(var.getName() + " = " + var.toString(e.getSource()) + " ");
                        System.out.println();
                    }
                }


                e.getSource().nextSolution(e.getQueryHandle());

            }
        }
    }

    // A Term has been notified with notify/2
	@Override
    public void termNotified(JIPEvent e)
    {
        synchronized(e.getSource())
        {
            if(m_nQueryHandle == e.getQueryHandle())
            {
                System.out.println("term " + e.getTerm());
            }
        }
    }

    // The end has been reached because there wasn't more solutions
	@Override
    public synchronized void endNotified(JIPEvent e)
    {
        synchronized(e.getSource())
        {
            if(m_nQueryHandle == e.getQueryHandle())
            {
                System.out.println("end");

                // get the source of the query
                JIPEngine jip = e.getSource();

                // close query
                jip.closeQuery(m_nQueryHandle);
            }
        }

        // notify end
        notify();
    }

	@Override
    public synchronized void closeNotified(JIPEvent e)
    {
        synchronized(e.getSource())
        {
            if(m_nQueryHandle == e.getQueryHandle())
            {
                System.out.println("close");
            }
        }

        // notify end
        notify();
    }

    // An error (exception) has been raised up by prolog engine
	@Override
    public synchronized void errorNotified(JIPErrorEvent e)
    {
        synchronized(e.getSource())
        {
            if(m_nQueryHandle == e.getQueryHandle())
            {
                System.out.println("Error:");
                System.out.println(e.getError());

                // get the source of the query
                JIPEngine jip = e.getSource();

                // close query
                jip.closeQuery(m_nQueryHandle);
            }
        }

        // notify end
        notify();
    }
}
