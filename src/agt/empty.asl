/* Initial beliefs and rules */

/* Initial goals */
x_thing_login(lamp, "basic", "header", "x-credentials", "opensesame").
!start.


/* Plans */


+!start : true
<-  ?xx_get_client(lamp, ID)
    json.create_empty_object(I);
    json.set(I, "color", "#ff0000");
    json.print(I);
    invokeAction("http://localhost:3000/affordances/smart-room/lamp-1/toggle", O)[artifact_id(ID)];
    json.parse(O, J);
    json.print(J).

+?xx_get_client(Thing, ID) : x_thing_login(Thing, Scheme, Location, KeyName, Value)
<-  println("Thing with login");
    .my_name(N);
    .concat(N, "_", Thing, "_client", A);
    makeArtifact(A,"wot.WotHttpClientArtifact",[N],ID);
    authorizeWithKey(Location, KeyName, Value)[artifact_id(ID)];
    +xx_get_client(Thing, ID).

+?xx_get_client(Thing, ID) : true
<-  .my_name(N);
    .concat(N, "_", Thing, "_client", A);
    makeArtifact(A,"wot.WotHttpClientArtifact",[N],ID);
    +xx_get_client(Thing, ID).


{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
