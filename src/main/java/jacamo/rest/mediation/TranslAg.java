package jacamo.rest.mediation;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.WorkspaceId;
import jaca.CAgentArch;
import jacamo.rest.JCMRest;
import jacamo.rest.config.RestAgArch;
import jacamo.rest.util.Message;
import jason.JasonException;
import jason.ReceiverNotFoundException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.TokenMgrError;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.infra.centralised.CentralisedAgArch;
import jason.runtime.RuntimeServicesFactory;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.SchemeBoard;
import ora4mas.nopl.oe.Group;

public class TranslAg {

    Map<String, StringBuilder> agLog = new HashMap<>();
    Executor executor = Executors.newFixedThreadPool(4);

    /**
     * Get existing agents
     *
     * @return Set of agents;
     */
    public Map<String,Map<String,String>> getAgents() {
        // read all data from ZK
        try {
            return JCMRest.getWP();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; //RuntimeServicesFactory.get().getAgentsNames();
    }

    /**
     * Create agent with the given name if available (otherwise name_1...)
     * If type is null an empty agent that prints Hello! is instantiated
     *
     *
     * @param agName the requested name for the agent
     * @param type the name of the agent source file
     * @return the assigned name of the newly created agent
     * @throws Exception
     * @throws JasonException
     */
    public String createAgent(String agName, String type) throws Exception, JasonException {
        //TODO SAMU modified to accept the type
        if(type == null){
            type = "empty";
        }
        String givenName = RuntimeServicesFactory.get().createAgent(agName, type+".asl",  null, null, null, null, null);
        RuntimeServicesFactory.get().startAgent(givenName);

        Agent ag = getAgent(givenName);

        /* not needed since the template file should include them
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{ include(\"$jacamoJar/templates/common-cartago.asl\") }\n");
        stringBuilder.append("{ include(\"$jacamoJar/templates/common-moise.asl\") }\n");
        ag.load(new StringInputStream( stringBuilder.toString()), "source-from-rest-api");
         */

        //agentLog created
        createAgLog(givenName, ag);

        return givenName;
    }


    /**
     * Creates a new entry in the WP
     * @throws Exception
     */
    public boolean createWP(String agName, Map<String,String> metaData) throws Exception {
        return RestAgArch.registerWP(JCMRest.getZKClient(), agName, metaData, false);
    }

    /**
     * ask to agent to run a command
     *
     * @param cmd
     * @param agName
     * @return
     * @throws TokenMgrError
     * @throws Exception
     */
    public Map<String, String> executeCommand(String cmd, String agName) throws TokenMgrError, Exception {
        Agent ag = getAgent(agName);
        if (ag == null) {
            throw new Exception("Receiver '" + agName + "' not found");
        }
        createAgLog(agName, ag);

        cmd = cmd.trim();
        if (cmd.endsWith("."))
            cmd = cmd.substring(0, cmd.length() - 1);

        Unifier u = execCmd(ag, ASSyntax.parsePlanBody(cmd));
        addAgLog(agName, "Command " + cmd + ": " + u);

        Map<String, String> um = new HashMap<>();
        for (VarTerm v : u) {
            um.put(v.toString(), u.get(v).toString());
        }
        return um;
    }

    /**
     * Creates a log area for an agent
     *
     * @param agName agent name
     * @param ag     agent object
     */
    public void createAgLog(String agName, Agent ag) {
        // adds a log for the agent
        if (agLog.get(agName) == null) {
            agLog.put(agName, new StringBuilder());
            ag.getTS().getLogger().addHandler(new StreamHandler() {
                @Override
                public void publish(LogRecord l) {
                    addAgLog(agName, l.getMessage());
                }
            });
        }
    }

    /**
     * Add a message to the agent log.
     *
     * @param agName agent name
     * @param msg    message to be added
     */
    protected void addAgLog(String agName, String msg) {
        StringBuilder o = agLog.get(agName);
        if (o == null) {
            o = new StringBuilder();
            agLog.put(agName, o);
        } else {
            o.append("\n");
        }
        String dt = new SimpleDateFormat("dd-MM-yy HH:mm:ss").format(new Date());
        o.append("[" + dt + "] " + msg);
    }

    /**
     * kill an agent
     * @param agName
     * @return
     * @throws Exception
     */
    //TODO SAMU modified deadline
    public void deleteAgent(String agName) throws Exception {
        int deadline = 1000;
        if (BaseCentralisedMAS.getRunner().getAg(agName) != null) {
            if (!RuntimeServicesFactory.get().killAgent(agName, "web", deadline))
                throw new Exception("Unable to kill agent: " + agName);
        } else {
            //TODO added else since this was redundant and it is already in the stop method (?)
            //RestAgArch.deleteWP(JCMRest.getZKClient(), agName);
            throw new Exception("Unable to kill agent: " + agName);
        }
    }

    /**
     * Return status of the agent
     * @param agName
     * @return
     */
    public Map<String, Object> getAgentStatus(String agName) throws JasonException {
        Agent ag = getAgent(agName);
        if (ag == null) {
            throw new ReceiverNotFoundException("agent "+agName+" does not exist in the MAS");
        }

        return ag.getTS().getAgArch().getStatus();
    }

    /**
     * get Agent Belief Base
     *
     * @param agName
     * @return
     */
    public List<String> getAgentsBB(String agName) {
        Agent ag = getAgent(agName);
        List<String> bbs = new ArrayList<>();
        for (Literal l : ag.getBB()) {
            bbs.add(l.toString());
        }
        return bbs;
    }

    /**
     * List of plans
     *
     * @param agName name of the agent
     * @param label optional filter
     * @return list of string
     */
    public Map<String,String> getAgentPlans(String agName, String label) {
        Map<String,String> plans = new HashMap<>();
        Agent ag = getAgent(agName);
        if (ag != null) {
            PlanLibrary pl = ag.getPL();
            if (label.equals("all")) {
                Iterator<Plan> i = pl.getPlans().iterator();
                while (i.hasNext()) {
                    Plan p = i.next();
                    plans.put(p.getLabel().toString(), p.toASString());
                }
            } else {
                plans.put(label, pl.get(label).toASString());
            }
        }
        return plans;
    }

    /**
     * Add a plan to the agent's plan library
     *
     * @param agName
     * @param plans
     * @throws Exception
     */
    public void addAgentPlan(String agName, String plans) throws Exception {
        Agent ag = getAgent(agName);
        if (ag == null) {
            throw new Exception("Receiver '" + agName + "' not found");
        }
        ag.parseAS(new StringReader(plans), "RestAPI");
    }

    /**
     * Get agent information (namespaces, roles, missions and workspaces)
     *
     * @param agName name of the agent
     * @return A Map with agent information
     * @throws CartagoException
     *
     */
    public Map<String, Object> getAgentDetails(String agName) throws Exception {

        Agent ag = getAgent(agName);
        if (ag == null) {
            throw new ReceiverNotFoundException("agent "+agName+" does not exist in the MAS");
        }

        // get workspaces the agent are in (including organisations)
        List<String> workspacesIn = new ArrayList<>();
        CAgentArch cartagoAgArch = getCartagoArch(ag);

        for (WorkspaceId wid : cartagoAgArch.getSession().getJoinedWorkspaces()) {
            workspacesIn.add(wid.getName());
        }
        List<String> nameSpaces = new ArrayList<>();
        ag.getBB().getNameSpaces().forEach(x -> {
            nameSpaces.add(x.toString());
        });

        // get groups and roles this agent plays
        List<Object> roles = new ArrayList<>();
        for (GroupBoard gb : GroupBoard.getGroupBoards()) {
            if (workspacesIn.contains(gb.getOEId())) {
                gb.getGrpState().getPlayers().forEach(p -> {
                    if (p.getAg().equals(agName)) {
                        Map<String, Object> groupRole = new HashMap<>();
                        groupRole.put("group", gb.getArtId());
                        groupRole.put("role", p.getTarget());
                        roles.add(groupRole);
                    }
                });

            }
        }

        // get schemed this agent belongs
        List<Object> missions = new ArrayList<>();
        for (SchemeBoard schb : SchemeBoard.getSchemeBoards()) {
            schb.getSchState().getPlayers().forEach(p -> {
                if (p.getAg().equals(agName)) {
                    Map<String, Object> schemeMission = new HashMap<>();
                    schemeMission.put("scheme", schb.getArtId());
                    schemeMission.put("mission", p.getTarget());
                    List<Object> responsibles = new ArrayList<>();
                    schemeMission.put("responsibles", responsibles);
                    for (Group gb : schb.getSchState().getGroupsResponsibleFor()) {
                        responsibles.add(gb.getId());
                    }
                    missions.add(schemeMission);
                }
            });
        }

        List<Object> workspaces = new ArrayList<>();
        workspacesIn.forEach(wksName -> {
            Map<String, Object> workspace = new HashMap<>();
            workspace.put("workspace", wksName);
            List<Object> artifacts = new ArrayList<>();
            try {
                for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {
                    ArtifactInfo info = CartagoService.getController(wksName).getArtifactInfo(aid.getName());
                    info.getObservers().forEach(y -> {
                        if (y.getAgentId().getAgentName().equals(agName)) {
                            // Build returning object
                            Map<String, Object> artifact = new HashMap<String, Object>();
                            artifact.put("artifact", info.getId().getName());
                            artifact.put("type", info.getId().getArtifactType());
                            artifacts.add(artifact);
                        }
                    });
                }
                workspace.put("artifacts", artifacts);
                workspaces.add(workspace);
            } catch (CartagoException e) {
                e.printStackTrace();
            }
        });

        List<String> beliefs = getAgentsBB(agName);

        Map<String, Object> agent = new HashMap<>();
        agent.put("agent", agName);
        agent.put("namespaces", nameSpaces);
        agent.put("roles", roles);
        agent.put("missions", missions);
        agent.put("workspaces", workspaces);
        agent.put("beliefs", beliefs);

        return agent;
    }

    /**
     * Send a command to an agent
     *
     * @param ag name of the agent
     * @param lCmd   command to be executed
     * @return Status message
     * @throws ParseException
     */
    @SuppressWarnings("serial")
    Unifier execCmd(Agent ag, PlanBody lCmd) throws ParseException {
        Trigger te = ASSyntax.parseTrigger("+!run_repl_expr");
        Intention i = new Intention();
        IntendedMeans im = new IntendedMeans(new Option(new Plan(null, te, null, lCmd), new Unifier()), te);
        i.push(im);

        Lock lock = new ReentrantLock();
        Condition goalFinished  = lock.newCondition();
        executor.execute( () -> {
                /*GoalListener gl = new GoalListener() {
                    public void goalSuspended(Trigger goal, String reason) {}
                    public void goalStarted(Event goal) {}
                    public void goalResumed(Trigger goal) {}
                    public void goalFinished(Trigger goal, FinishStates result) {
                        System.out.println("finished!");
                        if (goal.equals(te)) {
                            // finished
                            //if (result.equals(FinishStates.achieved)) {
                            //}
                            try {
                                lock.lock();
                                goalFinished.signalAll();
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                    public void goalFailed(Trigger goal) {
                        if (goal.equals(te)) {
                            try {
                                lock.lock();
                                goalFinished.signalAll();
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                };*/
                CircumstanceListener cl = new CircumstanceListener() {
                    public void intentionDropped(Intention ci) {
                        //System.out.println("*finished!"+ci);
                        if (ci.equals(i)) {
                            try {
                                lock.lock();
                                goalFinished.signalAll();
                            } finally {
                                lock.unlock();
                            }
                        }
                    };

                };
                TransitionSystem ts = ag.getTS();
                try {
                    lock.lock();
                    //ts.addGoalListener(gl);
                    ts.getC().addEventListener(cl);
                    ts.getC().addRunningIntention(i);
                    ts.getAgArch().wake();
                    goalFinished.await();
                    //ts.removeGoalListener(gl);
                    ts.getC().removeEventListener(cl);
                } catch (InterruptedException e) {
                } finally {
                    lock.unlock();
                }
        });
        try {
            lock.lock();
            goalFinished.await();

            return im.getUnif();
        } catch (InterruptedException e) {
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * get agent log
     *
     * @param agName
     * @throws Exception
     */
    public String getAgentLog(String agName) {
        if (agLog.containsKey(agName)) {
            return agLog.get(agName).toString();
        } else {
            return "Log is empty/absent.";
        }
    }

    /**
     * Return agent object by agent's name
     *
     * @param agName name of the agent
     * @return Agent object
     */
    public Agent getAgent(String agName) {
        CentralisedAgArch cag = BaseCentralisedMAS.getRunner().getAg(agName);
        if (cag != null)
            return cag.getTS().getAg();
        else
            return null;
    }

    /**
     * Get agent's CArtAgO architecture
     *
     * @param ag Agent object
     * @return agent's CArtAgO architecture
     */
    public CAgentArch getCartagoArch(Agent ag) {
        AgArch arch = ag.getTS().getAgArch().getFirstAgArch();
        while (arch != null) {
            if (arch instanceof CAgentArch) {
                return (CAgentArch) arch;
            }
            arch = arch.getNextAgArch();
        }
        return null;
    }

    /**
     * add a message to the agent's mailbox
     *
     * @param m
     * @param agName
     * @throws Exception
     */
    public void addMessageToAgentMailbox(Message m, String agName) throws Exception {
        CentralisedAgArch a = BaseCentralisedMAS.getRunner().getAg(agName);
        if (a != null) {
            a.receiveMsg(m.getAsJasonMsg());
        } else {
            throw new Exception("Internal Server Error! Receiver '" + agName + "' not found");
        }
    }

    /**
     * Returns agents by services
     *
     * @return
     * @throws Exception
     */
    public Map<String, Set<String>> getCommonDF() throws Exception {
        return RuntimeServicesFactory.get().getDF();
    }

    /**
     * Return content of getCommonDF but ready to send to the client (in Json format)
     *
     * @return
     * @throws Exception
     */
    public Map<String, Object> getJsonifiedDF() throws Exception {
        Map<String, Set<String>> commonDF = getCommonDF();

        // Json of the DF
        Map<String,Object> jsonifiedDF = new HashMap<>();
        for (String s : commonDF.keySet()) {
            Map<String, Object> agent = new HashMap<>();
            agent.put("agent", s);
            Set<String> services = new HashSet<>();
            services.addAll(commonDF.get(s));
            agent.put("services", services);
            jsonifiedDF.put(s,agent);
        }
        return jsonifiedDF;
    }

    /**
     * Add a service to a given agent
     *
     * @param agName
     * @param values
     * @throws Exception
     */
    public void addServiceToAgent(String agName, String service, Map<String, Object> values) throws Exception {
        String type = "no-type";
        // if a map was provided, use it instead of original given service
        if (values != null) {
            service = values.getOrDefault("service", service).toString();
            type    = values.getOrDefault("type", "no-type").toString();
        }
        RuntimeServicesFactory.get().dfRegister(agName, service, type);
    }

    /**
     * Remove a service from a given agent
     *
     * @param agName
     * @param service
     * @throws Exception
     */
    public void removeServiceToAgent(String agName, String service) throws Exception {
        RuntimeServicesFactory.get().dfDeRegister(agName, service, "no-type");
    }

}
