/* Initial beliefs and rules */

/* Initial goals */
!start.


/* Plans */


+!start : true
<-  ?xx_get_client(lamp, ID1);
    ?xx_get_client(thing, ID2);
    ?xx_get_client(john, ID3);
    ?xx_get_client(bill, ID4);
    ?xx_get_client(bob, ID5).

+jag_shutting_down(X) : xx_get_client(T, ID)
<-  .concat("cleaning artifact ", T, M);
    .println(M);
    disposeArtifact(ID);
    -xx_get_client(T,ID);
    -+jag_shutting_down(X).


+jag_shutting_down(_) : true
<-  .println("KILLED").

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
