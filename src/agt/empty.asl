/* Initial beliefs and rules */

/* Initial goals */

!start(4).

/* Plans */

@hello
+!start(T) : true
<-  makeArtifact("client","wot.WotHttpClientArtifact",[],C);
    readProperty("http://localhost:3000/affordances/smart-room/lamp-1/state", R)
    println(R)
    json.parse("{'ciao':1}", J)
    json.print(J).


{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
