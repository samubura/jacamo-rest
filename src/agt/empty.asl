/* Initial beliefs and rules */

/* Initial goals */
//x_thing_login(lamp, "basic", "header", "x-credentials", "1234").
!start.


/* Plans */


+!start : true
<-  ?xx_get_client(lamp, ID)
    readProperty("http://localhost:3000/affordances/smart-room/lamp-1/state", R) [artifact_id(ID)]
    println(R);
    .wait(1000)
    !start.


+?xx_get_client(Thing, ID) : x_thing_login(Thing, Scheme, Location, KeyName, Value)
<-  println("Thing with login")
    .my_name(N)
    .concat(N, "_", Thing, "_client", A)

    println(Location) //here is fine

    makeArtifact(A,"wot.WotHttpClientArtifact",[N],ID);

    //println(Location) //here is not

    //authorizeWithKey(Location, KeyName, Value) [artifact_id(ID)]
    +xx_get_client(Thing, ID).


+?xx_get_client(Thing, ID) : true
<-  .my_name(N)
    .concat(N, "_", Thing, "_client", A)
    makeArtifact(A,"wot.WotHttpClientArtifact",[N],ID);
    +xx_get_client(Thing, ID).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
