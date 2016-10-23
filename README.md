Giant Killer Robot controller
===========================

A Controller program in Java and Scala to control the hardware and software of a
semi-autonomous robot which will perform goals and react to events.

Requires Libraries:
===================
messagebus
motozero

To Build this application
=========================
Ensure that the above libraries are up-to-date and included in the lib directory of this project.
Use SBT and the command:

sbt clean assembly

This will produce an executable jar in the form *robot-assembly-x.x.x.jar*

