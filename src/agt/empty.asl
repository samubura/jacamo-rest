/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

@hello
+!start : true <- .print("Hello!").

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
