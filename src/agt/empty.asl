/* Initial beliefs and rules */

/* Initial goals */

!start(4).

/* Plans */

@hello
+!start(T) : true
<-  makeArtifact("client","wot.WotHttpClientArtifact",[],C);
    json.parse("{'color':'#ff0000'}", I);
    invokeAction("http://localhost:3000/affordances/smart-room/lamp-1/color", I, O) [artifact_id(C)];
    json.print(I).


{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
