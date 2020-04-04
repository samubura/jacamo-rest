package jacamo.rest;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.w3c.dom.Document;

import com.google.gson.Gson;

import jason.ReceiverNotFoundException;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.infra.centralised.CentralisedAgArch;

/**
 * Agent's REST compile class
 * 
 * @author Jomi Fred Hubner
 * @author Cleber Jorge Amaral
 *
 */
@Singleton
@Path("/agents")
public class RestImplAg extends AbstractBinder {

    TranslAg tAg = new TranslAg();
    Gson gson = new Gson();

    @Override
    protected void configure() {
        bind(new RestImplAg()).to(RestImplAg.class);
    }
    
    /**
     * Produces JSON containing the list of existing agents Example: ["ag1","ag2"]
     * 
     * @return HTTP 200 Response (ok status)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAgentsJSON() {
        return Response.ok().entity(gson.toJson(tAg.getAgents())).header("Access-Control-Allow-Origin", "*").build();
    }

    /**
     * Create an Agent. Produces PLAIN TEXT with HTTP response for this operation If
     * an ASL file with the given name exists, it will launch an agent with existing
     * code. Otherwise, creates an agent that will start say 'Hi'.
     * 
     * @param agName name of the agent to be created
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response createNewAgent(@PathParam("agentname") String agName) {
        try {
            String givenName = tAg.createAgent(agName);

            return Response.created(new URI("/agents/" + givenName)).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Kill an agent. Produces PLAIN TEXT with response for this operation.
     * 
     * @param agName agent's name to be killed
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     * @throws ReceiverNotFoundException
     */
    @Path("/{agentname}")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response killAgent(@PathParam("agentname") String agName) throws ReceiverNotFoundException {
        try {
            boolean r = BaseCentralisedMAS.getRunner().getRuntimeServices().killAgent(agName, "web", 0);

            return Response.ok("Result of kill: " + r).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Produces Agent's intentions statuses in JSON format. Example:
     * {"idle":true,"nbIntentions":1,"intentions":[{"size":1,"finished":false,"id":161,"suspended":false}]}
     * 
     * @param agName agent's name
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/status")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAgentStatusJSON(@PathParam("agentname") String agName) {
        try {
            Map<String, Object> props = tAg.getAgent(agName).getTS().getUserAgArch().getStatus();
            return Response.ok(gson.toJson(props)).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Get agent information (namespaces, roles, missions and workspaces) in JSON
     * format
     * 
     * @param agName name of the agent
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     * 
     */
    @Path("/{agentname}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAgentDetailsJSON(@PathParam("agentname") String agName) {
        try {
            return Response.ok(gson.toJson(tAg.getAgentDetails(agName))).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Return XML of agent's mind content including belief base, intentions and
     * plans. See Jason's agInspection.xsl file for processing this data.
     * 
     * @param agName name of the agent
     * @return A XML Document
     * @deprecated Agent's mind in JSON format is provided in /{agentname}
     */
    @Path("/{agentname}/mind")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Document getAgentMindXml(@PathParam("agentname") String agName) {
        try {
            Agent ag = tAg.getAgent(agName);
            if (ag != null)
                return ag.getAgState();
            else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return agent's Belief base in JSON format.
     * 
     * @param agName
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/mind/bb")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAgentBBJSON(@PathParam("agentname") String agName) {
        try {
            Agent ag = tAg.getAgent(agName);
            List<String> bbs = new ArrayList<>();
            for (Literal l : ag.getBB()) {
                bbs.add(l.toString());
            }

            return Response.ok(gson.toJson(bbs)).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }

    //TODO: This method will turn deprecated after GET/{agent} returning plans
    /**
     * Return agent's plans in TEXT PLAIN format
     * 
     * @param agName
     * @param label
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/plans")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getAgentPlansTxt(@PathParam("agentname") String agName,
            @DefaultValue("all") @QueryParam("label") String label) {
        StringWriter so = new StringWriter();
        try {
            Agent ag = tAg.getAgent(agName);
            if (ag != null) {
                PlanLibrary pl = ag.getPL();
                if (label.equals("all"))
                    so.append(pl.getAsTxt(false));
                else
                    so.append(pl.get(label).toASString());
            }

            return Response.ok(so.toString()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response
                .status(500, "Internal Server Error! Agent '" + agName + "' does not exist or cannot be observed.")
                .build();
    }

    /**
     * Upload new plans to an agent. Plan maintained only in memory.
     * 
     * @param agName              name of the agent
     * @param plans               plans to be uploaded
     * @param uploadedInputStream <need revision>
     * @param fileDetail          <need revision>
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/plans")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response loadPlans(@PathParam("agentname") String agName,
            @DefaultValue("") @FormDataParam("plans") String plans,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {
        try {
            Agent ag = tAg.getAgent(agName);
            if (ag != null) {
                ag.parseAS(new StringReader(plans), "RestAPI");

                System.out.println("agName: " + agName);
                System.out.println("plans: " + plans);
                System.out.println("restAPI://" + fileDetail.getFileName());
                System.out.println("uis: " + uploadedInputStream);

                ag.load(uploadedInputStream, "restAPI://" + fileDetail.getFileName());
            }

            return Response.ok("ok, code uploaded for agent '" + agName + "'!").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Upload new plans into an agent.
     * 
     * @param agName              name of the agent
     * @param plans               plans to be uploaded, as an String
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/plans")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response loadPlans(@PathParam("agentname") String agName, String plans) {
        try {
            Agent ag = tAg.getAgent(agName);
            if (ag == null) {
                return Response.status(500, "Receiver '" + agName + "' not found").build();
            }
            ag.parseAS(new StringReader(plans), "RestAPI");
            return Response.ok("ok, code uploaded for agent '" + agName + "'!").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Return a TEXT PLAIN of available internal action, external actions and
     * commands for the given agent Example:
     * "['.desire','.drop_desire','.drop_all_desires']"
     * 
     * @param agName Name of the agent
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/code")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCodeCompletionSuggestions(@PathParam("agentname") String agName) {
        Map<String,String> commands = new HashMap<>();
        try {
            // get internal actions
            tAg.getPlansSuggestions(agName, commands);
            // get internal actions
            tAg.getIASuggestions(commands);
            // get external actions
            tAg.getEASuggestions(agName, commands);

            Gson json = new Gson();
            Map<String,String> sortedCmds = new TreeMap<>(commands);
            return Response.ok(json.toJson(sortedCmds)).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500, "Server Internal Error! Could not get code completion suggestions.").build();
    }

    /**
     * Send a command to an agent. Produces a TEXT PLAIN output containing a status
     * message
     * 
     * @param cmd    command expression
     * @param agName agent name
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/cmd")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response runCmdPost(@FormParam("c") String cmd, @PathParam("agentname") String agName) {
        Agent ag = tAg.getAgent(agName);
        if (ag == null) {
            return Response.status(500, "Receiver '" + agName + "' not found").build();
        }
        try {
            tAg.createAgLog(agName, ag);
            
            cmd = cmd.trim();
            if (cmd.endsWith(".")) cmd = cmd.substring(0, cmd.length() - 1);

            Unifier u = tAg.execCmd(ag, ASSyntax.parsePlanBody(cmd));
            tAg.addAgLog(agName, "Command " + cmd + ": " + u);

            Map<String,String> um = new HashMap<String, String>();
            for (VarTerm v: u) {
                um.put(v.toString(), u.get(v).toString());
            }
            return Response.ok(gson.toJson(um)).build();
        } catch (ParseException e) {
            return Response.status(500, "Error parsing '" + cmd + "."+e.getMessage()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Get agent full log in a TEXT PLAIN format
     * 
     * @param agName agent name
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/log")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLogOutput(@PathParam("agentname") String agName) {
        try {
            StringBuilder o = tAg.agLog.get(agName);
            if (o != null) {
                return Response.ok(o.toString()).build();
            }
            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }

    /**
     * Delete agent's log.
     * 
     * @param agName
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     * 
     */
    @Path("/{agentname}/log")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response delLogOutput(@PathParam("agentname") String agName) {
        try {
            tAg.agLog.put(agName, new StringBuilder());

            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }

    /**
     * Send a message to an agent. Consumes an XML containing the message.
     * 
     * @param m      Message
     * @param agName Agent name
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/mb")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addAgMsg(Message m, @PathParam("agentname") String agName) {
        try {
            CentralisedAgArch a = BaseCentralisedMAS.getRunner().getAg(agName);
            if (a != null) {
                a.receiveMsg(m.getAsJasonMsg());
                return Response.ok().build();
            } else {
                return Response.status(500, "Internal Server Error! Receiver '" + agName + "' not found").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    @Path("/{agentname}/mb")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addAgMsgJson(Message m, @PathParam("agentname") String agName) {
        try {
            CentralisedAgArch a = BaseCentralisedMAS.getRunner().getAg(agName);
            if (a != null) {
                a.receiveMsg(m.getAsJasonMsg());
                return Response.ok().build();
            } else {
                return Response.status(500, "Internal Server Error! Receiver '" + agName + "' not found").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }
}
