/* Initial beliefs and rules */

/* Initial goals */

!start(4).

/* Plans */

@hello
+!start(T) : true
<-  json.parse("{'key': 1}",J);
    json.set(J, "key", "x");
    json.print(J).


{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
