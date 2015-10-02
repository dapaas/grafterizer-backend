/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.sintef.jarfter;


import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

import no.sintef.jarfter.JarCreatorStandAlone;

/**
 *
 * @author havahol
 */
@ApplicationPath("webresources")
public class ApplicationConfig extends Application{

    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet();
        classes.add(JarCreatorStandAlone.class);
        classes.add(JarCreator.class);
        classes.add(RawData.class);
        classes.add(Transform.class);
        classes.add(TransformStandAlone.class);
        classes.add(Warfter.class);
        //classes.add(no.sintef.gss.RestFileUtilities.class);
        //classes.add(PLMResource.class);
        return classes;
    }

    
}
