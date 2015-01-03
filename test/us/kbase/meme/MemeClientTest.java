package us.kbase.meme;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import us.kbase.auth.AuthException;
import us.kbase.auth.AuthService;
import us.kbase.auth.AuthToken;
import us.kbase.common.service.JsonClientException;
import us.kbase.common.service.Tuple7;
import us.kbase.common.service.UnauthorizedException;
import us.kbase.kbasesequences.SequenceSet;
import us.kbase.userandjobstate.Results;
import us.kbase.userandjobstate.UserAndJobStateClient;
import us.kbase.workspace.ObjectIdentity;
import us.kbase.workspace.WorkspaceClient;

public class MemeClientTest {
	
	private SequenceSet testSequenceSet = new SequenceSet();
	private MemeRunResult memeRunResult = new MemeRunResult();
	private String serverUrl = "http://140.221.67.196:7108/";//this is a new enigma instance, old was "http://140.221.85.173:7077";
//	private String serverUrl = "http://kbase.us/services/meme/";
//	private String serverUrl = "http://127.0.0.1:7108";

	private static final String USER_NAME = "";
	private static final String PASSWORD = "";
	private static final String TEST_WORKSPACE = "AKtest";
	
	private final String JOB_SERVICE = "https://kbase.us/services/userandjobstate";
	private final String WS_SERVICE = "https://kbase.us/services/ws";
	private String testSequenceSetId = "mod_desulfovibrio";//"Halobacterium_sp_NRC-1_Idr2_regulon";//
	private String testCollectionId1 = "kb|memepspmcollection.57";//"kb|memepspmcollection.64";//	
	private String testCollectionId2 = "kb|memepspmcollection.57";//"kb_pspmcollection_regprecise";//
	private String testMemeRunResultId = "kb|memerunresult.187";
	private String testMemePspmId = "kb|memepspm.115";
	private static AuthToken token = null;

		
	@Before
	public void setUp() throws Exception {
		try {
			token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		} catch (AuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(testSequenceSetId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		testSequenceSet = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(SequenceSet.class);

	}

	@After
	public void tearDown() throws Exception {
		testSequenceSet = null;
		memeRunResult = null;
	}

	@Test
	public final void testKbasememeClient() throws Exception {
		URL serviceUrl = new URL(serverUrl);
		MEMEClient client = new MEMEClient(serviceUrl, USER_NAME, PASSWORD);
		client.setAuthAllowedForHttp(true);
		assertNotNull(client);
	}

	@Test
	public final void testFindMotifsWithMeme() throws Exception {
		URL serviceUrl = new URL(serverUrl);
		MEMEClient client = new MEMEClient(serviceUrl, USER_NAME, PASSWORD);
		client.setAuthAllowedForHttp(true);
		
		//set params
		MemeRunParameters params = new MemeRunParameters();
		params.setMod("oops");
		params.setNmotifs(2L);
		params.setMinw(14L);
		params.setMaxw(24L);
		params.setNsites(0L);
		params.setMinsites(0L);
		params.setMaxsites(0L);
		params.setPal(1L);
		params.setRevcomp(0L);
		params.setSourceId(testSequenceSetId);
		params.setSourceRef(TEST_WORKSPACE + "/" + testSequenceSetId);

		
		MemeRunResult result = client.findMotifsWithMeme(testSequenceSet, params);

		assertEquals(Long.valueOf("0"),result.getSeed());
		assertEquals(Long.valueOf("1"),result.getSeqfrac());
		assertEquals("+",result.getStrands());
		assertEquals(Long.valueOf("1500"),result.getN());
		assertEquals(Long.valueOf("6"),result.getNCap());
		assertEquals(Double.valueOf("0.00001"),result.getDistance());
		assertEquals("dirichlet",result.getPrior());
		assertEquals(Double.valueOf("0.01"),result.getB());
		assertEquals(Long.valueOf("50"),result.getMaxiter());
		assertEquals("yes",result.getSubstring());
		assertEquals("no",result.getBranching());
		assertEquals("no",result.getWbranch());
		assertEquals(Long.valueOf("1"),result.getProb());
		assertEquals("uni",result.getSpmap());
		assertEquals("0.5",result.getSpfuzz());
		assertEquals(Long.valueOf("6"),result.getMinsites());
		assertEquals(Long.valueOf("6"),result.getMaxsites());
		assertEquals(Double.valueOf("0.8"),result.getWnsites());
		assertEquals(Long.valueOf("11"),result.getWg());
		assertEquals(Long.valueOf("1"),result.getWs());
		assertEquals("yes",result.getEndgaps());
		assertEquals(Long.valueOf("14"),result.getMinw());
		assertEquals(Long.valueOf("24"),result.getMaxw());
		assertEquals(Double.valueOf("0.00"),result.getMinic());
		assertEquals("E-value of product of p-values",result.getObjectFunction());
		assertEquals("oops",result.getMod());
		assertEquals(Long.valueOf("2"),result.getNmotifs());
		assertEquals("inf",result.getEvt());
		assertNotNull(result.getMotifs().get(0).getId());
		assertEquals(Long.valueOf("24"),result.getMotifs().get(0).getWidth());
		assertEquals(6,result.getMotifs().get(0).getSites().size());
		assertEquals(Double.valueOf("90"),result.getMotifs().get(0).getLlr());
		assertEquals(Double.valueOf("2300"),result.getMotifs().get(0).getEvalue());
		assertEquals("kb|sequence.115",result.getMotifs().get(0).getSites().get(0).getSourceSequenceId());
		assertEquals(Long.valueOf("134"),result.getMotifs().get(0).getSites().get(0).getStart());
		assertEquals(Double.valueOf("0.000000000152"),result.getMotifs().get(0).getSites().get(0).getPvalue());
		assertEquals("ACTGGTTTTG",result.getMotifs().get(0).getSites().get(0).getLeftFlank());
		assertEquals("TCACGATTTTCAGGACATTCGTGA",result.getMotifs().get(0).getSites().get(0).getSequence());
		assertEquals("CCGCGTTGGC",result.getMotifs().get(0).getSites().get(0).getRightFlank());
	}

	@Test
	public final void testFindMotifsWithMemeFromWs() throws MalformedURLException, Exception {
		
		//AuthToken token = JsonClientCaller.requestTokenFromKBase(USER_NAME, PASSWORD.toCharArray());
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
//		System.out.println(token.toString());
		URL serviceUrl = new URL(serverUrl);
//		MEMEClient client = new MEMEClient(serviceUrl, USER_NAME, PASSWORD);
		MEMEClient client = new MEMEClient(serviceUrl, token);
		client.setAuthAllowedForHttp(true);
		
/*		System.out.println(input.getSequenceSetId());
		System.out.println(input.getSequences().get(0).getSequenceId());
		System.out.println(input.getSequences().get(0).getSequence());
*/		
		//set params
		MemeRunParameters params = new MemeRunParameters();
		params.setMod("oops");
		params.setNmotifs(2L);
		params.setMinw(14L);
		params.setMaxw(24L);
		params.setNsites(0L);
		params.setMinsites(0L);
		params.setMaxsites(0L);
		params.setPal(1L);
		params.setRevcomp(0L);
		params.setSourceId(testSequenceSetId);
		params.setSourceRef(TEST_WORKSPACE + "/" + testSequenceSetId);

		
		String resultId = client.findMotifsWithMemeFromWs(TEST_WORKSPACE, params);
		System.out.println(resultId);
//Read result from WS
		
//		List<ObjectIdentity> objectIds = new ArrayList<ObjectIdentity>();
//		ObjectIdentity objectIdentity = new ObjectIdentity().withWorkspace("AKtest").withName(resultId);
//		objectIds.add(objectIdentity);
//		List<ObjectData> output = MemeServerImpl.wsClient(token.toString()).getObjects(objectIds);
		
//		MemeRunResult result = UObject.transformObjectToObject(output.get(0).getData(), MemeRunResult.class);

		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(resultId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MemeRunResult result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MemeRunResult.class);

		assertEquals(Long.valueOf("0"),result.getSeed());
		assertEquals(Long.valueOf("1"),result.getSeqfrac());
		assertEquals("+",result.getStrands());
		assertEquals(Long.valueOf("1500"),result.getN());
		assertEquals(Long.valueOf("6"),result.getNCap());
		assertEquals(Double.valueOf("0.00001"),result.getDistance());
		assertEquals("dirichlet",result.getPrior());
		assertEquals(Double.valueOf("0.01"),result.getB());
		assertEquals(Long.valueOf("50"),result.getMaxiter());
		assertEquals("yes",result.getSubstring());
		assertEquals("no",result.getBranching());
		assertEquals("no",result.getWbranch());
		assertEquals(Long.valueOf("1"),result.getProb());
		assertEquals("uni",result.getSpmap());
		assertEquals("0.5",result.getSpfuzz());
		assertEquals(Long.valueOf("6"),result.getMinsites());
		assertEquals(Long.valueOf("6"),result.getMaxsites());
		assertEquals(Double.valueOf("0.8"),result.getWnsites());
		assertEquals(Long.valueOf("11"),result.getWg());
		assertEquals(Long.valueOf("1"),result.getWs());
		assertEquals("yes",result.getEndgaps());
		assertEquals(Long.valueOf("14"),result.getMinw());
		assertEquals(Long.valueOf("24"),result.getMaxw());
		assertEquals(Double.valueOf("0.00"),result.getMinic());
		assertEquals("E-value of product of p-values",result.getObjectFunction());
		assertEquals("oops",result.getMod());
		assertEquals(Long.valueOf("2"),result.getNmotifs());
		assertEquals("inf",result.getEvt());
		assertEquals(Long.valueOf("24"),result.getMotifs().get(0).getWidth());
		assertEquals(6,result.getMotifs().get(0).getSites().size());
		assertEquals(Double.valueOf("90"),result.getMotifs().get(0).getLlr());
		assertEquals(Double.valueOf("2300"),result.getMotifs().get(0).getEvalue());
		assertEquals("kb|sequence.115",result.getMotifs().get(0).getSites().get(0).getSourceSequenceId());
		assertEquals(Long.valueOf("134"),result.getMotifs().get(0).getSites().get(0).getStart());
		assertEquals(Double.valueOf("0.000000000152"),result.getMotifs().get(0).getSites().get(0).getPvalue());
		assertEquals("ACTGGTTTTG",result.getMotifs().get(0).getSites().get(0).getLeftFlank());
		assertEquals("TCACGATTTTCAGGACATTCGTGA",result.getMotifs().get(0).getSites().get(0).getSequence());
		assertEquals("CCGCGTTGGC",result.getMotifs().get(0).getSites().get(0).getRightFlank());
	}

	@Test
	public final void testFindMotifsWithMemeJobFromWs() throws MalformedURLException, Exception {
		
		String resultId = null;
		
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		URL serviceUrl = new URL(serverUrl);
		MEMEClient memeClient = new MEMEClient(serviceUrl, token);
		memeClient.setAuthAllowedForHttp(true);

		//set params
		MemeRunParameters params = new MemeRunParameters();
		params.setMod("oops");
		params.setNmotifs(2L);
		params.setMinw(14L);
		params.setMaxw(24L);
		params.setNsites(0L);
		params.setMinsites(0L);
		params.setMaxsites(0L);
		params.setPal(1L);
		params.setRevcomp(0L);
		params.setSourceRef(TEST_WORKSPACE + "/" + testSequenceSetId);

		
		String jobId = memeClient.findMotifsWithMemeJobFromWs(TEST_WORKSPACE, params);
		System.out.println("Job ID = " + jobId);
		assertNotNull(jobId);
		
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;

		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client = new UserAndJobStateClient(jobServiceUrl, token);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);
		
		String status = "";
		
		while (!status.equalsIgnoreCase("finished")){
			
			try {
			    Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			try {
				Tuple7<String,String,String,Long,String,Long,Long> t = client.getJobStatus(jobId); 
				//System.out.println(t.getE1());
				//System.out.println(t.getE2());
				status = t.getE3();
				//System.out.println(t.getE3());//Status
				//System.out.println(t.getE4());
				//System.out.println(t.getE5());
				//System.out.println(t.getE6());
				//System.out.println(t.getE7());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			Results res = client.getResults(jobId);			
			resultId = res.getWorkspaceids().get(0);
			System.out.println("Result ID = " + resultId);
			assertNotNull(resultId);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] resultIdParts = resultId.split("/");
		resultId = resultIdParts[1];
//Read result from WS
		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(resultId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MemeRunResult result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MemeRunResult.class);

		assertEquals(Long.valueOf("0"),result.getSeed());
		assertEquals(Long.valueOf("1"),result.getSeqfrac());
		assertEquals("+",result.getStrands());
		assertEquals(Long.valueOf("1500"),result.getN());
		assertEquals(Long.valueOf("6"),result.getNCap());
		assertEquals(Double.valueOf("0.00001"),result.getDistance());
		assertEquals("dirichlet",result.getPrior());
		assertEquals(Double.valueOf("0.01"),result.getB());
		assertEquals(Long.valueOf("50"),result.getMaxiter());
		assertEquals("yes",result.getSubstring());
		assertEquals("no",result.getBranching());
		assertEquals("no",result.getWbranch());
		assertEquals(Long.valueOf("1"),result.getProb());
		assertEquals("uni",result.getSpmap());
		assertEquals("0.5",result.getSpfuzz());
		assertEquals(Long.valueOf("6"),result.getMinsites());
		assertEquals(Long.valueOf("6"),result.getMaxsites());
		assertEquals(Double.valueOf("0.8"),result.getWnsites());
		assertEquals(Long.valueOf("11"),result.getWg());
		assertEquals(Long.valueOf("1"),result.getWs());
		assertEquals("yes",result.getEndgaps());
		assertEquals(Long.valueOf("14"),result.getMinw());
		assertEquals(Long.valueOf("24"),result.getMaxw());
		assertEquals(Double.valueOf("0.00"),result.getMinic());
		assertEquals("E-value of product of p-values",result.getObjectFunction());
		assertEquals("oops",result.getMod());
		assertEquals(Long.valueOf("2"),result.getNmotifs());
		assertEquals("inf",result.getEvt());
		assertEquals(Long.valueOf("24"),result.getMotifs().get(0).getWidth());
		assertEquals(6,result.getMotifs().get(0).getSites().size());
		assertEquals(Double.valueOf("90"),result.getMotifs().get(0).getLlr());
		assertEquals(Double.valueOf("2300"),result.getMotifs().get(0).getEvalue());
		assertEquals("kb|sequence.115",result.getMotifs().get(0).getSites().get(0).getSourceSequenceId());
		assertEquals(Long.valueOf("134"),result.getMotifs().get(0).getSites().get(0).getStart());
		assertEquals(Double.valueOf("0.000000000152"),result.getMotifs().get(0).getSites().get(0).getPvalue());
		assertEquals("ACTGGTTTTG",result.getMotifs().get(0).getSites().get(0).getLeftFlank());
		assertEquals("TCACGATTTTCAGGACATTCGTGA",result.getMotifs().get(0).getSites().get(0).getSequence());
		assertEquals("CCGCGTTGGC",result.getMotifs().get(0).getSites().get(0).getRightFlank());

	}
	
	
	@Test
	public final void testCompareMotifsWithTomtomByCollection() throws Exception {
		URL serviceUrl = new URL(serverUrl);
		MEMEClient client = new MEMEClient(serviceUrl, USER_NAME, PASSWORD);
		client.setAuthAllowedForHttp(true);
		
		//set params
		MemeRunParameters params = new MemeRunParameters();
		params.setMod("oops");
		params.setNmotifs(2L);
		params.setMinw(14L);
		params.setMaxw(24L);
		params.setNsites(0L);
		params.setMinsites(0L);
		params.setMaxsites(0L);
		params.setPal(1L);
		params.setRevcomp(0L);
		
		memeRunResult = client.findMotifsWithMeme(testSequenceSet, params);
		MemePSPMCollection memePspmCollection = client.getPspmCollectionFromMemeResult(memeRunResult);
//		String distTomtom = new String("pearson".getBytes("UTF-8"), "ISO-8859-1");
		
		TomtomRunParameters paramsTomtom = new TomtomRunParameters();
		paramsTomtom.setDist("pearson");
		paramsTomtom.setThresh(0.0000001);
		paramsTomtom.setEvalue(0L);
		paramsTomtom.setInternal(1L);
		paramsTomtom.setMinOverlap(0L);

		
		TomtomRunResult result = client.compareMotifsWithTomtomByCollection(memePspmCollection, memePspmCollection, paramsTomtom);
		assertNotNull(result.getHits().get(0).getTargetPspmId());
		assertEquals(result.getHits().get(0).getTargetPspmId(),result.getHits().get(0).getQueryPspmId());
		assertEquals(Long.valueOf("0"),result.getHits().get(0).getOptimalOffset());
		assertEquals(Double.valueOf("3.89353E-37"),result.getHits().get(0).getPvalue());
		assertEquals(Double.valueOf("7.78706e-37"),result.getHits().get(0).getEvalue());
		assertEquals(Double.valueOf("7.78706e-37"),result.getHits().get(0).getQvalue());
		assertEquals(Long.valueOf("24"),result.getHits().get(0).getOverlap());
		assertEquals("TCACGCTCGTCATGACGAGCGTGA",result.getHits().get(0).getQueryConsensus());
		assertEquals("TCACGCTCGTCATGACGAGCGTGA",result.getHits().get(0).getTargetConsensus());
		assertEquals("+",result.getHits().get(0).getStrand());
	}

	@Test
	public final void testCompareMotifsWithTomtomByCollectionFromWs() throws Exception {
		
		URL serviceUrl = new URL(serverUrl);
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		System.out.println(token.toString());

		MEMEClient client = new MEMEClient(serviceUrl, token);
		client.setAuthAllowedForHttp(true);

		
		TomtomRunParameters paramsTomtom = new TomtomRunParameters();
		paramsTomtom.setDist("pearson");
		paramsTomtom.setThresh(0.1);
		paramsTomtom.setEvalue(0L);
		paramsTomtom.setInternal(0L);
		paramsTomtom.setMinOverlap(6L);
		paramsTomtom.setQueryRef(TEST_WORKSPACE + "/" + testCollectionId1);
		paramsTomtom.setTargetRef(TEST_WORKSPACE + "/" + testCollectionId2);

				
		String resultId = client.compareMotifsWithTomtomByCollectionFromWs(TEST_WORKSPACE, paramsTomtom);
		System.out.println(resultId);
		
		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(resultId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		TomtomRunResult result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(TomtomRunResult.class);

		assertNotNull(result.getHits().get(0).getTargetPspmId());
		assertEquals(result.getHits().get(0).getTargetPspmId(),result.getHits().get(0).getQueryPspmId());
		assertEquals(Long.valueOf("0"),result.getHits().get(0).getOptimalOffset());
		assertEquals(Double.valueOf("3.89353e-37"),result.getHits().get(0).getPvalue());
		assertEquals(Double.valueOf("7.78706e-37"),result.getHits().get(0).getEvalue());
		assertEquals(Long.valueOf("24"),result.getHits().get(0).getOverlap());
		assertEquals("TCACGCTCGTCATGACGAGCGTGA",result.getHits().get(0).getQueryConsensus());
		assertEquals("TCACGCTCGTCATGACGAGCGTGA",result.getHits().get(0).getTargetConsensus());
		assertEquals("+",result.getHits().get(0).getStrand());
	}
	
	@Test
	public final void testCompareMotifsWithTomtomJobByCollectionFromWs() throws Exception {
		
		String resultId = null;

		URL serviceUrl = new URL(serverUrl);
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
//		System.out.println(token.toString());

		MEMEClient clientMeme = new MEMEClient(serviceUrl, token);
		clientMeme.setAuthAllowedForHttp(true);

		TomtomRunParameters paramsTomtom = new TomtomRunParameters();
		paramsTomtom.setDist("pearson");
		paramsTomtom.setThresh(0.1);
		paramsTomtom.setEvalue(0L);
		paramsTomtom.setInternal(0L);
		paramsTomtom.setMinOverlap(0L);
		paramsTomtom.setQueryRef(TEST_WORKSPACE + "/" + testCollectionId1);
		paramsTomtom.setTargetRef(TEST_WORKSPACE + "/" + testCollectionId2);
				
		String jobId = clientMeme.compareMotifsWithTomtomJobByCollectionFromWs(TEST_WORKSPACE, paramsTomtom);

		System.out.println("Job ID = " + jobId);
		assertNotNull(jobId);
		
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;

		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client = new UserAndJobStateClient(jobServiceUrl, token);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);
		
		String status = "";
		
		while (!status.equalsIgnoreCase("finished")){
			
			try {
			    Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			try {
				Tuple7<String,String,String,Long,String,Long,Long> t = client.getJobStatus(jobId); 
				//System.out.println(t.getE1());
				//System.out.println(t.getE2());
				status = t.getE3();
				//System.out.println(t.getE3());//Status
				//System.out.println(t.getE4());
				//System.out.println(t.getE5());
				//System.out.println(t.getE6());
				//System.out.println(t.getE7());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			Results res = client.getResults(jobId);			
			resultId = res.getWorkspaceids().get(0);
			System.out.println("Result ID = " + resultId);
			assertNotNull(resultId);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] resultIdParts = resultId.split("/");
		resultId = resultIdParts[1];
//Read result from WS
		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(resultId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		TomtomRunResult result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(TomtomRunResult.class);

		assertNotNull(result.getHits().get(0).getTargetPspmId());
		assertEquals(result.getHits().get(0).getTargetPspmId(),result.getHits().get(0).getQueryPspmId());
		assertEquals(Long.valueOf("0"),result.getHits().get(0).getOptimalOffset());
		assertEquals(Double.valueOf("3.89353e-37"),result.getHits().get(0).getPvalue());
		assertEquals(Double.valueOf("7.78706e-37"),result.getHits().get(0).getEvalue());
		assertEquals(Long.valueOf("24"),result.getHits().get(0).getOverlap());
		assertEquals("TCACGCTCGTCATGACGAGCGTGA",result.getHits().get(0).getQueryConsensus());
		assertEquals("TCACGCTCGTCATGACGAGCGTGA",result.getHits().get(0).getTargetConsensus());
		assertEquals("+",result.getHits().get(0).getStrand());
	}
	
	@Test
	public final void testFindSitesWithMastByMotifCollection() throws Exception {
		URL serviceUrl = new URL(serverUrl);
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
//		System.out.println(token.toString());

		MEMEClient client = new MEMEClient(serviceUrl, token);
		client.setAuthAllowedForHttp(true);

		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(testCollectionId1).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MemePSPMCollection memePspmCollection = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MemePSPMCollection.class);


		MastRunParameters mastParams = new MastRunParameters().withMt(0.0005D).withPspmId(testMemePspmId);

		MastRunResult result = client.findSitesWithMastByCollection(memePspmCollection, testSequenceSet, mastParams);
		assertNotNull(result);
		assertFalse(result.getHits().size() == 0);
		assertEquals("kb|sequence.112", result.getHits().get(0).getSeqId());
		assertEquals("+", result.getHits().get(0).getStrand());
		assertEquals("1", result.getHits().get(0).getPspmId());
		assertEquals(Long.valueOf("122"), result.getHits().get(0).getHitStart());
		assertEquals(Long.valueOf("145"), result.getHits().get(0).getHitEnd());
		assertEquals(Double.valueOf("2594.71"), result.getHits().get(0).getScore());
		assertEquals(Double.valueOf("5.82E-10"), result.getHits().get(0).getHitPvalue());
	}

	@Test
	public final void testFindSitesWithMastByMotifCollectionFromWs() throws Exception {
		URL serviceUrl = new URL(serverUrl);
		MEMEClient client = new MEMEClient(serviceUrl, USER_NAME, PASSWORD);
		client.setAuthAllowedForHttp(true);

		MastRunParameters mastParams = new MastRunParameters().withMt(0.0005D).withQueryRef(TEST_WORKSPACE + "/" + testCollectionId1). withTargetRef(TEST_WORKSPACE + "/" + testSequenceSetId);
		
		String resultId = client.findSitesWithMastByCollectionFromWs(TEST_WORKSPACE, mastParams);
		System.out.println(resultId);

		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		
		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(resultId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MastRunResult result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MastRunResult.class);

		assertNotNull(result);
		assertFalse(result.getHits().size() == 0);
		assertEquals("kb|sequence.112", result.getHits().get(0).getSeqId());
		assertEquals("+", result.getHits().get(0).getStrand());
		assertEquals("1", result.getHits().get(0).getPspmId());
		assertEquals(Long.valueOf("122"), result.getHits().get(0).getHitStart());
		assertEquals(Long.valueOf("145"), result.getHits().get(0).getHitEnd());
		assertEquals(Double.valueOf("2594.71"), result.getHits().get(0).getScore());
		assertEquals(Double.valueOf("5.82E-10"), result.getHits().get(0).getHitPvalue());
	}

	@Test
	public final void testFindSitesWithMastJobByMotifCollectionFromWs() throws Exception {
		String resultId = null;
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		URL serviceUrl = new URL(serverUrl);
		MEMEClient clientMeme = new MEMEClient(serviceUrl, token);
		clientMeme.setAuthAllowedForHttp(true);
		MastRunParameters mastParams = new MastRunParameters().withMt(0.0005D).withPspmId(testMemePspmId).withQueryRef(TEST_WORKSPACE + "/" + testCollectionId1). withTargetRef(TEST_WORKSPACE + "/" + testSequenceSetId);

		String jobId = clientMeme.findSitesWithMastJobByCollectionFromWs(TEST_WORKSPACE, mastParams);
	
		System.out.println("Job ID = " + jobId);
		assertNotNull(jobId);
		
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;

		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client = new UserAndJobStateClient(jobServiceUrl, token);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);
		
		String status = "";
		
		while (!status.equalsIgnoreCase("finished")){
			
			try {
			    Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			try {
				Tuple7<String,String,String,Long,String,Long,Long> t = client.getJobStatus(jobId); 
				//System.out.println(t.getE1());
				//System.out.println(t.getE2());
				status = t.getE3();
				//System.out.println(t.getE3());//Status
				//System.out.println(t.getE4());
				//System.out.println(t.getE5());
				//System.out.println(t.getE6());
				//System.out.println(t.getE7());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			Results res = client.getResults(jobId);			
			resultId = res.getWorkspaceids().get(0);
			System.out.println("Result ID = " + resultId);
			assertNotNull(resultId);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] resultIdParts = resultId.split("/");
		resultId = resultIdParts[1];
//Read result from WS
		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(resultId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MastRunResult result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MastRunResult.class);
		
		assertNotNull(result);
		assertFalse(result.getHits().size() == 0);
		assertEquals("kb|sequence.112", result.getHits().get(0).getSeqId());
		assertEquals("+", result.getHits().get(0).getStrand());
		assertEquals("1", result.getHits().get(0).getPspmId());
		assertEquals(Long.valueOf("122"), result.getHits().get(0).getHitStart());
		assertEquals(Long.valueOf("145"), result.getHits().get(0).getHitEnd());
		assertEquals(Double.valueOf("2594.71"), result.getHits().get(0).getScore());
		assertEquals(Double.valueOf("5.82E-10"), result.getHits().get(0).getHitPvalue());
	}

	@Test
	public final void testFindSitesWithMastJobByWholeMotifCollectionFromWs() throws Exception {
		String resultId = null;
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		URL serviceUrl = new URL(serverUrl);
		MEMEClient clientMeme = new MEMEClient(serviceUrl, token);
		clientMeme.setAuthAllowedForHttp(true);
		MastRunParameters mastParams = new MastRunParameters().withMt(0.0005D).withQueryRef(TEST_WORKSPACE + "/" + testCollectionId1). withTargetRef(TEST_WORKSPACE + "/" + testSequenceSetId);

		String jobId = clientMeme.findSitesWithMastJobByCollectionFromWs(TEST_WORKSPACE, mastParams);
	
		System.out.println("Job ID = " + jobId);
		assertNotNull(jobId);
		
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;

		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client = new UserAndJobStateClient(jobServiceUrl, token);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);
		
		String status = "";
		
		while (!status.equalsIgnoreCase("finished")){
			
			try {
			    Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			try {
				Tuple7<String,String,String,Long,String,Long,Long> t = client.getJobStatus(jobId); 
				//System.out.println(t.getE1());
				//System.out.println(t.getE2());
				status = t.getE3();
				//System.out.println(t.getE3());//Status
				//System.out.println(t.getE4());
				//System.out.println(t.getE5());
				//System.out.println(t.getE6());
				//System.out.println(t.getE7());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			Results res = client.getResults(jobId);			
			resultId = res.getWorkspaceids().get(0);
			System.out.println("Result ID = " + resultId);
			assertNotNull(resultId);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] resultIdParts = resultId.split("/");
		resultId = resultIdParts[1];
//Read result from WS
		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(resultId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MastRunResult result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MastRunResult.class);
		
		assertNotNull(result);
		assertFalse(result.getHits().size() == 0);
		assertEquals("kb|sequence.112", result.getHits().get(0).getSeqId());
		assertEquals("+", result.getHits().get(0).getStrand());
		assertEquals("2", result.getHits().get(0).getPspmId());
		assertEquals(Long.valueOf("65"), result.getHits().get(0).getHitStart());
		assertEquals(Long.valueOf("78"), result.getHits().get(0).getHitEnd());
		assertEquals(Double.valueOf("1416.26"), result.getHits().get(0).getScore());
		assertEquals(Double.valueOf("1.01E-5"), result.getHits().get(0).getHitPvalue());
	}

	@Test
	public final void testFindSitesWithMast() throws Exception {
		URL serviceUrl = new URL(serverUrl);
		MEMEClient client = new MEMEClient(serviceUrl, USER_NAME, PASSWORD);
		client.setAuthAllowedForHttp(true);

		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();

		MastRunParameters mastParams = new MastRunParameters().withMt(0.0005D).withPspmId(testMemePspmId);

		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(testCollectionId1).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MemePSPMCollection memePspmCollection = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MemePSPMCollection.class);

		MemePSPM pspm = memePspmCollection.getPspms().get(0);

		MastRunResult result = client.findSitesWithMast(pspm, testSequenceSet, mastParams);
		assertNotNull(result);
		assertFalse(result.getHits().size() == 0);
		assertEquals("kb|sequence.112", result.getHits().get(0).getSeqId());
		assertEquals("+", result.getHits().get(0).getStrand());
		assertEquals("1", result.getHits().get(0).getPspmId());
		assertEquals(Long.valueOf("122"), result.getHits().get(0).getHitStart());
		assertEquals(Long.valueOf("145"), result.getHits().get(0).getHitEnd());
		assertEquals(Double.valueOf("2594.71"), result.getHits().get(0).getScore());
		assertEquals(Double.valueOf("0.000000000582"), result.getHits().get(0).getHitPvalue());
	}
	
/*	@Test
	public final void testFindSitesWithMastFromWs() throws Exception {
		URL serviceUrl = new URL(serverUrl);
		MEMEClient client = new MEMEClient(serviceUrl, USER_NAME, PASSWORD);
		client.setAuthAllowedForHttp(true);

		String resultId = client.findSitesWithMastFromWs(TEST_WORKSPACE, testPspmId, testSequenceSetId, 0.0005);
		
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		
		
		GetObjectParams objectParams = new GetObjectParams().withType("MastRunResult").withId(resultId).withWorkspace(TEST_WORKSPACE).withAuth(token.toString());   
		GetObjectOutput output = MemeServerImpl.wsClient(token.toString()).getObject(objectParams);
		MastRunResult result = UObject.transformObjectToObject(output.getData(), MastRunResult.class);

		
		assertNotNull(result);
		assertFalse(result.getHits().size() == 0);
		assertEquals("209110", result.getHits().get(0).getSequenceId());
		assertEquals("+", result.getHits().get(0).getStrand());
		assertEquals("1", result.getHits().get(0).getPspmId());
		assertEquals(Long.valueOf("122"), result.getHits().get(0).getHitStart());
		assertEquals(Long.valueOf("145"), result.getHits().get(0).getHitEnd());
		assertEquals(Double.valueOf("2594.71"), result.getHits().get(0).getScore());
		assertEquals(Double.valueOf("0.000000000582"), result.getHits().get(0).getHitPvalue());
	}

	@Test
	public final void testFindSitesWithMastJobFromWs() throws Exception {
		String resultId = null;
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		URL serviceUrl = new URL(serverUrl);
		MEMEClient clientMeme = new MEMEClient(serviceUrl, token);
		clientMeme.setAuthAllowedForHttp(true);
		
		String jobId = clientMeme.findSitesWithMastJobFromWs(TEST_WORKSPACE, testPspmId, testSequenceSetId, 0.0005);
	
		System.out.println("Job ID = " + jobId);
		assertNotNull(jobId);
		
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;

		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client = new UserAndJobStateClient(jobServiceUrl, token);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);
		
		String status = "";
		
		while (!status.equalsIgnoreCase("finished")){
			
			try {
			    Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			try {
				Tuple7<String,String,String,Long,String,Long,Long> t = client.getJobStatus(jobId); 
				//System.out.println(t.getE1());
				//System.out.println(t.getE2());
				status = t.getE3();
				//System.out.println(t.getE3());//Status
				//System.out.println(t.getE4());
				//System.out.println(t.getE5());
				//System.out.println(t.getE6());
				//System.out.println(t.getE7());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			Results res = client.getResults(jobId);			
			resultId = res.getWorkspaceids().get(0);
			System.out.println("Result ID = " + resultId);
			assertNotNull(resultId);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] resultIdParts = resultId.split("/");
		resultId = resultIdParts[1];
//Read result from WS

		GetObjectParams objectParams = new GetObjectParams().withType("MastRunResult").withId(resultId).withWorkspace(TEST_WORKSPACE).withAuth(token.toString());   
		GetObjectOutput output = MemeServerImpl.wsClient(token.toString()).getObject(objectParams);
		MastRunResult result = UObject.transformObjectToObject(output.getData(), MastRunResult.class);

		
		assertNotNull(result);
		assertFalse(result.getHits().size() == 0);
		assertEquals("209110", result.getHits().get(0).getSequenceId());
		assertEquals("+", result.getHits().get(0).getStrand());
		assertEquals("1", result.getHits().get(0).getPspmId());
		assertEquals(Long.valueOf("122"), result.getHits().get(0).getHitStart());
		assertEquals(Long.valueOf("145"), result.getHits().get(0).getHitEnd());
		assertEquals(Double.valueOf("2594.71"), result.getHits().get(0).getScore());
		assertEquals(Double.valueOf("0.000000000582"), result.getHits().get(0).getHitPvalue());
	}
*/

	@Test
	public final void testGetPspmCollectionFromMemeResult() throws Exception {
		URL serviceUrl = new URL(serverUrl);
		MEMEClient client = new MEMEClient(serviceUrl, USER_NAME, PASSWORD);
		client.setAuthAllowedForHttp(true);

		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		
/*		List<ObjectIdentity> objectIds = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withWorkspace("AKtest").withName(id);
		objectIds.add(objectIdentity);
		List<ObjectData> output = MemeServerImpl.wsClient(token.getTokenData()).getObjects(objectIds);
		
		MemeRunResult input = UObject.transformObjectToObject(output.get(0).getData(), MemeRunResult.class);
*/
		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(testMemeRunResultId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MemeRunResult input = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MemeRunResult.class);

		MemePSPMCollection result = client.getPspmCollectionFromMemeResult(input);
		assertNotNull(result);
		assertNotNull(result.getId());
		assertFalse(result.getPspms().size() == 0);
		assertEquals("ACGT", result.getAlphabet());
	}

	@Test
	public final void testGetPspmCollectionFromMemeResultFromWs() throws Exception {
		URL serviceUrl = new URL(serverUrl);
		MEMEClient client = new MEMEClient(serviceUrl, USER_NAME, PASSWORD);
		client.setAuthAllowedForHttp(true);
		
		String resultId = client.getPspmCollectionFromMemeResultFromWs(TEST_WORKSPACE, TEST_WORKSPACE + "/" + testMemeRunResultId);
		System.out.println(resultId);

		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(resultId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MemePSPMCollection result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MemePSPMCollection.class);
		
		assertNotNull(result);
		assertFalse(result.getPspms().size() == 0);
		assertEquals("ACGT", result.getAlphabet());
	}
	
	@Test
	public final void testGetPspmCollectionFromMemeResultJobFromWs() throws Exception {
		String resultId = null;
		URL serviceUrl = new URL(serverUrl);
		MEMEClient clientMeme = new MEMEClient(serviceUrl, USER_NAME, PASSWORD);
		clientMeme.setAuthAllowedForHttp(true);
		
		String jobId = clientMeme.getPspmCollectionFromMemeResultJobFromWs(TEST_WORKSPACE, TEST_WORKSPACE + "/" + testMemeRunResultId);

		
		System.out.println("Job ID = " + jobId);
		assertNotNull(jobId);
		
		URL jobServiceUrl = null;
		UserAndJobStateClient client = null;

		try {
			jobServiceUrl = new URL(JOB_SERVICE);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client = new UserAndJobStateClient(jobServiceUrl, token);
		} catch (UnauthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setAuthAllowedForHttp(true);
		
		String status = "";
		
		while (!status.equalsIgnoreCase("finished")){
			
			try {
			    Thread.sleep(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			try {
				Tuple7<String,String,String,Long,String,Long,Long> t = client.getJobStatus(jobId); 
				//System.out.println(t.getE1());
				//System.out.println(t.getE2());
				status = t.getE3();
				//System.out.println(t.getE3());//Status
				//System.out.println(t.getE4());
				//System.out.println(t.getE5());
				//System.out.println(t.getE6());
				//System.out.println(t.getE7());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			Results res = client.getResults(jobId);			
			resultId = res.getWorkspaceids().get(0);
			System.out.println("Result ID = " + resultId);
			assertNotNull(resultId);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] resultIdParts = resultId.split("/");
		resultId = resultIdParts[1];

		//Read result from WS
		WorkspaceClient wsClient = new WorkspaceClient(new URL (WS_SERVICE), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(resultId).withWorkspace(TEST_WORKSPACE);
		objectsIdentity.add(objectIdentity);
		MemePSPMCollection result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(MemePSPMCollection.class);

		assertNotNull(result);
		assertFalse(result.getPspms().size() == 0);
		assertEquals("ACGT", result.getAlphabet());
	}

}
