/* Initial beliefs and rules */

/* Initial goals */
!start.


/* Plans */


+!start : true
<-  ?xx_wot_client(lamp, ID1);
    ?xx_wot_client(thing, ID2);
    ?xx_wot_client(john, ID3);
    ?xx_wot_client(bill, ID4);
    ?xx_wot_client(bob, ID5).


{ include("./templates/wot.asl")}

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
