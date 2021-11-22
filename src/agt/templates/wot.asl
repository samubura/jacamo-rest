+?xx_wot_client(Thing, ID) : x_thing_login(Thing, Scheme, Location, KeyName, Value)
<-  println("Thing with login");
    .my_name(N);
    .concat(N, "_", Thing, "_client", A);
    makeArtifact(A,"wot.WotHttpClientArtifact",[N],ID);
    authorizeWithKey(Location, KeyName, Value)[artifact_id(ID)];
    +xx_wot_client(Thing, ID).

+?xx_wot_client(Thing, ID) : true
<-  .my_name(N);
    .concat(N, "_", Thing, "_client", A);
    makeArtifact(A,"wot.WotHttpClientArtifact",[N],ID);
    +xx_wot_client(Thing, ID).

+jag_shutting_down(X) : xx_wot_client(T, ID)
<-  .concat("cleaning artifact ", T, M);
    .println(M);
    dispose(ID);
    -xx_wot_client(T,ID);
    -+jag_shutting_down(X).

+jag_shutting_down(_) : true
<-  .println("KILLED").