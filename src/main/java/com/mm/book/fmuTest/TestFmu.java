package com.mm.book.fmuTest;

/**
 * @author 马蒙
 * @date 2024/4/1 19:09
 */
import no.ntnu.ihb.fmi4j.Fmi4jVariableUtils;
import no.ntnu.ihb.fmi4j.importer.fmi1.CoSimulationSlave;
import no.ntnu.ihb.fmi4j.importer.fmi1.Fmu;
import no.ntnu.ihb.fmi4j.modeldescription.util.FmiModelDescriptionUtil;
import no.ntnu.ihb.fmi4j.modeldescription.variables.RealVariable;

import java.io.File;
import java.io.IOException;


public class TestFmu {
    public static void main(String[] args) throws IOException {
        Fmu fmu = Fmu.from(new File("D:\\JetBrains-work\\Java\\BouncingBall.fmu")); //URLs are also supported

        CoSimulationSlave slave = fmu.asCoSimulationFmu().newInstance();
        slave.simpleSetup();

        double t = 0;
        double stop = 10;
        double stepSize = 1.0/100;

        final RealVariable h = slave.getModelDescription()
                .getVariableByName("h").asRealVariable();

        final RealVariable v = slave.getModelDescription()
                .getVariableByName("v").asRealVariable();

        while(t <= stop) {
            if (!slave.doStep(t, stepSize)) {
                break;
            }
            double hValue = Fmi4jVariableUtils.read(h, slave).getValue();
            double vValue = Fmi4jVariableUtils.read(v, slave).getValue();

            System.out.println(hValue+";"+vValue);
            t += stepSize;
        }
        slave.terminate(); //or close, try with resources is also supported
        fmu.close();


    }

}
